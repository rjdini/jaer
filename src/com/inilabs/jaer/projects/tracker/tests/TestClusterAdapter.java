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
package com.inilabs.jaer.projects.tracker.tests;

import com.inilabs.jaer.projects.tracker.ClusterAdapter;
import java.awt.Color;
import java.awt.geom.Point2D;
import com.inilabs.jaer.projects.tracker.FieldOfView;



public class TestClusterAdapter implements ClusterAdapter {
    private float azimuth;
    private float elevation;
    private boolean visible;
    private String key;
    private long startTime;
    private static FieldOfView fov = FieldOfView.getInstance();

    public TestClusterAdapter(String key, float azimuth, float elevation) {
        this.key = key;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.visible = true; // Default visibility
        this.startTime = getTimestamp();
    }

     protected long getTimestamp() {
        return System.currentTimeMillis();
    }
     
       public void resetLifeTime() {
        startTime = getTimestamp();
    }

   @Override
  public long getLifeTime() {
        return( getTimestamp() -  startTime);
    }
     
     @Override
     public boolean isRCTCluster() {
         return false;
     } 
     
     public float getSize() {
         return 0.5f;
     }
     
     @Override
     public Color getColor(){
         return Color.BLUE;
     }
    
     public void resetLifetime() {
        startTime = getTimestamp();
    }
    
    @Override
    public float getAzimuth() {
        return azimuth;
    }

   
    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    @Override
    public float getElevation() {
        return elevation;
    }

   
    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    @Override
    public Point2D.Float getLocation() {
        return new Point2D.Float(azimuth, elevation); // Simplified for testing
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

   @Override
    public void setIsVisible(boolean yes) {
        this.visible = yes;
    }
}
