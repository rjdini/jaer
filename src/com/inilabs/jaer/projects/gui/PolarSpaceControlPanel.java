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
private JSlider azimuthWaypointSlider;
private JSlider elevationWaypointSlider;
private JSlider supportQualitySlider;

// Reset button
private JButton resetHeadingButton;
private JButton resetWaypointButton;

private float defaultWaypointAzimuth;
private float defaultWaypointElevation;

private  static SpatialAttention spatialAttention  = SpatialAttention.getInstance();


public PolarSpaceControlPanel(PolarSpaceDisplay polarDisplay, ActionListener closeAction) {
    this.polarDisplay = polarDisplay;
  //  setLayout(new BorderLayout(5, 5)); // Use BorderLayout for main layout
    AgentLogger.initialize();

    // Register SpatialAttention as a KeyListener
    polarDisplay.setFocusable(true); // Ensure polarDisplay can receive focus
    polarDisplay.requestFocusInWindow(); // Request focus for polarDisplay
    polarDisplay.addKeyListener(spatialAttention);

    // West Panel
    JPanel westPanel = new JPanel(new BorderLayout(5, 5)); // Use BorderLayout    
    westPanel.add(createHeadingGroupPanel(), BorderLayout.NORTH);
    westPanel.add(createRangeSettingsPanel(), BorderLayout.SOUTH);

    // Center Panel
    JPanel centerPanel = new JPanel(new BorderLayout(5, 5)); // Use BorderLayout
    JPanel spatialAttentionPanel = createSpatialAttentionGroupPanel();
    spatialAttentionPanel.setPreferredSize(new Dimension(400, 200)); // Ensure a visible size
    JPanel waypointPanel = createWaypointPanel();
    waypointPanel.setPreferredSize(new Dimension(400, 200));
    centerPanel.add(spatialAttentionPanel, BorderLayout.NORTH);
    centerPanel.add(waypointPanel, BorderLayout.SOUTH);
    centerPanel.setBackground(Color.RED);

    // East Panel: Logging Controls and Keyboard Control
    JPanel eastPanel = new JPanel(new BorderLayout(5, 5)); // Use BorderLayout for better organization
    // Create a container for logging and gimbal controls
    JPanel loggingAndGimbalPanel = new JPanel(new BorderLayout(5, 5));
    loggingAndGimbalPanel.add(createLoggingPanel(), BorderLayout.NORTH);
    loggingAndGimbalPanel.add(createGimbalControlPanel(), BorderLayout.CENTER);
    loggingAndGimbalPanel.add(createKeyboardControlPanel(), BorderLayout.SOUTH);
    // Add the combined panel and keyboard controls to the east panel
    eastPanel.add(loggingAndGimbalPanel, BorderLayout.NORTH);
    eastPanel.add(createButtonPanel(closeAction), BorderLayout.SOUTH);

    // Add panels to the layout
    add(westPanel, BorderLayout.WEST);   // West: Range sliders
    add(centerPanel, BorderLayout.CENTER); // Center: Spatial attention group
    add(eastPanel, BorderLayout.EAST);   // East: Logging controls

    // Synchronize sliders if linked
    synchronizeSliders();
}



private JPanel createSpatialAttentionGroupPanel() {
    // Create the main panel with a vertical BoxLayout
    JPanel spatialAttentionGroupPanel = new JPanel();
    spatialAttentionGroupPanel.setLayout(new BoxLayout(spatialAttentionGroupPanel, BoxLayout.Y_AXIS));
    spatialAttentionGroupPanel.setBorder(BorderFactory.createTitledBorder("Attention Controls"));
    spatialAttentionGroupPanel.setPreferredSize(new Dimension(400, 200));

    // Create a sub-panel for the Support Quality slider
    JPanel supportQualityPanel = new JPanel(new BorderLayout(5, 5));
    JLabel supportQualityLabel = new JLabel("Support Quality:", SwingConstants.RIGHT);
    supportQualitySlider = createSlider(0, 100, (int)spatialAttention.getSupportQualityThreshold(), e -> updateSupportQuality());

    // Configure the slider
    supportQualitySlider.setMajorTickSpacing(10); // 
    supportQualitySlider.setMinorTickSpacing(2);  // 
    supportQualitySlider.setPaintTicks(true);
    supportQualitySlider.setPaintLabels(true);

    // Add the label and slider to the sub-panel
    supportQualityPanel.add(supportQualityLabel, BorderLayout.WEST);
    supportQualityPanel.add(supportQualitySlider, BorderLayout.CENTER);

    // Add the sub-panel to the main panel
    spatialAttentionGroupPanel.add(supportQualityPanel);

    return spatialAttentionGroupPanel;
}


