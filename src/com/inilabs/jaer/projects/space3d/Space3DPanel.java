package com.inilabs.jaer.projects.space3d;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XZ viewer (North up) with optional OSM map background that stays coherent with
 * the overlay zoom/pan. The map tile size on screen is dynamically scaled so that
 * meters-per-pixel matches the overlay's pixelsPerMeter.
 *
 * Requirements in Space3D:
 *  - getOriginLatDeg(), getOriginLonDeg(), getHalfExtentM()
 *
 * Axes:
 *  +X = East (right), +Z = North (up). Units are meters.
 */
public class Space3DPanel extends JPanel {
    private final Space3D space;
    private double pixelsPerMeter = 0.5;         // overlay zoom (px/m)
    private double offsetXM = 0.0;               // pan in meters (world X at panel center, East)
    private double offsetZM = 0.0;               // pan in meters (world Z at panel center, North)
    private JLabel statusLabel;                  // optional status readout

    private boolean showLabels = true;
    private boolean showGrid = true;

    // ---- Map layer toggles/params ----
    private boolean mapEnabled = true;
    private int mapZoom = 16; // OSM 1..19 typical
    private final TileCache tileCache = new TileCache(128);

    public Space3DPanel(Space3D space) {
        this.space = space;
        setBackground(Color.white);
        setOpaque(true);
        setPreferredSize(new Dimension(900, 650));

        // Mouse coordinate readout
        MouseMotionAdapter mma = new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                double[] wz = screenToWorld(e.getX(), e.getY());
                setStatus(String.format("E=%.2f m, N=%.2f m", wz[0], wz[1]));
            }
        };
        addMouseMotionListener(mma);
    }

    public void setStatusLabel(JLabel label) { this.statusLabel = label; }
    public void setPixelsPerMeter(double ppm) { this.pixelsPerMeter = Math.max(1e-6, ppm); repaint(); }
    public double getPixelsPerMeter(){ return pixelsPerMeter; }
    public void setOffsetXM(double m){ this.offsetXM = m; repaint(); }
    public void setOffsetZM(double m){ this.offsetZM = m; repaint(); }
    public double getOffsetXM(){ return offsetXM; }
    public double getOffsetZM(){ return offsetZM; }
    public void setShowLabels(boolean v){ this.showLabels = v; repaint(); }
    public void setShowGrid(boolean v){ this.showGrid = v; repaint(); }

    public void setMapEnabled(boolean enabled){ this.mapEnabled = enabled; repaint(); }
    public boolean isMapEnabled(){ return mapEnabled; }
    public void setMapZoom(int zoom){ this.mapZoom = Math.max(1, Math.min(19, zoom)); repaint(); }
    public int getMapZoom(){ return mapZoom; }

    private void setStatus(String s){
        if (statusLabel != null) statusLabel.setText(s);
    }

    /** world->screen mapping */
    private int worldToScreenX(double xm){
        int cx = getWidth()/2;
        return (int)Math.round(cx + (xm - offsetXM) * pixelsPerMeter);
    }
    private int worldToScreenY(double zm){
        int cy = getHeight()/2;
        return (int)Math.round(cy - (zm - offsetZM) * pixelsPerMeter);
    }
    /** screen->world mapping (x,z) */
    private double[] screenToWorld(int sx, int sy){
        int cx = getWidth()/2;
        int cy = getHeight()/2;
        double xm = (sx - cx)/pixelsPerMeter + offsetXM;
        double zm = (cy - sy)/pixelsPerMeter + offsetZM;
        return new double[]{xm, zm};
    }

    // ====== Map math (Web Mercator ground resolution) ======
    private static final double R_EARTH = 6378137.0; // meters
    private static final int TILE_SIZE = 256;        // OSM native px per tile

    private double originLat(){ return space.getOriginLatDeg(); }
    private double originLon(){ return space.getOriginLonDeg(); }

    /** meters-per-pixel of Web Mercator at this zoom & latitude */
    private double mapMetersPerPixel(int zoom, double latDeg){
        double latRad = Math.toRadians(latDeg);
        return Math.cos(latRad) * 2.0 * Math.PI * R_EARTH / (TILE_SIZE * Math.pow(2.0, zoom));
    }

    private double worldToLat(double zMetersNorth){
        double lat0 = originLat();
        return lat0 + (zMetersNorth / R_EARTH) * 180.0/Math.PI;
    }
    private double worldToLon(double xMetersEast, double latDeg){
        double lon0 = originLon();
        double latRad = Math.toRadians(latDeg);
        double dlon = (xMetersEast / (R_EARTH * Math.cos(latRad))) * 180.0/Math.PI;
        return lon0 + dlon;
    }
    private double[] enuToLatLon(double xMetersEast, double zMetersNorth){
        double lat = worldToLat(zMetersNorth);
        double lon = worldToLon(xMetersEast, lat);
        return new double[]{lat, lon};
    }
    private static double lonToTileX(double lonDeg, int zoom){
        return (lonDeg + 180.0) / 360.0 * (1<<zoom);
    }
    private static double latToTileY(double latDeg, int zoom){
        double latRad = Math.toRadians(latDeg);
        return (1.0 - Math.log(Math.tan(latRad) + 1.0/Math.cos(latRad)) / Math.PI) / 2.0 * (1<<zoom);
    }

    /** Draw map tiles covering the viewport, scaled to match overlay pixelsPerMeter */
    private void drawMap(Graphics2D g2){
        if (!mapEnabled) return;

        int w = getWidth(), h = getHeight();

        // Center ENU at panel center
        double centerX = offsetXM;
        double centerZ = offsetZM;
        double[] centerLla = enuToLatLon(centerX, centerZ);
        int z = mapZoom;

        // Compute scale factor so that map's m/px equals overlay's m/px
        double mpp_map = mapMetersPerPixel(z, centerLla[0]);   // meters per native map pixel
        double mpp_overlay = 1.0 / pixelsPerMeter;             // meters per screen pixel for overlay
        double s = mpp_map / mpp_overlay;                      // map native px → on-screen px multiplier

        // Tile indices at center
        double centerTileX = lonToTileX(centerLla[1], z);
        double centerTileY = latToTileY(centerLla[0], z);

        // Pixel offset of panel center within the center tile, IN NATIVE TILE PIXELS
        double fracX = centerTileX - Math.floor(centerTileX);
        double fracY = centerTileY - Math.floor(centerTileY);
        double centerOffsetPxX = fracX * TILE_SIZE; // native px
        double centerOffsetPxY = fracY * TILE_SIZE; // native px

        // How many tiles to cover viewport: use scaled tile size on screen
        double tileScreen = TILE_SIZE * s;
        int tilesLeft  = (int)Math.ceil((w/2.0 + centerOffsetPxX * s) / tileScreen) + 1;
        int tilesRight = (int)Math.ceil((w/2.0 + (TILE_SIZE - centerOffsetPxX) * s) / tileScreen) + 1;
        int tilesUp    = (int)Math.ceil((h/2.0 + centerOffsetPxY * s) / tileScreen) + 1;
        int tilesDown  = (int)Math.ceil((h/2.0 + (TILE_SIZE - centerOffsetPxY) * s) / tileScreen) + 1;

        int centerTileXI = (int)Math.floor(centerTileX);
        int centerTileYI = (int)Math.floor(centerTileY);
        int maxXY = (1<<z);

        for (int dy = -tilesUp; dy <= tilesDown; dy++){
            for (int dx = -tilesLeft; dx <= tilesRight; dx++){
                int tx = centerTileXI + dx;
                int ty = centerTileYI + dy;
                if (tx < 0 || ty < 0 || tx >= maxXY || ty >= maxXY) continue;

                // Screen draw position (top-left), in SCREEN pixels
                int drawX = (int)Math.round(w/2.0 - centerOffsetPxX * s + dx * tileScreen);
                int drawY = (int)Math.round(h/2.0 - centerOffsetPxY * s + dy * tileScreen);

                BufferedImage tile = tileCache.get(z, tx, ty);
                if (tile == null){
                    tile = fetchTile(z, tx, ty);
                    tileCache.put(z, tx, ty, tile);
                }
                if (tile != null){
                    g2.drawImage(tile, drawX, drawY, (int)Math.ceil(tileScreen), (int)Math.ceil(tileScreen), null);
                } else {
                    g2.setColor(new Color(240,240,240));
                    g2.fillRect(drawX, drawY, (int)Math.ceil(tileScreen), (int)Math.ceil(tileScreen));
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(drawX, drawY, (int)Math.ceil(tileScreen), (int)Math.ceil(tileScreen));
                }
            }
        }

        // Attribution
        g2.setColor(new Color(255,255,255,200));
        g2.fillRect(8, h-26, 260, 18);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        g2.drawString("© OpenStreetMap contributors", 12, h-13);
    }

    private static BufferedImage fetchTile(int z, int x, int y){
        String urlStr = String.format("https://tile.openstreetmap.org/%d/%d/%d.png", z, x, y);
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Space3DPanel/1.0 (research; contact: youremail@example.com)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                return ImageIO.read(conn.getInputStream());
            }
        } catch (IOException ignored) {}
        return null;
    }

    /** Simple LRU cache for tiles */
    private static final class TileCache {
        private final int capacity;
        private final LinkedHashMap<String, BufferedImage> map;
        TileCache(int capacity){
            this.capacity = capacity;
            this.map = new LinkedHashMap<String, BufferedImage>(capacity, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > TileCache.this.capacity;
                }
            };
        }
        String key(int z, int x, int y){ return z+"/"+x+"/"+y; }
        BufferedImage get(int z,int x,int y){ return map.get(key(z,x,y)); }
        void put(int z,int x,int y, BufferedImage img){ if (img!=null) map.put(key(z,x,y), img); }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Map layer first (now scaled to match overlay)
        drawMap(g2);

        // Grid & axes on top
        if (showGrid) drawGrid(g2);
        drawAxes(g2);

        // Draw agents
        for (Map.Entry<String, Agent3DInterface> e : space.viewAgents().entrySet()) {
            Agent3DInterface a = e.getValue();
            Space3D.Vec3 p = a.getPositionDVX(); // (x,y,z), we use x and z
            int sx = worldToScreenX(p.x);
            int sy = worldToScreenY(p.z);
            drawAgent(g2, a, sx, sy);
            if (showLabels) {
                g2.setColor(Color.black);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
                g2.drawString(a.getKey(), sx + 8, sy - 8);
            }
        }

        // Legend
        drawLegend(g2);
        g2.dispose();
    }

    private void drawGrid(Graphics2D g2){
        int w = getWidth(), h = getHeight();
        // choose a "nice" grid step in meters based on pixelsPerMeter
        double targetPx = 80.0; // aim ~80 px between grid lines
        double stepM = niceStep(targetPx / pixelsPerMeter);
        // draw light grid
        g2.setColor(new Color(230,230,230, 180));
        for (double xm = -1e6; xm <= 1e6; xm += stepM){
            int sx = worldToScreenX(xm);
            if (sx < 0 || sx > w) continue;
            g2.drawLine(sx, 0, sx, h);
        }
        for (double zm = -1e6; zm <= 1e6; zm += stepM){
            int sy = worldToScreenY(zm);
            if (sy < 0 || sy > h) continue;
            g2.drawLine(0, sy, w, sy);
        }

        // annotate a few ticks near axes
        g2.setColor(new Color(50,50,50, 200));
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        for (double xm = -1e6; xm <= 1e6; xm += stepM){
            int sx = worldToScreenX(xm);
            int sy = worldToScreenY(0);
            if (sx < 0 || sx > w || sy < 0 || sy > h) continue;
            g2.drawLine(sx, sy-3, sx, sy+3);
            g2.drawString(String.format("%.0f", xm), sx+2, sy-4);
        }
        for (double zm = -1e6; zm <= 1e6; zm += stepM){
            int sy = worldToScreenY(zm);
            int sx = worldToScreenX(0);
            if (sx < 0 || sx > w || sy < 0 || sy > h) continue;
            g2.drawLine(sx-3, sy, sx+3, sy);
            g2.drawString(String.format("%.0f", zm), sx+6, sy-2);
        }
    }

    private void drawAxes(Graphics2D g2){
        int w = getWidth(), h = getHeight();
        int x0 = worldToScreenX(0);
        int z0 = worldToScreenY(0);
        g2.setColor(new Color(80,80,80));
        g2.setStroke(new BasicStroke(1.5f));
        // X axis (East, horizontal)
        g2.drawLine(0, z0, w, z0);
        // Z axis (North, vertical)
        g2.drawLine(x0, 0, x0, h);

        // Labels
        g2.setColor(Color.black);
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g2.drawString("E (m)", w - 50, z0 - 6);
        g2.drawString("N (m)", x0 + 6, 16);
    }

    private void drawAgent(Graphics2D g2, Agent3DInterface a, int sx, int sy){
        final int r = 5;
        switch (a.getType()){
            case DVXPLORER:
                g2.setColor(new Color(30,144,255));
                g2.fillRect(sx - r, sy - r, 2*r, 2*r);
                g2.setColor(Color.black);
                g2.drawRect(sx - r, sy - r, 2*r, 2*r);
                break;
            case TARGET:
                g2.setColor(new Color(220,20,60));
                g2.fillOval(sx - r, sy - r, 2*r, 2*r);
                g2.setColor(Color.black);
                g2.drawOval(sx - r, sy - r, 2*r, 2*r);
                break;
            case WAYPOINT:
                g2.setColor(new Color(34,139,34));
                Polygon tri = new Polygon();
                tri.addPoint(sx, sy - r);
                tri.addPoint(sx - r, sy + r);
                tri.addPoint(sx + r, sy + r);
                g2.fillPolygon(tri);
                g2.setColor(Color.black);
                g2.drawPolygon(tri);
                break;
        }
    }

    private void drawLegend(Graphics2D g2){
        int x = 10, y = 10;
        int box = 12, gap = 6, line = 16;
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        // DVX
        g2.setColor(new Color(30,144,255)); g2.fillRect(x, y, box, box);
        g2.setColor(Color.black); g2.drawRect(x, y, box, box);
        g2.drawString("DVXplorer", x + box + gap, y + box - 2);
        y += line;
        // Target
        g2.setColor(new Color(220,20,60)); g2.fillOval(x, y, box, box);
        g2.setColor(Color.black); g2.drawOval(x, y, box, box);
        g2.drawString("Target", x + box + gap, y + box - 2);
        y += line;
        // Waypoint
        g2.setColor(new Color(34,139,34));
        Polygon tri = new Polygon();
        tri.addPoint(x + box/2, y);
        tri.addPoint(x, y + box);
        tri.addPoint(x + box, y + box);
        g2.fillPolygon(tri);
        g2.setColor(Color.black); g2.drawPolygon(tri);
        g2.drawString("Waypoint", x + box + gap, y + box - 2);
    }

    /** choose a "nice" step size (1,2,5 × 10^k) >= v */
    private static double niceStep(double v){
        double base = Math.pow(10, Math.floor(Math.log10(Math.max(1e-9, v))));
        double[] mult = {1, 2, 5, 10};
        for (double m : mult){ if (base*m >= v - 1e-12) return base*m; }
        return base*10;
    }
}
