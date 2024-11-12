package com.inilabs.jaer.gimbal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.logging.Logger;
import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.hardwareinterface.HardwareInterfaceException;
<<<<<<< HEAD

/** This filter enables aiming the pan-tilt using a GUI and allows controlling
=======
import net.sf.jaer.util.DrawGL;
import net.sf.jaer.util.EngineeringFormat;
import org.slf4j.LoggerFactory;


/**
 * This filter enables aiming the pan-tilt using a GUI and allows controlling
>>>>>>> working
 * jitter of the pan-tilt when not moving it.
 *
 * @author Tobi Delbruck
 */
@Description("Allows control of pan-tilt using a panel to aim it and parameters to control the jitter")
@DevelopmentStatus(DevelopmentStatus.Status.Experimental)
<<<<<<< HEAD
public class GimbalAimer extends EventFilter2DMouseAdaptor implements GimbalInterface, LaserOnOffControl, PropertyChangeListener {

     private static final Logger log = Logger.getLogger("net.sf.jaer");
    
    private Gimbal panTiltHardware;
    private GimbalAimerGUI gui;
    private boolean jitterEnabled   = getBoolean("jitterEnabled", false);
    private float   jitterFreqHz    = getFloat("jitterFreqHz", 1);
    private float   jitterAmplitude = getFloat("jitterAmplitude", .05f);
    
    /// These values are now reference to normalized 0-1 FOV
    private boolean invertPan       = getBoolean("invertPan", false);
    private boolean invertTilt      = getBoolean("invertTilt", false);
    private boolean linearMotion    = getBoolean("linearMotion", false);
    private float   limitOfPan      = getFloat("limitOfPan", 1.0f);
    private float   limitOfTilt     = getFloat("limitOfTilt", 1.0f);
    private float   PanValue        = getFloat("panValue", 0.5f);
    private float   tiltValue       = getFloat("tiltValue", 0.5f);
    private float   maxMovePerUpdate= getFloat("maxMovePerUpdate",  0.1f);
    private float   minMovePerUpdate= getFloat("minMovePerUpdate", 0.01f);
    private int     moveUpdateFreqHz= getInt("moveUpdateFreqHz", 100);
    
    
    private String who ="";
    
    
    
    
    
    private final PropertyChangeSupport supportPanTilt = new PropertyChangeSupport(this);
    private boolean recordingEnabled = false; // not used
    Trajectory mouseTrajectory;
    Trajectory targetTrajectory = new Trajectory();
    Trajectory jitterTargetTrajectory = new Trajectory();
=======
public class GimbalAimer extends EventFilter2DMouseAdaptor implements FrameAnnotater, GimbalInterface, LaserOnOffControl, PropertyChangeListener {

    //   private static final Logger log = Logger.getLogger("net.sf.jaer");
    private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(GimbalAimer.class);

    private boolean jitterEnabled = getBoolean("jitterEnabled", false);
    private float jitterFreqHz = getFloat("jitterFreqHz", 1.0f);
    private float jitterAmplitude = getFloat("jitterAmplitude", 0.4f);

    /// These values are now reference to normalized 0-1 FOV
    private boolean invertPan = getBoolean("invertPan", false);
    private boolean invertTilt = getBoolean("invertTilt", false);
    private boolean linearMotion = getBoolean("linearMotion", false);
    private float limitOfPan = getFloat("limitOfPan", 0.9f);
    private float limitOfTilt = getFloat("limitOfTilt", 0.9f);
    private float PanValue = getFloat("panValue", 0.5f);
    private float tiltValue = getFloat("tiltValue", 0.5f);
    private float maxMovePerUpdate = getFloat("maxMovePerUpdate", 0.8f);
    private float minMovePerUpdate = getFloat("minMovePerUpdate", 0.02f);
    private int moveUpdateFreqHz = getInt("moveUpdateFreqHz", 100);
    private boolean recordingEnabled = false; // not used

    private GimbalBase gimbalbase;
    private GimbalAimerGUI gui;
    private String who = "";
    private Point2D.Float targetLocation = null;
    private float[] rgb = {0, 0, 0, 0};

    EngineeringFormat fmt = new EngineeringFormat();

    private final PropertyChangeSupport supportPanTilt = new PropertyChangeSupport(this);
>>>>>>> working

