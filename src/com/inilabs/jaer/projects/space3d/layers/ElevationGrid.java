package com.inilabs.jaer.projects.space3d.layers;

import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.WorldGeoConfig;

public final class ElevationGrid {
    private ElevationGrid(){}
    public static void configureFromWorld(Space3D world){
        WorldGeoConfig g = world.getGeo();
        if (g == null) throw new IllegalStateException("Space3D.getGeo() is null");
        if (g.elevProvider == WorldGeoConfig.ElevationProvider.NONE) return;
        // Hook into your DEM resample here.
    }
    public static void ensureCoverage(Space3D world){
        WorldGeoConfig g = world.getGeo();
        if (g == null) throw new IllegalStateException("Space3D.getGeo() is null");
    }
}