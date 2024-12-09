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

import com.inilabs.jaer.projects.gui.AgentDrawable;
import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.EventType;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerAgentDrawable extends AgentDrawable implements Expirable, Runnable, Drawable {

    // This logger logs class-specific performance issues, not the event logger
    private static final Logger logger = LoggerFactory.getLogger(TrackerAgentDrawable.class);
    private static final AgentLogger agentLogger = AgentLogger.getInstance();
    private static final float QUALITY_THRESHOLD = 0.5f; // Threshold for cluster support quality
    private float optimizationCost = 0f;
  //  private final long startTime = System.currentTimeMillis(); // Creation time
  //  private long expirationTime; // Time at which the agent expires

    private final CopyOnWriteArrayList<EventCluster> clusters = new CopyOnWriteArrayList<>();

    private float lastAzimuth;
    private float lastElevation;
    private long lastMovementTime;
    private long startTime;
    private long maxLifetime;

    private boolean supportQualityTestEnabled = false;
    private double mockSupportQuality = 50;
    
    // private List<EventCluster> clusters = new ArrayList<>();
    //  private float azimuth; // Current azimuth position
    //  private float elevation; // Current elevation position
    public static final int MAX_CLUSTERS = 4;

    public TrackerAgentDrawable(long lifetimeMillis) {
        new TrackerAgentDrawable();   
        this.startTime = getTimestamp();
        this.setColor(Color.BLACK);
        this.maxLifetime = lifetimeMillis; // Set initial expiration
        this. lastMovementTime = getTimestamp();
      
        TrackerAgentDrawable.logger.info("TrackerAgentDrawable created with key: {} at startTime: {}", getKey(), startTime);
        AgentLogger.logAgentEvent(EventType.CREATE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }
    
    public TrackerAgentDrawable() {
    super();    
}
    

    private boolean loggingEnabled = true;

    public void setLogging(boolean enabled) {
        this.loggingEnabled = enabled;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void enableSupportQualltyTests(boolean yes) {
        supportQualityTestEnabled = yes;
    }
    
    // used for testing
    public void setMockSupportQuality(double quality) {
        mockSupportQuality = quality ; 
    }
    
    // Example implementation for support quality calculation
    public synchronized double getSupportQuality() {
        if (supportQualityTestEnabled) {
            return mockSupportQuality ;
        }
        
    List<EventCluster> clusters = getClusters(); // Assuming getClusters() exists

    // If no clusters are associated, the quality score is zero
    if (clusters.isEmpty()) {
        return 0.0;
    }

    double elapsedLifeTimeFactor = getLifetime(); // Assume this method provides elapsed life time
    int numberOfClusters = clusters.size();
    
    // Aggregate the contribution of each cluster
    double clusterContribution = clusters.stream()
            .mapToDouble(cluster -> {
                double clusterLifeTime = cluster.getLifetime(); // Lifetime of the cluster
                double clusterDistance = getClusterDistance(cluster); // Distance of the cluster
                double distanceFactor = clusterDistance > 0 ? 1.0 / clusterDistance : 1.0; // Inverse relationship
                return clusterLifeTime * distanceFactor; // Contribution scales with lifetime and inversely with distance
            })
            .sum();

    // Calculate overall quality
    double qualityScore = elapsedLifeTimeFactor 
                        + numberOfClusters 
                        + clusterContribution;

    return qualityScore / 100;
}

       private float getClusterDistance(EventCluster cluster) {
        float deltaAzimuth = getAzimuth() - cluster.getAzimuth();
        float deltaElevation = getElevation() - cluster.getElevation();
        return (float) Math.sqrt(deltaAzimuth * deltaAzimuth + deltaElevation * deltaElevation);
           }
    
 
    public void revertColor() {
        setColor(Color.BLACK);
    }

    /**
     * Adds a cluster to the agent's list. If the limit is exceeded, removes the
     * farthest cluster.
     */
    public synchronized void addCluster(EventCluster eventCluster) {
    // Check if the cluster already exists in the list
    for (EventCluster existingCluster : clusters) {
        if (existingCluster.getKey().equals(eventCluster.getKey())) {
            logger.debug("Cluster with key {} already exists in agent {}. Skipping addition.", 
                        eventCluster.getKey(), getKey());
            return; // Cluster already exists, so do not add it again
        }
    }

    // Add the cluster as it's novel
    clusters.add(eventCluster);
    // extendLifetime(500); // Reward: extend lifetime by 500ms for novel cluster

    if (clusters.size() > MAX_CLUSTERS) {
        // Remove the farthest cluster if the limit is exceeded
        removeFarthestCluster();
    }

    logger.debug("Agent: {} added eventCluster: {}", getKey(), eventCluster);
}
    
    public synchronized void removeCluster(EventCluster eventCluster) {
        clusters.remove(eventCluster);
        logger.debug("Cluster removed from agent {}: eventCluster: {}", getKey(), eventCluster.getKey());
    }

 
    public void move() {
          updateCentroid();
             // Check for movement
        if (Math.abs(lastAzimuth - getAzimuth()) > 0.1 || Math.abs(lastElevation - getElevation()) > 0.1) {
            lastMovementTime = System.currentTimeMillis(); // Update movement timestamp
        }
            moveToCentroid(); // Move the agent
            agentLogger.logAgentEvent(EventType.MOVE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }
    
     private void checkTrackerAgentExpired() {
     if ((getLifetime() > maxLifetime) && getClusters().isEmpty() ) {
            setExpired(true);
        }
    }
    
     @Override
    public long getLifetime()  {
     return getTimestamp() - startTime;
 }
    
    @Override
    public synchronized void run() {
        clusters.removeIf(EventCluster::isExpired); // Remove expired clusters
        move();
        checkTrackerAgentExpired();
        if(isExpired()){
            setColor(Color.BLUE); }
        
        
     // agentLogger.logAgentEvent(EventType.RUN, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

       // the cluster management shouldnt be down here in BasicDrawable!  TODO
       // Helper method to get cluster keys as a list of strings
   
    
    public boolean isStatic() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastMovementTime) > 5000; // Static if no movement for 5 seconds
    }

    private void updateCentroid() {
        if (clusters.isEmpty()) {
            return; // No clusters to process
        }

        float sumAzimuth = 0;
        float sumElevation = 0;

        for (EventCluster cluster : clusters) {
            sumAzimuth += cluster.getAzimuth();
            sumElevation += cluster.getElevation();
        }

        int clusterCount = clusters.size();
        this.lastAzimuth = this.getAzimuth();
        this.lastElevation = this.getElevation();
        this.setAzimuth(sumAzimuth / clusterCount);
        this.setElevation(sumElevation / clusterCount);
    }

    private void moveToCentroid() {
        // Update the agent's position (for visualization or further processing)
        setAzimuth(this.getAzimuth());
        setElevation(this.getElevation());
    }

    /**
     * Removes the farthest cluster from the agent's cluster list.
     */
    private void removeFarthestCluster() {
        EventCluster farthestCluster = null;
        float maxDistance = -1;

        for (EventCluster cluster : clusters) {
            float distance = calculateDistance(this.getAzimuth(), this.getElevation(), cluster.getAzimuth(), cluster.getElevation());
            if (distance > maxDistance) {
                maxDistance = distance;
                farthestCluster = cluster;
            }
        }

        if (farthestCluster != null) {
            clusters.remove(farthestCluster);
        }
    }

    /**
     * Calculates the distance between two polar coordinates.
     */
    private float calculateDistance(float az1, float el1, float az2, float el2) {
        float deltaAzimuth = az1 - az2;
        float deltaElevation = el1 - el2;
        return (float) Math.sqrt(deltaAzimuth * deltaAzimuth + deltaElevation * deltaElevation);
    }

    /**
     * Updates the centroid based on the agent's assigned clusters.
     */
//    public void updateCentroid() {
//        if (clusters.isEmpty()) {
//            // No clusters, centroid remains unchanged
//            return;
//        }
//
//        // Calculate the average azimuth and elevation
//        double sumAzimuth = 0;
//        double sumElevation = 0;
//
//        for (EventCluster cluster : clusters) {
//            sumAzimuth += cluster.getAzimuth();
//            sumElevation += cluster.getElevation();
//        }
//
//        int clusterCount = clusters.size();
//        this.azimuth = (float) (sumAzimuth / clusterCount);
//        this.elevation = (float) (sumElevation / clusterCount);
//    }
    private void updatePosition() {
        if (clusters.isEmpty()) {
            return;
        }

        float sumAzimuth = 0;
        float sumElevation = 0;
        for (EventCluster cluster : clusters) {
            sumAzimuth += cluster.getAzimuth();
            sumElevation += cluster.getElevation();
        }

        setAzimuth(sumAzimuth / clusters.size());
        setElevation(sumElevation / clusters.size());
        logger.debug("Agent {} position updated to Azimuth = {}, Elevation = {}", getKey(), getAzimuth(), getElevation());
    }

    @Override
    public void close() {
        setLastTime(getTimestamp());
        clusters.clear();
        logger.info("Agent {} closed at lastTime: {}", getKey(), getLastTime());
        agentLogger.logAgentEvent(EventType.CLOSE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }
    

    public void setOptimizationCost(float cost) {
        optimizationCost = cost;
    }

    public float getOptimizationCost() {
        return optimizationCost;
    }

    @Override
    public synchronized void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int x = getCenterX() + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
        int y = getCenterY() - (int) ((getElevation() - getElevationHeading()) * getElevationScale());

        g2d.setColor(color);
        int pixelSizeX = (int) (getSize() * getAzimuthScale());
        int pixelSizeY = (int) (getSize() * getElevationScale());
        g2d.drawOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);
        g2d.drawString(getKey()+"qual: % .1f "+getSupportQuality() , x, y - pixelSizeY / 2);

   //     for (EventCluster cluster : clusters) {
    //        if (cluster != null) {
    //            cluster.draw(g);
    //        }
    //    }

        if (showPath) {
            drawPath(g2d);
        }

        logger.trace("Agent {} draw operation completed.", getKey());
    }

    
}
