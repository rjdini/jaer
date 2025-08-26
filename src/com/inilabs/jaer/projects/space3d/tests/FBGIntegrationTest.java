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
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;

import net.sf.jaer.chip.AEChip;
//import net.sf.jaer.chip.GenericChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.event.PolarityEvent.Polarity;

/**
 * Launches Space3D + moving TargetAgent, displays Space3DGUI,
 * and drives FlyingBlobGenerator so it injects a visible blob.
 *
 * NOTE: In a normal jAER session, the framework constructs the filter with the real chip
 * and calls filterPacket(...) for you. This launcher is just a self-contained sanity check.
 */
public class FBGIntegrationTest {

    public static void main(String[] args) {
        // --- World (Space3D) ---
        Space3D space = new Space3D();   // your default origin
        space.setHalfExtentM(300);
        Space3DRegistry.set(space);   // <-- make it visible to FBG in this JVM

        // Optional camera agent at origin (blue square in GUI)
        AbstractAgent3D cam = new AbstractAgent3D("dvx-0", Agent3D.ObjectType.DVXPLORER) {};
        cam.setPositionDVX(new Space3D.Vec3(0,0,0));
        space.addAgent(cam);

        // --- Moving target: (-10,0,100) <-> (+10,0,20) at 10 m/s ---
        TargetAgent target = new TargetAgent(
                "tgt-FBG",
                new Space3D.Vec3(-10, 0, 100),
                new Space3D.Vec3(+10, 0, 20),
                10.0
        );
        space.addAgent(target);
        target.start();
        
        
        // --- Show the space GUI so you can see the target moving ---
        SwingUtilities.invokeLater(() -> new Space3DGUI(space));

        // --- FBG wired to a chip (use your real chip in jAER; GenericChip is fine for a demo) ---
        AEChip chip = new AEChip();
        FlyingBlobGenerator fbg = new FlyingBlobGenerator(chip);
        fbg.setSpace3D(space);
        fbg.setTargetAgent(target);
        fbg.setTargetDiameterM(1.0f);       // 1 m physical target
        fbg.setLensFocalLengthMm(22.5f);    // match your DVXplorer lens
        fbg.setEventsPerPacket(400);        // make it visible
        fbg.setInjectedPolarity(+1);        // ON events
        fbg.initFilter();

        // --- Local pump: repeatedly call filterPacket(...) so FBG injects ---
        EventPacket<PolarityEvent> pkt = new EventPacket<>(PolarityEvent.class);
        long t0 = System.nanoTime();

        // Clean shutdown for the target thread
        Runtime.getRuntime().addShutdownHook(new Thread(target::stop));

        try {
            while (true) {
                // Minimal input event with correct Polarity enum (NOT boolean!)
                pkt.clear();
                PolarityEvent e = new PolarityEvent();
                e.timestamp = (int)((System.nanoTime() - t0) / 1000); // microseconds
                e.x = 0; e.y = 0; e.setPolarity(Polarity.On);
                pkt.appendCopyOfEvent(e);

                fbg.filterPacket(pkt);

                // If you want to inspect what FBG produced, use fbg.getOutPacket() here

                Thread.sleep(5); // ~200 Hz
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            target.stop();
        }
    }
}