private JPanel createWaypointPanel() {
    // Main panel with vertical BoxLayout
    JPanel wayPointPanel = new JPanel();
    wayPointPanel.setLayout(new BoxLayout(wayPointPanel, BoxLayout.Y_AXIS));
    wayPointPanel.setBorder(BorderFactory.createTitledBorder("Waypoint Controls"));

    wayPointPanel.setPreferredSize(new Dimension(600, 200));
      
    // Azimuth Waypoint Slider
    JPanel azimuthPanel = new JPanel(new BorderLayout(5, 5));
    azimuthPanel.add(new JLabel("Azimuth:", SwingConstants.RIGHT), BorderLayout.WEST);
    defaultWaypointAzimuth = spatialAttention.getWaypointAzimuth();
    azimuthWaypointSlider = createSlider(-180, 180, (int)defaultWaypointAzimuth,  e -> updateAzimuthWaypoint());
    azimuthWaypointSlider.setMajorTickSpacing(60); // Major ticks every 20 units
    azimuthWaypointSlider.setMinorTickSpacing(30);
    azimuthPanel.add(azimuthWaypointSlider, BorderLayout.CENTER);
    wayPointPanel.add(azimuthPanel);
    
    // Elevation Waypoint Slider
    JPanel elevationPanel = new JPanel(new BorderLayout(5, 5));
    elevationPanel.add(new JLabel("Elevation:", SwingConstants.RIGHT), BorderLayout.WEST);
    defaultWaypointElevation = spatialAttention.getWaypointElevation();
    elevationWaypointSlider = createSlider(-90, 90,  (int)defaultWaypointElevation, e -> updateElevationWaypoint());
    elevationWaypointSlider.setMajorTickSpacing(30); // Major ticks every 20 units
    elevationWaypointSlider.setMinorTickSpacing(10);
    elevationPanel.add(elevationWaypointSlider, BorderLayout.CENTER);
    wayPointPanel.add(elevationPanel);

    // Reset Button Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    resetWaypointButton = new JButton("Reset Azimuth/Elevation");
    resetWaypointButton.addActionListener(e -> resetWaypoint());
    buttonPanel.add(resetWaypointButton);
    wayPointPanel.add(buttonPanel);

    return wayPointPanel;
}



private JPanel createHeadingGroupPanel() {
    // Main panel with vertical BoxLayout
    JPanel headingGroupPanel = new JPanel();
    headingGroupPanel.setLayout(new BoxLayout(headingGroupPanel, BoxLayout.Y_AXIS));
    headingGroupPanel.setBorder(BorderFactory.createTitledBorder("Heading Controls"));

    headingGroupPanel.setPreferredSize(new Dimension(600, 200));
      
    // Azimuth Heading Slider
    JPanel azimuthPanel = new JPanel(new BorderLayout(5, 5));
    azimuthPanel.add(new JLabel("Azimuth:", SwingConstants.RIGHT), BorderLayout.WEST);
    azimuthHeadingSlider = createSlider(-180, 180, 0, e -> updateAzimuthHeading());
    azimuthHeadingSlider.setMajorTickSpacing(60); // Major ticks every 20 units
    azimuthHeadingSlider.setMinorTickSpacing(10);
    azimuthPanel.add(azimuthHeadingSlider, BorderLayout.CENTER);
    headingGroupPanel.add(azimuthPanel);
    
    // Elevation Heading Slider
    JPanel elevationPanel = new JPanel(new BorderLayout(5, 5));
    elevationPanel.add(new JLabel("Elevation:", SwingConstants.RIGHT), BorderLayout.WEST);
    elevationHeadingSlider = createSlider(-90, 90, 0, e -> updateElevationHeading());
    elevationHeadingSlider.setMajorTickSpacing(30); // Major ticks every 20 units
    elevationHeadingSlider.setMinorTickSpacing(10);
    elevationPanel.add(elevationHeadingSlider, BorderLayout.CENTER);
    headingGroupPanel.add(elevationPanel);

    // Reset Button Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    resetHeadingButton = new JButton("Reset Azimuth/Elevation");
    resetHeadingButton.addActionListener(e -> resetHeading());
    buttonPanel.add(resetHeadingButton);
    headingGroupPanel.add(buttonPanel);

    return headingGroupPanel;
}


private JPanel createRangeSettingsPanel() {
    // Create the main panel with a vertical BoxLayout
    JPanel settingsPanel = new JPanel();
    settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
    settingsPanel.setBorder(BorderFactory.createTitledBorder("Range Controls"));

    settingsPanel.setPreferredSize(new Dimension(600, 200));
    
    // Azimuth Range Slider
    JPanel azimuthPanel = new JPanel(new BorderLayout(5, 5));
    azimuthPanel.add(new JLabel("Azimuth", SwingConstants.RIGHT), BorderLayout.WEST);
    azimuthRangeSlider = createSlider(0, 150, 30, e -> updateAzimuthRange());
    azimuthPanel.add(azimuthRangeSlider, BorderLayout.CENTER);
    settingsPanel.add(azimuthPanel);

    // Elevation Range Slider
    JPanel elevationPanel = new JPanel(new BorderLayout(5, 5));
    elevationPanel.add(new JLabel("Elevation", SwingConstants.RIGHT), BorderLayout.WEST);
    elevationRangeSlider = createSlider(0, 90, 30, e -> updateElevationRange());
    elevationPanel.add(elevationRangeSlider, BorderLayout.CENTER);
    settingsPanel.add(elevationPanel);

    // Link Sliders Button
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    linkSlidersButton = createToggleButton("Link Sliders", 120, 25);
    linkSlidersButton.addActionListener(e -> slidersLinked = linkSlidersButton.isSelected());
    buttonPanel.add(linkSlidersButton);
    settingsPanel.add(buttonPanel);

    return settingsPanel;
}

private JPanel createButtonPanel(ActionListener closeAction) {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
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


private void updateSupportQuality() {
    double supportQuality = supportQualitySlider.getValue();
    spatialAttention.setSupportQualityThreshold(supportQuality);
}
    


   // New methods to update headings
private void updateAzimuthWaypoint() {
    float azimuth = (float)azimuthHeadingSlider.getValue()   ;
    spatialAttention.setWaypointAzimuth(azimuth); // Update display
}

private void updateElevationWaypoint() {
    float elevation = (float)elevationHeadingSlider.getValue();
    spatialAttention.setWaypointElevation(elevation); // Update display
}


// Method to reset azimuth and elevation to 0
private void resetWaypoint() {
  azimuthWaypointSlider.setValue((int)defaultWaypointAzimuth);
  elevationWaypointSlider.setValue((int)defaultWaypointElevation);
   spatialAttention.setWaypoint(defaultWaypointAzimuth, defaultWaypointElevation); // Reset heading in the display
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
private void resetHeading() {
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
