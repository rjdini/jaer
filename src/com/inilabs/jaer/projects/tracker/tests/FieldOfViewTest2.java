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
package com.inilabs.jaer.projects.tracker.tests;

import com.inilabs.jaer.projects.tracker.FieldOfView;

public class FieldOfViewTest2 {

    public static void main(String[] args) {
        testInitializationAtZeroPose();
        testInitializationAtNonZeroPose();
        testPolarInitializationAtZeroPose();
        testPolarInitializationAtNonZeroPose();
        testPolarInitializationWithAbsolutePixelAtZeroPose();
        testPolarInitializationWithAbsolutePixelAtNonZeroPose();
    }

    private static void testInitializationAtZeroPose() {
        System.out.println("Testing Initialization at Zero Pose...");

        FieldOfView fov = new FieldOfView();
        fov.setPose(0f, 0f, 0f);

        assert fov.getAxialYaw() == 0f : "Yaw mismatch! Expected 0.0, got " + fov.getAxialYaw();
        assert fov.getAxialPitch() == 0f : "Pitch mismatch! Expected 0.0, got " + fov.getAxialPitch();
        assert fov.getAxialRoll() == 0f : "Roll mismatch! Expected 0.0, got " + fov.getAxialRoll();

        System.out.println("Passed: Initialization at Zero Pose.");
    }

    private static void testInitializationAtNonZeroPose() {
        System.out.println("Testing Initialization at Non-Zero Pose...");

        FieldOfView fov = new FieldOfView();
        fov.setPose(45f, -30f, 90f);

        assert fov.getAxialYaw() == 45f : "Yaw mismatch! Expected 45.0, got " + fov.getAxialYaw();
        assert fov.getAxialPitch() == -30f : "Pitch mismatch! Expected -30.0, got " + fov.getAxialPitch();
        assert fov.getAxialRoll() == 90f : "Roll mismatch! Expected 90.0, got " + fov.getAxialRoll();

        System.out.println("Passed: Initialization at Non-Zero Pose.");
    }

    private static void testPolarInitializationAtZeroPose() {
        System.out.println("Testing Polar Initialization at Zero Pose...");

        FieldOfView fov = new FieldOfView();
        fov.setPose(0f, 0f, 0f);

        float yawAtPixel = fov.getYawAtPixel(fov.getPixelsAtYaw(0f));
        float pitchAtPixel = fov.getPitchAtPixel(fov.getPixelsAtPitch(0f));

        assert Math.abs(yawAtPixel) < 0.01f : "Yaw mismatch at zero pose! Expected ~0.0, got " + yawAtPixel;
        assert Math.abs(pitchAtPixel) < 0.01f : "Pitch mismatch at zero pose! Expected ~0.0, got " + pitchAtPixel;

        System.out.println("Passed: Polar Initialization at Zero Pose.");
    }

    private static void testPolarInitializationAtNonZeroPose() {
        System.out.println("Testing Polar Initialization at Non-Zero Pose...");

        FieldOfView fov = new FieldOfView();
        fov.setPose(45f, -30f, 90f);

        float yawAtPixel = fov.getYawAtPixel(fov.getPixelsAtYaw(45f));
        float pitchAtPixel = fov.getPitchAtPixel(fov.getPixelsAtPitch(-30f));

        assert Math.abs(yawAtPixel - 45f) < 0.01f : "Yaw mismatch at non-zero pose! Expected 45.0, got " + yawAtPixel;
        assert Math.abs(pitchAtPixel - (-30f)) < 0.01f : "Pitch mismatch at non-zero pose! Expected -30.0, got " + pitchAtPixel;

        System.out.println("Passed: Polar Initialization at Non-Zero Pose.");
    }

    private static void testPolarInitializationWithAbsolutePixelAtZeroPose() {
        System.out.println("Testing Polar Initialization with Absolute Pixel at Zero Pose...");

        FieldOfView fov = new FieldOfView();
        fov.setPose(0f, 0f, 0f);

        int centerX = (int)fov.getPixelsAtYaw(0f);
        int centerY = (int)fov.getPixelsAtPitch(0f);

        float yaw = fov.getYawAtPixel(centerX);
        float pitch = fov.getPitchAtPixel(centerY);

        assert Math.abs(yaw) < 0.01f : "Yaw mismatch at absolute pixel! Expected ~0.0, got " + yaw;
        assert Math.abs(pitch) < 0.01f : "Pitch mismatch at absolute pixel! Expected ~0.0, got " + pitch;

        System.out.println("Passed: Polar Initialization with Absolute Pixel at Zero Pose.");
    }

    private static void testPolarInitializationWithAbsolutePixelAtNonZeroPose() {
        System.out.println("Testing Polar Initialization with Absolute Pixel at Non-Zero Pose...");

        FieldOfView fov = new FieldOfView();
        fov.setPose(45f, -30f, 90f);

        int pixelX = (int)fov.getPixelsAtYaw(45f);
        int pixelY = (int)fov.getPixelsAtPitch(-30f);

        float yaw = fov.getYawAtPixel(pixelX);
        float pitch = fov.getPitchAtPixel(pixelY);

        assert Math.abs(yaw - 45f) < 0.01f : "Yaw mismatch at absolute pixel! Expected 45.0, got " + yaw;
        assert Math.abs(pitch - (-30f)) < 0.01f : "Pitch mismatch at absolute pixel! Expected -30.0, got " + pitch;

        System.out.println("Passed: Polar Initialization with Absolute Pixel at Non-Zero Pose.");
    }
}

