package com.inilabs.jaer.projects.agent.api;

import java.awt.Graphics2D;

/**
 * AgentPolarDrawable: optional capability for an agent to draw itself in Polar space.
 * The adapter computes az/el and screen coords (x,y), then calls this method.
 */
public interface AgentPolarDrawable {

    /**
     * Draw the agent in Polar space at the given already-projected position.
     *
     * @param g2   graphics
     * @param azDeg computed azimuth (deg) of the agent from the tracker
     * @param elDeg computed elevation (deg) of the agent from the tracker
     * @param x     screen x (pixels)
     * @param y     screen y (pixels)
     */
    void drawInPolar(Graphics2D g2, float azDeg, float elDeg, int x, int y);
}
