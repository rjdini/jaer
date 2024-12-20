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

import com.inilabs.jaer.projects.review.tests.LogReviewTestPanel;
import java.io.IOException;
import javax.swing.*;
import java.util.Map;

public class Main {
    
    public static void main(String[] args) {
    try {
        LogParser parser = new LogParser();
        Map<String, Map<String, TrajectoryDrawable>> sessions = parser.parseLogFile("logs/AgentLogger.json");

        // Example: Print session details
        sessions.forEach((session, trackers) -> {
            System.out.println("Session: " + session);
            trackers.forEach((trackerName, trajectory) -> {
                System.out.println("  Tracker: " + trackerName);
                System.out.println("    Points: " + trajectory.getPoints());
                System.out.println("    Clusters: " + trajectory.getClusters());
            });
        });
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
