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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.inilabs.jaer.projects.review.TrajectoryPointDrawable;

public class LogParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a newline-delimited JSON log file into a structured map.
     *
     * @param filePath The path to the JSON log file.
     * @return A map containing sessions and their associated tracker data.
     * @throws IOException If the file cannot be read or parsed.
     */
    public Map<String, Map<String, List<TrajectoryPointDrawable>>> parseLogFile(String filePath) throws IOException {
        Map<String, Map<String, List<TrajectoryPointDrawable>>> sessions = new LinkedHashMap<>();
        List<JsonNode> events = new ArrayList<>();

        // Read the file line by line
        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    JsonNode node = objectMapper.readTree(line);
                    events.add(node);
                }
            }
        }

        // Process each event
        for (JsonNode event : events) {
            String eventType = event.get("event").asText();
            String session = event.has("session") ? event.get("session").asText() : "default";

            sessions.putIfAbsent(session, new LinkedHashMap<>());

            if ("run".equals(eventType)) {
                String key = event.get("key").asText();
                float azim = event.get("azim").floatValue();
                float elev = event.get("elev").floatValue();
                float time = event.get("time").floatValue();
                TrajectoryPointDrawable point = new TrajectoryPointDrawable(azim, elev, time);

                sessions.get(session).putIfAbsent(key, new ArrayList<>());
                sessions.get(session).get(key).add(point);
            }
        }

        return sessions;
    }
}
