/**
*   Copyright (c) 2005 Affymetrix, Inc.
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

package com.affymetrix.igb.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.affymetrix.genometry.*;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.event.SymMapChangeEvent;
import com.affymetrix.igb.event.SymMapChangeListener;
import com.affymetrix.igb.prefs.IPrefEditorComponent;
import com.affymetrix.igb.util.TableSorter2;
import com.affymetrix.igb.tiers.AnnotStyle;
import com.affymetrix.swing.*;
import java.util.prefs.*;

/**
 *  A panel for choosing tier properties for the {@link SeqMapView}.
 */
public class TierPrefsView extends JPanel implements ListSelectionListener, IPrefEditorComponent  {

  private final JTable table = new JTable();
  
  private static final String TIER_NAME = "Tier";
  private static final String COLOR = "Color";
  private static final String SEPARATE = "2 Tiers";
  private static final String COLLAPSED = "Collapsed";
  private static final String MAX_DEPTH = "Max Depth";
  private static final String BACKGROUND = "Background";
  private static final String GLYPH_DEPTH = "Connected";
  private static final String LABEL_FIELD = "Label Field";
  private static final String HUMAN_NAME = "Display Name";

  private final static String[] col_headings = {
    TIER_NAME, HUMAN_NAME,
    COLOR, BACKGROUND,
    SEPARATE, COLLAPSED,
    MAX_DEPTH, GLYPH_DEPTH, LABEL_FIELD,
  };
  
  private final int COL_TIER_NAME = 0;
  private final int COL_HUMAN_NAME = 1;
  private final int COL_COLOR = 2;
  private final int COL_BACKGROUND = 3;
  private final int COL_SEPARATE = 4;
  private final int COL_COLLAPSED = 5;
  private final int COL_MAX_DEPTH = 6;
  private final int COL_GLYPH_DEPTH = 7;
  private final int COL_LABEL_FIELD = 8;

  
  private final TierPrefsTableModel model;
  private final ListSelectionModel lsm;

  JButton refresh_list_B = new JButton("Refresh List");
  
  JButton refresh_map_B = new JButton("Refresh Map");
  SeqMapView smv;
  
  static AnnotStyle default_annot_style = AnnotStyle.getDefaultInstance(); // make sure at least the default instance exists;
  
  public TierPrefsView() {
    this(true, true);
  }
  
