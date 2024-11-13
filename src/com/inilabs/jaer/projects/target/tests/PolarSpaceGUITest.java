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


package com.inilabs.jaer.projects.target.tests;

import javax.swing.*;
import java.awt.*;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.tracker.TrackerManager;

import javax.swing.*;
import java.awt.*;

public class PolarSpaceGUITest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the main PolarSpaceDisplay and TrackerManager
         //   PolarSpaceDisplay display = new PolarSpaceDisplay();
          //  TrackerManager targetManager = new TrackerManager(display);

            // Initialize PolarSpaceGUI as its own window
            PolarSpaceGUI gui = new PolarSpaceGUI();
           gui.setVisible(false);  // Show PolarSpaceGUI window
            
             TrackerManager targetManager = new TrackerManager(gui.getPolarSpaceDisplay());

            // Create the test panel for additional controls with TrackerManager
           TestPanel testPanel = new TestPanel(gui.getPolarSpaceDisplay());

            // Set up the main testing frame for additional controls on the EAST
            JFrame testFrame = new JFrame("Polar Space GUI Test");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setLayout(new BorderLayout());

            // Add PolarSpaceGUI in the CENTER and TestPanel in the EAST
            testFrame.add(gui.getContentPane(), BorderLayout.CENTER); // Embed PolarSpaceGUI's content pane
            testFrame.add(testPanel, BorderLayout.EAST);              // Test controls on the right

            // Set frame size and make it visible
            testFrame.setSize(1200, 800);
            testFrame.setVisible(true);
        });
    }
}