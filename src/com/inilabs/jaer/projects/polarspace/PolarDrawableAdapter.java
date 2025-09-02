package com.inilabs.jaer.projects.polarspace;

import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.utils.AgentColors;

import com.inilabs.jaer.projects.agents.api.Agent3DInterface;
import com.inilabs.jaer.projects.agents.api.AgentPolarDrawable;
import com.inilabs.jaer.projects.agents.api.AgentMath;          // NEW
import com.inilabs.jaer.projects.agents.api.Agent3DTypes;        // NEW
import com.inilabs.jaer.projects.agents.api.Activatable;       // NEW

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Bridges a Space3D agent into PolarSpaceGUI.
 * Defaults match the legacy behavior; new features are opt-in.
 */
public final class PolarDrawableAdapter implements PolarDrawable {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private final Agent3DInterface agent;
    private final Supplier<Space3D.Vec3> trackerPos;
    private final Supplier<double[]> trackerYprDeg;

    private final String key;
    private final int id;

    private Color color;
    private float sizeDeg = 2.0f;

    private float azDeg = 0f, elDeg = 0f;
    private double distM = Double.NaN;                         // NEW: last computed range

    private float azimuthScale = 1.0f, elevationScale = 1.0f;
    private float azimuthHeading = 0f, elevationHeading = 0f;
    private int centerX = 0, centerY = 0;

    private boolean drawLabel = true;
    private int labelDX = 8, labelDY = -8;
    private Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    // ---------- Opt-in enhancements (all default OFF / neutral) ----------
    private boolean enableFovClip = false;                     // NEW
    private float fovHalfAzDeg = 180f, fovHalfElDeg = 90f;     // NEW
    private boolean enableRangeScaling = false;                // NEW
    private float minPx = 2f, maxPx = 10f;                     // NEW

    public PolarDrawableAdapter(Agent3DInterface agent,
                                Supplier<Space3D.Vec3> trackerPos,
                                Supplier<double[]> trackerYprDeg) {
        this.agent = Objects.requireNonNull(agent);
        this.trackerPos = Objects.requireNonNull(trackerPos);
        this.trackerYprDeg = Objects.requireNonNull(trackerYprDeg);
        this.key = "polarAdapter:" + agent.getKey();
        this.id = SEQ.getAndIncrement();
        this.color = AgentColors.colorForKey(agent.getKey());
    }

    // ---------------- Fluent config (kept) ----------------
    public PolarDrawableAdapter color(Color c){ if (c!=null) this.color = c; return this; }
    public PolarDrawableAdapter sizeDeg(float s){ this.sizeDeg = Math.max(0.1f, s); return this; }
    public PolarDrawableAdapter label(boolean on){ this.drawLabel = on; return this; }
    public PolarDrawableAdapter labelOffset(int dx, int dy){ this.labelDX = dx; this.labelDY = dy; return this; }
    public PolarDrawableAdapter labelFont(Font f){ if (f!=null) this.labelFont = f; return this; }

    // ---------------- New fluent config (opt-in) ----------------
    /** Clip drawing to a tracker-centered FOV window (half-angles in degrees). */
    public PolarDrawableAdapter fovClip(boolean enable, float halfAzDeg, float halfElDeg){
        this.enableFovClip = enable;
        this.fovHalfAzDeg = Math.max(0, halfAzDeg);
        this.fovHalfElDeg = Math.max(0, halfElDeg);
        return this;
    }

    /** Scale symbol radius (px) with range in a soft-bounded way. */
    public PolarDrawableAdapter rangeScaling(boolean enable, float minPixels, float maxPixels){
        this.enableRangeScaling = enable;
        this.minPx = Math.max(1f, minPixels);
        this.maxPx = Math.max(this.minPx, maxPixels);
        return this;
    }

    // ---------------- PolarDrawable (existing API) ----------------
    @Override public String getKey() { return key; }
    @Override public int getId() { return id; }
    @Override public void showPath(boolean yes) {}
    @Override public void setSize(float sizeDegrees) { this.sizeDeg = sizeDegrees; }
    @Override public float getSize() { return sizeDeg; }
    @Override public void setColor(Color c) { if (c!=null) this.color = c; }
    @Override public Color getColor() { return color; }
    @Override public void setParentCallback(java.util.function.BiConsumer<ActionType,String> cb) {}
    @Override public long getLifetime() { return Long.MAX_VALUE; }
    @Override public boolean isExpired() { return false; }
    @Override public boolean isOrphaned() { return false; }
    @Override public void close() {}

    @Override
    public void onTransformChanged(float azScale, float elScale,
                                   float azHead, float elHead,
                                   int cx, int cy) {
        this.azimuthScale = azScale;
        this.elevationScale = elScale;
        this.azimuthHeading = azHead;
        this.elevationHeading = elHead;
        this.centerX = cx;
        this.centerY = cy;
    }

    /** Expose last computed range (meters) if consumers want it. */
    public double getDistanceMeters() { return distM; }        // NEW

