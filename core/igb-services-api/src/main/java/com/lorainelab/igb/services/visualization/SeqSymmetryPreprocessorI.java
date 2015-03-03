package com.lorainelab.igb.services.visualization;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.lorainelab.igb.genoviz.extensions.SeqMapViewExtendedI;

/**
 *
 * @author dcnorris
 */
public interface SeqSymmetryPreprocessorI {

    public String getName();

    public FileTypeCategory getCategory();

    public void process(RootSeqSymmetry sym, ITrackStyleExtended style, SeqMapViewExtendedI gviewer, BioSeq seq);

}
