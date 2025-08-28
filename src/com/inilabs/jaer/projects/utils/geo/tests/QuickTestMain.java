package com.inilabs.jaer.projects.utils.geo.tests;

import com.inilabs.jaer.projects.utils.geo.GeoMath;
import com.inilabs.jaer.projects.utils.geo.FrameTransforms;

/** Plain Java quick-check runner (no JUnit dependency). */
public final class QuickTestMain {
    public static void main(String[] args) {
        int failed = 0;
        try {
            failed += testGeoRoundTrips();
            failed += testFrames();
        } catch (Throwable t) {
            t.printStackTrace();
            failed++;
        }
        if (failed == 0) {
            System.out.println("[OK] Quick tests passed.");
        } else {
            System.err.println("[FAIL] Quick tests failed: " + failed);
            System.exit(1);
        }
    }

    private static int testGeoRoundTrips(){
        int f=0;
        double lat=46.948, lon=7.4474, h=540;
        double[] ecef = GeoMath.llhDegToEcef(lat, lon, h);
        double[] llh2 = GeoMath.ecefToLlhDeg(ecef[0], ecef[1], ecef[2]);
        f += assertApprox(lat, llh2[0], 1e-7, "LLH-ECEF-LLH lat");
        f += assertApprox(lon, llh2[1], 1e-7, "LLH-ECEF-LLH lon");
        f += assertApprox(h,   llh2[2], 0.05,  "LLH-ECEF-LLH h");

        double lat0=46.0, lon0=8.0, h0=500.0;
        double ex=123.0, uy=45.6, nz=-78.9;
        double[] ecef2 = GeoMath.enuToEcef(ex, uy, nz, lat0, lon0, h0);
        double[] enu2  = GeoMath.ecefToEnu(ecef2[0], ecef2[1], ecef2[2], lat0, lon0, h0);
        f += assertApprox(ex, enu2[0], 1e-9, "ENU roundtrip x");
        f += assertApprox(uy, enu2[1], 1e-9, "ENU roundtrip y");
        f += assertApprox(nz, enu2[2], 1e-9, "ENU roundtrip z");
        return f;
    }

    private static int testFrames(){
        int f=0;
        double[] north = new double[]{0,0,1};
        double[] body  = FrameTransforms.enuToBody(north, 90, 0, 0);
        f += assertApprox(-1, body[0], 1e-9, "North->(-East) under yaw=90");
        f += assertApprox( 0, body[1], 1e-9, "Up invariant under yaw");
        f += assertApprox( 0, body[2], 1e-9, "North component -> 0 under yaw");

        double[] up = new double[]{0,1,0};
        double[] body2 = FrameTransforms.enuToBody(up, 123, 0, 0);
        f += assertApprox(0, body2[0], 1e-9, "Up x under yaw");
        f += assertApprox(1, body2[1], 1e-9, "Up y under yaw");
        f += assertApprox(0, body2[2], 1e-9, "Up z under yaw");

        double[] enu = new double[]{0.3, -0.2, 0.5};
        double yaw=35, pitch=-10, roll=25;
        double[] b = FrameTransforms.enuToBody(enu, yaw, pitch, roll);
        double[] e = FrameTransforms.bodyToEnu(b, yaw, pitch, roll);
        f += assertApprox(enu[0], e[0], 1e-9, "Inverse x");
        f += assertApprox(enu[1], e[1], 1e-9, "Inverse y");
        f += assertApprox(enu[2], e[2], 1e-9, "Inverse z");
        return f;
    }

    private static int assertApprox(double exp, double got, double tol, String msg){
        if (Math.abs(exp-got) <= tol) return 0;
        System.err.printf("ASSERT FAIL: %s expected=%.12f got=%.12f tol=%.3g%n", msg, exp, got, tol);
        return 1;
    }
}
