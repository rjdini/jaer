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

package com.inilabs.jaer.projects.review;

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Trajectory Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);

            // Create the PolarSpaceDisplay
            PolarSpaceDisplay display = new PolarSpaceDisplay();
            TrajectoryManager manager = new TrajectoryManager(display);
            LogVisualizerPanel logVisualizerPanel = new LogVisualizerPanel(display, manager);

            // Setup layout
            frame.setLayout(new BorderLayout());
            frame.add(logVisualizerPanel, BorderLayout.WEST);
            frame.add(display, BorderLayout.CENTER);
            frame.setVisible(true);

            // Load test data
            String testFilePath = "./data/AgentLogger_TEST.json"; // Adjust to your test file location
            try {
                LogParser parser = new LogParser();
                Map<String, Map<String, List<TrajectoryPointDrawable>>> sessions = parser.parseLogFile(testFilePath);

                // Load sessions into the visualization panel
                logVisualizerPanel.loadSessions(sessions);
                System.out.println("Test data loaded successfully.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Failed to load test data: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
