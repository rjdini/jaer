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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PIDController {

    private final Gimbal gimbal;

    // PID coefficients
    private float kP;
    private float kI;
    private float kD;

    // State variables
    private float yawIntegral = 0, rollIntegral = 0, pitchIntegral = 0;
    private float prevYawError = 0, prevRollError = 0, prevPitchError = 0;
    private long lastUpdateTime = 0;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    // Constructor
    public PIDController(Gimbal gimbal, float kP, float kI, float kD) {
        this.gimbal = gimbal;
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;

        // Run at 10 Hz (100 ms interval)
        executor.scheduleAtFixedRate(this::updateGimbal, 0, 20, TimeUnit.MILLISECONDS);
    }

    public void setPIDCoefficients(float kP, float kI, float kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    // Upstream target pose
    private volatile float targetYaw = 0, targetRoll = 0, targetPitch = 0;

    // Method to update target pose
    public synchronized void setTargetPose(float yaw, float roll, float pitch) {
        this.targetYaw = yaw;
        this.targetRoll = roll;
        this.targetPitch = pitch;
    }

    // Update the PID state at 10 Hz
    private synchronized void updateGimbal() {
        // Get current gimbal pose (delayed)
        Pose currentPose = gimbal.getPose();
        if (currentPose == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f; // Convert to seconds
        if (lastUpdateTime == 0 || deltaTime <= 0) {
            lastUpdateTime = currentTime;
            return; // Skip first iteration or invalid delta time
        }
        lastUpdateTime = currentTime;

        // Calculate errors
        float yawError = targetYaw - currentPose.getYaw();
        float rollError = targetRoll - currentPose.getRoll();
        float pitchError = targetPitch - currentPose.getPitch();

        // Proportional terms
        float yawP = kP * yawError;
        float rollP = kP * rollError;
        float pitchP = kP * pitchError;

        // Integral terms
        yawIntegral += yawError * deltaTime;
        rollIntegral += rollError * deltaTime;
        pitchIntegral += pitchError * deltaTime;

        float yawI = kI * yawIntegral;
        float rollI = kI * rollIntegral;
        float pitchI = kI * pitchIntegral;

        // Derivative terms
        float yawD = kD * (yawError - prevYawError) / deltaTime;
        float rollD = kD * (rollError - prevRollError) / deltaTime;
        float pitchD = kD * (pitchError - prevPitchError) / deltaTime;

        prevYawError = yawError;
        prevRollError = rollError;
        prevPitchError = pitchError;

        // Calculate control outputs
        float yawControl = yawP + yawI + yawD;
        float rollControl = rollP + rollI + rollD;
        float pitchControl = pitchP + pitchI + pitchD;

        // Send commands to the gimbal
        gimbal.setPose(yawControl, rollControl, pitchControl);
    }

    // Shut down the controller
    public void shutdown() {
        executor.shutdown();
    }
}
