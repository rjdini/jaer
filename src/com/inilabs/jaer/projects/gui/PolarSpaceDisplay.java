/*
 * Copyright (C) 2024 rjd.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 PolarSpaceDisplay settings.
*/

package com.inilabs.jaer.projects.gui;

import com.inilabs.jaer.gimbal.FieldOfView;
import com.inilabs.jaer.projects.tracker.TrackerManagerEngine;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.slf4j.LoggerFactory;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;

public class PolarSpaceDisplay extends JPanel {

    private float azimuthHeading = 0.0f; // Heading azimuth
    private float elevationHeading = 0.0f; // Heading elevation
    private float azimuthRange = 30.0f; // Azimuth range on either side of the heading
    private float elevationRange = 30.0f; // Elevation range on either side of the heading
    private int width = 0;
    private int height = 0;
    private int centerX = 0;
    private int centerY = 0;
    private float azimuthScale = 1f;
    private float elevationScale = 1f;
    private JLabel coordinatesLabel;
    private static PolarSpaceDisplay instance = null;

    private final Map<String, Drawable> drawables = new HashMap<>(); // Map of drawables managed by key
  private static final ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(PolarSpaceDisplay.class);
    
  private PolarSpaceDisplay() {
        setBackground(Color.WHITE);
        initializeDisplay();
        this.repaint();
          SwingUtilities.invokeLater(() -> repaint());
    }
    
   public static PolarSpaceDisplay getInstance() {
        if (instance == null) {
            instance = new PolarSpaceDisplay();
        }
        return instance;
    }
  
  
    
    private void initializeDisplay() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 800));
    
        azimuthHeading = 0.0f; // Heading azimuth
        elevationHeading = 0.0f; // Heading elevation
        azimuthRange = 30.0f; // Azimuth range on either side of the heading
        elevationRange = 30.0f; // Elevation range on either side of the heading
        width = getWidth();
        height = getHeight();
        centerX = width / 2;
        centerY = height / 2;
        azimuthScale = getAzimuthScale();
        elevationScale = getElevationScale();

        // Add a label to display coordinates
        coordinatesLabel = new JLabel("Coordinates: ");
        coordinatesLabel.setForeground(Color.WHITE);
        add(coordinatesLabel);
        
          // Enable tooltips and set custom behavior
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000); // Tooltip disappears after 5 seconds
        ToolTipManager.sharedInstance().registerComponent(this);

        // Attach mouse listener for click-based tooltips
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showTooltipOnClick(e);
            }
        });
    
    }


    
    private void showTooltipOnClick(MouseEvent e) {
      int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Calculate azimuth and elevation based on clicked coordinates
        float azimuth = ((e.getX() - centerX) / getAzimuthScale()) + azimuthHeading;
        float elevation = ((centerY - e.getY()) / getElevationScale()) + elevationHeading;

        // Update tooltip text
        setToolTipText(String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°",
                e.getX(), e.getY(), azimuth, elevation));    
       
    // Manually show the tooltip by forcing a repaint (ensures visibility on click)
    SwingUtilities.invokeLater(() -> ToolTipManager.sharedInstance().mousePressed(e));
}


    @Override
    public Point getToolTipLocation(MouseEvent e) {
        // Position the tooltip above the mouse cursor
        return new Point(e.getX(), e.getY() - 25); // Adjust height as needed
    }


        @Override
    public String getToolTipText(MouseEvent e) {
        // Provide tooltip text dynamically
    int    centerX = getWidth() / 2;
     int   centerY = getHeight() / 2;

        float azimuth = ((e.getX() - centerX) / getAzimuthScale()) + azimuthHeading;
        float elevation = ((centerY - e.getY()) / getElevationScale()) + elevationHeading;

        return String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°",
                e.getX(), e.getY(), azimuth, elevation);
    }
    
    

    private void reportCoordinates(int x, int y) {
    // Obtain the current transformation parameters from PolarSpaceDisplay
    float azimuthScale = getAzimuthScale();          // Pixels per degree for azimuth
    float elevationScale = getElevationScale();      // Pixels per degree for elevation
    float azimuthHeading = this.azimuthHeading;      // Current azimuth heading in degrees
    float elevationHeading = this.elevationHeading;  // Current elevation heading in degrees

    int centerX = getWidth() / 2; // Horizontal center of the display in pixels
    int centerY = getHeight() / 2; // Vertical center of the display in pixels

    // Reverse the transformation to compute azimuth and elevation
    float azimuth = ((x - centerX) / azimuthScale) + azimuthHeading;
    float elevation = ((centerY - y) / elevationScale) + elevationHeading;

    // Update the coordinates label
    coordinatesLabel.setText(String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°", 
                                            x, y, azimuth, elevation));

     // Update the tooltip with the calculated values
    setToolTipText(String.format("X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°", 
                                  x, y, azimuth, elevation));
    
    // Optional: Log the coordinates for debugging
    System.out.printf("Mouse at X: %d, Y: %d | Azimuth: %.2f°, Elevation: %.2f°%n", 
                      x, y, azimuth, elevation);
}

    
    
