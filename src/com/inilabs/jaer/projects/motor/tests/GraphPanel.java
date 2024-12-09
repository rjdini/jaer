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

package com.inilabs.jaer.projects.motor.tests;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class GraphPanel extends JPanel {
    private final List<Float> inputValues = new ArrayList<>();
    private final List<Float> responseValues = new ArrayList<>();
    private final List<Float> timeStamps = new ArrayList<>();
    private final int maxTimeInSeconds; // Fixed cycle duration (e.g., 15 seconds)
    private final int yMaxDegrees = 20; // Maximum Y-axis value in degrees

    public GraphPanel(int maxTimeInSeconds) {
        this.maxTimeInSeconds = maxTimeInSeconds;
    }

    public void addPoint(float time, float input, float response) {
        synchronized (this) {
            if (time >= maxTimeInSeconds) {
                // Reset the data at the start of a new trial
                timeStamps.clear();
                inputValues.clear();
                responseValues.clear();
            }
            timeStamps.add(time);
            inputValues.add(input);
            responseValues.add(response);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (this) {
            int width = getWidth();
            int height = getHeight();

            // Clear background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // Draw axes
            g.setColor(Color.BLACK);
            g.drawLine(50, height - 50, width - 50, height - 50); // X-axis
            g.drawLine(50, 50, 50, height - 50);                 // Y-axis

            // Annotate axes
            drawAnnotations(g, width, height);

            // Draw input (blue) and response (red)
            g.setColor(Color.BLUE);
            drawGraph(g, timeStamps, inputValues, width, height);

            g.setColor(Color.RED);
            drawGraph(g, timeStamps, responseValues, width, height);
        }
    }

    private void drawAnnotations(Graphics g, int width, int height) {
        int graphWidth = width - 100;
        int graphHeight = height - 100;

        // Draw Y-axis annotations
        for (int i = 0; i <= yMaxDegrees; i += 5) {
            int y = height - 50 - i * graphHeight / yMaxDegrees;
            g.drawString(String.valueOf(i), 25, y);
            g.drawLine(45, y, 50, y);
        }

        // Draw X-axis annotations
        for (int i = 0; i <= maxTimeInSeconds; i++) {
            int x = 50 + i * graphWidth / maxTimeInSeconds;
            g.drawString(String.valueOf(i), x - 5, height - 30);
            g.drawLine(x, height - 50, x, height - 45);
        }

        // Label axes
        g.drawString("Time (s)", width / 2 - 20, height - 10);
        g.drawString("Yaw (°)", 10, height / 2);
    }

    private void drawGraph(Graphics g, List<Float> times, List<Float> values, int width, int height) {
        int graphWidth = width - 100;
        int graphHeight = height - 100;

        if (values.isEmpty() || times.isEmpty()) return;

        int prevX = 50;
        int prevY = height - 50 - Math.round(values.get(0) * graphHeight / yMaxDegrees);

        for (int i = 1; i < values.size(); i++) {
            int x = 50 + Math.round(times.get(i) * graphWidth / maxTimeInSeconds);
            int y = height - 50 - Math.round(values.get(i) * graphHeight / yMaxDegrees);
            g.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }
    }
}
