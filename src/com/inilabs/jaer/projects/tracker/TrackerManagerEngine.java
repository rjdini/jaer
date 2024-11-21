package com.inilabs.jaer.projects.tracker;

import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.Color;
import java.util.*;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;


public class TrackerManagerEngine {
    private static final int MAX_TRACKER_AGENTS = 3;
    private static final int MAX_CLUSTERS_PER_AGENT = 5; // Limit on clusters per agent
    private final Map<String, TrackerAgentDrawable> agents = new HashMap<>();
    private final LinkedList<EventCluster> eventClusters = new LinkedList<>();
    private PolarSpaceDisplay polarSpaceDisplay;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private TrackerAgentDrawable currentBestAgent = null;
    private FieldOfView fov;
    private long defaultAgentLifeTimeMillis = 10000; // 10 secs
     private long defaultEventClusterLifeTimeMillis = 2000; // 2 secs
     private long lifeTimeExtensionMillis = 0; // reward for good agent taking on new cluster
     private TrackerAgentDrawable lastBestAgent = null; // Reference to the previous best tracker
     private List<TrackerAgentDrawable> bestTrackerAgentList = new ArrayList<>();  

    private final List<TrackerAgentDrawable> trackerAgentDrawables = new ArrayList<>();
    private final Map<String, Color> originalColors = new HashMap<>(); // Track original colors
   
    private Color bestAgentColor = Color.RED; // Define the color for the best agents

