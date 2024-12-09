/*
 * Copyright (C) 2024 rjd.
 *
 * This library is free software; you can redistribute it and/orcluster
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
package com.inilabs.jaer.projects.review;

import com.inilabs.jaer.projects.gui.BasicDrawable;
import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.*;

public class TrajectoryPointDrawable extends BasicDrawable implements Drawable {
 
    private long timestamp; // Timestamp -- either jaer timestamp, or systtime in millisec
    private Color color;

    public TrajectoryPointDrawable(float azimuth, float elevation, long timestamp) {
        this.setAzimuth(azimuth);
        this.setElevation(elevation);
        this.setTimestamp(timestamp);
        this.setColor(Color.BLUE); // Default color
    }

    public float getAzimuth() {
        return getAzimuth();
    }

    public float getElevation() {
        return getElevation();
    }

    public long getTime() {
        return timestamp;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int x = getCenterX() + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
        int y = getCenterY() - (int) ((getElevation() - getElevationHeading()) * getElevationScale());

        g2d.setColor(color);
        int size = 5; // Fixed size for individual points
        g2d.fillOval(x - size / 2, y - size / 2, size, size);

        // Optionally annotate the point with its timestamp
     //   g2d.drawString(String.format("%d", timestamp), x + 5, y - 5);
    }

    @Override
    public String toString() {
        return String.format("Azimuth: %.2f, Elevation: %.2f, Time: %d", getAzimuth(), getElevation(), timestamp);
    }
}

