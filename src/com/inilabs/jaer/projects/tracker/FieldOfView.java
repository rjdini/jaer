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

import com.inilabs.jaer.projects.polarspace.ActionType;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.inilabs.jaer.projects.polarspace.Drawable;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.EventType;
import com.inilabs.jaer.projects.motor.Pose;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

/**
 * FieldOfView represents the DVX camera FOV (pose + chip/lens params) drawn in Polar space.
 * 
 * This class keeps backward compatibility with the existing singleton via getInstance(),
 * while also allowing a scoped-per-tracker singleton via get(trackerId).
 */
public class FieldOfView implements Drawable, PropertyChangeListener {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final ch.qos.logback.classic.Logger log =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(FieldOfView.class);

    // ====== FOV & chip parameters ======
    private float focalLength = 100f;
    private float chipWidthPixels = 640f;
    private float chipHeightPixels = 480f;
    private float lensOffsetWidthPixels = 0f;
    private float lensOffsetHeightPixels = 0f;
    private float centerChipX = chipWidthPixels / 2f - lensOffsetWidthPixels;
    private float centerChipY = (chipHeightPixels / 2f) - lensOffsetHeightPixels;
    private float FOVX = 30.0f;      // small lens ~ 30deg
    private float FOVY = FOVX * (getChipHeightPixels() / getChipWidthPixels());

    // Orientation (yaw, pitch, roll) in degrees
    private float axialYaw = 0f;
    private float axialPitch = 0f;
    private float axialRoll = 0f;

    // For logging / debugging
    private final List<EventCluster> clusters = new ArrayList<>();

    // Drawable identity & style
    private String key;
    private int id;
    private boolean showPath = false;
    private float azimuth = 0.0f;
    private float elevation = 0.0f;
    private float size = 1.0f;
    private Color color = Color.BLACK;
    private BiConsumer<ActionType, String> parentCallback;

    // Path trace in polar view
    protected final LinkedList<float[]> pathBuffer = new LinkedList<>();
    protected final int maxPathLength = 40;

    // PolarSpace transform (injected by parent display)
    private int centerX = 0;
    private int centerY = 0;
    private float azimuthScale = 1.0f;
    private float elevationScale = 1.0f;
    private float azimuthHeading = 0f;
    private float elevationHeading = 0f;

    // Lifecycle timestamps
    private long startTime; // agent created
    private long lastTime;  // agent closed
    protected long lifetime0;        // temp value used for extending lifetime.
    protected long maxLifeTime = 100; // millisec
    protected boolean isOrphaned = false;
    protected boolean isExpired = false;

    // ====== Singleton support ======
    // Legacy global singleton
    private static FieldOfView instance;

    // Scoped instances per tracker (allows one FOV per DVX without global collisions)
    private static final ConcurrentHashMap<String, FieldOfView> INSTANCES = new ConcurrentHashMap<>();

    /** Returns the FieldOfView instance associated with the given trackerId. */
    public static FieldOfView get(String trackerId) {
        if (trackerId == null || trackerId.isEmpty()) {
            trackerId = "DEFAULT";
        }
        final String tid = trackerId;
        return INSTANCES.computeIfAbsent(tid, id -> {
            FieldOfView f = new FieldOfView();
            f.key = "fov_" + id;
            return f;
        });
    }

    /** Legacy global singleton accessor; also seeds the DEFAULT scoped instance. */
    public static FieldOfView getInstance() {
        if (instance == null) {
            instance = new FieldOfView();
            instance.key = "fov_instance";
            // Seed DEFAULT scoped instance for backward compatibility
            INSTANCES.putIfAbsent("DEFAULT", instance);
        }
        return instance;
    }

    // ====== Construction ======
    private FieldOfView() {
        this.setColor(Color.RED);
        this.setSize(FOVX);
        init();
    }

    private void init() {
        AgentLogger.logAgentEvent(EventType.CREATE, getKey(), getAzimuth(), getElevation(), getColor(), getClusterKeys());
        startTime = System.currentTimeMillis();
    }

    public void close() {
        lastTime = System.currentTimeMillis();
        AgentLogger.logAgentEvent(EventType.CLOSE, getKey(), getAzimuth(), getElevation(), getColor(), getClusterKeys());
    }

