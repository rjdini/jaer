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

<<<<<<< HEAD

=======
>>>>>>> working
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
<<<<<<< HEAD
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
=======
            // Initialize the main PolarSpace GUI window
            PolarSpaceGUI polarSpaceGUI = new PolarSpaceGUI();

            // Get the display after GUI construction
            PolarSpaceDisplay display = polarSpaceGUI.getPolarSpaceDisplay();

            // Add test drawables to the PolarSpaceDisplay
            AgentDrawable drawable1 = new AgentDrawable();
            drawable1.setAzimuth(15);
            drawable1.setElevation(-10);
            drawable1.setSize(5);
            drawable1.setColor(Color.RED);

            AgentDrawable drawable2 = new AgentDrawable();
            drawable2.setAzimuth(-20);
            drawable2.setElevation(5);
            drawable2.setSize(8);
            drawable2.setColor(Color.BLUE);

            AgentDrawable drawable3 = new AgentDrawable();
            drawable3.setAzimuth(30);
            drawable3.setElevation(15);
            drawable3.setSize(6);
            drawable3.setColor(Color.GREEN);

            // Add the drawables to the display
            display.addDrawable(drawable1);
            display.addDrawable(drawable2);
            display.addDrawable(drawable3);

            // Optionally set up initial control panel settings if needed
            PolarSpaceControlPanel controlPanel = new PolarSpaceControlPanel(display, e -> polarSpaceGUI.dispose());
            polarSpaceGUI.add(controlPanel, BorderLayout.SOUTH);
            controlPanel.setHeading(10, -5);     
     //       controlPanel.setAzimuthRange(40);
      //      controlPanel.setElevationRange(30);
>>>>>>> working
        });
    }
}

