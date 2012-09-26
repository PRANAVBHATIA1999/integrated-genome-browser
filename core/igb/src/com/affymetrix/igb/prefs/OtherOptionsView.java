package com.affymetrix.igb.prefs;

import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.action.ClearPreferencesAction;
import com.affymetrix.igb.action.DrawCollapseControlAction;
import com.affymetrix.igb.shared.ResidueColorHelper;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor.TrackStylePropertyListener;
import com.affymetrix.igb.tiers.CoordinateStyle;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.util.ColorUtils;
import com.affymetrix.igb.view.OrfAnalyzer;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.UnibrowHairline;
import java.awt.BorderLayout;
import java.util.EventObject;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 *
 * @author nick
 */
public class OtherOptionsView extends IPrefEditorComponent implements PreferenceChangeListener, TrackStylePropertyListener {

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
		TrackstylePropertyMonitor.getPropertyTracker().addPropertyListener(this);
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
        bgLabel = new javax.swing.JLabel();
        bgComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), OrfAnalyzer.PREF_BACKGROUND_COLOR, OrfAnalyzer.default_background_color, this);
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
        clear_prefsB = new javax.swing.JButton(ClearPreferencesAction.getAction());
        showZoomStripLabelCheckBox = PreferenceUtils.createCheckBox("Show Zoom Stripe Label", PreferenceUtils.getTopNode(),
            UnibrowHairline.PREF_HAIRLINE_LABELED, UnibrowHairline.default_show_hairline_label);
        edgeMatchPanel = new javax.swing.JPanel();
        edgeMatchColorComboBox = ColorUtils.createColorComboBox(PreferenceUtils.getTopNode(), SeqMapView.PREF_EDGE_MATCH_COLOR, SeqMapView.default_edge_match_color, this);
        edgeMatchLabel = new javax.swing.JLabel();
        showCollapseOptionCheckBox = PreferenceUtils.createCheckBox("Show Collapse Option", PreferenceUtils.getTopNode(),     PreferenceUtils.SHOW_COLLAPSE_OPTION, TrackStyle.getDrawCollapseState());
        confirmBeforeLoadCheckBox = PreferenceUtils.createCheckBox("Confirm before loading large data set", PreferenceUtils.getTopNode(),
            PreferenceUtils.CONFIRM_BEFORE_LOAD, PreferenceUtils.default_confirm_before_load);
        displayOption = PreferenceUtils.createCheckBox("Display Errors on Status Bar", PreferenceUtils.getTopNode(),
            PreferenceUtils.DISPLAY_ERRORS_STATUS_BAR, PreferenceUtils.default_display_errors);
        autoloadSequenceCheckBox = PreferenceUtils.createCheckBox("Autoload Sequence", PreferenceUtils.getTopNode(),       PreferenceUtils.AUTO_LOAD_SEQUENCE, PreferenceUtils.default_auto_load_sequence);

        setPreferredSize(new java.awt.Dimension(545, 540));

        coordinatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Coordinates"));
        coordinatePanel.setPreferredSize(new java.awt.Dimension(510, 55));

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
                .add(10, 10, 10)
                .add(backgroundLabel)
                .add(10, 10, 10)
                .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(30, 30, 30)
                .add(foregroundLabel)
                .add(10, 10, 10)
                .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(30, 30, 30)
                .add(numFormatLabel)
                .add(10, 10, 10)
                .add(coordinates_label_format_CB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5))
        );
        coordinatePanelLayout.setVerticalGroup(
            coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(coordinatePanelLayout.createSequentialGroup()
                .add(coordinatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(backgroundLabel)
                    .add(bgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(foregroundLabel)
                    .add(fgColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(numFormatLabel)
                    .add(coordinates_label_format_CB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5))
        );

        orfAnalyzerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("ORF Analyzer (Sliced View tab)"));
        orfAnalyzerPanel.setPreferredSize(new java.awt.Dimension(510, 55));

        stopCodonLabel.setText("Stop Codon:");

        dynamicORFLabel.setText("Dynamic ORF:");

        DynamicORFColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DynamicORFColorComboBoxActionPerformed(evt);
            }
        });

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
                .addContainerGap(27, Short.MAX_VALUE))
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

        askBeforeExitCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                askBeforeExitCheckBoxActionPerformed(evt);
            }
        });

        confirmBeforeDeleteCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmBeforeDeleteCheckBoxActionPerformed(evt);
            }
        });

        clear_prefsB.setText("Reset preference to defaults");
        clear_prefsB.setMaximumSize(new java.awt.Dimension(32767, 32767));
        clear_prefsB.setMinimumSize(new java.awt.Dimension(0, 0));
        clear_prefsB.setPreferredSize(new java.awt.Dimension(210, 29));
        clear_prefsB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_prefsBActionPerformed(evt);
            }
        });

        showZoomStripLabelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showZoomStripLabelCheckBoxActionPerformed(evt);
            }
        });

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
                .addContainerGap(420, Short.MAX_VALUE))
        );
        edgeMatchPanelLayout.setVerticalGroup(
            edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(edgeMatchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(edgeMatchLabel)
                .add(edgeMatchColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        showCollapseOptionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCollapseOptionCheckBoxActionPerformed(evt);
            }
        });

        if(displayOption.isSelected())
        com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(IGB.status_bar);
        else
        com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(null);
        displayOption.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                displayOptionStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(15, 15, 15)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(confirmBeforeLoadCheckBox)
                                    .add(confirmBeforeDeleteCheckBox)
                                    .add(askBeforeExitCheckBox)
                                    .add(autoloadSequenceCheckBox)
                                    .add(displayOption))
                                .add(30, 30, 30)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(keepZoomStripeCheckBox)
                                    .add(showZoomStripLabelCheckBox)
                                    .add(showCollapseOptionCheckBox)))
                            .add(orfAnalyzerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(edgeMatchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(coordinatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(residueColorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(clear_prefsB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 510, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(20, 20, 20))
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(askBeforeExitCheckBox)
                            .add(keepZoomStripeCheckBox))
                        .add(10, 10, 10)
                        .add(confirmBeforeDeleteCheckBox))
                    .add(showZoomStripLabelCheckBox))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(confirmBeforeLoadCheckBox)
                    .add(showCollapseOptionCheckBox))
                .add(8, 8, 8)
                .add(autoloadSequenceCheckBox)
                .add(16, 16, 16)
                .add(displayOption)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(clear_prefsB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	boolean refresh = false;
	private void bgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgColorComboBoxActionPerformed
		if(!refresh){
			TierPrefsView.getSingleton().refreshSeqMapViewAndSlicedView();
			TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(evt);
		}
	}//GEN-LAST:event_bgColorComboBoxActionPerformed

	private void fgColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fgColorComboBoxActionPerformed
		if(!refresh){
			TierPrefsView.getSingleton().refreshSeqMapViewAndSlicedView();
			TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(evt);
		}
	}//GEN-LAST:event_fgColorComboBoxActionPerformed

	private void showCollapseOptionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCollapseOptionCheckBoxActionPerformed
		DrawCollapseControlAction.getAction().actionPerformed(evt);
	}//GEN-LAST:event_showCollapseOptionCheckBoxActionPerformed

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
			com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(IGB.status_bar);
		else
			com.affymetrix.genometryImpl.util.ErrorHandler.setDisplayHandler(null);
	}//GEN-LAST:event_displayOptionStateChanged

	private void DynamicORFColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DynamicORFColorComboBoxActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_DynamicORFColorComboBoxActionPerformed

	private void showZoomStripLabelCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showZoomStripLabelCheckBoxActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_showZoomStripLabelCheckBoxActionPerformed

	private void confirmBeforeDeleteCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmBeforeDeleteCheckBoxActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_confirmBeforeDeleteCheckBoxActionPerformed

	private void askBeforeExitCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_askBeforeExitCheckBoxActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_askBeforeExitCheckBoxActionPerformed

	private void clear_prefsBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_prefsBActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_clear_prefsBActionPerformed

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
    private javax.swing.JCheckBox autoloadSequenceCheckBox;
    private javax.swing.JLabel backgroundLabel;
    private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
    private com.jidesoft.combobox.ColorComboBox bgComboBox;
    private javax.swing.JLabel bgLabel;
    private javax.swing.JLabel cLabel;
    private javax.swing.JButton clear_prefsB;
    private javax.swing.JCheckBox confirmBeforeDeleteCheckBox;
    private javax.swing.JCheckBox confirmBeforeLoadCheckBox;
    private javax.swing.JPanel coordinatePanel;
    private javax.swing.JComboBox coordinates_label_format_CB;
    private javax.swing.JCheckBox displayOption;
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

	@Override
	public void preferenceChange(PreferenceChangeEvent pce) {
		if (!pce.getNode().equals(PreferenceUtils.getTopNode()) || smv == null) {
			return;
		}

		if (pce.getKey().equals(SeqMapView.PREF_AUTO_CHANGE_VIEW)) {
			ThreadUtils.runOnEventQueue(new Runnable() {

				@Override
				public void run() {
					smv.updatePanel();
				}
			});
		}
	}

	@Override
	public void refresh() {
		refresh = true;
		//Update Coordinate Track Colors
		bgColorComboBox.setSelectedColor(CoordinateStyle.coordinate_annot_style.getBackground());
		fgColorComboBox.setSelectedColor(CoordinateStyle.coordinate_annot_style.getForeground());
		refresh = false;
	}

	@Override
	public void trackstylePropertyChanged(EventObject eo) {
		refresh();
	}
}
