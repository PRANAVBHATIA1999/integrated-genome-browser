package com.affymetrix.igb.prefs;

import java.awt.BorderLayout;
import java.util.EventObject;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.action.ClearPreferencesAction;
import com.affymetrix.igb.shared.CodonGlyph;
import com.affymetrix.igb.shared.ResidueColorHelper;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor.TrackStylePropertyListener;
import com.affymetrix.igb.tiers.AccordionTierResizer;
import com.affymetrix.igb.tiers.TierResizer;
import com.affymetrix.igb.util.ColorUtils;
import com.affymetrix.igb.view.OrfAnalyzer;
import com.affymetrix.igb.view.SeqMapView;

/**
 *
 * @author nick
 */
public class OtherOptionsView extends IPrefEditorComponent implements PreferenceChangeListener, TrackStylePropertyListener {

	private static final long serialVersionUID = 1L;
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
		initComponents();
		TrackstylePropertyMonitor.getPropertyTracker().addPropertyListener(this);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codonButtonGroup = new javax.swing.ButtonGroup();
        trackResizeGroup = new javax.swing.ButtonGroup();
        coordinatePanel = new javax.swing.JPanel();
        numFormatLabel = new javax.swing.JLabel();
        coordinates_label_format_CB = PreferenceUtils.createComboBox(PreferenceUtils.getTopNode(),
            SeqMapView.PREF_COORDINATE_LABEL_FORMAT,
            label_format_options,
            default_label_format)
        ;
        orfAnalyzerPanel = new javax.swing.JPanel();
        stopCodonLabel = new javax.swing.JLabel();
        dynamicORFLabel = new javax.swing.JLabel();
        StopCodonColorComboBox = ColorUtils.createColorComboBox(OrfAnalyzer.PREF_STOP_CODON_COLOR, OrfAnalyzer.default_stop_codon_color, this);
        DynamicORFColorComboBox = ColorUtils.createColorComboBox(OrfAnalyzer.PREF_DYNAMIC_ORF_COLOR, OrfAnalyzer.default_dynamic_orf_color, this);
        bgLabel = new javax.swing.JLabel();
        bgComboBox = ColorUtils.createColorComboBox(OrfAnalyzer.PREF_BACKGROUND_COLOR, OrfAnalyzer.default_background_color, this);
        residueColorPanel = new javax.swing.JPanel();
        aLabel = new javax.swing.JLabel();
        tLabel = new javax.swing.JLabel();
        gLabel = new javax.swing.JLabel();
        cLabel = new javax.swing.JLabel();
        otherLabel = new javax.swing.JLabel();
        AColorComboBox = ColorUtils.createColorComboBox(ResidueColorHelper.PREF_A_COLOR, ResidueColorHelper.default_A_color, this);
        TColorComboBox = ColorUtils.createColorComboBox(ResidueColorHelper.PREF_T_COLOR, ResidueColorHelper.default_T_color, this);
        GColorComboBox = ColorUtils.createColorComboBox(ResidueColorHelper.PREF_G_COLOR, ResidueColorHelper.default_G_color, this);
        CColorComboBox = ColorUtils.createColorComboBox(ResidueColorHelper.PREF_C_COLOR, ResidueColorHelper.default_C_color, this);
        OtherColorComboBox = ColorUtils.createColorComboBox(ResidueColorHelper.PREF_OTHER_COLOR, ResidueColorHelper.default_other_color, this);
        askBeforeExitCheckBox = PreferenceUtils.createCheckBox("Ask Before Exit",
            PreferenceUtils.ASK_BEFORE_EXITING, PreferenceUtils.default_ask_before_exiting);
        confirmBeforeDeleteCheckBox = PreferenceUtils.createCheckBox("Confirm Before Delete",
            PreferenceUtils.CONFIRM_BEFORE_DELETE, PreferenceUtils.default_confirm_before_delete);
        clear_prefsB = new javax.swing.JButton(ClearPreferencesAction.getAction());
        edgeMatchPanel = new javax.swing.JPanel();
        edgeMatchColorComboBox = ColorUtils.createColorComboBox(SeqMapView.PREF_EDGE_MATCH_COLOR, SeqMapView.default_edge_match_color, this);
        edgeMatchLabel = new javax.swing.JLabel();
        showEdgeMatchCheckBox = PreferenceUtils.createCheckBox("Show Edge Matching", PreferenceUtils.SHOW_EDGEMATCH_OPTION, PreferenceUtils.default_show_edge_match);
        confirmBeforeLoadCheckBox = PreferenceUtils.createCheckBox("Confirm Before Loading Large Data Set",
            PreferenceUtils.CONFIRM_BEFORE_LOAD, PreferenceUtils.default_confirm_before_load);
        displayOption = PreferenceUtils.createCheckBox("Display Errors on Status Bar",
            PreferenceUtils.DISPLAY_ERRORS_STATUS_BAR, PreferenceUtils.default_display_errors);
        jPanel1 = new javax.swing.JPanel();
        hideButton = PreferenceUtils.createRadioButton("Hide", "0", PreferenceUtils.getTopNode(),
            CodonGlyph.CODON_GLYPH_CODE_SIZE, String.valueOf(CodonGlyph.default_codon_glyph_code_size));
        oneLetterButton = PreferenceUtils.createRadioButton("One Letter", "1", PreferenceUtils.getTopNode(),
            CodonGlyph.CODON_GLYPH_CODE_SIZE, String.valueOf(CodonGlyph.default_codon_glyph_code_size));
        threeLetterButton = PreferenceUtils.createRadioButton("Three Letter", "3", PreferenceUtils.getTopNode(),
            CodonGlyph.CODON_GLYPH_CODE_SIZE, String.valueOf(CodonGlyph.default_codon_glyph_code_size));
        jPanel2 = new javax.swing.JPanel();
        jRadioButton1 = PreferenceUtils.createRadioButton("Adjust All Tracks", AccordionTierResizer.class.getSimpleName(),
            PreferenceUtils.getTopNode(), SeqMapView.PREF_TRACK_RESIZING_BEHAVIOR, TierResizer.class.getSimpleName());
        jRadioButton2 = PreferenceUtils.createRadioButton("Adjust Adjacent Tracks", TierResizer.class.getSimpleName(),
            PreferenceUtils.getTopNode(), SeqMapView.PREF_TRACK_RESIZING_BEHAVIOR, TierResizer.class.getSimpleName());

