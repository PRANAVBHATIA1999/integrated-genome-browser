package com.affymetrix.igb.util;

import java.util.List;
import java.util.prefs.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.genometryImpl.SmartAnnotBioSeq;
import com.affymetrix.igb.das2.*;
import com.affymetrix.igb.view.SeqMapView;


public class ViewPersistenceUtils  {
  // For now need to use full URIs for DAS2 genome autoload defaults
//  public static String DEFAULT_DAS2_SERVER_URL = "http://netaffxdas.affymetrix.com/das2/sources";
  public static String DEFAULT_DAS2_SERVER_URL = (String)Das2Discovery.getDas2Urls().get(Das2Discovery.DEFAULT_DAS2_SERVER_NAME);
  public static String DEFAULT_DAS2_SOURCE_URI = "http://netaffxdas.affymetrix.com/das2/genome/H_sapiens";
  public static String DEFAULT_DAS2_VERSION_URI = "http://netaffxdas.affymetrix.com/das2/genome/H_sapiens_Mar_2006";
  public static String DEFAULT_SELECTED_GENOME = "H_sapiens_Mar_2006";
  //  public static String DEFAULT_SELECTED_SEQ = "http://netaffxdas.affymetrix.com/das2/H_sapiens_Mar_2006/chr21";
  public static String DEFAULT_SELECTED_SEQ = "chr21";

  public static boolean DEBUG = false;

  public static String GENOME_ID = "GENOME_ID";  // full genome ID if gets MD5-compressed in node creation
  public static String SEQ_ID = "SEQ_ID";  // full seq ID if gets MD5-compressed in node creation
  public static String DAS2_SERVER_URL_PREF = "DAS2_SERVER_URL_PREF";
  public static String DAS2_SOURCE_URI_PREF = "DAS2_SOURCE_URI_PREF";
  public static String DAS2_VERSION_URI_PREF = "DAS2_VERSION_URI_PREF";
  public static String SELECTED_GENOME_PREF = "SELECTED_GENOME_PREF";
  public static String SELECTED_SEQ_PREF = "SELECTED_SEQ_PREF";
  public static String SEQ_MIN_PREF = "SEQ_MIN_PREF";
  public static String SEQ_MAX_PREF = "SEQ_MAX_PREF";

  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();

  /**
   *  Saves information on current group
   *  Using Preferences node: [igb_root_pref]/genomes/[group_id]
   *  Using UnibrowPrefsUtils to convert node names if they are too long
   *  tagvals:
   *      GENOME_ID
   *      SEQ_ID
   *      SELECTED_GENOME_PREF
   *      SELECTED_SEQ_PREF
   *      DAS2_SERVER_URL_PREF
   *      DAS2_SOURCE_URI_PREF
   *      DAS2_VERSION_URI_PREF
   */
  public static void saveCurrentView(SeqMapView gviewer) {
    AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();
    MutableAnnotatedBioSeq seq = gmodel.getSelectedSeq();
    saveGroupSelection(group);
    saveSeqSelection(seq);
    saveSeqVisibleSpan(gviewer);
  }

  public static AnnotatedSeqGroup restoreLastView(SeqMapView gviewer) {
    AnnotatedSeqGroup group = restoreGroupSelection();
    if (group != null) {
      try {
	restoreSeqSelection(group);
	restoreSeqVisibleSpan(gviewer);
      } catch (Exception e) {
	e.printStackTrace();
      }
    }
    return group;
  }


  public static void saveGroupSelection(AnnotatedSeqGroup group) {
    Preferences genomes_node = UnibrowPrefsUtil.getGenomesNode();
     if (genomes_node == null || group == null) {
      return;
     }
     genomes_node.put(SELECTED_GENOME_PREF, group.getID());

     Preferences group_node = UnibrowPrefsUtil.getSubnode(genomes_node, group.getID(), true);
       //  encodes id via MD5 if too long, also remove forward slashes ("/")
     group_node.put(GENOME_ID, group.getID());  // preserve actual ID, no MD5 encoding, no slash removal

    //  get all accessed Das2VersionedSources that support SEGMENTS query
    List versions = Das2Discovery.getVersionedSources(group, false, Das2VersionedSource.SEGMENTS_CAP_QUERY);

    if (versions != null && versions.size() > 0) {
      Das2VersionedSource version = (Das2VersionedSource)versions.get(0);
      Das2Source source = version.getSource();
      Das2ServerInfo server = source.getServerInfo();
      group_node.put(DAS2_SERVER_URL_PREF, server.getID());
      group_node.put(DAS2_SOURCE_URI_PREF, source.getID());
      group_node.put(DAS2_VERSION_URI_PREF, version.getID());
    }

  }

