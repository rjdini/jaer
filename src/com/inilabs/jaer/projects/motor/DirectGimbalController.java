
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

package com.inilabs.jaer.projects.motor;

import com.inilabs.birdland.gimbal.RS4ControllerV2;
import com.inilabs.jaer.gimbal.RS4ControllerGUISwingV1;
import com.inilabs.jaer.projects.motor.Pose;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.bytebuddy.implementation.bytecode.Throw;
import org.slf4j.LoggerFactory;

public class DirectGimbalController {
 
    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DirectGimbalController.class);

    private static RS4ControllerV2 rs4controller;
    public static RS4ControllerGUISwingV1 rs4controllerGUI;
    
   
     
    private Pose currentSendPose = new Pose(0.0f, 0.0f, 0.0f);  // pose sent to Gimbal
    private Pose previousSendPose = new Pose(0.0f, 0.0f, 0.0f);
    private Pose currentGimbalPose = new Pose(0.0f, 0.0f, 0.0f);  // pose returned from gimbal
    private Pose previousGimbalPose = new Pose(0.0f, 0.0f, 0.0f);

    private volatile float targetYaw = 0, targetRoll = 0, targetPitch = 0;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static DirectGimbalController instance;
    
    private final float leftYawLimit = -90f;
    private final float rightYawLimit = 90f;
    private final float leftRollLimit = -60f;
    private final float rightRollLimit = 60f;
    private final float lowerPitchLimit = -60f;
    private final float upperPitchLimit = 60f;
      
    
