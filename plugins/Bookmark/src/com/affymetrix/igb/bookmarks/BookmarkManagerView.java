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
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
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

/**
 *  A panel for viewing and re-arranging bookmarks in a hierarchy.
 */
public final class BookmarkManagerView extends IGBTabPanel implements TreeSelectionListener {

  private static final long serialVersionUID = 1L;
  private static final int TAB_POSITION = 9;
  private static JFileChooser static_chooser = null;

  public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bookmark");
  private JTree tree;
  private BottomThing thing;

  private final DefaultTreeModel tree_model = new DefaultTreeModel(null, true);

  // refresh_action is an action that is useful during debugging, but should go away later.
  private final Action import_action;
  private final Action export_action;
  private final Action delete_action;
  private final Action add_separator_action;
  private final Action add_folder_action;
  private final Action add_bookmark_action;
  private final Action forward_action;
  private final Action backward_action;
  private List<TreePath> bookmark_history;
  private int   history_pointer = -1;

  private final BookmarkTreeCellRenderer renderer;


  protected int last_selected_row = -1;  // used by dragUnderFeedback()

  /** Creates a new instance of Class */
  public BookmarkManagerView(IGBService igbService) {
    super(igbService, BUNDLE.getString("bookmarksTab"), BUNDLE.getString("bookmarksTab"), false, TAB_POSITION);

    tree = new DragDropTree();
    tree.setModel(tree_model);
    bookmark_history = new ArrayList<TreePath>();

    JScrollPane scroll_pane = new JScrollPane(tree);

    this.setLayout(new BorderLayout());
    scroll_pane.setMinimumSize(new Dimension(50,50));
    this.add(scroll_pane, BorderLayout.CENTER);

    thing = new BottomThing(tree);
    thing.setIGBService(igbService);
    this.add(thing, BorderLayout.SOUTH);
    tree.addTreeSelectionListener(thing);

    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
    tree.setRootVisible(true);
    tree.setShowsRootHandles(true);
    tree.setEditable(false); // too much work to allow direct editing of item names

    renderer = new BookmarkTreeCellRenderer();
    tree.setCellRenderer(renderer);

    ToolTipManager.sharedInstance().registerComponent(tree);

    // Would love to try using setDragEnabled(true) because this gives a better
    // selection mechanism. But that would involve a re-write of much of this code
    // adding a custom TransferHandler.
    //tree.setDragEnabled(true);

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

    setUpMenuBar();
    ImageIcon test_icon = MenuUtil.getIcon("toolbarButtonGraphics/general/Properties16.gif");
    if (test_icon != null) {
      // Only use the toolbar if the icons are available.
      // Otherwise the toolbar just looks stupid.
      setUpToolBar();
    }
    setUpPopupMenu();

    tree.addTreeSelectionListener(this);
    this.validate();
  }

