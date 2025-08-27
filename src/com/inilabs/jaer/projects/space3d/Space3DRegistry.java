package com.inilabs.jaer.projects.space3d;

import java.util.Objects;

/**
 * Process-local registry so filters/tools can share the same Space3D instance.
 * Call Space3DRegistry.set(space) inside jAER to make the world discoverable.
 */
public final class Space3DRegistry {
    private static volatile Space3D space;

    private Space3DRegistry(){}

    /** Register the Space3D world to be used by filters/tools in this JVM. */
    public static void set(Space3D s){
        space = Objects.requireNonNull(s, "Space3D cannot be null");
    }

    /** Optional: clear the registry. */
    public static void clear(){
        space = null;
    }

    /** Get the registered Space3D (or null if none). */
    public static Space3D get(){
        return space;
    }
}
