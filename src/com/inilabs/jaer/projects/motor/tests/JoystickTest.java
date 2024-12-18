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
package com.inilabs.jaer.projects.motor.tests;

import com.inilabs.jaer.projects.motor.DirectGimbalController;
import com.inilabs.jaer.projects.motor.JoystickController;
import com.inilabs.jaer.projects.tracker.FieldOfView;

public class JoystickTest {
    public static void main(String[] args) {
        
        FieldOfView fov = FieldOfView.getInstance();
        DirectGimbalController gimbal = DirectGimbalController.getInstance(fov); // Assuming Gimbal is implemented
        JoystickController controller = JoystickController.getInstance(gimbal);

        System.out.println("Starting Joystick Controller...");
        controller.start();

        // Keep the program running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}