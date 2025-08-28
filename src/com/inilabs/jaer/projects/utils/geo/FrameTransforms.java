package com.inilabs.jaer.projects.utils.geo;

/**
 * FrameTransforms: yaw/pitch/roll rotations for Space3D (x=E, y=U, z=N).
 * yaw about +y, pitch about +x, roll about +z.
 */
public final class FrameTransforms {
    private FrameTransforms(){}
    private static double d2r(double d){ return Math.toRadians(d); }

    public static double[][] Rx(double deg){
        double a=d2r(deg), c=Math.cos(a), s=Math.sin(a);
        return new double[][]{{1,0,0},{0,c,-s},{0,s,c}};
    }
    public static double[][] Ry(double deg){
        double a=d2r(deg), c=Math.cos(a), s=Math.sin(a);
        return new double[][]{{c,0,s},{0,1,0},{-s,0,c}};
    }
    public static double[][] Rz(double deg){
        double a=d2r(deg), c=Math.cos(a), s=Math.sin(a);
        return new double[][]{{c,-s,0},{s,c,0},{0,0,1}};
    }

    public static double[][] mul(double[][] A, double[][] B){
        double[][] C = new double[3][3];
        for(int i=0;i<3;i++) for(int j=0;j<3;j++){
            double s=0; for(int k=0;k<3;k++) s+=A[i][k]*B[k][j]; C[i][j]=s;
        }
        return C;
    }
    public static double[][] T(double[][] R){
        return new double[][]{
            {R[0][0], R[1][0], R[2][0]},
            {R[0][1], R[1][1], R[2][1]},
            {R[0][2], R[1][2], R[2][2]}
        };
    }

    /** R = Rz(roll) * Rx(pitch) * Ry(yaw) */
    public static double[][] R_ypr(double yawDeg, double pitchDeg, double rollDeg){
        return mul(Rz(rollDeg), mul(Rx(pitchDeg), Ry(yawDeg)));
    }
    public static double[] apply(double[][] R, double[] v){
        return new double[]{
            R[0][0]*v[0]+R[0][1]*v[1]+R[0][2]*v[2],
            R[1][0]*v[0]+R[1][1]*v[1]+R[1][2]*v[2],
            R[2][0]*v[0]+R[2][1]*v[1]+R[2][2]*v[2]
        };
    }
    public static double[] enuToBody(double[] enu, double yawDeg, double pitchDeg, double rollDeg){
        double[][] Rt = T(R_ypr(yawDeg, pitchDeg, rollDeg));
        return apply(Rt, enu);
    }
    public static double[] bodyToEnu(double[] body, double yawDeg, double pitchDeg, double rollDeg){
        return apply(R_ypr(yawDeg, pitchDeg, rollDeg), body);
    }
}
