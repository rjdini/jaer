package com.inilabs.jaer.projects.space3d;

import com.inilabs.jaer.projects.space3d.api.Agent3DDrawable;
import com.inilabs.jaer.projects.space3d.api.AgentPolarDrawable;
import com.inilabs.jaer.projects.util.AgentColors;


import java.awt.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * TrackerAgent (DVXPLORER) — Stage 1
 *
 * - Implements Agent3DDrawable to self-draw its Field-Of-View (FOV) cone in Space3D (top-down x–z).
 * - Optional AgentPolarDrawable for a simple boresight marker in polar space.
 * - Orientation & FOV are provided via lightweight suppliers so we don't depend on FieldOfView's package.
 *
 * Conventions:
 *  - World frame is ENU: x=East, y=Up, z=North.
 *  - Yaw is about +y (Up). For top-down drawing we use yaw only (pitch ignored for footprint).
 *  - Azimuth heading φ (deg) -> forward vector on x–z: [sin φ, cos φ].
 */
public class TrackerAgent implements Agent3DInterface, Agent3DDrawable, AgentPolarDrawable {

    private final String key;
    private final Agent3D.ObjectType type = Agent3D.ObjectType.DVXPLORER;

    private volatile Space3D.Vec3 pos; // world position (ENU)

    /** Supplier of [yawDeg, pitchDeg, rollDeg] (degrees). */
    private final java.util.function.Supplier<double[]> yprDegSupplier;
    /** Supplier of [hfovDeg, vfovDeg] (degrees). */
    private final java.util.function.Supplier<double[]> fovDegSupplier;

    /** Cone length in meters (default 200m). */
    private volatile double coneLengthM = 200.0;

    /** Appearance */
    private volatile boolean drawLabel = true;
    private final Color color;
    private TrackerFovDrawable fovDrawable;

    // Optional stored geodetic and orientation state to satisfy Agent3DInterface
    private volatile double latDeg = Double.NaN, lonDeg = Double.NaN, altM = Double.NaN;
    private volatile double[] yprDegOverride = null; // if set via setter, overrides supplier for getters

    public TrackerAgent(String key,
                        Space3D.Vec3 positionENU,
                        java.util.function.Supplier<double[]> yprDegSupplier,
                        java.util.function.Supplier<double[]> fovDegSupplier){
        this.key = Objects.requireNonNull(key, "key");
        this.pos = Objects.requireNonNull(positionENU, "positionENU");
        this.yprDegSupplier = Objects.requireNonNull(yprDegSupplier, "ypr supplier");
        this.fovDegSupplier = Objects.requireNonNull(fovDegSupplier, "fov supplier");
        this.color = AgentColors.colorForKey(key);
      //  this.fovDrawable = new TrackerFovDrawable(this);
    }

    // ---------------- Agent3DInterface ----------------
    @Override public String getKey(){ return key; }
    @Override public Agent3D.ObjectType getType(){ return type; }
    @Override public Space3D.Vec3 getPosition3D(){ return pos; }
    
    @Override
    public void setLLA(double latDeg, double lonDeg, double altM){
        // Store provided geodetic coordinates; conversion to ENU depends on world origin,
        // which is not available here. Upstream can also call setPosition(...) directly.
        this.latDeg = latDeg; this.lonDeg = lonDeg; this.altM = altM;
    }
    
    @Override
    public double[] getLLA(){
        if (Double.isNaN(latDeg) || Double.isNaN(lonDeg) || Double.isNaN(altM)){
            return new double[]{0.0, 0.0, 0.0};
        }
        return new double[]{latDeg, lonDeg, altM};
    }
    
