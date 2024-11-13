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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PolarSpaceControlPanel extends JPanel {

    private final JTextField azimuthHeadingField;
    private final JTextField elevationHeadingField;
    private final JSlider azimuthRangeSlider;
    private final JSlider elevationRangeSlider;
    private final JToggleButton linkSlidersButton;
    private final JToggleButton pathToggleButton;
    private final JButton closeButton;
    private final JButton startLoggingButton;
    private final JButton stopLoggingButton;
    private final PolarSpaceDisplay polarDisplay;
    private boolean slidersLinked = true;

    public PolarSpaceControlPanel(PolarSpaceDisplay polarDisplay, ActionListener closeAction) {
        this.polarDisplay = polarDisplay;
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout for WEST and EAST sections

        // Settings panel for sliders and heading fields
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

        // Place settings panel on the WEST side
        add(settingsPanel, BorderLayout.WEST);

        // Button panel with smaller buttons
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

        // Logging control panel on the EAST side
        JPanel loggingPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        startLoggingButton = new JButton("Start Logging");
        startLoggingButton.addActionListener(e -> startLogging());
        loggingPanel.add(startLoggingButton);

        stopLoggingButton = new JButton("Stop Logging");
        stopLoggingButton.addActionListener(e -> stopLogging());
        loggingPanel.add(stopLoggingButton);

        add(loggingPanel, BorderLayout.EAST);

        // Synchronize sliders if linked
        azimuthRangeSlider.addChangeListener(e -> {
            if (slidersLinked) {
                elevationRangeSlider.setValue(azimuthRangeSlider.getValue());
            }
         updateAzimuthRange();
        });
        elevationRangeSlider.addChangeListener(e -> {
            if (slidersLinked) {
                azimuthRangeSlider.setValue(elevationRangeSlider.getValue());
            }
            updateElevationRange();
        });
    }

    private JSlider createSlider(int min, int max, int initial, ChangeListener listener) {
        JSlider slider = new JSlider(min, max, initial);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.addChangeListener(listener);
        return slider;
    }

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

    public void setHeading(float azimuth, float elevation) {
        azimuthHeadingField.setText(String.valueOf(azimuth));
        elevationHeadingField.setText(String.valueOf(elevation));
        polarDisplay.setHeading(azimuth, elevation);
    }

    private void startLogging() {
        // Implement logging start logic
    }

    private void stopLogging() {
        // Implement logging stop logic
    }

    private class PathToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            polarDisplay.showPaths(pathToggleButton.isSelected());
        }
    }
}
