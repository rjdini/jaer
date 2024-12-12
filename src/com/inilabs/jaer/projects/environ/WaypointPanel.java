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
import java.io.File;
import java.io.IOException;

public class WaypointPanel extends BasicTestPanel {

    private JTextField nameField;
    private JSpinner azimuthSpinner;
    private JSpinner elevationSpinner;
    private JButton addButton, editButton, listButton, deleteButton, saveButton, loadButton, colorButton;
    private Color selectedColor = Color.WHITE; // Default color
    private WaypointDrawable editingWaypoint;
    private final WaypointManager manager;

    public WaypointPanel(WaypointManager manager) {
        this.manager = manager;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        setNameField(new JTextField(10));
        setAzimuthSpinner(new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 0.1)));
        setElevationSpinner(new JSpinner(new SpinnerNumberModel(0.0, -90.0, 90.0, 0.1)));

        colorButton = new JButton("Choose Color");
        colorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Select a Color", getSelectedColor());
            if (color != null) {
                setSelectedColor(color);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(getNameField(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Azimuth:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(getAzimuthSpinner(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Elevation:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(getElevationSpinner(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(colorButton, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        listButton = new JButton("List");
        deleteButton = new JButton("Delete");
        saveButton = new JButton("Save");
        loadButton = new JButton("Load");

        buttonPanel.add(getAddButton(), gbc);
        gbc.gridy++;
        buttonPanel.add(editButton, gbc);
        gbc.gridy++;
        buttonPanel.add(listButton, gbc);
        gbc.gridy++;
        buttonPanel.add(deleteButton, gbc);
        gbc.gridy++;
        buttonPanel.add(saveButton, gbc);
        gbc.gridy++;
        buttonPanel.add(loadButton, gbc);

        getAddButton().addActionListener(e -> addWaypoint());
        editButton.addActionListener(e -> editWaypoint());
        listButton.addActionListener(e -> listWaypoints());
        deleteButton.addActionListener(e -> deleteWaypoint());
        saveButton.addActionListener(e -> saveWaypoints());
        loadButton.addActionListener(e -> loadWaypoints());

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }


    private void addWaypoint() {
        String name = getNameField().getText();
        float azimuth = ((Double) getAzimuthSpinner().getValue()).floatValue();
        float elevation = ((Double) getElevationSpinner().getValue()).floatValue();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        WaypointDrawable waypoint = new WaypointDrawable(name, azimuth, elevation, getSelectedColor());
        manager.addWaypoint(waypoint);
        clearFields();
    }

    private void editWaypoint() {
        if (editingWaypoint == null) {
            JOptionPane.showMessageDialog(this, "No waypoint selected for editing!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = getNameField().getText();
        float azimuth = ((Double) getAzimuthSpinner().getValue()).floatValue();
        float elevation = ((Double) getElevationSpinner().getValue()).floatValue();

        editingWaypoint.setName(name);
        editingWaypoint.setAzimuth(azimuth);
        editingWaypoint.setElevation(elevation);
        editingWaypoint.setColor(getSelectedColor());

        manager.updateWaypoint(editingWaypoint);
        clearFields();
    }

    private void listWaypoints() {
        StringBuilder waypointList = new StringBuilder("Waypoints:\n");
        for (WaypointDrawable waypoint : manager.getAllWaypoints().values()) {
            waypointList.append("Name: ").append(waypoint.getName())
                        .append(", Azimuth: ").append(waypoint.getAzimuth())
                        .append(", Elevation: ").append(waypoint.getElevation())
                        .append(", Color: ").append(waypoint.getColor())
                        .append("\n");
        }
        JOptionPane.showMessageDialog(this, waypointList.toString(), "Waypoint List", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteWaypoint() {
        if (editingWaypoint == null) {
            JOptionPane.showMessageDialog(this, "No waypoint selected for deletion!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        manager.removeWaypoint(editingWaypoint.getKey());
        clearFields();
    }

    private void saveWaypoints() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                manager.saveWaypointsToFile(fileChooser.getSelectedFile());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving waypoints: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWaypoints() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                manager.loadWaypointsFromFile(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading waypoints: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        getNameField().setText("");
        getAzimuthSpinner().setValue(0.0);
        getElevationSpinner().setValue(0.0);
        setSelectedColor(Color.WHITE);
    }
    
    
     public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Waypoint Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            WaypointManager manager = WaypointManager.getInstance();
            WaypointPanel waypointPanel = new WaypointPanel(manager);

            frame.add(waypointPanel, BorderLayout.CENTER);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * @return the nameField
     */
    public JTextField getNameField() {
        return nameField;
    }

    /**
     * @param nameField the nameField to set
     */
    public void setNameField(JTextField nameField) {
        this.nameField = nameField;
    }

    /**
     * @return the azimuthSpinner
     */
    public JSpinner getAzimuthSpinner() {
        return azimuthSpinner;
    }

    /**
     * @param azimuthSpinner the azimuthSpinner to set
     */
    public void setAzimuthSpinner(JSpinner azimuthSpinner) {
        this.azimuthSpinner = azimuthSpinner;
    }

    /**
     * @return the elevationSpinner
     */
    public JSpinner getElevationSpinner() {
        return elevationSpinner;
    }

    /**
     * @param elevationSpinner the elevationSpinner to set
     */
    public void setElevationSpinner(JSpinner elevationSpinner) {
        this.elevationSpinner = elevationSpinner;
    }

    /**
     * @return the selectedColor
     */
    public Color getSelectedColor() {
        return selectedColor;
    }

    /**
     * @param selectedColor the selectedColor to set
     */
    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
    }

    /**
     * @return the addButton
     */
    public JButton getAddButton() {
        return addButton;
    }
    
}

