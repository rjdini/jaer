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

/**
 *
 * @author rjd
 */

package com.inilabs.jaer.projects.gui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PolarSpaceControlPanel extends JPanel {

    private final JTextField azimuthHeadingField;
    private final JTextField elevationHeadingField;
    private final JSlider azimuthRangeSlider;
    private final JSlider elevationRangeSlider;
    private final JToggleButton linkSlidersButton;
    private final JToggleButton pathToggleButton;
    private final JButton closeButton;
    private final PolarSpaceDisplay polarDisplay;
    private boolean slidersLinked = false;

    public PolarSpaceControlPanel(PolarSpaceDisplay polarDisplay, ActionListener closeAction) {
        this.polarDisplay = polarDisplay;

        setLayout(new BorderLayout(10, 10)); // Use BorderLayout for top and bottom sections

        // Main settings panel with a 2-column grid
        JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 10, 5));

        // Azimuth heading
        settingsPanel.add(new JLabel("Azimuth Heading:", SwingConstants.RIGHT));
        azimuthHeadingField = new JTextField("0", 5);
        azimuthHeadingField.addActionListener(e -> updateHeadingFromField());
        settingsPanel.add(azimuthHeadingField);

        // Elevation heading
        settingsPanel.add(new JLabel("Elevation Heading:", SwingConstants.RIGHT));
        elevationHeadingField = new JTextField("0", 5);
        elevationHeadingField.addActionListener(e -> updateHeadingFromField());
        settingsPanel.add(elevationHeadingField);

        // Azimuth range slider
        settingsPanel.add(new JLabel("Azimuth Range:", SwingConstants.RIGHT));
        azimuthRangeSlider = createSlider(0, 150, 30, e -> updateAzimuthRange());
        settingsPanel.add(azimuthRangeSlider);

        // Elevation range slider
        settingsPanel.add(new JLabel("Elevation Range:", SwingConstants.RIGHT));
        elevationRangeSlider = createSlider(0, 90, 30, e -> updateElevationRange());
        settingsPanel.add(elevationRangeSlider);

        add(settingsPanel, BorderLayout.CENTER);

        // Button panel with smaller buttons in FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        // Link sliders button
        linkSlidersButton = createToggleButton("Link Sliders", 120, 25);
        linkSlidersButton.addActionListener(e -> slidersLinked = linkSlidersButton.isSelected());
        buttonPanel.add(linkSlidersButton);
            
        // Path toggle button
        pathToggleButton = createToggleButton("Show Paths", 120, 25);
        pathToggleButton.addActionListener(new PathToggleListener());
        buttonPanel.add(pathToggleButton);

        // Close button
        closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(80, 25));
        closeButton.addActionListener(closeAction);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Synchronize sliders if linked
        azimuthRangeSlider.addChangeListener(e -> {
            if (slidersLinked) {
                elevationRangeSlider.setValue(azimuthRangeSlider.getValue());
            }
            updateAzimuthScale();
        });
        elevationRangeSlider.addChangeListener(e -> {
            if (slidersLinked) {
                azimuthRangeSlider.setValue(elevationRangeSlider.getValue());
            }
            updateElevationScale();
        });
    }

    // Helper method to create sliders
    private JSlider createSlider(int min, int max, int initial, ChangeListener listener) {
        JSlider slider = new JSlider(min, max, initial);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.addChangeListener(listener);
        return slider;
    }

    // Helper method to create smaller toggle buttons
    private JToggleButton createToggleButton(String text, int width, int height) {
        JToggleButton button = new JToggleButton(text);
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    
    private void updateHeadingFromField() {
    try {
        float azimuth = Float.parseFloat(azimuthHeadingField.getText());
        float elevation = Float.parseFloat(elevationHeadingField.getText());
        polarDisplay.setHeading(azimuth, elevation);  // Update display with new heading
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Please enter valid numbers for azimuth and elevation.",
                                      "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }
}
    

    protected void updateAzimuthRange() {
        polarDisplay.setAzimuthRange(azimuthRangeSlider.getValue());
    }

    protected void updateElevationRange() {
        polarDisplay.setElevationRange(elevationRangeSlider.getValue());
    }

    private void updateAzimuthScale() {
        // Code to update azimuth scale if needed
    }

    private void updateElevationScale() {
        // Code to update elevation scale if needed
    }

    private class PathToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            polarDisplay.showPaths(pathToggleButton.isSelected());
        }
    }
}
