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
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import com.inilabs.jaer.projects.review.TrajectoryPointDrawable;

public class LogVisualizerPanel extends JPanel {
    private JTree sessionTree;
    private PolarSpaceDisplay display;
    private TrajectoryManager manager;

    public LogVisualizerPanel(PolarSpaceDisplay display, TrajectoryManager manager) {
        this.display = display;
        this.manager = manager;

        setLayout(new BorderLayout());

        sessionTree = new JTree();
        sessionTree.addTreeSelectionListener(e -> {
            Object selectedNode = sessionTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                manager.toggleTrajectoryVisibility(selectedNode.toString());
            }
        });

        add(new JScrollPane(sessionTree), BorderLayout.CENTER);
    }

    public void loadSessions(Map<String, Map<String, List<TrajectoryPointDrawable>>> sessions) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sessions");
        for (String session : sessions.keySet()) {
            DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(session);
            Map<String, List<TrajectoryPointDrawable>> trackers = sessions.get(session);

            for (String tracker : trackers.keySet()) {
                DefaultMutableTreeNode trackerNode = new DefaultMutableTreeNode(tracker);
                sessionNode.add(trackerNode);

                manager.addTrajectory(tracker, trackers.get(tracker));
            }
            root.add(sessionNode);
        }
        sessionTree.setModel(new DefaultTreeModel(root));
    }
}
