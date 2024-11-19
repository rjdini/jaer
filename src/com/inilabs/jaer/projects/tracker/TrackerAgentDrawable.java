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

    

    public TrackerAgentDrawable() {
        super();
        this.startTime = getTimestamp();
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
    
    public void addCluster(EventCluster cluster) {
        cluster.setEnclosingAgent(this);
        clusters.add(cluster);
        logger.info("Cluster added to agent {}: Cluster ID = {}", getKey(), cluster.getId());
    }

    public void removeCluster(EventCluster cluster) {
        clusters.remove(cluster);
        logger.info("Cluster removed from agent {}: Cluster ID = {}", getKey(), cluster.getId());
    }

    @Override
    public void run() {
        updatePosition();
        if (clusters.isEmpty()) {
            // close();
        }
        AgentLogger.logAgentEvent(EventType.RUN, getKey(), getAzimuth(), getElevation(), getClusterKeys());
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

