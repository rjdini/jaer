package com.inilabs.jaer.projects.dvxplorer.imu;

/**
 * Returns the camera pose (SE3) for an event timestamp.
 */
public interface ImuPoseProvider {
    PoseSE3 getPoseAtTimestamp(long timestampUs);
}
