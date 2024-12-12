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

import com.inilabs.jaer.projects.tracker.ClusterAdapter;
import com.inilabs.jaer.projects.tracker.EventCluster;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.tests.TestClusterAdapter;


public class EventClusterTest {
     private static long lifeDurationMillis = 2000 ;
     private static FieldOfView fov = FieldOfView.getInstance();
    

    public static void main(String[] args) {
        testFromClusterAdapter();
        testEncapsulation();
        testVisibilityMethods();
    }

    private static void testFromClusterAdapter() {
        System.out.println("Testing EventCluster.fromClusterAdapter...");

        // Create a TestClusterAdapter
        TestClusterAdapter clusterAdapter = new TestClusterAdapter("testCluster", 45.0f, 15.0f);

        // Create an EventCluster using fromClusterAdapter
        EventCluster eventCluster = EventCluster.fromClusterAdapter(clusterAdapter, lifeDurationMillis);

        // Validate encapsulation
        assert eventCluster.getEnclosedCluster() == clusterAdapter :
                "Encapsulation failed: ClusterAdapter not properly set in EventCluster";

        assert eventCluster.getKey().equals(clusterAdapter.getKey()) :
                "Key mismatch! Expected: " + clusterAdapter.getKey() + ", Found: " + eventCluster.getKey();

        assert eventCluster.getAzimuth() == clusterAdapter.getAzimuth() :
                "Azimuth mismatch! Expected: " + clusterAdapter.getAzimuth() + ", Found: " + eventCluster.getAzimuth();

        assert eventCluster.getElevation() == clusterAdapter.getElevation() :
                "Elevation mismatch! Expected: " + clusterAdapter.getElevation() + ", Found: " + eventCluster.getElevation();

        System.out.println("Passed: EventCluster.fromClusterAdapter");
    }

    private static void testEncapsulation() {
        System.out.println("Testing EventCluster encapsulation of ClusterAdapter...");

        // Create a TestClusterAdapter with sample data
        TestClusterAdapter clusterAdapter = new TestClusterAdapter("cluster1", 60.0f, 30.0f);

        // Create an EventCluster using fromClusterAdapter
        EventCluster eventCluster = EventCluster.fromClusterAdapter(clusterAdapter, lifeDurationMillis);

        // Modify the TestClusterAdapter
        clusterAdapter.setAzimuth(90.0f);
        clusterAdapter.setElevation(45.0f);

        // Ensure EventCluster reflects the changes in the TestClusterAdapter
        assert eventCluster.getAzimuth() == clusterAdapter.getAzimuth() :
                "Encapsulation mismatch! Azimuth in EventCluster did not update with ClusterAdapter.";

        assert eventCluster.getElevation() == clusterAdapter.getElevation() :
                "Encapsulation mismatch! Elevation in EventCluster did not update with ClusterAdapter.";

        System.out.println("Passed: EventCluster encapsulation test.");
    }

    private static void testVisibilityMethods() {
        System.out.println("Testing visibility methods...");

        // Create a TestClusterAdapter
        TestClusterAdapter clusterAdapter = new TestClusterAdapter("testCluster", 30.0f, 15.0f);

        // Create an EventCluster
        EventCluster eventCluster = EventCluster.fromClusterAdapter(clusterAdapter, lifeDurationMillis);

        // Validate initial visibility
        assert eventCluster.isVisible() : "Visibility mismatch! Expected: true, Found: false.";

        // Change visibility
        eventCluster.setIsVisible(false);
        assert !eventCluster.isVisible() : "Visibility mismatch! Expected: false, Found: true.";

        // Toggle back to visible
        eventCluster.setIsVisible(true);
        assert eventCluster.isVisible() : "Visibility mismatch! Expected: true, Found: false.";

        System.out.println("Passed: Visibility methods test.");
    }
}
