
package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.view.load.GeneralLoadView;

import java.text.MessageFormat;
import java.awt.event.ActionEvent;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author hiralv
 */
public class LoadWholeSequenceAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private static final LoadWholeSequenceAction ACTION = new LoadWholeSequenceAction();

	public static LoadWholeSequenceAction getAction() {
		return ACTION;
	}

	private LoadWholeSequenceAction(){
		super();
	}

	public void actionPerformed(ActionEvent e) {
		GeneralLoadView.getLoadView().loadResidues(this);
	}

	@Override
	public String getText() {
		return MessageFormat.format(BUNDLE.getString("load"),BUNDLE.getString("allSequenceCap"));
	}
}
