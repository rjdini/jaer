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

import java.util.LinkedList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rjd
 */
public class TMExerciser {
    
 private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TMExerciser.class);    
private int numberClustersAdded = 3;
// Class-level variables to track the azimuth and direction
private float currentAzimuth = 0.0f;
private boolean movingTowardsNegative = true;


 public TMExerciser() {
     
 }    

 // individual patterns are generated around azimuth, elevation 0,0
public  LinkedList<TestCluster>  getTestClusters() {
    LinkedList<TestCluster> clusterList = new LinkedList<>();
    for (int i = 0; i < numberClustersAdded; i++) {
        
// Generate random azimuth and elevation within a range
        float azimuth = (float) (Math.random() * 20 - 10); // Range: [-10, 10]
        float elevation = (float)(Math.random() * 20 - 10); // Range: [-10, 10]

        // Create TestCluster
        TestCluster testCluster = new TestCluster(azimuth, elevation);

      log.info("Test cluster: ID={}, Azimuth={}, Elevation={}",
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());

        clusterList.add(testCluster);
    }
    
return clusterList;
}

public  LinkedList<TestCluster> getTestClustersHorizontal() {
    LinkedList<TestCluster> clusterList = new LinkedList<>();
    for (int i = 0; i < numberClustersAdded; i++) {
        // Generate elevation randomly within a range
        
         float azimuth = (float) (Math.random() * 20 - 10); // Range: [-10, 10]
        float elevation = (float)(Math.random() * 20 - 10); // Range: [-10, 10]

        // Create random testCluster around current azimuth
        TestCluster testCluster = new TestCluster(currentAzimuth+azimuth, elevation);
        
        log.info("Test cluster: ID={}, Azimuth={}, Elevation={}",
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());
        
        clusterList.add(testCluster);
    }

    // Update the azimuth for the next call
    if (movingTowardsNegative) {
        currentAzimuth -= 1.0f;
        if (currentAzimuth <= -20.0f) {
            movingTowardsNegative = false; // Reverse direction
        }
    } else {
        currentAzimuth += 1.0f;
        if (currentAzimuth >= 20.0f) {
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
        TestCluster testCluster = new TestCluster(baseAzimuth+azimuth, baseElevation+elevation);

        // Add the cluster to the list
        clusterList.add(testCluster);

        // Log the details
        log.info("Test cluster: ID={}, Azimuth={}, Elevation={}",
                 testCluster.getKey(), testCluster.getAzimuth(), testCluster.getElevation());

        // Advance the angle by 0.1 radians
        angle += 0.1;
    }
    return clusterList;
}
    
}
