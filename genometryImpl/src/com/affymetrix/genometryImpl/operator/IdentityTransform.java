/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometryImpl.operator;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import java.util.List;
import java.util.Map;

/**
 *
 * @author auser
 */
public class IdentityTransform implements Operator{

	@Override
	public String getName() {
		return "Copy";
	}
	
	@Override
	public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
		if (symList.size() != 1 || !(symList.get(0) instanceof GraphSym)) {
			return null;
		}
		GraphSym sourceSym = (GraphSym)symList.get(0);
		GraphSym graphSym;
		int[] x = new int[sourceSym.getGraphXCoords().length];
		System.arraycopy(sourceSym.getGraphXCoords(), 0, x, 0, sourceSym.getGraphXCoords().length);
		float[] sourceY = sourceSym.getGraphYCoords();
		float[] y = new float[sourceY.length];
		for (int i = 0; i < sourceY.length; i++) {
			y[i] = transform(sourceY[i]);
		}
		System.arraycopy(sourceSym.getGraphYCoords(), 0, y, 0, sourceSym.getGraphYCoords().length);
		String id = sourceSym.getID();
		BioSeq seq = sourceSym.getGraphSeq();
		if (sourceSym.hasWidth()) {
			int[] w = new int[sourceSym.getGraphWidthCoords().length];
			System.arraycopy(sourceSym.getGraphWidthCoords(), 0, w, 0, sourceSym.getGraphWidthCoords().length);
			graphSym = new GraphSym(x, w, y, id, seq);
		}
		else {
			graphSym = new GraphSym(x, y, id, seq);
		}
		return graphSym;
	}
	
	@Override
	public int getOperandCountMin(FileTypeCategory category) {
		return category == FileTypeCategory.Graph ? 1 : 0;
	}
	
	@Override
	public int getOperandCountMax(FileTypeCategory category) {
		return category == FileTypeCategory.Graph ? 1 : 0;
	}
	
	@Override
	public Map<String, Class<?>> getParameters() {
		return null;
	}
	
	@Override
	public boolean setParameters(Map<String, Object> parms) {
		return false;
	}
	
	@Override
	public boolean supportsTwoTrack() {
		return false;
	}
	
	@Override
	public FileTypeCategory getOutputCategory() {
		return FileTypeCategory.Graph;
	}
	
	public float transform(float x) { 
		return x; 
	}
	
	public String getDisplay() { 
		return "Copy"; 
	}
	
	public boolean setParameter(String s) { 
		return true; 
	}
	
	public String getParamPrompt() { 
		return null; 
	}
	
}
