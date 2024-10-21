/*
 * PanTiltGUI.java
 *
 * Created on April 21, 2008, 11:50 AM
 */
package com.inilabs.jaer.gimbal;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

import net.sf.jaer.util.ExceptionListener;

/**
 * Tests pantilt by mimicing mouse movements. Also can serve as calibration source via PropertyChangeSupport.
 * @author  tobi
 */
public class PanTiltGUI extends javax.swing.JFrame implements ExceptionListener {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    Logger log = Logger.getLogger("net.sf.jaer");
    private PanTilt panTilt;
    private int w = 200,  h = 200,  x0 = 0,  y0 = 0;
    private Point2D.Float lastPanTilt = new Point2D.Float(0.5f, 0.5f);
    private Point lastMousePressLocation = new Point(w / 2, h / 2);
    PanTiltCalibrator calibrator;
    
    public enum Message {

        AddSample,
        ClearSamples,
        EraseLastSample,
        ShowCalibration,
        ComputeCalibration,
        RevertCalibration,
        ResetCalibration
    }

    /** Make the GUI.
     * 
     * @param pt the pan tilt unit
     * @param calibrator that we give calibration points to and that provides calibration points to paint here
     */
    public PanTiltGUI(PanTilt pt, PanTiltCalibrator calibrator) {
        this.calibrator=calibrator;
        panTilt = pt;
        initComponents();
        calibrationPanel.setPreferredSize(new Dimension(w, h));
//        HardwareInterfaceException.addExceptionListener(this);
        calibrationPanel.requestFocusInWindow();
        pack();
    }
   
 

