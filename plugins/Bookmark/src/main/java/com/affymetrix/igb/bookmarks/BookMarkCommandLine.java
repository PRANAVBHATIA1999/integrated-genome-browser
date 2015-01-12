package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericServerInitListener;
import com.affymetrix.igb.osgi.service.IGBService;
import java.net.MalformedURLException;
import javax.swing.SwingUtilities;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hiralv
 */
public class BookMarkCommandLine {

    private final IGBService igbService;
    private ServiceRegistration registration;
    private final String url;
    private final boolean force;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BookMarkCommandLine.class);

    BookMarkCommandLine(final BundleContext bundleContext, final IGBService igbService, final String url, final boolean force) {
        this.igbService = igbService;
        this.url = url;
        this.force = force;

        if (igbService.areAllServersInited()) {
            gotoBookmark();
        } else {
            GenericServerInitListener genericServerListener = evt -> {
                if (!igbService.areAllServersInited()) { // do this first to avoid race condition
                    return;
                }
                registration.unregister();
                registration = null;
                gotoBookmark();
            };
            registration = bundleContext.registerService(GenericServerInitListener.class, genericServerListener, null);
        }
    }

    // If the command line contains a parameter "-href http://..." where
    // the URL is a valid IGB control bookmark, then go to that bookmark.
    private void gotoBookmark() {
        GenometryModel gmodel = GenometryModel.getInstance();

        // If it is -home then do not force to switch unless no species is selected.
        if (!force && gmodel.getSelectedSeqGroup() != null && gmodel.getSelectedSeq() != null) {
            logger.warn("Previous species already loaded. Home {0} will be not loaded", url);
            return;
        }

        try {
            final Bookmark bm = new Bookmark(null, "", url);
            if (bm.isValidBookmarkFormat()) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        //sleep thread to allow time for osgi bundles to completely load
                        Thread.sleep(5000);
                        logger.info("Loading bookmark: {0}", url);
                        BookmarkController.viewBookmark(igbService, bm);
                    } catch (InterruptedException ex) {
                        logger.error("Thread Interrupted", ex.getMessage());
                    }
                });
            } else {
                logger.error("Invalid bookmark given with -href argument: \n{0}", url);
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace(System.err);
        }
    }
}