  public static AnnotatedSeqGroup restoreGroupSelection() {
      Preferences genomes_node = UnibrowPrefsUtil.getGenomesNode();
      String group_id = genomes_node.get(SELECTED_GENOME_PREF, DEFAULT_SELECTED_GENOME);

      Preferences group_node = UnibrowPrefsUtil.getSubnode(genomes_node, group_id, true);
      //  encodes id via MD5 if too long, also remove forward slashes ("/")

      String server_url = group_node.get(DAS2_SERVER_URL_PREF, DEFAULT_DAS2_SERVER_URL);
      String source_id = group_node.get(DAS2_SOURCE_URI_PREF, DEFAULT_DAS2_SOURCE_URI);
      String version_id = group_node.get(DAS2_VERSION_URI_PREF, DEFAULT_DAS2_VERSION_URI);

      //HACK: steve, don't know how to activate debug mode....
      //    if (DEBUG) {
      System.out.println("Restoring group:");
      System.out.println("     " + server_url);
      System.out.println("     " + source_id);
      System.out.println("     " + version_id);

      Das2VersionedSource version = Das2ServerInfo.GetDas2Version(server_url, source_id, version_id,
              DEFAULT_DAS2_SERVER_URL,DEFAULT_DAS2_SOURCE_URI,DEFAULT_DAS2_VERSION_URI);
      if (version == null) {
          return null;
      }
      
      AnnotatedSeqGroup group = version.getGenome();  // adds genome to singleton genometry model if not already present
      // Calling version.getSegments() to ensure that Das2VersionedSource is populated with Das2Region segments,
      //    which in turn ensures that AnnotatedSeqGroup is populated with SmartAnnotBioSeqs
      version.getSegments();

      if (gmodel.getSelectedSeqGroup() != group) {
          gmodel.setSelectedSeqGroup(group);
      }
      return group;
  }


  

  

  
  /**
   *  Save information on which seq is currently being viewed
   *  Using Preferences node: [igb_root_pref]/genomes/[group_id], {SELECTED_SEQ_PREF ==> seq_id }
   *  Using UnibrowPrefUtils to convert node names if they are too long
   */
  public static void saveSeqSelection(AnnotatedBioSeq seq) {
    if (seq == null || !(seq instanceof SmartAnnotBioSeq))
        return;

      AnnotatedSeqGroup current_group = ((SmartAnnotBioSeq) seq).getSeqGroup();
      Preferences genomes_node = UnibrowPrefsUtil.getGenomesNode();
      Preferences group_node = UnibrowPrefsUtil.getSubnode(genomes_node, current_group.getID(), true);
      //  encodes id via MD5 if too long, removes slashes rather than make deeply nested node hierarchy
      group_node.put(SELECTED_SEQ_PREF, seq.getID());
  }


  public static MutableAnnotatedBioSeq restoreSeqSelection(AnnotatedSeqGroup group) {
    Preferences genomes_node = UnibrowPrefsUtil.getGenomesNode();
    Preferences group_node = UnibrowPrefsUtil.getSubnode(genomes_node, group.getID(), true);
         //  encodes id via MD5 if too long, removes slashes rather than make deeply nested node hierarchy
    String seq_id = group_node.get(SELECTED_SEQ_PREF, DEFAULT_SELECTED_SEQ);
    MutableAnnotatedBioSeq seq = group.getSeq(seq_id);
    // if selected or default seq can't be found, use first seq in group
    if (seq == null && group.getSeqCount() > 0) { seq = group.getSeq(0); }
    if (gmodel.getSelectedSeq() != seq) {
      gmodel.setSelectedSeq(seq);
    }
    return seq;
  }



  /**
   *  Saving visible span info for currently viewed seq
   *  Uses Preferences node: [igb_root_pref]/genomes/[group_id]/seqs/[seq_id]
   *                                {SEQ_MIN_PREF ==> viewspan.getMin() }
   *                                {SEQ_MAX_PREF ==> viewspan.getMax() }
   *                                {ID ==> seq_id }
   *  Using UnibrowPrefUtils to convert node names if they are too long
   */
  public static void saveSeqVisibleSpan(SeqMapView gviewer) {
    SeqSpan visible_span = gviewer.getVisibleSpan();
    if (visible_span != null) {
      BioSeq seq = visible_span.getBioSeq();
      if (seq instanceof SmartAnnotBioSeq) {
	AnnotatedSeqGroup group = ((SmartAnnotBioSeq)seq).getSeqGroup();
	Preferences genomes_node = UnibrowPrefsUtil.getGenomesNode();
	Preferences group_node = UnibrowPrefsUtil.getSubnode(genomes_node, group.getID(), true);  //  encodes id via MD5 if too long
        Preferences seqs_node = UnibrowPrefsUtil.getSubnode(group_node, "seqs");
        Preferences seq_node = UnibrowPrefsUtil.getSubnode(seqs_node, seq.getID(), true);  //  encodes id via MD5 if too long
	seq_node.put(SEQ_ID, seq.getID());   // in case node name is MD5 encoded
	seq_node.putInt(SEQ_MIN_PREF, visible_span.getMin());
	seq_node.putInt(SEQ_MAX_PREF, visible_span.getMax());
      }
    }
  }


  /**
   *  Assumes that correct seq has already been set in gviewer (usually due to gviewr bein a SeqSelectionListener on gmodel)
   */
  public static SeqSpan restoreSeqVisibleSpan(SeqMapView gviewer) {
    BioSeq seq = gviewer.getViewSeq();
    SeqSpan span = null;
    if (seq instanceof SmartAnnotBioSeq) {
      AnnotatedSeqGroup group = ((SmartAnnotBioSeq)seq).getSeqGroup();
      Preferences genomes_node = UnibrowPrefsUtil.getGenomesNode();
      Preferences group_node = UnibrowPrefsUtil.getSubnode(genomes_node, group.getID(), true);  //  encodes id via MD5 if too long
      Preferences seqs_node = UnibrowPrefsUtil.getSubnode(group_node, "seqs");
      Preferences seq_node = UnibrowPrefsUtil.getSubnode(seqs_node, seq.getID(), true);  //  encodes id via MD5 if too long
      int seq_min = seq_node.getInt(SEQ_MIN_PREF, 0);
      int seq_max = seq_node.getInt(SEQ_MAX_PREF, seq.getLength());
      span = new SimpleSeqSpan(seq_min, seq_max, seq);
      gviewer.zoomTo(span);
    }
    return span;
  }
}

