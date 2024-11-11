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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PolarSpaceGUITest {
    private JFrame frame;
    private PolarSpaceDisplay polarSpaceDisplay;
    private JSlider azimuthRangeSlider;
    private JSlider elevationRangeSlider;
    private JTextField azimuthHeadingField;
    private JTextField elevationHeadingField;

    public PolarSpaceGUITest() {
        // Initialize the GUI components
        frame = new JFrame("Polar Space GUI Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Initialize PolarSpaceDisplay (the main panel to test)
        polarSpaceDisplay = new PolarSpaceDisplay();
        polarSpaceDisplay.setPreferredSize(new Dimension(600, 400));

        // Set initial heading to 0,0 (center of viewport)
        polarSpaceDisplay.setHeading(0, 0);

        // Set up azimuth and elevation range sliders
        azimuthRangeSlider = new JSlider(10, 90, 30); // Range from 10 to 90 degrees, starting at 30
        azimuthRangeSlider.setMajorTickSpacing(20);
        azimuthRangeSlider.setPaintTicks(true);
        azimuthRangeSlider.setPaintLabels(true);
        azimuthRangeSlider.addChangeListener(e -> {
            int range = azimuthRangeSlider.getValue();
            polarSpaceDisplay.setAzimuthRange(range);
        });

        elevationRangeSlider = new JSlider(10, 90, 30); // Range from 10 to 90 degrees, starting at 30
        elevationRangeSlider.setMajorTickSpacing(20);
        elevationRangeSlider.setPaintTicks(true);
        elevationRangeSlider.setPaintLabels(true);
        elevationRangeSlider.addChangeListener(e -> {
            int range = elevationRangeSlider.getValue();
            polarSpaceDisplay.setElevationRange(range);
        });

        // Set up heading controls for azimuth and elevation
        azimuthHeadingField = new JTextField("0", 5);
        elevationHeadingField = new JTextField("0", 5);
        JButton setHeadingButton = new JButton("Set Heading");

        // Button to update the heading based on the text field inputs
        setHeadingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float azimuthHeading = Float.parseFloat(azimuthHeadingField.getText());
                    float elevationHeading = Float.parseFloat(elevationHeadingField.getText());
                    polarSpaceDisplay.setHeading(azimuthHeading, elevationHeading);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numbers for azimuth and elevation headings.");
                }
            }
        });

        // Organize controls into a control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 2));

        controlPanel.add(new JLabel("Azimuth Range (degrees):"));
        controlPanel.add(azimuthRangeSlider);
        controlPanel.add(new JLabel("Elevation Range (degrees):"));
        controlPanel.add(elevationRangeSlider);
        controlPanel.add(new JLabel("Azimuth Heading:"));
        controlPanel.add(azimuthHeadingField);
        controlPanel.add(new JLabel("Elevation Heading:"));
        controlPanel.add(elevationHeadingField);
        controlPanel.add(setHeadingButton);

        // Add components to frame
        frame.add(polarSpaceDisplay, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // Pack and display the frame
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Run the test case on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new PolarSpaceGUITest());
    }
}
