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

public class Pose {
    public float yaw;
    public float roll;
    public float pitch;

    public Pose() {
        this.yaw = 0.0f;
        this.roll = 0.0f;
        this.pitch = 0.0f;
    }
    
    
    public Pose(float yaw, float roll, float pitch) {
        this.yaw = yaw;
        this.roll = roll;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

    public float getPitch() {
        return pitch;
    }

    /**
     * @param yaw the yaw to set
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * @param roll the roll to set
     */
    public void setRoll(float roll) {
        this.roll = roll;
    }

    /**
     * @param pitch the pitch to set
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}

