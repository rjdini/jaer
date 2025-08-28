package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.TargetShape;

/**
 * Simple value type that describes a target's trajectory and optional physical params.
 */
public class TargetSpec {
    public final String key;
    public final Space3D.Vec3 start;
    public final Space3D.Vec3 end;
    public final double speedMps;

    // Optional physical / visual params
    public float physicalDiameterM = Float.NaN;
    public float densityScale      = Float.NaN;
    public TargetShape shape       = null;

    public TargetSpec(String key, Space3D.Vec3 start, Space3D.Vec3 end, double speedMps){
        this.key = key;
        this.start = start;
        this.end = end;
        this.speedMps = speedMps;
    }

    public TargetSpec diameter(float meters){ this.physicalDiameterM = meters; return this; }
    public TargetSpec density(float scale)  { this.densityScale      = scale;  return this; }
    public TargetSpec shape(TargetShape s)  { this.shape             = s;      return this; }
}
