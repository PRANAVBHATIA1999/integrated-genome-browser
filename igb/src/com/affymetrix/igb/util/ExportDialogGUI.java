package com.affymetrix.igb.util;

import com.affymetrix.genometryImpl.util.DisplayUtils;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.tiers.AffyLabelledTierMap;
import com.affymetrix.igb.tiers.AffyTieredMap;
import com.affymetrix.igb.view.AltSpliceView;
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
	private static final String TITLE = "Export Image";
	private static ExportDialogGUI singleton;
	private ExportDialog export;
	private AffyTieredMap seqMap;
	private Component wholeFrame;
	private Component mainView;
	private Component mainViewWithLabels;
	private Component slicedView;

	public synchronized void display(boolean isSequenceViewer) {
		initRadioButton(isSequenceViewer);

		initFrame();

		DisplayUtils.bringFrameToFront(static_frame);

		export.previewImage();
	}

	private void initRadioButton(boolean isSequenceViewer) {
		if (!isSequenceViewer) {
			initView();

			if (svRadioButton.isSelected()) {
				export.setComponent(slicedView);
			} else if (mvRadioButton.isSelected()) {
				export.setComponent(mainView);
			} else if (mvlRadioButton.isSelected()) {
				export.setComponent(mainViewWithLabels);
			} else {
				export.setComponent(wholeFrame);
				wfRadioButton.setSelected(true);
			}

			mvRadioButton.setEnabled(seqMap.getTiers().size() != 0);
			mvlRadioButton.setEnabled(seqMap.getTiers().size() != 0);
			svRadioButton.setEnabled(seqMap.getSelected().size() != 0);
		}

		buttonsPanel.setVisible(!isSequenceViewer);
	}

	private void initView() {
		seqMap = IGB.getSingleton().getMapView().getSeqMap();
		wholeFrame = IGB.getSingleton().getFrame().getContentPane();

		mainView = seqMap.getNeoCanvas();

		AffyLabelledTierMap tm = (AffyLabelledTierMap) seqMap;
		mainViewWithLabels = tm.getSplitPane();

		AltSpliceView slice_view = (AltSpliceView) ((IGB) IGB.getSingleton()).getView(AltSpliceView.class.getName());
		slicedView = ((AffyLabelledTierMap) slice_view.getSplicedView().getSeqMap()).getSplitPane();
	}

	private void initFrame() {
		if (static_frame == null) {
			export.init();
			static_frame = PreferenceUtils.createFrame(TITLE, singleton);
			static_frame.setLocationRelativeTo(IGB.getSingleton().getFrame());
		} else {
			export.initSpinner((String) unitComboBox.getSelectedItem());
		}
	}

	private void setEnable(boolean b) {
		this.wfRadioButton.setEnabled(!b);
		this.mvRadioButton.setEnabled(!b);
		this.mvlRadioButton.setEnabled(!b);
		this.svRadioButton.setEnabled(!b);
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
        imageSizePanel = new javax.swing.JPanel();
        widthLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        widthSpinner = export.widthSpinner;
        heightSpinner = export.heightSpinner;
        xLabel = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();
        resolutionComboBox = export.resolutionComboBox;
        unitComboBox = export.unitComboBox;
        sizeLabel = export.sizeLabel;
        previewPanel = new javax.swing.JPanel();
        previewLabel = export.previewLabel;
        buttonsPanel = new javax.swing.JPanel();
        svRadioButton = new javax.swing.JRadioButton();
        mvRadioButton = new javax.swing.JRadioButton();
        wfRadioButton = new javax.swing.JRadioButton();
        mvlRadioButton = new javax.swing.JRadioButton();
        topPanel = new javax.swing.JPanel();
        browseButton = new javax.swing.JButton();
        filePathTextField = export.filePathTextField;
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        extComboBox = export.extComboBox;

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

        xLabel.setText("Resolution:");

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
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
                        .add(xLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resolutionComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .add(14, 14, 14)
                .add(imageSizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(resetButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(unitComboBox, 0, 91, Short.MAX_VALUE))
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
                    .add(resetButton)
                    .add(xLabel)))
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
                .add(15, 15, 15)
                .add(previewLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(previewLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, topPanelLayout.createSequentialGroup()
                .add(filePathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton))
            .add(topPanelLayout.createSequentialGroup()
                .add(extComboBox, 0, 309, Short.MAX_VALUE)
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
                    .add(previewPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(imageSizePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(topPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
		export.browseButtonActionPerformed(this);
	}//GEN-LAST:event_browseButtonActionPerformed

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

	private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
		export.resetButtonActionPerformed();
	}//GEN-LAST:event_resetButtonActionPerformed

	private void mvRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mvRadioButtonActionPerformed
		export.setComponent(mainView);
		export.initSpinner((String) unitComboBox.getSelectedItem());
		export.previewImage();
	}//GEN-LAST:event_mvRadioButtonActionPerformed

	private void mvlRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mvlRadioButtonActionPerformed
		export.setComponent(mainViewWithLabels);
		export.initSpinner((String) unitComboBox.getSelectedItem());
		export.previewImage();
	}//GEN-LAST:event_mvlRadioButtonActionPerformed

	private void wfRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wfRadioButtonActionPerformed
		export.setComponent(wholeFrame);
		export.initSpinner((String) unitComboBox.getSelectedItem());
		export.previewImage();
	}//GEN-LAST:event_wfRadioButtonActionPerformed

	private void svRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_svRadioButtonActionPerformed
		export.setComponent(slicedView);
		export.initSpinner((String) unitComboBox.getSelectedItem());
		export.previewImage();
	}//GEN-LAST:event_svRadioButtonActionPerformed

	private void resolutionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboBoxActionPerformed
		export.resolutionComboBoxActionPerformed();
	}//GEN-LAST:event_resolutionComboBoxActionPerformed

	private void unitComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unitComboBoxActionPerformed
		export.unitComboBoxActionPerformed();
	}//GEN-LAST:event_unitComboBoxActionPerformed
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
    private javax.swing.JButton resetButton;
    private javax.swing.JComboBox resolutionComboBox;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JRadioButton svRadioButton;
    private javax.swing.JPanel topPanel;
    private javax.swing.JComboBox unitComboBox;
    private javax.swing.JRadioButton wfRadioButton;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JSpinner widthSpinner;
    private javax.swing.JLabel xLabel;
    // End of variables declaration//GEN-END:variables
}
