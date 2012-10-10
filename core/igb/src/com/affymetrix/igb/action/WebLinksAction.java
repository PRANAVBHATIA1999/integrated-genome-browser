package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;

import com.affymetrix.igb.view.WebLinksViewGUI;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.event.*;
import javax.swing.*;

/**
 *  A panel for viewing and editing weblinks.
 */
public final class WebLinksAction extends GenericAction {
  private static final long serialVersionUID = 1L;
	private static final WebLinksAction ACTION = new WebLinksAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static WebLinksAction getAction() {
		return ACTION;
	}

	private WebLinksAction() {
		super(BUNDLE.getString("configureWebLinks"), "Manage Web Links", 
				"16x16/categories/applications-internet.png",
				//"22x22/categories/applications-internet.png", 
				null,
				KeyEvent.VK_W, null, true);
	    putValue(Action.SHORT_DESCRIPTION, "Manage Web Links");
	}

	@Override
    public void actionPerformed(ActionEvent evt) {
    	super.actionPerformed(evt);
		WebLinksViewGUI.getSingleton().displayPanel();
    }
}
