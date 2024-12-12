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

package com.inilabs.jaer.projects.tracker.tests;

import com.inilabs.jaer.gimbal.GimbalBase;
import com.inilabs.jaer.projects.cog.SpatialAttention;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.tracker.FieldOfView;
import com.inilabs.jaer.projects.tracker.TrackerManagerEngine;
import java.awt.Dimension;
import javax.swing.Timer;

public class TrackerManagerEngineTest {
    private static GimbalBase gimbalBase = new GimbalBase();
    private static FieldOfView fov = FieldOfView.getInstance();
    private static SpatialAttention spatialAttention = SpatialAttention.getInstance();
    
     
    public static void main(String[] args) {
         // Create an instance of TrackerManagerEngine
        TrackerManagerEngine engine = new TrackerManagerEngine();
           // Create an instance of TrackerManagerEngineTestPanel
         TrackerManagerEngineTestPanel testPanel = new TrackerManagerEngineTestPanel(engine);
         
         
        // Create an instance of PolarSpaceGUI
        PolarSpaceGUI gui = new PolarSpaceGUI();
        gui.getPolarSpaceDisplay().setPreferredSize(new Dimension(1000, 600));

        // Integrate the test panel with the PolarSpaceGUI
        gui.setTestPanel(testPanel);

        // Explicitly ensure the timer starts
       
        gui.setVisible(true);
        gui.getPolarSpaceDisplay().repaint();
        
         AgentLogger. setIsSystemTimestamp(true);
    }
}
