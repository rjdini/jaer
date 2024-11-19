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

import javax.swing.JPanel;

public class BasicTestPanel extends JPanel implements TestPanel {

    private PolarSpaceGUI guiCallback;

    @Override
    public void setGUICallBack(PolarSpaceGUI gui) {
        this.guiCallback = gui;
    }

    @Override
    public void update() {
        // Default implementation; subclasses can override this if needed.
    }

    /**
     * Returns the current PolarSpaceGUI callback instance.
     *
     * @return The PolarSpaceGUI instance.
     */
    protected PolarSpaceGUI getGUICallBack() {
        return guiCallback;
    }
}
