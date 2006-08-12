/**
*   Copyright (c) 2005-2006 Affymetrix, Inc.
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

package com.affymetrix.igb.tiers;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.genometry.SingletonGenometryModel;
import com.affymetrix.igb.glyph.*;
import com.affymetrix.igb.menuitem.FileTracker;
import com.affymetrix.igb.parsers.BedParser;
import com.affymetrix.igb.parsers.Das2FeatureSaxParser;
import com.affymetrix.igb.prefs.PreferencesPanel;
import com.affymetrix.igb.util.*;
import com.affymetrix.igb.view.*;

public class SeqMapViewPopup implements TierLabelManager.PopupListener {

  static final boolean DEBUG = false;

  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  AnnotatedSeqViewer gviewer;
  TierLabelManager handler;

  Action select_all_tiers_action = new AbstractAction("Select All Tiers") {
    public void actionPerformed(ActionEvent e) {
      handler.selectAllTiers();
    }
  };
  
  Action rename_action = new AbstractAction("Change Display Name") {
    public void actionPerformed(ActionEvent e) {
      java.util.List current_tiers = handler.getSelectedTiers();
      if (current_tiers.size() != 1) {
        ErrorHandler.errorPanel("Must select only one tier");
      }
      TierGlyph current_tier = (TierGlyph) current_tiers.get(0);
      renameTier(current_tier);
    }
  };

  Action customize_action = new AbstractAction("Customize") {
    public void actionPerformed(ActionEvent e) {
      showCustomizer();
    }
  };

  Action expand_action = new AbstractAction("Expand") {
    public void actionPerformed(ActionEvent e) {
      setTiersCollapsed(handler.getSelectedTierLabels(), false);
    }
  };

  Action expand_all_action = new AbstractAction("Expand All") {
    public void actionPerformed(ActionEvent e) {
      setTiersCollapsed(handler.getAllTierLabels(), false);
    }
  };

  Action collapse_action = new AbstractAction("Collapse") {
    public void actionPerformed(ActionEvent e) {
      setTiersCollapsed(handler.getSelectedTierLabels(), true);
    }
  };

  Action collapse_all_action = new AbstractAction("Collapse All") {
    public void actionPerformed(ActionEvent e) {
      setTiersCollapsed(handler.getAllTierLabels(), true);
    }
  };

  Action hide_action = new AbstractAction("Hide") {
    public void actionPerformed(ActionEvent e) {
      hideTiers(handler.getSelectedTierLabels());
    }
  };

  Action show_all_action = new AbstractAction("Show All") {
    public void actionPerformed(ActionEvent e) {
      showAllTiers();
    }
  };

  Action change_color_action = new AbstractAction("Change FG Color") {
    public void actionPerformed(ActionEvent e) {
      changeColor(handler.getSelectedTierLabels(), true);
    }
  };

  Action change_bg_color_action = new AbstractAction("Change BG Color") {
    public void actionPerformed(ActionEvent e) {
      changeColor(handler.getSelectedTierLabels(), false);
    }
  };
  
  //TODO: make a change_height_action
  //Action change_height_action = ....


  Action sym_summarize_action = new AbstractAction("Make Annotation Depth Graph") {
    public void actionPerformed(ActionEvent e) {
      java.util.List current_tiers = handler.getSelectedTiers();
      if (current_tiers.size() > 1) {
        ErrorHandler.errorPanel("Must select only one tier");
      }
      TierGlyph current_tier = (TierGlyph) current_tiers.get(0);
      addSymSummaryTier(current_tier);
    }
  };
  Action coverage_action = new AbstractAction("Make Annotation Coverage Track") {
    public void actionPerformed(ActionEvent e) {
      java.util.List current_tiers = handler.getSelectedTiers();
      if (current_tiers.size() > 1) {
        ErrorHandler.errorPanel("Must select only one tier");
      }
      TierGlyph current_tier = (TierGlyph) current_tiers.get(0);
      addSymCoverageTier(current_tier);
    }
  };
  Action save_bed_action = new AbstractAction("Save tier as BED file") {
    public void actionPerformed(ActionEvent e) {
      java.util.List current_tiers = handler.getSelectedTiers();
      if (current_tiers.size() > 1) {
        ErrorHandler.errorPanel("Must select only one tier");
      }
      TierGlyph current_tier = (TierGlyph) current_tiers.get(0);
      saveAsBedFile(current_tier);
    }
  };

  Action save_das_action = new AbstractAction("Save tier to DAS/2") {
    public void actionPerformed(ActionEvent e) {
      java.util.List current_tiers = handler.getSelectedTiers();
      if (current_tiers.size() > 1) {
        ErrorHandler.errorPanel("Must select only one tier");
      }
      TierGlyph current_tier = (TierGlyph) current_tiers.get(0);
      saveToDas2(current_tier);
    }
  };

  Action write_das_action = new AbstractAction("Test writing to DAS/2 server") {
    public void actionPerformed(ActionEvent e) {
      java.util.List current_tiers = handler.getSelectedTiers();
      if (current_tiers.size() > 1) {
        ErrorHandler.errorPanel("Must select only one tier");
      }
      TierGlyph current_tier = (TierGlyph) current_tiers.get(0);
      testDas2Writeback(current_tier);
    }
  };

  Action change_expand_max_action = new AbstractAction("Adjust Max Expand") {
    public void actionPerformed(ActionEvent e) {
      changeExpandMax(handler.getSelectedTierLabels());
    }
  };

  Action change_expand_max_all_action = new AbstractAction("Adjust Max Expand All") {
    public void actionPerformed(ActionEvent e) {
      changeExpandMax(handler.getAllTierLabels());
    }
  };
  JMenu showMenu = new JMenu("Show...");
  JMenu changeMenu = new JMenu("Change...");

  public SeqMapViewPopup(TierLabelManager handler, AnnotatedSeqViewer gviewer) {
    this.handler = handler;
    this.gviewer = gviewer;
  }

  void showCustomizer() {
    PreferencesPanel pv = PreferencesPanel.getSingleton();
    pv.setTab(PreferencesPanel.TAB_NUM_TIERS);
    JFrame f = pv.getFrame();
    f.setVisible(true);
  }

  java.util.List getStyles(java.util.List tier_label_glyphs) {
    if (tier_label_glyphs.size() == 0) { return Collections.EMPTY_LIST; }

    // styles is a list of styles with no duplicates, so a Set rather than a List
    // might make sense.  But at the moment it seems faster to use a List
    java.util.List styles = new ArrayList(tier_label_glyphs.size());

    for (int i=0; i<tier_label_glyphs.size(); i++) {
      TierLabelGlyph tlg = (TierLabelGlyph) tier_label_glyphs.get(i);
      TierGlyph tier = tlg.getReferenceTier();
      IAnnotStyle tps = tier.getAnnotStyle();
      if (tps != null && ! styles.contains(tps)) styles.add(tps);
    }
    return styles;
  }

  void setTiersCollapsed(java.util.List tier_labels, boolean collapsed) {
    for (int i=0; i<tier_labels.size(); i++) {
      TierLabelGlyph tlg = (TierLabelGlyph) tier_labels.get(i);
      IAnnotStyle style = tlg.getReferenceTier().getAnnotStyle();
      if (style.getExpandable()) {
        style.setCollapsed(collapsed);
        
        // When collapsing, make them all be the same height as the tier.
        // (this is for simplicity in figuring out how to draw things.)
        if (collapsed) {
          java.util.List graphs = handler.getContainedGraphs(tlg);
          double tier_height = style.getHeight();
          for (int j=0; j<graphs.size(); j++) {
            GraphGlyph graph = (GraphGlyph) graphs.get(j);
            graph.getGraphState().getTierStyle().setHeight(tier_height);
          }
        }
      }
    }

    refreshMap(false);
  }

  public void changeExpandMax(java.util.List tier_labels) {
    if (tier_labels == null || tier_labels.size() == 0) {
      ErrorHandler.errorPanel("changeExpandMaxAll called with an empty list");
      return;
    }

    String initial_value = "0";
    if (tier_labels.size() == 1) {
      TierLabelGlyph tlg = (TierLabelGlyph) tier_labels.get(0);
      TierGlyph tg = (TierGlyph) tlg.getInfo();
      IAnnotStyle style = tg.getAnnotStyle();
      if (style != null) { initial_value = "" + style.getMaxDepth(); }
    }

    String input =
      (String)JOptionPane.showInputDialog(null,
					  "Enter new maximum tier height, 0 for unlimited",
					  "Change Selected Tiers Max Height", JOptionPane.PLAIN_MESSAGE,
					  null, null, initial_value);

    if (input == JOptionPane.UNINITIALIZED_VALUE || !(input instanceof String)) {
      return;
    }

    int newmax;
    try {
      newmax = Integer.parseInt(input);
    }
    catch (NumberFormatException ex) {
      ErrorHandler.errorPanel("Couldn't parse new tier max '"+input+"'");
      return;
    }

    changeExpandMax(tier_labels, newmax);
  }

  void changeExpandMax(java.util.List tier_label_glyphs, int max) {
    for (int i=0; i<tier_label_glyphs.size(); i++) {
      TierLabelGlyph tlg = (TierLabelGlyph) tier_label_glyphs.get(i);
      TierGlyph tier = (TierGlyph) tlg.getInfo();
      IAnnotStyle style = tier.getAnnotStyle();
      style.setMaxDepth(max);
      tier.setMaxExpandDepth(max);
    }
    refreshMap(false);
  }

//  void changeHeight(java.util.List tier_label_glyphs, double height) {
//    if (gviewer instanceof SeqMapView) {
//    AffyTieredMap map = ((SeqMapView) gviewer).getSeqMap();
//    for (int i=0; i<tier_label_glyphs.size(); i++) {
//      TierLabelGlyph tlg = (TierLabelGlyph) tier_label_glyphs.get(i);
//      TierGlyph tier = (TierGlyph) tlg.getInfo();
//      IAnnotStyle style = tier.getAnnotStyle();
//      style.setHeight(???);
//      tier.pack(map.getView());
//    } 
//    map.packTiers(false, true, false);
//    map.stretchToFit(false, true);
//    map.updateWidget();
//    }
//  }
  
  public void showAllTiers() {
    java.util.List tiervec = handler.getAllTierLabels();

    for (int i=0; i<tiervec.size(); i++) {
      TierLabelGlyph label = (TierLabelGlyph) tiervec.get(i);
      TierGlyph tier = (TierGlyph) label.getInfo();
      IAnnotStyle style = tier.getAnnotStyle();
      if (style != null) {
        style.setShow(true);
      }
      if (style.getShow()) {
        tier.restoreState();
      }
    }
    showMenu.removeAll();
    refreshMap(true);
  }

  /** Hides one tier and creates a JMenuItem that can be used to show it again.
   *  Does not re-pack the given tier, or any other tiers.
   */
  protected void hideOneTier(final TierGlyph tier) {
    final IAnnotStyle style = tier.getAnnotStyle();
    // if style.getShow() is already false, there is likely a bug somewhere!
    if (style != null && style.getShow()) {
      style.setShow(false);
      final JMenuItem show_tier = new JMenuItem() {
        // override getText() because the HumanName of the style might change
        public String getText() {
          String name = style.getHumanName();
          if (name == null) { name = "<unnamed>"; }
          if (name.length() > 30) {
            name = name.substring(0,30) + "...";
          }
          return name;
        }
      };
      show_tier.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          style.setShow(true);
          showMenu.remove(show_tier);
          refreshMap(false);
        }
      });
      showMenu.add(show_tier);
    }
    if (! style.getShow()) {
      tier.setState(TierGlyph.HIDDEN);
    }
  }

  /** Hides multiple tiers and then repacks.
   *  @param tiers  a List of GlyphI objects for each of which getInfo() returns a TierGlyph.
   */
  public void hideTiers(java.util.List tiers) {
    Iterator iter = tiers.iterator();
    while (iter.hasNext()) {
      GlyphI g = (GlyphI) iter.next();
      if (g.getInfo() instanceof TierGlyph) {
        TierGlyph tier = (TierGlyph) g.getInfo();
        hideOneTier(tier);
        //tier.pack(tiermap.getView());
      }
    }

    refreshMap(false);
  }

  public void changeColor(final java.util.List tier_label_glyphs, final boolean fg) {
    if (tier_label_glyphs.isEmpty()) {
      return;
    }

    final JColorChooser chooser = new JColorChooser();

    TierLabelGlyph tlg_0 = (TierLabelGlyph) tier_label_glyphs.get(0);
    TierGlyph tier_0 = (TierGlyph) tlg_0.getInfo();
    IAnnotStyle style_0 = tier_0.getAnnotStyle();
    if (style_0 != null) {
      if (fg) {
        chooser.setColor(style_0.getColor());
      } else {
        chooser.setColor(style_0.getBackground());
      }
    }

    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i=0; i<tier_label_glyphs.size(); i++) {
          TierLabelGlyph tlg = (TierLabelGlyph) tier_label_glyphs.get(i);
          TierGlyph tier = (TierGlyph) tlg.getInfo();
          IAnnotStyle style = tier.getAnnotStyle();

          if (style != null) {
            //System.out.println("Setting color of " + style.getHumanName() + ", " + style);
            if (fg) {
              style.setColor(chooser.getColor());
            } else {
              style.setBackground(chooser.getColor());
            }
          }
          Iterator graphs_iter = handler.getContainedGraphs(tier_label_glyphs).iterator();
          while (graphs_iter.hasNext()) {
            GraphGlyph gg =(GraphGlyph) graphs_iter.next();
            if (fg) {
              gg.setColor(chooser.getColor());
              gg.getGraphState().getTierStyle().setColor(chooser.getColor());
            } else {
              //gg.setBackgroundColor(chooser.getColor()); // this wouldn't really work
              gg.getGraphState().getTierStyle().setBackground(chooser.getColor());
            }
          }
        }
      }
    };

    JDialog dialog = JColorChooser.createDialog((java.awt.Component) null, // parent
                                        "Pick a Color",
                                        true,  //modal
                                        chooser,
                                        al,  //OK button handler
                                        null); //no CANCEL button handler
    dialog.setVisible(true);

    refreshMap(false);
  }

  public void renameTier(final TierGlyph tier) {
    if (tier == null) {
      return;
    }
    IAnnotStyle style = tier.getAnnotStyle();

    String new_label = JOptionPane.showInputDialog("Label: ", style.getHumanName());
    if (new_label != null && new_label.length() > 0) {
      style.setHumanName(new_label);
    }
    refreshMap(false);
  }

  public void testDas2Writeback(TierGlyph atier) {
    MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)gmodel.getSelectedSeq();
    String annot_type = atier.getLabel();
    int childcount= atier.getChildCount();
    java.util.List syms = new ArrayList(childcount);
    int MAX_SYMS = 3;
    for (int i=0; i<childcount; i++) {
      GlyphI child = atier.getChild(i);
      if (child.getInfo() instanceof SeqSymmetry) {
	syms.add(child.getInfo());
      }
      if (i>=MAX_SYMS) { break; }
    }

    Das2FeatureSaxParser das_parser = new Das2FeatureSaxParser();
    System.out.println("writeback doc:");
    das_parser.writeBackAnnotations(syms, aseq, "type/SO:region", System.out); // diagnostic
    try {
      System.out.println("Testing DAS/2 writeback: "+ syms.size());

      //      URL writeback_url = new URL("http://localhost:7085/Das2WritebackTester/write");
      URL writeback_url = new URL("http://genomics.ctrl.ucla.edu/~allenday/cgi-bin/das2xml-parser/stable1.pl");
      URLConnection con = writeback_url.openConnection();
      con.setDoInput(true);
      con.setDoOutput(true);

      OutputStream conos = con.getOutputStream();
      BufferedOutputStream bos = new BufferedOutputStream(conos);
      //      outputWritebackTestFile(bos);

      //      das_parser.writeBackAnnotations(syms, aseq, annot_type, bos);
      das_parser.writeBackAnnotations(syms, aseq, "type/SO:region", bos);

      bos.flush();
      bos.close();
      InputStream istr = con.getInputStream();
      System.out.println("****** Response from writeback server: ");

      //  for now just need to change ids to match ids from writeback server
      //  eventually want to completely replace syms with given ids with those from server (if they differ...)
      BufferedReader reader = new BufferedReader(new InputStreamReader(istr));
      String line;
      while ((line = reader.readLine()) != null) {
	System.out.println(line);
      }
      istr.close();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    System.out.println("finished test writeback");
  }

  public void outputWritebackTestFile(OutputStream ostr) {
    String test_writeback_file = "C:/data/das2_testing/writeback_test5.xml";
    // String test_writeback_file = "C:/data/das2_testing/allen_writeback_doc.xml";
    try {
      BufferedReader reader =
	new BufferedReader(new InputStreamReader(new FileInputStream(new File(test_writeback_file))));
      PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(ostr)));
      String line;
      while ((line = reader.readLine()) != null) {
	writer.println(line);
        System.out.println(line);
      }
      writer.flush();
    }
    catch (Exception ex) { ex.printStackTrace(); }
  }

  /*
  public void saveCurationTier() {
    System.out.println("trying to save tier back to DAS server");
    TierGlyph atier = tier_manager.getCurrentTier();
    if (atier==null) {
      IGB.errorPanel("No curation tier to save");
      return;
    }
    String tier_label = atier.getLabel();
    if (tier_label.indexOf(" (+)") > 0) {
      tier_label = tier_label.substring(0, tier_label.indexOf(" (+)"));
    }
    tier_label = tier_label + "_saved";
    System.out.println("tier label = " + tier_label + ", tier info = " + atier.getInfo());
    //    ArrayList leaves = new ArrayList();
    AnnotatedBioSeq aseq = gmodel.getSelectedSeq();
    String seqid = aseq.getID();
    //    ArrayList syms_to_save = new ArrayList();
    // collect symmetries to write out as DAS <FEATURE> elements
    SeqSpan fullspan = new SimpleSeqSpan(0, aseq.getLength(), aseq);
    int sym_count = 0;

    try {
      String das_servlet_root = (String)IGB.getIGBPrefs().get("DasStashServletUrl");
      if (das_servlet_root == null) {
	das_servlet_root =   default_das_root;
      }
      String das_submit_url = das_servlet_root + "/das/Human_Apr_2003/submit_features";
      URL das_submit_server = new URL(das_submit_url);
      URLConnection con = das_submit_server.openConnection();
      con.setDoInput(true);
      con.setDoOutput(true);

      OutputStream conos = con.getOutputStream();
      BufferedOutputStream bos = new BufferedOutputStream(conos);
      PrintWriter pw = new PrintWriter(bos);
      das_parser.writeDasFeatHeader(fullspan, pw);
      for (int i=0; i<atier.getChildCount(); i++) {
	if (atier.getChild(i).getInfo() instanceof SeqSymmetry) {
	  SeqSymmetry sym = (SeqSymmetry)(atier.getChild(i).getInfo());
	  //	  das_parser.writeDasFeature(sym, aseq, "test", pw);
	  das_parser.writeDasFeature(sym, aseq, tier_label, pw);
	  sym_count++;
	}
      }
      das_parser.writeDasFeatFooter(pw);
      pw.flush();
      pw.close();
      //      conos.close();

      InputStream istr = con.getInputStream();
      istr.close();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    //    System.out.println("syms to save count: " + syms_to_save.size());
    System.out.println("syms to save count: " + sym_count);
  }
  */


  public void saveToDas2(TierGlyph atier) {
    String annot_type = atier.getLabel();
    int childcount= atier.getChildCount();
    java.util.List syms = new ArrayList(childcount);
    for (int i=0; i<childcount; i++) {
      GlyphI child = atier.getChild(i);
      if (child.getInfo() instanceof SeqSymmetry) {
	syms.add(child.getInfo());
      }
    }
    System.out.println("Saving symmetries to DAS/2: "+ syms.size());

    JFileChooser chooser = UniFileChooser.getFileChooser("DAS/2 file (*.das2xml)", "das2xml");
    chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());

    int option = chooser.showSaveDialog(null);
    if (option == JFileChooser.APPROVE_OPTION) {
      FileTracker.DATA_DIR_TRACKER.setFile(chooser.getCurrentDirectory());
      MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)gmodel.getSelectedSeq();
      BufferedWriter bw = null;
      try {
	File fil = chooser.getSelectedFile();
	FileOutputStream fos = new FileOutputStream(fil);
	Das2FeatureSaxParser das_parser = new Das2FeatureSaxParser();
	das_parser.writeAnnotations(syms, aseq, annot_type, fos);
        fos.close();
      }
      catch (Exception ex) {
	ErrorHandler.errorPanel("Problem saving file", ex);
      } finally {
        if (bw != null) try {bw.close();} catch (IOException ioe) {}
      }
    }
  }

  public void saveAsBedFile(TierGlyph atier) {
    int childcount= atier.getChildCount();
    java.util.List syms = new ArrayList(childcount);
    for (int i=0; i<childcount; i++) {
      GlyphI child = atier.getChild(i);
      if (child.getInfo() instanceof SeqSymmetry) {
	syms.add(child.getInfo());
      }
    }
    System.out.println("Saving symmetries as BED file: "+ syms.size());
//    com.affymetrix.genometry.util.SeqUtils.printSymmetry((SeqSymmetry) syms.get(0));

    JFileChooser chooser = UniFileChooser.getFileChooser("Bed file (*.bed)", "bed");
    chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());

    int option = chooser.showSaveDialog(null);
    if (option == JFileChooser.APPROVE_OPTION) {
      FileTracker.DATA_DIR_TRACKER.setFile(chooser.getCurrentDirectory());
      MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)gmodel.getSelectedSeq();
      BufferedWriter bw = null;
      try {
	File fil = chooser.getSelectedFile();
	FileWriter fw = new FileWriter(fil);
	bw = new BufferedWriter(fw);
	BedParser.writeBedFormat(bw, syms, aseq);
	bw.close();
      }
      catch (Exception ex) {
	ErrorHandler.errorPanel("Problem saving file", ex);
      } finally {
        if (bw != null) try {bw.close();} catch (IOException ioe) {}
      }
    }
  }

  static void collectSyms(GlyphI gl, java.util.List syms) {
    Object info = gl.getInfo();
    if ((info != null)  && (info instanceof SeqSymmetry)) {
      syms.add((SeqSymmetry)info);
    }
    else if (gl.getChildCount() > 0) {
      // if no SeqSymmetry associated with glyph, descend and try children
      int child_count = gl.getChildCount();
      for (int i=0; i<child_count; i++) {
	collectSyms(gl.getChild(i), syms);
      }
    }
  }

  public void addSymCoverageTier(TierGlyph atier) {
    MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)gmodel.getSelectedSeq();
    int child_count = atier.getChildCount();

    java.util.List syms = new ArrayList(child_count);
    collectSyms(atier, syms);
    if (child_count == 0 || syms.size() == 0) {
      ErrorHandler.errorPanel("Empty Tier",
        "The selected tier is empty.  Can not make a coverage tier for an empty tier.");
      return;
    }

    SeqSymmetry union_sym = SeqSymSummarizer.getUnion(syms, aseq);
    SymWithProps wrapperSym;;
    if (union_sym instanceof SymWithProps) {
      wrapperSym = (SymWithProps) union_sym;
    } else {
      wrapperSym = new SimpleSymWithProps();
      ((SimpleSymWithProps) wrapperSym).addChild(union_sym);
      for (int i=0; i<union_sym.getSpanCount(); i++) {
        ((SimpleSymWithProps) wrapperSym).addSpan(union_sym.getSpan(i));
      }
    }

    String human_name = "coverage: " + atier.getLabel();
    //wrapperSym.setProperty("method", method);

    // Generate a non-persistent style.  
    // Factory will be CoverageSummarizerFactory because name starts with "coverage:"

    String unique_name = AnnotStyle.getUniqueName(human_name);
    wrapperSym.setProperty("method", unique_name);
    AnnotStyle style = AnnotStyle.getInstance(unique_name, false);
    style.setHumanName(human_name);
    style.setGlyphDepth(1);
    style.setSeparate(false); // there are not separate (+) and (-) strands
    style.setExpandable(false); // cannot expand and collapse
    style.setCustomizable(false); // the user can change the color, but not much else is meaningful

    aseq.addAnnotation(wrapperSym);
    gviewer.setAnnotatedSeq(aseq, true, true);
  }


  public void addSymSummaryTier(TierGlyph atier) {
    // not sure best way to collect syms from tier, but for now,
    //   just recursively descend through child glyphs of the tier, and if
    //   childA.getInfo() is a SeqSymmetry, add to symmetry list and prune recursion
    //   (don't descend into childA's children)
    java.util.List syms = new ArrayList();
    collectSyms(atier, syms);
    if (syms.size() == 0) {
      ErrorHandler.errorPanel("Nothing to Summarize",
        "The selected tier is empty. It contains nothing to summarize");
      return;
    }

    MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)gmodel.getSelectedSeq();
    String graphid = "summary: " + atier.getLabel();
    GraphSym gsym = SeqSymSummarizer.getSymmetrySummary(syms, aseq, false, graphid);
    gsym.setGraphName("depth: " + atier.getLabel());
    aseq.addAnnotation(gsym);
    gviewer.setAnnotatedSeq(aseq, true, true);
    GraphGlyph gl = (GraphGlyph)((SeqMapView)gviewer).getSeqMap().getItem(gsym);
    //    gl.setState(GraphGlyph.STAIRSTEP);
    gl.setGraphStyle(GraphGlyph.STAIRSTEP_GRAPH);
    gl.setColor(atier.getForegroundColor());
    // System.out.println("glyph: " + gl);
    // System.out.println("datamodel: " + gsym);
  }

  void refreshMap(boolean stretch_vertically) {
    if (gviewer != null) {
      // if an AnnotatedSeqViewer is being used, ask it to update itself.
      // later this can be made more specific to just update the tiers that changed
      gviewer.setAnnotatedSeq(gviewer.getAnnotatedSeq(), true, true);
    } else {
      // if no AnnotatedSeqViewer (as in simple test programs), update the tiermap itself.
      handler.repackTheTiers(false, stretch_vertically);
    }
  }

  public void popupNotify(javax.swing.JPopupMenu popup, TierLabelManager handler) {
    java.util.List labels = handler.getSelectedTierLabels();
    int num_selections = labels.size();
    boolean not_empty = ! handler.getAllTierLabels().isEmpty();

    boolean any_are_collapsed = false;
    boolean any_are_expanded = false;
    for (int i=0; i<labels.size(); i++) {
      TierLabelGlyph label = (TierLabelGlyph) labels.get(i);
      TierGlyph glyph = (TierGlyph) label.getInfo();
      IAnnotStyle style = glyph.getAnnotStyle();
      if (style.getExpandable()) {
        any_are_collapsed |= style.getCollapsed();
        any_are_expanded |= ! style.getCollapsed();
      }
    }

    select_all_tiers_action.setEnabled(true);
    customize_action.setEnabled(true);

    hide_action.setEnabled(num_selections > 0);
    show_all_action.setEnabled(not_empty);

    change_color_action.setEnabled(num_selections > 0);
    change_bg_color_action.setEnabled(num_selections > 0);
    //change_height_action.setEnabled(num_selections > 0);
    rename_action.setEnabled(num_selections == 1);

    collapse_action.setEnabled(any_are_expanded);
    expand_action.setEnabled(any_are_collapsed);
    change_expand_max_action.setEnabled(any_are_expanded);
    collapse_all_action.setEnabled(not_empty);
    expand_all_action.setEnabled(not_empty);
    change_expand_max_all_action.setEnabled(not_empty);
    showMenu.setEnabled(showMenu.getMenuComponentCount() > 0);

    save_bed_action.setEnabled(num_selections == 1);
    save_das_action.setEnabled(num_selections == 1);
    write_das_action.setEnabled(num_selections == 1);
    JMenu save_menu = new JMenu("Save Annotations");

    if (num_selections == 1) {
      // Check whether this selection is a graph or an annotation
      TierLabelGlyph label = (TierLabelGlyph) labels.get(0);
      TierGlyph glyph = (TierGlyph) label.getInfo();
      IAnnotStyle style = glyph.getAnnotStyle();
      boolean is_annotation_type = ! style.isGraphTier();
      sym_summarize_action.setEnabled(is_annotation_type);
      coverage_action.setEnabled(is_annotation_type);
      save_menu.setEnabled(is_annotation_type);
      save_bed_action.setEnabled(is_annotation_type);
      save_das_action.setEnabled(is_annotation_type);
    } else {
      sym_summarize_action.setEnabled(false);
      coverage_action.setEnabled(false);
      save_menu.setEnabled(false);
      save_bed_action.setEnabled(false);
      save_das_action.setEnabled(false);
    }

    changeMenu.removeAll();
    changeMenu.add(change_color_action);
    changeMenu.add(change_bg_color_action);
    //changeMenu.add(change_height_action);
    changeMenu.add(rename_action);

    popup.add(customize_action);
    popup.add(new JSeparator());
    popup.add(hide_action);
    popup.add(showMenu);
    popup.add(show_all_action);
    popup.add(new JSeparator());
    popup.add(select_all_tiers_action);
    popup.add(changeMenu);
    //popup.add(rename_action);
    //popup.add(change_color_action);
    popup.add(new JSeparator());
    popup.add(collapse_action);
    popup.add(expand_action);
    popup.add(change_expand_max_action);
    //popup.add(collapse_all_action);
    //popup.add(expand_all_action);
    //popup.add(change_expand_max_all_action);
    popup.add(new JSeparator());

    popup.add(save_menu);
    save_menu.add(save_bed_action);
    save_menu.add(save_das_action);
    //    save_menu.add(write_das_action);

    popup.add(new JSeparator());
    popup.add(sym_summarize_action);
    popup.add(coverage_action);

    if (DEBUG) {
      popup.add(new AbstractAction("DEBUG") {
        public void actionPerformed(ActionEvent e) {
          doDebugAction();
        }
      });
    }
  }

  // purely for debugging
  void doDebugAction() {
    if (DEBUG) {
      java.util.List current_tiers = handler.getSelectedTiers();
      Iterator iter = current_tiers.iterator();
      while (iter.hasNext()) {
        TierGlyph tg = (TierGlyph) iter.next();
        IAnnotStyle style = (IAnnotStyle) tg.getAnnotStyle();
        System.out.println("Tier: " + tg);
        System.out.println("Style: " + style);
      }
    }
  }
}
