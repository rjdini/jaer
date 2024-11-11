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

public class PolarSpaceDisplay extends JPanel {

    private float azimuthHeading = 0.0f; // Heading azimuth
    private float elevationHeading = 0.0f; // Heading elevation
    private float azimuthRange = 30.0f; // Azimuth range on either side of the heading
    private float elevationRange = 30.0f; // Elevation range on either side of the heading

    public PolarSpaceDisplay() {
        setBackground(Color.WHITE);
    }

    public void setHeading(float azimuth, float elevation) {
        this.azimuthHeading = azimuth;
        this.elevationHeading = elevation;
        repaint();
    }

    public void setAzimuthRange(float range) {
        this.azimuthRange = range;
        repaint();
    }

    public void setElevationRange(float range) {
        this.elevationRange = range;
        repaint();
    }

    public float getAzimuthScale() {
        return (float) getWidth() / (2 * azimuthRange); // Pixels per degree for azimuth
    }

    public float getElevationScale() {
        return (float) getHeight() / (2 * elevationRange); // Pixels per degree for elevation
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

        // Create and draw azimuth and elevation scale bars, centered at the heading point
        ScaleBar azimuthScaleBar = new ScaleBar(true, (int) azimuthRange, getAzimuthScale(), azimuthHeading);
        azimuthScaleBar.draw(g2d, centerX, centerY);

        ScaleBar elevationScaleBar = new ScaleBar(false, (int) elevationRange, getElevationScale(), elevationHeading);
        elevationScaleBar.draw(g2d, centerX, centerY);
    }
}