//    @Override
//public Dimension getPreferredSize() {
//    return new Dimension(800, 600);
//}
    
    public void shutdown() {
        
    }

    public void setHeading(float azimuth, float elevation) {
        this.azimuthHeading = azimuth;
        this.elevationHeading = elevation;
        notifyTransformListeners();
        repaint();
    }

    public void setAzimuthRange(float range) {
        this.azimuthRange = range;
        notifyTransformListeners();
        repaint();
    }

    public float getAzimuthRange() {
        return azimuthRange;
    }
    
    public void setElevationRange(float range) {
        this.elevationRange = range;
        notifyTransformListeners();
        repaint();
    }
public float getElevationRange() {
        return elevationRange;
    }
    
    
    public float getAzimuthScale() {
        return (float) getWidth() / (2 * azimuthRange); // Pixels per degree for azimuth
    }

    public float getElevationScale() {
        return (float) getHeight() / (2 * elevationRange); // Pixels per degree for elevation
    }

    /**
     * Adds a drawable to the display.
     * @param drawable The drawable to add.
     */
     public void addDrawable(Drawable drawable) {
        drawables.put(drawable.getKey(), drawable);

        // Set the callback to remove the drawable
        drawable.setParentCallback((action, key) -> {
            if (action == ActionType.REMOVE) {
                removeDrawable(key);
            }
        });

        notifyTransformListeners();
        repaint();
    }
    
    

    /**
     * Removes a drawable by its key.
     * @param key The unique key of the drawable to remove.
     */
    public void removeDrawable(String key) {
        drawables.remove(key);
        repaint();
    }

     public List<String> getDrawableNames() {
        return new ArrayList<>(drawables.keySet());
    }
    
    /**
     * Updates all drawables with the latest scaling and translation offset.
     */
    private void notifyTransformListeners() {
        azimuthScale = getAzimuthScale();
        elevationScale = getElevationScale();

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        for (Drawable drawable : drawables.values()) {
            drawable.onTransformChanged(azimuthScale, elevationScale, azimuthHeading, elevationHeading, centerX, centerY);
        }
    }
    
   /**
 * Checks if a drawable is already present in the display by its unique key.
 * 
 * @param drawable The drawable to check.
 * @return true if the drawable is present, false otherwise.
 */
public boolean containsDrawable(Drawable drawable) {
    return drawables.containsKey(drawable.getKey());
}
    
    
     /**
     * Toggle visibility of paths for all drawables.
     * @param show true to show paths, false to hide
     */
    public void showPaths(boolean show) {
        for (Drawable drawable : drawables.values()) {
            drawable.showPath(show);
        }
        repaint();
    }
   
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw red dot at the heading point
        g2d.setColor(Color.RED);
        g2d.fillOval(centerX - 3, centerY - 3, 6, 6);

        // Draw azimuth and elevation scale bars centered at the heading point
        ScaleBar azimuthScaleBar = new ScaleBar(true, (int) azimuthRange, getAzimuthScale(), azimuthHeading);
        azimuthScaleBar.draw(g2d, centerX, centerY);

        ScaleBar elevationScaleBar = new ScaleBar(false, (int) elevationRange, getElevationScale(), elevationHeading);
        elevationScaleBar.draw(g2d, centerX, centerY);

        // Simply call draw() on each Drawable, letting each one handle its position based on scaling
        for (Drawable drawable : drawables.values()) {
            drawable.draw(g2d);
          
        }
        
    
        // Draw crosshairs or grid if needed
        g.setColor(Color.GRAY);
        g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight()); // Vertical line
        g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2); // Horizontal line
    
        
        
      //   log.info("drawing drawables --- size of drawables = {} ", drawables.size());
    }
}

