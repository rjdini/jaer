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
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LogParserTest {

    @Test
    public void testParseLogFileWithColor() throws IOException {
        // Create a sample log file
        String sampleLog = """
            {
                "event": "move",
                "session": "testSession",
                "key": "tracker1",
                "azim": 45.0,
                "elev": 30.0,
                "jaerts": 123456789,
                "color": -16776961,
                "clust": ["cluster1", "cluster2"]
            }
            """;

        // Write the sample log to a temporary file
        Path tempLogFile = Files.createTempFile("testLog", ".json");
        try (FileWriter writer = new FileWriter(tempLogFile.toFile())) {
            writer.write(sampleLog);
        }

        // Parse the log file
        LogParser parser = new LogParser();
        Map<String, Map<String, TrajectoryDrawable>> sessions = parser.parseLogFile(tempLogFile.toString());

        // Validate the parsed data
        assertNotNull(sessions, "Sessions should not be null");
        assertTrue(sessions.containsKey("testSession"), "Session 'testSession' should exist");

        Map<String, TrajectoryDrawable> session = sessions.get("testSession");
        assertNotNull(session, "Session data should not be null");
        assertTrue(session.containsKey("tracker1"), "Tracker 'tracker1' should exist");

        TrajectoryDrawable tracker = session.get("tracker1");
        assertNotNull(tracker, "Tracker should not be null");
        assertEquals(1, tracker.getPoints().size(), "Tracker should have exactly one point");

        TrajectoryPointDrawable point = tracker.getPoints().get(0);
        assertNotNull(point, "Trajectory point should not be null");
        assertEquals(45.0f, point.getAzimuth(), 0.001, "Azimuth should match");
        assertEquals(30.0f, point.getElevation(), 0.001, "Elevation should match");
        assertEquals(123456789L, point.getTimestamp(), "Timestamp should match");
        assertEquals(new Color(-16776961), point.getColor(), "Color should match");

        List<String> clusters = tracker.getClusters();
        assertNotNull(clusters, "Clusters should not be null");
        assertEquals(List.of("cluster1", "cluster2"), clusters, "Clusters should match");

        // Clean up temporary file
        Files.delete(tempLogFile);
    }
}
