/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OtherOptions.java
 *
 * Created on May 31, 2011, 12:02:48 PM
 */
package com.affymetrix.igb.prefs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.JOptionPane;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.action.DrawCollapseControlAction;
import com.affymetrix.igb.shared.ResidueColorHelper;
import com.affymetrix.igb.stylesheet.XmlStylesheetParser;
import com.affymetrix.igb.tiers.CoordinateStyle;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.view.OrfAnalyzer;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.UnibrowHairline;
import com.affymetrix.igb.util.ColorUtils;
import com.affymetrix.igb.view.TierPrefsView;

/**
 *
 * @author lorainelab
 * Please be aware that any changes to the design from the gui builder will
 * result in two variables being generated which must be manually removed to prevent bugs on windows machines
 * jProgressBar1 and colorChooserPanel1
 */
public class OtherOptionsView extends IPrefEditorComponent implements ActionListener, PreferenceChangeListener {

	private static final long serialVersionUID = 1L;
	private final SeqMapView smv;
	private static OtherOptionsView singleton;
	String default_label_format = SeqMapView.VALUE_COORDINATE_LABEL_FORMAT_COMMA;
	String[] label_format_options = new String[]{SeqMapView.VALUE_COORDINATE_LABEL_FORMAT_FULL,
		SeqMapView.VALUE_COORDINATE_LABEL_FORMAT_COMMA,
		SeqMapView.VALUE_COORDINATE_LABEL_FORMAT_ABBREV};

	public static synchronized OtherOptionsView getSingleton() {
		if (singleton == null) {
			singleton = new OtherOptionsView();
		}
		return singleton;
	}

