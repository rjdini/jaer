package com.inilabs.jaer.projects.eventprocessing.filters;

import net.sf.jaer.Description;
import net.sf.jaer.DevelopmentStatus;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.BasicEvent;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.event.OutputEventIterator;
import net.sf.jaer.event.PolarityEvent;
import net.sf.jaer.eventprocessing.EventFilter2DMouseAdaptor;
import net.sf.jaer.graphics.FrameAnnotater;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import com.inilabs.jaer.projects.dvxplorer.imu.ImuPoseProvider;
import com.inilabs.jaer.projects.dvxplorer.imu.PoseSE3;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * IMU-driven planar warping to cancel ego-motion (DVXplorer).
 */
@Description("IMU-driven planar warping to cancel ego-motion (DVXplorer).")
@DevelopmentStatus(DevelopmentStatus.Status.InDevelopment)
public class EgomotionCompensatorIMU extends EventFilter2DMouseAdaptor
        implements FrameAnnotater, Observer, PropertyChangeListener {

    private double z0Meters = 3.0;
    private int refWindowUs = 20_000;

    private double fx = 420.0;
    private double fy = 420.0;
    private double cx = 320.0;
    private double cy = 240.0;

    private ImuPoseProvider imu;
    private PoseSE3 pose_t0;
    private long t0;

    private boolean annotationEnabled = true;

    private final double[][] K = new double[3][3];
    private final double[][] Kinv = new double[3][3];
    private final double[][] H = new double[3][3];
    
     private EventPacket outPacket=null;

    public EgomotionCompensatorIMU(AEChip chip) {
        super(chip);
        updateK();
    }

   // @Override
    public String getFilterName() {
        return "EgomotionCompensatorIMU";
    }

    @Override
    public void initFilter() {
        resetFilter();
    }

    @Override
    public synchronized void resetFilter() {
        t0 = 0;
        pose_t0 = null;
    }
    
    

    @Override
    public synchronized EventPacket<?> filterPacket(EventPacket<?> in) {
        if (in == null || in.getSize() == 0) {
            return in;
        }
        if (imu == null) {
            return in;
        }

        final long lastTs = in.getLastTimestamp();
        if (pose_t0 == null || (lastTs - t0) > refWindowUs) {
            t0 = lastTs;
            pose_t0 = imu.getPoseAtTimestamp(t0);
            if (pose_t0 == null) {
                return in;
            }
        }

       
        // ✅ pick one of these two:
    //EventPacket out = new EventPacket(in);                 // Option A
    
     // ✅ Option B done right: typed + capacity
   //   EventPacket<PolarityEvent> out = new EventPacket<>(PolarityEvent.class, in.getSize());
   //EventPacket out = new EventPacket(PolarityEvent.class, in.getSize());
   //  EventPacket<PolarityEvent> out =
     //   new EventPacket<PolarityEvent>(PolarityEvent.class, in.getSize());
     
    // If your jAER doesn’t have the (Class,int) ctor, do:
   // EventPacket<PolarityEvent> out = new EventPacket<>(PolarityEvent.class);
   // out.ensureCapacity(in.getSize());
        if(outPacket==null){
            outPacket=new EventPacket(in.getEventClass());
        }

        outPacket=new EventPacket(in.getEventClass());
        OutputEventIterator outItr = outPacket.outputIterator();

        for (Object oe : in) {
            BasicEvent e = (BasicEvent) oe;

            PoseSE3 pose_e = imu.getPoseAtTimestamp(e.timestamp);
            if (pose_e == null) {
                continue;
            }

            computeHomography(pose_e, pose_t0, z0Meters, H);

            float[] xy = warpPoint(H, e.x, e.y);
            if (xy == null) {
                continue;
            }

            int xw = Math.round(xy[0]);
            int yw = Math.round(xy[1]);
            if (xw < 0 || yw < 0 || xw >= chip.getSizeX() || yw >= chip.getSizeY()) {
                continue;
            }

            PolarityEvent outEv = (PolarityEvent) outItr.nextOutput();
            outEv.copyFrom(e);
            outEv.x = (short) xw;
            outEv.y = (short) yw;
        }

        return outPacket;
    }

    @Override
    public String getDescription() {
        return "IMU-driven planar warping to cancel ego-motion (DVXplorer).";
    }

    // GL-only FrameAnnotater
    @Override
    public void annotate(GLAutoDrawable drawable) {
        // Optional: add OpenGL overlay if desired
    }

    @Override
    public boolean isAnnotationEnabled() {
        return annotationEnabled;
    }

    @Override
    public void setAnnotationEnabled(boolean annotationEnabled) {
        this.annotationEnabled = annotationEnabled;
    }

    @Override
    public void update(Observable o, Object arg) {
        // no-op
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // no-op
    }

    private void computeHomography(PoseSE3 from, PoseSE3 to, double z0, double[][] Hout) {
        double[][] Rf = from.R;
        double[] tf = from.t;

        double[][] Rt = to.R;
        double[] tt = to.t;

        double[][] Rft = transpose(Rf);
        double[][] Rrel = matMul(Rt, Rft);

        double[] trel = matMul(Rt, sub(tf, tt));

        double[][] A = add(Rrel, outerDiv(trel, new double[]{0, 0, 1}, z0));

        double[][] tmp = matMul(K, A);
        double[][] tmp2 = matMul(tmp, Kinv);

        copy3x3(tmp2, Hout);
    }

    private float[] warpPoint(double[][] H, int x, int y) {
        double X = H[0][0] * x + H[0][1] * y + H[0][2];
        double Y = H[1][0] * x + H[1][1] * y + H[1][2];
        double W = H[2][0] * x + H[2][1] * y + H[2][2];
        if (Math.abs(W) < 1e-6) {
            return null;
        }
        return new float[]{(float) (X / W), (float) (Y / W)};
    }

    private void updateK() {
        K[0][0] = fx;  K[0][1] = 0;   K[0][2] = cx;
        K[1][0] = 0;   K[1][1] = fy;  K[1][2] = cy;
        K[2][0] = 0;   K[2][1] = 0;   K[2][2] = 1;
        invert3x3(K, Kinv);
    }

    private static void invert3x3(double[][] a, double[][] ainv) {
        double det =
                a[0][0] * (a[1][1] * a[2][2] - a[1][2] * a[2][1])
              - a[0][1] * (a[1][0] * a[2][2] - a[1][2] * a[2][0])
              + a[0][2] * (a[1][0] * a[2][1] - a[1][1] * a[2][0]);
        if (Math.abs(det) < 1e-12) {
            return;
        }
        double invDet = 1.0 / det;
        ainv[0][0] =  (a[1][1] * a[2][2] - a[1][2] * a[2][1]) * invDet;
        ainv[0][1] = -(a[0][1] * a[2][2] - a[0][2] * a[2][1]) * invDet;
        ainv[0][2] =  (a[0][1] * a[1][2] - a[0][2] * a[1][1]) * invDet;
        ainv[1][0] = -(a[1][0] * a[2][2] - a[1][2] * a[2][0]) * invDet;
        ainv[1][1] =  (a[0][0] * a[2][2] - a[0][2] * a[2][0]) * invDet;
        ainv[1][2] = -(a[0][0] * a[1][2] - a[0][2] * a[1][0]) * invDet;
        ainv[2][0] =  (a[1][0] * a[2][1] - a[1][1] * a[2][0]) * invDet;
        ainv[2][1] = -(a[0][0] * a[2][1] - a[0][1] * a[2][0]) * invDet;
        ainv[2][2] =  (a[0][0] * a[1][1] - a[0][1] * a[1][0]) * invDet;
    }

    private static double[][] matMul(double[][] A, double[][] B) {
        double[][] C = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double s = 0.0;
                for (int k = 0; k < 3; k++) {
                    s += A[i][k] * B[k][j];
                }
                C[i][j] = s;
            }
        }
        return C;
    }

    private static double[] matMul(double[][] A, double[] v) {
        return new double[]{
            A[0][0] * v[0] + A[0][1] * v[1] + A[0][2] * v[2],
            A[1][0] * v[0] + A[1][1] * v[1] + A[1][2] * v[2],
            A[2][0] * v[0] + A[2][1] * v[1] + A[2][2] * v[2]
        };
    }

    private static double[][] transpose(double[][] R) {
        double[][] Rt = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Rt[i][j] = R[j][i];
            }
        }
        return Rt;
    }

    private static double[] sub(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[][] add(double[][] A, double[][] B) {
        double[][] C = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    private static double[][] outerDiv(double[] u, double[] v, double d) {
        double[][] O = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                O[i][j] = (u[i] * v[j]) / d;
            }
        }
        return O;
    }

    private static void copy3x3(double[][] A, double[][] B) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                B[i][j] = A[i][j];
            }
        }
    }

    public double getZ0Meters() { return z0Meters; }
    public void setZ0Meters(double z0Meters) { this.z0Meters = z0Meters; }
    public int getRefWindowUs() { return refWindowUs; }
    public void setRefWindowUs(int refWindowUs) { this.refWindowUs = refWindowUs; }
    public double getFx() { return fx; }
    public void setFx(double fx) { this.fx = fx; updateK(); }
    public double getFy() { return fy; }
    public void setFy(double fy) { this.fy = fy; updateK(); }
    public double getCx() { return cx; }
    public void setCx(double cx) { this.cx = cx; updateK(); }
    public double getCy() { return cy; }
    public void setCy(double cy) { this.cy = cy; updateK(); }
    public ImuPoseProvider getImu() { return imu; }
    public void setImu(ImuPoseProvider imu) { this.imu = imu; }
}
