package com.inilabs.jaer.projects.polarspace;

import com.inilabs.jaer.projects.space3d.Agent3DInterface;
import com.inilabs.jaer.projects.space3d.Space3D;
import com.inilabs.jaer.projects.space3d.api.AgentPolarDrawable;
import com.inilabs.jaer.projects.util.AgentColors;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class PolarDrawableAdapter implements Drawable {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private final Agent3DInterface agent;
    private final Supplier<Space3D.Vec3> trackerPos;
    private final Supplier<double[]> trackerYprDeg;

    private final String key;
    private final int id;

    private Color color;
    private float sizeDeg = 2.0f;

    private float azDeg = 0f, elDeg = 0f;

    private float azimuthScale = 1.0f, elevationScale = 1.0f;
    private float azimuthHeading = 0f, elevationHeading = 0f;
    private int centerX = 0, centerY = 0;

    private boolean drawLabel = true;
    private int labelDX = 8, labelDY = -8;
    private Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    public PolarDrawableAdapter(Agent3DInterface agent,
                                Supplier<Space3D.Vec3> trackerPos,
                                Supplier<double[]> trackerYprDeg) {
        this.agent = Objects.requireNonNull(agent);
        this.trackerPos = Objects.requireNonNull(trackerPos);
        this.trackerYprDeg = Objects.requireNonNull(trackerYprDeg);
        this.key = "polarAdapter:" + agent.getKey();
        this.id = SEQ.getAndIncrement();
        this.color = AgentColors.colorForKey(agent.getKey());
    }

    public PolarDrawableAdapter color(Color c){ if (c!=null) this.color = c; return this; }
    public PolarDrawableAdapter sizeDeg(float s){ this.sizeDeg = Math.max(0.1f, s); return this; }
    public PolarDrawableAdapter label(boolean on){ this.drawLabel = on; return this; }
    public PolarDrawableAdapter labelOffset(int dx, int dy){ this.labelDX = dx; this.labelDY = dy; return this; }
    public PolarDrawableAdapter labelFont(Font f){ if (f!=null) this.labelFont = f; return this; }

    @Override public String getKey() { return key; }
    @Override public int getId() { return id; }
    @Override public void showPath(boolean yes) {}
    @Override public void setSize(float sizeDegrees) { this.sizeDeg = sizeDegrees; }
    @Override public float getSize() { return sizeDeg; }
    @Override public void setColor(Color c) { if (c!=null) this.color = c; }
    @Override public Color getColor() { return color; }
    @Override public void setParentCallback(java.util.function.BiConsumer<ActionType,String> cb) {}
    @Override public long getLifetime() { return Long.MAX_VALUE; }
    @Override public boolean isExpired() { return false; }
    @Override public boolean isOrphaned() { return false; }
    @Override public void close() {}

    @Override
    public void onTransformChanged(float azScale, float elScale,
                                   float azHead, float elHead,
                                   int cx, int cy) {
        this.azimuthScale = azScale;
        this.elevationScale = elScale;
        this.azimuthHeading = azHead;
        this.elevationHeading = elHead;
        this.centerX = cx;
        this.centerY = cy;
    }

    @Override
    public void draw(Graphics g) {
        Space3D.Vec3 tp = trackerPos.get();
        Space3D.Vec3 ap = agent.getPosition3D();
        if (tp == null || ap == null) return;

        double rx = ap.x - tp.x, ry = ap.y - tp.y, rz = ap.z - tp.z;
        double[] ypr = trackerYprDeg.get();
        double yaw = (ypr != null && ypr.length>0) ? Math.toRadians(ypr[0]) : 0.0;
        double pitch = (ypr != null && ypr.length>1) ? Math.toRadians(ypr[1]) : 0.0;

        double cy = Math.cos(-yaw), sy = Math.sin(-yaw);
        double x1 =  cy*rx + sy*rz;
        double z1 = -sy*rx + cy*rz;

        double cp = Math.cos(-pitch), sp = Math.sin(-pitch);
        double y2 =  cp*ry + sp*z1;
        double z2 = -sp*ry + cp*z1;

        double rxy = Math.hypot(x1, z2);
        azDeg = (float)Math.toDegrees(Math.atan2(x1, z2));
        elDeg = (float)Math.toDegrees(Math.atan2(y2, rxy));

        int x = centerX + (int)((azDeg - azimuthHeading) * azimuthScale);
        int y = centerY - (int)((elDeg - elevationHeading) * elevationScale);

        Graphics2D g2 = (Graphics2D) g;

        // If the agent knows how to draw itself in polar space, delegate to it.
        if (agent instanceof AgentPolarDrawable) {
            ((AgentPolarDrawable)agent).drawInPolar(g2, azDeg, elDeg, x, y);
            return;
        }

        // Fallback: simple crosshair + label
        g2.setColor(AgentColors.colorForKey(agent.getKey()));
        int rpx = Math.max(2, (int)(Math.max(azimuthScale, elevationScale) * sizeDeg * 0.1f));
        g2.drawOval(x - rpx, y - rpx, 2*rpx, 2*rpx);
        g2.drawLine(x - rpx, y, x + rpx, y);
        g2.drawLine(x, y - rpx, x, y + rpx);

        if (drawLabel) {
            String label = agent.getKey();
            if (label != null && !label.isEmpty()) {
                Font oldF = g2.getFont();
                g2.setFont(labelFont);
                g2.setColor(Color.BLACK);
                g2.drawString(label, x + labelDX, y + labelDY);
                g2.setFont(oldF);
            }
        }
    }

    @Override public void setAzimuth(float az){ this.azDeg = az; }
    @Override public float getAzimuth(){ return azDeg; }
    @Override public void setElevation(float el){ this.elDeg = el; }
    @Override public float getElevation(){ return elDeg; }
}
