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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LogParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a newline-delimited JSON log file into sessions.
     *
     * @param filePath The path to the JSON log file.
     * @return A map of sessions, each containing a map of tracker names to their trajectories and relationships.
     * @throws IOException If the file cannot be read or parsed.
     */
    public Map<String, Map<String, TrajectoryDrawable>> parseLogFile(String filePath) throws IOException {
        Map<String, Map<String, TrajectoryDrawable>> sessions = new LinkedHashMap<>();

        File file = new File(filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Preprocess to ensure JSON compliance
                line = preprocessLine(line);

                if (!line.isEmpty()) {
                    try {
                        JsonNode node = objectMapper.readTree(line);

                        String eventType = node.has("event") ? node.get("event").asText() : null;

                        // Skip unsupported events
                        if (eventType == null || !eventType.equals("move")) {
                            System.err.println("Skipping invalid or unsupported event: " + node.toString());
                            continue;
                        }

                        String session = node.has("session") ? node.get("session").asText() : "default";
                        String trackerName = node.has("key") ? node.get("key").asText() : null;

                        if (trackerName == null || trackerName.equals("null")) {
                            System.err.println("Skipping record with missing tracker name: " + node.toString());
                            continue;
                        }

                        float azim = node.has("azim") ? node.get("azim").floatValue() : 0.0f;
                        float elev = node.has("elev") ? node.get("elev").floatValue() : 0.0f;
                        long time = node.has("jaerts") ? node.get("jaerts").asLong() : 0L;

                        // Process clusters if present
                        List<String> clusters = new ArrayList<>();
                        if (node.has("clust") && node.get("clust").isArray()) {
                            for (JsonNode clusterNode : node.get("clust")) {
                                if (!clusterNode.isNull()) {
                                    clusters.add(clusterNode.asText());
                                }
                            }
                        }

                        // Create trajectory point
                        TrajectoryPointDrawable point = new TrajectoryPointDrawable(azim, elev, time);

                        // Ensure session exists
                        sessions.putIfAbsent(session, new LinkedHashMap<>());

                        // Ensure tracker exists
                        sessions.get(session).putIfAbsent(trackerName, new TrajectoryDrawable(trackerName));

                        // Add point to tracker
                        TrajectoryDrawable tracker = sessions.get(session).get(trackerName);
                        tracker.addPoint(point);

                        // Link clusters
                        tracker.addClusters(clusters);
                    } catch (Exception e) {
                        System.err.println("Failed to parse record: " + line);
                        e.printStackTrace();
                    }
                }
            }
        }

        return sessions;
    }

    /**
     * Preprocesses a log line to ensure JSON compliance.
     *
     * @param line The raw log line.
     * @return The preprocessed log line.
     */
    private String preprocessLine(String line) {
    // Pattern to match the clust field
    Pattern pattern = Pattern.compile("clust\": \\[(.*?)\\]");
    Matcher matcher = pattern.matcher(line);

    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
        String content = matcher.group(1).trim();
        if (content.isEmpty()) {
            matcher.appendReplacement(result, "clust\": []");
        } else {
            String[] items = content.split(",");
            String quotedItems = Arrays.stream(items)
                    .map(String::trim)
                    .map(item -> "\"" + item + "\"") // Wrap items in quotes
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            matcher.appendReplacement(result, "clust\": [" + quotedItems + "]");
        }
    }
    matcher.appendTail(result);

    return result.toString();
}

}