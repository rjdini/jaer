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

<<<<<<< HEAD
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

=======
>>>>>>> working
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
<<<<<<< HEAD

import com.inilabs.jaer.projects.space3d.AgentDrawable;

public class FieldOfView extends AgentDrawable implements PropertyChangeListener {

    private static final Logger log = (Logger) LoggerFactory.getLogger(FieldOfView.class);
    private static FieldOfView instance = null;
    private final float FOVX = 30.0f;  // Field of view in degrees along X-axis
    private final float FOVY;          // Field of view in degrees along Y-axis, based on aspect ratio

    // Camera and FOV parameters
    private float focalLength = 100;
    private float chipWidthPixels = 640f;
    private float chipHeightPixels = 480f;
    private float chipAspectRatio;
    private float chipPixelPitch = 9;  // micron
    private float pixelsPerXdeg;
    private float pixelsPerYdeg;

    // Axial rotations in degrees
    private float axialYaw = 0f;
    private float axialRoll = 0f;
    private float axialPitch = -30f;

    private double azimuthScale = 1.0;
    private double elevationScale = 1.0;

    // Singleton Pattern for FieldOfView instance
    private FieldOfView(){
        // Adjusted constructor to match the AgentDrawable structure
        chipAspectRatio = chipHeightPixels / chipWidthPixels;
        FOVY = FOVX * chipAspectRatio;
        updatePixelPerDegree();
    }

    public static FieldOfView getInstance(float yawDegrees, float pitchDegrees, float rollDegrees) {
        if (instance == null) {
            instance = new FieldOfView(yawDegrees, pitchDegrees, rollDegrees);
=======
import java.beans.PropertyChangeSupport;

import com.inilabs.jaer.projects.gui.BasicDrawable;

public class FieldOfView extends BasicDrawable implements PropertyChangeListener {

    private static FieldOfView instance = null;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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

    // Singleton pattern for FieldOfView instance
    private FieldOfView() {
        super();
        setColor(Color.RED);
        setSize(FOVX);
    }

    public static FieldOfView getInstance() {
        if (instance == null) {
            instance = new FieldOfView();
>>>>>>> working
        }
        return instance;
    }

<<<<<<< HEAD
    private void updatePixelPerDegree() {
        pixelsPerXdeg = chipWidthPixels / FOVX;
        pixelsPerYdeg = chipHeightPixels / FOVY;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("FetchedGimbalPose".equals(evt.getPropertyName())) {
            float[] newValues = (float[]) evt.getNewValue();
            this.axialYaw = newValues[0];
            this.axialRoll = newValues[1];
            this.axialPitch = newValues[2];
            log.info("Updated Gimbal Pose (Yaw, Roll, Pitch): " + axialYaw + ", " + axialRoll + ", " + axialPitch);
        }
    }

    @Override
    public void onAzimuthScaleChanged(double azimuthScale) {
        this.azimuthScale = azimuthScale;
    }

    @Override
    public void onElevationScaleChanged(double elevationScale) {
        this.elevationScale = elevationScale;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int boxWidth = (int) (FOVX * azimuthScale * 10);
        int boxHeight = (int) (FOVY * elevationScale * 10);

        // Convert axialYaw and axialPitch to scaled pixel coordinates
        int centerX = (int) (axialYaw * azimuthScale) + (g.getClipBounds().width / 2);
        int centerY = (int) (-axialPitch * elevationScale) + (g.getClipBounds().height / 2);

        AffineTransform originalTransform = g2d.getTransform();
        AffineTransform transform = new AffineTransform();
        transform.translate(centerX, centerY);
        transform.rotate(Math.toRadians(axialRoll));

        g2d.setTransform(transform);
        g2d.setColor(Color.RED);
        g2d.drawRect(-boxWidth / 2, -boxHeight / 2, boxWidth, boxHeight);

        g2d.setTransform(originalTransform);
    }

    // Field of View calculations
    public float getYawAtPan(float pan) {
        return axialYaw + fovea(pan) * FOVX;
    }

    public float getPitchAtTilt(float tilt) {
        return axialPitch + fovea(tilt) * FOVY;
    }

    public float getPixelsAtYaw(float yaw) {
        float pixelX = (yaw - axialYaw) * pixelsPerXdeg + chipWidthPixels / 2;
        return Math.max(0, Math.min(pixelX, chipWidthPixels));
    }

    public float getPixelsAtPitch(float pitch) {
        float pixelY = (pitch - axialPitch) * pixelsPerYdeg + chipHeightPixels / 2;
        return Math.max(0, Math.min(pixelY, chipHeightPixels));
    }

    private float fovea(float displace) {
        return displace - 0.5f;
    }

    // Getters and setters for other FOV properties
    public float getAxialYaw() { return axialYaw; }
    public void setAxialYaw(float axialYaw) { this.axialYaw = axialYaw; }
    
    public float getAxialPitch() { return axialPitch; }
    public void setAxialPitch(float axialPitch) { this.axialPitch = axialPitch; }
    
    public float getAxialRoll() { return axialRoll; }
    public void setAxialRoll(float axialRoll) { this.axialRoll = axialRoll; }

    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
    }

    public void setChipDimensions(int width, int height) {
        this.chipWidthPixels = width;
        this.chipHeightPixels = height;
        updatePixelPerDegree();
    }
=======
    // Property Change Listener Implementation
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("FetchedGimbalPose".equals(evt.getPropertyName())) {
            float[] newOrientation = (float[]) evt.getNewValue();
            setAxialYaw(newOrientation[0]);
            setAxialRoll(newOrientation[1]);
            setAxialPitch(newOrientation[2]);
        }
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

      // Method to draw the Field of View, positioning based on azimuth and elevation
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

    // Methods for adjusting and retrieving orientation
    public void setAxialYaw(float axialYaw) {
        this.axialYaw = axialYaw;
        setAzimuth(axialYaw);
    }

    @Override
    public void setAzimuth(float azimuth) {
        super.setAzimuth(azimuth);
        this.axialYaw = azimuth;
        addCurrentPositionToPath();
    }

    public void setAxialPitch(float axialPitch) {
        this.axialPitch = axialPitch;
        setElevation(axialPitch);
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);
        this.axialPitch = elevation;
        addCurrentPositionToPath();
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

    // Field of View-specific calculations
    public float getYawAtPan(float pan) {
        return axialYaw + (pan - 0.5f) * FOVX;
    }

    public float getPitchAtTilt(float tilt) {
        return axialPitch + (tilt - 0.5f) * FOVY;
    }

    public float getPixelsAtYaw(float yaw) {
        return chipWidthPixels / FOVX * (yaw - axialYaw) + chipWidthPixels / 2;
    }

    public float getPixelsAtPitch(float pitch) {
        return chipHeightPixels / FOVY * (pitch - axialPitch) + chipHeightPixels / 2;
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
>>>>>>> working
}

