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

import com.inilabs.jaer.projects.tracker.TestCluster;

import java.awt.geom.Point2D;

public class TestClusterTest {

    public static void main(String[] args) {
        // Test the constructor with azimuth and elevation
        testAzimuthElevationConstructor();

        // Test the constructor with a Point2D.Float
        testPointConstructor();
    }

    private static void testAzimuthElevationConstructor() {
        System.out.println("Testing constructor with azimuth and elevation...");

        float testAzimuth = 30.0f;
        float testElevation = 15.0f;

        // Instantiate TestCluster
        TestCluster cluster = new TestCluster(testAzimuth, testElevation);

        // Validate properties
        assert cluster.getAzimuth() == testAzimuth :
                "Azimuth mismatch! Expected: " + testAzimuth + ", Found: " + cluster.getAzimuth();
        assert cluster.getElevation() == testElevation :
                "Elevation mismatch! Expected: " + testElevation + ", Found: " + cluster.getElevation();

        System.out.printf("Passed: Azimuth = %.2f, Elevation = %.2f%n",
                cluster.getAzimuth(), cluster.getElevation());
    }

    private static void testPointConstructor() {
        System.out.println("Testing constructor with Point2D.Float...");

        Point2D.Float testPoint = new Point2D.Float(100.0f, 200.0f);

        // Instantiate TestCluster
        TestCluster cluster = new TestCluster(testPoint);

        // Validate properties
        assert cluster.getLocation().equals(testPoint) :
                "Location mismatch! Expected: " + testPoint + ", Found: " + cluster.getLocation();

        float expectedAzimuth = TestCluster.fov.getYawAtPixel(testPoint.x);
        float expectedElevation = TestCluster.fov.getPitchAtPixel(testPoint.y);

        assert cluster.getAzimuth() == expectedAzimuth :
                "Azimuth mismatch! Expected: " + expectedAzimuth + ", Found: " + cluster.getAzimuth();
        assert cluster.getElevation() == expectedElevation :
                "Elevation mismatch! Expected: " + expectedElevation + ", Found: " + cluster.getElevation();

        System.out.printf("Passed: Location = %s, Azimuth = %.2f, Elevation = %.2f%n",
                cluster.getLocation(), cluster.getAzimuth(), cluster.getElevation());
    }
}
