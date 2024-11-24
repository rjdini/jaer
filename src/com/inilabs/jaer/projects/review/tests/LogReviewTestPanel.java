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

import com.inilabs.jaer.projects.gui.BasicTestPanel;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;
import com.inilabs.jaer.projects.gui.PolarSpaceGUI;
import com.inilabs.jaer.projects.gui.TestPanel;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import com.inilabs.jaer.projects.review.*;

public class LogReviewTestPanel extends BasicTestPanel implements TestPanel  {
   
    private LogVisualizerPanel logVisualizerPanel; 
      private final PolarSpaceGUI gui;
    private TrajectoryManager manager;

    public LogReviewTestPanel( PolarSpaceGUI gui) {
        this.gui = gui;
         manager = new TrajectoryManager(gui.getPolarSpaceDisplay());
        logVisualizerPanel = new LogVisualizerPanel(gui.getPolarSpaceDisplay(), manager); 
        initUI();
    }

    private void initUI() { 
         setPreferredSize(new Dimension(300, 400));
        setLayout(new BorderLayout());
      
        
    //     JPanel mainPanel = new JPanel();
         add(new JScrollPane(logVisualizerPanel), BorderLayout.NORTH);
     //   mainPanel.add(display, BorderLayout.NORTH);
     //   add(mainPanel, BorderLayout.NORTH);   
        // Load button for testing
       
           // Load button for testing
        JButton loadButton = new JButton("Load Data");
        loadButton.addActionListener(e -> loadData());
         add(loadButton, BorderLayout.SOUTH);   
       
    }
    
    
    @Override
    public void setGUICallBack(PolarSpaceGUI gui) {
        super.setGUICallBack(gui);
    }
    
    
    
    private void loadData() {
        JFileChooser fileChooser = new JFileChooser("./data");
        fileChooser.setDialogTitle("Select Log File");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                LogParser parser = new LogParser();
                Map<String, Map<String, TrajectoryDrawable>> sessions = parser.parseLogFile(filePath);

                logVisualizerPanel.loadSessions(sessions);
                System.out.println("Log data loaded successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load log data: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
