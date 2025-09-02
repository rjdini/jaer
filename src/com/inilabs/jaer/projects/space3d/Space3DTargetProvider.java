package com.inilabs.jaer.projects.space3d;

import com.inilabs.jaer.projects.agents.api.Agent3DInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter that exposes FBGTarget instances from a Space3D world.
 * Assumes Space3D has a method returning its agent map; if not, this provider
 * returns an empty list (update to match your Space3D API).
 */
public class Space3DTargetProvider implements FBGTargetProvider {

    private final Space3D space;

    public Space3DTargetProvider(Space3D space){
        this.space = space;
    }

    @Override
    public Iterable<FBGTarget> getFBGTargets() {
        try {
            // Expected API: Map<String, Agent3DInterface> Space3D.getAgents()
            Map<String, Agent3DInterface> map = space.getAgents();
            List<FBGTarget> out = new ArrayList<>();
            for (Agent3DInterface a : map.values()) {
                if (a instanceof FBGTarget) out.add((FBGTarget)a);
            }
            return out;
        } catch (Throwable t) {
            // Fallback: no visible agents API
            return java.util.Collections.emptyList();
        }
    }
}
