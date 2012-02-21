package com.affymetrix.genometryImpl.parsers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.symloader.Wiggle;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;

public class GraphFileExporter implements FileExporterI {

	@Override
	public void exportFile(DataOutputStream dos, SeqSymmetry sym,
			BioSeq aseq) throws IOException {
		List<SeqSymmetry> syms = new ArrayList<SeqSymmetry>();
		syms.add(sym);
		Wiggle.writeAnnotations(syms,aseq,dos);
	}

	@Override
	public String getFileExtension() {
		return "wig";
	}
}
