/**
 *   Copyright (c) 2001-2006 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.bookmarks;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.util.UniFileFilter;
import com.affymetrix.genoviz.swing.DragDropTree;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.shared.FileTracker;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import javax.swing.undo.UndoManager;

/**
 *  A panel for viewing and re-arranging bookmarks in a hierarchy.
 */
public final class BookmarkManagerView implements TreeSelectionListener {

	private static final long serialVersionUID = 1L;
	//private static final int TAB_POSITION = 9;
	private static JFileChooser static_chooser = null;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bookmark");
	public JTree tree;
	public BottomThing thing;
	private final DefaultTreeModel tree_model = new DefaultTreeModel(null, true);
	// refresh_action is an action that is useful during debugging, but should go away later.
	public final Action import_action;
	public final Action export_action;
	public final Action delete_action;
	public final Action add_separator_action;
	public final Action add_folder_action;
	public final Action add_bookmark_action;
	public final Action forward_action;
	public final Action backward_action;
	private List<TreePath> bookmark_history;
	private int history_pointer = -1;
	private final BookmarkTreeCellRenderer renderer;
	private static BookmarkManagerView singleton;
	protected int last_selected_row = -1;  // used by dragUnderFeedback()

	public static void init(IGBService _igbService) {
		singleton = new BookmarkManagerView(_igbService);
	}

	public static synchronized BookmarkManagerView getSingleton() {
		return singleton;
	}

	/** Creates a new instance of Class */
	public BookmarkManagerView(IGBService igbService) {

		tree = new DragDropTree();
		tree.setModel(tree_model);
		bookmark_history = new ArrayList<TreePath>();

		JScrollPane scroll_pane = new JScrollPane(tree);

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
		add_separator_action = makeAddAction(tree, 0);
		add_folder_action = makeAddAction(tree, 1);
		add_bookmark_action = makeAddAction(tree, 2);
		forward_action = makeForwardAction();
		backward_action = makeBackwardAction();
		forward_action.setEnabled(false);
		backward_action.setEnabled(false);

		setUpPopupMenu();

		tree.addTreeSelectionListener(this);
	}

	private boolean insert(JTree tree, TreePath tree_path, DefaultMutableTreeNode[] nodes) {
		if (tree_path == null) {
			return false;
		}
		DefaultMutableTreeNode tree_node = (DefaultMutableTreeNode) tree_path.getLastPathComponent();
		if (tree_node == null) {
			return false;
		}

		// Highlight the drop location while we perform the drop
		tree.setSelectionPath(tree_path);

		DefaultMutableTreeNode parent = null;
		int row = tree.getRowForPath(tree_path);
		if (tree_node.getAllowsChildren() && dropInto(row)) {
			parent = tree_node;
		} else {
			parent = (DefaultMutableTreeNode) tree_node.getParent();
		}
		int my_index = 0;
		if (parent != null) {
			my_index = parent.getIndex(tree_node);
		} else if (tree_node.isRoot()) {
			parent = tree_node;
			my_index = -1;
		}

		// Copy or move each source object to the target
		// if we count backwards, we can always add new nodes at (my_index + 1)
		for (int i = nodes.length - 1; i >= 0; i--) {
			DefaultMutableTreeNode node = nodes[i];
			try {
				((DefaultTreeModel) tree.getModel()).insertNodeInto(node, parent, my_index + 1);
			} catch (IllegalStateException e) {
				// Cancelled by user
				return false;
			}
		}

		return true;
	}

	public void setBList(BookmarkList blist) {
		tree_model.setRoot(blist);
		// selecting, then clearing the selection, makes sure that valueChanged() gets called.
		tree.setSelectionRow(0);
		tree.clearSelection();
	}

	private static void setAccelerator(Action a) {
		KeyStroke ks = PreferenceUtils.getAccelerator("Bookmark Manager / " + a.getValue(Action.NAME));
		a.putValue(Action.ACCELERATOR_KEY, ks);
	}

