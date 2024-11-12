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

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolarSpaceDisplay extends JPanel {

    private float azimuthHeading = 0.0f; // Heading azimuth
    private float elevationHeading = 0.0f; // Heading elevation
    private float azimuthRange = 30.0f; // Azimuth range on either side of the heading
    private float elevationRange = 30.0f; // Elevation range on either side of the heading

    private final Map<String, Drawable> drawables = new HashMap<>(); // Map of drawables managed by key

    public PolarSpaceDisplay() {
        setBackground(Color.WHITE);
    }

    public void setHeading(float azimuth, float elevation) {
        this.azimuthHeading = azimuth;
        this.elevationHeading = elevation;
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
     * @param drawable The drawable to add.
     */
    public void addDrawable(Drawable drawable) {
        drawables.put(drawable.getKey(), drawable);
        notifyTransformListeners(); // Notify scaling listeners initially
        repaint();
    }

    /**
     * Removes a drawable by its key.
     * @param key The unique key of the drawable to remove.
     */
    public void removeDrawable(String key) {
        drawables.remove(key);
        repaint();
    }

     public List<String> getDrawableNames() {
        return new ArrayList<>(drawables.keySet());
    }
    
    /**
     * Updates all drawables with the latest scaling and translation offset.
     */
    private void notifyTransformListeners() {
        float azimuthScale = getAzimuthScale();
        float elevationScale = getElevationScale();

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        for (Drawable drawable : drawables.values()) {
            drawable.onTransformChanged(azimuthScale, elevationScale, azimuthHeading, elevationHeading, centerX, centerY);
        }
    }
    
     /**
     * Toggle visibility of paths for all drawables.
     * @param show true to show paths, false to hide
     */
    public void showPaths(boolean show) {
        for (Drawable drawable : drawables.values()) {
            drawable.showPath(show);
        }
        repaint();
    }
    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw red dot at the heading point
        g2d.setColor(Color.RED);
        g2d.fillOval(centerX - 3, centerY - 3, 6, 6);

        // Draw azimuth and elevation scale bars centered at the heading point
        ScaleBar azimuthScaleBar = new ScaleBar(true, (int) azimuthRange, getAzimuthScale(), azimuthHeading);
        azimuthScaleBar.draw(g2d, centerX, centerY);

        ScaleBar elevationScaleBar = new ScaleBar(false, (int) elevationRange, getElevationScale(), elevationHeading);
        elevationScaleBar.draw(g2d, centerX, centerY);

        // Simply call draw() on each Drawable, letting each one handle its position based on scaling
        for (Drawable drawable : drawables.values()) {
            drawable.draw(g2d);
        }
    }
}

