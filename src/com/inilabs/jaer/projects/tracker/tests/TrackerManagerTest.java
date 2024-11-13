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

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.tracker.TrackerManager;

import javax.swing.*;
import java.awt.*;

public class TrackerManagerTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
                       
            // Initialize PolarSpaceGUI and TrackerManager
            PolarSpaceGUI polarSpaceGUI = new PolarSpaceGUI();
            TrackerManager trackerManager = new TrackerManager(polarSpaceGUI.getPolarSpaceDisplay());

            // Create the control panel for TrackerManager
            TrackerManagerTestPanel testPanel = new TrackerManagerTestPanel(trackerManager);

            // Set up the main testing frame
            JFrame testFrame = new JFrame("Tracker Manager Test");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setLayout(new BorderLayout());

            // Add PolarSpaceGUI in the CENTER and TrackerManagerTestPanel in the EAST
            testFrame.add(polarSpaceGUI.getContentPane(), BorderLayout.CENTER); // Embed PolarSpaceGUI's content pane
            testFrame.add(testPanel, BorderLayout.EAST);                         // Control panel on the right

            // Set frame size and make it visible
            testFrame.setSize(1200, 800);
            testFrame.setVisible(true);

            // Timer to update TrackerManager periodically
            Timer updateTimer = new Timer(100, e -> trackerManager.updateAgents());
            updateTimer.start();
        });
    }
}

