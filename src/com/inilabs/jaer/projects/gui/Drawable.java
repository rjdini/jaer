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

/**
 *
 * @author rjd
 */

package com.inilabs.jaer.projects.gui;

import java.awt.Color;
import java.awt.Graphics;
<<<<<<< HEAD
import java.util.function.Consumer;

public interface Drawable extends DrawableListener {

    // Method to draw the object, converting degrees to pixels as needed
    void draw(Graphics g);

    // Unique key for the drawable
    String getKey();

=======
import java.awt.Graphics2D;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.inilabs.jaer.projects.gui.ActionType;

public interface Drawable extends DrawableListener {

    // Unique key for the drawable
    String getKey();
  
    int getId();
    
    // Method to draw the object, converting degrees to pixels as needed
    void draw(Graphics g);
    
    void showPath(boolean yes) ;
    
    //void updateScale(float azimuthScale, float elevationScale);
  
>>>>>>> working
    // Setters and getters for position in degrees
    void setAzimuth(float azimuthDegrees);  // Set azimuth in degrees
    float getAzimuth();                     // Get azimuth in degrees

    void setElevation(float elevationDegrees);  // Set elevation in degrees
    float getElevation();                       // Get elevation in degrees

    // Setters and getters for size and color
    void setSize(float sizeDegrees);  // Set size in degrees
    float getSize();                  // Get size in degrees

    void setColor(Color color);  // Set color
    Color getColor();            // Get color

<<<<<<< HEAD
    // Set a callback for self-removal
    void setRemoveCallback(Consumer<String> removeCallback);
=======
    // Set a callback 
    void setParentCallback(BiConsumer<ActionType, String> parentCallback) ;
    
    void close();
 
>>>>>>> working
}
