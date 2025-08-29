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

/**
 *
 * @author rjd
 */

package com.inilabs.jaer.projects.space3d.tests;

import javax.swing.SwingUtilities;

import com.inilabs.jaer.projects.space3d.*;

/**
 * Demo/test: loads a TargetAgent moving between
 *   p1 = (-10, 0, 100) and p2 = (+10, 0, 20)
 * at 10 m/s, visualized in Space3DGUI.
 */
public class TargetAgentDemo {

    public static void main(String[] args) {
        // Space with your default origin; half-extent gives slider ranges
        Space3D space = new Space3D();
        space.setHalfExtentM(200);

        // Create and add target agent (reciprocal motion)
        TargetAgent target = new TargetAgent(
                "tgt-recip-10mps",
                new Space3D.Vec3(-10, 0, 100),
                new Space3D.Vec3(+10, 0, 20),
                10.0
        );
        space.addAgent(target);
        target.start(); // start its own motion thread

        // (Optional) add a DVX agent at origin if you have one.
        // If AbstractAgent3D exists in your codebase, uncomment:
        // AbstractAgent3D cam = new AbstractAgent3D("dvx-0", Agent3D.ObjectType.DVXPLORER) {};
        // cam.setPosition3D(new Space3D.Vec3(0,0,0));
        // space.addAgent(cam);

        // Show GUI
        SwingUtilities.invokeLater(() -> new Space3DGUI(space));

        // Add a shutdown hook to stop the agent thread when the JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(target::stop));
    }
}

