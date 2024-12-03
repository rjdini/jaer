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
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.Type;

public class WaypointGUI extends JFrame {
    private final WaypointManager manager;
    private final JTextField nameField = new JTextField(10);
    private final JTextField azimuthField = new JTextField(5);
    private final JTextField elevationField = new JTextField(5);
    private final JButton colorButton = new JButton("Pick Color");
    private Color selectedColor = Color.GREEN;

    public WaypointGUI(WaypointManager manager) {
        this.manager = manager;
        setTitle("Waypoint Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new GridLayout(0, 2));
        mainPanel.add(new JLabel("Name:"));
        mainPanel.add(nameField);
        mainPanel.add(new JLabel("Azimuth:"));
        mainPanel.add(azimuthField);
        mainPanel.add(new JLabel("Elevation:"));
        mainPanel.add(elevationField);
        mainPanel.add(new JLabel("Color:"));
        mainPanel.add(colorButton);

        colorButton.addActionListener(e -> {
            selectedColor = JColorChooser.showDialog(this, "Choose Waypoint Color", selectedColor);
            colorButton.setBackground(selectedColor); // Reflect the selected color in the button
        });

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Waypoint");
        JButton editButton = new JButton("Edit Waypoint");
        JButton deleteButton = new JButton("Delete Waypoint");
        JButton saveButton = new JButton("Save Waypoints");
        JButton loadButton = new JButton("Load Waypoints");

        addButton.addActionListener(this::addWaypoint);
        editButton.addActionListener(this::editWaypoint);
        deleteButton.addActionListener(this::deleteWaypoint);
        saveButton.addActionListener(this::saveWaypoints);
        loadButton.addActionListener(this::loadWaypoints);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addWaypoint(ActionEvent e) {
        try {
            String name = nameField.getText();
            float azimuth = Float.parseFloat(azimuthField.getText());
            float elevation = Float.parseFloat(elevationField.getText());
            WaypointDrawable waypoint = new WaypointDrawable(name, azimuth, elevation);
            waypoint.setColor(selectedColor);
            manager.addWaypoint(waypoint);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid azimuth or elevation value.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editWaypoint(ActionEvent e) {
        String name = nameField.getText();
        WaypointDrawable waypoint = manager.getWaypointByName(name);
        if (waypoint != null) {
            try {
                waypoint.setAzimuth(Float.parseFloat(azimuthField.getText()));
                waypoint.setElevation(Float.parseFloat(elevationField.getText()));
                waypoint.setColor(selectedColor);
                manager.updateWaypoint(waypoint); // Ensure the manager updates the waypoint correctly
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid azimuth or elevation value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Waypoint not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteWaypoint(ActionEvent e) {
        String name = nameField.getText();
        if (!name.isEmpty()) {
            manager.removeWaypointByName(name); // Manager handles removal by name
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a waypoint name to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveWaypoints(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".")); // Set to working directory
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            manager.saveWaypointsToFile(file);
        }
    }

    private void loadWaypoints(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".")); // Set to working directory
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            manager.loadWaypointsFromFile(file);
        }
    }

    public void populateFields(WaypointDrawable waypoint) {
        nameField.setText(waypoint.getName());
        azimuthField.setText(String.valueOf(waypoint.getAzimuth()));
        elevationField.setText(String.valueOf(waypoint.getElevation()));
        selectedColor = waypoint.getColor();
        colorButton.setBackground(selectedColor); // Reflect the waypoint's color in the button
    }
}

