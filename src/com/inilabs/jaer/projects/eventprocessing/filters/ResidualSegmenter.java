package com.inilabs.jaer.projects.eventprocessing.filters;

import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.*;
import net.sf.jaer.eventprocessing.EventFilter2D;
import java.util.*;

/**
 * ResidualSegmenter (API-robust)
 *
 * - Polarity-agnostic residual gating using tile-wise contrast gain.
 * - Builds output packets using jAER's OutputEventIterator when available,
 *   otherwise falls back to addEvent/add via reflection.
 */
public class ResidualSegmenter extends EventFilter2D {

    // --- Camera intrinsics (should match those in EgomotionCompensator) ---
    private float fx = 300f, fy = 300f, cx = 160f, cy = 120f;

    // --- Window length in milliseconds ---
    private int windowMs = 20;

    // --- Tile size in pixels and thresholds ---
    private int tile = 16;
    private float gainThreshold = 1.3f; // tiles with < gain are IMOs

    // --- Internal buffers ---
    private int W = 320, H = 240;
    private int[][] accRaw, accComp;

    // Simple ring buffer of lightweight events (x,y,ts) for windowing
    private static class E { short x,y; int ts; E(short x, short y, int ts){ this.x=x; this.y=y; this.ts=ts; } }
    private Deque<E> ring = new ArrayDeque<>();

    public ResidualSegmenter(AEChip chip) {
        super(chip);
        if (chip != null) {
            W = chip.getSizeX();
            H = chip.getSizeY();
            cx = (W - 1) * 0.5f;
            cy = (H - 1) * 0.5f;
        }
        accRaw  = new int[H][W];
        accComp = new int[H][W];
    }

    // GUI properties
    public float getFx() { return fx; }
    public void setFx(float v) { fx = v; }
    public float getFy() { return fy; }
    public void setFy(float v) { fy = v; }
    public float getCx() { return cx; }
    public void setCx(float v) { cx = v; }
    public float getCy() { return cy; }
    public void setCy(float v) { cy = v; }

    public int getWindowMs() { return windowMs; }
    public void setWindowMs(int v) { windowMs = Math.max(5, v); }

    public int getTile() { return tile; }
    public void setTile(int v) { tile = Math.max(8, v); }

    public float getGainThreshold() { return gainThreshold; }
    public void setGainThreshold(float v) { gainThreshold = Math.max(1.0f, v); }

    @Override
    public EventPacket<?> filterPacket(EventPacket<?> in) {
        if (in == null || in.isEmpty()) return in;

        if (getChip() != null) {
            W = getChip().getSizeX();
            H = getChip().getSizeY();
        }

        // read rotation from compensator
        float rx = EgomotionCompensator.RotationProvider.getRx();
        float ry = EgomotionCompensator.RotationProvider.getRy();
        float rz = EgomotionCompensator.RotationProvider.getRz();

        // Process events: update ring and accumulators
        int now = ((BasicEvent)in.getLastEvent()).timestamp;
        int cutoff = now - msToTimestamp(windowMs);

        for (Object o : in) {
            if (!(o instanceof PolarityEvent)) continue;
            PolarityEvent e = (PolarityEvent) o;
            ring.addLast(new E(e.x, e.y, e.timestamp));

            // RAW accumulator
            if (e.y>=0 && e.y<H && e.x>=0 && e.x<W) accRaw[e.y][e.x] += 1;

            // COMPENS accumulator with rotation warp
            float[] uv = warpRotationOnly(e.x, e.y, rx, ry, rz);
            int u = Math.round(uv[0]);
            int v = Math.round(uv[1]);
            if (u >= 0 && u < W && v >= 0 && v < H) {
                accComp[v][u] += 1;
            }
        }

        // Purge old events from ring and decrement their contributions
        while (!ring.isEmpty()) {
            E head = ring.peekFirst();
            if (head == null) break;
            if (head.ts >= cutoff) break;
            ring.removeFirst();

            if (head.y>=0 && head.y<H && head.x>=0 && head.x<W) {
                accRaw[head.y][head.x] = Math.max(0, accRaw[head.y][head.x] - 1);
                float[] uv = warpRotationOnly(head.x, head.y, rx, ry, rz);
                int u = Math.round(uv[0]);
                int v = Math.round(uv[1]);
                if (u >= 0 && u < W && v >= 0 && v < H) {
                    accComp[v][u] = Math.max(0, accComp[v][u] - 1);
                }
            }
        }

        // Compute tile flags (IMO tiles)
        boolean[][] imoTile = computeImoTiles();

        // Build output packet robustly across jAER API versions
        EventPacket<PolarityEvent> out = new EventPacket<>(PolarityEvent.class);
        Object outIter = null;
        java.lang.reflect.Method nextOutput = null;
        try {
            java.lang.reflect.Method outputIterator = out.getClass().getMethod("outputIterator");
            outIter = outputIterator.invoke(out);
            nextOutput = outIter.getClass().getMethod("nextOutput");
        } catch (Throwable ignored) {}

        java.lang.reflect.Method addEvent = null, add = null;
        if (nextOutput == null) {
            try { addEvent = out.getClass().getMethod("addEvent", BasicEvent.class); } catch (Throwable ignored) {}
            if (addEvent == null) {
                try { add = out.getClass().getMethod("add", Object.class); } catch (Throwable ignored) {}
            }
        }

        for (Object o : in) {
            if (!(o instanceof PolarityEvent)) continue;
            PolarityEvent e = (PolarityEvent) o;
            int tx = Math.min((W-1), Math.max(0, e.x)) / tile;
            int ty = Math.min((H-1), Math.max(0, e.y)) / tile;
            boolean keep = (ty>=0 && ty<imoTile.length && tx>=0 && tx<imoTile[0].length) && imoTile[ty][tx];
            if (!keep) continue;

            if (nextOutput != null) {
                try {
                    Object be = nextOutput.invoke(outIter);
                    if (be instanceof BasicEvent) {
                        BasicEvent bo = (BasicEvent) be;
                        bo.copyFrom(e);
                    }
                } catch (Throwable ignored) {}
            } else if (addEvent != null) {
                try { addEvent.invoke(out, e); } catch (Throwable ignored) {}
            } else if (add != null) {
                try { add.invoke(out, e); } catch (Throwable ignored) {}
            } else {
                // As a last resort, return the input packet unfiltered
                return in;
            }
        }
        return out;
    }

