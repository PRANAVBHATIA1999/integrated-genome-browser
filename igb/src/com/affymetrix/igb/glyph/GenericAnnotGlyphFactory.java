/**
*   Copyright (c) 2001-2007 Affymetrix, Inc.
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

package com.affymetrix.igb.glyph;

import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.IAnnotStyleExtended;
import java.awt.*;
import java.util.*;

import com.affymetrix.genoviz.bioviews.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.util.*;
import com.affymetrix.genometry.span.SimpleMutableSeqSpan;
import com.affymetrix.genometry.symmetry.SimpleDerivedSeqSymmetry;
import com.affymetrix.genometry.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.*;
import com.affymetrix.igb.tiers.*;
import com.affymetrix.genometryImpl.parsers.TrackLineParser;
import com.affymetrix.genoviz.util.NeoConstants;
import com.affymetrix.igb.util.ObjectUtils;
import com.affymetrix.igb.view.SeqMapView;

/**
 *
 * @version $Id$
 */
public final class GenericAnnotGlyphFactory implements MapViewGlyphFactoryI  {
  static boolean DEBUG = false;
  static boolean SET_PARENT_INFO = true;
  static boolean SET_CHILD_INFO = true;
  static boolean ADD_CHILDREN = true;
  static boolean OPTIMIZE_CHILD_MODEL = false;

  /** Set to true if the we can assume the container SeqSymmetry being passed
   *  to addLeafsToTier has all its leaf nodes at the same depth from the top.
   */
  static final boolean ASSUME_CONSTANT_DEPTH = true;

  static Class default_eparent_class = (new EfficientLineContGlyph()).getClass();
  static Class default_echild_class = (new EfficientFillRectGlyph()).getClass();
  static Class default_elabelled_parent_class = (new EfficientLabelledLineGlyph()).getClass();

  static int DEFAULT_THICK_HEIGHT = 25;
  static int DEFAULT_THIN_HEIGHT = 15;

  SeqMapView gviewer;
  int glyph_depth = 2;  // default is depth = 2 (only show leaf nodes and parents of leaf nodes)

  MutableSeqSpan model_span = new SimpleMutableSeqSpan();
  SymWithProps placeholder = new SimpleSymWithProps();
  Class parent_glyph_class;
  Class child_glyph_class;
  Class parent_labelled_glyph_class;
  public boolean DRAW_ARROWS = false;


  public GenericAnnotGlyphFactory() {
    parent_glyph_class = default_eparent_class;
    child_glyph_class = default_echild_class;
    parent_labelled_glyph_class = default_elabelled_parent_class;
  }

  public void init(Map options) {
    if (DEBUG) {System.out.println("     @@@@@@@@@@@@@     in GenericAnnotGlyphFactory.init(), props: " + options);}

    String parent_glyph_name = (String)options.get("parent_glyph");
    if (parent_glyph_name != null) {
      try {
        parent_glyph_class = ObjectUtils.classForName(parent_glyph_name);
      }
      catch (Exception ex) {
        System.err.println();
        System.err.println("WARNING: Class for parent glyph not found: " + parent_glyph_name);
        System.err.println();
        parent_glyph_class = default_eparent_class;
      }
    }
    String child_glyph_name = (String)options.get("child_glyph");
    if (child_glyph_name != null) {
      try {
        child_glyph_class = ObjectUtils.classForName(child_glyph_name);
      }
      catch (Exception ex) {
        System.err.println();
        System.err.println("WARNING: Class for child glyph not found: " + child_glyph_name);
        System.err.println();
        child_glyph_class = default_echild_class;
      }
    }
  }

  public void createGlyph(SeqSymmetry sym, SeqMapView smv) {
    MutableAnnotatedBioSeq aseq = smv.getAnnotatedSeq();
    MutableAnnotatedBioSeq vseq = smv.getViewSeq();
    if (SeqMapView.DEBUG_COMP)  {
      System.out.println("called GenericAnnotGlyphFactory.createGlyph(sym,smv), " +
			 "annotated_seq = " + aseq.getID() + ", view_seq = " + vseq.getID() + ", " + (aseq == vseq));
      if (aseq != vseq) {
	SeqSymmetry comp = ((SmartAnnotBioSeq)vseq).getComposition();
	SeqUtils.printSymmetry(comp);
      }
    }
    createGlyph(sym, smv, false);
  }

