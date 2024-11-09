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


/**
 *
 * @author rjd
 */

package com.inilabs.jaer.projects.gui;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the main frame
            JFrame frame = new JFrame("Drawable Agent Test");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Create the display panel and agent
            PolarSpaceDisplay display = new PolarSpaceDisplay();
            display.setAzimuthRange(50);    // Set the range for testing
            display.setElevationRange(50);  // Set the range for testing

            // Create and configure the AgentDrawable at (azimuth, elevation) = (+10, -20)
            AgentDrawable agent = new AgentDrawable("TestAgent");
            agent.setAzimuth(10);     // +10 degrees azimuth
            agent.setElevation(-20);  // -20 degrees elevation
            agent.setSize(5);         // 5 degrees size for the test circle
            agent.setColor(Color.RED);

            // Add the agent to the display's listeners to receive scaling updates
            display.addScalingListener(agent);

            // Override paintComponent to include our agent's drawing
            display.addDrawable(agent);  // Add agent to be drawn in the display

            // Add the display panel to the frame
            frame.add(display, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}

