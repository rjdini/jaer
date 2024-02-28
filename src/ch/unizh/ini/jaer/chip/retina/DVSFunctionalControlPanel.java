/*
 * Tmpdiff128FunctionalBiasgenPanel.java
 *
 * Created on June 19, 2006, 1:48 PM
 */
package ch.unizh.ini.jaer.chip.retina;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import net.sf.jaer.biasgen.PotTweaker;
import net.sf.jaer.util.EngineeringFormat;

/**
 * A panel for simplified control of DVS retina biases.
 *
 * @author tobi
 */
public class DVSFunctionalControlPanel extends javax.swing.JPanel implements PropertyChangeListener {

    AETemporalConstastRetina chip;
    DVSTweaks biasgen;
    private static final Logger log = Logger.getLogger("net.sf.jaer");
    private static EngineeringFormat engFmt = new EngineeringFormat();

    /**
     * Creates new form Tmpdiff128FunctionalBiasgenPanel
     */
    public DVSFunctionalControlPanel(AETemporalConstastRetina chip) {
        initComponents();
        this.chip = chip;
        biasgen = (DVSTweaks) chip.getBiasgen();
        PotTweaker[] tweakers = {thresholdTweaker, onOffBalanceTweaker, maxFiringRateTweaker, bandwidthTweaker};
        for (PotTweaker tweaker : tweakers) {
            chip.getBiasgen().getSupport().addPropertyChangeListener(tweaker); // to reset sliders on load/save of biases
        }
        setEstimatedThresholdValues();
        setEstimatedBandwidthValues();
        chip.getSupport().addPropertyChangeListener(this);
    }

    private void setFileModified() {
        if (chip != null && chip.getAeViewer() != null && chip.getAeViewer().getBiasgenFrame() != null) {
            chip.getAeViewer().getBiasgenFrame().setFileModified(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        bandwidthTweaker = new net.sf.jaer.biasgen.PotTweaker();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        bwTF = new javax.swing.JTextField();
        thresholdTweaker = new net.sf.jaer.biasgen.PotTweaker();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        onThrTF = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        offThrTF = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        onMinusOffTF = new javax.swing.JTextField();
        onOffBalanceTweaker = new net.sf.jaer.biasgen.PotTweaker();
        maxFiringRateTweaker = new net.sf.jaer.biasgen.PotTweaker();

        setLayout(new java.awt.GridLayout(0, 1));

        jLabel1.setText("<html>This panel allows \"tweaking\" bias values around the nominal ones loaded from the XML file. <p>Changes made here are <b>not</b> permanent until the settings are saved to an XML file. <p>On restart, these new settings will then become the nominal settings.");
        add(jLabel1);

        bandwidthTweaker.setLessDescription("Slower");
        bandwidthTweaker.setMoreDescription("Faster");
        bandwidthTweaker.setName("Bandwidth"); // NOI18N
        bandwidthTweaker.setTweakDescription("Tweaks bandwidth of pixel front end.");
        bandwidthTweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bandwidthTweakerStateChanged(evt);
            }
        });
        add(bandwidthTweaker);

        jLabel2.setText("Est. photoreceptor bandwidth");
        jLabel2.setToolTipText("Show theoertical computed BW based on SF bias; only makes sense in range 100Hz to few kHz with sufficient light");
        jPanel1.add(jLabel2);

        bwTF.setEditable(false);
        bwTF.setColumns(14);
        bwTF.setToolTipText("Show theoertical computed BW based on SF bias; only makes sense in range 100Hz to few kHz with sufficient light");
        jPanel1.add(bwTF);

        add(jPanel1);

