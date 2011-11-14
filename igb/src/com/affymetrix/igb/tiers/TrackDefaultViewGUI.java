package com.affymetrix.igb.tiers;

import com.affymetrix.genometryImpl.event.SeqMapRefreshed;
import com.affymetrix.igb.prefs.IPrefEditorComponent;

/**
 *
 * @author lorainelab
 */
public class TrackDefaultViewGUI extends IPrefEditorComponent implements SeqMapRefreshed {
	private static final long serialVersionUID = 1L;

	/** Creates new form FileTypeViewNew */
	public TrackDefaultViewGUI() {
		setName("Track Defaults");
		TrackDefaultView.init();
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showStrandPanel = new javax.swing.JPanel();
        possitiveLabel = new javax.swing.JLabel();
        negativeLabel = new javax.swing.JLabel();
        possitiveColorComboBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getPossitiveColorCombo();
        negativeColorComboBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getNegativeColorComboBox();
        colorCheckBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getColorCheckBox();
        arrowCheckBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getArrowCheckBox();
        selectTrackDefaultPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getTable();
        addTrackDefaultButton = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getAddTrackDefaultButton();
        removeTrackDefaultButton = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getRemoveTrackDefaultButton();
        propertiesPanel = new javax.swing.JPanel();
        TrackTypeNameLabel = new javax.swing.JLabel();
        trackDefaultTextField = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getTrackDefaultTextField();
        bgLabel = new javax.swing.JLabel();
        bgColorComboBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getBgColorComboBox();
        trackNameSizeLabel = new javax.swing.JLabel();
        trackNameSizeComboBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getTrackNameSizeComboBox();
        labelFieldLabel = new javax.swing.JLabel();
        fgLabel = new javax.swing.JLabel();
        fgColorComboBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getFgColorComboBox();
        labelFieldComboBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getLabelFieldComboBox();
        maxDepthLabel = new javax.swing.JLabel();
        maxDepthTextField = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getMaxDepthTextField();
        show2TracksCheckBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getShow2TracksCheckBox();
        connectedCheckBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getConnectedCheckBox();
        collapsedCheckBox = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getCollapsedCheckBox();
        labelFieldTip = com.affymetrix.igb.tiers.TrackDefaultView.getTrackDefaultView().getLabelFieldTip();

        setPreferredSize(new java.awt.Dimension(540, 512));

        showStrandPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Strand"));

        possitiveLabel.setText("+");

        negativeLabel.setText("-");

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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        selectTrackDefaultPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Track Default"));
        selectTrackDefaultPanel.setPreferredSize(new java.awt.Dimension(600, 271));

        jScrollPane1.setViewportView(table);

        addTrackDefaultButton.setText("Add");
        addTrackDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTrackDefaultButtonActionPerformed(evt);
            }
        });

        removeTrackDefaultButton.setText("Remove");
        removeTrackDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTrackDefaultButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout selectTrackDefaultPanelLayout = new org.jdesktop.layout.GroupLayout(selectTrackDefaultPanel);
        selectTrackDefaultPanel.setLayout(selectTrackDefaultPanelLayout);
        selectTrackDefaultPanelLayout.setHorizontalGroup(
            selectTrackDefaultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(selectTrackDefaultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(selectTrackDefaultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                    .add(selectTrackDefaultPanelLayout.createSequentialGroup()
                        .add(addTrackDefaultButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(removeTrackDefaultButton)))
                .addContainerGap())
        );

        selectTrackDefaultPanelLayout.linkSize(new java.awt.Component[] {addTrackDefaultButton, removeTrackDefaultButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        selectTrackDefaultPanelLayout.setVerticalGroup(
            selectTrackDefaultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, selectTrackDefaultPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectTrackDefaultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addTrackDefaultButton)
                    .add(removeTrackDefaultButton)))
        );

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Properties"));
        propertiesPanel.setPreferredSize(new java.awt.Dimension(501, 183));

        TrackTypeNameLabel.setText("Track Type:");

        trackDefaultTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackDefaultTextFieldActionPerformed(evt);
            }
        });

        bgLabel.setText("Background:");

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
        trackNameSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.SUPPORTED_SIZE));
        trackNameSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackNameSizeComboBoxActionPerformed(evt);
            }
        });

        labelFieldLabel.setText("Label Field:");

        fgLabel.setText("Foreground:");

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

        labelFieldComboBox.setEditable(true);
        labelFieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.LABELFIELD));
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

        labelFieldTip.setText(" ");

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(9, 9, 9)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(TrackTypeNameLabel)
                    .add(bgLabel)
                    .add(trackNameSizeLabel))
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(propertiesPanelLayout.createSequentialGroup()
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, labelFieldLabel)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, fgLabel)))
                            .add(maxDepthLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(maxDepthTextField)
                            .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(1, 1, 1)
                        .add(labelFieldTip))
                    .add(propertiesPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(trackDefaultTextField))))
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(show2TracksCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectedCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(collapsedCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        propertiesPanelLayout.linkSize(new java.awt.Component[] {labelFieldComboBox, maxDepthTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(TrackTypeNameLabel)
                    .add(trackDefaultTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(bgLabel)
                    .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fgLabel)
                    .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelFieldTip)
                    .add(trackNameSizeLabel)
                    .add(trackNameSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelFieldLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maxDepthLabel)
                    .add(maxDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(show2TracksCheckBox)
                    .add(connectedCheckBox)
                    .add(collapsedCheckBox)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, selectTrackDefaultPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 372, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(selectTrackDefaultPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(propertiesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 185, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(showStrandPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 104, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void addTrackDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTrackDefaultButtonActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().addTrackDefaultButtonActionPerformed();
	}//GEN-LAST:event_addTrackDefaultButtonActionPerformed

	private void removeTrackDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTrackDefaultButtonActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().removeTrackDefaultButtonActionPerformed();
	}//GEN-LAST:event_removeTrackDefaultButtonActionPerformed

	private void trackDefaultTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackDefaultTextFieldActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().trackNameSizeComboBoxActionPerformed();
	}//GEN-LAST:event_trackDefaultTextFieldActionPerformed

	private void bgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorComboBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().bgColorComboBoxActionPerformed();
	}//GEN-LAST:event_bgColorComboBoxActionPerformed

	private void fgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorComboBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().fgColorComboBoxActionPerformed();
	}//GEN-LAST:event_fgColorComboBoxActionPerformed

	private void trackNameSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackNameSizeComboBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().trackNameSizeComboBoxActionPerformed();
	}//GEN-LAST:event_trackNameSizeComboBoxActionPerformed

	private void labelFieldComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelFieldComboBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().labelFieldComboBoxActionPerformed();
	}//GEN-LAST:event_labelFieldComboBoxActionPerformed

	private void maxDepthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxDepthTextFieldActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().maxDepthTextFieldActionPerformed();
	}//GEN-LAST:event_maxDepthTextFieldActionPerformed

	private void show2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_show2TracksCheckBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().show2TracksCheckBoxActionPerformed();
	}//GEN-LAST:event_show2TracksCheckBoxActionPerformed

	private void connectedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectedCheckBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().connectedCheckBoxActionPerformed();
	}//GEN-LAST:event_connectedCheckBoxActionPerformed

	private void collapsedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapsedCheckBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().collapsedCheckBoxActionPerformed();
	}//GEN-LAST:event_collapsedCheckBoxActionPerformed

	private void arrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_arrowCheckBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().arrowCheckBoxActionPerformed();
	}//GEN-LAST:event_arrowCheckBoxActionPerformed

	private void colorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorCheckBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().colorCheckBoxActionPerformed();
	}//GEN-LAST:event_colorCheckBoxActionPerformed

	private void possitiveColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_possitiveColorComboBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().possitiveColorComboBoxActionPerformed();
	}//GEN-LAST:event_possitiveColorComboBoxActionPerformed

	private void negativeColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_negativeColorComboBoxActionPerformed
		// TODO add your handling code here:
		TrackDefaultView.getTrackDefaultView().negativeColorComboBoxActionPerformed();
	}//GEN-LAST:event_negativeColorComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel TrackTypeNameLabel;
    private javax.swing.JButton addTrackDefaultButton;
    private javax.swing.JCheckBox arrowCheckBox;
    private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JCheckBox collapsedCheckBox;
    private javax.swing.JCheckBox colorCheckBox;
    private javax.swing.JCheckBox connectedCheckBox;
    private com.jidesoft.combobox.ColorComboBox fgColorComboBox;
    private javax.swing.JLabel fgLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox labelFieldComboBox;
    private javax.swing.JLabel labelFieldLabel;
    private javax.swing.JLabel labelFieldTip;
    private javax.swing.JLabel maxDepthLabel;
    private javax.swing.JTextField maxDepthTextField;
    private com.jidesoft.combobox.ColorComboBox negativeColorComboBox;
    private javax.swing.JLabel negativeLabel;
    private com.jidesoft.combobox.ColorComboBox possitiveColorComboBox;
    private javax.swing.JLabel possitiveLabel;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JButton removeTrackDefaultButton;
    private javax.swing.JPanel selectTrackDefaultPanel;
    private javax.swing.JCheckBox show2TracksCheckBox;
    private javax.swing.JPanel showStrandPanel;
    private javax.swing.JTable table;
    private javax.swing.JTextField trackDefaultTextField;
    private javax.swing.JComboBox trackNameSizeComboBox;
    private javax.swing.JLabel trackNameSizeLabel;
    // End of variables declaration//GEN-END:variables

	@Override
	public void refresh() {
	}

	public void mapRefresh() {
		if (isVisible()) {
			refreshList();
		}
	}

	public void refreshList() {
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