  public void createGlyph(SeqSymmetry sym, SeqMapView smv, boolean next_to_axis) {
    setMapView(smv);
    //AffyTieredMap map = gviewer.getSeqMap();

	/*
    if (sym instanceof GFF3Sym) {
      GFF3GlyphFactory.getInstance().createGlyph(sym, smv, next_to_axis);
      return;
    }
	 */

    String meth = SmartAnnotBioSeq.determineMethod(sym);

    if (meth != null) {
      IAnnotStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(meth);
      glyph_depth = style.getGlyphDepth();

      TierGlyph[] tiers = smv.getTiers(meth, next_to_axis, style);
      if (style.getSeparate()) {
        addLeafsToTier(sym, tiers[0], tiers[1], glyph_depth);
      } else {
        // use only one tier
        addLeafsToTier(sym, tiers[0], tiers[0], glyph_depth);
      }
    }
    else {  // keep recursing down into child syms if parent sym has no "method" property
      int childCount = sym.getChildCount();
      for (int i=0; i<childCount; i++) {
        SeqSymmetry childSym = sym.getChild(i);
        createGlyph(childSym, gviewer, false);
	//        createGlyph(childSym, gviewer, next_to_axis);
      }
    }
  }


  public void setMapView(SeqMapView smv) {
    gviewer = smv;
  }

  int getDepth(SeqSymmetry sym) {
    int depth = 1;
    SeqSymmetry current = sym;
    if (ASSUME_CONSTANT_DEPTH) {
      while (current.getChildCount() != 0) {
        current = current.getChild(0);
        depth++;
      }
    } else {
      depth = SeqUtils.getDepth(sym);
    }
    return depth;
  }

  public void addLeafsToTier(SeqSymmetry sym,
                             TierGlyph ftier, TierGlyph rtier,
                             int desired_leaf_depth) {

	  /*
    if (sym instanceof GFF3Sym) {
      GFF3GlyphFactory.getInstance().createGlyph(sym, gviewer);
      return;
    }
	   */

    int depth = getDepth(sym);
    if (depth > desired_leaf_depth || sym instanceof TypeContainerAnnot) {
      for (int i=0; i<sym.getChildCount(); i++) {
        SeqSymmetry child = sym.getChild(i);
        addLeafsToTier(child, ftier, rtier, desired_leaf_depth);
      }
    }
    else if (depth < 1) {
      System.out.println("############## in GenericAnnotGlyphFactory, should never get here???");
    }
    else {  // depth == desired_leaf_depth
      addToTier(sym, ftier, rtier, (depth >= 2));
    }
  }

  static Color getSymColor(SeqSymmetry insym, IAnnotStyleExtended style) {
    boolean use_score_colors = style.getColorByScore();
    boolean use_item_rgb = "on".equalsIgnoreCase((String) style.getTransientPropertyMap().get(TrackLineParser.ITEM_RGB));

    if (! (use_score_colors || use_item_rgb)) {
      return style.getColor();
    }

    SeqSymmetry sym = insym;
    if (insym instanceof DerivedSeqSymmetry) {
      sym = (SymWithProps)  getMostOriginalSymmetry(insym);
      //sym = ((DerivedSeqSymmetry) insym).getOriginalSymmetry();
    }

    if (use_item_rgb && sym instanceof SymWithProps) {
      Color cc = (Color) ((SymWithProps) sym).getProperty(TrackLineParser.ITEM_RGB);
      if (cc != null) return cc;
    }
    if (use_score_colors && sym instanceof Scored) {
      float score = ((Scored) sym).getScore();
      if (score != Float.NEGATIVE_INFINITY && score > 0.0f) {
        return style.getScoreColor(score);
      }
    }

    return style.getColor();
  }

  boolean allows_labels = true;

