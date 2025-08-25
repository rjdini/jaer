package com.inilabs.jaer.projects.dvxplorer.imu;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Minimal ring-buffer-based IMU pose provider with linear interpolation in time.
 * Replace with your BOSCH-driven provider; this class is for testing/wiring.
 */
public class SimpleImuPoseProvider implements ImuPoseProvider {

    private static class Sample { final PoseSE3 pose;
        Sample(PoseSE3 p){ this.pose = p; } }
    private final Deque<Sample> buf = new ArrayDeque<>();
    private final int capacity = 512;

    /** Push a new pose sample in timestamp order. */
    public synchronized void push(PoseSE3 pose){
        if (!buf.isEmpty() && pose.timestampUs <= buf.getLast().pose.timestampUs) {
            // out-of-order; drop
            return;
        }
        buf.addLast(new Sample(pose));
        while (buf.size() > capacity) buf.removeFirst();
    }

    @Override
    public synchronized PoseSE3 getPoseAtTimestamp(long timestampUs) {
        if (buf.isEmpty()) return null;
        Sample prev = null;
        for (Sample s : buf) {
            if (s.pose.timestampUs == timestampUs) return s.pose;
            if (s.pose.timestampUs > timestampUs) {
                if (prev == null) return s.pose; // earliest
                return lerp(prev.pose, s.pose, timestampUs);
            }
            prev = s;
        }
        // requested time newer than newest sample
        return buf.getLast().pose;
    }

    private PoseSE3 lerp(PoseSE3 a, PoseSE3 b, long tReq){
        double ta=a.timestampUs, tb=b.timestampUs;
        double u = (tReq - ta) / Math.max(1.0, (tb - ta));
        // naive linear interp for t; slerp for R would be better; we linearize for simplicity here
        double[] t = new double[3];
        for (int i=0;i<3;i++) t[i] = (1-u)*a.t[i] + u*b.t[i];
        double[][] R = new double[3][3];
        for (int i=0;i<3;i++) for (int j=0;j<3;j++) R[i][j] = (1-u)*a.R[i][j] + u*b.R[i][j];
        return new PoseSE3(R,t,tReq);
    }
}
