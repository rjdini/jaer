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
package com.inilabs.jaer.projects.space3d;

/**
 *
 * @author rjd
 */

/**
 * TargetAgent: moves reciprocally between two ENU waypoints at constant speed.
 * - Owns its update thread (start/stop).
 * - Motion is along the straight segment p1 <-> p2 with "bounce" at the ends.
 * - XZ plane motion (Y stays as provided in p1/p2; use 0 if you want strictly flat).
 */
public class TargetAgent implements Agent3DInterface, Runnable {

    private final String key;
    private final Agent3D.ObjectType type = Agent3D.ObjectType.TARGET;

    private final Space3D.Vec3 p1;       // start waypoint
    private final Space3D.Vec3 p2;       // end waypoint
    private final double speedMps;       // constant speed along segment

    // Parametric position s in [0,1] along segment p(s) = p1 + s*(p2-p1).
    // Direction dir = +1 forward (p1->p2) or -1 backward (p2->p1).
    private double s = 0.0;
    private int dir = +1;

    // Threading
    private volatile boolean running = false;
    private Thread worker;
    private long lastNanos;

    public TargetAgent(String key, Space3D.Vec3 p1, Space3D.Vec3 p2, double speedMps) {
        if (speedMps <= 0) throw new IllegalArgumentException("speedMps must be > 0");
        this.key = key;
        this.p1 = p1;
        this.p2 = p2;
        this.speedMps = speedMps;
        this.lastNanos = System.nanoTime();
    }

    /* ================= Agent3DInterface ================= */

    @Override public String getKey() { return key; }

    @Override public Agent3D.ObjectType getType() { return type; }

    @Override
    public Space3D.Vec3 getPositionDVX() {
        // Compute current point from s
        Space3D.Vec3 d = p2.sub(p1);
        return new Space3D.Vec3(p1.x + s * d.x, p1.y + s * d.y, p1.z + s * d.z);
    }

    @Override
    public void setPositionDVX(Space3D.Vec3 p) {
        // Reproject onto segment to set s (optional; not typically used)
        Space3D.Vec3 d = p2.sub(p1);
        double len2 = d.x*d.x + d.y*d.y + d.z*d.z;
        if (len2 == 0) { s = 0; return; }
        Space3D.Vec3 r = p.sub(p1);
        double t = (r.x*d.x + r.y*d.y + r.z*d.z) / len2;
        s = Math.max(0.0, Math.min(1.0, t));
    }

    @Override public double[] getYawPitchRollDeg() { return new double[]{0,0,0}; }
    @Override public void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg) { /* no-op */ }

    @Override public double[] getLLA() { return new double[]{Double.NaN, Double.NaN, Double.NaN}; }
    @Override public void setLLA(double latDeg, double lonDeg, double altM) { /* no-op */ }

    /* ================= Thread control ================= */

    public synchronized void start() {
        if (running) return;
        running = true;
        lastNanos = System.nanoTime();
        worker = new Thread(this, "TargetAgent-" + key);
        worker.setDaemon(true);
        worker.start();
    }

    public synchronized void stop() {
        running = false;
        if (worker != null) {
            try { worker.join(300); } catch (InterruptedException ignored) {}
            worker = null;
        }
    }

    public boolean isRunning() { return running; }

    /* ================= Motion loop ================= */

    @Override
    public void run() {
        // Precompute segment length
        final Space3D.Vec3 d = p2.sub(p1);
        final double L = d.norm();
        if (L == 0) return; // degenerate case

        final double vOverL = speedMps / L; // rate of change of s per second

        while (running) {
            long now = System.nanoTime();
            double dt = (now - lastNanos) * 1e-9;
            lastNanos = now;

            // Advance param s
            double ds = dir * vOverL * dt;
            s += ds;

            // Reflect at boundaries; handle big dt (multiple bounces)
            while (s > 1.0 || s < 0.0) {
                if (s > 1.0) {
                    s = 2.0 - s;   // reflect around 1
                    dir = -dir;
                } else if (s < 0.0) {
                    s = -s;        // reflect around 0
                    dir = -dir;
                }
            }

            // Sleep a bit to cap CPU usage; ~60 Hz update is plenty for GUI
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
