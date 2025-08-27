package com.inilabs.jaer.projects.space3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified Space3D container with registry and coordinate utilities.
 * 
 * Default origin: ETH Hönggerberg (approx)
 *   47.306505° N, 8.549888° E, altitude 542 m.
 */
public final class Space3D {

    /* ===== Basic vector ===== */
    public static final class Vec3 {
        public final double x,y,z;
        public Vec3(double x,double y,double z){ this.x=x; this.y=y; this.z=z; }
        public Vec3 add(Vec3 o){ return new Vec3(x+o.x,y+o.y,z+o.z); }
        public Vec3 sub(Vec3 o){ return new Vec3(x-o.x,y-o.y,z-o.z); }
        public Vec3 scale(double s){ return new Vec3(s*x,s*y,s*z); }
        public double norm(){ return Math.sqrt(x*x+y*y+z*z); }
        public Vec3 normalize(){ double n=norm(); return n>0?scale(1.0/n):this; }
        @Override public String toString(){ return String.format("(%.3f,%.3f,%.3f)",x,y,z); }
    }

    /* ===== Origin fields ===== */
    private double originLatDeg;
    private double originLonDeg;
    private double originAltM;
    private double halfExtentM = 500.0;

    /* ===== Constructors ===== */
    /** Default origin: 47.306505N, 8.549888E, alt=542 m. */
    public Space3D() {
        this(47.306505, 8.549888, 542.0);
    }

    /** Custom origin. */
    public Space3D(double latDeg, double lonDeg, double altM) {
        this.originLatDeg = latDeg;
        this.originLonDeg = lonDeg;
        this.originAltM   = altM;
    }

    /* ===== Getters / setters ===== */
    public double getOriginLatDeg() { return originLatDeg; }
    public void setOriginLatDeg(double latDeg) { this.originLatDeg = latDeg; }

    public double getOriginLonDeg() { return originLonDeg; }
    public void setOriginLonDeg(double lonDeg) { this.originLonDeg = lonDeg; }

    public double getOriginAltM() { return originAltM; }
    public void setOriginAltM(double altM) { this.originAltM = altM; }

    public double getHalfExtentM() { return halfExtentM; }
    public void setHalfExtentM(double halfExtentM) { this.halfExtentM = halfExtentM; }

    /* ===== Agent registry ===== */
    public Map<String,Agent3DInterface> agents = new ConcurrentHashMap<>();
    public void addAgent(Agent3DInterface a){ getAgents().put(a.getKey(),a); }
    public Agent3DInterface getAgent(String key){ return getAgents().get(key); }
    public Agent3DInterface removeAgent(String key){ return getAgents().remove(key); }
    public void clearAgents(){ getAgents().clear(); }
    public Map<String,Agent3DInterface> viewAgents(){ return Map.copyOf(getAgents()); }

    /* ===== Utilities ===== */
    public Agent3D.AzElDist azElDistBetween(String keyA, String keyB){
        Agent3DInterface A = getAgents().get(keyA), B = getAgents().get(keyB);
        if (A==null||B==null) throw new IllegalArgumentException("Missing agent(s)");
        return A.azElDistFromDVX(B.getPositionDVX());
    }

    @Override
    public String toString() {
        return String.format("Space3D origin lat=%.6f, lon=%.6f, alt=%.2f m, halfExtent=%.1f m",
                originLatDeg, originLonDeg, originAltM, halfExtentM);
    }

    /**
     * @return the agents
     */
    public Map<String,Agent3DInterface> getAgents() {
        return agents;
    }

    /**
     * @param agents the agents to set
     */
    public void setAgents(Map<String,Agent3DInterface> agents) {
        this.agents = agents;
    }
}
