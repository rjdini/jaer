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
package com.inilabs.jaer.projects.tracker;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Random;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rjd
 */
public class TMExerciser {
    
 private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TMExerciser.class);    
private int numberClustersAdded = 5 ;
// Class-level variables to track the azimuth and direction
private float currentAzimuth = 0.0f;
private float currentElevation= 0.0f;
private boolean movingTowardsNegative = true;
private LinkedList<TestCluster> clusterList = null; // Persistent list to store clusters
    private final Random random = new Random(); // Random number generator


 public TMExerciser() {
     
 }    

 
 
 
 
 
  public LinkedList<TestCluster> getTestClusters(float offsetAzimuth, float offsetElevation) {
        if (clusterList == null) {
            // First call: Create clusters around the offsets
            clusterList = new LinkedList<>();
            for (int i = 0; i < numberClustersAdded; i++) {
                // Generate initial random azimuth and altitude around offsets
                float azimuth = offsetAzimuth + (float) (Math.random() * 20 - 10); // Range: [offset - 10, offset + 10]
                float elevation = offsetElevation + (float) (Math.random() * 20 - 10); // Range: [offset - 10, offset + 10]

                // Create and add TestCluster
                TestCluster testCluster = new TestCluster(azimuth, elevation, Color.GREEN);
                log.debug("Created Test cluster: ID={}, Azimuth={}, Elevation={}",
                        testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());
                clusterList.add(testCluster);
            }
        } else {
            // Subsequent calls: Move clusters by small random increments
            for (TestCluster testCluster : clusterList) {
                // Apply small random increments to azimuth and elevation
                float deltaAzimuth = (float) (random.nextGaussian() * 0.5); // Small random change
                float deltaElevation = (float) (random.nextGaussian() * 0.5); // Small random change

                // Update cluster position
                float newAzimuth = testCluster.getAzimuth() + deltaAzimuth;
                float newElevation = testCluster.getElevation() + deltaElevation;

                // Ensure the movement stays roughly around the offsets
                newAzimuth = clamp(newAzimuth, offsetAzimuth - 15, offsetAzimuth + 15); // Limit movement range
                newElevation = clamp(newElevation, offsetElevation - 15, offsetElevation + 15); // Limit movement range

                testCluster.setAzimuth(newAzimuth);
                testCluster.setElevation(newElevation);

                log.debug("Moved Test cluster: ID={}, New Azimuth={}, New Elevation={}",
                        testCluster.getKey(), newAzimuth, newElevation);
            }
        }

        return clusterList;
    }

    // Helper method to clamp values within a range
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
 
 
 
 
 // individual patterns are generated around azimuth, elevation 0,0
public  LinkedList<TestCluster>  getMultipleTestClusters( float centerAzim, float centerElev) {
    LinkedList<TestCluster> clusterList = new LinkedList<>();
    for (int i = 0; i < numberClustersAdded; i++) {
        
// Generate random azimuth and elevation within a range
        float azimuth = (float) (Math.random() * 20 - 10); // Range: [-10, 10]
        float elevation = (float)(Math.random() * 20 - 10); // Range: [-10, 10]

        // Create TestCluster
        TestCluster testCluster = new TestCluster(azimuth+centerAzim, elevation+centerElev, Color.GREEN);

      log.debug("Test cluster: ID={}, Azimuth={}, Elevation={}",
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());

        clusterList.add(testCluster);
    }
    
return clusterList;
}

public  LinkedList<TestCluster> getTestClustersHorizontal() {
    LinkedList<TestCluster> clusterList = new LinkedList<>();
    for (int i = 0; i < numberClustersAdded; i++) {
        // Generate elevation randomly within a range
        
         float azimuth = (float) (Math.random() * 10 - 5); // Range: [-10, 10]
        float elevation = (float)(Math.random() * 10 - 5); // Range: [-10, 10]

        // Create random testCluster around current azimuth
        TestCluster testCluster = new TestCluster(currentAzimuth+azimuth, currentElevation+elevation, Color.GREEN);
        
        log.debug("Test cluster: ID={}, Azimuth={}, Elevation={}",
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());
        
        clusterList.add(testCluster);
    }

    // Update the azimuth for the next call
    if (movingTowardsNegative) {
        currentElevation += 0.2f;
        currentAzimuth -= 0.5f;
        if (currentAzimuth <= -30.0f) {
            movingTowardsNegative = false; // Reverse direction
        }
    } else {
        currentElevation -= 0.2f;
        currentAzimuth += 0.5f;
        if (currentAzimuth >= 30.0f) {
            movingTowardsNegative = true; // Reverse direction
        }
    }
    
   return clusterList;  
}


public  LinkedList<TestCluster> getTestClustersCircular() {
    LinkedList<TestCluster> clusterList = new LinkedList<>();
    float angle = 0; // Angle in radians, starts at 0 and advances by 0.1 radians per call
    float radius = 10; // Radius in degrees

    for (int i = 0; i < numberClustersAdded; i++) {
        // Base coordinates on the circle
        float baseAzimuth = (float) (radius * Math.cos(angle));
        float baseElevation = (float) (radius * Math.sin(angle));

        // Add a small random perturbation to the base coordinates
        float azimuth = baseAzimuth + (float) (Math.random() * 20 - 10); // Random offset [-1, 1]
        float elevation = baseElevation + (float) (Math.random() * 20 - 10); // Random offset [-1, 1]

        // Create TestCluster
        TestCluster testCluster = new TestCluster(baseAzimuth+azimuth, baseElevation+elevation, Color.GREEN);

        // Add the cluster to the list
        clusterList.add(testCluster);

        // Log the details
        log.debug("Test cluster: ID={}, Azimuth={}, Elevation={}",
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());

        // Advance the angle by 0.1 radians
        angle += 0.1;
    }
    return clusterList;
}
    
}
