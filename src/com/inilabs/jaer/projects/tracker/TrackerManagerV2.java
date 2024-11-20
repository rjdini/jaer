/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inilabs.jaer.projects.tracker;

import com.inilabs.jaer.projects.gui.*;
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
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.LoggingStatePropertyChangeFilter;
import com.inilabs.jaer.projects.tracker.RCTClusterAdapter;
import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import com.inilabs.jaer.projects.tracker.TrackerManagerEngine;
import java.util.LinkedList;
import java.util.Timer;
import java.util.stream.Collectors;



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
public class TrackerManagerV2 extends EventFilter2DMouseAdaptor implements FrameAnnotater {

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrackerManagerV2.class);

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
    private boolean isEnableTestClusters = true;
    private PolarSpaceGUI polarSpaceGUI = null;
    private TrackerAgentDrawable trackerAgentDrawable = null;
    private LoggingStatePropertyChangeFilter loggingStateFilter;
   private TrackerManagerEngine engine; 
   private FieldOfView fov;
   
    private int numberClustersAdded = 5 ; // sets the number of clusters generated for testing
    private TMExerciser exerciser = new TMExerciser();
    
    Timer timer = new Timer();

    public TrackerManagerV2(AEChip chip) {
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
        
         javax.swing.Timer updateTestTimer = new javax.swing.Timer(200, e ->   updateTrackerManagerEngineTests()) ;
        updateTestTimer.start();  
         javax.swing.Timer updateGimbalTimer = new javax.swing.Timer(200, e ->   updateGimbal()) ;
        updateGimbalTimer.start();  
        
         Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
         
         fov = new FieldOfView();
         polarSpaceGUI = getPolarSpaceGUI();
         polarSpaceGUI.getPolarSpaceDisplay().addDrawable(fov);
         panTilt.getGimbalBase().addPropertyChangeListener(fov);
         engine = new TrackerManagerEngine();
         engine.setPolarSpaceDisplay(polarSpaceGUI.getPolarSpaceDisplay());
        AgentLogger.initialize();
         polarSpaceGUI.getPolarSpaceDisplay().setHeading(0, 0);
         panTilt.getGimbalBase().setGimbalPoseDirect(0f, 0f, 0f); 
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
            polarSpaceGUI.getPolarSpaceDisplay().setHeading(0, 0);
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

    // Log the timestamp for debugging purposes
    AgentLogger.setJAERTimestamp(in.getLastTimestamp());
    
    // Apply any preceding filters in the chain
    getEnclosedFilterChain().filterPacket(in);

    // Get visible clusters from the RCT tracker
    List<RectangularClusterTracker.Cluster> visibleClusters = tracker.getVisibleClusters();

    if (!visibleClusters.isEmpty()) {
        // Adapt the RCT tracker JAER clusters and pass them to the TrackerManagerEngine 
        // Optionally limit the number of clusters for testing
        List<RectangularClusterTracker.Cluster> limitedClusters = visibleClusters.stream()
                .limit(10) // Restrict to a maximum of 10 clusters for testing
                .collect(Collectors.toList());

        // Encapsulate clusters into RCTClusterAdapter for visualization
         List<RCTClusterAdapter> adaptedClusters = limitedClusters.stream()
                .map(RCTClusterAdapter::new)
                .collect(Collectors.toList());
        
        // Update the TME engine with these clusters
       // engine.updateRCTClusterList(limitedClusters); // this done automagically within TME
        engine.updateBestTrackerAgentList();
          }
     return in;
    }

    

private void updateTrackerManagerEngineTests() {    
    if(isEnableTestClusters) {
        engine.updateTestClusterList(exerciser.getTestClustersHorizontal() ); 
       // engine.updateBestTrackerAgentList(); // this is done by TME
     } else {
    engine.updateBestTrackerAgentList(); // this is done by TME
}
}



private void updateGimbal() {
TrackerAgentDrawable trackerAgentDrawable = engine.getBestTrackerAgentDrawable();
        if (trackerAgentDrawable != null) {
            // Update gimbal pose directly using azimuth and elevation
            float azimuth = trackerAgentDrawable.getAzimuth();
            float elevation = trackerAgentDrawable.getElevation();
            panTilt.getGimbalBase().setGimbalPoseDirect(azimuth, 0f, elevation);
            log.info("**************** setGimbalPoseDirect azi {} ele {} ", azimuth, elevation );
        } else {
            // Default behavior if no TrackerAgentDrawable exists
            panTilt.getGimbalBase().setGimbalPoseDirect(0f, 0f, 0f); // Default pose
             log.info("@@@@@@  Deafult setting setGimbalPoseDirect azi {} ele {} ", 0, 0, 0           
             );
        }
}

    
    // <editor-fold defaultstate="collapsed" desc="GUI button --Aim--">
    /**
     * Invokes the calibration GUI Calibration values are stored persistently as
     * preferences. Built automatically into filter parameter panel as an
     * action.
     */
    public void doEnableTestClusters() {
        if(!isEnableTestClusters) {
            isEnableTestClusters = true;
        } else {
            isEnableTestClusters = false;
        }
    }
    // </editor-fold>      
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
