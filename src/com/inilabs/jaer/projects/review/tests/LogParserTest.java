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

import java.util.List;
import java.util.Map;

public class LogParserTest {
    public static void main(String[] args) {
        String filePath = "./data/AgentLogger_TEST.json"; // Update to your file location if needed

        try {
            LogParser parser = new LogParser();
            Map<String, Map<String, TrajectoryDrawable>> sessions = parser.parseLogFile(filePath);

            // Display parsed sessions and their trajectories
            for (String session : sessions.keySet()) {
                System.out.println("Session: " + session);
                Map<String, TrajectoryDrawable> trackers = sessions.get(session);

                for (String tracker : trackers.keySet()) {
                    System.out.println("  Tracker: " + tracker);
                    List<TrajectoryPointDrawable> points = trackers.get(tracker).getPoints();

                    for (TrajectoryPointDrawable point : points) {
                        System.out.println("    Point: Azimuth=" + point.getAzimuth() +
                                ", Elevation=" + point.getElevation() +
                                ", Time=" + point.getTime());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
