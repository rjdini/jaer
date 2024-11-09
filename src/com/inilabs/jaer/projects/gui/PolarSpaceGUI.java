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
package com.inilabs.jaer.projects.gui;

import javax.swing.*;
import java.awt.*;

public class PolarSpaceGUI extends JFrame {

    private JPanel controlPanel;
    private PolarSpaceDisplay polarDisplay;
    private JSlider azimuthRangeSlider;
    private JSlider elevationRangeSlider;
    private JTextField azimuthHeadingField;
    private JTextField elevationHeadingField;
    private JButton closeButton;

    public PolarSpaceGUI() {
        setTitle("Polar Space GUI");
        setSize(1600, 1200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize Polar Display
        polarDisplay = new PolarSpaceDisplay();
        polarDisplay.setPreferredSize(new Dimension(1600, 1000));
        add(polarDisplay, BorderLayout.CENTER);

        // Create Control Panel with a more accessible layout
        controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);  // Padding for accessibility

        // Azimuth heading text field
        azimuthHeadingField = new JTextField("0", 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("Azimuth Heading:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(azimuthHeadingField, gbc);

        // Elevation heading text field
        elevationHeadingField = new JTextField("0", 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("Elevation Heading:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(elevationHeadingField, gbc);

        // Azimuth range slider (0 to 90 degrees)
        azimuthRangeSlider = new JSlider(0, 90, 30);
        azimuthRangeSlider.setPaintLabels(true);
        azimuthRangeSlider.setPaintTicks(true);
        azimuthRangeSlider.setMajorTickSpacing(10);
        azimuthRangeSlider.setMinorTickSpacing(5);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("Azimuth Range:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(azimuthRangeSlider, gbc);

        // Elevation range slider (0 to 90 degrees)
        elevationRangeSlider = new JSlider(0, 90, 30);
        elevationRangeSlider.setPaintLabels(true);
        elevationRangeSlider.setPaintTicks(true);
        elevationRangeSlider.setMajorTickSpacing(10);
        elevationRangeSlider.setMinorTickSpacing(5);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(new JLabel("Elevation Range:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(elevationRangeSlider, gbc);

        // Close button
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(closeButton, gbc);

        // Add control panel to the south region with larger spacing
        add(controlPanel, BorderLayout.SOUTH);

        // Add listeners for slider and text field changes
        azimuthRangeSlider.addChangeListener(e -> polarDisplay.setAzimuthRange(azimuthRangeSlider.getValue()));
        elevationRangeSlider.addChangeListener(e -> polarDisplay.setElevationRange(elevationRangeSlider.getValue()));
        azimuthHeadingField.addActionListener(e -> updateHeading());
        elevationHeadingField.addActionListener(e -> updateHeading());

        pack();
        setVisible(true);
    }

    private void updateHeading() {
        try {
            int azimuthHeading = Integer.parseInt(azimuthHeadingField.getText());
            int elevationHeading = Integer.parseInt(elevationHeadingField.getText());
            polarDisplay.setHeading(azimuthHeading, elevationHeading);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid integer values for heading.");
        }
    }

    // Getter for PolarSpaceDisplay to allow adding drawables
    public PolarSpaceDisplay getPolarSpaceDisplay() {
        return polarDisplay;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PolarSpaceGUI::new);
    }
}
