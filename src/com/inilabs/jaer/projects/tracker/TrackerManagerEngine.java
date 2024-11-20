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

import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker.Cluster;

public class TrackerManagerEngine {

    private static final ch.qos.logback.classic.Logger log = 
        
    (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TrackerManagerEngine.class);
    
    private static final int MAX_TRACKER_AGENTS = 3; // Limit the number of TrackerAgentDrawables
    private final Map<String, TrackerAgentDrawable> agents = new HashMap<>();
    private final LinkedList<EventCluster> eventClusters = new LinkedList<>();
    private TrackerAgentDrawable currentBestAgent = null; // Track the current best agent
    private List<TrackerAgentDrawable> bestTrackerAgentList = new ArrayList<>();
    private PolarSpaceDisplay polarSpaceDisplay;
    
    private final List<TrackerAgentDrawable> trackerAgentDrawables = new ArrayList<>();
 
    
    private final Map<String, Color> originalColors = new HashMap<>(); // Track original colors
    private Color bestAgentColor = Color.RED; // Define the color for the best agents
 

    public void setPolarSpaceDisplay(PolarSpaceDisplay display) {
        this.polarSpaceDisplay = display;
    }

    
   
     /**
     * Updates the agent and cluster list with real sensor data clusters.
     * 
     * @param clusters List of RectangularClusterTracker.Cluster objects.
     */
    
    public void updateRCTClusterList(List<RectangularClusterTracker.Cluster> clusters) {
    // Step 1: Process clusters
    List<ClusterAdapter> adaptedClusters = clusters.stream()
            .map(RCTClusterAdapter::new)
            .collect(Collectors.toList());

    processClusters(adaptedClusters);
    processTrackers();

    // Step 2: Manage drawables in PolarSpaceDisplay
    // Remove old drawables from display
//    for (Drawable drawable : trackerAgentDrawables) {
 //       removeDrawableFromDisplay(drawable);
 //   }

    // trackerAgentDrawables.clear();

    // Create and add new drawables
   // for (RectangularClusterTracker.Cluster cluster : clusters) {
    //    TrackerAgentDrawable drawable = new TrackerAgentDrawable(cluster);
    //            
    //    trackerAgentDrawables.add(drawable);
    //    addDrawableToDisplay(drawable);
   // }
}

    
    
    /**
     * Updates the agent and cluster list with test clusters.
     * 
     * @param testClusters List of TestCluster objects.
     */

public void updateTestClusterList(List<TestCluster> clusters) {
    processClusters(clusters); // TestCluster implements ClusterAdapter directly
    processTrackers();
}
    
   
    /**
     * Generic method to process clusters and encapsulate them as EventClusters.
     *
     * @param clusters List of input clusters (real or test).
     * @param isTestClusters Indicates whether the clusters are test data.
     */


private void processClusters(List<? extends ClusterAdapter> clusters) {
    if (polarSpaceDisplay != null) {
        // Update and remove only expired clusters from the display and TrackerManagerEngine
        eventClusters.removeIf(cluster -> {
            cluster.run(); // Update the cluster
            if (cluster.isExpired()) {
                // Remove from PolarSpaceDisplay
                polarSpaceDisplay.removeDrawable(cluster.getKey());
                log.info("Removed expired cluster with key: {}", cluster.getKey());
                return true; // Mark for removal from eventClusters
            }
            return false; // Retain non-expired clusters
        });
    }

    // Process new clusters
    for (ClusterAdapter adapter : clusters) {
        if (adapter == null) {
            log.warn("Encountered a null ClusterAdapter, skipping.");
            continue;
        }

        // Create a new EventCluster from the adapter
        EventCluster eventCluster = EventCluster.fromClusterAdapter(adapter);
        eventClusters.add(eventCluster);

        // Add to PolarSpaceDisplay if it's not null
        if (polarSpaceDisplay != null) {
            polarSpaceDisplay.addDrawable(eventCluster);
        }

        // Link the cluster to its tracker agent
        TrackerAgentDrawable agent = findOrCreateAgent(eventCluster);
        agent.addCluster(eventCluster);
    }

    updateBestTrackerAgentList();
}

