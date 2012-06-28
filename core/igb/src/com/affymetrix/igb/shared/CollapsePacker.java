package com.affymetrix.igb.shared;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cern.colt.list.DoubleArrayList;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.LinearTransform;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.widget.tieredmap.CollapsedTierPacker;

public final class CollapsePacker extends CollapsedTierPacker {

	@Override
	public Rectangle pack(GlyphI parent, ViewI view) {
		List<GlyphI> children = parent.getChildren();
		
		if (children != null) {
			maxHeight = getMaxHeightAndSkipDraw(parent);
		}

		adjustHeight(parent);
		moveAllChildren(parent);

		// trying to transform according to tier's internal transform
		//   (since packing is done based on tier's children)
		if (parent instanceof TransformTierGlyph) {
			Rectangle2D.Double newbox = new Rectangle2D.Double();
			newbox.setRect(parent.getCoordBox());
			TransformTierGlyph transtier = (TransformTierGlyph) parent;
			LinearTransform tier_transform = transtier.getTransform();
			LinearTransform.transform(tier_transform, newbox, newbox);
			parent.setCoords(newbox.x, newbox.y, newbox.width, newbox.height);
		}

		return null;
	}
	
	public static double getMaxHeightAndSkipDraw(GlyphI parent){
		double maxHeight = 0;
		Rectangle2D.Double cbox;
		DoubleArrayList slot_maxes = new DoubleArrayList(1000);
		double prev_min_xmax = Double.POSITIVE_INFINITY;
		int min_xmax_slot_index = 0;	//index of slot with max of prev_min_xmax
		int prev_slot_index = 0;
		boolean skipDraw = false;
		
		List<GlyphI> children = new CopyOnWriteArrayList<GlyphI>(parent.getChildren());
		for (GlyphI child : children) {
			maxHeight = Math.max(maxHeight, child.getCoordBox().height);
			
			child.setOverlapped(true);
			child.setSkipDraw(false);
			cbox = child.getCoordBox();
			double child_min = cbox.x;
			double child_max = child_min + cbox.width;
			boolean child_placed = false;
			int start_slot_index = 0;
			if (prev_min_xmax >= child_min) {
				// no point in checking slots prior to and including prev_slot_index, so
				//  modify start_slot_index to be prev_slot_index++;
				start_slot_index = prev_slot_index + 1;
			}
			int slot_count = slot_maxes.size();
			for (int slot_index = start_slot_index; slot_index < slot_count; slot_index++) {
				double slot_max = slot_maxes.get(slot_index);
				if (slot_max < prev_min_xmax) {
					min_xmax_slot_index = slot_index;
					prev_min_xmax = slot_max;
				}
				if (slot_max < child_min) {
					child_placed = true;
					slot_maxes.set(slot_index, child_max);
					prev_slot_index = slot_index;
					if (slot_index == min_xmax_slot_index) {
						prev_slot_index = 0;
						min_xmax_slot_index = 0;
						prev_min_xmax = slot_maxes.get(0);
						skipDraw = false;
					} else if (child_max < prev_min_xmax) {
						prev_min_xmax = child_max;
						min_xmax_slot_index = slot_index;
					}
					break;
				}
			}
			if (!child_placed) {
				if (slot_maxes.size() >= 1) {
					int slot_index = slot_maxes.size() - 1;
					prev_slot_index = slot_index;
					child.setSkipDraw(skipDraw);
					
					skipDraw = true;
					//chr1 : 16,091,572 - 16,339,646
				} else {
					slot_maxes.add(child_max);
					int slot_index = slot_maxes.size() - 1;
					if (child_max < prev_min_xmax) {
						min_xmax_slot_index = slot_index;
						prev_min_xmax = child_max;
					}
					prev_slot_index = slot_index;
				}
			}
		}
		return maxHeight;
	}
}