	/** A JPanel that listens for TreeSelectionEvents, displays
	 *  the name(s) of the selected item(s), and may allow you to edit them.
	 */
	public class BottomThing extends JPanel implements TreeSelectionListener {

		private static final long serialVersionUID = 1L;
		JLabel name_label = new JLabel("Name:");
		public JRPTextField name_text_field = new JRPTextField("BookmarkManagerView_name_text_area");
		public javax.swing.JTextArea comment_text_area = new javax.swing.JTextArea();
		BookmarkListEditor bl_editor;
		TreePath selected_path = null;
		BookmarkList selected_bl = null;
		BookmarkList previousSelected_bl = null;
		private final JTree tree;
		private IGBService igbService = null;
		public final DefaultTreeModel def_tree_model;
		Action properties_action;
		Action goto_action;
		UndoManager undoNameManager = new UndoManager();
		UndoManager undoCommentManager = new UndoManager();

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
			this.name_text_field.getDocument().addUndoableEditListener(undoNameManager);
			this.comment_text_area.setEnabled(false);
			this.comment_text_area.getDocument().addUndoableEditListener(undoCommentManager);

			bl_editor = new BookmarkListEditor(def_tree_model);
		}

		/** Sets the instance of IGBService.  This is the instance
		 *  in which the bookmarks will be opened when the "GoTo" button
		 *  is pressed.
		 *  @param igbService an instance of IGBService; null is ok.
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
			if (selections == null || selections.length != 1) {
				name_text_field.setText("");
				comment_text_area.setText("");
				comment_text_area.setEnabled(false);
				name_text_field.setEnabled(false);
				properties_action.setEnabled(false);
				goto_action.setEnabled(false);
				return;
			} else {
				selected_path = selections[0];
				selected_bl = (BookmarkList) selected_path.getLastPathComponent();
				Object user_object = selected_bl.getUserObject();
				//bl_editor.setBookmarkList(selected_bl);
				if (user_object instanceof Bookmark) {
					Bookmark bm = (Bookmark) user_object;

					name_text_field.setText(bm.getName());
					comment_text_area.setText(bm.getComment());
					comment_text_area.setEnabled(true);
					name_text_field.setEnabled(true);
					properties_action.setEnabled(true);
					goto_action.setEnabled(igbService != null);
				} else if (user_object instanceof Separator) {
					name_text_field.setText("Separator");
					comment_text_area.setText("Uneditable");
					comment_text_area.setEnabled(false);
					name_text_field.setEnabled(false);
					properties_action.setEnabled(false);
					goto_action.setEnabled(false);
				} else {
					name_text_field.setText(user_object.toString());
					// don't allow editing the root bookmark list name: see rename()
					name_text_field.setEnabled(selected_bl != def_tree_model.getRoot());
					properties_action.setEnabled(selected_bl != def_tree_model.getRoot());
					goto_action.setEnabled(false);
					comment_text_area.setText("Uneditable");
					comment_text_area.setEnabled(false);
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
				// I do not allow re-naming the root node because the current BookmarkParser
				// class cannot actually read the name of a bookmark list, so any
				// name change would be lost after saving and re-loading.
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
				def_tree_model.nodeChanged(bl);
			} else if (user_object instanceof String) {
				selected_bl.setUserObject(name);
				def_tree_model.nodeChanged(bl);
			}
		}

		public Action getPropertiesAction() {
			return properties_action;
		}

		private Action makePropertiesAction() {
			Action a = new GenericAction() {

				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent ae) {
					super.actionPerformed(ae);
					if (selected_bl == null || selected_bl.getUserObject() instanceof Separator) {
						setEnabled(false);
					} else {
						ImageIcon icon = CommonUtils.getInstance().getIcon("images/properties16.png");
						Image image = null;

						if (icon == null) {
							JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, tree);
							if (frame != null) {
								image = frame.getIconImage();
							}
						} else {
							image = icon.getImage();
						}

						if (image != null) {
							bl_editor.setIconImage(image);
						}

						bl_editor.openDialog(selected_bl);
					}
				}

				@Override
				public String getText() {
					return "Properties ...";
				}

				@Override
				public String getIconPath() {
					return "images/properties16.png";
				}

				@Override
				public int getMnemonic() {
					return KeyEvent.VK_P;
				}

				@Override
				public String getTooltip() {
					return "Properties";
				}
			};
			setAccelerator(a);
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

				@Override
				public String getIconPath() {
					return "images/play16.png";
				}

				@Override
				public int getMnemonic() {
					return KeyEvent.VK_G;
				}

				@Override
				public String getTooltip() {
					return "Go To Bookmark";
				}
			};
			setAccelerator(a);
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

	public void valueChanged(TreeSelectionEvent e) {
		if (e.getSource() != tree) {
			return;
		}
		int selections = tree.getSelectionCount();
		delete_action.setEnabled(selections != 0);
		add_separator_action.setEnabled(selections != 0);
		add_folder_action.setEnabled(selections != 0);
		add_bookmark_action.setEnabled(selections != 0);
		//  the "properties" and "go to" actions belong to the BottomThing and it will enable or disable them
	}

	private void setUpPopupMenu() {
		final JPopupMenu popup = new JPopupMenu() {

			private static final long serialVersionUID = 1L;

			@Override
			public JMenuItem add(Action a) {
				JMenuItem menu_item = super.add(a);
				menu_item.setToolTipText(null);
				return menu_item;
			}
		};

		popup.add(delete_action);
		popup.addSeparator();
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

			@Override
			public void mouseReleased(MouseEvent e) {
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

	Action makeRefreshAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				tree_model.reload();
			}

			@Override
			public String getText() {
				return "Refresh";
			}

			@Override
			public String getIconPath() {
				return "toolbarButtonGraphics/general/Refresh16.gif";
			}

			@Override
			public int getMnemonic() {
				return KeyEvent.VK_R;
			}

			@Override
			public String getTooltip() {
				return "Refresh";
			}
		};
		setAccelerator(a);
		return a;
	}

	/**
	 *  Tries to import bookmarks into Unibrow.
	 *  Makes use of {@link BookmarksParser#parse(BookmarkList, File)}.
	 */
	private void importBookmarks(BookmarkList bookmark_list, JFrame frame) {
		JFileChooser chooser = getJFileChooser();
		chooser.setCurrentDirectory(getLoadDirectory());
		int option = chooser.showOpenDialog(frame);
		if (option == JFileChooser.APPROVE_OPTION) {
			setLoadDirectory(chooser.getCurrentDirectory());
			try {
				File fil = chooser.getSelectedFile();
				BookmarksParser.parse(bookmark_list, fil);
			} catch (Exception ex) {
				ErrorHandler.errorPanel(frame, "Error", "Error importing bookmarks", ex);
			}
		}
	}

