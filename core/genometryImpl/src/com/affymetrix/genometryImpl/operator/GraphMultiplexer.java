/*  Licensed under the Common Public License, Version 1.0 (the "License").
 *  A copy of the license must be included
 *  with any distribution of this source code.
 *  Distributions from Genentech, Inc. place this in the IGB_LICENSE.html file.
 * 
 *  The license is also available at
 *  http://www.opensource.org/licenses/CPL
 */
package com.affymetrix.genometryImpl.operator;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combines multiple graphs into one.
 * "Multiplexer" is used in the name
 * because one should be able to invert this operation.
 * That is,
 * one should be able to take the result of this operation and demultiplex it
 * into the original graphs.
 */
public class GraphMultiplexer implements Operator {

	private static int serialNumber = 0;
	private static final int MINIMUMINPUTGRAPHS = 2;
	private static final int MAXIMUMINPUTGRAPHS = 9;

	private Map<String, Class<?>> properties = new HashMap<String, Class<?>>();
	private String name;

	public GraphMultiplexer() {
		this.name = this.getClass().getName();
		if (0 < serialNumber) {
			this.name += Integer.toString(serialNumber);
		}
		serialNumber += 1;
		this.name = "multiplexer"; // for now.
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDisplay() {
		String display/* = GenometryConstants.BUNDLE.getString("operator_" + getName())*/;
		display = "Multiplex";
		return display;
	}

	@Override
	public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
		if (symList.size() < MINIMUMINPUTGRAPHS) {
			throw new IllegalArgumentException("Must give me at least " + MINIMUMINPUTGRAPHS + " graphs.");
		}
		if (MAXIMUMINPUTGRAPHS < symList.size()) {
			throw new IllegalArgumentException("Must give no more than " + MAXIMUMINPUTGRAPHS + " graphs.");
		}
		SeqSymmetry firstOne = symList.get(0);
		if (!(firstOne instanceof GraphSym)) {
			throw new IllegalArgumentException("Can only deal with GraphSyms.");
		}
		GraphSym paradigm = (GraphSym) firstOne;
		GraphSym newParent;
		// As a first pass let's assume that all the input graphs have the same domain.
		int[] x = new int[10];
		float[] y = new float[10];
		newParent = new GraphSym(x, y, "newguy", aseq); // This aint right?
		for (SeqSymmetry s: symList) {
			newParent.addChild(s);
		}
		//return newParent;
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Don't think this makes sense for less than two graphs.
	 * @return 2
	 */
	@Override
	public int getOperandCountMin(FileTypeCategory category) {
		return category == FileTypeCategory.Graph ? MINIMUMINPUTGRAPHS : 0;
	}

	/**
	 * Restrict multiplexing to a relatively small number of tracks.
	 * @return 9 for the moment.
	 */
	@Override
	public int getOperandCountMax(FileTypeCategory category) {
		return category == FileTypeCategory.Graph ? MAXIMUMINPUTGRAPHS : 0;
	}

	@Override
	public Map<String, Class<?>> getParameters() {
		return this.properties;
	}

	/**
	 * No need for this yet.
	 * @param parms doesn't matter.
	 * @return false
	 */
	@Override
	public boolean setParameters(Map<String, Object> parms) {
		return false;
	}

	/**
	 * Do we need this?
	 * @return false
	 */
	@Override
	public boolean supportsTwoTrack() {
		return false;
	}

	@Override
	public FileTypeCategory getOutputCategory() {
		return FileTypeCategory.Graph;
	}

	public static void main(String[] argv) {
		System.out.println("Hi from the multiplexer.");
		Operator o = new GraphMultiplexer();
		System.out.println(o.getName());
		System.out.println(o.getDisplay());
	}

}