    private Trajectory commandTrajectory = new Trajectory("commandTrajectory"); // commands are issued via the mouse.
    private Trajectory targetTrajectory = new Trajectory("targetTrajectory");
    private Trajectory jitterTargetTrajectory = new Trajectory("jitterTargetTrajectory");
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Message.SetRecordingEnabled.name())) {
            recordingEnabled = (Boolean) evt.getNewValue();
        } else if (evt.getPropertyName().equals(Message.AbortRecording.name())) {
            recordingEnabled = false;
            if (commandTrajectory != null) {
                commandTrajectory.clear();
            }
        } else if (evt.getPropertyName().equals(Message.ClearRecording.name())) {
            if (commandTrajectory != null) {
                commandTrajectory.clear();
            }
        } else if (evt.getPropertyName().equals(Message.PanTiltSet.name())) {
            supportPanTilt.firePropertyChange(evt);
        } 
        
        else if (evt.getPropertyName().equals("PanTiltValues")) {
            float[] NewV = (float[]) evt.getNewValue();
            float[] OldV = (float[]) evt.getOldValue();

            this.PanValue = NewV[0];
            this.tiltValue = NewV[1];
            support.firePropertyChange("panValue", OldV[0], this.PanValue);
            support.firePropertyChange("tiltValue", OldV[1], this.tiltValue);
            supportPanTilt.firePropertyChange("panTiltValues", OldV, NewV);
        } else if (evt.getPropertyName().equals("Target")) {
            float[] NewV = (float[]) evt.getNewValue();
            targetTrajectory.add(NewV[0], NewV[1]);
        } else if (evt.getPropertyName().equals("JitterTarget")) {
            float[] NewV = (float[]) evt.getNewValue();
            this.jitterTargetTrajectory.add(NewV[0], NewV[1]);
        }
        
    }
<<<<<<< HEAD
    
=======

