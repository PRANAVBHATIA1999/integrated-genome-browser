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

package com.affymetrix.igb.genometry;

import java.util.*;

import com.affymetrix.genometry.*;

import com.affymetrix.igb.util.SynonymLookup;
import com.affymetrix.igb.event.SymMapChangeEvent;
import com.affymetrix.igb.event.SymMapChangeListener;

public class AnnotatedSeqGroup {

  String id;
  String organism;
  String version; // not currently used
  Date version_date;  // not currently used
  String description;

  boolean use_synonyms = true;

  // Using a sorted map to get the chromosome numbers in a reasonable order.
  // A better option may be to use a LinkedHashMap and simply make all users
  // of this class sort the sequences id's before adding them.
  //  SortedMap id2seq = new TreeMap(new ChromComparator());
  Map id2seq = new LinkedHashMap();
  ArrayList seqlist = new ArrayList();

  static Vector sym_map_change_listeners = new Vector(1);
  SortedMap id2sym_hash = new ListmakingHashMap();

  public AnnotatedSeqGroup(String gid) {
    id = gid;
  }

  public String getID() { return id; }

  /**
   *  Returns a List of MutableAnnotatedBioSeq objects.
   *  Will not return null.  The list is in the same order as in
   *  {@link #getSeq(int)}.
   */
  public List getSeqList() {
    if (seqlist.size() != id2seq.size()) {
      // lazily keep the seqlist up-to-date
      seqlist = new ArrayList(id2seq.values());
    }
    return seqlist;
  }

  /**
   *  Returns the sequence at the given position in the sequence list.
   */
  public MutableAnnotatedBioSeq getSeq(int index) {
    List the_list = getSeqList();
    if (index < the_list.size()) {
      return (MutableAnnotatedBioSeq) the_list.get(index);
    }
    else { return null; }
  }

  /** Returns the number of sequences in the group. */
  public int getSeqCount() {
    return id2seq.size();
  }

  /**
   *  Sets whether or not to use the SynonymLookup class to search for synonymous
   *  BioSeq's when using the getSeq(String) method.
   *  If you set this to false and then add new sequences, you should probably
   *  NOT later set it back to true unless you are sure you did not add
   *  some synonymous sequences.
   */
  public void setUseSynonyms(boolean b) {
    use_synonyms = b;
  }