  public TierPrefsView(boolean add_refresh_list_button, boolean add_refresh_map_button) {
    super();
    this.setLayout(new BorderLayout());
   
    JScrollPane table_scroll_pane = new JScrollPane(table);
    
    this.add(table_scroll_pane, BorderLayout.CENTER);
    JPanel button_panel = new JPanel();
    button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.X_AXIS));

    JButton apply_bg_button = new JButton("Apply Default Background");
    button_panel.add(Box.createHorizontalGlue());
    button_panel.add(apply_bg_button);
    
    
    IGB igb = IGB.getSingletonIGB();
    if (igb != null) {
      smv = igb.getMapView();
    }

    // Add a "refresh map" button, iff there is an instance of IGB
    if (smv != null && add_refresh_map_button) {
      refresh_map_B.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          refreshSeqMapView();
        }
      });
      button_panel.add(Box.createHorizontalStrut(10));
      button_panel.add(refresh_map_B);
    }

    apply_bg_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Iterator iter = AnnotStyle.getAllLoadedInstances().iterator();
        while (iter.hasNext()) {
          AnnotStyle as = (AnnotStyle) iter.next();
          as.setBackground(default_annot_style.getBackground());
        }
        refreshList();
        refreshSeqMapView();
      }
    });
    
    if (add_refresh_list_button) {
      button_panel.add(Box.createHorizontalStrut(10));
      button_panel.add(refresh_list_B);
      //this.add(refresh_list_B, BorderLayout.SOUTH);
      refresh_list_B.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          refreshList();
        }
      });
    }
    button_panel.add(Box.createHorizontalGlue());
    this.add(button_panel, BorderLayout.SOUTH);
    
    model = new TierPrefsTableModel();
    model.addTableModelListener(new javax.swing.event.TableModelListener() {
      public void tableChanged(javax.swing.event.TableModelEvent e) {
        // do nothing.
      }
    });

    lsm = table.getSelectionModel();
    lsm.addListSelectionListener(this);
    lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    TableSorter2 sort_model = new TableSorter2(model);
    sort_model.setTableHeader(table.getTableHeader());
    table.setModel(sort_model);

    table.setRowSelectionAllowed(true);
    table.setEnabled( true ); // doesn't do anything ?
    
    table.setDefaultRenderer(Color.class, new ColorTableCellRenderer(true));
    table.setDefaultEditor(Color.class, new ColorTableCellEditor());
    table.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());
    
    validate();
  }
  
  public void applyChanges() {
    refreshSeqMapView();
  }
  
  public void setStyleList(java.util.List styles) {
    model.setStyles(styles);
    model.fireTableDataChanged();
  }
  
  /** Called when the user selects a row of the table.
   */
  public void valueChanged(ListSelectionEvent evt) {
    if (evt.getSource()==lsm && ! evt.getValueIsAdjusting()) {
    }
  }
  
  public void destroy() {
    removeAll();
    if (lsm != null) {lsm.removeListSelectionListener(this);}
  }

  void refreshSeqMapView() {
    if (smv != null) {
      smv.setAnnotatedSeq(smv.getAnnotatedSeq(), true, true);
    }
  }  
  
  void refreshList() {
    java.util.List styles = AnnotStyle.getAllLoadedInstances();
    this.setStyleList(styles);
  }
  
  class TierPrefsTableModel extends AbstractTableModel {
    
    java.util.List tier_styles;
    
    TierPrefsTableModel() {
      this.tier_styles = Collections.EMPTY_LIST;
    }
    
    public void setStyles(java.util.List tier_styles) {
      this.tier_styles = tier_styles;
    }
    
    public java.util.List getStyles() {
      return this.tier_styles;
    }
        
    // Allow editing most fields in normal rows, but don't allow editing some
    // fields in the "default" style row.
    public boolean isCellEditable(int row, int column) {
      if (tier_styles.get(row) == default_annot_style) {
        if (column == COL_COLOR || column == COL_BACKGROUND || column == COL_SEPARATE
            || column == COL_COLLAPSED || column == COL_MAX_DEPTH) {
          return true;
        }
        else {
          return false;
        }
      } else {
        return (column != COL_TIER_NAME);
      }
    }
    
    public Class getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }
    
    public int getColumnCount() {
      return col_headings.length;
    }
    
    public String getColumnName(int columnIndex) {
      return col_headings[columnIndex];
    }
    
    public int getRowCount() {
      return tier_styles.size();
    }
    
    public Object getValueAt(int row, int column) {
      AnnotStyle style = (AnnotStyle) tier_styles.get(row);
      switch (column) {
        case COL_COLOR: 
          return style.getColor();
        case COL_SEPARATE: 
          return Boolean.valueOf(style.getSeparate());
        case COL_COLLAPSED: 
          return Boolean.valueOf(style.getCollapsed());
        case COL_TIER_NAME:
          String name = style.getUniqueName();
          if (! style.getPersistent()) { name = "<html><i>" + name + "</i></html>"; }
          return name;
        case COL_MAX_DEPTH: 
          int md = style.getMaxDepth();
          if (md == 0) { return ""; }
          else { return String.valueOf(md); }
        case COL_BACKGROUND:
          return style.getBackground();
        case COL_GLYPH_DEPTH:
          return (style.getGlyphDepth()==2 ? Boolean.TRUE : Boolean.FALSE);
        case COL_LABEL_FIELD:
          return style.getLabelField();
        case COL_HUMAN_NAME:
          if (style == default_annot_style) { return ""; }
          else { return style.getHumanName(); }
        default:
          return null;
      }
    }
    
    public void setValueAt(Object value, int row, int col) {
      try {
      AnnotStyle style = (AnnotStyle) tier_styles.get(row);
      switch (col) {
        case COL_COLOR:
          style.setColor((Color) value);
          break;
        case COL_SEPARATE:
          style.setSeparate(((Boolean) value).booleanValue());
          break;
        case COL_COLLAPSED:
          style.setCollapsed(((Boolean) value).booleanValue());
          break;
        case COL_TIER_NAME:
          System.out.println("Tier name is not changeable!");
          break;
        case COL_MAX_DEPTH: 
          {
            int i = parseInteger(((String) value), 0, style.getMaxDepth());
            style.setMaxDepth(i);
          }
          break;
        case COL_BACKGROUND:
          style.setBackground((Color) value);
          break;
        case COL_GLYPH_DEPTH: 
          if (Boolean.TRUE.equals(value)) {
            style.setGlyphDepth(2);
          } else {
            style.setGlyphDepth(1);
          }
          break;
        case COL_LABEL_FIELD:
          style.setLabelField((String) value);
          break;
        case COL_HUMAN_NAME:
          style.setHumanName((String) value);
          break;
        default:
          System.out.println("Unknown column selected: " + col);;
      }
      fireTableCellUpdated(row, col);
      } catch (Exception e) {
        // exceptions should not happen, but must be caught if they do
        System.out.println("Exception in TierPrefsView.setValueAt(): " + e);
      }
    }
  
    /** Parse an integer, using the given fallback if any exception occurrs.
     *  @param s  The String to parse.
     *  @param empty_string  the value to return if the input is an empty string.
     *  @param fallback  the value to return if the input String is unparseable.
     */
    int parseInteger(String s, int empty_string, int fallback) {
      //System.out.println("Parsing string: '" + s + "'");
      int i = fallback;
      try {
        if ("".equals(s.trim())) {i = empty_string; }
        else { i = Integer.parseInt(s); }
      }
      catch (Exception e) {
        //System.out.println("Exception: " + e);
        // don't report the error, use the fallback value
      }
      return i;
    }
    
  };
  
  
  static JFrame static_frame;
  static TierPrefsView static_instance;
  
  static final String WINDOW_NAME = "Tier Customizer";
  
  /**
   *  Gets an instance of TierPrefsView wrapped in a JFrame, useful
   *  as a pop-up dialog for setting annotatin styles. 
   */
  public static JFrame showFrame() {
    if (static_frame == null) {
      static_frame = new JFrame(WINDOW_NAME);
      static_instance = new TierPrefsView(false, false);
      static_frame.getContentPane().add(static_instance);

      static_frame.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          //static_instance.refreshSeqMapView();
          // save window size
        }
      });
      static_frame.pack();
      // restore saved window size
    }
    
    static_instance.refreshList();
    
    
    static_frame.show();
    return static_frame;
  }
  
  /** Used for testing.  Opens a window with the TierPrefsView in it. */
  public static void main(String[] args) {

    TierPrefsView t = new TierPrefsView();

    AnnotStyle.getInstance("RefSeq");
    AnnotStyle.getInstance("EnsGene");
    AnnotStyle.getInstance("Contig");
    AnnotStyle.getInstance("KnownGene");
    AnnotStyle.getInstance("TwinScan", false);
    
    t.setStyleList(AnnotStyle.getAllLoadedInstances());
    
    JFrame f = new JFrame(WINDOW_NAME);
    f.getContentPane().add(t);
    
    f.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        System.exit(0);
      }
    });
    f.pack();
    
    f.setSize(800, 800);
    f.show();
  }
  
  public String getName() {
    return "Tiers";
  }
  
  public String getHelpTextHTML() {
    StringBuffer sb = new StringBuffer();
    
    sb.append("<h1>" + this.getName() + "</h1>\n");
    sb.append("<p>\n");
    sb.append("Use this panel to change properties of annotation tiers.  ");
    sb.append("Changes do not require re-start, and most ");
    sb.append("will be remembered between sessions.  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>Default Row</h2>\n");
    sb.append("Properties on the default row apply when loading <em>new</em> types ");
    sb.append("where specific properties have not been set before.  Once properties are set for ");
    sb.append("any type, those properties will be remembered and the default properties will ");
    sb.append("not be used for that type.  ");
    sb.append("Certain properties of the default row, such as "+HUMAN_NAME+", are not editable.  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>"+TIER_NAME+"</h2>\n");
    sb.append("The name of the tier.  ");
    sb.append("If the name is shown in <i>italic</i> font, the settings for that tier ");
    sb.append("will apply only to the current session.  ");
    sb.append("If the tier name is shown in normal font, the settings will persist between sessions.  ");
    sb.append("Non-persistent settings apply to graphs and temporary data resulting from arithmetic manipulations,  ");
    sb.append("such as intersections and unions of tiers.  ");
    //sb.append("Such settings that are not remembered between sessions are indicated by a tier name in <i>italics</i>.  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>"+HUMAN_NAME+"</h2>\n");
    sb.append("Sets the name to display as the tier label.  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>Color and Background</h2>\n");
    sb.append("Sets the foreground and background colors.  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>"+SEPARATE+"</h2>\n");
    sb.append("Whether to display annotations in two tiers (+) and (-), or one (+/-).  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>"+COLLAPSED+"</h2>\n");
    sb.append("Whether to collapse the tier to its minimum height.  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>"+MAX_DEPTH+"</h2>\n");
    sb.append("The maximum rows of annotations to show in tiers that are <em>not</em> collapsed.  ");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>"+GLYPH_DEPTH+"</h2>\n");
    sb.append("Whether to connect groups of exons into transcripts.  ");
    sb.append("Should be false for data with no intron-exon structure,  ");
    sb.append("such as repeats or contigs.  ");
    sb.append("Setting this to false for items that <em>do</em> have intron-exon structure ");
    sb.append("may affect whether labels are displayed.  See below.");
    sb.append("</p>\n");

    sb.append("<p>\n");
    sb.append("<h2>"+LABEL_FIELD+"</h2>\n");
    sb.append("The name of the field to use to construct labels.  ");
    sb.append("For example, with 'RefSeq' data, you may choose to use 'gene name'.  ");
    sb.append("For many other data types you may choose to use 'id'.  ");
    sb.append("Leave this blank to turn labels off.  ");
    sb.append("Turning labels off reduces the memory required by the program.  ");
    sb.append("Note that setting "+GLYPH_DEPTH+" to false will make labels disappear if the ");
    sb.append("individual exons don't also have the chosen 'id' property.  ");
    sb.append("</p>\n");

    return sb.toString();
  }
  
  public Icon getIcon() {
    return null;
  }
  
  public String getInfoURL() {
    return "";
  }
  
  public String getToolTip() {
    return "Set Tier Colors and Properties";
  }
  
  // implementation of IPrefEditorComponent
  public void refresh() {
    refreshList();
  }
  
}


