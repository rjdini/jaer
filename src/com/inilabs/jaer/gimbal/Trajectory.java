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

import java.util.ArrayList;

/**
 *
 * @author rjd
 */
 public class Trajectory extends ArrayList<TrajectoryPoint> {
        
        String name = "" ;
         long lastTime;
        
        public Trajectory(String name) {
            this.name = name;
        }
        
        void start() {
            start(System.nanoTime());
        }

        void start(long startTime) {
            if (!isEmpty()) {
                super.clear();
            }
            lastTime = startTime;
        }

        void add(float pan, float tilt) {
            if (isEmpty()) {
                start();
            }

            long now = System.nanoTime(); //We want this in nanotime, as the panTilt values do change very fast and millis is often not accurate enough.
            add(new TrajectoryPoint(now - lastTime, pan, tilt));
            lastTime = now;
        }
    }
    
