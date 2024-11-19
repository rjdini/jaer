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
package com.inilabs.jaer.projects.tracker;

import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker.Cluster;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import com.inilabs.jaer.projects.gui.BasicDrawable;
import  com.inilabs.jaer.gimbal.FieldOfView;
import com.inilabs.jaer.projects.gui.AgentDrawable;
import com.inilabs.jaer.projects.gui.Drawable;
import com.inilabs.jaer.projects.gui.DrawableListener;
import com.inilabs.jaer.projects.tracker.ClusterAdapter;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventCluster extends AgentDrawable implements Drawable, DrawableListener{
   private static final Logger log = LoggerFactory.getLogger(EventCluster.class);
   public ClusterAdapter enclosedCluster;
    private TrackerAgentDrawable enclosingAgent; // Reference to the enclosing agent
    private Color color = Color.BLACK; // Default color for visualization
    private float size = 2.0f; // Default size for drawing
   
    
  //  private static final FieldOfView fov = FieldOfView.getInstance(); // Shared FieldOfView instance

    
    /**
 * Factory method to adapt a Cluster into an EventCluster.
 *
 * EventCluster provides encapsulation of RCT Clusters (and  equivalent test clusters).
 * Because RCT is a heavy jaer filter class, its clusters will not run natively in the PolarSpace environment.]
 * Therefore RCT clusters and test clusters are enclosed by common ClusterAdapter, which is then encapsulated by EventCluster.  
 * 
 * Note that RCT clsuters (and hopefully test clusters in future) are continually updated in background (eg by RCT).
 * So - they are 'live'.
 * 
 * @param cluster The Cluster object to adapt.
 * @return A new EventCluster instance based on the Cluster data.
 */
public static EventCluster fromClusterAdapter(ClusterAdapter clusterAdapter) {
    // Optionally include transformation logic
    EventCluster eventCluster = new EventCluster();
    eventCluster.enclosedCluster = clusterAdapter; 
    eventCluster.setKey(clusterAdapter.getKey());
    eventCluster.azimuth = clusterAdapter.getAzimuth();
    eventCluster.elevation = clusterAdapter.getElevation();
    eventCluster.color=Color.BLACK;
    eventCluster.size = 1f;
    return eventCluster;
}
    public EventCluster() {
        super();
    }
    
    
 
    // Constructors
    public EventCluster(ClusterAdapter clusterAdapter, TrackerAgentDrawable agent) {
        super();
        if (clusterAdapter == null) {
            throw new IllegalArgumentException("ClusterAdapter cannot be null");
        }
        this.enclosedCluster = clusterAdapter;
        this.enclosingAgent = agent;
        this.color = agent.getColor(); // Inherit color from the agent
    }

//    public EventCluster(TrackerAgentDrawable agent) {
//        this(null, agent); // For testing, allow null cluster
//        this.color = Color.RED;
//    }
//    
   
    public void setKey(String key) {
        this.key = key;
    }
    
    @Override
    public float getAzimuth() {
        if (this.enclosedCluster == null) {
            throw new IllegalStateException("Enclosed cluster is not set");
        }
        return enclosedCluster.getAzimuth();
    }
 
    @Override
    public float getElevation() {
       if (enclosedCluster == null) {
            throw new IllegalStateException("Enclosed cluster is not set");
        }        return enclosedCluster.getElevation();
    }

    @Override
    public String getKey() {
        if (enclosedCluster != null) { 
        return enclosedCluster.getKey(); }  
        else {
            return this.key;
        }
    }

    public ClusterAdapter getEnclosedCluster() {
        return enclosedCluster;
    }

    public void setEnclosedCluster(ClusterAdapter enclosedCluster) {
        this.enclosedCluster = enclosedCluster;
    }


    // Accessors and Mutators
    public void setEnclosingAgent(TrackerAgentDrawable agent) {
        this.enclosingAgent = agent;
        this.color = agent.getColor(); // Match the agent’s color
    }

    public TrackerAgentDrawable getEnclosingAgent() {
        return enclosingAgent;
    }

    
    public float getSupportQuality() {
        // Proxy method; real logic to calculate support quality
        return 1.0f;
      //  return cluster != null ? cluster.getSupport() : 1.0f; // Default for testing
    }
    
    public boolean isVisible() {
        return enclosedCluster != null && enclosedCluster.isVisible();
    }

    public void  setIsVisible( boolean yes) {
        if (enclosedCluster != null ) {
            enclosedCluster.setIsVisible(yes);
        } 
       
    }

    
//    @Override
//public void draw(Graphics g) {
//    g.setColor(Color.RED);
//    g.fillOval(100, 100, 20, 20); // Draw a red circle for testing
//}

    
//    @Override
//    public void draw(Graphics g) {
//       Graphics2D g2d = (Graphics2D) g;
//
//        int x = centerX + (int) ((getAzimuth() - azimuthHeading) * azimuthScale);
//        int y = centerY - (int) ((getElevation() - elevationHeading) * elevationScale);
//          
//
//        g2d.setColor(color);
//        int pixelSizeX = (int) (size * azimuthScale);
//        int pixelSizeY = (int) (size * elevationScale);
//        g2d.fillOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);
//        g2d.drawString(getKey(), x, y - pixelSizeY / 2);
//        
//       // log.info("EventCluster {} completed draw at azi {}, ele {} ", getKey(), getAzimuth(), getElevation());
//         }

        
    
//    // Drawing the cluster
   // @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
   
        // Calculate the cluster's location in relation to the enclosing agent
     // if (enclosingAgent != null) {      
             if (false) {         
              g2d.setColor(enclosingAgent.getColor());
        //      g2d.setColor(Color.GREEN);
  
            int xAgent = enclosingAgent.getCenterX() + (int) ((enclosingAgent.getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
            int yAgent= enclosingAgent.getCenterY() - (int) ((enclosingAgent.getElevation() - getElevationHeading()) * getElevationScale());
       
            
// Draw a circle for the cluster
            int pixelSizeX = (int) (size * enclosingAgent.getAzimuthScale());
            int pixelSizeY = (int) (size * enclosingAgent.getElevationScale());
            g2d.fillOval(xAgent - pixelSizeX / 2, yAgent - pixelSizeY / 2, pixelSizeX, pixelSizeY);
            g2d.drawString(getKey(), xAgent, yAgent - pixelSizeY/2);
            // Draw a line connecting the cluster to its enclosing agent
            int myX =  centerX + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
            int myY =  centerY - (int) ((getElevation() - getElevationHeading()) * getElevationScale());  
         //   g2d.drawLine(myX, myY, xAgent, yAgent); //not working TODO
        } 
        else {  //roll your own...
             g2d.setColor(getColor());         
             
     //        int x = getCenterX() + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
      //       int y = getCenterY() - (int) ((getElevation() - getElevationHeading()) * getElevationScale());
     
                 int x = centerX + (int) ((getAzimuth() - azimuthHeading) * azimuthScale);
               int y = centerY - (int) ((getElevation() - elevationHeading) * elevationScale);

             
// Draw a circle for the cluster
            int pixelSizeX = (int) (size * azimuthScale);
            int pixelSizeY = (int) (size * elevationScale);
            g2d.fillOval(x - pixelSizeX / 2, y - pixelSizeY / 2, pixelSizeX, pixelSizeY);
            g2d.drawString(getKey(), x, y - pixelSizeY/2);
        }
        
        }
    }
    
