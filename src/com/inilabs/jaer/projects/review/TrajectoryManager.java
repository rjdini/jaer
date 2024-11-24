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

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.util.HashMap;
import java.util.Map;

public class TrajectoryManager {
    private PolarSpaceDisplay display;
    private Map<String, TrajectoryDrawable> loadedTrajectories;

    public TrajectoryManager(PolarSpaceDisplay display) {
        this.display = display;
        this.loadedTrajectories = new HashMap<>();
    }

    /**
     * Adds a trajectory by adding all its points to the display.
     */
    public void addTrajectory(TrajectoryDrawable trajectory) {
        if (!loadedTrajectories.containsKey(trajectory.getTrackerName())) {
            loadedTrajectories.put(trajectory.getTrackerName(), trajectory);
            for (TrajectoryPointDrawable point : trajectory.getPoints()) {
                display.addDrawable(point);
            }
            display.repaint();
        }
    }

    /**
     * Removes a trajectory by removing all its points from the display.
     */
    public void removeTrajectory(String trackerName) {
        TrajectoryDrawable trajectory = loadedTrajectories.get(trackerName);
        if (trajectory != null) {
            for (TrajectoryPointDrawable point : trajectory.getPoints()) {
                display.removeDrawable(point.getKey());
            }
            loadedTrajectories.remove(trackerName);
            display.repaint();
        }
    }

    /**
     * Toggles the visibility of a trajectory.
     */
    public void toggleTrajectoryVisibility(String trackerName) {
        if (loadedTrajectories.containsKey(trackerName)) {
            removeTrajectory(trackerName);
        } else {
            // Reload the trajectory if it was previously removed
            // This requires a source to reload trajectories
        }
    }
}

