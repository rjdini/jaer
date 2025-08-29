package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.JAERViewer;

/**
 * DemoMain:
 * - Bootstraps the Executive with a jAER AEChip
 * - Adds one TargetAgent
 * - Starts the FlyingBlobGenerator (FBG) injection filter automatically
 * It will attempt to attach FBG to the AEViewer pipeline via reflection if AEViewer is running.
 */
public final class DemoMain {

    public static void main(String[] args) {
//        AEChip chip = findChipOrExit();
         AEChip chip = new AEChip();
      // JAERViewer = new net.sf.jaer.JAERViewer();
         
        var exec = new Executive(chip, new DefaultAgentFactory()).start();

        // Add one example target flying roughly towards the tracker
        exec.addTarget(new TargetSpec(
                "demo-bird-1",
                new Space3D.Vec3(-20,  5, 120),
                new Space3D.Vec3( 15,  3,  30),
                12.0
        ));

        // Start FBG injection and attempt to wire it into the AEViewer filter pipeline
        FlyingBlobGenerator fbg = exec.createFBG();
        tryAttachFilterToAEViewer(fbg);

        // Keep JVM alive; your GUIs are Swing-based
        try { Thread.sleep(5_000_000); } catch (InterruptedException ignored) {}
    }
    
    
    private static AEChip findChipOrExit(){
        AEChip chip = null;
        // Try AEViewer singleton via reflection (no hard dependency)
        try {
            Class<?> viewerClz = Class.forName("net.sf.jaer.viewer.AEViewer");
            Object viewer = viewerClz.getMethod("getInstance").invoke(null);
            if (viewer != null) {
                Object chipObj = viewerClz.getMethod("getChip").invoke(viewer);
                if (chipObj instanceof AEChip) chip = (AEChip) chipObj;
            }
        } catch (Throwable ignore) {}

        // Try system property: -Djaer.chip.class=fully.qualified.ClassName
        if (chip == null) {
            String chipClz = System.getProperty("jaer.chip.class", "").trim();
            if (!chipClz.isEmpty()) {
                try {
                    Class<?> c = Class.forName(chipClz);
                    Object o = c.getDeclaredConstructor().newInstance();
                    if (o instanceof AEChip) chip = (AEChip) o;
                } catch (Throwable e) {
                    System.err.println("Failed to instantiate chip from jaer.chip.class=" + chipClz + " : " + e);
                }
            }
        }

        if (chip == null) {
            System.err.println("No AEChip found. Start from jAER (so AEViewer has a chip), or pass -Djaer.chip.class=... to instantiate one.");
            System.exit(2);
        }
        return chip;
    }

    /** Best-effort attachment of an EventFilter2D to the AEViewer pipeline using reflection. */
    private static void tryAttachFilterToAEViewer(Object filter){
        if (filter == null) return;
        try {
            Class<?> viewerClz = Class.forName("net.sf.jaer.viewer.AEViewer");
            Object viewer = viewerClz.getMethod("getInstance").invoke(null);
            if (viewer == null) { System.out.println("AEViewer not running; FBG created but not attached."); return; }

            // Path A: AEViewer has getFilterFrame() with addFilter(EventFilter2D)
            try {
                Object ff = viewerClz.getMethod("getFilterFrame").invoke(viewer);
                if (ff != null) {
                    for (var m : ff.getClass().getMethods()) {
                        if (m.getName().equals("addFilter") && m.getParameterCount() == 1) {
                            try {
                                m.invoke(ff, filter);
                                System.out.println("FBG attached via FilterFrame.addFilter(...)");
                                return;
                            } catch (Throwable ignore) {}
                        }
                    }
                }
            } catch (Throwable ignore) {}

            // Path B: AEViewer exposes a filter chain directly
            try {
                Object chain = viewerClz.getMethod("getFilterChain").invoke(viewer);
                if (chain != null) {
                    for (var m : chain.getClass().getMethods()) {
                        if ((m.getName().equals("add") || m.getName().equals("addFilter")) && m.getParameterCount() == 1) {
                            try {
                                m.invoke(chain, filter);
                                System.out.println("FBG attached via FilterChain.add(...)");
                                return;
                            } catch (Throwable ignore) {}
                        }
                    }
                }
            } catch (Throwable ignore) {}

            System.out.println("FBG created; could not attach to AEViewer via reflection. Add it manually if needed.");
        } catch (Throwable t) {
            System.out.println("FBG created; AEViewer not present or attachment failed: " + t);
        }
    }
}
