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
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

public class LogbackDebugTest {

    public static void main(String[] args) {
        // Get the LoggerContext (Logback's internal logging context)
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Enable Logback's internal debug output
        StatusPrinter.setPrintStream(System.out);  // Print to console
        StatusPrinter.print(context);

        // Proceed with your logging code
        org.slf4j.Logger logger = LoggerFactory.getLogger(LogbackDebugTest.class);
        logger.info("Test log message - INFO level.");
        logger.debug("Test log message - DEBUG level.");
    }
}

