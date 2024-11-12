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

package com.inilabs.jaer.projects.gui.tests;

import com.inilabs.jaer.projects.gui.BasicDrawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class DrawableTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize the PolarSpace GUI and display
            PolarSpaceGUI gui = new PolarSpaceGUI();
            PolarSpaceDisplay display = gui.getPolarSpaceDisplay();

            // Test 1: Adding a drawable at specified position
            BasicDrawable agent1 = new BasicDrawable();
            agent1.setAzimuth(10f);   // Set azimuth to 10 degrees
            agent1.setElevation(-20f); // Set elevation to -20 degrees
            agent1.setSize(5f);       // Set size in degrees
            agent1.setColor(Color.RED);
            display.addDrawable(agent1);

            // Test 2: Adding multiple drawables with different positions and sizes
            BasicDrawable agent2 = new BasicDrawable();
            agent2.setAzimuth(30f);    // Set azimuth to 30 degrees
            agent2.setElevation(10f);  // Set elevation to 10 degrees
            agent2.setSize(8f);        // Larger size in degrees
            agent2.setColor(Color.BLUE);
            display.addDrawable(agent2);

            BasicDrawable agent3 = new BasicDrawable();
            agent3.setAzimuth(-15f);   // Set azimuth to -15 degrees
            agent3.setElevation(25f);  // Set elevation to 25 degrees
            agent3.setSize(6f);        // Set size in degrees
            agent3.setColor(Color.GREEN);
            display.addDrawable(agent3);

            // Test 3: Simulate autonomous removal of a drawable
            javax.swing.Timer removeTimer = new javax.swing.Timer(5000, e -> {
                System.out.println("Removing Agent2 autonomously");
                display.removeDrawable("Agent2");  // Agent2 requests self-removal after 5 seconds
                display.repaint();
            });
            removeTimer.setRepeats(false);
            removeTimer.start();

            // Test 4: Update scaling and observe drawables adjust in real-time
            javax.swing.Timer scaleUpdateTimer = new javax.swing.Timer(7000, e -> {
                System.out.println("Updating display ranges for scaling test");
                display.setAzimuthRange(40f);  // Narrower range to test scaling
                display.setElevationRange(40f);
                display.repaint();
            });
            scaleUpdateTimer.setRepeats(false);
            scaleUpdateTimer.start();

            // Test 5: Restore range after additional delay
            javax.swing.Timer rangeRestoreTimer = new javax.swing.Timer(10000, e -> {
                System.out.println("Restoring display ranges");
                display.setAzimuthRange(90f);  // Original range
                display.setElevationRange(90f);
                display.repaint();
            });
            rangeRestoreTimer.setRepeats(false);
            rangeRestoreTimer.start();

            // Test 6: Moving a magenta drawable agent periodically to the four corners of a 20-degree square
            BasicDrawable movingAgent = new BasicDrawable();
            movingAgent.setSize(4f);         // Set size of agent
            movingAgent.setColor(Color.MAGENTA); // Magenta color
            display.addDrawable(movingAgent);

            // Use java.util.Timer for periodic movement
            java.util.Timer moveTimer = new java.util.Timer();
            float[][] positions = {
                {10f, 10f},  // Top-right corner
                {-10f, 10f}, // Top-left corner
                {-10f, -10f},// Bottom-left corner
                {10f, -10f}  // Bottom-right corner
            };

            moveTimer.scheduleAtFixedRate(new TimerTask() {
                int index = 0;

                @Override
                public void run() {
                    movingAgent.setAzimuth(positions[index][0]);
                    movingAgent.setElevation(positions[index][1]);
                    display.repaint();
                    index = (index + 1) % positions.length;  // Cycle through positions
                }
            }, 0, 3000);  // Move every 3 seconds
        });
    }
}