>>>>>>> working
    public enum Message {
        AbortRecording,
        ClearRecording,
        SetRecordingEnabled,
        PanTiltSet
    }

    /**
     * Constructs instance of the new 'filter' CalibratedPanTilt. The only time
     * events are actually used is during calibration. The PanTilt hardware
     * interface is also constructed.
     *
     * @param chip
     */
    public GimbalAimer(AEChip chip) {
<<<<<<< HEAD
        this(chip, Gimbal.getInstance());
        who="GimbalAimer";
        support = new PropertyChangeSupport(this);  // rjd
=======
        this(chip, GimbalBase.getInstance());
        targetLocation = new Point2D.Float(100, 100); // temporary home for the target
        initFilter();
        who = "GimbalAimer";
        support = new PropertyChangeSupport(this);
>>>>>>> working
    }

    /**
     * If a panTilt unit is already used by implementing classes it can be
     * handed to the PanTiltAimer for avoiding initializing multiple pantilts
<<<<<<< HEAD
     * @param chip 
     * @param pt the panTilt unit to be used*/
    public GimbalAimer(AEChip chip, Gimbal pt) {
        super(chip);
        panTiltHardware = pt;
        panTiltHardware.setJitterAmplitude(jitterAmplitude);
        panTiltHardware.setJitterFreqHz(jitterFreqHz);
        panTiltHardware.setJitterEnabled(jitterEnabled);
        panTiltHardware.setPanInverted(invertPan);
        panTiltHardware.setTiltInverted(invertTilt);
        panTiltHardware.setLimitOfPan(limitOfPan);
        panTiltHardware.setLimitOfTilt(limitOfTilt);
        panTiltHardware.addPropertyChangeListener(this); //We want to know the current position of the panTilt as it changes
        
        // <editor-fold defaultstate="collapsed" desc="-- Property Tooltips --">
        setPropertyTooltip("Jitter","jitterEnabled", "enables servo jitter to produce microsaccadic movement");
        setPropertyTooltip("Jitter","jitterAmplitude", "Jitter of pantilt amplitude for circular motion");
        setPropertyTooltip("Jitter","jitterFreqHz", "Jitter frequency in Hz of circular motion");
        
        setPropertyTooltip("Pan","panInverted", "flips the pan");
        setPropertyTooltip("Pan","limitOfPan", "limits pan around 0.5 by this amount to protect hardware");
        setPropertyTooltip("Pan","panValue", "The current value of the pan");
        
        setPropertyTooltip("Tilt","tiltInverted", "flips the tilt");
        setPropertyTooltip("Tilt","limitOfTilt", "limits tilt around 0.5 by this amount to protect hardware");
        setPropertyTooltip("Tilt","tiltValue", "The current value of the tilt");
        
        setPropertyTooltip("CamMove","maxMovePerUpdate", "Maximum change in ServoValues per update");
        setPropertyTooltip("CamMove","minMovePerUpdate", "Minimum change in ServoValues per update");
        setPropertyTooltip("CamMove","MoveUpdateFreqHz", "Frequenzy of updating the Servo values");
        setPropertyTooltip("CamMove","followEnabled", "Whether the PanTilt should automatically move towards the target or not");
        setPropertyTooltip("CamMove","linearMotion","Wheather the panTilt should move linearly or exponentially towards the target");
        
        setPropertyTooltip("center", "centers pan and tilt");
        setPropertyTooltip("disableServos", "disables servo PWM output. Servos should relax but digital servos may store last value and hold it.");
        setPropertyTooltip("aim", "show GUI for controlling pan and tilt");
        // </editor-fold>
    }

    @Override public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        return in;
    }

    @Override public void resetFilter() {
        panTiltHardware.close();
=======
     *
     * @param chip
     * @param pt the panTilt unit to be used
     */
    public GimbalAimer(AEChip chip, GimbalBase pt) {
        super(chip);
        gimbalbase = pt;
        gimbalbase.setJitterAmplitude(jitterAmplitude);
        gimbalbase.setJitterFreqHz(jitterFreqHz);
        gimbalbase.setJitterEnabled(jitterEnabled);
        gimbalbase.setPanInverted(invertPan);
        gimbalbase.setTiltInverted(invertTilt);
        gimbalbase.setLimitOfPan(limitOfPan);
        gimbalbase.setLimitOfTilt(limitOfTilt);
        gimbalbase.addPropertyChangeListener(this); //We want to know the current position of the panTilt as it changes

        gimbalbase.enableGimbal(true);
        gimbalbase.setTargetEnabled(true);
    }

    @Override
    public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        return in;
    }

    @Override
    public void resetFilter() {
        gimbalbase.close();
>>>>>>> working
    }

    @Override
    public void initFilter() {
        resetFilter();
<<<<<<< HEAD
    }

=======
        initDefaults();

    }

    private void initDefaults() {
        // see RCT for an example of setting defaults
    }

    // <editor-fold defaultstate="collapsed" desc="GUI button --ControllerGUI--">
    /**
     * Invokes the calibration GUI Calibration values are stored persistently as
     * preferences. Built automatically into filter parameter panel as an
     * action.
     */
    public void doControllerGUI() {
        getGimbalBase().rs4controllerGUI.setVisible(true);
    }
    // </editor-fold>

