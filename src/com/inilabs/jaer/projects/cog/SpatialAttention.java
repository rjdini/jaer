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
import com.inilabs.jaer.projects.motor.DirectGimbalController;
import com.inilabs.jaer.projects.motor.JoystickController;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import com.inilabs.jaer.projects.tracker.TrackerManagerEngine;
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

public class SpatialAttention {
   
      private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SpatialAttention.class);
    
    private float azimuth = 0; // Current azimuth for manual control
    private float roll = 0; // Current roll for manual control
    private float elevation = 0; // Current elevation for manual control
    private float waypointAzimuth = 0f ; // Current azimuth for manual control
    private float waypointElevation = -40f; // Current elevation for manual control
    
    
    private Timer updateTimer;
   
    private boolean enableGimbalPose = true ; 
    private double supportQualityThreshold = 90.0;
    
     private static final long BREAK_CONTACT_DURATION = 2000; // Threshold in milliseconds
    private long lastSuccessfulUpdate = System.currentTimeMillis();
    private boolean isSaccade = false; // State to ignore incoming data during waypoint movement
    private final ScheduledExecutorService scheduler;
    
    
    private TrackerAgentDrawable bestTrackerAgent = null; // Reference to the best tracker agent
    
     private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    
   

    private static DirectGimbalController gimbal = DirectGimbalController.getInstance();
//    private TrackerManagerEngine engine = new TrackerManagerEngine();
  
    private static JoystickController joystickController = JoystickController.getInstance(gimbal);
    
    
    private static SpatialAttention instance;
    
   private SpatialAttention() {

         this.scheduler = Executors.newSingleThreadScheduledExecutor(); 
      //  this.updateTimer = new Timer(100, e -> updateGimbalPose());
     //   this.updateTimer.start();
                 
         Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));  
}


    public static SpatialAttention getInstance() {
        if (instance == null) {
            instance = new SpatialAttention();
            instance.startTasks();
        }
        return instance;
    }
   
   
public void startTasks() {
        try {
                   // Schedule a task to periodically update the gimbal
                  executor.scheduleAtFixedRate(this::updateGimbalPose, 50, 50, TimeUnit.MILLISECONDS);
        log.info("Scheduled tasks started....");
        } catch(Exception e ) { 
            log.error("Error starting scheduled tasks : {}", e.getMessage(), e);
        }
    }

    
public void shutdown() {
 executor.shutdown();    
 scheduler.shutdown();
}    

   
    /**
    *  This is he crucial update date of the Gimbal by various  sources:
    *  The default update is against the current best TrackerAgentDrawable.
    * 
    * That agent is decided by the TrackerManagerEngine, and assigned here .
    *  
    * Finally the gimbal can be completely inactivated by de-selecting the 'Gimbal Enable' button on the PolarSpaceControlPanel.
    * This freezes the pose, so that (eg) filters can be tested without interference of gimbal motion.
    * 
    */
    private synchronized void updateGimbalPose() {
        // under normal operation SA simply sends the coordinates of current best trackeragent to the gimbal.
        // However - large moves of the gimbal would lead to generation false trackers, so these moveets occur within a saccade.
        // when TrackerManagerEngine has isSaccade true,  it does not process incomming clusters (both RCT and Test).
        // In future we could make this more sophisticated - eg continue to attend to 'imagined' test targets.
    
        if (enableGimbalPose)  { // override from PolarSpaceControlPanel
        // Check if the system is in a saccade state
        if (isSaccade) {
            log.info("Ignoring incoming data due to saccade.");
            return;
        }

        if (getBestTrackerAgent() != null &&
            (getBestTrackerAgent().getSupportQuality() > getSupportQualityThreshold())) {

            // Update the tracker agent and send pose to gimbal
            getBestTrackerAgent().run();
            log.info("Best Tracker Agent supportQuality threshold: {}, current: {}",
                    getSupportQualityThreshold(), getBestTrackerAgent().getSupportQuality());
            gimbal.setGimbalPose(getBestTrackerAgent().getAzimuth(), 0f, getBestTrackerAgent().getElevation());

            // Update the last successful update timestamp
            lastSuccessfulUpdate = System.currentTimeMillis();
        } else {
            // Check if the time since the last successful update exceeds the threshold
            if (System.currentTimeMillis() - lastSuccessfulUpdate >= BREAK_CONTACT_DURATION) {
                goToWaypoint(getWaypointAzimuth(), getWaypointElevation());
            }
        }
        }
    }

    private void goToWaypoint(float azimuth, float elevation) {
        // Check if already at the waypoint
        if (azimuth == gimbal.getGimbalPose().getYaw() && elevation == gimbal.getGimbalPose().getPitch()) {
            log.info("Gimbal is already at the waypoint.");
            return;
        }

        // Enter saccade state
        isSaccade = true;
        TrackerManagerEngine.setIsSaccade(isSaccade);
        log.info("Entering saccade state: Moving to waypoint azimuth: {}, elevation: {}", azimuth, elevation);

        gimbal.setGimbalPose(azimuth, 0f, elevation);

        // Schedule exiting the saccade state
        scheduler.schedule(() -> {
            isSaccade = false;
            TrackerManagerEngine.setIsSaccade(isSaccade);
            log.info("Saccade completed. Exiting saccade state.");
        }, 2000, TimeUnit.MILLISECONDS); // Delay after reaching waypoint
    }
 
    
       public void setBestTrackerAgent(TrackerAgentDrawable agent) {
        bestTrackerAgent = agent; // Update the best tracker agent
    }
    
    
    public float getWaypointAzimuth() {
        return waypointAzimuth; 
    }

    public float getWaypointElevation() {
        return waypointElevation; 
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

    /**
     * @return the bestTrackerAgent
     */
    public TrackerAgentDrawable getBestTrackerAgent() {
        return bestTrackerAgent;
    }


    /**
     * @return the supportQualityThreshold
     */
    public double getSupportQualityThreshold() {
        return supportQualityThreshold;
    }

    /**
     * @param supportQualityThreshold the supportQualityThreshold to set
     */
    public void setSupportQualityThreshold(double supportQualityThreshold) {
        this.supportQualityThreshold = supportQualityThreshold;
    }

 

    /**
     * @param waypointAzimuth the waypointAzimuth to set
     */
    public void setWaypointAzimuth(float waypointAzimuth) {
        this.waypointAzimuth = waypointAzimuth;
    }

    
    public void setWaypoint(float azi, float ele) {
        waypointAzimuth = azi;
        waypointElevation = ele;
    }
    
    /**
     * @param waypointElevation the waypointElevation to set
     */
    public void setWaypointElevation(float waypointElevation) {
        this.waypointElevation = waypointElevation;
    }
}

