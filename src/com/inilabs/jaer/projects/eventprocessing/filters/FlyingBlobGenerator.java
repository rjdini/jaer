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

/**
 * Generates and injects synthetic blobs of events from model of flying object
 * into the event stream.
 *
 * @author tobid
 */
@Description("Generates and injects synthetic blobs of events from model of flying object into the event stream")
@net.sf.jaer.DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class FlyingBlobGenerator extends EventFilter2DMouseAdaptor {

    private EngineeringFormat eng = new EngineeringFormat();
    @Preferred
    @Description("Mean velocity for flying blobs")
    private float velocityMps = getFloat("velocityMps", 5);

    //@Description("Lens focal length (default 3.7mm for Kowa 3.5mm)")
    public float lensFocalLengthMm = getFloat("lensFocalLengthMm", 3.7f);

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
    private EventPacket outPacket = null;

    public FlyingBlobGenerator(AEChip chip) {
        super(chip);
    }

    private final java.util.Random rng = new java.util.Random();

    private void injectBlob(OutputEventIterator outItr, int ts) {
        if (!injectEnabled || eventsPerPacket <= 0 || blobRadiusPx <= 0) {
            return;
        }

        final int w = chip.getSizeX();
        final int h = chip.getSizeY();
        final int cx = Math.round(centerXFrac * (w - 1));
        final int cy = Math.round(centerYFrac * (h - 1));

        // Generate points uniformly over a disk
        for (int i = 0; i < eventsPerPacket; i++) {
            // polar sampling with sqrt for uniform area
            double theta = 2.0 * Math.PI * rng.nextDouble();
            double r = blobRadiusPx * Math.sqrt(rng.nextDouble());
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
                if (injectedPolarity == 0) {
                    // Alternate On/Off for visibility
                    pe.setPolarity(((i & 1) == 0) ? Polarity.On : Polarity.Off);
                } else {
                    pe.setPolarity(injectedPolarity > 0 ? Polarity.On : Polarity.Off);
                }
            }
        }
    }

    @Override
    public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        // Initialize output packet once, but CLEAR it every call
        if (outPacket == null) {
            outPacket = new EventPacket(in.getEventClass());
        }
        outPacket.clear();

        OutputEventIterator outItr = outPacket.outputIterator();

        // Pass-through: copy input events
        for (BasicEvent ie : in) {
            BasicEvent oe = outItr.nextOutput();
            oe.copyFrom(ie);
        }

        // Inject the synthetic blob at the end of the packet, timestamped to the packet tail
        int ts = in.getSize() > 0 ? in.getLastTimestamp() : (int) (System.nanoTime() / 1000_000); // fallback µs-ish
        injectBlob(outItr, ts);

        return outPacket;
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

    }

    /**
     * Called after AEChip and this are fully constructed.
     *
     */
    @Override
    public void initFilter() {
        computeStartingDistance();
        computeFoVDeg();
    }

    private float radToDeg(float rad) {
        return 180f * rad;
    }

    private Point2D.Float computeFoVDeg() {
        // computes the horizontal and vertical field of view in degrees
        int nx = chip.getSizeX(), ny = chip.getSizeY();
        float pxSizeM = chip.getPixelWidthUm() * 1e-6f;
        float pxAngRad = pxSizeM / (getLensFocalLengthMm() * 1e-3f); // approx tan
        float fovX = radToDeg(2 * (float) Math.atan(nx * pxAngRad / 2));
        float fovY = radToDeg(2 * (float) Math.atan(ny * pxAngRad / 2));
        return new Point2D.Float(fovX, fovY);
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

}
