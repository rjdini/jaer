package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.Space3D;

/**
 * Strategy to initialize a Space3D world (agents, targets, sliders, etc.).
 * Implementations must be side-effect free beyond mutating the provided world.
 */
@FunctionalInterface
public interface WorldInitializer {
    void init(Space3D world) throws Exception;
}
