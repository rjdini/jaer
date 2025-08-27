package com.inilabs.jaer.projects.space3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * Top-down (XZ) world-view drawable that renders a tracker's
 * Field-Of-View cone footprint and a boresight arrow in Space3DGUI.
 *
 * This class is intentionally self-contained:
 * - No dependency on Space3DGUI internals.
 * - The host view calls {@link #setTransform(int,int,float)} with its current
 *   screen center and pixels-per-meter scale.
 *
 * Alignment with FieldOfView (chip/lens) will be added later. For now, we use
 * the TrackerAgent's yaw/pitch and a configurable horizontal FOV and range.
 */
public class TrackerFovDrawable {

    private final TrackerAgent tracker;

    /** Horizontal FOV in degrees (placeholder until wired to FieldOfView). */
    private float fovXDeg = 30f;

    /** Draw cone length (meters) for visualization. */
    private float rangeMeters = 50f;

    /** Screen transform: center (px) and pixels-per-meter scale. */
    private int centerX = 0, centerY = 0;
    private float pixelsPerMeter = 1f;

    /** Styling. */
    private Color coneColor = new Color(255, 0, 255, 120); // magenta, semi-transparent
    private Color edgeColor = new Color(255, 0, 255, 200);
    private Color arrowColor = new Color(255, 0, 255);

    public TrackerFovDrawable(TrackerAgent tracker){
        if (tracker == null) throw new IllegalArgumentException("tracker must not be null");
        this.tracker = tracker;
    }

    public void setTransform(int centerX, int centerY, float pixelsPerMeter){
        this.centerX = centerX;
        this.centerY = centerY;
        this.pixelsPerMeter = Math.max(0.0001f, pixelsPerMeter);
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
        Graphics2D g2 = (Graphics2D) g;

        // World pose
        Space3D.Vec3 p = tracker.getPositionDVX();
        double[] ypr = tracker.getYawPitchRollDeg();
        double yawDeg = ypr[0];

        // Direction unit vector in XZ plane (east=x, north=z)
        double yawRad = Math.toRadians(yawDeg);
        double dx = Math.sin(yawRad); // x-right
        double dz = Math.cos(yawRad); // z-forward (north)

        // Cone geometry in meters
        double L = Math.max(0.1, rangeMeters);
        double halfWidth = L * Math.tan(Math.toRadians(fovXDeg * 0.5));

        // Tip and base points in world meters
        double tipX = p.x + dx * L;
        double tipZ = p.z + dz * L;

        // Perpendicular to (dx,dz) in XZ
        double px = -dz;
        double pz =  dx;

        double baseLx = p.x + px * halfWidth;
        double baseLz = p.z + pz * halfWidth;

        double baseRx = p.x - px * halfWidth;
        double baseRz = p.z - pz * halfWidth;

        // Convert world meters to screen pixels (north-up: +Z = -y on screen)
        int sx = toScreenX(p.x);
        int sy = toScreenY(p.z);
        int sxTip  = toScreenX(tipX);
        int syTip  = toScreenY(tipZ);
        int sxBL   = toScreenX(baseLx);
        int syBL   = toScreenY(baseLz);
        int sxBR   = toScreenX(baseRx);
        int syBR   = toScreenY(baseRz);

        // Filled cone
        Polygon poly = new Polygon(new int[]{sxBL, sxBR, sxTip},
                                   new int[]{syBL, syBR, syTip}, 3);
        g2.setColor(coneColor);
        g2.fillPolygon(poly);
        g2.setColor(edgeColor);
        g2.drawPolygon(poly);

        // Boresight arrow (shorter than L to avoid clutter)
        double arrowLen = Math.min(L, 20.0);
        int sxArrow = toScreenX(p.x + dx * arrowLen);
        int syArrow = toScreenY(p.z + dz * arrowLen);
        g2.setColor(arrowColor);
        g2.drawLine(sx, sy, sxArrow, syArrow);
    }

    private int toScreenX(double worldX){ return centerX + (int)Math.round(worldX * pixelsPerMeter); }
    private int toScreenY(double worldZ){ return centerY - (int)Math.round(worldZ * pixelsPerMeter); }
}
