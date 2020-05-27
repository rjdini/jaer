/*
 * Copyright (C) 2020 Tobi.
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
package net.sf.jaer;

import java.awt.Cursor;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.sf.jaer.util.WindowSaver.DontRestore;

/**
 * Frame to run update operations on jAER
 *
 * @author Tobi
 */
public class JaerUpdaterFrame extends javax.swing.JFrame implements DontRestore {

    private static Logger log = Logger.getLogger("JaerUpdater");

    /**
     * Creates new form JaerUpdaterFrame. Make sure to run this from swing
     * thread. See main() method.
     */
    public JaerUpdaterFrame() {
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource(JaerConstants.ICON_IMAGE)).getImage());
        boolean debug = JaerUpdater.DEBUG;
        try {
            setGitButtonsEnabled(false);
            initGitButton.setEnabled(false);
            gitPullButton.addActionListener(JaerUpdater.gitPullActionListener(this));
            antBuildButton.addActionListener(JaerUpdater.buildActionListener(this));
            statusButton.addActionListener(JaerUpdater.gitCFetchChangesActionListener(this));
            setGitButtonsEnabled(true);
        } catch (IOException e) {
            log.warning(e.toString());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(JaerUpdaterFrame.this, "<html>.git folder not found or is corrupted; you must initialize git before any update operations."
                            + "<p>It is expected if you are running jAER from a release, which does not include git information."
                            + "<p>Click the jaerproject.org link in the Updater dialog to find out how to <br>"
                            + "initialize your release to a working copy using command line git."
                            + "<p> Or you can use the built in function in the updater to initialize git.", "git not set up", JOptionPane.WARNING_MESSAGE);
                }
            });

            try {
                if (!debug) {
                    initGitButton.setEnabled(true);
                    initGitButton.addActionListener(JaerUpdater.initReleaseForGitActionListener(this));
                }
            } catch (IOException ex) {
                Logger.getLogger(JaerUpdaterFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        pack();

        //debug only
        if (debug) {
            initGitButton.setEnabled(true);
            try {
                initGitButton.addActionListener(JaerUpdater.initReleaseForGitActionListener(this));
            } catch (IOException ex) {
                Logger.getLogger(JaerUpdaterFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void setGitButtonsEnabled(boolean yes) {
        gitPullButton.setEnabled(yes);
        antBuildButton.setEnabled(yes);
        statusButton.setEnabled(yes);
        initGitButton.setEnabled(!yes);
    }

    private void showInBrowser(String url) {
        if (!Desktop.isDesktopSupported()) {
            log.warning("No Desktop support, can't show help from " + url);
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Couldn't show " + url + "; caught " + ex, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jaerProjectLinkLabel = new javax.swing.JLabel();
        seeReleasesButton = new javax.swing.JButton();
        seeCommitsButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane4 = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        statusButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        antBuildButton = new javax.swing.JButton();
        gitPullButton = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextPane5 = new javax.swing.JTextPane();
        initGitButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("jAER updater");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("jAER updater");

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jaerProjectLinkLabel.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        jaerProjectLinkLabel.setText("<html> <em><a href=\"http://jaerproject.org\">jaerproject.org</a> </em></html>");
        jaerProjectLinkLabel.setToolTipText("Go to project home page");
        jaerProjectLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jaerProjectLinkLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jaerProjectLinkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jaerProjectLinkLabelMouseExited(evt);
            }
        });

        seeReleasesButton.setText("See releases");
        seeReleasesButton.setToolTipText("Go to release page to see tagged releases");
        seeReleasesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seeReleasesButtonActionPerformed(evt);
            }
        });

        seeCommitsButton.setText("See commits");
        seeCommitsButton.setToolTipText("See most recent commits");
        seeCommitsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seeCommitsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jaerProjectLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(seeReleasesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seeCommitsButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jaerProjectLinkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(seeReleasesButton)
                    .addComponent(seeCommitsButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextPane4.setText("You will generally benefit from running from the most recent commits since bugs are fixed frequently but releases are infrequent");
        jScrollPane1.setViewportView(jTextPane4);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextPane1.setText("Check your code to see if there have been remote changes with \"git status\".");
        jScrollPane2.setViewportView(jTextPane1);

        statusButton.setText("See git status");

        jTextPane3.setEditable(false);
        jTextPane3.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextPane3.setText("Update your source code using \"git pull\" .");
        jScrollPane4.setViewportView(jTextPane3);

        jTextPane2.setEditable(false);
        jTextPane2.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextPane2.setText("Rebuild from sources  using ant with \"ant jar\".\nThe rebuild task can fail if you are running from jar file, since it will be in use.");
        jScrollPane3.setViewportView(jTextPane2);

        antBuildButton.setText("Rebuild jAER from source");

        gitPullButton.setText("Pull git changes");

        jTextPane5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jTextPane5.setText("You must be running jAER from a git clone for anything here to work.");
        jScrollPane5.setViewportView(jTextPane5);

        initGitButton.setText("Initizalize release for Git");
        initGitButton.setToolTipText("<html>Will clone jAER to a new temporary folder, and then copy missing files <br>\n(including the .git folder) to your release folder. \n<p>After this, you can compile an updated dist/jAER.jar that has most recent code.");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(antBuildButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane4)
                            .addComponent(jScrollPane2))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(gitPullButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 507, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(initGitButton)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(initGitButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(statusButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gitPullButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(antBuildButton))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(closeButton)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(closeButton)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void jaerProjectLinkLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jaerProjectLinkLabelMouseClicked
        try {
            showInBrowser(JaerConstants.HELP_URL_JAER_HOME);

            setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }//GEN-LAST:event_jaerProjectLinkLabelMouseClicked

    private void jaerProjectLinkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jaerProjectLinkLabelMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jaerProjectLinkLabelMouseEntered

    private void jaerProjectLinkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jaerProjectLinkLabelMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_jaerProjectLinkLabelMouseExited

    private void seeCommitsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seeCommitsButtonActionPerformed
        try {
            showInBrowser(JaerConstants.JAER_COMMITS);
            setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }//GEN-LAST:event_seeCommitsButtonActionPerformed

    private void seeReleasesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seeReleasesButtonActionPerformed
        try {
            showInBrowser(JaerConstants.JAER_RELEASES);
            setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            log.warning(e.toString());
        }
    }//GEN-LAST:event_seeReleasesButtonActionPerformed

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
            java.util.logging.Logger.getLogger(JaerUpdaterFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JaerUpdaterFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JaerUpdaterFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JaerUpdaterFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JaerUpdaterFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton antBuildButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton gitPullButton;
    private javax.swing.JButton initGitButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTextPane jTextPane3;
    private javax.swing.JTextPane jTextPane4;
    private javax.swing.JTextPane jTextPane5;
    private javax.swing.JLabel jaerProjectLinkLabel;
    private javax.swing.JButton seeCommitsButton;
    private javax.swing.JButton seeReleasesButton;
    private javax.swing.JButton statusButton;
    // End of variables declaration//GEN-END:variables
}
