package com.inilabs.jaer.projects.space3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * Top-down (XZ) world-view drawable that renders a tracker's
 * Field-Of-View cone footprint and a boresight arrow in Space3DGUI.
 *
 * This version uses WorldTransform for all world→screen mapping
 * so panning/zooming never detaches the cone from the agent base.
 */
public class TrackerFovDrawable {

    private final DVXAgent tracker;

    /** Horizontal FOV in degrees (placeholder until wired to FieldOfView). */
    private float fovXDeg = 30f;

    /** Draw cone length (meters) for visualization. */
    private float rangeMeters = 50f;

    /** World→screen transform (may be injected each frame). */
    private WorldTransform tx;

    /** Styling. */
    private Color coneColor  = new Color(255, 0, 255, 120); // magenta, semi-transparent
    private Color edgeColor  = new Color(255, 0, 255, 200);
    private Color arrowColor = new Color(255, 0, 255);

    public TrackerFovDrawable(DVXAgent tracker){
        if (tracker == null) throw new IllegalArgumentException("tracker must not be null");
        this.tracker = tracker;
    }

    /** Preferred: set once per frame from the host panel before draw(Graphics). */
    public void setTransform(WorldTransform tx){
        this.tx = tx;
    }

    /** Optional stateless entry point if you prefer to pass the transform per call. */
    public void draw(Graphics g, WorldTransform tx){
        WorldTransform old = this.tx;
        try {
            this.tx = tx;
            draw(g);
        } finally {
            this.tx = old;
        }
    }

    /** Placeholder FOV until we bind to FieldOfView. */
    public void setFovXDeg(float deg){ this.fovXDeg = deg; }
    public float getFovXDeg(){ return fovXDeg; }

    /** Visualization range (meters). */
    public void setRangeMeters(float r){ this.rangeMeters = r; }
    public float getRangeMeters(){ return rangeMeters; }

    public void setConeColor(Color c){ if (c!=null) this.coneColor = c; }
    public void setEdgeColor(Color c){ if (c!=null) this.edgeColor = c; }
    public void setArrowColor(Color c){ if (c!=null) this.arrowColor = c; }

    /** Draws the cone footprint and a boresight arrow onto a top-down XZ map. */
    public void draw(Graphics g){
        if (tx == null) throw new IllegalStateException("WorldTransform not set");
        Graphics2D g2 = (Graphics2D) g;

        // World pose
        Space3D.Vec3 p = tracker.getPosition3D();
        if (p == null) return;

        double[] ypr = tracker.getYawPitchRollDeg();
        double yawDeg = (ypr != null && ypr.length > 0) ? ypr[0] : 0.0;

        // Direction unit vector in XZ plane (east=x, north=z)
        double yawRad = Math.toRadians(yawDeg);
        double dx = Math.sin(yawRad); // +x right (east)
        double dz = Math.cos(yawRad); // +z up/north (screen -y)

        // Cone geometry in meters
        double L = Math.max(0.1, rangeMeters);
        double halfWidth = L * Math.tan(Math.toRadians(fovXDeg * 0.5));

        // Tip and base points in world meters
        double tipX = p.x + dx * L;
        double tipZ = p.z + dz * L;

        // Perpendicular to (dx,dz) in XZ
        double px = -dz, pz = dx;

        double baseLx = p.x + px * halfWidth;
        double baseLz = p.z + pz * halfWidth;

        double baseRx = p.x - px * halfWidth;
        double baseRz = p.z - pz * halfWidth;

        // Convert world meters to screen pixels via WorldTransform
        int sx    = tx.toScreenX(p.x);
        int sy    = tx.toScreenY(p.z);
        int sxTip = tx.toScreenX(tipX);
        int syTip = tx.toScreenY(tipZ);
        int sxBL  = tx.toScreenX(baseLx);
        int syBL  = tx.toScreenY(baseLz);
        int sxBR  = tx.toScreenX(baseRx);
        int syBR  = tx.toScreenY(baseRz);

        // Filled cone
        Polygon poly = new Polygon(new int[]{sxBL, sxBR, sxTip},
                                   new int[]{syBL, syBR, syTip}, 3);
        Color old = g2.getColor();
        g2.setColor(coneColor);
        g2.fillPolygon(poly);
        g2.setColor(edgeColor);
        g2.drawPolygon(poly);

        // Boresight arrow (shorter than L to avoid clutter)
        double arrowLen = Math.min(L, 20.0);
        int sxArrow = tx.toScreenX(p.x + dx * arrowLen);
        int syArrow = tx.toScreenY(p.z + dz * arrowLen);
        g2.setColor(arrowColor);
        g2.drawLine(sx, sy, sxArrow, syArrow);

        g2.setColor(old);
    }
}