  @Override
  public TabState getDefaultState() {
    return TabState.COMPONENT_STATE_HIDDEN;
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
    for (int i = nodes.length-1; i >= 0; i--) {
      DefaultMutableTreeNode node = nodes[i];
      try {
        ((DefaultTreeModel) tree.getModel()).insertNodeInto(node, parent, my_index+1);
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
    KeyStroke ks = PreferenceUtils.getAccelerator("Bookmark Manager / "+a.getValue(Action.NAME));
    a.putValue(Action.ACCELERATOR_KEY, ks);
  }


  /** A JPanel that listens for TreeSelectionEvents, displays
   *  the name(s) of the selected item(s), and may allow you to edit them.
   */
  private class BottomThing extends JPanel implements TreeSelectionListener, ActionListener, FocusListener {
	private static final long serialVersionUID = 1L;
    JLabel type_label = new JLabel("Type:");
    JLabel type_label_2 = new JLabel("");
    JLabel name_label = new JLabel("Name:");
    JRPTextField name_text_field = new JRPTextField("BookmarkManagerView_name_text_field", 30);
    BookmarkListEditor bl_editor;
    TreePath selected_path = null;
    BookmarkList selected_bl = null;

    private final JTree tree;
    private IGBService igbService = null;
    private final DefaultTreeModel def_tree_model;

    Action properties_action;
    Action goto_action;

    BottomThing(JTree tree) {
      if (tree==null) throw new IllegalArgumentException();

      this.tree = tree;
      this.def_tree_model = (DefaultTreeModel) tree.getModel();

      properties_action = makePropertiesAction();
      properties_action.setEnabled(false);
      goto_action = makeGoToAction();
      goto_action.setEnabled(false);

      Box type_box = new Box(BoxLayout.X_AXIS);
      type_box.add(type_label);
      type_box.add(Box.createHorizontalStrut(5));
      type_box.add(type_label_2);
      type_box.add(Box.createHorizontalGlue());

      Box name_box = new Box(BoxLayout.X_AXIS);
      name_box.add(name_label);
      name_box.add(Box.createHorizontalStrut(5));
      name_box.add(name_text_field);
      this.name_text_field.setEnabled(false);
      name_text_field.addActionListener(this);
      name_text_field.addFocusListener(this);

      Box button_box = new Box(BoxLayout.X_AXIS);
      button_box.add(Box.createHorizontalGlue());
      JRPButton edit_button = new JRPButton("BookmarkManagerView_edit_button", properties_action);
      button_box.add(edit_button);
      button_box.add(Box.createHorizontalStrut(5));
      JRPButton goto_button = new JRPButton("BookmarkManagerView_goto_button", goto_action);
      button_box.add(goto_button);
      button_box.add(Box.createHorizontalGlue());

      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(type_box);
      this.add(name_box);
      this.add(button_box);

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
      Object source = e.getSource();
      assert source == tree;
      if (source != tree) {
        return;
      }

      TreePath[] selections = tree.getSelectionPaths();
      type_label_2.setText("");
      name_text_field.setText("");
      if (selections == null || selections.length != 1) {
        name_text_field.setText("");
        name_text_field.setEnabled(false);
        properties_action.setEnabled(false);
        goto_action.setEnabled(false);
        return;
      }
      else {
        selected_path = selections[0];
        selected_bl = (BookmarkList) selected_path.getLastPathComponent();
        Object user_object = selected_bl.getUserObject();
        //bl_editor.setBookmarkList(selected_bl);
        if (user_object instanceof Bookmark) {
          Bookmark bm = (Bookmark) user_object;
          if (!bm.isUnibrowControl()) {
            type_label_2.setText("External Bookmark");
          } else {
            type_label_2.setText("Internal Bookmark");
          }
          name_text_field.setText(bm.getName());
          name_text_field.setEnabled(true);
          properties_action.setEnabled(true);
          goto_action.setEnabled(igbService != null);
        } else if (user_object instanceof Separator) {
          type_label_2.setText("Separator");
          name_text_field.setText("Separator");
          name_text_field.setEnabled(false);
          properties_action.setEnabled(false);
          goto_action.setEnabled(false);
        } else {
          type_label_2.setText("Bookmark List");
          name_text_field.setText(user_object.toString());
          // don't allow editing the root bookmark list name: see rename()
          name_text_field.setEnabled(selected_bl != def_tree_model.getRoot());
          properties_action.setEnabled(selected_bl != def_tree_model.getRoot());
          goto_action.setEnabled(false);
        }
      }
    }

    public void rename(BookmarkList bl, String name) {
      if (bl == def_tree_model.getRoot()) {
        // I do not allow re-naming the root node because the current BookmarkParser
        // class cannot actually read the name of a bookmark list, so any
        // name change would be lost after saving and re-loading.
        return;
      }
      if (name == null || name.length()==0) return;
      Object user_object = selected_bl.getUserObject();
      if (user_object instanceof Bookmark) {
        Bookmark bm = (Bookmark) user_object;
        bm.setName(name);
        def_tree_model.nodeChanged(bl);
      } else if (user_object instanceof String) {
        selected_bl.setUserObject(name);
        def_tree_model.nodeChanged(bl);
      }
    }

    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == name_text_field && e.getID() == ActionEvent.ACTION_PERFORMED) {
        rename(selected_bl, name_text_field.getText());
      }
    }

    public void focusGained(FocusEvent e) {
    }

    /** Allows renaming the selected BookmarkList when the text field loses
     *  focus due to tabbing.
     */
    public void focusLost(FocusEvent e) {
      if (e.getSource() == name_text_field) {
        //System.out.println("Lost focus! "+name_text_field.getText());
        rename(selected_bl, name_text_field.getText());
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
            ImageIcon icon = MenuUtil.getIcon("toolbarButtonGraphics/general/Properties16.gif");
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
			return "toolbarButtonGraphics/general/Properties16.gif";
		}
		@Override
		public int getShortcut() {
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
          if (igbService==null || selected_bl == null || !(selected_bl.getUserObject() instanceof Bookmark)) {
            setEnabled(false);
          } else {
            Bookmark bm = (Bookmark) selected_bl.getUserObject();
            addBookmarkToHistory(tree.getSelectionPath());
            BookmarkController.viewBookmark(igbService, bm);
          }
        }
		@Override
		public String getText() {
			return "Go To";
		}
		@Override
		public String getIconPath() {
			return "toolbarButtonGraphics/media/Play16.gif";
		}
		@Override
		public int getShortcut() {
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
  }

  public void valueChanged(TreeSelectionEvent e) {
    if (e.getSource() != tree) return;
    int selections = tree.getSelectionCount();
    delete_action.setEnabled(selections != 0);
    add_separator_action.setEnabled(selections != 0);
    add_folder_action.setEnabled(selections != 0);
    add_bookmark_action.setEnabled(selections != 0);
    //  the "properties" and "go to" actions belong to the BottomThing and it will enable or disable them
  }

  private void setUpMenuBar() {
    JMenuBar menu_bar = new JMenuBar();
    JMenu bookmarks_menu = new JMenu("Bookmarks") {
  	  private static final long serialVersionUID = 1L;
			@Override
      public JMenuItem add(Action a) {
        JMenuItem menu_item = super.add(a);
        menu_item.setToolTipText(null);
        return menu_item;
      }
    };
    bookmarks_menu.setMnemonic('B');

    bookmarks_menu.add(add_bookmark_action);
    bookmarks_menu.add(add_folder_action);
    bookmarks_menu.add(add_separator_action);
    bookmarks_menu.addSeparator();
    bookmarks_menu.add(thing.getPropertiesAction());
    bookmarks_menu.add(thing.getGoToAction());
    bookmarks_menu.addSeparator();
    bookmarks_menu.add(delete_action);
    bookmarks_menu.addSeparator();
    bookmarks_menu.add(import_action);
    bookmarks_menu.add(export_action);
    bookmarks_menu.add(forward_action);
    bookmarks_menu.add(backward_action);

    menu_bar.add(bookmarks_menu);
    this.add(menu_bar, BorderLayout.NORTH);
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
    popup.add(add_bookmark_action);
    popup.add(add_folder_action);
    popup.add(add_separator_action);
    popup.addSeparator();
    popup.add(thing.getPropertiesAction());
    popup.add(thing.getGoToAction());
    popup.addSeparator();
    popup.add(delete_action);
    popup.addSeparator();
    popup.add(import_action);
    popup.add(export_action);
    MouseAdapter mouse_adapter = new MouseAdapter() {
			@Override
      public void mousePressed(MouseEvent e) {
		if(processDoubleClick(e))
			return;

        if (popup.isPopupTrigger(e)) {
          popup.show(tree, e.getX(), e.getY());
        }
      }
			@Override
      public void mouseReleased(MouseEvent e) {
		if(processDoubleClick(e))
			return;

        if (popup.isPopupTrigger(e)) {
          popup.show(tree, e.getX(), e.getY());
        }
      }

	  private boolean processDoubleClick(MouseEvent e) {
		  if(e.getClickCount() != 2)
			return false;

		  thing.getGoToAction().actionPerformed(null);

		  return true;
	  }
    };
    tree.addMouseListener(mouse_adapter);
  }

  private void setUpToolBar() {
    JToolBar tool_bar = new JToolBar(JToolBar.VERTICAL);
    tool_bar.setFloatable(false);
    tool_bar.add(add_folder_action);
    tool_bar.add(add_separator_action);
    tool_bar.add(add_bookmark_action);
    tool_bar.addSeparator();
    tool_bar.add(thing.getPropertiesAction());
    tool_bar.add(thing.getGoToAction());
    tool_bar.addSeparator();
    tool_bar.add(delete_action);
    tool_bar.addSeparator();
    tool_bar.add(import_action);
    tool_bar.add(export_action);
    tool_bar.add(backward_action);
    tool_bar.add(forward_action);
    this.add(tool_bar, BorderLayout.WEST);
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
		public int getShortcut() {
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
      }
      catch (Exception ex) {
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
			return "toolbarButtonGraphics/general/Import16.gif";
		}
		@Override
		public int getShortcut() {
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
    if (main_bookmark_list == null || main_bookmark_list.getChildCount()==0) {
      ErrorHandler.errorPanel(frame, "Error", "No bookmarks to save", (Exception)null);
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

        if ((! full_path.endsWith(".html"))
         && (! full_path.endsWith(".htm"))
         && (! full_path.endsWith(".xhtml"))) {
          fil = new File(full_path + ".html");
        }

        BookmarkList.exportAsHTML(main_bookmark_list, fil, CommonUtils.getInstance().getAppName(), CommonUtils.getInstance().getAppVersion());
      }
      catch (Exception ex) {
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
			return "toolbarButtonGraphics/general/Export16.gif";
		}
		@Override
		public int getShortcut() {
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
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					if (node.getParent() != null) {
						tree_model.removeNodeFromParent(node);
						removeBookmarkFromHistory(path);
					}
				}
			}
		}
		@Override
		public String getText() {
			return "Delete ...";
		}
		@Override
		public String getIconPath() {
			return "toolbarButtonGraphics/general/Delete16.gif";
		}
		@Override
		public int getShortcut() {
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
    if (type==0) {
      title = "New Separator";
      // "RowDelete" looks vaguely like a separator...
      iconPath = "toolbarButtonGraphics/table/RowDelete16.gif";
      tool_tip = "New Separator";
      mnemonic = KeyEvent.VK_S;
    } else if (type==1) {
      title = "New Folder";
      // the "Open" icon looks like a folder...
      iconPath = "toolbarButtonGraphics/general/Open16.gif";
      tool_tip = "New Folder";
      mnemonic = KeyEvent.VK_F;
    } else if (type==2) {
      title = "New Bookmark";
      iconPath = "toolbarButtonGraphics/general/Bookmarks16.gif";
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
					Bookmark b = new Bookmark("Bookmark",
							Bookmark.constructURL(Collections
									.<String, String[]> emptyMap()));
					bl = new BookmarkList(b);
				} catch (MalformedURLException mue) {
					mue.printStackTrace();
				}
			}
			if (bl != null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) bl;
				insert(tree, path, new DefaultMutableTreeNode[] { node });
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
		public int getShortcut() {
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
					BookmarkController.viewBookmark(igbService, bm);
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
				return "toolbarButtonGraphics/media/FastForward16.gif";
			}
			@Override
			public int getShortcut() {
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
					BookmarkController.viewBookmark(igbService, bm);
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
				return "toolbarButtonGraphics/media/Rewind16.gif";
			}

			@Override
			public int getShortcut() {
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
        new String[] {"html", "htm", "xhtml"}, "HTML Files");
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
    }
    else if (tree.isExpanded(path)) {
      into = true;
    }
    else {
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

  @Override
  public boolean isEmbedded() {
    return true;
  }
}
