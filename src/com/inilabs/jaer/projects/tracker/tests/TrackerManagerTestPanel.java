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

package com.inilabs.jaer.projects.tracker.tests;

import com.inilabs.jaer.projects.tracker.TrackerManager;
import com.inilabs.jaer.projects.tracker.EventCluster;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TrackerManagerTestPanel extends JPanel {
    private final TrackerManager trackerManager;

    // Constructor that accepts a TrackerManager instance
    public TrackerManagerTestPanel(TrackerManager trackerManager) {
        this.trackerManager = trackerManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, 400));

        // Add Cluster Button
        JButton addClusterButton = new JButton("Add Cluster");
        addClusterButton.addActionListener(e -> addRandomCluster());
        add(addClusterButton);

        // Remove Agent Button
        JButton removeRandomAgentButton = new JButton("Remove Random Agent");
        removeRandomAgentButton.addActionListener(e -> removeRandomAgent());
        add(removeRandomAgentButton);
    }

    // Adds a random cluster to the TrackerManager
    private void addRandomCluster() {
        EventCluster cluster = new EventCluster();
        cluster.setAzimuth((float) (Math.random() * 60 - 30));  // Random azimuth
        cluster.setElevation((float) (Math.random() * 60 - 30)); // Random elevation
        trackerManager.processClusters(List.of(cluster));  // Process the cluster
    }

    // Removes a random agent from TrackerManager
    private void removeRandomAgent() {
        List<String> agentKeys = new ArrayList<>(trackerManager.getAgentKeys());
        if (!agentKeys.isEmpty()) {
            String randomKey = agentKeys.get((int) (Math.random() * agentKeys.size()));
            trackerManager.removeAgent(randomKey);
            System.out.println("Removed agent with key: " + randomKey);
        }
    }
}

