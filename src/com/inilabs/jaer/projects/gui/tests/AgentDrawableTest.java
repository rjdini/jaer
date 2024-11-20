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
package com.inilabs.jaer.projects.gui.tests;

import com.inilabs.jaer.projects.gui.AgentDrawable;
import com.inilabs.jaer.projects.gui.PolarSpaceControlPanel;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AgentDrawableTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AgentDrawableTest::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Initialize the frame and the GUI
        JFrame frame = new JFrame("AgentDrawable Test with PolarSpaceGUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        PolarSpaceDisplay display = new PolarSpaceDisplay();

        // Pass a dummy ActionListener that does nothing, just to satisfy the constructor requirement
        PolarSpaceControlPanel controlPanel = new PolarSpaceControlPanel(display, e -> {});

        // Set default heading and ranges
        display.setHeading(0, 0);
        display.setAzimuthRange(30);
        display.setElevationRange(30);

        // Add PolarSpaceDisplay and Control Panel to the frame
        frame.setLayout(new BorderLayout());
        frame.add(display, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // Create a static AgentDrawable at azimuth 10, elevation -10
        AgentDrawable staticAgent = new AgentDrawable();
        staticAgent.setAzimuth(20);
        staticAgent.setElevation(-20);
        staticAgent.setColor(Color.BLUE);
        display.addDrawable(staticAgent);

        // Create a moving AgentDrawable following a box pattern
        AgentDrawable movingAgent = new AgentDrawable();
        movingAgent.setShowPath(true);  
        movingAgent.setColor(Color.GREEN);

        // Box pattern animation
        Timer timer = new Timer(500, new ActionListener() {
            private int step = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                switch (step % 4) {
                    case 0 -> { // Move right
                        movingAgent.setAzimuth(10);
                        movingAgent.setElevation(10);
                    }
                    case 1 -> { // Move down
                        movingAgent.setAzimuth(10);
                        movingAgent.setElevation(-10);
                    }
                    case 2 -> { // Move left
                        movingAgent.setAzimuth(-10);
                        movingAgent.setElevation(-10);
                    }
                    case 3 -> { // Move up
                        movingAgent.setAzimuth(-10);
                        movingAgent.setElevation(10);
                    }
                }
                step++;
                display.repaint();
            }
        });
        timer.start();

        // Add the moving agent to the display
        display.addDrawable(movingAgent);

        // Make the frame visible
        frame.setVisible(true);
    }
}
