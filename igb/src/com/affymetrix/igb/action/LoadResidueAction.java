
package com.affymetrix.igb.action;

import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.ThreadUtils;

import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.load.GeneralLoadView;

/**
 *
 * @author hiralv
 */
public class LoadResidueAction extends GenericAction {
	private static final long serialVersionUID = 1L;
	final SeqSpan span;
	final boolean tryFull;
	
	public LoadResidueAction(final SeqSpan span, boolean tryFull){
		this.span = span;
		this.tryFull = tryFull;
	}
	
	@Override
	public String getText() {
		return "Load Residue";
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		
		//Check if sequence is already loaded
		if (!span.getBioSeq().isAvailable(span)) {
			boolean new_residue_loaded = false;
			Application app = Application.getSingleton();
			JFrame frame = (app == null) ? null : app.getFrame();
			final JDialog dialog = new JDialog(frame, "Loading...");
			JLabel message = new JLabel("Residues are being loaded please wait");
			JProgressBar progressBar = new JProgressBar();

			//progressBar.setMaximumSize(new Dimension(150, 5));
			progressBar.setIndeterminate(true);

			Box box = Box.createVerticalBox();
			box.add(Box.createGlue());
			box.add(message);
			box.add(progressBar);

			dialog.getContentPane().add(box, "Center");

			//dialog.setUndecorated(true);
			dialog.setResizable(false);
			dialog.validate();
			dialog.pack();

			if (frame != null) {
				Point location = frame.getLocation();
				dialog.setLocation(location.x + frame.getWidth() / 2 - dialog.getWidth() / 2, location.y + frame.getHeight() / 2 - dialog.getHeight() / 2);
			}


			dialog.setVisible(true);
			new_residue_loaded = GeneralLoadView.getLoadView().loadResidues(span, tryFull);

			dialog.setVisible(false);
			dialog.dispose();
			
			if (new_residue_loaded) {
				ThreadUtils.runOnEventQueue(new Runnable() {

					public void run() {
						final SeqMapView smv = IGB.getSingleton().getMapView();
						smv.setAnnotatedSeq(span.getBioSeq(), true, true, true);
					}
				});
			}
		}

		actionDone();
	}
}
