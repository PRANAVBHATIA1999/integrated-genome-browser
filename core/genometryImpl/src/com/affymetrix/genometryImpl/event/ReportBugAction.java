package com.affymetrix.genometryImpl.event;

import com.affymetrix.genometryImpl.util.GeneralUtils;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 *
 * @author sgblanch
 * @version $Id: ReportBugAction.java 9589 2011-12-20 15:54:10Z lfrohman $
 */
public class ReportBugAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private static final ReportBugAction ACTION = new ReportBugAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ReportBugAction getAction() {
		return ACTION;
	}

	private ReportBugAction() {
		super("Report a bug", null,
				"16x16/actions/report bug.png",
				"22x22/actions/report bug.png",
				KeyEvent.VK_R, null, true);
		this.ordinal = 130;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		GeneralUtils.browse("http://sourceforge.net/p/genoviz/bugs/");
	}
}
