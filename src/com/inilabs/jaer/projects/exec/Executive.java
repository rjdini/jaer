package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.Agent3D;
import com.inilabs.jaer.projects.space3d.Agent3DInterface;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.Space3DGUI;
import com.inilabs.jaer.projects.space3d.Space3DRegistry;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import net.sf.jaer.chip.AEChip;

import javax.swing.SwingUtilities;
import java.util.Map;
import java.util.Objects;

/**
 * Executive orchestrates the DVX tracker + world.
 * Now supports pluggable world initialization via WorldInitializer.
 */
public final class Executive {

    private final AEChip chip;

    private TrackerManagerV2 manager;
    private Space3D          world;
    private FieldOfView      fov;
    private Agent3DInterface trackerAgent;
    private boolean          showWorldGUI = false;
    private Space3DGUI       worldGUI;

    private WorldInitializer worldInitializer = WorldPresets.preset(WorldPresets.Preset.EXAMPLE);

    public Executive(AEChip chip){
        this.chip = Objects.requireNonNull(chip, "chip");
    }

    /** Select a world initializer (optional; default is EXAMPLE). */
    public Executive withWorld(WorldInitializer initializer){
        this.worldInitializer = Objects.requireNonNull(initializer, "initializer");
        return this;
    }

    /** Toggle the Space3D GUI window. */
    public Executive showWorldGUI(boolean show){
        this.showWorldGUI = show;
        return this;
    }

    /** Bootstraps the system. */
    public Executive start(){
        // 1) Manager
        this.manager = new TrackerManagerV2(chip);
//        this.manager.doPolarSpaceGUI();

        // 2) Create & register world
        this.world = new Space3D();
        Space3DRegistry.set(this.world);

        // 3) Initialize world via strategy (default EXAMPLE)
        try {
            this.worldInitializer.init(this.world);
        } catch (Exception ex){
            throw new IllegalStateException("World initialization failed", ex);
        }

        // 4) FOV singleton for this tracker
        this.fov = FieldOfView.getInstance();

        // 5) Optionally pick tracker agent from world for position (DVXPLORER)
        Map<String, Agent3DInterface> agents = this.world.getAgents();
        if (agents != null) {
            for (Agent3DInterface a : agents.values()) {
                if (a.getType() == Agent3D.ObjectType.DVXPLORER) {
                    this.trackerAgent = a;
                    break;
                }
            }
        }

        // 6) Show world GUI if requested
        if (showWorldGUI) {
            SwingUtilities.invokeLater(() -> {
                worldGUI = new Space3DGUI(world);
                worldGUI.setVisible(true);
            });
        }

        return this;
    }

    /** Stop and clean up the Space3D world. */
    public void stopSpace3DWorld() {
        try {
            if (worldGUI != null) {
                try { worldGUI.dispose(); } catch (Throwable ignore) {}
                worldGUI = null;
            }
        } finally {
            world = null;
            Space3DRegistry.clear();
        }
    }

    /** Convenience to construct FBG with registry auto-connect. */
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
