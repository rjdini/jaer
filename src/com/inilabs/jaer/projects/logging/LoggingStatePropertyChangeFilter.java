/*
 * Copyright (C) 2024 tobi.
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

import java.beans.PropertyChangeEvent;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.graphics.AEViewer;
import net.sf.jaer.eventprocessing.EventFilter2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author tobi
 */
public class LoggingStatePropertyChangeFilter extends EventFilter2D {
    private boolean loggingEnabled = false;
    private String JAERLoggingFilename = null;
 
 private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LoggingStatePropertyChangeFilter.class);
 
    public LoggingStatePropertyChangeFilter(AEChip chip) {
        super(chip);
    }

    @Override
    public EventPacket<? extends BasicEvent> filterPacket(EventPacket<? extends BasicEvent> in) {
        return in;
    }

    @Override
    public void resetFilter() {

    }

    @Override
    public void initFilter() {
        if (chip.getAeViewer() != null) {
            chip.getAeViewer().getSupport().addPropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isFilterEnabled()) {
            return;
        }
        switch (evt.getPropertyName()) {
            case AEViewer.EVENT_LOGGING_STARTED: 
                if(!AgentLogger.isLoggingEnabled()) { // dont interfere with an already running GUI job
                 JAERLoggingFilename =  chip.getAeViewer().getLoggingFile().getName();
                 chip.getAeViewer().getLoggingFile();
                 //  set default file name.
                 AgentLogger.setJAERFilename(JAERLoggingFilename);
                 AgentLogger.setJAERLoggingEnabled(true);
                log.info("Jaer Logging started: {} Filename: {}", evt.toString(), JAERLoggingFilename);
                }
                break;
            
            case AEViewer.EVENT_LOGGING_STOPPED:
                   if(!AgentLogger.isLoggingEnabled()) { // dont interfere with an already running GUI job
                JAERLoggingFilename =  chip.getAeViewer().getLoggingFile().getName();
                 //  chosen file name is available only at close.  TODO - prefer to keep default
                AgentLogger.setJAERFilename(JAERLoggingFilename);
                AgentLogger.setJAERLoggingEnabled(false);
                 log.info("Jaer Logging stopped: {} Filename: {}", evt.toString(), JAERLoggingFilename);
                   }
                 break;
                
            default:
        }
    }

    /**
     * @return the isLoggingEnabled
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * @param isLoggingEnabled the isLoggingEnabled to set
     */
    public void setLoggingEnabled(boolean yes) {
        this.loggingEnabled = yes;
    }

}
