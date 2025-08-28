package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.Agent3DInterface;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import net.sf.jaer.chip.AEChip;

/**
 * Factory abstraction for creating managers, agents, and filters.
 * This indirection lets the Executive assemble a DVX system without
 * hard-coding concrete classes or constructors.
 */
public interface AgentFactory {
    /** Create a tracker manager bound to the given chip. */
    TrackerManagerV2 createTrackerManager(AEChip chip);

    /** Create a SIM target agent in world coordinates (Space3D). */
    Agent3DInterface createTargetAgent(TargetSpec spec);

    /** Create an FBG filter; it should attach to the given chip. */
    FlyingBlobGenerator createFlyingBlobGenerator(AEChip chip);
}
