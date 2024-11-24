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

package com.inilabs.jaer.projects.review;

import com.inilabs.jaer.projects.gui.BasicDrawable;
import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import java.awt.*;
import java.util.List;

public class TrajectoryDrawable  extends BasicDrawable implements Drawable {
    private final String trackerName;
    private final List<TrajectoryPointDrawable> points;
    private boolean visible;
    private Color color;

    public TrajectoryDrawable(String trackerName, List<TrajectoryPointDrawable> points) {
        this.trackerName = trackerName;
        this.points = points;
        this.visible = true;
        this.color = Color.BLUE; // Default trajectory color
    }

    public void close() {
    }
    
    public boolean isOrphaned() {
        return false;
    }
    
    public boolean isExpired() {
        return false;
    }
    
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setColor(Color color) {
        this.color = color;
        for (TrajectoryPointDrawable point : points) {
            point.setColor(color); // Ensure all points share the trajectory's color
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!visible) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);

        TrajectoryPointDrawable previousPoint = null;
        for (TrajectoryPointDrawable point : points) {
            if (previousPoint != null) {
                // Draw line connecting consecutive points
                int x1 = centerX + (int) ((previousPoint.getAzimuth() - azimuthHeading) * azimuthScale);
                int y1 = centerY - (int) ((previousPoint.getElevation() - elevationHeading) * elevationScale);

                int x2 = centerX + (int) ((point.getAzimuth() - azimuthHeading) * azimuthScale);
                int y2 = centerY - (int) ((point.getElevation() - elevationHeading) * elevationScale);

                g2d.drawLine(x1, y1, x2, y2);
            }
            // Draw the individual point
            point.draw(g);
            previousPoint = point;
        }
    }

    @Override
    public String toString() {
        return "TrajectoryDrawable{" +
                "trackerName='" + trackerName + '\'' +
                ", points=" + points +
                '}';
    }
}
