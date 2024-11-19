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




package com.inilabs.jaer.projects.tracker.tests;

import com.inilabs.jaer.projects.gui.BasicTestPanel;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.TrackerManagerEngine;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import com.inilabs.jaer.projects.tracker.TestCluster;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeListener;

public class TrackerManagerEngineTestPanel extends BasicTestPanel {

    private static final ch.qos.logback.classic.Logger log = 
        (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(TrackerManagerEngineTestPanel.class);

    private float previousSendYaw = 0;
     private float previousSendRoll = 0;
     private float previousSendPitch = 0;
     private float currentSendYaw = 0;
     private float currentSendRoll = 0;
     private float currentSendPitch = 0;
    
      // Sliders for yaw, roll, and pitch
    private JSlider yawSlider;
    private JSlider rollSlider;
    private JSlider pitchSlider;
     
    private final TrackerManagerEngine trackerManagerEngine;
    private int numberClustersAdded = 5; // Default number of test clusters to add
    private static FieldOfView fov = FieldOfView.getInstance();
    private PolarSpaceDisplay polarDisplay;
    private PolarSpaceGUI gui = getGUICallBack();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public TrackerManagerEngineTestPanel(TrackerManagerEngine engine) {
        this.trackerManagerEngine = engine;
        initUI();
        this.pcs.addPropertyChangeListener(fov);
        initializeFieldOfView();
    }

    private void initUI() {
        setPreferredSize(new Dimension(300, 400));
        setLayout(new BorderLayout());

        // Add existing functionality
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 1));
        mainPanel.add(createButton("Add Test Clusters", e -> addTestClusters()));
        mainPanel.add(createButton("Show Best Agents", e -> showBestAgents()));
        mainPanel.add(createButton("Update Display", e -> updateDisplay()));
        mainPanel.add(createButton("Reset Sliders", e -> resetSliders())); // Reset button
        add(mainPanel, BorderLayout.NORTH);

        // Add sliders for yaw, roll, and pitch
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(3, 2)); // 3 sliders in a grid
        yawSlider = createSlider("Yaw", sliderPanel, -90, 90, 0, e -> updateGimbalPose());
        rollSlider = createSlider("Roll", sliderPanel, -90, 90, 0, e -> updateGimbalPose());
        pitchSlider = createSlider("Pitch", sliderPanel, -90, 90, 0, e -> updateGimbalPose());
        add(sliderPanel, BorderLayout.SOUTH);
       
    }
    
     private JSlider createSlider(String label, JPanel parent, int min, int max, int initial, ChangeListener listener) {
        JLabel sliderLabel = new JLabel(label);
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);

        slider.addChangeListener(listener);
        slider.setMajorTickSpacing((max - min) / 4);
        slider.setMinorTickSpacing((max - min) / 20);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        parent.add(sliderLabel);
        parent.add(slider);

        return slider;
    }
     
     
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        return button;
    }
    
    private void updateGimbalPose() {
        // Read slider values
        float yaw = yawSlider.getValue();
        float roll = rollSlider.getValue();
        float pitch = pitchSlider.getValue();

        // Call setGimbalPoseDirect with the current values
        setGimbalPoseDirect(yaw, roll, pitch);

        // Log the update for debugging
        System.out.printf("Updated Gimbal Pose: Yaw=%.2f, Roll=%.2f, Pitch=%.2f%n", yaw, roll, pitch);
    }
    
    private void resetSliders() {
        // Reset sliders to default values
        yawSlider.setValue(0); // Default yaw
        rollSlider.setValue(0);  // Default roll
        pitchSlider.setValue(0); // Default pitch

        // Update gimbal pose with default values
        updateGimbalPose();
    }
 
    
    @Override
    public void setGUICallBack(PolarSpaceGUI gui) {
        super.setGUICallBack(gui);

        // Initialize FieldOfView when GUI callback is set
        initializeFieldOfView();
    }
    
     private void initializeFieldOfView() {
        PolarSpaceGUI gui = getGUICallBack();
        if (gui != null) {
            PolarSpaceDisplay display = gui.getPolarSpaceDisplay();
            FieldOfView fov = FieldOfView.getInstance();
            gui.polarDisplay.addDrawable(fov);
            gui.polarDisplay.repaint(); 
            gui.polarDisplay.setVisible(true);
        
            fov.setPose(0, 0, 0);
            }
     }

  public void  setGimbalPoseDirect( float yaw, float roll, float pitch) {       
          float [] previousSendPose = {previousSendYaw, previousSendRoll, previousSendPitch};
           currentSendYaw = yaw;
           currentSendRoll = roll;
           currentSendPitch = pitch;         
    //      rs4controller.setPose(currentSendYaw, currentSendRoll, currentSendPitch); // PanTilt does not consider Roll 
           
           previousSendYaw = currentSendYaw;
           previousSendRoll = currentSendRoll;
           previousSendPitch = currentSendPitch;
           float [] newSendPose = {currentSendYaw, currentSendRoll,  currentSendPitch};
    //        this.pcs.firePropertyChange("SendGimbalPose", previousSendPose, newSendPose);  
           // using fetch to control FOV, because that is where the gimbal really is now. (SendGimbalPose is wher it will be in future)
            this.pcs.firePropertyChange("FetchedGimbalPose", previousSendPose, newSendPose);    
        //    log.info("SendGimbalPoseDirect (y,r,p) " + currentSendYaw + ",  " + currentSendRoll + ", " + currentSendPitch );
    }
    
 
    /**
     * Adds random test clusters to the engine.
     */
