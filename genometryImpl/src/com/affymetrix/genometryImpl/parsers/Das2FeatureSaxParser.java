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
package com.affymetrix.genometryImpl.parsers;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.seq.*;
import com.affymetrix.genometry.span.*;
import com.affymetrix.genometry.util.SeqUtils;
import com.affymetrix.genometryImpl.SmartAnnotBioSeq;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.genometryImpl.SymWithProps;
import com.affymetrix.genoviz.util.GeneralUtils;
import com.affymetrix.genometryImpl.das2.SimpleDas2Feature;

/**
 * Das2FeatureSaxParser reads and writes DAS2FEATURE XML format.
 *   Spec for this format is at http://biodas.org/documents/das2/das2_get.html *   DTD is at http://www.biodas.org/dtd/das2feature.dtd ???
 */
public class Das2FeatureSaxParser extends org.xml.sax.helpers.DefaultHandler
        implements AnnotationWriter {

    // DO_SEQID_HACK is a very temporary fix!!!
    // Need to move to using full URI references to identify sequences,
    public static boolean DO_SEQID_HACK = true;
    static boolean DEBUG = false;
    static boolean REPORT_RESULTS = false;
    static boolean REPORT_MULTI_LOC = true;
    static boolean REQUIRE_DAS2_NAMESPACE = false;    // ADD_NEW_SEQS_TO_GROUP should be true to allow opening a file in a "new" genome via File->Open
    static boolean ADD_NEW_SEQS_TO_GROUP = false;    
    //   "text/plain";
    //   "text/x-das-feature+xml";
    public static String FEATURES_CONTENT_TYPE = "application/x-das-features+xml";
    public static String FEATURES_CONTENT_SUBTYPE = "x-das-features+xml";
    public static String FILENAME_EXTENSION = "das2xml";
    public static String FILENAME_EXTENSION_WITH_DOT = "." + FILENAME_EXTENSION;    //  static String DAS2_NAMESPACE = "http://www.biodas.org/ns/das/2.00";
    public static String DAS2_NAMESPACE = "http://biodas.org/documents/das2";
    /**
     *  elements possible in DAS2 feature response
     */
    //  static final String FEATURES = "FEATURES";
    public static String FEATURELIST = "FEATURELIST";
    public static String FEATURES = "FEATURES";
    public static String FEATURE = "FEATURE";
    public static String LOC = "LOC";
    public static String XID = "XID";
    public static String PART = "PART";
    public static String PARENT = "PARENT";
    //  public static String ALIGN = "ALIGN";  // no longer have ALIGN elements, these are now also LOC elements
    public static String PROP = "PROP";
    public static String WRITEBACK = "WRITEBACK";
    /**
     *  attributes possible in DAS2 feature response
     */
    static public String XMLBASE = "xml:base";   // common to all elements?
    static public String XMLLANG = "xml:lang";   // common to all elements?
    static public String ID = "id";     // replaced by "uri"?
    static public String URID = "uri";  // FEATURE, PARENT, PART, LOC
    static public String TYPE = "type";      // replaced by "type_id"?
    static public String TYPEID = "type_id";     // FEATURE
    static public String TYPEURI = "type_uri";     // FEATURE
    static public String NAME = "name";          // replaced by "title"?
    static public String TITLE = "title";          // FEATURE
    static public String CREATED = "created";    // FEATURE
    static public String MODIFIED = "modified";  // FEATURE
    static public String DOC_HREF = "doc_href";  // FEATURE
    static public String MIME_TYPE = "mimetype";  // PROP
    static public String RANGE = "range";         // LOC
    static public String CIGAR = "gap";             // LOC
    static public String KEY = "key";             // PROP
    static public String VALUE = "value";         // PROP
    static public String SEGMENT = "segment";     // LOC

    // PROP attributes -- leaving out for now, not sure if common.rnc is current for t
    //  static final String PTYPE = "ptype";  // in <PROP>
    //  static final String CONTENT_ENCODING = "content_encoding";  // in <PROP>
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
    boolean add_to_sym_hash = true;
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
    //  List feat_aligns = new ArrayList();  // alignments are now merged with locations
    List feat_xids = new ArrayList();
    /**
     *  map of child feature id to either:
     *      itself  (if child feature not parsed yet), or
     *      child feature object (if child feature already parsed)
     */
    Map feat_parts = new LinkedHashMap();
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
     *  Mapping of parent sym to map of child ids to connect parents and children.
     */
    Map parent2parts = new HashMap();

    /*
     *  need mapping of parent id to child count for efficiently figuring out when
     *    symmetry is fully populated with children
     */

    /*
     *   setBaseURI should only be used when writing out DAS2XML
     *   (maybe should force specification of base URI in constructor?
     *      then wuoldn't need extra url argument in parse() method...)
     */
    public void setBaseURI(URI base) {
        current_base_uri = base;
    }

    public URI getBaseURI() {
        return current_base_uri;
    }

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
     *   with standard IGB DAS2 access, don't want to add annotatons directly to seqs, but rather want
     *   them to be children of a Das2FeatureRequestSym (which in turn is a child of TypeContainerAnnot
     *   constructed by SmartAnnotSeq itself [though may change this to a Das2ContainerAnnot in the future]),
     *   which in turn is directly attached to the seq as an annotation (giving two levels of additional
     *   annotation hierarchy)
     */
    public List parse(InputSource isrc, String uri, AnnotatedSeqGroup group, boolean annot_seq) throws IOException, SAXException {
        clearAll();
        try {
            //      URI source_uri = new URI(isrc.getSystemId());
            URI source_uri = new URI(uri);
            System.out.println("parsing XML doc, original URI = " + source_uri);
            current_base_uri = source_uri.resolve("");
            System.out.println("  initial base uri: " + current_base_uri);
            base_uri_stack.push(current_base_uri);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        add_annots_to_seq = annot_seq;

        /*
         *  result_syms get populated via callbacks from reader.parse(),
         *    eventually leading to result_syms.add() calls in addFeatue();
         */
        result_syms = new ArrayList();

        seqgroup = group;

        XMLReader reader = null;
        try {
            SAXParserFactory f = SAXParserFactory.newInstance();
            f.setNamespaceAware(true);
            reader = f.newSAXParser().getXMLReader();
            //      reader.setFeature("http://xml.org/sax/features/string-interning", true);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://apache.org/xml/features/validation/dynamic", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
            reader.parse(isrc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("finished parsing das2xml feature doc, number of top-level features: " + result_syms.size());
        if (REPORT_RESULTS) {
            for (int i = 0; i < result_syms.size(); i++) {
                SeqUtils.printSymmetry((SeqSymmetry) result_syms.get(i));
            }
        }

	System.out.println("feature constructor calls: "+ feature_constructor_calls);
	//    clearAll();
        //    return aseq;
        return result_syms;
    }

    /**
     *  implementing sax content handler interface.
     */
    public void startDocument() {
        System.out.println("Das2FeaturesSaxParser.startDocument() called");
    }

    /**
     *  implementing sax content handler interface.
     */
    public void endDocument() {
        //    System.out.println("Das2FeaturesSaxParser.endDocument() called");
    }

    /**
     *  implementing sax content handler interface.
     */
    public void startElement(String uri, String localName, String qname, Attributes atts)
            throws SAXException {
        // to be fully compliant with DAS/2 spec, should comply with XML namespaces, and therefore
        //     should make sure that the uri is the DAS/2 namespace URI
        //     because otherwise if there is arbitrary embedded XML, could have other elements with same localName
        //     (but they will have different namespace uri (or none)?)
        String name = localName;
        if (DEBUG) {
            System.out.println("start element: " + name);
        }

        elemstack.push(current_elem);
        current_elem = name.intern();
        String xml_base = atts.getValue(XMLBASE);
        if (xml_base != null) {
            current_base_uri = current_base_uri.resolve(xml_base);
            System.out.println("resolved new base uri: " + current_base_uri);
        }
        // push base_uri onto stack whether it has changed or not
        base_uri_stack.push(current_base_uri);
        
        
        // check to make sure elements are in DAS/2 namespace for standard handling
        if ((REQUIRE_DAS2_NAMESPACE) && !uri.equalsIgnoreCase(DAS2_NAMESPACE)) {
           // element is not in DAS/2 namespace
            // this may be some arbitrary XML mixed in with feature XML
            // if within the feature XML, should make a subtree of this XML and any subnodes, and attach
            //    as additional structured data as property of feature?
            System.out.println("element not recognized, and not within DAS2 namespace: " + current_elem);
            return;
        }
            
        if (current_elem.equals(FEATURELIST) || current_elem.equals(FEATURES) || current_elem.equals(XID)) {
            return;
        } 
        
        if (current_elem.equals(FEATURE)) {
            // feat_parent_id has moved to <PARENT> element
            //      feat_parent_id = atts.getValue("parent");
            parseFeature(atts);
        } else if (current_elem.equals(LOC)) {
            // DO_SEQID_HACK is a very temporary fix!!!
            // Need to move to using full URI references to identify sequences,
            parseLoc(atts);
        } else if (current_elem.equals(PARENT)) {
            parseParent(atts);
        } else if (current_elem.equals(PART)) {
            parsePart(atts);
        } else if (current_elem.equals(PROP)) {
            feat_prop_key = atts.getValue(KEY);
            feat_prop_val = atts.getValue(VALUE);
        } else {
            System.out.println("element not recognized, but within DAS2 namespace: " + current_elem);
        }
    }
    
    private void parseFeature(Attributes atts) throws SAXException {
        String feat_id_att = atts.getValue(URID);
        if (feat_id_att == null) {
            feat_id_att = atts.getValue(ID);
        } // for backward-compatibility
        try {
            feat_id = URLDecoder.decode(current_base_uri.resolve(feat_id_att).toString());
        } catch (IllegalArgumentException ioe) {
            throw new SAXException("Feature id uses illegal characters: '" + feat_id_att + "'");
        }
        // trying "type", "type_id", and "type_uri" for type attribute name
        String feat_type_att = atts.getValue(TYPE);
        if (feat_type_att == null) {
            feat_type_att = atts.getValue(TYPEID);
        } // for backward-compatibility
        if (feat_type_att == null) {
            feat_type_att = atts.getValue(TYPEURI);
        } // for backward-compatibility
        try {
            feat_type = URLDecoder.decode(current_base_uri.resolve(feat_type_att).toString());
        } catch (IllegalArgumentException ioe) {
            throw new SAXException("Feature type uses illegal characters: '" + feat_type_att + "'");
        }
        feat_name = atts.getValue(TITLE);
        if (feat_name == null) {
            feat_name = atts.getValue(NAME);
        } // for backward-compatibility
        // feat_parent_id has moved to <PARENT> element
        //      feat_parent_id = atts.getValue("parent");
        feat_created = atts.getValue(CREATED);
        feat_modified = atts.getValue(MODIFIED);
        feat_doc_href = atts.getValue(DOC_HREF);
    }

    private void parseLoc(Attributes atts) throws SAXException {
        String seqid_att = atts.getValue(SEGMENT);
        if (seqid_att == null) {
            seqid_att = atts.getValue(URID);
        }
        if (seqid_att == null) {
            seqid_att = atts.getValue(ID);
        }
        String seqid;
        try {
            seqid = URLDecoder.decode(current_base_uri.resolve(seqid_att).toString());
        } catch (IllegalArgumentException ioe) {
            throw new SAXException("Segment id uses illegal characters: '" + seqid_att + "'");
        }
        String range = atts.getValue(RANGE);
        String cigar = atts.getValue(CIGAR); // location can optionally have an alignment cigar string
        // DO_SEQID_HACK is a very temporary fix!!!
        // Need to move to using full URI references to identify sequences,
        if (DO_SEQID_HACK) {
            seqid = doSeqIdHack(seqid);
        }
        SeqSpan span = getLocationSpan(seqid, range, seqgroup);
        feat_locs.add(span);
    }

    private void parseParent(Attributes atts) throws SAXException {
        if (feat_parent_id == null) {
            feat_parent_id = atts.getValue(URID);
            if (feat_parent_id == null) {
                feat_parent_id = atts.getValue(ID);
            }
            try {
                feat_parent_id = URLDecoder.decode(current_base_uri.resolve(feat_parent_id).toString());
            } catch (IllegalArgumentException ioe) {
                throw new SAXException("Parent id uses illegal characters: '" + feat_parent_id + "'");
            }
        } else {
            System.out.println("WARNING:  multiple parents for feature, just using first one");
        }
    }

    private void parsePart(Attributes atts) throws SAXException {
        String part_id = atts.getValue(URID);
        if (part_id == null) {
            part_id = atts.getValue(ID);
        }
        try {
            part_id = URLDecoder.decode(current_base_uri.resolve(part_id).toString());
        } catch (IllegalArgumentException ioe) {
            throw new SAXException("Part id uses illegal characters: '" + part_id + "'");
        }
        /*
         *  Use part_id to look for child sym already constructed and placed in id2sym hash
         *  If child sym found then map part_id to child sym in feat_parts
         *  If child sym not found then map part_id to itself, and swap in child sym later when it's created
         */
        SeqSymmetry child_sym = (SeqSymmetry) id2sym.get(part_id);
        if (child_sym == null) {
            feat_parts.put(part_id, part_id);
        } else {
            feat_parts.put(part_id, child_sym);
        }
    }



    public void clearAll() {
	feature_constructor_calls = 0;
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
     *  implementing sax content handler interface.
     */
    public void endElement(String uri, String name, String qname) {
        if (DEBUG) {
            System.out.println("end element: " + name);
        }
        // only two elements that need post-processing are  <FEATURE> and <PROP> ?
        //   other elements are either top <FEATURELISTS> or have only attributes
        if (name.equals(FEATURE)) {
            addFeature();
            clearFeature();
        } else if (name.equals(PROP)) {
            // need to process <PROP> elements after element is ended, because value may be in CDATA?
            // need to account for possibility that there are multiple property values of same ptype
            //    for such cases, make object that feat_prop_key maps to a List of the prop vals
            //
            // Update Feb2006 -- now that feature props use attribute value instead of content,
            //   should probably move this stuff up to the startElement() conditional for clarity,
            //   then can make feat_prop_key and feat_prop_val local to method
            if (feat_props == null) {
                feat_props = new HashMap();
            }
            Object prev = feat_props.get(feat_prop_key);
            if (prev == null) {
                feat_props.put(feat_prop_key, feat_prop_val);
            } else if (prev instanceof List) {
                ((List) prev).add(feat_prop_val);
            } else {
                List multivals = new ArrayList();
                multivals.add(prev);
                multivals.add(feat_prop_val);
                feat_props.put(feat_prop_key, multivals);
            }
            //      feat_prop_content = "";
            feat_prop_key = null;
            feat_prop_val = null;
            current_elem = (String) elemstack.pop();
        }

        // base_uri_stack.push(...) is getting called in every startElement() call,
        // so need to call base_uri_stack.pop() at end of every endElement() call;
        current_base_uri = (URI) base_uri_stack.pop();

    }

    /**
     *  implementing sax handler interface.
     */
    public void characters(char[] ch, int start, int length) {
        // used to need to collect characters for property CDATA
        // but PROP now has data in attributes instead of content, so not needed anymore
        //    if (current_elem == PROP) {
        //      feat_prop_content += new String(ch, start, length);
        //    }
    }
    int dup_count = 0;

    int feature_constructor_calls = 0;
    public void addFeature() {
        // checking to make sure feature with same id doesn't already exist
        //   (ids _should_ be unique, but want to make sure)
        if (id2sym.get(feat_id) != null) {
            dup_count++;
            System.out.println("WARNING, duplicate feature id: " + feat_id);
            return;
        }
        //SimpleDas2Feature featsym = new SimpleDas2Feature(new URI(feat_id), feat_type, feat_name, feat_parent_id,

        SimpleDas2Feature featsym = new SimpleDas2Feature(feat_id, feat_type, feat_name, feat_parent_id,
                feat_created, feat_modified, feat_doc_href, feat_props);
	feature_constructor_calls++;
        // add featsym to id2sym hash
        id2sym.put(feat_id, featsym);
        parent2parts.put(featsym, feat_parts);

        // add locations as spans...
        int loc_count = feat_locs.size();
        for (int i = 0; i < loc_count; i++) {
            SeqSpan span = (SeqSpan) feat_locs.get(i);
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
            if (REPORT_MULTI_LOC && loc_count > 2) {
                System.out.println("loc count: " + loc_count);
            }
            for (int i = 0; i < loc_count; i++) {
                SeqSpan span = (SeqSpan) feat_locs.get(i);
                BioSeq seq = span.getBioSeq();
                //	System.out.println("top-level annotation created, seq = " + seq.getID());
                MutableAnnotatedBioSeq aseq = seqgroup.getSeq(seq.getID());  // should be a SmartAnnotBioSeq
                if ((seq != null) && (aseq != null) && (seq == aseq)) {
                    result_syms.add(featsym);
                    if (add_to_sym_hash) {
                        //	    System.out.println("adding to sym hash: " + featsym.getName() + ", " + featsym.getID());
                        seqgroup.addToIndex(featsym.getID(), featsym);
                        if (featsym.getName() != null) {
                            seqgroup.addToIndex(featsym.getName(), featsym);
                        }
                    }
                    if (add_annots_to_seq) {
                        aseq.addAnnotation(featsym);
                    }
                }
            }
        } else {
            MutableSeqSymmetry parent = (MutableSeqSymmetry) id2sym.get(feat_parent_id);
            if (parent != null) {
                // add child to parent parts map
                LinkedHashMap parent_parts = (LinkedHashMap) parent2parts.get(parent);
                if (parent_parts == null) {
                    System.out.println("WARNING: no parent_parts found for parent, id=" + feat_parent_id);
                } else {
                    parent_parts.put(feat_id, featsym);
                    if (childrenReady(parent)) {
                        addChildren(parent);
                    //	  parent2parts.remove(parent_sym);
                    }
                }
            }
        }

    }

    protected boolean childrenReady(MutableSeqSymmetry parent_sym) {
        LinkedHashMap parts = (LinkedHashMap) parent2parts.get(parent_sym);
        Iterator citer = parts.values().iterator();
        boolean all_child_syms = true;
        while (citer.hasNext()) {
            Object val = citer.next();
            if (!(val instanceof SeqSymmetry)) {
                all_child_syms = false;
                break;
            }
        }
        return all_child_syms;
    }

    protected void addChildren(MutableSeqSymmetry parent_sym) {
        // get parts
        LinkedHashMap parts = (LinkedHashMap) parent2parts.get(parent_sym);
        Iterator citer = parts.entrySet().iterator();
        while (citer.hasNext()) {
            Map.Entry keyval = (Map.Entry) citer.next();
            String child_id = (String) keyval.getKey();
            SeqSymmetry child_sym = (SeqSymmetry) keyval.getValue();
            if (child_sym instanceof SymWithProps) {
                String child_type = (String) ((SymWithProps) child_sym).getProperty("type");
                if (child_type != null && child_type.endsWith("SO:intron")) {
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
        return FEATURES_CONTENT_TYPE;
    }

    /**
     *  writes out annotations in DAS/2 feature XML, but wraps them in a <WRITEBACK> element?
     */
    public boolean writeBackAnnotations(java.util.Collection syms, BioSeq seq, String type, OutputStream outstream) {
        //  does not use seq arg, but still takes a seq arg to comply with AnnotationWriter interface (but can be null)

        boolean success = true;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outstream)));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<" + WRITEBACK + " xmlns=\"" + DAS2_NAMESPACE + "\">");
            //      pw.println("    xmlns=\"" + DAS2_NAMESPACE + "\"");
            //      pw.println("    xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
            //      pw.println(" >");
            pw.println("<" + FEATURES + ">");
            MutableSeqSpan mspan = new SimpleMutableSeqSpan();
            Iterator iterator = syms.iterator();
            while (iterator.hasNext()) {
                SeqSymmetry annot = (SeqSymmetry) iterator.next();
                // removed aseq argument from writeDasFeature() args, don't need any more since writing out all spans/LOCs
                //	  writeDasFeature(annot, null, 0, seq, type, pw, mspan);
                writeDasFeature(annot, null, 0, type, pw, mspan);
            }
            pw.println("</" + FEATURES + ">");
            pw.println("</" + WRITEBACK + ">");

            pw.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            success = false;
        }
        finally {
            GeneralUtils.safeClose(pw);
        }
        return success;
    }

    /**
     *  Implementing AnnotationWriter interface to write out annotations
     *    to an output stream as "DASGFF" XML format.
     */
    public boolean writeAnnotations(java.util.Collection<SeqSymmetry> syms, BioSeq seq, String type, OutputStream outstream) {
        // Das2FeatureSaxParser.writeAnnotations() does not use seq arg, since now writing out all spans
        //  but still takes a seq arg to comply with AnnotationWriter interface (but can be null)

        boolean success = true;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outstream)));

            // may need to extract seqid, seq version, genome for properly setting xml:base...
            // for now only way to specify xml:base is to explicitly set via this.setXmlBase()
            //	String seq_id = seq.getID();
            //	String seq_version = null;ons
            //	if (seq instanceof Versioned) {
            //	  seq_version = ((Versioned)seq).getVersion();
            //	}

            //	pw.println("<?xml version=\"1.0\" standalone=\"no\"?>");
            //	pw.println("<!DOCTYPE DAS2FEATURE SYSTEM \"http://www.biodas.org/dtd/das2feature.dtd\"> ");
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<" + FEATURES + " ");
            pw.println("    xmlns=\"" + DAS2_NAMESPACE + "\"");
            pw.println("    xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
            if (getBaseURI() != null) {
                String genome_id = "";
                if (seq instanceof SmartAnnotBioSeq) {
                    genome_id = ((SmartAnnotBioSeq) seq).getSeqGroup().getID();
                }
                String xbase = getBaseURI().toString() + genome_id + "/";
                //	  pw.println("   xml:base=\"" + getBaseURI().toString() + "\" >");
                pw.println("   " + XMLBASE + "=\"" + xbase + "\" >");
            } else {
                pw.println(" >");
            }

            MutableSeqSpan mspan = new SimpleMutableSeqSpan();
            Iterator iterator = syms.iterator();
            while (iterator.hasNext()) {
                SeqSymmetry annot = (SeqSymmetry) iterator.next();
                // removed aseq argument from writeDasFeature() args, don't need any more since writing out all spans/LOCs
                //	  writeDasFeature(annot, null, 0, seq, type, pw, mspan);
                writeDasFeature(annot, null, 0, type, pw, mspan);
            }
            pw.println("</" + FEATURES + ">");
            pw.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            success = false;
        }
        finally {
            GeneralUtils.safeClose(pw);
        }
        return success;
    }

    /**
     *  Write out a SeqSymmetry in DAS2FEATURE format.
     *  Recursively descends to write out all descendants.
     */
    public void writeDasFeature(SeqSymmetry annot, String parent_id, int parent_index,
            String feat_type, PrintWriter pw, MutableSeqSpan mspan) {
        // removed aseq argument from writeDasFeature() args, don't need any more since writing out all spans
        //	BioSeq aseq, String feat_type, PrintWriter pw, MutableSeqSpan mspan) {
        String feat_name = null;
        if (feat_type == null) {
            feat_type = SmartAnnotBioSeq.determineMethod(annot);
        }
        String feat_id = getChildID(annot, parent_id, parent_index);
        String feat_title = null;
        if (annot instanceof Propertied) {
            Propertied swp = (Propertied) annot;
            feat_title = (String) swp.getProperty("name");
            if (feat_title == null) {
                feat_title = (String) swp.getProperty("title");
            }
            if (feat_title == null) {
                feat_title = (String) swp.getProperty("gene_name");
            }
        }
        if (feat_title == null && annot.getChildCount() > 0) {
            feat_title = feat_id;
        }
        /*
        if (annot instanceof Named) {
        feat_title = ((Named)annot).getName();
        }
         */

        // print <FEATURE ...> line
        pw.print("  <FEATURE uri=\"");
        pw.print(URLEncoder.encode(feat_id));
        if (feat_title != null) {
            pw.print("\" title=\"");
            pw.print(feat_title);
        }
        pw.print("\" type=\"");
        pw.print(URLEncoder.encode(feat_type));
        pw.print("\" >");
        pw.println();

        
        /*
        // If there is a CDsSpan, then include it.
        if ((annot instanceof SupportsCdsSpan) && ((SupportsCdsSpan) annot).hasCdsSpan()) {
            SeqSpan span = ((SupportsCdsSpan) annot).getCdsSpan();
            pw.print("     <LOC segment=\"");
            pw.print(URLEncoder.encode(span.getBioSeq().getID()));
            pw.print("\" range=\"");
            String range = getRangeString(span);
            pw.print(range);
            pw.print("\" />");
            pw.println();
        }*/
        
        // print  all spans as <LOC .../> elements
        int scount = annot.getSpanCount();
        for (int i = 0; i < scount; i++) {
            SeqSpan span = null;
            if (mspan == null) {
                span = annot.getSpan(i);
            } else { // trying use of mspan for efficiency...
                annot.getSpan(i, mspan);
                span = mspan;
            }
            pw.print("     <LOC segment=\"");
            pw.print(URLEncoder.encode(span.getBioSeq().getID()));
            pw.print("\" range=\"");
            String range = getRangeString(span);
            pw.print(range);
            pw.print("\" />");
            pw.println();
        }

        /*
        pw.print("     <LOC segment=\"");
        pw.print(span.getBioSeq().getID());
        pw.print("\" range=\"");
        String range = getRangeString(span);
        pw.print(range);
        pw.print("\" />");
        pw.println();
         */

        //  parent has moved from being an attribute to being an element (zero or more)
        //    writeDasFeature() currently does not handle multiple parents, only zero or one
        if (parent_id != null) {
            pw.print("     <PARENT ");
            pw.print(URID);
            pw.print("=\"");
            pw.print(URLEncoder.encode(parent_id));
            pw.print("\" />");
            pw.println();
        }

        // print  <PART .../> line for each child
        int child_count = annot.getChildCount();
        if (child_count > 0) {
            for (int i = 0; i < child_count; i++) {
                SeqSymmetry child = annot.getChild(i);
                String child_id = getChildID(child, feat_id, i);
                pw.print("     <PART ");
                pw.print(URID);
                pw.print("=\"");
                pw.print(URLEncoder.encode(child_id));
                pw.print("\" />");
                pw.println();
            }
        }

        // also need to write out any properties (other than type, id, start, end, length, etc.....)
        //   but may want to leave them out when a compact format is desired rather than more detail

        // close this feature element
        pw.println("  </FEATURE>");

        // recursively call writeDasFeature() on each child
        if (child_count > 0) {
            for (int i = 0; i < child_count; i++) {
                SeqSymmetry child = annot.getChild(i);
                // removed aseq argument from writeDasFeature() args, don't need any more since writing out all spans
                //	  writeDasFeature(child, feat_id, i, aseq, feat_type, pw, mspan);
                writeDasFeature(child, feat_id, i, feat_type, pw, mspan);
            }
        }
    }

    // Get a child ID (return "unknown" if we can't determine it).
    // If there is a parent, then we differentiate the children by using the child_index.
    protected static String getChildID(SeqSymmetry child, String parent_id, int child_index) {
        String feat_id = child.getID();
        if (feat_id == null) {
            if (child instanceof Propertied) {
                feat_id = (String) ((Propertied) child).getProperty("id");
            }
            if (feat_id == null) {
                if (parent_id != null) {
                    feat_id = parent_id;
                }
                if (feat_id == null) {
                    return "unknown";
                }
            }
        }

        if (parent_id != null) {
            feat_id += "." + Integer.toString(child_index);
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
        int sindex = seqrng.lastIndexOf("/");
        String seqid = seqrng.substring(0, sindex);
        String rng = seqrng.substring(sindex + 1);
        return getLocationSpan(seqid, rng, group);
    }

    public static String getLocationSeqId(String seqrng) {
        int sindex = seqrng.lastIndexOf("/");
        String seqid = seqrng.substring(0, sindex);
        return seqid;
    }

    /**
     *  A temporary hack.
     *  This is a very temporary fix!!!
     *  Need to move to using full URI references to identify sequences,
     *      and optional name property to present to users
     */
    public static String doSeqIdHack(String seqid) {
        String new_seqid = seqid;
        int slash_index = new_seqid.lastIndexOf("/");
        if (slash_index >= 0) {
            new_seqid = new_seqid.substring(slash_index + 1);
        }
        return new_seqid;
    }

    public static SeqSpan getLocationSpan(String seqid, String rng, AnnotatedSeqGroup group) {
        if (seqid == null || rng == null) {
            return null;
        }
        String[] subfields = interval_splitter.split(rng);
        int min = Integer.parseInt(subfields[0]);
        int max = Integer.parseInt(subfields[1]);
        boolean forward = true;
        if (subfields.length >= 3) {
            if (subfields[2].equals("-1")) {
                forward = false;
            }
        }
        BioSeq seq;
        // need to revisit what to do if group == null
        if (group == null) {
            seq = new SimpleBioSeq(seqid, max);
        } else {
            if (ADD_NEW_SEQS_TO_GROUP) {
                // this will both create the seq and stretch its length if necessary.
                seq = group.addSeq(seqid, max);
            } else {
                seq = group.getSeq(seqid);
                if (seq == null) {
                    seq = new SimpleBioSeq(seqid, max);
                }
            }
        }
        SeqSpan span;
        if (forward) {
            span = new SimpleSeqSpan(min, max, seq);
        } else {
            span = new SimpleSeqSpan(max, min, seq);
        }
        return span;
    }

    public static String getRangeString(SeqSpan span) {
        return getRangeString(span, true);
    }

    /**
     *  Generating a range string from a SeqSpan
     */
    public static String getRangeString(SeqSpan span, boolean indicate_strand) {
        if (span == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(100);
        buf.append(Integer.toString(span.getMin()));
        buf.append(":");
        buf.append(Integer.toString(span.getMax()));
        if (indicate_strand) {
            if (span.isForward()) {
                buf.append(":1");
            } else {
                buf.append(":-1");
            }
        }
        return buf.toString();
    }
/*
    public static void main(String[] args) {
      boolean test_result_list = true;
      Das2FeatureSaxParser test = new Das2FeatureSaxParser();
      try {
	  //	String test_file_name = "c:/data/das2_responses/codesprint/feature_query3.xml";
	  //	String test_file_name = "c:/data/das2_responses/codesprint/genometry/features3.xml";
	  String test_file_name = System.getProperty("user.dir") + 
	      "/igb/test/test_files/das2_sample_features.das2xml";

	File test_file = new File(test_file_name);
	FileInputStream fistr = new FileInputStream(test_file);
	BufferedInputStream bis = new BufferedInputStream(fistr);
	boolean prev_add_seqs_to_group = ADD_NEW_SEQS_TO_GROUP;
	ADD_NEW_SEQS_TO_GROUP = true;
	List annots = test.parse(new InputSource(bis), test_file_name, gmodel.addSeqGroup("test_group"), true);
	ADD_NEW_SEQS_TO_GROUP = prev_add_seqs_to_group;
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

*/
  }
