package com.affymetrix.igb.bookmarks.action;

import com.affymetrix.igb.bookmarks.Bookmark;
import com.affymetrix.igb.bookmarks.BookmarkController;
import com.affymetrix.igb.bookmarks.BookmarkManagerView;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.IGBAction;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genoviz.util.ErrorHandler;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.prefs.InvalidPreferencesFormatException;

import javax.swing.JFileChooser;

public class LoadSessionAction extends IGBAction {
	private static final long serialVersionUID = 1l;
	private IGBService igbService;

	public LoadSessionAction(IGBService igbService) {
		super();
		this.igbService = igbService;
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = PreferenceUtils.getJFileChooser();
		int option = chooser.showOpenDialog(igbService.getFrame().getContentPane());
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				File f = chooser.getSelectedFile();
				PreferenceUtils.importPreferences(f);
				igbService.loadState();
				String url = URLDecoder.decode(PreferenceUtils.getSessionPrefsNode().get("bookmark", ""), Bookmark.ENC);
				if (url != null) {
			        BookmarkController.viewBookmark(igbService, new Bookmark(null, url));
				}
				PreferenceUtils.getSessionPrefsNode().removeNode();
			}
			catch (InvalidPreferencesFormatException ipfe) {
				ErrorHandler.errorPanel("ERROR", "Invalid preferences format:\n" + ipfe.getMessage()
						+ "\n\nYou can only load a session from a file that was created with save session.");
			}
			catch (Exception x) {
				ErrorHandler.errorPanel("ERROR", "Error loading session from file", x);
			}
		}
	}

	@Override
	public String getText() {
		return MessageFormat.format(
				BookmarkManagerView.BUNDLE.getString("menuItemHasDialog"),
				BookmarkManagerView.BUNDLE.getString("loadSession"));
	}

	@Override
	public String getIconPath() {
		return "toolbarButtonGraphics/general/Import16.gif";
	}

	@Override
	public int getShortcut() {
		return KeyEvent.VK_L;
	}
}
