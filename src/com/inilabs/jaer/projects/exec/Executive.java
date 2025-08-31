package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.AbstractAgent3D;
import com.inilabs.jaer.projects.space3d.Agent3D;
import com.inilabs.jaer.projects.space3d.Agent3DInterface;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.Space3DGUI;
import com.inilabs.jaer.projects.space3d.Space3DRegistry;
import com.inilabs.jaer.projects.space3d.TargetAgent;
import com.inilabs.jaer.projects.space3d.TargetShape;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import net.sf.jaer.chip.AEChip;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Executive {

    private final AEChip chip;

    private TrackerManagerV2 manager;
    private Space3D          world;
    private FieldOfView      fov;
    private Agent3DInterface trackerAgent;
    private boolean          showWorldGUI = false;
    private Space3DGUI       worldGUI;
    private final List<TargetAgent> startedTargets = new ArrayList<>();

    public Executive(AEChip chip){
        this.chip = Objects.requireNonNull(chip, "chip");
    }

    public Executive start(){
        this.manager = new TrackerManagerV2(chip);
        this.manager.doPolarSpaceGUI();
        startSpace3DWorld();
        this.fov = FieldOfView.getInstance();
        Map<String, Agent3DInterface> agents = this.world.getAgents();
        if (agents != null) {
            for (Agent3DInterface a : agents.values()) {
                if (a.getType() == Agent3D.ObjectType.DVXPLORER) {
                    this.trackerAgent = a;
                    break;
                }
            }
        }
        return this;
    }

    public Executive showWorldGUI(boolean show){
        this.showWorldGUI = show;
        return this;
    }

    private void startSpace3DWorld() {
        this.world = new Space3D();
        this.world.setHalfExtentM(300);
        Space3DRegistry.set(this.world);

        AbstractAgent3D cam = new AbstractAgent3D("dvx-0", Agent3D.ObjectType.DVXPLORER) {};
        cam.setPosition3D(new Space3D.Vec3(0, 0, 0));
        this.world.addAgent(cam);

        TargetAgent t1 = new TargetAgent("tgt-circle",
                new Space3D.Vec3(-15, 0, 120),
                new Space3D.Vec3(+15, 0, 40),
                10.0);
        t1.setPhysicalDiameterM(1.0f);
        t1.setShape(TargetShape.CIRCLE);
        t1.setDensityScale(1.0f);
        this.world.addAgent(t1);

        TargetAgent t2 = new TargetAgent("tgt-square",
                new Space3D.Vec3(+20, 0, 150),
                new Space3D.Vec3(-20, 0, 60),
                8.0);
        t2.setPhysicalDiameterM(1.5f);
        t2.setShape(TargetShape.SQUARE);
        t2.setDensityScale(1.2f);
        this.world.addAgent(t2);

        TargetAgent t3 = new TargetAgent("tgt-triangle",
                new Space3D.Vec3(-10, 5, 130),
                new Space3D.Vec3(+10, -5, 50),
                12.0);
        t3.setPhysicalDiameterM(0.8f);
        t3.setShape(TargetShape.TRIANGLE);
        t3.setDensityScale(0.9f);
        this.world.addAgent(t3);

        TargetAgent t4 = new TargetAgent("tgt-cross",
                new Space3D.Vec3(+5, 0, 110),
                new Space3D.Vec3(-5, 0, 30),
                9.0);
        t4.setPhysicalDiameterM(1.2f);
        t4.setShape(TargetShape.CROSS);
        t4.setDensityScale(1.0f);
        this.world.addAgent(t4);

        for (TargetAgent t : new TargetAgent[]{t1, t2, t3, t4}) {
            t.start();
            startedTargets.add(t);
        }

        if (showWorldGUI) {
            SwingUtilities.invokeLater(() -> {
                worldGUI = new Space3DGUI(world);
                worldGUI.setVisible(true);
            });
        }
    }

    public void stopSpace3DWorld() {
        for (TargetAgent t : startedTargets) {
            try { t.stop(); } catch (Throwable ignore) {}
        }
        startedTargets.clear();

        if (worldGUI != null) {
            try { worldGUI.dispose(); } catch (Throwable ignore) {}
            worldGUI = null;
        }
        world = null;
        Space3DRegistry.clear();
    }

    public FlyingBlobGenerator createFBG(){
        FlyingBlobGenerator fbg = new FlyingBlobGenerator(chip);
        try { fbg.autoConnectRegistry = true; } catch (Throwable ignore) {}
        return fbg;
    }

    public Space3D getWorld()          { return world; }
    public TrackerManagerV2 getManager(){ return manager; }
    public FieldOfView getFov()        { return fov; }
    public Agent3DInterface getTrackerAgent(){ return trackerAgent; }
}
