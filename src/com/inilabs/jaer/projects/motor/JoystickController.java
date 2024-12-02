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

public class JoystickController implements JoystickReader.JoystickListener {
    private final DirectGimbalController gimbal;
    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;

    public JoystickController() {
        this.gimbal = DirectGimbalController.getInstance();
    }

    public void start() {
        // Create and start the JoystickReader
        JoystickReader joystickReader = new JoystickReader(this);
        joystickReader.start();
    }

    @Override
    public void onAxisChange(JoystickReader.Axis axis, float value) {
        switch (axis) {
            case YAW -> yaw = value * 180.0f; // Map normalized [-1, 1] to [-180, 180] degrees
            case PITCH -> pitch = value * 90.0f; // Map normalized [-1, 1] to [-90, 90] degrees
            case ROLL -> roll = value * 45.0f; // Map normalized [-1, 1] to [-45, 45] degrees
            default -> System.err.println("Unknown axis: " + axis);
        }
        updateGimbalPose();
    }

    @Override
    public void onButtonPress(JoystickReader.Button button, boolean pressed) {
        if (pressed) {
            switch (button) {
                case BUTTON1 -> System.out.println("Button 1 pressed: Resetting gimbal pose.");
                case BUTTON2 -> System.out.println("Button 2 pressed: Performing custom action.");
                default -> System.err.println("Unknown button: " + button);
            }
        }
    }

    private void updateGimbalPose() {
        gimbal.setGimbalPose(yaw, roll, pitch);
        System.out.printf("Updated Gimbal Pose - Yaw: %.2f°, Roll: %.2f°, Pitch: %.2f°%n", yaw, roll, pitch);
    }
}
