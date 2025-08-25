# jAER DVXplorer: Ego-motion Compensation + Residual Segmentation (Patched)

This bundle fixes the **"incompatible types: Polarity cannot be converted to boolean"** error by using a version-agnostic polarity check (enum vs boolean).

## Files
- `EgomotionCompensator.java` — rotation-only warper, time-surface stamping, IMU reflection hook. **Polarity handled via `isOn()` helper.**
- `ResidualSegmenter.java` — tile-wise contrast gain; polarity-agnostic.
- `README.md` — setup and parameters.

## Usage
1. Drop files into:
   ```
   jaer/src/com/rjd/jaer/filters/
   ```
2. Build jAER.
3. Filter chain:
   - RefractoryFilter → BackgroundActivityFilter → **EgomotionCompensator** → **ResidualSegmenter** → your tracker.

> If your jAER branch already warps events upstream, you may prefer to place `ResidualSegmenter` on the unwarped branch (duplicate chain) for a true RAW vs COMPENS contrast. Otherwise it still acts as a residual gate using the current rotation.

## Parameters
- `tauMs`, `windowMs`: 10–30 ms typical.
- `tile`: 16 px (8 for very small targets).
- `gainThreshold`: 1.3–1.8 (higher = more aggressive IMO gating).
- Intrinsics: set `fx, fy, cx, cy` to your calibration.

## IMU Notes
Set `useImu=true` in `EgomotionCompensator` to enable gyro integration (reflection). Adjust `gyroScale` to your IMU units.

License: MIT for these two files.