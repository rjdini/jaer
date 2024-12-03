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
package com.inilabs.jaer.projects.environ.tests;

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.environ.WaypointDrawable;
import com.inilabs.jaer.projects.environ.WaypointManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class WaypointSystemTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create a PolarSpaceDisplay for visualization
            PolarSpaceDisplay display = new PolarSpaceDisplay();
            display.setPreferredSize(new Dimension(1000, 600));
            display.initializeDisplay();

            // Use WaypointManager singleton to manage waypoints
            WaypointManager manager = WaypointManager.getInstance(display);

            // Create a JFrame to host the display and controls
            JFrame frame = new JFrame("Waypoint System Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Add the display to the center of the frame
            frame.add(display, BorderLayout.CENTER);

            // Create a control panel for managing waypoints
            JPanel controlPanel = new JPanel(new FlowLayout());
            controlPanel.setPreferredSize(new Dimension(1000, 100));

            JTextField nameField = new JTextField(10);
            JTextField azimuthField = new JTextField(5);
            JTextField elevationField = new JTextField(5);
            JButton colorButton = new JButton("Pick Color");
            Color[] selectedColor = {Color.GREEN}; // Default color

            JButton addButton = new JButton("Add Waypoint");
            JButton removeButton = new JButton("Remove Waypoint");
            JButton listButton = new JButton("List Waypoints");
            JButton saveButton = new JButton("Save Waypoints");
            JButton loadButton = new JButton("Load Waypoints");

            // Add components to the control panel
            controlPanel.add(new JLabel("Name:"));
            controlPanel.add(nameField);
            controlPanel.add(new JLabel("Azimuth:"));
            controlPanel.add(azimuthField);
            controlPanel.add(new JLabel("Elevation:"));
            controlPanel.add(elevationField);
            controlPanel.add(new JLabel("Color:"));
            controlPanel.add(colorButton);
            controlPanel.add(addButton);
            controlPanel.add(removeButton);
            controlPanel.add(listButton);
            controlPanel.add(saveButton);
            controlPanel.add(loadButton);

            // Color picker action
            colorButton.addActionListener(e -> {
                Color newColor = JColorChooser.showDialog(frame, "Choose Waypoint Color", selectedColor[0]);
                if (newColor != null) {
                    selectedColor[0] = newColor;
                    colorButton.setBackground(newColor); // Reflect selected color
                }
            });

            addButton.addActionListener((ActionEvent e) -> {
                try {
                    String name = nameField.getText();
                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("Name cannot be empty.");
                    }

                    float azimuth = Float.parseFloat(azimuthField.getText());
                    float elevation = Float.parseFloat(elevationField.getText());

                    WaypointDrawable waypoint = new WaypointDrawable(name, azimuth, elevation);
                    waypoint.setColor(selectedColor[0]); // Set the chosen color
                    manager.addWaypoint(waypoint);
                    display.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid azimuth or elevation value.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Remove button action
            removeButton.addActionListener((ActionEvent e) -> {
                String name = nameField.getText();
                if (!name.isEmpty()) {
                    manager.removeWaypointByName(name); // Use the new method
                    display.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a waypoint name to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // List button action
            listButton.addActionListener((ActionEvent e) -> {
                StringBuilder waypointsList = new StringBuilder("Current Waypoints:\n");
                manager.getWaypoints().forEach((key, waypoint) -> {
                    waypointsList.append(String.format(
                            "Name: %s, Azimuth: %.2f, Elevation: %.2f, Color: %s\n",
                            waypoint.getName(),
                            waypoint.getAzimuth(),
                            waypoint.getElevation(),
                            waypoint.getColor().toString()
                    ));
                });
                JOptionPane.showMessageDialog(frame, waypointsList.toString(), "Waypoint List", JOptionPane.INFORMATION_MESSAGE);
            });

            // Save button action
            saveButton.addActionListener((ActionEvent e) -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(".")); // Start in working directory
                if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    manager.saveWaypointsToFile(file);
                }
            });

            // Load button action
            loadButton.addActionListener((ActionEvent e) -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(".")); // Start in working directory
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    manager.loadWaypointsFromFile(file);
                    display.repaint();
                }
            });

            // Add the control panel to the frame
            frame.add(controlPanel, BorderLayout.SOUTH);

            // Set frame size and make it visible
            frame.setSize(1000, 800);
            frame.setVisible(true);
        });
    }
}
