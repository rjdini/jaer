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
<<<<<<< HEAD
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
=======
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import com.inilabs.jaer.projects.tracker.TrackerManager;
>>>>>>> working

public class PolarSpaceGUI extends JFrame {

    private JPanel controlPanel;
<<<<<<< HEAD
=======
    
>>>>>>> working
    private PolarSpaceDisplay polarDisplay;
    private JSlider azimuthRangeSlider;
    private JSlider elevationRangeSlider;
    private JTextField azimuthHeadingField;
    private JTextField elevationHeadingField;
    private JButton closeButton;
    private JToggleButton linkSlidersButton;
    private boolean slidersLinked = false;

<<<<<<< HEAD
    public PolarSpaceGUI() {
        setTitle("Polar Space GUI");
        setSize(1600, 1200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize Polar Display
        polarDisplay = new PolarSpaceDisplay();
        polarDisplay.setPreferredSize(new Dimension(1600, 1000));
        add(polarDisplay, BorderLayout.CENTER);

        // Create Control Panel
        controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

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

        // Link sliders button
        linkSlidersButton = new JToggleButton("Link Sliders");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(linkSlidersButton, gbc);

        // Close button
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        gbc.gridy = 5;
        controlPanel.add(closeButton, gbc);

        // Add control panel to the south region
        add(controlPanel, BorderLayout.SOUTH);

        // Add listeners
        azimuthRangeSlider.addChangeListener(new RangeSliderListener());
        elevationRangeSlider.addChangeListener(new RangeSliderListener());
        azimuthHeadingField.addActionListener(e -> updateHeadingFromField());
        elevationHeadingField.addActionListener(e -> updateHeadingFromField());
        linkSlidersButton.addActionListener(e -> slidersLinked = linkSlidersButton.isSelected());

        pack();
        setVisible(true);
    }

=======
    private Timer updateTimer;
    
    
  //  private final PolarSpaceDisplay polarDisplay;
    private DefaultListModel<String> drawableListModel;
    private JList<String> drawableList;

    
    private final TrackerManager targetManager;

    // Default constructor
    public PolarSpaceGUI() {
        //super("Polar Space GUI");
        this.targetManager = null; // No TrackerManager assigned
        initializeGUI();
    }
    
    private void initializeGUI() {
     
         // Frame settings
       // setDefaultCloseOperation(JPanel );
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

        SwingUtilities.invokeLater(() -> polarDisplay.repaint());

        setVisible(true);
    }


>>>>>>> working
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
<<<<<<< HEAD
=======
    
    
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
    
>>>>>>> working
}