    private void processTrackers() {
    // Gather the current cluster keys
    Set<String> currentClusterKeys = eventClusters.stream()
        .map(EventCluster::getKey)
        .collect(Collectors.toSet());

    Iterator<Map.Entry<String, TrackerAgentDrawable>> agentIterator = agents.entrySet().iterator();
    while (agentIterator.hasNext()) {
        Map.Entry<String, TrackerAgentDrawable> entry = agentIterator.next();
        TrackerAgentDrawable agent = entry.getValue();

        // Remove orphaned clusters
        agent.getClusters().removeIf(cluster -> 
            cluster.getEnclosedCluster() == null || 
            !currentClusterKeys.contains(cluster.getEnclosedCluster().getKey()));

        // Remove agents with zero support
        if (agent.getSupportQuality() == 0) {
            agentIterator.remove();
            if (polarSpaceDisplay != null) {
                polarSpaceDisplay.removeDrawable(agent.getKey());
            }
            continue; // Skip further processing for this agent
        }

        // Optimize agent's position
        optimizeAgentPosition(agent);
    }
}

/**
 * Optimize the position of a tracker agent by minimizing its summed distance to the supported clusters.
 */
    
private void optimizeAgentPosition(TrackerAgentDrawable agent) {
    List<EventCluster> clusters = agent.getClusters();

    if (clusters.isEmpty()) {
        return; // No clusters to optimize
    }

    // Calculate the centroid in 2D (azimuth and elevation)
    double sumAzimuth = 0;
    double sumElevation = 0;

    for (EventCluster cluster : clusters) {
        float azimuth = cluster.getAzimuth();    // Assuming clusters have getAzimuth()
        float elevation = cluster.getElevation(); // Assuming clusters have getElevation()
        sumAzimuth += azimuth;
        sumElevation += elevation;
    }

    int clusterCount = clusters.size();
    double optimizedAzimuth = sumAzimuth / clusterCount;
    double optimizedElevation = sumElevation / clusterCount;

    // Update agent's position to the centroid
    agent.setAzimuth((float) optimizedAzimuth);
    agent.setElevation((float) optimizedElevation);

    // Optionally, calculate the optimization cost (summed distances in 2D)
    double totalDistance = 0;
    for (EventCluster cluster : clusters) {
        float azimuth = cluster.getAzimuth();
        float elevation = cluster.getElevation();
        totalDistance += Math.sqrt(
            Math.pow(azimuth - optimizedAzimuth, 2) +
            Math.pow(elevation - optimizedElevation, 2)
        );
    }

    agent.setOptimizationCost((float)totalDistance); // Example method to store optimization data
}
    

    
    
    private TrackerAgentDrawable findOrCreateAgent(EventCluster cluster) {
        TrackerAgentDrawable nearestAgent = findNearestAgent(cluster);
        if (nearestAgent == null) {
            nearestAgent = createNewAgent(cluster);
        }
        return nearestAgent;
    }

