package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.*;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import com.inilabs.jaer.projects.polarspace.PolarDrawableAdapter;
import com.inilabs.jaer.projects.polarspace.PolarSpaceGUI;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import net.sf.jaer.chip.AEChip;

import com.inilabs.jaer.projects.utils.geo.GeoMath;
import com.inilabs.jaer.projects.utils.geo.FrameTransforms;

import java.util.Objects;

/**
 * Executive: orchestrates one DVX system without modifying existing working code.
 * Responsibilities:
 *  - Create TrackerManagerV2 via a factory (no behavior change)
 *  - Discover Space3D via Space3DRegistry (as current code does)
 *  - Manage FieldOfView for this tracker
 *  - Add TargetAgents to Space3D and mirror them into PolarSpaceDisplay via adapter
 *  - Provide a convenience to construct FBG (keeps old behavior intact)
 */
public final class Executive {
    private final AgentFactory factory;
    private final AEChip chip;

    private TrackerManagerV2 manager;
    private Space3D          world;
    private FieldOfView      fov;
    private Agent3DInterface trackerAgent; // optional; used for tracker position in polar

    public Executive(AEChip chip, AgentFactory factory){
        this.chip = Objects.requireNonNull(chip, "chip");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /** Bootstraps the system using only working code paths. */
    public Executive start(){
        // 1) Manager (unchanged behavior)
        this.manager = factory.createTrackerManager(chip);
        // 🚀 ensure polar GUI is constructed
        this.manager.doPolarSpaceGUI();

        // 2) World (unchanged discovery via registry)
        this.world = Space3DRegistry.get();
        if (this.world == null) {
            throw new IllegalStateException("Space3D world not available via Space3DRegistry");
        }

        // 3) FOV singleton for this tracker (existing approach)
        this.fov = FieldOfView.getInstance();

        // 4) Optional: pick tracker agent from world for position (DVXPLORER)
        if (this.world.getAgents() != null) {
            for (Agent3DInterface a : this.world.getAgents().values()) {
                if (a.getType() == Agent3D.ObjectType.DVXPLORER) {
                    this.trackerAgent = a;
                    break;
                }
            }
        }
        return this;
    }

    /** Adds a target to Space3D and bridges it into the polar view via adapter. */
    public Executive addTarget(TargetSpec spec){
        ensureStarted();
        Agent3DInterface target = factory.createTargetAgent(spec);
        world.addAgent(target);

        // Bridge into polar display via adapter
        PolarSpaceGUI pgui = manager.getPolarSpaceGUI();
        if (pgui != null) {
            var adapter = new PolarDrawableAdapter(
                    target,
                    () -> (trackerAgent != null ? trackerAgent.getPositionDVX() : new Space3D.Vec3(0,0,0)),
                    () -> new double[]{ fov.getAxialYaw(), fov.getAxialPitch(), fov.getAxialRoll() }
            ).sizeDeg(2.0f);
            pgui.getPolarSpaceDisplay().addDrawable(adapter);
            pgui.getPolarSpaceDisplay().repaint();
        }
        return this;
    }

    /** Creates and returns an FBG filter; it auto-connects to Space3D via your registry pattern. */
    public FlyingBlobGenerator createFBG(){
        ensureStarted();
        return factory.createFlyingBlobGenerator(chip);
    }

    private void ensureStarted(){
        if (world == null || manager == null) {
            throw new IllegalStateException("Executive not started: call start() first.");
        }
    }

    public Space3D getWorld()          { return world; }
    public TrackerManagerV2 getManager(){ return manager; }
    public FieldOfView getFov()        { return fov; }

    // === Geo helpers (non-breaking) ===

    /** Convert WGS-84 LLH (deg,deg,m) to Space3D ENU (x=E, y=U, z=N) using the world's origin. */
    public double[] llhToEnu(double latDeg, double lonDeg, double altM){
        ensureStarted();
        return GeoMath.llhDegToEnu(latDeg, lonDeg, altM,
                world.getOriginLatDeg(), world.getOriginLonDeg(), world.getOriginAltM());
    }

    /** Convert Space3D ENU (x=E, y=U, z=N) to WGS-84 LLH (deg,deg,m) using the world's origin. */
    public double[] enuToLlh(double ex, double uy, double nz){
        ensureStarted();
        return GeoMath.enuToLlhDeg(ex, uy, nz,
                world.getOriginLatDeg(), world.getOriginLonDeg(), world.getOriginAltM());
    }

    /** Set FOV pose with degrees (kept GPS-agnostic). */
    public Executive setFovPoseDegrees(float yawDeg, float pitchDeg, float rollDeg){
        ensureStarted();
        fov.setPose(yawDeg, rollDeg, pitchDeg); // existing FOV order (yaw, roll, pitch)
        return this;
    }


    /** Convenience: add a target by GPS start/end (deg,deg,m), converting to Space3D ENU via world origin. */
    public Executive addTargetLLH(String key,
                                  double startLatDeg, double startLonDeg, double startAltM,
                                  double endLatDeg,   double endLonDeg,   double endAltM,
                                  float speedMps){
        ensureStarted();
        double[] s = GeoMath.llhDegToEnu(startLatDeg, startLonDeg, startAltM,
                world.getOriginLatDeg(), world.getOriginLonDeg(), world.getOriginAltM());
        double[] e = GeoMath.llhDegToEnu(endLatDeg, endLonDeg, endAltM,
                world.getOriginLatDeg(), world.getOriginLonDeg(), world.getOriginAltM());
        TargetSpec spec = new TargetSpec(key,
                new Space3D.Vec3(s[0], s[1], s[2]),
                new Space3D.Vec3(e[0], e[1], e[2]),
                speedMps);
        return addTarget(spec);
    }

    /** Convenience: add a target by ENU start/end (meters) explicitly. */
    public Executive addTargetENU(String key,
                                  double startE, double startU, double startN,
                                  double endE,   double endU,   double endN,
                                  float speedMps){
        ensureStarted();
        TargetSpec spec = new TargetSpec(key,
                new Space3D.Vec3(startE, startU, startN),
                new Space3D.Vec3(endE,   endU,   endN),
                speedMps);
        return addTarget(spec);
    }

    /** Set tracker pose from GPS + orientation (degrees). FOV remains GPS-agnostic. */
    public Executive setTrackerPoseLLH(double latDeg, double lonDeg, double altM,
                                       float yawDeg, float pitchDeg, float rollDeg){
        ensureStarted();
        double[] enu = GeoMath.llhDegToEnu(latDeg, lonDeg, altM,
                world.getOriginLatDeg(), world.getOriginLonDeg(), world.getOriginAltM());
        if (trackerAgent != null) {
            trackerAgent.setPositionDVX(new Space3D.Vec3(enu[0], enu[1], enu[2]));
        }
        fov.setPose(yawDeg, rollDeg, pitchDeg); // preserve established call order
        return this;
    }

}
