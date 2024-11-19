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
package com.inilabs.jaer.projects.logging.tests;

import com.inilabs.jaer.projects.gui.AgentDrawable;
import com.inilabs.jaer.projects.logging.AgentLogger;
import com.inilabs.jaer.projects.logging.EventType;
import com.inilabs.jaer.projects.logging.ListAppender;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentDrawableLogTest {

    private ListAppender listAppender;

    @BeforeEach
    public void setup() {
        // Retrieve the LoggerContext and get the ListAppender
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        listAppender = (ListAppender) config.getAppender("TestListAppender");

        // Clear previous log entries
        if (listAppender != null) {
            listAppender.clear();
        }
    }

    @Test
    public void testLogEventCreation() {
        // Arrange
        String key = "TestAgent";
        float azimuth = 10.0f;
        float elevation = 5.0f;
        List<String> clusters = List.of("Cluster1", "Cluster2");

        // Act
        AgentLogger.logAgentEvent(EventType.CREATE, key, azimuth, elevation, clusters);

        // Retrieve log messages captured by ListAppender
        List<String> logMessages = listAppender.getMessages();

        // Assert - Check that log contains the expected JSON structure with correct values
        assertTrue(logMessages.stream().anyMatch(message -> message.contains("\"event_type\": \"creation\"")));
        assertTrue(logMessages.stream().anyMatch(message -> message.contains("\"key\": \"" + key + "\"")));
        assertTrue(logMessages.stream().anyMatch(message -> message.contains("\"azimuth\": 10.0")));
        assertTrue(logMessages.stream().anyMatch(message -> message.contains("\"elevation\": 5.0")));
        assertTrue(logMessages.stream().anyMatch(message -> message.contains("\"clusters\": [\"Cluster1\", \"Cluster2\"]")));
    }
}
