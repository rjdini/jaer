package com.inilabs.jaer.projects.eventprocessing.filters;

import net.sf.jaer.graphics.FrameAnnotater;

import com.inilabs.jaer.projects.dvxplorer.imu.BoschImuPoseProvider;
import com.inilabs.jaer.projects.dvxplorer.imu.PoseSE3;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * Optional overlay: IMU status and simple GL marker rendering.
 */
public class DVXOverlayRenderer implements FrameAnnotater {

    private final SmallTargetTrackerFilter tracker;
    private final BoschImuPoseProvider imu;
    private final int sx;
    private final int sy;

    private boolean annotationEnabled = true;

    public DVXOverlayRenderer(SmallTargetTrackerFilter tracker,
                              BoschImuPoseProvider imu,
                              int sx,
                              int sy) {
        this.tracker = tracker;
        this.imu = imu;
        this.sx = sx;
        this.sy = sy;
    }

    @Override
    public void annotate(GLAutoDrawable drawable) {
        if (!annotationEnabled) {
            return;
        }
        final GL2 gl = drawable.getGL().getGL2();

        gl.glLineWidth(1f);
        gl.glColor3f(0f, 1f, 0f);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2f(0, 0);
        gl.glVertex2f(sx - 1, 0);
        gl.glVertex2f(sx - 1, sy - 1);
        gl.glVertex2f(0, sy - 1);
        gl.glEnd();

        if (imu != null) {
            PoseSE3 pose = imu.getPoseAtTimestamp(System.nanoTime() / 1000L);
            if (pose != null) {
                int x = sx / 2;
                int y = sy / 2;
                gl.glColor3f(1f, 1f, 1f);
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(x - 5, y);
                gl.glVertex2f(x + 5, y);
                gl.glVertex2f(x, y - 5);
                gl.glVertex2f(x, y + 5);
                gl.glEnd();
            }
        }

        if (tracker != null) {
            gl.glPointSize(4f);
            gl.glColor3f(0f, 1f, 1f);
            gl.glBegin(GL2.GL_POINTS);
            for (SmallTargetTrackerFilter.Track tr : tracker.getTracks()) {
                gl.glVertex2d(tr.x, tr.y);
            }
            gl.glEnd();
        }
    }

    @Override
    public boolean isAnnotationEnabled() {
        return annotationEnabled;
    }

    @Override
    public void setAnnotationEnabled(boolean annotationEnabled) {
        this.annotationEnabled = annotationEnabled;
    }
}
