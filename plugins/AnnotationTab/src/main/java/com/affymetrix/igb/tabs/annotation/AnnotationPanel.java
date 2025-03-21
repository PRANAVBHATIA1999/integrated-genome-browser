package com.affymetrix.igb.tabs.annotation;

import com.affymetrix.igb.swing.JRPTextField;
import com.affymetrix.genoviz.swing.NumericFilter;

/**
 *
 * @author hiralv
 */
public abstract class AnnotationPanel extends javax.swing.JPanel {

    protected boolean is_listening = true; // used to turn on and off listening to GUI events

    /**
     * Creates new form AnnotationPanel
     */
    public AnnotationPanel() {
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

        annotationsPanel = new javax.swing.JPanel();
        labelFieldLabel = new javax.swing.JLabel();
        labelFieldComboBox = new javax.swing.JComboBox();
        strands2TracksCheckBox = new javax.swing.JCheckBox();
        strandsArrowCheckBox = new javax.swing.JCheckBox();
        strandsColorCheckBox = new javax.swing.JCheckBox();
        strandsLabel = new javax.swing.JLabel();
        strandsForwardColorLabel = new javax.swing.JLabel();
        strandsReverseColorLabel = new javax.swing.JLabel();
        strandsReverseColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        strandsForwardColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        stackHeightPanel = new javax.swing.JPanel();
        stackDepthTextField = new JRPTextField("trackPreference_maxDepth");
        ((javax.swing.text.AbstractDocument)stackDepthTextField.getDocument()).setDocumentFilter(new NumericFilter.IntegerNumericFilter());
        stackDepthGoButton = new javax.swing.JButton();
        stackDepthAllButton = new javax.swing.JButton();
        trackHeightPanel = new javax.swing.JPanel();
        lockTierHeightCheckBox = new javax.swing.JCheckBox();
        setHeightInPxLabel = new javax.swing.JLabel();
        setPxHeightTextBox = new JRPTextField("trackPreference_tierHeight");
        ((javax.swing.text.AbstractDocument)setPxHeightTextBox.getDocument()).setDocumentFilter(new NumericFilter.IntegerNumericFilter());
        pxGoButton = new javax.swing.JButton();

        annotationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Labels and Strand"));
        annotationsPanel.setToolTipText("Use this to configure how IGB displays labels and strand information.");

        labelFieldLabel.setText("Label Field:");
        labelFieldLabel.setToolTipText("Choose a feature property to label features within the track.");

        labelFieldComboBox.setToolTipText("Choose a feature property to label features within the track.");
        labelFieldComboBox.addActionListener(this::labelFieldComboBoxActionPerformed);

        strands2TracksCheckBox.setText("+/-");
        strands2TracksCheckBox.setToolTipText("Show + and - strand features the same track.");
        strands2TracksCheckBox.addActionListener(this::strands2TracksCheckBoxActionPerformed);

        strandsArrowCheckBox.setText("Arrow");
        strandsArrowCheckBox.setToolTipText("Show strand using arrows.");
        strandsArrowCheckBox.addActionListener(this::strandsArrowCheckBoxActionPerformed);

        strandsColorCheckBox.setText("Color by Strand:");
        strandsColorCheckBox.setToolTipText("Show strand using different colors for + and - strand features.");
        strandsColorCheckBox.addActionListener(this::strandsColorCheckBoxActionPerformed);

        strandsLabel.setText("Strand:");
        strandsLabel.setToolTipText("Configure how IGB represents strand.");

        strandsForwardColorLabel.setText("+");

        strandsReverseColorLabel.setText("-");

        strandsReverseColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        strandsReverseColorComboBox.setToolTipText("Choose color for - strand features.");
        strandsReverseColorComboBox.setButtonVisible(false);
        strandsReverseColorComboBox.setColorValueVisible(false);
        strandsReverseColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        strandsReverseColorComboBox.setStretchToFit(true);
        strandsReverseColorComboBox.addActionListener(this::strandsReverseColorComboBoxActionPerformed);

        strandsForwardColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        strandsForwardColorComboBox.setToolTipText("Choose color for + strand features.");
        strandsForwardColorComboBox.setButtonVisible(false);
        strandsForwardColorComboBox.setColorValueVisible(false);
        strandsForwardColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        strandsForwardColorComboBox.setStretchToFit(true);
        strandsForwardColorComboBox.addActionListener(this::strandsForwardColorComboBoxActionPerformed);

        org.jdesktop.layout.GroupLayout annotationsPanelLayout = new org.jdesktop.layout.GroupLayout(annotationsPanel);
        annotationsPanel.setLayout(annotationsPanelLayout);
        annotationsPanelLayout.setHorizontalGroup(
            annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(annotationsPanelLayout.createSequentialGroup()
                .add(20, 20, 20)
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(annotationsPanelLayout.createSequentialGroup()
                        .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelFieldLabel)
                            .add(strandsLabel))
                        .add(0, 0, 0)
                        .add(labelFieldComboBox, 0, 143, Short.MAX_VALUE))
                    .add(annotationsPanelLayout.createSequentialGroup()
                        .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(strandsArrowCheckBox)
                            .add(annotationsPanelLayout.createSequentialGroup()
                                .add(strandsColorCheckBox)
                                .add(5, 5, 5)))
                        .add(0, 76, Short.MAX_VALUE))
                    .add(annotationsPanelLayout.createSequentialGroup()
                        .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(strands2TracksCheckBox)
                            .add(annotationsPanelLayout.createSequentialGroup()
                                .add(30, 30, 30)
                                .add(strandsForwardColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(strandsForwardColorLabel)
                                .add(18, 18, 18)
                                .add(strandsReverseColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(strandsReverseColorLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(107, Short.MAX_VALUE))))
        );
        annotationsPanelLayout.setVerticalGroup(
            annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(annotationsPanelLayout.createSequentialGroup()
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelFieldLabel)
                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15)
                .add(strandsLabel)
                .add(5, 5, 5)
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(strands2TracksCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(strandsArrowCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(strandsColorCheckBox)
                .add(0, 0, 0)
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(strandsForwardColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(strandsForwardColorLabel)
                    .add(strandsReverseColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(strandsReverseColorLabel))
                .add(0, 0, 0))
        );

        stackHeightPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Stack Height"));

        stackDepthTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        stackDepthTextField.addActionListener(this::stackDepthTextFieldActionPerformed);

        stackDepthGoButton.setText("Go");
        stackDepthGoButton.setToolTipText("Click to run operation on selected tracks.");
        stackDepthGoButton.addActionListener(this::stackDepthGoButtonActionPerformed);

        stackDepthAllButton.setText("Optimize");
        stackDepthAllButton.setToolTipText("Set stack height to show everything. ");
        stackDepthAllButton.addActionListener(this::stackDepthAllButtonActionPerformed);

        org.jdesktop.layout.GroupLayout stackHeightPanelLayout = new org.jdesktop.layout.GroupLayout(stackHeightPanel);
        stackHeightPanel.setLayout(stackHeightPanelLayout);
        stackHeightPanelLayout.setHorizontalGroup(
            stackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, stackHeightPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(stackDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(stackDepthGoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(stackDepthAllButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                .add(0, 0, 0))
        );
        stackHeightPanelLayout.setVerticalGroup(
            stackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, stackHeightPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(stackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stackDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(stackDepthGoButton)
                    .add(stackDepthAllButton))
                .add(0, 0, 0))
        );

        trackHeightPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Track Height"));

        lockTierHeightCheckBox.setText("Lock Track Height (Pixels)");
        lockTierHeightCheckBox.setToolTipText("Lock track height to the value entered below. ");
        lockTierHeightCheckBox.addActionListener(this::lockTierHeightCheckBoxActionPerformed);

        setHeightInPxLabel.setText("Set Height (Pixels):");

        setPxHeightTextBox.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        setPxHeightTextBox.addActionListener(this::setPxHeightTextBoxActionPerformed);

        pxGoButton.setText("Go");
        pxGoButton.setToolTipText("Click to run operation on selected tracks.");
        pxGoButton.addActionListener(this::pxGoButtonActionPerformed);

        org.jdesktop.layout.GroupLayout trackHeightPanelLayout = new org.jdesktop.layout.GroupLayout(trackHeightPanel);
        trackHeightPanel.setLayout(trackHeightPanelLayout);
        trackHeightPanelLayout.setHorizontalGroup(
            trackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(trackHeightPanelLayout.createSequentialGroup()
                .add(trackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lockTierHeightCheckBox)
                    .add(trackHeightPanelLayout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(trackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(setHeightInPxLabel)
                            .add(trackHeightPanelLayout.createSequentialGroup()
                                .add(setPxHeightTextBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, 0)
                                .add(pxGoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .add(16, 16, 16))
        );
        trackHeightPanelLayout.setVerticalGroup(
            trackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(trackHeightPanelLayout.createSequentialGroup()
                .add(lockTierHeightCheckBox)
                .add(5, 5, 5)
                .add(setHeightInPxLabel)
                .add(0, 0, 0)
                .add(trackHeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(setPxHeightTextBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pxGoButton))
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(stackHeightPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(trackHeightPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0)
                .add(annotationsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(annotationsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(stackHeightPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(trackHeightPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void stackDepthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackDepthTextFieldActionPerformed
        if (is_listening) {
            stackDepthTextFieldActionPerformedA(evt);
        }
    }//GEN-LAST:event_stackDepthTextFieldActionPerformed

    private void labelFieldComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelFieldComboBoxActionPerformed
        if (is_listening) {
            labelFieldComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_labelFieldComboBoxActionPerformed

    private void strands2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strands2TracksCheckBoxActionPerformed
        if (is_listening) {
            strands2TracksCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strands2TracksCheckBoxActionPerformed

    private void strandsArrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsArrowCheckBoxActionPerformed
        if (is_listening) {
            strandsArrowCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsArrowCheckBoxActionPerformed

    private void strandsColorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsColorCheckBoxActionPerformed
        if (is_listening) {
            strandsColorCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsColorCheckBoxActionPerformed

    private void strandsReverseColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsReverseColorComboBoxActionPerformed
        if (is_listening) {
            strandsReverseColorComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsReverseColorComboBoxActionPerformed

    private void strandsForwardColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsForwardColorComboBoxActionPerformed
        if (is_listening) {
            strandsForwardColorComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsForwardColorComboBoxActionPerformed

    private void stackDepthGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackDepthGoButtonActionPerformed
        if (is_listening) {
            stackDepthGoButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_stackDepthGoButtonActionPerformed

    private void stackDepthAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackDepthAllButtonActionPerformed
        if (is_listening) {
            stackDepthAllButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_stackDepthAllButtonActionPerformed

    private void setPxHeightTextBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setPxHeightTextBoxActionPerformed
        if (is_listening) {
            setPxHeightTextBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_setPxHeightTextBoxActionPerformed

    private void pxGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pxGoButtonActionPerformed
        if (is_listening) {
            pxGoButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_pxGoButtonActionPerformed

    private void lockTierHeightCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lockTierHeightCheckBoxActionPerformed
        if (is_listening) {
            lockTierHeightCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_lockTierHeightCheckBoxActionPerformed

// getters generated by Eclipse, NetBeans balked: "Read-only block of text cannot be refactored."
    public javax.swing.JPanel getAnnotationsPanel() {
        return annotationsPanel;
    }

    public javax.swing.JComboBox getLabelFieldComboBox() {
        return labelFieldComboBox;
    }

    public javax.swing.JLabel getLabelFieldLabel() {
        return labelFieldLabel;
    }

    public javax.swing.JTextField getStackDepthTextField() {
        return stackDepthTextField;
    }

    public javax.swing.JCheckBox getStrands2TracksCheckBox() {
        return strands2TracksCheckBox;
    }

    public javax.swing.JCheckBox getStrandsArrowCheckBox() {
        return strandsArrowCheckBox;
    }

    public javax.swing.JCheckBox getStrandsColorCheckBox() {
        return strandsColorCheckBox;
    }

    public com.jidesoft.combobox.ColorComboBox getStrandsForwardColorComboBox() {
        return strandsForwardColorComboBox;
    }

    public javax.swing.JLabel getStrandsForwardColorLabel() {
        return strandsForwardColorLabel;
    }

    public javax.swing.JLabel getStrandsLabel() {
        return strandsLabel;
    }

    public com.jidesoft.combobox.ColorComboBox getStrandsReverseColorComboBox() {
        return strandsReverseColorComboBox;
    }

    public javax.swing.JLabel getStrandsReverseColorLabel() {
        return strandsReverseColorLabel;
    }

    public javax.swing.JButton getStackDepthAllButton() {
        return stackDepthAllButton;
    }

    public javax.swing.JButton getStackDepthGoButton() {
        return stackDepthGoButton;
    }

    public javax.swing.JCheckBox getLockTierHeightCheckBox() {
        return lockTierHeightCheckBox;
    }

    public javax.swing.JButton getPxGoButton() {
        return pxGoButton;
    }

    public javax.swing.JLabel getSetHeightInPxLabel() {
        return setHeightInPxLabel;
    }

    public javax.swing.JTextField getSetPxHeightTextBox() {
        return setPxHeightTextBox;
    }

    public javax.swing.JPanel getStackHeightPanel() {
        return stackHeightPanel;
    }

    public javax.swing.JPanel getTrackHeightPanel() {
        return trackHeightPanel;
    }

    // you can "generate" these by copying all the event handlers
    // into your text processor and globally changing (must handle regex)
    // "ActionPerformed" to "ActionPerformedA"
    // "private" to "protected abstract"
    // "// TODO add your handling code here:" to ""
    // "                                     
    // "       
    protected abstract void stackDepthTextFieldActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void labelFieldComboBoxActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void strands2TracksCheckBoxActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void strandsArrowCheckBoxActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void strandsColorCheckBoxActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void strandsReverseColorComboBoxActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void strandsForwardColorComboBoxActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void stackDepthGoButtonActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void stackDepthAllButtonActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void setPxHeightTextBoxActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void pxGoButtonActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void lockTierHeightCheckBoxActionPerformedA(java.awt.event.ActionEvent evt);

    // you can "generate" these by copying all the event handlers
    // into your text processor and globally changing (must handle regex)
    // "ActionPerformed" to "Reset"
    // "private" to "protected abstract"
    // "java.awt.event.ActionEvent evt" to ""
    // "// TODO add your handling code here:" to ""
    // "                                     
    // "
    protected abstract void stackDepthTextFieldReset();

    protected abstract void labelFieldComboBoxReset();

    protected abstract void strands2TracksCheckBoxReset();

    protected abstract void strandsArrowCheckBoxReset();

    protected abstract void strandsColorCheckBoxReset();

    protected abstract void strandsReverseColorComboBoxReset();

    protected abstract void strandsForwardColorComboBoxReset();

    protected abstract void stackDepthGoButtonReset();

    protected abstract void stackDepthAllButtonReset();

    protected abstract void lockTierHeightCheckBoxReset();

    protected abstract void setPxHeightTextBoxReset();

    protected abstract void pxGoButtonReset();

    protected final void resetAll() {
        is_listening = false;
        //getStylePanel().setEnabled(allStyles.size() > 0);
        //getAnnotationsPanel().setEnabled(annotStyles.size() > 0);
        //getGraphPanel().setEnabled(graphStates.size() > 0);
        stackDepthTextFieldReset();
        labelFieldComboBoxReset();
        strands2TracksCheckBoxReset();
        strandsArrowCheckBoxReset();
        strandsColorCheckBoxReset();
        strandsReverseColorComboBoxReset();
        strandsForwardColorComboBoxReset();
        stackDepthGoButtonReset();
        stackDepthAllButtonReset();
        lockTierHeightCheckBoxReset();
        setPxHeightTextBoxReset();
        pxGoButtonReset();

        is_listening = true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel annotationsPanel;
    private javax.swing.JComboBox labelFieldComboBox;
    private javax.swing.JLabel labelFieldLabel;
    private javax.swing.JCheckBox lockTierHeightCheckBox;
    private javax.swing.JButton pxGoButton;
    private javax.swing.JLabel setHeightInPxLabel;
    private javax.swing.JTextField setPxHeightTextBox;
    private javax.swing.JButton stackDepthAllButton;
    private javax.swing.JButton stackDepthGoButton;
    private javax.swing.JTextField stackDepthTextField;
    private javax.swing.JPanel stackHeightPanel;
    private javax.swing.JCheckBox strands2TracksCheckBox;
    private javax.swing.JCheckBox strandsArrowCheckBox;
    private javax.swing.JCheckBox strandsColorCheckBox;
    private com.jidesoft.combobox.ColorComboBox strandsForwardColorComboBox;
    private javax.swing.JLabel strandsForwardColorLabel;
    private javax.swing.JLabel strandsLabel;
    private com.jidesoft.combobox.ColorComboBox strandsReverseColorComboBox;
    private javax.swing.JLabel strandsReverseColorLabel;
    private javax.swing.JPanel trackHeightPanel;
    // End of variables declaration//GEN-END:variables

}
