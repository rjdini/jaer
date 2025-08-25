package com.inilabs.jaer.projects.eventprocessing.filters;

import net.sf.jaer.chip.AEChip;
import java.util.*;

/**
 * Lightweight constant-velocity tracker with greedy assignment.
 * Tracks are kept in image-plane with simple process/measurement noise.
 */
public class SmallTargetTracker {

    public static class Track {
        public int id;
        public double x, y, vx, vy;
        public long lastUpdate;
        public int hits, misses;
        public double score;
    }

    private int nextId = 1;
    private final List<Track> tracks = new ArrayList<>();

    // Tunables
    private double sigmaProcess = 80.0;   // px/s^2 equivalent (used as gating growth)
    private double measNoisePx  = 1.5;    // measurement sigma
    private int confirmM = 2, confirmN = 3;
    private int maxMissMs = 500;
    private double gatePx = 25.0;

    public SmallTargetTracker(AEChip chip) {}

    /** Update from detector outputs for given frame time (us). */
    public synchronized void update(List<ResidualSmallTargetDetector.Detection> dets, long frameTsUs, int sx, int sy) {
        // Predict
        final double dt = predictAndDecay(frameTsUs);

        // Greedy assignment with gating
        boolean[] detUsed = new boolean[dets.size()];
        for (Track tr : tracks) {
            int best = -1;
            double bestCost = gatePx*gatePx;
            for (int i = 0; i < dets.size(); i++) {
                if (detUsed[i]) continue;
                ResidualSmallTargetDetector.Detection d = dets.get(i);
                double dx = d.x - tr.x, dy = d.y - tr.y;
                double dist2 = dx*dx + dy*dy;
                if (dist2 < bestCost) { bestCost = dist2; best = i; }
            }
            if (best >= 0) {
                detUsed[best] = true;
                ResidualSmallTargetDetector.Detection d = dets.get(best);
                // Simple alpha-beta update
                double alpha = 0.5, beta = 0.2;
                double mx = d.x, my = d.y;
                double rx = mx - tr.x, ry = my - tr.y;
                tr.x += alpha*rx; tr.y += alpha*ry;
                tr.vx += beta*rx/Math.max(dt,1e-3); tr.vy += beta*ry/Math.max(dt,1e-3);
                tr.lastUpdate = frameTsUs;
                tr.hits++; tr.misses = 0;
                tr.score = 0.8*tr.score + 0.2*(d.strength);
            } else {
                tr.misses++;
            }
        }

        // Initiate new tracks for unmatched detections
        for (int i = 0; i < dets.size(); i++) {
            if (detUsed[i]) continue;
            ResidualSmallTargetDetector.Detection d = dets.get(i);
            Track tr = new Track();
            tr.id = nextId++;
            tr.x = d.x; tr.y = d.y; tr.vx = d.vx; tr.vy = d.vy;
            tr.lastUpdate = frameTsUs;
            tr.hits = 1; tr.misses = 0;
            tr.score = d.strength;
            tracks.add(tr);
        }

        // Prune stale
        prune(frameTsUs);
    }

    private double predictAndDecay(long frameTsUs) {
        double dtMax = 0.0;
        for (Track tr : tracks) {
            if (tr.lastUpdate > 0) {
                double dtLocal = (frameTsUs - tr.lastUpdate) * 1e-6;
                dtMax = Math.max(dtMax, dtLocal);
                tr.x += tr.vx * dtLocal;
                tr.y += tr.vy * dtLocal;
            }
        }
        return Math.max(dtMax, 1e-3);
    }

    private void prune(long nowUs) {
        tracks.removeIf(tr -> {
            boolean deadByMiss = tr.misses * 20 > maxMissMs; // approx assuming ~20ms packet cadence
            boolean outOfBounds = (tr.x < -5 || tr.y < -5);
            return deadByMiss || outOfBounds;
        });
    }

    public synchronized List<Track> getTracks() { return new ArrayList<>(tracks); }

    // Beans
    public double getSigmaProcess() { return sigmaProcess; }
    public void setSigmaProcess(double s) { sigmaProcess = s; }
    public double getMeasNoisePx() { return measNoisePx; }
    public void setMeasNoisePx(double m) { measNoisePx = m; }
    public int getConfirmM() { return confirmM; }
    public void setConfirmM(int v) { confirmM = v; }
    public int getConfirmN() { return confirmN; }
    public void setConfirmN(int v) { confirmN = v; }
    public int getMaxMissMs() { return maxMissMs; }
    public void setMaxMissMs(int v) { maxMissMs = v; }
    public double getGatePx() { return gatePx; }
    public void setGatePx(double v) { gatePx = v; }
}
