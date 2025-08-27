/*
 * Patched FlyingBlobGenerator.java
 * - Auto-connects to Space3D via Space3DRegistry (same JVM) if enabled
 * - Uses epsilon-gated FOV check to avoid border flicker
 * - Scales event count with blob area (density * πr²) with min/max clamps
 * - Removes duplicate injection path
 * - Adds throttled logging for visibility
 */
package com.inilabs.jaer.projects.eventprocessing.filters;

import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.Agent3DInterface;

import java.awt.geom.Point2D;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.Preferred;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.OutputEventIterator;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.event.PolarityEvent.Polarity;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import net.sf.jaer.util.EngineeringFormat;
import org.slf4j.LoggerFactory;

@Description("Generates and injects synthetic blobs of events from external moving object into the event stream")
@DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class FlyingBlobGenerator extends EventFilter2DMouseAdaptor {

    private static final ch.qos.logback.classic.Logger log
            = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FlyingBlobGenerator.class);

    // === External world/agent integration (provided by the app) ===
    private Space3D space3D;                  // world (optional but useful for GUI)
    public Agent3DInterface targetAgent;     // moving target (must be set)

    // cache FOV in degrees (computed in initFilter)
    public double fovXDeg = Double.NaN;

    // cache FOV in degrees (computed in initFilter)
    public double fovYDeg = Double.NaN;

    private final EngineeringFormat eng = new EngineeringFormat();
    private long lastLogMs = 0;

    // --- Legacy params kept for UI compatibility (some may be unused now) ---
    @Preferred @Description("Mean velocity for flying blobs")
    public float velocityMps = getFloat("velocityMps", 5);

    @Description("Lens focal length (mm)")
    public float lensFocalLengthMm = getFloat("lensFocalLengthMm", 22.5f);

    @Description("Blob size in meters (legacy)")
    public float blobSizeM = getFloat("blobSizeM", .25f);

    @Preferred @Description("CoV of speeds of flying blobs")
    public float covSpeed = getFloat("covSpeed", 1);

    @Description("Enable/disable synthetic blob injection")
    public boolean injectEnabled = getBoolean("injectEnabled", true);

    @Description("Blob center as fraction of chip width (legacy - not used)")
    public float centerXFrac = getFloat("centerXFrac", 0.60f);

    @Description("Blob center as fraction of chip height (legacy - not used)")
    public float centerYFrac = getFloat("centerYFrac", 0.60f);

    @Description("Fallback blob radius (px) if distance sizing is disabled")
    public int blobRadiusPx = getInt("blobRadiusPx", 5);

    @Description("Base number of synthetic events (used only if area scaling disabled)")
    public int eventsPerPacket = getInt("eventsPerPacket", 200);

    @Description(value = "Polarity for injected events: +1=ON, 0=alternate, -1=OFF")
    public int injectedPolarity = getInt("injectedPolarity", +1);

    @Description("Agent key to follow (looked up in Space3DRegistry if targetAgent is null)")
    public String targetAgentKey = getString("targetAgentKey", "tgt-FBG");

    @Description("If true, FBG will try Space3DRegistry.get() in initFilter() to resolve space/agent")
    public boolean autoConnectRegistry = getBoolean("autoConnectRegistry", true);

    // debug vectors (kept for potential UI/telemetry)
    public Vector3D blobPosition = new Vector3D(0, 0, 0);
    public Vector3D blobVelocity = new Vector3D(0, 0, 0);
    public Vector2D blob2dPosition = new Vector2D(0, 0);
    public Vector2D blob2dVelocity = new Vector2D(0, 0);

    public float startingDistanceM = Float.NaN;
    public EventPacket outPacket = null;

    // ---- 3D target parameters ----
    @Description("Target diameter in meters")
    public float targetDiameterM = 1.0f; // default 1 m

    @Description("Synthetic event density (events per pixel^2 of blob area)")
    public float eventDensityPerPx2 = getFloat("eventDensityPerPx2", 0.30f);

    // === Local fixed test target (no 3D) ===
    @net.sf.jaer.Description("Enable a local fixed test target at (xFrac,yFrac) on the image")
    public boolean localTestEnabled = getBoolean("localTestEnabled", false);

    @net.sf.jaer.Description("Local test target center X fraction [0..1]")
    public float localTestXFrac = getFloat("localTestXFrac", 0.60f);

    @net.sf.jaer.Description("Local test target center Y fraction [0..1]")
    public float localTestYFrac = getFloat("localTestYFrac", 0.60f);

    @net.sf.jaer.Description("Local test target radius in pixels")
    public int localTestRadiusPx = getInt("localTestRadiusPx", 6);

    @net.sf.jaer.Description("Local test events per packet (if <=0, uses area-density)")
    public int localTestEventsPerPacket = getInt("localTestEventsPerPacket", 300);

    public java.util.Random rng = new java.util.Random();

    public FlyingBlobGenerator(AEChip chip) { super(chip); }

    /* ================= Lifecycle ================= */
    @Override
    public void initFilter() {
        computeStartingDistance();
        Point2D.Double fovDeg = computeFoVDeg();  // HFOV/VFOV from chip + focal
        setFovXDeg(fovDeg.x);
        setFovYDeg(fovDeg.y);

        if (getOutPacket() == null) setOutPacket(new EventPacket(PolarityEvent.class));
        getOutPacket().clear();

        log.info("FBG initialized. HFOV={} deg, VFOV={} deg", eng.format(getFovXDeg()), eng.format(getFovYDeg()));

        // Try auto-connect if nothing has been injected yet
        if (isAutoConnectRegistry() && (space3D == null || getTargetAgent() == null)) {
            Space3D s = com.inilabs.jaer.projects.space3d.Space3DRegistry.get();
            if (s != null) {
                this.space3D = s;
                if (this.getTargetAgent() == null && getTargetAgentKey() != null) {
                    Agent3DInterface a = s.getAgent(getTargetAgentKey());
                    if (a != null) {
                        this.setTargetAgent(a);
                        log.info("FBG: auto-connected to Space3D and target agent '{}'", getTargetAgentKey());
                    } else {
                        log.warn("FBG: Space3D present, but no agent with key '{}'", getTargetAgentKey());
                    }
                } else {
                    log.info("FBG: auto-connected to Space3D (target already set)");
                }
            } else {
                log.warn("FBG: no Space3D in Space3DRegistry; setSpace3D()/setTargetAgent() or run world in SAME JVM.");
            }
        }
    }

    @Override public void resetFilter() { initFilter(); }

    @Override public void cleanup() { /* nothing to stop here (target thread owned outside) */ }

    /* ================= Core processing ================= */
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

        // Timestamp for synthetic events
        final int ts = in.getSize() > 0 ? in.getLastTimestamp() : (int) (System.nanoTime() / 1000);

        // === External target → compute az/el/dist, FOV gate, project, inject ===
        if (isInjectEnabled()) {
            if (getTargetAgent() == null) {
                throttleLog("FBG: targetAgent is null; no injection.");
            } else {
                Space3D.Vec3 p = getTargetAgent().getPositionDVX(); // ENU w.r.t. DVX at origin
                AzElDist aed = azElDistFromCamera(p.x, p.y, p.z);
                boolean inFov = insideFOV(aed);
                throttleLog(String.format("FBG: tgt=(%.1f,%.1f,%.1f)m az=%.2f° el=%.2f° d=%.1fm FOV=%s",
                        p.x, p.y, p.z, aed.azDeg, aed.elDeg, aed.distM, inFov ? "IN" : "OUT"));
                if (inFov) {
                    Point2D.Float px = projectToPixel(p.x, p.y, p.z);
                    if (px != null) {
                        int rpx = pixelRadiusFromDistance(aed.distM); // uses targetDiameterM
                        // area-scaled event count with clamps
                        int area = (int)Math.round(Math.PI * rpx * rpx);
                        int n = Math.min(2000, Math.max(50, (int)Math.round(getEventDensityPerPx2() * area)));
                        injectBlobAt(outItr, ts, Math.round(px.x), Math.round(px.y), rpx, n);
                    } else {
                        throttleLog("FBG: projection returned null (clipped).");
                    }
                }
            }
        }

        // === Local fixed test target (pure injector self-check) ===
        if (isLocalTestEnabled()) {
            final int w = chip.getSizeX();
            final int h = chip.getSizeY();
            int cx = Math.round(Math.max(0f, Math.min(1f, getLocalTestXFrac())) * (w - 1));
            int cy = Math.round(Math.max(0f, Math.min(1f, getLocalTestYFrac())) * (h - 1));
            int r  = Math.max(1, getLocalTestRadiusPx());
            int n;
            if (getLocalTestEventsPerPacket() > 0) {
                n = getLocalTestEventsPerPacket();
            } else {
                int area = (int)Math.round(Math.PI * r * r);
                n = Math.min(2000, Math.max(50, (int)Math.round(getEventDensityPerPx2() * area)));
            }
            injectBlobAt(outItr, ts, cx, cy, r, n);
        }

        return getOutPacket();
    }

    private void throttleLog(String msg){
        long now = System.currentTimeMillis();
        if (now - lastLogMs >= 500) { // log at most twice a second
            log.info(msg);
            lastLogMs = now;
        }
    }

    /* ================= Geometry & helpers ================= */
    private float getPixelPitchM() { return chip.getPixelWidthUm() * 1e-6f; }
    private float getFocalLenM()   { return getLensFocalLengthMm() * 1e-3f; }

    private Point2D.Float projectToPixel(double x, double y, double z) { return projectToPixel(new Vector3D(x, y, z)); }

    /** Project a 3D point (meters; camera at origin, Z forward) to pixel coords. Returns null if behind camera or outside chip. */
    private Point2D.Float projectToPixel(Vector3D P) {
        final float f = getFocalLenM();
        if (P.getZ() <= 0) return null; // behind camera
        final double u_m = f * (P.getX() / P.getZ());
        final double v_m = f * (P.getY() / P.getZ());
        final double pitch = getPixelPitchM();
        final double u_px = u_m / pitch;
        final double v_px = v_m / pitch;

        final int w = chip.getSizeX(), h = chip.getSizeY();
        final float cx = (w - 1) / 2f, cy = (h - 1) / 2f;
        final float x = (float) (cx + u_px);
        final float y = (float) (cy - v_px); // image y down

        if (x < 0 || x >= w || y < 0 || y >= h) return null;
        return new Point2D.Float(x, y);
    }

    /** Pixel radius from distance using small-angle pinhole geometry and targetDiameterM. */
    private int pixelRadiusFromDistance(double distM) {
        if (distM <= 0) return 1;
        final double f = getFocalLenM();
        final double pitch = getPixelPitchM();
        final double r_px = (f * (getTargetDiameterM() / 2.0)) / (distM * pitch);
        return Math.max(1, (int) Math.round(r_px));
    }

    // Overload with explicit nEvents
    private void injectBlobAt(OutputEventIterator outItr, int ts, int cx, int cy, int radiusPx, int nEvents) {
        if (!isInjectEnabled() || nEvents <= 0 || radiusPx <= 0) return;
        final int w = chip.getSizeX(), h = chip.getSizeY();
        final int r = radiusPx;
        for (int i = 0; i < nEvents; i++) {
            double theta = 2.0 * Math.PI * getRng().nextDouble();
            double rr = r * Math.sqrt(getRng().nextDouble());
            int x = cx + (int) Math.round(rr * Math.cos(theta));
            int y = cy + (int) Math.round(rr * Math.sin(theta));
            if (x < 0 || x >= w || y < 0 || y >= h) continue;
            BasicEvent e = outItr.nextOutput();
            e.x = (short) x; e.y = (short) y; e.timestamp = ts;
            if (e instanceof PolarityEvent) {
                PolarityEvent pe = (PolarityEvent) e;
                if (getInjectedPolarity() == 0) {
                    pe.setPolarity(((i & 1) == 0) ? Polarity.On : Polarity.Off);
                } else {
                    pe.setPolarity(getInjectedPolarity() > 0 ? Polarity.On : Polarity.Off);
                }
            }
        }
    }

    private Point2D.Double computeFoVDeg() {
        // HFOV/VFOV from chip size and focal length
        final double f_m = getLensFocalLengthMm() * 1e-3;
        final double w_px = chip.getPixelWidthUm() * 1e-6;
        final double h_px = chip.getPixelHeightUm() * 1e-6;
        final double W_m  = chip.getSizeX() * w_px;
        final double H_m  = chip.getSizeY() * h_px;
        final double fovX = Math.toDegrees(2.0 * Math.atan(W_m / (2.0 * f_m)));
        final double fovY = Math.toDegrees(2.0 * Math.atan(H_m / (2.0 * f_m)));
        log.debug("lensFL(m): {} pxPitch(m): {} pixels: x={}, y={} FOV: {}° {}°",
                f_m, w_px, chip.getSizeX(), chip.getSizeY(), fovX, fovY);
        return new Point2D.Double(fovX, fovY);
    }

    private void computeStartingDistance() {
        // distance at which a blob of size blobSizeM subtends one pixel
        float pxSizeM = chip.getPixelWidthUm() * 1e-6f;
        float pxAngRad = pxSizeM / (getLensFocalLengthMm() * 1e-3f);
        setStartingDistanceM(getBlobSizeM() / pxAngRad);
        log.info("Pixel angle {} deg; 1px blob distance {}", eng.format(Math.toDegrees(pxAngRad)), eng.format(getStartingDistanceM()));
    }

    /* ============== Az/El gate ============== */
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
        double eps = 0.2; // deg; reduces edge flicker
        double hx = getFovXDeg() * 0.5, hy = getFovYDeg() * 0.5;
        return Math.abs(aed.azDeg) <= (hx - eps) && Math.abs(aed.elDeg) <= (hy - eps);
    }

    /* ================= Setters/Getters for integration ================= */
    public void setSpace3D(Space3D s) { this.space3D = s; }
    public Space3D getSpace3D() { return space3D; }
    public void setTargetAgent(Agent3DInterface a) { this.targetAgent = a; }
    public Agent3DInterface getTargetAgent() { return targetAgent; }

    public float getVelocityMps() { return velocityMps; }
    public void setVelocityMps(float v) { velocityMps = v; putFloat("velocityMps", v); }

    public float getLensFocalLengthMm() { return lensFocalLengthMm; }
    public void setLensFocalLengthMm(float mm) { lensFocalLengthMm = mm; putFloat("lensFocalLengthMm", mm); computeStartingDistance(); computeFoVDeg(); }

    public float getBlobSizeM() { return blobSizeM; }
    public void setBlobSizeM(float m) { blobSizeM = m; putFloat("blobSizeM", m); computeStartingDistance(); }

    public float getCovSpeed() { return covSpeed; }
    public void setCovSpeed(float v) { covSpeed = v; putFloat("covSpeed", v); }

    public boolean isInjectEnabled() { return injectEnabled; }
    public void setInjectEnabled(boolean v) { injectEnabled = v; }

    public int getEventsPerPacket() { return eventsPerPacket; }
    public void setEventsPerPacket(int n) { eventsPerPacket = n; }

    public int getInjectedPolarity() { return injectedPolarity; }
    public void setInjectedPolarity(int p) { injectedPolarity = p; }

    public float getTargetDiameterM() { return targetDiameterM; }
    public void setTargetDiameterM(float m) { targetDiameterM = m; }

    public EventPacket getOutPacket() { return outPacket; }
    public void setOutPacket(EventPacket out) { outPacket = out; }

    public Vector3D getBlobPosition() { return blobPosition; }
    public void setBlobPosition(Vector3D p) { blobPosition = p; }
    public Vector3D getBlobVelocity() { return blobVelocity; }
    public void setBlobVelocity(Vector3D v) { blobVelocity = v; }
    public Vector2D getBlob2dPosition() { return blob2dPosition; }
    public void setBlob2dPosition(Vector2D p) { blob2dPosition = p; }
    public Vector2D getBlob2dVelocity() { return blob2dVelocity; }
    public void setBlob2dVelocity(Vector2D v) { blob2dVelocity = v; }

    public float getStartingDistanceM() { return startingDistanceM; }
    public void setStartingDistanceM(float d) { startingDistanceM = d; }

    public String getTargetAgentKey() { return targetAgentKey; }
    public void setTargetAgentKey(String key) { targetAgentKey = key; putString("targetAgentKey", key); }

    public boolean isAutoConnectRegistry() { return autoConnectRegistry; }
    public void setAutoConnectRegistry(boolean v) { autoConnectRegistry = v; putBoolean("autoConnectRegistry", v); }

    /**
     * @return the fovXDeg
     */
    public double getFovXDeg() {
        return fovXDeg;
    }

    /**
     * @param fovXDeg the fovXDeg to set
     */
    public void setFovXDeg(double fovXDeg) {
        this.fovXDeg = fovXDeg;
    }

    /**
     * @return the fovYDeg
     */
    public double getFovYDeg() {
        return fovYDeg;
    }

    /**
     * @param fovYDeg the fovYDeg to set
     */
    public void setFovYDeg(double fovYDeg) {
        this.fovYDeg = fovYDeg;
    }

    /**
     * @return the centerXFrac
     */
    public float getCenterXFrac() {
        return centerXFrac;
    }

    /**
     * @param centerXFrac the centerXFrac to set
     */
    public void setCenterXFrac(float centerXFrac) {
        this.centerXFrac = centerXFrac;
    }

    /**
     * @return the centerYFrac
     */
    public float getCenterYFrac() {
        return centerYFrac;
    }

    /**
     * @param centerYFrac the centerYFrac to set
     */
    public void setCenterYFrac(float centerYFrac) {
        this.centerYFrac = centerYFrac;
    }

    /**
     * @return the blobRadiusPx
     */
    public int getBlobRadiusPx() {
        return blobRadiusPx;
    }

    /**
     * @param blobRadiusPx the blobRadiusPx to set
     */
    public void setBlobRadiusPx(int blobRadiusPx) {
        this.blobRadiusPx = blobRadiusPx;
    }

    /**
     * @return the eventDensityPerPx2
     */
    public float getEventDensityPerPx2() {
        return eventDensityPerPx2;
    }

    /**
     * @param eventDensityPerPx2 the eventDensityPerPx2 to set
     */
    public void setEventDensityPerPx2(float eventDensityPerPx2) {
        this.eventDensityPerPx2 = eventDensityPerPx2;
    }

    /**
     * @return the localTestEnabled
     */
    public boolean isLocalTestEnabled() {
        return localTestEnabled;
    }

    /**
     * @param localTestEnabled the localTestEnabled to set
     */
    public void setLocalTestEnabled(boolean localTestEnabled) {
        this.localTestEnabled = localTestEnabled;
    }

    /**
     * @return the localTestXFrac
     */
    public float getLocalTestXFrac() {
        return localTestXFrac;
    }

    /**
     * @param localTestXFrac the localTestXFrac to set
     */
    public void setLocalTestXFrac(float localTestXFrac) {
        this.localTestXFrac = localTestXFrac;
    }

    /**
     * @return the localTestYFrac
     */
    public float getLocalTestYFrac() {
        return localTestYFrac;
    }

    /**
     * @param localTestYFrac the localTestYFrac to set
     */
    public void setLocalTestYFrac(float localTestYFrac) {
        this.localTestYFrac = localTestYFrac;
    }

    /**
     * @return the localTestRadiusPx
     */
    public int getLocalTestRadiusPx() {
        return localTestRadiusPx;
    }

    /**
     * @param localTestRadiusPx the localTestRadiusPx to set
     */
    public void setLocalTestRadiusPx(int localTestRadiusPx) {
        this.localTestRadiusPx = localTestRadiusPx;
    }

    /**
     * @return the localTestEventsPerPacket
     */
    public int getLocalTestEventsPerPacket() {
        return localTestEventsPerPacket;
    }

    /**
     * @param localTestEventsPerPacket the localTestEventsPerPacket to set
     */
    public void setLocalTestEventsPerPacket(int localTestEventsPerPacket) {
        this.localTestEventsPerPacket = localTestEventsPerPacket;
    }

    /**
     * @return the rng
     */
    public java.util.Random getRng() {
        return rng;
    }

    /**
     * @param rng the rng to set
     */
    public void setRng(java.util.Random rng) {
        this.rng = rng;
    }
}
