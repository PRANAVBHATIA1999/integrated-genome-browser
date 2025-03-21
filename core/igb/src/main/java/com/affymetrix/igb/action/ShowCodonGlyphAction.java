package com.affymetrix.igb.action;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.common.PreferenceUtils;
import com.affymetrix.igb.glyph.CodonGlyph;
import org.lorainelab.igb.genoviz.extensions.SeqMapViewI;
import java.awt.event.ActionEvent;

/**
 */
@SuppressWarnings("serial")
public class ShowCodonGlyphAction extends SeqMapViewActionA {

//	private static final ShowCodonGlyphAction SHOW_CODON_GLYPH_ACTION_THREE_LETTER = new ShowCodonGlyphAction("3 letter", 3);
//	private static final ShowCodonGlyphAction SHOW_CODON_GLYPH_ACTION_ONE_LETTER = new ShowCodonGlyphAction("1 letter", 1);
//	private static final ShowCodonGlyphAction HIDE_CODON_GLYPH = new ShowCodonGlyphAction("hide", 0);
//
//	public static ShowCodonGlyphAction getThreeLetterAction() {
//		return SHOW_CODON_GLYPH_ACTION_THREE_LETTER;
//	}
//
//	public static ShowCodonGlyphAction getOneLetterAction() {
//		return SHOW_CODON_GLYPH_ACTION_ONE_LETTER;
//	}
//
//	public static ShowCodonGlyphAction getHideCodonAction() {
//		return HIDE_CODON_GLYPH;
//	}
    final private int size;

    public ShowCodonGlyphAction(String text, int size) {
        super(text, null, null);
        this.size = size;
        if (size == PreferenceUtils.getIntParam(CodonGlyph.CODON_GLYPH_CODE_SIZE, CodonGlyph.default_codon_glyph_code_size)) {
            this.putValue(SELECTED_KEY, true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        PreferenceUtils.getTopNode().putInt(CodonGlyph.CODON_GLYPH_CODE_SIZE, size);
        redraw(getSeqMapView());
    }

    private void redraw(SeqMapViewI seqMapView) {
        BioSeq seq = GenometryModel.getInstance().getSelectedSeq().orElse(null);
        seqMapView.setAnnotatedSeq(seq, true, true, true);
    }
}
