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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackTest {

    // Create a logger instance
    private static final Logger logger = LoggerFactory.getLogger(LogbackTest.class);

    public static void main(String[] args) {

        // Test log messages at various levels
        logger.trace("This is a TRACE message - should appear if root level is set to TRACE.");
        logger.debug("This is a DEBUG message - should appear if root level is DEBUG or lower.");
        logger.info("This is an INFO message - should appear if root level is INFO or lower.");
        logger.warn("This is a WARN message - should appear if root level is WARN or lower.");
        logger.error("This is an ERROR message - should appear if root level is ERROR or lower.");
    }
}

