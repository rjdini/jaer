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

import java.awt.geom.Point2D;

public interface ClusterAdapter {
    float getAzimuth();  // degrees in PolarSpace
    float getElevation(); // egrees in PolarSpace
    Point2D.Float getLocation(); // x, y in chip pixels space.
    String getKey(); // For unique identification
    boolean isVisible(); // for rendering
    void setIsVisible(boolean yes);
    long getLifeTime();  // has the lifetiem of this cluster expired?
    float getSize();
    boolean isRCTCluster();
    void resetLifeTime();
}