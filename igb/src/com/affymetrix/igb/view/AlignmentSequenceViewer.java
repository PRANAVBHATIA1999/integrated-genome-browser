/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.view;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.event.GenericActionDoneCallback;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithResidues;
import com.affymetrix.genometryImpl.util.SeqUtils;
import javax.swing.SwingWorker;

/**
 *
 * @author auser
 */
public class AlignmentSequenceViewer extends AbstractSequenceViewer{

	@Override
	public String getResidues(SeqSymmetry sym, BioSeq aseq) {
		return ((SymWithResidues)sym).getResidues();
	}

	@Override
	public void doBackground(final GenericActionDoneCallback doneback) {
		doneback.actionDone(null);
	}
		
	@Override
	protected void addIntron(SeqSymmetry residues_sym, BioSeq aseq) {
		//Do nothing
	}
}
