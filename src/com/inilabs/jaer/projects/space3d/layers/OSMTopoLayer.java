package com.inilabs.jaer.projects.space3d.layers;

import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.WorldGeoConfig;

public final class OSMTopoLayer {
    private OSMTopoLayer(){}
    public static void configureFromWorld(Space3D world){
        WorldGeoConfig g = world.getGeo();
        if (g == null) throw new IllegalStateException("Space3D.getGeo() is null");
        if (g.metersPerPixelAtOrigin <= 0) throw new IllegalArgumentException("metersPerPixelAtOrigin must be > 0");
        // Hook into your real OSM/texture code here.
    }
    public static void ensureCoverage(Space3D world){
        WorldGeoConfig g = world.getGeo();
        if (g == null) throw new IllegalStateException("Space3D.getGeo() is null");
        double pixelsNeeded = (2.0 * g.halfExtentM) / g.metersPerPixelAtOrigin;
        // Optionally assert zoom bounds vs extent; left as advisory.
    }
}