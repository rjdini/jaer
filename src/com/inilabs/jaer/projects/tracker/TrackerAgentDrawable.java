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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import com.inilabs.jaer.projects.gui.AgentDrawable;
import java.util.stream.Collectors;

public class TrackerAgentDrawable extends AgentDrawable implements Runnable {
    // this logger logs class specific  perfomance issues.
    // it is not the datalogger
    private static final Logger logger = LoggerFactory.getLogger(TrackerAgentDrawable.class);
    private final List<EventCluster> clusters = new ArrayList<>();
    private static final float QUALITY_THRESHOLD = 0.5f; // Threshold for cluster support quality

    private final long startTime;
    private long lastTime;

    public TrackerAgentDrawable() {
        super();
        this.startTime = getTimestamp();
        logger.info("TargetAgentDrawable created with key: {} at startTime: {}", this.getKey(), startTime);
         logEvent("creation", getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }
    
     // Helper method to get cluster keys as a list of strings
    private List<String> getClusterKeys() {
        return clusters.stream().map(EventCluster::getKey).collect(Collectors.toList());
    }

    
  private boolean loggingEnabled = true;

    public void setLogging(boolean enabled) {
        this.loggingEnabled = enabled;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }
    

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public void addCluster(EventCluster cluster) {
        cluster.setEnclosingAgent(this);
        clusters.add(cluster);
        logger.info("Cluster added to agent {}: Cluster ID = {}", getKey(), cluster.getId());
    }

    public void removeCluster(EventCluster cluster) {
        clusters.remove(cluster);
        logger.info("Cluster removed from agent {}: Cluster ID = {}", getKey(), cluster.getId() 
        );
    }

    @Override
    public void run() {
        updatePosition();
   //     clusters.removeIf(cluster -> cluster.getSupportQuality() < QUALITY_THRESHOLD);
        if (clusters.isEmpty()) {
  //          close();
        }
         logEvent("run", getKey(), getAzimuth(), getElevation(), getClusterKeys());
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

    public void close() {
        lastTime = getTimestamp();
        clusters.clear();
        logger.info("Agent {} closed at lastTime: {}", getKey(), lastTime);
          logEvent("close", getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        for (EventCluster cluster : clusters) {
            cluster.draw(g);
        }
         logEvent("draw", getKey(), getAzimuth(), getElevation(), getClusterKeys());
        logger.trace("Agent {} draw operation completed.", getKey());
    }
}

