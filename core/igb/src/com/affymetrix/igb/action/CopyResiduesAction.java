package com.affymetrix.igb.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.SeqUtils;

import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBServiceImpl;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 * Copies residues of selection to clipboard If a region of sequence is
 * selected, should copy genomic residues If an annotation is selected,
 * should the residues of the leaf nodes of the annotation, spliced together
 * @author sgblanch
 * @version $Id: CopyResiduesAction.java 11358 2012-05-02 13:28:22Z anuj4159 $
 */
@SuppressWarnings("serial")
public class CopyResiduesAction extends GenericAction {
	private static final CopyResiduesAction ACTION = new CopyResiduesAction(BUNDLE.getString("copySelectedResiduesToClipboard"));
	private static final CopyResiduesAction ACTION_SHORT = new CopyResiduesAction("Copy");

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
		//GenericActionHolder.getInstance().addGenericAction(ACTION_SHORT);
	}
	public static CopyResiduesAction getAction() {
		return ACTION;
	}

	public static CopyResiduesAction getActionShort() {
		return ACTION_SHORT;
	}

	private CopyResiduesAction(String text) {
		super(text, null, "16x16/actions/edit-copy.png", "22x22/actions/edit-copy.png", KeyEvent.VK_C);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		copySelectedResidues(false);
	}
	
	private void copySelectedResidues(boolean allResidues){
		boolean success = false;
		SeqSymmetry residues_sym = null;
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		String from = "";

		if (IGB.getSingleton().getMapView().getSeqSymmetry() != null) {
			residues_sym = IGB.getSingleton().getMapView().getSeqSymmetry();
			from = " from selected region";
		} else {
			List<SeqSymmetry> syms = IGB.getSingleton().getMapView().getSelectedSyms();
			if (syms.size() == 1) {
				residues_sym = syms.get(0);
				from = " from selected item";
			}

		}

		if (residues_sym == null) {
			ErrorHandler.errorPanel("Can't copy to clipboard",
					"No selection or multiple selections.  Select a single item before copying its residues to clipboard.", Level.WARNING);
		} else {
			String residues = null;
			if (allResidues) {
				residues = SeqUtils.selectedAllResidues(residues_sym, IGB.getSingleton().getMapView().getAnnotatedSeq());
			} else {
				residues = SeqUtils.determineSelectedResidues(residues_sym, IGB.getSingleton().getMapView().getAnnotatedSeq());
			}
			
			if (residues != null) {
				if (SeqUtils.areResiduesComplete(residues)) {
					/*
					 * WARNING This bit of code *looks* unnecessary, but is
					 * needed because StringSelection is buggy (at least with
					 * jdk1.3): making a StringSelection with a String that has
					 * been derived from another String via substring() ends up
					 * starting from the beginning of the _original_ String
					 * (maybe because of the way derived and original Strings do
					 * char-array sharing) THEREFORE, need to make a String with
					 * its _own_ internal char array that starts with the 0th
					 * character...
					 */
					StringBuffer hackbuf = new StringBuffer(residues);
					String hackstr = new String(hackbuf);
					StringSelection data = new StringSelection(hackstr);
					clipboard.setContents(data, null);
					String message = "Copied " + hackstr.length() + " residues" + from + " to clipboard";
					IGBServiceImpl.getInstance().setStatus(message);
					success = true;
				} else {
					ErrorHandler.errorPanel("Missing Sequence Residues",
							"Don't have all the needed residues, can't copy to clipboard.\n"
							+ "Please load sequence residues for this region.", Level.WARNING);
				}
			}
		}
		if (!success) {
			// null out clipboard if unsuccessful (otherwise might get fooled into thinking
			//   the copy operation worked...)
			// GAH 12-16-2003
			// for some reason, can't null out clipboard with [null] or [new StringSelection("")],
			//   have to put in at least one character -- just putting in a space for now
			clipboard.setContents(new StringSelection(" "), null);
		}
	}
}