private void addTestClusters() {
     
    
// Generate random test clusters
    LinkedList<TestCluster> clusters = new LinkedList<>();
    // Generate the test pixels in Polar coords, because we need to place them into the FOV,
    // whose pose is known.
    // once we have polars, we translate to pixels that are pose independent.
    for (int i = 0; i < numberClustersAdded; i++) {
        float azi = (float) (Math.random() * 20 - 10) ; // Random azimuth [-30, 30]
        float ele = (float) (Math.random() * 20 - 10) ; // Random elevation [-30, 30]
          
     //   TestCluster testCluster = new TestCluster(azimuth, elevation);
        float pixelX = fov.getPixelsAtYaw( azi  +  fov.getPose()[0] );
        float pixelY = fov.getPixelsAtPitch( ele + fov.getPose()[2] );
    
         TestCluster testCluster = new TestCluster(new Point2D.Float(pixelX, pixelY));
         
         log.info("Test cluster: ID={}, Azimuth={}, Elevation={}, Location.x {}, Location.y {}", 
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation(),
                 testCluster.getLocation().getX(), testCluster.getLocation().getY());

        clusters.add(testCluster);

        log.info("Added test cluster: ID={}, Azimuth={}, Elevation={}", 
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());
    }
  
    // Update the engine with these test clusters
    trackerManagerEngine.updateTestClusterList(clusters);

    // Repaint the PolarSpaceDisplay
    getGUICallBack().getPolarSpaceDisplay().repaint();
    log.info("Added {} test clusters.", numberClustersAdded);
}

   
    /**
     * Displays the best tracker agents.
     */
    private void showBestAgents() {
        List<TrackerAgentDrawable> bestAgents = trackerManagerEngine.getBestTrackerAgentList();

        if (bestAgents.isEmpty()) {
            System.out.println("No agents available.");
            return;
        }

        System.out.println("Best Tracker Agents:");
        bestAgents.forEach(agent -> System.out.printf("Agent Key: %s, Support Quality: %.2f%n",
                                                      agent.getKey(), agent.getSupportQuality()));
    }

    /**
     * Refreshes the display in the PolarSpaceGUI.
     */
    private void updateDisplay() {
        PolarSpaceGUI gui = getGUICallBack();
        if (gui != null) {
            gui.getPolarSpaceDisplay().repaint();
            log.info("Display updated.");
        }
    }
}
