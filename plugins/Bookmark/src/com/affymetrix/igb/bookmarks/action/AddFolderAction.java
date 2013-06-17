package com.affymetrix.igb.bookmarks.action;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.bookmarks.BookmarkList;
import static com.affymetrix.igb.bookmarks.BookmarkManagerView.BUNDLE;

/**
 *
 * @author lorainelab
 */
public class AddFolderAction extends BookmarkAction {

	private static final long serialVersionUID = 1L;
	private static final AddFolderAction ACTION = new AddFolderAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static AddFolderAction getAction() {
		return ACTION;
	}

	private AddFolderAction() {
		super(BUNDLE.getString("addBookmarkFolder"), null,
				null, null, KeyEvent.VK_G, null, false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		addBookmarkFolder();
	}
	
	/**
	 * add a folder to bookmark tree.
	 */
	public static void addBookmarkFolder() {
		BookmarkList bl = new BookmarkList("Folder");
		DefaultMutableTreeNode node = bl;
		addNode(node);
	}
}
