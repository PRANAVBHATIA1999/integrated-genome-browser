package com.gene.bigwighandler;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.parsers.FileTypeHandler;
import com.affymetrix.genometryImpl.parsers.IndexWriter;
import com.affymetrix.genometryImpl.parsers.Parser;
import com.affymetrix.genometryImpl.symloader.SymLoader;

public class BigWigHandler implements FileTypeHandler {
	private static final String[] EXTENSIONS = new String[]{"bw", "bigWig", "bigwig"};

	public BigWigHandler() {
		super();
	}

	@Override
	public String getName() {
		return "Graph";
	}

	@Override
	public String[] getExtensions() {
		return EXTENSIONS;
	}

	public static List<String> getFormatPrefList() {
		return Arrays.asList(EXTENSIONS);
	}

	@Override
	public SymLoader createSymLoader(URI uri, String featureName,
			AnnotatedSeqGroup group) {
		SymLoader symLoader = new BigWigSymLoader(uri, featureName, group);
		((BigWigSymLoader)symLoader).init();
		return symLoader;
	}

	@Override
	public Parser getParser() {
		return null;
	}

	@Override
	public IndexWriter getIndexWriter(String stream_name) {
		return null;
	}

	@Override
	public FileTypeCategory getFileTypeCategory() {
		return FileTypeCategory.Graph;
	}
}
