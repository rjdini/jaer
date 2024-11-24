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
package com.inilabs.jaer.projects.review.tests;

import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;

import java.awt.Dimension;

public class LogReviewTest {
    public static void main(String[] args) {
        // Create PolarSpaceGUI
        PolarSpaceGUI gui = new PolarSpaceGUI();
        gui.getPolarSpaceDisplay().setPreferredSize(new Dimension(1500, 600));

        // Create LogReviewTestPanel and integrate it with PolarSpaceGUI
        LogReviewTestPanel logReviewTestPanel = new LogReviewTestPanel(gui.getPolarSpaceDisplay());
        gui.setTestPanel(logReviewTestPanel);

        // Show GUI
        gui.setVisible(true);
        gui.getPolarSpaceDisplay().repaint();

        System.out.println("Logging Test initialized.");
    }
}

