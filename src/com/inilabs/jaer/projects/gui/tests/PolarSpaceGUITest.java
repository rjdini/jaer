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
package com.inilabs.jaer.projects.gui.tests;

import com.inilabs.jaer.projects.gui.BasicDrawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class PolarSpaceGUITest {
    private JFrame frame;
    private PolarSpaceDisplay polarSpaceDisplay;
    private JSlider azimuthRangeSlider;
    private JSlider elevationRangeSlider;
    private JTextField azimuthHeadingField;
    private JTextField elevationHeadingField;
    private BasicDrawable movingDrawable;

    public PolarSpaceGUITest() {
        // Initialize the GUI components
        frame = new JFrame("Polar Space GUI Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Initialize PolarSpaceDisplay (the main panel to test)
        polarSpaceDisplay = PolarSpaceDisplay.getInstance();
        polarSpaceDisplay.setPreferredSize(new Dimension(600, 400));

        // Set initial heading and range
        polarSpaceDisplay.setHeading(0, 0);
        polarSpaceDisplay.setAzimuthRange(30);
        polarSpaceDisplay.setElevationRange(30);

        // Set up azimuth and elevation range sliders
        azimuthRangeSlider = new JSlider(10, 90, 30);
        azimuthRangeSlider.setMajorTickSpacing(20);
        azimuthRangeSlider.setPaintTicks(true);
        azimuthRangeSlider.setPaintLabels(true);
        azimuthRangeSlider.addChangeListener(e -> polarSpaceDisplay.setAzimuthRange(azimuthRangeSlider.getValue()));

        elevationRangeSlider = new JSlider(10, 90, 30);
        elevationRangeSlider.setMajorTickSpacing(20);
        elevationRangeSlider.setPaintTicks(true);
        elevationRangeSlider.setPaintLabels(true);
        elevationRangeSlider.addChangeListener(e -> polarSpaceDisplay.setElevationRange(elevationRangeSlider.getValue()));

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
        JPanel controlPanel = new JPanel(new GridLayout(3, 2));
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

        // Load drawables
        addDrawables();

        // Pack and display the frame
        frame.pack();
        frame.setVisible(true);

        // Start moving one of the drawables in a box pattern
        startBoxPatternAnimation();
    }

    /**
     * Adds two BasicDrawables to the PolarSpaceDisplay, each with a unique color.
     */
    private void addDrawables() {
        // Create drawables at initial positions and add them to the display
        BasicDrawable drawable1 = new BasicDrawable("Drawable1", 10, 0);
        drawable1.setColor(Color.RED);

        movingDrawable = new BasicDrawable("Drawable2", -10, -10);
        movingDrawable.setColor(Color.GREEN);

        polarSpaceDisplay.addDrawable(drawable1);
        polarSpaceDisplay.addDrawable(movingDrawable);
    }

    /**
     * Animates the moving drawable in a box pattern around the heading.
     */
    private void startBoxPatternAnimation() {
        Timer timer = new Timer();
        int delay = 1000; // Delay between moves in milliseconds
        int[] boxPattern = {0, 20, 20, 0, 0, -20, -20, 0}; // Pattern of azimuth and elevation moves

        timer.scheduleAtFixedRate(new TimerTask() {
            int index = 0;

            @Override
            public void run() {
                // Update azimuth and elevation in box pattern
                movingDrawable.setAzimuth(boxPattern[index % 8]);
                movingDrawable.setElevation(boxPattern[(index + 1) % 8]);
                polarSpaceDisplay.repaint();
                index = (index + 2) % 8;
            }
        }, delay, delay);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PolarSpaceGUITest());
    }
}
