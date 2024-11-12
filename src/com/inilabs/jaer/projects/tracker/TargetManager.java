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

public class TargetManager {
    private final List<TargetAgentDrawable> agents = new ArrayList<>();
    private  PolarSpaceDisplay display = null;  
    
     public TargetManager(PolarSpaceDisplay display) {
        this.display = display;
    }
  
    public void processClusters(List<EventCluster> clusters) {
        // Sort clusters and assign to agents or create new ones
        for (EventCluster cluster : clusters) {
            TargetAgentDrawable nearestAgent = findNearestAgent(cluster);
            if (nearestAgent != null) {
                nearestAgent.addCluster(cluster);
            } else {
                createNewAgent(cluster);
            }
        }
    }

    
    private TargetAgentDrawable findNearestAgent(EventCluster cluster) {
        TargetAgentDrawable nearestAgent = null;
        float minDistance = Float.MAX_VALUE;
        for (TargetAgentDrawable agent : agents) {
            float distance = calculateDistance(agent, cluster);
            if (distance < minDistance && distance < 10) { // 10-degree threshold
                minDistance = distance;
                nearestAgent = agent;
            }
        }
        return nearestAgent;
    }

    private float calculateDistance(TargetAgentDrawable agent, EventCluster cluster) {
        float deltaAzimuth = agent.getAzimuth() - cluster.getAzimuth();
        float deltaElevation = agent.getElevation() - cluster.getElevation();
        return (float) Math.sqrt(deltaAzimuth * deltaAzimuth + deltaElevation * deltaElevation);
    }

    private void createNewAgent(EventCluster cluster) {
        TargetAgentDrawable newAgent = new TargetAgentDrawable();
        newAgent.addCluster(cluster);
        agents.add(newAgent);
    }

    public void startLoggingAllAgents() {
        for (TargetAgentDrawable agent : agents) {
            agent.setLogging(true);
        }
    }

    public void stopLoggingAllAgents() {
        for (TargetAgentDrawable agent : agents) {
            agent.setLogging(false);
        }
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }
    
    //************************* test functions
    
     // Example of adding random clusters for testing
    public void addRandomClusters(int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            float azimuth = random.nextFloat() * 60 - 30;   // Random azimuth around heading
            float elevation = random.nextFloat() * 40 - 20; // Random elevation around heading
          
            EventCluster cluster = new EventCluster(new TargetAgentDrawable());
            cluster.setAzimuth(azimuth);
            cluster.setElevation(elevation);

            // Assign cluster to an agent or create new agent if necessary
            TargetAgentDrawable agent = findOrCreateAgent(cluster);
            agent.addCluster(cluster);
            if (!agents.contains(agent)) {
                agents.add(agent);
                display.addDrawable(agent); // Add agent to the GUI for visualization
                display.repaint();
            }
        }
    }

    private TargetAgentDrawable findOrCreateAgent(EventCluster cluster) {
        // Simple implementation to find the nearest agent or create a new one
        TargetAgentDrawable nearestAgent = null;
        float minDistance = Float.MAX_VALUE;
        for (TargetAgentDrawable agent : agents) {
            float distance = calculateDistance(agent, cluster);
            if (distance < minDistance && distance < 10) { // 10-degree threshold
                minDistance = distance;
                nearestAgent = agent;
            }
        }
        if (nearestAgent == null) {
            nearestAgent = new TargetAgentDrawable();
            nearestAgent.setColor(new Color((int) (Math.random() * 0x1000000))); // Random color
        }
        return nearestAgent;
    }


    // Method to update agents and manage cluster quality
    public void update() {
        for (TargetAgentDrawable agent : agents) {
            agent.run(); // Execute each agent's run() method to update positions
        }
    }
}

   