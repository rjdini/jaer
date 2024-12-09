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
package com.inilabs.jaer.projects.environ.tests;

import com.inilabs.jaer.projects.environ.WaypointManager;
import com.inilabs.jaer.projects.environ.WaypointGUI;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;

import javax.swing.*;
import java.awt.*;

public class WaypointSystemTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Get singleton instances
            PolarSpaceDisplay display = PolarSpaceDisplay.getInstance();
            WaypointManager manager = WaypointManager.getInstance();

            // Create the main frame
            JFrame frame = new JFrame("Waypoint System Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Set up the Waypoint GUI
            WaypointGUI waypointGUI = new WaypointGUI();
            manager.setWaypointGUI(waypointGUI);

            // Add components to the frame
            frame.add(display, BorderLayout.CENTER); // Display panel for visualizing waypoints
            frame.add(waypointGUI, BorderLayout.EAST); // Waypoint GUI for controls

            // Set frame size and make it visible
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null); // Center the frame on the screen
            frame.setVisible(true);
        });
    }
}
