package com.inilabs.jaer.projects.space3d.api;

import com.inilabs.jaer.projects.space3d.Space3D;

/**
 * AgentRunnable: optional capability for agents to own their motion/logic.
 * Called by a scheduler/loop with a fixed timestep.
 */
public interface AgentRunnable {
    /**
     * Advance agent state by dtSec seconds in the given world.
     * Implementations should be fast and allocation-free when possible.
     */
    void runStep(double dtSec, Space3D world);

    /** Whether this agent should continue to be scheduled. */
    default boolean isActive(){ return true; }
}
