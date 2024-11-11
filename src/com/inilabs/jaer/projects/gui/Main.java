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

/**
 *
 * @author rjd
 */

package com.inilabs.jaer.projects.gui;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize the main PolarSpace GUI window
            PolarSpaceGUI polarSpaceGUI = new PolarSpaceGUI();

            // Get the display after GUI construction
            PolarSpaceDisplay display = polarSpaceGUI.getPolarSpaceDisplay();

            // Add test drawables to the PolarSpaceDisplay
            AgentDrawable drawable1 = new AgentDrawable("Drawable1");
            drawable1.setAzimuth(15);
            drawable1.setElevation(-10);
            drawable1.setSize(5);
            drawable1.setColor(Color.RED);

            AgentDrawable drawable2 = new AgentDrawable("Drawable2");
            drawable2.setAzimuth(-20);
            drawable2.setElevation(5);
            drawable2.setSize(8);
            drawable2.setColor(Color.BLUE);

            AgentDrawable drawable3 = new AgentDrawable("Drawable3");
            drawable3.setAzimuth(30);
            drawable3.setElevation(15);
            drawable3.setSize(6);
            drawable3.setColor(Color.GREEN);

            // Add the drawables to the display
            display.addDrawable(drawable1);
            display.addDrawable(drawable2);
            display.addDrawable(drawable3);

            // Optionally set up initial control panel settings if needed
            PolarSpaceControlPanel controlPanel = new PolarSpaceControlPanel(display, e -> polarSpaceGUI.dispose());
            polarSpaceGUI.add(controlPanel, BorderLayout.SOUTH);
            controlPanel.setAzimuthHeading(10);
            controlPanel.setElevationHeading(-5);
            controlPanel.setAzimuthRange(40);
            controlPanel.setElevationRange(30);
        });
    }
}