//    private float previousSendYaw = 0f;
//    private float previousSendRoll = 0f;
//    private float previousSendPitch = 0f;
//    
//    private float currentSendYaw = 0f;
//    private float currentSendRoll = 0f;
//    private float currentSendPitch = 0f;
//    
//     private float previousYaw = 0f;
//     private float previousRoll = 0f;
//     private float previousPitch = 0f;
//          
//     private float currentYaw = 0f;
//     private float currentRoll = 0f;
//     private float currentPitch = 0f;
     
     private long previousUpdateTime = 0; // millisec
     private long currentUpdateTime= 0; // millisecs
     private float deltaUpdateTime = 1000; //seconds
     private Point2D gimbalVelocity = new Point2D.Float(0.0f, 0.0f); 

    private Pose resetPose = new Pose(0f, 0f, 0f);
    private Pose defaultPose = new Pose(10, 0, -30); // nidelbadstrasse
    private boolean gimbalPoseEnabled = true;
     private boolean isDummyMode = false;
    
    
    private static final float gimbalYawOffsetError = -1.5f;
    private static final float gimbalPitchOffsetError = 0.5f;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final FieldOfView fov;
    
    // Constructor
   private DirectGimbalController(FieldOfView fov) {
        super();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        // Run at 10 Hz (100 ms interval)
        executor.scheduleAtFixedRate(this::updateGimbal, 100, 100, TimeUnit.MILLISECONDS);
        this.fov = fov;
       init();
    }
    
       public static DirectGimbalController getInstance(FieldOfView fov) {
        if (instance == null) {
           instance = new DirectGimbalController(fov);
           instance.addPropertyChangeListener(fov);
        }
        return instance;
    }
    
       
    private void init() {
       previousUpdateTime = System.currentTimeMillis(); // initialize gimbal velocity reference time
        
       //  TODO --  need to solve the case where RS4Controller is not present, and then use effence copy 
        if(true) {
            rs4controller = RS4ControllerV2.getInstance();
        } else {
            log.warn("RS4 Controller not available. Switching to dummy mode.");
            isDummyMode = true;
        }
            sendDefaultGimbalPose();
    }
  
       
       
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
        
 public void sendDefaultGimbalPose(){   
     setGimbalPoseDirect(getDefaultPose());
 }
 
 public void resetPose() {
     setGimbalPoseDirect(getResetPose());
 }    
 
 
 public synchronized void setGimbalPose(Pose pose) {
     setGimbalPose(pose.getYaw(), pose.getRoll(), pose.getPitch());
 }
 
    public synchronized void setGimbalPose(float yaw, float roll, float pitch) {
        
        this.targetYaw =  rangeCheckYaw(yaw);
        this.targetRoll = rangeCheckRoll(roll);
        this.targetPitch = rangeCheckPitch(pitch);
    }
   
    private float rangeCheckYaw(float yaw) {
        float checkedYaw = 0;
        if(yaw < leftYawLimit ) { checkedYaw = leftYawLimit; }
        else if(yaw > rightYawLimit) { checkedYaw = rightYawLimit; }
        else { checkedYaw = yaw; }
        return checkedYaw;
    }
    
    private float rangeCheckRoll(float roll) {
        float checkedRoll = 0;
        if(roll < leftRollLimit ) { checkedRoll = leftRollLimit; }
        else if(roll > rightRollLimit) { checkedRoll = rightRollLimit; }
        else { checkedRoll = roll; }
        return checkedRoll;
    }
    
    private float rangeCheckPitch(float pitch) {
        float checkedPitch = 0;
        if (pitch < lowerPitchLimit ) { checkedPitch = lowerPitchLimit; }
        else if (pitch > upperPitchLimit) { checkedPitch = upperPitchLimit; }
        else { checkedPitch = pitch; }
        return checkedPitch;
    }
    
   public Pose getGimbalPose() {
        if (isDummyMode) {
            return currentGimbalPose;
        }
        return fetchGimbalPose();
    }
   
   
   private void updateGimbalVelocity() {
    // Calculates the velocity in 2D space (returns angle and magnitude as a float array)
        if (deltaUpdateTime <= 0) {
            throw new IllegalArgumentException("Time interval must be greater than zero.");
        }
        
        float deltaX = currentSendPose.getYaw() - previousSendPose.getYaw() ;
        float deltaY = currentSendPose.getPitch() - previousSendPose.getPitch();
        log.debug("****** gimbal velocity current Yaw {} previous Yaw{} ",  currentSendPose.getYaw(), previousSendPose.getYaw());
         log.debug("****** gimbal velocity current Pitch {} previous Pitch{} ",  currentSendPose.getPitch(), previousSendPose.getPitch());
        log.debug("****** gimbal velocity deltaX {} deltaY {}  deltaUpdateTime {}",  deltaX, deltaY,  deltaUpdateTime);

        float magnitude = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY) / deltaUpdateTime;
        float angle = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
        log.debug("****** gimbal velocity angle {} mag(d/s) {}", angle, magnitude );
        gimbalVelocity.setLocation(deltaX/deltaUpdateTime, deltaY/deltaUpdateTime);
    }

   
   public Point2D getGimbalVelocity() {
           return gimbalVelocity;  // velocity as 2D vector
   }
   
 
    // periodic update of the RS4 Gimbal state 
    private synchronized void updateGimbal() {
        fetchGimbalPose(); 
        
       if(isGimbalPoseEnabled()) { 
        setGimbalPoseDirect(targetYaw, targetRoll, targetPitch);
      }
    }
    
    
    // FetchGimbal retrieves fresh data directly from RS4ControllerV2 at each scheduled interval, 
    // regardless of whether data has changed.
    // This ensures GimbalBase consistently reflects the latest data from RS4ControllerV2, 
    // minimizing any discrepancies between the controller’s actual and reported poses.  
     private Pose fetchGimbalPose() {
       
          // store the current values
          previousGimbalPose = new Pose(currentGimbalPose.getYaw(), currentGimbalPose.getRoll(), currentGimbalPose.getPitch());
            
          currentGimbalPose = new Pose();
          // update thecurrent  values 
        if(!isDummyMode) {
           currentGimbalPose.setYaw(rs4controller.getYaw()-gimbalYawOffsetError);  // (deg, in gimbal polar space)
           currentGimbalPose.setRoll(rs4controller.getRoll());
           currentGimbalPose.setPitch(rs4controller.getPitch()-gimbalPitchOffsetError);
            }
            else { // use efference copy  
               currentGimbalPose.setYaw(currentSendPose.getYaw());  // (deg, in gimbal polar space)
               currentGimbalPose.setRoll(currentSendPose.getRoll());
               currentGimbalPose.setPitch(currentSendPose.getPitch());    
                   }
           
          // notify the listeners of polar cordinate updates
           pcs.firePropertyChange("FetchedGimbalPose", previousGimbalPose, currentGimbalPose);    
           log.debug("Fetched RS4Controller pose (y,r,p)  {}, {}, {}", currentGimbalPose.getYaw(), currentGimbalPose.getRoll(), currentGimbalPose.getPitch() );          
           return currentGimbalPose;
    }

      public void  setGimbalPoseDirect( Pose pose) {
          setGimbalPoseDirect(pose.getYaw(), pose.getRoll(), pose.getPitch()) ;
      }
      
      
    // direct update of the RS4    
     public synchronized void setGimbalPoseDirect( float yaw, float roll, float pitch) {           
          previousSendPose =  new Pose(currentSendPose.getYaw(), currentSendPose.getRoll(), currentSendPose.getPitch());
          currentSendPose = new Pose(rangeCheckYaw(yaw), rangeCheckRoll(roll), rangeCheckPitch(pitch)); 
          log.debug("^^^^^ currentSendPose  yaw :  {}, pitch :  {}  ",  currentSendPose.getYaw(), currentSendPose.getPitch());
          
         currentUpdateTime = System.currentTimeMillis();
         deltaUpdateTime = (float) (currentUpdateTime - previousUpdateTime)/(float)1000.0 ;
         previousUpdateTime = currentUpdateTime;
         updateGimbalVelocity();     
                   
        if (!isDummyMode) {
               rs4controller.setPoseDirect(currentSendPose.getYaw()+gimbalYawOffsetError, currentSendPose.getRoll(), currentSendPose.getPitch()+gimbalPitchOffsetError); // PanTilt does not consider Roll 
         } else  {
     //       currentSendPose = new Pose(currentSendPose.getYaw(), currentSendPose.getRoll(), currentSendPose.getPitch());
            log.debug("Dummy mode: Pose set to {}", currentSendPose);
        }
    
           this.pcs.firePropertyChange("SendGimbalPose", previousSendPose, currentSendPose); 
            log.debug("SendGimbalPoseDirect (y,r,p)  {}, {}, {}", currentSendPose.getYaw(), currentSendPose.getRoll(), currentSendPose.getPitch() );
    }
         
     
     
    // Shut down the controller
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * @return the defaultPose
     */
    public Pose getDefaultPose() {
        return defaultPose;
    }

    /**
     * @param defaultPose the defaultPose to set
     */
    public void setDefaultPose(Pose defaultPose) {
        this.defaultPose = defaultPose;
    }

    /**
     * @return the resetPose
     */
    public Pose getResetPose() {
        return resetPose;
    }

    /**
     * @param resetPose the resetPose to set
     */
    public void setResetPose(Pose resetPose) {
        this.resetPose = resetPose;
    }

    /**
     * @return the gimbalPoseEnabled
     */
    public boolean isGimbalPoseEnabled() {
        return gimbalPoseEnabled;
    }

    /**
     * @param gimbalPoseEnabled the gimbalPoseEnabled to set
     */
    public void setGimbalPoseEnabled(boolean gimbalPoseEnabled) {
        this.gimbalPoseEnabled = gimbalPoseEnabled;
    }
    
     public RS4ControllerGUISwingV1 getRS4ControllerGUI() {
        return rs4controllerGUI;
    }
    
}
