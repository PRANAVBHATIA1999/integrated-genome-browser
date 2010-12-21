package com.affymetrix.igb.action;

import com.affymetrix.igb.IGB;
import com.affymetrix.genometryImpl.symloader.BAM;
import com.affymetrix.genometryImpl.SymWithProps;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.igb.view.SeqMapView;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author hiralv
 */
public class ViewReadAlignmentAction extends AbstractAction {
	private static final long serialVersionUID = 1l;

	private static final String RESTOREREAD = BUNDLE.getString("restoreAlignment");
	private static final String SHOWMISMATCH = BUNDLE.getString("showMismatch");

	private static final ViewReadAlignmentAction restoreRead = new ViewReadAlignmentAction(RESTOREREAD);
	private static final ViewReadAlignmentAction showMismatch = new ViewReadAlignmentAction(SHOWMISMATCH);

	private final List<SeqSymmetry> syms = new ArrayList<SeqSymmetry>();

	SeqMapView gViewer = IGB.getSingleton().getMapView();

	private ViewReadAlignmentAction(String str){
		super(str);
	}

	public static ViewReadAlignmentAction getReadRestoreAction(List<SeqSymmetry> syms){
		return getAction(restoreRead, syms);
	}

	public static ViewReadAlignmentAction getMismatchAligmentAction(List<SeqSymmetry> syms){
		return getAction(showMismatch, syms);
	}

	private static ViewReadAlignmentAction getAction(ViewReadAlignmentAction action, List<SeqSymmetry> syms){
		action.syms.clear();
		action.syms.addAll(syms);
		return action;
	}

	public void actionPerformed(ActionEvent e) {

		boolean set = true;
		
		if(RESTOREREAD.equals(e.getActionCommand())){
			set = false;
		}

		for (SeqSymmetry sym : syms) {
			if (sym instanceof SymWithProps) {
				SymWithProps swp = (SymWithProps) sym;
				if (swp.getProperty(BAM.SHOWMASK) != null) {
					swp.setProperty(BAM.SHOWMASK, set);
				}
			}
		}

		gViewer.getSeqMap().repaint();
	}

}
