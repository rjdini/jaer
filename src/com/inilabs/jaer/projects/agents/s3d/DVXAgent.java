package com.inilabs.jaer.projects.agents.s3d;

import com.inilabs.jaer.projects.agents.api.AgentPolarDrawable;
import com.inilabs.jaer.projects.agents.api.Agent3DTypes;
import com.inilabs.jaer.projects.agents.api.DrawableInSpace3D;
import com.inilabs.jaer.projects.agents.core.AbstractAgent;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.TrackerFovDrawable;
import com.inilabs.jaer.projects.space3d.WorldTransform;
import com.inilabs.jaer.projects.utils.AgentColors;

import java.awt.*;
import java.util.Objects;

/**
 * DVXAgent (Tracker) – extends AbstractAgent for unified base. - Draws FOV cone
 * in Space3D (top-down). - Draws boresight marker in Polar space. - YPR and FOV
 * provided via suppliers (or overridden via setters).
 *
 * World frame: ENU (x=East, y=Up, z=North). Yaw about +y; footprint uses yaw
 * only.
 */
public class DVXAgent extends AbstractAgent implements DrawableInSpace3D, AgentPolarDrawable {

    /**
     * Supplier of [yawDeg, pitchDeg, rollDeg] (degrees).
     */
    private final java.util.function.Supplier<double[]> yprDegSupplier;
    /**
     * Supplier of [hfovDeg, vfovDeg] (degrees).
     */
    private final java.util.function.Supplier<double[]> fovDegSupplier;

    private final Color CONE_FILL = new Color(255, 0, 255, 80);
    private final Color CONE_EDGE = new Color(255, 0, 255, 160);
    private final Color ARROW = Color.BLACK;
    private final float FOVX_DEG = 30f;   // placeholder; will wire to FieldOfView later
    private final float RANGE_M = 200f;

    /**
     * Cone length in meters (default 200m).
     */
      private volatile double coneLengthM =RANGE_M;
      
      
    /**
     * Appearance
     */
    private volatile boolean drawLabel = true;
    private final Color color;
    private TrackerFovDrawable fovDrawable;

    // If set via setter, overrides supplier for getters
    private volatile double[] yprDegOverride = null;

    public DVXAgent(String key,
            Space3D.Vec3 positionENU,
            java.util.function.Supplier<double[]> yprDegSupplier,
            java.util.function.Supplier<double[]> fovDegSupplier) {
        super(Objects.requireNonNull(key, "key"), Agent3DTypes.ObjectType.DVXPLORER);
        this.posDVX = Objects.requireNonNull(positionENU, "positionENU");
        this.yprDegSupplier = Objects.requireNonNull(yprDegSupplier, "ypr supplier");
        this.fovDegSupplier = Objects.requireNonNull(fovDegSupplier, "fov supplier");
        this.color = AgentColors.colorForKey(key);
        // this.fovDrawable = new TrackerFovDrawable(this); // re-enable when that class is ready
    }

    /* ================= Agent3DInterface (from AbstractAgent) ================= */
    @Override
    public void setPosition3D(Space3D.Vec3 p) {
        super.setPosition3D(p);
    }

    @Override
    public Space3D.Vec3 getPosition3D() {
        return posDVX;
    }

