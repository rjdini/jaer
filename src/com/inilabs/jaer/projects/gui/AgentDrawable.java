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

import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.EventType;
import com.inilabs.jaer.projects.tracker.EventCluster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AgentDrawable extends BasicDrawable implements Drawable, DrawableListener{
   
     /**
     * Returns the current timestamp. This method encapsulates the time source,
     * allowing for flexibility in future implementations.
     *
     * @return The current timestamp in milliseconds.
     */
    protected long getTimestamp() {
        return System.currentTimeMillis();
    }

    public AgentDrawable() {
        super();
        init();
        // *** the constuctor of EventCluster si broken by this looging call - ?? reason?
    //    AgentLogger.logAgentEvent(EventType.CREATE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

    private void init() {
        setSize(2f);
        setColor(Color.BLACK);
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    @Override
    public void setAzimuth(float azimuth) {
        super.setAzimuth(azimuth);
        addCurrentPositionToPath();
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);
        addCurrentPositionToPath();
    }

    @Override
    protected void addCurrentPositionToPath() {
        if (pathBuffer.size() >= maxPathLength) {
            pathBuffer.removeFirst();
        }
        pathBuffer.addLast(new float[]{getAzimuth(), getElevation()});
    }

    @Override
    public void onTransformChanged(float azimuthScale, float elevationScale, float azimuthHeading, float elevationHeading, int centerX, int centerY) {
        this.azimuthScale = azimuthScale;
        this.elevationScale = elevationScale;
        this.azimuthHeading = azimuthHeading;
        this.elevationHeading = elevationHeading;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int x = centerX + (int) ((getAzimuth() - azimuthHeading) * azimuthScale);
        int y = centerY - (int) ((getElevation() - elevationHeading) * elevationScale);

        g2d.setColor(color);
        int pixelSizeX = (int) (size * azimuthScale);
        int pixelSizeY = (int) (size * elevationScale);
        g2d.drawOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);
        g2d.drawString(getKey(), x, y+pixelSizeY);

        if (showPath) {
            drawPath(g2d);
        }

    //    AgentLogger.logAgentEvent(EventType.DRAW, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }

    protected void drawPath(Graphics2D g2d) {
        g2d.setColor(color);
        float[] previousPosition = {0,0};

        for (float[] position : pathBuffer) {
            int pathX = centerX + (int) ((position[0] - azimuthHeading) * azimuthScale);
            int pathY = centerY - (int) ((position[1] - elevationHeading) * elevationScale);

            if (previousPosition != null) {
                int prevX = centerX + (int) ((previousPosition[0] - azimuthHeading) * azimuthScale);
                int prevY = centerY - (int) ((previousPosition[1] - elevationHeading) * elevationScale);
                g2d.drawLine(prevX, prevY, pathX, pathY);
            }
            previousPosition = position;
        }
    }

    public void close() {
        lastTime = getTimestamp();
        clusters.clear();
        AgentLogger.logAgentEvent(EventType.CLOSE, getKey(), getAzimuth(), getElevation(), getClusterKeys());
    }
}
