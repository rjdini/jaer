package com.inilabs.jaer.projects.utils.geo;

/**
 * GeoMath: WGS84 <-> ECEF <-> ENU utilities.
 * ENU axes (Space3D convention): x=EAST, y=UP, z=NORTH.
 */
public final class GeoMath {
    private GeoMath(){}

    public static final double WGS84_A  = 6378137.0;
    public static final double WGS84_F  = 1.0 / 298.257223563;
    public static final double WGS84_B  = WGS84_A*(1.0 - WGS84_F);
    public static final double WGS84_E2 = 1.0 - (WGS84_B*WGS84_B)/(WGS84_A*WGS84_A);

    private static double deg2rad(double d){ return Math.toRadians(d); }
    private static double rad2deg(double r){ return Math.toDegrees(r); }

    public static double[] llhDegToEcef(double latDeg, double lonDeg, double h){
        final double lat = deg2rad(latDeg);
        final double lon = deg2rad(lonDeg);
        final double sinLat = Math.sin(lat), cosLat = Math.cos(lat);
        final double sinLon = Math.sin(lon), cosLon = Math.cos(lon);
        final double N = WGS84_A / Math.sqrt(1.0 - WGS84_E2*sinLat*sinLat);
        final double x = (N + h) * cosLat * cosLon;
        final double y = (N + h) * cosLat * sinLon;
        final double z = (N*(1.0 - WGS84_E2) + h) * sinLat;
        return new double[]{x, y, z};
    }

    public static double[] ecefToLlhDeg(double x, double y, double z){
        final double a = WGS84_A, b = WGS84_B;
        final double e2 = WGS84_E2;
        final double ep2 = (a*a - b*b)/(b*b);
        final double p = Math.hypot(x, y);
        final double th = Math.atan2(a*z, b*p);
        final double sinTh = Math.sin(th), cosTh = Math.cos(th);
        final double lat = Math.atan2(z + ep2*b*sinTh*sinTh*sinTh, p - e2*a*cosTh*cosTh*cosTh);
        final double lon = Math.atan2(y, x);
        final double sinLat = Math.sin(lat);
        final double N = a / Math.sqrt(1.0 - e2*sinLat*sinLat);
        final double h = p/Math.cos(lat) - N;
        return new double[]{ rad2deg(lat), rad2deg(lon), h };
    }

    public static double[][] ecefToEnuR(double lat0Deg, double lon0Deg){
        final double lat = deg2rad(lat0Deg);
        final double lon = deg2rad(lon0Deg);
        final double sLat = Math.sin(lat), cLat = Math.cos(lat);
        final double sLon = Math.sin(lon), cLon = Math.cos(lon);
        return new double[][]{
            { -sLon,          cLon,         0    },   // x=East
            {  cLat*cLon,  cLat*sLon,   sLat    },   // y=Up
            { -sLat*cLon, -sLat*sLon,   cLat    }    // z=North
        };
    }

    public static double[][] enuToEcefR(double lat0Deg, double lon0Deg){
        return transpose(ecefToEnuR(lat0Deg, lon0Deg));
    }

    public static double[] ecefToEnu(double x, double y, double z,
                                     double lat0Deg, double lon0Deg, double h0){
        double[] ecef0 = llhDegToEcef(lat0Deg, lon0Deg, h0);
        double dx = x - ecef0[0], dy = y - ecef0[1], dz = z - ecef0[2];
        double[][] R = ecefToEnuR(lat0Deg, lon0Deg);
        return matVec(R, new double[]{dx, dy, dz});
    }

    public static double[] enuToEcef(double ex, double uy, double nz,
                                     double lat0Deg, double lon0Deg, double h0){
        double[] ecef0 = llhDegToEcef(lat0Deg, lon0Deg, h0);
        double[][] R = enuToEcefR(lat0Deg, lon0Deg);
        double[] d = matVec(R, new double[]{ex, uy, nz});
        return new double[]{ ecef0[0]+d[0], ecef0[1]+d[1], ecef0[2]+d[2] };
    }

    public static double[] llhDegToEnu(double latDeg, double lonDeg, double h,
                                       double lat0Deg, double lon0Deg, double h0){
        double[] ecef = llhDegToEcef(latDeg, lonDeg, h);
        return ecefToEnu(ecef[0], ecef[1], ecef[2], lat0Deg, lon0Deg, h0);
    }

    public static double[] enuToLlhDeg(double ex, double uy, double nz,
                                       double lat0Deg, double lon0Deg, double h0){
        double[] ecef = enuToEcef(ex, uy, nz, lat0Deg, lon0Deg, h0);
        return ecefToLlhDeg(ecef[0], ecef[1], ecef[2]);
    }

    public static double[][] transpose(double[][] M){
        int r=M.length, c=M[0].length;
        double[][] T = new double[c][r];
        for(int i=0;i<r;i++) for(int j=0;j<c;j++) T[j][i]=M[i][j];
        return T;
    }
    public static double[] matVec(double[][] M, double[] v){
        int r=M.length, c=M[0].length;
        double[] out = new double[r];
        for(int i=0;i<r;i++){
            double s=0; for(int j=0;j<c;j++) s+=M[i][j]*v[j]; out[i]=s;
        }
        return out;
    }
}
