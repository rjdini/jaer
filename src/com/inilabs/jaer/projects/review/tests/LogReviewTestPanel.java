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
package com.inilabs.jaer.projects.review.tests;

import com.inilabs.jaer.projects.gui.BasicTestPanel;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.review.LogParser;
import com.inilabs.jaer.projects.review.LogVisualizerPanel;
import com.inilabs.jaer.projects.review.TrajectoryManager;
import com.inilabs.jaer.projects.review.TrajectoryPointDrawable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class LogReviewTestPanel extends BasicTestPanel {
    private LogVisualizerPanel logVisualizerPanel;

    public LogReviewTestPanel(PolarSpaceDisplay display) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 600)); // Ensure broader width
        // Initialize LogVisualizerPanel
        TrajectoryManager manager = new TrajectoryManager(display);
        logVisualizerPanel = new LogVisualizerPanel(display, manager);
        add(new JScrollPane(logVisualizerPanel), BorderLayout.CENTER);

        // Add Load Button
        JButton loadButton = new JButton("Load Log File");
        loadButton.addActionListener(e -> loadLogFile());
        add(loadButton, BorderLayout.SOUTH);
    }

    private void loadLogFile() {
        JFileChooser fileChooser = new JFileChooser("./data");
        fileChooser.setDialogTitle("Select Log File");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                LogParser parser = new LogParser();
                Map<String, Map<String, List<TrajectoryPointDrawable>>> sessions = parser.parseLogFile(filePath);
                logVisualizerPanel.loadSessions(sessions);
                System.out.println("Log data loaded successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load log data: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}

