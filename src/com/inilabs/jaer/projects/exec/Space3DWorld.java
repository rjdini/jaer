/*
 * Copyright (C) 2025 rjd.
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
package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.AbstractAgent3D;
import com.inilabs.jaer.projects.space3d.Agent3D;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.Space3DGUI;
import com.inilabs.jaer.projects.space3d.Space3DRegistry;
import com.inilabs.jaer.projects.space3d.TargetAgent;
import com.inilabs.jaer.projects.space3d.TargetShape;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import javax.swing.SwingUtilities;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rjd
 */
public class Space3DWorld {
    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Space3DWorld.class);
    
  // [3D-WORLD] begin
    /** The shared Space3D world visible to other filters (e.g., FlyingBlobGenerator). */
    private Space3D world3D;

    /** The synthetic moving target that flies reciprocally. */
    private TargetAgent syntheticTarget;

    /** Optional viewer; null if you don’t want a window. */
    private Space3DGUI worldGUI;

    /** Toggle this to open/close the 3D viewer window when TrackerManager starts. */
    private boolean showWorldGUI = true;

    /** Key under which the target is registered in Space3D (FBG will look for this). */
    private static final String TARGET_KEY = "tgt-FBG";
    // [3D-WORLD] end   
    
    
      // [3D-WORLD] Build & register the world, start target, optionally show GUI
    public void startSpace3DWorld() {
        try {
            // Create the world with your default origin (Zurich defaults already in Space3D)
            world3D = new Space3D();
            world3D.setHalfExtentM(300);                 // slider range in GUI
            Space3DRegistry.set(world3D);               // <-- make discoverable for FBG

            // Marker for the DVX at the origin (blue square in GUI)
            AbstractAgent3D cam = new AbstractAgent3D("dvx-0", Agent3D.ObjectType.DVXPLORER) {};
            cam.setPosition3D(new Space3D.Vec3(0, 0, 0));
            world3D.addAgent(cam);

            // Reciprocal target: (-10,0,100) <-> (+10,0,20) at 10 m/s, key TARGET_KEY
//            syntheticTarget = new TargetAgent(
//                    TARGET_KEY,
//                    new Space3D.Vec3(-10, 0, 100),
//                    new Space3D.Vec3(+10, 0, 20),
//                    10.0
//            );
//            world3D.addAgent(syntheticTarget);
//            syntheticTarget.start();

              // --- Multiple targets with shapes & sizes ---
        TargetAgent t1 = new TargetAgent("tgt-circle",
                new Space3D.Vec3(-15, 0, 120),
                new Space3D.Vec3(+15, 0, 40),
                10.0);
        t1.setPhysicalDiameterM(1.0f);
        t1.setShape(TargetShape.CIRCLE);
        t1.setDensityScale(1.0f);
        world3D.addAgent(t1);

        TargetAgent t2 = new TargetAgent("tgt-square",
                new Space3D.Vec3(+20, 0, 150),
                new Space3D.Vec3(-20, 0, 60),
                8.0);
        t2.setPhysicalDiameterM(1.5f);
        t2.setShape(TargetShape.SQUARE);
        t2.setDensityScale(1.2f);
        world3D.addAgent(t2);

        TargetAgent t3 = new TargetAgent("tgt-triangle",
                new Space3D.Vec3(-10, 5, 130),
                new Space3D.Vec3(+10, -5, 50),
                12.0);
        t3.setPhysicalDiameterM(0.8f);
        t3.setShape(TargetShape.TRIANGLE);
        t3.setDensityScale(0.9f);
        world3D.addAgent(t3);

        TargetAgent t4 = new TargetAgent("tgt-cross",
                new Space3D.Vec3(+5, 0, 110),
                new Space3D.Vec3(-5, 0, 30),
                9.0);
        t4.setPhysicalDiameterM(1.2f);
        t4.setShape(TargetShape.CROSS);
        t4.setDensityScale(1.0f);
        world3D.addAgent(t4);

        // Start motion
        t1.start();
        t2.start();
        t3.start();
        t4.start();

            
            if (showWorldGUI) {
                SwingUtilities.invokeLater(() -> {
                    worldGUI = new Space3DGUI(world3D);
                    worldGUI.setVisible(true);
                });
            }

            log.info("TrackerManagerV2: Space3D started, target '{}' running. "
                    + "FBG can auto-connect (autoConnectRegistry=true, targetAgentKey='{}').",
                    TARGET_KEY, TARGET_KEY);

        } catch (Exception ex) {
            log.error("TrackerManagerV2: failed to start Space3D world", ex);
        }
    }

    /** Stop and clean up the Space3D world. */
public void stopSpace3DWorld() {
    try {
        if (syntheticTarget != null) {
            syntheticTarget.stop();   // idempotent, halts thread
            syntheticTarget = null;
        }
        if (worldGUI != null) {
            worldGUI.dispose();       // close the window if showing
            worldGUI = null;
        }
        world3D = null;
        com.inilabs.jaer.projects.space3d.Space3DRegistry.clear();  // reset process-local handle
        log.info("TrackerManagerV2: Space3D world stopped and registry cleared.");
    } catch (Exception ex) {
        log.warn("TrackerManagerV2: error stopping Space3D world", ex);
    }
}

    
}
