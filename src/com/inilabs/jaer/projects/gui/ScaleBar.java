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

package com.inilabs.jaer.projects.gui;

import java.awt.Color;
import java.awt.Graphics2D;

public class ScaleBar {
    private final boolean horizontal; // True for horizontal (azimuth), false for vertical (elevation)
    private final int range; // Range on either side of the heading point in degrees
    private final float scale; // Pixels per degree
    private final float heading; // Heading value to start the labels from

    public ScaleBar(boolean horizontal, int range, float scale, float heading) {
        this.horizontal = horizontal;
        this.range = range;
        this.scale = scale;
        this.heading = heading;
    }

    public void draw(Graphics2D g2d, int centerX, int centerY) {
        // Length of the scale bar on each side of the heading point
        int scaleBarLength = (int) (range * scale);
        int tickSpacing = (int) (10 * scale); // 10-degree intervals

        // Draw the scale bar centered at (centerX, centerY)
        g2d.setColor(Color.BLACK);
        if (horizontal) {
            // Draw horizontal scale bar
            g2d.drawLine(centerX - scaleBarLength, centerY, centerX + scaleBarLength, centerY);

            // Draw ticks and labels
            for (int i = 0; i <= range; i += 10) {
                int offset = (int) (i * scale);
                String positiveLabel = String.valueOf((int) (heading + i));
                String negativeLabel = String.valueOf((int) (heading - i));

                // Ticks and labels for positive side
                g2d.drawLine(centerX + offset, centerY - 5, centerX + offset, centerY + 5);
                g2d.drawString(positiveLabel, centerX + offset - 5, centerY + 20);

                // Ticks and labels for negative side
                g2d.drawLine(centerX - offset, centerY - 5, centerX - offset, centerY + 5);
                g2d.drawString(negativeLabel, centerX - offset - 10, centerY + 20);
            }
        } else {
            // Draw vertical scale bar
            g2d.drawLine(centerX, centerY - scaleBarLength, centerX, centerY + scaleBarLength);

            // Draw ticks and labels
            for (int i = 0; i <= range; i += 10) {
                int offset = (int) (i * scale);
                String positiveLabel = String.valueOf((int) (heading + i));
                String negativeLabel = String.valueOf((int) (heading - i));

                // Ticks and labels for positive side
                g2d.drawLine(centerX - 5, centerY - offset, centerX + 5, centerY - offset);
                g2d.drawString(positiveLabel, centerX + 10, centerY - offset + 5);

                // Ticks and labels for negative side
                g2d.drawLine(centerX - 5, centerY + offset, centerX + 5, centerY + offset);
                g2d.drawString(negativeLabel, centerX + 10, centerY + offset + 5);
            }
        }
    }
}
