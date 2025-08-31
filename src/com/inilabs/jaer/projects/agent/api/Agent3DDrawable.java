package com.inilabs.jaer.projects.agent.api;

import com.inilabs.jaer.projects.space3d.Space3D;
import java.awt.Graphics2D;

/**
 * Optional capability: draw the agent in the Space3D (3D world) view.
 * The caller (panel/GUI) should provide a Graphics2D already transformed
 * to world meters (or pass a transform context if needed).
 */
public interface Agent3DDrawable {
    /**
     * Draw the agent in the Space3D view.
     * The Graphics2D is assumed to be in world coordinates (meters) OR
     * the hosting panel applies the appropriate transform before calling.
     */
    void drawInSpace3D(Graphics2D g2, Space3D world);
}
