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

    public Map<String, Map<String, List<TrajectoryPointDrawable>>> parseLogFile(String filePath) throws IOException {
        Map<String, Map<String, List<TrajectoryPointDrawable>>> sessions = new LinkedHashMap<>();

        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    JsonNode node = objectMapper.readTree(line);

                    String eventType = node.get("event").asText();
                    String session = node.has("session") ? node.get("session").asText() : "default";

                    sessions.putIfAbsent(session, new LinkedHashMap<>());

                    if ("run".equals(eventType)) {
                        String key = node.get("key").asText();
                        float azim =  node.get("azim").floatValue();
                        float elev = node.get("elev").floatValue();
                        long timestamp = node.has("jaerts") && !node.get("jaerts").isNull() ? node.get("jaerts").asLong() : 0L;
                     
                        TrajectoryPointDrawable point = new TrajectoryPointDrawable(azim, elev, timestamp);
                        sessions.get(session).putIfAbsent(key, new ArrayList<>());
                        sessions.get(session).get(key).add(point);
                    }
                }
            }
        }

        return sessions;
    }
}
