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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class AgentDrawable extends BasicDrawable implements Drawable {

 
    public AgentDrawable() {
        super();
        init();
  //     AgentLogger.logAgentEvent(EventType.CREATE, getKey(), getAzimuth(), getElevation(), getColor(), dummyClusterList);
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
        this.setAzimuthScale(azimuthScale);
        this.setElevationScale(elevationScale);
        this.setAzimuthHeading(azimuthHeading);
        this.setElevationHeading(elevationHeading);
        this.setCenterX(centerX);
        this.setCenterY(centerY);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int x = getCenterX() + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
        int y = getCenterY() - (int) ((getElevation() - getElevationHeading()) * getElevationScale());

        g2d.setColor(color);
        int pixelSizeX = (int) (getSize() * getAzimuthScale());
        int pixelSizeY = (int) (getSize() * getElevationScale());
        g2d.drawOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);
        g2d.drawString(getKey(), x, y+pixelSizeY);

        if (showPath) {
            drawPath(g2d);
        }
    }
  
    @Override
    protected void drawPath(Graphics2D g2d) {
        g2d.setColor(color);
        float[] previousPosition = {0,0};

        for (float[] position : pathBuffer) {
            int pathX = getCenterX() + (int) ((position[0] - getAzimuthHeading()) * getAzimuthScale());
            int pathY = getCenterY() - (int) ((position[1] - getElevationHeading()) * getElevationScale());

            if (previousPosition != null) {
                int prevX = getCenterX() + (int) ((previousPosition[0] - getAzimuthHeading()) * getAzimuthScale());
                int prevY = getCenterY() - (int) ((previousPosition[1] - getElevationHeading()) * getElevationScale());
                g2d.drawLine(prevX, prevY, pathX, pathY);
            }
            previousPosition = position;
        }
    }

    @Override 
    public void close() {
        setLastTime(getSystemTimestamp());
        AgentLogger.logAgentEvent(EventType.CLOSE, getKey(), getAzimuth(), getElevation(), getColor(), dummyClusterList);
    }
}
