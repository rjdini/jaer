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

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.review.LogParser;
import com.inilabs.jaer.projects.review.LogVisualizerPanel;
import com.inilabs.jaer.projects.review.TrajectoryManager;
import com.inilabs.jaer.projects.review.TrajectoryPointDrawable;
import java.awt.BorderLayout;
import javax.swing.*;
import java.util.List;
import java.util.Map;

public class LogVisualizerTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Log Visualizer Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);

            PolarSpaceDisplay display = PolarSpaceDisplay.getInstance();
            TrajectoryManager manager = new TrajectoryManager(display);
            LogVisualizerPanel panel = new LogVisualizerPanel(display, manager);

            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.WEST);
            frame.add(display, BorderLayout.CENTER);
            frame.setVisible(true);

            try {
                LogParser parser = new LogParser();
          //      Map<String, Map<String, List<TrajectoryPointDrawable>>> sessions =
           //            parser.parseLogFile("./data/AgentLogger_TEST.json");
         //       panel.loadSessions(sessions);
        //        System.out.println("LogVisualizerTest: Sessions loaded successfully.");
            } catch (Exception e) {
         
                JOptionPane.showMessageDialog(frame, "Failed to load log data: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

