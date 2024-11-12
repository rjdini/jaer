/*
 * Copyright (C) 2024 rjd.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.inilabs.jaer.projects.tracker;

import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker.Cluster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.gimbal.FieldOfView;
import com.inilabs.jaer.projects.gui.ActionType;
import java.util.function.BiConsumer;
import com.inilabs.jaer.projects.gui.BasicDrawable;
        
public class EventCluster extends BasicDrawable {
    private final Cluster cluster;          // Reference to the Cluster instance
    private TrackerAgentDrawable enclosingAgent;  // Reference to the enclosing agent
    private Color color = Color.BLACK;                    // Color of the cluster for drawing
    private float size = 2.0f;
   private static FieldOfView fov = FieldOfView.getInstance();
    private BiConsumer<ActionType, String> parentCallback ;
    
    public EventCluster(Cluster cluster, TrackerAgentDrawable agent) {
        this.cluster = cluster;
        this.enclosingAgent = agent;
        this.color = agent.getColor();      // Use the enclosing agent’s color
    }

    public EventCluster(TrackerAgentDrawable agent) {
        this.cluster = null;  // for testing
        this.enclosingAgent = agent;
        this.color = agent.getColor();      // Use the enclosing agent’s color
    }
    
    // Set the enclosing agent and update color to match agent
    public void setEnclosingAgent(TrackerAgentDrawable agent) {
        this.enclosingAgent = agent;
        this.color = agent.getColor();
    }
        
    public float getSupportQuality() {
        return (float)  1.0f  ;  // until we find a better method!
    }
   
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);

        // Calculate the cluster's location in relation to the enclosing agent
        int centerX = enclosingAgent.getCenterX();
        int centerY = enclosingAgent.getCenterY();
        float azimuthScale = enclosingAgent.getAzimuthScale();
        float elevationScale = enclosingAgent.getElevationScale();

        // Calculate the position for drawing based on the cluster’s azimuth and elevation
        int x = centerX + (int) ((getAzimuth() - enclosingAgent.getAzimuth()) * azimuthScale);
        int y = centerY - (int) ((getElevation() - enclosingAgent.getElevation()) * elevationScale);

        // Draw the cluster as a small circle
        g2d.fillOval(x - 3, y - 3, 6, 6);

        // Draw a line connecting the cluster to its enclosing agent
        int agentX = centerX;
        int agentY = centerY;
        g2d.drawLine(agentX, agentY, x, y);
    }
}
