/*
 * Copyright (C) 2024 rjd.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 
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

import com.inilabs.jaer.projects.gui.ActionType;
import com.inilabs.jaer.projects.gui.Drawable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;

public class TrackerManager {
 
    private final Map<String, TrackerAgentDrawable> agents = new HashMap<>(); // Map of drawables managed by key
    private  final PolarSpaceDisplay display;  
    
   
     public TrackerManager(PolarSpaceDisplay display) {
        this.display = display;  // Ensure only this instance is used
     //    javax.swing.Timer updateTimer = new javax.swing.Timer(100, e -> updateAgents());
     //   updateTimer.start();
    }
     
    
       
    public void processClusters(List<EventCluster> clusters) {
        // Sort clusters and assign to agents or create new ones
        for (EventCluster cluster : clusters) {
            cluster.setSize(2.0f);
            display.addDrawable(cluster);
            TrackerAgentDrawable nearestAgent = findNearestAgent(cluster);
            if (nearestAgent != null) {
                nearestAgent.addCluster(cluster);
            } else {
                createNewAgent(cluster);
            }
        }
    }

     private void deployTrackerAgent( EventCluster cluster ) {  // Assign cluster to an agent or create new agent if necessary
            TrackerAgentDrawable agent = findOrCreateAgent(cluster);
            agent.addCluster(cluster);
        //    display.addDrawable(cluster);
       if (!agents.containsKey(agent.getKey())) {
                addAgent(agent);
                display.addDrawable(agent); // Add agent to the GUI for visualizatio
            }
     }
     
    private TrackerAgentDrawable findOrCreateAgent(EventCluster cluster) {
        // Simple implementation to find the nearest agent or create a new one
        TrackerAgentDrawable nearestAgent = null;
        float minDistance = Float.MAX_VALUE;
         for (TrackerAgentDrawable agent : agents.values()) {
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
          for (TrackerAgentDrawable agent : agents.values()) {
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
        newAgent.setSize(5.0f);
        newAgent.setColor(Color.green);
        newAgent.addCluster(cluster);
        addAgent(newAgent);
    }

    public void startLoggingAllAgents() {
        for (TrackerAgentDrawable agent : agents.values()) {
            agent.setLogging(true);
        }
    }

    
    public void stopLoggingAllAgents() {
            for (TrackerAgentDrawable agent : agents.values()) {
            agent.setLogging(false);
        }
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }
    
  
    /**
     * Adds a drawable to the display.
     * @param drawable The drawable to add.
     */
     public void addAgent(  TrackerAgentDrawable agent) {
        agents.put(agent.getKey(), agent);

        // Set the callback to remove the drawable
        agent.setParentCallback((action, key) -> {
            if (action == ActionType.REMOVE) {
                removeAgent(key);
            }
        });
        
        display.addDrawable(agent);  // Add agent to the correct display
        display.repaint();  
    }
    
     public List<String> getAgentKeys() {
    return new ArrayList<>(agents.keySet());
    }
     
      /**
     * Removes an agent by its key.
     * @param key The unique key of the drawable to remove.
     */
    // Ensure removal happens on the same display instance
    public void removeAgent(String key) {
        agents.remove(key);
        display.removeDrawable(key);
        display.repaint();
    }
    

    public void updateAgents() {
     for (TrackerAgentDrawable agent : agents.values()) {
            agent.run();
        }
    }
   
}

   