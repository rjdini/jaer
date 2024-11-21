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

import com.inilabs.jaer.gimbal.GimbalBase;
import com.inilabs.jaer.projects.cog.SpatialAttention;
import javax.swing.*;
import java.awt.*;

public class PolarSpaceGUI extends JFrame {

    private PolarSpaceControlPanel controlPanel;
    public PolarSpaceDisplay polarDisplay;
    private DrawableDisplayPanel drawableDisplayPanel; // Re-added
    private BasicTestPanel testPanel;
    private Timer updateTimer;

    public PolarSpaceGUI() {
        initializeGUI();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void shutdown() {
        polarDisplay.shutdown();
        controlPanel.shutdown();
    }

    private void initializeGUI() {
        // Frame settings    
        setSize(1500, 1000); // Adjust overall frame size
        setLayout(new BorderLayout());

        // Initialize the PolarSpaceDisplay at the center
        polarDisplay = new PolarSpaceDisplay();
        polarDisplay.setPreferredSize(new Dimension(1200, 800)); // Ensure broader width
        polarDisplay.setMinimumSize(new Dimension(1000, 800));   // Minimum size constraints
        polarDisplay.setMaximumSize(new Dimension(1500, 1000));   // Maximum size constraints
        add(polarDisplay, BorderLayout.CENTER);

        // Create SpatialAttention instance
        GimbalBase gimbalBase = new GimbalBase(); // Replace with your actual GimbalBase instance
        SpatialAttention spatialAttention = new SpatialAttention(gimbalBase);

        // Register SpatialAttention as a KeyListener
        polarDisplay.setFocusable(true); // Ensure polarDisplay can receive focus
        polarDisplay.requestFocusInWindow(); // Request focus for polarDisplay
        polarDisplay.addKeyListener(spatialAttention);

        // Add toggle button for keyboard control
        // Add toggle button for keyboard control
        JButton keyboardControlButton = new JButton("Keyboard Control OFF");
        keyboardControlButton.setBackground(Color.RED);
        keyboardControlButton.setOpaque(true);
        keyboardControlButton.addActionListener(e -> {
            spatialAttention.toggleKeyboardControl();
            if (spatialAttention.isKeyboardControlEnabled()) {
                keyboardControlButton.setText("Keyboard Control ON");
                keyboardControlButton.setBackground(Color.GREEN);
                polarDisplay.setFocusable(true);
                polarDisplay.requestFocusInWindow(); // Ensure focus for keyboard input
            } else {
                keyboardControlButton.setText("Keyboard Control OFF");
                keyboardControlButton.setBackground(Color.RED);
            }
        });
        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        eastPanel.add(keyboardControlButton);

        // Initialize the control panel at the bottom
        controlPanel = new PolarSpaceControlPanel(polarDisplay, e -> dispose());
        add(controlPanel, BorderLayout.SOUTH);

        // Initialize the DrawableDisplayPanel at the left
        drawableDisplayPanel = new DrawableDisplayPanel(polarDisplay);
        drawableDisplayPanel.setPreferredSize(new Dimension(200, 800)); // Adjust width if needed
        add(drawableDisplayPanel, BorderLayout.WEST);
        controlPanel.add(eastPanel);

        // Set up a timer to refresh the display at regular intervals
        int refreshRate = 30; // Refresh every 30 ms (~33 FPS)
        updateTimer = new Timer(refreshRate, e -> {
            polarDisplay.repaint();
            if (testPanel != null) {
                testPanel.update();
            }
            drawableDisplayPanel.updateDrawableList(); // Update the list of drawables
        });
        updateTimer.start();
    }

    /**
     * Sets the test panel to the EAST region, replacing any existing one.
     *
     * @param testPanel The test panel to set.
     */
    public void setTestPanel(BasicTestPanel testPanel) {
        if (this.testPanel != null) {
            remove(this.testPanel);
        }
        this.testPanel = testPanel;
        if (this.testPanel != null) {
            this.testPanel.setGUICallBack(this);
            add(this.testPanel, BorderLayout.EAST);
        }
        revalidate();
        repaint();
    }

    /**
     * Stops the scheduler timer.
     */
    public void stopScheduler() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }

    /**
     * Gets the PolarSpaceDisplay.
     *
     * @return The PolarSpaceDisplay instance.
     */
    public PolarSpaceDisplay getPolarSpaceDisplay() {
        return polarDisplay;
    }

    /**
     * Gets the PolarSpaceControlPanel.
     *
     * @return The PolarSpaceControlPanel instance.
     */
    public PolarSpaceControlPanel getPolarSpaceControlPanel() {
        return controlPanel;
    }

    /**
     * Gets the DrawableDisplayPanel.
     *
     * @return The DrawableDisplayPanel instance.
     */
    public DrawableDisplayPanel getDrawableDisplayPanel() {
        return drawableDisplayPanel;
    }
}
