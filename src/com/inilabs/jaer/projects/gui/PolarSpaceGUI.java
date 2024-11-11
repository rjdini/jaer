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
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;


public class PolarSpaceGUI extends JFrame {

    private JPanel controlPanel;
    
    private PolarSpaceDisplay polarDisplay;
    private JSlider azimuthRangeSlider;
    private JSlider elevationRangeSlider;
    private JTextField azimuthHeadingField;
    private JTextField elevationHeadingField;
    private JButton closeButton;
    private JToggleButton linkSlidersButton;
    private boolean slidersLinked = false;

    private final Timer updateTimer;
    
    
  //  private final PolarSpaceDisplay polarDisplay;
    private final DefaultListModel<String> drawableListModel;
    private final JList<String> drawableList;

    public PolarSpaceGUI() {
        super("Polar Space GUI");

         // Frame settings
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 1000);
        setLayout(new BorderLayout());
        
        // Initialize display panel for drawables
        polarDisplay = new PolarSpaceDisplay();
        add(polarDisplay, BorderLayout.CENTER);

        // Side panel setup to show drawable names
        drawableListModel = new DefaultListModel<>();
        drawableList = new JList<>(drawableListModel);
        JScrollPane scrollPane = new JScrollPane(drawableList);
        scrollPane.setPreferredSize(new Dimension(200, 0));
        
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.add(new JLabel("Drawable Objects"), BorderLayout.NORTH);
        sidePanel.add(scrollPane, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.WEST);

        // Control panel setup (omitted for brevity)
        PolarSpaceControlPanel controlPanel = new PolarSpaceControlPanel(polarDisplay, e -> dispose());
        controlPanel.setPreferredSize(new Dimension(1000, 200));
        add(controlPanel, BorderLayout.SOUTH);
        controlPanel.updateAzimuthRange();
        controlPanel.updateElevationRange();
        
        // Set up the timer to refresh the display at regular intervals
        int refreshRate = 30; // Refresh every 30 ms (~33 FPS)
        updateTimer = new Timer(refreshRate, e -> {
            polarDisplay.repaint(); // Repaint display at each timer tick
            updateDrawableList(); // Update the side panel with drawable names
        });
        updateTimer.start();

     
        setVisible(true);
    }


     public PolarSpaceDisplay getPolarSpaceDisplay() {
        return polarDisplay;
    }
    
    private void updateHeadingFromField() {
        try {
            int azimuthHeading = Integer.parseInt(azimuthHeadingField.getText());
            int elevationHeading = Integer.parseInt(elevationHeadingField.getText());
            polarDisplay.setHeading(azimuthHeading, elevationHeading);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid integer values for heading.");
        }
    }

    // Internal class to handle linked slider updates
    private class RangeSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (slidersLinked) {
                if (e.getSource() == azimuthRangeSlider) {
                    int value = azimuthRangeSlider.getValue();
                    elevationRangeSlider.setValue(value);
                    polarDisplay.setAzimuthRange(value);
                    polarDisplay.setElevationRange(value);
                } else if (e.getSource() == elevationRangeSlider) {
                    int value = elevationRangeSlider.getValue();
                    azimuthRangeSlider.setValue(value);
                    polarDisplay.setAzimuthRange(value);
                    polarDisplay.setElevationRange(value);
                }
            } else {
                if (e.getSource() == azimuthRangeSlider) {
                    polarDisplay.setAzimuthRange(azimuthRangeSlider.getValue());
                } else if (e.getSource() == elevationRangeSlider) {
                    polarDisplay.setElevationRange(elevationRangeSlider.getValue());
                }
            }
        }
    }

    // Update control values to match internal state
    public void setAzimuthRange(int range) {
        azimuthRangeSlider.setValue(range);
        polarDisplay.setAzimuthRange(range);
    }

    public void setElevationRange(int range) {
        elevationRangeSlider.setValue(range);
        polarDisplay.setElevationRange(range);
    }

    public void setAzimuthHeading(int heading) {
        azimuthHeadingField.setText(String.valueOf(heading));
        polarDisplay.setHeading(heading, getElevationHeading());
    }

    public void setElevationHeading(int heading) {
        elevationHeadingField.setText(String.valueOf(heading));
        polarDisplay.setHeading(getAzimuthHeading(), heading);
    }

    public int getAzimuthHeading() {
        return Integer.parseInt(azimuthHeadingField.getText());
    }

    public int getElevationHeading() {
        return Integer.parseInt(elevationHeadingField.getText());
    }
    
    
     // Method to stop the timer if needed
    public void stopScheduler() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
    
     // Method to update the list of drawable names in the side panel
    private void updateDrawableList() {
        List<String> currentDrawableNames = polarDisplay.getDrawableNames();
        drawableListModel.clear();
        for (String name : currentDrawableNames) {
            drawableListModel.addElement(name);
        }
    }
    
}
