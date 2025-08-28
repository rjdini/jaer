# Geo Utilities (WGS-84, ECEF, ENU & Frame Transforms)

This package centralizes **all geodesy and frame math** used by Birdland so we don’t duplicate logic or drift on units/axes. It contains:

- `GeoMath.java` — WGS-84 ⇄ ECEF ⇄ ENU conversions  
- `FrameTransforms.java` — Yaw/Pitch/Roll rotation utilities for Space3D/Polar

## Why this exists

- **Single source of truth** for coordinate math (no copy-paste formulas sprinkled around).
- **Consistency** across Space3D, Polar, Trackers, Targets.
- **Safety**: clear units/axes; degree-only public APIs.
- **Refactorable**: callers can migrate incrementally without behavior changes.

## Canonical conventions

- **World frame (Space3D local):** **ENU** with  
  `x = EAST`, `y = UP`, `z = NORTH`  
  This matches existing polar math that computes  
  `azimuth = atan2(x, z)` and `elevation = atan2(y, sqrt(x²+z²))`.

- **Angles:** **degrees** in all public methods.

- **Yaw/Pitch/Roll (camera/tracker):**  
  - **yaw** about `+y` (UP)  
  - **pitch** about `+x` (EAST)  
  - **roll** about `+z` (NORTH)  
  Composite rotation: `R = Rz(roll) * Rx(pitch) * Ry(yaw)`.

- **GPS origin:** Single authority is **Space3D** (lat0, lon0, alt0).  
  Utilities take the origin as parameters; they never store global state.

## Responsibilities split

- **Space3D & agents**: store positions (in ENU) and define the GPS origin.  
- **FieldOfView**: stays GPS-agnostic; owns chip/lens and orientation (degrees).  
- **Executive / Tracker glue**: if GPS is needed, convert via `GeoMath` first, then pass angles to FOV.

## API sketch

### `GeoMath`
- `llhDegToEcef(latDeg, lonDeg, h) -> double[3]`
- `ecefToLlhDeg(x, y, z) -> double[3]`
- `ecefToEnu(x, y, z, lat0, lon0, h0) -> double[3]`  *(Space3D ENU: x=E, y=U, z=N)*
- `enuToEcef(ex, uy, nz, lat0, lon0, h0) -> double[3]`
- `llhDegToEnu(lat, lon, h, lat0, lon0, h0) -> double[3]`
- `enuToLlhDeg(ex, uy, nz, lat0, lon0, h0) -> double[3]`

### `FrameTransforms`
- `Rx(deg), Ry(deg), Rz(deg)` → 3×3 matrices
- `R_ypr(yawDeg, pitchDeg, rollDeg)` → 3×3 composite
- `enuToBody(enu, yaw, pitch, roll) -> double[3]` *(inverse rotation)*
- `bodyToEnu(body, yaw, pitch, roll) -> double[3]`

All methods are **static** and **stateless**.

## Usage examples

**Convert GPS to Space3D local ENU** (e.g., when placing a target):
```java
double[] enu = GeoMath.llhDegToEnu(latDeg, lonDeg, altM,
                                   world.lat0Deg, world.lon0Deg, world.alt0M);
// enu[0]=E, enu[1]=U, enu[2]=N
agent.setPosition(new Vec3(enu[0], enu[1], enu[2]));
```

**Rotate ENU vector into tracker (camera) frame** for polar math:
```java
double[] body = FrameTransforms.enuToBody(
    new double[]{ex, uy, nz}, yawDeg, pitchDeg, rollDeg);
// az = atan2(body_x, body_z); el = atan2(body_y, hypot(body_x, body_z));
```

**Keep FOV GPS-free**:
```java
// Executive computes position/origin via GeoMath if needed,
// but passes only angles (degrees) into FOV:
fov.setPose(yawDeg, rollDeg, pitchDeg);
```

## Migration plan (safe/incremental)

1. **Add utils** (done).  
2. **New code** must use only `GeoMath` / `FrameTransforms`.  
3. **Refactor hot spots**:
   - Replace any inline WGS-84/ECEF/ENU math with `GeoMath`.
   - Replace ad-hoc yaw/pitch/roll code with `FrameTransforms`.
   - Keep FOV APIs degree-based; don’t pipe GPS into FOV.
4. **Delete duplicated math** once callers are migrated.

## Tests to keep us honest

- **Round-trips**  
  - LLH → ECEF → LLH (within cm–m tolerance depending on altitude).  
  - ENU → ECEF → ENU (within floating-point epsilon).  
- **Axes sanity**  
  - A pure **North** ENU vector rotated by **yaw=90°** becomes pure **East** in body.  
  - A pure **Up** ENU vector is invariant to yaw; affected only by pitch/roll.  
- **Units**  
  - Verify all public angle parameters are **degrees**.

## Pitfalls & gotchas

- Don’t assume **y=Up** unless you’re in Space3D’s ENU—stick to the stated axes.  
- Don’t mix radians in public calls; convert at the boundary if you must.  
- Never duplicate the GPS origin; always read it from **Space3D**.

## Performance & thread-safety

- All methods are pure; no allocations beyond small arrays.  
- Safe for concurrent use. Inline where needed; JVM will optimize tiny matrices.

## Future extensions

- Datum handling (if non-WGS84 is ever required).  
- Velocity/attitude wrappers (if we add IMU fusion).  
- Batch transforms for multi-target pipelines.
