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


package com.inilabs.jaer.projects.environ;

import com.inilabs.jaer.projects.gui.BasicDrawable;
import com.inilabs.jaer.projects.gui.Drawable;
import com.google.gson.annotations.Expose;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

    

/**
 * Represents a drawable waypoint in polar space (azimuth and elevation).
 */
    @JsonIgnoreProperties(ignoreUnknown = true)
public class WaypointDrawable extends BasicDrawable implements Drawable {
    @Expose private String name;
    @Expose private float azimuth;
    @Expose private float elevation;
   @Expose private Color color;     
   

    /**
     * Constructs a WaypointDrawable.
     *
     * @param name      Name of the waypoint.
     * @param azimuth   Azimuth angle in degrees.
     * @param elevation Elevation angle in degrees.
     * @param color     Color of the waypoint.
     */
    public WaypointDrawable(String name, float azimuth, float elevation, Color color) {
        this.name = name;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.color = color;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String getKey() {
        return "Waypoint_" + name; // Generates a unique key based on the name
    }

    /**
     * Draws the waypoint on the given Graphics object.
     *
     * @param g Graphics object to draw on.
     */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Transform coordinates based on azimuth and elevation
        int x = getCenterX() + (int) ((this.getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
        int y = getCenterY() - (int) ((this.getElevation() - getElevationHeading()) * getElevationScale());

        // Draw circle
        g2d.setColor(color);
        int radius = (int) (2 * getAzimuthScale()); // 2 degrees radius
        g2d.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);

        // Draw crosshair
        g2d.drawLine(x - radius, y, x + radius, y);
        g2d.drawLine(x, y - radius, x, y + radius);

        // Annotate with name and coordinates
        g2d.setColor(Color.BLACK);
        g2d.drawString(name, x - 15, y - radius - 10); // Name above the circle
        g2d.drawString(String.format("Key: %s", getKey()), x - 20, y + radius + 10);
        g2d.drawString(String.format("Az: %.2f, El: %.2f", this.getAzimuth(), this.getElevation()), x - 30, y + radius + 25);
    }
}