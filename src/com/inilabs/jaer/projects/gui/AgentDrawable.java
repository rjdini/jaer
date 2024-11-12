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
<<<<<<< HEAD

=======
>>>>>>> working
package com.inilabs.jaer.projects.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
<<<<<<< HEAD
import java.util.function.Consumer;

public class AgentDrawable implements Drawable, DrawableListener {

    private final String key;
    private float azimuth;
    private float elevation;
    private float size = 5.0f;
    private Color color = Color.RED;
    private float azimuthScale = 1.0f;
    private float elevationScale = 1.0f;
    private Consumer<String> removeCallback;

    public AgentDrawable(String key) {
        this.key = key;
=======
import java.util.LinkedList;

public class AgentDrawable extends BasicDrawable {
    private boolean showPath = true;
    private final LinkedList<float[]> pathBuffer = new LinkedList<>();
    private int maxPathLength = 20; // Increased path length
    private float azimuthScale;
    private float elevationScale;
    private float azimuthHeading;
    private float elevationHeading;
    private int centerX;
    private int centerY;

    public AgentDrawable() {
        super();
        init();
    }

    private void init() {
        setSize(2f);
        setColor(Color.MAGENTA);
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    public void setMaxPathLength(int maxPathLength) {
        this.maxPathLength = maxPathLength;
    }

    @Override
    public void setAzimuth(float azimuth) {
        super.setAzimuth(azimuth);
        addCurrentPositionToPath();
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);
        addCurrentPositionToPath();
    }

    @Override
    protected void addCurrentPositionToPath() {
        if (pathBuffer.size() >= maxPathLength) {
            pathBuffer.removeFirst();
        }
        pathBuffer.addLast(new float[]{getAzimuth(), getElevation()});
    }

    @Override
    public void onTransformChanged(float azimuthScale, float elevationScale, float azimuthHeading, float elevationHeading, int centerX, int centerY) {
        this.azimuthScale = azimuthScale;
        this.elevationScale = elevationScale;
        this.azimuthHeading = azimuthHeading;
        this.elevationHeading = elevationHeading;
        this.centerX = centerX;
        this.centerY = centerY;
>>>>>>> working
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

<<<<<<< HEAD
        int pixelSize = (int) (size * azimuthScale);
        int centerX = (int) (azimuth * azimuthScale) + (g.getClipBounds().width / 2);
        int centerY = (int) (-elevation * elevationScale) + (g.getClipBounds().height / 2);

        g2d.setColor(color);
        g2d.fillOval(centerX - pixelSize / 2, centerY - pixelSize / 2, pixelSize, pixelSize);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(centerX - pixelSize / 2, centerY, centerX + pixelSize / 2, centerY);
        g2d.drawLine(centerX, centerY - pixelSize / 2, centerX, centerY + pixelSize / 2);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setAzimuth(float azimuthDegrees) {
        this.azimuth = azimuthDegrees;
    }

    @Override
    public float getAzimuth() {
        return azimuth;
    }

    @Override
    public void setElevation(float elevationDegrees) {
        this.elevation = elevationDegrees;
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

    // Scaling methods updated to float
    @Override
    public void onAzimuthScaleChanged(float azimuthScale) {
        this.azimuthScale = azimuthScale;
    }

    @Override
    public void onElevationScaleChanged(float elevationScale) {
        this.elevationScale = elevationScale;
    }

    @Override
    public void setRemoveCallback(Consumer<String> removeCallback) {
        this.removeCallback = removeCallback;
    }

    public void remove() {
        if (removeCallback != null) {
            removeCallback.accept(key);
        }
    }
=======
        // Calculate screen position based on the absolute polar coordinates
        int x = centerX + (int) ((getAzimuth() - azimuthHeading) * azimuthScale);
        int y = centerY - (int) ((getElevation() - elevationHeading) * elevationScale);

        // Draw the drawable as a circle
        g2d.setColor(color);
        int pixelSizeX = (int) (size * azimuthScale);
        int pixelSizeY = (int) (size * elevationScale);
        g2d.fillOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);

        // Draw the path if enabled
        if (showPath) {
            drawPath(g2d);
        }
    }

    
    protected void drawPath(Graphics2D g2d) {
    g2d.setColor(color); // Use the same color as the agent

    // Initialize previous point as null
    float[] previousPosition = null;

    for (float[] position : pathBuffer) {
        int pathX = centerX + (int) ((position[0] - azimuthHeading) * azimuthScale);
        int pathY = centerY - (int) ((position[1] - elevationHeading) * elevationScale);

        // Draw a small point at the path location
      //  g2d.fillOval(pathX - 2, pathY - 2, 4, 4);

        // Draw a line from the previous point to the current one, if there is a previous point
        if (previousPosition != null) {
            int prevX = centerX + (int) ((previousPosition[0] - azimuthHeading) * azimuthScale);
            int prevY = centerY - (int) ((previousPosition[1] - elevationHeading) * elevationScale);
            g2d.drawLine(prevX, prevY, pathX, pathY);
        }

        // Update previous position to current position
        previousPosition = position;
    }
}

>>>>>>> working
}
