package com.affymetrix.igb.util;

import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genoviz.util.ErrorHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author auser
 */
public class BugOrFeatureRequestForm extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private static String URL = "http://bioviz.org/bugreporter/report";

    /**
     * Creates new form BugOrRequestFeatureForm1
     */
    private boolean feature;

    public BugOrFeatureRequestForm(boolean feature) {
        this.feature = feature;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        bugButton = new javax.swing.JRadioButton();
        this.setTitle(feature?"Request A Feature" : "Report A Bug");
        featureButton = new javax.swing.JRadioButton();
        summLabel = new javax.swing.JLabel();
        summField = new javax.swing.JTextField();
        descLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descArea = new javax.swing.JTextArea();
        submit = new javax.swing.JButton();
        errLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        typeLabel.setText("Type :");

        buttonGroup.add(bugButton);
        bugButton.setText("Bug");
        bugButton.setSelected(!feature);
        bugButton.addActionListener(this::bugButtonActionPerformed);

        buttonGroup.add(featureButton);
        featureButton.setText("Feature");
        featureButton.setSelected(feature);
        featureButton.addActionListener(this::featureButtonActionPerformed);

        summLabel.setText("Summary* :");

        descLabel.setText("Description :");

        descArea.setColumns(20);
        descArea.setRows(5);
        jScrollPane1.setViewportView(descArea);

        submit.setText("Submit");
        submit.addActionListener(this::submitActionPerformed);

        errLabel.setForeground(new java.awt.Color(255, 0, 51));
        errLabel.setText("*Summary Required");
        errLabel.setVisible(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(errLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(submit))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(summLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(summField))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(typeLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(bugButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(featureButton))
                                    .add(descLabel))
                                .add(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bugButton)
                    .add(typeLabel)
                    .add(featureButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summLabel)
                    .add(summField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(descLabel)
                .add(1, 1, 1)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(submit)
                    .add(errLabel)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bugButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bugButtonActionPerformed
        feature = false;
        this.setTitle("Report A Bug");
    }//GEN-LAST:event_bugButtonActionPerformed

    private void featureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_featureButtonActionPerformed
        feature = true;
        this.setTitle("Request A Feature");
    }//GEN-LAST:event_featureButtonActionPerformed

    private void submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitActionPerformed
        errLabel.setVisible(false);
        if (summField.getText().isEmpty()) {
            errLabel.setVisible(true);
            return;
        }
        this.setVisible(false);
        try {
            if (feature) {
                newReport("newFeature", summField.getText(), descArea.getText(), "IGB_7_0_0");
            } else {
                newReport("bug", summField.getText(), descArea.getText(), "IGB_7_0_0");
            }
        } catch (IOException ex) {
            ErrorHandler.errorPanel("Unable to send " + (feature ? "new feature request." : "bug report."), ex);
        }
    }//GEN-LAST:event_submitActionPerformed

    private static String newReport(String type, String summary, String description, String version)
            throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("type", type);
        params.put("summary", summary);
        params.put("description", description);
        params.put("version", version);

        return LocalUrlCacher.httpPost(URL, params);
    }

    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bugButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JTextArea descArea;
    private javax.swing.JLabel descLabel;
    private javax.swing.JLabel errLabel;
    private javax.swing.JRadioButton featureButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton submit;
    private javax.swing.JTextField summField;
    private javax.swing.JLabel summLabel;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
