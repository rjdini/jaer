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

import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.RCTClusterAdapter;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;

public class RCTClusterAdapterTest {

    private static final Logger logger = LoggerFactory.getLogger(RCTClusterAdapterTest.class);

    public static void main(String[] args) {
      //  testInitializationWithCluster();
        testInitializationWithTestLocation();
        testVisibilityToggle();
        testClusterLocationToAzimuthElevation();
    }

//    private static void testInitializationWithCluster() {
//        logger.info("Testing Initialization with RectangularClusterTracker.Cluster...");
//
//        FieldOfView fov = FieldOfView.getInstance();
//        fov.setChipDimensions(640, 480);
//        fov.setPose(0f, 0f, 0f);
//
//        RectangularClusterTracker.Cluster cluster = new RectangularClusterTracker.Cluster(new Point2D.Float(320, 240));
//        RCTClusterAdapter adapter = new RCTClusterAdapter(cluster);
//
//        if (adapter.getLocation().equals(cluster.getLocation())) {
//            logger.info("Success: Cluster location correctly encapsulated.");
//        } else {
//            logger.error("Failure: Cluster location mismatch. Expected: {}, Found: {}", cluster.getLocation(), adapter.getLocation());
//        }
//    }

    private static void testInitializationWithTestLocation() {
        logger.info("Testing Initialization with Test Location...");

        FieldOfView fov = new FieldOfView();
        fov.setChipDimensions(640, 480);
        fov.setPose(0f, 0f, 0f);

        Point2D.Float testLocation = new Point2D.Float(200, 150);
        RCTClusterAdapter adapter = new RCTClusterAdapter();
        adapter.setLocation(testLocation);

        if (adapter.getLocation().equals(testLocation)) {
            logger.info("Success: Test location correctly encapsulated.");
        } else {
            logger.error("Failure: Test location mismatch. Expected: {}, Found: {}", testLocation, adapter.getLocation());
        }
    }

    private static void testVisibilityToggle() {
        logger.info("Testing Visibility Toggle...");

        RCTClusterAdapter adapter = new RCTClusterAdapter();
        adapter.setIsVisible(false);

        if (!adapter.isVisible()) {
            logger.info("Success: Visibility toggled to false.");
        } else {
            logger.error("Failure: Visibility toggle to false failed.");
        }

        adapter.setIsVisible(true);
        if (adapter.isVisible()) {
            logger.info("Success: Visibility toggled to true.");
        } else {
            logger.error("Failure: Visibility toggle to true failed.");
        }
    }

    
    private static void testClusterLocationToAzimuthElevation() {
    logger.info("Testing Cluster Location to Azimuth/Elevation Mapping...");

    FieldOfView fov = new FieldOfView();
    fov.setChipDimensions(640, 480);
    fov.setFOVX(20.0f); // Horizontal FOV
    fov.setFOVY(15.0f); // Vertical FOV

    // Define test cases with explicit calculations
    Object[][] testCases = {
        {new Point2D.Float(320, 240), 0f, 0f, 0f, 0f},     // Center
        {new Point2D.Float(0, 240), 0f, 0f, -10f, 0f},     // Left edge
        {new Point2D.Float(640, 240), 0f, 0f, 10f, 0f},    // Right edge
        {new Point2D.Float(320, 480), 0f, 0f, 0f, 7.5f},   // Top edge
        {new Point2D.Float(320, 0), 0f, 0f, 0f, -7.5f},    // Bottom edge
        {new Point2D.Float(160, 360), 0f, 0f, -5f, 3.75f}, // Random point top-left quadrant
        {new Point2D.Float(480, 120), 0f, 0f, 5f, -3.75f}  // Random point bottom-right quadrant
    };

    for (Object[] testCase : testCases) {
        Point2D.Float location = (Point2D.Float) testCase[0];
        float poseYaw = (float) testCase[1];
        float posePitch = (float) testCase[2];
        float expectedAzimuth = (float) testCase[3];
        float expectedElevation = (float) testCase[4];

        // Set pose
        fov.setPose(poseYaw, posePitch, 0f);

        // Create and set the adapter location
        RCTClusterAdapter adapter = new RCTClusterAdapter();
        adapter.setLocation(location);

        // Validate results
        float actualAzimuth = adapter.getAzimuth();
        float actualElevation = adapter.getElevation();

        if (Math.abs(actualAzimuth - expectedAzimuth) < 0.01f && Math.abs(actualElevation - expectedElevation) < 0.01f) {
            logger.info("Success: Cluster at {} with pose (Yaw: {}, Pitch: {}) mapped correctly to Azimuth: {}, Elevation: {}",
                    location, poseYaw, posePitch, expectedAzimuth, expectedElevation);
        } else {
            logger.error("Failure: Cluster at {} with pose (Yaw: {}, Pitch: {}) mapping mismatch. Expected Azimuth: {}, Elevation: {}, Found Azimuth: {}, Elevation: {}",
                    location, poseYaw, posePitch, expectedAzimuth, expectedElevation, actualAzimuth, actualElevation);
        }
    }
}
    
}
