package com.inilabs.jaer.projects.tracker;

import com.inilabs.jaer.projects.cog.SpatialAttention;
import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.utils.Vector2DUtil;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.*;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;

public class TrackerManagerEngine {

    private static final int MAX_TRACKER_AGENTS = 3;
    private static final int MAX_CLUSTERS_PER_AGENT = 5; // Limit on clusters per agent
    private final PolarSpaceDisplay polarSpaceDisplay;
    private final SpatialAttention spatialAttention;
    private final FieldOfView fov;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private TrackerAgentDrawable currentBestAgent = null;
   
    private TrackerAgentDrawable lastBestAgent = null; // Reference to the previous best tracker
    private List<TrackerAgentDrawable> bestTrackerAgentList = new ArrayList<>();

    private final List<TrackerAgentDrawable> trackerAgentDrawables = new ArrayList<>();
    private final Map<String, Color> originalColors = new HashMap<>(); // Track original colors

    private final ConcurrentHashMap<String, TrackerAgentDrawable> agents = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<EventCluster> eventClusters = new CopyOnWriteArrayList<>();

    private Color bestAgentColor = Color.RED; // Define the color for the best agents

    private volatile boolean freshDataAvailable = false;

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrackerManagerEngine.class);
    private static boolean isSaccade = false;

    public TrackerManagerEngine(FieldOfView fov, SpatialAttention spatialAttention, PolarSpaceDisplay polarDisplay) {
        this.fov = fov;
        this.spatialAttention = spatialAttention;
        this.polarSpaceDisplay = polarDisplay;
        // Start periodic processing task (10 Hz) -- 
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
     * Adapt RCT (real sensor data) Clusters to generic cluster processing
     * stream.
     *
     * Always process clusters when !isIsSaccade(). Apply the gimbal velocity
     * filter (to suppress background non target clusters ) only if
     * referenceVelocity != null && speed > threshold. Otherwise, process all
     * clusters unfiltered as usual
     *
     * newest bestest single streamin' process 26mar25
     *
     * @param clusters List of input clusters (real or test).
     *
     */
//public synchronized void updateRCTClusterList(List<RectangularClusterTracker.Cluster> clusters) {
//   
//    if (!isIsSaccade()) { 
//    // Convert RectangularClusterTracker.Cluster to RCTClusterAdapter
//    List<RCTClusterAdapter> adaptedClusters = clusters.stream()
//        .map(cluster -> new RCTClusterAdapter(cluster))
//        .collect(Collectors.toList());
//    freshDataAvailable = true;
//    processClusters(adaptedClusters);
//    processTrackers();
//    
//    }
//}
    
    
// Always process clusters when !isIsSaccade().
// Apply the velocity filter only if referenceVelocity != null && speed > threshold.
// Otherwise, process all clusters unfiltered.
//    
    public synchronized void updateRCTClusterList(List<RectangularClusterTracker.Cluster> clusters) {
        
    if(true) return;  //@@@@@@@@  DEBUG
    
    if (isIsSaccade()) return;

     List<RectangularClusterTracker.Cluster> filteredClusters = filterRCTClusterList(clusters);
    

    List<RCTClusterAdapter> adaptedClusters = filteredClusters.stream()
        .peek(c -> System.out.println("Passing cluster with velocity: " + c.getVelocity()))
        .map(RCTClusterAdapter::new)
        .toList();

    System.out.println("Total clusters AFTER filtering: " + adaptedClusters.size());

    freshDataAvailable = true;
    processClusters(adaptedClusters);
    processTrackers();
}

private synchronized List<RectangularClusterTracker.Cluster> filterRCTClusterList(List<RectangularClusterTracker.Cluster> clusters) {
    System.out.println("Total clusters BEFORE filtering: " + clusters.size());

    Point2D referenceVelocity = SpatialAttention.getGimbalReferenceVelocity();
    float minSpeedThreshold = SpatialAttention.getGimbalReferenceVelocityThreshold();

    if (referenceVelocity == null || magnitude(referenceVelocity) < minSpeedThreshold) {
        // Filter not enabled; return original
        return clusters;
    }

    return clusters.stream()
        .filter(cluster -> {
            Point2D clusterVelocity = cluster.getVelocity();
            boolean sameDir = isSameDirection(clusterVelocity, referenceVelocity);
            log.info("Cluster velocity {}, gimbal velocity {} -> isSameDirection = {}", clusterVelocity, referenceVelocity, sameDir);
            return sameDir;
        })
        .toList();
}

private boolean isSameDirection(Point2D clusterVel, Point2D referenceVel) {
    return Math.signum(clusterVel.getX()) == Math.signum(referenceVel.getX()) &&
           Math.signum(clusterVel.getY()) == Math.signum(referenceVel.getY());
}

private double magnitude(Point2D p) {
    return Math.hypot(p.getX(), p.getY());
}

   
    /**
     * Dummy input for standard 'Test' Clusters to generic cluster processing
     * stream.
     *
     * @param clusters List of input clusters (real or test).
     *
     * newest bestest single streamin' process 22nov24
     */
    public synchronized void updateTestClusterList(List<TestCluster> clusters) {
        if (!isIsSaccade()) {
            freshDataAvailable = true;
            processClusters(clusters);
            processTrackers();
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
                    cluster.close();
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
                    eventCluster.extendLifetime(eventCluster.getLifetimeExtensionMillis()); // reward  well supported eventClusters
                    clusterReplaced = true;
                    log.debug("Replaced existing cluster:  key {} in EventCluster: {},  with cluster: key: {}", 
                            eventCluster.enclosedCluster.getKey(),
                            eventCluster.getKey(),
                            freshClusterKey);
                    break; 
                }
            }

            // If the fresh cluster's key was not found, create a new EventCluster
            if (!clusterReplaced) {
                EventCluster newEventCluster = EventCluster.fromClusterAdapter(adapter);
                eventClusters.add(newEventCluster);

                if (polarSpaceDisplay != null) {
                    polarSpaceDisplay.addDrawable(newEventCluster);
                }

                TrackerAgentDrawable agent = findOrCreateAgent(newEventCluster);
                agent.addCluster(newEventCluster);
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
        for (EventCluster eventCluster : eventClusters) {
            TrackerAgentDrawable nearestAgent = findNearestAgent(eventCluster);

            if (nearestAgent != null && calculateDistance(nearestAgent, eventCluster) <= 0.4 * fov.getFOVX()) {
                nearestAgent.addCluster(eventCluster);
                nearestAgent.extendLifetime(nearestAgent.getLifeTimeExtensionMillis()); // Reward active agents
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
            agents.get(key).close();
            agents.remove(key);
        }

        // Step 3: Update best tracker agent
        updateBestTrackerAgentList();
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
        TrackerAgentDrawable agent = new TrackerAgentDrawable(cluster.getAzimuth(), cluster.getElevation());
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
            leastSignificantAgent.close();
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
        if (getBestTrackerAgentDrawable() != null) {
            
           // if there is bestTrackerAgent, then check if its cluster support quality is high enough  
            if (getBestTrackerAgentDrawable().getSupportQuality() > spatialAttention.getSupportQualityThreshold()) {
                               log.debug("UPDATE BEST AGENT--- BestTrackerAgent: {}   supportQuality: {}  threshold: {}  azi: {}, ele: {}",
                                getBestTrackerAgentDrawable().getKey(),    String.format("%.2f", 
                                getBestTrackerAgentDrawable().getSupportQuality()),  
                                String.format("%.2f",getSpatialAttention(). getSupportQualityThreshold()),
                                getBestTrackerAgentDrawable().getAzimuth(),
                                getBestTrackerAgentDrawable().getElevation());
         
                getSpatialAttention().setBestTrackerAgent(getBestTrackerAgentDrawable());  // good one
            } else {
                // if the bestTrackerAgent does not have enough support, tell this to SpatialAttention
                getSpatialAttention().setBestTrackerAgent(null); // candidate score is not high enough
            }
        } else {
            // there is currently no bestTrackerAgent candidate, so tell this to SpatialAttention
            getSpatialAttention().setBestTrackerAgent(null); 
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
        drawable.close();
    }

    /**
     * Returns the best TrackerAgentDrawable based on the highest support
     * quality.
     *
     * @return The TrackerAgentDrawable with the highest support quality, or
     * null if no agents exist.
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
