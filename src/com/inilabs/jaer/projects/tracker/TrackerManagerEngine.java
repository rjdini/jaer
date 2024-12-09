package com.inilabs.jaer.projects.tracker;

import com.inilabs.jaer.projects.cog.SpatialAttention;
import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.Color;
import java.util.*;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.LoggerFactory;


public class TrackerManagerEngine {
    private static final int MAX_TRACKER_AGENTS = 3;
    private static final int MAX_CLUSTERS_PER_AGENT = 5; // Limit on clusters per agent
    private PolarSpaceDisplay polarSpaceDisplay = PolarSpaceDisplay.getInstance();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private TrackerAgentDrawable currentBestAgent = null;
    private FieldOfView fov = FieldOfView.getInstance();
    private long defaultAgentLifeTimeMillis = 5000; // 10 secs
     private long defaultEventClusterLifeTimeMillis =2000; // 2 secs
     private long lifeTimeExtensionMillis = 0; // reward for good agent taking on new cluster
     private TrackerAgentDrawable lastBestAgent = null; // Reference to the previous best tracker
     private List<TrackerAgentDrawable> bestTrackerAgentList = new ArrayList<>();  

    private final List<TrackerAgentDrawable> trackerAgentDrawables = new ArrayList<>();
    private final Map<String, Color> originalColors = new HashMap<>(); // Track original colors
   
 private final ConcurrentHashMap<String, TrackerAgentDrawable> agents = new ConcurrentHashMap<>();
private final CopyOnWriteArrayList<EventCluster> eventClusters = new CopyOnWriteArrayList<>();
    
    
    private Color bestAgentColor = Color.RED; // Define the color for the best agents

    private volatile boolean freshDataAvailable = false;
    private final SpatialAttention spatialAttention = SpatialAttention.getInstance();
    
    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrackerManagerEngine.class);
    private static boolean isSaccade = false;
    
    
    public TrackerManagerEngine() {
         // Start periodic processing task (10 Hz)
        scheduler.scheduleAtFixedRate(this::processPeriodically, 0, 100, TimeUnit.MILLISECONDS);
    }

//    public TrackerManagerEngine() {
//       // throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }

    
    private SpatialAttention getSpatialAttention() {
        return spatialAttention;
    }

    
//    public void setPolarSpaceDisplay(PolarSpaceDisplay display) {
//        this.polarSpaceDisplay = display;
//    }
    
    /**
     * Periodically processes clusters and trackers.
     */
    private synchronized void processPeriodically() {
//        if (!freshDataAvailable && eventClusters.isEmpty()) {
//            return; // Skip processing if no fresh data and no clusters to process
//        }
//        freshDataAvailable = false; // Reset the flag
        processClusters(Collections.emptyList()); // Process existing clusters
        processTrackers();
    }

    
    
     /**
     * Adapt RCT  (real sensor data) Clusters to generic cluster processing stream.
     *
     * @param clusters List of input clusters (real or test).
     * 
     *  newest bestest single streamin' process 22nov24
     */
    
public synchronized void updateRCTClusterList(List<RectangularClusterTracker.Cluster> clusters) {
   
    if (!isIsSaccade()) { 
    // Convert RectangularClusterTracker.Cluster to RCTClusterAdapter
    List<RCTClusterAdapter> adaptedClusters = clusters.stream()
        .map(cluster -> new RCTClusterAdapter(cluster))
        .collect(Collectors.toList());
    freshDataAvailable = true;
    processClusters(adaptedClusters);
    
    }
}

 /**
     * Dummy input for standard 'Test' Clusters to generic cluster processing stream.
     *
     * @param clusters List of input clusters (real or test).
     * 
     *  newest bestest single streamin' process 22nov24
     */   
public synchronized void updateTestClusterList(List<TestCluster> clusters) {
    if(!isIsSaccade()) {
    freshDataAvailable = true;
    processClusters(clusters);
    }
}



    /**
     * Generic method to process clusters and encapsulate them as EventClusters.
     *
     * @param clusters List of input clusters (real or test).
     */

