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
import com.inilabs.jaer.projects.motor.Pose;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class StepInputTest {
    public static void main(String[] args) {
        // Create Gimbal and Controller
 //       Gimbal gimbal = new Gimbal();
  //      GimbalController controller = new GimbalController(gimbal, 0.4f, 0.0f, 0.000f);

  DirectGimbalController gimbalController = new DirectGimbalController();
  
        // Create GUI for visualization
        JFrame frame = new JFrame("Step Input Test");
        GraphPanel graphPanel = new GraphPanel(15); // Fixed 15-second cycle
        frame.add(graphPanel);
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Simulate step input and measure response
        Timer timer = new Timer();
        long startTime = System.currentTimeMillis();

        timer.scheduleAtFixedRate(new TimerTask() {
            private float targetYaw = 0.0f;

            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;

                // Generate the 15-second step sequence
                long currentSecond = (elapsedTime / 1000) % 15;
                if (currentSecond < 5) {
                    targetYaw = 0.0f; // 0° for first 5 seconds
                } else if (currentSecond < 10) {
                    targetYaw = 20.0f; // 10° for next 5 seconds
                } else {
                    targetYaw = 0.0f; // Back to 0° for final 5 seconds
                }

                // Apply step input to the PID controller
                gimbalController.setGimbalPose(targetYaw, 0.0f, 0.0f);

                // Get the gimbal's response
                 Pose responsePose = gimbalController.getGimbalPose();

                // Pass constant input and response to the graph
                float currentTime = currentSecond + (elapsedTime % 1000) / 1000.0f;
                graphPanel.addPoint(currentTime, targetYaw, responsePose.getYaw());
            }
        }, 0, 100); // Update every 100 ms
    }
}
