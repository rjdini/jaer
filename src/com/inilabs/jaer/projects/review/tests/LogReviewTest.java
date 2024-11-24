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

import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import java.awt.Dimension;
import javax.swing.*;

public class LogReviewTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create an instance of PolarSpaceGUI
            PolarSpaceGUI gui = new PolarSpaceGUI();
            gui.setName("Log Review Test");
            gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            PolarSpaceDisplay display = gui.getPolarSpaceDisplay();
            display.setPreferredSize(new Dimension(1000, 600));
            display.initializeDisplay();

            LogReviewTestPanel testPanel = new LogReviewTestPanel(gui);
            gui.setTestPanel(testPanel);

            gui.setVisible(true);
            gui.getPolarSpaceDisplay().repaint();

        });
    }
}
