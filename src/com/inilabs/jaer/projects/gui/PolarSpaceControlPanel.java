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

import com.inilabs.jaer.gimbal.GimbalBase;
import com.inilabs.jaer.projects.cog.SpatialAttention;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.inilabs.jaer.projects.logging.AgentLogger;

public class PolarSpaceControlPanel extends JPanel {

    private JTextField azimuthHeadingField;
    private JTextField elevationHeadingField;
    private JSlider azimuthRangeSlider;
    private JSlider elevationRangeSlider;
    private JToggleButton linkSlidersButton;
    private JToggleButton pathToggleButton;
    private JButton closeButton;
    private JButton startLoggingButton;
    private JButton stopLoggingButton;
    private final PolarSpaceDisplay polarDisplay;
    private boolean slidersLinked = true;
    
    // Sliders for azimuth and elevation headings
private JSlider azimuthHeadingSlider;
private JSlider elevationHeadingSlider;

// Reset button
private JButton resetHeadingButton;
private  static SpatialAttention spatialAttention  = SpatialAttention.getInstance();



public PolarSpaceControlPanel(PolarSpaceDisplay polarDisplay, ActionListener closeAction) {
    this.polarDisplay = polarDisplay;
    setLayout(new BorderLayout(10, 10)); // Use BorderLayout for main layout
    AgentLogger.initialize();

     // Register SpatialAttention as a KeyListener
        polarDisplay.setFocusable(true); // Ensure polarDisplay can receive focus
        polarDisplay.requestFocusInWindow(); // Request focus for polarDisplay
        polarDisplay.addKeyListener(spatialAttention);
        
    // Center Panel: Heading Controls (Sliders and Reset Button)
    JPanel headingGroupPanel = createHeadingGroupPanel();

    // West Panel: Azimuth and Elevation Range Sliders
    JPanel rangeSettingsPanel = createRangeSettingsPanel();

    // South Panel: Control Buttons
    JPanel buttonPanel = createButtonPanel(closeAction);

    
     // East Panel: Logging Controls and Keyboard Control
    JPanel eastPanel = new JPanel(new BorderLayout(5, 5)); // Use BorderLayout for better organization
    eastPanel.add(createLoggingPanel(), BorderLayout.NORTH); // Logging controls at the top
    eastPanel.add(createGimbalControlPanel(), BorderLayout.CENTER); // Logging controls at the top
    eastPanel.add(createKeyboardControlPanel(), BorderLayout.SOUTH); // Keyboard control at the bottom

    // Add panels to the layout
    add(headingGroupPanel, BorderLayout.NORTH); // Center: Heading group panel
    add(rangeSettingsPanel, BorderLayout.WEST);  // West: Range sliders
    add(buttonPanel, BorderLayout.SOUTH);        // South: Control buttons
    add(eastPanel, BorderLayout.EAST);        // East: Logging controls

    // Synchronize sliders if linked
    synchronizeSliders();
}


private JPanel createHeadingGroupPanel() {
    JPanel headingGroupPanel = new JPanel(new GridLayout(0, 2, 10, 5));
    headingGroupPanel.setBorder(BorderFactory.createTitledBorder("Heading Controls"));

    // Azimuth Heading Slider
    JLabel azimuthHeadingLabel = new JLabel("Azimuth:", SwingConstants.RIGHT);
    azimuthHeadingSlider = createSlider(-180, 180, 0, e -> updateAzimuthHeading());

    // Elevation Heading Slider
    JLabel elevationHeadingLabel = new JLabel("Elevation:", SwingConstants.RIGHT);
    elevationHeadingSlider = createSlider(-90, 90, 0, e -> updateElevationHeading());

    // Reset Button
    resetHeadingButton = new JButton("Reset Azimuth/Elevation");
    resetHeadingButton.addActionListener(e -> resetAzimuthElevation());

    // Add components to panel
    headingGroupPanel.add(azimuthHeadingLabel);
    headingGroupPanel.add(azimuthHeadingSlider);
    headingGroupPanel.add(elevationHeadingLabel);
    headingGroupPanel.add(elevationHeadingSlider);
    headingGroupPanel.add(new JLabel()); // Placeholder for alignment
    headingGroupPanel.add(resetHeadingButton);

    return headingGroupPanel;
}

private JPanel createRangeSettingsPanel() {
    JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 10, 5));

    // Azimuth Range Slider
    settingsPanel.add(new JLabel("Azimuth Range:", SwingConstants.RIGHT));
    azimuthRangeSlider = createSlider(0, 150, 30, e -> updateAzimuthRange());
    settingsPanel.add(azimuthRangeSlider);

    // Elevation Range Slider
    settingsPanel.add(new JLabel("Elevation Range:", SwingConstants.RIGHT));
    elevationRangeSlider = createSlider(0, 90, 30, e -> updateElevationRange());
    settingsPanel.add(elevationRangeSlider);

    return settingsPanel;
}

