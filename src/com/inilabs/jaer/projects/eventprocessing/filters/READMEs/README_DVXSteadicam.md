
# Steadicam_DVXplorer (DVXplorer + Bosch BMI160) — What changed

This port removes DAVIS APS dependencies and uses the Bosch BMI160 IMU via your classes in `com.inilabs.jaer.projects.dvxplorer.imu` to stabilize rotation **and** translation for DVS-only streams.

## Files
- `Steadicam_DVXplorer.java` — filter class under `net.sf.jaer.eventprocessing.filter`

## Key changes
1. **DVX-only:** Removed `ApsDvsEvent`/frame code; operates on `PolarityEvent` only.
2. **IMU path:** Overrides `filterPacket(AEPacketRaw)` and parses BMI160 samples with `IMUSample.constructFromAEPacketRaw(...)`. Handles sample boundaries with `IncompleteIMUSampleException`.
3. **Units/filters:** Integrates BMI160 gyro **deg/s** using `getDeltaTimeUs()`; high‑pass filters pan/tilt/roll (τ configurable).
4. **Transform:** Converts pan/tilt (deg) → pixel translations using `radPerPixel = atan(pixel_width_mm / focal_length_mm)`; roll (deg) → radians; applies per‑event R+T about a center of rotation.
5. **Lag compensation:** FIFO aligned by `imuLagMs` to match IMU timing against events.
6. **Output compatibility:** Uses `outputIterator().nextOutput().copyFrom(e)` with fallbacks to `addEvent(e)`/`add(e)` across jAER variants.
7. **Polarity handling:** Enum/boolean‑agnostic getter/setter; optional “flip contrast” preserved.
8. **GUI params:** `electronicStabilizationEnabled`, `flipContrast`, `imuLagMs`, `lensFocalLengthMm`, `highpassTauMsTranslation`, `highpassTauMsRotation`, `transformResetLimitDegrees`, `disableTranslation`, `disableRotation`, `showGrid`, `showTransformRectangle`.

## Install
1. Place `Steadicam_DVXplorer.java` into `jaer/src/net/sf/jaer/eventprocessing/filter/`.
2. Ensure BMI160 classes live under `com/inilabs/jaer/projects/dvxplorer/imu/` on the classpath.
3. Build jAER; select **Steadicam_DVXplorer** in the filter list.

## Chain & tuning
- Typical chain: `DvsNoiseFilter → BackgroundActivityFilter → Steadicam_DVXplorer → tracker`
- Start with `imuLagMs = 0…3`; set `lensFocalLengthMm` to your lens; `τ` 500–1500 ms.

