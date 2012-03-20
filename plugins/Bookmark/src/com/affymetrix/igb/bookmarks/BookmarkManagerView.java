/**
 * Copyright (c) 2001-2006 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.bookmarks;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.util.UniFileFilter;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.igb.bookmarks.action.AddBookmarkAction;
import com.affymetrix.igb.bookmarks.action.BookmarkActionManager;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.FileTracker;
import java.awt.Container;
import java.awt.event.*;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.*;
import javax.swing.undo.UndoManager;

/**
 * A panel for viewing and re-arranging bookmarks in a hierarchy.
 */
public final class BookmarkManagerView implements TreeSelectionListener {

	private static JFileChooser static_chooser = null;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bookmark");
	public JTree tree;
	public BottomThing thing;
	private final DefaultTreeModel tree_model = new DefaultTreeModel(null, true);
	// refresh_action is an action that is useful during debugging, but should go away later.
	public final Action import_action;
	public final Action export_action;
	public final Action delete_action;
	public JButton forwardButton = new JButton();
	public JButton backwardButton = new JButton();
	public JButton addBookmarkButton = new JButton();
	public JButton addSeparatorButton = new JButton();
	public JButton addFolderButton = new JButton();
	public JButton deleteBookmarkButton = new JButton();
	public List<TreePath> bookmark_history;
	public FileFilter ff = new TextExportFileFilter(new UniFileFilter(new String[]{"txt"}, "TEXT Files"));
	public FileFilter ff1 = new HTMLExportFileFilter(new UniFileFilter(new String[]{"html", "htm", "xhtml"}, "HTML Files"));
	public int history_pointer = -1;
	private final BookmarkTreeCellRenderer renderer;
	private static BookmarkManagerView singleton;
	protected int last_selected_row = -1;  // used by dragUnderFeedback()
	private boolean doNotShowWarning = false;

	public static void init(IGBService _igbService) {
		if (singleton == null) {
			singleton = new BookmarkManagerView(_igbService);
		}
	}

	public static synchronized BookmarkManagerView getSingleton() {
		return singleton;
	}

	/**
	 * Creates a new instance of Class
	 */
	public BookmarkManagerView(IGBService igbService) {

		tree = new BookmarkTree(this);
		tree.setModel(tree_model);
		bookmark_history = new ArrayList<TreePath>();

		thing = new BottomThing(tree);
		thing.setIGBService(igbService);
		tree.addTreeSelectionListener(thing);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		tree.setRootVisible(true);
		tree.setShowsRootHandles(true);
		tree.setEditable(false);

		renderer = new BookmarkTreeCellRenderer();
		tree.setCellRenderer(renderer);

		ToolTipManager.sharedInstance().registerComponent(tree);

		export_action = makeExportAction();
		import_action = makeImportAction();
		delete_action = makeDeleteAction();
		forwardButton.setEnabled(false);
		backwardButton.setEnabled(false);

		initPopupMenu();

		tree.addTreeSelectionListener(this);
	}

