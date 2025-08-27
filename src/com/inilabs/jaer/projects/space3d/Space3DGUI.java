package com.inilabs.jaer.projects.space3d;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * GUI shell for Space3DPanel with coherent map + overlay scaling/panning.
 */
public class Space3DGUI extends JFrame {

    private final Space3D space;
    private final Space3DPanel panel;
    private final JLabel status = new JLabel(" ");

    private JSlider scaleSlider;  // 2^k px/m, k in [-3..8]
    private JSlider offsetXSlider;
    private JSlider offsetZSlider;
    private JCheckBox mapToggle;
    private JSlider mapZoomSlider;

    // in Space3DGUI.java
    public static Space3DGUI showIfPossible(Space3D space) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return null;
        }
        return new Space3DGUI(space);
    }

    public Space3DGUI(Space3D space) {
        super("Space3D XZ Viewer (North up) + Map");
        this.space = space;
        this.panel = new Space3DPanel(space);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buildControls(), BorderLayout.EAST);
        add(status, BorderLayout.SOUTH);
        panel.setStatusLabel(status);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        new Timer(1000 / 30, e -> panel.repaint()).start();
    }

    private JPanel buildControls() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(260, 0));

        // Scale slider (log2)
        scaleSlider = new JSlider(JSlider.HORIZONTAL, -3, 8, 0);
        JLabel scaleVal = new JLabel();
        ChangeListener scl = e -> {
            double k = scaleSlider.getValue();
            double ppm = Math.pow(2.0, k);
            panel.setPixelsPerMeter(ppm);
            scaleVal.setText(String.format("zoom: %.3f px/m", ppm));
        };
        scl.stateChanged(null);
        scaleSlider.addChangeListener(scl);
        p.add(new JLabel("Scale"));
        p.add(scaleSlider);
        p.add(scaleVal);
        p.add(Box.createVerticalStrut(12));

        // Offsets (pan)
        int half = (int) Math.max(500, Math.round(space.getHalfExtentM()));
        offsetXSlider = new JSlider(JSlider.HORIZONTAL, -half, +half, 0);
        JLabel offXVal = new JLabel();
        ChangeListener ox = e -> {
            double m = offsetXSlider.getValue();
            panel.setOffsetXM(m);
            offXVal.setText(String.format("offset X (E): %.1f m", m));
        };
        ox.stateChanged(null);
        offsetXSlider.addChangeListener(ox);
        p.add(new JLabel("Offset X (East)"));
        p.add(offsetXSlider);
        p.add(offXVal);
        p.add(Box.createVerticalStrut(12));

        offsetZSlider = new JSlider(JSlider.HORIZONTAL, -half, +half, 0);
        JLabel offZVal = new JLabel();
        ChangeListener oz = e -> {
            double m = offsetZSlider.getValue();
            panel.setOffsetZM(m);
            offZVal.setText(String.format("offset Z (North): %.1f m", m));
        };
        oz.stateChanged(null);
        offsetZSlider.addChangeListener(oz);
        p.add(new JLabel("Offset Z (North)"));
        p.add(offsetZSlider);
        p.add(offZVal);
        p.add(Box.createVerticalStrut(18));

        // Map controls
        mapToggle = new JCheckBox("Show Map (OSM)", true);
        mapToggle.addActionListener(e -> panel.setMapEnabled(mapToggle.isSelected()));
        p.add(mapToggle);
        p.add(Box.createVerticalStrut(6));

        mapZoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 19, 16);
        JLabel mapZoomVal = new JLabel();
        ChangeListener mz = e -> {
            int z = mapZoomSlider.getValue();
            panel.setMapZoom(z);
            mapZoomVal.setText("map zoom: " + z);
        };
        mz.stateChanged(null);
        mapZoomSlider.addChangeListener(mz);
        p.add(new JLabel("Map Zoom"));
        p.add(mapZoomSlider);
        p.add(mapZoomVal);
        p.add(Box.createVerticalStrut(18));

        JButton reset = new JButton("Reset View");
        reset.addActionListener(e -> {
            scaleSlider.setValue(0);
            offsetXSlider.setValue(0);
            offsetZSlider.setValue(0);
            mapZoomSlider.setValue(16);
            mapToggle.setSelected(true);
            panel.setMapEnabled(true);
        });
        p.add(reset);
        p.add(Box.createVerticalGlue());
        return p;
    }

    public static void main(String[] args) {
        Space3D space = new Space3D(); // default origin
        space.setHalfExtentM(1000);

        AbstractAgent3D cam = new AbstractAgent3D("dvx-0", Agent3D.ObjectType.DVXPLORER) {
        };
        cam.setPositionDVX(new Space3D.Vec3(0, 0, 0));
        space.addAgent(cam);

        SwingUtilities.invokeLater(() -> new Space3DGUI(space));
    }
}
