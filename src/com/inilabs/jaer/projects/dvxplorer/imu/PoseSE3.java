package com.inilabs.jaer.projects.dvxplorer.imu;

/**
 * Simple SE3 pose container.
 * R maps world -> camera. t is camera position in world (meters).
 */
public class PoseSE3 {
    public final double[][] R;   // 3x3 rotation (world -> camera)
    public final double[] t;     // 3-vector translation (world meters)
    public final long timestampUs;

    public PoseSE3(double[][] R, double[] t, long timestampUs) {
        this.R = R;
        this.t = t;
        this.timestampUs = timestampUs;
    }

    public static PoseSE3 identity(long ts) {
        return new PoseSE3(
            new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
            },
            new double[]{0, 0, 0},
            ts
        );
    }
}
