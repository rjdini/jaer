/*
 * Copyright (C) 2024 rjd.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 PolarSpaceDisplay settings.
 */
package com.inilabs.jaer.projects.gui;

import com.inilabs.jaer.projects.environ.WaypointDrawable;
import com.inilabs.jaer.projects.environ.WaypointManager;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.slf4j.LoggerFactory;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.JFrame;

public class PolarSpaceDisplay extends JPanel {

    private float azimuthHeading = 0.0f; // Heading azimuth
    private float elevationHeading = 0.0f; // Heading elevation
    private float azimuthRange = 30.0f; // Azimuth range on either side of the heading
    private float elevationRange = 30.0f; // Elevation range on either side of the heading
    private JLabel coordinatesLabel;
    private static PolarSpaceDisplay instance = null;
    private BiConsumer<Float, Float> waypointAdder; // Optional callback for adding waypoints
    private Consumer<WaypointDrawable> waypointEditor;
    private Consumer<WaypointDrawable> waypointRemover;

    private final Map<String, Drawable> drawables = Collections.synchronizedMap(new HashMap<>());

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(PolarSpaceDisplay.class);

    private PolarSpaceDisplay() {
        initializePolarSpatialData();
        initializeDisplay();
        registerListeners();
        SwingUtilities.invokeLater(() -> repaint());
    }

    public static PolarSpaceDisplay getInstance() {
        if (instance == null) {
            instance = new PolarSpaceDisplay();
        }
        return instance;
    }

    public void refresh() {
        notifyTransformListeners(); // update drawables scaling etc
        repaint();
    }

    private void initializePolarSpatialData() {
        setAzimuthHeading(0.0f); // Heading azimuth with respect to RS4 origin  (yaw=0, roll=0, pitch=0) 
        setElevationHeading(0.0f); // Heading elevation wrt RS4 origin 
        azimuthRange = 60.0f; // Azimuth range on either side of the heading
        elevationRange = 60.0f; // Elevation range on either side of the heading
    }

