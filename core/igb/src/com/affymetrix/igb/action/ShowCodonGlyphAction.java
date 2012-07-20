package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import java.awt.event.ActionEvent;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.osgi.service.SeqMapViewI;
import com.affymetrix.igb.shared.CodonGlyph;

/**
 */
@SuppressWarnings("serial")
public class ShowCodonGlyphAction extends SeqMapViewActionA {
	
	private static final ShowCodonGlyphAction SHOW_CODON_GLYPH_ACTION_THREE_LETTER = new ShowCodonGlyphAction("3 letter", 3);
	private static final ShowCodonGlyphAction SHOW_CODON_GLYPH_ACTION_ONE_LETTER = new ShowCodonGlyphAction("1 letter", 1);
	private static final ShowCodonGlyphAction HIDE_CODON_GLYPH = new ShowCodonGlyphAction("hide", 0);
	
	final private int size;
	
	static{
		GenericActionHolder.getInstance().addGenericAction(SHOW_CODON_GLYPH_ACTION_THREE_LETTER);
	}
	
	public static ShowCodonGlyphAction getThreeLetterAction() {
		return SHOW_CODON_GLYPH_ACTION_THREE_LETTER;
	}

	public static ShowCodonGlyphAction getOneLetterAction() {
		return SHOW_CODON_GLYPH_ACTION_ONE_LETTER;
	}
	
	public static ShowCodonGlyphAction getHideCodonAction() {
		return HIDE_CODON_GLYPH;
	}
	
	public ShowCodonGlyphAction(String text, int size){
		super(text, null, null);
		this.size = size;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		PreferenceUtils.getTopNode().putInt(CodonGlyph.CODON_GLYPH_CODE_SIZE, size);
		redraw(getSeqMapView());
	}
	
	private void redraw(SeqMapViewI seqMapView) {
		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();
		seqMapView.setAnnotatedSeq(seq, true, true, true);
	}
}