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
package com.inilabs.jaer.projects.environ;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.utils.ColorAdapter;
import com.inilabs.jaer.projects.utils.NoSuperclassExclusionStrategy;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

public class WaypointManager {

    private static final ch.qos.logback.classic.Logger log
            = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(WaypointManager.class);

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorAdapter())
            .setPrettyPrinting()
            .addSerializationExclusionStrategy(new NoSuperclassExclusionStrategy(WaypointDrawable.class))
            .addDeserializationExclusionStrategy(new NoSuperclassExclusionStrategy(WaypointDrawable.class))
            .create();

    private final Map<String, WaypointDrawable> waypoints = new HashMap<>();
    private final PolarSpaceDisplay display;
    private static WaypointManager instance;
    private WaypointGUI waypointGUI;

    // Private constructor to enforce singleton
    private WaypointManager() {
        this.display = PolarSpaceDisplay.getInstance();
        this.display.setWaypointAdder(this::addWaypointAtAzimuthElevation); // Register waypoint adder
        registerMouseClickListener();

        this.display.setWaypointEditor(waypoint -> {
            if (waypointGUI != null) {
                waypointGUI.populateFields(waypoint);
            }
        });

        this.display.setWaypointRemover(waypoint -> {
            removeWaypoint(waypoint.getKey());
            JOptionPane.showMessageDialog(display, "Waypoint removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        // Load default waypoints if the file exists
        try {
            File defaultFile = new File("conf/default_waypoints.json");
            if (defaultFile.exists()) {
                loadWaypointsFromFile(defaultFile.getAbsolutePath());
                log.info("Default waypoints loaded successfully.");
            } else {
                log.info("Default waypoints file not found.");
            }
        } catch (IOException e) {
            log.error("Error loading default waypoints: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    // Get the singleton instance
    public static synchronized WaypointManager getInstance() {
        if (instance == null) {
            instance = new WaypointManager();
            instance.showGUI(true);
        }
        return instance;
    }

    private void addWaypointAtAzimuthElevation(float azimuth, float elevation) {
        String name = JOptionPane.showInputDialog(display, "Enter waypoint name:");
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(display, "Waypoint name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Color color = JColorChooser.showDialog(display, "Select Waypoint Color", Color.WHITE);
        if (color == null) {
            color = Color.WHITE;
        }

        WaypointDrawable waypoint = new WaypointDrawable(name, azimuth, elevation, color);
        addWaypoint(waypoint);

        JOptionPane.showMessageDialog(display, "Waypoint added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public synchronized void addWaypoint(WaypointDrawable waypoint) {
        waypoints.put(waypoint.getKey(), waypoint);
        if (display != null) {
            display.addDrawable(waypoint); // Add to display
            display.refresh(); // Trigger a repaint to show the waypoint
        }
    }

    
    
    public synchronized void updateWaypoint(WaypointDrawable waypoint) {
        waypoints.put(waypoint.getKey(), waypoint);
        display.removeDrawable(waypoint.getKey());
        display.addDrawable(waypoint);
    }

    public synchronized void removeWaypoint(String key) {
        WaypointDrawable removed = waypoints.remove(key);
        if (removed != null) {
            display.removeDrawable(key);
        }
    }

    private void registerMouseClickListener() {
        display.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                float azimuth = (e.getX() - display.getCenterX()) / display.getAzimuthScale();
                float elevation = (display.getCenterY() - e.getY()) / display.getElevationScale();

                WaypointDrawable closestWaypoint = findClosestWaypoint(azimuth, elevation, 5.0f); // Tolerance of 5 degrees
                if (closestWaypoint != null) {
                    if (waypointGUI != null) {
                        waypointGUI.populateFields(closestWaypoint); // Notify GUI of the selected waypoint
                    }
                } else {
                    JOptionPane.showMessageDialog(display, "No waypoint found at this location.", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    public WaypointDrawable findClosestWaypoint(float azimuth, float elevation, float tolerance) {
        WaypointDrawable closestWaypoint = null;
        float minDistance = Float.MAX_VALUE;

        for (WaypointDrawable waypoint : waypoints.values()) {
            float distance = (float) Math.sqrt(
                    Math.pow(waypoint.getAzimuth() - azimuth, 2)
                    + Math.pow(waypoint.getElevation() - elevation, 2));

            if (distance < minDistance && distance <= tolerance) {
                minDistance = distance;
                closestWaypoint = waypoint;
            }
        }
        return closestWaypoint;
    }

    public synchronized void removeWaypointByKey(String key) {
    waypoints.remove(key);
}
    
    
    public synchronized boolean removeWaypointByName(String name) {
        WaypointDrawable waypoint = waypoints.values()
                .stream()
                .filter(wp -> wp.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (waypoint != null) {
            waypoints.remove(waypoint.getKey());
            if (display != null) {
                display.removeDrawable(waypoint.getKey());
                display.refresh();
            }
            return true; // Indicate successful removal
        }
        return false; // Waypoint not found
    }
    

    public synchronized WaypointDrawable getWaypointByKey(String key) {
        return waypoints.get(key);
    }

    public synchronized WaypointDrawable getWaypointByName(String name) {
        return waypoints.get("Waypoint_" + name);
    }

    public synchronized WaypointDrawable getNextWaypoint() {
    if (waypoints.isEmpty()) {
        return null;
    }
    // ***********  temp return first WP. TODO Manger should decide best next WP from its list.
    return waypoints.values().iterator().next(); // Get the first value
}
   
    public synchronized Map<String, WaypointDrawable> getAllWaypoints() {
    return Collections.unmodifiableMap(waypoints);
}
    
 public synchronized List<String> getAllWaypointKeys() {
        return List.copyOf(waypoints.keySet());
    }

    public synchronized List<String> getAllWaypointNames() {
        return waypoints.values()
                .stream()
                .map(WaypointDrawable::getName)
                .collect(Collectors.toUnmodifiableList());
    }    
    
  
    public void saveWaypointsToFile(File filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(waypoints.values(), writer);
        }
    }

    public void loadWaypointsFromFile(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            Type waypointListType = new TypeToken<List<WaypointDrawable>>() {
            }.getType();
            List<WaypointDrawable> loadedWaypoints = gson.fromJson(reader, waypointListType);

            // Clear existing waypoints and add loaded ones
            waypoints.clear();
            for (WaypointDrawable waypoint : loadedWaypoints) {
                addWaypoint(waypoint);
            }
            display.refresh(); // Update the display
        }
    }

    public void showGUI(boolean visible) {
        if (waypointGUI == null) {
            waypointGUI = new WaypointGUI();
        }
        // SwingUtilities.invokeLater(() -> waypointGUI.setVisible(visible));
    }

    public void setWaypointGUI(WaypointGUI waypointGUI) {
        this.waypointGUI = waypointGUI;
    }
}
