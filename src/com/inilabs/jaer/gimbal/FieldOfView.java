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
        }
    }

      // Method to draw the Field of View, positioning based on azimuth and elevation
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Calculate position based on azimuth and elevation scales
        int centerX = g.getClipBounds().width / 2;
        int centerY = g.getClipBounds().height / 2;
        int x = centerX + (int) (getAzimuth() * getAzimuthScale());
        int y = centerY - (int) (getElevation() * getElevationScale());

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

