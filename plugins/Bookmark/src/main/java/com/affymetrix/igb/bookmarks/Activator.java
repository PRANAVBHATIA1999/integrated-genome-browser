package com.affymetrix.igb.bookmarks;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.igb.bookmarks.action.AddBookmarkAction;
import com.affymetrix.igb.bookmarks.action.BookmarkActionManager;
import com.affymetrix.igb.bookmarks.action.CopyBookmarkAction;
import com.affymetrix.igb.bookmarks.action.ExportBookmarkAction;
import com.affymetrix.igb.bookmarks.action.ImportBookmarkAction;
import com.lorainelab.igb.service.api.IWindowRoutine;
import com.lorainelab.igb.service.api.IgbService;
import com.lorainelab.igb.service.api.IgbTabPanel;
import com.lorainelab.igb.service.api.IgbTabPanelI;
import com.lorainelab.igb.service.api.XServiceRegistrar;
import com.affymetrix.igb.swing.JRPMenu;
import com.affymetrix.igb.swing.JRPMenuItem;
import com.affymetrix.igb.swing.MenuUtil;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends XServiceRegistrar<IgbService> implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);
    private static final String WILDCARD = "*";

    public Activator() {
        super(IgbService.class);
    }

    @Override
    protected ServiceRegistration<?>[] getServices(BundleContext bundleContext, IgbService igbService) throws Exception {

        // assuming last file menu item is Exit, leave it there
//        JRPMenu file_menu = igbService.getMenu("file");
//        final int index = file_menu.getItemCount() - 1;
//        file_menu.insertSeparator(index);
        return new ServiceRegistration[]{
            bundleContext.registerService(IgbTabPanelI.class, getPage(bundleContext, igbService), null)
        };
    }

    private IgbTabPanel getPage(BundleContext bundleContext, IgbService igbService) {
        ResourceBundle BUNDLE = ResourceBundle.getBundle("bookmark");

        // Need to let the QuickLoad system get started-up before starting
        //   the control server that listens to ping requests?
        // Therefore start listening for http requests only after all set-up is done.
        String[] args = CommonUtils.getInstance().getArgs(bundleContext);
        String url = CommonUtils.getInstance().getArg("-href", args);

        if (StringUtils.equals(url, WILDCARD)) {
            url = null;
        }
        if (StringUtils.isNotBlank(url)) {
            logger.info("Loading bookmark {}", url);
            new BookMarkCommandLine(bundleContext, igbService, url, true);
        } else {
            url = CommonUtils.getInstance().getArg("-home", args);
            if (StringUtils.isNotBlank(url)) {
                logger.info("Loading home {}", url);
                new BookMarkCommandLine(bundleContext, igbService, url, false);
            }
        }

        String portString = CommonUtils.getInstance().getArg("-port", args);
        if (portString != null) {
            SimpleBookmarkServer.setServerPort(portString);
        }
        SimpleBookmarkServer.init(igbService);

        AddBookmarkAction.createAction(igbService);

        BookmarkList main_bookmark_list = new BookmarkList("Bookmarks");
        JRPMenu bookmark_menu = igbService.addTopMenu("Bookmark_bookmarksMenu", BUNDLE.getString("bookmarksMenu"), 6);
        bookmark_menu.setMnemonic(BUNDLE.getString("bookmarksMenuMnemonic").charAt(0));
        MenuUtil.addToMenu(bookmark_menu, new JRPMenuItem("Bookmark_add_pos", AddBookmarkAction.getAction()));
        MenuUtil.addToMenu(bookmark_menu, new JRPMenuItem("Bookmark_import", ImportBookmarkAction.getAction()));
        MenuUtil.addToMenu(bookmark_menu, new JRPMenuItem("Bookmark_export", ExportBookmarkAction.getAction()));
        MenuUtil.addToMenu(bookmark_menu, new JRPMenuItem("Bookmark_clipboard", CopyBookmarkAction.getAction()));
        bookmark_menu.addSeparator();

        BookmarkActionManager.init(igbService, bookmark_menu, main_bookmark_list);
        final BookmarkActionManager bmark_action = BookmarkActionManager.getInstance();
        bundleContext.registerService(IWindowRoutine.class.getName(),
                new IWindowRoutine() {
                    @Override
                    public void stop() {
                        bmark_action.autoSaveBookmarks();
                    }

                    @Override
                    public void start() { /* Do Nothing */ }
                },
                null
        );
        BookmarkManagerViewGUI.init(igbService);
        BookmarkManagerView.getSingleton().addTreeModelListener(bmark_action);
        BookmarkManagerViewGUI.getSingleton().getBookmarkManagerView().setBList(main_bookmark_list);
        return BookmarkManagerViewGUI.getSingleton();
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        super.start(bundleContext);
    }

}
