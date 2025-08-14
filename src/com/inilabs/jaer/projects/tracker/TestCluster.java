package com.inilabs.jaer.projects.tracker;

import java.awt.Color;
import java.awt.geom.Point2D;
  
// Test Cluster that can be processed similarly to jAER rectangular cluster tracker cluster.
// Used for testing porcssing chain, and also for exercising gimbal.

public class TestCluster implements ClusterAdapter {
    protected static int idCounter = 0; // Auto-incrementing ID counter for instance
    protected int id; // Unique ID for this instance
    private boolean isVisible = true; 
    private float azimuth = 0;
    private float elevation = 0;
    private Point2D.Float fovPixelLocation = new Point2D.Float(0,0); 
    protected long startTime;
    private float clusterSize = 0.5f;   // nominal 0.5 deg
    private Color color = Color.GREEN;
    private FieldOfView fov = FieldOfView.getInstance();
    private String key;

    public TestCluster(float azimuth, float elevation, Color color) {
        this.id = ++idCounter;
        this.key = getClass().getSimpleName() + "_" + id;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.color = color;
        this.fovPixelLocation.setLocation(
                   fov.getPixelsAtYaw(azimuth), // chip / fov  pixel fovPixelLocation x
                   fov.getPixelsAtPitch(elevation) // chip / fov pixel fovPixelLocation y
//                fov.getPixelsAtYaw(azimuth - fov.getPose()[0]), // chip / fov  pixel fovPixelLocation x
//                 fov.getPixelsAtPitch(elevation - fov.getPose()[2]) // chip / fov pixel fovPixelLocation y
                );
    }
    
    public TestCluster(Point2D.Float pt) {
        this.id = ++idCounter;
        this.key = getClass().getSimpleName() + "_" + id;
        this.fovPixelLocation = pt;
        this.azimuth = fov.getYawAtPixel(fovPixelLocation.x );
        this.elevation = fov.getPitchAtPixel(fovPixelLocation.y);
        this.startTime = getTimestamp();
    }

    
    
    
    
    public void setAzimuth(float azim) {
       azimuth = azim ;
          
           this.fovPixelLocation.setLocation(
                  fov.getPixelsAtYaw(azimuth),   // chip / fov  pixel fovPixelLocation x
                  fov.getPixelsAtPitch(elevation) ); // chip / fov pixel fovPixelLocation y
                   }
    
    
     public void setElevation(float elev) {
        elevation = elev ;
           this.fovPixelLocation.setLocation(
                   fov.getPixelsAtYaw(azimuth), // chip / fov  pixel fovPixelLocation x
                  fov.getPixelsAtPitch(elevation)  );   // chip / fov pixel fovPixelLocation y
    }
    
    
    
    
    
    
    public boolean isRCTCluster() {
        return false;
    }
    
    public void resetLifeTime() {
        startTime = getTimestamp();
    }
    
    public float getSize() {    // in degrees
        return clusterSize;
    }
    
     protected long getTimestamp() {
        return System.currentTimeMillis();
    }
     
      public Color getColor() {
      return this.color;
  }
     
    // This is the call the simulation engine will use to set TestCluster position in FOV
    public void setLocation(Point2D.Float pt) {
        fovPixelLocation = pt;  
        this.azimuth = fov.getYawAtPixel(fovPixelLocation.x );
        this.elevation = fov.getPitchAtPixel(fovPixelLocation.y);
    }

    public long getLifeTime() {
        return( getTimestamp() -  startTime);
    }
       
     @Override
    public float getAzimuth() {
        return azimuth;
       // return (float) fov.getYawAtPixel((float)getLocation().getX());
    }

    @Override
    public float getElevation() {
        return elevation;
        //return (float) fov.getPitchAtPixel((float)getLocation().getY());
    }

    @Override
    public Point2D.Float getLocation() {
        return fovPixelLocation;
    }
    
    @Override
    public void setIsVisible(boolean yes) {
        isVisible = yes;
    }
    
    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public String getKey() {
        return key;
    }
}


