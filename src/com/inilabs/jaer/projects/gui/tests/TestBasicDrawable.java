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

package com.inilabs.jaer.projects.gui.tests;

import com.inilabs.jaer.projects.gui.BasicDrawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TestBasicDrawable {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestBasicDrawable::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Main JFrame
        JFrame frame = new JFrame("BasicDrawable Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // PolarSpaceDisplay
        PolarSpaceDisplay display = PolarSpaceDisplay.getInstance();
        frame.add(display, BorderLayout.CENTER);

        // Create a BasicDrawable at (0,0) with auto-incremented ID and add it to display
        BasicDrawable drawable = new BasicDrawable();
        drawable.setSize(10); // Larger size for visibility
        drawable.setColor(Color.BLUE);
        drawable.showPath(true); // Enable path for testing
        display.addDrawable(drawable);

        // Control Panel to simulate movement and range changes
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 3));

        // Buttons for movement simulation
        JButton moveButton = new JButton("Move Drawable");
        JButton increaseAzimuthRange = new JButton("Increase Azimuth Range");
        JButton decreaseAzimuthRange = new JButton("Decrease Azimuth Range");
        JButton increaseElevationRange = new JButton("Increase Elevation Range");
        JButton decreaseElevationRange = new JButton("Decrease Elevation Range");

        // Action listeners for movement and range adjustments
        moveButton.addActionListener(e -> simulateMovement(drawable, display));
        increaseAzimuthRange.addActionListener(e -> adjustAzimuthRange(display, 10));
        decreaseAzimuthRange.addActionListener(e -> adjustAzimuthRange(display, -10));
        increaseElevationRange.addActionListener(e -> adjustElevationRange(display, 10));
        decreaseElevationRange.addActionListener(e -> adjustElevationRange(display, -10));

        // Add buttons to control panel
        controlPanel.add(moveButton);
        controlPanel.add(increaseAzimuthRange);
        controlPanel.add(decreaseAzimuthRange);
        controlPanel.add(increaseElevationRange);
        controlPanel.add(decreaseElevationRange);

        // Add the control panel to the frame
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    // Method to simulate movement of the BasicDrawable
    private static void simulateMovement(BasicDrawable drawable, PolarSpaceDisplay display) {
        Timer timer = new Timer(500, new AbstractAction() {
            private int step = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                switch (step % 4) {
                    case 0 -> {
                        drawable.setAzimuth(10); // Move to 10° azimuth
                        drawable.setElevation(-10); // Move to -10° elevation
                    }
                    case 1 -> {
                        drawable.setAzimuth(-10); // Move to -10° azimuth
                        drawable.setElevation(10); // Move to 10° elevation
                    }
                    case 2 -> {
                        drawable.setAzimuth(20); // Move to 20° azimuth
                        drawable.setElevation(5);  // Move to 5° elevation
                    }
                    case 3 -> {
                        drawable.setAzimuth(-20); // Move to -20° azimuth
                        drawable.setElevation(-5); // Move to -5° elevation
                    }
                }
                step++;
                display.repaint();
            }
        });
        timer.start();
    }

    // Methods to adjust azimuth and elevation range in PolarSpaceDisplay
    private static void adjustAzimuthRange(PolarSpaceDisplay display, int delta) {
        float newAzimuthRange = display.getAzimuthRange() + delta;
        display.setAzimuthRange(newAzimuthRange);
    }

    private static void adjustElevationRange(PolarSpaceDisplay display, int delta) {
        float newElevationRange = display.getElevationRange() + delta;
        display.setElevationRange(newElevationRange);
    }
}


