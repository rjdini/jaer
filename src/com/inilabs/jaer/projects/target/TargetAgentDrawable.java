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
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.EventType;
import com.inilabs.jaer.projects.tracker.EventCluster;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TargetAgentDrawable extends AgentDrawable implements Runnable {
    private AgentCallback callback;
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
    private static final Logger logger = LoggerFactory.getLogger(TargetAgentDrawable.class);

    public TargetAgentDrawable() {
        super();
        this.startTime = getTimestamp();
        this.lastTime = startTime;
        this.maxLifeTime = DEFAULT_LIFETIME;
        this.velocityAzimuth = DEFAULT_VELOCITY;
        this.velocityElevation = DEFAULT_VELOCITY;

        // Log agent creation
        AgentLogger.logAgentEvent(EventType.CREATE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

    public String getKey() {
        return this.getKey();
    }

    public void setCallback(AgentCallback callback) {
        this.callback = callback;
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
        move();

        // Check if the agent has exceeded its max lifetime
        long currentTime = getTimestamp();
        float elapsedTime = (currentTime - startTime) / 1000.0f;
        if (elapsedTime > maxLifeTime) {
            close();
        }

        // Log run event
        AgentLogger.logAgentEvent(EventType.RUN, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

    private void move() {
        long currentTime = getTimestamp();
        float deltaTime = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        setAzimuth(getAzimuth() + velocityAzimuth * deltaTime);
        setElevation(getElevation() + velocityElevation * deltaTime);
        logger.debug("Agent {} position updated to Azimuth = {}, Elevation = {}", getKey(), getAzimuth(), getElevation());
    }

    public void close() {
        lastTime = getTimestamp();
        clusters.clear();
        removeFromDisplay();
        if (callback != null) {
            callback.onAgentAction(ActionType.REMOVE, getKey());
        }
        // Log close event
        AgentLogger.logAgentEvent(EventType.CLOSE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
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
        // Log draw event
        AgentLogger.logAgentEvent(EventType.DRAW, getKey(), getAzimuth(), getElevation(), getClusterKeys());
        logger.trace("Agent {} draw operation completed.", getKey());
    }
    
//    @Override
//    private List<String> getClusterKeys() {
//        return clusters.stream().map(EventCluster::getKey).collect(Collectors.toList());
//    }
}
