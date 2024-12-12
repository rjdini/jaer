package com.inilabs.jaer.projects.tracker.tests;

import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.TestCluster;
import java.awt.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;

public class TestClusterFieldOfViewInteractionTest {

    private static final Logger logger = LoggerFactory.getLogger(TestClusterFieldOfViewInteractionTest.class);

    public static void main(String[] args) {
        testInitializationAtZeroPose();
        testInitializationAtNonZeroPose();
        testPolarInitializationAtZeroPose();
        testPolarInitializationAtNonZeroPose();
         testPolarInitializationWithAbsolutePixelAtZeroPose();
         testPolarInitializationWithAbsolutePixelAtNonZeroPose();
    }

    private static void testInitializationAtZeroPose() {
        logger.info("Testing Initialization at Zero Pose...");

        FieldOfView fov = FieldOfView.getInstance();
        fov.setChipDimensions(640, 480);
        fov.setPose(0, 0, 0); // Azimuth = 0, Roll = 0, Elevation = 0

        Point2D.Float pixel = new Point2D.Float(320, 240); // Center of chip
        TestCluster cluster = new TestCluster(pixel);

        float expectedAzimuth = 0.0f;
        float expectedElevation = 0.0f;

        if (Math.abs(cluster.getAzimuth() - expectedAzimuth) < 0.01 && Math.abs(cluster.getElevation() - expectedElevation) < 0.01) {
            logger.info("Success: Pixel ({}, {}) -> Azimuth = {}, Elevation = {}", pixel.x, pixel.y, cluster.getAzimuth(), cluster.getElevation());
        } else {
            logger.error("Failure: Pixel ({}, {}) -> Expected Azimuth = {}, Elevation = {}, Found Azimuth = {}, Elevation = {}",
                    pixel.x, pixel.y, expectedAzimuth, expectedElevation, cluster.getAzimuth(), cluster.getElevation());
        }
    }

    private static void testInitializationAtNonZeroPose() {
        logger.info("Testing Initialization at Non-Zero Pose...");

        FieldOfView fov = FieldOfView.getInstance();
        fov.setChipDimensions(640, 480);

        float poseAzimuth = 10.0f;
        float poseElevation = -5.0f;
        fov.setPose(poseAzimuth, 0, poseElevation); // Azimuth = 10, Roll = 0, Elevation = -5

        Point2D.Float pixel = new Point2D.Float(320, 240); // Center of chip
        TestCluster cluster = new TestCluster(pixel);

        float expectedAzimuth = poseAzimuth;
        float expectedElevation = poseElevation;

        if (Math.abs(cluster.getAzimuth() - expectedAzimuth) < 0.01 && Math.abs(cluster.getElevation() - expectedElevation) < 0.01) {
            logger.info("Success: Pixel ({}, {}) -> Azimuth = {}, Elevation = {}", pixel.x, pixel.y, cluster.getAzimuth(), cluster.getElevation());
        } else {
            logger.error("Failure: Pixel ({}, {}) -> Expected Azimuth = {}, Elevation = {}, Found Azimuth = {}, Elevation = {}",
                    pixel.x, pixel.y, expectedAzimuth, expectedElevation, cluster.getAzimuth(), cluster.getElevation());
        }
    }

    private static void testPolarInitializationAtZeroPose() {
        logger.info("Testing Polar Initialization at Zero Pose...");

        FieldOfView fov = FieldOfView.getInstance();
        fov.setChipDimensions(640, 480);
        fov.setPose(0, 0, 0); // Azimuth = 0, Roll = 0, Elevation = 0

        float azimuth = -5.0f;
        float elevation = 3.0f;
    
        
        TestCluster cluster = new TestCluster(azimuth, elevation, Color.GREEN);

        float deltaAzimuth = azimuth - fov.getAxialYaw();
        float deltaElevation = elevation - fov.getAxialPitch();

        int centerX = 640 / 2;
        int centerY = 480 / 2;
        float expectedPixelX = centerX + (deltaAzimuth / 20.0f) * 640;
        float expectedPixelY = centerY - (deltaElevation / 15.0f) * 480;

        Point2D.Float pixelLocation = cluster.getLocation();
        if (Math.abs(pixelLocation.x - expectedPixelX) < 0.01 && Math.abs(pixelLocation.y - expectedPixelY) < 0.01) {
            logger.info("Success: Azimuth = {}, Elevation = {} -> Absolute Pixel ({}, {})", azimuth, elevation, pixelLocation.x, pixelLocation.y);
        } else {
            logger.error("Failure: Azimuth = {}, Elevation = {} -> Expected Pixel ({}, {}), Found ({}, {})",
                    azimuth, elevation, expectedPixelX, expectedPixelY, pixelLocation.x, pixelLocation.y);
        }
    }

