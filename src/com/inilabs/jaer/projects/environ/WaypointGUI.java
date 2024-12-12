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

import com.inilabs.jaer.projects.gui.BasicTestPanel;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class WaypointGUI extends BasicTestPanel {
    private JTextField nameField;
    private JSpinner azimuthSpinner;
    private JSpinner elevationSpinner;
    private JButton addButton, editButton, listButton, deleteButton, saveButton, loadButton, colorButton;
    private Color selectedColor = Color.WHITE; // Default color
    private WaypointDrawable editingWaypoint;
    private final WaypointManager manager = WaypointManager.getInstance();

    public WaypointGUI() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(10);
        azimuthSpinner = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.1));
        elevationSpinner = new JSpinner(new SpinnerNumberModel(0.0, -90.0, 90.0, 0.1));

        colorButton = new JButton("Choose Color");
        colorButton.setPreferredSize(new Dimension(100, 20));        
        colorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Select a Color", selectedColor);
            if (color != null) {
                selectedColor = color;
            }
        });
       
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Azimuth:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(azimuthSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Elevation:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(elevationSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(colorButton, gbc);

        // Button Panel (Stacked Vertically)
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        listButton = new JButton("List");
        deleteButton = new JButton("Delete");
        saveButton = new JButton("Save");
        loadButton = new JButton("Load");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
         buttonPanel.add(listButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        addButton.addActionListener(e -> addWaypoint());
        editButton.addActionListener(e -> editWaypoint());
        deleteButton.addActionListener(e -> deleteWaypoint());
        listButton.addActionListener(e -> listWaypoints());
        saveButton.addActionListener(e -> {
            try {
                saveWaypoints();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        loadButton.addActionListener(e -> {
            try {
                loadWaypoints();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Combine Panels
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    
    private void addWaypoint() {
        String name = nameField.getText();
        float azimuth = ((Double) azimuthSpinner.getValue()).floatValue();
        float elevation = ((Double) elevationSpinner.getValue()).floatValue();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        WaypointDrawable waypoint = new WaypointDrawable(name, azimuth, elevation, selectedColor);
        manager.addWaypoint(waypoint);
        clearFields();
        JOptionPane.showMessageDialog(this, "Waypoint added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editWaypoint() {
    if (editingWaypoint == null) {
        JOptionPane.showMessageDialog(this, "No waypoint selected for editing!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String name = nameField.getText();
    float azimuth = ((Double) azimuthSpinner.getValue()).floatValue();
    float elevation = ((Double) elevationSpinner.getValue()).floatValue();
   // Color color = colorChooser.getColor();

    editingWaypoint.setName(name);
    editingWaypoint.setAzimuth(azimuth);
    editingWaypoint.setElevation(elevation);
    editingWaypoint.setColor(selectedColor);

    manager.updateWaypoint(editingWaypoint); // Update in manager
    clearFields();
    editingWaypoint = null;
    JOptionPane.showMessageDialog(this, "Waypoint updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
}

    
    private void listWaypoints() {
       
    StringBuilder waypointList = new StringBuilder("Waypoints:\n");
    for (WaypointDrawable waypoint : manager.getAllWaypoints().values()) {
        waypointList.append("Name: ").append(waypoint.getName())
                    .append(", Azimuth: ").append(waypoint.getAzimuth())
                    .append(", Elevation: ").append(waypoint.getElevation())
                    .append(", Color: ").append(waypoint.getColor().toString())
                    .append("\n");
    }
    if (waypointList.toString().equals("Waypoints:\n")) {
        JOptionPane.showMessageDialog(this, "No waypoints found.", "Waypoint List", JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, waypointList.toString(), "Waypoint List", JOptionPane.INFORMATION_MESSAGE);
    }
}
    
    
    
    private void deleteWaypoint() {
        if (editingWaypoint == null) {
            JOptionPane.showMessageDialog(this, "No waypoint selected for deletion!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        manager.removeWaypoint(editingWaypoint.getKey());
        clearFields();
        editingWaypoint = null;
        JOptionPane.showMessageDialog(this, "Waypoint deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void populateFields(WaypointDrawable waypoint) {
        editingWaypoint = waypoint;
        nameField.setText(waypoint.getName());
        azimuthSpinner.setValue((double) waypoint.getAzimuth());
        elevationSpinner.setValue((double) waypoint.getElevation());
        selectedColor = waypoint.getColor();
    }
    
    
    private void saveWaypoints() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            manager.saveWaypointsToFile(fileChooser.getSelectedFile());
        }
    }

    private void loadWaypoints() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            manager.loadWaypointsFromFile(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void clearFields() {
        nameField.setText("");
        azimuthSpinner.setValue(0.0);
        elevationSpinner.setValue(0.0);
        selectedColor = Color.WHITE;
    }
}
