package com.affymetrix.igb.tiers;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.event.NeoMouseEvent;
import com.affymetrix.igb.shared.GraphGlyph;
import com.affymetrix.igb.shared.TransformTierGlyph;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.SeqMapView.MapMode;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hiralv
 */
final public class MouseShortCut implements MouseListener{
	final SeqMapViewPopup popup;
	final SeqMapView smv;
	
	public MouseShortCut(SeqMapViewPopup popup){
		this.popup = popup;
		this.smv = popup.getSeqMapView();
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		
	}

	public void mouseReleased(MouseEvent e) {
		processDoubleClick(e);
	}

	private void processDoubleClick(MouseEvent e) {

		if(e.getClickCount() != 2)
			return;

		//AffyLabelledTierMap
		if (e.getSource() instanceof AffyLabelledTierMap) {
			if (e.isAltDown()) {
				//if alt is pressed.
				
				return;
			}

			if (e.isShiftDown()) {
				//Center hairline if shift is pressed.
				smv.centerAtHairline();
			}

			List<GlyphI> glyphs = smv.getSeqMap().getSelected();
			
			if(smv.getMapMode() == MapMode.MapScrollMode && e instanceof NeoMouseEvent){
				NeoMouseEvent nevt = (NeoMouseEvent)e;
				glyphs = new ArrayList<GlyphI>();
				Point2D.Double zoom_point = new Point2D.Double(nevt.getCoordX(), nevt.getCoordY());
				
				GlyphI topgl = null;
				if (!nevt.getItems().isEmpty()) {
					topgl = nevt.getItems().get(nevt.getItems().size() - 1);
					topgl = smv.getSeqMap().zoomCorrectedGlyphChoice(topgl, zoom_point);
					glyphs.add(topgl);
				}
				
			}

			if(glyphs == null || glyphs.isEmpty() || glyphs.get(0) instanceof GraphGlyph)
				return;
			
			//Zoom to glyphs.
			smv.zoomToGlyphs(glyphs);
			return;
		}
		
		//AffyTieredMap
		if(e.getSource() instanceof AffyTieredMap){

			List<TierLabelGlyph> tier_Labels = smv.getTierManager().getSelectedTierLabels();
			if(tier_Labels.size() == 1){
				if(tier_Labels.get(0).getInfo() instanceof TransformTierGlyph){
					popup.showAllTiers();
					return;
				}
			}
			
			if(e.isAltDown()){
				//alt is pressed.
				return;
			}

			if(e.isShiftDown()){
				//Hide tiers if shift is pressed.
				popup.hideTiers(tier_Labels);
				return;
			}

			//Collapse or Expand
			smv.getTierManager().toggleTierCollapsed(tier_Labels);
			popup.refreshMap(true, true);
			return;
		}

	}

}