    // ====== PropertyChangeListener ======
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("FetchedGimbalPose".equals(evt.getPropertyName())) {
            Pose newFOVPose = (Pose) evt.getNewValue();
            setPose(newFOVPose.getYaw(), newFOVPose.getRoll(), newFOVPose.getPitch());
            log.debug("Received evt FetchedGimbalPose  azi {}, ele {}", newFOVPose.getYaw(), newFOVPose.getPitch());
        }
    }

    // Helper method to get cluster keys as a list of strings (for logging)
    public List<String> getClusterKeys() {
        return clusters.stream().map(EventCluster::getKey).collect(Collectors.toList());
    }

    // ====== DrawableListener (transform updates from display) ======
    @Override
    public void onTransformChanged(float azimuthScale, float elevationScale, float azimuthHeading, float elevationHeading, int centerX, int centerY) {
        this.azimuthScale = azimuthScale;
        this.elevationScale = elevationScale;
        this.azimuthHeading = azimuthHeading;
        this.elevationHeading = elevationHeading;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    // ====== Pose management ======
    public void setPose(float yaw, float roll, float pitch) {
        setAxialYaw(yaw);
        setAxialRoll(roll);
        setAxialPitch(pitch);
        AgentLogger.logAgentEvent(EventType.MOVE, getKey(), getAzimuth(), getElevation(), getColor(), getClusterKeys());
    }

    public float[] getPose() {
        return new float[]{axialYaw, axialRoll, axialPitch};
    }

    // yaw, roll, pitch refer to the gimbal's behavioral frame.
    // azimuth and elevation refer to the FOV axis in polar space.
    public void setAxialYaw(float axialYaw) {
        this.axialYaw = axialYaw;
        setAzimuth(axialYaw);
    }

    public float getAxialYaw() {
        return axialYaw;
    }

    public void setAxialPitch(float axialPitch) {
        this.axialPitch = axialPitch;
        setElevation(axialPitch);
    }

    public float getAxialPitch() {
        return axialPitch;
    }

    public void setAxialRoll(float axialRoll) {
        this.axialRoll = axialRoll;
    }

    public float getAxialRoll() {
        return axialRoll;
    }

    @Override
    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
        this.axialYaw = azimuth;
        addCurrentPositionToPath();
    }

    @Override
    public void setElevation(float elevation) {
        this.elevation = elevation;
        this.axialPitch = elevation;
        addCurrentPositionToPath();
    }

    @Override
    public float getAzimuth() {
        return azimuth;
    }

    @Override
    public float getElevation() {
        return elevation;
    }

    // ====== Rendering ======
    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Calculate screen coordinates based on polar coordinates and the transform broadcast
        int x = getCenterX() + (int) ((getAzimuth() - getAzimuthHeading()) * getAzimuthScale());
        int y = getCenterY() - (int) ((getElevation() - getElevationHeading()) * getElevationScale());

        // Calculate box dimensions based on FOV and scales
        int boxWidth = (int) (getFOVX() * getAzimuthScale());
        int boxHeight = (int) (getFOVY() * getElevationScale());

        // Apply roll rotation and draw the FOV box
        AffineTransform originalTransform = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(axialRoll));
        g2d.setColor(getColor());
        g2d.drawRect(-boxWidth / 2, -boxHeight / 2, boxWidth, boxHeight);
        g2d.setTransform(originalTransform);

        // Draw the path if enabled
        if (showPath) {
            drawPath(g2d);
        }
    }

    protected void drawPath(Graphics2D g2d) {
        g2d.setColor(color);
        float[] previousPosition = null;

        for (float[] position : pathBuffer) {
            int pathX = getCenterX() + (int) ((position[0] - getAzimuthHeading()) * getAzimuthScale());
            int pathY = getCenterY() - (int) ((position[1] - getElevationHeading()) * getElevationScale());

            if (previousPosition != null) {
                int prevX = getCenterX() + (int) ((previousPosition[0] - getAzimuthHeading()) * getAzimuthScale());
                int prevY = getCenterY() - (int) ((previousPosition[1] - getElevationHeading()) * getElevationScale());
                g2d.drawLine(prevX, prevY, pathX, pathY);
            }
            previousPosition = position;
        }
    }

    // ====== Legacy pan/tilt helpers (normalized 0..1) ======
    public float getYawAtPan(float pan) {
        return axialYaw + (pan - 0.5f) * getFOVX();
    }

    public float getPitchAtTilt(float tilt) {
        return axialPitch + (tilt - 0.5f) * getFOVY();
    }

    // Chip-pixel helpers at absolute yaw/pitch
    public float getPixelsAtYaw(float yaw) {
        float deltaYaw = yaw - getPose()[0];
        return getCenterChipX() + (deltaYaw / getFOVX()) * getChipWidthPixels();
    }

    public float getPixelsAtPitch(float pitch) {
        float deltaPitch = pitch - getPose()[2];
        return getCenterChipY() + (deltaPitch / getFOVY()) * getChipHeightPixels();
    }

    public float getPixelsAtPan(float pan) {
        return getPixelsAtYaw(getYawAtPan(pan));
    }

    public float getPixelsAtTilt(float tilt) {
        return getPixelsAtPitch(getPitchAtTilt(tilt));
    }

    public float getPanAtYaw(float yaw) {
        return 0.5f + (yaw - axialYaw) / getFOVX();
    }

    public float getTiltAtPitch(float pitch) {
        return 0.5f + (pitch - axialPitch) / getFOVY();
    }

    /** Converts pixel X to yaw (degrees), relative to current pose center. */
    public float getYawAtPixel(float pixelX) {
        float deltaYaw = -(getFOVX() / 2f) + ((pixelX / getChipWidthPixels()) * getFOVX());
        return getPose()[0] + deltaYaw;
    }

    /** Converts pixel Y to pitch (degrees), relative to current pose center. */
    public float getPitchAtPixel(float pixelY) {
        float deltaPitch = -(getFOVY() / 2f) + ((pixelY / getChipHeightPixels()) * getFOVY());
        return getPose()[2] + deltaPitch;
    }

    // ====== Chip/lens setters & getters ======
    public void setChipDimensions(float width, float height) {
        this.setChipWidthPixels(width);
        this.setChipHeightPixels(height);
    }

    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
    }

    public float getFOVX() { return FOVX; }
    public void setFOVX(float FOVX) { this.FOVX = FOVX; }

    public float getFOVY() { return FOVY; }
    public void setFOVY(float FOVY) { this.FOVY = FOVY; }

    public float getFocalLength() { return focalLength; }

    public float getChipWidthPixels() { return chipWidthPixels; }
    public void setChipWidthPixels(float chipWidthPixels) { this.chipWidthPixels = chipWidthPixels; }

    public float getChipHeightPixels() { return chipHeightPixels; }
    public void setChipHeightPixels(float chipHeightPixels) { this.chipHeightPixels = chipHeightPixels; }

    public float getCenterChipX() { return centerChipX; }
    public void setCenterChipX(float centerChipX) { this.centerChipX = centerChipX; }

    public float getCenterChipY() { return centerChipY; }
    public void setCenterChipY(float centerChipY) { this.centerChipY = centerChipY; }

    // ====== Drawable API ======
    @Override
    public String getKey() { return this.key; }

    @Override
    public int getId() { return this.id; }

    @Override
    public void showPath(boolean yes) { this.setShowPath(yes); }

    @Override
    public void setSize(float sizeDegrees) { this.size = sizeDegrees; }

    @Override
    public float getSize() { return size; }

    @Override
    public void setColor(Color color) { this.color = color; }

    @Override
    public Color getColor() { return color; }

    @Override
    public void setParentCallback(BiConsumer<ActionType, String> parentCallback) {
        this.parentCallback = parentCallback;
    }

    @Override
    public boolean isExpired() { return false; } // Dummy implementation
    
    public boolean isPathVisible() {
        return showPath;
    }

    @Override
    public boolean isOrphaned() { return false; } // Dummy implementation

    public long getLifetime() { return 10000L; } // dummy to conform to Drawable

    protected void addCurrentPositionToPath() {
        if (pathBuffer.size() >= maxPathLength) {
            pathBuffer.removeFirst();
        }
        pathBuffer.addLast(new float[]{getAzimuth(), getElevation()});
    }

    public boolean isShowPath() { return showPath; }
    public void setShowPath(boolean showPath) { this.showPath = showPath; }

    public int getCenterX() { return centerX; }
    public int getCenterY() { return centerY; }
    public float getAzimuthScale() { return azimuthScale; }
    public float getElevationScale() { return elevationScale; }
    public float getAzimuthHeading() { return azimuthHeading; }
    public float getElevationHeading() { return elevationHeading; }
    public long getStartTime() { return startTime; }
    public long getLastTime() { return lastTime; }

    // ====== Placeholder class for cluster logging ======
    // (Assumes EventCluster exists elsewhere in your project; if not, replace with your real type.)
    public static class EventCluster {
        public String getKey() { return "cluster"; }
    }
}
