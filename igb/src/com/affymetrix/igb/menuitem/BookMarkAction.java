/**
*   Copyright (c) 2001-2004 Affymetrix, Inc.
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

package com.affymetrix.igb.menuitem;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.MenuListener;

import com.affymetrix.genoviz.bioviews.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.*;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.util.SynonymLookup;

import com.affymetrix.igb.tiers.*;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.parsers.*;

import com.affymetrix.igb.util.UniFileFilter;

import com.affymetrix.igb.bookmarks.*;
import com.affymetrix.igb.util.UnibrowPrefsUtil;
import com.affymetrix.igb.view.BookmarkManagerView;

public class BookMarkAction implements ActionListener, MenuListener {
  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  final static boolean DEBUG = false;

  JMenu bookmark_menu;
  JMenuItem add_pos_markMI;
  JMenuItem add_graph_markMI;
  JMenuItem exportMI;
  JMenuItem importMI;
  JMenuItem clearMI;
  SeqMapView gviewer;
  IGB uni;
  private Map component_hash = new HashMap();
  BookmarkList main_bookmark_list = new BookmarkList("Bookmarks");
  JMenu main_bm_menu;
  boolean unsaved_bookmarks = false;
  BookmarkManagerView bmv = null;

  public BookMarkAction(IGB unib, SeqMapView smv, JMenu bm_menu) {
    uni = unib;
    gviewer = smv;
    bookmark_menu = bm_menu;
    bookmark_menu.addMenuListener(this);
    add_pos_markMI = new JMenuItem("Add Position Bookmark", KeyEvent.VK_P);
    add_graph_markMI = new JMenuItem("Add Position & Graphs Bookmark", KeyEvent.VK_G);
    exportMI = new JMenuItem("Export Bookmarks", KeyEvent.VK_E);
    importMI = new JMenuItem("Import Bookmarks", KeyEvent.VK_I);
    clearMI = new JMenuItem("Clear Bookmarks", KeyEvent.VK_C);

    add_pos_markMI.addActionListener(this);
    add_graph_markMI.addActionListener(this);
    importMI.addActionListener(this);
    exportMI.addActionListener(this);
    clearMI.addActionListener(this);
    MenuUtil.addToMenu(bm_menu, add_pos_markMI);
    MenuUtil.addToMenu(bm_menu, add_graph_markMI);
    bm_menu.addSeparator();
    MenuUtil.addToMenu(bm_menu, exportMI);
    MenuUtil.addToMenu(bm_menu, importMI);
    MenuUtil.addToMenu(bm_menu, clearMI);
    bm_menu.addSeparator();

    main_bm_menu = bm_menu;
    component_hash.put(main_bookmark_list, main_bm_menu);

    addDefaultBookmarks();
    buildMenus(main_bm_menu, main_bookmark_list);
  }

  public BookmarkList getBookmarks() {
    return main_bookmark_list;
  }

  public void setBookmarkManager(BookmarkManagerView bmv) {
    this.bmv = bmv;
    if (bmv != null) {
      main_bm_menu.remove(clearMI);
    }
    updateBookmarkManager();
  }

  void updateBookmarkManager() {
    if (bmv != null) bmv.setBList(main_bookmark_list);
  }
  
  public static File getBookmarksFile() {
    String app_dir = UnibrowPrefsUtil.getAppDataDirectory();
    File f = new File(app_dir, "bookmarks.html");
    return f;
  }
  
  /**
   *  Loads bookmarks from the file specified by {@link #getBookmarksFile()}.
   *  If loading succeeds, also creates a backup copy of that bookmark list
   *  in a new file with the same name, but "~" added at the end.
   */
  void addDefaultBookmarks() {
    File f = getBookmarksFile();
    String filename = f.getAbsolutePath();
    if (f.exists()) try {
      System.out.println("Loading bookmarks from file \""+filename+"\"");
      BookmarksParser.parse(main_bookmark_list, f);

      if (main_bookmark_list != null && main_bookmark_list.getChildCount() != 0) {
        File f2 = new File(filename+"~");
        try {
          System.out.println("Creating backup bookmarks file: \""+f2+"\"");
          BookmarkList.exportAsNetscapeHTML(main_bookmark_list, f2);
        } catch (Exception e) {
          System.out.println("Error while trying to create backup bookmarks file: \""+f2+"\"");
        }
      }

    } catch (FileNotFoundException fnfe) {
      System.err.println("Could not load bookmarks. File not found or not readable: \""
        +filename + "\"");
    } catch (IOException ioe) {
      System.err.println("Could not load bookmarks from file \""
        +filename + "\"");
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
      System.out.println("Saving bookmarks to file \""+filename+"\"");
      File parent_dir = f.getParentFile();
      if (parent_dir != null) {
        parent_dir.mkdirs();
      }
      BookmarkList.exportAsNetscapeHTML(main_bookmark_list, f);
      saved = true;
    } catch (FileNotFoundException fnfe) {
      System.err.println("Could not auto-save bookmarks to \""
        +filename + "\"");
    } catch (IOException ioe) {
      System.err.println("Error while saving bookmarks to \"" +filename + "\"");
    }
    return saved;
  }

  /** Returns true if it has some unsaved, modified bookmarks.
   *  If the bookmark manager is used, this may return false even
   *  though some bookmarks were added there.
   */
  public boolean hasUnsavedBookmarks() {
    //TODO: integrate this better with the bookmark manager
    return unsaved_bookmarks;
  }


  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (src == add_pos_markMI) {
      bookmarkCurrentPosition(false);
    }
    else if (src == add_graph_markMI) {
      bookmarkCurrentPosition(true);
    }
    else if (src == exportMI) {
      exportBookmarks(main_bookmark_list, gviewer.getFrame());
      unsaved_bookmarks = false;
    }
    else if (src == importMI) {
      importBookmarks(main_bookmark_list, gviewer.getFrame());
      updateBookmarkManager();
      //rebuildMenus(); // menus get rebuilt when the menu item is opened
    }
    else if (src == clearMI) {
      removeAllBookmarks();
      unsaved_bookmarks = false;
    } else if (src instanceof BookmarkJMenuItem) {
      BookmarkJMenuItem item = (BookmarkJMenuItem) src;
      Bookmark bm = item.getBookmark();
      try {
        BookmarkController.viewBookmark(uni, gviewer, bm);
      } catch (Exception e) {
        IGB.errorPanel("Problem viewing bookmark", e);
      }
    } else if (DEBUG) {
      System.out.println("Got an action event from an unknown source: "+src);
      System.out.println("command: "+evt.getActionCommand());
    }
  }

  void removeAllBookmarks() {
    removeAllBookmarkMenuItems();
    main_bookmark_list.removeAllChildren();
    updateBookmarkManager();
  }

  void removeAllBookmarkMenuItems() {
    Iterator iter = component_hash.values().iterator();
    while (iter.hasNext()) {
      Component comp = (Component) iter.next();
      if (comp == main_bm_menu) {
        // component_hash contains a mapping of main_bookmark_list to main_bm_menu.
        // That is the only JMenu we do not want to remove from its parent.
        continue;
      }
      if (comp instanceof JMenuItem) {
        JMenuItem item = (JMenuItem) comp;
        ActionListener[] listeners = item.getActionListeners();
        for (int i=0; i<listeners.length; i++) {
          item.removeActionListener(listeners[i]);
        }
      } else { // if not a JMenuItem, should be a JSeparator
      }
      Container cont = comp.getParent();
      if (cont != null) {cont.remove(comp);}
    }
    component_hash.clear();
    component_hash.put(main_bookmark_list, main_bm_menu);
  }

  /** Currently unused. Instead we use removeAllBookmarkMenuItems(). */
  void removeComponentFor(BookmarkList bl) {
    Object o = bl.getUserObject();
    Component comp = (Component) component_hash.get(o);
    if (comp instanceof JMenuItem) {
      JMenuItem item = (JMenuItem) comp;
      ActionListener[] listeners = item.getActionListeners();
      for (int i=0; i<listeners.length; i++) {
        item.removeActionListener(listeners[i]);
      }
    }
    if (comp == null) {
      if (DEBUG) System.out.println("Couldn't find a component to remove for "+o);
    } else {
      Container cont = comp.getParent();
      if (cont != null) {cont.remove(comp);}
      component_hash.remove(o);
    }
  }

  public JMenuItem addBookmark(Map props, String name) {
    return addBookmark(props, name, main_bookmark_list);
  }

  public JMenuItem addBookmark(Map props, String name, BookmarkList bl) {
    JMenuItem markMI = null;
    JMenu parent_menu = (JMenu) component_hash.get(bl);
    if (parent_menu == null) {
      IGB.errorPanel("Couldn't add bookmark. Lost reference to menu");
      return null;
    }
    if (name == null || name.equals("")) {
      IGB.errorPanel("A bookmark must have a name.");
      return null;
    } else try {
      String url = Bookmark.constructURL(props);
      Bookmark bm = new Bookmark(name, url);
      addBookmarkMI(parent_menu, bm);
      bl.addBookmark(bm);
    } catch (MalformedURLException m) {
      IGB.errorPanel("Couldn't add bookmark", m);
    }

    updateBookmarkManager();
    return markMI;
  }

  JMenuItem addBookmarkMI(JMenu parent_menu, Bookmark bm) {
    JMenuItem markMI = (JMenuItem) component_hash.get(bm);
    if (markMI != null) {
      return markMI;
    }
    markMI = new BookmarkJMenuItem(bm);
    component_hash.put(bm, markMI);
    parent_menu.add(markMI);
    markMI.addActionListener(this);
    return markMI;
  }

  JMenu addBookmarkListMenu(JMenu parent_menu, BookmarkList bm_list) {
    JMenu sub_menu = (JMenu) component_hash.get(bm_list);
    if (sub_menu != null) {
      return sub_menu;
    }
    sub_menu = new JMenu(bm_list.getName());
    component_hash.put(bm_list,  sub_menu);
    parent_menu.add(sub_menu);
    return sub_menu;
  }

  JSeparator addSeparator(JMenu parent_menu, Separator s) {
    JSeparator jsep = (JSeparator) component_hash.get(s);
    if (jsep != null) {
      return null;
    }
    jsep = new JSeparator();
    component_hash.put(s, jsep);
    parent_menu.add(jsep);
    return jsep;
  }

  public void rebuildMenus() {
    removeAllBookmarkMenuItems();
    buildMenus(main_bm_menu, main_bookmark_list);
  }

  void buildMenus(JMenu pp, BookmarkList bl) {
    JMenu bl_menu = (JMenu) component_hash.get(bl);
    if (bl_menu == null) {
      bl_menu = addBookmarkListMenu(pp, bl);
    }
    Enumeration e = bl.children();
    while (e.hasMoreElements()) {
      BookmarkList node = (BookmarkList) e.nextElement();
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

  /**
   *  Tries to import bookmarks into Unibrow.
   *  Makes use of {@link BookmarksParser#parse(BookmarkList, File)}.
   */
  public static void importBookmarks(BookmarkList bookmark_list, JFrame frame) {
    boolean parse_error = false;
    JFileChooser chooser = getJFileChooser();
    chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
    int option = chooser.showOpenDialog(frame);
    if (option == JFileChooser.APPROVE_OPTION) {
      FileTracker.DATA_DIR_TRACKER.setFile(chooser.getCurrentDirectory());
      SynonymLookup lookup = SynonymLookup.getDefaultLookup();
      try {
        File fil = chooser.getSelectedFile();
        BookmarksParser.parse(bookmark_list, fil);
        if (DEBUG) {
          System.out.println("Imported bookmarks: ");
          bookmark_list.printText(System.out);
        }
      }
      catch (Exception ex) {
        IGB.errorPanel(frame, "Error", "Error importing bookmarks", ex);
      }
    }
  }

  static JFileChooser static_chooser = null;

  /** Gets a static re-usable file chooser that prefers "html" files. */
  public static JFileChooser getJFileChooser() {
    if (static_chooser == null) {
      static_chooser = new JFileChooser();
      static_chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
      UniFileFilter filter = new UniFileFilter(
        new String[] {"html", "htm", "xhtml"}, "HTML Files");
      static_chooser.addChoosableFileFilter(filter);
    }
    static_chooser.rescanCurrentDirectory();
    return static_chooser;
  }

  public static void exportBookmarks(BookmarkList main_bookmark_list, JFrame frame) {
    if (main_bookmark_list == null || main_bookmark_list.getChildCount()==0) {
      IGB.errorPanel(frame, "Error", "No bookmarks to save", null);
      return;
    }
    JFileChooser chooser = getJFileChooser();
    chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
    int option = chooser.showSaveDialog(frame);
    if (option == JFileChooser.APPROVE_OPTION) {
      try {
        FileTracker.DATA_DIR_TRACKER.setFile(chooser.getCurrentDirectory());
        File fil = chooser.getSelectedFile();
        String full_path = fil.getCanonicalPath();

        if ((! full_path.endsWith(".html"))
         && (! full_path.endsWith(".htm"))
         && (! full_path.endsWith(".xhtml"))) {
          fil = new File(full_path + ".html");
        }

        if (DEBUG) {System.out.println("bookmark file chosen: " + fil);}
        BookmarkList.exportAsNetscapeHTML(main_bookmark_list, fil);
      }
      catch (Exception ex) {
        IGB.errorPanel(frame, "Error", "Error exporting bookmarks", ex);
      }
    }
  }

  void bookmarkCurrentPosition(boolean include_graphs) {
    AffyTieredMap map = (AffyTieredMap)gviewer.getSeqMap();
    MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)gmodel.getSelectedSeq();

    if (aseq == null) {
      uni.errorPanel("Error", "Nothing to bookmark");
    } else {
      Rectangle2D vbox = map.getView().getCoordBox();
      SimpleSymWithProps mark_sym = new SimpleSymWithProps();
      SeqSpan mark_span = new SimpleSeqSpan((int)vbox.x,
                                            (int)(vbox.x+vbox.width),
                                            aseq);
      mark_sym.addSpan(mark_span);
      if (include_graphs) {
	//        java.util.List graphs = BookmarkController.collectGraphs(gviewer.getSeqMap());
        java.util.List graphs = gviewer.collectGraphs();
        BookmarkController.addGraphProperties(mark_sym, graphs);
      }

      String version = "unknown";
      if (aseq instanceof NibbleBioSeq) {
        version = ((NibbleBioSeq)aseq).getVersion();
      }
      String default_name =
        version + ", " + aseq.getID() + ":" + mark_span.getMin() +
          ", " + mark_span.getMax();
      mark_sym.setProperty("version", version);
      mark_sym.setProperty("seqid", aseq.getID());
      mark_sym.setProperty("start", new Integer(mark_span.getMin()));
      mark_sym.setProperty("end", new Integer(mark_span.getMax()));
      String bookmark_name = (String) JOptionPane.showInputDialog(gviewer,
        "Enter name for bookmark", "Input",
        JOptionPane.PLAIN_MESSAGE, null, null, default_name);
      if (bookmark_name == null) {
        if (DEBUG) {System.out.println("bookmark action cancelled");}
      }
      else {
        if (bookmark_name.trim().length()==0) {bookmark_name = default_name;}
        if (DEBUG) {System.out.println("bookmark name: " + bookmark_name);}
        addBookmark(mark_sym.getProperties(), bookmark_name);
        unsaved_bookmarks = true;
      }
    }
  }

  /** Does nothing. */
  public void menuCanceled(javax.swing.event.MenuEvent e) {
  }

  /** Does nothing. */
  public void menuDeselected(javax.swing.event.MenuEvent e) {
  }

  /** Every time the menu is selected (thus opened) re-build the bookmark menu items.
   *  Thus if the bookmarks have been changed by the bookmark manager, we will
   *  adapt to that now.
   *  Slow? Yes.  Too slow?  Not really.
   */
  public void menuSelected(javax.swing.event.MenuEvent e) {
    if (e.getSource()==bookmark_menu) {
      rebuildMenus();
    }
  }

}
