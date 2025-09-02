package com.inilabs.jaer.projects.agents.s3d;

import com.inilabs.jaer.projects.agents.api.AgentRunnable;
import com.inilabs.jaer.projects.space3d.Space3D;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AgentScheduler: minimal, optional scheduler for AgentRunnable updates.
 * Use only if you want agents to self-run without an external controller.
 * You can also call tickAll(dtSec) from an existing animation loop instead.
 */
public final class AgentScheduler implements AutoCloseable {

    private final Space3D world;
    private final List<AgentRunnable> agents = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService exec;

    public AgentScheduler(Space3D world){
        this.world = world;
    }

    public void add(AgentRunnable a){ if (a != null) agents.add(a); }
    public void remove(AgentRunnable a){ agents.remove(a); }
    public void clear(){ agents.clear(); }

    /** Start a background fixed-rate runner. */
    public void start(double dtSec){
        if (exec != null) return;
        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AgentScheduler"); t.setDaemon(true); return t;
        });
        long periodNs = (long)(dtSec * 1e9);
        exec.scheduleAtFixedRate(() -> {
            try {
                tickAll(dtSec);
            } catch (Throwable t){
                t.printStackTrace();
            }
        }, periodNs, periodNs, TimeUnit.NANOSECONDS);
    }

    /** Stop background runner. */
    public void stop(){
        if (exec != null){
            exec.shutdownNow();
            exec = null;
        }
    }

    /** Manual tick for all registered AgentRunnable agents. */
    public void tickAll(double dtSec){
        for (AgentRunnable a : agents){
            if (a.isActive()){
                a.runStep(dtSec, world);
            }
        }
    }

    @Override public void close(){ stop(); }
}
