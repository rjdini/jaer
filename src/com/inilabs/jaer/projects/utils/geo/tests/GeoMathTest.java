package com.inilabs.jaer.projects.utils.geo.tests;

import com.inilabs.jaer.projects.utils.geo.GeoMath;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GeoMathTest {
    @Test
    public void testRoundTripLlhEcef() {
        double lat=46.95, lon=7.45, h=540;
        double[] ecef = GeoMath.llhDegToEcef(lat, lon, h);
        double[] llh2 = GeoMath.ecefToLlhDeg(ecef[0], ecef[1], ecef[2]);
        assertEquals(lat, llh2[0], 1e-7);
        assertEquals(lon, llh2[1], 1e-7);
        assertEquals(h,   llh2[2], 0.1);
    }
}
