package com.inilabs.jaer.projects.space3d;

public final class WorldGeoConfig {
    public enum CRS { ENU_WGS84 }
    public enum MapProvider { OSM_WEBMERCATOR, SWISSTOPO }
    public enum ElevationProvider { NONE, SRTM30, SWISSTOPO_2M }
    public final double originLatDeg, originLonDeg, originAltM, halfExtentM;
    public final CRS crs;
    public final MapProvider mapProvider;
    public final ElevationProvider elevProvider;
    public final int tileZoom;
    public final double metersPerPixelAtOrigin;
    public WorldGeoConfig(double lat, double lon, double alt, double halfExtentM,
                          CRS crs, MapProvider mapProvider, ElevationProvider elevProvider,
                          int tileZoom, double mppAtOrigin){
        this.originLatDeg=lat; this.originLonDeg=lon; this.originAltM=alt; this.halfExtentM=halfExtentM;
        this.crs=crs; this.mapProvider=mapProvider; this.elevProvider=elevProvider;
        this.tileZoom=tileZoom; this.metersPerPixelAtOrigin=mppAtOrigin;
    }
}