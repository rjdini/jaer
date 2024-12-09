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


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


public class HelloWorldLogger {

    // Initialize the logger using Logback
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldLogger.class);
    private static final Marker DATA_MARKER = MarkerFactory.getMarker("DATA");
 
    
      public static void main(String[] args) {
        logger.info("This is a test message. If you see this, Logback is active.");
        logger.info(DATA_MARKER, "AgentData This will go to data.log because it has the DATA marker.");
        logger.info("This will go to the console only, not data.log.");
    }
   
}
