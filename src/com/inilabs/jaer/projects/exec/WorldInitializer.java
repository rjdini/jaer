package com.inilabs.jaer.projects.exec;
import com.inilabs.jaer.projects.space3d.Space3D;
@FunctionalInterface public interface WorldInitializer { void init(Space3D world) throws Exception; }