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

<<<<<<< HEAD
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

public class PolarSpaceDisplay extends JPanel implements MouseMotionListener {

    private float azimuthRange = 30.0f;
    private float elevationRange = 30.0f;
    private float azimuthHeading = 0.0f;
    private float elevationHeading = 0.0f;
    private int mouseX = -1;
    private int mouseY = -1;

    private final Map<String, Drawable> drawables = new HashMap<>();

    public PolarSpaceDisplay() {
        setBackground(Color.WHITE);
        addMouseMotionListener(this);
        initializeScaling();
    }

    private void initializeScaling() {
        float azimuthScale = getWidth() / (2 * azimuthRange);
        float elevationScale = getHeight() / (2 * elevationRange);

        for (Drawable drawable : drawables.values()) {
            drawable.onAzimuthScaleChanged(azimuthScale);
            drawable.onElevationScaleChanged(elevationScale);
        }
    }

    public void setAzimuthRange(float range) {
        this.azimuthRange = range;
        notifyScalingListeners();
        repaint();
    }

    public void setElevationRange(float range) {
        this.elevationRange = range;
        notifyScalingListeners();
        repaint();
=======
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public class PolarSpaceDisplay extends JPanel {

    private float azimuthHeading = 0.0f; // Heading azimuth
    private float elevationHeading = 0.0f; // Heading elevation
    private float azimuthRange = 30.0f; // Azimuth range on either side of the heading
    private float elevationRange = 30.0f; // Elevation range on either side of the heading

    private final Map<String, Drawable> drawables = new HashMap<>(); // Map of drawables managed by key

    public PolarSpaceDisplay() {
        setBackground(Color.WHITE);
         SwingUtilities.invokeLater(() -> repaint());
>>>>>>> working
    }

    public void setHeading(float azimuth, float elevation) {
        this.azimuthHeading = azimuth;
        this.elevationHeading = elevation;
<<<<<<< HEAD
        repaint();
    }

    public void addDrawable(Drawable drawable) {
        drawables.put(drawable.getKey(), drawable);

        float azimuthScale = getWidth() / (2 * azimuthRange);
        float elevationScale = getHeight() / (2 * elevationRange);
        drawable.onAzimuthScaleChanged(azimuthScale);
        drawable.onElevationScaleChanged(elevationScale);

        drawable.setRemoveCallback(this::removeDrawable);
        repaint();
    }

=======
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
>>>>>>> working
    public void removeDrawable(String key) {
        drawables.remove(key);
        repaint();
    }

<<<<<<< HEAD
    private void notifyScalingListeners() {
        float azimuthScale = getWidth() / (2 * azimuthRange);
        float elevationScale = getHeight() / (2 * elevationRange);

        for (Drawable drawable : drawables.values()) {
            drawable.onAzimuthScaleChanged(azimuthScale);
            drawable.onElevationScaleChanged(elevationScale);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
=======
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
>>>>>>> working

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

<<<<<<< HEAD
        int horizonY = centerY - (int) (0 * height / (2 * elevationRange));
        g.setColor(Color.GREEN);
        g.drawLine(0, horizonY, width, horizonY);

        int headingX = centerX + (int) (azimuthHeading * width / (2 * azimuthRange));
        int headingY = centerY - (int) (elevationHeading * height / (2 * elevationRange));

        g.setColor(Color.RED);
        g.fillOval(headingX - 5, headingY - 5, 10, 10);

        g.setColor(Color.BLACK);
        g.drawLine(0, headingY, width, headingY);
        g.drawLine(headingX, 0, headingX, height);

        drawTicks(g, headingX, headingY, width, height);

        for (Drawable drawable : drawables.values()) {
            drawable.draw(g);
        }

        if (mouseX != -1 && mouseY != -1) {
            drawCrosshair(g, centerX, centerY, width, height);
        }
    }

    private void drawTicks(Graphics g, int headingX, int headingY, int width, int height) {
        int tickLengthMajor = 10;
        int tickLengthMinor = 5;

        g.setColor(Color.BLACK);

        for (int i = (int) -azimuthRange; i <= azimuthRange; i += 5) {
            int xTick = headingX + (int) (i * (width / (2 * azimuthRange)));
            int tickLength = (i % 10 == 0) ? tickLengthMajor : tickLengthMinor;
            g.drawLine(xTick, headingY - tickLength, xTick, headingY + tickLength);
            if (i % 10 == 0) {
                g.drawString(Integer.toString(i + (int) azimuthHeading), xTick - 10, headingY + 3 * tickLength);
            }
        }

        for (int i = (int) -elevationRange; i <= elevationRange; i += 5) {
            int yTick = headingY - (int) (i * (height / (2 * elevationRange)));
            int tickLength = (i % 10 == 0) ? tickLengthMajor : tickLengthMinor;
            g.drawLine(headingX - tickLength, yTick, headingX + tickLength, yTick);
            if (i % 10 == 0) {
                g.drawString(Integer.toString(i + (int) elevationHeading), headingX + 3 * tickLength, yTick + 5);
            }
        }
    }

    private void drawCrosshair(Graphics g, int centerX, int centerY, int width, int height) {
        g.setColor(Color.GRAY);
        g.drawLine(0, mouseY, width, mouseY);
        g.drawLine(mouseX, 0, mouseX, height);

        int azimuth = (int) (azimuthHeading + ((mouseX - centerX) * azimuthRange * 2.0f / width));
        int elevation = (int) (elevationHeading - ((mouseY - centerY) * elevationRange * 2.0f / height));

        g.setColor(Color.BLACK);
        g.drawString("Azimuth: " + azimuth + "°, Elevation: " + elevation + "°", mouseX + 10, mouseY - 10);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Not used but required by MouseMotionListener
    }
}
=======
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

>>>>>>> working