    private volatile boolean freshDataAvailable = false;

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrackerManagerEngine.class);

    public TrackerManagerEngine(FieldOfView fov) {
        this.fov = fov;
         // Start periodic processing task (10 Hz)
        scheduler.scheduleAtFixedRate(this::processPeriodically, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void setPolarSpaceDisplay(PolarSpaceDisplay display) {
        this.polarSpaceDisplay = display;
    }
    
    /**
     * Periodically processes clusters and trackers.
     */
    private synchronized void processPeriodically() {
        if (!freshDataAvailable && eventClusters.isEmpty()) {
            return; // Skip processing if no fresh data and no clusters to process
        }
        freshDataAvailable = false; // Reset the flag
        processClusters(Collections.emptyList()); // Process existing clusters
        processTrackers();
    }

    /**
     * Updates the agent and cluster list with real sensor data clusters.
     *
     * @param clusters List of RectangularClusterTracker.Cluster objects.
     */
    public synchronized void updateRCTClusterList(List<RectangularClusterTracker.Cluster> clusters) {
    freshDataAvailable = true; // Mark new data as available

    // Pass the FieldOfView instance to the RCTClusterAdapter constructor
    List<ClusterAdapter> adaptedClusters = clusters.stream()
            .map(cluster -> new RCTClusterAdapter(cluster, this.fov)) // Pass fov
            .collect(Collectors.toList());

    processClustersForRCT(adaptedClusters);
    processTrackers();
}
    
    /**
     * Updates the agent and cluster list with test clusters.
     *
     * @param testClusters List of TestCluster objects.
     */
    public synchronized void updateTestClusterList(List<TestCluster> clusters) {
        freshDataAvailable = true; // Mark new data as available
        processClusters(clusters);
        processTrackers();
    }

    /**
     * Processes clusters for RCT sources and encapsulates them as EventClusters.
     *
     * @param clusters List of RectangularClusterTracker.ClusterAdapter objects.
     */
    private void processClustersForRCT(List<? extends ClusterAdapter> clusters) {
        // Remove expired EventClusters and their drawables
        eventClusters.removeIf(cluster -> {
            cluster.run(); // Update the cluster
            if (cluster.isExpired()) {
                if (polarSpaceDisplay != null) {
                    polarSpaceDisplay.removeDrawable(cluster.getKey());
                }
                return true; // Remove expired cluster
            }
            return false; // Retain non-expired clusters
        });

        // Add new RCT clusters
        for (ClusterAdapter adapter : clusters) {
            if (adapter == null) {
                log.warn("Encountered a null ClusterAdapter, skipping.");
                continue;
            }

            EventCluster eventCluster = EventCluster.fromClusterAdapter(adapter, fov, defaultEventClusterLifeTimeMillis);
            eventClusters.add(eventCluster);

            if (polarSpaceDisplay != null) {
                polarSpaceDisplay.addDrawable(eventCluster);
            }

            TrackerAgentDrawable agent = findOrCreateAgent(eventCluster);
            agent.addCluster(eventCluster);
        }
    }

    /**
     * Generic method to process clusters and encapsulate them as EventClusters.
     *
     * @param clusters List of input clusters (real or test).
     */
    private void processClusters(List<? extends ClusterAdapter> clusters) {
        // Similar to processClustersForRCT
        eventClusters.removeIf(cluster -> {
            cluster.run(); // Update the cluster
            if (cluster.isExpired()) {
                if (polarSpaceDisplay != null) {
                    polarSpaceDisplay.removeDrawable(cluster.getKey());
                }
                return true; // Remove expired cluster
            }
            return false; // Retain non-expired clusters
        });

        // Add new clusters
        for (ClusterAdapter adapter : clusters) {
            if (adapter == null) {
                log.warn("Encountered a null ClusterAdapter, skipping.");
                continue;
            }

            EventCluster eventCluster = EventCluster.fromClusterAdapter(adapter,fov,  defaultEventClusterLifeTimeMillis);
            eventClusters.add(eventCluster);

            if (polarSpaceDisplay != null) {
                polarSpaceDisplay.addDrawable(eventCluster);
            }

            TrackerAgentDrawable agent = findOrCreateAgent(eventCluster);
            agent.addCluster(eventCluster);
        }
    }

    private void processTrackers() {
    for (EventCluster cluster : eventClusters) {
        TrackerAgentDrawable nearestAgent = findNearestAgent(cluster);

        if (nearestAgent != null && calculateDistance(nearestAgent, cluster) <= 0.4 * fov.getFOVX()) {
            nearestAgent.addCluster(cluster);
            nearestAgent.extendLifetime(lifeTimeExtensionMillis); // Reward active agents
        } else {
            // Create a new agent for clusters with no nearby agent
            TrackerAgentDrawable newAgent = createNewAgent(cluster);
            newAgent.addCluster(cluster);
            agents.put(newAgent.getKey(), newAgent);
        }
    }

    for (TrackerAgentDrawable agent : agents.values()) {
        agent.run(); // Update clusters and centroids

        // Check if agent is static (not moving) and remove if static for too long
        if (agent.isStatic() && agent.getClusters().isEmpty()) {
            log.info("Removing static tracker agent: {}", agent.getKey());
            agents.remove(agent.getKey());
            removeDrawableFromDisplay(agent);
        }
    }

    // Remove expired agents
    agents.entrySet().removeIf(entry -> {
        TrackerAgentDrawable agent = entry.getValue();
        if (agent.isExpired() && agent.getClusters().isEmpty()) {
            log.info("Removing expired tracker agent: {}", agent.getKey());
            removeDrawableFromDisplay(agent);
            return true;
        }
        return false;
    });

    updateBestTrackerAgent();
}

    private void updateBestTrackerAgent() {
    // Find the new best agent
    TrackerAgentDrawable newBestAgent = agents.values().stream()
        .max(Comparator.comparingDouble(TrackerAgentDrawable::getSupportQuality)) // Replace with a relevant metric
        .orElse(null);

    // Revert color of the last best tracker
    if (lastBestAgent != null && lastBestAgent != newBestAgent) {
        lastBestAgent.revertColor(); // Revert to the original color
    }

    // Highlight the new best tracker
    if (newBestAgent != null) {
        newBestAgent.setColor(Color.RED); // Highlight the new best tracker
    }

    // Update the reference to the last best tracker
    lastBestAgent = newBestAgent;
    currentBestAgent = newBestAgent;

    if (newBestAgent != null) {
        log.info("Updated BestAgent: {}", newBestAgent.getKey());
    }
}

    
    public void shutdown() {
        scheduler.shutdownNow(); // Stop periodic processing
    }

    private TrackerAgentDrawable findOrCreateAgent(EventCluster cluster) {
        TrackerAgentDrawable nearestAgent = findNearestAgent(cluster);
        if (nearestAgent == null) {
            nearestAgent = createNewAgent(cluster);
        }
        return nearestAgent;
    }

    private TrackerAgentDrawable findNearestAgent(EventCluster cluster) {
    return agents.values().stream()
        .min(Comparator.comparingDouble(agent -> calculateDistance(agent, cluster)))
        .orElse(null);
}
    
    private TrackerAgentDrawable createNewAgent(EventCluster cluster) {
        TrackerAgentDrawable agent = new TrackerAgentDrawable(defaultAgentLifeTimeMillis);
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

    private float calculateDistance(TrackerAgentDrawable agent, EventCluster cluster) {
        float deltaAzimuth = agent.getAzimuth() - cluster.getAzimuth();
        float deltaElevation = agent.getElevation() - cluster.getElevation();
        return (float) Math.sqrt(deltaAzimuth * deltaAzimuth + deltaElevation * deltaElevation);
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
        }
    }
    
   

    private void redistributeClusters(TrackerAgentDrawable agent) {
        List<EventCluster> excessClusters = agent.getClusters().stream()
            .sorted(Comparator.comparingDouble(cluster -> calculateDistance(agent, cluster)))
            .skip(MAX_CLUSTERS_PER_AGENT)
            .collect(Collectors.toList());

        for (EventCluster cluster : excessClusters) {
            agent.removeCluster(cluster);
            TrackerAgentDrawable nearestAgent = findNearestAgent(cluster);
            if (nearestAgent != null) {
                nearestAgent.addCluster(cluster);
            } else {
                TrackerAgentDrawable newAgent = createNewAgent(cluster);
                agents.put(newAgent.getKey(), newAgent);
            }
        }
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

    
    
    
    
}
