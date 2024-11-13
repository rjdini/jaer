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
     * Formats a log message based on the event type and provided parameters.
     *
     * @param eventType The type of event to log (e.g., "run", "creation", "close").
     * @param timestamp The timestamp of the event.
     * @param key The unique identifier for the agent.
     * @param azimuth The azimuth value of the agent.
     * @param elevation The elevation value of the agent.
     * @param clusters A list of cluster keys associated with the agent.
     * @return A JSON-formatted log string tailored to the event type.
     */
    public static String createLog(String eventType, long timestamp, String key, float azimuth, float elevation, List<String> clusters) {
        String clustersList = clusters.stream().collect(Collectors.joining(", "));

        switch (eventType.toLowerCase()) {
            case "run":
                return String.format(
                    "{\"time\": \"%d\", \"event_type\": \"%s\", \"key\": \"%s\", \"azimuth\": %.2f, \"elevation\": %.2f, \"clusters\": [%s]}",
                    timestamp, eventType, key, azimuth, elevation, clustersList
                );
                
            case "creation":
                return String.format(
                    "{\"time\": \"%d\", \"event_type\": \"%s\", \"key\": \"%s\", \"azimuth\": %.2f, \"elevation\": %.2f, \"clusters\": [%s]}",
                    timestamp, eventType, key, azimuth, elevation, clustersList
                );

            case "close":
                return String.format(
                    "{\"time\": \"%d\", \"event_type\": \"%s\", \"key\": \"%s\", \"azimuth\": %.2f, \"elevation\": %.2f, \"clusters\": [%s]}",
                    timestamp, eventType, key, azimuth, elevation, clustersList
                );

            default:
                // Default format if the eventType is not specifically handled
                return String.format(
                    "{\"time\": \"%d\", \"event_type\": \"%s\", \"key\": \"%s\", \"azimuth\": %.2f, \"elevation\": %.2f, \"clusters\": [%s]}",
                    timestamp, eventType, key, azimuth, elevation, clustersList
                );
        }
    }
}
