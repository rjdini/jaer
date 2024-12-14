/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.inilabs.jaer.projects.tracker;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

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
import com.jogamp.opengl.util.gl2.GLUT;
import net.sf.jaer.util.DrawGL;
import net.sf.jaer.util.EngineeringFormat;
import com.inilabs.jaer.projects.cog.SpatialAttention;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeSupport;
import java.util.List;
import org.slf4j.LoggerFactory;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.LoggingStatePropertyChangeFilter;
import com.inilabs.jaer.projects.motor.DirectGimbalController;
import java.util.Timer;
import java.util.stream.Collectors;
import net.sf.jaer.graphics.AEViewer;



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

@Description("Rev 3Dec24:  RS4Ronin gimbal tracks real and synthetic targets in PolarSpace")
@net.sf.jaer.DevelopmentStatus(net.sf.jaer.DevelopmentStatus.Status.Stable)
public class TrackerManagerV2 extends EventFilter2DMouseAdaptor implements FrameAnnotater {

    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TrackerManagerV2.class);

    RectangularClusterTracker tracker;
  //  GimbalAimer panTilt = null;
    Point2D.Float targetLocation = null;
    RectangularClusterTracker.Cluster targetCluster = null;

    private String who = "";  // the name of  this class, for locking gimbal access (if necessary) 
    private float[] rgb = {0, 0, 0, 0};
    private boolean isEnableTestClusters = false;
    private static PolarSpaceGUI polarSpaceGUI = null;
    private TrackerAgentDrawable trackerAgentDrawable = null;
    private final LoggingStatePropertyChangeFilter loggingStateFilter;
   private TrackerManagerEngine engine; 
   private static FieldOfView fov;
   private final SpatialAttention spatialAttention;
   
    private final int numberClustersAdded = 5 ; // sets the number of clusters generated for testing
    private final TMExerciser exerciser = new TMExerciser();
    private TrackerAgentDrawable primaryTrackerAgent;
     EngineeringFormat fmt = new EngineeringFormat();
     
    Timer timer = new Timer();
   // private GimbalBase gimbalBase; 
      private static DirectGimbalController gimbal;

    public TrackerManagerV2(AEChip chip) {
        super(chip);
        FilterChain filterChain = new FilterChain(chip);
        loggingStateFilter = new LoggingStatePropertyChangeFilter(chip); 
        loggingStateFilter.getSupport().addPropertyChangeListener(this);
        tracker = new RectangularClusterTracker(chip);
        tracker.getSupport().addPropertyChangeListener(this);
        filterChain.add(loggingStateFilter);
        filterChain.add(tracker);
        fov = FieldOfView.getInstance();
        gimbal = DirectGimbalController.getInstance();
        gimbal.addPropertyChangeListener(fov);
        setEnclosedFilterChain(filterChain);

        who = "TargetManager";
        support = new PropertyChangeSupport(this);
        
         javax.swing.Timer updateTestTimer = new javax.swing.Timer(50, e ->   updateTrackerManagerEngineTests()) ;
        updateTestTimer.start();
        
         Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        
         polarSpaceGUI = new PolarSpaceGUI();
         polarSpaceGUI.getPolarSpaceDisplay().addDrawable(fov);
         spatialAttention = SpatialAttention.getInstance();
         
         engine = new TrackerManagerEngine();
         polarSpaceGUI.getPolarSpaceDisplay().setHeading(0, 0);
    }
    
   private void shutdown() {
       AgentLogger.shutdown();
       log.info("Shutting down TargetManager...");
} 
   
   private DirectGimbalController getGimbal() {
    return gimbal;
} 
   
   private FieldOfView getFOV() {
       return fov;
   }
   
   private SpatialAttention getSpatialAttention() {
       return spatialAttention;
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
    if(in != null) {
    AgentLogger.setJAERTimestamp(in.getLastTimestamp());}
    
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
    .map(cluster -> new RCTClusterAdapter(cluster))
    .collect(Collectors.toList());

        
        // Update the TME engine with these clusters
         engine.updateRCTClusterList(limitedClusters); 
        engine.updateBestTrackerAgentList();
          }
     return in;
    }

    

private void updateTrackerManagerEngineTests() {    
    if(isEnableTestClusters) {
      //  engine.updateTestClusterList(exerciser.getTestClusters(10, -30) ); 
         engine.updateTestClusterList(exerciser.getTestClustersHorizontal() ); 
     } 
}

private void setPrimaryTrackerAgent( TrackerAgentDrawable agent ) {
primaryTrackerAgent = agent;
}