private void processClusters(List<? extends ClusterAdapter> clusters) {
    // Step 1: Remove expired EventClusters
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

    // Step 2: Process new clusters
    for (ClusterAdapter adapter : clusters) {
        if (adapter == null) {
            log.warn("Encountered a null ClusterAdapter, skipping.");
            continue;
        }

        String freshClusterKey = adapter.getKey(); // Get the key of the fresh cluster
        boolean clusterReplaced = false;

        // Check if the fresh cluster's key is already present in existing EventClusters
        for (EventCluster eventCluster : eventClusters) {
            if (eventCluster.getEnclosedCluster().getKey().equals(freshClusterKey)) {
                // Replace the old cluster with the fresh one
                eventCluster.setEnclosedCluster(adapter);
                clusterReplaced = true;

               log.debug("Replaced existing cluster in EventCluster with key: {}", freshClusterKey);
                break;
            }
            
        }

        // If the fresh cluster's key was not found, create a new EventCluster
        if (!clusterReplaced) {
            EventCluster newEventCluster = EventCluster.fromClusterAdapter(adapter, defaultEventClusterLifeTimeMillis);
            eventClusters.add(newEventCluster);

            if (polarSpaceDisplay != null) {
                polarSpaceDisplay.addDrawable(newEventCluster);
            }

            //TrackerAgentDrawable agent = findOrCreateAgent(newEventCluster);
           // agent.addCluster(newEventCluster);

            log.debug("Created new EventCluster for cluster with key: {}", freshClusterKey);
        }
    }
         // Step 3: Run all EventClusters to update their locations derived  their new enclosed clusters
          for (EventCluster eventCluster : eventClusters) {
        eventCluster.run();
    }
}



    private void processTrackers() {
    // Step 1: Assign clusters to agents
    for (EventCluster eventCluster: eventClusters) {
        TrackerAgentDrawable nearestAgent = findNearestAgent(eventCluster);

        if (nearestAgent != null && calculateDistance(nearestAgent, eventCluster) <= 0.4 * fov.getFOVX()) {
            nearestAgent.addCluster(eventCluster);
            nearestAgent.extendLifetime(lifeTimeExtensionMillis); // Reward active agents
        } else {
            // Create a new agent for clusters with no nearby agent
            TrackerAgentDrawable newAgent = createNewAgent(eventCluster);
            newAgent.addCluster(eventCluster);
            agents.put(newAgent.getKey(), newAgent);
        }
    }

    // Step 2: Process agents
    List<String> agentsToRemove = new ArrayList<>();
    for (TrackerAgentDrawable agent : agents.values()) {
        agent.run(); // Update clusters and centroids

         // Check if agent is expired 
        if (agent.isExpired()) {
            log.debug("Removing expired tracker agent: {}", agent.getKey());
            removeDrawableFromDisplay(agent);
            agentsToRemove.add(agent.getKey());
        }
        
        // Check if agent is static (not moving) and remove if static for too long
        if (agent.isStatic() && agent.getClusters().isEmpty()) {
            log.debug("Removing static tracker agent: {}", agent.getKey());
            removeDrawableFromDisplay(agent);
            agentsToRemove.add(agent.getKey());
        }
    }
    // Remove expired agents
    for (String key : agentsToRemove) {
        agents.remove(key);
    }

    // Step 3: Update best tracker agent
    updateBestTrackerAgentList();
}

    
    
private void updateBestTrackerAgent() {
    TrackerAgentDrawable newBestAgent = agents.values().stream()
        .max(Comparator.comparingDouble(TrackerAgentDrawable::getSupportQuality))
        .orElse(null);

    if (lastBestAgent != null && lastBestAgent != newBestAgent) {
        lastBestAgent.revertColor(); // Revert color of the previous best tracker
    }

    if (newBestAgent != null) {
        newBestAgent.setColor(Color.RED); // Highlight the new best tracker
    }

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
        TrackerAgentDrawable newAgent = createNewAgent(cluster);
        agents.put(newAgent.getKey(), newAgent);
        return newAgent;
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
       if(getBestTrackerAgentDrawable() != null) {
           if(getBestTrackerAgentDrawable().getSupportQuality() > spatialAttention.getSupportQualityThreshold()) {
                 getSpatialAttention().setBestTrackerAgent(getBestTrackerAgentDrawable());  // good one
           }  else {
            getSpatialAttention().setBestTrackerAgent(null); // candidate score is not high enough
           }
        } else {
           getSpatialAttention().setBestTrackerAgent(null); // no candidate
       }
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
public synchronized TrackerAgentDrawable getBestTrackerAgentDrawable() {
    return agents.values().stream()
            .max(Comparator.comparingDouble(TrackerAgentDrawable::getSupportQuality))
            .orElse(null);
}

    /**
     * @return the isSaccade
     */
    public static boolean isIsSaccade() {
        return isSaccade;
    }

    /**
     * @param aIsSaccade the isSaccade to set
     */
    public static void setIsSaccade(boolean aIsSaccade) {
        isSaccade = aIsSaccade;
    }
    
}
