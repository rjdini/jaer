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

package com.inilabs.jaer.projects.logging;

import com.inilabs.jaer.gimbal.GimbalBase;
import com.inilabs.jaer.projects.cog.SpatialAttention;
import java.beans.PropertyChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static net.sf.jaer.eventprocessing.EventFilter.log;
import net.sf.jaer.graphics.AEViewer;

public class AgentLogger {

   
    private static final Logger datalog = LoggerFactory.getLogger(AgentLogger.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static boolean loggingEnabled = true;
    private static boolean jaerLoggingEnabled = false;
    private static boolean guiLoggingEnabled = false;
    private static boolean initialized = false;
    private static String JAERFilename = "null";
    private static int jaerTimestamp = 0;
    private static int aerLoggingSessionNumber = 0;
    private static int guiLoggingSessionNumber = 0;
    private static boolean isSystemTimestamp = false;
    
    private static AgentLogger instance ;
    
    public AgentLogger() {
    }
    
    public static AgentLogger getInstance() {
    if (instance == null) {
        synchronized (AgentLogger.class) {
            if (instance == null) {
                instance = new AgentLogger();
            }
        }
    }
    return instance;
}
    
    
    
    
    
    /**
     * 
     *     Initialize the Logger: Before using AgentLogger, call AgentLogger.initialize() once at the start of your application.
    *     Shutdown: Call AgentLogger.shutdown() when you want to stop logging, for instance, when your application closes
     *    This approach avoids the static initializer and ensures controlled initialization and shutdown of logging without triggering exceptions.
     *    
     *    Initializes the logger by logging the LOGGER_START event.
     */
    public static void initialize() {
        if (!initialized) {
            initialized = true;
            loggingEnabled = true;
            logSystemEvent(EventType.LOGGER_START, "Logger started");
            loggingEnabled = false;
            jaerLoggingEnabled = false;
            guiLoggingEnabled = false;
        }
    }

    /**
     * Shuts down the logger by logging the LOGGER_CLOSE event if logging is enabled.
     */
    public static void shutdown() { 
            loggingEnabled = true;
            logSystemEvent(EventType.LOGGER_CLOSE, "Logger shutting down");
    }

    /**
     * @return the isSystemTimestamp
     */
    public static boolean isIsSystemTimestamp() {
        return isSystemTimestamp;
    }

    /**
     * @param isSystemTimestamp the isSystemTimestamp to set
     */
    public static void setIsSystemTimestamp(boolean yes) {
        isSystemTimestamp = yes;
    }
    
  
    /**
     * Enables or disables logging.
     * Logging remains on while either PolarSpaceGUI or AER are true
     */
    public static void updateLoggingEnabled() {
        if( jaerLoggingEnabled || guiLoggingEnabled) {
            loggingEnabled = true; }
        else { 
            loggingEnabled = false;
        }
    }

    public static void setJAERTimestamp(int ts) {
        jaerTimestamp = ts;
    }
    
    /**
     * Enables or disables AER logging.
     */
    public static void setJAERLoggingEnabled(boolean enabled) {
        if(enabled){
             aerLoggingSessionNumber =  aerLoggingSessionNumber +1;
            // sequence critical
            jaerLoggingEnabled = true;
            updateLoggingEnabled();
            logJAEREvent( EventType.JAER_START_LOG,  aerLoggingSessionNumber, "see jaer_stop_log");
        } 
        else {
            String filename = getJAERFilename();
            if(filename==null){filename="null";}
             // sequence critical
            logJAEREvent( EventType.JAER_STOP_LOG, aerLoggingSessionNumber, filename);
            jaerLoggingEnabled = false;
            updateLoggingEnabled();
        }
    }
    
    /**
     * Enables or disables PolarSpatialGUI logging.
     */
    public static void setGUILoggingEnabled(boolean enabled) {
        if(enabled) {
              guiLoggingSessionNumber =  guiLoggingSessionNumber +1;
               // sequence critical
               guiLoggingEnabled = true;
              updateLoggingEnabled();
               logGUIEvent( EventType.GUI_START_LOG,  guiLoggingSessionNumber, "from PolarSpaceGUI");   
        } else {
               // sequence critical
              logGUIEvent( EventType.GUI_STOP_LOG,  guiLoggingSessionNumber, "from PolarSpaceGUI");    
              guiLoggingEnabled = false;
              updateLoggingEnabled();
        }
    }

    /**
     * Checks if logging is enabled.
     */
    public static boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public static void logAgentEvent(EventType eventType, String key, float azimuth, float elevation, List<String> clusters) {
    if (loggingEnabled) {
        long timestamp = getTimestamp();
        String logMessage = LogEventFormatter.formatAgentLogEvent(eventType, timestamp, key, azimuth, elevation, clusters);
        datalog.info(logMessage);
    }
}
    
    /**
     * Logs a general system event in JSON format with an event type, datetime, and message if logging is enabled.
     */
    public static void logSystemEvent(EventType eventType, String message) {
        if (loggingEnabled) {
            long timestamp = getTimestamp();
            String datetime = LocalDateTime.now().format(ISO_FORMATTER);
            String logMessage = LogEventFormatter.formatSystemLogEvent(eventType, datetime, timestamp, message);
            datalog.info(logMessage);
        }
    }
    
     public static void logJAEREvent(EventType eventType, int sessionNumber, String filename) {
        if (loggingEnabled) {
            long timestamp = getTimestamp();
            String datetime = LocalDateTime.now().format(ISO_FORMATTER);
            String logMessage = LogEventFormatter.formatJAERLogEvent(eventType, datetime, timestamp, sessionNumber, filename);
            datalog.info(logMessage);
        }
    }

      public static void logGUIEvent(EventType eventType, int sessionNumber, String message) {
        if (loggingEnabled) {
            long timestamp = getTimestamp();
            String datetime = LocalDateTime.now().format(ISO_FORMATTER);
            String logMessage = LogEventFormatter.formatJAERLogEvent(eventType, datetime, timestamp, sessionNumber, message);
            datalog.info(logMessage);
        }
    }
     
     
     
    /**
     * Provides the current timestamp in milliseconds, encapsulated for future flexibility.
     */
    protected static long getTimestamp() {
         if( isIsSystemTimestamp() ) { 
             return System.currentTimeMillis();
         }
         else {
           return jaerTimestamp;
         }
    }

    /**
     * @return the JAERFilename
     */
    public static String getJAERFilename() {
        return JAERFilename;
    }

    /**
     * @param aJAERFilename the JAERFilename to set
     */
    public static void setJAERFilename(String aJAERFilename) {
        JAERFilename = aJAERFilename;
    }
}