    private void initializeDisplay() {

        setPreferredSize(new Dimension(1000, 800));
        setBackground(Color.WHITE);

        // Add a label to display coordinates
        coordinatesLabel = new JLabel("Coordinates: ");
        coordinatesLabel.setForeground(Color.WHITE);
        add(coordinatesLabel);

        // Enable tooltips and set custom behavior
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000); // Tooltip disappears after 5 seconds
        ToolTipManager.sharedInstance().registerComponent(this);

    }

    private void registerListeners() {
        // Attach mouse listener for click-based tooltips
        addMouseListener(new MouseAdapter() {
            
           @Override
            public void mouseClicked(MouseEvent e) {
               showTooltipOnClick(e);
          
                int x = e.getX();
                int y = e.getY();
                float azimuth = (x - getCenterX()) / getAzimuthScale();
                float elevation = (getCenterY() - y) / getElevationScale();

                if (SwingUtilities.isLeftMouseButton(e) && waypointAdder != null) {
                    waypointAdder.accept(azimuth, elevation);
                } else if (SwingUtilities.isMiddleMouseButton(e) && waypointEditor != null) {
                    WaypointDrawable closestWaypoint = findClosestWaypoint(azimuth, elevation, 5.0f);
                    if (closestWaypoint != null) {
                        waypointEditor.accept(closestWaypoint);
                    }
                } else if (SwingUtilities.isRightMouseButton(e) && waypointRemover != null) {
                    WaypointDrawable closestWaypoint = findClosestWaypoint(azimuth, elevation, 5.0f);
                    if (closestWaypoint != null) {
                        waypointRemover.accept(closestWaypoint);
                    }
                }
            }
        });

    }

    private WaypointDrawable findClosestWaypoint(float azimuth, float elevation, float tolerance) {
        WaypointDrawable closestWaypoint = null;
        float minDistance = Float.MAX_VALUE;

        for (WaypointDrawable waypoint : WaypointManager.getInstance().getAllWaypoints().values()) {
            float distance = (float) Math.sqrt(
                    Math.pow(waypoint.getAzimuth() - azimuth, 2)
                    + Math.pow(waypoint.getElevation() - elevation, 2)
            );

            if (distance < minDistance && distance <= tolerance) {
                minDistance = distance;
                closestWaypoint = waypoint;
            }
        }
        return closestWaypoint;
    }

    public void setWaypointAdder(BiConsumer<Float, Float> waypointAdder) {
        this.waypointAdder = waypointAdder; // Register a waypoint manager or any handler
    }

    public void setWaypointEditor(Consumer<WaypointDrawable> waypointEditor) {
        this.waypointEditor = waypointEditor;
    }

    public void setWaypointRemover(Consumer<WaypointDrawable> waypointRemover) {
        this.waypointRemover = waypointRemover;
    }

    /**
     * Re-initializes the display to its default state.
     */
    public synchronized void reinitializeDisplay() {
        drawables.clear();
        initializeDisplay();
        log.info("PolarSpaceDisplay re-initialized.");
        repaint();
    }

    /**
     * Clears orphaned drawables that are no longer valid.
     */
    public synchronized void clearOrphanedDrawables() {
        List<String> orphans = new ArrayList<>();
        for (Map.Entry<String, Drawable> entry : drawables.entrySet()) {
            if (entry.getValue().isOrphaned()) { // Assuming Drawable has an `isOrphaned` method
                orphans.add(entry.getKey());
            }
        }
        for (String key : orphans) {
            drawables.remove(key);
            log.info("Removed orphaned drawable with key: {}", key);
        }
        repaint();
    }

    private void showTooltipOnClick(MouseEvent e) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Calculate azimuth and elevation based on clicked coordinates
        float azimuth = ((e.getX() - centerX) / getAzimuthScale()) + getAzimuthHeading();
        float elevation = ((centerY - e.getY()) / getElevationScale()) + getElevationHeading();

        // Update tooltip text
        setToolTipText(String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°",
                e.getX(), e.getY(), azimuth, elevation));

        // Manually show the tooltip by forcing a repaint (ensures visibility on click)
        SwingUtilities.invokeLater(() -> ToolTipManager.sharedInstance().mousePressed(e));
    }

    @Override
    public Point getToolTipLocation(MouseEvent e) {
        // Position the tooltip above the mouse cursor
        return new Point(e.getX(), e.getY() - 25); // Adjust height as needed
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        // Provide tooltip text dynamically
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        float azimuth = ((e.getX() - centerX) / getAzimuthScale()) + getAzimuthHeading();
        float elevation = ((centerY - e.getY()) / getElevationScale()) + getElevationHeading();

        return String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°",
                e.getX(), e.getY(), azimuth, elevation);
    }

    private void reportCoordinates(int x, int y) {
        // Obtain the current transformation parameters from PolarSpaceDisplay
        float azimuthScale = getAzimuthScale();          // Pixels per degree for azimuth
        float elevationScale = getElevationScale();      // Pixels per degree for elevation
        float azimuthHeading = this.getAzimuthHeading();      // Current azimuth heading in degrees
        float elevationHeading = this.getElevationHeading();  // Current elevation heading in degrees

        int centerX = getWidth() / 2; // Horizontal center of the display in pixels
        int centerY = getHeight() / 2; // Vertical center of the display in pixels

        // Reverse the transformation to compute azimuth and elevation
        float azimuth = ((x - centerX) / azimuthScale) + azimuthHeading;
        float elevation = ((centerY - y) / elevationScale) + elevationHeading;

        // Update the coordinates label
        coordinatesLabel.setText(String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°",
                x, y, azimuth, elevation));

        // Update the tooltip with the calculated values
        setToolTipText(String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°",
                x, y, azimuth, elevation));

        // Optional: Log the coordinates for debugging
        System.out.printf("Mouse at X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°%n",
                x, y, azimuth, elevation);
    }

