package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.agents.api.Agent3DTypes;
import com.inilabs.jaer.projects.agents.api.Agent3DInterface;
import com.inilabs.jaer.projects.space3d.GeoTransforms;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.Space3DGUI;
import com.inilabs.jaer.projects.space3d.Space3DRegistry;
import com.inilabs.jaer.projects.space3d.WorldGeoConfig;
import com.inilabs.jaer.projects.space3d.WorldGeoRegistry;
import com.inilabs.jaer.projects.space3d.layers.ElevationGrid;
import com.inilabs.jaer.projects.space3d.layers.OSMTopoLayer;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import net.sf.jaer.chip.AEChip;

import javax.swing.SwingUtilities;
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

    private WorldInitializer worldInitializer = WorldPresets.preset(WorldPresets.Preset.EXAMPLE);

    public Executive(AEChip chip){
        this.chip = Objects.requireNonNull(chip, "chip");
    }

    public Executive withWorld(WorldInitializer initializer){
        this.worldInitializer = Objects.requireNonNull(initializer, "initializer");
        return this;
    }

    public Executive showWorldGUI(boolean show){
        this.showWorldGUI = show;
        return this;
    }

    public Executive start(){
        this.manager = new TrackerManagerV2(chip);
       // this.manager.doPolarSpaceGUI();

        this.world = new Space3D();
        Space3DRegistry.set(this.world);

        double lat = 47.3769, lon = 8.5417, alt = 408.0;
        int zoom = 18;
        double mpp = GeoTransforms.metersPerPixelAt(lat, zoom);
        WorldGeoConfig geo = new WorldGeoConfig(lat, lon, alt, this.world.getHalfExtentM(),
                WorldGeoConfig.CRS.ENU_WGS84,
                WorldGeoConfig.MapProvider.OSM_WEBMERCATOR,
                WorldGeoConfig.ElevationProvider.SRTM30,
                zoom, mpp);
        WorldGeoRegistry.set(this.world, geo);

        try { this.worldInitializer.init(this.world); }
        catch (Exception ex){ throw new IllegalStateException("World initialization failed", ex); }

        OSMTopoLayer.ensureCoverage(this.world);
        ElevationGrid.ensureCoverage(this.world);
        OSMTopoLayer.configureFromWorld(this.world);
        ElevationGrid.configureFromWorld(this.world);

        this.fov = FieldOfView.getInstance();

        Map<String, Agent3DInterface> agents = this.world.getAgents();
        if (agents != null) {
            for (Agent3DInterface a : agents.values()) {
                if (a.getType() == Agent3DTypes.ObjectType.DVXPLORER) { this.trackerAgent = a; break; }
            }
        }

        if (showWorldGUI) {
            SwingUtilities.invokeLater(() -> {
                worldGUI = new Space3DGUI(world);
                worldGUI.setVisible(true);
            });
        }
        return this;
    }

    public void stopSpace3DWorld() {
        try { if (worldGUI != null) { try { worldGUI.dispose(); } catch (Throwable ignore) {} worldGUI = null; } }
        finally { 
            if (world != null) {
                try { WorldGeoRegistry.clear(world); } catch (Throwable ignore) {}
            }
            world = null; 
            Space3DRegistry.clear(); 
        }
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
