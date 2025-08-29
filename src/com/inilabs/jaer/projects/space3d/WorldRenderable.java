package com.inilabs.jaer.projects.space3d;

import java.awt.Graphics;

/** Agents that can draw themselves in the Space3D world (top-down XZ). */
public interface WorldRenderable {
    /** Draw using the provided world→screen transform. */
    void drawWorld(Graphics g, WorldTransform tx);
}

