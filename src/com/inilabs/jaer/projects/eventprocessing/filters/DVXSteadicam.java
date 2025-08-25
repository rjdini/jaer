/* 
 * Steadicam (DVXplorer + BMI160)
 * Rotation + translation stabilization for DVS-only (DVXplorer) using Bosch BMI160 IMU.
 * - Reads IMU samples from AEPacketRaw via IMUSample.constructFromAEPacketRaw(...)
 * - Integrates gyro (deg/s) -> radians, high-pass filters pan/tilt/roll, 
 *   applies per-event 2D transform: R(theta) around COR + pixel translation from pan/tilt.
 * - Robust output building via OutputEventIterator with fallbacks.
* 
* DVS-only steadicam with full NetBeans Bean support like the original Steadicam.
 */

package com.inilabs.jaer.projects.eventprocessing.filters;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.TextRenderer;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.*;
import net.sf.jaer.eventio.AEFileInputStreamInterface;
import net.sf.jaer.eventio.AEInputStream;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.graphics.AEViewer;
import net.sf.jaer.graphics.AbstractAEPlayer;
import net.sf.jaer.graphics.FrameAnnotater;
import net.sf.jaer.util.filter.HighpassFilter;
import static net.sf.jaer.eventprocessing.EventFilter.log;
import net.sf.jaer.aemonitor.AEPacketRaw;

// BMI160 IMU classes
import com.inilabs.jaer.projects.dvxplorer.imu.IMUSample;
import com.inilabs.jaer.projects.dvxplorer.imu.IMUSampleType;

import java.awt.Point;
import java.awt.event.MouseEvent;

