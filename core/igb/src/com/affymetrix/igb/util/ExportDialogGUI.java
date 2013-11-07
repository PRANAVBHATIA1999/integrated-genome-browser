package com.affymetrix.igb.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 * An Export Image UI class for IGB. It is generated by Netbeans GUI Builder.
 *
 * @author nick
 */
public class ExportDialogGUI extends JPanel {

	private ExportDialog ed;

	public ExportDialogGUI(ExportDialog ed) {
		this.ed = ed;
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
        imageSizePanel = new javax.swing.JPanel();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        widthSpinner = ed.widthSpinner;
        heightSpinner = ed.heightSpinner;
        resolutionLabel = new javax.swing.JLabel();
        refreshButton = ed.refreshButton;
        resolutionComboBox = ed.resolutionComboBox;
        unitComboBox = ed.unitComboBox;
        sizeLabel = ed.sizeLabel;
        previewPanel = new javax.swing.JPanel();
        previewLabel = ed.previewLabel;
        buttonsPanel = ed.buttonsPanel;
        svRadioButton = ed.svRadioButton;
        mvRadioButton = ed.mvRadioButton;
        wfRadioButton = ed.wfRadioButton;
        mvlRadioButton = ed.mvlRadioButton;
        topPanel = new javax.swing.JPanel();
        browseButton = ed.browseButton;
        filePathTextField = ed.filePathTextField;
        okButton = ed.okButton;
        cancelButton = ed.cancelButton;
        extComboBox = ed.extComboBox;

        setMinimumSize(new java.awt.Dimension(506, 329));

        imageSizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Image Size"));

        widthLabel.setText("Width:");

        heightLabel.setText("Height:");

        widthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                widthSpinnerStateChanged(evt);
            }
        });

        heightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightSpinnerStateChanged(evt);
            }
        });

        resolutionLabel.setText("Resolution:");

        refreshButton.setText("Refresh");
        refreshButton.setToolTipText("Refreshes the size and the preview to the current dimensions of the image.");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        resolutionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolutionComboBoxActionPerformed(evt);
            }
        });

        unitComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unitComboBoxActionPerformed(evt);
            }
        });

        sizeLabel.setText("  ");

        org.jdesktop.layout.GroupLayout imageSizePanelLayout = new org.jdesktop.layout.GroupLayout(imageSizePanel);
        imageSizePanel.setLayout(imageSizePanelLayout);
        imageSizePanelLayout.setHorizontalGroup(
            imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(imageSizePanelLayout.createSequentialGroup()
                .add(20, 20, 20)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(imageSizePanelLayout.createSequentialGroup()
                        .add(widthLabel)
                        .add(20, 20, 20)
                        .add(widthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(sizeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(imageSizePanelLayout.createSequentialGroup()
                        .add(heightLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(heightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(imageSizePanelLayout.createSequentialGroup()
                        .add(resolutionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resolutionComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(14, 14, 14)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(unitComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(0, 0, Short.MAX_VALUE))
        );

        imageSizePanelLayout.linkSize(new java.awt.Component[] {heightSpinner, widthSpinner}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        imageSizePanelLayout.setVerticalGroup(
            imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(imageSizePanelLayout.createSequentialGroup()
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(unitComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(heightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(widthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(heightLabel)
                    .add(widthLabel))
                .add(5, 5, 5)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(sizeLabel)
                    .add(resolutionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshButton)
                    .add(resolutionLabel)))
        );

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));

        previewLabel.setText("   ");

        svRadioButton.setText("Sliced View (with Labels)");
        buttonGroup.add(svRadioButton);
        svRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                svRadioButtonActionPerformed(evt);
            }
        });

        mvRadioButton.setText("Main View");
        buttonGroup.add(mvRadioButton);
        mvRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mvRadioButtonActionPerformed(evt);
            }
        });

        wfRadioButton.setText("Whole Frame");
        buttonGroup.add(wfRadioButton);
        wfRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wfRadioButtonActionPerformed(evt);
            }
        });

        mvlRadioButton.setText("Main View (with Labels)");
        buttonGroup.add(mvlRadioButton);
        mvlRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mvlRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout buttonsPanelLayout = new org.jdesktop.layout.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(wfRadioButton)
            .add(svRadioButton)
            .add(mvlRadioButton)
            .add(mvRadioButton)
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonsPanelLayout.createSequentialGroup()
                .add(wfRadioButton)
                .add(5, 5, 5)
                .add(mvRadioButton)
                .add(5, 5, 5)
                .add(mvlRadioButton)
                .add(5, 5, 5)
                .add(svRadioButton))
        );

        org.jdesktop.layout.GroupLayout previewPanelLayout = new org.jdesktop.layout.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(previewPanelLayout.createSequentialGroup()
                .add(15, 15, 15)
                .add(buttonsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(22, 22, 22)
                .add(previewLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 218, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(previewPanelLayout.createSequentialGroup()
                .add(previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(buttonsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(previewLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5))
        );

        previewPanelLayout.linkSize(new java.awt.Component[] {buttonsPanel, previewLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        browseButton.setText("Browse...");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        extComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, topPanelLayout.createSequentialGroup()
                .add(filePathTextField)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton))
            .add(topPanelLayout.createSequentialGroup()
                .add(extComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(okButton))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(topPanelLayout.createSequentialGroup()
                .add(topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseButton)
                    .add(filePathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0)
                .add(topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(extComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cancelButton)
                    .add(okButton)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(topPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, imageSizePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(20, 20, 20))
        );

        layout.linkSize(new java.awt.Component[] {imageSizePanel, previewPanel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(topPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(imageSizePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(previewPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
		ed.browseButtonActionPerformed(this);
	}//GEN-LAST:event_browseButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
		ed.cancelButtonActionPerformed();
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
		try {
			ed.okButtonActionPerformed();
		} catch (IOException ex) {
			Logger.getLogger(ExportDialogGUI.class.getName()).log(Level.SEVERE, null, ex);
		}

	}//GEN-LAST:event_okButtonActionPerformed

	private void widthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_widthSpinnerStateChanged
		ed.widthSpinnerStateChanged();
	}//GEN-LAST:event_widthSpinnerStateChanged

	private void heightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heightSpinnerStateChanged
		ed.heightSpinnerStateChanged();
	}//GEN-LAST:event_heightSpinnerStateChanged

	private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
		ed.refreshButtonActionPerformed();
	}//GEN-LAST:event_refreshButtonActionPerformed

	private void mvRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mvRadioButtonActionPerformed
		ed.mvRadioButtonActionPerformed();
	}//GEN-LAST:event_mvRadioButtonActionPerformed

	private void mvlRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mvlRadioButtonActionPerformed
		ed.mvlRadioButtonActionPerformed();
	}//GEN-LAST:event_mvlRadioButtonActionPerformed

	private void wfRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wfRadioButtonActionPerformed
		ed.wfRadioButtonActionPerformed();
	}//GEN-LAST:event_wfRadioButtonActionPerformed

	private void svRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svRadioButtonActionPerformed
		ed.svRadioButtonActionPerformed();
	}//GEN-LAST:event_svRadioButtonActionPerformed

	private void resolutionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboBoxActionPerformed
		ed.resolutionComboBoxActionPerformed();
	}//GEN-LAST:event_resolutionComboBoxActionPerformed

	private void unitComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unitComboBoxActionPerformed
		ed.unitComboBoxActionPerformed();
	}//GEN-LAST:event_unitComboBoxActionPerformed

	private void extComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extComboBoxActionPerformed
		ed.extComboBoxActionPerformed();
	}//GEN-LAST:event_extComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox extComboBox;
    private javax.swing.JTextField filePathTextField;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JSpinner heightSpinner;
    private javax.swing.JPanel imageSizePanel;
    private javax.swing.JRadioButton mvRadioButton;
    private javax.swing.JRadioButton mvlRadioButton;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox resolutionComboBox;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JRadioButton svRadioButton;
    private javax.swing.JPanel topPanel;
    private javax.swing.JComboBox unitComboBox;
    private javax.swing.JRadioButton wfRadioButton;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSpinner widthSpinner;
    // End of variables declaration//GEN-END:variables
}
