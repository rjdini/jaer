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


package com.inilabs.jaer.projects.space3d;

import com.inilabs.jaer.gimbal.FieldOfView;
import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PolarSpaceGUI extends JFrame {

    private JPanel controlPanel;
    private PolarPane polarPane;
    private DrawManager drawManager;
    private JSlider azimuthRangeSlider;
    private JSlider elevationRangeSlider;
    private JTextField headingAzimuthField;
    private JTextField headingPitchField;

    public PolarSpaceGUI() {
        setTitle("Polar Space GUI");
        setSize(1600, 1200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        polarPane = new PolarPane();
        polarPane.setPreferredSize(new Dimension(1600, 1000));
        add(polarPane, BorderLayout.CENTER);

        drawManager = new DrawManager();

        setupControlPanel();

        // Initialize scale based on default slider values to ensure correct axis and tick spacing.
        notifyScaleChange();

        pack();
        setVisible(true);
    }

    private void setupControlPanel() {
        controlPanel = new JPanel(new FlowLayout());

        azimuthRangeSlider = new JSlider(10, 180, 90);
        azimuthRangeSlider.setPaintLabels(true);
        azimuthRangeSlider.setPaintTicks(true);
        azimuthRangeSlider.setMajorTickSpacing(30);
        azimuthRangeSlider.setMinorTickSpacing(10);
        controlPanel.add(new JLabel("Azimuth Range:"));
        controlPanel.add(azimuthRangeSlider);

        elevationRangeSlider = new JSlider(10, 180, 90);
        elevationRangeSlider.setPaintLabels(true);
        elevationRangeSlider.setPaintTicks(true);
        elevationRangeSlider.setMajorTickSpacing(30);
        elevationRangeSlider.setMinorTickSpacing(10);
        controlPanel.add(new JLabel("Elevation Range:"));
        controlPanel.add(elevationRangeSlider);

        headingAzimuthField = new JTextField("-10", 10); // Default heading azimuth
        headingPitchField = new JTextField("-20", 10);   // Default heading pitch
        controlPanel.add(new JLabel("Heading Azimuth:"));
        controlPanel.add(headingAzimuthField);
        controlPanel.add(new JLabel("Heading Pitch:"));
        controlPanel.add(headingPitchField);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        controlPanel.add(closeButton);

        add(controlPanel, BorderLayout.SOUTH);

        azimuthRangeSlider.addChangeListener(new ScaleChangeListener());
        elevationRangeSlider.addChangeListener(new ScaleChangeListener());
    }

    private void notifyScaleChange() {
    int azimuthRange = azimuthRangeSlider.getValue();
    int elevationRange = elevationRangeSlider.getValue();

    double newAzimuthScale = polarPane.getWidth() / (double) (2 * azimuthRange);
    double newElevationScale = polarPane.getHeight() / (double) (2 * elevationRange);

    // Only update and repaint if scales have changed to prevent infinite loop
    if (newAzimuthScale != polarPane.getAzimuthScale() || newElevationScale != polarPane.getElevationScale()) {
        polarPane.onScaleUpdate(newAzimuthScale, newElevationScale);
    }
}
    
    
    private double calculateAzimuthScale() {
    int azimuthRange = azimuthRangeSlider.getValue();
    return polarPane.getWidth() / (double) (2 * azimuthRange);
}

private double calculateElevationScale() {
    int elevationRange = elevationRangeSlider.getValue();
    return polarPane.getHeight() / (double) (2 * elevationRange);
}

   
    private class ScaleChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            notifyScaleChange();
        }
    }

    public void addDrawable(Drawable drawable) {
        drawManager.add(drawable.getKey(), drawable);
        polarPane.repaint(); // Trigger repaint to render the drawable
    }
    
    
 
    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        PolarSpaceGUI gui = new PolarSpaceGUI();

        // Initialize FieldOfView with specific yaw, pitch, and roll in degrees
        FieldOfView fov = FieldOfView.getInstance(20, -10, 15); // Pass yaw, pitch, roll directly in degrees
        gui.addDrawable(fov);

        // Initialize an AgentDrawable with azimuth and elevation in degrees, and size in pixels
        AgentDrawable agent = new AgentDrawable("agent1", 30, -20, Color.BLUE, 20);
        gui.addDrawable(agent);
    });
}
    
}




