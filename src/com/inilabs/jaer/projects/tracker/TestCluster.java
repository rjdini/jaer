package com.inilabs.jaer.projects.tracker;

import java.awt.geom.Point2D;
  
public class TestCluster implements ClusterAdapter {
    private boolean isVisible = true; 
    private float azimuth = 0;
    private float elevation = 0;
    private Point2D.Float location = new Point2D.Float(0,0); 
    private FOVUtils fov = new FOVUtils();
    protected long startTime;
    private float clusterSize = 0.5f;   // nominal 0.5 deg
 

    public TestCluster(float azimuth, float elevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.location.setLocation(
                   fov.getPixelsAtYaw(azimuth), // chip / fov  pixel location x
                  fov.getPixelsAtPitch(elevation) // chip / fov pixel location y
//                fov.getPixelsAtYaw(azimuth - fov.getPose()[0]), // chip / fov  pixel location x
//                 fov.getPixelsAtPitch(elevation - fov.getPose()[2]) // chip / fov pixel location y
                );
    }
    
    public TestCluster(Point2D.Float pt) {
        this.location = pt;
        this.azimuth = fov.getYawAtPixel(location.x );
        this.elevation = fov.getPitchAtPixel(location.y);
        this.startTime = getTimestamp();
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
     
     
    // This is the call the simulation engine will use to set TestCluster position in FOV
    public void setLocation(Point2D.Float pt) {
        location = pt;  
        this.azimuth = fov.getYawAtPixel(location.x );
        this.elevation = fov.getPitchAtPixel(location.y);
    }

    public long getLifeTime() {
        return( getTimestamp() -  startTime);
    }
       
     @Override
    public float getAzimuth() {
        return (float) fov.getYawAtPixel((float)getLocation().getX());
    }

    @Override
    public float getElevation() {
        return (float) fov.getPitchAtPixel((float)getLocation().getY());
    }

    @Override
    public Point2D.Float getLocation() {
        return location;
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
        return "TestCluster-" + this.hashCode();
    }
}