private TrackerAgentDrawable getPrimaryTrackerAgent() {
return primaryTrackerAgent ;
}



 // <editor-fold defaultstate="collapsed" desc="GUI button --ControllerGUI--">
    /**
     * Invokes the calibration GUI Calibration values are stored persistently as
     * preferences. Built automatically into filter parameter panel as an
     * action.
     */
    public void doControllerGUI() {
        getGimbal().rs4controllerGUI.setVisible(true);
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
    
   
//    public void setPanTiltVisualAimPixels(float pan, float tilt) {
//        // convert pixels to normalized (0-1) location
//        float normalizedPan = pan / chip.getSizeX();
//        float normalizedTilt = tilt / chip.getSizeY();
//        //  targetCluster = null;  // debug
//        if (targetCluster != null) {
//            getGimbalBase().setTargetEnabled(true);
//            // getGimbalBase().setTarget(normalizedPan, normalizedTilt);
//            panTilt.setPanTiltTarget(normalizedPan, normalizedTilt);
//            //   targetCluster = null;
//        } else {
//            getGimbalBase().setTargetEnabled(false);
//            getGimbalBase().sendDefaultGimbalPose();
//        }
//    }



    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanTiltTarget--">
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = this.getMousePixel(e);       
   //     setPanTiltVisualAimPixels(p.x, p.y); 
     
    }

    @Override
    public void resetFilter() {
    //    panTilt.resetFilter();
    }

    @Override
    public void initFilter() {
         AgentLogger.initialize(chip.getAeViewer());
     }

   
    
    public void annotate(float[][][] frame) {
    }

    public void annotate(Graphics2D g) {
    }

 
    
    /**
 * Draws the target location with text annotations.
 *
 * @param gl the OpenGL context.
 */

@Override
synchronized public void annotate(GLAutoDrawable drawable) {
    if (!this.isFilterEnabled()) {
        return;
    }
    fmt.setPrecision(1); // digits after decimal point
    GL2 gl = drawable.getGL().getGL2();
    if (gl == null) {
        log.warn("null GL in RectangularClusterTracker.annotate");
        return;
    }

    drawGimbalPoseCrossHair(gl); // This method includes its own push/pop matrix calls

    try {
        gl.glPushMatrix();
        gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT | GL2.GL_ENABLE_BIT);
        drawTargetLocation(gl);  // Target location with matrix and state managed inside
    } catch (java.util.ConcurrentModificationException e) {
        log.warn("Concurrent modification of target list while drawing");
    } finally {
        gl.glPopAttrib();
        gl.glPopMatrix();
    }
}

private GL2 drawGimbalPoseCrossHair(GL2 gl) {
    int sx2 = chip.getSizeX() / 8, sy2 = chip.getSizeY() / 8;
    int midX = chip.getSizeX() / 2, midY = chip.getSizeY() / 2;
    
    gl.glPushMatrix();
    try {
        gl.glTranslatef(midX, midY, 0);
        gl.glLineWidth(2f);
        gl.glColor3f(0, 1, 1);

        gl.glBegin(GL.GL_LINES);
        gl.glVertex2f(-sx2, 0);
        gl.glVertex2f(sx2, 0);
        gl.glVertex2f(0, -sy2);
        gl.glVertex2f(0, sy2);
        gl.glEnd();
        
        
          // Render POSE output text above the crosshair
        GLUT cGLUT = chip.getCanvas().getGlut();
        final int font = GLUT.BITMAP_TIMES_ROMAN_24;

        // Move the raster position just above the crosshair
        gl.glRasterPos3f(0, sy2 + 10, 0); // Offset by 10 pixels above crosshair
        cGLUT.glutBitmapString(font, String.format("FOV(y,p) %.1f, %.1f deg ",
                getGimbal().getGimbalPose().getYaw(),
                getGimbal().getGimbalPose().getPitch()));
   
    } finally {
        gl.glPopMatrix();
    }

    return gl;
}

private GL2 drawTargetLocation(GL2 gl) {
      float sx = chip.getSizeX() / 32;
      TrackerAgentDrawable agent =  spatialAttention.getBestTrackerAgent(); 
      if (agent != null) {
       agent.run(); // update data
  //  float[] target = getGimbalBase().getTarget()
     float pixelX = agent.getChipLocation().x;
     float pixelY = agent.getChipLocation().y;            
    gl.glPushMatrix();
    gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT);
    try {
        gl.glTranslatef(pixelX, pixelY, 0);
        gl.glColor3f(agent.getColor().getRed(), agent.getColor().getGreen(), agent.getColor().getBlue());
        DrawGL.drawCircle(gl, 0f, 0f, sx, 10);

        // Text annotation on clusters
        GLUT cGLUT = chip.getCanvas().getGlut();
        final int font = GLUT.BITMAP_TIMES_ROMAN_24;
        gl.glRasterPos3f(0, -sx, 0);
        cGLUT.glutBitmapString(font, String.format(agent.getKey()+" qual: %.1f ", agent.getSupportQuality()));
        gl.glRasterPos3f(0, sx, 0);
        cGLUT.glutBitmapString(font, String.format("TARG(a,e) %.1f, %.1f deg ",
               agent.getAzimuth(),
               agent.getElevation()));
        
    } finally {
        gl.glPopAttrib();
        gl.glPopMatrix();
    }
      } else {
      log.debug("Draw target best agent = null");
      }
    return gl;
}
    

    /**
     * @return the loggingStateFilter
     */
    public LoggingStatePropertyChangeFilter getLoggingStateFilter() {
        return loggingStateFilter;
    }

    
    
}
