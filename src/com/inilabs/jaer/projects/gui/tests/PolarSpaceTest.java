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

import com.inilabs.jaer.projects.gui.PolarSpaceControlPanel;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

public class PolarSpaceTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize the GUI components
                PolarSpaceGUI polarSpaceGUI = new PolarSpaceGUI();
                polarSpaceGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                polarSpaceGUI.setTitle("Polar Space GUI - Test Environment");
                polarSpaceGUI.setVisible(true);

                // Access the control panel and add mock interactions
                PolarSpaceControlPanel controlPanel = polarSpaceGUI.getPolarSpaceControlPanel();

                // Simulate slider adjustments (waypoint azimuth/elevation)
                simulateSliderAdjustments(controlPanel);

                // Simulate button interactions
                simulateButtonClicks(controlPanel);

                // Center the window for convenience
                polarSpaceGUI.setLocationRelativeTo(null);

                System.out.println("PolarSpaceGUI launched successfully for testing.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Simulates interactions with sliders on the control panel.
     */
    private static void simulateSliderAdjustments(PolarSpaceControlPanel controlPanel) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Adjust azimuth waypoint slider
                JSlider azimuthSlider = controlPanel.azimuthWaypointSlider;
                azimuthSlider.setValue(45); // Set azimuth to 45 degrees

                // Adjust elevation waypoint slider
                JSlider elevationSlider = controlPanel.elevationWaypointSlider;
                elevationSlider.setValue(15); // Set elevation to 15 degrees

                System.out.println("Sliders adjusted: Azimuth = " + azimuthSlider.getValue()
                        + ", Elevation = " + elevationSlider.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Simulates button clicks on the control panel.
     */
    private static void simulateButtonClicks(PolarSpaceControlPanel controlPanel) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Simulate 'Reset Waypoint' button click
                JButton resetButton = controlPanel.resetWaypointButton;
                resetButton.doClick();

                // Simulate 'Gimbal Tracking ON' toggle
                JButton gimbalButton = controlPanel.createGimbalControlPanel().getComponent(0) instanceof JButton
                        ? (JButton) controlPanel.createGimbalControlPanel().getComponent(0)
                        : null;

                if (gimbalButton != null) {
                    gimbalButton.doClick();
                    System.out.println("Gimbal tracking button toggled.");
                }

                System.out.println("Button interactions simulated successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
