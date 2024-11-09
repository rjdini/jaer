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

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import com.inilabs.jaer.projects.gui.AgentDrawable;
import java.awt.geom.AffineTransform;

public class FieldOfView extends AgentDrawable {

    private static final Logger log = (Logger) LoggerFactory.getLogger(FieldOfView.class);
    private static FieldOfView instance = null;

    private float focalLength = 100f;
    private float chipWidthPixels = 640f;
    private float chipHeightPixels = 480f;
    private float chipFoveaX = chipWidthPixels / 2;
    private float chipFoveaY = chipHeightPixels / 2;
    private float chipAspectRatio = chipHeightPixels / chipWidthPixels;

    // Field of View in degrees for X and Y axes
    private final float FOVX = 20.0f;
    private final float FOVY = FOVX * chipAspectRatio;

    private float axialYaw = 0f;
    private float axialRoll = 0f;
    private float axialPitch = 0f;

    // Singleton pattern
    private FieldOfView() {
        super("FieldOfView");
        setColor(Color.RED);
        setSize(FOVX);  // Set size using FOVX to initialize dimensions in degrees
    }
    
    public static FieldOfView getInstance() {
        if (instance == null) {
            instance = new FieldOfView();
        }
        return instance;
    }

    // Method to set focal length
    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
        log.info("Focal length set to: " + focalLength);
    }

    // Method to set chip dimensions and update related properties
    public void setChipDimensions(float width, float height) {
        this.chipWidthPixels = width;
        this.chipHeightPixels = height;
        this.chipFoveaX = width / 2;
        this.chipFoveaY = height / 2;
        this.chipAspectRatio = height / width;
        log.info("Chip dimensions set to width: {}, height: {}", width, height);
    }

    
    // Override draw() to render Field of View box
   @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        updateGraphicsLocation(g);
      //  drawShapeAtLocation(g2d);  // debug
      
//        // Draw the FOV
        int boxWidth = (int) (FOVX * azimuthScale);
        int boxHeight = (int) (FOVY * elevationScale);
        g2d.setColor(getColor());
        
        // Apply rotation and translate to calculated center
        AffineTransform originalTransform = g2d.getTransform();
       g2d.translate(centerX,  centerY);
       g2d.rotate(Math.toRadians(axialRoll));
       g2d.drawRect(-boxWidth/2 , -boxHeight/2, boxWidth, boxHeight);  //box g2d takes top left corner as starting point
     // Reset the transform
        g2d.setTransform(originalTransform);
  
     log.info("Rendering FieldOfView at (centerX: {}, centerY: {}) with width: {} and height: {}", centerX, centerY, boxWidth, boxHeight);
    }
    
    // Field of View-specific getters for yaw and pitch based on pan/tilt
    public float getYawAtPan(float pan) {
        return axialYaw + (pan - 0.5f) * FOVX;
    }

    public float getPitchAtTilt(float tilt) {
        return axialPitch + (tilt - 0.5f) * FOVY;
    }

    
    //The yaw, pitch, roll convention for pose, is differnt to the azimuth and elevation of objects observed in polar space.
   // We need to treat the gimbal case differnt than we treat its targets. 
    // Setters and getters for orientation
    public void setAxialYaw(float axialYaw) {
        this.axialYaw = axialYaw;
        setAzimuth(axialYaw);
    }
    
    @Override
    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
        this.axialYaw = azimuth;
    }

    public void setAxialPitch(float axialPitch) {
        this.axialPitch = axialPitch;
        setElevation(axialPitch);
    }

    @Override
    public void setElevation(float elevation) {
        this.elevation = elevation;
        this.axialPitch = elevation;
    }
    
    
    public void setAxialRoll(float axialRoll) {
        this.axialRoll = axialRoll;
    }

    public float getAxialYaw() {
        return axialYaw;
    }

    public float getAxialPitch() {
        return axialPitch;
    }

    public float getAxialRoll() {
        return axialRoll;
    }
}

