package com.inilabs.jaer.projects.eventprocessing.filters;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.*;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.aemonitor.AEPacketRaw;

import java.util.Arrays;

import com.inilabs.jaer.projects.dvxplorer.imu.IMUSample;
import com.inilabs.jaer.projects.dvxplorer.imu.IMUSampleType;

/**
 * EgomotionCompensator — rotation-only stabilization for DVXplorer.
 *
 * Uses **Bosch BMI160** IMU samples via {@link IMUSample} parsed from {@link AEPacketRaw}
 * to integrate camera rotation at microsecond resolution. Events are then warped
 * to a reference time using the current rotation estimate.
 */
@Description("DVXplorer Steadicam: compensates global rotation using built-in Bosch BMI160 IMU.")
@DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class EgomotionCompensator extends EventFilter2DMouseAdaptor {

    // -------- Camera intrinsics (set from calibration) --------
    private float fx = 300f, fy = 300f, cx = 160f, cy = 120f; // default for ~320x240; adjust to your chip

    // -------- Time-surface decay (ms) --------
    private float tauMs = 20f;

    // -------- Reference time (initialized on first packet) --------
    private int t0 = 0;

    // -------- Rotation state (Rodrigues vector, radians) --------
    private float rx = 0, ry = 0, rz = 0;

    // -------- IMU control --------
    private boolean useImu = true; // enable IMU-based integration
    private static final float DEG2RAD = (float) (Math.PI / 180.0);

    // Keep partially read IMU sample across raw packets
    private IMUSample.IncompleteIMUSampleException pendingImu = null;

    // -------- Time surface buffers --------
    private int W = 320, H = 240; // set from chip when available
    private float[][] tsOn, tsOff;

    // -------- Optional: mini-batch contrast refinement --------
    private int refineEveryN = 2000;
    private int sinceRefine = 0;

    public EgomotionCompensator(AEChip chip) {
        super(chip);
        if (chip != null) {
            W = chip.getSizeX();
            H = chip.getSizeY();
            cx = (W - 1) * 0.5f;
            cy = (H - 1) * 0.5f;
        }
        tsOn = new float[H][W];
        tsOff = new float[H][W];
    }

    // -------- Properties (exposed in jAER GUI) --------
    public float getFx() { return fx; }
    public void setFx(float v) { fx = v; }
    public float getFy() { return fy; }
    public void setFy(float v) { fy = v; }
    public float getCx() { return cx; }
    public void setCx(float v) { cx = v; }
    public float getCy() { return cy; }
    public void setCy(float v) { cy = v; }

    public float getTauMs() { return tauMs; }
    public void setTauMs(float v) { tauMs = Math.max(1f, v); }

    public boolean isUseImu() { return useImu; }
    public void setUseImu(boolean v) { useImu = v; }

    public int getRefineEveryN() { return refineEveryN; }
    public void setRefineEveryN(int v) { refineEveryN = Math.max(200, v); }

    public float getRx() { return rx; }
    public float getRy() { return ry; }
    public float getRz() { return rz; }

    // =====================================================================================
    // RAW PACKET PATH — parse BMI160 IMU samples and integrate rotation in radians
    // =====================================================================================
   // @Override
    public AEPacketRaw filterPacket(AEPacketRaw in) {
        if (!useImu || in == null) return in;

        // iterate over raw addresses; look for the start of an IMU sample (ax)
        final int n = (in.addresses != null) ? in.addresses.length : 0;
        int i = 0;
        while (i < n) {
            try {
                // Only try to parse when the code matches the first IMU field (ax)
                int code = IMUSample.extractSampleTypeCode(in.addresses[i]);
                if (code != IMUSampleType.ax.code) {
                    i++;
                    continue;
                }
                IMUSample s = IMUSample.constructFromAEPacketRaw(in, i, pendingImu);
                pendingImu = null; // fully parsed sample

                // Integrate rotation using the sample's dt (us → s)
                float dt = Math.max(0f, s.getDeltaTimeUs() * 1e-6f);
                if (dt == 0f) { // fallback if dt not available
                    // Estimate dt from successive timestamps (us) if needed
                    // Not strictly necessary; we can skip integration for zero dt.
                }
                // BMI160 getters return deg/s; convert to rad/s
                float gx = s.getGyroTiltX() * DEG2RAD;
                float gy = s.getGyroYawY()  * DEG2RAD;
                float gz = s.getGyroRollZ() * DEG2RAD;

                rx += gx * dt;
                ry += gy * dt;
                rz += gz * dt;

                // Advance index by the size of a complete IMU sample in events
                i += IMUSample.SIZE_EVENTS;
            } catch (IMUSample.IncompleteIMUSampleException ex) {
                // Hold across packets until completed
                pendingImu = ex;
                break; // need more raw events next call
            } catch (IMUSample.BadIMUDataException bad) {
                // Skip this location and continue
                i++;
            } catch (Throwable t) {
                // Any unexpected issue: move on to avoid blocking the stream
                i++;
            }
        }
        // Publish rotation for downstream 2D packet path
        RotationProvider.update(rx, ry, rz);
        return in;
    }

    // =====================================================================================
    // 2D EVENT PACKET PATH — warp events using the current rotation
    // =====================================================================================
    @Override
    public EventPacket<?> filterPacket(EventPacket<?> in) {
        if (in == null || in.isEmpty()) return in;
        if (t0 == 0) t0 = ((BasicEvent) in.getFirstEvent()).timestamp;

        // Update W/H from chip dynamically (hot swaps)
        if (getChip() != null) {
            W = getChip().getSizeX();
            H = getChip().getSizeY();
        }

        // Optional: tiny refinement around yaw to maximize contrast every N events
        sinceRefine += in.getSize();
        if (sinceRefine >= refineEveryN) {
            sinceRefine = 0;
            float bestDelta = argmaxYawDelta(in, new float[]{-0.002f, 0f, 0.002f}); // radians (small)
            rz += bestDelta;
            RotationProvider.update(rx, ry, rz);
        }

        // Warp each DVS event to reference time (rotation-only)
        for (Object o : in) {
            if (!(o instanceof PolarityEvent)) continue;
            PolarityEvent e = (PolarityEvent) o;

            final int ts = e.timestamp;

            float[] uv = warpRotationOnly(e.x, e.y, ts);
            int u = Math.round(uv[0]);
            int v = Math.round(uv[1]);
            if (u < 0 || u >= W || v < 0 || v >= H) continue;

            if (isOn(e)) tsOn[v][u] = ts;
            else         tsOff[v][u] = ts;

            // Overwrite event coordinates with warped coords
            e.x = (short) u;
            e.y = (short) v;
        }

        // Publish rotation for downstream filters
        RotationProvider.update(rx, ry, rz);
        return in;
    }

    // Helper to read polarity across jAER versions (enum vs boolean) without hard dependency
    private boolean isOn(PolarityEvent e) {
        try {
            Object pol = e.getClass().getMethod("getPolarity").invoke(e);
            if (pol != null) {
                String s = pol.toString().toLowerCase();
                return s.contains("on") || s.contains("pos");
            }
        } catch (Throwable ignored) {}
        try {
            Object val = e.getClass().getField("polarity").get(e);
            if (val instanceof Boolean) return ((Boolean) val).booleanValue();
        } catch (Throwable ignored) {}
        return true;
    }

    // --------- Contrast maximization (yaw micro-refinement) ---------
    private float argmaxYawDelta(EventPacket<?> in, float[] deltas) {
        float bestDelta = 0f;
        double bestScore = -1e9;
        for (float d : deltas) {
            double s = scoreVarianceYaw(in, rz + d);
            if (s > bestScore) { bestScore = s; bestDelta = d; }
        }
        return bestDelta;
    }

    private double scoreVarianceYaw(EventPacket<?> in, float yaw) {
        int w = Math.min(W, 320);
        int h = Math.min(H, 240);
        int stride = 2; // subsample for speed
        int[][] acc = new int[h/stride][w/stride];
        int count = 0;
        for (Object o : in) {
            if (!(o instanceof PolarityEvent)) continue;
            PolarityEvent e = (PolarityEvent) o;
            float[] uv = warpRotationOnly(e.x, e.y, e.timestamp, rx, ry, yaw);
            int u = Math.round(uv[0]);
            int v = Math.round(uv[1]);
            if (u < 0 || u >= w || v < 0 || v >= h) continue;
            acc[v/stride][u/stride]++;
            count++;
        }
        if (count < 100) return -1e9;
        double sum=0, sum2=0; int n=0;
        for (int y=0; y<acc.length; y++) for (int x=0; x<acc[0].length; x++) {
            int val = acc[y][x];
            sum += val;
            sum2 += val*val;
            n++;
        }
        double mean = sum/n;
        return (sum2/n) - mean*mean;
    }

    // --------- Rotation-only warp (Rodrigues) ---------
    private float[] warpRotationOnly(int x, int y, int ts) {
        return warpRotationOnly(x, y, ts, rx, ry, rz);
    }

    private float[] warpRotationOnly(int x, int y, int ts, float rx, float ry, float rz) {
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

    // -------- Lifecycle --------
    @Override
    public void resetFilter() {
        for (int y = 0; y < H; y++) {
            Arrays.fill(tsOn[y], 0);
            Arrays.fill(tsOff[y], 0);
        }
        rx = ry = rz = 0;
        t0 = 0;
        pendingImu = null;
        RotationProvider.update(0,0,0);
    }

    @Override
    public void initFilter() {
        // no-op
    }

    /** Shares current rotation with other filters. */
    public static class RotationProvider {
        private static volatile float RX=0, RY=0, RZ=0;
        public static void update(float rx, float ry, float rz){ RX=rx; RY=ry; RZ=rz; }
        public static float getRx(){ return RX; }
        public static float getRy(){ return RY; }
        public static float getRz(){ return RZ; }
    }
}
