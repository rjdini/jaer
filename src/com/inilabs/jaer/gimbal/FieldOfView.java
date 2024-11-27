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
package com.inilabs.jaer.gimbal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.inilabs.jaer.projects.gui.BasicDrawable;
import com.inilabs.jaer.projects.gui.TrackerManager;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.EventType;
import com.inilabs.jaer.projects.tracker.EventCluster;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

public class FieldOfView extends BasicDrawable implements PropertyChangeListener {

    private static FieldOfView instance = null;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FieldOfView.class);
    
    // Default FOV dimensions and chip parameters
    private float focalLength = 100f;
    private float chipWidthPixels = 640f;
    private float chipHeightPixels = 480f;
    private final float FOVX = 20.0f;
    private final float FOVY = FOVX * (chipHeightPixels / chipWidthPixels);

    // Orientation (yaw, pitch, roll) in degrees
    private float axialYaw = 0f;
    private float axialPitch = 0f;
    private float axialRoll = 0f;
    private final List<EventCluster> clusters = new ArrayList<>();
    

    // Singleton pattern for FieldOfView instance
    public FieldOfView() {
        super();
        setColor(Color.RED);
        setSize(FOVX);
    }

    public static FieldOfView getInstance() {
        if (instance == null) {
            instance = new FieldOfView();
        }
        return instance;
    }

    // Property Change Listener Implementation
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("FetchedGimbalPose".equals(evt.getPropertyName())) {
            float[] newOrientation = (float[]) evt.getNewValue();
            setAxialYaw(newOrientation[0]);
            setAxialRoll(newOrientation[1]);
            setAxialPitch(newOrientation[2]);
             AgentLogger.logAgentEvent(EventType.MOVE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
        }
    }
    
    // ******* TO remove  - this is a temp hack to test logging of  FOV to json output
    // Helper method to get cluster keys as a list of strings
    public List<String> getClusterKeys() {
        return clusters.stream().map(EventCluster::getKey).collect(Collectors.toList());
    }
    
    
    
    @Override
    public void onTransformChanged(float azimuthScale, float elevationScale, float azimuthHeading, float elevationHeading, int centerX, int centerY) {
        this.azimuthScale = azimuthScale;
        this.elevationScale = elevationScale;
        this.azimuthHeading = azimuthHeading;
        this.elevationHeading = elevationHeading;
        this.centerX = centerX;
        this.centerY = centerY;
    }


    // Methods for adjusting and retrieving orientation
    // These methods get and set the FOV's  pose - 
   //   here called axis, since it is the reference for all otherframes of measurement of activity in the feild of view. 
   //
    // yaw, roll, pitch refer to the gimbals behavioral frame of reference.
    // azimuth amd elevation refer to the axis of the field of view in polar space
    
    public void setAxialYaw(float axialYaw) {
        this.axialYaw = axialYaw;
        setAzimuth(axialYaw);
    }
    
     public float getAxialYaw() {
        return axialYaw;
    }
    
     public void setAxialPitch(float axialPitch) {
        this.axialPitch = axialPitch;
        setElevation(axialPitch);
    }

       public float getAxialPitch() {
        return axialPitch;
    }
     
      public void setAxialRoll(float axialRoll) {
        this.axialRoll = axialRoll;
    }
      
    public float getAxialRoll() {
        return axialRoll;
    }  
    
    @Override
    public void setAzimuth(float azimuth) {
        super.setAzimuth(azimuth);
        this.axialYaw = azimuth;
        addCurrentPositionToPath();
    }
    
    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);
        this.axialPitch = elevation;
        addCurrentPositionToPath();
    }


    
      // Method to draw the Field of View in PolarSpace (azimuth and elevation)
    
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

   
        // Calculate screen coordinates based on polar coordinates and the transform broadcast
        int x = getCenterX() + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
        int y = getCenterY() - (int) ((getElevation() - getElevationHeading()) * getElevationScale());

        // Calculate box dimensions based on FOV and scales
        int boxWidth = (int) (FOVX * getAzimuthScale());
        int boxHeight = (int) (FOVY * getElevationScale());

        // Apply roll rotation and draw the FOV box
        AffineTransform originalTransform = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(axialRoll));
        g2d.setColor(getColor());
        g2d.drawRect(-boxWidth / 2, -boxHeight / 2, boxWidth, boxHeight);
        g2d.setTransform(originalTransform);

        // Draw path if enabled
        if (isPathVisible()) {
            drawPath(g2d);
        }
       
    }
    
     // pan and tilt are legacy dimensions from Tobi's pan tilt system. 
    // Both range 0-1, with 0,0 at bottom left. 0-1 normalizes the chip dimentsion (chip_width, chip height) 
    // Field of View-specific calculations
    
    // get yaw and pitch in degrees, given the pan and tilt.
    public float getYawAtPan(float pan) {
        return axialYaw + (pan - 0.5f) * FOVX;
    }

    public float getPitchAtTilt(float tilt) {
        return axialPitch + (tilt - 0.5f) * FOVY;
    }

    
    // get the chip pixel at the requested absolute yaw/picth
    public float getPixelsAtYaw(float yaw) {
        float pixatyaw = chipWidthPixels / FOVX * (yaw - axialYaw) + chipWidthPixels / 2;
        if (pixatyaw < 0 )  { 
            pixatyaw = 0 ;
            log.warn("Yaw to pixels beneath range -> trunated to 0.");
        }
         if (pixatyaw > chipWidthPixels )  { 
            pixatyaw = chipWidthPixels ; 
              log.warn("yaw to pixels beyond range -> trunated to chipWidth.");
        }
        return pixatyaw;   
    }

    public float getPixelsAtPitch(float pitch) {
        float pixatpitch = chipHeightPixels / FOVY * (pitch - axialPitch) + chipHeightPixels / 2;
    if (pixatpitch < 0 )  { 
            pixatpitch = 0 ;
            log.warn("pitch to pixels beneath range -> trunated to 0.");
        }
         if (pixatpitch > chipWidthPixels )  { 
            pixatpitch = chipWidthPixels ;
                  log.warn("pitch to pixels above range -> trunated to chipHeight.");
        }
    return pixatpitch;
    }

    
    
    public float getPixelsAtPan(float pan) {
        return getPixelsAtYaw(getYawAtPan(pan));
    }

    public float getPixelsAtTilt(float tilt) {
        return getPixelsAtPitch(getPitchAtTilt(tilt));
    }

    public float getPanAtYaw(float yaw) {
        return 0.5f + (yaw - axialYaw) / FOVX;
    }

    public float getTiltAtPitch(float pitch) {
        return 0.5f + (pitch - axialPitch) / FOVY;
    }
    
    /**
     * Converts a given x-coordinate (pixel) into the corresponding yaw (azimuth) angle
     * relative to the center point (heading) of the display.
     * @param pixelX The x-coordinate on the display.
     * @return The yaw (azimuth) in degrees at the specified pixel.
     */
    public float getYawAtPixel(float pixelX) {
        // Calculate pixel offset from the center of the display
        float pixelOffsetX = pixelX - getCenterX();

        // Convert pixel offset to yaw angle using the azimuth scale
        return pixelOffsetX * getAzimuthScale();
    }

    /**
     * Converts a given y-coordinate (pixel) into the corresponding pitch (elevation) angle
     * relative to the center point (heading) of the display.
     * @param pixelY The y-coordinate on the display.
     * @return The pitch (elevation) in degrees at the specified pixel.
     */
    public float getPitchAtPixel(float pixelY) {
        // Calculate pixel offset from the center of the display
        float pixelOffsetY = getCenterY() - pixelY; // Note: Y-axis is typically inverted

        // Convert pixel offset to pitch angle using the elevation scale
        return pixelOffsetY * getElevationScale();
    }
    
  
    // Set chip dimensions and update dependent parameters
    public void setChipDimensions(float width, float height) {
        this.chipWidthPixels = width;
        this.chipHeightPixels = height;
    }

    // Set focal length
    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
    }
}

