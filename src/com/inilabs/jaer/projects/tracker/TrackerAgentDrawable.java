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
import com.inilabs.jaer.projects.gui.DrawableListener;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.EventType;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackerAgentDrawable extends AgentDrawable implements Runnable, Drawable, DrawableListener  {
    // This logger logs class-specific performance issues, not the event logger
    private static final Logger logger = LoggerFactory.getLogger(TrackerAgentDrawable.class);
    private static final float QUALITY_THRESHOLD = 0.5f; // Threshold for cluster support quality
   private float optimizationCost = 0f;
    
  // private List<EventCluster> clusters = new ArrayList<>();
  //  private float azimuth; // Current azimuth position
  //  private float elevation; // Current elevation position
    public static final int MAX_CLUSTERS = 4; 
   
   
   

    public TrackerAgentDrawable() {
        super();
        setColor(Color.BLACK);
        logger.info("TrackerAgentDrawable created with key: {} at startTime: {}", this.getKey(), startTime);
        AgentLogger.logAgentEvent(EventType.CREATE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

    private boolean loggingEnabled = true;

    public void setLogging(boolean enabled) {
        this.loggingEnabled = enabled;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }
 
    
     // Example implementation for support quality calculation
    public double getSupportQuality() {
        // Aggregate the support quality of all associated clusters
        List<EventCluster> clusters = getClusters(); // Assuming getClusters() exists
        return clusters.stream()
                .mapToDouble(EventCluster::getSupportQuality)
                .sum();
    }

    // Stub for `getClusters()` - ensure this method exists to retrieve associated clusters
    public List<EventCluster> getClusters() {
        // Replace with actual logic to return associated clusters
        return clusters; // Return an empty list for now
    }
    
  
    
    /**
     * Adds a cluster to the agent's list. If the limit is exceeded, removes the farthest cluster.
     */
    public void addCluster(EventCluster cluster) {
        clusters.add(cluster);
        resetLifeTime(); // Reset lifetime when a cluster is added

        if (clusters.size() > MAX_CLUSTERS) {
            // Remove the farthest cluster if the limit is exceeded
            removeFarthestCluster();
        }
          logger.info("Cluster added to agent {}: Cluster ID = {}", getKey(), cluster.getId());
    }
    
    /**
     * Removes the farthest cluster from the agent's cluster list.
     */
    private void removeFarthestCluster() {
        EventCluster farthestCluster = null;
        float maxDistance = -1;

        for (EventCluster cluster : clusters) {
            float distance = calculateDistance(this.azimuth, this.elevation, cluster.getAzimuth(), cluster.getElevation());
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

    
    
    public void removeCluster(EventCluster cluster) {
        clusters.remove(cluster);
        logger.info("Cluster removed from agent {}: Cluster ID = {}", getKey(), cluster.getId());
    }

    /**
     * Updates the centroid based on the agent's assigned clusters.
     */
    public void updateCentroid() {
        if (clusters.isEmpty()) {
            // No clusters, centroid remains unchanged
            return;
        }

        // Calculate the average azimuth and elevation
        double sumAzimuth = 0;
        double sumElevation = 0;

        for (EventCluster cluster : clusters) {
            sumAzimuth += cluster.getAzimuth();
            sumElevation += cluster.getElevation();
        }

        int clusterCount = clusters.size();
        this.azimuth = (float) (sumAzimuth / clusterCount);
        this.elevation = (float) (sumElevation / clusterCount);
    }

    /**
     * Moves the agent to its computed centroid position.
     */
    public void moveToCentroid() {
        // Update the agent's polar coordinates to match the centroid
        setAzimuth(this.azimuth);
        setElevation(this.elevation);
    }
   
   
    @Override
    public void run() {
        AgentLogger.logAgentEvent(EventType.RUN, getKey(), getAzimuth(), getElevation(), getClusterKeys());
       updateCentroid();
        moveToCentroid();
        
        if (clusters.isEmpty() && isTerminated()) {
            setIsExpired(true);
             AgentLogger.logAgentEvent(EventType.CLOSE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
             close();
        }
    }

    private void updatePosition() {
        if (clusters.isEmpty()) return;

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
        lastTime = getTimestamp();
        clusters.clear();
        logger.info("Agent {} closed at lastTime: {}", getKey(), lastTime);
        AgentLogger.logAgentEvent(EventType.CLOSE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

    public void setOptimizationCost( float cost) {
        optimizationCost = cost;
    }
    
    public float getOptimizationCost() {
        return optimizationCost ;
    }
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int x = centerX + (int) ((getAzimuth() - azimuthHeading) * azimuthScale);
        int y = centerY - (int) ((getElevation() - elevationHeading) * elevationScale);

        g2d.setColor(color);
        int pixelSizeX = (int) (size * azimuthScale);
        int pixelSizeY = (int) (size * elevationScale);
        g2d.drawOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);
        g2d.drawString(getKey(), x, y - pixelSizeY / 2);
      
        for (EventCluster cluster : clusters) {
            cluster.draw(g);
        }

        if (showPath) {
            drawPath(g2d);
        }
        
     logger.trace("Agent {} draw operation completed.", getKey());
    }
}

