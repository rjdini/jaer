/*
 * Copyright (C) 2025 rjd.
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
package com.inilabs.jaer.projects.agents.core;

/**
 *
 * @author rjd
 */

import com.inilabs.jaer.projects.agents.api.*;
import com.inilabs.jaer.projects.space3d.Space3D;


public abstract class AbstractAgent
        implements Agent3DInterface, Activatable {

    protected final String key;
    protected Agent3DTypes.ObjectType type;
    protected Space3D.Vec3 posDVX = new Space3D.Vec3(0,0,0);
    protected double yawDeg, pitchDeg, rollDeg;
    protected double latDeg = Double.NaN, lonDeg = Double.NaN, altM = Double.NaN;

    protected volatile boolean enabled = true;

// ctor
protected AbstractAgent(String key, Agent3DTypes.ObjectType type) {
    this.key  = java.util.Objects.requireNonNull(key, "key");
    this.type = java.util.Objects.requireNonNull(type, "type");
}
    
    
    // Agent3DInterface
    @Override public String getKey() { return key; }
    @Override public Space3D.Vec3 getPosition3D() { return posDVX; }
    
    // setPosition3D
    @Override public void setPosition3D(Space3D.Vec3 p) {
        this.posDVX = java.util.Objects.requireNonNull(p, "position");
    }
    @Override public double[] getYawPitchRollDeg(){ return new double[]{yawDeg,pitchDeg,rollDeg}; }
    @Override public void setYawPitchRollDeg(double y,double p,double r){ yawDeg=y; pitchDeg=p; rollDeg=r; }
    @Override public double[] getLLA(){ return new double[]{latDeg,lonDeg,altM}; }
    @Override public void setLLA(double la,double lo,double alt){ latDeg=la; lonDeg=lo; altM=alt; }

    // Activatable
    @Override public boolean isActive() { return enabled; }
    @Override public void setActive(boolean on) { enabled = on; }

    /**
     * @return the type
     */
    public Agent3DTypes.ObjectType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Agent3DTypes.ObjectType type) {
        this.type = type;
    }
}

