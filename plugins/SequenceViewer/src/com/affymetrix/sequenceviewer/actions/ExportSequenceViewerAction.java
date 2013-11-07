package com.affymetrix.sequenceviewer.actions;


import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.igb.shared.ExportDialog;
import com.affymetrix.sequenceviewer.AbstractSequenceViewer;
import java.awt.Adjustable;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

/**
 *
 * @author hiralv
 * Modified by nick
 */
public class ExportSequenceViewerAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private final Component comp;
	private final Adjustable scroller;
	
	public ExportSequenceViewerAction(Component comp, Adjustable scroller) {
		super(AbstractSequenceViewer.BUNDLE.getString("exportImage"), null, "22x22/actions/Sequence_Viewer_export.png");
		this.comp = comp;
		this.scroller = scroller;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (comp == null || scroller == null) {
			return;
		}

		try {
			ExportDialog.getSingleton().setComponent(comp);
			ExportDialog.getSingleton().initImageInfo();
			ExportDialog.getSingleton().initSeqViewListener(comp, scroller);
			ExportDialog.getSingleton().display(true);
		} catch (Exception ex) {
			ErrorHandler.errorPanel("Problem during output.", ex, Level.SEVERE);
		}
	}
	
}
