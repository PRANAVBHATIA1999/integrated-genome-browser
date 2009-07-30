package com.affymetrix.genometryImpl.comparator;

import java.util.*;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;

public final class GenomeVersionDateComparator implements Comparator<AnnotatedSeqGroup> {
	private static Comparator<String> stringComp = new StringVersionDateComparator();
	public int compare(AnnotatedSeqGroup group1, AnnotatedSeqGroup group2) {
		String name1 = group1.getID();
		String name2 = group2.getID();
		return stringComp.compare(name1, name2);
	}
}