	public boolean insert(JTree tree, TreePath tree_path, DefaultMutableTreeNode[] nodes) {
		if (tree_path == null) {
			return false;
		}

		DefaultMutableTreeNode tree_node = (DefaultMutableTreeNode) tree_path.getLastPathComponent();

		if (tree_node == null) {
			return false;
		}

		DefaultMutableTreeNode parent;
		int row = tree.getRowForPath(tree_path);
		if (tree_node.getAllowsChildren() && dropInto(row)) {
			parent = tree_node;
		} else {
			parent = (DefaultMutableTreeNode) tree_node.getParent();
		}

		int index = parent.getChildCount();

		// Copy or move each source object to the target
		for (int i = nodes.length - 1; i >= 0; i--) {
			DefaultMutableTreeNode node = nodes[i];
			try {
				((DefaultTreeModel) tree.getModel()).insertNodeInto(node, parent, index);
				TreePath path = new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(node));

				tree.setSelectionPath(path);
			} catch (IllegalStateException e) {
				// Cancelled by user
				return false;
			}
		}
		return true;
	}

	public void setBList(BookmarkList blist) {
		tree_model.setRoot(blist);
		tree.setSelectionRow(0);
		tree.clearSelection();
	}

	public void valueChanged(TreeSelectionEvent e) {
		if (e.getSource() != tree) {
			return;
		}

		if (tree.getSelectionCount() > 0) {
			if (tree.isRowSelected(0)) {
				deleteBookmarkButton.setEnabled(false);
			} else {
				deleteBookmarkButton.setEnabled(true);
			}
		}
	}

	private void initPopupMenu() {
		final JPopupMenu popup = new JPopupMenu() {

			private static final long serialVersionUID = 1L;

			@Override
			public JMenuItem add(Action a) {
				JMenuItem menu_item = super.add(a);
				menu_item.setToolTipText(null);
				return menu_item;
			}
		};

		popup.add(thing.getPropertiesAction());

		MouseAdapter mouse_adapter = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (processDoubleClick(e)) {
					return;
				}

				if (popup.isPopupTrigger(e)) {
					popup.show(tree, e.getX(), e.getY());
				}
			}

			private boolean processDoubleClick(MouseEvent e) {
				if (e.getClickCount() != 2) {
					return false;
				}

				thing.getGoToAction().actionPerformed(null);

				return true;
			}
		};
		tree.addMouseListener(mouse_adapter);
	}

	/**
	 * Tries to import bookmarks into Unibrow. Makes use of {@link BookmarksParser#parse(BookmarkList, File)}.
	 */
	private void importBookmarks(BookmarkList bookmark_list, JFrame frame) {
		JFileChooser chooser = getJFileChooser(false);
		chooser.setCurrentDirectory(getLoadDirectory());
		int option = chooser.showOpenDialog(frame);
		if (option == JFileChooser.APPROVE_OPTION) {
			setLoadDirectory(chooser.getCurrentDirectory());
			try {
				File fil = chooser.getSelectedFile();
				BookmarksParser.parse(bookmark_list, fil);
				AddBookmarkAction.addNode((DefaultMutableTreeNode) bookmark_list);
			} catch (Exception ex) {
				ErrorHandler.errorPanel(frame, "Error", "Error importing bookmarks", ex);
			}
		}
	}

	public Action makeImportAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				BookmarkList bl = new BookmarkList("Import");
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				String createdTime = dateFormat.format(Calendar.getInstance().getTime());
				bl.setComment("Created Time: " + createdTime);
				importBookmarks(bl, null);
				//tree_model.reload();
			}

			@Override
			public String getText() {
				return "Import ...";
			}
		};
		return a;
	}

	private void exportBookmarks(BookmarkList main_bookmark_list, JFrame frame) {
		if (main_bookmark_list == null || main_bookmark_list.getChildCount() == 0) {
			ErrorHandler.errorPanel(frame, "Error", "No bookmarks to save", (Exception) null);
			return;
		}
		JFileChooser chooser = getJFileChooser(true);
		chooser.setCurrentDirectory(getLoadDirectory());
		int option = chooser.showSaveDialog(frame);
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				setLoadDirectory(chooser.getCurrentDirectory());
				((ExportFileFilter) chooser.getFileFilter()).export(main_bookmark_list, chooser.getSelectedFile());
			} catch (Exception ex) {
				ErrorHandler.errorPanel(frame, "Error", "Error exporting bookmarks", ex);
			}
		}
	}

	public Action makeExportAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				BookmarkList bl = (BookmarkList) tree_model.getRoot();
				exportBookmarks(bl, null); // already contains a null check on bookmark list
			}

			@Override
			public String getText() {
				return "Export ...";
			}
		};
		return a;
	}

	Action makeDeleteAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				deleteAction();
		//		setBList(BookmarkActionManager.getInstance().getMainBookmarkList());
			}

			@Override
			public String getText() {
				return "Delete ...";
			}
		};
		return a;
	}

	public void deleteAction() {
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if (selectionPaths == null) {
			return;
		}
		Container frame = SwingUtilities.getAncestorOfClass(JFrame.class, tree);
		JCheckBox checkbox = PreferenceUtils.createCheckBox("Do not show this message again.", PreferenceUtils.getTopNode(), "BookmarkManagerView_showDialog", false);
		String message = "Delete these " + selectionPaths.length + " selected items?";
		Object[] params = {message, checkbox};
		doNotShowWarning = checkbox.isSelected();
		if (!doNotShowWarning) {
			int yes = JOptionPane.showConfirmDialog(frame, params, "Delete?", JOptionPane.YES_NO_OPTION);
			doNotShowWarning = checkbox.isSelected();
			if (yes == JOptionPane.YES_OPTION) {
				for (int i = 0; i < selectionPaths.length; i++) {
					TreePath path = selectionPaths[i];
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					if (node.getParent() != null) {
						tree_model.removeNodeFromParent(node);
						removeBookmarkFromHistory(path);
					}
				}
			}
		} else {
			for (int i = 0; i < selectionPaths.length; i++) {
				TreePath path = selectionPaths[i];
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.getParent() != null) {
					tree_model.removeNodeFromParent(node);
					removeBookmarkFromHistory(path);
				}
			}
		}

		BookmarkActionManager.getInstance().rebuildMenus();
	}

	public TreePath getPath(TreeNode treeNode) {
		List<Object> nodes = new ArrayList<Object>();
		if (treeNode != null) {
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while (treeNode != null) {
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}
		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

	public void addBookmarkToHistory(BookmarkList bl) {
		addBookmarkToHistory(getPath(bl));
	}

	public void addBookmarkToHistory(TreePath tp) {
		if (tp == null) {
			return;
		}
		int lastEntryIndex = bookmark_history.size() - 1;
		if (!bookmark_history.isEmpty()) {
			if (!bookmark_history.get(lastEntryIndex).equals(tp)) {
				bookmark_history.add(tp);
				history_pointer = bookmark_history.size() - 1;
				forwardButton.setEnabled(true);
				backwardButton.setEnabled(bookmark_history.size() > 1);
			}
		} else if (bookmark_history.isEmpty()) {
			bookmark_history.add(tp);
			history_pointer = bookmark_history.size() - 1;
			forwardButton.setEnabled(true);
			backwardButton.setEnabled(bookmark_history.size() > 1);
		}
	}

	public void removeBookmarkFromHistory(TreePath tp) {
		if (tp == null) {
			return;
		}
		int remove_pos = bookmark_history.indexOf(tp);
		if (remove_pos > -1) {
			if (history_pointer >= remove_pos) {
				history_pointer--;
			}
			bookmark_history.remove(remove_pos);
			forwardButton.setEnabled(history_pointer < bookmark_history.size() - 1);
			backwardButton.setEnabled(history_pointer > 0);
		}
	}

	public File getLoadDirectory() {
		return FileTracker.DATA_DIR_TRACKER.getFile();
	}

	public void setLoadDirectory(File file) {
		FileTracker.DATA_DIR_TRACKER.setFile(file);
	}

	/**
	 * Gets a static re-usable file chooser that prefers "html" files.
	 */
	private JFileChooser getJFileChooser(boolean export) {
		if (static_chooser == null) {
			static_chooser = new JFileChooser() {

				@Override
				public void approveSelection() {
					File f = getSelectedFile();
					String path;
					if((f.getAbsolutePath().indexOf("."))<0)
					{
						if(static_chooser.getFileFilter().equals(ff))
							path = f.getAbsolutePath()+".txt";
						else
							path = f.getAbsolutePath()+".html";
						f = new File(path);
					}
					if (f.exists() && getDialogType() == SAVE_DIALOG) {
						int result = JOptionPane.showConfirmDialog(this,
								"The file exists, overwrite?", "Existing file",
								JOptionPane.YES_NO_OPTION);
						switch (result) {
							case JOptionPane.YES_OPTION:
								super.approveSelection();
								return;
							case JOptionPane.NO_OPTION:
								return;
						}
					}
					super.approveSelection();
				}
			};
			ff1 = new HTMLExportFileFilter(new UniFileFilter(
					new String[]{"html", "htm", "xhtml"}, "HTML Files"));
			static_chooser.setAcceptAllFileFilterUsed(false);
			static_chooser.setCurrentDirectory(getLoadDirectory());
			static_chooser.addChoosableFileFilter(ff1);
			
		}
		if(export)
			static_chooser.addChoosableFileFilter(ff);
		else
			static_chooser.removeChoosableFileFilter(ff);
		static_chooser.setFileFilter(ff1);
		static_chooser.rescanCurrentDirectory();
		return static_chooser;
	}
	
	/**
	 * Returns true or false to indicate that if an item is inserted at the
	 * given row it will be inserted "into" (true) or "after" (false) the item
	 * currently at that row. Will return true only if the given row contains a
	 * folder and that folder is currently expanded or empty or is the root
	 * node.
	 */
	private boolean dropInto(int row) {
		boolean into = false;
		TreePath path = tree.getPathForRow(row);
		if (path == null) {
			// not necessarily an error
			return false;
		}
		if (row == 0) { // node is root [see DefaultMutableTreeNode.isRoot()]
			into = true;
		} else if (tree.isExpanded(path)) {
			into = true;
		} else {
			TreeNode node = (TreeNode) path.getLastPathComponent();
			if (node.getAllowsChildren()) {
				into = true;
			}
		}
		return into;
	}

	public void destroy() {
//    this.setApplication(null);
		tree.removeTreeSelectionListener(this);
		thing = null;
		tree = null;
	}

	/**
	 * A JPanel that listens for TreeSelectionEvents, displays the name(s) of
	 * the selected item(s), and may allow you to edit them.
	 */
	public class BottomThing extends JPanel implements TreeSelectionListener {

		private static final long serialVersionUID = 1L;
		public JTabbedPane tabPane = new JTabbedPane();
		JLabel name_label = new JLabel("Name:");
		public JRPTextField name_text_field = new JRPTextField("BookmarkManagerView_name_text_area");
		public JTextArea comment_text_area = new JTextArea();
		public BookmarkData bookmarkData;
		TreePath selected_path = null;
		public BookmarkList selected_bl = null;
		BookmarkList previousSelected_bl = null;
		private final JTree tree;
		public IGBService igbService = null;
		public final DefaultTreeModel def_tree_model;
		Action properties_action;
		Action goto_action;
		UndoManager undoManager = new UndoManager();

		BottomThing(JTree tree) {
			if (tree == null) {
				throw new IllegalArgumentException();
			}

			this.tree = tree;
			this.tree.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if ((e.getKeyCode() == KeyEvent.VK_DELETE)) {
						deleteAction();
					}
					if ((e.getKeyCode() == KeyEvent.VK_ENTER)) {
						goToAction();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}
			});
			this.def_tree_model = (DefaultTreeModel) tree.getModel();

			properties_action = makePropertiesAction();
			properties_action.setEnabled(false);
			goto_action = makeGoToAction();
			goto_action.setEnabled(false);

			this.name_text_field.setEnabled(false);
			this.name_text_field.getDocument().addUndoableEditListener(undoManager);
			this.comment_text_area.setEnabled(false);
			this.comment_text_area.getDocument().addUndoableEditListener(undoManager);

			bookmarkData = new BookmarkData();
		}

		public void updateInfoOrDataTable() {
			Object o = null;
			if (selected_bl != null) {
				o = selected_bl.getUserObject();
			}
			if (o instanceof Bookmark) {
				switch (tabPane.getSelectedIndex()) {
					case 1:
						bookmarkData.setInfoTableFromBookmark(selected_bl);
						break;
					case 2:
						bookmarkData.setDataListTableFromBookmark(selected_bl);
						break;
				}
			}
		}

		/**
		 * Sets the instance of IGBService. This is the instance in which the
		 * bookmarks will be opened when the "GoTo" button is pressed.
		 *
		 * @param igbService an instance of IGBService; null is ok.
		 */
		void setIGBService(IGBService igbService) {
			this.igbService = igbService;
		}

		public void valueChanged(TreeSelectionEvent e) {
			updatePreviousBookmarkData();

			Object source = e.getSource();
			assert source == tree;
			if (source != tree) {
				return;
			}

			TreePath[] selections = tree.getSelectionPaths();

			name_text_field.setText("");
			comment_text_area.setText("");
			comment_text_area.setEnabled(false);
			name_text_field.setEnabled(false);
			properties_action.setEnabled(false);
			goto_action.setEnabled(false);

			if (selections != null && selections.length == 1) {
				selected_path = selections[0];
				selected_bl = (BookmarkList) selected_path.getLastPathComponent();
				Object user_object = selected_bl.getUserObject();
				name_text_field.setText(selected_bl.getName());
				comment_text_area.setText(selected_bl.getComment());
				bookmarkData.getInfoModel().clear();
				bookmarkData.getDataListModel().clear();

				if (user_object instanceof Bookmark) {
					comment_text_area.setEnabled(true);
					name_text_field.setEnabled(true);
					properties_action.setEnabled(true);
					goto_action.setEnabled(igbService != null);
					comment_text_area.setText(((Bookmark) user_object).getComment());
					updateInfoOrDataTable();
				} else if (user_object instanceof Separator) {
					name_text_field.setText("Separator");
					comment_text_area.setText("Uneditable");
				} else if (selected_bl == def_tree_model.getRoot()) {
					comment_text_area.setText("Uneditable");
				} else {
					name_text_field.setEnabled(true);
					comment_text_area.setEnabled(true);
				}

				previousSelected_bl = selected_bl;
			}
		}

		/*
		 * Auto save comments when another node is selected.
		 */
		public void updatePreviousBookmarkData() {
			updateNode(previousSelected_bl,
					name_text_field.getText(),
					comment_text_area.getText());
		}

		public void updateBookmarkData() {
			TreePath[] selections = tree.getSelectionPaths();
			selected_path = selections[0];
			selected_bl = (BookmarkList) selected_path.getLastPathComponent();
			updateNode(selected_bl,
					name_text_field.getText(),
					comment_text_area.getText());
		}

		public void updateNode(BookmarkList bl, String name, String comment) {
			if (bl == def_tree_model.getRoot()) {
				// I do not allow re-naming the root node currently
				return;
			}
			if (name == null || name.length() == 0) {
				return;
			}
			Object user_object = selected_bl.getUserObject();
			if (user_object instanceof Bookmark) {
				Bookmark bm = (Bookmark) user_object;
				bm.setName(name);
				bm.setComment(comment);
			} else if (user_object instanceof String) {
				selected_bl.setUserObject(name);
				selected_bl.setComment(comment);
			}

			def_tree_model.nodeChanged(bl);
		}

		public Action getPropertiesAction() {
			return properties_action;
		}

		private Action makePropertiesAction() {
			Action a = new GenericAction() {

				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae) {
					super.actionPerformed(ae);
					BookmarkPropertiesGUI.getSingleton().displayPanel(selected_bl);
				}

				@Override
				public String getText() {
					return "Properties ...";
				}
			};
			return a;
		}

		public Action getGoToAction() {
			return goto_action;
		}

		private Action makeGoToAction() {
			Action a = new GenericAction() {

				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae) {
					super.actionPerformed(ae);
					goToAction();
				}

				@Override
				public String getText() {
					return "Go To";
				}
			};
			return a;
		}

		private void goToAction() {
			if (igbService == null || selected_bl == null || !(selected_bl.getUserObject() instanceof Bookmark)) {
				setEnabled(false);
			} else {

				Bookmark bm = (Bookmark) selected_bl.getUserObject();
				addBookmarkToHistory(tree.getSelectionPath());
				BookmarkController.viewBookmark(igbService, bm);
			}
		}
	}

	private abstract class ExportFileFilter extends javax.swing.filechooser.FileFilter {

		final UniFileFilter filter;

		public ExportFileFilter(UniFileFilter filter) {
			this.filter = filter;
		}

		@Override
		public boolean accept(File f) {
			return filter.accept(f);
		}

		@Override
		public String getDescription() {
			return filter.getDescription();
		}

		public void export(BookmarkList main_bookmark_list, File fil) throws Exception {
			String full_path = fil.getCanonicalPath();
			boolean extSet = false;
			for (String ext : filter.getExtensions()) {
				if (full_path.endsWith("." + ext)) {
					extSet = true;
					break;
				}
			}
			if (!extSet) {
				fil = new File(full_path + "." + filter.getExtensions().iterator().next());
			}
			write(main_bookmark_list, fil);
		}

		protected abstract void write(BookmarkList main_bookmark_list, File fil) throws Exception;
	}

	private class HTMLExportFileFilter extends ExportFileFilter {

		public HTMLExportFileFilter(UniFileFilter filter) {
			super(filter);
		}

		@Override
		public void write(BookmarkList main_bookmark_list, File fil) throws Exception {
			BookmarkList.exportAsHTML(main_bookmark_list, fil, CommonUtils.getInstance().getAppName(), CommonUtils.getInstance().getAppVersion());
		}
	}

	private class TextExportFileFilter extends ExportFileFilter {

		public TextExportFileFilter(UniFileFilter filter) {
			super(filter);
		}

		@Override
		public void write(BookmarkList main_bookmark_list, File fil) throws Exception {
			BookmarkList.exportAsTEXT(main_bookmark_list, fil, CommonUtils.getInstance().getAppName(), CommonUtils.getInstance().getAppVersion());
		}
	}
}