    @Override
    public void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg){
        this.yprDegOverride = new double[]{yawDeg, pitchDeg, rollDeg};
    }
    
    @Override
    public double[] getYawPitchRollDeg(){
        if (yprDegOverride != null) return yprDegOverride.clone();
        double[] ypr = yprDegSupplier.get();
        if (ypr == null) return new double[]{0.0, 0.0, 0.0};
        double yaw = ypr.length>0?ypr[0]:0.0;
        double pit = ypr.length>1?ypr[1]:0.0;
        double rol = ypr.length>2?ypr[2]:0.0;
        return new double[]{yaw, pit, rol};
    }


    @Override
    public void setPosition3D(Space3D.Vec3 p){ this.pos = p; }
    // Legacy alias if referenced elsewhere
    public void setPosition(Space3D.Vec3 p){ this.pos = p; }
    public TrackerAgent setConeLengthM(double m){ this.coneLengthM = Math.max(1.0, m); return this; }
    public TrackerAgent setDrawLabel(boolean on){ this.drawLabel = on; return this; }

 
    // ---------------- Agent3DDrawable (Space3D) ----------------
    @Override
    public void drawInSpace3D(Graphics2D g2, Space3D world){
        Space3D.Vec3 p = pos;
        if (p == null) return;

        double[] ypr = yprDegSupplier.get();
        double yawDeg = (ypr != null && ypr.length>0) ? ypr[0] : 0.0;
        double[] fov = fovDegSupplier.get();
        double hfovDeg = (fov != null && fov.length>0) ? fov[0] : 30.0;

        // Convert to radians
        double yaw = Math.toRadians(yawDeg);
        double half = Math.toRadians(hfovDeg * 0.5);

        // Apex (tracker position) in world x–z
        double ax = p.x, az = p.z;

        // Heading (top-down) vector
        double fx = Math.sin(yaw);
        double fz = Math.cos(yaw);

        // Edge headings (left/right) by rotating heading by ±half around +y
        double leftYaw  = yaw + half;
        double rightYaw = yaw - half;

        double lx = Math.sin(leftYaw);
        double lz = Math.cos(leftYaw);
        double rx = Math.sin(rightYaw);
        double rz = Math.cos(rightYaw);

    //    fovDrawable.draw(g2);
        // Far points at coneLength
        double L = coneLengthM;
        int xL = (int)Math.round(ax + L * lx);
        int zL = (int)Math.round(az + L * lz);
        int xR = (int)Math.round(ax + L * rx);
        int zR = (int)Math.round(az + L * rz);
        int xF = (int)Math.round(ax + L * fx);
        int zF = (int)Math.round(az + L * fz);

        // Draw triangle footprint (outline)
        Stroke oldS = g2.getStroke();
        Color oldC = g2.getColor();
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(color);
        int[] xs = new int[]{ (int)Math.round(ax), xL, xR };
        int[] ys = new int[]{ (int)Math.round(az), zL, zR };
        g2.drawPolygon(xs, ys, 3);

        // Draw boresight arrow to far point
        g2.drawLine((int)Math.round(ax), (int)Math.round(az), xF, zF);
        // Arrow head (short)
        double ah = 12.0; // meters; panel scale converts to pixels
        double hx1 = xF - fx * ah - lz * (ah * 0.3); // use left normal for arrow wing
        double hz1 = zF - fz * ah + lx * (ah * 0.3);
        double hx2 = xF - fx * ah + lz * (ah * 0.3);
        double hz2 = zF - fz * ah - lx * (ah * 0.3);
        g2.drawLine(xF, zF, (int)Math.round(hx1), (int)Math.round(hz1));
        g2.drawLine(xF, zF, (int)Math.round(hx2), (int)Math.round(hz2));

        // Label
        if (drawLabel){
            g2.setColor(Color.BLACK);
            g2.drawString(key, (int)Math.round(ax + 6), (int)Math.round(az - 6));
        }
        g2.setStroke(oldS);
        g2.setColor(oldC);
    }

    // ---------------- AgentPolarDrawable (simple marker) ----------------
    @Override
    public void drawInPolar(Graphics2D g2, float azDeg, float elDeg, int x, int y) {
        g2.setColor(color);
        g2.drawLine(x - 6, y, x + 6, y);
        g2.drawLine(x, y - 6, x, y + 6);
        g2.drawOval(x - 4, y - 4, 8, 8);
        g2.setColor(Color.BLACK);
        g2.drawString(key, x + 8, y - 8);
    }
}