        setPreferredSize(new java.awt.Dimension(545, 540));

        coordinatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Coordinates"));
        coordinatePanel.setPreferredSize(new java.awt.Dimension(510, 55));

        numFormatLabel.setText("Number format:");

        org.jdesktop.layout.GroupLayout coordinatePanelLayout = new org.jdesktop.layout.GroupLayout(coordinatePanel);
        coordinatePanel.setLayout(coordinatePanelLayout);
        coordinatePanelLayout.setHorizontalGroup(
            coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(coordinatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(numFormatLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(coordinates_label_format_CB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        coordinatePanelLayout.setVerticalGroup(
            coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(coordinatePanelLayout.createSequentialGroup()
                .add(coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(numFormatLabel)
                    .add(coordinates_label_format_CB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5))
        );

        orfAnalyzerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("ORF Analyzer (Sliced View tab)"));
        orfAnalyzerPanel.setPreferredSize(new java.awt.Dimension(510, 55));

        stopCodonLabel.setText("Stop Codon:");

        dynamicORFLabel.setText("Dynamic ORF:");

        bgLabel.setText("Background: ");

        org.jdesktop.layout.GroupLayout orfAnalyzerPanelLayout = new org.jdesktop.layout.GroupLayout(orfAnalyzerPanel);
        orfAnalyzerPanel.setLayout(orfAnalyzerPanelLayout);
        orfAnalyzerPanelLayout.setHorizontalGroup(
            orfAnalyzerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(orfAnalyzerPanelLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(stopCodonLabel)
                .add(10, 10, 10)
                .add(StopCodonColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(62, 62, 62)
                .add(dynamicORFLabel)
                .add(10, 10, 10)
                .add(DynamicORFColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(62, 62, 62)
                .add(bgLabel)
                .add(10, 10, 10)
                .add(bgComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        orfAnalyzerPanelLayout.setVerticalGroup(
            orfAnalyzerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(orfAnalyzerPanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(orfAnalyzerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(stopCodonLabel)
                    .add(dynamicORFLabel)
                    .add(StopCodonColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(DynamicORFColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bgLabel)
                    .add(bgComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5))
        );

        residueColorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Residue Colors"));
        residueColorPanel.setPreferredSize(new java.awt.Dimension(510, 55));

        aLabel.setText("A:");

        tLabel.setText("T:");

        gLabel.setText("G:");

        cLabel.setText("C:");

        otherLabel.setText("Other:");

        AColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AColorComboBoxActionPerformed(evt);
            }
        });

        TColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TColorComboBoxActionPerformed(evt);
            }
        });

        GColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GColorComboBoxActionPerformed(evt);
            }
        });

        CColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CColorComboBoxActionPerformed(evt);
            }
        });

        OtherColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OtherColorComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout residueColorPanelLayout = new org.jdesktop.layout.GroupLayout(residueColorPanel);
        residueColorPanel.setLayout(residueColorPanelLayout);
        residueColorPanelLayout.setHorizontalGroup(
            residueColorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(residueColorPanelLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(aLabel)
                .add(10, 10, 10)
                .add(AColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(54, 54, 54)
                .add(tLabel)
                .add(10, 10, 10)
                .add(TColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(54, 54, 54)
                .add(gLabel)
                .add(10, 10, 10)
                .add(GColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(54, 54, 54)
                .add(cLabel)
                .add(10, 10, 10)
                .add(CColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(54, 54, 54)
                .add(otherLabel)
                .add(10, 10, 10)
                .add(OtherColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                .add(5, 5, 5))
        );

        clear_prefsB.setText("Reset Preference to Defaults");
        clear_prefsB.setMaximumSize(new java.awt.Dimension(32767, 32767));
        clear_prefsB.setMinimumSize(new java.awt.Dimension(0, 0));
        clear_prefsB.setPreferredSize(new java.awt.Dimension(210, 29));

        edgeMatchPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Edge Match"));
        edgeMatchPanel.setPreferredSize(new java.awt.Dimension(510, 55));

        edgeMatchLabel.setText("Color:");

        org.jdesktop.layout.GroupLayout edgeMatchPanelLayout = new org.jdesktop.layout.GroupLayout(edgeMatchPanel);
        edgeMatchPanel.setLayout(edgeMatchPanelLayout);
        edgeMatchPanelLayout.setHorizontalGroup(
            edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(edgeMatchPanelLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(edgeMatchLabel)
                .add(10, 10, 10)
                .add(edgeMatchColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(191, 191, 191)
                .add(showEdgeMatchCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        edgeMatchPanelLayout.setVerticalGroup(
            edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showEdgeMatchCheckBox)
            .add(edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(edgeMatchLabel)
                .add(edgeMatchColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        if(displayOption.isSelected())
        com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(IGB.getSingleton().status_bar);
        else
        com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(null);
        displayOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                displayOptionStateChanged(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Codon Display"));

        codonButtonGroup.add(hideButton);
        hideButton.setText("Hide");

        codonButtonGroup.add(oneLetterButton);
        oneLetterButton.setText("One Letter");

        codonButtonGroup.add(threeLetterButton);
        threeLetterButton.setText("Three Letter");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(hideButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 104, Short.MAX_VALUE)
                .add(oneLetterButton)
                .add(86, 86, 86)
                .add(threeLetterButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hideButton)
                    .add(oneLetterButton)
                    .add(threeLetterButton))
                .add(0, 6, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Track Resize Behavior"));

        trackResizeGroup.add(jRadioButton1);
        jRadioButton1.setText("Adjust All Tracks");

        trackResizeGroup.add(jRadioButton2);
        jRadioButton2.setText("Adjust Adjacent Tracks");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jRadioButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jRadioButton2)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jRadioButton1)
                    .add(jRadioButton2))
                .add(0, 12, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(clear_prefsB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 482, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(15, 15, 15)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(edgeMatchPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                            .add(orfAnalyzerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                            .add(residueColorPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(confirmBeforeDeleteCheckBox)
                                    .add(askBeforeExitCheckBox))
                                .add(55, 55, 55)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(confirmBeforeLoadCheckBox)
                                    .add(displayOption)))
                            .add(coordinatePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE))))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(coordinatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(residueColorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(orfAnalyzerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(edgeMatchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(confirmBeforeLoadCheckBox)
                    .add(askBeforeExitCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(confirmBeforeDeleteCheckBox)
                    .add(displayOption))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(clear_prefsB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	boolean refresh = false;
	private void AColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AColorComboBoxActionPerformed
		TierPrefsView.getSingleton().refreshSeqMapViewAndSlicedView();
	}//GEN-LAST:event_AColorComboBoxActionPerformed

	private void TColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TColorComboBoxActionPerformed
		TierPrefsView.getSingleton().refreshSeqMapViewAndSlicedView();
	}//GEN-LAST:event_TColorComboBoxActionPerformed

	private void GColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GColorComboBoxActionPerformed
		TierPrefsView.getSingleton().refreshSeqMapViewAndSlicedView();
	}//GEN-LAST:event_GColorComboBoxActionPerformed

	private void CColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CColorComboBoxActionPerformed
		TierPrefsView.getSingleton().refreshSeqMapViewAndSlicedView();
	}//GEN-LAST:event_CColorComboBoxActionPerformed

	private void OtherColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OtherColorComboBoxActionPerformed
		TierPrefsView.getSingleton().refreshSeqMapViewAndSlicedView();
	}//GEN-LAST:event_OtherColorComboBoxActionPerformed

	private void displayOptionStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_displayOptionStateChanged
		if(displayOption.isSelected())
			com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(IGB.getSingleton().status_bar);
		else
			com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(null);
	}//GEN-LAST:event_displayOptionStateChanged

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
    private com.jidesoft.combobox.ColorComboBox bgComboBox;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JLabel cLabel;
    private javax.swing.JButton clear_prefsB;
    private javax.swing.ButtonGroup codonButtonGroup;
    private javax.swing.JCheckBox confirmBeforeDeleteCheckBox;
    private javax.swing.JCheckBox confirmBeforeLoadCheckBox;
    private javax.swing.JPanel coordinatePanel;
    private javax.swing.JComboBox coordinates_label_format_CB;
    private javax.swing.JCheckBox displayOption;
    private javax.swing.JLabel dynamicORFLabel;
    private com.jidesoft.combobox.ColorComboBox edgeMatchColorComboBox;
    private javax.swing.JLabel edgeMatchLabel;
    private javax.swing.JPanel edgeMatchPanel;
    private javax.swing.JLabel gLabel;
    private javax.swing.JRadioButton hideButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JLabel numFormatLabel;
    private javax.swing.JRadioButton oneLetterButton;
    private javax.swing.JPanel orfAnalyzerPanel;
    private javax.swing.JLabel otherLabel;
    private javax.swing.JPanel residueColorPanel;
    private javax.swing.JCheckBox showEdgeMatchCheckBox;
    private javax.swing.JLabel stopCodonLabel;
    private javax.swing.JLabel tLabel;
    private javax.swing.JRadioButton threeLetterButton;
    private javax.swing.ButtonGroup trackResizeGroup;
    // End of variables declaration//GEN-END:variables

	@Override
	public void preferenceChange(PreferenceChangeEvent pce) {
		
	}

	@Override
	public void refresh() {
		refresh = true;
		//Update Coordinate Track Colors
		//bgColorComboBox.setSelectedColor(CoordinateStyle.coordinate_annot_style.getBackground());
		//fgColorComboBox.setSelectedColor(CoordinateStyle.coordinate_annot_style.getForeground());
		refresh = false;
	}

	@Override
	public void trackstylePropertyChanged(EventObject eo) {
		refresh();
	}
}
