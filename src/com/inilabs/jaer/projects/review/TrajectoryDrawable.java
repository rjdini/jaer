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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrajectoryDrawable extends BasicDrawable implements Drawable {
    private final String trackerName;
    private final List<TrajectoryPointDrawable> points;
    private final List<String> clusters; // List of associated clusters

    public TrajectoryDrawable(String trackerName) {
        this.trackerName = trackerName;
        this.points = new ArrayList<>();
        this.clusters = new ArrayList<>();
    }

    public String getTrackerName() {
        return trackerName;
    }

    public List<TrajectoryPointDrawable> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public void addPoint(TrajectoryPointDrawable point) {
        points.add(point);
    }

    public List<String> getClusters() {
        return Collections.unmodifiableList(clusters);
    }

    public void addCluster(String cluster) {
        if (!clusters.contains(cluster)) {
            clusters.add(cluster);
        }
    }

    public void addClusters(List<String> newClusters) {
        for (String cluster : newClusters) {
            addCluster(cluster);
        }
    }

    public void removeCluster(String cluster) {
        clusters.remove(cluster);
    }

    public void clearClusters() {
        clusters.clear();
    }

    public void draw(Graphics g) {
        if (points.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;

        // Draw the trajectory
        TrajectoryPointDrawable previousPoint = null;
        for (int i = 0; i < points.size(); i++) {
            TrajectoryPointDrawable currentPoint = points.get(i);

            // Draw a line connecting to the previous point
            if (previousPoint != null) {
                int x1 = centerX + (int) ((previousPoint.getAzimuth() - azimuthHeading) * azimuthScale);
                int y1 = centerY - (int) ((previousPoint.getElevation() - elevationHeading) * elevationScale);

                int x2 = centerX + (int) ((currentPoint.getAzimuth() - azimuthHeading) * azimuthScale);
                int y2 = centerY - (int) ((currentPoint.getElevation() - elevationHeading) * elevationScale);

                g2d.drawLine(x1, y1, x2, y2);
            }

            // Draw the first point with the tracker name as a label
            if (i == 0) {
                int x = centerX + (int) ((currentPoint.getAzimuth() - azimuthHeading) * azimuthScale);
                int y = centerY - (int) ((currentPoint.getElevation() - elevationHeading) * elevationScale);

                g2d.drawString(trackerName, x + 5, y - 5); // Label with the tracker name
            }

            // Draw the current point itself
            currentPoint.draw(g);

            previousPoint = currentPoint;
        }

        // Draw associated clusters
        if (!clusters.isEmpty()) {
            int x = centerX + 10; // Example offset for cluster info
            int y = centerY + 20; // Example offset for cluster info
            g2d.drawString("Clusters: " + String.join(", ", clusters), x, y);
        }
    }
}

