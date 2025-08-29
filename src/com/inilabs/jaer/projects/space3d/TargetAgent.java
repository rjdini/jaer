package com.inilabs.jaer.projects.space3d;

/**
 * TargetAgent: reciprocal motion (as before) + FBGTarget properties (size & shape).
 */
public class TargetAgent implements Agent3DInterface, Runnable, FBGTarget {

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

    // FBGTarget properties
    private float physicalDiameterM = 1.0f;
    private TargetShape shape = TargetShape.CIRCLE;
    private float densityScale = 1.0f;

    public TargetAgent(String key, Space3D.Vec3 p1, Space3D.Vec3 p2, double speedMps) {
        if (speedMps <= 0) throw new IllegalArgumentException("speedMps must be > 0");
        this.key = key;
        this.p1 = p1;
        this.p2 = p2;
        this.speedMps = speedMps;
    }

    /* ================= Agent3DInterface ================= */

    @Override public String getKey() { return key; }

    @Override public Agent3D.ObjectType getType() { return type; }

    @Override
    public Space3D.Vec3 getPosition3D() {
        Space3D.Vec3 d = p2.sub(p1);
        return new Space3D.Vec3(p1.x + s * d.x, p1.y + s * d.y, p1.z + s * d.z);
    }

    @Override
    public void setPosition3D(Space3D.Vec3 p) {
        Space3D.Vec3 d = p2.sub(p1);
        double len2 = d.x*d.x + d.y*d.y + d.z*d.z;
        if (len2 == 0) { s = 0; return; }
        Space3D.Vec3 r = p.sub(p1);
        double t = (r.x*d.x + r.y*d.y + r.z*d.z) / len2;
        s = Math.max(0.0, Math.min(1.0, t));
    }

    @Override public double[] getYawPitchRollDeg() { return new double[]{0,0,0}; }
    @Override public void setYawPitchRollDeg(double yawDeg, double pitchDeg, double rollDeg) { }
    @Override public double[] getLLA() { return new double[]{Double.NaN, Double.NaN, Double.NaN}; }
    @Override public void setLLA(double latDeg, double lonDeg, double altM) { }

    /* ================= FBGTarget ================= */

    @Override public float getPhysicalDiameterM() { return physicalDiameterM; }
    public void setPhysicalDiameterM(float d) { this.physicalDiameterM = d; }

    @Override public TargetShape getShape() { return shape; }
    public void setShape(TargetShape s) { this.shape = s; }

    @Override public float getDensityScale() { return densityScale; }
    public void setDensityScale(float ds) { this.densityScale = ds; }

    /* ================= Motion (threaded) ================= */

    public synchronized void start() {
        if (running) return;
        running = true;
        worker = new Thread(this, "TargetAgent-" + key);
        worker.setDaemon(true);
        worker.start();
    }

    public synchronized void stop() {
        running = false;
        Thread t = worker;
        worker = null;
        if (t != null && t.isAlive()) {
            try { t.join(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    public boolean isRunning() { return running; }

    /** Advance motion by dt seconds; reflects at endpoints; safe for tests. */
    public void tick(double dt){
        if (dt <= 0) return;
        final Space3D.Vec3 d = p2.sub(p1);
        final double L = d.norm();
        if (L == 0) return;
        final double vOverL = speedMps / L;
        double ds = dir * vOverL * dt;
        s += ds;
        while (s > 1.0 || s < 0.0) {
            if (s > 1.0) { s = 2.0 - s; dir = -dir; }
            else         { s = -s;      dir = -dir; }
        }
    }

    @Override
    public void run() {
        final long nanosPerStep = 16_000_000L; // ~60 Hz
        long last = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double dt = (now - last) * 1e-9;
            last = now;
            tick(dt);
            long spent = System.nanoTime() - now;
            long sleep = nanosPerStep - spent;
            if (sleep > 0) {
                try { Thread.sleep(sleep / 1_000_000, (int)(sleep % 1_000_000)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }
    }
}
