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
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
        }
        return instance;
    }

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
}