    private boolean[][] computeImoTiles() {
        int tilesX = (W + tile - 1) / tile;
        int tilesY = (H + tile - 1) / tile;
        boolean[][] imo = new boolean[tilesY][tilesX];

        for (int ty = 0; ty < tilesY; ty++) {
            for (int tx = 0; tx < tilesX; tx++) {
                int x0 = tx * tile;
                int y0 = ty * tile;
                int x1 = Math.min(x0 + tile, W);
                int y1 = Math.min(y0 + tile, H);

                // raw variance
                double sumR=0, sumR2=0; int n=0;
                for (int y = y0; y < y1; y++) {
                    for (int x = x0; x < x1; x++) {
                        int v = accRaw[y][x];
                        sumR  += v;
                        sumR2 += v*v;
                        n++;
                    }
                }
                double varR = (n>0) ? (sumR2/n - (sumR/n)*(sumR/n)) : 0;

                // compensated variance
                double sumC=0, sumC2=0; n=0;
                for (int y = y0; y < y1; y++) {
                    for (int x = x0; x < x1; x++) {
                        int v = accComp[y][x];
                        sumC  += v;
                        sumC2 += v*v;
                        n++;
                    }
                }
                double varC = (n>0) ? (sumC2/n - (sumC/n)*(sumC/n)) : 0;

                double gain = (varR <= 1e-6) ? 0.0 : (varC / (varR + 1e-6));
                imo[ty][tx] = (gain < gainThreshold);
            }
        }
        return imo;
    }

    private int msToTimestamp(int ms) {
        return ms * 1000; // jAER typically uses microsecond ticks
    }

    // Rotation-only warp (match EgomotionCompensator math)
    private float[] warpRotationOnly(int x, int y, float rx, float ry, float rz) {
        float X = (x - cx) / fx;
        float Y = (y - cy) / fy;
        float Z = 1f;

        float theta = (float) Math.sqrt(rx*rx + ry*ry + rz*rz);
        float s, c, k;
        if (theta < 1e-6f) {
            s = 1f; c = 1f; k = 0.5f;
        } else {
            s = (float) Math.sin(theta) / theta;
            c = (float) Math.cos(theta);
            k = (1f - c) / (theta*theta);
        }
        float vx = X, vy = Y, vz = Z;
        float cxv_x =  ry*vz - rz*vy;
        float cxv_y =  rz*vx - rx*vz;
        float cxv_z =  rx*vy - ry*vx;
        float rdotv = rx*vx + ry*vy + rz*vz;
        float r2 = rx*rx + ry*ry + rz*rz;
        float c2_x = rx*rdotv - vx*r2;
        float c2_y = ry*rdotv - vy*r2;
        float c2_z = rz*rdotv - vz*r2;
        float Xp = vx + s*cxv_x + k*c2_x;
        float Yp = vy + s*cxv_y + k*c2_y;
        float Zp = vz + s*cxv_z + k*c2_z;

        float u = fx * (Xp / Zp) + cx;
        float v = fy * (Yp / Zp) + cy;
        return new float[]{u, v};
    }

    @Override
    public void resetFilter() {
        accRaw  = new int[H][W];
        accComp = new int[H][W];
        ring.clear();
    }

    @Override
    public void initFilter() {
        // no-op
    }
}
