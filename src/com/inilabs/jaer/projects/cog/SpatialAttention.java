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
import static com.inilabs.jaer.projects.cog.JoystickReader.Axis.PITCH;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;
import org.slf4j.LoggerFactory;

public class SpatialAttention implements KeyListener {
   
      private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SpatialAttention.class);
    
    private float azimuth = 0; // Current azimuth for manual control
    private float roll = 0; // Current roll for manual control
    private float elevation = 0; // Current elevation for manual control
    private final float stepSize = 1.0f; // Incremental step size for azimuth/elevation
    private final Set<Integer> keyDown = new HashSet<>(); // Tracks currently pressed keys
    private Timer updateTimer;
    
    private boolean enableKeyboardControl = false; // Tracks if keyboard control is active
    private boolean enableJoystickControl = true;
    private boolean enableGimbalPose = true ; 
    
    
    private TrackerAgentDrawable bestTrackerAgent = null; // Reference to the best tracker agent
    private JoystickReader joystickReader;

     private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean joystickActive = false;
    private final long JOYSTICK_TIMEOUT = 3000; // Timeout in milliseconds

    private static GimbalBase gimbalBase = GimbalBase.getInstance();
    
    private static SpatialAttention instance;
    
   private SpatialAttention() {
       // Timer for periodic updates
        this.updateTimer = new Timer(100, e -> updateGimbalPose());
        this.updateTimer.start();
     
        init();
}

    public static SpatialAttention getInstance() {
    if (instance == null) {
        synchronized (GimbalBase.class) {
            if (instance == null) {
                instance = new SpatialAttention();
            }
        }
    }
    return instance;
}
    
private void init() {
              
     // Initialize joystick reader
     joystickReader = new JoystickReader(new JoystickReader.JoystickListener() {
          
         @Override
    public void onAxisChange(JoystickReader.Axis axis, float value) {
        // Mark joystick as active
        joystickActive = true;

        if (enableJoystickControl) {
            switch (axis) {
                case YAW -> setAzimuth(Math.max(-180.0f, Math.min(180.0f, getAzimuth() + value * 10.0f)));
                case ROLL -> setRoll(Math.max(-90.0f, Math.min(90.0f, getElevation() + value * 10.0f)));
                case PITCH -> setElevation(Math.max(-90.0f, Math.min(90.0f, getElevation() + value * 10.0f)));
                default -> log.debug("Unhandled axis: {}", axis);
            }
        }
        
        // Schedule a task to mark joystick as inactive after a delay
        scheduler.schedule(() -> {
            joystickActive = false;
            log.debug("Joystick inactive");
        }, JOYSTICK_TIMEOUT, TimeUnit.MILLISECONDS);
         
    }
                
            @Override
            public void onButtonPress(JoystickReader.Button button, boolean pressed) {
                if (pressed && button == JoystickReader.Button.BUTTON1) {
                    enableJoystickControl= !enableJoystickControl;
                    System.out.println("Joystick control " + (enableJoystickControl ? "enabled" : "disabled"));
                }
            }
        });

        joystickReader.start();
    }

    
    
    
    
    
    
    
    public boolean isJoystickActive() {
        return joystickActive;
    }


   
    /**
    *  This is he crucial update date of the Gimbal by various  sources:
    *  The default update is against the current best TrackerAgentDrawable.
    * 
    * That agent is decided by the TrackerManagerEngine, and assigned here .
    * 
    * However, the default can be override by keyboard controls (a legacy, in case no joystick is available).
    * The keyboard controls override the default only if the keyboard is enabled by the 'keyboard enable' button on the PolarSpaceControlPanel.
    * 
    *  The joystick  is always live, and has the hightest priority for updating the gimbal pose. 
    * Because the joystick may change the pose over many degrees, any input from the gimbal activates a saccadic blink
    * that lasts a short while after input from the joystick stops. 
    * 
    * Finally the gimbal can be completely inactivated by de-selecting the 'Gimbal Enable' button on the PolarSpaceControlPanel.
    * This freezes the pose, so that (eg) filters can be tested without interference of gimbal motion.
    * 
    */
    public void updateGimbalPose() {
        
        if(isEnableGimbalPose()) {
       
         //joystick is enabled by default, isJoystickActive provides saccadic suppression window 
        if (enableJoystickControl && isJoystickActive()) {  // 
            // Apply joystick input to gimbal
            gimbalBase.setGimbalPoseDirect(getAzimuth(), 0, getElevation());
        
        } else if (isEnableKeyboardControl()) {
            updateAzimuthAndElevation();
            gimbalBase.setGimbalPoseDirect(getAzimuth(), 0, getElevation()); // Send manual pose
        
        } else if (bestTrackerAgent != null) {
            gimbalBase.setGimbalPoseDirect(bestTrackerAgent.getAzimuth(), 0, bestTrackerAgent.getElevation()); // Send best tracker agent pose
        }
   
        }
    }
    
   
    
 
    public boolean isEnableKeyboardControl() {
        return enableKeyboardControl;
    }

    public void setBestTrackerAgent(TrackerAgentDrawable agent) {
        bestTrackerAgent = agent; // Update the best tracker agent
    }

    private void updateAzimuthAndElevation() {
        if (keyDown.contains(KeyEvent.VK_6)) {
            setAzimuth(getAzimuth() - stepSize);
        }
        if (keyDown.contains(KeyEvent.VK_7)) {
            setAzimuth(getAzimuth() + stepSize);
        }
        if (keyDown.contains(KeyEvent.VK_8)) {
            setElevation(getElevation() + stepSize);
        }
        if (keyDown.contains(KeyEvent.VK_9)) {
            setElevation(getElevation() - stepSize);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isEnableKeyboardControl()) {
            keyDown.add(e.getKeyCode()); // Track pressed keys
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isEnableKeyboardControl()) {
            keyDown.remove(e.getKeyCode()); // Remove released keys
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No implementation needed for keyTyped
    }

    
    
    
    
    /**
     * @return the azimuth
     */
    private float getAzimuth() {
        return azimuth;
    }

    /**
     * @param azimuth the azimuth to set
     */
    private void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }
    
        /**
     * @return the roll
     */
    private float getRoll() {
        return roll;
    }
    
    /**
     * @param roll the roll to set
     */
    private void setRoll(float roll) {
        this.roll = roll;
    }

    /**
     * @return the elevation
     */
    private float getElevation() {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    private void setElevation(float elevation) {
        this.elevation = elevation;
    }

    /**
     * @param enableKeyboardControl the enableKeyboardControl to set
     */
    public void setEnableKeyboardControl(boolean yes) {
        this.enableKeyboardControl = yes;
    }

    /**
     * @return the isEnableGimbalPose
     */
    public boolean isEnableGimbalPose() {
        return enableGimbalPose;
    }

    /**
     * @param setEnableGimbaPosel the isEnableGimbal to set
     */
    public void setEnableGimbalPose(boolean yes) {
        this.enableGimbalPose = yes;
    }
}