@Description("DVXplorer Steadicam: compensates global translation and rotation using built-in BMI160 IMU (with full property panel support).")
@DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class DVXSteadicam extends EventFilter2DMouseAdaptor implements FrameAnnotater, Observer, PropertyChangeListener {

    public enum CameraRotationEstimator { VORSensor }
    private CameraRotationEstimator cameraRotationEstimator = CameraRotationEstimator.VORSensor;

    // ---- User properties (persist + show via Beans) ----
    
    // bypass for debugging
    private boolean bypass = getBoolean("bypass", true);
    public boolean isBypass() { return bypass; }
    public void setBypass(boolean v) { bypass = v; putBoolean("bypass", v); }
    //
    
    private float  gainPanTiltServos = getFloat("gainPanTiltServos", 1);     // compat placeholders
    private boolean feedforwardEnabled = getBoolean("feedforwardEnabled", false);
    private boolean panTiltEnabled     = getBoolean("panTiltEnabled", false);

    private boolean electronicStabilizationEnabled = getBoolean("electronicStabilizationEnabled", true);
    private boolean annotateEnclosedEnabled = getBoolean("annotateEnclosedEnabled", true);
    private boolean flipContrast = getBoolean("flipContrast", false);
    private boolean showTransformRectangle = getBoolean("showTransformRectangle", true);
    private boolean showGrid = getBoolean("showGrid", true);
    private boolean disableTranslation = getBoolean("disableTranslation", false);
    private boolean disableRotation    = getBoolean("disableRotation", false);
    private float imuLagMs = getFloat("imuLagMs", 0);
    private final int lagUs = (int)(imuLagMs * 1000);

    private float lensFocalLengthMm = getFloat("lensFocalLengthMm", 8.5f);
    private float radPerPixel;

    private float highpassTauMsTranslation = getFloat("highpassTauMsTranslation", 1000);
    private float highpassTauMsRotation    = getFloat("highpassTauMsRotation", 1000);
    private int   transformResetLimitDegrees = getInt("transformResetLimitDegrees", 45);

    // ---- IMU state / filters ----
    private float panRate=0, tiltRate=0, rollRate=0; // deg/s
    private float panOffset=getFloat("panOffset",0), tiltOffset=getFloat("tiltOffset",0), rollOffset=getFloat("rollOffset",0);
    private float panTranslationDeg=0, tiltTranslationDeg=0, rollDeg=0;
    private float panDC=0, tiltDC=0, rollDC=0;
    private HighpassFilter panTranslationFilter=new HighpassFilter(), tiltTranslationFilter=new HighpassFilter(), rollFilter=new HighpassFilter();
    private HighpassFilter filterX=new HighpassFilter(), filterY=new HighpassFilter(), filterRotation=new HighpassFilter();

    // ---- Transform state ----
    private TransformAtTime lastTransform=null;
    private boolean initialized=false, evenMotion=true;

    // ---- Raw AER IMU parsing ----
    private IMUSample.IncompleteIMUSampleException pendingImu=null;

    // ---- Geometry / UI ----
    private int sxm1, sym1, sx2, sy2;
    private Point centerOfRotation=null;
    private boolean centerOfRotationSelectionPending=false;
    private TextRenderer imuTextRenderer=null;

    // ---- Calibration ----
    private boolean calibrating=false;
    private int calibrationSampleCount=0;
    private final int NUM_CALIBRATION_SAMPLES_DEFAULT=800;
    private int numCalibrationSamples=getInt("numCalibrationSamples", NUM_CALIBRATION_SAMPLES_DEFAULT);
    private CalibrationFilter panCalibrator=new CalibrationFilter(), tiltCalibrator=new CalibrationFilter(), rollCalibrator=new CalibrationFilter();

    // ---- Event FIFO for IMU lag ----
    private static class E { short x,y; int ts; byte type; Object polarity; boolean filteredOut; }
    private Deque<E> fifo=new ArrayDeque<>(2048);
    private E held=null;

    private EventPacket outPacket=null;
    
    public DVXSteadicam(AEChip chip){
        super(chip);
        chip.addObserver(this);
        addObserver(this);

        // Grouped tooltips similar to original
        String transform="Transform", display="Display", imu="IMU", pantilt="Pan-Tilt";
           // bypass debug tooltip in ctor
        setPropertyTooltip("bypass", "Debug: pass input through untouched");
        
        setPropertyTooltip("cameraRotationEstimator","<html>Rotation source (VORSensor=IMU)</html>");
        setPropertyTooltip(display,"flipContrast","flips contrast depending on motion sign");
        setPropertyTooltip(display,"annotateEnclosedEnabled","show annotation of enclosed filters");
        setPropertyTooltip(transform,"highpassTauMsTranslation","high-pass tau (ms) for pan/tilt");
        setPropertyTooltip(transform,"highpassTauMsRotation","high-pass tau (ms) for roll");
        setPropertyTooltip(transform,"lensFocalLengthMm","lens focal length (mm) for rotation→pixel scale");
        setPropertyTooltip(transform,"transformResetLimitDegrees","auto-reset if exceeded");
        setPropertyTooltip(display,"showTransformRectangle","draw transform viz");
        setPropertyTooltip(display,"showGrid","draw grid");
        setPropertyTooltip(transform,"disableRotation","disable rotational stabilization");
        setPropertyTooltip(transform,"disableTranslation","disable translational stabilization");
        setPropertyTooltip(transform,"selectCenterOfRotation","select COR via mouse");
        setPropertyTooltip(transform,"eraseCenterOfRotationSelection","reset COR to center");
        setPropertyTooltip(imu,"zeroGyro","zero gyro offsets (keep still)");
        setPropertyTooltip(imu,"eraseGyroZero","erase stored gyro zeros");
        setPropertyTooltip(imu,"numCalibrationSamples","samples for zeroing");
        setPropertyTooltip("electronicStabilizationEnabled","apply R+T to events from IMU");
        setPropertyTooltip(imu,"imuLagMs","IMU lag (ms)");
        setPropertyTooltip(pantilt,"gainPanTiltServos","compat; no HW");
        setPropertyTooltip(pantilt,"panTiltEnabled","compat; no HW");

        initFilter();
    }

    // ---------------- RAW path: parse BMI160 and update transform ----------------
    //@Override
    public AEPacketRaw filterPacket(AEPacketRaw in){
        if (bypass) return in;   // DEBUG  bypass to check basic filer functionality
        
        if(in==null) return in;
        final int n=(in.addresses!=null)?in.addresses.length:0; int i=0;
        while(i<n){
            try{
                int code=IMUSample.extractSampleTypeCode(in.addresses[i]);
                if(code!=IMUSampleType.ax.code){ i++; continue; }
                IMUSample s=IMUSample.constructFromAEPacketRaw(in,i,pendingImu); pendingImu=null;

                final int ts=s.getTimestampUs();
                final float dt=Math.max(0f, s.getDeltaTimeUs()*1e-6f);

                panRate = s.getGyroYawY();
                tiltRate= s.getGyroTiltX();
                rollRate= s.getGyroRollZ();

                if(calibrating){
                    calibrationSampleCount++;
                    if(calibrationSampleCount>numCalibrationSamples){
                        calibrating=false;
                        panOffset = panCalibrator.computeAverage();
                        tiltOffset= tiltCalibrator.computeAverage();
                        rollOffset= rollCalibrator.computeAverage();
                        putFloat("panOffset",panOffset); putFloat("tiltOffset",tiltOffset); putFloat("rollOffset",rollOffset);
                    }else{
                        panCalibrator.addSample(panRate); tiltCalibrator.addSample(tiltRate); rollCalibrator.addSample(rollRate);
                    }
                    i+=IMUSample.SIZE_EVENTS; continue;
                }

                panDC  += getPanRate()*dt;
                tiltDC += getTiltRate()*dt;
                rollDC += getRollRate()*dt;

                panTranslationDeg  = panTranslationFilter.filter(panDC, ts);
                tiltTranslationDeg = tiltTranslationFilter.filter(tiltDC, ts);
                rollDeg            = rollFilter.filter(rollDC, ts);

                if(Math.abs(panTranslationDeg)>transformResetLimitDegrees
                || Math.abs(tiltTranslationDeg)>transformResetLimitDegrees
                || Math.abs(rollDeg) > transformResetLimitDegrees*3){
                    resetTransform();
                }

                if(flipContrast){
                    evenMotion = Math.abs(panRate)>Math.abs(tiltRate) ? (panRate>0) : (tiltRate>0);
                }

                if(initialized){
                    float txPix = disableTranslation ? 0f : (float)((Math.PI/180.0)*panTranslationDeg)/radPerPixel;
                    float tyPix = disableTranslation ? 0f : (float)((Math.PI/180.0)*tiltTranslationDeg)/radPerPixel;
                    float rRad  = disableRotation    ? 0f : (float)(-rollDeg*Math.PI/180.0);
                    lastTransform = new TransformAtTime(ts, new Point2D.Float(txPix,tyPix), rRad);
                }

                i+=IMUSample.SIZE_EVENTS;
            }catch(IMUSample.IncompleteIMUSampleException ex){
                pendingImu=ex; break;
            }catch(Throwable t){
                i++; // skip malformed word
            }
        }
        return in;
    }

    // ---------------- 2D path: warp events with current transform ----------------
    @Override @SuppressWarnings("unchecked")
    public EventPacket<?> filterPacket(EventPacket<?> in){
         if (bypass) return in;   // DEBUG  bypass to check basic filer functionality

        if(in==null || in.isEmpty()) return in;

        sx2=chip.getSizeX()/2; sy2=chip.getSizeY()/2;
        sxm1=chip.getSizeX()-1; sym1=chip.getSizeY()-1;
        final int corx = centerOfRotation==null? this.sx2 : centerOfRotation.x;
        final int cory = centerOfRotation==null? this.sy2 : centerOfRotation.y;

        EventPacket<PolarityEvent> out=new EventPacket<>(PolarityEvent.class);
        //if (outputPacket == null) {
         //   outputPacket = new ApsDvsEventPacket(in.getEventClass());
       // }
        
        OutputEventIterator itrOut=null; try{ itrOut=out.outputIterator(); }catch(Throwable ignored){}

        Iterator<?> it=in.iterator();
        while(it.hasNext()){
            Object o=it.next(); if(!(o instanceof PolarityEvent)) continue;
            PolarityEvent ev=(PolarityEvent)o;

            push(ev);

            PolarityEvent be;
       //     while((be=peek())!=null && (be.timestamp<=ev.timestamp-(int)(imuLagMs*1000) || be.timestamp>ev.timestamp)){
             while ((be = peek()) != null && be.timestamp <= ev.timestamp - lagUs) {         
                 
                be=pop(); if(be==null) break;
                if(electronicStabilizationEnabled && lastTransform!=null){
                    final int nx=be.x-corx, ny=be.y-cory;
                    final short newx=(short)Math.round(((lastTransform.cosAngle*nx)-(lastTransform.sinAngle*ny)+lastTransform.translationPixels.x)+corx);
                    final short newy=(short)Math.round(((lastTransform.sinAngle*nx)+(lastTransform.cosAngle*ny)+lastTransform.translationPixels.y)+cory);
                    be.x=newx; be.y=newy;

                    if((be.x>sxm1)||(be.x<0)||(be.y>sym1)||(be.y<0)) be.setFilteredOut(true);
                    else be.setFilteredOut(false);
                    if(flipContrast && evenMotion) togglePolarity(be);
                }
                if(itrOut!=null){ itrOut.nextOutput().copyFrom(be); }
                else{
                    try{ out.appendCopyOfEvent(be); }catch(Throwable t){ try{ out.appendCopyOfEvent(be);}catch(Throwable t2){ return in; } }
                }
            }
        }
        return out;
    }

    // ---------------- FIFO / polarity helpers ----------------
    private void push(PolarityEvent e){ if(imuLagMs==0){held=toE(e); return;} if(fifo.size()>1<<20) fifo.clear(); fifo.addLast(toE(e));}
    //private PolarityEvent pop(){ if(imuLagMs==0) return fromE(held); E e=fifo.pollFirst(); return fromE(e); }
    private PolarityEvent pop() {
    if (imuLagMs == 0) {
        PolarityEvent r = fromE(held);
        held = null;                    // <-- critical: clear it
        return r;
    }
    E e = fifo.pollFirst();
    return fromE(e);
}
    
    private PolarityEvent peek(){ if(imuLagMs==0) return fromE(held); return fromE(fifo.peekFirst()); }

    private E toE(PolarityEvent p){ if(p==null) return null; E e=new E(); e.x=p.x; e.y=p.y; e.ts=p.timestamp; e.type=p.type; e.polarity=getPolarityObject(p); e.filteredOut=p.isFilteredOut(); return e; }
    
    private PolarityEvent fromE(E e){ if(e==null) return null; PolarityEvent p=new PolarityEvent(); p.x=e.x; p.y=e.y; p.timestamp=e.ts; p.type=e.type; setPolarityObject(p,e.polarity); p.setFilteredOut(e.filteredOut); return p; }

    

    
    
    
    private Object getPolarityObject(PolarityEvent e){
        try{ return e.getClass().getMethod("getPolarity").invoke(e); }
        catch(Throwable t){ try{ return e.getClass().getField("polarity").get(e);}catch(Throwable t2){ return null; } }
    }
    @SuppressWarnings({ "rawtypes","unchecked" })
    private void setPolarityObject(PolarityEvent e, Object obj){
        if(obj==null) return;
        try{ e.getClass().getMethod("setPolarity", obj.getClass()).invoke(e, obj); }
        catch(Throwable t){ try{ e.getClass().getField("polarity").set(e,obj);}catch(Throwable t2){} }
    }
    @SuppressWarnings({ "unchecked","rawtypes" })
    private void togglePolarity(PolarityEvent e){
        try{
            Object pol=getPolarityObject(e);
            if(pol!=null){
                String s=pol.toString().toLowerCase();
                if(s.contains("on")) setPolarityEnumByName(e,"Off");
                else if(s.contains("off")) setPolarityEnumByName(e,"On");
            }else{
                boolean val=(boolean)e.getClass().getField("polarity").get(e);
                e.getClass().getField("polarity").set(e,!val);
            }
        }catch(Throwable ignored){}
    }
    @SuppressWarnings({ "unchecked","rawtypes" })
    private void setPolarityEnumByName(PolarityEvent e, String name){
        try{
            Class<?> polEnum=Class.forName("net.sf.jaer.event.PolarityEvent$Polarity");
            Object newVal=java.lang.Enum.valueOf((Class<Enum>)polEnum.asSubclass(Enum.class), name);
            e.getClass().getMethod("setPolarity", polEnum).invoke(e, newVal);
        }catch(Throwable ignored){}
    }

    // ---------------- lifecycle ----------------
    private void resetTransform(){ panDC=tiltDC=rollDC=0; panTranslationDeg=tiltTranslationDeg=rollDeg=0; panTranslationFilter.reset(); tiltTranslationFilter.reset(); rollFilter.reset(); }

    @Override synchronized public void resetFilter(){
        panRate=tiltRate=rollRate=0;
        resetTransform();
        // match original Steadicam UI scaling
        radPerPixel = (float)Math.asin((getChip().getPixelWidthUm()*1e-3f)/lensFocalLengthMm);
        filterX.reset(); filterY.reset(); filterRotation.reset();
        lastTransform=null;
        fifo.clear(); held=null;
        initialized=true;
    }

    @Override public void initFilter(){
        rollFilter.setTauMs(highpassTauMsRotation);
        panTranslationFilter.setTauMs(highpassTauMsTranslation);
        tiltTranslationFilter.setTauMs(highpassTauMsTranslation);
        filterX.setTauMs(highpassTauMsTranslation);
        filterY.setTauMs(highpassTauMsTranslation);
        resetFilter();
        if(chip.getAeViewer()!=null){ chip.getAeViewer().getSupport().addPropertyChangeListener(this); }
    }

    // ---------------- Bean PROPERTIES (for property sheet) ----------------
    public boolean isElectronicStabilizationEnabled(){ return electronicStabilizationEnabled; }
    public void setElectronicStabilizationEnabled(boolean v){ electronicStabilizationEnabled=v; putBoolean("electronicStabilizationEnabled",v); }

    public boolean isFlipContrast(){ return flipContrast; }
    public void setFlipContrast(boolean v){ flipContrast=v; putBoolean("flipContrast",v); }

    public boolean isAnnotateEnclosedEnabled(){ return annotateEnclosedEnabled; }
    public void setAnnotateEnclosedEnabled(boolean v){ annotateEnclosedEnabled=v; putBoolean("annotateEnclosedEnabled",v); }

    public boolean isShowTransformRectangle(){ return showTransformRectangle; }
    public void setShowTransformRectangle(boolean v){ showTransformRectangle=v; putBoolean("showTransformRectangle",v); }

    public boolean isShowGrid(){ return showGrid; }
    public void setShowGrid(boolean v){ showGrid=v; putBoolean("showGrid",v); }

    public boolean isDisableTranslation(){ return disableTranslation; }
    public void setDisableTranslation(boolean v){ disableTranslation=v; putBoolean("disableTranslation",v); }

    public boolean isDisableRotation(){ return disableRotation; }
    public void setDisableRotation(boolean v){ disableRotation=v; putBoolean("disableRotation",v); }

    public float getHighpassTauMsTranslation(){ return highpassTauMsTranslation; }
    public void setHighpassTauMsTranslation(float v){ highpassTauMsTranslation=v; putFloat("highpassTauMsTranslation",v);
        panTranslationFilter.setTauMs(v); tiltTranslationFilter.setTauMs(v); filterX.setTauMs(v); filterY.setTauMs(v); }

    public float getHighpassTauMsRotation(){ return highpassTauMsRotation; }
    public void setHighpassTauMsRotation(float v){ highpassTauMsRotation=v; putFloat("highpassTauMsRotation",v); rollFilter.setTauMs(v); }

    public float getLensFocalLengthMm(){ return lensFocalLengthMm; }
    public void setLensFocalLengthMm(float v){ lensFocalLengthMm=v; putFloat("lensFocalLengthMm",v);
        radPerPixel=(float)Math.asin((getChip().getPixelWidthUm()*1e-3f)/lensFocalLengthMm); }

    public int getTransformResetLimitDegrees(){ return transformResetLimitDegrees; }
    public void setTransformResetLimitDegrees(int v){ transformResetLimitDegrees=v; putInt("transformResetLimitDegrees",v); }

    public float getImuLagMs(){ return imuLagMs; }
    public void setImuLagMs(float v){ imuLagMs=v; putFloat("imuLagMs",v); }

    public int getNumCalibrationSamples(){ return numCalibrationSamples; }
    public void setNumCalibrationSamples(int n){ numCalibrationSamples=n; putInt("numCalibrationSamples",n); }

    // Compatibility properties
    public float getGainPanTiltServos(){ return gainPanTiltServos; }
    public void setGainPanTiltServos(float v){ gainPanTiltServos=v; putFloat("gainPanTiltServos",v); }
    public boolean isFeedforwardEnabled(){ return feedforwardEnabled; }
    public void setFeedforwardEnabled(boolean v){ feedforwardEnabled=v; putBoolean("feedforwardEnabled",v); }
    public boolean isPanTiltEnabled(){ return panTiltEnabled; }
    public void setPanTiltEnabled(boolean v){ panTiltEnabled=v; putBoolean("panTiltEnabled",v); }

    public float getPanOffset(){ return panOffset; }    public void setPanOffset(float v){ panOffset=v; putFloat("panOffset",v); }
    public float getTiltOffset(){ return tiltOffset; }  public void setTiltOffset(float v){ tiltOffset=v; putFloat("tiltOffset",v); }
    public float getRollOffset(){ return rollOffset; }  public void setRollOffset(float v){ rollOffset=v; putFloat("rollOffset",v); }

    public CameraRotationEstimator getCameraRotationEstimator(){ return cameraRotationEstimator; }
    public void setCameraRotationEstimator(CameraRotationEstimator est){ this.cameraRotationEstimator=est; putString("positionComputer", est.toString()); }

    // Readouts (deg/s after offsets)
    public float getPanRate(){ return panRate - panOffset; }
    public float getTiltRate(){ return tiltRate - tiltOffset; }
    public float getRollRate(){ return rollRate - rollOffset; }

    // ---------------- Actions (buttons in property sheet) ----------------
    public void doZeroGyro(){ calibrating=true; calibrationSampleCount=0; panCalibrator.reset(); tiltCalibrator.reset(); rollCalibrator.reset(); log.info("gyro zeroing started"); }
    public void doEraseGyroZero(){ panOffset=tiltOffset=rollOffset=0; putFloat("panOffset",0); putFloat("tiltOffset",0); putFloat("rollOffset",0); log.info("gyro zeroing erased"); }
    public void doSelectCenterOfRotation(){ centerOfRotationSelectionPending=true; log.info("select COR by mouse click"); }
    public void doEraseCenterOfRotationSelection(){ centerOfRotation=null; putInt("centerOfRotationX",-1); putInt("centerOfRotationY",-1); }

    // ---------------- UI / mouse / observers ----------------
    @Override public void annotate(GLAutoDrawable drawable){
        if(calibrating){
            if(imuTextRenderer==null) imuTextRenderer=new TextRenderer(new Font("SansSerif", Font.PLAIN, 36));
            imuTextRenderer.begin3DRendering();
            imuTextRenderer.setColor(1,1,1,1);
            final String saz=String.format("Don't move sensor (Calibrating %d/%d)", calibrationSampleCount, numCalibrationSamples);
            Rectangle2D rect=imuTextRenderer.getBounds(saz);
            final float scale=.25f;
            imuTextRenderer.draw3D(saz,(chip.getSizeX()/2)-(((float)rect.getWidth()*scale)/2), chip.getSizeY()/2, 0, scale);
            imuTextRenderer.end3DRendering();
        }
    }
    @Override public void mouseClicked(MouseEvent e){
        if(isDontProcessMouse()) return; if(!centerOfRotationSelectionPending) return;
        Point p=getMousePixel(e); centerOfRotation=p; log.info("selected COR: "+centerOfRotation);
        putInt("centerOfRotationX", p.x); putInt("centerOfRotationY", p.y); centerOfRotationSelectionPending=false;
    }
    @Override public void mouseMoved(MouseEvent e){
        if(isDontProcessMouse()) return; if(!centerOfRotationSelectionPending) return;
        centerOfRotation=getMousePixel(e);
    }

    @Override public void update(Observable o,Object arg){}

    @Override public void propertyChange(PropertyChangeEvent evt){
        if(evt.getPropertyName()==AEViewer.EVENT_TIMESTAMPS_RESET){ resetFilter();
        }else if(AEInputStream.EVENT_REWOUND.equals(evt.getPropertyName())){ resetFilter();
        }else if(AEViewer.EVENT_FILEOPEN.equals(evt.getPropertyName())){
            AbstractAEPlayer player=chip.getAeViewer().getAePlayer();
            AEFileInputStreamInterface in=(player.getAEInputStream());
            in.getSupport().addPropertyChangeListener(this);
            resetFilter();
        }
    }

    // ---------------- helpers ----------------
    public static class TransformAtTime{
        public final int timestamp; public final Point2D.Float translationPixels; public final float rotationRad; public final float sinAngle; public final float cosAngle;
        public TransformAtTime(int ts, Point2D.Float t, float rot){ timestamp=ts; translationPixels=t; rotationRad=rot; sinAngle=(float)Math.sin(rot); cosAngle=(float)Math.cos(rot); }
    }
    private static class CalibrationFilter{
        int count=0; float sum=0; void reset(){count=0; sum=0;} void addSample(float s){sum+=s; count++;} float computeAverage(){ return count==0?0:sum/count; }
    }
}
