package com.affymetrix.igb.util;

import com.affymetrix.genometryImpl.util.DisplayUtils;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.tiers.AffyLabelledTierMap;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author nick
 */
public class ExportDialogGUI extends JPanel {

	public static JFrame static_frame = null;
	private static ExportDialogGUI singleton;
	private static ExportDialog export;

	public synchronized void display() {
		if (svRadioButton.isSelected()) {
			export.setComponent(export.determineSlicedComponent());
		} else if (mvRadioButton.isSelected()) {
			AffyLabelledTierMap tm = (AffyLabelledTierMap) IGB.getSingleton().getMapView().getSeqMap();
			export.setComponent(tm.getSplitPane());
		} else if (mvlRadioButton.isSelected()) {
			export.setComponent(IGB.getSingleton().getMapView().getSeqMap().getNeoCanvas());
		} else {
			export.setComponent(IGB.getSingleton().getFrame());
			wfRadioButton.setSelected(true);
		}

		export.initImageInfo();

		if (static_frame == null) {
			export.init();

			static_frame = PreferenceUtils.createFrame("Export view as",
					getSingleton());

			Application app = Application.getSingleton();
			JFrame frame = (app == null) ? null : app.getFrame();

			Point location = frame.getLocation();
			// Display frame at center when initialize it
			static_frame.setLocation(location.x + frame.getWidth() / 2 - static_frame.getWidth() / 2,
					location.y + frame.getHeight() / 2 - static_frame.getHeight() / 2);
		}

		DisplayUtils.bringFrameToFront(static_frame);

		export.previewImage();
	}

	public static synchronized ExportDialogGUI getSingleton() {
		if (singleton == null) {
			singleton = new ExportDialogGUI();
		}
		return singleton;
	}