	Action makeImportAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				BookmarkList bl = (BookmarkList) tree_model.getRoot();
				importBookmarks(bl, null);
				tree_model.reload();
			}

			@Override
			public String getText() {
				return "Import ...";
			}

			@Override
			public String getIconPath() {
				return "images/import.png";
			}

			@Override
			public int getMnemonic() {
				return KeyEvent.VK_I;
			}

			@Override
			public String getTooltip() {
				return "Import Bookmarks";
			}
		};
		setAccelerator(a);
		return a;
	}

	private void exportBookmarks(BookmarkList main_bookmark_list, JFrame frame) {
		if (main_bookmark_list == null || main_bookmark_list.getChildCount() == 0) {
			ErrorHandler.errorPanel(frame, "Error", "No bookmarks to save", (Exception) null);
			return;
		}
		JFileChooser chooser = getJFileChooser();
		chooser.setCurrentDirectory(getLoadDirectory());
		int option = chooser.showSaveDialog(frame);
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				setLoadDirectory(chooser.getCurrentDirectory());
				File fil = chooser.getSelectedFile();
				String full_path = fil.getCanonicalPath();

				if ((!full_path.endsWith(".html"))
						&& (!full_path.endsWith(".htm"))
						&& (!full_path.endsWith(".xhtml"))) {
					fil = new File(full_path + ".html");
				}

				BookmarkList.exportAsHTML(main_bookmark_list, fil, CommonUtils.getInstance().getAppName(), CommonUtils.getInstance().getAppVersion());
			} catch (Exception ex) {
				ErrorHandler.errorPanel(frame, "Error", "Error exporting bookmarks", ex);
			}
		}
	}

	Action makeExportAction() {
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

			@Override
			public String getIconPath() {
				return "images/export.png";
			}

			@Override
			public int getMnemonic() {
				return KeyEvent.VK_E;
			}

			@Override
			public String getTooltip() {
				return "Export Bookmarks";
			}
		};
		setAccelerator(a);
		return a;
	}

	Action makeDeleteAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				deleteAction();
			}

			@Override
			public String getText() {
				return "Delete ...";
			}

			@Override
			public String getIconPath() {
				return "images/removeBookmark16.png";
			}

			@Override
			public int getMnemonic() {
				return KeyEvent.VK_D;
			}

			@Override
			public String getTooltip() {
				return "Delete Selected Bookmark(s)";
			}
		};
		setAccelerator(a);
		return a;
	}

	private void deleteAction() {
		TreePath[] paths = tree.getSelectionPaths();
		if (paths == null) {
			return;
		}
		Container frame = SwingUtilities.getAncestorOfClass(JFrame.class, tree);
		int yes = JOptionPane.showConfirmDialog(frame, "Delete these "
				+ paths.length + " selected bookmarks?", "Delete?",
				JOptionPane.YES_NO_OPTION);
		if (yes == JOptionPane.YES_OPTION) {
			for (int i = 0; i < paths.length; i++) {
				TreePath path = paths[i];
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.getParent() != null) {
					tree_model.removeNodeFromParent(node);
					removeBookmarkFromHistory(path);
				}
			}
		}
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
		bookmark_history.add(tp);
		history_pointer = bookmark_history.size() - 1;
		forward_action.setEnabled(false);
		backward_action.setEnabled(bookmark_history.size() > 1);
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
			forward_action.setEnabled(history_pointer < bookmark_history.size() - 1);
			backward_action.setEnabled(history_pointer > 0);
		}
	}

	private Action makeAddAction(final JTree tree, final int type) {
		final String title;
		final String iconPath;
		final String tool_tip;
		final int mnemonic;
		if (type == 0) {
			title = "New Separator";
			// "RowDelete" looks vaguely like a separator...
			iconPath = "images/separator16.png";
			tool_tip = "New Separator";
			mnemonic = KeyEvent.VK_S;
		} else if (type == 1) {
			title = "New Folder";
			// the "Open" icon looks like a folder...
			iconPath = "images/addFolder.png";
			tool_tip = "New Folder";
			mnemonic = KeyEvent.VK_F;
		} else if (type == 2) {
			title = "New Bookmark";
			iconPath = "images/addBookmark.png";
			tool_tip = "New Bookmark";
			mnemonic = KeyEvent.VK_N;
		} else {
			title = "New ???";
			iconPath = null;
			tool_tip = null;
			mnemonic = KeyEvent.VK_EXCLAMATION_MARK;
		}

		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				TreePath path = tree.getSelectionModel().getSelectionPath();
				if (path == null) {
					Logger.getLogger(BookmarkManagerView.class.getName()).log(
							Level.SEVERE, "No selection");
					return;
				}
				BookmarkList bl = null;
				if (type == 0) {
					Separator s = new Separator();
					bl = new BookmarkList(s);
				} else if (type == 1) {
					bl = new BookmarkList("Folder");
				} else if (type == 2) {
					try {
						Bookmark b = new Bookmark("Bookmark", "",
								Bookmark.constructURL(Collections.<String, String[]>emptyMap()));
						bl = new BookmarkList(b);
					} catch (MalformedURLException mue) {
						mue.printStackTrace();
					}
				}
				if (bl != null) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) bl;
					insert(tree, path, new DefaultMutableTreeNode[]{node});
				}
			}

			@Override
			public String getText() {
				return title;
			}

			@Override
			public String getIconPath() {
				return iconPath;
			}

			@Override
			public int getMnemonic() {
				return Integer.valueOf(mnemonic);
			}

			@Override
			public String getTooltip() {
				return tool_tip;
			}
		};
		setAccelerator(a);
		return a;
	}

	/**
	 * Action to move forward in the Bookmark History
	 * 
	 * @return the Action forward
	 */
	public Action makeForwardAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				if (history_pointer < bookmark_history.size() - 1) {
					history_pointer++;
					TreePath path = bookmark_history.get(history_pointer);
					tree.setSelectionPath(path);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					Bookmark bm = (Bookmark) node.getUserObject();
					BookmarkController.viewBookmark(thing.igbService, bm);
					forward_action.setEnabled(history_pointer < bookmark_history.size() - 1);
					backward_action.setEnabled(true);
				}
			}

			@Override
			public String getText() {
				return "Forward";
			}

			@Override
			public String getIconPath() {
				return "images/forward16.png";
			}

			@Override
			public int getMnemonic() {
				return KeyEvent.VK_F;
			}

			@Override
			public String getTooltip() {
				return "Forward";
			}
		};
		setAccelerator(a);
		return a;
	}

	/**
	 * Action to move backward in the Bookmark History
	 * 
	 * @return the Action backward
	 */
	public Action makeBackwardAction() {
		Action a = new GenericAction() {

			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent ae) {
				super.actionPerformed(ae);
				if (history_pointer > 0) {
					history_pointer--;
					TreePath path = bookmark_history.get(history_pointer);
					tree.setSelectionPath(path);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					Bookmark bm = (Bookmark) node.getUserObject();
					BookmarkController.viewBookmark(thing.igbService, bm);
					forward_action.setEnabled(true);
					backward_action.setEnabled(history_pointer > 0);
				}
			}

			@Override
			public String getText() {
				return "Backward";
			}

			@Override
			public String getIconPath() {
				return "images/backward16.png";
			}

			@Override
			public int getMnemonic() {
				return KeyEvent.VK_B;
			}

			@Override
			public String getTooltip() {
				return "Backward";
			}
		};
		setAccelerator(a);
		return a;
	}

	public File getLoadDirectory() {
		return FileTracker.DATA_DIR_TRACKER.getFile();
	}

	public void setLoadDirectory(File file) {
		FileTracker.DATA_DIR_TRACKER.setFile(file);
	}

	/** Gets a static re-usable file chooser that prefers "html" files. */
	private JFileChooser getJFileChooser() {
		if (static_chooser == null) {
			static_chooser = new JFileChooser();
			static_chooser.setCurrentDirectory(getLoadDirectory());
			UniFileFilter filter = new UniFileFilter(
					new String[]{"html", "htm", "xhtml"}, "HTML Files");
			static_chooser.addChoosableFileFilter(filter);
		}
		static_chooser.rescanCurrentDirectory();
		return static_chooser;
	}

	/** Returns true or false to indicate that if an item is inserted at
	 *  the given row it will be inserted "into" (true) or "after" (false)
	 *  the item currently at that row.  Will return true only if the given
	 *  row contains a folder and that folder is currently expanded or empty
	 *  or is the root node.
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
			if (node.getAllowsChildren() && node.getChildCount() == 0) {
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
}
