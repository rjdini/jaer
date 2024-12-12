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
package com.inilabs.jaer.projects.motor;

import com.inilabs.jaer.projects.tracker.TrackerAgentDrawable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author rjd
 * 
 * 
 *  #############  NOT FUNCTIONAL ### TODO = test and amke available as alternative to Joystick
 */
public class KeyboardController  implements KeyListener {
    
private final Set<Integer> keyDown = new HashSet<>(); // Tracks currently pressed keys
 private float azimuth = 0; // Current azimuth for manual control
    private float roll = 0; // Current roll for manual control
    private float elevation = 0; // Current elevation for manual control
    private boolean enableKeyboardControl = false;
    private final float stepSize = 1.0f; // Incremental step size for azimuth/elevation
 
    public boolean isEnableKeyboardControl() {
        return enableKeyboardControl;
    }

    public void setEnableKeyboardControl(boolean yes) {
        enableKeyboardControl = yes;
    }

    private void updateAzimuthAndElevation() {
        if (keyDown.contains(KeyEvent.VK_6)) {
            setAzimuth(getAzimuth() - stepSize);
        }
        if (keyDown.contains(KeyEvent.VK_7)) {
            setAzimuth(getAzimuth() + stepSize);
        }
        if (keyDown.contains(KeyEvent.VK_8)) {
            setElevation(getElevation() + stepSize);
        }
        if (keyDown.contains(KeyEvent.VK_9)) {
            setElevation(getElevation() - stepSize);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isEnableKeyboardControl()) {
            keyDown.add(e.getKeyCode()); // Track pressed keys
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isEnableKeyboardControl()) {
            keyDown.remove(e.getKeyCode()); // Remove released keys
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No implementation needed for keyTyped
    }

    /**
     * @return the azimuth
     */
    public float getAzimuth() {
        return azimuth;
    }

    /**
     * @param azimuth the azimuth to set
     */
    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * @return the roll
     */
    public float getRoll() {
        return roll;
    }

    /**
     * @param roll the roll to set
     */
    public void setRoll(float roll) {
        this.roll = roll;
    }

    /**
     * @return the elevation
     */
    public float getElevation() {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    
    
}
