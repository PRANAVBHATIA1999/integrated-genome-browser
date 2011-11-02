package com.affymetrix.genometryImpl.parsers.graph;

import java.util.ArrayList;
import java.util.List;

import com.affymetrix.genometryImpl.symmetry.GraphSym;

public class GraphParserUtil {
	private static final GraphParserUtil instance = new GraphParserUtil();
	private GraphParserUtil() { super(); }
	public static GraphParserUtil getInstance() {
		return instance;
	}

	public List<GraphSym> wrapInList(GraphSym gsym) {
		List<GraphSym> grafs = null;
		if (gsym != null) {
			grafs = new ArrayList<GraphSym>();
			grafs.add(gsym);
		}
		return grafs;
	}
}
