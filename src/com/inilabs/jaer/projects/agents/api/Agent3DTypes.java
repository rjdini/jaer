/*
 * Copyright (C) 2025 rjd.
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
package com.inilabs.jaer.projects.agents.api;

/**
 *
 * @author rjd
 */

public final class Agent3DTypes {
    private Agent3DTypes(){}

    public enum ObjectType { DVXPLORER, TARGET, WAYPOINT }

    // Java 16+; if on older Java, use a final class with fields.
    public static final class AzElDist {
        public final double azDeg, elDeg, distM;
        public AzElDist(double azDeg, double elDeg, double distM){
            this.azDeg = azDeg; this.elDeg = elDeg; this.distM = distM;
        }
        @Override public String toString(){
            return String.format("az=%.3f°, el=%.3f°, d=%.3f m", azDeg, elDeg, distM);
        }
    }
}
