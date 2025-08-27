/*
 * FlyingBlobGenerator.java (multi-target, shapes)
 * - Iterates over a provider of FBGTarget agents (Space3DTargetProvider or custom)
 * - Sizes per target using physical diameter
 * - Renders different shapes (CIRCLE, SQUARE, TRIANGLE, CROSS)
 * - Keeps local fixed test blob for injector sanity
 */
package com.inilabs.jaer.projects.eventprocessing.filters;

import com.inilabs.jaer.projects.space3d.*;

import java.awt.geom.Point2D;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.OutputEventIterator;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.event.PolarityEvent.Polarity;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.slf4j.LoggerFactory;

@Description("Injects synthetic blob events for all Space3D FBGTargets in FOV; supports basic shapes")
@DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class FlyingBlobGenerator extends EventFilter2DMouseAdaptor {

    private static final ch.qos.logback.classic.Logger log
            = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FlyingBlobGenerator.class);

    /* ===================== Core parameters ===================== */

    @Description("Enable/disable synthetic injection")
    public boolean injectEnabled = getBoolean("injectEnabled", true);

    @Description("Lens focal length (mm) used for projection & sizing")
    public float lensFocalLengthMm = getFloat("lensFocalLengthMm", 22.5f);

    @Description("Base event density (events per pixel^2 of shape area)")
    public float eventDensityPerPx2 = getFloat("eventDensityPerPx2", 0.30f);

    @Description("Polarity for injected events: +1=ON, 0=alternate, -1=OFF")
    public int injectedPolarity = getInt("injectedPolarity", +1);

    /* ===================== Remote agents provider ===================== */

    private FBGTargetProvider targetProvider; // preferred way (multiple)
    private Agent3DInterface singleTarget;    // backwards compatibility: single target

    @Description("If true, auto-connect provider via Space3DRegistry in initFilter()")
    public boolean autoConnectRegistry = getBoolean("autoConnectRegistry", true);

    /* ===================== Local fixed test blob ===================== */

    @Description("Enable a local fixed test target at (xFrac,yFrac) on the image")
    public boolean localTestEnabled = getBoolean("localTestEnabled", false);

    @Description("Local test target center X fraction [0..1]")
    public float localTestXFrac = getFloat("localTestXFrac", 0.60f);

    @Description("Local test target center Y fraction [0..1]")
    public float localTestYFrac = getFloat("localTestYFrac", 0.60f);

    @Description("Local test target radius in pixels")
    public int localTestRadiusPx = getInt("localTestRadiusPx", 6);

    @Description("Local test events per packet (if <=0, uses area-density)")
    public int localTestEventsPerPacket = getInt("localTestEventsPerPacket", 300);

    /* ===================== Internals ===================== */

    private double fovXDeg = Double.NaN, fovYDeg = Double.NaN; // computed at init
    public EventPacket outPacket = null;
    private final java.util.Random rng = new java.util.Random();
    private long lastLogMs = 0;

    public FlyingBlobGenerator(AEChip chip) { super(chip); }

    /* ===================== Lifecycle ===================== */

    @Override
    public void initFilter() {
        computeFoV();
        if (getOutPacket() == null) setOutPacket(new EventPacket(PolarityEvent.class));
        getOutPacket().clear();

        if (autoConnectRegistry && targetProvider == null) {
            Space3D s = Space3DRegistry.get();
            if (s != null) {
                this.targetProvider = new Space3DTargetProvider(s);
                log.info("FBG: auto-connected Space3DTargetProvider ({} agents total).", s.getAgents().size());
            } else {
                log.warn("FBG: no Space3D in Space3DRegistry; setTargetProvider() explicitly.");
            }
        }
    }

    @Override public void resetFilter() { initFilter(); }
    @Override public void cleanup() { /* no-op */ }

    /* ===================== Core filter ===================== */

    @Override
    public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        if (getOutPacket() == null) setOutPacket(new EventPacket(in.getEventClass()));
        getOutPacket().clear();
        final OutputEventIterator outItr = getOutPacket().outputIterator();

        // Pass-through inputs
        for (BasicEvent ie : in) {
            BasicEvent oe = outItr.nextOutput();
            oe.copyFrom(ie);
        }

        if (!injectEnabled) return getOutPacket();
        final int ts = in.getSize() > 0 ? in.getLastTimestamp() : (int) (System.nanoTime() / 1000);

        // Multi-target path
        if (targetProvider != null) {
            for (FBGTarget tgt : targetProvider.getFBGTargets()) {
                renderTarget(tgt, outItr, ts);
            }
        } else if (singleTarget != null && singleTarget instanceof FBGTarget) {
            renderTarget((FBGTarget)singleTarget, outItr, ts);
        }

        // Local fixed test blob
        if (localTestEnabled) {
            final int w = chip.getSizeX();
            final int h = chip.getSizeY();
            int cx = Math.round(Math.max(0f, Math.min(1f, localTestXFrac)) * (w - 1));
            int cy = Math.round(Math.max(0f, Math.min(1f, localTestYFrac)) * (h - 1));
            int r  = Math.max(1, localTestRadiusPx);
            int n;
            if (localTestEventsPerPacket > 0) {
                n = localTestEventsPerPacket;
            } else {
                int area = (int)Math.round(Math.PI * r * r);
                n = Math.min(2000, Math.max(50, (int)Math.round(eventDensityPerPx2 * area)));
            }
            injectCircle(outItr, ts, cx, cy, r, n);
        }

        return getOutPacket();
    }

    private void renderTarget(FBGTarget tgt, OutputEventIterator outItr, int ts){
        Space3D.Vec3 p = tgt.getPositionDVX();
        AzElDist aed = azElDistFromCamera(p.x, p.y, p.z);
        boolean inFov = insideFOV(aed);
        throttleLog(String.format("FBG: %s az=%.2f el=%.2f d=%.1f FOV=%s", tgt.getKey(), aed.azDeg, aed.elDeg, aed.distM, inFov ? "IN" : "OUT"));
        if (!inFov) return;
        Point2D.Float px = projectToPixel(p.x, p.y, p.z);
        if (px == null) return;
        int rpx = pixelRadiusFromDistance(aed.distM, Math.max(1e-6f, tgt.getPhysicalDiameterM()));
        int n = eventsForShape(tgt.getShape(), rpx, eventDensityPerPx2 * Math.max(1f, tgt.getDensityScale()));
        switch (tgt.getShape()) {
            case CIRCLE:  injectCircle(outItr, ts, Math.round(px.x), Math.round(px.y), rpx, n); break;
            case SQUARE:  injectSquare(outItr, ts, Math.round(px.x), Math.round(px.y), rpx, n); break;
            case TRIANGLE:injectTriangle(outItr, ts, Math.round(px.x), Math.round(px.y), rpx, n); break;
            case CROSS:   injectCross(outItr, ts, Math.round(px.x), Math.round(px.y), rpx, n); break;
        }
    }

    /* ===================== Geometry ===================== */

    private float getPixelPitchM() { return chip.getPixelWidthUm() * 1e-6f; }
    private float getFocalLenM()   { return getLensFocalLengthMm() * 1e-3f; }

    private void computeFoV() {
        final double f_m = getLensFocalLengthMm() * 1e-3;
        final double w_px = chip.getPixelWidthUm() * 1e-6;
        final double h_px = chip.getPixelHeightUm() * 1e-6;
        final double W_m  = chip.getSizeX() * w_px;
        final double H_m  = chip.getSizeY() * h_px;
        fovXDeg = Math.toDegrees(2.0 * Math.atan(W_m / (2.0 * f_m)));
        fovYDeg = Math.toDegrees(2.0 * Math.atan(H_m / (2.0 * f_m)));
    }

    private Point2D.Float projectToPixel(double x, double y, double z) { return projectToPixel(new Vector3D(x, y, z)); }

    private Point2D.Float projectToPixel(Vector3D P) {
        final float f = getFocalLenM();
        if (P.getZ() <= 0) return null;
        final double u_m = f * (P.getX() / P.getZ());
        final double v_m = f * (P.getY() / P.getZ());
        final double pitch = getPixelPitchM();
        final double u_px = u_m / pitch;
        final double v_px = v_m / pitch;

        final int w = chip.getSizeX(), h = chip.getSizeY();
        final float cx = (w - 1) / 2f, cy = (h - 1) / 2f;
        final float x = (float) (cx + u_px);
        final float y = (float) (cy - v_px);

        if (x < 0 || x >= w || y < 0 || y >= h) return null;
        return new Point2D.Float(x, y);
    }

    private int pixelRadiusFromDistance(double distM, float diameterM) {
        if (distM <= 0) return 1;
        final double f = getFocalLenM();
        final double pitch = getPixelPitchM();
        final double r_px = (f * (diameterM / 2.0)) / (distM * pitch);
        return Math.max(1, (int) Math.round(r_px));
    }

    /* ===================== Angles & FOV gate ===================== */

    private static double toDeg(double rad) { return Math.toDegrees(rad); }

    private static final class AzElDist {
        final double azDeg, elDeg, distM;
        AzElDist(double azDeg, double elDeg, double distM) { this.azDeg=azDeg; this.elDeg=elDeg; this.distM=distM; }
    }

    /** Camera at origin; +Z forward (optical axis), +X right/East, +Y up. */
    private AzElDist azElDistFromCamera(double x, double y, double z) {
        double dist = Math.sqrt(x * x + y * y + z * z);
        if (z <= 0) return new AzElDist(Double.NaN, Double.NaN, dist); // behind
        double az = toDeg(Math.atan2(x, z)); // horiz angle vs optical axis
        double el = toDeg(Math.atan2(y, z)); // vert angle vs optical axis
        return new AzElDist(az, el, dist);
    }

    private boolean insideFOV(AzElDist aed) {
        if (Double.isNaN(aed.azDeg) || Double.isNaN(aed.elDeg)) return false;
        double eps = 0.2; // deg; reduces edge flicker at border
        double hx = fovXDeg * 0.5, hy = fovYDeg * 0.5;
        return Math.abs(aed.azDeg) <= (hx - eps) && Math.abs(aed.elDeg) <= (hy - eps);
    }

    /* ===================== Shape event counts ===================== */

    private int eventsForShape(TargetShape shape, int rpx, float density){
        double k;
        switch (shape){
            case SQUARE:   k = 4.0; break;                         // (2r)^2
            case TRIANGLE: k = 3.0*Math.sqrt(3)/4.0; break;        // ~1.299 r^2
            case CROSS:    k = 2.0; break;                         // approx bar area
            case CIRCLE:
            default:       k = Math.PI; break;                     // π r^2
        }
        int n = (int)Math.round(density * k * rpx * rpx);
        return Math.min(3000, Math.max(30, n));
    }

    /* ===================== Shape injectors ===================== */

    private void injectCircle(OutputEventIterator outItr, int ts, int cx, int cy, int r, int n){
        final int w = chip.getSizeX(), h = chip.getSizeY();
        for (int i=0; i<n; i++){
            double theta = 2.0 * Math.PI * rng.nextDouble();
            double rr = r * Math.sqrt(rng.nextDouble());
            int x = cx + (int)Math.round(rr * Math.cos(theta));
            int y = cy + (int)Math.round(rr * Math.sin(theta));
            if (x<0||x>=w||y<0||y>=h) continue;
            emit(outItr, ts, x, y, i);
        }
    }

    private void injectSquare(OutputEventIterator outItr, int ts, int cx, int cy, int r, int n){
        final int w = chip.getSizeX(), h = chip.getSizeY();
        for (int i=0; i<n; i++){
            int x = cx + (int)Math.round((rng.nextDouble()*2-1) * r);
            int y = cy + (int)Math.round((rng.nextDouble()*2-1) * r);
            if (x<0||x>=w||y<0||y>=h) continue;
            emit(outItr, ts, x, y, i);
        }
    }

    private void injectTriangle(OutputEventIterator outItr, int ts, int cx, int cy, int r, int n){
        final int w = chip.getSizeX(), h = chip.getSizeY();
        final double s = Math.sqrt(3) * r; // side length for circumradius r
        final double htri = Math.sqrt(3)/2 * s;
        double x1 = 0,         y1 = -2.0/3.0 * htri;
        double x2 = -s/2.0,    y2 =  1.0/3.0 * htri;
        double x3 =  s/2.0,    y3 =  1.0/3.0 * htri;

        for (int i=0; i<n; i++){
            double u = rng.nextDouble();
            double v = rng.nextDouble();
            if (u+v > 1){ u = 1-u; v = 1-v; }
            double x = x1 + u*(x2-x1) + v*(x3-x1);
            double y = y1 + u*(y2-y1) + v*(y3-y1);
            int xi = cx + (int)Math.round(x);
            int yi = cy + (int)Math.round(y);
            if (xi<0||xi>=w||yi<0||yi>=h) continue;
            emit(outItr, ts, xi, yi, i);
        }
    }

    private void injectCross(OutputEventIterator outItr, int ts, int cx, int cy, int r, int n){
        final int w = chip.getSizeX(), h = chip.getSizeY();
        final int half = r;
        final int barHalfWidth = Math.max(1, r/4);
        for (int i=0; i<n; i++){
            boolean vertical = rng.nextBoolean();
            int x, y;
            if (vertical){
                x = cx + (int)Math.round((rng.nextDouble()*2-1) * barHalfWidth);
                y = cy + (int)Math.round((rng.nextDouble()*2-1) * half);
            } else {
                x = cx + (int)Math.round((rng.nextDouble()*2-1) * half);
                y = cy + (int)Math.round((rng.nextDouble()*2-1) * barHalfWidth);
            }
            if (x<0||x>=w||y<0||y>=h) continue;
            emit(outItr, ts, x, y, i);
        }
    }

    private void emit(OutputEventIterator outItr, int ts, int x, int y, int i){
        BasicEvent e = outItr.nextOutput();
        e.x = (short)x; e.y = (short)y; e.timestamp = ts;
        if (e instanceof PolarityEvent) {
            PolarityEvent pe = (PolarityEvent) e;
            if (injectedPolarity == 0) {
                pe.setPolarity(((i & 1) == 0) ? Polarity.On : Polarity.Off);
            } else {
                pe.setPolarity(injectedPolarity > 0 ? Polarity.On : Polarity.Off);
            }
        }
    }

    private void throttleLog(String msg){
        long now = System.currentTimeMillis();
        if (now - lastLogMs >= 500) { log.info(msg); lastLogMs = now; }
    }

    /* ===================== Helpers & getters ===================== */

    public void setTargetProvider(FBGTargetProvider p){ this.targetProvider = p; }
    public FBGTargetProvider getTargetProvider(){ return targetProvider; }

    // Back-compat single target (optional)
    public void setTargetAgent(Agent3DInterface a){ this.singleTarget = a; }
    public Agent3DInterface getTargetAgent(){ return singleTarget; }

    /**
     * @return the outPacket
     */
    public EventPacket getOutPacket() {
        return outPacket;
    }

    /**
     * @param outPacket the outPacket to set
     */
    public void setOutPacket(EventPacket outPacket) {
        this.outPacket = outPacket;
    }

    /**
     * @return the lensFocalLengthMm
     */
    public float getLensFocalLengthMm() {
        return lensFocalLengthMm;
    }

    /**
     * @param lensFocalLengthMm the lensFocalLengthMm to set
     */
    public void setLensFocalLengthMm(float lensFocalLengthMm) {
        this.lensFocalLengthMm = lensFocalLengthMm;
    }
}