    @Override
    public void draw(Graphics g) {
        Space3D.Vec3 tp = trackerPos.get();
        Space3D.Vec3 ap = agent.getPosition3D();
        if (tp == null || ap == null) return;

        // 1) Compute az/el/dist relative to tracker, using unified math
        Agent3DTypes.AzElDist aed = AgentMath.azElDistFromDVX(tp, ap); // NEW core
        azDeg = (float) aed.azDeg;
        elDeg = (float) aed.elDeg;
        distM = aed.distM;

        // 2) Rotate by tracker yaw/pitch to tracker frame (as before)
        double[] ypr = trackerYprDeg.get(); // degrees
        double yaw = (ypr != null && ypr.length>0) ? Math.toRadians(ypr[0]) : 0.0;
        double pitch = (ypr != null && ypr.length>1) ? Math.toRadians(ypr[1]) : 0.0;

        // Rebuild relative vector to apply rotation (keeps behavior consistent)
        double rx = ap.x - tp.x, ry = ap.y - tp.y, rz = ap.z - tp.z;
        double cy = Math.cos(-yaw), sy = Math.sin(-yaw);
        double x1 =  cy*rx + sy*rz;
        double z1 = -sy*rx + cy*rz;
        double cp = Math.cos(-pitch), sp = Math.sin(-pitch);
        double y2 =  cp*ry + sp*z1;
        double z2 = -sp*ry + cp*z1;

        double rxy = Math.hypot(x1, z2);
        azDeg = (float)Math.toDegrees(Math.atan2(x1, z2));
        elDeg = (float)Math.toDegrees(Math.atan2(y2, rxy));

        // 3) Optional: clip to FOV around current headings
        if (enableFovClip) {
            float da = normalizeAngle(azDeg - azimuthHeading);
            float de = elDeg - elevationHeading;
            if (Math.abs(da) > fovHalfAzDeg || Math.abs(de) > fovHalfElDeg) return;
        }

        // 4) Map to screen
        int x = centerX + (int)((azDeg - azimuthHeading) * azimuthScale);
        int y = centerY - (int)((elDeg - elevationHeading) * elevationScale);

        Graphics2D g2 = (Graphics2D) g;

        // 5) Dim if agent is paused (no per-agent threads; scheduler pauses via Activatable)
        boolean inactive = (agent instanceof Activatable) && !((Activatable)agent).isActive(); // NEW

        // 6) If the agent knows how to draw in polar space, delegate
        if (agent instanceof AgentPolarDrawable) {
            if (inactive) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                ((AgentPolarDrawable)agent).drawInPolar(g2, azDeg, elDeg, x, y);
                g2.setComposite(old);
            } else {
                ((AgentPolarDrawable)agent).drawInPolar(g2, azDeg, elDeg, x, y);
            }
            return;
        }

        // 7) Fallback symbol (crosshair + ring + optional label)
        Object aaOld = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING); // NEW
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = AgentColors.colorForKey(agent.getKey());
        Color use = inactive ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 120) : base;
        g2.setColor(use);

        int rpx = symbolRadiusPx();
        g2.drawOval(x - rpx, y - rpx, 2*rpx, 2*rpx);
        g2.drawLine(x - rpx, y, x + rpx, y);
        g2.drawLine(x, y - rpx, x, y + rpx);

        if (drawLabel) {
            String label = agent.getKey();
            if (label != null && !label.isEmpty()) {
                Font oldF = g2.getFont();
                g2.setFont(labelFont);

                // subtle background for contrast (NEW)
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(label);
                int h = fm.getAscent();
                Shape bg = new RoundRectangle2D.Float(x + labelDX - 3, y + labelDY - h,
                        w + 6, h + 4, 6, 6);
                g2.setColor(new Color(255,255,255, inactive ? 120 : 180));
                g2.fill(bg);

                g2.setColor(Color.BLACK);
                g2.drawString(label, x + labelDX, y + labelDY);
                g2.setFont(oldF);
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aaOld); // restore
    }

    @Override public void setAzimuth(float az){ this.azDeg = az; }
    @Override public float getAzimuth(){ return azDeg; }
    @Override public void setElevation(float el){ this.elDeg = el; }
    @Override public float getElevation(){ return elDeg; }

    // ---------------- Helpers ----------------
    private static float normalizeAngle(float a){
        // wrap to [-180, +180]
        float r = (a + 540f) % 360f - 180f;
        return (r == -180f) ? 180f : r;
    }

    private int symbolRadiusPx(){
        if (!enableRangeScaling || Double.isNaN(distM) || distM <= 0) {
            return Math.max(2, (int)(Math.max(azimuthScale, elevationScale) * sizeDeg * 0.1f));
        }
        // Smoothly maps distance to [maxPx .. minPx] (closer = bigger).
        double k = 1.0 / Math.log1p(100.0); // tune 100 m as a soft scale; adjust to taste
        double t = Math.exp(-k * Math.log1p(distM)); // in (0,1]
        double px = minPx + (maxPx - minPx) * t;
        return (int)Math.max(1, Math.round(px));
    }
}
