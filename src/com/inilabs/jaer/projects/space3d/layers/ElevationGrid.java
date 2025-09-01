package com.inilabs.jaer.projects.space3d.layers;

import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.WorldGeoConfig;
import com.inilabs.jaer.projects.space3d.WorldGeoRegistry;

public final class ElevationGrid {
    private ElevationGrid(){}
    public static void configureFromWorld(Space3D world){
        WorldGeoConfig g = WorldGeoRegistry.require(world);
        if (g.elevProvider == WorldGeoConfig.ElevationProvider.NONE) return;
        // Hook into your DEM resample here.
    }
    public static void ensureCoverage(Space3D world){
        WorldGeoConfig g = WorldGeoRegistry.require(world);
        // Advisory checks can go here.
    }
}