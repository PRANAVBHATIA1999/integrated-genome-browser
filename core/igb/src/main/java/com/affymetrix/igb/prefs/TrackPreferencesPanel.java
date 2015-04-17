/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.prefs;

import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.event.GroupSelectionEvent;
import com.affymetrix.genometry.event.GroupSelectionListener;
import com.affymetrix.genometry.event.SeqMapRefreshed;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.view.SeqMapView;
import com.lorainelab.igb.services.window.preferences.PreferencesPanelProvider;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JPanel;

/**
 *
 * @author Anuj
 */
public abstract class TrackPreferencesPanel extends JPanel implements SeqMapRefreshed, WindowListener, GroupSelectionListener, PreferencesPanelProvider {

    private static final long serialVersionUID = 1L;
    /**
     * This is the panel which is used for both Preferences-> Tracks tab and
     * Preferences->Track Defaults Tab
     * It replaced the use of TierPrefsViewGUI and TrackDefaultViewGUI classes
     */
    public TrackPreferences tdv;
    public boolean pref;
    public javax.swing.JTable dtable;
    public javax.swing.JTextField tracknametype;
    public javax.swing.JLabel tracknametypeLabel;
    public javax.swing.JButton addButton, deleteButton;
    public SeqMapView smv;

    public TrackPreferencesPanel(String title, TrackPreferences tdv) {
        super();
        this.tdv = tdv;
        this.setName(title);
        dtable = tdv.table;
        IGB igb = IGB.getInstance();
        if (igb != null) {
            smv = igb.getMapView();
            smv.addToRefreshList(this);
        }
        tracknametype = tdv.getTrackDefaultTextField();
        GenometryModel.getInstance().addGroupSelectionListener(this);
        initComponents();
        enableSpecificComponents();
    }

    protected abstract void enableSpecificComponents();

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox6 = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        tablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = dtable;
        selectAndAddButton = new javax.swing.JButton();
        deleteAndRestoreButton = new javax.swing.JButton();
        showStrandPanel = new javax.swing.JPanel();
        arrowCheckBox = tdv.arrowCheckBox;
        colorCheckBox = tdv.colorCheckBox;
        jLabel8 = new javax.swing.JLabel();
        positiveColorComboBox = tdv.possitiveColorComboBox;
        jLabel9 = new javax.swing.JLabel();
        negativeColorComboBox = tdv.negativeColorComboBox;
        show2TracksCheckBox = tdv.show2TracksCheckBox;
        jPanel1 = new javax.swing.JPanel();
        refreshButton = tdv.refreshButton;
        autoRefreshCheckBox = tdv.autoRefreshCheckBox;
        propertiesPanel = new javax.swing.JPanel();
        trackTypeNameLabel = new javax.swing.JLabel();
        trackNameTypeTextField = tracknametype;
        bgColorLabel = new javax.swing.JLabel();
        bgColorComboBox = tdv.bgColorComboBox;
        fgColorLabel = new javax.swing.JLabel();
        fgColorComboBox = tdv.fgColorComboBox;
        nameSizeLabel = new javax.swing.JLabel();
        nameSizeComboBox = tdv.trackNameSizeComboBox;
        FieldLabel = new javax.swing.JLabel();
        labelFieldComboBox = tdv.labelFieldComboBox;
        labelColorComboBox = tdv.labelColorComboBox;
        labelColorLabel = new javax.swing.JLabel();
        applyButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        annotationLabelSizeCombobox = tdv.getAnnotationLabelSizeComboBox();
        annotationLabelAutoResizeCheckbox = tdv.getannotationLabelAutoSizeCheckBox();

        jButton2.setText("jButton2");

        jCheckBox2.setText("jCheckBox2");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        jCheckBox6.setText("jCheckBox6");

        jLabel7.setText("jLabel7");

        tablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(tdv.title));

        table.setModel(dtable.getModel());
        jScrollPane1.setViewportView(table);

        selectAndAddButton.setText(tdv.b1Text);
        selectAndAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAndAddButtonActionPerformed(evt);
            }
        });

        deleteAndRestoreButton.setText(tdv.b2Text);
        deleteAndRestoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAndRestoreButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout tablePanelLayout = new org.jdesktop.layout.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tablePanelLayout.createSequentialGroup()
                .add(selectAndAddButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(deleteAndRestoreButton))
            .add(jScrollPane1)
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tablePanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                .add(tablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectAndAddButton)
                    .add(deleteAndRestoreButton)))
        );

        showStrandPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Strand"));

        arrowCheckBox.setText("Arrow");
        arrowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrowCheckBoxActionPerformed(evt);
            }
        });

        colorCheckBox.setText("Color");
        colorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorCheckBoxActionPerformed(evt);
            }
        });

        jLabel8.setText("+");

        positiveColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        positiveColorComboBox.setButtonVisible(false);
        positiveColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        positiveColorComboBox.setStretchToFit(true);
        positiveColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positiveColorComboBoxActionPerformed(evt);
            }
        });

        jLabel9.setText("-");

        negativeColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        negativeColorComboBox.setButtonVisible(false);
        negativeColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        negativeColorComboBox.setStretchToFit(true);
        negativeColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                negativeColorComboBoxActionPerformed(evt);
            }
        });

        show2TracksCheckBox.setText("+/-");
        show2TracksCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                show2TracksCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout showStrandPanelLayout = new org.jdesktop.layout.GroupLayout(showStrandPanel);
        showStrandPanel.setLayout(showStrandPanelLayout);
        showStrandPanelLayout.setHorizontalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(showStrandPanelLayout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(positiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel9)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(colorCheckBox)
                    .add(arrowCheckBox)
                    .add(show2TracksCheckBox))
                .add(25, 25, 25))
        );
        showStrandPanelLayout.setVerticalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(arrowCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(show2TracksCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(colorCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(positiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel8))
                    .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel9)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        refreshButton = new javax.swing.JButton();
        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonPressed(evt);
            }
        });

        autoRefreshCheckBox.setVisible(false);
        autoRefreshCheckBox.setText("Auto Refresh");
        autoRefreshCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoRefreshCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(0, 0, Short.MAX_VALUE)
                .add(autoRefreshCheckBox))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(refreshButton)
                .add(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(autoRefreshCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8))
        );

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));

        trackTypeNameLabel.setText(tdv.track+":");

        trackNameTypeTextField.setEditable(pref);
        trackNameTypeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameKeyReleased(evt);
            }
        });

        bgColorLabel.setText("Background:");

        bgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        bgColorComboBox.setButtonVisible(false);
        bgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgColorComboBoxActionPerformed(evt);
            }
        });

        fgColorLabel.setText("Foreground:");

        fgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        fgColorComboBox.setButtonVisible(false);
        fgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fgColorComboBoxActionPerformed(evt);
            }
        });

        nameSizeLabel.setText("Track Label Font:");

        nameSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackNameSizeComboBoxActionPerformed(evt);
            }
        });

        FieldLabel.setText("Annotation Label Field:");

        labelFieldComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelComboBoxActionPerformed(evt);
            }
        });

        labelColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        labelColorComboBox.setButtonVisible(false);
        labelColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelColorComboBoxActionPerformed(evt);
            }
        });

        labelColorLabel.setText("Track Label:");

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Annotation Label Font:");

        annotationLabelSizeCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotationLabelSizeComboboxActionPerformed(evt);
            }
        });

        annotationLabelAutoResizeCheckbox.setText("AutoSize");
        annotationLabelAutoResizeCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotationLabelAutoResizeCheckboxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(trackTypeNameLabel)
                .add(5, 5, 5)
                .add(trackNameTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 211, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE)
                .add(applyButton))
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(fgColorLabel)
                        .add(0, 0, 0)
                        .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(30, 30, 30)
                        .add(bgColorLabel)
                        .add(0, 0, 0)
                        .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(30, 30, 30)
                        .add(labelColorLabel)
                        .add(0, 0, 0)
                        .add(labelColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(37, 37, 37)
                                .add(nameSizeLabel))
                            .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jLabel1)
                                .add(FieldLabel)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(annotationLabelSizeCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(annotationLabelAutoResizeCheckbox))
                            .add(nameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                        .add(trackTypeNameLabel)
                        .add(applyButton))
                    .add(trackNameTypeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fgColorLabel)
                    .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bgColorLabel)
                    .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelColorLabel)
                    .add(labelColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(nameSizeLabel)
                    .add(nameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(FieldLabel)
                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel1)
                    .add(annotationLabelSizeCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(annotationLabelAutoResizeCheckbox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(11, 11, 11))
        );
    }// </editor-fold>//GEN-END:initComponents

	protected void selectAndAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAndAddButtonActionPerformed

	}//GEN-LAST:event_selectAndAddButtonActionPerformed

	private void bgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorComboBoxActionPerformed
        tdv.bgColorComboBox();
	}//GEN-LAST:event_bgColorComboBoxActionPerformed

	private void fgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorComboBoxActionPerformed
        tdv.fgColorComboBox();
	}//GEN-LAST:event_fgColorComboBoxActionPerformed

	protected void deleteAndRestoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAndRestoreButtonActionPerformed

	}//GEN-LAST:event_deleteAndRestoreButtonActionPerformed

	private void positiveColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positiveColorComboBoxActionPerformed
        tdv.possitiveColorComboBox();
	}//GEN-LAST:event_positiveColorComboBoxActionPerformed

	private void negativeColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_negativeColorComboBoxActionPerformed
        tdv.negativeColorComboBox();
	}//GEN-LAST:event_negativeColorComboBoxActionPerformed

	private void trackNameSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackNameSizeComboBoxActionPerformed
        tdv.trackNameSizeComboBox();
	}//GEN-LAST:event_trackNameSizeComboBoxActionPerformed

	private void labelComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelComboBoxActionPerformed
        tdv.labelFieldComboBox();
	}//GEN-LAST:event_labelComboBoxActionPerformed

	private void arrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowCheckBoxActionPerformed
        tdv.arrowCheckBox();
	}//GEN-LAST:event_arrowCheckBoxActionPerformed

	private void colorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorCheckBoxActionPerformed
        tdv.colorCheckBox();
	}//GEN-LAST:event_colorCheckBoxActionPerformed

	private void nameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameKeyReleased
        //((TierPrefsView)tdv).displayNameTextField();
	}//GEN-LAST:event_nameKeyReleased

	private void show2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show2TracksCheckBoxActionPerformed
        tdv.show2TracksCheckBox();
	}//GEN-LAST:event_show2TracksCheckBoxActionPerformed

	private void autoRefreshCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoRefreshCheckBoxActionPerformed
        // TODO add your handling code here:
	}//GEN-LAST:event_autoRefreshCheckBoxActionPerformed

	private void refreshButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonPressed
        ((TierPrefsView) (tdv)).refreshSeqMapViewAndSlicedView();
	}//GEN-LAST:event_refreshButtonPressed

	private void labelColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelColorComboBoxActionPerformed
        tdv.labelColorComboBox();
	}//GEN-LAST:event_labelColorComboBoxActionPerformed

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        if (tdv instanceof TierPrefsView) {
            ((TierPrefsView) tdv).displayNameTextField();
        }
	}//GEN-LAST:event_applyButtonActionPerformed

    private void annotationLabelAutoResizeCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotationLabelAutoResizeCheckboxActionPerformed
        tdv.annotationLabelAutoSizeCheckBoxActionPerformed();
    }//GEN-LAST:event_annotationLabelAutoResizeCheckboxActionPerformed

    private void annotationLabelSizeComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotationLabelSizeComboboxActionPerformed
        tdv.annotationLabelSizeComboBoxActionPerformed();
    }//GEN-LAST:event_annotationLabelSizeComboboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel FieldLabel;
    private javax.swing.JCheckBox annotationLabelAutoResizeCheckbox;
    private javax.swing.JComboBox annotationLabelSizeCombobox;
    private javax.swing.JButton applyButton;
    private javax.swing.JCheckBox arrowCheckBox;
    protected javax.swing.JCheckBox autoRefreshCheckBox;
    private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
    private javax.swing.JLabel bgColorLabel;
    private javax.swing.JCheckBox colorCheckBox;
    private javax.swing.JButton deleteAndRestoreButton;
    private com.jidesoft.combobox.ColorComboBox fgColorComboBox;
    private javax.swing.JLabel fgColorLabel;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private com.jidesoft.combobox.ColorComboBox labelColorComboBox;
    private javax.swing.JLabel labelColorLabel;
    private javax.swing.JComboBox labelFieldComboBox;
    private javax.swing.JComboBox nameSizeComboBox;
    private javax.swing.JLabel nameSizeLabel;
    private com.jidesoft.combobox.ColorComboBox negativeColorComboBox;
    private com.jidesoft.combobox.ColorComboBox positiveColorComboBox;
    private javax.swing.JPanel propertiesPanel;
    protected javax.swing.JButton refreshButton;
    private javax.swing.JButton selectAndAddButton;
    private javax.swing.JCheckBox show2TracksCheckBox;
    private javax.swing.JPanel showStrandPanel;
    private javax.swing.JTable table;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTextField trackNameTypeTextField;
    private javax.swing.JLabel trackTypeNameLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void refresh() {
    }

    public void mapRefresh() {

    }

    public void windowOpened(WindowEvent we) {

    }

    public void windowClosing(WindowEvent we) {

    }

    public void windowClosed(WindowEvent we) {
        stopEditing();
    }

    public void windowIconified(WindowEvent we) {

    }

    public void windowDeiconified(WindowEvent we) {

    }

    public void windowActivated(WindowEvent we) {

    }

    public void windowDeactivated(WindowEvent we) {

    }

    public void groupSelectionChanged(GroupSelectionEvent evt) {

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
}
