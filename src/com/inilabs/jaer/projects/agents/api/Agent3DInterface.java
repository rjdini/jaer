package com.inilabs.jaer.projects.agents.api;

import com.inilabs.jaer.projects.space3d.Space3D.Vec3;

/**
 * Contract for all 3D agents inhabiting Space3D.
 */
public interface Agent3DInterface {
    String getKey();
    Agent3DTypes.ObjectType getType();

    // Pose in DVXSpace (ENU)
    Vec3 getPosition3D();
    void setPosition3D(Vec3 p);

    /** Yaw/Pitch/Roll in degrees */
    double[] getYawPitchRollDeg();          // {yaw, pitch, roll}
    void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg);

    // GPS position (LLA)
    double[] getLLA();                      // {latDeg, lonDeg, altM}
    void setLLA(double latDeg, double lonDeg, double altM);

    // Query relative to a reference position in DVXSpace (ENU)
default Agent3DTypes.AzElDist azElDistFromDVX(Vec3 refDVX) {
  
        Vec3 d = getPosition3D().sub(refDVX);
        double az = Math.toDegrees(Math.atan2(d.x, d.z));
        double el = Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z)));
        return AgentMath.azElDistFromDVX(refDVX, getPosition3D());
    }
}
