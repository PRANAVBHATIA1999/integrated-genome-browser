
package com.affymetrix.igb.viewmode;

import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.symmetry.GraphIntervalSym;
import com.affymetrix.igb.shared.AbstractGraphGlyph;
import com.affymetrix.igb.shared.SeqMapViewExtendedI;

/**
 *
 * @author hiralv
 */
public class ScoredContainerGlyphFactory extends AbstractScoredContainerGlyphFactory{
	final GraphGlyphFactory graphGlyphFactory;
	
	public ScoredContainerGlyphFactory(GraphGlyphFactory graphGlyphFactory){
		this.graphGlyphFactory = graphGlyphFactory;
	}
	
	@Override
	protected AbstractGraphGlyph createViewModeGlyph(GraphIntervalSym graf, GraphState graphState, SeqMapViewExtendedI smv) {
		return graphGlyphFactory.createViewModeGlyph(graf, graphState, smv);
	}
	
	@Override
	public String getName(){
		return (super.getName() + "_" + graphGlyphFactory.getName());
	}
}
