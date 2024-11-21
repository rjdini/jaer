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

package com.inilabs.jaer.projects.cog;

import com.inilabs.jaer.gimbal.GimbalBase;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.swing.Timer;

public class SpatialAttention implements KeyListener {
    private final GimbalBase gimbalBase;
    private boolean keyboardControlEnabled = false; // Tracks if keyboard control is active
    private float azimuth = 0; // Current azimuth for manual control
    private float elevation = 0; // Current elevation for manual control
    private final float stepSize = 1.0f; // Incremental step size for azimuth/elevation
    private final Set<Integer> keyDown = new HashSet<>(); // Tracks currently pressed keys
    private final Timer updateTimer;

    private TrackerAgentDrawable bestTrackerAgent = null; // Reference to the best tracker agent

    public SpatialAttention(GimbalBase gimbalBase) {
        this.gimbalBase = gimbalBase;

        // Timer for periodic updates
        updateTimer = new Timer(100, e -> {
            if (keyboardControlEnabled) {
                updateAzimuthAndElevation();
                gimbalBase.setGimbalPoseDirect(azimuth, 0, elevation); // Send manual pose
            } else if (bestTrackerAgent != null) {
                gimbalBase.setGimbalPoseDirect(bestTrackerAgent.getAzimuth(), 0, bestTrackerAgent.getElevation()); // Send best tracker agent pose
            }
        });
        updateTimer.start();
    }

    public void toggleKeyboardControl() {
        keyboardControlEnabled = !keyboardControlEnabled;
        if (!keyboardControlEnabled) {
            keyDown.clear(); // Clear all key states
        }
    }

    public boolean isKeyboardControlEnabled() {
        return keyboardControlEnabled;
    }

    public void setBestTrackerAgent(TrackerAgentDrawable agent) {
        bestTrackerAgent = agent; // Update the best tracker agent
    }

    private void updateAzimuthAndElevation() {
        if (keyDown.contains(KeyEvent.VK_6)) {
            azimuth -= stepSize;
        }
        if (keyDown.contains(KeyEvent.VK_7)) {
            azimuth += stepSize;
        }
        if (keyDown.contains(KeyEvent.VK_8)) {
            elevation += stepSize;
        }
        if (keyDown.contains(KeyEvent.VK_9)) {
            elevation -= stepSize;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (keyboardControlEnabled) {
            keyDown.add(e.getKeyCode()); // Track pressed keys
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (keyboardControlEnabled) {
            keyDown.remove(e.getKeyCode()); // Remove released keys
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No implementation needed for keyTyped
    }
}

