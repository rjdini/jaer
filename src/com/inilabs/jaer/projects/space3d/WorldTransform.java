package com.inilabs.jaer.projects.space3d;

import java.awt.Point;

/** Immutable world→screen transform for Space3D top-down rendering. */
public final class WorldTransform {
    private final int centerX, centerY;
    private final double pixelsPerMeter;

    public WorldTransform(int centerX, int centerY, double pixelsPerMeter){
        this.centerX = centerX;
        this.centerY = centerY;
        this.pixelsPerMeter = pixelsPerMeter;
    }

    public int centerX(){ return centerX; }
    public int centerY(){ return centerY; }
    public double ppm(){ return pixelsPerMeter; }

    public int toScreenX(double worldX){ return centerX + (int)Math.round(worldX * pixelsPerMeter); }
    public int toScreenY(double worldZ){ return centerY - (int)Math.round(worldZ * pixelsPerMeter); }

    public Point toScreen(double worldX, double worldZ){ return new Point(toScreenX(worldX), toScreenY(worldZ)); }
}