    private static void testPolarInitializationAtNonZeroPose() {
        logger.info("Testing Polar Initialization at Non-Zero Pose...");

        FieldOfView fov = FieldOfView.getInstance();
        fov.setChipDimensions(640, 480);

        float poseAzimuth = 10.0f;
        float poseElevation = -5.0f;
        fov.setPose(poseAzimuth, 0, poseElevation); // Azimuth = 10, Roll = 0, Elevation = -5

        float azimuth = 15.0f;
        float elevation = -7.0f;
        TestCluster cluster = new TestCluster(azimuth, elevation,Color.GREEN);

        float deltaAzimuth = azimuth - poseAzimuth;
        float deltaElevation = elevation - poseElevation;

        int centerX = 640 / 2;
        int centerY = 480 / 2;
        float expectedPixelX = centerX + (deltaAzimuth / 20.0f) * 640;
        float expectedPixelY = centerY - (deltaElevation / 15.0f) * 480;

        Point2D.Float pixelLocation = cluster.getLocation();
        if (Math.abs(pixelLocation.x - expectedPixelX) < 0.01 && Math.abs(pixelLocation.y - expectedPixelY) < 0.01) {
            logger.info("Success: Azimuth = {}, Elevation = {} -> Absolute Pixel ({}, {})", azimuth, elevation, pixelLocation.x, pixelLocation.y);
        } else {
            logger.error("Failure: Azimuth = {}, Elevation = {} -> Expected Pixel ({}, {}), Found ({}, {})",
                    azimuth, elevation, expectedPixelX, expectedPixelY, pixelLocation.x, pixelLocation.y);
        }
    }

    private static void testPolarInitializationWithAbsolutePixelAtZeroPose() {
        logger.info("Testing Polar Initialization with Absolute Pixels at Zero Pose...");

        FieldOfView fov = FieldOfView.getInstance();
        fov.setChipDimensions(640, 480);
        fov.setPose(0, 0, 0); // Azimuth = 0, Roll = 0, Elevation = 0

        float azimuth = -5.0f;
        float elevation = 3.0f;
   
        int centerX = 640 / 2;
        int centerY = 480 / 2;
        float expectedPixelX = centerX + (azimuth / 20.0f) * 640;
        float expectedPixelY = centerY + (elevation / 15.0f) * 480;

        Point2D.Float expectedPixel = new Point2D.Float(expectedPixelX, expectedPixelY);
        TestCluster cluster = new TestCluster(expectedPixel);

        if (Math.abs(cluster.getAzimuth() - azimuth) < 0.01 && Math.abs(cluster.getElevation() - elevation) < 0.01) {
            logger.info("Success: Pixel ({}, {}) -> Azimuth = {}, Elevation = {}", expectedPixel.x, expectedPixel.y, cluster.getAzimuth(), cluster.getElevation());
        } else {
            logger.error("Failure: Pixel ({}, {}) -> Expected Azimuth = {}, Elevation = {}, Found Azimuth = {}, Elevation = {}",
                    expectedPixel.x, expectedPixel.y, azimuth, elevation, cluster.getAzimuth(), cluster.getElevation());
        }
    }

    private static void testPolarInitializationWithAbsolutePixelAtNonZeroPose() {
        logger.info("Testing Polar Initialization with Absolute Pixels at Non-Zero Pose...");

        FieldOfView fov = FieldOfView.getInstance();
        fov.setChipDimensions(640, 480);

        float poseAzimuth = 10.0f;
        float poseElevation = -5.0f;
        
     
   
        fov.setPose(poseAzimuth, 0, poseElevation); // Azimuth = 10, Roll = 0, Elevation = -5

        float azimuth = 15.0f;
         float elevation = -7.0f;
        
     

        int centerX = 640 / 2;
        int centerY = 480 / 2;
        float expectedPixelX = centerX + ((azimuth - poseAzimuth) / 20.0f) * 640;
        float expectedPixelY = centerY + ((elevation - poseElevation) / 15.0f) * 480;

        Point2D.Float expectedPixel = new Point2D.Float(expectedPixelX, expectedPixelY);
        TestCluster cluster = new TestCluster(expectedPixel);

        if (Math.abs(cluster.getAzimuth() - azimuth) < 0.01 && Math.abs(cluster.getElevation() - elevation) < 0.01) {
            logger.info("Success: Pixel ({}, {}) -> Azimuth = {}, Elevation = {}", expectedPixel.x, expectedPixel.y, cluster.getAzimuth(), cluster.getElevation());
        } else {
            logger.error("Failure: Pixel ({}, {}) -> Expected Azimuth = {}, Elevation = {}, Found Azimuth = {}, Elevation = {}",
                    expectedPixel.x, expectedPixel.y, azimuth, elevation, cluster.getAzimuth(), cluster.getElevation());
        }
    }
}

