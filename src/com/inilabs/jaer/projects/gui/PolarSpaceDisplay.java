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
 PolarSpaceDisplay settings.
*/
package com.inilabs.jaer.projects.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

public class PolarSpaceDisplay extends JPanel implements MouseMotionListener {

    private int azimuthRange = 30;
    private int elevationRange = 30;
    private int azimuthHeading = 0;
    private int elevationHeading = 0;
    private int mouseX = -1;
    private int mouseY = -1;

    // Map to store drawables by their unique key
    private final Map<String, Drawable> drawables = new HashMap<>();

    public PolarSpaceDisplay() {
        setBackground(Color.WHITE);
        addMouseMotionListener(this);
    }

    public void setAzimuthRange(int range) {
        this.azimuthRange = range;
        notifyScalingListeners();
        repaint();
    }

    public void setElevationRange(int range) {
        this.elevationRange = range;
        notifyScalingListeners();
        repaint();
    }

    public void setHeading(int azimuth, int elevation) {
        this.azimuthHeading = azimuth;
        this.elevationHeading = elevation;
        repaint();
    }

    public void addDrawable(Drawable drawable) {
        drawables.put(drawable.getKey(), drawable);

        // Set up scaling for the new drawable
        double azimuthScale = getWidth() / (double) (2 * azimuthRange);
        double elevationScale = getHeight() / (double) (2 * elevationRange);
        drawable.onAzimuthScaleChanged(azimuthScale);
        drawable.onElevationScaleChanged(elevationScale);

        // Provide callback for self-removal
        drawable.setRemoveCallback(this::removeDrawable);
        repaint();
    }

    public void removeDrawable(String key) {
        drawables.remove(key);
        repaint();
    }

    private void notifyScalingListeners() {
        double azimuthScale = getWidth() / (double) (2 * azimuthRange);
        double elevationScale = getHeight() / (double) (2 * elevationRange);

        for (Drawable drawable : drawables.values()) {
            drawable.onAzimuthScaleChanged(azimuthScale);
            drawable.onElevationScaleChanged(elevationScale);
        }
    }

    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw the green horizontal line at 0 degrees elevation (horizon)
        int horizonY = centerY - (0 * height / (2 * elevationRange));
        g.setColor(Color.GREEN);
        g.drawLine(0, horizonY, width, horizonY);

        // Calculate and draw heading point
        int headingX = centerX + (azimuthHeading * width / (2 * azimuthRange));
        int headingY = centerY - (elevationHeading * height / (2 * elevationRange));

        g.setColor(Color.RED);
        g.fillOval(headingX - 5, headingY - 5, 10, 10);

        // Draw horizontal and vertical lines for the heading
        g.setColor(Color.BLACK);
        g.drawLine(0, headingY, width, headingY);
        g.drawLine(headingX, 0, headingX, height);

        // Draw axis labels
        g.drawString("Azimuth (degrees)", width - 100, centerY - 10);
        g.drawString("Elevation (degrees)", centerX + 10, 20);

        // Draw ticks for azimuth and elevation
        drawTicks(g, headingX, headingY, width, height);

        // Draw all registered drawables
        for (Drawable drawable : drawables.values()) {
            drawable.draw(g);  // Instruct each drawable to render itself
        }

        // Draw crosshair and display azimuth/elevation above it if mouse position is set
        if (mouseX != -1 && mouseY != -1) {
            drawCrosshair(g, centerX, centerY, width, height);
        }
    }

    private void drawTicks(Graphics g, int headingX, int headingY, int width, int height) {
        int tickLengthMajor = 10;
        int tickLengthMinor = 5;

        g.setColor(Color.BLACK);

        for (int i = -azimuthRange; i <= azimuthRange; i += 5) {
            int xTick = headingX + i * (width / (2 * azimuthRange));
            int tickLength = (i % 10 == 0) ? tickLengthMajor : tickLengthMinor;
            g.drawLine(xTick, headingY - tickLength, xTick, headingY + tickLength);
            if (i % 10 == 0) {
                g.drawString(Integer.toString(i + azimuthHeading), xTick - 10, headingY + 3 * tickLength);
            }
        }

        for (int i = -elevationRange; i <= elevationRange; i += 5) {
            int yTick = headingY - i * (height / (2 * elevationRange));
            int tickLength = (i % 10 == 0) ? tickLengthMajor : tickLengthMinor;
            g.drawLine(headingX - tickLength, yTick, headingX + tickLength, yTick);
            if (i % 10 == 0) {
                g.drawString(Integer.toString(i + elevationHeading), headingX + 3 * tickLength, yTick + 5);
            }
        }
    }

    private void drawCrosshair(Graphics g, int centerX, int centerY, int width, int height) {
        g.setColor(Color.GRAY);
        g.drawLine(0, mouseY, width, mouseY);
        g.drawLine(mouseX, 0, mouseX, height);

        int azimuth = azimuthHeading + (int) ((mouseX - centerX) * azimuthRange * 2.0 / width);
        int elevation = elevationHeading - (int) ((mouseY - centerY) * elevationRange * 2.0 / height);

        g.setColor(Color.BLACK);
        g.drawString("Azimuth: " + azimuth + "°, Elevation: " + elevation + "°", mouseX + 10, mouseY - 10);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Not used but required by MouseMotionListener
    }
}

