package com.inilabs.jaer.projects.space3d;

/**
 * TrackerAgent: represents a DVX/gimbal node in the 3D world so it can be
 * displayed in Space3DGUI and used as an origin for polar projections.
 */
public class TrackerAgent implements Agent3DInterface, WorldRenderable {

    private final String key;
    private final Agent3D.ObjectType type = Agent3D.ObjectType.DVXPLORER;

    /** Position in DVX world meters (X east, Y up, Z north). */
    private Space3D.Vec3 position = new Space3D.Vec3(0,0,0);

    /** Orientation in degrees: yaw (azimuth), pitch (elevation), roll. */
    private double yawDeg = 0.0, pitchDeg = 0.0, rollDeg = 0.0;

    /** Optional geodetic location; NaN means unknown. */
    private double latDeg = Double.NaN, lonDeg = Double.NaN, altM = Double.NaN;

    public TrackerAgent(String key){
        if (key == null || key.isEmpty()) throw new IllegalArgumentException("key must be non-empty");
        this.key = key;
    }

    /* ================= Agent3DInterface ================= */

    @Override public String getKey() { return key; }
    @Override public Agent3D.ObjectType getType() { return type; }

    @Override public Space3D.Vec3 getPositionDVX() { return position; }
    @Override public void setPositionDVX(Space3D.Vec3 p) { if (p != null) this.position = p; }

    @Override public double[] getYawPitchRollDeg() { return new double[]{yawDeg, pitchDeg, rollDeg}; }
    @Override public void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg) {
        this.yawDeg = yawDeg; this.pitchDeg = pitchDeg; this.rollDeg = rollDeg;
    }

    @Override public double[] getLLA() { return new double[]{latDeg, lonDeg, altM}; }
    @Override public void setLLA(double latDeg, double lonDeg, double altM) {
        this.latDeg = latDeg; this.lonDeg = lonDeg; this.altM = altM;
    }

    /* ================= WorldRenderable ================= */

    @Override
    public void drawWorld(java.awt.Graphics g, WorldTransform tx){
        // Placeholder FOV and range until wired to FieldOfView
        float fovXDeg = 30f;
        float rangeM  = 80f;

        // Pose
        Space3D.Vec3 p = getPositionDVX();
        double[] ypr = getYawPitchRollDeg();
        double yawDeg = (ypr != null && ypr.length>0) ? ypr[0] : 0.0;

        // Screen apex
        int sx = tx.toScreenX(p.x);
        int sy = tx.toScreenY(p.z);

        // Direction in XZ
        double yawRad = Math.toRadians(yawDeg);
        double dx = Math.sin(yawRad);
        double dz = Math.cos(yawRad);

        // Base center forward
        double L = Math.max(0.1, rangeM);
        double halfWidth = L * Math.tan(Math.toRadians(fovXDeg * 0.5));
        double baseCx = p.x + dx * L;
        double baseCz = p.z + dz * L;
        double px = -dz, pz = dx;

        double baseLx = baseCx + px * halfWidth;
        double baseLz = baseCz + pz * halfWidth;
        double baseRx = baseCx - px * halfWidth;
        double baseRz = baseCz - pz * halfWidth;

        int sxBL = tx.toScreenX(baseLx);
        int syBL = tx.toScreenY(baseLz);
        int sxBR = tx.toScreenX(baseRx);
        int syBR = tx.toScreenY(baseRz);

        int[] xs = { sx, sxBL, sxBR };
        int[] ys = { sy, syBL, syBR };

        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        java.awt.Color fill = new java.awt.Color(255, 0, 255, 80);
        java.awt.Color edge = new java.awt.Color(255, 0, 255, 160);
        java.awt.Color arrow = new java.awt.Color(255, 0, 255);

        g2.setColor(fill);
        g2.fillPolygon(xs, ys, 3);
        g2.setColor(edge);
        g2.drawPolygon(xs, ys, 3);

        // Boresight arrow
        double arrowLen = Math.min(L, 20.0);
        int sxArrow = tx.toScreenX(p.x + dx * arrowLen);
        int syArrow = tx.toScreenY(p.z + dz * arrowLen);
        g2.setColor(arrow);
        g2.drawLine(sx, sy, sxArrow, syArrow);
    }

    /* ================= Convenience API ================= */

    public void setPoseDeg(double yawDeg, double pitchDeg, double rollDeg){
        setYawPitchRollDeg(yawDeg, pitchDeg, rollDeg);
    }

    public void setPositionMeters(double x, double y, double z){
        setPositionDVX(new Space3D.Vec3(x,y,z));
    }
}
