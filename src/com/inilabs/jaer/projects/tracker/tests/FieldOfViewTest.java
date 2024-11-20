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

public class FieldOfViewTest {

    public static void main(String[] args) {
        testSingletonBehavior();
        testPoseSettersAndGetters();
        testPixelToAngleConversions();
        testChipDimensionUpdates();
    }

    private static void testSingletonBehavior() {
        System.out.println("Testing Singleton Behavior...");

        FieldOfView fov1 = new FieldOfView();
        FieldOfView fov2 = new FieldOfView();

        assert fov1 == fov2 : "Singleton instances are not the same!";
        System.out.println("Passed: Singleton behavior.");
    }

    private static void testPoseSettersAndGetters() {
        System.out.println("Testing Pose Setters and Getters...");

        FieldOfView fov = new FieldOfView();

        // Set and validate yaw
        fov.setAxialYaw(30f);
        assert fov.getAxialYaw() == 30f : "Yaw mismatch! Expected 30.0, got " + fov.getAxialYaw();

        // Set and validate pitch
        fov.setAxialPitch(-15f);
        assert fov.getAxialPitch() == -15f : "Pitch mismatch! Expected -15.0, got " + fov.getAxialPitch();

        // Set and validate roll
        fov.setAxialRoll(45f);
        assert fov.getAxialRoll() == 45f : "Roll mismatch! Expected 45.0, got " + fov.getAxialRoll();

        System.out.println("Passed: Pose setters and getters.");
    }

    private static void testPixelToAngleConversions() {
        System.out.println("Testing Pixel-to-Angle Conversions...");

        FieldOfView fov = new FieldOfView();

        // Set chip dimensions
        fov.setChipDimensions(640, 480);

        // Test yaw at pixel
        float pixelX = 320f; // Center of the chip
        float yaw = fov.getYawAtPixel(pixelX);
        assert Math.abs(yaw - fov.getPose()[0]) < 0.01f :
                "Yaw mismatch! Expected center yaw " + fov.getPose()[0] + ", got " + yaw;

        // Test pitch at pixel
        float pixelY = 240f; // Center of the chip
        float pitch = fov.getPitchAtPixel(pixelY);
        assert Math.abs(pitch - fov.getPose()[2]) < 0.01f :
                "Pitch mismatch! Expected center pitch " + fov.getPose()[2] + ", got " + pitch;

        System.out.println("Passed: Pixel-to-Angle conversions.");
    }

    private static void testChipDimensionUpdates() {
        System.out.println("Testing Chip Dimension Updates...");

        FieldOfView fov = new FieldOfView();

        // Set new chip dimensions
        fov.setChipDimensions(800, 600);

        // Validate chip dimensions
        assert fov.getPixelsAtYaw(0) == 400 :
                "Chip width mismatch! Expected pixel center 400, got " + fov.getPixelsAtYaw(0);
        assert fov.getPixelsAtPitch(0) == 300 :
                "Chip height mismatch! Expected pixel center 300, got " + fov.getPixelsAtPitch(0);

        System.out.println("Passed: Chip dimension updates.");
    }
}
