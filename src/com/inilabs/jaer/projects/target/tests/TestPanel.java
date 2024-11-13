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

package com.inilabs.jaer.projects.target.tests;

import com.inilabs.jaer.projects.target.TargetAgentDrawable;
import com.inilabs.jaer.projects.gui.PolarSpaceDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.inilabs.jaer.projects.target.AgentCallback;
import com.inilabs.jaer.projects.target.ActionType;

public class TestPanel extends JPanel implements AgentCallback {
    private final PolarSpaceDisplay display;
    private final List<TargetAgentDrawable> agents = new ArrayList<>();
    private float meanSpeed = 1.0f; // Default mean speed in degrees/second
    private float meanMaxLifeTime = 10.0f; // Default mean max lifetime in seconds

    public TestPanel(PolarSpaceDisplay display) {
        this.display = display;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, 400));

        JButton addAgentButton = new JButton("Add Target Agent");
        addAgentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TargetAgentDrawable agent = createAgentNearStart();
                agents.add(agent);
                agent.setCallback(TestPanel.this);  // Set the callback to this TestPanel instance
                display.addDrawable(agent);
                display.repaint();
            }
        });
        add(addAgentButton);

        JSlider speedSlider = new JSlider(1, 10, 1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.addChangeListener(e -> meanSpeed = speedSlider.getValue());
        add(new JLabel("Mean Speed (deg/sec)"));
        add(speedSlider);

        JSlider lifetimeSlider = new JSlider(5, 30, 10);
        lifetimeSlider.setPaintTicks(true);
        lifetimeSlider.setPaintLabels(true);
        lifetimeSlider.setMajorTickSpacing(5);
        lifetimeSlider.addChangeListener(e -> meanMaxLifeTime = lifetimeSlider.getValue());
        add(new JLabel("Mean Max Lifetime (seconds)"));
        add(lifetimeSlider);

        Timer updateTimer = new Timer(100, e -> updateAgents());
        updateTimer.start();
    }

    private TargetAgentDrawable createAgentNearStart() {
        TargetAgentDrawable agent = new TargetAgentDrawable();
        Random random = new Random();

        float azimuth = -20 + random.nextFloat() * 10 - 5;
        float elevation = 20 + random.nextFloat() * 10 - 5;
        agent.setAzimuth(azimuth);
        agent.setElevation(elevation);

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
        for (TargetAgentDrawable agent : new ArrayList<>(agents)) {
            agent.run();
        }
        display.repaint();
    }

    @Override
    public void onAgentAction(ActionType action, String key) {
        if (action == ActionType.REMOVE) {
            agents.removeIf(agent -> agent.getKey().equals(key));
            display.removeDrawable(key);
            display.repaint();
        }
    }
}

