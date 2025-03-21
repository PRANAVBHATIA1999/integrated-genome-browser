package com.affymetrix.igb.prefs;

import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.event.GenomeVersionSelectionEvent;
import com.affymetrix.genometry.event.GroupSelectionListener;
import com.affymetrix.genometry.event.SeqMapRefreshed;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.view.SeqMapView;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

public class TierPrefsViewGUI extends JPanel implements WindowListener, SeqMapRefreshed, GroupSelectionListener {

    private static final long serialVersionUID = 1L;
    public TierPrefsView tpv;
    private SeqMapView smv;

    /**
     * Creates new form PrototypeOne
     */
    public TierPrefsViewGUI() {

        super();
        this.setName("Tracks");
        this.setToolTipText("Set Track Properties");

        tpv = TierPrefsView.getSingleton();

        IGB igb = IGB.getInstance();
        if (igb != null) {
            smv = igb.getMapView();
            smv.addToRefreshList(this);
        }

        GenometryModel.getInstance().addGroupSelectionListener(this);
        initComponents();
        validate();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectTrackPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = tpv.table;
        restoreToDefaultButton = new javax.swing.JButton();
        selectAllButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        refreshButton = tpv.refreshButton;
        showStrandPanel = new javax.swing.JPanel();
        possitiveLabel = new javax.swing.JLabel();
        negativeLabel = new javax.swing.JLabel();
        possitiveColorComboBox = tpv.possitiveColorComboBox;
        negativeColorComboBox = tpv.negativeColorComboBox;
        colorCheckBox = tpv.colorCheckBox;
        arrowCheckBox = tpv.arrowCheckBox;
        autoRefreshCheckBox = tpv.autoRefreshCheckBox;
        propertiesPanel = new javax.swing.JPanel();
        displayNameLabel = new javax.swing.JLabel();
        displayNameTextField = tpv.displayNameTextField;
        bgLabel = new javax.swing.JLabel();
        bgColorComboBox = tpv.bgColorComboBox;
        trackNameSizeLabel = new javax.swing.JLabel();
        trackNameSizeComboBox = tpv.trackNameSizeComboBox;
        labelFieldLabel = new javax.swing.JLabel();
        fgLabel = new javax.swing.JLabel();
        fgColorComboBox = tpv.fgColorComboBox;
        labelFieldComboBox = tpv.labelFieldComboBox;
        maxDepthLabel = new javax.swing.JLabel();
        maxDepthTextField = tpv.maxDepthTextField;
        show2TracksCheckBox = tpv.show2TracksCheckBox;
        collapsedCheckBox = tpv.collapsedCheckBox;
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        applyButton = new javax.swing.JButton();

        selectTrackPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Track List"));

        jScrollPane1.setViewportView(table);
        tpv.refreshList();

        restoreToDefaultButton.setText("Restore to default");
        restoreToDefaultButton.setToolTipText("Restore selected tracks to default setting.");
        restoreToDefaultButton.addActionListener(this::restoreToDefaultButtonActionPerformed);

        selectAllButton.setText("Select All");
        selectAllButton.addActionListener(this::selectAllButtonActionPerformed);

        org.jdesktop.layout.GroupLayout selectTrackPanelLayout = new org.jdesktop.layout.GroupLayout(selectTrackPanel);
        selectTrackPanel.setLayout(selectTrackPanelLayout);
        selectTrackPanelLayout.setHorizontalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, selectTrackPanelLayout.createSequentialGroup()
                .add(selectAllButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(restoreToDefaultButton))
        );
        selectTrackPanelLayout.setVerticalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, selectTrackPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(restoreToDefaultButton)
                    .add(selectAllButton)))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        refreshButton.setText("Refresh");

        showStrandPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Strand"));

        possitiveLabel.setText("+");

        negativeLabel.setText("-");

        possitiveColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        possitiveColorComboBox.setButtonVisible(false);
        possitiveColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        possitiveColorComboBox.setStretchToFit(true);
        possitiveColorComboBox.addActionListener(this::possitiveColorComboBoxActionPerformed);

        negativeColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        negativeColorComboBox.setButtonVisible(false);
        negativeColorComboBox.setColorValueVisible(false);
        negativeColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        negativeColorComboBox.setStretchToFit(true);
        negativeColorComboBox.addActionListener(this::negativeColorComboBoxActionPerformed);

        colorCheckBox.setText("Color");
        colorCheckBox.addActionListener(this::colorCheckBoxActionPerformed);

        arrowCheckBox.setText("Arrow");
        arrowCheckBox.addActionListener(this::arrowCheckBoxActionPerformed);

        org.jdesktop.layout.GroupLayout showStrandPanelLayout = new org.jdesktop.layout.GroupLayout(showStrandPanel);
        showStrandPanel.setLayout(showStrandPanelLayout);
        showStrandPanelLayout.setHorizontalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(showStrandPanelLayout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(possitiveLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(possitiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negativeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(colorCheckBox)
                    .add(arrowCheckBox))
                .addContainerGap())
        );
        showStrandPanelLayout.setVerticalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(arrowCheckBox)
                .add(5, 5, 5)
                .add(colorCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(possitiveLabel)
                    .add(possitiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(negativeLabel)
                    .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        autoRefreshCheckBox.setText("Auto Refresh");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(autoRefreshCheckBox)
            .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 71, Short.MAX_VALUE)
                .add(autoRefreshCheckBox)
                .add(5, 5, 5)
                .add(refreshButton))
        );

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));

        displayNameLabel.setText("Track Name:");

        displayNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                displayNameTextFieldKeyReleased(evt);
            }
        });

        bgLabel.setText("Background:");

        bgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        bgColorComboBox.setButtonVisible(false);
        bgColorComboBox.setColorValueVisible(false);
        bgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        bgColorComboBox.setStretchToFit(true);
        bgColorComboBox.addActionListener(this::bgColorComboBoxActionPerformed);

        trackNameSizeLabel.setText("Name Size:");

        trackNameSizeComboBox.addActionListener(this::trackNameSizeComboBoxActionPerformed);

        labelFieldLabel.setText("Label Field:");

        fgLabel.setText("Foreground:");

        fgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        fgColorComboBox.setButtonVisible(false);
        fgColorComboBox.setColorValueVisible(false);
        fgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        fgColorComboBox.setStretchToFit(true);
        fgColorComboBox.addActionListener(this::fgColorComboBoxActionPerformed);

        labelFieldComboBox.addActionListener(this::labelFieldComboBoxActionPerformed);

        maxDepthLabel.setText("Max Stack Depth:");

        maxDepthTextField.addActionListener(this::maxDepthTextFieldActionPerformed);

        show2TracksCheckBox.setText("Show 2 tracks (+/-)");
        show2TracksCheckBox.addActionListener(this::show2TracksCheckBoxActionPerformed);

        collapsedCheckBox.setText("Collapsed");
        collapsedCheckBox.addActionListener(this::collapsedCheckBoxActionPerformed);

        applyButton.setText("Apply");
        applyButton.setToolTipText("Apply max stack depth value to track");
        applyButton.addActionListener(this::applyButtonActionPerformed);

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSeparator1)
            .add(jSeparator2)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(92, 92, 92)
                .add(displayNameTextField))
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(bgLabel)
                                    .add(displayNameLabel)
                                    .add(fgLabel))
                                .add(6, 6, 6)
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(maxDepthLabel))
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(30, 30, 30)
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(labelFieldLabel)
                                    .add(trackNameSizeLabel))
                                .add(6, 6, 6)
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(labelFieldComboBox, 0, 109, Short.MAX_VALUE)
                                    .add(trackNameSizeComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(6, 6, 6)
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(collapsedCheckBox)
                                    .add(applyButton)))))
                    .add(show2TracksCheckBox))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(displayNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(7, 7, 7)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(bgLabel)
                    .add(trackNameSizeLabel)
                    .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(fgLabel)
                    .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelFieldLabel)
                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(7, 7, 7)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxDepthLabel)
                    .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(applyButton))
                .add(6, 6, 6)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(show2TracksCheckBox)
                    .add(collapsedCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(1, 1, 1))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(propertiesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(15, 15, 15)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(15, 15, 15))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15))
        );

        layout.linkSize(new java.awt.Component[] {jPanel1, propertiesPanel}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void show2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show2TracksCheckBoxActionPerformed
        tpv.show2TracksCheckBox();
}//GEN-LAST:event_show2TracksCheckBoxActionPerformed

	private void collapsedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapsedCheckBoxActionPerformed
        tpv.collapsedCheckBox();
	}//GEN-LAST:event_collapsedCheckBoxActionPerformed

	private void fgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorComboBoxActionPerformed
        tpv.fgColorComboBox();
}//GEN-LAST:event_fgColorComboBoxActionPerformed

	private void bgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorComboBoxActionPerformed
        tpv.bgColorComboBox();
}//GEN-LAST:event_bgColorComboBoxActionPerformed

	private void labelFieldComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelFieldComboBoxActionPerformed
        tpv.labelFieldComboBox();
}//GEN-LAST:event_labelFieldComboBoxActionPerformed

	private void possitiveColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_possitiveColorComboBoxActionPerformed
        tpv.possitiveColorComboBox();
}//GEN-LAST:event_possitiveColorComboBoxActionPerformed

	private void negativeColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_negativeColorComboBoxActionPerformed
        tpv.negativeColorComboBox();
}//GEN-LAST:event_negativeColorComboBoxActionPerformed

	private void colorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorCheckBoxActionPerformed
        tpv.colorCheckBox();
}//GEN-LAST:event_colorCheckBoxActionPerformed

	private void arrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowCheckBoxActionPerformed
        tpv.arrowCheckBox();
}//GEN-LAST:event_arrowCheckBoxActionPerformed

	private void restoreToDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreToDefaultButtonActionPerformed
        tpv.restoreToDefault();
	}//GEN-LAST:event_restoreToDefaultButtonActionPerformed

	private void displayNameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_displayNameTextFieldKeyReleased
        tpv.displayNameTextField();
	}//GEN-LAST:event_displayNameTextFieldKeyReleased

	private void trackNameSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackNameSizeComboBoxActionPerformed
        tpv.trackNameSizeComboBox();
	}//GEN-LAST:event_trackNameSizeComboBoxActionPerformed

	private void maxDepthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxDepthTextFieldActionPerformed
        tpv.maxDepthTextField();
	}//GEN-LAST:event_maxDepthTextFieldActionPerformed

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        tpv.maxDepthTextField();
	}//GEN-LAST:event_applyButtonActionPerformed

	private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        tpv.selectAll();
	}//GEN-LAST:event_selectAllButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JCheckBox arrowCheckBox;
    private javax.swing.JCheckBox autoRefreshCheckBox;
    private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JCheckBox collapsedCheckBox;
    private javax.swing.JCheckBox colorCheckBox;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField displayNameTextField;
    private com.jidesoft.combobox.ColorComboBox fgColorComboBox;
    private javax.swing.JLabel fgLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JComboBox labelFieldComboBox;
    private javax.swing.JLabel labelFieldLabel;
    private javax.swing.JLabel maxDepthLabel;
    private javax.swing.JTextField maxDepthTextField;
    private com.jidesoft.combobox.ColorComboBox negativeColorComboBox;
    private javax.swing.JLabel negativeLabel;
    private com.jidesoft.combobox.ColorComboBox possitiveColorComboBox;
    private javax.swing.JLabel possitiveLabel;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton restoreToDefaultButton;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JPanel selectTrackPanel;
    private javax.swing.JCheckBox show2TracksCheckBox;
    private javax.swing.JPanel showStrandPanel;
    private javax.swing.JTable table;
    private javax.swing.JComboBox trackNameSizeComboBox;
    private javax.swing.JLabel trackNameSizeLabel;
    // End of variables declaration//GEN-END:variables

    // implementation of IPrefEditorComponent
    public void refresh() {
        tpv.refreshList();
    }

    public void destroy() {
        removeAll();
        if (tpv.lsm != null) {
            tpv.lsm.removeListSelectionListener(tpv);
        }
    }

    public void mapRefresh() {
        if (isVisible()) {
            tpv.refreshList();
        }
    }

    public void groupSelectionChanged(GenomeVersionSelectionEvent evt) {
        mapRefresh();
    }

    private void stopEditing() {
        if (table != null && table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            stopEditing();
        }
    }

    public void windowClosed(WindowEvent e) {
        stopEditing();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void valueChanged(ListSelectionEvent lse) {
        tpv.valueChanged(lse);
    }
}