  /**
   *  @param parent_and_child  Whether to draw this sym as a parent and
   *    also draw its children, or to just draw the sym itself
   *   (using the child glyph style).  If this is set to true, then
   *    the symmetry must have a depth of at least 2.
   */
  public GlyphI addToTier(SeqSymmetry insym,
                          TierGlyph forward_tier,
                          TierGlyph reverse_tier,
                          boolean parent_and_child) {

    GlyphI g = null;

    try {
      if (parent_and_child && insym.getChildCount() > 0) {
        g = doTwoLevelGlyph(insym, forward_tier, reverse_tier);
      } else {
        // depth !>= 2, so depth <= 1, so _no_ parent, use child glyph instead...
        g = doSingleLevelGlyph(insym, forward_tier, reverse_tier);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return g;
  }


  /**
   *   Creation of genoviz Glyphs for rendering
   *      a two-level symmetry (parent with children) in the SeqMapView 
   *      includes transformations used by slice view and other alternative coordinate systems
   */
  public GlyphI doTwoLevelGlyph(SeqSymmetry insym, TierGlyph forward_tier, TierGlyph reverse_tier)
  throws java.lang.InstantiationException, java.lang.IllegalAccessException {

    AffyTieredMap map = gviewer.getSeqMap();
    MutableAnnotatedBioSeq annotseq = gviewer.getAnnotatedSeq();
    MutableAnnotatedBioSeq coordseq = gviewer.getViewSeq();
    SeqSymmetry sym = insym;
    boolean same_seq = (annotseq == coordseq);

    if (! same_seq) {
      sym = gviewer.transformForViewSeq(insym, annotseq);
    }

    SeqSpan pspan = gviewer.getViewSeqSpan(sym);

    if (pspan == null || pspan.getLength() == 0) {
      return null;
    }  // if no span corresponding to seq, then return;

    TierGlyph the_tier = pspan.isForward() ? forward_tier : reverse_tier;

    GlyphI pglyph = null;

    double thick_height = DEFAULT_THICK_HEIGHT;
    double thin_height = DEFAULT_THIN_HEIGHT;

    // I hate having to do this cast to AnnotStyle.  But how can I avoid it?
    IAnnotStyleExtended the_style = (IAnnotStyleExtended) the_tier.getAnnotStyle();

    //double thick_height = the_style.getHeight();
    //double thin_height = the_style.getHeight() * 3.0/5.0;

    // Note: Setting parent height (pheight) larger than the child height (cheight)
    // allows the user to select both the parent and the child as separate entities
    // in order to look at the properties associated with them.  Otherwise, the method
    // EfficientGlyph.pickTraversal() will only allow one to be chosen.
    double pheight = thick_height + 0.0001;

    boolean use_label = false;
    String label_field = the_style.getLabelField();
    if (allows_labels) {
      use_label = (label_field != null && (label_field.trim().length()>0));
    }

    if (use_label) {
      LabelledGlyph lglyph = (LabelledGlyph)parent_labelled_glyph_class.newInstance();
      Object property = getTheProperty(insym, label_field);
      String label = (property == null) ? "" : property.toString();
      if (the_tier.getDirection() == TierGlyph.DIRECTION_REVERSE) {
        lglyph.setLabelLocation(LabelledGlyph.SOUTH);
      } else { lglyph.setLabelLocation(LabelledGlyph.NORTH); }
      //          System.out.println("using label: " + label);

      if (DRAW_ARROWS && lglyph instanceof EfficientLabelledLineGlyph) {
        ((EfficientLabelledLineGlyph) lglyph).setArrowDirection(
            insym.getSpan(0).isForward() ? NeoConstants.RIGHT : NeoConstants.LEFT);
      }
      
      lglyph.setLabel(label);
      pheight = 2 * pheight;
      pglyph = lglyph;
    } else {
      pglyph = (GlyphI)parent_glyph_class.newInstance();
    }

    pglyph.setCoords(pspan.getMin(), 0, pspan.getLength(), pheight);
    pglyph.setColor(getSymColor(insym, the_style));
    if (SET_PARENT_INFO) {
      map.setDataModelFromOriginalSym(pglyph, sym);
    }

    SeqSpan cdsSpan = null;
    SeqSymmetry cds_sym = null;

    if ((insym instanceof SupportsCdsSpan) && ((SupportsCdsSpan)insym).hasCdsSpan() )  {
      cdsSpan = ((SupportsCdsSpan)insym).getCdsSpan();
      MutableSeqSymmetry tempsym = new SimpleMutableSeqSymmetry();
      tempsym.addSpan(new SimpleMutableSeqSpan(cdsSpan));
      if (! same_seq) {
        SeqUtils.transformSymmetry(tempsym, gviewer.getTransformPath());
        cdsSpan = gviewer.getViewSeqSpan(tempsym);
      }
      cds_sym = tempsym;
    }

    if (ADD_CHILDREN) {
      int childCount = sym.getChildCount();

      java.util.List<SeqSymmetry> outside_children = new ArrayList<SeqSymmetry>();
      for (int i=0; i<childCount; i++) {
        SeqSymmetry child = null;
        SeqSpan cspan = null;
        child = sym.getChild(i);

        cspan = gviewer.getViewSeqSpan(child);

	if (cspan == null)  {   // if no span for view, then child is either to left or right of view
	  outside_children.add(child);  // collecting children outside of view to handle later
	}
	else  {
	  GlyphI cglyph;
	  if (cspan.getLength() == 0) {
	    cglyph = new DeletionGlyph();
	  } 
	  else {
	    cglyph = (GlyphI)child_glyph_class.newInstance();
	  }
	
	  double cheight = thick_height;
	  Color child_color = getSymColor(child, the_style);
	  if (cdsSpan != null) {
	    cheight = thin_height;
	    if (SeqUtils.contains(cdsSpan, cspan)) { cheight = thick_height; } 
	    else if (SeqUtils.overlap(cdsSpan, cspan)) {

	      SeqSymmetry cds_sym_2 = SeqUtils.intersection(cds_sym, child, annotseq);
	      SeqSymmetry cds_sym_3 = cds_sym_2;
	      if (! same_seq) {
		cds_sym_3 = gviewer.transformForViewSeq(cds_sym_2, annotseq);
	      }
	      SeqSpan cds_span = gviewer.getViewSeqSpan(cds_sym_3);
	      if (cds_span != null) {
		GlyphI cds_glyph;
		if (cspan.getLength() == 0) {
		  cds_glyph = new DeletionGlyph();
		} else {
		  cds_glyph = (GlyphI)child_glyph_class.newInstance();
		}
		cds_glyph.setCoords(cds_span.getMin(), 0, cds_span.getLength(), thick_height);
		cds_glyph.setColor(child_color); // CDS same color as exon
		pglyph.addChild(cds_glyph);
		if (SET_CHILD_INFO) {
		  map.setDataModelFromOriginalSym(cds_glyph, cds_sym_3);
		}
	      }
	    }
	  }  // END CDS rendering conditional
	  cglyph.setCoords(cspan.getMin(), 0, cspan.getLength(), cheight);
	  cglyph.setColor(child_color);
	  pglyph.addChild(cglyph);
	  if (SET_CHILD_INFO) {
	    map.setDataModelFromOriginalSym(cglyph, child);
	  }
	}
      }   // END child rendering loop

      // call out to handle rendering to indicate if any of the children of the 
      //    orginal annotation are completely outside the view
      DeletionGlyph.handleEdgeRendering(outside_children, pglyph, annotseq, coordseq, 0.0, thin_height);
    }     // END child rendering conditional

    the_tier.addChild(pglyph);
    return pglyph;
  }





  static Object getTheProperty(SeqSymmetry sym, String prop) {
    if (prop == null || (prop.trim().length()==0)) {
      return null;
    }
    SeqSymmetry original = getMostOriginalSymmetry(sym);

    if (original instanceof SymWithProps) {
      return ((SymWithProps) original).getProperty(prop);
    }
    return null;
  }


  static SeqSymmetry getMostOriginalSymmetry(SeqSymmetry sym) {
    if (sym instanceof DerivedSeqSymmetry) {
      return getMostOriginalSymmetry( ((DerivedSeqSymmetry) sym).getOriginalSymmetry() );
    }
    else return sym;
  }

  GlyphI doSingleLevelGlyph(SeqSymmetry insym, TierGlyph forward_tier, TierGlyph reverse_tier)
    throws java.lang.InstantiationException, java.lang.IllegalAccessException {

    AffyTieredMap map = gviewer.getSeqMap();
    MutableAnnotatedBioSeq annotseq = gviewer.getAnnotatedSeq();
    MutableAnnotatedBioSeq coordseq = gviewer.getViewSeq();
    SeqSymmetry sym = insym;
    boolean same_seq = (annotseq == coordseq);

    if (! same_seq) {
      sym = gviewer.transformForViewSeq(insym, annotseq);
    }

    SeqSpan pspan = gviewer.getViewSeqSpan(sym);
    if (pspan == null || pspan.getLength() == 0) {
      return null;
    }  // if no span corresponding to seq, then return;

    TierGlyph the_tier = pspan.isForward() ? forward_tier : reverse_tier;

    GlyphI pglyph = null;

    // I hate having to do this cast to IAnnotStyleExtended.  But how can I avoid it?
    IAnnotStyleExtended the_style = (IAnnotStyleExtended) the_tier.getAnnotStyle();

    // Note: Setting parent height (pheight) larger than the child height (cheight)
    // allows the user to select both the parent and the child as separate entities
    // in order to look at the properties associated with them.  Otherwise, the method
    // EfficientGlyph.pickTraversal() will only allow one to be chosen.
    double pheight = DEFAULT_THICK_HEIGHT + 0.0001;

    boolean use_label = false;
    String label_field = the_style.getLabelField();
    if (allows_labels) {
      use_label = (label_field != null && (label_field.trim().length()>0));
    }

    if (use_label) {
      LabelledGlyph lglyph = (LabelledGlyph)parent_labelled_glyph_class.newInstance();
      Object property = getTheProperty(insym, label_field);
      String label = (property == null) ? "" : property.toString();
      if (the_tier.getDirection() == TierGlyph.DIRECTION_REVERSE) {
        lglyph.setLabelLocation(LabelledGlyph.SOUTH);
      } else { lglyph.setLabelLocation(LabelledGlyph.NORTH); }

      lglyph.setLabel(label);
      pheight = 2 * pheight;
      pglyph = lglyph;      
    } else {
      pglyph = (GlyphI)child_glyph_class.newInstance();
    }

    pglyph.setCoords(pspan.getMin(), 0, pspan.getLength(), pheight);
    pglyph.setColor(getSymColor(insym, the_style));
    if (SET_PARENT_INFO) {
      map.setDataModelFromOriginalSym(pglyph, sym);
    }

    the_tier.addChild(pglyph);
    return pglyph;
  }

  // Copies to a derived SeqSymmetry without copying any of the children.
  public static DerivedSeqSymmetry copyToDerivedNonRecursive(SeqSymmetry sym) {
    DerivedSeqSymmetry der = getDerived();

    der.clear(); // redundant, but ok

    if (sym instanceof DerivedSeqSymmetry) {
      der.setOriginalSymmetry(((DerivedSeqSymmetry)sym).getOriginalSymmetry());
    }
    else {
      der.setOriginalSymmetry(sym);
    }
    int spanCount = sym.getSpanCount();
    for (int i=0; i<spanCount; i++) {
      // just point to spans rather than copying them.
      // we can trust that the glyph factories won't modify these spans,
      // although it may add new spans.
      SeqSpan span = sym.getSpan(i);
      der.addSpan(span);
    }
    return der;
  }

  /** Used to manage a re-usable object pool. */
  static Stack<DerivedSeqSymmetry> derived_stack = new Stack<DerivedSeqSymmetry>();
  static final int max_stack_size = 100;

  /** Get an instance of DerivedSeqSymmetry from an object pool. */
  static DerivedSeqSymmetry getDerived() {
    if (derived_stack.isEmpty()) {
      return new SimpleDerivedSeqSymmetry();
    } else {
      return derived_stack.pop();
    }
  }

  /** Indicate that the given object is no longer in use and can be re-used later. */
  static void recycleDerived(DerivedSeqSymmetry sym) {
    for (int i=sym.getChildCount()-1; i>=0; i--) {
      recycleDerived((DerivedSeqSymmetry) sym.getChild(i));
    }

    sym.clear();
    sym.setOriginalSymmetry(null);
    if (derived_stack.size() < max_stack_size) {
      derived_stack.push(sym);
    }
  }

}
