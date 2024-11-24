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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LogParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a newline-delimited JSON log file into sessions.
     *
     * @param filePath The path to the JSON log file.
     * @return A map of sessions, each containing a map of tracker names to their trajectories.
     * @throws IOException If the file cannot be read or parsed.
     */
    public Map<String, Map<String, TrajectoryDrawable>> parseLogFile(String filePath) throws IOException {
        Map<String, Map<String, TrajectoryDrawable>> sessions = new LinkedHashMap<>();

        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    try {
                        JsonNode node = objectMapper.readTree(line);

                        String eventType = node.has("event") ? node.get("event").asText() : null;
                        if (eventType == null || !"move".equals(eventType)) {
                            System.err.println("Skipping invalid or unsupported event: " + node.toString());
                            continue;
                        }

                        String session = node.has("session") ? node.get("session").asText() : "default";
                        String trackerName = node.has("key") ? node.get("key").asText() : null;

                        if (trackerName == null) {
                            System.err.println("Skipping record with missing tracker name: " + node.toString());
                            continue;
                        }

                        float azim = node.has("azim") ? node.get("azim").floatValue() : 0.0f;
                        float elev = node.has("elev") ? node.get("elev").floatValue() : 0.0f;
                        long time = node.has("jaerts") ? node.get("jaerts").asLong() : 0L;

                        TrajectoryPointDrawable point = new TrajectoryPointDrawable(azim, elev, time);

                        sessions.putIfAbsent(session, new LinkedHashMap<>());
                        sessions.get(session).putIfAbsent(trackerName, new TrajectoryDrawable(trackerName));
                        sessions.get(session).get(trackerName).addPoint(point);
                    } catch (Exception e) {
                        System.err.println("Failed to parse record: " + line);
                        e.printStackTrace();
                    }
                }
            }
        }

        return sessions;
    }
}
