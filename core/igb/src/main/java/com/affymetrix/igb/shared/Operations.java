package com.affymetrix.igb.shared;

import com.affymetrix.genoviz.swing.NumericFilter;
import com.affymetrix.igb.swing.JRPButton;
import com.affymetrix.igb.swing.JRPComboBoxWithSingleListener;
import com.affymetrix.igb.swing.JRPTextField;

public abstract class Operations extends javax.swing.JPanel {

    protected boolean is_listening = true; // used to turn on and off listening to GUI events
    private static final long serialVersionUID = 1L;

    public Operations() {
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

        stPanel = new javax.swing.JPanel();
        transformationGoB = new JRPButton("TrackOperationsTab_transformationGoB");
        transformationParam = new JRPTextField("TrackOperationsTab_transformParam");
        transformationCB = new JRPComboBoxWithSingleListener("TrackOperationsTab_transformation");
        transformationParamLabel = new javax.swing.JLabel();
        singleTrackLabel = new javax.swing.JLabel();
        mtPanel = new javax.swing.JPanel();
        operationCB = new JRPComboBoxWithSingleListener("TrackOperationsTab_operation");
        operationParam = new JRPTextField("TrackOperationsTab_operationParam");
        operationGoB = new JRPButton("TrackOperationsTab_operationGoB");
        operationParamLabel = new javax.swing.JLabel();
        multiTrackLabel = new javax.swing.JLabel();
        btPanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Operations"));

        transformationGoB.setText("Apply");
        transformationGoB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transformationGoBActionPerformed(evt);
            }
        });

        transformationParam.setEditable(false);
        ((javax.swing.text.AbstractDocument)transformationParam.getDocument()).setDocumentFilter(new NumericFilter.FloatNumericFilter());

        transformationParamLabel.setText(null);
        transformationParamLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        transformationParamLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        transformationParamLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        singleTrackLabel.setText("Single-Track:");

        org.jdesktop.layout.GroupLayout stPanelLayout = new org.jdesktop.layout.GroupLayout(stPanel);
        stPanel.setLayout(stPanelLayout);
        stPanelLayout.setHorizontalGroup(
            stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(stPanelLayout.createSequentialGroup()
                .add(stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(stPanelLayout.createSequentialGroup()
                        .add(transformationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(transformationGoB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(stPanelLayout.createSequentialGroup()
                        .add(singleTrackLabel)
                        .add(5, 5, 5)
                        .add(transformationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(transformationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 0, 0))
        );
        stPanelLayout.setVerticalGroup(
            stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(stPanelLayout.createSequentialGroup()
                .add(stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(singleTrackLabel)
                    .add(transformationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transformationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0)
                .add(stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(transformationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transformationGoB)))
        );

        operationParam.setEditable(false);
        ((javax.swing.text.AbstractDocument)operationParam.getDocument()).setDocumentFilter(new NumericFilter.FloatNumericFilter());

        operationGoB.setText("Apply");
        operationGoB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operationGoBActionPerformed(evt);
            }
        });

        operationParamLabel.setText(null);
        operationParamLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        operationParamLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        operationParamLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        multiTrackLabel.setText("Multi-Track:");

        org.jdesktop.layout.GroupLayout mtPanelLayout = new org.jdesktop.layout.GroupLayout(mtPanel);
        mtPanel.setLayout(mtPanelLayout);
        mtPanelLayout.setHorizontalGroup(
            mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mtPanelLayout.createSequentialGroup()
                .add(mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mtPanelLayout.createSequentialGroup()
                        .add(operationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(operationGoB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mtPanelLayout.createSequentialGroup()
                        .add(multiTrackLabel)
                        .add(15, 15, 15)
                        .add(operationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(operationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 0, 0))
        );
        mtPanelLayout.setVerticalGroup(
            mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mtPanelLayout.createSequentialGroup()
                .add(mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(multiTrackLabel)
                        .add(operationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(operationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0)
                .add(mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(operationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(operationGoB))
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout btPanelLayout = new org.jdesktop.layout.GroupLayout(btPanel);
        btPanel.setLayout(btPanelLayout);
        btPanelLayout.setHorizontalGroup(
            btPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 181, Short.MAX_VALUE)
        );
        btPanelLayout.setVerticalGroup(
            btPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(stPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mtPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(stPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(mtPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(btPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void transformationGoBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transformationGoBActionPerformed
        if (is_listening) {
            transformationGoBActionPerformedA(evt);
        }
    }//GEN-LAST:event_transformationGoBActionPerformed

    private void operationGoBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_operationGoBActionPerformed
        if (is_listening) {
            operationGoBActionPerformedA(evt);
        }
    }//GEN-LAST:event_operationGoBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btPanel;
    private javax.swing.JPanel mtPanel;
    private javax.swing.JLabel multiTrackLabel;
    private javax.swing.JComboBox operationCB;
    private javax.swing.JButton operationGoB;
    private javax.swing.JTextField operationParam;
    private javax.swing.JLabel operationParamLabel;
    private javax.swing.JLabel singleTrackLabel;
    private javax.swing.JPanel stPanel;
    private javax.swing.JComboBox transformationCB;
    private javax.swing.JButton transformationGoB;
    private javax.swing.JTextField transformationParam;
    private javax.swing.JLabel transformationParamLabel;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JPanel getBtPanel() {
        return btPanel;
    }

    public javax.swing.JPanel getMtPanel() {
        return mtPanel;
    }

    public javax.swing.JComboBox getOperationCB() {
        return operationCB;
    }

    public javax.swing.JButton getOperationGoB() {
        return operationGoB;
    }

    public javax.swing.JTextField getOperationParam() {
        return operationParam;
    }

    public javax.swing.JLabel getOperationParamLabel() {
        return operationParamLabel;
    }

    public javax.swing.JPanel getStPanel() {
        return stPanel;
    }

    public javax.swing.JComboBox getTransformationCB() {
        return transformationCB;
    }

    public javax.swing.JButton getTransformationGoB() {
        return transformationGoB;
    }

    public javax.swing.JTextField getTransformationParam() {
        return transformationParam;
    }

    public javax.swing.JLabel getTransformationParamLabel() {
        return transformationParamLabel;
    }

    public javax.swing.JLabel getMultiTrackLabel() {
        return multiTrackLabel;
    }

    public javax.swing.JLabel getSingleTrackLabel() {
        return singleTrackLabel;
    }

    protected abstract void transformationGoBActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void operationGoBActionPerformedA(java.awt.event.ActionEvent evt);

}
