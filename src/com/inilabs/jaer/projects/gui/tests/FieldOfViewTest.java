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

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.tracker.FieldOfView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FieldOfViewTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FieldOfViewTest::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Main JFrame
        JFrame frame = new JFrame("FieldOfView Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        // Create a PolarSpaceDisplay to render
        PolarSpaceDisplay display = PolarSpaceDisplay.getInstance();
        frame.add(display, BorderLayout.CENTER);

        // Create and configure FieldOfView instance
        FieldOfView fov = FieldOfView.getInstance();
        fov.setSize(10); // Set initial size for visibility
        fov.setColor(Color.RED);
        fov.showPath(true); // Enable path display for tracking movement

        // Explicitly add FieldOfView to display and repaint
        display.addDrawable(fov);
        display.repaint();

        // Debugging output to check if FieldOfView is in drawable list
        System.out.println("Attempting to add FieldOfView. Current drawables:");
        display.getDrawableNames().forEach(System.out::println);
        if (display.getDrawableNames().contains(fov.getKey())) {
            System.out.println("FieldOfView successfully added to display.");
        } else {
            System.out.println("FieldOfView not found in display drawable list.");
        }

        // Control Panel for FOV adjustments
        JPanel controlPanel = new JPanel(new GridLayout(2, 3));

        // Movement buttons
        JButton moveButton = new JButton("Move FOV");
        JButton rotateRollButton = new JButton("Rotate Roll");
        JButton increaseAzimuthRange = new JButton("Increase Azimuth Range");
        JButton decreaseAzimuthRange = new JButton("Decrease Azimuth Range");
        JButton togglePathButton = new JButton("Toggle Path");

        // Movement and adjustment listeners
        moveButton.addActionListener(e -> simulateMovement(fov, display));
        rotateRollButton.addActionListener(e -> simulateRollRotation(fov, display));
        increaseAzimuthRange.addActionListener(e -> adjustAzimuthRange(display, 10));
        decreaseAzimuthRange.addActionListener(e -> adjustAzimuthRange(display, -10));
        togglePathButton.addActionListener(e -> {
            fov.showPath(!fov.isPathVisible());
            display.repaint();
        });

        // Add buttons to control panel
        controlPanel.add(moveButton);
        controlPanel.add(rotateRollButton);
        controlPanel.add(increaseAzimuthRange);
        controlPanel.add(decreaseAzimuthRange);
        controlPanel.add(togglePathButton);

        // Add control panel to frame
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    // Method to simulate movement of FieldOfView across different azimuth and elevation values
    private static void simulateMovement(FieldOfView fov, PolarSpaceDisplay display) {
        Timer timer = new Timer(500, new AbstractAction() {
            private int step = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                switch (step % 4) {
                    case 0 -> {
                        fov.setAzimuth(10); // Move to 10° azimuth
                        fov.setElevation(-10); // Move to -10° elevation
                    }
                    case 1 -> {
                        fov.setAzimuth(-10); // Move to -10° azimuth
                        fov.setElevation(10); // Move to 10° elevation
                    }
                    case 2 -> {
                        fov.setAzimuth(20); // Move to 20° azimuth
                        fov.setElevation(5);  // Move to 5° elevation
                    }
                    case 3 -> {
                        fov.setAzimuth(-20); // Move to -20° azimuth
                        fov.setElevation(-5); // Move to -5° elevation
                    }
                }
                step++;
                display.repaint();
            }
        });
        timer.start();
    }

    // Method to simulate roll rotation for FieldOfView
    private static void simulateRollRotation(FieldOfView fov, PolarSpaceDisplay display) {
        Timer timer = new Timer(500, new AbstractAction() {
            private float roll = 0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                roll += 10f; // Rotate by 10 degrees each step
                fov.setAxialRoll(roll);
                display.repaint();
            }
        });
        timer.start();
    }

    // Method to adjust azimuth range in PolarSpaceDisplay
    private static void adjustAzimuthRange(PolarSpaceDisplay display, int delta) {
        float newAzimuthRange = display.getAzimuthRange() + delta;
        display.setAzimuthRange(newAzimuthRange);
    }
}
