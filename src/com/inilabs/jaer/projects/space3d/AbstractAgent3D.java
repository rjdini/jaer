package com.inilabs.jaer.projects.space3d;

import java.util.Objects;
import com.inilabs.jaer.projects.space3d.Space3D.Vec3;

/**
 * Minimal base class for 3D agents. Extend for DVXplorer, target, waypoint.
 */
public abstract class AbstractAgent3D implements Agent3DInterface {
    protected final String key;
    protected final Agent3D.ObjectType type;
    protected Vec3 posDVX = new Vec3(0,0,0);
    protected double yawDeg = 0, pitchDeg = 0, rollDeg = 0;
    protected double latDeg = Double.NaN, lonDeg = Double.NaN, altM = Double.NaN;

    protected AbstractAgent3D(String key, Agent3D.ObjectType type) {
        this.key = Objects.requireNonNull(key);
        this.type = Objects.requireNonNull(type);
    }

    @Override public String getKey(){ return key; }
    @Override public Agent3D.ObjectType getType(){ return type; }
    @Override public Vec3 getPositionDVX(){ return posDVX; }
    @Override public void setPositionDVX(Vec3 p){ posDVX = Objects.requireNonNull(p); }
    @Override public double[] getYawPitchRollDeg(){ return new double[]{yawDeg,pitchDeg,rollDeg}; }
    @Override public void setYawPitchRollDeg(double yawDeg,double pitchDeg,double rollDeg){
        this.yawDeg=yawDeg; this.pitchDeg=pitchDeg; this.rollDeg=rollDeg;
    }
    @Override public double[] getLLA(){ return new double[]{latDeg,lonDeg,altM}; }
    @Override public void setLLA(double latDeg,double lonDeg,double altM){
        this.latDeg=latDeg; this.lonDeg=lonDeg; this.altM=altM;
    }
}
