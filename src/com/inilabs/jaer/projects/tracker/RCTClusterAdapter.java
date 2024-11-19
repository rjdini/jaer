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
package com.inilabs.jaer.projects.tracker;

import com.inilabs.jaer.projects.tracker.ClusterAdapter;
import java.awt.geom.Point2D;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;

public class RCTClusterAdapter implements ClusterAdapter {
    private final RectangularClusterTracker.Cluster cluster;
    private static final FieldOfView fov = FieldOfView.getInstance();
    private boolean isVisible = true;

    public RCTClusterAdapter(RectangularClusterTracker.Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public float getAzimuth() {
        return (float) fov.getYawAtPixel((float)cluster.getLocation().getX());
    }

    @Override
    public float getElevation() {
        return (float) fov.getPitchAtPixel((float)cluster.getLocation().getY());
    }

    @Override
    public Point2D.Float getLocation() {
        return  cluster.getLocation();
    }
    
    @Override
    public String getKey() {
        return "Cluster-" + cluster.hashCode();
    }
    
    @Override
    public void setIsVisible(boolean yes) {
        isVisible = yes;
    }
    
    @Override
    public boolean isVisible() {
        return isVisible;
    }

}