        thresholdTweaker.setLessDescription("Lower/more events");
        thresholdTweaker.setMoreDescription("Higher/less events");
        thresholdTweaker.setName("Threshold"); // NOI18N
        thresholdTweaker.setTweakDescription("Adjusts event threshold");
        thresholdTweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                thresholdTweakerStateChanged(evt);
            }
        });
        add(thresholdTweaker);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel9.setText("Estimated DVS thresholds:");
        jLabel9.setToolTipText("<html>Displays computed values of DVS event temporal contrast thresholds<br> \nbased on paper\n<a href=\"https://ieeexplore.ieee.org/document/7962235\">Temperature and\n Parasitic Photocurrent <br> Effects in Dynamic Vision Sensors, <br>Y Nozaki, T\nDelbruck. <br>IEEE Trans. on Electron Devices, 2018</a>");
        jPanel4.add(jLabel9);

        jLabel7.setText("ON");
        jPanel4.add(jLabel7);

        onThrTF.setEditable(false);
        onThrTF.setColumns(14);
        onThrTF.setToolTipText("Estimated DVS  temporal contrast threshold  (log base e units)");
        jPanel4.add(onThrTF);

        jLabel8.setText("OFF");
        jPanel4.add(jLabel8);

        offThrTF.setEditable(false);
        offThrTF.setColumns(14);
        offThrTF.setToolTipText("Estimated DVS  temporal contrast threshold  (log base e units)");
        offThrTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offThrTFActionPerformed(evt);
            }
        });
        jPanel4.add(offThrTF);

        jLabel10.setText("ON+OFF");
        jLabel10.setToolTipText("difference ON to OFF thresholds (nominal balance)");
        jPanel4.add(jLabel10);

        onMinusOffTF.setEditable(false);
        onMinusOffTF.setColumns(7);
        onMinusOffTF.setToolTipText("difference ON to OFF thresholds (nominal balance)");
        jPanel4.add(onMinusOffTF);

        add(jPanel4);

        onOffBalanceTweaker.setLessDescription("More Off events");
        onOffBalanceTweaker.setMoreDescription("More On events");
        onOffBalanceTweaker.setName("On/Off balance"); // NOI18N
        onOffBalanceTweaker.setTweakDescription("Adjusts balance bewteen On and Off events");
        onOffBalanceTweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                onOffBalanceTweakerStateChanged(evt);
            }
        });
        add(onOffBalanceTweaker);

        maxFiringRateTweaker.setLessDescription("Slower");
        maxFiringRateTweaker.setMoreDescription("Faster");
        maxFiringRateTweaker.setName("Maximum firing rate"); // NOI18N
        maxFiringRateTweaker.setTweakDescription("Adjusts maximum pixel firing rate (1/refactory period)");
        maxFiringRateTweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxFiringRateTweakerStateChanged(evt);
            }
        });
        add(maxFiringRateTweaker);
    }// </editor-fold>//GEN-END:initComponents

    private void bandwidthTweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bandwidthTweakerStateChanged
        biasgen.setBandwidthTweak(bandwidthTweaker.getValue());
        setEstimatedBandwidthValues();
        setFileModified();
    }//GEN-LAST:event_bandwidthTweakerStateChanged

    private void thresholdTweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thresholdTweakerStateChanged
        biasgen.setThresholdTweak(thresholdTweaker.getValue());

        setEstimatedThresholdValues();
        setFileModified();
    }//GEN-LAST:event_thresholdTweakerStateChanged

    private void setEstimatedThresholdValues() {
        final float onThresholdLogE = biasgen.getOnThresholdLogE();
        final float offThresholdLogE = biasgen.getOffThresholdLogE();
        final float onPerCent = (float) (100 * (Math.exp(onThresholdLogE) - 1));
        final float offPerCent = (float) (100 * (Math.exp(offThresholdLogE) - 1));
        onThrTF.setText(String.format("%.3f e-folds (%.1f%%)", onThresholdLogE, onPerCent));
        offThrTF.setText(String.format("%.3f e-folds (%.1f%%)", offThresholdLogE, offPerCent));
        onMinusOffTF.setText(String.format("%.3f", onThresholdLogE + offThresholdLogE));
    }

    private void setEstimatedBandwidthValues() {
        final float bw=biasgen.getPhotoreceptorSourceFollowerBandwidthHz();
        bwTF.setText(String.format("%s Hz",engFmt.format(bw)));
    }


    private void maxFiringRateTweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxFiringRateTweakerStateChanged
        biasgen.setMaxFiringRateTweak(maxFiringRateTweaker.getValue());
        setFileModified();
    }//GEN-LAST:event_maxFiringRateTweakerStateChanged

    private void onOffBalanceTweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_onOffBalanceTweakerStateChanged
        biasgen.setOnOffBalanceTweak(onOffBalanceTweaker.getValue());
        setEstimatedThresholdValues();
        setFileModified();
    }//GEN-LAST:event_onOffBalanceTweakerStateChanged

    private void offThrTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offThrTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_offThrTFActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private net.sf.jaer.biasgen.PotTweaker bandwidthTweaker;
    private javax.swing.JTextField bwTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private net.sf.jaer.biasgen.PotTweaker maxFiringRateTweaker;
    private javax.swing.JTextField offThrTF;
    private javax.swing.JTextField onMinusOffTF;
    private net.sf.jaer.biasgen.PotTweaker onOffBalanceTweaker;
    private javax.swing.JTextField onThrTF;
    private net.sf.jaer.biasgen.PotTweaker thresholdTweaker;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getPropertyName() == DVSTweaks.THRESHOLD) {
                float v = (Float) evt.getNewValue();
                thresholdTweaker.setValue(v);

            } else if (evt.getPropertyName() == DVSTweaks.BANDWIDTH) {
                float v = (Float) evt.getNewValue();
                bandwidthTweaker.setValue(v);

            } else if (evt.getPropertyName() == DVSTweaks.MAX_FIRING_RATE) {
                float v = (Float) evt.getNewValue();
                maxFiringRateTweaker.setValue(v);

            } else if (evt.getPropertyName() == DVSTweaks.ON_OFF_BALANCE) {
                float v = (Float) evt.getNewValue();
                onOffBalanceTweaker.setValue(v);

            }
        } catch (Exception e) {
            log.warning("responding to property change, caught " + e.toString());
            e.printStackTrace();
        }
    }

}
