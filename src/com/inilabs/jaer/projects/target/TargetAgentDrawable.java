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

package com.inilabs.jaer.projects.target;

import com.inilabs.jaer.projects.gui.AgentDrawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.tracker.EventCluster;
//import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.inilabs.jaer.projects.target.AgentCallback;
import com.inilabs.jaer.projects.target.ActionType;
import java.util.stream.Collectors;

public class TargetAgentDrawable extends AgentDrawable implements Runnable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TargetAgentDrawable.class);
    private static final org.apache.logging.log4j.Logger datalog = LogManager.getLogger(TargetAgentDrawable.class);
   
    private final String key;
    private AgentCallback callback;  // Add the callback
    private final List<EventCluster> clusters = new ArrayList<>();
    private boolean isLogging = false;
    private static final float DEFAULT_LIFETIME = 100.0f;
    private static final float DEFAULT_VELOCITY = 0.0f;

    private final long startTime;
    private long lastTime;
    private float maxLifeTime;
    private float velocityAzimuth;
    private float velocityElevation;
    private PolarSpaceDisplay display;
    private final long creationTime;

    public TargetAgentDrawable() {
        super();
        this.startTime = getTimestamp();
        this.lastTime = startTime;
        this.maxLifeTime = DEFAULT_LIFETIME;
        this.velocityAzimuth = DEFAULT_VELOCITY;
        this.velocityElevation = DEFAULT_VELOCITY;
        this.creationTime = System.currentTimeMillis();
        // Log agent creation
        datalog.info(createLog("creation"));
    }

    public void setCallback(AgentCallback callback) {
        this.callback = callback;
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public void setMaxLifeTime(float maxLifeTimeSeconds) {
        this.maxLifeTime = maxLifeTimeSeconds;
    }

    public void setVelocity(float azimuthVelocity, float elevationVelocity) {
        this.velocityAzimuth = azimuthVelocity;
        this.velocityElevation = elevationVelocity;
    }

    @Override
    public void run() {
        datalog.info(createLog("run"));
        move();

        if (isLogging) {
            logData();
        }

        // Check if the agent has exceeded its max lifetime
        long currentTime = getTimestamp();
        float elapsedTime = (currentTime - startTime) / 1000.0f; // Convert ms to seconds
        if (elapsedTime > maxLifeTime) {
            close(); // Close and remove the agent from the display
        }
    }

    private void move() {
        long currentTime = getTimestamp();
        float deltaTime = (currentTime - lastTime) / 1000.0f; // Convert ms to seconds
        lastTime = currentTime;

        setAzimuth(getAzimuth() + velocityAzimuth * deltaTime);
        setElevation(getElevation() + velocityElevation * deltaTime);
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
        datalog.info(createLog("close"));
        removeFromDisplay();

        // Notify the callback (manager) to remove this agent
        if (callback != null) {
            callback.onAgentAction(ActionType.REMOVE, getKey());
        }
    }

    private void removeFromDisplay() {
        if (display != null) {
            display.removeDrawable(this.getKey());
            logger.info("Agent {} removed from display", getKey());
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        for (EventCluster cluster : clusters) {
            cluster.draw(g);
        }
        logger.trace("Agent {} draw operation completed.", getKey());
    }
    
    
     private String createLog(String eventType) {
        // Capture timestamp and essential data for logging
        long timestamp = System.currentTimeMillis();
        String clustersList = clusters.stream().map(EventCluster::getKey).collect(Collectors.joining(", "));
        
        return String.format(
            "{\"time\": \"%d\", \"ev_type\": \"%s\", \"key\": \"%s\", \"az\": %.2f, \"el\": %.2f, \"clust\": [%s]}",
            timestamp, eventType, getKey(), getAzimuth(), getElevation(), clustersList
        );
    }
    
}

