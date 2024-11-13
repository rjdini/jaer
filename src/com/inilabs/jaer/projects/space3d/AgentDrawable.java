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





package com.inilabs.jaer.projects.space3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class AgentDrawable implements Drawable {
    private String key;
    private double azimuthDegrees;  // Azimuth in degrees
    private double elevationDegrees; // Elevation in degrees
    private Color color;
    private int size; // Size in pixels
    private double azimuthScale = 1.0;  // Scale for converting degrees to pixels
    private double elevationScale = 1.0;

    public AgentDrawable() {
        super();
    }
    
    public AgentDrawable(String key, double azimuthDegrees, double elevationDegrees, Color color, int size) {
        this.key = key;
        this.azimuthDegrees = azimuthDegrees;
        this.elevationDegrees = elevationDegrees;
        this.color = color;
        this.size = size;
    }

  
    
    
    
    @Override
    public void draw(Graphics g) {
        // Convert azimuth and elevation from degrees to pixels
        int x = (int) (azimuthDegrees * azimuthScale);
        int y = (int) (-elevationDegrees * elevationScale);  // Invert y-axis for correct orientation

        // Center the drawable based on its size
        g.setColor(color);
        g.fillOval(x - size / 2, y - size / 2, size, size);
    }

    @Override
    public void onScaleUpdate(double azimuthScale, double elevationScale) {
        this.azimuthScale = azimuthScale;
        this.elevationScale = elevationScale;
    }

    @Override
    public String getKey() {
        return key;
    }

    // Setters and getters for azimuth and elevation in degrees
    @Override
    public void setAzimuth(double azimuthDegrees) {
        this.azimuthDegrees = azimuthDegrees;
    }

    @Override
    public double getAzimuth() {
        return azimuthDegrees;
    }

    @Override
    public void setElevation(double elevationDegrees) {
        this.elevationDegrees = elevationDegrees;
    }

    @Override
    public double getElevation() {
        return elevationDegrees;
    }
}
