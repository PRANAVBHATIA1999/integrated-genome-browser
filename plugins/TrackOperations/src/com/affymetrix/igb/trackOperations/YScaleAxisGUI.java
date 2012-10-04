package com.affymetrix.igb.trackOperations;

import java.awt.geom.Rectangle2D;

import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.GraphGlyph;
import com.affymetrix.igb.shared.GraphVisibleBoundsSetter;
import com.affymetrix.igb.shared.Selections;
import com.affymetrix.igb.shared.TierGlyph;
import static com.affymetrix.igb.shared.Selections.*;


public class YScaleAxisGUI extends javax.swing.JPanel implements Selections.RefreshSelectionListener {

	private static final long serialVersionUID = 1L;
	private final IGBService igbService;
	private GraphVisibleBoundsSetter vis_bounds_setter;
	private boolean is_listening = true; // used to turn on and off listening to GUI events

	/**
	 * Creates new form YScaleAxisGUI
	 */
	public YScaleAxisGUI(IGBService igbService) {
		super();
		this.igbService = igbService;
		vis_bounds_setter = new GraphVisibleBoundsSetter(igbService.getSeqMap());
		initComponents();
		resetAll();
		Selections.addRefreshSelectionListener(this);
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
        heightPanel = new javax.swing.JPanel();
        heightSlider = new javax.swing.JSlider(javax.swing.JSlider.HORIZONTAL, 10, 500, 50);

        jMenu1.setText("jMenu1");

        RangePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Y Axis Scale"));
        RangePanel.setPreferredSize(new java.awt.Dimension(231, 133));

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

        minText.setColumns(5);
        minText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minTextActionPerformed(evt);
            }
        });

        maxText.setColumns(5);
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
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, RangePanelLayout.createSequentialGroup()
                        .add(minValLabel)
                        .add(0, 0, 0)
                        .add(minText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(40, 40, 40)
                        .add(maxValLabel)
                        .add(0, 0, 0)
                        .add(maxText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0))
                    .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(RangePanelLayout.createSequentialGroup()
                            .add(setByLabel)
                            .add(5, 5, 5)
                            .add(by_valRB_val)
                            .add(5, 5, 5)
                            .add(by_percentileRB_val))
                        .add(rangeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 0, 0))
        );
        RangePanelLayout.setVerticalGroup(
            RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, RangePanelLayout.createSequentialGroup()
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(setByLabel)
                    .add(by_valRB_val)
                    .add(by_percentileRB_val))
                .add(15, 15, 15)
                .add(rangeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(maxText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(maxValLabel))
                    .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(minText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(minValLabel)))
                .add(0, 0, 0))
        );

        heightPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Height"));

        heightSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout heightPanelLayout = new org.jdesktop.layout.GroupLayout(heightPanel);
        heightPanel.setLayout(heightPanelLayout);
        heightPanelLayout.setHorizontalGroup(
            heightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(heightPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(heightSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 212, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
        heightPanelLayout.setVerticalGroup(
            heightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(heightPanelLayout.createSequentialGroup()
                .add(heightSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(heightPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(RangePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 222, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(heightPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(RangePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                .add(0, 0, 0))
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup DisplayButtonGroup;
    private javax.swing.ButtonGroup DisplayModeButtonGroup;
    private javax.swing.JPanel RangePanel;
    private javax.swing.ButtonGroup VisibleRangeButtonGroup;
    private javax.swing.JRadioButton by_percentileRB_val;
    private javax.swing.JRadioButton by_valRB_val;
    private javax.swing.JPanel heightPanel;
    private javax.swing.JSlider heightSlider;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JTextField maxText;
    private javax.swing.JLabel maxValLabel;
    private javax.swing.JTextField minText;
    private javax.swing.JLabel minValLabel;
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
	public void selectionRefreshed() {
		resetAll();
	}
	
	private int getStretchableCount() {
		int stretchableCount = 0;
		for (Glyph glyph : igbService.getVisibleTierGlyphs()) {
			if (!((TierGlyph)glyph).getAnnotStyle().getFloatTier()) {
				FileTypeCategory category = ((TierGlyph)glyph).getFileTypeCategory();
				if (category == null) {
					RootSeqSymmetry rootSeqSymmetry = (RootSeqSymmetry)glyph.getInfo();
					if (rootSeqSymmetry != null) {
						category = rootSeqSymmetry.getCategory();
					}
				}
				if (category != null && category != FileTypeCategory.Sequence) {
					stretchableCount++;
				}
			}
		}
		return stretchableCount;
	}

	private boolean isAllFloat() {
		for (ITrackStyleExtended style : allStyles) {
			if (!style.getFloatTier()) {
				return false;
			}
		}
		return true;
	}

	private void resetAll() {
		is_listening = false;
		boolean enabled = graphGlyphs.size() > 0 && graphGlyphs.size() == allStyles.size();
		boolean heightEnabled = enabled && (getStretchableCount() > 1 || isAllFloat());
		setByLabel.setEnabled(enabled);
	    by_percentileRB_val.setEnabled(enabled);
	    by_valRB_val.setEnabled(enabled);
	    maxText.setEnabled(enabled);
	    maxValLabel.setEnabled(enabled);
	    minText.setEnabled(enabled);
	    minValLabel.setEnabled(enabled);
	    rangeSlider.setEnabled(enabled);
	    heightSlider.setEnabled(heightEnabled);
	    vis_bounds_setter.setGraphs(enabled ? graphGlyphs : null);
		if (enabled && graphGlyphs.size() == 1) {
			double the_height = graphGlyphs.get(0).getGraphState().getTierStyle().getHeight();
			heightSlider.setValue((int) the_height);
		}
		else {
			heightSlider.setValue(0);
		}
		is_listening = true;
	}

	public boolean isTierGlyph(GlyphI glyph) {
		return glyph instanceof TierGlyph;
	}

	public void setTrackHeight(double height) {
		for (GraphGlyph gl : graphGlyphs) {
			Rectangle2D.Double cbox = gl.getCoordBox();
			gl.setCoords(cbox.x, cbox.y, cbox.width, height);

			// If a graph is joined with others in a combo tier, repack that tier.
			GlyphI parentgl = gl.getParent();
			if (isTierGlyph(parentgl)) {
				parentgl.pack(igbService.getView());
			}
			if (gl.getGraphState().getTierStyle().getFloatTier()) {
				gl.getGraphState().getTierStyle().setHeight(height);
			}
		}
		igbService.packMap(false, true);
	}
}
