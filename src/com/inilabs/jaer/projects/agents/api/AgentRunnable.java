package com.inilabs.jaer.projects.agents.api;

import com.inilabs.jaer.projects.space3d.Space3D;

/**
 * AgentRunnable: optional capability for agents to own their motion/logic.
 * Called by a scheduler/loop with a fixed timestep.
 */

// com.inilabs.jaer.projects.agents.api.AgentRunnable
import com.inilabs.jaer.projects.space3d.Space3D;

public interface AgentRunnable extends Activatable {
    void runStep(double dtSec, Space3D world);
}