	/** Creates new form OtherOptions */
	public OtherOptionsView() {
		super();
		this.setName("Other Options");
		this.setLayout(new BorderLayout());

		Application igb = Application.getSingleton();
		if (igb != null) {
			smv = igb.getMapView();
			PreferenceUtils.getTopNode().addPreferenceChangeListener(this);
		} else {
			smv = null;
		}

		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        coordinatePanel = new javax.swing.JPanel();
        backgroundLabel = new javax.swing.JLabel();
        foregroundLabel = new javax.swing.JLabel();
        numFormatLabel = new javax.swing.JLabel();
        coordinates_label_format_CB = PreferenceUtils.createComboBox(PreferenceUtils.getTopNode(),
            SeqMapView.PREF_COORDINATE_LABEL_FORMAT,
            label_format_options,
            default_label_format)
        ;
        bgColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), CoordinateStyle.PREF_COORDINATE_BACKGROUND, CoordinateStyle.default_coordinate_background, this);
        fgColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), CoordinateStyle.PREF_COORDINATE_COLOR, CoordinateStyle.default_coordinate_color, this);
        orfAnalyzerPanel = new javax.swing.JPanel();
        stopCodonLabel = new javax.swing.JLabel();
        dynamicORFLabel = new javax.swing.JLabel();
        StopCodonColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), OrfAnalyzer.PREF_STOP_CODON_COLOR, OrfAnalyzer.default_stop_codon_color, this);
        DynamicORFColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), OrfAnalyzer.PREF_DYNAMIC_ORF_COLOR, OrfAnalyzer.default_dynamic_orf_color, this);
        residueColorPanel = new javax.swing.JPanel();
        aLabel = new javax.swing.JLabel();
        tLabel = new javax.swing.JLabel();
        gLabel = new javax.swing.JLabel();
        cLabel = new javax.swing.JLabel();
        otherLabel = new javax.swing.JLabel();
        AColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), ResidueColorHelper.PREF_A_COLOR, ResidueColorHelper.default_A_color, this);
        TColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), ResidueColorHelper.PREF_T_COLOR, ResidueColorHelper.default_T_color, this);
        GColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), ResidueColorHelper.PREF_G_COLOR, ResidueColorHelper.default_G_color, this);
        CColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), ResidueColorHelper.PREF_C_COLOR, ResidueColorHelper.default_C_color, this);
        OtherColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), ResidueColorHelper.PREF_OTHER_COLOR, ResidueColorHelper.default_other_color, this);
        askBeforeExitCheckBox = PreferenceUtils.createCheckBox("Ask before exit", PreferenceUtils.getTopNode(),
            PreferenceUtils.ASK_BEFORE_EXITING, PreferenceUtils.default_ask_before_exiting);
        confirmBeforeDeleteCheckBox = PreferenceUtils.createCheckBox("Confirm before delete", PreferenceUtils.getTopNode(),
            PreferenceUtils.CONFIRM_BEFORE_DELETE, PreferenceUtils.default_confirm_before_delete);
        keepZoomStripeCheckBox = PreferenceUtils.createCheckBox("Show Zoom Stripe", PreferenceUtils.getTopNode(),
            UnibrowHairline.PREF_KEEP_HAIRLINE_IN_VIEW, UnibrowHairline.default_keep_hairline_in_view);
        confirmBeforeLoadingCheckBox = PreferenceUtils.createCheckBox("Confirm before loading large data set", PreferenceUtils.getTopNode(),
            PreferenceUtils.CONFIRM_BEFORE_LOAD, PreferenceUtils.default_confirm_before_load);
        clear_prefsB = new javax.swing.JButton();
        showZoomStripLabelCheckBox = PreferenceUtils.createCheckBox("Show Zoom Stripe Label", PreferenceUtils.getTopNode(),
            UnibrowHairline.PREF_HAIRLINE_LABELED, UnibrowHairline.default_show_hairline_label);
        autoChangeView = PreferenceUtils.createCheckBox("Auto Change view for BAM/SAM", PreferenceUtils.getTopNode(),
            SeqMapView.PREF_AUTO_CHANGE_VIEW, SeqMapView.default_auto_change_view);
        edgeMatchPanel = new javax.swing.JPanel();
        edgeMatchColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), SeqMapView.PREF_EDGE_MATCH_COLOR, SeqMapView.default_edge_match_color, this);
        edgeMatchLabel = new javax.swing.JLabel();
        showCollapseOptionCheckBox = new javax.swing.JCheckBox();

        coordinatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Coordinates"));

        backgroundLabel.setText("Background:");

        foregroundLabel.setText("Foreground:");

        numFormatLabel.setText("Number format:");

        bgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgColorComboBoxActionPerformed(evt);
            }
        });

        fgColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fgColorComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout coordinatePanelLayout = new org.jdesktop.layout.GroupLayout(coordinatePanel);
        coordinatePanel.setLayout(coordinatePanelLayout);
        coordinatePanelLayout.setHorizontalGroup(
            coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(coordinatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(numFormatLabel)
                    .add(backgroundLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(coordinatePanelLayout.createSequentialGroup()
                        .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(42, 42, 42)
                        .add(foregroundLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(coordinates_label_format_CB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        coordinatePanelLayout.setVerticalGroup(
            coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(coordinatePanelLayout.createSequentialGroup()
                .add(coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(backgroundLabel)
                    .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(foregroundLabel)
                    .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(numFormatLabel)
                    .add(coordinates_label_format_CB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        orfAnalyzerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("ORF Analyzer (Sliced View tab)"));

        stopCodonLabel.setText("Stop Codon:");

        dynamicORFLabel.setText("Dynamic ORF:");

        org.jdesktop.layout.GroupLayout orfAnalyzerPanelLayout = new org.jdesktop.layout.GroupLayout(orfAnalyzerPanel);
        orfAnalyzerPanel.setLayout(orfAnalyzerPanelLayout);
        orfAnalyzerPanelLayout.setHorizontalGroup(
            orfAnalyzerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(orfAnalyzerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(stopCodonLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(StopCodonColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(dynamicORFLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(DynamicORFColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );
        orfAnalyzerPanelLayout.setVerticalGroup(
            orfAnalyzerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(orfAnalyzerPanelLayout.createSequentialGroup()
                .add(orfAnalyzerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(stopCodonLabel)
                    .add(dynamicORFLabel)
                    .add(StopCodonColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(DynamicORFColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        residueColorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Residue Colors"));

        aLabel.setText("A:");

        tLabel.setText("T:");

        gLabel.setText("G:");

        cLabel.setText("C:");

        otherLabel.setText("Other:");

        org.jdesktop.layout.GroupLayout residueColorPanelLayout = new org.jdesktop.layout.GroupLayout(residueColorPanel);
        residueColorPanel.setLayout(residueColorPanelLayout);
        residueColorPanelLayout.setHorizontalGroup(
            residueColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(residueColorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(aLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(AColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(tLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(TColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(13, 13, 13)
                .add(gLabel)
                .add(13, 13, 13)
                .add(GColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(CColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(13, 13, 13)
                .add(otherLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(OtherColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        residueColorPanelLayout.setVerticalGroup(
            residueColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(residueColorPanelLayout.createSequentialGroup()
                .add(residueColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(aLabel)
                    .add(AColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(gLabel)
                    .add(cLabel)
                    .add(otherLabel)
                    .add(tLabel)
                    .add(TColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(GColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(CColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(OtherColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        clear_prefsB.setText("Reset preference to defaults");

        edgeMatchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Edge Match"));
        edgeMatchPanel.setMaximumSize(new java.awt.Dimension(335, 57));
        edgeMatchPanel.setPreferredSize(new java.awt.Dimension(335, 57));

        edgeMatchLabel.setText("Color:");

        org.jdesktop.layout.GroupLayout edgeMatchPanelLayout = new org.jdesktop.layout.GroupLayout(edgeMatchPanel);
        edgeMatchPanel.setLayout(edgeMatchPanelLayout);
        edgeMatchPanelLayout.setHorizontalGroup(
            edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(edgeMatchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(edgeMatchLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(edgeMatchColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        edgeMatchPanelLayout.setVerticalGroup(
            edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(edgeMatchLabel)
                .add(edgeMatchColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        showCollapseOptionCheckBox.setText("Show Collapse Option");
        showCollapseOptionCheckBox.setSelected(TrackStyle.getDrawCollapseState());
        showCollapseOptionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCollapseOptionCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(3, 3, 3)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, orfAnalyzerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, residueColorPanel, 0, 326, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, coordinatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(keepZoomStripeCheckBox)
                            .add(showZoomStripLabelCheckBox)
                            .add(confirmBeforeDeleteCheckBox)
                            .add(confirmBeforeLoadingCheckBox)
                            .add(askBeforeExitCheckBox)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(edgeMatchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 326, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(showCollapseOptionCheckBox))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(autoChangeView))
                    .add(layout.createSequentialGroup()
                        .add(69, 69, 69)
                        .add(clear_prefsB)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {coordinatePanel, edgeMatchPanel, orfAnalyzerPanel, residueColorPanel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(coordinatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(residueColorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(orfAnalyzerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(edgeMatchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(askBeforeExitCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(confirmBeforeLoadingCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(confirmBeforeDeleteCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showZoomStripLabelCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(keepZoomStripeCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showCollapseOptionCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autoChangeView)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clear_prefsB)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        clear_prefsB.addActionListener(this);
    }// </editor-fold>//GEN-END:initComponents

	private void bgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorComboBoxActionPerformed
		TierPrefsView.getSingleton().refreshSeqMapView();
	}//GEN-LAST:event_bgColorComboBoxActionPerformed

	private void fgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorComboBoxActionPerformed
		TierPrefsView.getSingleton().refreshSeqMapView();
	}//GEN-LAST:event_fgColorComboBoxActionPerformed

	private void showCollapseOptionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCollapseOptionCheckBoxActionPerformed
		DrawCollapseControlAction.getAction().actionPerformed(evt);
	}//GEN-LAST:event_showCollapseOptionCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jidesoft.combobox.ColorComboBox AColorComboBox;
    private com.jidesoft.combobox.ColorComboBox CColorComboBox;
    private com.jidesoft.combobox.ColorComboBox DynamicORFColorComboBox;
    private com.jidesoft.combobox.ColorComboBox GColorComboBox;
    private com.jidesoft.combobox.ColorComboBox OtherColorComboBox;
    private com.jidesoft.combobox.ColorComboBox StopCodonColorComboBox;
    private com.jidesoft.combobox.ColorComboBox TColorComboBox;
    private javax.swing.JLabel aLabel;
    private javax.swing.JCheckBox askBeforeExitCheckBox;
    private javax.swing.JCheckBox autoChangeView;
    private javax.swing.JLabel backgroundLabel;
    private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
    private javax.swing.JLabel cLabel;
    private javax.swing.JButton clear_prefsB;
    private javax.swing.JCheckBox confirmBeforeDeleteCheckBox;
    private javax.swing.JCheckBox confirmBeforeLoadingCheckBox;
    private javax.swing.JPanel coordinatePanel;
    private javax.swing.JComboBox coordinates_label_format_CB;
    private javax.swing.JLabel dynamicORFLabel;
    private com.jidesoft.combobox.ColorComboBox edgeMatchColorComboBox;
    private javax.swing.JLabel edgeMatchLabel;
    private javax.swing.JPanel edgeMatchPanel;
    private com.jidesoft.combobox.ColorComboBox fgColorComboBox;
    private javax.swing.JLabel foregroundLabel;
    private javax.swing.JLabel gLabel;
    private javax.swing.JCheckBox keepZoomStripeCheckBox;
    private javax.swing.JLabel numFormatLabel;
    private javax.swing.JPanel orfAnalyzerPanel;
    private javax.swing.JLabel otherLabel;
    private javax.swing.JPanel residueColorPanel;
    private javax.swing.JCheckBox showCollapseOptionCheckBox;
    private javax.swing.JCheckBox showZoomStripLabelCheckBox;
    private javax.swing.JLabel stopCodonLabel;
    private javax.swing.JLabel tLabel;
    // End of variables declaration//GEN-END:variables

	public void preferenceChange(PreferenceChangeEvent pce) {
		if (!pce.getNode().equals(PreferenceUtils.getTopNode()) || smv == null) {
			return;
		}

		if (pce.getKey().equals(SeqMapView.PREF_AUTO_CHANGE_VIEW)) {
			ThreadUtils.runOnEventQueue(new Runnable() {

				public void run() {
					smv.setAnnotatedSeq(smv.getAnnotatedSeq(), true, true, true);
				}
			});
		}
	}

	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		if (src == clear_prefsB) {
			// The option pane used differs from the confirmDialog only in
			// that "No" is the default choice.
			String[] options = {"Yes", "No"};
			if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
					this, "Really reset all preferences to defaults?\n(this will also exit the application)", "Clear preferences?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					options, options[1])) {

				try {
					XmlStylesheetParser.removeUserStylesheetFile();
					((IGB) Application.getSingleton()).defaultCloseOperations();
					PreferenceUtils.clearPreferences();
					System.exit(0);
				} catch (Exception e) {
					ErrorHandler.errorPanel("ERROR", "Error clearing preferences", e);
				}
			}
		}
	}

	public void refresh() {
		//Update Coordinate Track Colors
		bgColorComboBox.setSelectedColor(CoordinateStyle.coordinate_annot_style.getBackground());
		fgColorComboBox.setSelectedColor(CoordinateStyle.coordinate_annot_style.getForeground());
	}
}
