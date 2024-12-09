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
/**
 *
 * @author rjd
 */

import java.awt.Graphics;

public interface Drawable extends DrawableListener {
    
    // Method to draw the object, converting degrees to pixels as needed
    void draw(Graphics g);

    // Unique key for the drawable
    String getKey();

    // Setters and getters for position in degrees
    void setAzimuth(double azimuthDegrees);  // Set azimuth in degrees
    double getAzimuth();                     // Get azimuth in degrees

    void setElevation(double elevationDegrees);  // Set elevation in degrees
    double getElevation();                       // Get elevation in degrees
}