	/** Creates new form ExportUtils */
	public ExportDialogGUI() {
		export = ExportDialog.getSingleton();

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

        buttonGroup = new javax.swing.ButtonGroup();
        filePathTextField = export.filePathTextField;
        browseButton = new javax.swing.JButton();
        extComboBox = export.extComboBox;
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        imageSizePanel = new javax.swing.JPanel();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        widthSpinner = export.widthSpinner;
        heightSpinner = export.heightSpinner;
        xLabel = new javax.swing.JLabel();
        yLabel = new javax.swing.JLabel();
        xSpinner = export.xSpinner;
        ySpinner = export.ySpinner;
        resetButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();
        previewLabel = export.previewLabel;
        chooseViewPanel = new javax.swing.JPanel();
        wfRadioButton = new javax.swing.JRadioButton();
        svRadioButton = new javax.swing.JRadioButton();
        mvRadioButton = new javax.swing.JRadioButton();
        mvlRadioButton = new javax.swing.JRadioButton();

        browseButton.setText("Browse...");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        extComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extComboBoxActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

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

        xLabel.setText("X Resolution:");

        yLabel.setText("Y Resolution:");

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout imageSizePanelLayout = new org.jdesktop.layout.GroupLayout(imageSizePanel);
        imageSizePanel.setLayout(imageSizePanelLayout);
        imageSizePanelLayout.setHorizontalGroup(
            imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(imageSizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(widthLabel)
                    .add(heightLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(heightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(widthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(yLabel)
                    .add(xLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(imageSizePanelLayout.createSequentialGroup()
                        .add(ySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(20, 20, 20)
                        .add(applyButton))
                    .add(imageSizePanelLayout.createSequentialGroup()
                        .add(xSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(20, 20, 20)
                        .add(resetButton))))
        );

        imageSizePanelLayout.linkSize(new java.awt.Component[] {applyButton, resetButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        imageSizePanelLayout.setVerticalGroup(
            imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(imageSizePanelLayout.createSequentialGroup()
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(widthLabel)
                    .add(widthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(xLabel)
                    .add(xSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resetButton))
                .add(5, 5, 5)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(heightSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(heightLabel)
                    .add(yLabel)
                    .add(ySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(applyButton)))
        );

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));

        previewLabel.setText("   ");

        org.jdesktop.layout.GroupLayout previewPanelLayout = new org.jdesktop.layout.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(previewLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 464, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(previewLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 219, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        chooseViewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Choose View"));

        wfRadioButton.setText("Whole Frame");
        buttonGroup.add(wfRadioButton);
        wfRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wfRadioButtonActionPerformed(evt);
            }
        });

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

        mvlRadioButton.setText("Main View (with Labels)");
        buttonGroup.add(mvlRadioButton);
        mvlRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mvlRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout chooseViewPanelLayout = new org.jdesktop.layout.GroupLayout(chooseViewPanel);
        chooseViewPanel.setLayout(chooseViewPanelLayout);
        chooseViewPanelLayout.setHorizontalGroup(
            chooseViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(chooseViewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(chooseViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(wfRadioButton)
                    .add(mvRadioButton))
                .add(48, 48, 48)
                .add(chooseViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mvlRadioButton)
                    .add(svRadioButton))
                .addContainerGap(102, Short.MAX_VALUE))
        );
        chooseViewPanelLayout.setVerticalGroup(
            chooseViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(chooseViewPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(chooseViewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(chooseViewPanelLayout.createSequentialGroup()
                        .add(svRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mvlRadioButton))
                    .add(chooseViewPanelLayout.createSequentialGroup()
                        .add(wfRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mvRadioButton)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(1, 1, 1)
                .add(filePathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 376, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(browseButton))
            .add(layout.createSequentialGroup()
                .add(1, 1, 1)
                .add(extComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 327, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(okButton))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                .add(org.jdesktop.layout.GroupLayout.LEADING, previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                    .add(0, 0, 0)
                    .add(imageSizePanel, 0, 476, Short.MAX_VALUE))
                .add(org.jdesktop.layout.GroupLayout.LEADING, chooseViewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(filePathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(extComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cancelButton)
                    .add(okButton))
                .add(0, 0, 0)
                .add(imageSizePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(chooseViewPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(previewPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
		export.browseButtonActionPerformed(this);
	}//GEN-LAST:event_browseButtonActionPerformed

	private void extComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extComboBoxActionPerformed
		export.extComboBoxActionPerformed();
	}//GEN-LAST:event_extComboBoxActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
		static_frame.setVisible(false);
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
		try {
			if (export.okButtonActionPerformed()) {
				static_frame.setVisible(false);
			}
		} catch (IOException ex) {
			Logger.getLogger(ExportDialogGUI.class.getName()).log(Level.SEVERE, null, ex);
		}
	}//GEN-LAST:event_okButtonActionPerformed

	private void widthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_widthSpinnerStateChanged
		export.widthSpinnerStateChanged();
	}//GEN-LAST:event_widthSpinnerStateChanged

	private void heightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heightSpinnerStateChanged
		export.heightSpinnerStateChanged();
	}//GEN-LAST:event_heightSpinnerStateChanged

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
		int width = (Integer) widthSpinner.getValue();
		int height = (Integer) heightSpinner.getValue();
		int x = (Integer) xSpinner.getValue();
		int y = (Integer) ySpinner.getValue();
		ExportDialog.imageInfo.reset(width, height, x, y);
	}//GEN-LAST:event_applyButtonActionPerformed

	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
		export.resetButtonActionPerformed();
	}//GEN-LAST:event_resetButtonActionPerformed

	private void svRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svRadioButtonActionPerformed
		export.setComponent(export.determineSlicedComponent());
		export.previewImage();
	}//GEN-LAST:event_svRadioButtonActionPerformed

	private void mvRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mvRadioButtonActionPerformed
		AffyLabelledTierMap tm = (AffyLabelledTierMap) IGB.getSingleton().getMapView().getSeqMap();
		export.setComponent(tm.getSplitPane());
		export.previewImage();
	}//GEN-LAST:event_mvRadioButtonActionPerformed

	private void mvlRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mvlRadioButtonActionPerformed
		export.setComponent(IGB.getSingleton().getMapView().getSeqMap().getNeoCanvas());
		export.previewImage();
	}//GEN-LAST:event_mvlRadioButtonActionPerformed

	private void wfRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wfRadioButtonActionPerformed
		export.setComponent(IGB.getSingleton().getFrame());
		export.previewImage();
	}//GEN-LAST:event_wfRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel chooseViewPanel;
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
    private javax.swing.JButton resetButton;
    private javax.swing.JRadioButton svRadioButton;
    private javax.swing.JRadioButton wfRadioButton;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSpinner widthSpinner;
    private javax.swing.JLabel xLabel;
    private javax.swing.JSpinner xSpinner;
    private javax.swing.JLabel yLabel;
    private javax.swing.JSpinner ySpinner;
    // End of variables declaration//GEN-END:variables
}
