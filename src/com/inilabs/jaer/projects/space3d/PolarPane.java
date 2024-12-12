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
import javax.swing.JPanel;

public class PolarPane extends JPanel implements DrawableListener {

    /**
     * @return the azimuthScale
     */
    public double getAzimuthScale() {
        return azimuthScale;
    }

    /**
     * @param azimuthScale the azimuthScale to set
     */
    public void setAzimuthScale(double azimuthScale) {
        this.azimuthScale = azimuthScale;
    }

    /**
     * @return the elevationScale
     */
    public double getElevationScale() {
        return elevationScale;
    }

    /**
     * @param elevationScale the elevationScale to set
     */
    public void setElevationScale(double elevationScale) {
        this.elevationScale = elevationScale;
    }

    private double azimuthScale;
    private double elevationScale;
    
    // Default azimuth and elevation ranges, matching the slider's initial values
    private final int defaultAzimuthRange = 90;
    private final int defaultElevationRange = 90;

    public PolarPane() {
        // Initialize scale values to match default slider ranges
        initializeScale();
    }

    // Method to initialize scales based on default ranges and component dimensions
    private void initializeScale() {
        int width = getWidth();
        int height = getHeight();
        
        if (width > 0 && height > 0) {  // Ensure component is sized
            this.setAzimuthScale(width / (double) (2 * defaultAzimuthRange));
            this.setElevationScale(height / (double) (2 * defaultElevationRange));
        }
    }

    @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    int width = getWidth();
    int height = getHeight();
    int centerX = width / 2;
    int centerY = height / 2;

    // Draw the green horizon line at 0 degrees elevation
    g.setColor(Color.GREEN);
    g.drawLine(0, centerY, width, centerY);

    // Draw axes and tick marks, converting from degrees to pixels
    drawTicks(g, centerX, centerY);
}

private void drawTicks(Graphics g, int centerX, int centerY) {
    int tickLengthMajor = 10;
    int tickLengthMinor = 5;
    g.setColor(Color.BLACK);

    for (int i = -90; i <= 90; i += 5) {
        int x = centerX + (int) (i * azimuthScale);
        int tickLength = (i % 10 == 0) ? tickLengthMajor : tickLengthMinor;
        g.drawLine(x, centerY - tickLength, x, centerY + tickLength);

        if (i % 10 == 0) {
            g.drawString(Integer.toString(i), x - 5, centerY + 3 * tickLength);
        }
    }

    for (int i = -90; i <= 90; i += 5) {
        int y = centerY - (int) (i * elevationScale);
        int tickLength = (i % 10 == 0) ? tickLengthMajor : tickLengthMinor;
        g.drawLine(centerX - tickLength, y, centerX + tickLength, y);

        if (i % 10 == 0) {
            g.drawString(Integer.toString(i), centerX + 3 * tickLength, y + 5);
        }
    }
}
    
    // Listener method to update scales whenever a scale change occurs
    @Override
    public void onScaleUpdate(double azimuthScale, double elevationScale) {
        this.setAzimuthScale(azimuthScale);
        this.setElevationScale(elevationScale);
        repaint();
    }
}



