package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.osgi.service.IGBService;
import static com.affymetrix.igb.shared.Selections.*;


public class YScaleAxis extends javax.swing.JPanel implements Selections.RefreshSelectionListener {
	private static final long serialVersionUID = 1L;
	private final IGBService igbService;
	private GraphVisibleBoundsSetter vis_bounds_setter;
	private boolean is_listening = true; // used to turn on and off listening to GUI events
	
	/**
	 * Creates new form YScaleAxis
	 */
	public YScaleAxis(IGBService igbService) {
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

        HeightPanel = new javax.swing.JPanel();
        heightSlider = new javax.swing.JSlider(javax.swing.JSlider.HORIZONTAL, 10, 500, 50);
        RangePanel = new javax.swing.JPanel();
        setByLabel = new javax.swing.JLabel();
        by_valRB_val = vis_bounds_setter.by_valRB;
        by_percentileRB_val = vis_bounds_setter.by_percentileRB;
        rangeSlider = vis_bounds_setter.ValueSlider;
        minValLabel = new javax.swing.JLabel();
        minText = vis_bounds_setter.min_valT;
        maxText = vis_bounds_setter.max_valT;
        maxValLabel = new javax.swing.JLabel();

        HeightPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Height"));

        heightSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightSliderStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout HeightPanelLayout = new org.jdesktop.layout.GroupLayout(HeightPanel);
        HeightPanel.setLayout(HeightPanelLayout);
        HeightPanelLayout.setHorizontalGroup(
            HeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(heightSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        HeightPanelLayout.setVerticalGroup(
            HeightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(heightSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        RangePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Y Axis Scale (Graph)"));
        RangePanel.setPreferredSize(new java.awt.Dimension(231, 133));

        setByLabel.setText("Set By:");

        by_valRB_val.setSelected(true);
        by_valRB_val.setText("Value");
        by_valRB_val.setIconTextGap(2);
        by_valRB_val.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                by_valRB_valActionPerformed(evt);
            }
        });

        by_percentileRB_val.setText("Percentile");
        by_percentileRB_val.setIconTextGap(2);
        by_percentileRB_val.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                by_percentileRB_valActionPerformed(evt);
            }
        });

        minValLabel.setText("Min:");

        minText.setColumns(6);
        minText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minTextActionPerformed(evt);
            }
        });

        maxText.setColumns(6);
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
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(by_valRB_val)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(by_percentileRB_val)
                        .addContainerGap())
                    .add(RangePanelLayout.createSequentialGroup()
                        .add(minValLabel)
                        .add(5, 5, 5)
                        .add(minText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(maxValLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(maxText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .add(34, 34, 34))))
            .add(RangePanelLayout.createSequentialGroup()
                .add(rangeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 205, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
        );
        RangePanelLayout.setVerticalGroup(
            RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, RangePanelLayout.createSequentialGroup()
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(setByLabel)
                    .add(by_valRB_val)
                    .add(by_percentileRB_val))
                .add(16, 16, 16)
                .add(rangeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(maxText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(maxValLabel))
                    .add(RangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(minText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(minValLabel))))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(HeightPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(RangePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(HeightPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(RangePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void by_valRB_valActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_by_valRB_valActionPerformed
        if (is_listening) {
            switchView(false);
        }
    }//GEN-LAST:event_by_valRB_valActionPerformed

    private void by_percentileRB_valActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_by_percentileRB_valActionPerformed
        if (is_listening) {
            switchView(true);
        }
    }//GEN-LAST:event_by_percentileRB_valActionPerformed

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

	private boolean isTierGlyph(GlyphI glyph) {
		return glyph instanceof TierGlyph;
	}

	public void setTrackHeight(double height) {
		for (GraphGlyph gl : graphGlyphs) {
			java.awt.geom.Rectangle2D.Double cbox = gl.getCoordBox();
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
	
	private void updateRange(int min, int max) {
		if (min < rangeSlider.getMinimum()) {
			rangeSlider.setMinimum(min);
		}
		if (max > rangeSlider.getMaximum()) {
			rangeSlider.setMaximum(max);
		}
	}

	
	@Override
	public void selectionRefreshed() {
		resetAll();
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel HeightPanel;
    private javax.swing.JPanel RangePanel;
    private javax.swing.JRadioButton by_percentileRB_val;
    private javax.swing.JRadioButton by_valRB_val;
    private javax.swing.JSlider heightSlider;
    private javax.swing.JTextField maxText;
    private javax.swing.JLabel maxValLabel;
    private javax.swing.JTextField minText;
    private javax.swing.JLabel minValLabel;
    private javax.swing.JSlider rangeSlider;
    private javax.swing.JLabel setByLabel;
    // End of variables declaration//GEN-END:variables

}