    @Override
    public void paint(Graphics g) {
        final int r = 6;
        super.paint(g);
        calibrator.paint(calibrationPanel.getGraphics());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statusLabel = new javax.swing.JLabel();
        calibrationPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        doneButton = new javax.swing.JButton();
        revertButton = new javax.swing.JButton();
        shwCalibrationButton = new javax.swing.JButton();
        clearCalibrationPointsButton = new javax.swing.JButton();
        resetCalibrationButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PanTilt");
        setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        statusLabel.setText("exception status");
        statusLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        calibrationPanel.setBackground(new java.awt.Color(255, 255, 255));
        calibrationPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        calibrationPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                calibrationPanelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                calibrationPanelMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                calibrationPanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                calibrationPanelMouseReleased(evt);
            }
        });
        calibrationPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                calibrationPanelComponentResized(evt);
            }
        });
        calibrationPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                calibrationPanelMouseDragged(evt);
            }
        });
        calibrationPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                calibrationPanelKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout calibrationPanelLayout = new javax.swing.GroupLayout(calibrationPanel);
        calibrationPanel.setLayout(calibrationPanelLayout);
        calibrationPanelLayout.setHorizontalGroup(
            calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 549, Short.MAX_VALUE)
        );
        calibrationPanelLayout.setVerticalGroup(
            calibrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );

        jLabel5.setText("<html>Move pan tilt to point near to corners of retina view.<br><em>SPACE</em> for each sample.<br><em>BACKSPACE</em> to erase last sample.</html>");

        doneButton.setText("Save and close");
        doneButton.setToolTipText("Done calibrating with these points");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        revertButton.setText("Cancel and revert");
        revertButton.setToolTipText("Cancel calibration, keep old values");
        revertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revertButtonActionPerformed(evt);
            }
        });

        shwCalibrationButton.setText("Show calibration points");
        shwCalibrationButton.setToolTipText("Cycles through calibration points");
        shwCalibrationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shwCalibrationButtonActionPerformed(evt);
            }
        });

        clearCalibrationPointsButton.setText("Clear");
        clearCalibrationPointsButton.setToolTipText("Clears all the calibration  points");
        clearCalibrationPointsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearCalibrationPointsButtonActionPerformed(evt);
            }
        });

        resetCalibrationButton.setText("Reset");
        resetCalibrationButton.setToolTipText("Resets calibration to default (unity gain) values");
        resetCalibrationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetCalibrationButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                        .addGap(1, 1, 1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(shwCalibrationButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetCalibrationButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearCalibrationPointsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(doneButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(revertButton)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(273, Short.MAX_VALUE))))
            .addComponent(calibrationPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(calibrationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(revertButton)
                    .addComponent(doneButton)
                    .addComponent(shwCalibrationButton)
                    .addComponent(clearCalibrationPointsButton)
                    .addComponent(resetCalibrationButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel)
                .addGap(25, 25, 25))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private float getPan(MouseEvent evt) {
        int x = evt.getX();
        float pan = (float) x / w;
//        log.info("computed pan="+pan);
        return pan;

    }

    private float getTilt(MouseEvent evt) {
        int y = evt.getY();
        float tilt = 1 - (float) (h - y) / h;
//        log.info("computed tilt="+tilt);
        return tilt;
    }

    private void setPanTilt(float pan, float tilt) {
        try {
            lastPanTilt.x = pan;
            lastPanTilt.y = tilt;
            panTilt.setPanTiltValues(pan, tilt);
            statusLabel.setText(String.format("%.3f, %.3f", pan, tilt));
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }
    
    public Point getMouseFromPanTilt(Point2D.Float pt){
        return new Point((int)(calibrationPanel.getWidth()*pt.x),(int)(calibrationPanel.getHeight()*pt.y));
    }
    
    public Point2D.Float getPanTiltFromMouse(Point mouse){
        return new Point2D.Float((float)mouse.x/calibrationPanel.getWidth(),(float)mouse.y/calibrationPanel.getHeight());
    }

    private void calibrationPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_calibrationPanelComponentResized
        w = calibrationPanel.getWidth();
        h = calibrationPanel.getHeight();
    }//GEN-LAST:event_calibrationPanelComponentResized

    private void calibrationPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calibrationPanelMouseDragged
        float pan = getPan(evt);
        float tilt = getTilt(evt);
        setPanTilt(pan, tilt);
    }//GEN-LAST:event_calibrationPanelMouseDragged

    private void calibrationPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calibrationPanelMousePressed
        float pan = getPan(evt);
        float tilt = getTilt(evt);
        lastMousePressLocation = evt.getPoint();
        setPanTilt(pan, tilt);
        panTilt.stopJitter();
    }//GEN-LAST:event_calibrationPanelMousePressed

    private void calibrationPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_calibrationPanelKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                // send a message with pantilt and mouse filled in, tracker will fill in retina if there is a tracked locaton
                support.firePropertyChange(Message.AddSample.name(), null, new PanTiltCalibrationPoint(new Point2D.Float(), (Point2D.Float)lastPanTilt.clone(), (Point)lastMousePressLocation.clone()));
                repaint();
                return;
            case KeyEvent.VK_ENTER:
                support.firePropertyChange(Message.ComputeCalibration.name(), null, null);
                dispose();
                break;
            case KeyEvent.VK_BACK_SPACE:
                support.firePropertyChange(Message.EraseLastSample.name(), null, null);
                repaint();
                break;
            default:
                Toolkit.getDefaultToolkit().beep();
        }
    }//GEN-LAST:event_calibrationPanelKeyPressed

    private void revertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revertButtonActionPerformed
        support.firePropertyChange(Message.RevertCalibration.name(), null, null);
        dispose();
        panTilt.stopJitter();
        log.info("calibration reverted");
}//GEN-LAST:event_revertButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        support.firePropertyChange(Message.ComputeCalibration.name(), null, null);
        dispose();
        panTilt.stopJitter();
        log.info("done calibrating");
}//GEN-LAST:event_doneButtonActionPerformed

    private void calibrationPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calibrationPanelMouseReleased
        float pan = getPan(evt);
        float tilt = getTilt(evt);
        lastMousePressLocation = evt.getPoint();
        setPanTilt(pan, tilt);
        panTilt.startJitter();
    }//GEN-LAST:event_calibrationPanelMouseReleased

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        panTilt.stopJitter();
    }//GEN-LAST:event_formWindowClosed

    private void calibrationPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calibrationPanelMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
        calibrationPanel.requestFocus();
    }//GEN-LAST:event_calibrationPanelMouseEntered

    private void calibrationPanelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_calibrationPanelMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_calibrationPanelMouseExited

    private void shwCalibrationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shwCalibrationButtonActionPerformed
        support.firePropertyChange(Message.ShowCalibration.name(), null, null);
    }//GEN-LAST:event_shwCalibrationButtonActionPerformed

private void clearCalibrationPointsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearCalibrationPointsButtonActionPerformed
        support.firePropertyChange(Message.ClearSamples.name(),null, null);
        repaint();
}//GEN-LAST:event_clearCalibrationPointsButtonActionPerformed

private void resetCalibrationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetCalibrationButtonActionPerformed
    support.firePropertyChange(Message.ResetCalibration.name(), null, null);       
}//GEN-LAST:event_resetCalibrationButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel calibrationPanel;
    private javax.swing.JButton clearCalibrationPointsButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton resetCalibrationButton;
    private javax.swing.JButton revertButton;
    private javax.swing.JButton shwCalibrationButton;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
    public void exceptionOccurred(Exception x, Object source) {
        statusLabel.setText(x.getMessage());
    }

    /** Property change events are fired to return events 
     * 
     * For sample messages "sample", the Point2D.Float object that is returned is the pan,tilt value for that point, i.e., the last 
     * pan,tilt value that has been set.
     * 
     * When samples have been chosen, "done" is passed.
     * 
     * @return the support. Add yourself as a listener to get notifications of new calibration points.
     */
    public PropertyChangeSupport getSupport() {
        return support;
    }
}
