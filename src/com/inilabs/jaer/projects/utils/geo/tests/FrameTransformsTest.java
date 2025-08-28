package com.inilabs.jaer.projects.utils.geo.tests;

import com.inilabs.jaer.projects.utils.geo.FrameTransforms;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FrameTransforms tests aligned with the existing polar math:
 * enuToBody applies the inverse rotation (R^T). With positive yaw (about +y/Up),
 * a forward/North ENU vector maps to NEGATIVE East in body.
 */
public class FrameTransformsTest {

    @Test
    public void testYaw90NorthMapsToNegativeEastInBody() {
        double[] enu = new double[]{0, 0, 1};              // ENU North
        double[] body = FrameTransforms.enuToBody(enu, 90.0, 0.0, 0.0); // +90 yaw about Up
        assertEquals(-1.0, body[0], 1e-9);                  // x (East) becomes -1
        assertEquals( 0.0, body[1], 1e-9);                  // y (Up) unchanged
        assertEquals( 0.0, body[2], 1e-9);                  // z (North) to 0
    }

    @Test
    public void testYawDoesNotAffectUpVector() {
        double[] up = new double[]{0, 1, 0};                // ENU Up
        double[] body = FrameTransforms.enuToBody(up, 123.0, 0.0, 0.0);
        assertEquals(0.0, body[0], 1e-9);
        assertEquals(1.0, body[1], 1e-9);
        assertEquals(0.0, body[2], 1e-9);
    }

    @Test
    public void testBodyToEnuIsInverseOfEnuToBody() {
        double[] enu = new double[]{0.3, 0.4, -0.5};
        double yaw=20, pitch=-15, roll=30;
        double[] body = FrameTransforms.enuToBody(enu, yaw, pitch, roll);
        double[] enu2 = FrameTransforms.bodyToEnu(body, yaw, pitch, roll);
        assertEquals(enu[0], enu2[0], 1e-9);
        assertEquals(enu[1], enu2[1], 1e-9);
        assertEquals(enu[2], enu2[2], 1e-9);
    }
}
