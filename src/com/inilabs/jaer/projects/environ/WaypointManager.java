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

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class WaypointManager {

    private final Map<String, WaypointDrawable> waypoints = new HashMap<>();
    private final PolarSpaceDisplay display;
    private  Gson gson = new Gson();
    private static WaypointManager instance;
    private WaypointGUI waypointGUI;
 


    // Private constructor to enforce singleton
    private WaypointManager(PolarSpaceDisplay display) {
        this.display = display;

        display.setWaypointAdder((azimuth, elevation) -> {
            WaypointDrawable closestWaypoint = findClosestWaypoint(azimuth, elevation, 5.0f);

            if (closestWaypoint != null) {
                // Populate the GUI fields for editing
                populateGUIFields(closestWaypoint);
            } else {
                // Add a new waypoint
                String name = "Waypoint_" + System.currentTimeMillis();
                WaypointDrawable newWaypoint = new WaypointDrawable(name, azimuth, elevation);
                addWaypoint(newWaypoint);
            }
        });
        
        this.gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation() // Only serialize @Expose fields
            .registerTypeAdapter(Color.class, new ColorAdapter()) // Handle Color serialization
            .create();
        
    }

    // Get the singleton instance
    public static synchronized WaypointManager getInstance(PolarSpaceDisplay display) {
        if (instance == null) {
            instance = new WaypointManager(display);
        }
        return instance;
    }

    // Find the closest waypoint
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

    // Populate GUI fields for a waypoint
    public void populateGUIFields(WaypointDrawable waypoint) {
        if (waypointGUI != null) {
            waypointGUI.populateFields(waypoint);
        }
    }

       public void saveWaypointsToFile(File file) {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(waypoints, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadWaypointsFromFile(File file) {
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, WaypointDrawable>>() {}.getType();
            Map<String, WaypointDrawable> loadedWaypoints = gson.fromJson(reader, type);
            loadedWaypoints.values().forEach(this::addWaypoint);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    // Add a waypoint
    public synchronized void addWaypoint(WaypointDrawable waypoint) {
        waypoints.put(waypoint.getKey(), waypoint);
        display.addDrawable(waypoint);
    }

    // Update an existing waypoint
    public synchronized void updateWaypoint(WaypointDrawable waypoint) {
        waypoints.put(waypoint.getKey(), waypoint);
        display.removeDrawable(waypoint.getKey());
        display.addDrawable(waypoint);
    }

    // Remove a waypoint by key
    public synchronized void removeWaypoint(String key) {
        WaypointDrawable removed = waypoints.remove(key);
        if (removed != null) {
            display.removeDrawable(key);
        }
    }

    // Remove a waypoint by name
    public synchronized void removeWaypointByName(String name) {
        String key = "Waypoint_" + name;
        removeWaypoint(key);
    }

    // Get waypoint by key
    public synchronized WaypointDrawable getWaypointByKey(String key) {
        return waypoints.get(key);
    }

    // Get waypoint by name
    public synchronized WaypointDrawable getWaypointByName(String name) {
        return waypoints.get("Waypoint_" + name);
    }

    // Get all waypoints
    public synchronized Map<String, WaypointDrawable> getWaypoints() {
        return waypoints;
    }


    // Show or hide the GUI
    public void showGUI(boolean visible) {
        if (waypointGUI == null) {
            waypointGUI = new WaypointGUI(this);
        }

        SwingUtilities.invokeLater(() -> waypointGUI.setVisible(visible));
    }
}
