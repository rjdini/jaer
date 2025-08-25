# DVXplorer Small Target Pipeline + Bosch IMU Pose Provider

This package is **ready-to-go** for DVXplorer + Bosch BMI160.

## Contents

### Filters
- `EgomotionCompensatorIMU`  
  Uses an `ImuPoseProvider` to warp events with planar homography.  
  Set intrinsics (fx, fy, cx, cy), depth (z0Meters), reference window (refWindowUs).  
  Wire IMU provider via `setImu(new BoschImuPoseProvider())`.

- `ResidualSmallTargetDetector`  
  Clusters stabilized events (~10 px targets) and tests x–y–t plane coherence.  
  Use `setTracker(new SmallTargetTracker(...))` to wire tracker.

- `SmallTargetTracker`  
  Greedy-assignment constant-velocity tracker. Keeps track IDs, positions, velocities.

### IMU
- `BoschImuPoseProvider` — definitive provider, implements `ImuPoseProvider`.  
  - `addSample(IMUSample s)` to ingest BMI160 samples.  
  - `getPoseAtTimestamp(long tsUs)` returns `PoseSE3 (R,t)`.  
  - Mahony AHRS (acc correction) + ZUPT translation.  
  - Supports extrinsics (IMU→camera).

- `ImuPoseProvider` — interface.  
- `PoseSE3` — SE3 container.  
- `SimpleImuPoseProvider` — stub provider (for wiring/tests).

### How to use
1. Decode IMU events to `IMUSample`.  
2. Feed to provider: `bosch.addSample(sample);`.  
3. Plug provider into filter:  
   ```java
   EgomotionCompensatorIMU ego = new EgomotionCompensatorIMU(chip);
   ego.setImu(bosch);
   ```  
4. Build filter chain:  
   - RefractoryFilter (200 µs)  
   - EgomotionCompensatorIMU  
   - ResidualSmallTargetDetector (Δt=20ms, ROI=15, Nmin=30)  
   - SmallTargetTracker  

### Notes
- World frame: +X right, +Y up, +Z forward; gravity = [0,-g,0].  
- R in PoseSE3 maps world→camera.  
- Yaw drifts without magnetometer; roll/pitch stabilized by gravity.  
- Translation integration is short-term only; fuse with vision for long runs.

