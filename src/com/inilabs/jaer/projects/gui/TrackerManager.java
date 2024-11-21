/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inilabs.jaer.projects.gui;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import java.util.TimerTask;
import com.jogamp.opengl.GL;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.eventprocessing.FilterChain;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker;
import net.sf.jaer.graphics.FrameAnnotater;
import net.sf.jaer.hardwareinterface.HardwareInterfaceException;
import com.jogamp.opengl.util.gl2.GLUT;
import net.sf.jaer.eventprocessing.tracking.ClusterInterface;
import net.sf.jaer.eventprocessing.tracking.ClusterPathPoint;
import net.sf.jaer.util.DrawGL;
import net.sf.jaer.util.EngineeringFormat;
import net.sf.jaer.util.filter.LowpassFilter;

import com.inilabs.jaer.gimbal.GimbalAimer;
import com.inilabs.jaer.gimbal.GimbalAimerGUI;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.List;
import net.sf.jaer.eventprocessing.tracking.RectangularClusterTracker.Cluster;
import org.slf4j.LoggerFactory;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.LoggingStatePropertyChangeFilter;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import java.util.Timer;



/**
 * Provides gimbal control and testing.
 *
 * Displays gimbal limits of pan as box, and pose as cross hairs with current
 * pose as text.
 *
 * Mouse displays mouse location chip x,y (pixels); pan tilt in FOV (0-1);
 * deltaYaw, deltaTilt (degs) with respect to current pose.
 *
 * Mouse click causes gimbal to pan to mouse location.
 *
 * @author tobi, rjd
 */
@DevelopmentStatus(DevelopmentStatus.Status.Experimental)
@Description("Rev 12Nov24:  Steers RS4 gimbal pose,  FOV and tracked target on PolarSpaceGUI")
public class TrackerManager extends EventFilter2DMouseAdaptor implements FrameAnnotater {

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrackerManager.class);

    RectangularClusterTracker tracker;
    GimbalAimer panTilt = null;
    Point2D.Float targetLocation = null;
    RectangularClusterTracker.Cluster targetCluster = null;

    private boolean mousePressed = false;
    private boolean shiftPressed = false;
    private boolean ctlPressed = false;
    private boolean altPressed = false;
    private Point mousePoint = null;
    private String who = "";  // the name of  this class, for locking gimbal access (if necessary) 
    private float[] rgb = {0, 0, 0, 0};
    private boolean shutdown = false;
    
    private PolarSpaceGUI polarSpaceGUI = null;
    private TrackerAgentDrawable trackerAgentDrawable = null;
    private LoggingStatePropertyChangeFilter loggingStateFilter;
    private FieldOfView fov;
       
    
    Timer timer = new Timer();

    public TrackerManager(AEChip chip) {
        super(chip);
        targetLocation = new Point2D.Float(100, 100);

        FilterChain filterChain = new FilterChain(chip);
        loggingStateFilter = new LoggingStatePropertyChangeFilter(chip); 
        loggingStateFilter.getSupport().addPropertyChangeListener(this);
        tracker = new RectangularClusterTracker(chip);
        tracker.getSupport().addPropertyChangeListener(this);
        panTilt = new GimbalAimer(chip);
        panTilt.getSupport().addPropertyChangeListener(this);
        filterChain.add(loggingStateFilter);
        filterChain.add(tracker);
        filterChain.add(panTilt);
        setEnclosedFilterChain(filterChain);

        who = "TargetManager";
        support = new PropertyChangeSupport(this);
        
         javax.swing.Timer updateTimer = new javax.swing.Timer(500, e ->   updateTrackerAgentDrawable()) ;
        updateTimer.start();  
         Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
         fov = new FieldOfView();
         polarSpaceGUI = new PolarSpaceGUI();
         polarSpaceGUI.getPolarSpaceDisplay().setHeading(0, -30);
         polarSpaceGUI.getPolarSpaceDisplay().addDrawable(fov);
        
        AgentLogger.initialize();
    }
    
   private void shutdown() {
       AgentLogger.shutdown();
       log.info("Shutting down TargetManager...");
} 
   
   
   
    /**
     * @return the polarSpaceGUI
     */
    public PolarSpaceGUI getPolarSpaceGUI() {
        if (polarSpaceGUI == null) {
            polarSpaceGUI = new PolarSpaceGUI();
            polarSpaceGUI.getPolarSpaceDisplay().setHeading(0, -30);
             polarSpaceGUI.getPolarSpaceDisplay().addDrawable(fov);
             polarSpaceGUI.setVisible(false);
        }
        return polarSpaceGUI;
    }
    
        
    
    
    @Override
    public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        if (!isFilterEnabled()) {
            return in;
        }
        AgentLogger.setJAERTimestamp(in.getLastTimestamp() ) ;
        getEnclosedFilterChain().filterPacket(in);
