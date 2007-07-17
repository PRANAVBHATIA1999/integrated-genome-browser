
package com.affymetrix.igb.view;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.*;
import com.affymetrix.genometry.symmetry.*;
import com.affymetrix.genometry.util.*;
import com.affymetrix.genometryImpl.*;
import com.affymetrix.genometryImpl.parsers.*;
import com.affymetrix.genometryImpl.util.*;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.menuitem.LoadFileAction;
import com.affymetrix.igb.menuitem.OpenGraphAction;
import com.affymetrix.igb.util.ErrorHandler;
import com.affymetrix.igb.util.GraphSymUtils;
import com.affymetrix.igb.util.LocalUrlCacher;
import com.affymetrix.igb.util.UnibrowPrefsUtil;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class QuickLoadServerModel {
  public static final String PREF_QUICKLOAD_CACHE_RESIDUES = "quickload_cache_residues";
  public static final String PREF_QUICKLOAD_CACHE_ANNOTS = "quickload_cache_annots";
  static String ENCODE_FILE_NAME = "encodeRegions.bed";
  static String ENCODE_FILE_NAME2 = "encode.bed";

  static boolean CACHE_RESIDUES_DEFAULT = false;
  static boolean CACHE_ANNOTS_DEFAULT = true;

  SingletonGenometryModel gmodel;

  static Pattern tab_regex = Pattern.compile("\t");

  String root_url;
  java.util.List genome_names = new ArrayList();

  Map group2name = new HashMap();
  Map genome2init = new HashMap();

  // A map from String genome name to a List of filenames on the server for that group
  Map genome2file_names = new HashMap();

  /**
   *  Map of AnnotatedSeqGroup to a load state map.
   *  Each load state map is a map of an annotation type name to Boolean for
   *  whether it has already been loaded or not
   */
  static Map group2states = new HashMap();


  public QuickLoadServerModel(SingletonGenometryModel gmodel, String url) {
    this.gmodel = gmodel;
    root_url = url;
    if (! root_url.endsWith("/")) {
      root_url = root_url + "/";
    }
    java.util.List xxx = loadGenomeNames();
    if (xxx == null || xxx.isEmpty()) {
      // do what?
    }
  }

  static Map url2quickload = new HashMap();

  public static QuickLoadServerModel getQLModelForURL(SingletonGenometryModel gmodel, URL url) {
    String ql_http_root = url.toExternalForm();
    if (! ql_http_root.endsWith("/")) {
      ql_http_root = ql_http_root + "/";
    }
    QuickLoadServerModel ql_server = (QuickLoadServerModel) url2quickload.get(ql_http_root);
    if (ql_server == null) {
      ql_server = new QuickLoadServerModel(gmodel, ql_http_root);
      url2quickload.put(ql_http_root, ql_server);
      LocalUrlCacher.loadSynonyms(SynonymLookup.getDefaultLookup(), ql_http_root+"synonyms.txt");
    }
    return ql_server;
  }

  static boolean getCacheResidues() {
    return UnibrowPrefsUtil.getBooleanParam(PREF_QUICKLOAD_CACHE_RESIDUES, CACHE_RESIDUES_DEFAULT);
  }

  static boolean getCacheAnnots() {
    return UnibrowPrefsUtil.getBooleanParam(PREF_QUICKLOAD_CACHE_ANNOTS, CACHE_ANNOTS_DEFAULT);
  }

  public String getRootUrl() { return root_url; }
  public java.util.List getGenomeNames() { return genome_names; }
  //public Map getSeqGroups() { return group2name; }
  public AnnotatedSeqGroup getSeqGroup(String genome_name) { return gmodel.addSeqGroup(genome_name);  }

  /** Returns the name that this QuickLoad server uses to refer to the given AnnotatedSeqGroup.
   *  Because of synonyms, different QuickLoad servers may use different names to
   *  refer to the same genome.
   */
  public String getGenomeName(AnnotatedSeqGroup group) {
    return (String)group2name.get(group);
  }

  public static String stripFilenameExtensions(String name) {
    String new_name = name;
    if (name.indexOf('.') > 0) {
      new_name = name.substring(0, name.lastIndexOf('.'));
    }
    return new_name;
  }

  /**
   *  Returns the list of String filenames that this QuickLoad server has
   *  for the genome with the given name.
   *  The list may (rarely) be empty, but never null.
   */
  public java.util.List getFilenames(String genome_name) {
    initGenome(genome_name);
    java.util.List filenames = (java.util.List) genome2file_names.get(genome_name);
    if (filenames == null) return Collections.EMPTY_LIST;
    else return filenames;
  }

  /** Returns Map of annotation type name to Boolean, true iff annotation type is already loaded */
  public static Map getLoadStates(AnnotatedSeqGroup group) {
    return (Map)group2states.get(group);
  }

  public static boolean getLoadState(AnnotatedSeqGroup group, String file_name) {
    Map load_states = getLoadStates(group);
    if (load_states == null) { return false; /* shouldn't happen */}
    Boolean boo = (Boolean)load_states.get(stripFilenameExtensions(file_name));
    if (boo == null) { return false; }
    else { return boo.booleanValue(); }
  }

  public static void setLoadState(AnnotatedSeqGroup group, String file_name, boolean loaded) {
    Map load_states = (Map) group2states.get(group);
    if (load_states == null) {
      load_states = new LinkedHashMap();
      group2states.put(group, load_states);
    }
    load_states.put(stripFilenameExtensions(file_name), Boolean.valueOf(loaded));
  }

  public boolean allow_reinitialization = false;
  
  public void initGenome(String genome_name) {
    if (genome_name == null) { return; }
    Boolean init = (Boolean)genome2init.get(genome_name);
    if (allow_reinitialization || init != Boolean.TRUE) {
      System.out.println("initializing data for genome: " + genome_name);
      boolean seq_init = loadSeqInfo(genome_name);
      boolean annot_init = loadAnnotationNames(genome_name);
      if (seq_init && annot_init) {
	genome2init.put(genome_name, Boolean.TRUE);
      }
    }
  }

  /**
   *  Determines the list of annotation files available in the genome directory.
   *  Looks for ~genome_dir/annots.txt file which lists annotation files
   *  available in same directory.  Returns true or false depending on
   *  whether the file is sucessfully loaded.
   *  You can retrieve the filenames with {@link #getFilenames(String)}
   */
  public boolean loadAnnotationNames(String genome_name) {
    boolean success = true;
    String genome_root = root_url + genome_name + "/";
    AnnotatedSeqGroup group = gmodel.getSeqGroup(genome_name);
    System.out.println("loading list of available annotations for genome: " + genome_name);
    String filename = genome_root + "annots.txt";

    InputStream istr = null;
    BufferedReader br = null;
    try {
      istr = LocalUrlCacher.getInputStream(filename, getCacheAnnots());
      br = new BufferedReader(new InputStreamReader(istr));
      String line;
      while ((line = br.readLine()) != null) {
        String[] fields = tab_regex.split(line);
        if (fields.length >= 1) {
          String annot_file_name = fields[0];
          //          System.out.println("    " + annot_file_name);
          java.util.List file_names = (java.util.List) genome2file_names.get(genome_name);
          if (file_names == null) {
            file_names = new ArrayList();
            genome2file_names.put(genome_name, file_names);
          }
          file_names.add(annot_file_name);
	  if (QuickLoadView2.build_virtual_encode &&
	      (annot_file_name.equalsIgnoreCase(ENCODE_FILE_NAME) || annot_file_name.equalsIgnoreCase(ENCODE_FILE_NAME2)) &&
	      (group.getSeq(QuickLoadView2.ENCODE_REGIONS_ID) == null) ) {
	    addEncodeVirtualSeq(group, (genome_root + annot_file_name));
	  }
        }
      }
      success = true;
    }
    catch (Exception ex) {
      System.out.println("Couldn't find or couldn't process file "+filename);
    } finally {
      if (istr != null) try { istr.close(); } catch (Exception e) {}
      if (br != null) try {br.close(); } catch (Exception e) {}
    }
    return success;
  }

  /**
   *  using negative start coord for virtual genome seq because (at least for human genome)
   *     whole genome start/end/length can't be represented with positive 4-byte ints (limit is +/- 2.1 billion)
   */
  double default_genome_min = -2100200300;
  boolean DEBUG_VIRTUAL_GENOME = false;
  public void addGenomeVirtualSeq(AnnotatedSeqGroup group) {
    int seq_count = group.getSeqCount();
    if (seq_count <= 1) {
      // no need to make a virtual "genome" seq if there is only a single chromosome
      return;
    }

    System.out.println("$$$$$ adding virtual genome seq to seq group");
    if (QuickLoadView2.build_virtual_genome &&
	(group.getSeq(QuickLoadView2.GENOME_SEQ_ID) == null) ) {
      SmartAnnotBioSeq genome_seq = group.addSeq(QuickLoadView2.GENOME_SEQ_ID, 0);
      for (int i=0; i<seq_count; i++) {
	BioSeq seq = group.getSeq(i);
	if (seq != genome_seq) {
	  double glength = genome_seq.getLengthDouble();
	  int clength = seq.getLength();
	  int spacer = (clength > 5000000) ? 5000000 : 100000;
	  double new_glength = glength + clength + spacer;
	  //	genome_seq.setLength(new_glength);
	  genome_seq.setBoundsDouble(default_genome_min, default_genome_min + new_glength);
	  if (DEBUG_VIRTUAL_GENOME)  {
	    System.out.println("added seq: " + seq.getID() + ", new genome bounds: min = " + genome_seq.getMin() +
			       ", max = " + genome_seq.getMax() + ", length = " + genome_seq.getLengthDouble());
	  }

	  MutableSeqSymmetry child = new SimpleMutableSeqSymmetry();
	  MutableSeqSymmetry mapping = (MutableSeqSymmetry)genome_seq.getComposition();
	  if (mapping == null) {
	    mapping = new SimpleMutableSeqSymmetry();
	    mapping.addSpan(new MutableDoubleSeqSpan(default_genome_min, default_genome_min + clength, genome_seq));
	    genome_seq.setComposition(mapping);
	  }
	  else {
	    MutableDoubleSeqSpan mspan = (MutableDoubleSeqSpan)mapping.getSpan(genome_seq);
	    mspan.setDouble(default_genome_min, default_genome_min + new_glength, genome_seq);
	  }

	  // using doubles for coords, because may end up with coords > MAX_INT
	  child.addSpan(new MutableDoubleSeqSpan(glength + default_genome_min, glength + clength + default_genome_min, genome_seq));
	  child.addSpan(new MutableDoubleSeqSpan(0, clength, seq));
	  if (DEBUG_VIRTUAL_GENOME) {
	    SeqUtils.printSpan(child.getSpan(0));
	    SeqUtils.printSpan(child.getSpan(1));
	  }
	  mapping.addChild(child);
	}
      }  // end loop through group's seqs
    }
  }

  /**
   *  addEncodeVirtualSeq.
   *  adds virtual CompositeBioSeq which is composed from all the ENCODE regions.
   *  assumes urlpath resolves to bed file for ENCODE regions
   */
  public void addEncodeVirtualSeq(AnnotatedSeqGroup seq_group, String urlpath)  {
    System.out.println("$$$$$ adding virtual encode seq to seq group");
    // assume it's a bed file...
    BedParser parser = new BedParser();
    try {
      InputStream istr= LocalUrlCacher.getInputStream(urlpath, getCacheAnnots());
      //      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(filepath)));
      java.util.List regions = parser.parse(istr, gmodel, seq_group, false, QuickLoadView2.ENCODE_REGIONS_ID, false);
      int rcount = regions.size();
      //      System.out.println("Encode regions: " + rcount);
      SmartAnnotBioSeq virtual_seq = seq_group.addSeq(QuickLoadView2.ENCODE_REGIONS_ID, 0);
      MutableSeqSymmetry mapping = new SimpleMutableSeqSymmetry();

      int min_base_pos = 0;
      int current_base = min_base_pos;
      int spacer = 20000;
      for (int i=0; i<rcount; i++) {
	SeqSymmetry esym = (SeqSymmetry)regions.get(i);
	SeqSpan espan = esym.getSpan(0);
	int elength = espan.getLength();

	SimpleSymWithProps child = new SimpleSymWithProps();
	String cid = esym.getID();
	if (cid != null) { child.setID(cid); }
	child.addSpan(espan);
	child.addSpan(new SimpleSeqSpan(current_base, current_base + elength, virtual_seq));
	mapping.addChild(child);
	current_base = current_base + elength + spacer;
      }
      virtual_seq.setBounds(min_base_pos, current_base);
      mapping.addSpan(new SimpleSeqSpan(min_base_pos, current_base, virtual_seq));
      virtual_seq.setComposition(mapping);
    }
    catch (Exception ex) {  ex.printStackTrace(); }
    return;
  }


  public boolean loadSeqInfo(String genome_name) {
    boolean success = false;
    String genome_root = root_url + genome_name + "/";
    AnnotatedSeqGroup group = gmodel.getSeqGroup(genome_name);
    System.out.println("loading list of chromosomes for genome: " + genome_name);
    InputStream lift_stream = null;
    InputStream cinfo_stream = null;
    try {

      System.out.println("lift URL: " + genome_root + "liftAll.lft");
      String lift_path = genome_root + "liftAll.lft";
      String cinfo_path = genome_root + "mod_chromInfo.txt";
      try {
        lift_stream = LocalUrlCacher.getInputStream(lift_path, getCacheAnnots());
      }
      catch (Exception ex) {
        System.out.println("couldn't find lift file, looking instead for mod_chromInfo file");
        lift_stream = null;
      }
      if (lift_stream == null) {
        try {
          cinfo_stream = LocalUrlCacher.getInputStream(cinfo_path,  getCacheAnnots());
        }
        catch (Exception ex) {
          System.err.println("ERROR: could find neither liftAll.txt nor mod_chromInfo.txt files");
          cinfo_stream = null;
        }
      }

      boolean annot_contigs = false;
      if (lift_stream != null) {
        LiftParser lift_loader = new LiftParser();
        group = lift_loader.parse(lift_stream, gmodel, genome_name, annot_contigs);
      }
      else if (cinfo_stream != null) {
        ChromInfoParser chrominfo_loader = new ChromInfoParser();
        group = chrominfo_loader.parse(cinfo_stream, gmodel, genome_name);
      }
      System.out.println("group: " + (group == null ? null : group.getID()) + ", " + group);
      //      gmodel.setSelectedSeqGroup(group);
      success = true;
      if (QuickLoadView2.build_virtual_genome) {  addGenomeVirtualSeq(group); }
    }
    catch (Exception ex) {
      ErrorHandler.errorPanel("ERROR", "Error loading data for genome '"+ genome_name +"'", ex);
    }
    finally {
      if (lift_stream != null)  try { lift_stream.close(); } catch (Exception e) {}
      if (cinfo_stream != null) try { cinfo_stream.close(); } catch (Exception e) {}
    }
    return success;
  }


  public java.util.List loadGenomeNames() {
    ArrayList glist = null;
    try {
      InputStream istr = null;
      try {
        istr = LocalUrlCacher.getInputStream(root_url + "contents.txt", getCacheAnnots());
      } catch (Exception e) {
        System.out.println("ERROR: Couldn't open '"+root_url+"contents.txt\n:  "+e.toString());
        istr = null; // dealt with below
      }
      if (istr == null) {
        System.out.println("Could not load QuickLoad contents from\n" + root_url + "contents.txt");
        return Collections.EMPTY_LIST;
      }
      InputStreamReader ireader = new InputStreamReader(istr);
      BufferedReader br = new BufferedReader(ireader);
      String line;
      glist = new ArrayList();
      while ((line = br.readLine()) != null) {
        AnnotatedSeqGroup group = null;
        String[] fields = tab_regex.split(line);
        if (fields.length >= 1) {
          String genome_name = fields[0];
          glist.add(genome_name);
          group = gmodel.addSeqGroup(genome_name);  // returns existing group if found, otherwise creates a new group
          genome_names.add(genome_name);
          group2name.put(group, genome_name);
          // System.out.println("added genome, name = " + line + ", group = " + group.getID() + ", " + group);
        }
        // if quickload server has description, and group is new or doesn't yet have description, add description to group
        if ((fields.length >= 2) && (group.getDescription() == null)) {
          group.setDescription(fields[1]);
        }
      }
      istr.close();
      ireader.close();
      br.close();
    }
    catch (Exception ex) {
      ErrorHandler.errorPanel("ERROR", "Error loading genome names", ex);
    }
    return glist;
  }

  public void loadAnnotations(AnnotatedSeqGroup current_group, String filename) {
    boolean loaded = getLoadState(current_group, filename);
    if (loaded) {
      System.out.println("already loaded: " + filename);
    }
    else {
      String annot_url = root_url + getGenomeName(current_group) + "/" + filename;
      System.out.println("need to load: " + annot_url);
      InputStream istr = null;
      BufferedInputStream bis = null;

      try {
        istr = LocalUrlCacher.askAndGetInputStream(annot_url, getCacheAnnots());
        if (istr != null) {
          bis = new BufferedInputStream(istr);

          if (GraphSymUtils.isAGraphFilename(filename)) {
            URL url = new URL(annot_url);
            java.util.List graphs = OpenGraphAction.loadGraphFile(url, current_group, gmodel.getSelectedSeq());
            if (graphs != null) {
              // Reset the selected Seq Group to make sure that the DataLoadView knows
              // about any new chromosomes that were added.
              gmodel.setSelectedSeqGroup(gmodel.getSelectedSeqGroup());
            }
          }
          else {
            LoadFileAction.load(Application.getSingleton().getFrame(), bis, filename, gmodel, gmodel.getSelectedSeq());
          }

          setLoadState(current_group, filename, true);
        }
      }
      catch (Exception ex) {
        ErrorHandler.errorPanel("ERROR", "Problem loading requested url:\n" + annot_url, ex);
        // keep load state false so we can load this annotation from a different server
        setLoadState(current_group, filename, false);
      } finally {
        if (bis != null) try {bis.close();} catch (Exception e) {}
        if (istr != null) try {istr.close();} catch (Exception e) {}
      }
    }
  }

  public String toString() {
    return "QuickLoadServerModel: url='" + getRootUrl() + "'";
  }
}
