package com.inilabs.jaer.projects.eventprocessing.filters;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.graphics.FrameAnnotater;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * Bean-style tracker filter for small targets.
 * Greedy nearest-neighbor association with alpha–beta updates.
 */
@Description("SmallTargetTrackerFilter: greedy CV tracker for residual small targets.")
@DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class SmallTargetTrackerFilter extends EventFilter2DMouseAdaptor
        implements FrameAnnotater, Observer, PropertyChangeListener {

    public static class Track {
        public int id;
        public double x;
        public double y;
        public double vx;
        public double vy;
        public long lastUpdate;
        public int hits;
        public int misses;
        public double score;
    }

    private int nextId = 1;
    private final List<Track> tracks = new ArrayList<>();

    private double gatePx = 25.0;
    private double alpha = 0.5;
    private double beta = 0.2;
    private int maxMissMs = 500;
    private double arrowScale = 0.05;

    private boolean annotationEnabled = true;

    public SmallTargetTrackerFilter(AEChip chip) {
        super(chip);
    }

//    @Override
    public String getFilterName() {
        return "SmallTargetTrackerFilter";
    }

    @Override
    public void initFilter() {
        resetFilter();
    }

    @Override
    public synchronized void resetFilter() {
        tracks.clear();
        nextId = 1;
    }

    @Override
    public synchronized EventPacket<?> filterPacket(EventPacket<?> in) {
        return in;
    }

    public synchronized void update(List<ResidualSmallTargetDetector.Detection> dets,
                                    long frameTsUs,
                                    int sx,
                                    int sy) {

        final double dt = predict(frameTsUs);

        boolean[] used = new boolean[dets.size()];
        for (Track tr : tracks) {
            int best = -1;
            double bestCost = gatePx * gatePx;
            for (int i = 0; i < dets.size(); i++) {
                if (used[i]) continue;
                ResidualSmallTargetDetector.Detection d = dets.get(i);
                double dx = d.x - tr.x;
                double dy = d.y - tr.y;
                double dist2 = dx * dx + dy * dy;
                if (dist2 < bestCost) {
                    bestCost = dist2;
                    best = i;
                }
            }
            if (best >= 0) {
                used[best] = true;
                ResidualSmallTargetDetector.Detection d = dets.get(best);
                double rx = d.x - tr.x;
                double ry = d.y - tr.y;
                tr.x += alpha * rx;
                tr.y += alpha * ry;
                tr.vx += beta * rx / Math.max(dt, 1e-3);
                tr.vy += beta * ry / Math.max(dt, 1e-3);
                tr.lastUpdate = frameTsUs;
                tr.hits++;
                tr.misses = 0;
                tr.score = 0.8 * tr.score + 0.2 * d.strength;
            } else {
                tr.misses++;
            }
        }

        for (int i = 0; i < dets.size(); i++) {
            if (used[i]) continue;
            ResidualSmallTargetDetector.Detection d = dets.get(i);
            Track tr = new Track();
            tr.id = nextId++;
            tr.x = d.x;
            tr.y = d.y;
            tr.vx = d.vx;
            tr.vy = d.vy;
            tr.lastUpdate = frameTsUs;
            tr.hits = 1;
            tr.misses = 0;
            tr.score = d.strength;
            tracks.add(tr);
        }

        tracks.removeIf(tr ->
            (tr.misses * 20 > maxMissMs) ||
            tr.x < -10 || tr.y < -10 ||
            tr.x > sx + 10 || tr.y > sy + 10
        );
    }

    private double predict(long nowUs) {
        double dtMax = 0.0;
        for (Track tr : tracks) {
            if (tr.lastUpdate > 0) {
                double dt = (nowUs - tr.lastUpdate) * 1e-6;
                dtMax = Math.max(dtMax, dt);
                tr.x += tr.vx * dt;
                tr.y += tr.vy * dt;
            }
        }
        return Math.max(dtMax, 1e-3);
    }

    public synchronized List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    @Override
    public void annotate(GLAutoDrawable drawable) {
        // Optional: GL overlay for tracker state
    }

    @Override
    public boolean isAnnotationEnabled() {
        return annotationEnabled;
    }

    @Override
    public void setAnnotationEnabled(boolean annotationEnabled) {
        this.annotationEnabled = annotationEnabled;
    }

    @Override
    public void update(Observable o, Object arg) { }

    @Override
    public void propertyChange(PropertyChangeEvent evt) { }

    public double getGatePx() { return gatePx; }
    public void setGatePx(double gatePx) { this.gatePx = gatePx; }
    public double getAlpha() { return alpha; }
    public void setAlpha(double alpha) { this.alpha = alpha; }
    public double getBeta() { return beta; }
    public void setBeta(double beta) { this.beta = beta; }
    public int getMaxMissMs() { return maxMissMs; }
    public void setMaxMissMs(int maxMissMs) { this.maxMissMs = maxMissMs; }
    public double getArrowScale() { return arrowScale; }
    public void setArrowScale(double arrowScale) { this.arrowScale = arrowScale; }
}
