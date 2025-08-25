# BMI160 Patch — What I changed (and why)

This README documents the changes I made to align your IMU code with the **Bosch Sensortec BMI160** used in DVXplorer, replacing assumptions that matched older InvenSense MPU‑61xx parts.

## Summary of files
Patched (drop-in replacements, for original package: eu.seebetter.ini.chips.davis.imu ):
- `IMUSample.java`
- `ImuGyroScale.java`
- `ImuAccelScale.java`
- `ImuControl.java`
- `ImuControlPanel.java`
- `IMUSampleType.java` (unchanged)

---

## 1) Temperature conversion (BMI160)

**Why:** Your original code used MPU‑61xx constants (`°C = 35 + raw/340`), producing wrong temperatures on DVXplorer.

**Change:** BMI160 uses a different format:
- **Scale:** `1/512 °C per LSB` (i.e., 2^-9 K/LSB)
- **Offset:** **23 °C** at raw = 0

**Resulting formula:**
```
temperatureDegC = 23 + raw / 512.0
```
Patched constants in `IMUSample.java`:
- `temperatureScaleFactorDegCPerLsb = 1f/512f`
- `temperatureOffsetDegC = 23`

---

## 2) Gyroscope scale factors (BMI160)

**Why:** Your gyro sensitivity factors assumed an InvenSense mapping (e.g., 65.5 LSB/(°/s)), which mis-scales BMI160 data.

**Change:** Updated LSB/(°/s) per full-scale range to BMI160 values and normalized defaults:
- ±250  °/s  → **131.2**
- ±500  °/s  → **65.6**
- ±1000 °/s  → **32.8**
- ±2000 °/s  → **16.4**

**Files/fields:**
- `ImuGyroScale.java`: enum entries now use BMI160 factors above.
- `IMUSample.java`: default `gyroSensitivityScaleFactorDegPerSecPerLsb = 1/32.8f` (assuming ±1000 °/s default).

**Note:** If you later select a different FS range at runtime, the scale must follow the selected enum entry (as it already does in your control code).

---

## 3) Accelerometer scale factors

**Why:** Verify LSB/g mapping. For BMI160 the accelerometer LSB/g are:
- ±2 g  → 16384
- ±4 g  → 8192
- ±8 g  → 4096
- ±16 g → 2048

**Change:** Your enum already matches these. I only updated comments to explicitly reference BMI160. No code logic changes required.

---

## 4) Comments, defaults, and naming hygiene

**Why:** The classes referenced “Invensense MPU‑6150/6100A” and listed their scales; this is misleading for DVXplorer/BMI160.

**Change:** Updated class headers and comments to say **Bosch BMI160**, and set sensible defaults:
- Gyro default range treated as **±1000 °/s** (32.8 LSB/(°/s)) in `IMUSample.java`.
- Accel default range **±4 g** (8192 LSB/g) unchanged.

---

## 5) API stability (no functional UI changes)

- **No method signatures changed.** UI panels (`ImuControlPanel`) and control wiring (`ImuControl`) continue to work.
- Tooltips/comments updated to reflect **BMI160 ODR/BW** semantics instead of MPU DLPF wording.
- If your lower layer already maps to BMI160 registers (`acc_odr`, `acc_bwp`, `gyr_odr`, `gyr_bwp`, `gyr_range`), the numbers now align with the reported physical units.

---

## 6) Optional: expose ±125 °/s

BMI160 supports **±125 °/s** with **262.4 LSB/(°/s)**. I did **not** add this FS to avoid changing enum ordinals and user prefs. If you want it, I can add a new enum constant and wire it through the control panel.

---

## 7) Downstream integration notes (ego-motion compensation)

- Ensure gyro is converted to **rad/s** before integration:
  ```java
  // gx_deg_s, dt_s
  rx += gx_deg_s * Math.PI / 180.0 * dt_s;
  ```
- Use true **Δt** between samples (don’t assume fixed rate), because DVXplorer can burst IMU events at kHz.
- When validating, check that small hand rotations produce plausible image-plane warps and that residuals shrink after compensation.

---

## 8) Sanity checks / How to verify

1. **Static camera:** Temperature should sit near room temp; gyro mean near zero; accel near 1 g on Z (depending on orientation).
2. **Known rotations:** Introduce a slow yaw and verify reported `gyrZ` is near the motion you applied.
3. **Range changes:** Switch gyro FS in UI and confirm the numeric outputs scale appropriately (no jump in physical units).
4. **Unit test:** Log raw IMU and converted values; compare against a trusted BMI160 driver or datasheet expectations.

---

## 9) Forward-looking (cleanest path)

- If you maintain both InvenSense and Bosch variants, put scale/offset tables behind a **chip ID** switch (BMI160 vs MPU‑6xxx) so the same codebase can target multiple sensors.
- Consider exposing **±125 °/s** and BMI160 bandwidth/ODR presets directly in the UI to simplify field tuning.