    private TrackerAgentDrawable findNearestAgent(EventCluster cluster) {
        TrackerAgentDrawable nearestAgent = null;
        float minDistance = Float.MAX_VALUE;

        for (TrackerAgentDrawable agent : agents.values()) {
            float distance = calculateDistance(agent, cluster);
            if (distance < minDistance && distance < 10) {
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

    private TrackerAgentDrawable createNewAgent(EventCluster cluster) {
        TrackerAgentDrawable agent = new TrackerAgentDrawable();
        agent.setAzimuth(cluster.getAzimuth());
        agent.setElevation(cluster.getElevation());
        agent.setSize(4f);
        addAgent(agent);

        if (polarSpaceDisplay != null) {
            polarSpaceDisplay.addDrawable(agent);
        }

        cluster.setEnclosingAgent(agent);
        return agent;
    }

    
    
    private void addAgent(TrackerAgentDrawable agent) {
        if (agents.size() >= MAX_TRACKER_AGENTS) {
            removeLeastSignificantAgent();
        }
        agents.put(agent.getKey(), agent);
    }
    
     private void removeLeastSignificantAgent() {
        TrackerAgentDrawable leastSignificantAgent = agents.values().stream()
                .min(Comparator.comparingDouble(TrackerAgentDrawable::getSupportQuality))
                .orElse(null);

        if (leastSignificantAgent != null) {
            agents.remove(leastSignificantAgent.getKey());
            if (polarSpaceDisplay != null) {
                polarSpaceDisplay.removeDrawable(leastSignificantAgent.getKey());
            }
            log.info("Removed least significant agent: {}", leastSignificantAgent.getKey());
        }
    }


     public void setBestAgentColor(Color color) {
        this.bestAgentColor = color;
    }
      
     
     
     public void updateBestTrackerAgentList() {
        // Determine the best agent based on support quality
        List<TrackerAgentDrawable> bestAgents = agents.values().stream()
                .sorted(Comparator.comparingDouble(TrackerAgentDrawable::getSupportQuality).reversed())
                .limit(MAX_TRACKER_AGENTS)
                .collect(Collectors.toList());

        // Restore the color of the previously highlighted best agent
        if (currentBestAgent != null && originalColors.containsKey(currentBestAgent.getKey())) {
            currentBestAgent.setColor(originalColors.get(currentBestAgent.getKey()));
        }

        // Highlight the new best agent
        if (!bestAgents.isEmpty()) {
            TrackerAgentDrawable bestAgent = bestAgents.get(0); // Top agent
            if (!originalColors.containsKey(bestAgent.getKey())) {
                originalColors.put(bestAgent.getKey(), bestAgent.getColor()); // Backup original color
            }
            bestAgent.setColor(bestAgentColor);
            currentBestAgent = bestAgent;
        }

        // Update the bestTrackerAgentList
        bestTrackerAgentList.clear();
        bestTrackerAgentList.addAll(bestAgents);

        // Enforce the limit on the number of TrackerAgentDrawables
        enforceAgentLimit();
    }

    private void enforceAgentLimit() {
        if (agents.size() > MAX_TRACKER_AGENTS) {
            List<TrackerAgentDrawable> excessAgents = agents.values().stream()
                    .sorted(Comparator.comparingDouble(TrackerAgentDrawable::getSupportQuality))
                    .limit(agents.size() - MAX_TRACKER_AGENTS)
                    .collect(Collectors.toList());

            for (TrackerAgentDrawable agent : excessAgents) {
                agents.remove(agent.getKey());
                if (polarSpaceDisplay != null) {
                    polarSpaceDisplay.removeDrawable(agent.getKey());
                }
                if (originalColors.containsKey(agent.getKey())) {
                    originalColors.remove(agent.getKey());
                }
            }
        }
    }

    public List<TrackerAgentDrawable> getBestTrackerAgentList() {
        return new ArrayList<>(bestTrackerAgentList); // Return a copy to avoid external modification
    }

    public void removeAgent(TrackerAgentDrawable drawable) {
        trackerAgentDrawables.remove(drawable);
        removeDrawableFromDisplay(drawable);
    }
    
        
    /**
 * Returns the best TrackerAgentDrawable based on the highest support quality.
 *
 * @return The TrackerAgentDrawable with the highest support quality, or null if no agents exist.
 */
public TrackerAgentDrawable getBestTrackerAgentDrawable() {
    return agents.values().stream()
            .max(Comparator.comparingDouble(TrackerAgentDrawable::getSupportQuality))
            .orElse(null);
}

    private void addDrawableToDisplay(Drawable drawable) {
        if (polarSpaceDisplay != null) {
            polarSpaceDisplay.addDrawable(drawable);
        }
    }

    private void removeDrawableFromDisplay(Drawable drawable) {
        if (polarSpaceDisplay != null) {
            polarSpaceDisplay.removeDrawable(drawable.getKey());
        }
    }


}
