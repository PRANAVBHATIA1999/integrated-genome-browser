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

package com.affymetrix.igb.bookmarks.action;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.MenuListener;

import com.affymetrix.igb.bookmarks.Bookmark;
import com.affymetrix.igb.bookmarks.BookmarkController;
import com.affymetrix.igb.bookmarks.BookmarkJMenuItem;
import com.affymetrix.igb.bookmarks.BookmarkList;
import com.affymetrix.igb.bookmarks.BookmarksParser;
import com.affymetrix.igb.bookmarks.Separator;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel.TabState;
import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenu;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenuItem;
import com.affymetrix.igb.bookmarks.BookmarkManagerViewGUI;

public final class BookmarkActionManager implements ActionListener, MenuListener {
   private final static boolean DEBUG = false;

  private final JRPMenu bookmark_menu;
  private final Map<Object,Component> component_hash = new HashMap<Object,Component>();
  private final BookmarkList main_bookmark_list = new BookmarkList("Bookmarks");
  private final JRPMenu main_bm_menu;
  private BookmarkManagerViewGUI bmvGUI = null;
  private IGBService igbService;

  private static BookmarkActionManager instance;

  public static void init(IGBService _igbService, JRPMenu bm_menu) {
    instance = new BookmarkActionManager(_igbService, bm_menu);
  }

  public static synchronized BookmarkActionManager getInstance() {
    return instance;
  }

  public BookmarkActionManager(IGBService _igbService, JRPMenu bm_menu) {
	igbService = _igbService;
    bookmark_menu = bm_menu;
    MenuUtil.addToMenu(bm_menu, new JRPMenuItem("Bookmark_add_pos", AddPositionBookmarkAction.getAction()));
    MenuUtil.addToMenu(bm_menu, new JRPMenuItem("Bookmark_add_data", AddPositionAndDataBookmarkAction.getAction()));
    bm_menu.addSeparator();
    MenuUtil.addToMenu(bm_menu, new JRPMenuItem("Bookmark_manage_bookmarks", ManageBookmarksAction.getAction()));
    MenuUtil.addToMenu(bm_menu, new JRPMenuItem("Bookmark_open_bookmark_tab", OpenBookmarkTabAction.getAction()));
    bm_menu.addSeparator();

    main_bm_menu = bm_menu;
    component_hash.put(main_bookmark_list, main_bm_menu);

    addDefaultBookmarks();
    buildMenus(main_bm_menu, main_bookmark_list);
  }

  public void setBmv(BookmarkManagerViewGUI bmvGUI) {
	this.bmvGUI = bmvGUI;
	bmvGUI.getBookmarkManagerView().setBList(main_bookmark_list);
  }

  public void updateBookmarkManager() {
    if (bmvGUI != null) bmvGUI.getBookmarkManagerView().setBList(main_bookmark_list);
  }

  public static File getBookmarksFile() {
    String app_dir = PreferenceUtils.getAppDataDirectory();
    File f = new File(app_dir, "bookmarks.html");
    return f;
  }

