package com.inilabs.jaer.projects.space3d;

import com.inilabs.jaer.projects.agents.api.Agent3DInterface;

/** Contract for Space3D agents that can be rendered by FlyingBlobGenerator. */
public interface FBGTarget extends Agent3DInterface {

    /** Physical diameter in meters used for perspective sizing. */
    float getPhysicalDiameterM();

    /** Shape to render on the DVS image plane. */
    TargetShape getShape();

    /** Optional: per-target event density multiplier (defaults to 1.0 if unused). */
    default float getDensityScale() { return 1.0f; }
}
