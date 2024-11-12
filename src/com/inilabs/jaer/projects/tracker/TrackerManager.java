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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.Color;

public class TrackerManager {
    private final List<TrackerAgentDrawable> agents = new ArrayList<>();
    private  PolarSpaceDisplay display = null;  
    
     public TrackerManager(PolarSpaceDisplay display) {
        this.display = display;
    }
  
    public void processClusters(List<EventCluster> clusters) {
        // Sort clusters and assign to agents or create new ones
        for (EventCluster cluster : clusters) {
            TrackerAgentDrawable nearestAgent = findNearestAgent(cluster);
            if (nearestAgent != null) {
                nearestAgent.addCluster(cluster);
            } else {
                createNewAgent(cluster);
            }
        }
    }

     private void deloyTrackerAgent( EventCluster cluster ) {  // Assign cluster to an agent or create new agent if necessary
            TrackerAgentDrawable agent = findOrCreateAgent(cluster);
            agent.addCluster(cluster);
        //    display.addDrawable(cluster);
            if (!agents.contains(agent)) {
                agents.add(agent);
                display.addDrawable(agent); // Add agent to the GUI for visualization
                display.repaint();
            }
     }
     
    private TrackerAgentDrawable findOrCreateAgent(EventCluster cluster) {
        // Simple implementation to find the nearest agent or create a new one
        TrackerAgentDrawable nearestAgent = null;
        float minDistance = Float.MAX_VALUE;
        for (TrackerAgentDrawable agent : agents) {
            float distance = calculateDistance(agent, cluster);
            if (distance < minDistance && distance < 10) { // 10-degree threshold
                minDistance = distance;
                nearestAgent = agent;
            }
        }
        if (nearestAgent == null) {
            nearestAgent = new TrackerAgentDrawable();
            nearestAgent.setColor(new Color((int) (Math.random() * 0x1000000))); // Random color
        }
        return nearestAgent;
    }

    
    private TrackerAgentDrawable findNearestAgent(EventCluster cluster) {
        TrackerAgentDrawable nearestAgent = null;
        float minDistance = Float.MAX_VALUE;
        for (TrackerAgentDrawable agent : agents) {
            float distance = calculateDistance(agent, cluster);
            if (distance < minDistance && distance < 10) { // 10-degree threshold
                minDistance = distance;
                nearestAgent = agent;
            }
        }
        return nearestAgent;
    }

    private float calculateDistance(TrackerAgentDrawable agent, EventCluster cluster) {
        float deltaAzimuth = agent.getAzimuth() - cluster.getAzimuth();
        float deltaElevation = agent.getElevation() - cluster.getElevation();
        return (float) Math.sqrt(deltaAzimuth * deltaAzimuth + deltaElevation * deltaElevation);
    }

    private void createNewAgent(EventCluster cluster) {
        TrackerAgentDrawable newAgent = new TrackerAgentDrawable();
        newAgent.addCluster(cluster);
        agents.add(newAgent);
    }

    public void startLoggingAllAgents() {
        for (TrackerAgentDrawable agent : agents) {
            agent.setLogging(true);
        }
    }

    public void stopLoggingAllAgents() {
        for (TrackerAgentDrawable agent : agents) {
            agent.setLogging(false);
        }
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }
    
  
    // Method to update agents and manage cluster quality
    public void update() {
        for (TrackerAgentDrawable agent : agents) {
            agent.run(); // Execute each agent's run() method to update positions
        }
    }
   
}

   