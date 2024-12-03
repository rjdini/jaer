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
package com.inilabs.jaer.projects.environ;

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.BorderLayout;
import javax.swing.JFrame;

public class Main {
   
        
        public static void main(String[] args) {
        // Create a PolarSpaceDisplay
        PolarSpaceDisplay display = new PolarSpaceDisplay();

        // Create and attach a WaypointManager to the display
        WaypointManager manager = WaypointManager.getInstance(display);

        // Create a JFrame to host the display
        JFrame frame = new JFrame("Waypoint System Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(display, BorderLayout.CENTER);

        // Add a WaypointGUI if necessary
        manager.showGUI(true);

        frame.setSize(1000, 800);
        frame.setVisible(true);
        }
}

