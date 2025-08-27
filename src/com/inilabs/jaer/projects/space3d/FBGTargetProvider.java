package com.inilabs.jaer.projects.space3d;

public interface FBGTargetProvider {
    /** Return iterable collection of all targets to be rendered by FBG (may be empty). */
    Iterable<FBGTarget> getFBGTargets();
}
