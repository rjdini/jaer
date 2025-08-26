/*
 * Copyright (C) 2025 tobid.
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
package com.inilabs.jaer.projects.eventprocessing.filters;

import com.inilabs.jaer.projects.tracker.TrackerManagerV2;
import java.awt.geom.Point2D;
import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.Preferred;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.OutputEventIterator;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.util.EngineeringFormat;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.event.PolarityEvent.Polarity;
import org.slf4j.LoggerFactory;

/**
 * Generates and injects synthetic blobs of events from model of flying object
 * into the event stream.
 *
 * @author tobid
 */
@Description("Generates and injects synthetic blobs of events from model of flying object into the event stream")
@net.sf.jaer.DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class FlyingBlobGenerator extends EventFilter2DMouseAdaptor {

private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FlyingBlobGenerator.class);    
    
    
    private EngineeringFormat eng = new EngineeringFormat();
    @Preferred
    @Description("Mean velocity for flying blobs")
    public float velocityMps = getFloat("velocityMps", 5);

    //@Description("Lens focal length (default 3.7mm for Kowa 3.5mm)")
    @Description("Lens focal length (default 22.5 mm birdland lens)")
    public float lensFocalLengthMm = getFloat("lensFocalLengthMm", 22.5f);  // this lens value is given by the helmut project coverage calculation

    @Description("Blob size in meters")
    public float blobSizeM = getFloat("blobSizeM", .25f);

    @Preferred
    @Description("CoV of speeds of flying blobs")
    public float covSpeed = getFloat("covSpeed", 1);

    @Description("Enable/disable synthetic blob injection")
    public boolean injectEnabled = getBoolean("injectEnabled", true);

    @Description("Blob center as fraction of chip width/height")
    public float centerXFrac = getFloat("centerXFrac", 0.60f);

    @Description("Blob center as fraction of chip width/height")
    public float centerYFrac = getFloat("centerYFrac", 0.60f);

    @Description("Blob radius in pixels")
    public int blobRadiusPx = getInt("blobRadiusPx", 5);

    @Description("Number of synthetic events to inject per packet")
    public int eventsPerPacket = getInt("eventsPerPacket", 200); // computed in initFilter

    @Description(value = "Polarity for injected events: +1=ON, 0=alternate, -1=OFF (if PolarityEvent)")
    public int injectedPolarity = getInt("injectedPolarity", +1);
    public Vector3D blobPosition = new Vector3D(0, 0, 0);
    public Vector3D blobVelocity = new Vector3D(0, 0, 0);
    public Vector2D blob2dPosition = new Vector2D(0, 0);
    public Vector2D blob2dVelocity = new Vector2D(0, 0);
    public float startingDistanceM = Float.NaN;
    public EventPacket outPacket = null;

    public double pathLenM = 0.0;
    public double sAlongM = 0.0;
    public Vector3D uAB = Vector3D.ZERO;
    public long motionStartUs = -1L;

    // ---- 3D motion & projection ----
    @Description("Target diameter in meters")
    public float targetDiameterM = 1.0f; // 1 m diameter

    @Description("Waypoint A distance (m) near RIGHT FOV edge")
    public float waypointADistM = 500f;

    @Description("Waypoint B distance (m) near LEFT FOV edge")
    public float waypointBDistM = 50f;

    @Description("Edge factor (0<k<1): how close to horizontal FOV edge (0.9 => 90%)")
    public float edgeFrac = 0.90f;

    @Description("Flight speed (m/s)")
    public float flightSpeedMps = 10f;

    @Description("Edge margin in pixels to keep target inside the image")
    public int edgeMarginPx = getInt("edgeMarginPx", 8);

    @Description("Synthetic event density (events per pixel^2 of blob area)")
    public float eventDensityPerPx2 = getFloat("eventDensityPerPx2", 0.3f);

    public Vector3D waypointA = Vector3D.ZERO;
    public Vector3D waypointB = Vector3D.ZERO;
    public Vector3D currentPos = Vector3D.ZERO;
    public Vector3D currentVel = Vector3D.ZERO;
    public int motionDir = +1;               // +1: A->B, -1: B->A
    private int lastUpdateTs = Integer.MIN_VALUE;  // μs timestamps

    private final java.util.Random rng = new java.util.Random();

    public FlyingBlobGenerator(AEChip chip) {
        super(chip);
    }

    private void preparePathAB() {
        Vector3D d = getWaypointB().subtract(getWaypointA());
        setPathLenM(d.getNorm());
        setsAlongM(0.0);
        setCurrentPos(getWaypointA());
        setCurrentVel(d.normalize().scalarMultiply(getFlightSpeedMps())); // initial vel A->B
        setMotionDir(+1);
    }

    private void injectBlob(OutputEventIterator outItr, int ts) {
        if (!isInjectEnabled() || getEventsPerPacket() <= 0 || getBlobRadiusPx() <= 0) {
            return;
        }

        final int w = chip.getSizeX();
        final int h = chip.getSizeY();
        final int cx = Math.round(getCenterXFrac() * (w - 1));
        final int cy = Math.round(getCenterYFrac() * (h - 1));

        // Generate points uniformly over a disk
        for (int i = 0; i < getEventsPerPacket(); i++) {
            // polar sampling with sqrt for uniform area
            double theta = 2.0 * Math.PI * rng.nextDouble();
            double r = getBlobRadiusPx() * Math.sqrt(rng.nextDouble());
            int x = cx + (int) Math.round(r * Math.cos(theta));
            int y = cy + (int) Math.round(r * Math.sin(theta));

            if (x < 0 || x >= w || y < 0 || y >= h) {
                continue;
            }

            BasicEvent e = outItr.nextOutput();
            e.x = (short) x;
            e.y = (short) y;
            e.timestamp = ts;

            // If the stream uses polarity, set it
            // (Depending on your jAER version, either use boolean field or setter)
            if (e instanceof PolarityEvent) {
                PolarityEvent pe = (PolarityEvent) e;
                if (getInjectedPolarity() == 0) {
                    // Alternate On/Off for visibility
                    pe.setPolarity(((i & 1) == 0) ? Polarity.On : Polarity.Off);
                } else {
                    pe.setPolarity(getInjectedPolarity() > 0 ? Polarity.On : Polarity.Off);
                }
            }
        }
    }

    @Override
    public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        // Initialize output packet once, but CLEAR it every call
        if (getOutPacket() == null) {
            setOutPacket(new EventPacket(in.getEventClass()));
        }
        getOutPacket().clear();

        OutputEventIterator outItr = getOutPacket().outputIterator();

        // Pass-through: copy input events
        for (BasicEvent ie : in) {
            BasicEvent oe = outItr.nextOutput();
            oe.copyFrom(ie);
        }

        // Inject the synthetic blob at the end of the packet, timestamped to the packet tail
        //       int ts = in.getSize() > 0 ? in.getLastTimestamp() : (int) (System.nanoTime() / 1000_000); // fallback µs-ish
        //       injectBlob(outItr, ts);
        // Inject the synthetic blob at the end of the packet, timestamped to the packet tail
        int ts = in.getSize() > 0 ? in.getLastTimestamp()
                : (int) (System.nanoTime() / 1000); // μs fallback
        stepMotionWall();  // update 3D position based on real time (μs)

        // in filterPacket, just before projecting
        org.apache.commons.math3.geometry.euclidean.threed.Vector3D P = getCurrentPos();
        Point2D.Float px = projectToPixel(P);

        if (isInjectEnabled() && px != null) {
            int rpx = pixelRadiusFromDistance(getCurrentPos().getZ());
            int events = Math.max(12, Math.round((float) (Math.PI * rpx * rpx) * getEventDensityPerPx2()));
            setEventsPerPacket(events); // reuse your existing field if you like

            injectBlobAt(outItr, ts, Math.round(px.x), Math.round(px.y), rpx);
        }

        return getOutPacket();
    }

    private void computeBlobProjection() {
        // computes projection from 3d blob to 2d image
    }

    /**
     * Called in rewind or when user wants to reset filter state.
     *
     */
    @Override
    public void resetFilter() {
   initFilter();
    }

    /**
     * Called after AEChip and this are fully constructed.
     *
     */
    //@Override
    // public void initFilter() {
    //     computeStartingDistance();
    //    computeFoVDeg();
    // }
    @Override
    public void initFilter() {
        // lens & chip geometry
        final float pitchM = getPixelPitchM();           // meters
        final float fM = getFocalLenM();             // meters
        final int w = chip.getSizeX();
        final float uEdgePx = (w / 2f) - getEdgeMarginPx();   // how far from center
        
        // exact horizontal angle that maps to the chosen pixel margin
        final double thetaEdge = Math.atan((uEdgePx * pitchM) / fM);

        // distances: far right (e.g., 500 m), near left (e.g., 50 m)
        final double zRight = getWaypointADistM();
        final double zLeft = getWaypointBDistM();

        computeFoVDeg() ;
        
        // Waypoint centers (Y=0); ensure they project to cx±uEdgePx
        setWaypointA(new Vector3D(Math.tan(+thetaEdge) * zRight, 0, zRight)); // right/far
        setWaypointB(new Vector3D(Math.tan(-thetaEdge) * zLeft, 0, zLeft));  // left/near

        // Precompute path geometry
        Vector3D d = getWaypointB().subtract(getWaypointA());
        setPathLenM(d.getNorm());
        setuAB((getPathLenM() > 0) ? d.scalarMultiply(1.0 / getPathLenM()) : Vector3D.ZERO);

        
        
        // Start at A
        setsAlongM(0.0);
        setCurrentPos(getWaypointA());
        setCurrentVel(getuAB().scalarMultiply(getFlightSpeedMps()));
        setMotionStartUs(-1);     // (re)init wall-clock
        lastUpdateTs = Integer.MIN_VALUE;
    }

    private Vector3D directionFor(Vector3D from, Vector3D to) {
        Vector3D d = to.subtract(from);
        double L = d.getNorm();
        return (L > 0) ? d.scalarMultiply(1.0 / L) : Vector3D.ZERO;
    }

    private float radToDeg(float rad) {
        return (float) Math.toDegrees(rad);
    }

    private void stepMotionWall() {
        final long nowUs = System.nanoTime() / 1000L; // microseconds
        if (getMotionStartUs() < 0) {
            setMotionStartUs(nowUs);
            return;
        }
        if (getPathLenM() <= 1e-9) {
            return;
        }

        final double tSec = (nowUs - getMotionStartUs()) * 1e-6;
        final double L = getPathLenM();
        final double twoL = 2.0 * L;

        // Distance travelled along the infinite line at speed v
        double s = (getFlightSpeedMps() * tSec) % twoL;
        if (s < 0) {
            s += twoL;
        }

        // Reflect into [0, L] => triangle wave
        final int dir; // +1 A->B, -1 B->A
        if (s <= L) {
            setsAlongM(s);
            dir = +1;
        } else {
            setsAlongM(twoL - s);
            dir = -1;
        }

        setCurrentPos(getWaypointA().add(getuAB().scalarMultiply(getsAlongM())));
        setCurrentVel(getuAB().scalarMultiply(getFlightSpeedMps() * dir));
    }

    

    private Point2D.Double computeFoVDeg() {
          // computes the horizontal and vertical field of view in degrees 
    final double f_m   = getLensFocalLengthMm() * 1e-3;                  // focal length [m]
    final double w_px  = chip.getPixelWidthUm()  * 1e-6;                 // pixel pitch X [m]
    final double h_px  = chip.getPixelHeightUm() * 1e-6;                 // pixel pitch Y [m]
    final double W_m   = chip.getSizeX() * w_px;                          // sensor width [m]
    final double H_m   = chip.getSizeY() * h_px;                          // sensor height [m]
    final double fovX  = Math.toDegrees(2.0 * Math.atan(W_m / (2.0 * f_m)));
    final double fovY  = Math.toDegrees(2.0 * Math.atan(H_m / (2.0 * f_m)));
     log.debug("lensFL(m): {} pxPitch(m): {}  pixels:  x: {},  y: {},   FOV: {}  {}", 
                           f_m,  w_px, chip.getSizeX(), chip.getSizeY(), fovX, fovY);
    return new Point2D.Double(fovX, fovY);
}


    private void computeStartingDistance() {
        // compute starting distance such that blobs are 1 pixel in size
        float pxSizeM = chip.getPixelWidthUm() * 1e-6f;
        float pxAngRad = pxSizeM / (getLensFocalLengthMm() * 1e-3f); // approx tan
        // when blob size/distance =pxAngRad the blob will be one pizel.
        // therefore distance=blob size/pxAngRad
        setStartingDistanceM(getBlobSizeM() / pxAngRad);
        log.info(String.format("Pixels subtend %s deg and blob starting distance is %sm",
                eng.format(radToDeg(pxAngRad)),
                eng.format(getStartingDistanceM()))
        );
    }

    // --- Projection helpers (pinhole) ---
    private float getPixelPitchM() {
        return chip.getPixelWidthUm() * 1e-6f;
    }

    private float getFocalLenM() {
        return getLensFocalLengthMm() * 1e-3f;
    }

    // drop this next to the existing projectToPixel(Vector3D)
    private Point2D.Float projectToPixel(double x, double y, double z) {
        return projectToPixel(new org.apache.commons.math3.geometry.euclidean.threed.Vector3D(x, y, z));
    }

    /**
     * Project a 3D point (meters; camera at origin, Z forward) to pixel coords.
     * Returns null if behind camera or outside chip.
     */
    private Point2D.Float projectToPixel(Vector3D P) {
        final float f = getFocalLenM();
        if (P.getZ() <= 0) {
            return null; // behind camera
        }
        final double u_m = f * (P.getX() / P.getZ()); // meters on sensor
        final double v_m = f * (P.getY() / P.getZ());
        final double pitch = getPixelPitchM();
        final double u_px = u_m / pitch;
        final double v_px = v_m / pitch;

        final int w = chip.getSizeX(), h = chip.getSizeY();
        final float cx = (w - 1) / 2f, cy = (h - 1) / 2f;
        final float x = (float) (cx + u_px);
        final float y = (float) (cy - v_px); // image y down

        if (x < 0 || x >= w || y < 0 || y >= h) {
            return null;
        }
        return new Point2D.Float(x, y);
    }

    /**
     * Pixel radius from distance using small-angle pinhole geometry.
     */
    private int pixelRadiusFromDistance(double distM) {
        if (distM <= 0) {
            return 1;
        }
        final double f = getFocalLenM();
        final double pitch = getPixelPitchM();
        final double r_px = (f * (getTargetDiameterM() / 2.0)) / (distM * pitch);
        return Math.max(1, (int) Math.round(r_px));
    }

    private void injectBlobAt(OutputEventIterator outItr, int ts, int cx, int cy, int radiusPx) {
        if (!isInjectEnabled() || getEventsPerPacket() <= 0 || radiusPx <= 0) {
            return;
        }

        final int w = chip.getSizeX(), h = chip.getSizeY();
        final int r = radiusPx;

        for (int i = 0; i < getEventsPerPacket(); i++) {
            double theta = 2.0 * Math.PI * rng.nextDouble();
            double rr = r * Math.sqrt(rng.nextDouble());
            int x = cx + (int) Math.round(rr * Math.cos(theta));
            int y = cy + (int) Math.round(rr * Math.sin(theta));

            if (x < 0 || x >= w || y < 0 || y >= h) {
                continue;
            }

            BasicEvent e = outItr.nextOutput();
            e.x = (short) x;
            e.y = (short) y;
            e.timestamp = ts;

            if (e instanceof PolarityEvent) {
                PolarityEvent pe = (PolarityEvent) e;
                if (getInjectedPolarity() == 0) {
                    pe.setPolarity(((i & 1) == 0) ? Polarity.On : Polarity.Off);
                } else {
                    pe.setPolarity(getInjectedPolarity() > 0 ? Polarity.On : Polarity.Off);
                }
            }
        }
    }

    /**
     * @return the velocityMps
     */
    public float getVelocityMps() {
        return velocityMps;
    }

    /**
     * @param velocityMps the velocityMps to set
     */
    public void setVelocityMps(float velocityMps) {
        this.velocityMps = velocityMps;
        putFloat("velocityMps", velocityMps);
    }

    /**
     * @return the lensFocalLengthMm
     */
    public float getLensFocalLengthMm() {
        return lensFocalLengthMm;
    }

    /**
     * @param lensFocalLengthMm the lensFocalLengthMm to set
     */
    public void setLensFocalLengthMm(float lensFocalLengthMm) {
        this.lensFocalLengthMm = lensFocalLengthMm;
        putFloat("lensFocalLengthMm", lensFocalLengthMm);
        computeStartingDistance();
        computeFoVDeg();
    }

    /**
     * @return the blobSizeM
     */
    public float getBlobSizeM() {
        return blobSizeM;
    }

    /**
     * @param blobSizeM the blobSizeM to set
     */
    public void setBlobSizeM(float blobSizeM) {
        this.blobSizeM = blobSizeM;
        putFloat("blobSizeM", blobSizeM);
        computeStartingDistance();
    }

    /**
     * @return the covSpeed
     */
    public float getCovSpeed() {
        return covSpeed;
    }

    /**
     * @param covSpeed the covSpeed to set
     */
    public void setCovSpeed(float covSpeed) {
        this.covSpeed = covSpeed;
        putFloat("covSpeed", covSpeed);
    }

    /**
     * @return the injectEnabled
     */
    public boolean isInjectEnabled() {
        return injectEnabled;
    }

    /**
     * @param injectEnabled the injectEnabled to set
     */
    public void setInjectEnabled(boolean injectEnabled) {
        this.injectEnabled = injectEnabled;
    }

    /**
     * @return the centerXFrac
     */
    public float getCenterXFrac() {
        return centerXFrac;
    }

    /**
     * @param centerXFrac the centerXFrac to set
     */
    public void setCenterXFrac(float centerXFrac) {
        this.centerXFrac = centerXFrac;
    }

    /**
     * @return the centerYFrac
     */
    public float getCenterYFrac() {
        return centerYFrac;
    }

    /**
     * @param centerYFrac the centerYFrac to set
     */
    public void setCenterYFrac(float centerYFrac) {
        this.centerYFrac = centerYFrac;
    }

    /**
     * @return the blobRadiusPx
     */
    public int getBlobRadiusPx() {
        return blobRadiusPx;
    }

    /**
     * @param blobRadiusPx the blobRadiusPx to set
     */
    public void setBlobRadiusPx(int blobRadiusPx) {
        this.blobRadiusPx = blobRadiusPx;
    }

    /**
     * @return the eventsPerPacket
     */
    public int getEventsPerPacket() {
        return eventsPerPacket;
    }

    /**
     * @param eventsPerPacket the eventsPerPacket to set
     */
    public void setEventsPerPacket(int eventsPerPacket) {
        this.eventsPerPacket = eventsPerPacket;
    }

    /**
     * @return the injectedPolarity
     */
    public int getInjectedPolarity() {
        return injectedPolarity;
    }

    /**
     * @param injectedPolarity the injectedPolarity to set
     */
    public void setInjectedPolarity(int injectedPolarity) {
        this.injectedPolarity = injectedPolarity;
    }

    /**
     * @return the blobPosition
     */
    public Vector3D getBlobPosition() {
        return blobPosition;
    }

    /**
     * @param blobPosition the blobPosition to set
     */
    public void setBlobPosition(Vector3D blobPosition) {
        this.blobPosition = blobPosition;
    }

    /**
     * @return the blobVelocity
     */
    public Vector3D getBlobVelocity() {
        return blobVelocity;
    }

    /**
     * @param blobVelocity the blobVelocity to set
     */
    public void setBlobVelocity(Vector3D blobVelocity) {
        this.blobVelocity = blobVelocity;
    }

    /**
     * @return the blob2dPosition
     */
    public Vector2D getBlob2dPosition() {
        return blob2dPosition;
    }

    /**
     * @param blob2dPosition the blob2dPosition to set
     */
    public void setBlob2dPosition(Vector2D blob2dPosition) {
        this.blob2dPosition = blob2dPosition;
    }

    /**
     * @return the blob2dVelocity
     */
    public Vector2D getBlob2dVelocity() {
        return blob2dVelocity;
    }

    /**
     * @param blob2dVelocity the blob2dVelocity to set
     */
    public void setBlob2dVelocity(Vector2D blob2dVelocity) {
        this.blob2dVelocity = blob2dVelocity;
    }

    /**
     * @return the startingDistanceM
     */
    public float getStartingDistanceM() {
        return startingDistanceM;
    }

    /**
     * @param startingDistanceM the startingDistanceM to set
     */
    public void setStartingDistanceM(float startingDistanceM) {
        this.startingDistanceM = startingDistanceM;
    }

    /**
     * @return the targetDiameterM
     */
    public float getTargetDiameterM() {
        return targetDiameterM;
    }

    /**
     * @param targetDiameterM the targetDiameterM to set
     */
    public void setTargetDiameterM(float targetDiameterM) {
        this.targetDiameterM = targetDiameterM;
    }

    /**
     * @return the waypointADistM
     */
    public float getWaypointADistM() {
        return waypointADistM;
    }

    /**
     * @param waypointADistM the waypointADistM to set
     */
    public void setWaypointADistM(float waypointADistM) {
        this.waypointADistM = waypointADistM;
    }

    /**
     * @return the waypointBDistM
     */
    public float getWaypointBDistM() {
        return waypointBDistM;
    }

    /**
     * @param waypointBDistM the waypointBDistM to set
     */
    public void setWaypointBDistM(float waypointBDistM) {
        this.waypointBDistM = waypointBDistM;
    }

    /**
     * @return the flightSpeedMps
     */
    public float getFlightSpeedMps() {
        return flightSpeedMps;
    }

    /**
     * @param flightSpeedMps the flightSpeedMps to set
     */
    public void setFlightSpeedMps(float flightSpeedMps) {
        this.flightSpeedMps = flightSpeedMps;
    }

    /**
     * @return the waypointA
     */
    public Vector3D getWaypointA() {
        return waypointA;
    }

    /**
     * @param waypointA the waypointA to set
     */
    public void setWaypointA(Vector3D waypointA) {
        this.waypointA = waypointA;
    }

    /**
     * @return the waypointB
     */
    public Vector3D getWaypointB() {
        return waypointB;
    }

    /**
     * @param waypointB the waypointB to set
     */
    public void setWaypointB(Vector3D waypointB) {
        this.waypointB = waypointB;
    }

    /**
     * @return the currentPos
     */
    public Vector3D getCurrentPos() {
        return currentPos;
    }

    /**
     * @param currentPos the currentPos to set
     */
    public void setCurrentPos(Vector3D currentPos) {
        this.currentPos = currentPos;
    }

    /**
     * @return the currentVel
     */
    public Vector3D getCurrentVel() {
        return currentVel;
    }

    /**
     * @param currentVel the currentVel to set
     */
    public void setCurrentVel(Vector3D currentVel) {
        this.currentVel = currentVel;
    }

    /**
     * @return the motionDir
     */
    public int getMotionDir() {
        return motionDir;
    }

    /**
     * @param motionDir the motionDir to set
     */
    public void setMotionDir(int motionDir) {
        this.motionDir = motionDir;
    }

    /**
     * @return the outPacket
     */
    public EventPacket getOutPacket() {
        return outPacket;
    }

    /**
     * @param outPacket the outPacket to set
     */
    public void setOutPacket(EventPacket outPacket) {
        this.outPacket = outPacket;
    }

    /**
     * @return the pathLenM
     */
    public double getPathLenM() {
        return pathLenM;
    }

    /**
     * @param pathLenM the pathLenM to set
     */
    public void setPathLenM(double pathLenM) {
        this.pathLenM = pathLenM;
    }

    /**
     * @return the sAlongM
     */
    public double getsAlongM() {
        return sAlongM;
    }

    /**
     * @param sAlongM the sAlongM to set
     */
    public void setsAlongM(double sAlongM) {
        this.sAlongM = sAlongM;
    }

    /**
     * @return the uAB
     */
    public Vector3D getuAB() {
        return uAB;
    }

    /**
     * @param uAB the uAB to set
     */
    public void setuAB(Vector3D uAB) {
        this.uAB = uAB;
    }

    /**
     * @return the motionStartUs
     */
    public long getMotionStartUs() {
        return motionStartUs;
    }

    /**
     * @param motionStartUs the motionStartUs to set
     */
    public void setMotionStartUs(long motionStartUs) {
        this.motionStartUs = motionStartUs;
    }

    /**
     * @return the edgeFrac
     */
    public float getEdgeFrac() {
        return edgeFrac;
    }

    /**
     * @param edgeFrac the edgeFrac to set
     */
    public void setEdgeFrac(float edgeFrac) {
        this.edgeFrac = edgeFrac;
    }

    /**
     * @return the edgeMarginPx
     */
    public int getEdgeMarginPx() {
        return edgeMarginPx;
    }

    /**
     * @param edgeMarginPx the edgeMarginPx to set
     */
    public void setEdgeMarginPx(int edgeMarginPx) {
        this.edgeMarginPx = edgeMarginPx;
    }

    /**
     * @return the eventDensityPerPx2
     */
    public float getEventDensityPerPx2() {
        return eventDensityPerPx2;
    }

    /**
     * @param eventDensityPerPx2 the eventDensityPerPx2 to set
     */
    public void setEventDensityPerPx2(float eventDensityPerPx2) {
        this.eventDensityPerPx2 = eventDensityPerPx2;
    }

}
