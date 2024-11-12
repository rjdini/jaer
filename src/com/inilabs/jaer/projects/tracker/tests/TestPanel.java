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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.inilabs.jaer.projects.tracker.TargetManager;

public class TestPanel extends JPanel {
    private final TargetManager targetManager;

    public TestPanel(TargetManager targetManager) {
        this.targetManager = targetManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(150, 300));

        // Add controls
        JButton addClustersButton = new JButton("Add Clusters");
        addClustersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                targetManager.addRandomClusters(5); // Example of adding random clusters
            }
        });
        add(addClustersButton);

        JButton startLoggingButton = new JButton("Start Logging");
        startLoggingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                targetManager.startLoggingAllAgents();
            }
        });
        add(startLoggingButton);

        JButton stopLoggingButton = new JButton("Stop Logging");
        stopLoggingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                targetManager.stopLoggingAllAgents();
            }
        });
        add(stopLoggingButton);
    }
}
