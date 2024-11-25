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
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

public class LogVisualizerPanel extends JPanel {
    private JTree sessionTree;
    private TrajectoryManager manager;
    private Map<String, Map<String, TrajectoryDrawable>> sessions;

    public LogVisualizerPanel(PolarSpaceDisplay display, TrajectoryManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());

        sessionTree = new JTree();
        sessionTree.setCellRenderer(new CustomTreeCellRenderer());
        sessionTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) sessionTree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.isLeaf()) {
                String trackerName = selectedNode.getUserObject().toString();
                for (Map<String, TrajectoryDrawable> session : sessions.values()) {
                    if (session.containsKey(trackerName)) {
                        TrajectoryDrawable trajectory = session.get(trackerName);
                        boolean isAdded = manager.toggleTrajectoryVisibility(trackerName, trajectory);
                        updateNodeColor(selectedNode, isAdded);
                    }
                }
            }
        });

        add(new JScrollPane(sessionTree), BorderLayout.CENTER);
    }

    public void loadSessions(Map<String, Map<String, TrajectoryDrawable>> sessions) {
        this.sessions = sessions;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Sessions");

        for (String sessionName : sessions.keySet()) {
            DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(sessionName);
            Map<String, TrajectoryDrawable> trackers = sessions.get(sessionName);

            for (String trackerName : trackers.keySet()) {
                sessionNode.add(new DefaultMutableTreeNode(trackerName));
            }

            root.add(sessionNode);
        }

        sessionTree.setModel(new DefaultTreeModel(root));
    }

    private void updateNodeColor(DefaultMutableTreeNode node, boolean isAdded) {
        String trackerName = node.getUserObject().toString();
        if (isAdded) {
            node.setUserObject("<html><font color='green'>" + trackerName + "</font></html>");
        } else {
            node.setUserObject(trackerName); // Reset to default
        }
        ((DefaultTreeModel) sessionTree.getModel()).nodeChanged(node);
    }

    private static class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() != null && node.getUserObject().toString().contains("<html>")) {
                    ((JLabel) component).setText(node.getUserObject().toString());
                }
            }
            return component;
        }
    }
}
