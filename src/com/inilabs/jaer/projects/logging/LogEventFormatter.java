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
package com.inilabs.jaer.projects.logging;

import java.util.List;
import java.util.stream.Collectors;

public class LogEventFormatter {

    /**
     * Formats agent-specific log events as JSON.
     *
     * @param eventType The type of event to log (e.g., EventType.RUN, EventType.CREATION).
     * @param timestamp The timestamp of the event in milliseconds.
     * @param key       The unique identifier for the agent.
     * @param azimuth   The azimuth value of the agent.
     * @param elevation The elevation value of the agent.
     * @param clusters  A list of cluster keys associated with the agent.
     * @return A JSON-formatted log string.
     */
    public static String formatAgentLogEvent(EventType eventType, long timestamp, String key, float azimuth, float elevation, List<String> clusters) {
        String clustersList = clusters.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return String.format(
            "{\"jaerts\": \"%d\", \"event\": \"%s\", \"key\": \"%s\", \"azim\": %.2f, \"elev\": %.2f, \"clust\": [%s]}",
            timestamp, eventType.name().toLowerCase(), key, azimuth, elevation, clustersList
        );
    }

    /**
     * Formats a general system log event as JSON.
     *
     * @param eventType The type of system event (e.g., LOGGER_START, LOGGER_CLOSE).
     * @param datetime  The ISO 24-hour format datetime string.
     * @param timestamp The timestamp of the event in milliseconds.
     * @param message   A message describing the system event.
     * @return A JSON-formatted log string.
     */
    public static String formatSystemLogEvent(EventType eventType, String datetime, long timestamp, String message) {
        return String.format(
            "{\"jaerts\": \"%d\", \"datetime\": \"%s\", \"event\": \"%s\", \"message\": \"%s\"}",
            timestamp, datetime, eventType.name().toLowerCase(), message
        );
    }
    
    public static String formatJAERLogEvent(EventType eventType, String datetime, long timestamp, int sessionNumber, String filename) {
        return String.format(
            "{\"jaerts\": \"%d\", \"datetime\": \"%s\", \"event\": \"%s\",\"session\": \"%d\", \"filename\": \"%s\"}",
            timestamp, datetime, eventType.name().toLowerCase(), sessionNumber, filename
        );
    }
    
    public static String formatGUILogEvent(EventType eventType, String datetime, long timestamp, int sessionNumber, String filename) {
        return String.format(
            "{\"jaerts\": \"%d\", \"datetime\": \"%s\", \"event\": \"%s\", \"session\": \"%d\", \"message\": \"%s\"}",
            timestamp, datetime, eventType.name().toLowerCase(), sessionNumber, filename
        );
    }
    
    
}

