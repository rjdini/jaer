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

public class TrackerAgentDrawable extends AgentDrawable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TrackerAgentDrawable.class);

    private final List<EventCluster> clusters = new ArrayList<>();
    private boolean isLogging = false;
    private static final float QUALITY_THRESHOLD = 0.5f; // Threshold for cluster support quality

    private final long startTime;
    private long lastTime;

    public TrackerAgentDrawable() {
        super();
        this.startTime = getTimestamp();
        logger.info("TargetAgentDrawable created with key: {} at startTime: {}", this.getKey(), startTime);
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

    public void setLogging(boolean logging) {
        this.isLogging = logging;
        logger.info("Logging set to {} for agent {}", logging, getKey());
    }

    @Override
    public void run() {
        updatePosition();
   //     clusters.removeIf(cluster -> cluster.getSupportQuality() < QUALITY_THRESHOLD);

        if (isLogging) {
            logData();
        }

        if (clusters.isEmpty()) {
  //          close();
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

    private void logData() {
        logger.info("Logging data for agent {} at timestamp: {}", getKey(), getTimestamp());
        for (EventCluster cluster : clusters) {
            logger.info("Cluster ID = {}, Quality = {}", cluster.getId(), cluster.getSupportQuality());
        }
    }

    public void close() {
        lastTime = getTimestamp();
        clusters.clear();
        logger.info("Agent {} closed at lastTime: {}", getKey(), lastTime);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        for (EventCluster cluster : clusters) {
            cluster.draw(g);
        }
        logger.trace("Agent {} draw operation completed.", getKey());
    }
}

