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


import com.inilabs.birdland.gimbal.RS4ControllerV2;
public class Gimbal {
    private Pose currentPose = new Pose(0, 0, 0);
    private RS4ControllerV2 controller = RS4ControllerV2.getInstance();

    public synchronized Pose getPose() {
        // Simulate a delay of 20-50ms
        currentPose = new Pose(controller.getYaw(),controller.getRoll(), controller.getPitch() );
        try {
            Thread.sleep((long) (Math.random() * 30 + 20));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return currentPose;
    }

    public synchronized void setPose(float yaw, float roll, float pitch) {
        controller.setPoseDirect(2*yaw, 2*roll, 2*pitch);
       // currentPose = new Pose(yaw, roll, pitch);
        System.out.printf("Gimbal set to: Yaw=%.2f, Roll=%.2f, Pitch=%.2f%n", yaw, roll, pitch);
    }
}

