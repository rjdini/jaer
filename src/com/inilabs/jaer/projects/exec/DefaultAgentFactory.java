package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.Agent3DInterface;
import com.inilabs.jaer.projects.space3d.TargetAgent;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import net.sf.jaer.chip.AEChip;

/**
 * Default concrete factory that uses existing implementations exactly as-is.
 * No behavioral changes; keeps working paths intact.
 */
public class DefaultAgentFactory implements AgentFactory {

    @Override
    public TrackerManagerV2 createTrackerManager(AEChip chip) {
        // Use the existing TrackerManagerV2 behavior (no overrides).
        return new TrackerManagerV2(chip);
    }

    @Override
    public Agent3DInterface createTargetAgent(TargetSpec spec) {
        // TargetAgent signature: (key, start, end, speedMps)
        TargetAgent t = new TargetAgent(spec.key, spec.start, spec.end, spec.speedMps);
        // Optional fields applied if present
        if (!Float.isNaN(spec.physicalDiameterM)) t.setPhysicalDiameterM(spec.physicalDiameterM);
        if (!Float.isNaN(spec.densityScale))     t.setDensityScale(spec.densityScale);
        if (spec.shape != null)                  t.setShape(spec.shape);
        return t;
    }

    @Override
    public FlyingBlobGenerator createFlyingBlobGenerator(AEChip chip) {
        // Keep original FBG behavior; it will find Space3D via your registry pattern.
        return new FlyingBlobGenerator(chip);
    }
}
