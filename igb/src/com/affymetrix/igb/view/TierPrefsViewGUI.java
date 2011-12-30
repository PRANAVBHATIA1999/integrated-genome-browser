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

		tpv = TierPrefsView.getSingleton();

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
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        selectTrackPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = tpv.table;
        restoreToDefaultButton = new javax.swing.JButton();
        showStrandPanel = new javax.swing.JPanel();
        possitiveLabel = new javax.swing.JLabel();
        negativeLabel = new javax.swing.JLabel();
        possitiveColorComboBox = tpv.possitiveColorComboBox;
        negativeColorComboBox = tpv.negativeColorComboBox;
        colorCheckBox = tpv.colorCheckBox;
        arrowCheckBox = tpv.arrowCheckBox;
        viewModelPanel = new javax.swing.JPanel();
        viewModeCB = tpv.viewModeCB;
        refreshButton = tpv.refreshButton;
        autoRefreshCheckBox = tpv.autoRefreshCheckBox;

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

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(1, 1, 1)
                .add(show2TracksCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectedCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(collapsedCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, propertiesPanelLayout.createSequentialGroup()
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
                        .add(27, 27, 27)
                        .add(fgLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(55, 55, 55))
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(displayNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(displayNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(maxDepthLabel)
                        .add(5, 5, 5)
                        .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelFieldLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(11, 11, 11))
            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
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
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(bgLabel)
                        .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(fgLabel)
                        .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(10, 10, 10)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(trackNameSizeLabel))
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

        selectTrackPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Track List"));

        jScrollPane1.setViewportView(table);
        tpv.refreshList();

        restoreToDefaultButton.setText("Restore to default");
        restoreToDefaultButton.setToolTipText("Restore selected tracks to default setting.");
        restoreToDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreToDefaultButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout selectTrackPanelLayout = new org.jdesktop.layout.GroupLayout(selectTrackPanel);
        selectTrackPanel.setLayout(selectTrackPanelLayout);
        selectTrackPanelLayout.setHorizontalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectTrackPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(restoreToDefaultButton)
                .add(314, 314, 314))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
        );
        selectTrackPanelLayout.setVerticalGroup(
            selectTrackPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, selectTrackPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(restoreToDefaultButton))
        );

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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(11, Short.MAX_VALUE))
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
            .add(viewModeCB, 0, 104, Short.MAX_VALUE)
        );
        viewModelPanelLayout.setVerticalGroup(
            viewModelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(viewModeCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        refreshButton.setText("Refresh");

        autoRefreshCheckBox.setText("Auto Refresh");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(propertiesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(showStrandPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(viewModelPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(autoRefreshCheckBox)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(selectTrackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(viewModelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(autoRefreshCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(refreshButton))
                    .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void show2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show2TracksCheckBoxActionPerformed
		tpv.show2TracksCheckBox();
}//GEN-LAST:event_show2TracksCheckBoxActionPerformed

    private void connectedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectedCheckBoxActionPerformed
		tpv.connectedCheckBox();
    }//GEN-LAST:event_connectedCheckBoxActionPerformed

	private void collapsedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapsedCheckBoxActionPerformed
		tpv.collapsedCheckBox();
	}//GEN-LAST:event_collapsedCheckBoxActionPerformed

	private void displayNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayNameTextFieldActionPerformed
		tpv.displayNameTextField();
	}//GEN-LAST:event_displayNameTextFieldActionPerformed

	private void maxDepthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxDepthTextFieldActionPerformed
		tpv.maxDepthTextField();
	}//GEN-LAST:event_maxDepthTextFieldActionPerformed

	private void trackNameSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackNameSizeComboBoxActionPerformed
		tpv.trackNameSizeComboBox();
}//GEN-LAST:event_trackNameSizeComboBoxActionPerformed

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

	private void viewModeCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewModeCBActionPerformed
		tpv.viewModeCB();
	}//GEN-LAST:event_viewModeCBActionPerformed

	private void restoreToDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreToDefaultButtonActionPerformed
		tpv.restoreToDefault();
	}//GEN-LAST:event_restoreToDefaultButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JButton restoreToDefaultButton;
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
