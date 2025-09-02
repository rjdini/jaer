/*
 * Revised FBGIntegrationTest.java
 * - Uses Space3DTargetProvider (multi-target) + shape-aware TargetAgent
 * - Safer "HeadlessChip" to avoid graphics classpath issues when not running inside jAER
 * - Keeps Space3DGUI optional (comment/uncomment as needed)
 */
package com.inilabs.jaer.projects.space3d.tests;

import com.inilabs.jaer.projects.agents.s3d.AgentScheduler;
import com.inilabs.jaer.projects.agents.s3d.TargetAgent;
import com.inilabs.jaer.projects.agents.api.Agent3DTypes;
import com.inilabs.jaer.projects.agents.core.AbstractAgent;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

import com.inilabs.jaer.projects.space3d.*;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import net.sf.jaer.graphics.*;

import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.event.PolarityEvent.Polarity;

public class FBGIntegrationTest {

    
    
    /**
     * Minimal AEChip that avoids loading display/render classes.
     */
    static class HeadlessChip extends AEChip {

        public HeadlessChip() {
            super();
            // Basic geometry that FBG needs
            setSizeX(240);
            setSizeY(180);
            setPixelWidthUm(10.0f);
            setPixelHeightUm(10.0f);
        }

        // Depending on your jAER version, one or both of these may exist.
        // If they don't, these just become ordinary methods (no override),
        // and you should ensure your classpath includes jAER graphics.
        protected void createDefaultDisplayMethod() {
            /* no-op: prevent renderer */ }

        protected void allocateDefaultDisplayMethods() {
            /* no-op: prevent renderer */ }
    }

    public static void main(String[] args) {
        // Helpful when running outside a full UI classpath
        System.setProperty("java.awt.headless", "false");

        // --- World (Space3D) ---
        Space3D space = new Space3D();
        
        space.setHalfExtentM(300);
        Space3DRegistry.set(space);   // expose world to any auto-connecting filters

          // --- Show the space GUI only if not headless ---
        Space3DGUI.showIfPossible(space);

        // Optional camera agent at origin
        AbstractAgent cam = new AbstractAgent("dvx-0", Agent3DTypes.ObjectType.DVXPLORER) {
        };
        cam.setPosition3D(new Space3D.Vec3(0, 0, 0));
        space.addAgent(cam);

        // --- Multiple targets with shapes & sizes ---
        TargetAgent t1 = new TargetAgent("tgt-circle",
                new Space3D.Vec3(-15, 0, 120),
                new Space3D.Vec3(+15, 0, 40),
                10.0);
        t1.setPhysicalDiameterM(1.0f);
        t1.setShape(TargetShape.CIRCLE);
        t1.setDensityScale(1.0f);
        space.addAgent(t1);

        TargetAgent t2 = new TargetAgent("tgt-square",
                new Space3D.Vec3(+20, 0, 150),
                new Space3D.Vec3(-20, 0, 60),
                8.0);
        t2.setPhysicalDiameterM(1.5f);
        t2.setShape(TargetShape.SQUARE);
        t2.setDensityScale(1.2f);
        space.addAgent(t2);

        TargetAgent t3 = new TargetAgent("tgt-triangle",
                new Space3D.Vec3(-10, 5, 130),
                new Space3D.Vec3(+10, -5, 50),
                12.0);
        t3.setPhysicalDiameterM(0.8f);
        t3.setShape(TargetShape.TRIANGLE);
        t3.setDensityScale(0.9f);
        space.addAgent(t3);

        TargetAgent t4 = new TargetAgent("tgt-cross",
                new Space3D.Vec3(+5, 0, 110),
                new Space3D.Vec3(-5, 0, 30),
                9.0);
        t4.setPhysicalDiameterM(1.2f);
        t4.setShape(TargetShape.CROSS);
        t4.setDensityScale(1.0f);
        space.addAgent(t4);

        AgentScheduler sched = new AgentScheduler(space); 
        sched.add(t1); sched.add(t2); sched.add(t3); sched.add(t4);
        sched.start(1.0 / 60.0);
 

        // --- Show the space GUI (comment this out if you want fully headless) ---
//        try {
//            SwingUtilities.invokeLater(() -> new Space3DGUI(space));
//        } catch (Throwable ignore) {
//            // If graphics classes aren't found, ignore—headless run.
//        }

        // --- FBG wired to a chip ---
        AEChip chip = new HeadlessChip(); // Use real chip when running inside jAER
        FlyingBlobGenerator fbg = new FlyingBlobGenerator(chip);
        fbg.setAutoConnectRegistry(false); // we'll wire the provider explicitly
        fbg.eventDensityPerPx2 = 0.30f;
        fbg.injectedPolarity = +1;
        fbg.setLocalTestEnabled(false);

        // Provide all targets via provider
        fbg.setTargetProvider(new Space3DTargetProvider(space));

        // Lens (match your hardware lens)
        fbg.setLensFocalLengthMm(22.5f);
        fbg.initFilter();

        // --- Local pump: repeatedly call filterPacket(...) so FBG injects ---
        EventPacket<PolarityEvent> pkt = new EventPacket<>(PolarityEvent.class);
        long t0 = System.nanoTime();

        // Clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         sched.stop();
        }));

        try {
            while (true) {
                // Minimal input event (jAER requires appendCopyOfEvent)
                pkt.clear();
                PolarityEvent e = new PolarityEvent();
                e.timestamp = (int) ((System.nanoTime() - t0) / 1000); // microseconds
                e.x = 0;
                e.y = 0;
                e.setPolarity(Polarity.On);
                pkt.appendCopyOfEvent(e);

                // Drive FBG
                fbg.filterPacket(pkt);

                // If you need to inspect the output:
                // EventPacket<?> out = fbg.getOutPacket();
                Thread.sleep(5); // ~200 Hz
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
         sched.stop();
        }
    }
}
