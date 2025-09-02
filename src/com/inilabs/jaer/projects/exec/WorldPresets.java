package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.agents.api.Agent3DInterface;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.agents.s3d.TargetAgent;
import com.inilabs.jaer.projects.agents.api.Agent3DTypes;
import com.inilabs.jaer.projects.agents.core.AbstractAgent;
import com.inilabs.jaer.projects.agents.s3d.AgentScheduler;
import com.inilabs.jaer.projects.agents.s3d.DVXAgent;
import com.inilabs.jaer.projects.polarspace.PolarDrawableAdapter;
import com.inilabs.jaer.projects.polarspace.PolarSpaceDisplay;
import com.inilabs.jaer.projects.polarspace.PolarSpaceGUI;
import com.inilabs.jaer.projects.space3d.TargetShape;
import java.util.function.Supplier;

public final class WorldPresets {

    private WorldPresets() {
    }
    private PolarSpaceGUI polarGUI;

    public enum Preset {
        EXAMPLE
    }

    public static WorldInitializer preset(Preset p) {
        return WorldPresets::buildExampleWorld;
    }

    private static void buildExampleWorld(Space3D world) {
        world.setHalfExtentM(300);

        //  setting up DVX
        // Simple suppliers (static yaw/pitch/roll and fov for now)
        Supplier<double[]> yprSupplier = () -> new double[]{0.0, 0.0, 0.0};   // yaw=0°, pitch=0°, roll=0°
        Supplier<double[]> fovSupplier = () -> new double[]{60.0, 40.0};      // 60° horiz, 40° vert

// Place DVX at origin in ENU (east, up, north)
        Space3D.Vec3 dvxPos = new Space3D.Vec3(0, 0, 0);

// Declare the agent
        DVXAgent dvx = new DVXAgent("dvx1", dvxPos, yprSupplier, fovSupplier);

// (optional) configure appearance
        dvx.setConeLengthM(200.0).setDrawLabel(true);

// Add it to the Space3D world if you keep a list
        world.addAgent(dvx);

        // Polar manager you already have (whatever class holds add/remove of Drawables):
        final PolarSpaceDisplay polar = PolarSpaceDisplay.getInstance();

        AgentScheduler sched = new AgentScheduler(world);

        TargetAgent t1 = new TargetAgent("tgt-circle", new Space3D.Vec3(-15, 0, 120), new Space3D.Vec3(+15, 0, 40), 10.0);
        t1.setPhysicalDiameterM(1.0f);
        t1.setShape(TargetShape.CIRCLE);
        t1.setDensityScale(1.0f);
        world.addAgent(t1);
        // add to the scheduler
        sched.add(t1);
        // Bridge each target to Polar using the adapter
        PolarDrawableAdapter t1P = new PolarDrawableAdapter(t1, dvx::getPosition3D, dvx::getYawPitchRollDeg);
        polar.addDrawable(t1P);
        t1.setActive(true);

        TargetAgent t2 = new TargetAgent("tgt-square", new Space3D.Vec3(+20, 0, 150), new Space3D.Vec3(-20, 0, 60), 8.0);
        t2.setPhysicalDiameterM(1.5f);
        t2.setShape(TargetShape.SQUARE);
        t2.setDensityScale(1.2f);
        world.addAgent(t2);
// add to the scheduler
        sched.add(t2);
        // Bridge each target to Polar using the adapter
        PolarDrawableAdapter t2P = new PolarDrawableAdapter(t2, dvx::getPosition3D, dvx::getYawPitchRollDeg);
        polar.addDrawable(t2P);
        t2.setActive(true);

        TargetAgent t3 = new TargetAgent("tgt-triangle", new Space3D.Vec3(-10, 5, 130), new Space3D.Vec3(+10, -5, 50), 12.0);
        t3.setPhysicalDiameterM(0.8f);
        t3.setShape(TargetShape.TRIANGLE);
        t3.setDensityScale(0.9f);
        world.addAgent(t3);
// add to the scheduler
        sched.add(t3);
        // Bridge each target to Polar using the adapter
        PolarDrawableAdapter t3P = new PolarDrawableAdapter(t3, dvx::getPosition3D, dvx::getYawPitchRollDeg);
        polar.addDrawable(t3P);
        t3.setActive(true);

        TargetAgent t4 = new TargetAgent("tgt-cross", new Space3D.Vec3(+5, 0, 110), new Space3D.Vec3(-5, 0, 30), 9.0);
        t4.setPhysicalDiameterM(1.2f);
        t4.setShape(TargetShape.CROSS);
        t4.setDensityScale(1.0f);
        world.addAgent(t4);
        // add to the scheduler
        sched.add(t4);
        // Bridge each target to Polar using the adapter
        PolarDrawableAdapter t4P = new PolarDrawableAdapter(t4, dvx::getPosition3D, dvx::getYawPitchRollDeg);
        polar.addDrawable(t4P);
        t4.setActive(true);

        sched.start(1.0 / 60.0);

    }
}
