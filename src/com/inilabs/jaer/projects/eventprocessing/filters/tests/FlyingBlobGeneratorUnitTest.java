package com.inilabs.jaer.projects.eventprocessing.filters.tests;

import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import com.inilabs.jaer.projects.space3d.Agent3DInterface;
import com.inilabs.jaer.projects.space3d.Agent3D;
import com.inilabs.jaer.projects.space3d.Space3D;

import net.sf.jaer.graphics.*;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.event.PolarityEvent.Polarity;

/**
 * Headless sanity tests for FlyingBlobGenerator. - Provides a DummyChip instead
 * of GenericChip. - DummyAgent simulates a simple target at a fixed 3D
 * position.
 */
public class FlyingBlobGeneratorUnitTest {

    /**
     * Minimal dummy AEChip that just reports a sensor size and pixel pitch.
     */
    static class DummyChip extends AEChip {

        public DummyChip() {
            super();
            setSizeX(240);
            setSizeY(180);
            setPixelWidthUm(10.0f);
            setPixelHeightUm(10.0f);
        }
    }

    /**
     * Minimal dummy target agent returning a fixed DVX-frame position.
     */
    static class DummyAgent implements Agent3DInterface {

        private final String key;
        private Space3D.Vec3 p = new Space3D.Vec3(0, 0, 10);

        DummyAgent(String key) {
            this.key = key;
        }

        public void setPosition(Space3D.Vec3 np) {
            this.p = np;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Agent3D.ObjectType getType() {
            return Agent3D.ObjectType.TARGET;
        }

        @Override
        public Space3D.Vec3 getPosition3D() {
            return p;
        }

        @Override
        public void setPosition3D(Space3D.Vec3 pos) {
            this.p = pos;
        }

        @Override
        public double[] getYawPitchRollDeg() {
            return new double[]{0, 0, 0};
        }

        @Override
        public void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg) {
        }

        @Override
        public double[] getLLA() {
            return new double[]{Double.NaN, Double.NaN, Double.NaN};
        }

        @Override
        public void setLLA(double latDeg, double lonDeg, double altM) {
        }
    }

    public static void main(String[] args) {
        testProjectionCenter();
        testFovGateBehindCamera();
        testLocalTestBlob();
        System.out.println("\nAll FlyingBlobGenerator headless tests PASSED.");
    }

    /**
     * Target straight ahead at z=10m should project near the image center and
     * inject events.
     */
    public static void testProjectionCenter() {
        AEChip chip = new DummyChip();
        FlyingBlobGenerator fbg = new FlyingBlobGenerator(chip);
        fbg.setAutoConnectRegistry(false);
        fbg.eventDensityPerPx2 = 0.3f;
        fbg.injectedPolarity = +1;
        fbg.setLocalTestEnabled(true);
        fbg.setLocalTestRadiusPx(8);
        fbg.setLocalTestEventsPerPacket(300);

        DummyAgent agent = new DummyAgent("dummy");
        agent.setPosition(new Space3D.Vec3(0, 0, 10)); // straight ahead
        fbg.setTargetAgent(agent);

        fbg.initFilter();

        EventPacket<PolarityEvent> in = new EventPacket<>(PolarityEvent.class);
        PolarityEvent e = new PolarityEvent();
        e.timestamp = 1000;
        e.x = 0;
        e.y = 0;
        e.setPolarity(Polarity.On);
        in.appendCopyOfEvent(e);

        EventPacket<?> out = fbg.filterPacket(in);
        if (out.getSize() <= in.getSize()) {
            throw new AssertionError("Expected injected events; got out.size=" + out.getSize());
        }
        System.out.printf("testProjectionCenter: in=%d out=%d%n", in.getSize(), out.getSize());
    }

    /**
     * Target behind the camera (z<0) should be OUT of FOV and inject nothing.
     */
    public static void testFovGateBehindCamera() {
        AEChip chip = new DummyChip();
        FlyingBlobGenerator fbg = new FlyingBlobGenerator(chip);

        fbg.setAutoConnectRegistry(false);

        DummyAgent agent = new DummyAgent("dummy");
        agent.setPosition(new Space3D.Vec3(0, 0, -10)); // behind camera
        fbg.setTargetAgent(agent);

        fbg.initFilter();

        EventPacket<PolarityEvent> in = new EventPacket<>(PolarityEvent.class);
        PolarityEvent e = new PolarityEvent();
        e.timestamp = 2000;
        e.x = 0;
        e.y = 0;
        e.setPolarity(Polarity.On);
        in.appendCopyOfEvent(e);

        EventPacket<?> out = fbg.filterPacket(in);
        if (out.getSize() != in.getSize()) {
            throw new AssertionError("Expected pass-through only when target behind camera.");
        }
        System.out.printf("testFovGateBehindCamera: in=%d out=%d%n", in.getSize(), out.getSize());
    }

    /**
     * Local fixed test blob should inject even without a targetAgent.
     */
    public static void testLocalTestBlob() {
        AEChip chip = new DummyChip();
        FlyingBlobGenerator fbg = new FlyingBlobGenerator(chip);
        fbg.setAutoConnectRegistry(false);
        fbg.eventDensityPerPx2 = 0.3f;
        fbg.injectedPolarity = +1;
        fbg.setLocalTestEnabled(true);
        fbg.setLocalTestRadiusPx(8);
        fbg.setLocalTestEventsPerPacket(300);

        fbg.initFilter();

        EventPacket<PolarityEvent> in = new EventPacket<>(PolarityEvent.class);
        PolarityEvent e = new PolarityEvent();
        e.timestamp = 3000;
        e.x = 0;
        e.y = 0;
        e.setPolarity(Polarity.On);
        in.appendCopyOfEvent(e);

        EventPacket<?> out = fbg.filterPacket(in);
        if (out.getSize() <= in.getSize()) {
            throw new AssertionError("Expected injected events from local test blob.");
        }
        System.out.printf("testLocalTestBlob: in=%d out=%d%n", in.getSize(), out.getSize());
    }
}
