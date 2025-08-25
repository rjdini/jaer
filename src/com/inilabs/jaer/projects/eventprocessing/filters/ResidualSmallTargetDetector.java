package com.inilabs.jaer.projects.eventprocessing.filters;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
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
 * Residual small-target detector: local ROI clustering + x–y–t plane-fit coherence.
 */
@Description("Residual small-target detector (cluster + x–y–t plane coherence).")
@DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class ResidualSmallTargetDetector extends EventFilter2DMouseAdaptor
        implements FrameAnnotater, Observer, PropertyChangeListener {

    private int roiPx = 15;
    private int nMin = 30;
    private int windowUs = 20_000;
    private double rmsMax = 0.6;

    private long[][] ts;
    private int sx;
    private int sy;

    private SmallTargetTrackerFilter tracker;
    private final List<Detection> lastDetections = new ArrayList<>();

    private boolean annotationEnabled = true;

    public ResidualSmallTargetDetector(AEChip chip) {
        super(chip);
        allocate();
    }

//    @Override
    public String getFilterName() {
        return "ResidualSmallTargetDetector";
    }

    @Override
    public void initFilter() {
        resetFilter();
    }

    @Override
    public synchronized void resetFilter() {
        allocate();
        lastDetections.clear();
    }

    @Override
    public synchronized EventPacket<?> filterPacket(EventPacket<?> in) {
        if (in == null || in.getSize() == 0) {
            return in;
        }

        for (Object oe : in) {
            BasicEvent e = (BasicEvent) oe;
            if (e.x >= 0 && e.y >= 0 && e.x < sx && e.y < sy) {
                ts[e.x][e.y] = e.timestamp;
            }
        }

        lastDetections.clear();
        for (Object oe : in) {
            BasicEvent e = (BasicEvent) oe;
            if (e.x < roiPx || e.y < roiPx || e.x >= sx - roiPx || e.y >= sy - roiPx) {
                continue;
            }

            List<Pt> pts = new ArrayList<>();
            final long tMin = e.timestamp - windowUs;
            for (int dx = -roiPx; dx <= roiPx; dx++) {
                int x = e.x + dx;
                for (int dy = -roiPx; dy <= roiPx; dy++) {
                    int y = e.y + dy;
                    long te = ts[x][y];
                    if (te >= tMin) {
                        pts.add(new Pt(x, y, te));
                    }
                }
            }
            if (pts.size() < nMin) {
                continue;
            }

            PlaneFit fit = PlaneFit.fit(pts);
            if (fit == null || fit.rms > rmsMax) {
                continue;
            }

            double cx = 0.0;
            double cy = 0.0;
            for (Pt p : pts) {
                cx += p.x;
                cy += p.y;
            }
            cx /= pts.size();
            cy /= pts.size();

            Detection d = new Detection(
                (float) cx,
                (float) cy,
                (float) fit.vx,
                (float) fit.vy,
                pts.size(),
                e.timestamp
            );
            lastDetections.add(d);
        }

        if (tracker != null && !lastDetections.isEmpty()) {
            tracker.update(lastDetections, in.getLastTimestamp(), sx, sy);
        }

        return in;
    }

    @Override
    public String getDescription() {
        return "Residual small-target detector (cluster + x–y–t plane coherence).";
    }

    @Override
    public void annotate(GLAutoDrawable drawable) {
        // Optional: GL overlay for detector state
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
    public void update(Observable o, Object arg) {
        // no-op
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // no-op
    }

    private void allocate() {
        sx = chip.getSizeX();
        sy = chip.getSizeY();
        ts = new long[sx][sy];
    }

    public static class Detection {
        public final float x;
        public final float y;
        public final float vx;
        public final float vy;
        public final int strength;
        public final long timestamp;

        public Detection(float x, float y, float vx, float vy, int strength, long ts) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.strength = strength;
            this.timestamp = ts;
        }
    }

    private static class Pt {
        final int x;
        final int y;
        final long t;

        Pt(int x, int y, long t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }
    }

    private static class PlaneFit {
        final double a;
        final double b;
        final double c;
        final double rms;
        final double vx;
        final double vy;

        PlaneFit(double a, double b, double c, double rms) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.rms = rms;
            this.vx = (Math.abs(a) > 1e-6) ? 1.0 / a : 1e6;
            this.vy = (Math.abs(b) > 1e-6) ? 1.0 / b : 1e6;
        }

        static PlaneFit fit(List<Pt> pts) {
            double Sx = 0, Sy = 0, St = 0;
            double Sxx = 0, Syy = 0, Sxy = 0;
            double Sxt = 0, Syt = 0;
            int n = pts.size();

            for (Pt p : pts) {
                double x = p.x;
                double y = p.y;
                double t = p.t;
                Sx += x;
                Sy += y;
                St += t;
                Sxx += x * x;
                Syy += y * y;
                Sxy += x * y;
                Sxt += x * t;
                Syt += y * t;
            }

            double[][] A = new double[][]{
                {Sxx, Sxy, Sx},
                {Sxy, Syy, Sy},
                {Sx,  Sy,  n }
            };
            double[] b = new double[]{Sxt, Syt, St};
            double[] x = solveSym3(A, b);
            if (x == null) {
                return null;
            }

            double err = 0.0;
            for (Pt p : pts) {
                double pred = x[0] * p.x + x[1] * p.y + x[2];
                double r = (p.t - pred);
                err += r * r;
            }
            double rms = Math.sqrt(err / Math.max(1, n - 3));
            return new PlaneFit(x[0], x[1], x[2], rms);
        }

        private static double[] solveSym3(double[][] A, double[] b) {
            double[][] M = new double[3][4];
            for (int i = 0; i < 3; i++) {
                M[i][0] = A[i][0];
                M[i][1] = A[i][1];
                M[i][2] = A[i][2];
                M[i][3] = b[i];
            }
            for (int p = 0; p < 3; p++) {
                int max = p;
                for (int i = p + 1; i < 3; i++) {
                    if (Math.abs(M[i][p]) > Math.abs(M[max][p])) {
                        max = i;
                    }
                }
                double[] tmp = M[p];
                M[p] = M[max];
                M[max] = tmp;

                double piv = M[p][p];
                if (Math.abs(piv) < 1e-12) {
                    return null;
                }
                for (int j = p; j < 4; j++) {
                    M[p][j] /= piv;
                }
                for (int i = 0; i < 3; i++) {
                    if (i == p) {
                        continue;
                    }
                    double f = M[i][p];
                    for (int j = p; j < 4; j++) {
                        M[i][j] -= f * M[p][j];
                    }
                }
            }
            return new double[]{M[0][3], M[1][3], M[2][3]};
        }
    }

    public int getRoiPx() { return roiPx; }
    public void setRoiPx(int roiPx) { this.roiPx = roiPx; }
    public int getNMin() { return nMin; }
    public void setNMin(int nMin) { this.nMin = nMin; }
    public int getWindowUs() { return windowUs; }
    public void setWindowUs(int windowUs) { this.windowUs = windowUs; }
    public double getRmsMax() { return rmsMax; }
    public void setRmsMax(double rmsMax) { this.rmsMax = rmsMax; }
    public SmallTargetTrackerFilter getTracker() { return tracker; }
    public void setTracker(SmallTargetTrackerFilter tracker) { this.tracker = tracker; }
    public List<Detection> getDetected() { return lastDetections; }
}
