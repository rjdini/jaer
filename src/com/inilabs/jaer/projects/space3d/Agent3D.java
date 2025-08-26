package com.inilabs.jaer.projects.space3d;

/**
 * Common agent-related types.
 */
public final class Agent3D {

    private Agent3D() {}

    /** Types of agents in the 3D space. */
    public enum ObjectType { DVXPLORER, TARGET, WAYPOINT }

    /** (azimuth°, elevation°, distance m) triple */
    public static final class AzElDist {
        public final double azDeg, elDeg, distM;
        public AzElDist(double azDeg, double elDeg, double distM) {
            this.azDeg = azDeg; this.elDeg = elDeg; this.distM = distM;
        }
        @Override public String toString() {
            return String.format("az=%.3f°, el=%.3f°, d=%.3f m", azDeg, elDeg, distM);
        }
    }
}
