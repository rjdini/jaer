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
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.inilabs.jaer.projects.space3d.AgentDrawable;

public class FieldOfView extends AgentDrawable implements PropertyChangeListener {

    private static final Logger log = (Logger) LoggerFactory.getLogger(FieldOfView.class);
    private static GimbalBase gimbalbase = GimbalBase.getInstance();
    private static FieldOfView instance = null;
    private float focalLength = 100; // Focal length in mm
    private float chipWidthPixels = 640f;
    private float chipHeightPixels = 480f;
    private float chipFoveaX = chipWidthPixels / 2;
    private float chipFoveaY = chipHeightPixels / 2;
    private float chipAspectRatio = chipHeightPixels / chipWidthPixels;
    private float chipPixelPitch = 9; // micron
    private final float FOVX = 30.0f; // Field of view in degrees along X-axis
    private final float FOVY = FOVX * chipAspectRatio; // Field of view in degrees along Y-axis
    private float pan2Xdegs = FOVX;
    private float pan2Ydegs = FOVY;
    private float Xdegs2Pan = 1 / FOVX;
    private float Ydegs3Tilt = 1 / FOVY;
    private float pixelsPerXdeg = chipWidthPixels / FOVX;
    private float pixelsPerYdeg = chipHeightPixels / FOVY;

    private double azimuthScale = 1.0;
    private double elevationScale = 1.0;

    // Axial rotations in degrees
    private float axialYaw = 0f;
    private float axialRoll = 0f;
    private float axialPitch = -30f;

    
    // Constructor now accepts yaw, pitch, and roll in degrees
    private FieldOfView(float yawDegrees, float pitchDegrees, float rollDegrees) {
        super("FieldOfView", 0, 0, Color.RED, 20); // Default size, color, and position
        this.axialYaw = yawDegrees;
        this.axialPitch = pitchDegrees;
        this.axialRoll = rollDegrees;
    }

    public static FieldOfView getInstance(float yawDegrees, float pitchDegrees, float rollDegrees) {
        if (instance == null) {
            instance = new FieldOfView(yawDegrees, pitchDegrees, rollDegrees);
        }
        return instance;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        float[] newValues = (float[]) evt.getNewValue();
        switch (evt.getPropertyName()) {
            case "FetchedGimbalPose":
                this.axialYaw = newValues[0];
                this.axialRoll = newValues[1];
                this.axialPitch = newValues[2];
                log.info("Received Gimbal Pose (Yaw, Roll, Pitch): " + axialYaw + ", " + axialRoll + ", " + axialPitch);
                break;
        }
    }

    @Override
    public void onScaleUpdate(double azimuthScale, double elevationScale) {
        this.azimuthScale = azimuthScale;
        this.elevationScale = elevationScale;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int boxWidth = (int) (FOVX * azimuthScale * 10);
        int boxHeight = (int) (FOVY * elevationScale * 10);

        int centerX = (int) (axialYaw * azimuthScale);
        int centerY = (int) (-axialPitch * elevationScale);

        AffineTransform originalTransform = g2d.getTransform();
        AffineTransform transform = new AffineTransform();
        transform.translate(centerX, centerY);
        transform.rotate(Math.toRadians(axialRoll));

        g2d.setTransform(transform);
        g2d.setColor(Color.RED);
        g2d.drawRect(-boxWidth / 2, -boxHeight / 2, boxWidth, boxHeight);

        g2d.setTransform(originalTransform);
    }

    // FOV calculations
    public float getYawAtPan(float pan) {
        return gimbalbase.getYaw() + fovea(pan) * pan2Xdegs;
    }

    public float getPitchAtTilt(float tilt) {
        return gimbalbase.getPitch() + fovea(tilt) * pan2Ydegs;
    }

    public float getPanAtYaw(float yaw) {
        float deltaYaw = gimbalbase.getYaw() - axialYaw;
        float relative = deltaYaw / FOVY;
        if (relative >= 0.5) {
            log.warn("********relative pan = " + relative);
            relative = 0.5f;
        } else if (relative <= -0.5f) {
            relative = -0.5f;
        }
        return (relative + 0.5f);
    }

    public float getTiltAtPitch(float pitch) {
        float deltaPitch = gimbalbase.getPitch() - axialPitch;
        float relative = deltaPitch / FOVY;
        if (relative >= 0.5) {
            relative = 0.5f;
        } else if (relative <= -0.5f) {
            relative = -0.5f;
        }
        return (relative + 0.5f);
    }

    public float getYawAtPixelX(int xpix) {
        float pan = xpix / chipWidthPixels;
        return getYawAtPan(pan);
    }

    public float getPitchAtPixelY(int ypix) {
        float tilt = ypix / chipHeightPixels;
        return getPitchAtTilt(tilt);
    }

    public float getPixelsAtPan(float pan) {
        return pan * chipWidthPixels;
    }

    public float getPixelsAtTilt(float tilt) {
        return tilt * chipHeightPixels;
    }

    public float getPixelsAtYaw(float yaw) {
        float pixelX = chipFoveaX + (yaw - axialYaw) * pixelsPerXdeg;
        return Math.max(0, Math.min(pixelX, chipWidthPixels));
    }

    public float getPixelsAtPitch(float pitch) {
        float pixelY = chipFoveaY + (pitch - axialPitch) * pixelsPerYdeg;
        return Math.max(0, Math.min(pixelY, chipHeightPixels));
    }

    // Getters and setters
    public float getWidthPixels() {
        return chipWidthPixels;
    }

    public void setWidthPixels(int pix) {
        chipWidthPixels = pix;
    }

    public float getHeightPixels() {
        return chipHeightPixels;
    }

    public void setHeightPixels(int pix) {
        chipHeightPixels = pix;
    }

    public float getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(float fl) {
        focalLength = fl;
    }

    private float fovea(float displace) {
        return displace - 0.5f;
    }

    public float getAxialYaw() {
        return axialYaw;
    }

    public void setAxialYaw(float axialYaw) {
        this.axialYaw = axialYaw;
    }

    public float getAxialRoll() {
        return axialRoll;
    }

    public void setAxialRoll(float axialRoll) {
        this.axialRoll = axialRoll;
    }

    public float getAxialPitch() {
        return axialPitch;
    }

    public void setAxialPitch(float axialPitch) {
        this.axialPitch = axialPitch;
    }

    public float getYaw() {
        return axialYaw;
    }

    public void setYaw(float yaw) {
        this.axialYaw = yaw;
    }

    public float getRoll() {
        return axialRoll;
    }

    public void setRoll(float roll) {
        this.axialRoll = roll;
    }

    public float getPitch() {
        return axialPitch;
    }

    public void setPitch(float pitch) {
        this.axialPitch = pitch;
    }
}

