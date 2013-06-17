package com.affymetrix.igb.bookmarks.action;

import java.net.MalformedURLException;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.igb.bookmarks.Bookmark;
import com.affymetrix.igb.bookmarks.BookmarkController;
import com.affymetrix.igb.bookmarks.BookmarkEditor;
import com.affymetrix.igb.bookmarks.BookmarkList;
import com.affymetrix.igb.bookmarks.BookmarkManagerView;
import com.affymetrix.igb.bookmarks.Separator;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * This class is designed to create and add new bookmark to the list.
 *
 * @author Nick & David
 */
public abstract class AddBookmarkAction extends GenericAction {

	private static final long serialVersionUID = 1L;

	public AddBookmarkAction(String text, String tooltip, String iconPath, String largeIconPath, int mnemonic, Object extraInfo, boolean popup) {
		super(text, tooltip, iconPath, largeIconPath, mnemonic, extraInfo, popup);
	}

	protected Bookmark getCurrentPosition(boolean include_sym_and_props) {
		if (!BookmarkController.hasSymmetriesOrGraphs()) {
			ErrorHandler.errorPanel("Error: No Symmetries or graphs to bookmark.");
			return null;
		}
		Bookmark bookmark = null;
		try {
			bookmark = BookmarkController.getCurrentBookmark(include_sym_and_props, BookmarkActionManager.getInstance().getVisibleSpan());
		} catch (MalformedURLException m) {
			ErrorHandler.errorPanel("Couldn't add bookmark", m, Level.SEVERE);
		}
		if (bookmark == null) {
			ErrorHandler.errorPanel("Error", "Nothing to bookmark", Level.INFO);
		}
		return bookmark;
	}
	
	/**
	 * add node to the tree.
	 *
	 * @param node (bookmark, folder or separator)
	 */
	public static void addNode(DefaultMutableTreeNode node) {
		JTree tree = BookmarkManagerView.getSingleton().tree;
		TreePath path;
		if (tree.getSelectionCount() > 0) {
			path = tree.getSelectionModel().getSelectionPath();
		} else {
			tree.setSelectionRow(0);
			path = tree.getSelectionModel().getSelectionPath();
		}
		BookmarkManagerView.getSingleton().insert(tree, path, new DefaultMutableTreeNode[]{node});
		BookmarkActionManager.getInstance().rebuildMenus();
	}
}
