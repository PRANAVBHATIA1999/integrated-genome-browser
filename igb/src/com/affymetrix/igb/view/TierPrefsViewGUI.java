package com.affymetrix.igb.view;

import com.affymetrix.igb.Application;
import java.awt.event.*;
import javax.swing.event.ListSelectionEvent;
import com.affymetrix.igb.prefs.IPrefEditorComponent;
import com.affymetrix.genometryImpl.event.SeqMapRefreshed;

public class TierPrefsViewGUI extends IPrefEditorComponent implements WindowListener, SeqMapRefreshed {

	public static final long serialVersionUID = 1l;
	public TierPrefsView tpv;
	private SeqMapView smv;

	/** Creates new form PrototypeOne */
	public TierPrefsViewGUI() {

		super();
		this.setName("Tracks");
		this.setToolTipText("Set Track Properties");

		TierPrefsView.init();
		tpv = TierPrefsView.getTierPrefsView();

		Application igb = Application.getSingleton();
		if (igb != null) {
			smv = igb.getMapView();
			smv.addToRefreshList(this);
		}

		initComponents();
		validate();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        refreshButton = tpv.refreshButton;
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
        connectedCheckBox = tpv.connectedCheckBox;
        collapsedCheckBox = tpv.collapsedCheckBox;
        applyToAllButton = tpv.applyToAllButton;
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        applyToAllTip = tpv.applyToAllTip;
        selectTrackPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = tpv.table;
        autoRefreshCheckBox = new javax.swing.JCheckBox();
        showStrandPanel = new javax.swing.JPanel();
        possitiveLabel = new javax.swing.JLabel();
        negativeLabel = new javax.swing.JLabel();
        possitiveColorComboBox = tpv.possitiveColorComboBox;
        negativeColorComboBox = tpv.negativeColorComboBox;
        colorCheckBox = tpv.colorCheckBox;
        arrowCheckBox = tpv.arrowCheckBox;
        viewModelPanel = new javax.swing.JPanel();
        viewModeCB = tpv.viewModeCB;

        refreshButton.setText("Refresh");

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));

        displayNameLabel.setText("Track Name:");

        displayNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayNameTextFieldActionPerformed(evt);
            }
        });

        bgLabel.setText("Background:");

        bgColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        bgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        bgColorComboBox.setButtonVisible(false);
        bgColorComboBox.setColorValueVisible(false);
        bgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        bgColorComboBox.setStretchToFit(true);
        bgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgColorComboBoxActionPerformed(evt);
            }
        });

        trackNameSizeLabel.setText("Name Size:");

        trackNameSizeComboBox.setEditable(true);
        trackNameSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackNameSizeComboBoxActionPerformed(evt);
            }
        });

        labelFieldLabel.setText("Label Field:");

        fgLabel.setText("Foreground:");

        fgColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        fgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        fgColorComboBox.setButtonVisible(false);
        fgColorComboBox.setColorValueVisible(false);
        fgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        fgColorComboBox.setStretchToFit(true);
        fgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fgColorComboBoxActionPerformed(evt);
            }
        });

        labelFieldComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelFieldComboBoxActionPerformed(evt);
            }
        });

        maxDepthLabel.setText("Max Stack Depth:");

        maxDepthTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxDepthTextFieldActionPerformed(evt);
            }
        });

        show2TracksCheckBox.setText("Show 2 tracks (+/-)");
        show2TracksCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                show2TracksCheckBoxActionPerformed(evt);
            }
        });

        connectedCheckBox.setText("Connected");
        connectedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectedCheckBoxActionPerformed(evt);
            }
        });

        collapsedCheckBox.setText("Collapsed");
        collapsedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collapsedCheckBoxActionPerformed(evt);
            }
        });

        applyToAllButton.setText("Apply To All Tracks");
        applyToAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyToAllButtonActionPerformed(evt);
            }
        });

        applyToAllTip.setText(" ");

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(9, 9, 9)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(bgLabel)
                            .add(trackNameSizeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(applyToAllButton)
                                .add(0, 0, 0)
                                .add(applyToAllTip))
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(9, 9, 9)
                                .add(fgLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(displayNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(displayNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 260, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(maxDepthLabel)
                        .add(5, 5, 5)
                        .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelFieldLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1)
                .add(org.jdesktop.layout.GroupLayout.LEADING, propertiesPanelLayout.createSequentialGroup()
                    .add(1, 1, 1)
                    .add(show2TracksCheckBox)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(connectedCheckBox)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(collapsedCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                .addContainerGap())
        );

        propertiesPanelLayout.linkSize(new java.awt.Component[] {jSeparator1, jSeparator2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(displayNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(bgLabel)
                        .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(fgLabel)
                        .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(10, 10, 10)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(applyToAllButton)
                    .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(trackNameSizeLabel)
                    .add(applyToAllTip))
                .add(8, 8, 8)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelFieldLabel)
                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxDepthLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(show2TracksCheckBox)
                    .add(connectedCheckBox)
                    .add(collapsedCheckBox)))
        );

        applyToAllButton.setToolTipText("Apply background, foreground, and Name Size to all selected tracks.");

        selectTrackPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Track"));

        jScrollPane1.setViewportView(table);
        tpv.refreshList();

        org.jdesktop.layout.GroupLayout selectTrackPanelLayout = new org.jdesktop.layout.GroupLayout(selectTrackPanel);
        selectTrackPanel.setLayout(selectTrackPanelLayout);
        selectTrackPanelLayout.setHorizontalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectTrackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1)
                .addContainerGap())
        );
        selectTrackPanelLayout.setVerticalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectTrackPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addContainerGap())
        );

        autoRefreshCheckBox.setText("Auto Refresh");

        showStrandPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Strand"));

        possitiveLabel.setText("+");

        negativeLabel.setText("-");

        possitiveColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        possitiveColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        possitiveColorComboBox.setButtonVisible(false);
        possitiveColorComboBox.setColorValueVisible(false);
        possitiveColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        possitiveColorComboBox.setStretchToFit(true);
        possitiveColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                possitiveColorComboBoxActionPerformed(evt);
            }
        });

        negativeColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
        negativeColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        negativeColorComboBox.setButtonVisible(false);
        negativeColorComboBox.setColorValueVisible(false);
        negativeColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        negativeColorComboBox.setStretchToFit(true);
        negativeColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                negativeColorComboBoxActionPerformed(evt);
            }
        });

        colorCheckBox.setText("Color");
        colorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorCheckBoxActionPerformed(evt);
            }
        });

        arrowCheckBox.setText("Arrow");
        arrowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                arrowCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout showStrandPanelLayout = new org.jdesktop.layout.GroupLayout(showStrandPanel);
        showStrandPanel.setLayout(showStrandPanelLayout);
        showStrandPanelLayout.setHorizontalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(colorCheckBox)
                    .add(arrowCheckBox)
                    .add(showStrandPanelLayout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(possitiveLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(possitiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negativeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        showStrandPanelLayout.setVerticalGroup(
            showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showStrandPanelLayout.createSequentialGroup()
                .add(arrowCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(colorCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showStrandPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(possitiveLabel)
                    .add(possitiveColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(negativeLabel)
                    .add(negativeColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        viewModelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("View Mode"));

        viewModeCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewModeCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout viewModelPanelLayout = new org.jdesktop.layout.GroupLayout(viewModelPanel);
        viewModelPanel.setLayout(viewModelPanelLayout);
        viewModelPanelLayout.setHorizontalGroup(
            viewModelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(viewModeCB, 0, 101, Short.MAX_VALUE)
        );
        viewModelPanelLayout.setVerticalGroup(
            viewModelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(viewModeCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 375, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(autoRefreshCheckBox)
                            .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(viewModelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(viewModelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoRefreshCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(refreshButton)))
                .add(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void show2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show2TracksCheckBoxActionPerformed
		tpv.show2TracksCheckBoxActionPerformed();
}//GEN-LAST:event_show2TracksCheckBoxActionPerformed

    private void connectedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectedCheckBoxActionPerformed
		tpv.connectedCheckBoxActionPerformed();
    }//GEN-LAST:event_connectedCheckBoxActionPerformed

	private void collapsedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapsedCheckBoxActionPerformed
		tpv.collapsedCheckBoxActionPerformed();
	}//GEN-LAST:event_collapsedCheckBoxActionPerformed

	private void displayNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayNameTextFieldActionPerformed
		tpv.displayNameTextFieldActionPerformed();
	}//GEN-LAST:event_displayNameTextFieldActionPerformed

	private void maxDepthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxDepthTextFieldActionPerformed
		tpv.maxDepthTextFieldActionPerformed();
	}//GEN-LAST:event_maxDepthTextFieldActionPerformed

	private void trackNameSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackNameSizeComboBoxActionPerformed
		tpv.trackNameSizeComboBoxActionPerformed();
}//GEN-LAST:event_trackNameSizeComboBoxActionPerformed

	private void fgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorComboBoxActionPerformed
		tpv.fgColorComboBoxActionPerformed();
}//GEN-LAST:event_fgColorComboBoxActionPerformed

	private void bgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorComboBoxActionPerformed
		tpv.bgColorComboBoxActionPerformed();
}//GEN-LAST:event_bgColorComboBoxActionPerformed

	private void applyToAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyToAllButtonActionPerformed
		tpv.applyToAllButtonActionPerformed();
	}//GEN-LAST:event_applyToAllButtonActionPerformed

	private void labelFieldComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelFieldComboBoxActionPerformed
		tpv.labelFieldComboBoxActionPerformed();
}//GEN-LAST:event_labelFieldComboBoxActionPerformed

	private void possitiveColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_possitiveColorComboBoxActionPerformed
		tpv.possitiveColorComboBoxActionPerformed();
}//GEN-LAST:event_possitiveColorComboBoxActionPerformed

	private void negativeColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_negativeColorComboBoxActionPerformed
		tpv.negativeColorComboBoxActionPerformed();
}//GEN-LAST:event_negativeColorComboBoxActionPerformed

	private void colorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorCheckBoxActionPerformed
		tpv.colorCheckBoxActionPerformed();
}//GEN-LAST:event_colorCheckBoxActionPerformed

	private void arrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowCheckBoxActionPerformed
		tpv.arrowCheckBoxActionPerformed();
}//GEN-LAST:event_arrowCheckBoxActionPerformed

	private void viewModeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeCBActionPerformed
		tpv.viewModeCBActionPerformed();
	}//GEN-LAST:event_viewModeCBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyToAllButton;
    private javax.swing.JLabel applyToAllTip;
    private javax.swing.JCheckBox arrowCheckBox;
    private javax.swing.JCheckBox autoRefreshCheckBox;
    private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JCheckBox collapsedCheckBox;
    private javax.swing.JCheckBox colorCheckBox;
    private javax.swing.JCheckBox connectedCheckBox;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField displayNameTextField;
    private com.jidesoft.combobox.ColorComboBox fgColorComboBox;
    private javax.swing.JLabel fgLabel;
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
    private javax.swing.JPanel selectTrackPanel;
    private javax.swing.JCheckBox show2TracksCheckBox;
    private javax.swing.JPanel showStrandPanel;
    private javax.swing.JTable table;
    private javax.swing.JComboBox trackNameSizeComboBox;
    private javax.swing.JLabel trackNameSizeLabel;
    private javax.swing.JComboBox viewModeCB;
    private javax.swing.JPanel viewModelPanel;
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

	public void windowOpened(WindowEvent e) {}

	public void windowClosing(WindowEvent e) {}

	public void windowIconified(WindowEvent e) {}

	public void windowDeiconified(WindowEvent e) {}

	public void windowActivated(WindowEvent e) {}

	public void windowDeactivated(WindowEvent e) {}

	public void valueChanged(ListSelectionEvent lse) {
		tpv.valueChanged(lse);
	}
}
