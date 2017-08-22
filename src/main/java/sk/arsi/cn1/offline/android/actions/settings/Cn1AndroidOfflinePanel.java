/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.arsi.cn1.offline.android.actions.settings;

import java.io.File;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbPreferences;
import sk.arsi.cn1.offline.android.actions.InitAndroidProjectAction;

final class Cn1AndroidOfflinePanel extends javax.swing.JPanel {

    private final Cn1AndroidOfflineOptionsPanelController controller;

    Cn1AndroidOfflinePanel(Cn1AndroidOfflineOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        sdk = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(Cn1AndroidOfflinePanel.class, "Cn1AndroidOfflinePanel.jLabel1.text")); // NOI18N

        sdk.setText(org.openide.util.NbBundle.getMessage(Cn1AndroidOfflinePanel.class, "Cn1AndroidOfflinePanel.sdk.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(Cn1AndroidOfflinePanel.class, "Cn1AndroidOfflinePanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sdk, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(sdk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap(206, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        FileChooserBuilder fcho = new FileChooserBuilder(InitAndroidProjectAction.class);
        fcho.setDirectoriesOnly(true);
        fcho.setTitle("Select Android SDK location");
        File sdkf = fcho.showOpenDialog();
        if (sdkf != null) {
            sdk.setText(sdkf.getAbsolutePath());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    void load() {
        sdk.setText(NbPreferences.forModule(InitAndroidProjectAction.class).get("sdk", ""));
    }

    void store() {
        NbPreferences.forModule(InitAndroidProjectAction.class).put("sdk", sdk.getText());
    }

    boolean valid() {
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField sdk;
    // End of variables declaration//GEN-END:variables
}
