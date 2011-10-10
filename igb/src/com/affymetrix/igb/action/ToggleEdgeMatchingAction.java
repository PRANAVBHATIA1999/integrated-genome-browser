package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.IGB;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import static com.affymetrix.igb.IGBConstants.BUNDLE;
/**
 *
 * @author hiralv
 */
public class ToggleEdgeMatchingAction extends GenericAction {
	private static final long serialVersionUID = 1;
	private static final ToggleEdgeMatchingAction ACTION = new ToggleEdgeMatchingAction();
	private SeqMapView map_view = IGB.getSingleton().getMapView();

	private ToggleEdgeMatchingAction(){
		super();
		this.putValue(SELECTED_KEY, map_view.getEdgeMatching());
	}

	public static ToggleEdgeMatchingAction getAction(){
		return ACTION;
	}
	
	public void actionPerformed(ActionEvent e) {
		map_view.setEdgeMatching(!map_view.getEdgeMatching());
		ACTION.putValue(AbstractAction.SELECTED_KEY, map_view.getEdgeMatching());
	}

	@Override
	public String getText() {
		return BUNDLE.getString("toggleEdgeMatching");
	}

	@Override
	public int getMnemonic() {
		return KeyEvent.VK_M;
	}
}
