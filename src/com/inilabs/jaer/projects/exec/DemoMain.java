package com.inilabs.jaer.projects.exec;

import com.inilabs.jaer.projects.eventprocessing.filters.FlyingBlobGenerator;
import net.sf.jaer.chip.AEChip;

import javax.swing.SwingUtilities;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DemoMain {

    private static Object viewer; // AEViewer or JAERViewer

    public static void main(String[] args) {
        AEChip chip = new AEChip();

        Executive exec = new Executive(chip)
                .showWorldGUI(true)
                .start();

        SwingUtilities.invokeLater(() -> {
            if (!tryLaunchAEViewer(chip)) {
                tryLaunchJAERViewer(chip);
            }
        });

        FlyingBlobGenerator fbg = exec.createFBG();
        tryAttachFilterToViewer(fbg);

        try { Thread.sleep(10_000_000); } catch (InterruptedException ignored) {}
    }

    private static boolean tryLaunchAEViewer(AEChip chip) {
        try {
            Class<?> aClass = Class.forName("net.sf.jaer.AEViewer");
            Constructor<?> ctor = aClass.getConstructor(String.class, AEChip.class);
            viewer = ctor.newInstance("Birdland + jAER (AEViewer)", chip);
            Method setVisible = aClass.getMethod("setVisible", boolean.class);
            setVisible.invoke(viewer, true);
            System.out.println("Launched AEViewer.");
            return true;
        } catch (Throwable t) {
            System.err.println("AEViewer not available: " + t.getClass().getSimpleName());
            return false;
        }
    }

    private static boolean tryLaunchJAERViewer(AEChip chip) {
        try {
            Class<?> jClass = Class.forName("net.sf.jaer.JAERViewer");

            // Initialize static Logger if null to avoid NPEs
            try {
                Field logField = jClass.getDeclaredField("log");
                logField.setAccessible(true);
                Object current = logField.get(null);
                if (current == null) {
                    Logger L = Logger.getLogger("net.sf.jaer.JAERViewer");
                    L.setUseParentHandlers(false);
                    ConsoleHandler h = new ConsoleHandler();
                    h.setLevel(Level.INFO);
                    L.addHandler(h);
                    L.setLevel(Level.INFO);
                    try { logField.set(null, L); } catch (Throwable ignored) {}
                }
            } catch (NoSuchFieldException ignored) {}

            try {
                Constructor<?> ctor = jClass.getConstructor(String.class, AEChip.class);
                viewer = ctor.newInstance("Birdland + jAER (JAERViewer)", chip);
            } catch (NoSuchMethodException e) {
                Constructor<?> ctor = jClass.getConstructor();
                viewer = ctor.newInstance();
                try {
                    Method setChip = jClass.getMethod("setChip", AEChip.class);
                    setChip.invoke(viewer, chip);
                } catch (Throwable ignore) {}
            }
            try {
                Method setVisible = jClass.getMethod("setVisible", boolean.class);
                setVisible.invoke(viewer, true);
            } catch (Throwable ignore) {}
            System.out.println("Launched JAERViewer.");
            return true;
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            return false;
        }
    }

    private static void tryAttachFilterToViewer(Object filter) {
        for (int i = 0; i < 50 && viewer == null; i++) {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
        if (viewer == null || filter == null) return;
        Class<?> vc = viewer.getClass();

        try {
            Class<?> ef2d = Class.forName("net.sf.jaer.eventprocessing.EventFilter2D");
            Method m = vc.getMethod("addFilter", ef2d);
            m.invoke(viewer, filter);
            System.out.println("Attached filter via addFilter(EventFilter2D).");
            return;
        } catch (Throwable ignore) {}

        try {
            Method m = vc.getMethod("addFilter", Object.class);
            m.invoke(viewer, filter);
            System.out.println("Attached filter via addFilter(Object).");
            return;
        } catch (Throwable ignore) {}

        try {
            Method getFilterFrame = vc.getMethod("getFilterFrame");
            Object frame = getFilterFrame.invoke(viewer);
            if (frame != null) {
                try {
                    Method addF = frame.getClass().getMethod("addFilter", filter.getClass());
                    addF.invoke(frame, filter);
                    System.out.println("Attached filter via FilterFrame.addFilter(filterClass).");
                    return;
                } catch (Throwable ignored) {}
                try {
                    Method addF2 = frame.getClass().getMethod("addFilter", Object.class);
                    addF2.invoke(frame, filter);
                    System.out.println("Attached filter via FilterFrame.addFilter(Object).");
                    return;
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignore) {}

        System.err.println("Could not auto-attach FBG to viewer; add it manually in the UI.");
    }
}
