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

import com.inilabs.jaer.projects.agents.api.Agent3DTypes;
import com.inilabs.jaer.projects.agents.core.AbstractAgent;
import javax.swing.SwingUtilities;
import com.inilabs.jaer.projects.space3d.*;
import com.inilabs.jaer.projects.space3d.Space3DGUI;

/**
 * Test harness for the Space3D system.
 * 
 * Creates a Space3D, adds a DVXplorer agent at the origin and a target agent
 * at an offset, then displays them in the Space3DGUI.
 */
public class Space3DTest {

    public static void main(String[] args) {
        // Define origin (Zurich HB approx.)
        Space3D space = new Space3D();
        space.setHalfExtentM(1000);

        // Add one DVXplorer agent at the origin
        AbstractAgent cam = new AbstractAgent("dvx-test", Agent3DTypes.ObjectType.DVXPLORER) {};
        cam.setPosition3D(new Space3D.Vec3(0, 0, 0));
        space.addAgent(cam);

        // Add a test target 300 m north, 50 m east, 20 m up
        AbstractAgent tgt = new AbstractAgent("tgt-test", Agent3DTypes.ObjectType.TARGET) {};
        tgt.setPosition3D(new Space3D.Vec3(50, 20, 300));
        space.addAgent(tgt);

        // Show the GUI
        SwingUtilities.invokeLater(() -> new Space3DGUI(space));
    }
}
