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
package com.inilabs.jaer.projects.environ.tests;
import com.inilabs.jaer.projects.environ.WaypointManager;
import com.inilabs.jaer.projects.environ.WaypointPanel;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;

public class WaypointPanelTest {

    private JFrame testFrame;
    private WaypointPanel waypointPanel;
    private WaypointManager waypointManager;

    @BeforeEach
    public void setUp() {
        // Initialize the WaypointManager
        waypointManager = WaypointManager.getInstance();

        // Create the WaypointPanel
        waypointPanel = new WaypointPanel(waypointManager);

        // Set up the JFrame containing the WaypointPanel
        SwingUtilities.invokeLater(() -> {
            testFrame = new JFrame("WaypointPanel Test");
            testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            testFrame.setLayout(new BorderLayout());
            testFrame.add(waypointPanel, BorderLayout.CENTER);
            testFrame.pack();
            testFrame.setVisible(true);
        });

        // Wait for the EDT to process the GUI setup
        try {
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        // Dispose of the JFrame to clean up resources
        if (testFrame != null) {
            SwingUtilities.invokeLater(() -> testFrame.dispose());
        }
    }

    @Test
    public void testAddWaypoint() {
        // Simulate user input
        SwingUtilities.invokeLater(() -> {
            waypointPanel.getNameField().setText("Test Waypoint");
            waypointPanel.getAzimuthSpinner().setValue(45.0);
            waypointPanel.getElevationSpinner().setValue(30.0);
            waypointPanel.getAddButton().doClick();
        });

        // Wait for the EDT to process the click event
        try {
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Verify that the waypoint was added
        Assertions.assertTrue(waypointManager.getAllWaypoints().containsKey("Test Waypoint"));
    }

    // Additional test methods can be added here
    
    public static void main(String[] args) {
        // Schedule the GUI creation to be run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Create the main application frame
            JFrame frame = new JFrame("Waypoint Panel Test with PolarSpaceDisplay");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Initialize the PolarSpaceDisplay
            PolarSpaceDisplay polarSpaceDisplay = PolarSpaceDisplay.getInstance();

            // Initialize the WaypointManager
            WaypointManager manager = WaypointManager.getInstance();

            // Create the WaypointPanel with the manager
            WaypointPanel waypointPanel = new WaypointPanel(manager);

            // Add the PolarSpaceDisplay to the center region
            frame.add(polarSpaceDisplay, BorderLayout.CENTER);

            // Add the WaypointPanel to the east region
            frame.add(waypointPanel, BorderLayout.EAST);

            // Set the frame size and make it visible
            frame.setSize(1200, 800); // Adjust size as needed
            frame.setLocationRelativeTo(null); // Center the frame on the screen
            frame.setVisible(true);
        });
    }
   
}

