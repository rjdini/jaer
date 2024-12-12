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

import com.inilabs.jaer.projects.review.LogParser;
import com.inilabs.jaer.projects.review.TrajectoryDrawable;
import com.inilabs.jaer.projects.review.TrajectoryPointDrawable;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class LogParserMainTest {

    public static void main(String[] args) {
        try {
            // Ensure the logs/ directory exists in the classpath
            Path logsDir = Path.of("logs");
            File logsDirectory = logsDir.toFile();
            if (!logsDirectory.exists()) {
                if (!logsDirectory.mkdirs()) {
                    System.err.println("Failed to create logs directory.");
                    return;
                }
            }

            // Create the test log file in the logs/ directory
            File logFile = new File(logsDirectory, "test.log");
            try (FileWriter writer = new FileWriter(logFile)) {
                // Write multiple JSON records separated by linefeeds
                String sampleLogs = """
                    {"jaerts": "6475601", "event": "move", "key": "EventCluster_9", "azim": 10.42, "elev": -25.16, "col": -10944257, "clust": ["RCTCluster_34"]}
                    {"jaerts": "6475601", "event": "move", "key": "TrackerAgentDrawable_6", "azim": 10.42, "elev": -25.16, "col": -65536, "clust": ["EventCluster_9"]}
                    {"jaerts": "6475601", "event": "move", "key": "fov_instance", "azim": 10.55, "elev": -24.26, "col": -65536, "clust": []}
                    """;
                writer.write(sampleLogs);
            }

            // Parse the log file
            LogParser parser = new LogParser();
            Map<String, Map<String, TrajectoryDrawable>> sessions = parser.parseLogFile(logFile.getAbsolutePath());

            // Validate tracker data for "EventCluster_9"
            Map<String, TrajectoryDrawable> session = sessions.get("default");
            TrajectoryDrawable tracker = session.get("EventCluster_9");
            validateTracker(tracker, 10.42f, -25.16f, 6475601L, new Color(-10944257), List.of("RCTCluster_34"));

            System.out.println("Test passed: All values are correct.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Test failed: An exception occurred.");
        }
    }

    private static void validateTracker(TrajectoryDrawable tracker, float expectedAzim, float expectedElev, long expectedTimestamp, Color expectedColor, List<String> expectedClusters) {
        if (tracker == null || tracker.getPoints().size() != 1) {
            System.err.println("Test failed: Incorrect number of trajectory points.");
            return;
        }

        TrajectoryPointDrawable point = tracker.getPoints().get(0);
        if (point == null) {
            System.err.println("Test failed: Trajectory point is null.");
            return;
        }

        if (Math.abs(point.getAzimuth() - expectedAzim) > 0.001) {
            System.err.println("Test failed: Incorrect azimuth.");
            return;
        }
        if (Math.abs(point.getElevation() - expectedElev) > 0.001) {
            System.err.println("Test failed: Incorrect elevation.");
            return;
        }
        if (point.getTimestamp() != expectedTimestamp) {
            System.err.println("Test failed: Incorrect timestamp.");
            return;
        }
        if (!point.getColor().equals(expectedColor)) {
            System.err.println("Test failed: Incorrect color.");
            return;
        }

        List<String> clusters = tracker.getClusters();
        if (clusters == null || !clusters.equals(expectedClusters)) {
            System.err.println("Test failed: Incorrect clusters.");
            return;
        }
    }
}