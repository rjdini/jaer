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
import com.inilabs.jaer.projects.agent.*;
import java.awt.Color;
/**
 *
 * @author rjd
 */

public class AgentDrawable extends BasicDrawable implements Drawable {
    private boolean isActive = false;

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * @return the isActive
     */
    public boolean isActive() {
        return isActive;
    }
    
    public AgentDrawable(String key) {
        super(key);
      init();
    }
    
    public AgentDrawable() {
        super("AgentDrawable");
       init();
    }
    
    private void init() {
         setSize(2f);
        setColor(Color.magenta);
    }
    
    
    
}