  /** Gets a sequence based on its name, possibly taking synonyms into account.
   *  See {@link #setUseSynonyms(boolean)}.
   */
  public MutableAnnotatedBioSeq getSeq(String synonym) {
    MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)id2seq.get(synonym);
    if (use_synonyms && aseq == null) {
      // try and find a synonym
      SynonymLookup lookup = SynonymLookup.getDefaultLookup();
      Iterator iter = id2seq.values().iterator();
      while (iter.hasNext()) {
        MutableAnnotatedBioSeq synseq = (MutableAnnotatedBioSeq)iter.next();
        if (synseq instanceof SmartAnnotBioSeq)  {
	  if (((SmartAnnotBioSeq)synseq).isSynonymous(synonym)) {
	    aseq = synseq;
	    break;
	  }
	}
	else {
	  if (lookup.isSynonym(synseq.getID(), synonym)) {
	    aseq = synseq;
	    break;
	  }
	}
      }
    }
    return aseq;
  }

  /**
   *  For the given symmetry, tries to find in the group a sequence
   *    that is pointed to by that symmetry.
   *  @return the first sequence it finds (by iterating through sym's spans),
   *    or null if none is found.
   */
  public MutableAnnotatedBioSeq getSeq(SeqSymmetry sym) {
    MutableAnnotatedBioSeq result = null;
    if (sym != null) {
      int spancount = sym.getSpanCount();
      for (int i=0; i<spancount; i++) {
	SeqSpan span = sym.getSpan(i);
	BioSeq seq1 = span.getBioSeq();
	String id = seq1.getID();
	MutableAnnotatedBioSeq seq2 = (MutableAnnotatedBioSeq)id2seq.get(id);
	if ((seq2 != null) && (seq1 == seq2)) {
	  result = seq2;
	  break;
	}
      }
    }
    return result;
  }

  public boolean isSynonymous(String synonym) {
    if (id.equals(synonym)) { return true; }
    else {
      SynonymLookup lookup = SynonymLookup.getDefaultLookup();
      return (lookup.isSynonym(id, synonym));
    }
  }

  /**
   *  Returns the BioSeq with the given id (or synonym), creating it if necessary,
   *  and increasing its length to the given value if necessary.
   */
  public SmartAnnotBioSeq addSeq(String seqid, int length) {
    if (seqid == null) {
      throw new NullPointerException();
    }
    SmartAnnotBioSeq aseq;
    aseq = (SmartAnnotBioSeq) getSeq(seqid);
    if (aseq != null) {
      if (aseq.getLength() < length) {
        aseq.setLength(length);
      }
    }
    else {
      aseq = new SmartAnnotBioSeq(seqid, this.getID(), length);
      this.addSeq(aseq);
    }
    return aseq;
  }

  public void addSeq(MutableAnnotatedBioSeq seq) {
    MutableAnnotatedBioSeq oldseq = (MutableAnnotatedBioSeq)id2seq.get(seq.getID());
    if (oldseq == null) {
      id2seq.put(seq.getID(), seq);
      //seqlist.add(seq); // don't add to seqlist, to keep it properly sorted, rebuild it only when needed
      if (seq instanceof SmartAnnotBioSeq) {
	((SmartAnnotBioSeq)seq).setSeqGroup(this);
      }
    }
    else {
      throw new RuntimeException("ERROR! tried to add seq: " + seq.getID() + " to AnnotatedSeqGroup: " +
        this.getID() + ", but seq with same id is already in group");
    }
  }

  public void setDescription(String str) { description = str; }
  public String getDescription() { return description; }

  /** may want to move set/getOrganism() to an AnnotatedGenome subclass */
  public void setOrganism(String org) {  organism = org; }
  public String getOrganism() { return organism; }

  /** Not currently used, may want to move getVersion() to an AnnotatedGenome subclass */
  public String getVersion() { return version; }

  /** Not currently used, may want to move getVersionDate() to an AnnotatedGenome subclass */
  public Date getVersionDate() { return version_date; }

  /** Finds all symmetries with the given case-insensitive ID.
   *  @return a non-null List, possibly an empty one.
   */
  public java.util.List findSyms(String id) {
    if (id==null) {
      return Collections.EMPTY_LIST;
    }
    java.util.List sym_list = null;
    Object o = id2sym_hash.get(id.toLowerCase());
    if (o == null) {
      sym_list = Collections.EMPTY_LIST;
    } else if (o instanceof java.util.List) {
      sym_list = (java.util.List) o;
    } else {
      sym_list = new ArrayList(1);
      sym_list.add(o);
    }
    return sym_list;
  }

  /** Finds all symmetries with the given case-insensitive ID and add them to
   *  the given list.  Also looks for id + ".1", id + ".2", etc.
   */
  public boolean findSyms(String id, java.util.List results) {
    return findSyms(id, results, true);
  }

  /** Finds all symmetries with the given case-insensitive ID and add them to
   *  the given list.
   *  @param id  a case-insensitive id.
   *  @param results  the list to which entries will be appended. It is responsibility of
   *   calling code to clear out results list before calling this, if desired.
   *  @param try_appended_id whether to also search for ids of the form
   *   id + ".1", id + ".2", etc.
   *  @return true if any symmetries were added to the list.
   */
  public boolean findSyms(String id, java.util.List results, boolean try_appended_id) {
    boolean success = false;
    if (id != null) {
      String lid = id.toLowerCase();
      Object obj = id2sym_hash.get(lid);
      if (obj instanceof SeqSymmetry) {
        results.add(obj);
        success = true;
      }
      else if (obj instanceof java.util.List) {
        java.util.List syms = (java.util.List) obj;
        results.addAll(syms);
        success = true;
      }
      // try id appended with ".n" where n is 0, 1, etc. till there is no match
      else if (obj == null && try_appended_id) {
        int postfix = 0;
        Object appendobj;
        while ( (appendobj = id2sym_hash.get(lid + "." + postfix)) != null) {
          if (appendobj instanceof SeqSymmetry) {
            results.add(appendobj);
            success = true;
          }
          else if (appendobj instanceof java.util.List) {
            java.util.List syms = (java.util.List) appendobj;
            results.addAll(syms);
            success = true;
          }
        }
        postfix++;
      }
    }
    return success;
  }

  /**
   *  Assosicates a symmetry with a case-insensitive ID.  You can later retrieve the
   *  list of all matching symmetries for a given ID by calling findSyms(String).
   *  Niether argument should be null.
   */
  public void addToIndex(String id, SeqSymmetry sym) {
    if (id==null || sym==null) throw new NullPointerException();
    id2sym_hash.put(id.toLowerCase(), sym);
  }

  /** Returns a set of the String IDs that have been added to the ID index using
   *  addToIndex(String, SeqSymmetry).  The IDs will be returned in lower-case.
   *  Each of the keys can be used as a parameter for the findSyms(String) method.
   */
  public Set getSymmetryIDs() {
    return id2sym_hash.keySet();
  }

  /** Returns a set of the String IDs alphabetically between start and end
   *  that have been added to the ID index using
   *  addToIndex(String, SeqSymmetry).  The IDs will be returned in lower-case.
   *  Each of the keys can be used as a parameter for the findSyms(String) method.
   *  @param start  A String indicating the lowest index value; null or empty start
   *   string will get all index strings up to the given end value.
   *  @param end    A String indicating the highest index value; null or empty end
   *   string will get all index strings above the given start value.
   */
  public Set getSymmetryIDs(String start, String end) {
    if (start == null) { start = ""; }
    if (end == null) { end = ""; }

    start = start.toLowerCase();
    end = end.toLowerCase();

    if (start.equals("") && end.equals("")) {
      return getSymmetryIDs();
    } else if (start.equals("")) {
      return id2sym_hash.headMap(end).keySet();
    } else if (end.equals("")) {
      return id2sym_hash.tailMap(start).keySet();
    } else {
      return id2sym_hash.subMap(start, end).keySet();
    }
  }

  /** Call this method if you alter the Map of IDs to SeqSymmetries.
   *  @param source  The source responsible for the change, used in constructing
   *    the {@link SymMapChangeEvent}.
   */
  public void symHashChanged(Object source) {
    java.util.List list = getSymMapChangeListeners();
    for (int i=0; i<list.size(); i++) {
      SymMapChangeListener l = (SymMapChangeListener) list.get(i);
      l.symMapModified(new SymMapChangeEvent(source, this));
    }
  }

  public static java.util.List getSymMapChangeListeners() {
    return sym_map_change_listeners;
  }

  public static void addSymMapChangeListener(SymMapChangeListener l) {
    sym_map_change_listeners.add(l);
  }

  public static void removeSymMapChangeListener(SymMapChangeListener l) {
    sym_map_change_listeners.remove(l);
  }

  public static class ListmakingHashMap extends TreeMap {
    public Object put(Object key, Object value) {
      Object x = this.get(key);
      if (x == value) {
        return x; // do not store the same value twice
      }

      if (value == null) {
        super.put(key, null);
      } else if (x == null) {
        super.put(key, value);
      } else if (x instanceof List) {
        List list_x = (List) x;
        if (! list_x.contains(value)) {
          list_x.add(value);
        }
      } else {
        ArrayList al = new ArrayList(2);
        al.add(x);
        al.add(value);
        super.put(key, al);
      }
      return x;
    }

    // Does exactly the same thing as the superclass:
    // reports true if the value is contained directly as a value,
    // but not if the value is included inside one of the Lists
    public boolean containsValue(Object value) {
      return super.containsValue(value);
    }
  }

}
