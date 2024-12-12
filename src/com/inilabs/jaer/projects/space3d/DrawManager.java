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
import java.util.HashMap;
import java.util.Map;

public class DrawManager {
    private final Map<String, Drawable> drawables = new HashMap<>();

    // Add a drawable to the manager
    public void add(String key, Drawable drawable) {
        drawables.put(key, drawable);
    }

    // Remove a drawable by key
    public void remove(String key) {
        drawables.remove(key);
    }

    // Draw all managed drawable objects
    public void drawAll(Graphics g) {
        for (Drawable drawable : drawables.values()) {
            drawable.draw(g);
        }
    }

    // Retrieve all drawables (for scale notifications)
    public Iterable<Drawable> getDrawables() {
        return drawables.values();
    }
}

