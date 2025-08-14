/*
 * Copyright (C) 2025 rjd.
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.slf4j.LoggerFactory;

public class TrackerManagerExerciser2 {

    public TrackerManagerExerciser2() {
    }
    
    private float currentAzimuth = 0.0f;
    private float currentElevation =0.0f;
    private boolean movingTowardsNegative = true;
    private final float offsetAzimuth = 0.0f;
    private final float offsetElevation =  0.0f;
    private final Random random = new Random();

      private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrackerManagerExerciser2.class);
    
    /**
     * Creates N test clusters centered around azimuth=0, elevation=0 with Gaussian noise.
     * @param N
     * @param SD
     * @return 
     */
    public LinkedList<TestCluster> createGaussianCluster(int N, float SD) {
       LinkedList<TestCluster> clusters = new LinkedList<>();
        for (int i = 0; i < N; i++) {
            float azimuth = (float) (random.nextGaussian() * SD );
            float elevation = (float) (random.nextGaussian()  * SD );
            log.info("@@@@@@@@@@ TestCluster azi {} ele {}", azimuth, elevation);
            TestCluster test = new TestCluster(azimuth + offsetAzimuth, elevation + offsetElevation, Color.GREEN);
            clusters.add(test);
        }
        return clusters;
    }

    /**
     * Moves each cluster in the linked list in oblique trajectory (along azimuth/elevation) 
     * according to the internal azimuth and elevation update logic.
     */
    public synchronized LinkedList<TestCluster> moveTestClustersOblique(LinkedList<TestCluster> clusters) {
 
        for (TestCluster cluster : clusters) {
       currentAzimuth = cluster.getAzimuth();
       currentElevation = cluster.getElevation();
            
             // Update for next step
        if (movingTowardsNegative) {
      //      currentElevation += 0.2f;
            currentAzimuth -= 0.5f;
            if (currentAzimuth <= -30.0f) {
                movingTowardsNegative = false;
            }
        } else {
       //     currentElevation -= 0.2f;
            currentAzimuth += 0.5f;
            if (currentAzimuth >= 30.0f) {
                movingTowardsNegative = true;
            }
        }
            
            
            cluster.setAzimuth(currentAzimuth);
            cluster.setElevation(currentElevation);
        }

       

        return clusters;
    }
}