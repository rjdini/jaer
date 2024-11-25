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
import com.inilabs.jaer.projects.cog.JoystickReader;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;

public class SpatialAttention implements KeyListener {
   
    private float azimuth = 0; // Current azimuth for manual control
    private float elevation = 0; // Current elevation for manual control
    private final float stepSize = 1.0f; // Incremental step size for azimuth/elevation
    private final Set<Integer> keyDown = new HashSet<>(); // Tracks currently pressed keys
    private Timer updateTimer;
    
    private boolean keyboardControlEnabled = false; // Tracks if keyboard control is active
    private boolean joystickControlEnabled = true;

    private GimbalBase gimbalBase;
    private TrackerAgentDrawable bestTrackerAgent = null; // Reference to the best tracker agent
    private JoystickReader joystickReader;

     private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean joystickActive = false;
    private final long JOYSTICK_TIMEOUT = 3000; // Timeout in milliseconds

    public SpatialAttention(GimbalBase gimbalBase) {
        this.gimbalBase = gimbalBase;
       // Timer for periodic updates
        this.updateTimer = new Timer(100, e -> updateGimbalPose());
        this.updateTimer.start();
     
  

     // Initialize joystick reader

     
     joystickReader = new JoystickReader(new JoystickReader.JoystickListener() {
          
         @Override
    public void onAxisChange(JoystickReader.Axis axis, float value) {
        // Mark joystick as active
        joystickActive = true;

        if (joystickControlEnabled) {
            switch (axis) {
                case YAW -> azimuth = Math.max(-180.0f, Math.min(180.0f, azimuth + value * 10.0f));
                case PITCH -> elevation = Math.max(-90.0f, Math.min(90.0f, elevation + value * 10.0f));
                default -> System.err.println("Unhandled axis: " + axis);
            }
        }
        
        // Schedule a task to mark joystick as inactive after a delay
        scheduler.schedule(() -> {
            joystickActive = false;
            System.out.println("Joystick inactive");
        }, JOYSTICK_TIMEOUT, TimeUnit.MILLISECONDS);
         
    }
                
            @Override
            public void onButtonPress(JoystickReader.Button button, boolean pressed) {
                if (pressed && button == JoystickReader.Button.BUTTON1) {
                    joystickControlEnabled = !joystickControlEnabled;
                    System.out.println("Joystick control " + (joystickControlEnabled ? "enabled" : "disabled"));
                }
            }
        });

        joystickReader.start();
    }


    
    public boolean isJoystickActive() {
        return joystickActive;
    }
    
    public void updateGimbalPose() {
        // isJoystickActive provides saccadic suppression window 
        if (joystickControlEnabled && isJoystickActive()) {
            // Apply joystick input to gimbal
            gimbalBase.setGimbalPoseDirect(azimuth, 0, elevation);
        } else if (keyboardControlEnabled) {
            updateAzimuthAndElevation();
            gimbalBase.setGimbalPoseDirect(azimuth, 0, elevation); // Send manual pose
        } else if (bestTrackerAgent != null) {
            gimbalBase.setGimbalPoseDirect(bestTrackerAgent.getAzimuth(), 0, bestTrackerAgent.getElevation()); // Send best tracker agent pose
        }
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

