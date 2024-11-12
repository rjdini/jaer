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
package com.inilabs.jaer.gimbal;

/**
 *
 * @author rjd
 */

      /**
     * Represents a point in polar space.
     * pan/ tilt refer locations in the FOV subspace.
     * pan, tilt are in range [0, 1] left to right, and bottom to top of FOV ( equivalently chip surface).
     * The pan tilt fovea (center of FOV, is at (0.5, 0.5).
     * (pan, tilt, would be better called  fovx, fovy, with respect to fovea. at (0, 0), but this is TODO.
     * 
     * yaw, pitch refer to gimbal foveal axis in 3D space.  We assume roll = 0, for the moment.
     * ****  This trajectory point refers only to trajectories on the FOV.
     * 
     */
    

    public class TrajectoryPoint {

        private final long timeNanos;
        private final float pan, tilt;
        private FieldOfView fov = FieldOfView.getInstance();
        
        public TrajectoryPoint(long timeNanos, float pan, float tilt) {
            this.timeNanos = timeNanos;
            this.pan = pan;
            this.tilt = tilt;
        }

        public long getTime() {
            return timeNanos;
        }

        public float getPan() {
            return pan;
        }

        public float getTilt() {
            return tilt;
        }
       
        public float getYaw() {
        return fov.getYawAtPan( pan);
    }
        
         public float getPitch() {
        return fov.getPitchAtTilt(tilt);
    }
              
    }

    