//		if(panTilt.isLockOwned()) {
//			return in;
//		}
        if (tracker.getNumClusters() > 0) {
            targetCluster = tracker.getClusters().get(0);
            if (targetCluster.isVisible()) {
                Point2D.Float p = targetCluster.getLocation();
                targetLocation = p;
                bestTargetCluster();
                float[] xy = {p.x, p.y, 1};
                try {
                    setPanTiltVisualAimPixels(p.x, p.y);
                } catch (Exception ex) {
                    log.warn(ex.toString());
                }
                panTilt.getGimbalBase().setLaserOn(true);
            } else {
                panTilt.getGimbalBase().setLaserOn(false);
            }
        } else {
            panTilt.getGimbalBase().setLaserOn(false);
        }
        return in;
    }

    // <editor-fold defaultstate="collapsed" desc="GUI button --Aim--">
    /**
     * Invokes the calibration GUI Calibration values are stored persistently as
     * preferences. Built automatically into filter parameter panel as an
     * action.
     */
    public void doEnableGimbal() {
        panTilt.getGimbalBase().enableGimbal(true);
    }
    // </editor-fold>      

    // <editor-fold defaultstate="collapsed" desc="GUI button --Aim--">
    /**
     * Invokes the calibration GUI Calibration values are stored persistently as
     * preferences. Built automatically into filter parameter panel as an
     * action.
     */
    public void doDisableGimbal() {
        panTilt.getGimbalBase().enableGimbal(false);
    }
    // </editor-fold>      

     // <editor-fold defaultstate="collapsed" desc="GUI button --PolarGUI--">
    /**
     * Sets polar GUI visible target tracking. 
     * Values are stored persistently as preferences. 
     * Built automatically into filter parameter panel as an action.
     */
    public void doPolarSpaceGUI() {
        polarSpaceGUI.setVisible(true);
    }
    // </editor-fold>
    
   
    public Cluster bestTargetCluster() {
        //  RectangularClusterTracker.Cluster thresholdCluster = new RectangularClusterTracker.Cluster();
        float eventThreshold = 2f;
        float sizeThreshold = 10f;

        if (tracker.getClusters() != null) {

            // intitialize cluster with first in group
            Cluster bestCluster = tracker.getClusters().get(0);
            List<Cluster> clusters = tracker.getClusters();
            Iterator<Cluster> clusterIterator = clusters.iterator();

            // check is there is a better custer than the first one
            while (clusterIterator.hasNext()) {
                Cluster c = clusterIterator.next();
                if (c.getAvgEventRateHz()>= eventThreshold && c.getRadius() < sizeThreshold && c.getAvgEventRateHz() > bestCluster.getMeasuredAverageEventRate()) {
                    bestCluster = c;
                }
            }
            if(true){
 //           if (bestCluster.getAvgEventRateHzPerPx() >= eventThreshold && bestCluster.getRadius() < sizeThreshold) {
                targetCluster = bestCluster;
                targetCluster.setColor(Color.red); 
                    log.info("$$$$$$$$$$$$$$$$$$$$$  bestTargetCluster");
                updateTrackerAgentDrawable( );
            } else {
                // there is no suitable cluster
                deassignTrackerAgentDrawable();
                targetCluster = null;
            }
        } else {
            // there is no suitable cluster
            deassignTrackerAgentDrawable();
            targetCluster = null;
        }

        return targetCluster;

    }
    
    private void updateTrackerAgentDrawable() {
        assignTrackerAgentDrawable();
         if ((trackerAgentDrawable != null ) && ( polarSpaceGUI != null )) {
             float[] target = panTilt.getGimbalBase().getTarget();
             float y = fov.getYawAtPan(target[0]);
             float p =fov.getPitchAtTilt(target[1]);
             trackerAgentDrawable.setAzimuth(y);
             trackerAgentDrawable.setElevation(p);
             trackerAgentDrawable.run();
             polarSpaceGUI.repaint();
         }
    }
    
    private AgentDrawable assignTrackerAgentDrawable() {
        if ((trackerAgentDrawable == null ) && ( polarSpaceGUI != null )) {
            trackerAgentDrawable = new TrackerAgentDrawable(2000)  ;
            polarSpaceGUI.getPolarSpaceDisplay().addDrawable(trackerAgentDrawable) ;
        }
        return trackerAgentDrawable ;
    }
    
    private void deassignTrackerAgentDrawable() {
        if ((trackerAgentDrawable != null ) && ( polarSpaceGUI != null )) {
            polarSpaceGUI.getPolarSpaceDisplay().removeDrawable(trackerAgentDrawable.getKey()) ;        
        }
    }
   

    public void setPanTiltVisualAimPixels(float pan, float tilt) {
        // convert pixels to normalized (0-1) location
        float normalizedPan = pan / chip.getSizeX();
        float normalizedTilt = tilt / chip.getSizeY();
        //  targetCluster = null;  // debug
        if (targetCluster != null) {
            panTilt.getGimbalBase().setTargetEnabled(true);
            // panTilt.getGimbalBase().setTarget(normalizedPan, normalizedTilt);
            panTilt.setPanTiltTarget(normalizedPan, normalizedTilt);
            //   targetCluster = null;
        } else {
            panTilt.getGimbalBase().setTargetEnabled(false);
            panTilt.getGimbalBase().sendDefaultGimbalPose();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanTiltTarget--">
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = this.getMousePixel(e);
       
        setPanTiltVisualAimPixels(p.x, p.y); 
            
    }

    @Override
    public void resetFilter() {
        panTilt.resetFilter();
    }

    @Override
    public void initFilter() {
        panTilt.resetFilter();
    }

    public void annotate(float[][][] frame) {
    }

    public void annotate(Graphics2D g) {
    }

    @Override
public void annotate(GLAutoDrawable drawable) {
    if (!isFilterEnabled()) {
        return;
    }
    GL2 gl = drawable.getGL().getGL2();
    if (gl == null) {
        log.warn("null GL in TargetManager.annotate");
        return;
    }

    if (targetCluster != null) {
        try {
            gl.glPushMatrix();
            gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT); // Ensure color and enable states are preserved
            drawTargetLocation(gl);
        } catch (java.util.ConcurrentModificationException e) {
            log.warn("Concurrent modification of target list while drawing");
        } finally {
            gl.glPopAttrib();
            gl.glPopMatrix();
        }
    }
}

