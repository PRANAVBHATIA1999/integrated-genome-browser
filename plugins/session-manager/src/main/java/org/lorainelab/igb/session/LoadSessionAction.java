package org.lorainelab.igb.session;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import com.affymetrix.common.PreferenceUtils;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.util.FileTracker;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.bookmarks.model.Bookmark;
import com.affymetrix.igb.bookmarks.service.BookmarkService;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.InvalidPreferencesFormatException;
import javafx.stage.FileChooser;
import org.lorainelab.igb.javafx.FileChooserUtil;
import org.lorainelab.igb.menu.api.model.MenuBarParentMenu;
import org.lorainelab.igb.menu.api.model.MenuIcon;
import org.lorainelab.igb.menu.api.model.MenuItem;
import org.lorainelab.igb.services.IgbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;

@Component(name = LoadSessionAction.COMPONENT_NAME, immediate = true, service = {MenuBarEntryProvider.class, GenericAction.class})
public class LoadSessionAction extends GenericAction implements MenuBarEntryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LoadSessionAction.class);
    public static final String COMPONENT_NAME = "LoadSessionAction";
    private static final String LOAD_SESSION_ICON = "load_session.png";
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundle");
    private static final int MENU_POSITION = 45;
    private IgbService igbService;
    private BookmarkService bookmarkService;
    private final int TOOLBAR_INDEX = 2;

    public LoadSessionAction() {
        super(BUNDLE.getString("loadSession"), BUNDLE.getString("openSessionTooltip"),
                "16x16/actions/load_session.png", "22x22/actions/load_session.png",
                KeyEvent.VK_L, null, true);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        // <Ashwini Kadam> IGBF-1150 : Related to IGBF-1140
        // JFileChooser displays JavaFX style Open File Session dialog
        // while loading a session. Instead we want OS Native file choooser. 
        // Thus we are turning to FileChooser for opening a dialog.
        FileTracker fileTracker = FileTracker.DATA_DIR_TRACKER;
        FileChooser.ExtensionFilter extFilter = 
                    new FileChooser.ExtensionFilter("XML File", "*.xml");
        Optional<File> selectedFile = FileChooserUtil.build()
                .setContext(fileTracker.getFile())
                .setTitle("Load Session")
                .setFileExtensionFilters(Lists.newArrayList(extFilter))
                .retrieveFileFromFxChooser();

        if (selectedFile.isPresent()) {
            fileTracker.setFile(selectedFile.get().getParentFile());
            try {
                loadSession(selectedFile.get());
            } catch (InvalidPreferencesFormatException ipfe) {
                ErrorHandler.errorPanel("ERROR", "Invalid preferences format:\n" + ipfe.getMessage()
                        + "\n\nYou can only load a session from a file that was created with save session.");
            } catch (Exception x) {
                ErrorHandler.errorPanel("ERROR", "Error loading session from file", x);
            }
        }
    }

    public void loadSession(File f) throws Exception {
        PreferenceUtils.importPreferences(f);
        igbService.loadState();
        String bk_url = PreferenceUtils.getSessionPrefsNode().get("bookmark", "");
        if (bk_url.length() <= 0) {
            StringBuilder buffer = new StringBuilder();
            int j = 0;
            while (true) {
                String sb_bk_url = PreferenceUtils.getSessionPrefsNode().get("bookmark" + j++, "");
                if (sb_bk_url.length() <= 0) {
                    bk_url = buffer.toString();
                    break;
                }
                buffer.append(sb_bk_url);
            }
        }

        String url = URLDecoder.decode(bk_url, Charsets.UTF_8.displayName());
        if (url != null && url.trim().length() > 0) {
            Bookmark bookmark = new Bookmark(null, "", url);
            bookmarkService.loadBookmark(bookmark);
        }
        PreferenceUtils.getSessionPrefsNode().removeNode();
    }

    @Reference
    public void setBookmarkService(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @Override
    public Optional<List<MenuItem>> getMenuItems() {
        MenuItem menuItem = new MenuItem(BUNDLE.getString("loadSession"), (Void t) -> {
            actionPerformed(null);
            return t;
        });
        try (InputStream resourceAsStream = LoadSessionAction.class.getClassLoader().getResourceAsStream(LOAD_SESSION_ICON)) {
            menuItem.setMenuIcon(new MenuIcon(resourceAsStream));
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        menuItem.setWeight(MENU_POSITION);
        return Optional.of(Arrays.asList(menuItem));
    }

    @Override
    public MenuBarParentMenu getMenuExtensionParent() {
        return MenuBarParentMenu.FILE;
    }
}
