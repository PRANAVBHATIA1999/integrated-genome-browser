package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericServerInitEvent;
import com.affymetrix.genometryImpl.event.GenericServerInitListener;
import com.affymetrix.igb.osgi.service.IGBService;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author hiralv
 */
public class BookMarkCommandLine {

	private final IGBService igbService;
	private final String url;
	private final boolean force;
	private static final Logger ourLogger
		  = Logger.getLogger(BookMarkCommandLine.class.getPackage().getName());
				
	BookMarkCommandLine(final IGBService igbService, final String url, final boolean force) {
		this.igbService = igbService;
		this.url = url;
		this.force = force;
		
		if (igbService.areAllServersInited()) {
			gotoBookmark();
		} else {
			GenericServerInitListener genericServerListener = new GenericServerInitListener() {
				@Override
				public void genericServerInit(GenericServerInitEvent evt) {
					if (!igbService.areAllServersInited()) { // do this first to avoid race condition
						return;
					}
					igbService.removeServerInitListener(this);
					gotoBookmark();
				}
			};
			igbService.addServerInitListener(genericServerListener);
		}
	}

	// If the command line contains a parameter "-href http://..." where
	// the URL is a valid IGB control bookmark, then go to that bookmark.
	private void gotoBookmark(){
		GenometryModel gmodel = GenometryModel.getGenometryModel();

		// If it is -home then do not force to switch unless no species is selected.
		if(!force && gmodel.getSelectedSeqGroup() != null && gmodel.getSelectedSeq() != null) {
			ourLogger.log(Level.WARNING,
					"Previous species already loaded. Home {0} will be not loaded", url);
			return;
		}

		try {
			final Bookmark bm = new Bookmark(null, "", url);
			if (bm.isUnibrowControl()) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						ourLogger.log(Level.INFO, "Loading bookmark: {0}", url);
						BookmarkController.viewBookmark(igbService, bm);
					}
				});
			} else {
				ourLogger.log(Level.SEVERE,
						"Invalid bookmark given with -href argument: \n{0}", url);
			}
		} catch (MalformedURLException mue) {
			mue.printStackTrace(System.err);
		}
	}
}
