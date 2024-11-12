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

import com.inilabs.jaer.projects.target.TargetAgentDrawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestPanel extends JPanel {
    private final PolarSpaceDisplay display;
    private final List<TargetAgentDrawable> agents = new ArrayList<>();
    private float meanSpeed = 1.0f; // Default mean speed in degrees/second
    private float meanMaxLifeTime = 10.0f; // Default mean max lifetime in seconds

    public TestPanel(PolarSpaceDisplay display) {
        this.display = display;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, 400));

        // Button to add a new TargetAgentDrawable
        JButton addAgentButton = new JButton("Add Target Agent");
        addAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TargetAgentDrawable agent = createAgentNearStart();
                agents.add(agent);
                display.addDrawable(agent);
                display.repaint();
            }
        });
        add(addAgentButton);

        // Mean Speed Slider
        JSlider speedSlider = new JSlider(1, 10, 1); // Range: 1 to 10 degrees/second
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.addChangeListener(e -> meanSpeed = speedSlider.getValue());
        add(new JLabel("Mean Speed (deg/sec)"));
        add(speedSlider);

        // Mean Lifetime Slider
        JSlider lifetimeSlider = new JSlider(5, 30, 10); // Range: 5 to 30 seconds
        lifetimeSlider.setPaintTicks(true);
        lifetimeSlider.setPaintLabels(true);
        lifetimeSlider.setMajorTickSpacing(5);
        lifetimeSlider.addChangeListener(e -> meanMaxLifeTime = lifetimeSlider.getValue());
        add(new JLabel("Mean Max Lifetime (seconds)"));
        add(lifetimeSlider);

        // Timer for periodic updates of agents
        Timer updateTimer = new Timer(1000, e -> updateAgents());
        updateTimer.start();
    }

    private TargetAgentDrawable createAgentNearStart() {
        TargetAgentDrawable agent = new TargetAgentDrawable();
        Random random = new Random();

        // Initialize position and velocity using TestPanel’s factory setup
        float azimuth = -20 + random.nextFloat() * 10 - 5; // Randomly near -20
        float elevation = 20 + random.nextFloat() * 10 - 5; // Randomly near +20
        agent.setAzimuth(azimuth);
        agent.setElevation(elevation);
        
        // Set velocity directed towards (20, -20)
        float targetAzimuth = 20;
        float targetElevation = -20;
        float distance = (float) Math.sqrt(Math.pow(targetAzimuth - azimuth, 2) + Math.pow(targetElevation - elevation, 2));
        float velocityAzimuth = meanSpeed * (targetAzimuth - azimuth) / distance;
        float velocityElevation = meanSpeed * (targetElevation - elevation) / distance;

        agent.setVelocity(velocityAzimuth, velocityElevation);
        agent.setMaxLifeTime(meanMaxLifeTime);

        return agent;
    }

    private void updateAgents() {
        for (TargetAgentDrawable agent : agents) {
            agent.run();
        }
        display.repaint();
    }
}
