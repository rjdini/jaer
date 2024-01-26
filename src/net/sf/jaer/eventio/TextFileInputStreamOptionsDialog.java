/*
 * Copyright (C) 2024 tobi.
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
 */
package net.sf.jaer.eventio;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author tobi
 */
public class TextFileInputStreamOptionsDialog extends javax.swing.JDialog implements PropertyChangeListener {

    TextFileInputStream textFileInputStream;

    /**
     * Creates new form TextFileInputStreamOptionsDialog
     *
     * @param parent the component to center over
     * @param modal whether the dialog takes over Swing thread or sits by it
     * live all the time
     * @param textFileInputStream the stream we control
     *
     */
    public TextFileInputStreamOptionsDialog(java.awt.Frame parent, boolean modal, TextFileInputStream textInputStream) {
        super(parent, modal);
        this.textFileInputStream = textInputStream;
        initComponents();
        getRootPane().setDefaultButton(closeButton);
        polSignedB.setSelected(textInputStream.isUseSignedPolarity());
        polBinB.setSelected(!textInputStream.isUseSignedPolarity());

        sepCommaB.setSelected(textInputStream.isUseCSV());
        sepSpaceB.setSelected(!textInputStream.isUseCSV());

        tsLastB.setSelected(textInputStream.isTimestampLast());
        tsFirstB.setSelected(!textInputStream.isTimestampLast());

        tsIntRB.setSelected(textInputStream.isUseUsTimestamps());
        tsFloatRB.setSelected(!textInputStream.isUseUsTimestamps());

        specialEventsCB.setSelected(textInputStream.isSpecialEvents());

        flipPolCB.setSelected(textInputStream.isFlipPolarity());

        setFormatString(textInputStream.getShortFormattingHintString());

        helpText.setContentType("text/html"); // https://stackoverflow.com/questions/13195131/how-to-use-html-tags-in-jtextarea
        setHelpText(textInputStream.getFormattingHelpString());

        textInputStream.addPropertyChangeListener(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tsIntFloatBG = new javax.swing.ButtonGroup();
        tsFirstLastBG = new javax.swing.ButtonGroup();
        sepBG = new javax.swing.ButtonGroup();
        polBG = new javax.swing.ButtonGroup();
        titleText = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        helpText = new javax.swing.JTextPane();
        eraseFileHashMapButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        sepCommaB = new javax.swing.JRadioButton();
        sepSpaceB = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tsIntRB = new javax.swing.JRadioButton();
        tsFloatRB = new javax.swing.JRadioButton();
        tsFirstB = new javax.swing.JRadioButton();
        tsLastB = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        polBinB = new javax.swing.JRadioButton();
        polSignedB = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        specialEventsCB = new javax.swing.JCheckBox();
        setToRPGFormatB = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        formatLabel = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        errorTA = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        flipYCB = new javax.swing.JCheckBox();
        flipXCB = new javax.swing.JCheckBox();
        flipPolCB = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        titleText.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        titleText.setText("Set the format of the event text file lines");

        closeButton.setMnemonic('c');
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        helpText.setEditable(false);
        helpText.setBorder(javax.swing.BorderFactory.createTitledBorder("Format guide"));
        jScrollPane3.setViewportView(helpText);

        eraseFileHashMapButton.setText("Erase hashed values for file lengths");
        eraseFileHashMapButton.setToolTipText("Erases the HashMap that stores previously read file sizes to avoid rereading them every time you open the same file path");
        eraseFileHashMapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraseFileHashMapButtonActionPerformed(evt);
            }
        });

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setText("Separator");

        sepBG.add(sepCommaB);
        sepCommaB.setText("comma");
        sepCommaB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sepCommaBActionPerformed(evt);
            }
        });

        sepBG.add(sepSpaceB);
        sepSpaceB.setText("space");
        sepSpaceB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sepSpaceBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sepCommaB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sepSpaceB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(sepCommaB)
                    .addComponent(sepSpaceB))
                .addGap(37, 37, 37))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setText("Timestamps");

        tsIntFloatBG.add(tsIntRB);
        tsIntRB.setText("int [us]");
        tsIntRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tsIntRBActionPerformed(evt);
            }
        });

        tsIntFloatBG.add(tsFloatRB);
        tsFloatRB.setText("float [s]");
        tsFloatRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tsFloatRBActionPerformed(evt);
            }
        });

        tsFirstLastBG.add(tsFirstB);
        tsFirstB.setText("first");
        tsFirstB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tsFirstBActionPerformed(evt);
            }
        });

        tsFirstLastBG.add(tsLastB);
        tsLastB.setText("last");
        tsLastB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tsLastBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tsIntRB)
                    .addComponent(tsFirstB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tsLastB)
                    .addComponent(tsFloatRB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tsIntRB)
                    .addComponent(tsFloatRB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tsFirstB)
                    .addComponent(tsLastB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setText("Polarity");

        polBG.add(polBinB);
        polBinB.setText("binary (0,1)");
        polBinB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                polBinBActionPerformed(evt);
            }
        });

        polBG.add(polSignedB);
        polSignedB.setText("signed (-1,+1)");
        polSignedB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                polSignedBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(polBinB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(polSignedB))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(polBinB)
                    .addComponent(polSignedB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        specialEventsCB.setText("include special events");
        specialEventsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specialEventsCBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(specialEventsCB)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(specialEventsCB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setToRPGFormatB.setText("Set format to RPG standard");
        setToRPGFormatB.setToolTipText("Resets state to RPG format");
        setToRPGFormatB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setToRPGFormatBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setToRPGFormatB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(setToRPGFormatB)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Expected line format"));

        formatLabel.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        formatLabel.setText("event format");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formatLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formatLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Last error line (bad format lines will appear here)"));

        errorTA.setEditable(false);
        errorTA.setColumns(20);
        errorTA.setRows(5);
        jScrollPane2.setViewportView(errorTA);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Transform"));
        jPanel8.setToolTipText("set desired address transforms");

        flipYCB.setText("Flip Y");
        flipYCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipYCBActionPerformed(evt);
            }
        });

        flipXCB.setText("Flip X");
        flipXCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipXCBActionPerformed(evt);
            }
        });

        flipPolCB.setText("Flip Polarity");
        flipPolCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flipPolCBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(flipPolCB)
                    .addComponent(flipXCB)
                    .addComponent(flipYCB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(flipXCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flipYCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flipPolCB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(titleText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(24, 24, 24)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(eraseFileHashMapButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleText)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 67, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(eraseFileHashMapButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void specialEventsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specialEventsCBActionPerformed
        textFileInputStream.setSpecialEvents(specialEventsCB.isSelected());
    }//GEN-LAST:event_specialEventsCBActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void eraseFileHashMapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraseFileHashMapButtonActionPerformed
        textFileInputStream.eraseFileHashMap();
    }//GEN-LAST:event_eraseFileHashMapButtonActionPerformed

    private void tsIntRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tsIntRBActionPerformed
        textFileInputStream.setUseUsTimestamps(true);
    }//GEN-LAST:event_tsIntRBActionPerformed

    private void sepCommaBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sepCommaBActionPerformed
        textFileInputStream.setUseCSV(true);
    }//GEN-LAST:event_sepCommaBActionPerformed

    private void sepSpaceBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sepSpaceBActionPerformed
        textFileInputStream.setUseCSV(false);
    }//GEN-LAST:event_sepSpaceBActionPerformed

    private void tsFloatRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tsFloatRBActionPerformed
        textFileInputStream.setUseUsTimestamps(false);
    }//GEN-LAST:event_tsFloatRBActionPerformed

    private void tsFirstBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tsFirstBActionPerformed
        textFileInputStream.setTimestampLast(false);
    }//GEN-LAST:event_tsFirstBActionPerformed

    private void tsLastBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tsLastBActionPerformed
        textFileInputStream.setTimestampLast(true);
    }//GEN-LAST:event_tsLastBActionPerformed

    private void polBinBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_polBinBActionPerformed
        textFileInputStream.setUseSignedPolarity(false);
    }//GEN-LAST:event_polBinBActionPerformed

    private void polSignedBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_polSignedBActionPerformed
        textFileInputStream.setUseSignedPolarity(true);
    }//GEN-LAST:event_polSignedBActionPerformed

    private void setToRPGFormatBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setToRPGFormatBActionPerformed
        textFileInputStream.doSetToRPGFormat();
    }//GEN-LAST:event_setToRPGFormatBActionPerformed

    private void flipXCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flipXCBActionPerformed
        textFileInputStream.setFlipX(flipXCB.isSelected());
    }//GEN-LAST:event_flipXCBActionPerformed

    private void flipYCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flipYCBActionPerformed
        textFileInputStream.setFlipY(flipYCB.isSelected());
    }//GEN-LAST:event_flipYCBActionPerformed

    private void flipPolCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flipPolCBActionPerformed
        textFileInputStream.setFlipPolarity(flipPolCB.isSelected());
    }//GEN-LAST:event_flipPolCBActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TextFileInputStreamOptionsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TextFileInputStreamOptionsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TextFileInputStreamOptionsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TextFileInputStreamOptionsDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TextFileInputStreamOptionsDialog dialog = new TextFileInputStreamOptionsDialog(new javax.swing.JFrame(), true, null);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton eraseFileHashMapButton;
    private javax.swing.JTextArea errorTA;
    private javax.swing.JCheckBox flipPolCB;
    private javax.swing.JCheckBox flipXCB;
    private javax.swing.JCheckBox flipYCB;
    private javax.swing.JLabel formatLabel;
    private javax.swing.JTextPane helpText;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.ButtonGroup polBG;
    private javax.swing.JRadioButton polBinB;
    private javax.swing.JRadioButton polSignedB;
    private javax.swing.ButtonGroup sepBG;
    private javax.swing.JRadioButton sepCommaB;
    private javax.swing.JRadioButton sepSpaceB;
    private javax.swing.JButton setToRPGFormatB;
    private javax.swing.JCheckBox specialEventsCB;
    private javax.swing.JLabel titleText;
    private javax.swing.JRadioButton tsFirstB;
    private javax.swing.ButtonGroup tsFirstLastBG;
    private javax.swing.JRadioButton tsFloatRB;
    private javax.swing.ButtonGroup tsIntFloatBG;
    private javax.swing.JRadioButton tsIntRB;
    private javax.swing.JRadioButton tsLastB;
    // End of variables declaration//GEN-END:variables

    void setFormatString(String s) {
        formatLabel.setText(s);
    }

    void setHelpText(String s) {
        helpText.setText(s);
    }

    void setSampleLine(String s) {
        errorTA.setText(s);
    }

    void setErrorMessage(String s) {
        errorTA.setText(s);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof TextFileInputStream) {
            if (evt.getPropertyName().equals("format")) {
                setFormatString(textFileInputStream.getShortFormattingHintString());
                setHelpText(textFileInputStream.getFormattingHelpString());
            } else if (evt.getPropertyName().equals("sampleLine")) {
                setSampleLine((String) evt.getNewValue());
            } else if (evt.getPropertyName().equals("lastError")) {
                setErrorMessage((String) evt.getNewValue());
            }
        }
    }
}
