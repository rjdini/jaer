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
package com.inilabs.jaer.projects.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author rjd
 */
public class DrawableDisplayPanel extends JPanel {
    private final PolarSpaceDisplay polarDisplay;
    private final DefaultListModel<String> drawableListModel;
    private final JList<String> drawableList;
    
    public DrawableDisplayPanel(PolarSpaceDisplay polarDisplay) {        
        this.polarDisplay = polarDisplay;  
        setLayout(new BorderLayout(10, 10)); 
        add(new JLabel("Drawable Objects"), BorderLayout.NORTH);
        
         // panel setup to show drawable names
        drawableListModel = new DefaultListModel<>();
        drawableList = new JList<>(drawableListModel);
        JScrollPane scrollPane = new JScrollPane(drawableList);
        scrollPane.setPreferredSize(new Dimension(200, 0)); 
        add(new JLabel("Drawable Objects"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
           
    }
    
     // Method to update the list of drawable names in the side panel
    public void updateDrawableList() {
        List<String> currentDrawableNames = polarDisplay.getDrawableNames();
        drawableListModel.clear();
        for (String name : currentDrawableNames) {
            drawableListModel.addElement(name);
        }
    }
    
}
