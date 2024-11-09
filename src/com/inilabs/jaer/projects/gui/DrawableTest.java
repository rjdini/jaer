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
import java.util.concurrent.TimeUnit;

public class DrawableTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize the PolarSpace GUI and display
            PolarSpaceGUI gui = new PolarSpaceGUI();
            PolarSpaceDisplay display = gui.getPolarSpaceDisplay();

            // Test 1: Adding a drawable at specified position
            AgentDrawable agent1 = new AgentDrawable("Agent1");
            agent1.setAzimuth(10);   // 10 degrees azimuth
            agent1.setElevation(-20); // -20 degrees elevation
            agent1.setSize(5);       // Size in degrees
            agent1.setColor(Color.RED);
            display.addDrawable(agent1);

            // Test 2: Adding multiple drawables with different positions and sizes
            AgentDrawable agent2 = new AgentDrawable("Agent2");
            agent2.setAzimuth(30);    // 30 degrees azimuth
            agent2.setElevation(10);  // 10 degrees elevation
            agent2.setSize(8);        // Larger size in degrees
            agent2.setColor(Color.BLUE);
            display.addDrawable(agent2);

            AgentDrawable agent3 = new AgentDrawable("Agent3");
            agent3.setAzimuth(-15);   // -15 degrees azimuth
            agent3.setElevation(25);  // 25 degrees elevation
            agent3.setSize(6);        // Size in degrees
            agent3.setColor(Color.GREEN);
            display.addDrawable(agent3);

            // Test 3: Simulate autonomous removal of a drawable
            new Timer(5000, e -> {
                System.out.println("Removing Agent2 autonomously");
                agent2.remove();  // Agent2 requests self-removal after 5 seconds
                display.repaint();
            }).start();

            // Test 4: Update scaling and observe drawables adjust in real-time
            new Timer(7000, e -> {
                System.out.println("Updating display ranges for scaling test");
                display.setAzimuthRange(40);  // Narrower range to test scaling
                display.setElevationRange(40);
                display.repaint();
            }).start();

            // Test 5: Restore range after additional delay
            new Timer(10000, e -> {
                System.out.println("Restoring display ranges");
                display.setAzimuthRange(90);  // Original range
                display.setElevationRange(90);
                display.repaint();
            }).start();
        });
    }
}
