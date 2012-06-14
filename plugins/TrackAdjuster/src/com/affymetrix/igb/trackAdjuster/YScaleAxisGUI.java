package com.affymetrix.igb.trackAdjuster;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.event.SymSelectionEvent;
import com.affymetrix.genometryImpl.event.SymSelectionListener;
import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.AbstractGraphGlyph;
import com.affymetrix.igb.shared.MultiGraphGlyph;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.shared.ViewModeGlyph;
import com.affymetrix.igb.viewmode.ComboGlyphFactory.ComboGlyph;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class YScaleAxisGUI extends javax.swing.JPanel implements SeqSelectionListener, SymSelectionListener {

	private static final long serialVersionUID = 1L;
	private final IGBService igbService;
	private GraphVisibleBoundsSetter vis_bounds_setter;
	private final List<AbstractGraphGlyph> graphGlyphs = new ArrayList<AbstractGraphGlyph>();
	private boolean is_listening = true; // used to turn on and off listening to GUI events

	/**
	 * Creates new form YScaleAxisGUI
	 */
	public YScaleAxisGUI(IGBService igbService) {
		super();
		this.igbService = igbService;
		vis_bounds_setter = new GraphVisibleBoundsSetter(igbService.getSeqMap());
		initComponents();
		resetAll(new ArrayList<AbstractGraphGlyph>());
		GenometryModel gmodel = GenometryModel.getGenometryModel();
		gmodel.addSeqSelectionListener(this);
		gmodel.addSymSelectionListener(this);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stylegroup = new javax.swing.ButtonGroup();
        DisplayModeButtonGroup = new javax.swing.ButtonGroup();
        VisibleRangeButtonGroup = new javax.swing.ButtonGroup();
        jMenu1 = new javax.swing.JMenu();
        DisplayButtonGroup = new javax.swing.ButtonGroup();
        RangePanel = new javax.swing.JPanel();
        setByLabel = new javax.swing.JLabel();
        by_valRB_val = vis_bounds_setter.by_valRB;
        by_percentileRB_val = vis_bounds_setter.by_percentileRB;
        rangeSlider = vis_bounds_setter.ValueSlider;
        minValLabel = new javax.swing.JLabel();
        minText = vis_bounds_setter.min_valT;
        maxText = vis_bounds_setter.max_valT;
        maxValLabel = new javax.swing.JLabel();
        otherOptionsButton = new javax.swing.JButton();
        heightLabel = new javax.swing.JLabel();
        heightSlider = new javax.swing.JSlider(javax.swing.JSlider.HORIZONTAL, 10, 500, 50);

        jMenu1.setText("jMenu1");

        RangePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Y Axis Scale (Graph)"));

        setByLabel.setText("Set By:");

        VisibleRangeButtonGroup.add(by_valRB_val);
        by_valRB_val.setSelected(true);
        by_valRB_val.setText("Value");
        by_valRB_val.setIconTextGap(2);
        by_valRB_val.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                by_valRB_valActionPerformed(evt);
            }
        });

        VisibleRangeButtonGroup.add(by_percentileRB_val);
        by_percentileRB_val.setText("Percentile");
        by_percentileRB_val.setIconTextGap(2);
        by_percentileRB_val.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                by_percentileRB_valActionPerformed(evt);
            }
        });

        minValLabel.setText("Min:");

        minText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minTextActionPerformed(evt);
            }
        });

        maxText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxTextActionPerformed(evt);
            }
        });

        maxValLabel.setText("Max:");

        org.jdesktop.layout.GroupLayout RangePanelLayout = new org.jdesktop.layout.GroupLayout(RangePanel);
        RangePanel.setLayout(RangePanelLayout);
        RangePanelLayout.setHorizontalGroup(
            RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(RangePanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(RangePanelLayout.createSequentialGroup()
                        .add(setByLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(by_valRB_val)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(by_percentileRB_val)
                        .addContainerGap())
                    .add(RangePanelLayout.createSequentialGroup()
                        .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(RangePanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(rangeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(RangePanelLayout.createSequentialGroup()
                                .add(minValLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(minText)
                                .add(21, 21, 21)
                                .add(maxValLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(maxText)
                                .add(21, 21, 21)))
                        .add(10, 10, 10))))
        );
        RangePanelLayout.setVerticalGroup(
            RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, RangePanelLayout.createSequentialGroup()
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(setByLabel)
                    .add(by_valRB_val)
                    .add(by_percentileRB_val))
                .add(10, 10, 10)
                .add(rangeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(minValLabel)
                    .add(minText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxValLabel)
                    .add(maxText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        otherOptionsButton.setText("Other Options");
        otherOptionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherOptionsButtonActionPerformed(evt);
            }
        });

        heightLabel.setText("Height:");

        heightSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(heightLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(otherOptionsButton)
                    .add(heightSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(0, 0, Short.MAX_VALUE)
                .add(RangePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(otherOptionsButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(13, 13, 13)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(heightLabel)
                    .add(heightSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(RangePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void by_percentileRB_valActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_by_percentileRB_valActionPerformed
		if (is_listening) {
			switchView(true);
		}
	}//GEN-LAST:event_by_percentileRB_valActionPerformed

	private void by_valRB_valActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_by_valRB_valActionPerformed
		if (is_listening) {
			switchView(false);
		}
	}//GEN-LAST:event_by_valRB_valActionPerformed

	private void minTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minTextActionPerformed
		if (is_listening) {
			int min;
			int max;
			try {
				min = Integer.parseInt(minText.getText());
				max = Integer.parseInt(maxText.getText());
			} catch (Exception e) {
				return;
			}
			updateRange(min, max);
		}
	}//GEN-LAST:event_minTextActionPerformed

	private void maxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxTextActionPerformed
		if (is_listening) {
			int min;
			int max;
			try {
				min = Integer.parseInt(minText.getText());
				max = Integer.parseInt(maxText.getText());
			} catch (Exception e) {
				return;
			}
			updateRange(min, max);
		}
	}//GEN-LAST:event_maxTextActionPerformed

	private void heightSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heightSliderStateChanged
		if (is_listening) {
			setTrackHeight(heightSlider.getValue());
		}
	}//GEN-LAST:event_heightSliderStateChanged

	private void otherOptionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherOptionsButtonActionPerformed
		if (is_listening) {
			igbService.openPreferencesOtherPanel();
		}
	}//GEN-LAST:event_otherOptionsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup DisplayButtonGroup;
    private javax.swing.ButtonGroup DisplayModeButtonGroup;
    private javax.swing.JPanel RangePanel;
    private javax.swing.ButtonGroup VisibleRangeButtonGroup;
    private javax.swing.JRadioButton by_percentileRB_val;
    private javax.swing.JRadioButton by_valRB_val;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JSlider heightSlider;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JTextField maxText;
    private javax.swing.JLabel maxValLabel;
    private javax.swing.JTextField minText;
    private javax.swing.JLabel minValLabel;
    private javax.swing.JButton otherOptionsButton;
    private javax.swing.JSlider rangeSlider;
    private javax.swing.JLabel setByLabel;
    private javax.swing.ButtonGroup stylegroup;
    // End of variables declaration//GEN-END:variables

	private void updateRange(int min, int max) {
		if (min < rangeSlider.getMinimum()) {
			rangeSlider.setMinimum(min);
		}
		if (max > rangeSlider.getMaximum()) {
			rangeSlider.setMaximum(max);
		}
	}

	private void switchView(boolean b) {
		if (b) {
			org.jdesktop.layout.GroupLayout layout = (org.jdesktop.layout.GroupLayout) RangePanel.getLayout();
			layout.replace(minText, vis_bounds_setter.min_perT);
			layout.replace(maxText, vis_bounds_setter.max_perT);
			layout.replace(rangeSlider, vis_bounds_setter.PercentSlider);
			minText = vis_bounds_setter.min_perT;
			maxText = vis_bounds_setter.max_perT;
			rangeSlider = vis_bounds_setter.PercentSlider;
		} else {
			org.jdesktop.layout.GroupLayout layout = (org.jdesktop.layout.GroupLayout) RangePanel.getLayout();
			layout.replace(minText, vis_bounds_setter.min_valT);
			layout.replace(maxText, vis_bounds_setter.max_valT);
			layout.replace(rangeSlider, vis_bounds_setter.ValueSlider);
			minText = vis_bounds_setter.min_valT;
			maxText = vis_bounds_setter.max_valT;
			rangeSlider = vis_bounds_setter.ValueSlider;
		}
	}

	@Override
	public void symSelectionChanged(SymSelectionEvent evt) {
		graphGlyphs.clear();
		for (Glyph glyph : igbService.getSelectedTierGlyphs()) {
			if(((TierGlyph)glyph).getViewModeGlyph() instanceof MultiGraphGlyph){
				MultiGraphGlyph multiGraphGlyph = ((MultiGraphGlyph)((TierGlyph)glyph).getViewModeGlyph());
				for(GlyphI g : multiGraphGlyph.getChildren()){
					if(g instanceof AbstractGraphGlyph){
						graphGlyphs.add((AbstractGraphGlyph)g);
					}
				}
			}else if (((TierGlyph)glyph).getViewModeGlyph() instanceof AbstractGraphGlyph) {
				graphGlyphs.add((AbstractGraphGlyph)((TierGlyph)glyph).getViewModeGlyph());
			}
		}
		resetAll(graphGlyphs);
	}

	private void resetAll(List<AbstractGraphGlyph> graphGlyphs) {
		is_listening = false;
		boolean enabled = graphGlyphs.size() > 0;
		RangePanel.setEnabled(enabled);
		setByLabel.setEnabled(enabled);
	    by_percentileRB_val.setEnabled(enabled);
	    by_valRB_val.setEnabled(enabled);
	    maxText.setEnabled(enabled);
	    maxValLabel.setEnabled(enabled);
	    minText.setEnabled(enabled);
	    minValLabel.setEnabled(enabled);
	    rangeSlider.setEnabled(enabled);
	    heightSlider.setEnabled(enabled);
	    heightLabel.setEnabled(enabled);
	    vis_bounds_setter.setGraphs(graphGlyphs);
		if (graphGlyphs.size() == 1) {
			double the_height = graphGlyphs.get(0).getAnnotStyle().getHeight();
			heightSlider.setValue((int) the_height);
		}
		else {
			heightSlider.setValue(0);
		}
		is_listening = true;
	}

	@Override
	public void seqSelectionChanged(SeqSelectionEvent evt) {
		resetAll(new ArrayList<AbstractGraphGlyph>());
	}

	public boolean isTierGlyph(GlyphI glyph) {
		return glyph instanceof TierGlyph;
	}

	public void setTrackHeight(double height) {
		for (ViewModeGlyph gl : graphGlyphs) {
			Rectangle2D.Double cbox = gl.getCoordBox();
			gl.setCoords(cbox.x, cbox.y, cbox.width, height);

			// If a graph is joined with others in a combo tier, repack that tier.
			GlyphI parentgl = gl.getParent();
			if (isTierGlyph(parentgl)) {
				parentgl.pack(igbService.getView());
			}
		}
		igbService.packMap(false, true);
	}
}