  /**
   *  Loads bookmarks from the file specified by {@link #getBookmarksFile()}.
   *  If loading succeeds, also creates a backup copy of that bookmark list
   *  in a new file with the same name, but "~" added at the end.
   */
	private void addDefaultBookmarks() {
		File f = getBookmarksFile();
		if (!f.exists()) {
			return;
		}

		String filename = f.getAbsolutePath();
		try {
			Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.INFO, "Loading bookmarks from file {0}", filename);
			BookmarksParser.parse(main_bookmark_list, f);

			if (main_bookmark_list != null && main_bookmark_list.getChildCount() != 0) {
				File f2 = new File(filename + "~");
				try {
					Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.INFO, "Creating backup bookmarks file: {0}", f2);
					BookmarkList.exportAsHTML(main_bookmark_list, f2, CommonUtils.getInstance().getAppName(), CommonUtils.getInstance().getAppVersion());
				} catch (Exception e) {
					Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.SEVERE, "Error while trying to create backup bookmarks file: {0}", f2);
				}
			}

		} catch (FileNotFoundException fnfe) {
			Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.SEVERE, "Could not load bookmarks. File not found or not readable: {0}", filename);
		} catch (IOException ioe) {
			Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.SEVERE, "Could not load bookmarks from file {0}", filename);
		}

	}

  /** Will save the current bookmarks into the file that was specified
   *  by {@link #getBookmarksFile()}.
   *  @return true for sucessfully saving the file
   */
  public boolean autoSaveBookmarks() {
    boolean saved = false;
    if (main_bookmark_list == null) {
      return saved;
    }
    File f = getBookmarksFile();
    String filename = f.getAbsolutePath();
    try {
      Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.INFO, "Saving bookmarks to file {0}", filename);
      File parent_dir = f.getParentFile();
      if (parent_dir != null) {
        parent_dir.mkdirs();
      }
      BookmarkList.exportAsHTML(main_bookmark_list, f, CommonUtils.getInstance().getAppName(), CommonUtils.getInstance().getAppVersion());
      saved = true;
    } catch (FileNotFoundException fnfe) {
      Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.SEVERE, "Could not auto-save bookmarks to {0}", filename);
    } catch (IOException ioe) {
      Logger.getLogger(BookmarkActionManager.class.getName()).log(Level.SEVERE, "Error while saving bookmarks to {0}", filename);
    }
    return saved;
  }


  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
     if (src instanceof BookmarkJMenuItem) {
      BookmarkJMenuItem item = (BookmarkJMenuItem) src;
      Bookmark bm = item.getBookmark();
      try {
        BookmarkController.viewBookmark(igbService, bm);
      } catch (Exception e) {
        ErrorHandler.errorPanel("Problem viewing bookmark", e);
      }
    } else if (DEBUG) {
      System.out.println("Got an action event from an unknown source: "+src);
      System.out.println("command: "+evt.getActionCommand());
    }
  }

  private void removeAllBookmarkMenuItems() {
    Iterator<Component> iter = component_hash.values().iterator();
    while (iter.hasNext()) {
      Component comp = iter.next();
      if (comp == main_bm_menu) {
        // component_hash contains a mapping of main_bookmark_list to main_bm_menu.
        // That is the only JRPMenu we do not want to remove from its parent.
        continue;
      }
      if (comp instanceof JRPMenuItem) {
        JRPMenuItem item = (JRPMenuItem) comp;
        ActionListener[] listeners = item.getActionListeners();
        for (int i=0; i<listeners.length; i++) {
          item.removeActionListener(listeners[i]);
        }
      } else { // if not a JRPMenuItem, should be a JSeparator
      }
      Container cont = comp.getParent();
      if (cont != null) {cont.remove(comp);}
    }
    component_hash.clear();
    component_hash.put(main_bookmark_list, main_bm_menu);
  }

  private void buildMenus(JRPMenu pp, BookmarkList bl) {
    JRPMenu bl_menu = (JRPMenu) component_hash.get(bl);
    if (bl_menu == null) {
      bl_menu = addBookmarkListMenu(pp, bl);
    }
    @SuppressWarnings("unchecked")
	Enumeration<BookmarkList> e = bl.children();
    while (e.hasMoreElements()) {
      BookmarkList node = e.nextElement();
      Object o = node.getUserObject();
      if (o instanceof String) {
        buildMenus(bl_menu, node);
      } else if (o instanceof Bookmark) {
        addBookmarkMI(bl_menu, (Bookmark) o);
      } else if (o instanceof Separator) {
        addSeparator(bl_menu, (Separator) o);
      }
    }
  }

  private JRPMenuItem addBookmarkMI(JRPMenu parent_menu, Bookmark bm) {
    JRPMenuItem markMI = (JRPMenuItem) component_hash.get(bm);
    if (markMI != null) {
      return markMI;
    }
    markMI = new BookmarkJMenuItem(getIdFromName(bm.getName()), bm);
    component_hash.put(bm, markMI);
    parent_menu.add(markMI);
    markMI.addActionListener(this);
    return markMI;
  }

  private String getIdFromName(String name) {
    String id = "";
    try {
    	id = "Bookmark_" + URLEncoder.encode("UTF-8", name);
    }
    catch (Exception x) {}
    return id;
  }

  private JRPMenu addBookmarkListMenu(JRPMenu parent_menu, BookmarkList bm_list) {
    JRPMenu sub_menu = (JRPMenu) component_hash.get(bm_list);
    if (sub_menu != null) {
      return sub_menu;
    }
    sub_menu = new JRPMenu(getIdFromName(bm_list.getName()),bm_list.getName());
    component_hash.put(bm_list,  sub_menu);
    parent_menu.add(sub_menu);
    return sub_menu;
  }

  private JSeparator addSeparator(JRPMenu parent_menu, Separator s) {
    JSeparator jsep = (JSeparator) component_hash.get(s);
    if (jsep != null) {
      return null;
    }
    jsep = new JSeparator();
    component_hash.put(s, jsep);
    parent_menu.add(jsep);
    return jsep;
  }

  /** Does nothing. */
  @Override
  public void menuCanceled(javax.swing.event.MenuEvent e) {
  }

  /** Does nothing. */
  @Override
  public void menuDeselected(javax.swing.event.MenuEvent e) {
  }

  /** Every time the menu is selected (thus opened) re-build the bookmark menu items.
   *  Thus if the bookmarks have been changed by the bookmark manager, we will
   *  adapt to that now.
   *  Slow? Yes.  Too slow?  Not really.
   */
  @Override
  public void menuSelected(javax.swing.event.MenuEvent e) {
    if (e.getSource()==bookmark_menu) {
      rebuildMenus();
    }
  }

  private void rebuildMenus() {
    removeAllBookmarkMenuItems();
    buildMenus(main_bm_menu, main_bookmark_list);
  }

  public BookmarkList getMainBookmarkList() {
	  return main_bookmark_list;
  }

  public BookmarkManagerViewGUI getBookmarkManagerViewGUI() {
	  return bmvGUI;
  }

  public Map<Object,Component> getComponentHash() {
	  return component_hash;
  }

  public SeqSpan getVisibleSpan() {
	  return igbService.getSeqMapView().getVisibleSpan();
  }

  public void setTabState(TabState tabState) {
      igbService.setTabStateAndMenu(bmvGUI, tabState);
  }
}