private JPanel createButtonPanel(ActionListener closeAction) {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    // Link Sliders Button
    linkSlidersButton = createToggleButton("Link Sliders", 120, 25);
    linkSlidersButton.addActionListener(e -> slidersLinked = linkSlidersButton.isSelected());
    buttonPanel.add(linkSlidersButton);

    // Path Toggle Button
    pathToggleButton = createToggleButton("Show Paths", 120, 25);
    pathToggleButton.addActionListener(new PathToggleListener());
    buttonPanel.add(pathToggleButton);

    // Close Button
    closeButton = new JButton("Close");
    closeButton.setPreferredSize(new Dimension(80, 25));
    closeButton.addActionListener(closeAction);
    buttonPanel.add(closeButton);

    return buttonPanel;
}


private JPanel createGimbalControlPanel() {
    JPanel gimbalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    // Keyboard Control Toggle Button
    JButton gimbalControlButton = new JButton("Gimbal Tracking ON");
    gimbalControlButton.setBackground(Color.GREEN);
    gimbalControlButton.setOpaque(true);
    spatialAttention.setEnableGimbalPose(true);
    gimbalControlButton.addActionListener(e -> {
        if (!spatialAttention.isEnableGimbalPose()) {
            spatialAttention.setEnableGimbalPose(true);
            gimbalControlButton.setText("Gimbal Tracking ON");
            gimbalControlButton.setBackground(Color.GREEN);
        } else {
            spatialAttention.setEnableGimbalPose(false);
            gimbalControlButton.setText("Gimbal Tracking OFF");
            gimbalControlButton.setBackground(Color.RED);
        }
    });

    gimbalPanel.add(gimbalControlButton);
    return gimbalPanel;
}


private JPanel createKeyboardControlPanel() {
    JPanel keyboardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

    // Keyboard Control Toggle Button
    JButton keyboardControlButton = new JButton("Keyboard Control OFF");
    keyboardControlButton.setBackground(Color.RED);
    keyboardControlButton.setOpaque(true);
    spatialAttention.setEnableKeyboardControl(false);
    keyboardControlButton.addActionListener(e -> {
   
        if (!spatialAttention.isEnableKeyboardControl()) {
            spatialAttention.setEnableKeyboardControl(true);
            keyboardControlButton.setText("Keyboard Control ON");
            keyboardControlButton.setBackground(Color.GREEN);
            polarDisplay.setFocusable(true);
            polarDisplay.requestFocusInWindow(); // Ensure focus for keyboard input
        } else {
            spatialAttention.setEnableKeyboardControl(false);
            keyboardControlButton.setText("Keyboard Control OFF");
            keyboardControlButton.setBackground(Color.RED);
        }
    });

    keyboardPanel.add(keyboardControlButton);
    return keyboardPanel;
}



















private JPanel createLoggingPanel() {
    JPanel loggingPanel = new JPanel(new GridLayout(2, 1, 5, 5));

    // Start Logging Button
    startLoggingButton = new JButton("Start Logging");
    startLoggingButton.addActionListener(e -> startLogging());
    loggingPanel.add(startLoggingButton);

    // Stop Logging Button
    stopLoggingButton = new JButton("Stop Logging");
    stopLoggingButton.addActionListener(e -> stopLogging());
    loggingPanel.add(stopLoggingButton);

    return loggingPanel;
}

private void synchronizeSliders() {
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



    
    // New methods to update headings
private void updateAzimuthHeading() {
    float azimuth = (float)azimuthHeadingSlider.getValue()   ;
    polarDisplay.setAzimuthHeading(azimuth); // Update display
}

private void updateElevationHeading() {
    float elevation = (float)elevationHeadingSlider.getValue();
    polarDisplay.setElevationHeading(elevation); // Update display
}

// Method to reset azimuth and elevation to 0
private void resetAzimuthElevation() {
    azimuthHeadingSlider.setValue(0);
    elevationHeadingSlider.setValue(0);
    polarDisplay.setHeading(0, 0); // Reset heading in the display
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
        AgentLogger.setGUILoggingEnabled(true);
    }

    private void stopLogging() {
        // Implement logging stop logic
        AgentLogger.setGUILoggingEnabled(false);
    }

    private class PathToggleListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            polarDisplay.showPaths(pathToggleButton.isSelected());
        }
    }

    public void shutdown() {
        AgentLogger.shutdown();
    }
}
