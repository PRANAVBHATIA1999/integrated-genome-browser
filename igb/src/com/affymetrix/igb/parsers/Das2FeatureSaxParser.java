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

package com.affymetrix.igb.parsers;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.util.SeqUtils;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.das2.SimpleDas2Feature;

import com.affymetrix.igb.util.GenometryViewer; // for testing main

/**
 * Das2FeatureSaxParser reads and writes DAS2FEATURE XML format.
 *   Spec for this format is at http://biodas.org/documents/das2/das2_get.html
 *   DTD is at http://www.biodas.org/dtd/das2feature.dtd ???
*/
public class Das2FeatureSaxParser extends org.xml.sax.helpers.DefaultHandler
    implements AnnotationWriter  {

  // DO_SEQID_HACK is a very temporary fix!!!
  // Need to move to using full URI references to identify sequences,
  public static boolean DO_SEQID_HACK = true;
  static boolean DEBUG = false;
  static boolean REPORT_RESULTS = false;

  /**
   *  elements possible in DAS2 feature response
   */
  static final String FEATURES = "FEATURES";
  static final String FEATURE = "FEATURE";
  static final String LOC = "LOC";
  static final String REGION = "REGION";
  static final String XID = "XID";
  static final String PART = "PART";
  static final String PARENT = "PARENT";
  static final String ALIGN = "ALIGN";
  static final String PROP = "PROP";

  /**
   *  attributes possible in DAS2 feature response
   */
  static final String XMLBASE = "xml:base";   // common to all elements?
  static final String XMLLANG = "xml:lang";   // common to all elements?
  static final String ID = "id";              // FEATURE, PARENT, PART, LOC, REGION
  //  static final String TYPE = "type";      // replaced by "type_id"?
  static final String TYPEID = "type_id";     // FEATURE
  static final String NAME = "name";          // FEATURE
  static final String CREATED = "created";    // FEATURE
  static final String MODIFIED = "modified";  // FEATURE
  static final String DOC_HREF = "doc_href";  // FEATURE
  static final String MIME_TYPE = "mimetype";  // FORMAT, PROP
  static final String RANGE = "range";         // LOC, ALIGN
  // PROP attributes -- leaving out for now, not sure if common.rnc is current for t
  static final String TARGETID = "target_id";  // ALIGN
  static final String GAP = "gap";             // ALIGN


  //  static final String POS = "pos";  // in <LOC>
  //  static final String PTYPE = "ptype";  // in <PROP>

  //  static final String CONTENT_ENCODING = "content_encoding";  // in <PROP>
  //  static final String TGT = "tgt";  // in <ALIGN>

  /**
   *  built-in ptype attribute values possible for <PROP> element in DAS2 feature response
   */
  //  static final String NOTE_PROP = "das:note";
  //  static final String ALIAS_PROP = "das:alias";
  //  static final String PHASE_PROP = "das:phase";
  //  static final String SCORE_PROP = "das:score";

  static final Pattern range_splitter = Pattern.compile("/");
  static final Pattern interval_splitter = Pattern.compile(":");

  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();

  AnnotatedSeqGroup seqgroup = null;
  boolean add_annots_to_seq = false;

  String current_elem = null;  // current element
  StringBuffer current_chars = null;
  Stack elemstack = new Stack();
  Stack base_uri_stack = new Stack();
  URI current_base_uri = null;

  String feat_id = null;
  String feat_type = null;
  String feat_name = null;
  String feat_parent_id = null;
  String feat_created = null;
  String feat_modified = null;
  String feat_doc_href = null;
  //  String feat_prop_content = "";
  String feat_prop_key = null;
  String feat_prop_val = null;

  /**  list of SeqSpans specifying feature locations */
  List feat_locs = new ArrayList();
  List feat_xids = new ArrayList();
  /**
   *  map of child feature id to either:
   *      itself  (if child feature not parsed yet), or
   *      child feature object (if child feature already parsed)
   */
  Map feat_parts = new LinkedHashMap();
  List feat_aligns = new ArrayList();
  Map feat_props = null;

  /**
   *  lists for builtin feature properties
   *  not using yet (but clearing in clearFeature() just in case)
   */
  List feat_notes = new ArrayList();
  List feat_aliass = new ArrayList();
  List feat_phases = new ArrayList();
  List feat_scores = new ArrayList();

  /**
   *  List of feature jsyms resulting from parse
   */
  List result_syms = null;

  /**
   *  Need mapping so can connect parents and children after sym has already been created
   */
  Map id2sym = new HashMap();

  /**
   *  Need mapping of parent sym to map of child ids to connect parents and children
   */
  Map parent2parts = new HashMap();

  /**
   *  need mapping of parent id to child count for efficiently figuring out when
   *    symmetry is fully populated with children
   */

  /**
   *   setBaseURI should only be used when writing out DAS2XML
   *   (maybe should force specification of base URI in constructor?  
   *      then wuoldn't need extra url argument in parse() method...)
   */
  public void setBaseURI(URI base) { current_base_uri = base; }
  public URI getBaseURI() { return current_base_uri; }


  /**
   *  Parse a DAS2 features document.
   *  return value is List of all top-level features as symmetries.
   *
   *  uri argument is the URI the XML document was retrieved from 
   *  this argument is needed to ensure that Xml Base resolution is handled correctly 
   *      (sometimes can get base url from isrc.getSystemId(), but some InputSources may not have this set correctly)
   *     not sure if this strategy is currently handling URL redirects correctly... 
   * 
   *  if annot_seq, then feature symmetries will also be added as annotations to seqs in seq group
   *
   *  For example of situation where annot_seq = false:
   *   with standard IGB DAS2 access, don't want to add annotatons directly to seqs,
   *   but rather want them to be children of a Das2FeatureRequestSym (which in turn is a child of
   *   Das2ContainerAnnot [or possibly TypeContainerAnnot constructed by SmartAnnotSeq itself]),
   *   which in turn is directly attached to the seq as an annotation (giving two levels of additional
   *   annotation hierarchy)
   */
  public List parse(InputSource isrc, String uri, AnnotatedSeqGroup group, boolean annot_seq)  throws IOException, SAXException {
    clearAll();
    try  {
      //      URI source_uri = new URI(isrc.getSystemId());
      URI source_uri = new URI(uri);
      System.out.println("parsing XML doc, original URI = " + source_uri);
      current_base_uri = source_uri.resolve("");
      System.out.println("  initial base uri: " + current_base_uri);
      base_uri_stack.push(current_base_uri);
    }
    catch (Exception ex)  { ex.printStackTrace(); }
    add_annots_to_seq = annot_seq;

    /*
     *  result_syms get populated via callbacks from reader.parse(),
     *    eventually leading to result_syms.add() calls in addFeatue();
     */
    result_syms = new ArrayList();

    seqgroup = group;

    try {
      XMLReader reader = new org.apache.xerces.parsers.SAXParser();
      //      reader.setFeature("http://xml.org/sax/features/string-interning", true);
      reader.setFeature("http://xml.org/sax/features/validation", false);
      reader.setFeature("http://apache.org/xml/features/validation/dynamic", false);
      reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      reader.setContentHandler(this);
      reader.parse(isrc);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    System.out.println("finished parsing das2xml feature doc, number of top-level features: " + result_syms.size());
    if (REPORT_RESULTS) {
      for (int i=0; i<result_syms.size(); i++) {
	SeqUtils.printSymmetry((SeqSymmetry)result_syms.get(i));
      }
    }
    //    return aseq;
    return result_syms;

    //    clearAll();
  }

  /**
   *  implementing sax content handler interface
   */
  public void startDocument() {
    System.out.println("Das2FeaturesSaxParser.startDocument() called");
  }

  /**
   *  implementing sax content handler interface
   */
  public void endDocument() {
    //    System.out.println("Das2FeaturesSaxParser.endDocument() called");
  }

  /**
   *  implementing sax content handler interface
   */
  public void startElement(String uri, String name, String qname, Attributes atts) {
    if (DEBUG)  { System.out.println("start element: " + name); }
    elemstack.push(current_elem);
    current_elem = name.intern();
    String xml_base = atts.getValue("xml:base");
    if (xml_base != null)  {
      current_base_uri = current_base_uri.resolve(xml_base);
      System.out.println("resolved new base uri: " + current_base_uri);
    }
    // push base_uri onto stack whether it has changed or not
    base_uri_stack.push(current_base_uri);

    if (current_elem == FEATURES) {
    }
    else if (current_elem == FEATURE) {

      String feat_id_att = atts.getValue("id");
      feat_id = current_base_uri.resolve(feat_id_att).toString();
      String feat_type_att = atts.getValue("type");
      feat_type = current_base_uri.resolve(feat_type_att).toString();
      
      feat_name = atts.getValue("name");
      // feat_parent_id has moved to <PARENT> element
      //      feat_parent_id = atts.getValue("parent");
      feat_created = atts.getValue("created");
      feat_modified = atts.getValue("modified");
      feat_doc_href = atts.getValue("doc_href");

    }
    else if (current_elem == LOC)  {
      String seqid = atts.getValue("id");
      String range = atts.getValue("range");
      // DO_SEQID_HACK is a very temporary fix!!!
      // Need to move to using full URI references to identify sequences,
      if (DO_SEQID_HACK) { seqid = doSeqIdHack(seqid); }
      SeqSpan span = getLocationSpan(seqid, range, seqgroup);
      feat_locs.add(span);
    }
    else if (current_elem == XID) {
    }
    else if (current_elem == PARENT) {
      if (feat_parent_id == null) {
	feat_parent_id = atts.getValue("id");
	feat_parent_id = current_base_uri.resolve(feat_parent_id).toString();
      }
      else {
	System.out.println("WARNING:  multiple parents for feature, just using first one");
      }
    }
    else if (current_elem == PART) {
      String part_id = atts.getValue("id");
      part_id = current_base_uri.resolve(part_id).toString();
      /*
       *  Use part_id to look for child sym already constructed and placed in id2sym hash
       *  If child sym found then map part_id to child sym in feat_parts
       *  If child sym not found then map part_id to itself, and swap in child sym later when it's created
       */
      SeqSymmetry child_sym = (SeqSymmetry)id2sym.get(part_id);
      if (child_sym == null) {
	feat_parts.put(part_id, part_id);
      }
      else {
	feat_parts.put(part_id, child_sym);
      }
    }
    else if (current_elem == PROP) {
      feat_prop_key = atts.getValue("key");
      feat_prop_val = atts.getValue("value");
    }
    else if (current_elem == ALIGN) {
    }
    else {
      System.out.println("element not recognized: " + current_elem);
    }
  }

  public void clearAll() {
    result_syms = null;
    id2sym.clear();
    base_uri_stack.clear();
    current_base_uri = null;
    clearFeature();
  }

  public void clearFeature() {
    feat_id = null;
    feat_type = null;
    feat_name = null;
    feat_parent_id = null;
    feat_created = null;
    feat_modified = null;
    feat_doc_href = null;

    feat_locs.clear();
    feat_xids.clear();
    // making new feat_parts map because ref to old feat_parts map may be held for parent/child resolution
    feat_parts = new LinkedHashMap();
    feat_aligns.clear();

    feat_notes.clear();
    feat_aliass.clear();
    feat_phases.clear();
    feat_scores.clear();
    feat_props = null;
    //    feat_prop_content = "";
    feat_prop_key = null;
    feat_prop_val = null;
  }


  /**
   *  implementing sax content handler interface
   */
  public void endElement(String uri, String name, String qname)  {
    if (DEBUG)  { System.out.println("end element: " + name); }
    // only two elements that need post-processing are  <FEATURE> and <PROP> ?
    //   other elements are either top <FEATURES> or have only attributes
    if (name == FEATURE) {
      addFeature();
      clearFeature();
    }
    else if (name == PROP) {
      // need to process <PROP> elements after element is ended, because value may be in CDATA?
      // need to account for possibility that there are multiple property values of same ptype
      //    for such cases, make object that feat_prop_key maps to a List of the prop vals
      //
      // Update Feb2006 -- now that feature props use attribute value instead of content,
      //   should probably move this stuff up to the startElement() conditional for clarity,
      //   then can make feat_prop_key and feat_prop_val local to method
      if (feat_props == null) { feat_props = new HashMap(); }
      Object prev = feat_props.get(feat_prop_key);
      if (prev == null) {
	feat_props.put(feat_prop_key, feat_prop_val);
      }
      else if (prev instanceof List) {
	((List)prev).add(feat_prop_val);
      }
      else {
	List multivals = new ArrayList();
	multivals.add(prev);
	multivals.add(feat_prop_val);
	feat_props.put(feat_prop_key, multivals);
      }
      //      feat_prop_content = "";
      feat_prop_key = null;
      feat_prop_val = null;
      current_elem = (String)elemstack.pop();
    }

    // base_uri_stack.push(...) is getting called in every startElement() call,
    // so need to call base_uri_stack.pop() at end of every endElement() call;
    current_base_uri = (URI)base_uri_stack.pop();

  }

    /**
     *  implementing sax handler interface
     */
  public void characters(char[] ch, int start, int length) {
    // used to need to collect characters for property CDATA
    // but PROP now has data in attributes instead of content, so not needed anymore
    //    if (current_elem == PROP) {
    //      feat_prop_content += new String(ch, start, length);
    //    }
  }

  public void addFeature() {
    // checking to make sure feature with same id doesn't already exist
    //   (ids _should_ be unique, but want to make sure)
    if (id2sym.get(feat_id) != null) {
      System.out.println("WARNING, duplicate feature id: " + feat_id);
      return;
    }
    SimpleDas2Feature featsym = new SimpleDas2Feature(feat_id, feat_type, feat_name, feat_parent_id,
						      feat_created, feat_modified, feat_doc_href, feat_props);
    // add featsym to id2sym hash
    id2sym.put(feat_id, featsym);
    parent2parts.put(featsym, feat_parts);

    // add locations as spans...
    int loc_count = feat_locs.size();
    for (int i=0; i<loc_count; i++) {
      SeqSpan span = (SeqSpan)feat_locs.get(i);
      featsym.addSpan(span);
    }

    /*
     *  Add children _only_ if all children already have symmetries in feat_parts
     *  Otherwise need to wait till have all child syms, because need to be
     *     added to parent sym in order.
     *   add children if already parsed (should then be in id2sym hash);
     */
    if (feat_parts.size() > 0) {
      if (childrenReady(featsym)) {
	addChildren(featsym);
	//	parent2parts.remove(featsym);
      }
    }

    // if no parent, then attach directly to AnnotatedBioSeq(s)  (get seqid(s) from location)
    if (feat_parent_id == null) {
      for (int i=0; i<loc_count; i++) {
	SeqSpan span = (SeqSpan)feat_locs.get(i);
	BioSeq seq = span.getBioSeq();
	MutableAnnotatedBioSeq aseq = seqgroup.getSeq(seq.getID());  // should be a SmartAnnotBioSeq
	if ((seq != null) && (aseq != null) && (seq == aseq)) {
	  // really want an extra level of annotation here (add as child to a Das2FeatureRequestSym),
	  //    but Das2FeatureRequestSym is not yet implemented
	  //
	  result_syms.add(featsym);
	  if (add_annots_to_seq)  {
	    aseq.addAnnotation(featsym);
	  }
	}
      }
    }

    else {
      MutableSeqSymmetry parent = (MutableSeqSymmetry)id2sym.get(feat_parent_id);
      if (parent != null) {
	// add child to parent parts map
	LinkedHashMap parent_parts = (LinkedHashMap)parent2parts.get(parent);
	if (parent_parts == null)  {
	  System.out.println("WARNING: no parent_parts found for parent, id=" + feat_parent_id);
	}
	else  {
	  parent_parts.put(feat_id, featsym);
	  if (childrenReady(parent)) {
	    addChildren(parent);
	    //	  parent2parts.remove(parent_sym);
	  }
	}
      }
    }

  }


    protected boolean childrenReady(MutableSeqSymmetry parent_sym)  {
      LinkedHashMap parts = (LinkedHashMap)parent2parts.get(parent_sym);
      Iterator citer = parts.values().iterator();
      boolean all_child_syms = true;
      while (citer.hasNext()) {
	Object val = citer.next();
	if (! (val instanceof SeqSymmetry)) {
	  all_child_syms = false;
	  break;
	}
      }
      return all_child_syms;
    }


    protected void addChildren(MutableSeqSymmetry parent_sym)  {
      // get parts
      LinkedHashMap parts = (LinkedHashMap)parent2parts.get(parent_sym);
      Iterator citer = parts.entrySet().iterator();
      while (citer.hasNext()) {
	Map.Entry keyval = (Map.Entry)citer.next();
	String child_id = (String)keyval.getKey();
	SeqSymmetry child_sym = (SeqSymmetry)keyval.getValue();
	if (child_sym instanceof SymWithProps)  {
          String child_type = (String)((SymWithProps)child_sym).getProperty("type");
          if (child_type != null && child_type.endsWith("SO:intron"))  {
            // GAH 2-2006
	    // TEMPORARY HACK!! -- hardwiring to not add intron children from codesprint server
            //    once stylesheets etc. are in place, should be able to add introns
            //    but specify a line or null drawing style
            continue;
          }
	}
	parent_sym.addChild(child_sym);
      }
      //    id2sym.remove(parent_sym);
      parent2parts.remove(parent_sym);
    }

    /**
     *  Implementing AnnotationWriter interface to write out annotations
     *    to an output stream as "DAS2FEATURE" XML format.
     *
     *  getMimeType() should really return "text/x-das-feature+xml" but easier to debug as "text/plain"
     *    need to switch over once stabilized
     **/
    public String getMimeType() {
      //    return "text/x-das-feature+xml";
      return "text/plain";
    }

    /**
     *  Implementing AnnotationWriter interface to write out annotations
     *    to an output stream as "DASGFF" XML format.
     */
    public boolean writeAnnotations(java.util.Collection syms, BioSeq seq,
				    String type, OutputStream outstream) {
      boolean success = true;
      try {
	PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outstream)));

	// may need to extract seqid, seq version, genome for properly setting xml:base...
	// for now only way to specify xml:base is to explicitly set via this.setXmlBase()
	String seq_id = seq.getID();
	String seq_version = null;
	if (seq instanceof Versioned) {
	  seq_version = ((Versioned)seq).getVersion();
	}

	pw.println("<?xml version=\"1.0\" standalone=\"no\"?>");
	pw.println("<!DOCTYPE DAS2FEATURE SYSTEM \"http://www.biodas.org/dtd/das2feature.dtd\"> ");
	pw.println("<FEATURES  ");
	pw.println("   xmlns=\"http://www.biodas.org/ns/das/2.00\" ");
	pw.println("   xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
	//      pw.println("   xml:base=\"http:...\"> ");
	if (getBaseURI() != null) {
	  pw.println("   xml:base=\"" + getBaseURI().toString() + "\" >");
	}

	Iterator iterator = syms.iterator();
	while (iterator.hasNext()) {
	  SeqSymmetry annot = (SeqSymmetry)iterator.next();
	  writeDasFeature(annot, null, 0, seq, type, pw);
	}
	pw.println("</FEATURES>");
	pw.flush();
      }
      catch (Exception ex) {
	ex.printStackTrace();
	success = false;
      }
      return success;
    }


    /**
     *  Write out a SeqSymmetry in DAS2FEATURE format.
     *  Recursively descends to write out all descendants
     */
    public void writeDasFeature(SeqSymmetry annot, String parent_id, int parent_index,
				BioSeq aseq, String feat_type, PrintWriter pw) {
      if (feat_type == null && annot instanceof SymWithProps) {
	feat_type = (String)((SymWithProps)annot).getProperty("method");
	if (feat_type == null) {
	  feat_type = (String)((SymWithProps)annot).getProperty("meth");
	}
	if (feat_type == null) {
	  feat_type = (String)((SymWithProps)annot).getProperty("type");
	}
      }
      String feat_id = getChildID(annot, parent_id, parent_index);
      SeqSpan span = annot.getSpan(aseq);

      // print <FEATURE ...> line
      pw.print("  <FEATURE id=\"");
      pw.print(feat_id);
      pw.print("\" type=\"");
      pw.print(feat_type);
      pw.print("\" ");
      /*   parent has moved from being an attribute to being an element (zero or more)
	   writeDasFeature() currently does not handle multiple parents, only zero or one
	   if (parent_id != null) {
	   pw.print("parent=\"");
	   pw.print(parent_id);
	   pw.print("\" ");
	   }
      */
      pw.print(">");
      pw.println();

      // print  <LOC .../> line

      pw.print("     <LOC id=\"");
      pw.print(span.getBioSeq().getID());
      pw.print("\" range=\"");
      String range = getRangeString(span);
      pw.print(range);
      pw.print("\" />");
      pw.println();

      //  parent has moved from being an attribute to being an element (zero or more)
      //    writeDasFeature() currently does not handle multiple parents, only zero or one
      if (parent_id != null) {
	pw.print("     <PARENT id=\"");
	pw.print(parent_id);
	pw.print("\" />");
	pw.println();
      }

      // print  <PART .../> line for each child
      int child_count = annot.getChildCount();
      if (child_count > 0) {
	for (int i=0; i<child_count; i++) {
	  SeqSymmetry child = annot.getChild(i);
	  String child_id = getChildID(child, feat_id, i);
	  pw.print("     <PART id=\"");
	  pw.print(child_id);
	  pw.print("\" />");
	  pw.println();
	}
      }

      // also need to write out any properties (other than type, id, start, end, length, etc.....)

      // close this feature element
      pw.println("  </FEATURE>");

      // recursively call writeDasFeature() on each child
      if (child_count > 0) {
	for (int i=0; i<child_count; i++) {
	  SeqSymmetry child = annot.getChild(i);
	  writeDasFeature(child, feat_id, i, aseq, feat_type, pw);
	}
      }
    }


    protected String getChildID(SeqSymmetry child, String parent_id, int parent_index)  {
      String feat_id = null;
      if (child instanceof Propertied) {
	feat_id = (String)((Propertied)child).getProperty("id");
      }
      if (feat_id == null) {
	if (parent_id != null) {
	  feat_id = parent_id + "." + Integer.toString(parent_index);
	}
      }
      if (feat_id == null) {
	feat_id = "unknown";
      }
      return feat_id;
    }



    /**
     *  Get position span as a SeqSpan.
     *  Or should this be called parseRegion() ??
     *
     *  From the DAS2 spec:
     *----------------------------------------------
     *
     *  <LOC> (and possibly other elements?) have the following attribute syntax
     *    "id" attribute is the relative or absolute URI reference for the
     *        sequence/segment the feature is located
     *    "range" attribute combines the min, max and strand of the feature's range in the form:
     *        [min][:max][:strand]
     *    In other words, all three parts are optional.
     *    I think at least one of [min] or [:max] is required though

     *  min and max are the minimum and maximum values
     *  of a range on the sequence, and strand denotes the forward, reverse, or both strands of the
     *  sequence using -1,1,0 notation.
     *
     *    Chr1/1000	Chr1 beginning at position 1000 and going to the end.
     *    Chr1/1000:2000	Chr1 from positions 1000 to 2000.
     *    Chr1/:2000	Chr1 from the start to position 2000.
     *    Chr1/1000:2000:-1	The reverse complement of positions 1000 to 2000.
     *
     *  The semantics of the strand are simple when retrieving sequences.
     *  A value of -1 means reverse complement of min:max, and everything else indicates the forward strand.
     *  As described later, the semantics of strand are more subtle when used in the context of the location
     *    of a feature.
     *
     *  Regions are numbered so that min is always less than max. The strand designation is -1 to indicate
     *  a feature on the reverse strand, 1 to indicate a feature on the forward strand, and 0 to indicate
     *  a feature that is on both strands. Leaving the strand field empty implies a value of "unknown."
     *-------------------------------------------
     *
     *  For first cut, assuming that chromosome, min, and max is always present, and strand is always left out
     *     (therefore SeqSpan is forward)
     *
     *  Currently getPositionSpan() handles both with or without extra [xyz/]* prefix, and with or without strand
     *         region/seqid/min:max:strand OR
     *         seqid/min:max:strand
     *   but _not_ the case where there is no seqid, or no min, or no max
     */
    public static SeqSpan getLocationSpan(String seqrng, AnnotatedSeqGroup group) {
      int sindex =  seqrng.lastIndexOf("/");
      String seqid = seqrng.substring(0, sindex);
      String rng = seqrng.substring(sindex+1);
      return getLocationSpan(seqid, rng, group);
    }

    public static String getLocationSeqId(String seqrng) {
      int sindex =  seqrng.lastIndexOf("/");
      String seqid = seqrng.substring(0, sindex);
      return seqid;
    }

  /**
   *  This is a very temporary fix!!!
   *  Need to move to using full URI references to identify sequences,
   *      and optional name property to present to users
   */
  public static String doSeqIdHack(String seqid) {
    String new_seqid = seqid;
    int slash_index =  new_seqid.lastIndexOf("/");
    if (slash_index >= 0) { new_seqid = new_seqid.substring(slash_index+1); }
    return new_seqid;
  }

  public static SeqSpan getLocationSpan(String seqid, String rng, AnnotatedSeqGroup group) {
    if (seqid == null || rng == null) { return null; }
    String[] subfields = interval_splitter.split(rng);
    int min = Integer.parseInt(subfields[0]);
    int max = Integer.parseInt(subfields[1]);
    boolean forward = true;
    if (subfields.length >= 3) {
      if (subfields[2].equals("-1")) { forward = false; }
    }
    BioSeq seq = group.getSeq(seqid);
    if (seq == null) {
      seq = group.addSeq(seqid, 123123123);
    }
    SeqSpan span;
    if (forward)  {
      span = new SimpleSeqSpan(min, max, seq);
    }
    else {
      span = new SimpleSeqSpan(max, min, seq);
    }
    return span;
  }


    /**
     *  Generating a range string from a SeqSpan
     */
    public static String getRangeString(SeqSpan span)  {
      if (span == null) { return null; }
      StringBuffer buf = new StringBuffer(100);
      buf.append(Integer.toString(span.getMin()));
      buf.append(":");
      buf.append(Integer.toString(span.getMax()));
      if (span.isForward()) { buf.append(":1"); }
      else { buf.append(":-1"); }
      return buf.toString();
    }


    public static void main(String[] args) {
      boolean test_result_list = true;
      Das2FeatureSaxParser test = new Das2FeatureSaxParser();
      try {
	String test_file_name = "c:/data/das2_responses/codesprint/feature_query3.xml";
	File test_file = new File(test_file_name);
	FileInputStream fistr = new FileInputStream(test_file);
	BufferedInputStream bis = new BufferedInputStream(fistr);
	List annots = test.parse(new InputSource(bis), test_file_name, gmodel.addSeqGroup("test_group"), true);
	bis.close();
	System.out.println("annot count: " + annots.size());
	SeqSymmetry first_annot = (SeqSymmetry)annots.get(0);
	//      SeqUtils.printSymmetry(first_annot);
	AnnotatedSeqGroup group = gmodel.getSeqGroup("test_group");
	AnnotatedBioSeq aseq = group.getSeq(first_annot);
	System.out.println("seq id: " + aseq.getID());
	GenometryViewer viewer = GenometryViewer.displaySeq(aseq, false);
	viewer.setAnnotatedSeq(aseq);
      }
      catch (Exception ex) {
	ex.printStackTrace();
      }
    }


  }
