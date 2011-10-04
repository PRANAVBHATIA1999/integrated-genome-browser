/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OneRecord.java
 *
 * Created on Sep 11, 2011, 9:57:53 PM
 */
package edu.uncc.bioinformatics.quickloadbuilder;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * This GUI class represents one file record.  This is used to allow a user to 
 * create the attributes for each file tag on the Annotations files.
 * @author jfvillal
 */
public class OneRecord extends javax.swing.JPanel {

    /** Creates new form OneRecord */
    public OneRecord() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        TitleTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        UrlTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        DescriptionTextField = new javax.swing.JTextField();
        NameLabel = new javax.swing.JLabel();
        LoadHintCheckBox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        LabelTextField = new javax.swing.JTextField();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edu.uncc.bioinformatics.quickloadbuilder.QuickLoadArchiveBuilderApp.class).getContext().getResourceMap(OneRecord.class);
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        setName("Form"); // NOI18N

        TitleTextField.setText(resourceMap.getString("TitleTextField.text")); // NOI18N
        TitleTextField.setName("TitleTextField"); // NOI18N
        TitleTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TitleTextFieldActionPerformed(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        UrlTextField.setName("UrlTextField"); // NOI18N
        UrlTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UrlTextFieldActionPerformed(evt);
            }
        });

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        DescriptionTextField.setText(resourceMap.getString("DescriptionTextField.text")); // NOI18N
        DescriptionTextField.setName("DescriptionTextField"); // NOI18N

        NameLabel.setText(resourceMap.getString("NameLabel.text")); // NOI18N
        NameLabel.setName("NameLabel"); // NOI18N

        LoadHintCheckBox.setText(resourceMap.getString("LoadHintCheckBox.text")); // NOI18N
        LoadHintCheckBox.setToolTipText(resourceMap.getString("LoadHintCheckBox.toolTipText")); // NOI18N
        LoadHintCheckBox.setName("LoadHintCheckBox"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        LabelTextField.setText(resourceMap.getString("LabelTextField.text")); // NOI18N
        LabelTextField.setName("LabelTextField"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(NameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 664, Short.MAX_VALUE)
                        .addComponent(LoadHintCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(59, 59, 59)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(DescriptionTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TitleTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(LabelTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(66, 66, 66)
                                .addComponent(UrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NameLabel)
                    .addComponent(LoadHintCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(TitleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(DescriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(UrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(LabelTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void TitleTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TitleTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TitleTextFieldActionPerformed

    private void UrlTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UrlTextFieldActionPerformed
        // TODO add your handling code here:
    }

    public JTextField getDescriptionTextField() {
        return DescriptionTextField;
    }

    public JTextField getLabelTextField() {
        return LabelTextField;
    }

    public JCheckBox getLoadHintCheckBox() {
        return LoadHintCheckBox;
    }

    public JLabel getNameLabel() {
        return NameLabel;
    }

    public JTextField getTitleTextField() {
        return TitleTextField;
    }

    public JTextField getUrlTextField() {
        return UrlTextField;
    }//GEN-LAST:event_UrlTextFieldActionPerformed

    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField DescriptionTextField;
    private javax.swing.JTextField LabelTextField;
    private javax.swing.JCheckBox LoadHintCheckBox;
    private javax.swing.JLabel NameLabel;
    private javax.swing.JTextField TitleTextField;
    private javax.swing.JTextField UrlTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    // End of variables declaration//GEN-END:variables
}
