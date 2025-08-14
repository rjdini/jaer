/*
 * Copyright (C) 2025 rjd.
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
package com.inilabs.jaer.projects.utils;

/**
 *
 * @author rjd
 */

import java.awt.geom.Point2D;

public class Vector2DUtil {

    // Compute magnitude (length) of a vector
    public static double magnitude(Point2D p) {
        return Math.hypot(p.getX(), p.getY());
    }

    // Compute dot product of two vectors
    public static double dot(Point2D a, Point2D b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    // Compute angle between vectors in degrees
    public static double angleBetween(Point2D a, Point2D b) {
        double dot = dot(a, b);
        double magA = magnitude(a);
        double magB = magnitude(b);
        if (magA == 0 || magB == 0) return 0;
        double cosTheta = dot / (magA * magB);
        // Clamp to avoid NaN from rounding errors
        cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));
        return Math.toDegrees(Math.acos(cosTheta));
    }

    // Normalize a vector to unit length
    public static Point2D normalize(Point2D p) {
        double mag = magnitude(p);
        if (mag == 0) return new Point2D.Double(0, 0);
        return new Point2D.Double(p.getX() / mag, p.getY() / mag);
    }

    // Add two vectors
    public static Point2D add(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
    }

    // Subtract b from a
    public static Point2D subtract(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
    }

    // Scale a vector by a scalar
    public static Point2D scale(Point2D p, double scalar) {
        return new Point2D.Double(p.getX() * scalar, p.getY() * scalar);
    }
}