>>>>>>> working
    // <editor-fold defaultstate="collapsed" desc="GUI button --Aim--">
    /**
     * Invokes the calibration GUI Calibration values are stored persistently as
     * preferences. Built automatically into filter parameter panel as an
     * action.
     */
    public void doAim() {
        getGui().setVisible(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="GUI button --Center--">
    public void doCenter() {
        if (panTiltHardware != null) {
//            if(!panTiltHardware.isFollowEnabled()) panTiltHardware.setFollowEnabled(true);
            panTiltHardware.setTarget(0.5f, 0.5f);
            System.out.println("**** doCenter");
        }
    }
    // </editor-fold>

<<<<<<< HEAD
   
    @Override public void acquire(String who) {
        getPanTiltHardware().acquire(who);
    }

    @Override public boolean isLockOwned() {
        return getPanTiltHardware().isLockOwned();
    }

    @Override public void release(String who) {
        getPanTiltHardware().release(who);
    }

    @Override public void startJitter() {
        getPanTiltHardware().startJitter();
    }

    @Override public void stopJitter() {
        getPanTiltHardware().stopJitter();
    }

    @Override public void setLaserEnabled(boolean yes) {
        getPanTiltHardware().setLaserEnabled(yes);
=======
    @Override
    public void acquire(String who) {
        getGimbalBase().acquire(who);
    }

    @Override
    public boolean isLockOwned() {
        return getGimbalBase().isLockOwned();
    }

    @Override
    public void release(String who) {
        getGimbalBase().release(who);
    }

    @Override
    public void startJitter() {
        getGimbalBase().startJitter();
    }

    @Override
    public void stopJitter() {
        getGimbalBase().stopJitter();
    }

    @Override
    public void setLaserEnabled(boolean yes) {
        getGimbalBase().setLaserEnabled(yes);
>>>>>>> working
    }

    @Override
    public synchronized void setFilterEnabled(boolean yes) {
        super.setFilterEnabled(yes);
        if (yes) {
            panTiltHardware.setJitterAmplitude(jitterAmplitude);
            panTiltHardware.setJitterFreqHz(jitterFreqHz);
            panTiltHardware.setJitterEnabled(jitterEnabled);
            panTiltHardware.setPanInverted(invertPan);
            panTiltHardware.setTiltInverted(invertTilt);
            panTiltHardware.setLimitOfPan(limitOfPan);
            panTiltHardware.setLimitOfTilt(limitOfTilt);
        } else {
            try {
                panTiltHardware.stopJitter();
                panTiltHardware.close();
            } catch (Exception ex) {
                log.warning(ex.toString());
            }
        }
    }

    // Add and remove property change listeners
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * @return the gui
     */
    public GimbalAimerGUI getGui() {
<<<<<<< HEAD
        if(gui == null) {
            gui = new GimbalAimerGUI(panTiltHardware);
=======
        if (gui == null) {
            gui = new GimbalAimerGUI(gimbalbase);
>>>>>>> working
            gui.getSupport().addPropertyChangeListener(this);
        }
        return gui;
    }

    /**
     * @return the support
     */
    @Override
    public PropertyChangeSupport getSupport() {
        return supportPanTilt;
    }

    
<<<<<<< HEAD
    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanTiltHardware--">
    public Gimbal getPanTiltHardware() {
        if(panTiltHardware == null) {
            log.warning("No Pan-Tilt Hardware found. Initialising new PanTilt");
            panTiltHardware = Gimbal.getInstance();
=======
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
                getGimbalBase().getYaw(),
                getGimbalBase().getPitch()));
   
    } finally {
        gl.glPopMatrix();
    }

    return gl;
}

private GL2 drawTargetLocation(GL2 gl) {
    float sx = chip.getSizeX() / 32;
    float[] target = getGimbalBase().getTarget();
    log.info("GimbalPose (y,p) " + target[0] + ", " + target[1]);
    
    float pixelX = getGimbalBase().getFOV().getPixelsAtPan(target[0]);
    float pixelY = getGimbalBase().getFOV().getPixelsAtTilt(target[1]);
    log.info("Target pixels received from FOV (p,t) " + pixelX + ", " + pixelY);
    
    gl.glPushMatrix();
    gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT);
    try {
        gl.glTranslatef(pixelX, pixelY, 0);
        gl.glColor3f(0, 1, 1);
        DrawGL.drawCircle(gl, 0f, 0f, sx, 10);

        // Text annotation on clusters
        GLUT cGLUT = chip.getCanvas().getGlut();
        final int font = GLUT.BITMAP_TIMES_ROMAN_24;
        gl.glRasterPos3f(0, sx, 0);
        cGLUT.glutBitmapString(font, String.format("FOV TARG(y,p) %.1f, %.1f deg ",
                getGimbalBase().getFOV().getYawAtPan(target[0]),
                getGimbalBase().getFOV().getPitchAtTilt(target[1]) ));
    } finally {
        gl.glPopAttrib();
        gl.glPopMatrix();
    }
    return gl;
}

    
    
    
    
    
    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanTiltHardware--">
    public GimbalBase getGimbalBase() {
        if (gimbalbase == null) {
            log.warn("No Pan-Tilt Hardware found. Initialising new PanTilt");
            gimbalbase = GimbalBase.getInstance();
>>>>>>> working
        }
        return panTiltHardware;
    }

    public void setPanTiltHardware(Gimbal panTilt) {
        this.panTiltHardware = panTilt;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --jitterEnabled--">
    /**
     * checks if jitter is enabled
     *
     * @return the jitterEnabled
     */
    public boolean isJitterEnabled() {
        return getPanTiltHardware().isJitterEnabled();
    }

    /**
     * sets the jitter flag true or false
     *
     * @param jitterEnabled the jitterEnabled to set
     */
    public void setJitterEnabled(boolean jitterEnabled) {
        putBoolean("jitterEnabled", jitterEnabled);
        boolean OldValue = this.jitterEnabled;
//        if(!isFollowEnabled()) setFollowEnabled(true); //To start jittering the pantilt must follow target

        this.jitterEnabled = jitterEnabled;
<<<<<<< HEAD
        getPanTiltHardware().setJitterEnabled(jitterEnabled);
        support.firePropertyChange("jitterEnabled",OldValue,jitterEnabled);
=======
        getGimbalBase().setJitterEnabled(jitterEnabled);
        support.firePropertyChange("jitterEnabled", OldValue, jitterEnabled);
>>>>>>> working
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --jitterAmplitude--">
    /**
     * gets the amplitude of the jitter
     *
     * @return the amplitude of the jitter
     */
    @Override
    public float getJitterAmplitude() {
        return getPanTiltHardware().getJitterAmplitude();
    }

    /**
     * Sets the amplitude (1/2 of peak to peak) of circular jitter of pan tilt
     * during jittering
     *
     * @param jitterAmplitude the amplitude
     */
    @Override
    public void setJitterAmplitude(float jitterAmplitude) {
        putFloat("jitterAmplitude", jitterAmplitude);
        float OldValue = this.jitterAmplitude;

        this.jitterAmplitude = jitterAmplitude;
<<<<<<< HEAD
        getPanTiltHardware().setJitterAmplitude(jitterAmplitude);
        support.firePropertyChange("jitterAmplitude",OldValue,jitterAmplitude);
=======
        getGimbalBase().setJitterAmplitude(jitterAmplitude);
        support.firePropertyChange("jitterAmplitude", OldValue, jitterAmplitude);
>>>>>>> working
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --jitterFreqHz--">
    /**
     * gets the frequency of the jitter
     *
     * @return the frequency of the jitter
     */
    @Override
    public float getJitterFreqHz() {
        return getPanTiltHardware().getJitterFreqHz();
    }

    /**
     * sets the frequency of the jitter
     *
     * @param jitterFreqHz in Hz
     */
    @Override
    public void setJitterFreqHz(float jitterFreqHz) {
        putFloat("jitterFreqHz", jitterFreqHz);
        float OldValue = this.jitterFreqHz;

        this.jitterFreqHz = jitterFreqHz;
<<<<<<< HEAD
        getPanTiltHardware().setJitterFreqHz(jitterFreqHz);
        support.firePropertyChange("jitterFreqHz",OldValue,jitterFreqHz);
=======
        getGimbalBase().setJitterFreqHz(jitterFreqHz);
        support.firePropertyChange("jitterFreqHz", OldValue, jitterFreqHz);
>>>>>>> working
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --MinMovePerUpdate--">
    public float getMinMovePerUpdate() {
        return getPanTiltHardware().getMinMovePerUpdate();
    }

    public void setMinMovePerUpdate(float MinMove) {
        putFloat("minMovePerUpdate", MinMove);
        float OldValue = getMinMovePerUpdate();
<<<<<<< HEAD
        getPanTiltHardware().setMinMovePerUpdate(MinMove);
        this.minMovePerUpdate=MinMove;
        support.firePropertyChange("minMovePerUpdate",OldValue,MinMove);
=======
        getGimbalBase().setMinMovePerUpdate(MinMove);
        this.minMovePerUpdate = MinMove;
        support.firePropertyChange("minMovePerUpdate", OldValue, MinMove);
>>>>>>> working
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --MaxMovePerUpdate--">
    public float getMaxMovePerUpdate() {
        return getPanTiltHardware().getMaxMovePerUpdate();
    }

    public void setMaxMovePerUpdate(float MaxMove) {
        putFloat("maxMovePerUpdate", MaxMove);
        float OldValue = getMaxMovePerUpdate();
<<<<<<< HEAD
        getPanTiltHardware().setMaxMovePerUpdate(MaxMove);
        this.maxMovePerUpdate=MaxMove;
        support.firePropertyChange("maxMovePerUpdate",OldValue,MaxMove);
=======
        getGimbalBase().setMaxMovePerUpdate(MaxMove);
        this.maxMovePerUpdate = MaxMove;
        support.firePropertyChange("maxMovePerUpdate", OldValue, MaxMove);
>>>>>>> working
    }
    // </editor-fold>    

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --MoveUpdateFreqHz--">
    public int getMoveUpdateFreqHz() {
        return getPanTiltHardware().getMoveUpdateFreqHz();
    }

    public void setMoveUpdateFreqHz(int UpdateFreq) {
        putFloat("moveUpdateFreqHz", UpdateFreq);
        float OldValue = getMoveUpdateFreqHz();
<<<<<<< HEAD
        getPanTiltHardware().setMoveUpdateFreqHz(UpdateFreq);
        this.moveUpdateFreqHz=UpdateFreq;
        support.firePropertyChange("moveUpdateFreqHz",OldValue,UpdateFreq);
    }
    // </editor-fold> 
    
    
    
=======
        getGimbalBase().setMoveUpdateFreqHz(UpdateFreq);
        this.moveUpdateFreqHz = UpdateFreq;
        support.firePropertyChange("moveUpdateFreqHz", OldValue, UpdateFreq);
    }
    // </editor-fold> 

//    public void setPanTiltValues(float pan, float tilt) {
//    }
//    
    public float getTiltValue() {
        return this.tiltValue;
    }

    public void setTiltValue(float TiltValue) {
        putFloat("tiltValue", TiltValue);
        float OldValue = this.tiltValue;
        this.tiltValue = TiltValue;
        support.firePropertyChange("tiltValue", OldValue, TiltValue);
        getGimbalBase().setTarget(this.PanValue, TiltValue);
    }

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanValue--">
    public float getPanValue() {
        return this.PanValue;
    }

    public void setPanValue(float PanValue) {
        putFloat("panValue", PanValue);
        float OldValue = this.PanValue;
        this.PanValue = PanValue;
        support.firePropertyChange("panValue", OldValue, PanValue);
        getGimbalBase().setTarget(PanValue, this.tiltValue);
    }

>>>>>>> working
    public void setPanTiltVisualAimPixels(float pan, float tilt) {
        // convert pixels to normalized (0-1) location
        float normPan = pan / chip.getSizeX();
        float normTilt = tilt / chip.getSizeY();
        setPanTiltTarget(normPan, normTilt);
<<<<<<< HEAD
        log.info( "Forwarding AimPixels to Gimbal as normalized values.");
=======
        log.info("*******Forwarding AimPixels to Gimbal as normalized values.");
>>>>>>> working
    }

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanTiltTarget--">
    public float[] getPanTiltTarget() {
        return getPanTiltHardware().getTarget();
    }

    public void setPanTiltTarget(float PanTarget, float TiltTarget) {
<<<<<<< HEAD
        getPanTiltHardware().setTarget(PanTarget, TiltTarget);
=======

        getGimbalBase().setTarget(PanTarget, TiltTarget);

        //       getGimbalBase().setPanTiltValues(PanTarget, TiltTarget);
>>>>>>> working
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanTiltValues--">
    @Override
    public float[] getPanTiltValues() {
        return getPanTiltHardware().getPanTiltValues();
    }

    /**
     * Sets the pan and tilt servo values
     *
     * @param pan 0 to 1 value
     * @param tilt 0 to 1 value
     */
    @Override
    public void setPanTiltValues(float pan, float tilt) throws HardwareInterfaceException {
        getPanTiltHardware().setPanTiltValues(pan, tilt);
    }
    // </editor-fold>

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="getter/setter for --TiltInverted--">
    /**
     * checks if tilt is inverted
     *
     * @return tiltinverted
     */
    public boolean isTiltInverted() {
        return getPanTiltHardware().getTiltInverted();
    }

    /**
     * sets weather tilt is inverted
     *
     * @param tiltInverted value to be set
     */
    public void setTiltInverted(boolean tiltInverted) {
        putBoolean("invertTilt", tiltInverted);
        boolean OldValue = isTiltInverted();
        getPanTiltHardware().setTiltInverted(tiltInverted);
        this.invertTilt = tiltInverted;
        getSupport().firePropertyChange("invertTilt", OldValue, tiltInverted);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanInverted--">
    /**
     * checks if pan is inverted
     *
     * @return paninverted
     */
    public boolean isPanInverted() {
        return getPanTiltHardware().getPanInverted();
    }

    /**
     * sets weather pan is inverted
     *
     * @param panInverted value to be set
     */
    public void setPanInverted(boolean panInverted) {
        putBoolean("invertPan", panInverted);
        boolean OldValue = isPanInverted();
        getPanTiltHardware().setPanInverted(panInverted);
        this.invertPan = panInverted;
        getSupport().firePropertyChange("invertPan", OldValue, panInverted);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --LimitOfTilt--">
    /**
     * gets the limit of the tilt for the hardware
     *
     * @return the tiltLimit
     */
    public float getLimitOfTilt() {
        return getPanTiltHardware().getLimitOfTilt();
    }

    /**
     * sets the limit of the tilt for the hardware
     *
     * @param TiltLimit the TiltLimit to set
     */
    public void setLimitOfTilt(float TiltLimit) {
        putFloat("limitOfTilt", TiltLimit);
        float OldValue = getLimitOfTilt();
<<<<<<< HEAD
        getPanTiltHardware().setLimitOfTilt(TiltLimit);
        this.limitOfTilt=TiltLimit;
        getSupport().firePropertyChange("limitOfTilt",OldValue,TiltLimit);
=======
        getGimbalBase().setLimitOfTilt(TiltLimit);
        this.limitOfTilt = TiltLimit;
        getSupport().firePropertyChange("limitOfTilt", OldValue, TiltLimit);
>>>>>>> working
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter/setter for --LimitOfPan--">
    /**
     * gets the limit of the pan for the hardware
     *
     * @return the panLimit
     */
    public float getLimitOfPan() {
        return getPanTiltHardware().getLimitOfPan();
    }

    /**
     * sets the limit of the pan for the hardware
     *
     * @param PanLimit the PanLimit to set
     */
    public void setLimitOfPan(float PanLimit) {
        putFloat("limitOfPan", PanLimit);
        float OldValue = getLimitOfPan();
<<<<<<< HEAD
        getPanTiltHardware().setLimitOfPan(PanLimit);
        this.limitOfPan=PanLimit;
        getSupport().firePropertyChange("limitOfPan",OldValue,PanLimit);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="getter/setter for --TiltValue--">
    public float getTiltValue() {
        return this.tiltValue;
    }
    
    public void setTiltValue(float TiltValue) {
        putFloat("tiltValue",TiltValue);
        float OldValue = this.tiltValue;
        this.tiltValue = TiltValue;
        support.firePropertyChange("tiltValue",OldValue,TiltValue);  
        getPanTiltHardware().setTarget(this.PanValue, TiltValue);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="getter/setter for --PanValue--">
    public float getPanValue() {
        return this.PanValue;
    }
    
    public void setPanValue(float PanValue) {
        putFloat("panValue",PanValue);
        float OldValue = this.PanValue;
        this.PanValue = PanValue;
        support.firePropertyChange("panValue",OldValue,PanValue);
        getPanTiltHardware().setTarget(PanValue,this.tiltValue);
    }

    // </editor-fold>
    
=======
        getGimbalBase().setLimitOfPan(PanLimit);
        this.limitOfPan = PanLimit;
        getSupport().firePropertyChange("limitOfPan", OldValue, PanLimit);
    }
    // </editor-fold>
    // </editor-fold>

>>>>>>> working
    // <editor-fold defaultstate="collapsed" desc="getter/setter for --linearMotion--">
    public boolean isLinearMotion() {
        return getPanTiltHardware().isLinearSpeedEnabled();
    }

    public void setLinearMotion(boolean linearMotion) {
        putBoolean("linearMotion", linearMotion);
        boolean OldValue = isLinearMotion();
        getPanTiltHardware().setLinearSpeedEnabled(linearMotion);
        this.linearMotion = linearMotion;
        getSupport().firePropertyChange("linearMotion", OldValue, linearMotion);
    }
    // </editor-fold>

}