    @Override
    public void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg) {
        // Store in base fields and set an override so getters prefer this until cleared
        this.yawDeg = yawDeg;
        this.pitchDeg = pitchDeg;
        this.rollDeg = rollDeg;
        this.yprDegOverride = new double[]{yawDeg, pitchDeg, rollDeg};
    }

    @Override
    public double[] getYawPitchRollDeg() {
        if (yprDegOverride != null) {
            return yprDegOverride.clone();
        }
        double[] ypr = yprDegSupplier.get();
        if (ypr == null) {
            return new double[]{0.0, 0.0, 0.0};
        }
        double yaw = ypr.length > 0 ? ypr[0] : 0.0;
        double pit = ypr.length > 1 ? ypr[1] : 0.0;
        double rol = ypr.length > 2 ? ypr[2] : 0.0;
        // Keep base fields roughly in sync (optional)
        this.yawDeg = yaw;
        this.pitchDeg = pit;
        this.rollDeg = rol;
        return new double[]{yaw, pit, rol};
    }

    // LLA getters/setters already provided by AbstractAgent; keep override only if you need custom defaults.

    /* ================= Fluent config ================= */
    public DVXAgent setConeLengthM(double m) {
        this.coneLengthM = Math.min(500.0, m); 
        return this;
    }

    public DVXAgent setDrawLabel(boolean on) {
        this.drawLabel = on;
        return this;
    }

    /* ================= Agent3DDrawable (Space3D) ================= */
    @Override
    public void drawInSpace3D(Graphics2D g2, Space3D world, WorldTransform tx) {
           //  Recipe
        // calculate shapes etc in world coords (m)
        // transform to screen coords using tx
        // plot ONCE, discard (dont fiddle with transformed screen coords!)

        // save originals
        Stroke oldS = g2.getStroke();
        Color oldC = g2.getColor();
        
        
        Space3D.Vec3 p = getPosition3D();
        if (p == null) {
            return;
        }
        
        int sx = tx.toScreenX(p.x);
        int sy = tx.toScreenY(p.z);

          // draw base
          int r = 4;
          g2.setColor(new Color(30, 144, 255));
          g2.fillRect(sx - r, sy - r, 2 * r, 2 * r);
          g2.setColor(Color.black);
          g2.drawRect(sx - r, sy - r, 2 * r, 2 * r);
        
        //  draw FOV

        double[] ypr = getYawPitchRollDeg();
        double yaw = Math.toRadians((ypr != null && ypr.length > 0) ? ypr[0] : 0.0);
        
        double hfovDeg = 20.0; // default
        // get hfov from supplier
        double[] fov = fovDegSupplier.get();
        if (fov != null && fov.length > 0) {
            hfovDeg = fov[0];
        }
        double half = Math.toRadians(hfovDeg * 0.5);

        // world coords (meters)
        double ax = p.x, az = p.z;
        double fx = Math.sin(yaw), fz = Math.cos(yaw);
        double L = coneLengthM;

        // edge headings in world
        double leftYaw = yaw + half, rightYaw = yaw - half;
        double lx = Math.sin(leftYaw), lz = Math.cos(leftYaw);
        double rx = Math.sin(rightYaw), rz = Math.cos(rightYaw);

        // world points (meters)
        double xLw = ax + L * lx, zLw = az + L * lz;
        double xRw = ax + L * rx, zRw = az + L * rz;
        double xFw = ax + L * fx, zFw = az + L * fz;

        // map to screen once via tx
        int sxA = tx.toScreenX(ax), szA = tx.toScreenY(az);
        int sxL = tx.toScreenX(xLw), szL = tx.toScreenY(zLw);
        int sxR = tx.toScreenX(xRw), szR = tx.toScreenY(zRw);
        int sxF = tx.toScreenX(xFw), szF = tx.toScreenY(zFw);

        
        g2.setStroke(new BasicStroke(1f));

        g2.setColor(CONE_FILL);
        g2.fillPolygon(new int[]{sxA, sxL, sxR}, new int[]{szA, szL, szR}, 3);
        g2.setColor(CONE_EDGE);
        g2.drawPolygon(new int[]{sxA, sxL, sxR}, new int[]{szA, szL, szR}, 3);

        g2.setColor(ARROW);
        g2.drawLine(sxA, szA, sxF, szF);

        // arrowhead: compute world, then transform
        double ah = 12.0; // meters
        double nx = -fz, nz = fx; // left normal
        double hx1w = xFw - fx * ah - nx * (ah * 0.3);
        double hz1w = zFw - fz * ah - nz * (ah * 0.3);
        double hx2w = xFw - fx * ah + nx * (ah * 0.3);
        double hz2w = zFw - fz * ah + nz * (ah * 0.3);
        g2.drawLine(sxF, szF, tx.toScreenX(hx1w), tx.toScreenY(hz1w));
        g2.drawLine(sxF, szF, tx.toScreenX(hx2w), tx.toScreenY(hz2w));

        if (drawLabel) {
            g2.setColor(Color.BLACK);
            g2.drawString(getKey(), sxA + 6, szA - 6);
        }
        
        // restore originals
        g2.setStroke(oldS);
        g2.setColor(oldC);
    }


    /* ================= AgentPolarDrawable (simple marker) ================= */
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
