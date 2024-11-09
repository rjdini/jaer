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
package com.inilabs.jaer.projects.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import com.inilabs.jaer.gimbal.FieldOfView;

public class FieldOfViewTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize the PolarSpace GUI and display
            PolarSpaceGUI gui = new PolarSpaceGUI();
            PolarSpaceDisplay display = gui.getPolarSpaceDisplay();

            // Create a FieldOfView instance and add it to the display
            FieldOfView fov = FieldOfView.getInstance();
            fov.setColor(Color.MAGENTA);  // Set FOV color for visibility
            display.addDrawable(fov);

            // Set initial azimuth, elevation, and roll
            fov.setAzimuth(0f);
            fov.setElevation(0f);
            fov.setAxialRoll(0f);
            display.repaint();

            // Test 1: Cycle through azimuth values
            javax.swing.Timer azimuthTimer = new javax.swing.Timer(2000, e -> {
                float newAzimuth = fov.getAzimuth() + 10f;
                if (newAzimuth > 40f) newAzimuth = -40f;  // Wrap around within +/-40 degrees
                System.out.println("Setting Azimuth to: " + newAzimuth);
                fov.setAzimuth(newAzimuth);
                display.repaint();
            });
            azimuthTimer.start();

            // Test 2: Cycle through elevation values
            javax.swing.Timer elevationTimer = new javax.swing.Timer(3000, e -> {
                float newElevation = fov.getElevation() + 10f;
                if (newElevation > 30f) newElevation = -30f;  // Wrap around within +/-30 degrees
                System.out.println("Setting Elevation to: " + newElevation);
                fov.setElevation(newElevation);
                display.repaint();
            });
            elevationTimer.start();

            // Test 3: Cycle through roll values
            Timer rollTimer = new Timer();
            rollTimer.scheduleAtFixedRate(new TimerTask() {
                float currentRoll = 0f;

                @Override
                public void run() {
                    currentRoll += 15f;
                    if (currentRoll >= 360f) currentRoll -= 360f;  // Keep roll within 0-360 degrees
                    System.out.println("Setting Roll to: " + currentRoll);
                    fov.setAxialRoll(currentRoll);
                    display.repaint();
                }
            }, 0, 4000);  // Update roll every 4 seconds
        });
    }
}