/**
 * Draws the target location with text annotations.
 *
 * @param gl the OpenGL context.
 */
private void drawTargetLocation(GL2 gl) {
    float sx = chip.getSizeX() / 32;

    // Draw target location
    gl.glPushMatrix();
    gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT);
    try {
        gl.glTranslatef(targetLocation.x, targetLocation.y, 0);
        gl.glColor3f(1, 0, 0);

        // Set up GLUT for text annotations
        GLUT cGLUT = chip.getCanvas().getGlut();
        final int font = GLUT.BITMAP_TIMES_ROMAN_24;

        // Draw agent annotation or "No Target" above the target location
        gl.glRasterPos3f(0, sx, 0); // Adjust to position text just above the target
        if (trackerAgentDrawable != null) {
            cGLUT.glutBitmapString(font, String.format("%s (y,p) %.1f, %.1f deg",
                    trackerAgentDrawable.getKey(), trackerAgentDrawable.getAzimuth(), trackerAgentDrawable.getElevation()));
        } else {
            cGLUT.glutBitmapString(font, "No Target");
        }

        // Draw cluster number annotation below the target
        gl.glRasterPos3f(0, -sx, 0);
        cGLUT.glutBitmapString(font, String.format("Cluster # %d", targetCluster.getClusterNumber()));

        // Draw a red circle at the target location
        drawCircle(gl, 0.0f, 0.0f, sx, 10);

    } finally {
        gl.glPopAttrib();
        gl.glPopMatrix();
    }
}

/**
 * Draws a circle with specified radius and segments.
 *
 * @param gl       the OpenGL context.
 * @param cx       the x-coordinate of the center.
 * @param cy       the y-coordinate of the center.
 * @param radius   the radius of the circle.
 * @param segments the number of segments to approximate the circle.
 */
private void drawCircle(GL2 gl, float cx, float cy, float radius, int segments) {
    gl.glBegin(GL2.GL_LINE_LOOP);
    for (int i = 0; i < segments; i++) {
        double theta = 2.0 * Math.PI * i / segments;
        float x = (float) (radius * Math.cos(theta));
        float y = (float) (radius * Math.sin(theta));
        gl.glVertex2f(x + cx, y + cy);
    }
    gl.glEnd();
}

    /**
     * @return the loggingStateFilter
     */
    public LoggingStatePropertyChangeFilter getLoggingStateFilter() {
        return loggingStateFilter;
    }

}
