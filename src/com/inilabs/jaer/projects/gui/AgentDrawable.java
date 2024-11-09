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
import java.util.function.Consumer;

public class AgentDrawable implements Drawable {

    private final String key;
    private double azimuth;
    private double elevation;
    private double size = 5.0;  // Default size in degrees (diameter of circle)
    private Color color = Color.RED;
    private double azimuthScale = 1.0;
    private double elevationScale = 1.0;
    private Consumer<String> removeCallback;  // Callback for autonomous removal

    public AgentDrawable(String key) {
        this.key = key;
    }

    @Override
    public void draw(Graphics g) {
        int pixelSize = (int) (size * azimuthScale);
        int centerX = (int) (azimuth * azimuthScale) + (g.getClipBounds().width / 2);
        int centerY = (int) (-elevation * elevationScale) + (g.getClipBounds().height / 2);

        g.setColor(color);
        g.fillOval(centerX - pixelSize / 2, centerY - pixelSize / 2, pixelSize, pixelSize);

        g.setColor(Color.BLACK);
        g.drawLine(centerX - pixelSize / 2, centerY, centerX + pixelSize / 2, centerY);
        g.drawLine(centerX, centerY - pixelSize / 2, centerX, centerY + pixelSize / 2);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setAzimuth(double azimuthDegrees) {
        this.azimuth = azimuthDegrees;
    }

    @Override
    public double getAzimuth() {
        return azimuth;
    }

    @Override
    public void setElevation(double elevationDegrees) {
        this.elevation = elevationDegrees;
    }

    @Override
    public double getElevation() {
        return elevation;
    }

    @Override
    public void setSize(double sizeDegrees) {
        this.size = sizeDegrees;
    }

    @Override
    public double getSize() {
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
    public void onAzimuthScaleChanged(double azimuthScale) {
        this.azimuthScale = azimuthScale;
    }

    @Override
    public void onElevationScaleChanged(double elevationScale) {
        this.elevationScale = elevationScale;
    }

    @Override
    public void setRemoveCallback(Consumer<String> removeCallback) {
        this.removeCallback = removeCallback;
    }

    // Example autonomous removal method
    public void remove() {
        if (removeCallback != null) {
            removeCallback.accept(key);
        }
    }
}

