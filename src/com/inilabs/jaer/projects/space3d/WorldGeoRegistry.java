package com.inilabs.jaer.projects.space3d;

import java.util.WeakHashMap;

/**
 * Attaches WorldGeoConfig to Space3D instances without changing Space3D's API.
 * Uses a WeakHashMap so worlds can be GC'd naturally.
 */
public final class WorldGeoRegistry {
    private static final WeakHashMap<Space3D, WorldGeoConfig> MAP = new WeakHashMap<>();
    private WorldGeoRegistry(){}

    public static void set(Space3D world, WorldGeoConfig cfg){
        if(world == null) throw new IllegalArgumentException("world is null");
        MAP.put(world, cfg);
    }

    public static WorldGeoConfig get(Space3D world){
        return MAP.get(world);
    }

    public static WorldGeoConfig require(Space3D world){
        WorldGeoConfig cfg = MAP.get(world);
        if (cfg == null) throw new IllegalStateException("No WorldGeoConfig registered for this Space3D");
        return cfg;
    }

    public static void clear(Space3D world){
        MAP.remove(world);
    }
}
