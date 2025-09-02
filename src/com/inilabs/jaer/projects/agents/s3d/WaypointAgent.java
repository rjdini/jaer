package com.inilabs.jaer.projects.agents.s3d;

import com.inilabs.jaer.projects.agents.api.AgentRunnable;
import com.inilabs.jaer.projects.agents.api.Agent3DTypes;
import com.inilabs.jaer.projects.agents.api.DrawableInPolar;
import com.inilabs.jaer.projects.agents.api.DrawableInSpace3D;
import com.inilabs.jaer.projects.agents.core.AbstractAgent;
import com.inilabs.jaer.projects.space3d.FBGTarget;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.TargetShape;
import com.inilabs.jaer.projects.space3d.WorldTransform;
import com.inilabs.jaer.projects.utils.AgentColors;
// imports needed:
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Polygon;

/**
 * TargetAgent: reciprocal motion (as before) + FBGTarget properties (size & shape).
 */

public class WaypointAgent extends AbstractAgent implements  DrawableInSpace3D,  DrawableInPolar,  AgentRunnable,  FBGTarget {

    private final Space3D.Vec3 p1;       // start waypoint
    private final Space3D.Vec3 p2;       // end waypoint
    private final double speedMps;       // constant speed along segment

    // Parametric position s in [0,1] along segment p(s) = p1 + s*(p2-p1).
    // Direction dir = +1 forward (p1->p2) or -1 backward (p2->p1).
    private double s = 0.0;
    private int dir = +1;

    // FBGTarget properties
    private float physicalDiameterM = 1.0f;
    private TargetShape shape = TargetShape.TRIANGLE;
    private float densityScale = 1.0f;     
        
    // FBGTarget fields as you have them…

    public WaypointAgent(String key, Space3D.Vec3 p1, Space3D.Vec3 p2, double speedMps){
        super(key, Agent3DTypes.ObjectType.WAYPOINT);
        if (speedMps <= 0) throw new IllegalArgumentException("speedMps must be > 0");
        this.p1 = p1; 
        this.p2 = p2; 
        this.speedMps = speedMps;
        this.posDVX = p1; // initial
    }
    
  
    /* ================= Agent3DInterface ================= */

    @Override public String getKey() { return key; }
   
    @Override
    public Space3D.Vec3 getPosition3D() {
        Space3D.Vec3 d = p2.sub(p1);
        return new Space3D.Vec3(p1.x + s * d.x, p1.y + s * d.y, p1.z + s * d.z);
    }

    @Override
    public void setPosition3D(Space3D.Vec3 p) {
        Space3D.Vec3 d = p2.sub(p1);
        double len2 = d.x*d.x + d.y*d.y + d.z*d.z;
        if (len2 == 0) { s = 0; return; }
        Space3D.Vec3 r = p.sub(p1);
        double t = (r.x*d.x + r.y*d.y + r.z*d.z) / len2;
        s = Math.max(0.0, Math.min(1.0, t));
    }
    
    @Override
    public void runStep(double dtSec, Space3D world) {
        tick(dtSec); // reuse your existing integrator exactly
    }

    @Override public double[] getYawPitchRollDeg() { return new double[]{0,0,0}; }
    @Override public void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg) { }
    @Override public double[] getLLA() { return new double[]{Double.NaN, Double.NaN, Double.NaN}; }
    @Override public void setLLA(double latDeg, double lonDeg, double altM) { }

    /* ================= FBGTarget ================= */

    @Override public float getPhysicalDiameterM() { return physicalDiameterM; }
    public void setPhysicalDiameterM(float d) { this.physicalDiameterM = d; }

    @Override public TargetShape getShape() { return shape; }
    public void setShape(TargetShape s) { this.shape = s; }

    @Override public float getDensityScale() { return densityScale; }
    public void setDensityScale(float ds) { this.densityScale = ds; }


    /** Advance motion by dt seconds; reflects at endpoints; safe for tests. */
    public void tick(double dt){
        if (dt <= 0) return;
        final Space3D.Vec3 d = p2.sub(p1);
        final double L = d.norm();
        if (L == 0) return;
        final double vOverL = speedMps / L;
        double ds = dir * vOverL * dt;
        s += ds;
        while (s > 1.0 || s < 0.0) {
            if (s > 1.0) { s = 2.0 - s; dir = -dir; }
            else         { s = -s;      dir = -dir; }
        }
    }
    
@Override
public void drawInSpace3D(Graphics2D g2, Space3D world, WorldTransform tx){
    Space3D.Vec3 p = getPosition3D();
    if (p == null) return;

    int sx = tx.toScreenX(p.x);
    int sy = tx.toScreenY(p.z);

    Color old = g2.getColor();
    Stroke oldS = g2.getStroke();
    int r= 4;
     g2.setColor(new Color(34, 139, 34));
    Polygon tri = new Polygon();
    tri.addPoint(sx, sy - r);
    tri.addPoint(sx - r, sy + r);
    tri.addPoint(sx + r, sy + r);
    g2.fillPolygon(tri);
    g2.setColor(Color.black);
    g2.drawPolygon(tri);
   
    g2.setColor(Color.BLACK);
    g2.drawString(getKey(), sx + r + 6, sy - r - 6);

    g2.setStroke(oldS);
    g2.setColor(old);
}

@Override
public void drawInPolar(Graphics2D g2, float azDeg, float elDeg, int x, int y) {
    Color old = g2.getColor();
    g2.setColor(Color.RED);
    g2.drawLine(x - 6, y, x + 6, y);
    g2.drawLine(x, y - 6, x, y + 6);
    g2.drawOval(x - 4, y - 4, 8, 8);
    g2.setColor(Color.BLACK);
    g2.drawString(key, x + 8, y - 8);
    g2.setColor(old);
}


}
