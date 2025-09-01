# Geodesy Design & Usage (exec + space3d)

## Core principle
All **geodesy belongs to `Space3D`**. The 3D world is the single source of truth for:
- **Origin** (lat, lon, alt in WGS‑84)
- **Extent** (half-extent in meters, defining the ENU box)
- **CRS** (local ENU tangent plane vs other projections)
- **Map/Elevation providers** (OSM, SwissTopo, SRTM)
- **Rendering scale** (zoom, meters-per-pixel)

Everything else — OSM tiles, DEM resampling, GUIs — derives its configuration from the `Space3D` world so map/elevation never drift from simulation geometry.

---

## Implementation approach

### 1) `WorldGeoConfig`
Immutable value object that encapsulates geodesy & rendering scale:

```java
public final class WorldGeoConfig {
    public enum CRS { ENU_WGS84 }
    public enum MapProvider { OSM_WEBMERCATOR, SWISSTOPO }
    public enum ElevationProvider { NONE, SRTM30, SWISSTOPO_2M }

    public final double originLatDeg, originLonDeg, originAltM;
    public final double halfExtentM;
    public final CRS crs;
    public final MapProvider mapProvider;
    public final ElevationProvider elevProvider;
    public final int tileZoom;
    public final double metersPerPixelAtOrigin;
}
```

### 2) `WorldGeoRegistry` (non-invasive binding)
If your current `Space3D` does **not** expose `getGeo()/setGeo()`, use:

```java
WorldGeoRegistry.set(world, geo);
WorldGeoConfig cfg = WorldGeoRegistry.require(world);
WorldGeoRegistry.clear(world);
```

This binds `WorldGeoConfig` to any `Space3D` via a `WeakHashMap`, avoiding changes to `Space3D`’s API.

### 3) Layers derive from `Space3D`
Map and elevation layers configure themselves from `Space3D`:

```java
OSMTopoLayer.ensureCoverage(world);
ElevationGrid.ensureCoverage(world);
OSMTopoLayer.configureFromWorld(world);
ElevationGrid.configureFromWorld(world);
```

They call `WorldGeoRegistry.require(world)` internally, so they can’t accumulate their own out-of-sync state.

### 4) CRS & scale invariants
- **CRS:** World uses ENU (meters) about the origin (WGS‑84). Conversions live in `GeoTransforms`.
- **Meters-per-pixel:** Derived from zoom and origin latitude → locks ground texture scale to ENU grid.
- **Coverage:** `ensureCoverage()` validates that zoom + extent cover the ENU box (advisory checks).
- **Elevation alignment:** DEM is resampled onto the ENU ground plane, not free-running.

---

## Startup workflow (Executive)
```java
Space3D world = new Space3D();
Space3DRegistry.set(world);

// Zurich defaults
double lat = 47.3769, lon = 8.5417, alt = 408.0;
int zoom = 18;
double mpp = GeoTransforms.metersPerPixelAt(lat, zoom);

WorldGeoConfig geo = new WorldGeoConfig(
    lat, lon, alt,
    world.getHalfExtentM(),
    WorldGeoConfig.CRS.ENU_WGS84,
    WorldGeoConfig.MapProvider.OSM_WEBMERCATOR,
    WorldGeoConfig.ElevationProvider.SRTM30,
    zoom,
    mpp
);
WorldGeoRegistry.set(world, geo);

// Build agents/targets
worldInitializer.init(world);

// Derive views from world
OSMTopoLayer.ensureCoverage(world);
ElevationGrid.ensureCoverage(world);
OSMTopoLayer.configureFromWorld(world);
ElevationGrid.configureFromWorld(world);
```

---

## Diagram

### ASCII (always renders)
```
+--------------------+
|     Executive      |
|  (boot sequence)   |
+---------+----------+
          |
          v
+--------------------+        binds geodesy        +---------------------+
|      Space3D       | --------------------------> |   WorldGeoRegistry  |
|  (world state)     |   (WorldGeoConfig for this  |  (Space3D -> Geo)   |
+----+-----------+---+            Space3D)         +----------+----------+
     |           |                                     ^       ^
     |           | calls                               |       |
     v           v                                     |       |
+---------+  +--------+                                |       |
|  OSM    |  |  DEM   | <------------------------------+       |
|  Layer  |  | Layer  |    WorldGeoConfig is the single source         |
+----+----+  +---+----+    of truth for map/elevation config ----------+
     |           |
     v           v
+------------------------------+
| GUIs / Renderers (AE/JAER)   |
+------------------------------+
```

### Mermaid (if your viewer supports it)
```mermaid
flowchart TD
    E[Executive] --> W[Space3D]
    W -- binds --> R[WorldGeoRegistry<br/>(Space3D → WorldGeoConfig)]
    R -. provides .-> M[OSMTopoLayer]
    R -. provides .-> D[ElevationGrid]
    M --> G[GUIs/Renderers (AE/JAER)]
    D --> G
```

---

## Practical notes
- **Distortion:** For ~600 m spans at Zurich, Web Mercator distortion is < 0.1% — negligible.
- **Zoom selection:** `zoom=18` ≈ 0.40 m/px at Zurich; good for 300 m half-extent. Use `GeoTransforms.metersPerPixelAt(lat, z)`.
- **Caching:** Cache OSM tiles/DEM by `(lat,lon,zoom,extent)` to avoid stalls.
- **Extensibility:** Add presets in `WorldPresets` (origin/provider/elevation), pass via `Executive.withWorld(...)`.

---

## Why this prevents drift
- Single config → one origin, one extent, one zoom, one CRS.
- All views re-derive from `Space3D` on init/refresh.
- Changing world parameters invalidates and reconfigures layers deterministically.