//    @Override
//public Dimension getPreferredSize() {
//    return new Dimension(800, 600);
//}
    public void shutdown() {

    }

    public void setAzimuthHeading(float azimuth) {
        this.azimuthHeading = azimuth;
        notifyTransformListeners();
    }

    public void setElevationHeading(float elevation) {
        this.elevationHeading = elevation;
        notifyTransformListeners();
    }

    public void setHeading(float azimuth, float elevation) {
        this.setAzimuthHeading(azimuth);
        this.setElevationHeading(elevation);
        notifyTransformListeners();
        repaint();
    }

    public void setAzimuthRange(float range) {
        this.azimuthRange = range;
        notifyTransformListeners();
        repaint();
    }

    public float getAzimuthRange() {
        return azimuthRange;
    }

    public void setElevationRange(float range) {
        this.elevationRange = range;
        notifyTransformListeners();
        repaint();
    }

    public float getElevationRange() {
        return elevationRange;
    }

    public float getAzimuthScale() {
        return (float) getWidth() / (2 * azimuthRange); // Pixels per degree for azimuth
    }

    public float getElevationScale() {
        return (float) getHeight() / (2 * elevationRange); // Pixels per degree for elevation
    }

    /**
     * Adds a drawable to the display.
     *
     * @param drawable The drawable to add.
     */
    public synchronized void addDrawable(Drawable drawable) {
        drawables.put(drawable.getKey(), drawable);

        // Set the callback to remove the drawable
        drawable.setParentCallback((action, key) -> {
            if (action == ActionType.REMOVE) {
                removeDrawable(key);
            }
        });

        notifyTransformListeners();
        repaint();
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    /**
     * Removes a drawable by its key.
     *
     * @param key The unique key of the drawable to remove.
     */
    public synchronized void removeDrawable(String key) {
        drawables.remove(key);
        repaint();
    }

    public synchronized List<String> getDrawableNames() {
        return new ArrayList<>(drawables.keySet());
    }

    /**
     * Retrieves a drawable by its unique key.
     *
     * @param key The key of the drawable to retrieve.
     * @return The drawable if found, or null if no drawable exists with the
     * given key.
     */
    public synchronized Drawable getDrawableByKey(String key) {
        return drawables.get(key);
    }

    /**
     * Updates all drawables with the latest scaling and translation offset.
     */
    public void notifyTransformListeners() {

        for (Drawable drawable : drawables.values()) {
            drawable.onTransformChanged(getAzimuthScale(), getElevationScale(), getAzimuthHeading(), getElevationHeading(), getCenterX(), getCenterY());
        }
    }

    /**
     * Checks if a drawable is already present in the display by its unique key.
     *
     * @param drawable The drawable to check.
     * @return true if the drawable is present, false otherwise.
     */
    public synchronized boolean containsDrawable(Drawable drawable) {
        return drawables.containsKey(drawable.getKey());
    }

    /**
     * Toggle visibility of paths for all drawables.
     *
     * @param show true to show paths, false to hide
     */
    public void showPaths(boolean show) {
        for (Drawable drawable : drawables.values()) {
            drawable.showPath(show);
        }
        repaint();
    }

    public synchronized void removeAllDrawables() {
        drawables.clear();
        repaint();
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        notifyTransformListeners();

        int horizonY = getCenterY() - (int) ((0 - getElevationHeading() * getElevationScale()));

        // Paint the upper half pale blue
        g2d.setColor(new Color(173, 216, 230, 100)); // Pale blue with alpha for translucency
        g2d.fillRect(0, 0, getWidth(), horizonY);

        // Paint the lower half pale green
        g2d.setColor(new Color(144, 238, 144, 100)); // Pale green with alpha for translucency
        g2d.fillRect(0, horizonY, getWidth(), getHeight() - horizonY);

        // Draw red dot at the heading point
        g2d.setColor(Color.RED);
        g2d.fillOval(getCenterX() - 3, getCenterY() - 3, 6, 6);

        // Draw azimuth and elevation scale bars centered at the heading point
        ScaleBar azimuthScaleBar = new ScaleBar(true, (int) azimuthRange, getAzimuthScale(), getAzimuthHeading());
        azimuthScaleBar.draw(g2d, getCenterX(), getCenterY());

        ScaleBar elevationScaleBar = new ScaleBar(false, (int) elevationRange, getElevationScale(), getElevationHeading());
        elevationScaleBar.draw(g2d, getCenterX(), getCenterY());

        // Draw all drawables
        for (Drawable drawable : drawables.values()) {
            try {
                drawable.draw(g2d);
            } catch (Exception e) {
                log.error("Error drawing drawable with key: {}", drawable.getKey(), e);
            }
        }

        // Draw crosshairs or grid if needed
        g2d.setColor(Color.GRAY);
        g2d.drawLine(getCenterX(), 0, getCenterX(), getHeight()); // Vertical line
        g2d.drawLine(0, getCenterY(), getWidth(), getCenterY()); // Horizontal line
    }

    /**
     * @return the azimuthHeading
     */
    public float getAzimuthHeading() {
        return azimuthHeading;
    }

    /**
     * @return the elevationHeading
     */
    public float getElevationHeading() {
        return elevationHeading;
    }

    /**
     * @return the centerX
     */
    public int getCenterX() {
        return getWidth() / 2;
    }

    /**
     * @return the centerY
     */
    public int getCenterY() {
        return getHeight() / 2;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CustomPanel Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new PolarSpaceDisplay());
            frame.pack();
            frame.setVisible(true);
        });
    }

}
