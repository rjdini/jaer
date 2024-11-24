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
import java.util.List;
import java.util.Map;
import com.inilabs.jaer.projects.review.TrajectoryPointDrawable;

public class LogParserTest {
    public static void main(String[] args) {
        try {
            LogParser parser = new LogParser();
            String filePath = "./data/AgentLogger_TEST.json";

            Map<String, Map<String, List<TrajectoryPointDrawable>>> sessions = parser.parseLogFile(filePath);

            System.out.println("Parsed sessions:");
            for (String session : sessions.keySet()) {
                System.out.println("Session: " + session);
                Map<String, List<TrajectoryPointDrawable>> trackers = sessions.get(session);

                for (String tracker : trackers.keySet()) {
                    System.out.println("  Tracker: " + tracker);
                    List<TrajectoryPointDrawable> points = trackers.get(tracker);

                    for (TrajectoryPointDrawable point : points) {
                        System.out.println("    " + point);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

