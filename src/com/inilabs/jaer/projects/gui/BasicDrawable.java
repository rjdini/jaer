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
 */

package com.inilabs.jaer.projects.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.function.BiConsumer;

public class BasicDrawable implements Drawable, DrawableListener {

    private static int idCounter = 0; // Auto-incrementing ID counter for instances
    private final String key;
    private final int id; // Unique ID for this instance
    private float azimuth = 0.0f;
    private float elevation = 0.0f;
    private float size = 5.0f;
    private Color color = Color.RED;
    private float azimuthScale = 1.0f;
    private float elevationScale = 1.0f;
    private boolean showPath = false;
    private BiConsumer<ActionType, String> parentCallback;

    // Path buffer for recent positions
    private final LinkedList<float[]> pathBuffer = new LinkedList<>();
    private final int maxPathLength = 20;

    // Default constructor, places object at (0,0) and auto-generates key
    public BasicDrawable() {
        this.id = ++idCounter;
        this.key = getClass().getSimpleName() + id;
    }

    // Constructor with specific azimuth and elevation, and optional key
    public BasicDrawable(String key, float initialAzimuth, float initialElevation) {
        this.id = ++idCounter;
        this.key = key != null ? key : getClass().getSimpleName() + id;
        this.azimuth = initialAzimuth;
        this.elevation = initialElevation;
    }

    public int getId() {
        return id;
    }
    // Drawable interface method to draw the drawable on the Graphics context
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Calculate position based on azimuth and elevation scales
        int centerX = g.getClipBounds().width / 2;
        int centerY = g.getClipBounds().height / 2;
        int x = centerX + (int) (azimuth * azimuthScale);
        int y = centerY - (int) (elevation * elevationScale);

        // Draw the drawable as a circle
        g2d.setColor(color);
        int pixelSize = (int) (size * azimuthScale);
        g2d.drawOval(x - pixelSize / 2, y - pixelSize / 2, pixelSize, pixelSize);

        // Draw the path if enabled
        if (showPath) {
            drawPath(g2d, centerX, centerY);
        }

        // Update path buffer with the current position
    //   addCurrentPositionToPath();  updated only through update azimuth / elevation
    }

    private void drawPath(Graphics2D g2d, int centerX, int centerY) {
        g2d.setColor(Color.GRAY);
        float[] previousPosition = null;

        for (float[] position : pathBuffer) {
            if (previousPosition != null) {
                int previousX = centerX + (int) (previousPosition[0] * azimuthScale);
                int previousY = centerY - (int) (previousPosition[1] * elevationScale);
                int currentX = centerX + (int) (position[0] * azimuthScale);
                int currentY = centerY - (int) (position[1] * elevationScale);
                g2d.drawLine(previousX, previousY, currentX, currentY);
            }
            previousPosition = position;
        }
    }

    protected void addCurrentPositionToPath() {
        if (pathBuffer.size() >= maxPathLength) {
            pathBuffer.removeFirst();
        }
        pathBuffer.addLast(new float[]{azimuth, elevation});
    }

    // Implementation of DrawableListener methods for scale updates
    @Override
    public void onAzimuthScaleChanged(float azimuthScale) {
        this.azimuthScale = azimuthScale;
    }

    @Override
    public void onElevationScaleChanged(float elevationScale) {
        this.elevationScale = elevationScale;
    }

    // Drawable interface methods for setting properties
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setAzimuth(float azimuthDegrees) {
        this.azimuth = azimuthDegrees;
        addCurrentPositionToPath();
    }

    @Override
    public float getAzimuth() {
        return azimuth;
    }

    @Override
    public void setElevation(float elevationDegrees) {
        this.elevation = elevationDegrees;
        addCurrentPositionToPath();
    }

    @Override
    public float getElevation() {
        return elevation;
    }

    @Override
    public void setSize(float sizeDegrees) {
        this.size = sizeDegrees;
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void showPath(boolean yes) {
        this.showPath = yes;
    }

    @Override
    public void setParentCallback(BiConsumer<ActionType, String> parentCallback) {
        this.parentCallback = parentCallback;
    }

    // Protected getters for subclasses
    protected float getAzimuthScale() {
        return azimuthScale;
    }

    protected float getElevationScale() {
        return elevationScale;
    }


    public boolean isPathVisible() {
        return showPath;
    }

    protected void drawPath(Graphics2D g2d) {
        drawPath(g2d, g2d.getClipBounds().width / 2, g2d.getClipBounds().height / 2);
    }
}
