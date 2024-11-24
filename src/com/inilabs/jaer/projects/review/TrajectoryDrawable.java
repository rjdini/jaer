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

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrajectoryDrawable {
    private final String trackerName;
    private final List<TrajectoryPointDrawable> points;

    public TrajectoryDrawable(String trackerName) {
        this.trackerName = trackerName;
        this.points = new ArrayList<>();
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

    public void draw(Graphics g) {
        for (TrajectoryPointDrawable point : points) {
            point.draw(g);
        }
    }
}
