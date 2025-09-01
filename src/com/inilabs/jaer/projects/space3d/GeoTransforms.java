package com.inilabs.jaer.projects.space3d;

public final class GeoTransforms {
    private static final double R_EARTH = 6378137.0;
    private GeoTransforms(){}
    public static double metersPerPixelAt(double latDeg, int zoom){
        double latRad = Math.toRadians(latDeg);
        return Math.cos(latRad) * 2.0 * Math.PI * R_EARTH / (256.0 * (1 << zoom));
    }
    public static double dLonDegForMeters(double metersEast, double atLatDeg){
        double latRad = Math.toRadians(atLatDeg);
        double metersPerDegLon = Math.cos(latRad) * 2.0 * Math.PI * R_EARTH / 360.0;
        return metersEast / metersPerDegLon;
    }
    public static double dLatDegForMeters(double metersNorth){
        double metersPerDegLat = 2.0 * Math.PI * R_EARTH / 360.0;
        return metersNorth / metersPerDegLat;
    }
}