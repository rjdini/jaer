package com.inilabs.jaer.projects.dvxplorer.imu;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Minimal Bosch IMU pose provider with buffered poses and timestamp interpolation.
 * Stores quaternions (w,x,y,z) and world positions, and returns interpolated PoseSE3.
 * Replace addSample(...) calls with your stream (BMI160 decode path).
 */
public class BoschImuPoseProvider implements ImuPoseProvider {

    private static class Sample {
        final long ts;
        final double[] q;   // quaternion [w, x, y, z]
        final double[] p;   // position [x, y, z] in world

        Sample(long ts, double[] q, double[] p) {
            this.ts = ts;
            this.q = q;
            this.p = p;
        }
    }

    private final Deque<Sample> buf = new ArrayDeque<>();
    private int capacity = 1024;

    public synchronized void reset() {
        buf.clear();
    }

    /** Feed a new pose sample (already fused from IMU) into the buffer. */
    public synchronized void addSample(long timestampUs, double[] quatWxyz, double[] posWorld) {
        buf.addLast(new Sample(timestampUs, quatWxyz, posWorld));
        while (buf.size() > capacity) {
            buf.removeFirst();
        }
    }

    @Override
    public synchronized PoseSE3 getPoseAtTimestamp(long timestampUs) {
        if (buf.isEmpty()) {
            return null;
        }

        Sample prev = null;
        for (Sample s : buf) {
            if (s.ts == timestampUs) {
                return toPose(s);
            }
            if (s.ts > timestampUs) {
                if (prev == null) {
                    return toPose(s);
                }
                double u = (timestampUs - prev.ts) / (double) (s.ts - prev.ts);
                double[] q = slerp(prev.q, s.q, u);
                double[] p = lerp3(prev.p, s.p, u);
                return new PoseSE3(quatToR(q), p, timestampUs);
            }
            prev = s;
        }

        // newer than newest
        return toPose(buf.getLast());
    }

    private PoseSE3 toPose(Sample s) {
        return new PoseSE3(quatToR(s.q), s.p, s.ts);
    }

    private static double[] lerp3(double[] a, double[] b, double u) {
        return new double[]{
            a[0] * (1 - u) + b[0] * u,
            a[1] * (1 - u) + b[1] * u,
            a[2] * (1 - u) + b[2] * u
        };
    }

    private static double[] slerp(double[] q1, double[] q2, double u) {
        double dot = q1[0] * q2[0] + q1[1] * q2[1] + q1[2] * q2[2] + q1[3] * q2[3];
        if (dot < 0) {
            q2 = new double[]{-q2[0], -q2[1], -q2[2], -q2[3]};
            dot = -dot;
        }

        final double DOT_THRESHOLD = 0.9995;
        if (dot > DOT_THRESHOLD) {
            double[] q = new double[]{
                q1[0] + u * (q2[0] - q1[0]),
                q1[1] + u * (q2[1] - q1[1]),
                q1[2] + u * (q2[2] - q1[2]),
                q1[3] + u * (q2[3] - q1[3])
            };
            double n = Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
            return new double[]{q[0] / n, q[1] / n, q[2] / n, q[3] / n};
        }

        double theta0 = Math.acos(dot);
        double theta = theta0 * u;
        double sinTheta = Math.sin(theta);
        double sinTheta0 = Math.sin(theta0);
        double s0 = Math.cos(theta) - dot * sinTheta / sinTheta0;
        double s1 = sinTheta / sinTheta0;

        return new double[]{
            s0 * q1[0] + s1 * q2[0],
            s0 * q1[1] + s1 * q2[1],
            s0 * q1[2] + s1 * q2[2],
            s0 * q1[3] + s1 * q2[3]
        };
    }

    private static double[][] quatToR(double[] q) {
        double w = q[0];
        double x = q[1];
        double y = q[2];
        double z = q[3];

        double ww = w * w;
        double xx = x * x;
        double yy = y * y;
        double zz = z * z;

        return new double[][]{
            {ww + xx - yy - zz, 2 * (x * y - w * z), 2 * (x * z + w * y)},
            {2 * (x * y + w * z), ww - xx + yy - zz, 2 * (y * z - w * x)},
            {2 * (x * z - w * y), 2 * (y * z + w * x), ww - xx - yy + zz}
        };
    }
}
