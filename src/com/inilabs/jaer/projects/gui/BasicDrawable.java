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

import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.EventCluster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class BasicDrawable implements Drawable, DrawableListener {

    protected static int idCounter = 0; // Auto-incrementing ID counter for instances
    protected String key;
    protected int id; // Unique ID for this instance
    protected BiConsumer<ActionType, String> parentCallback;

    // Path buffer for recent positions
    protected final LinkedList<float[]> pathBuffer = new LinkedList<>();
    protected final int maxPathLength = 20;
    protected Color color = Color.BLACK;
    protected boolean showPath = false;
    protected float size = 1.0f;
    protected float azimuth = 0.0f;
    protected float elevation = 0.0f;
    protected int centerX = 0;
    protected int centerY = 0;
    protected float azimuthScale = 1.0f;
    protected float elevationScale = 1.0f;
    protected float azimuthHeading = 0f;
    protected float elevationHeading = 0f;
    protected long startTime; // agent created
    protected long lastTime; // agent closed
    protected long maxLifetime = 100 ; //millisec
    protected boolean isOrphaned = false;
    protected boolean isExpired = false;
    protected static FieldOfView fov;
    protected List<EventCluster> clusters = new ArrayList<>();

    // Default constructor, places object at (0,0) and auto-generates key
    public BasicDrawable() {
        this.id = ++idCounter;
        this.key = getClass().getSimpleName() + "_" + id;
        this.startTime = getTimestamp();
        this.fov = FieldOfView.getInstance();
    }

    // Constructor with specific azimuth and elevation, and optional key
    public BasicDrawable(String key, float initialAzimuth, float initialElevation) {
        this.id = ++idCounter;
        this.key = key != null ? key : getClass().getSimpleName() + "_" + id;
        this.azimuth = initialAzimuth;
        this.elevation = initialElevation;
       this.startTime = getTimestamp();
       this.fov = FieldOfView.getInstance();
    }
 
    
      /**
     * Returns the current timestamp. This method encapsulates the time source,
     * allowing for flexibility in future implementations.
     *
     * @return The current timestamp in milliseconds.
     */
    protected long getTimestamp() {
        return System.currentTimeMillis();
    }
    
   
    public void extendLifetime(long incrementMillis) {
        maxLifetime += incrementMillis; // Add reward time
    }
    
    
    @Override
    public int getId() {
        return id;
    }
    
    public FieldOfView getFOV() {
        return fov;
    }
    
    @Override
    public String getKey() {
        return this.key;
    }
    
    @Override
    public boolean isOrphaned() {
        return isOrphaned;
    }
    
    public void setIsOrphaned(boolean yes) {
        isOrphaned = yes;
    }
    

 public long getLifetime()  {
     return getTimestamp() - startTime;
 }
         
  
//public boolean isTerminated() {
  //  return this.getLifeTime() >= maxLifeTime; // Check if lifetime has expired
 //    return true; // Check if lifetime has expired
//}
    
     /**
     * Clears the assigned clusters.
     */
    public void clearClusters() {
        clusters.clear();
    }
   
    
    public Point2D.Float getChipLocation() {
        float x = fov.getPixelsAtYaw(azimuth);
        float y = fov.getPixelsAtPitch(elevation);
        return new Point2D.Float(x, y) ;        
    }
    
    protected void setExpired(boolean yes) {
        isExpired = true;
    }
    
    @Override
    public boolean isExpired() {
      return isExpired;
    }
    
     protected void setMaxLifeTime(long max) {
         maxLifetime = max;
     }
     
    
        // the cluster management shouldnt be down here in BasicDrawable!  TODO
       // Helper method to get cluster keys as a list of strings
    public List<String> getClusterKeys() {
        return clusters.stream().map(EventCluster::getKey).collect(Collectors.toList());
    }
    
    // Drawable interface method to draw the drawable on the Graphics context
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Calculate position based on azimuth and elevation scales
        centerX = g.getClipBounds().width / 2;
        centerY = g.getClipBounds().height / 2;
   //     int x = centerX + (int) (azimuth * azimuthScale);
    //    int y = centerY - (int) (elevation * elevationScale);
        // Calculate screen coordinates based on polar coordinates and the transform broadcast
        int x = getCenterX() + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
        int y = getCenterY() - (int) ((getElevation() - getElevationHeading()) * getElevationScale());

        // Draw the drawable as a circle
        g2d.setColor(color);
        int pixelSizeX = (int) (size * getAzimuthScale());
        int pixelSizeY = (int) (size * getElevationScale());
        g2d.drawOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);

        // Draw the path if enabled
        if (showPath) {
            drawPath(g2d, getCenterX(), getCenterY());
        }

        // Update path buffer with the current position
    //   addCurrentPositionToPath();  updated only through update azimuth / elevation
    }

   
    protected void drawPath(Graphics2D g2d, int centerX, int centerY) {
        g2d.setColor(Color.GRAY);
        float[] previousPosition = null;

        for (float[] position : pathBuffer) {
            if (previousPosition != null) {
                int previousX = centerX + (int) (previousPosition[0] * getAzimuthScale());
                int previousY = centerY - (int) (previousPosition[1] * getElevationScale());
                int currentX = centerX + (int) (position[0] * getAzimuthScale());
                int currentY = centerY - (int) (position[1] * getElevationScale());
                g2d.drawLine(previousX, previousY, currentX, currentY);
            }
            previousPosition = position;
        }
    }

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
    public void setAzimuth(float azimuthDegrees) {
        this.azimuth = azimuthDegrees;
        addCurrentPositionToPath();
    }

    @Override
    public float getAzimuth() {
        return azimuth;
    }

    @Override
    public void setElevation(float elevationDegrees) {
        this.elevation = elevationDegrees;
        addCurrentPositionToPath();
    }

    @Override
    public float getElevation() {
        return elevation;
    }

    @Override
    public void setSize(float sizeDegrees) {
        this.size = sizeDegrees;
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void showPath(boolean yes) {
        this.showPath = yes;
    }

    @Override
    public void setParentCallback(BiConsumer<ActionType, String> parentCallback) {
        this.parentCallback = parentCallback;
    }

    // Protected getters for subclasses
    public float getAzimuthScale() {
        return azimuthScale;
    }

    public float getElevationScale() {
        return elevationScale;
    }


    public boolean isPathVisible() {
        return showPath;
    }

    
    public void setMaxPathLength(int maxPathLength) {
        maxPathLength = maxPathLength;
    }

    
    protected void drawPath(Graphics2D g2d) {
        drawPath(g2d, g2d.getClipBounds().width / 2, g2d.getClipBounds().height / 2);
    }
    
   @Override 
     public void close() {
        // Clear the path buffer
        pathBuffer.clear();
        
        // Reset position and properties to default values (optional)
        azimuth = 0.0f;
        elevation = 0.0f;
        size = 5.0f;
        color = Color.RED;
        showPath = false;

        // Notify parent or manager, if a callback is set
        if (parentCallback != null) {
            parentCallback.accept(ActionType.REMOVE, key);
        }
    }
     
     
 
    /**
     * @return the centerX
     */
    public int getCenterX() {
        return centerX;
    }

    /**
     * @return the centerY
     */
    public int getCenterY() {
        return centerY;
    }

    /**
     * @return the azimuthHeading
     */
    public float getAzimuthHeading() {
        return azimuthHeading;
    }

    /**
     * @return the elevationHeading
     */
    public float getElevationHeading() {
        return elevationHeading;
    }
    
}
