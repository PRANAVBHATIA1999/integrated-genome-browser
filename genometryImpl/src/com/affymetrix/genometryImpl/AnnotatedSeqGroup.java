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
package com.affymetrix.genometryImpl;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.affymetrix.genometry.AnnotatedBioSeq;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genometryImpl.event.SymMapChangeEvent;
import com.affymetrix.genometryImpl.event.SymMapChangeListener;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.StateProvider;
import com.affymetrix.genometryImpl.util.SynonymLookup;

public class AnnotatedSeqGroup {

    final private String id;
    private String organism;
    private String description;
    private String source; //as in Das2 server name
    private boolean use_synonyms;
    final private Map<String, SmartAnnotBioSeq> id2seq;
    private ArrayList<SmartAnnotBioSeq> seqlist; //lazy copy of id2seq.values()
    private boolean id2seq_dirty_bit; // used to keep the lazy copy
    final private TreeMap<String,ArrayList<SeqSymmetry>> id2sym_hash;
    final private static Vector<SymMapChangeListener> sym_map_change_listeners = new Vector<SymMapChangeListener>(1);

    public AnnotatedSeqGroup(String gid) {
        id = gid;
        use_synonyms = true;
        id2seq = new LinkedHashMap<String, SmartAnnotBioSeq>();
        id2seq_dirty_bit = false;
        seqlist = new ArrayList<SmartAnnotBioSeq>();
        id2sym_hash = new TreeMap<String,ArrayList<SeqSymmetry>>();
    }

    final public String getID() {
        return id;
    }

    final public String getSource() {
        return source;
    }

    final public void setSource(String source) {
        this.source = source;
    }

    final public void setDescription(String str) {
        description = str;
    }

    final public String getDescription() {
        return description;
    }

    /** may want to move set/getOrganism() to an AnnotatedGenome subclass */
    final public void setOrganism(String org) {
        organism = org;
    }

    final public String getOrganism() {
        return organism;
    }

    /** By default, simply returns the global StateProvider, but subclasses
     *  can implement a different one for each seq group.
     */
    final public static StateProvider getStateProvider() {
        return DefaultStateProvider.getGlobalStateProvider();
    }

    /** Call this method if you alter the Map of IDs to SeqSymmetries.
     *  @param source  The source responsible for the change, used in constructing
     *    the {@link SymMapChangeEvent}.
     */
    final public void symHashChanged(Object source) {
        for (SymMapChangeListener l : getSymMapChangeListeners()) {
            l.symMapModified(new SymMapChangeEvent(source, this));
        }
    }

    final private static List<SymMapChangeListener> getSymMapChangeListeners() {
        return sym_map_change_listeners;
    }

    final public static void addSymMapChangeListener(SymMapChangeListener l) {
        sym_map_change_listeners.add(l);
    }

    final public static void removeSymMapChangeListener(SymMapChangeListener l) {
        sym_map_change_listeners.remove(l);
    }

    /**
     *  Returns a List of SmartAnnotBioSeq objects.
     *  Will not return null.  The list is in the same order as in
     *  {@link #getSeq(int)}.
     */
    public List<SmartAnnotBioSeq> getSeqList() {
        if (id2seq_dirty_bit) {
            // lazily keep the seqlist up-to-date
            seqlist = new ArrayList<SmartAnnotBioSeq>(id2seq.values());
            id2seq_dirty_bit = false;
        }
        return Collections.<SmartAnnotBioSeq>unmodifiableList(seqlist);
    }

    /**
     *  Remove all annotations of a given type from all seqs in this group.
     */
    /*
    public void removeType(String type) {
    List seq_list = getSeqList();
    Iterator iter = seq_list.iterator();
    while (iter.hasNext()) {
    MutableAnnotatedBioSeq seq = (MutableAnnotatedBioSeq) iter.next();
    if (seq instanceof SmartAnnotBioSeq) {
    ((SmartAnnotBioSeq) seq).removeType(type.toLowerCase());
    }
    }
    }*/
    /**
     *  Returns the sequence at the given position in the sequence list.
     */
    public SmartAnnotBioSeq getSeq(int index) {
        final List<SmartAnnotBioSeq> seq_list = getSeqList();
        if (index < seq_list.size()) {
            return seq_list.get(index);
        }
        return null;
    }

    /** Returns the number of sequences in the group. */
    public int getSeqCount() {
        return id2seq.size();
    }

    /**
     *  Sets whether or not to use the SynonymLookup class to search for synonymous
     *  BioSeqs when using the getSeq(String) method.
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
    public SmartAnnotBioSeq getSeq(String synonym) {
        SmartAnnotBioSeq aseq = id2seq.get(synonym);
        if (use_synonyms && aseq == null) {
            // try and find a synonym
            for (SmartAnnotBioSeq synseq : id2seq.values()) {
                if (synseq.isSynonymous(synonym)) {
                    return synseq;
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
     * PRECONDITION: sym != null.
     */
    public SmartAnnotBioSeq getSeq(SeqSymmetry sym) {
        final int spancount = sym.getSpanCount();
        for (int i = 0; i < spancount; i++) {
            SeqSpan span = sym.getSpan(i);
            BioSeq seq1 = span.getBioSeq();
            String seqid = seq1.getID();
            SmartAnnotBioSeq seq2 = id2seq.get(seqid);
            if ((seq2 != null) && (seq1 == seq2)) {
                return seq2;
            }
        }
        return null;
    }

    final public boolean isSynonymous(String synonym) {
        return id.equals(synonym) || SynonymLookup.getDefaultLookup().isSynonym(id, synonym);
    }

    /**
     *  Returns the SmartAnnotBioSeq with the given id (or synonym), creating it if necessary,
     *  and increasing its length to the given value if necessary.
     */
    public SmartAnnotBioSeq addSeq(String seqid, int length) {
        if (seqid == null) {
            throw new NullPointerException();
        }
        
        SmartAnnotBioSeq aseq = getSeq(seqid);
        if (aseq != null) {
            if (aseq.getLength() < length) {
                aseq.setLength(length);
            }
        } else {
            aseq = new SmartAnnotBioSeq(seqid, this.getID(), length);
            this.addSeq(aseq);
        }
        return aseq;
    }

    /**
     * Adds the SmartAnnotBioSeq to the group.
     */
    final public void addSeq(SmartAnnotBioSeq seq) {
        final SmartAnnotBioSeq oldseq = id2seq.get(seq.getID());
        if (oldseq == null) {
            id2seq_dirty_bit = true;
            id2seq.put(seq.getID(), seq);
            seq.setSeqGroup(this);
        } else {
            throw new RuntimeException("ERROR! tried to add seq: " + seq.getID() + " to AnnotatedSeqGroup: " +
                    this.getID() + ", but seq with same id is already in group");
        }
    }

    /**
     * @return list of SeqSymmetries matching the pattern.
     */
    final public List<SeqSymmetry> findSyms(Pattern regex) {
        final HashSet<SeqSymmetry> symset = new HashSet<SeqSymmetry>();
        final Matcher matcher = regex.matcher("");

        for (Map.Entry<String, ArrayList<SeqSymmetry>> ent : id2sym_hash.entrySet()) {
            String seid = ent.getKey();
            ArrayList<SeqSymmetry> val = ent.getValue();
            if (seid != null && val != null) {
                matcher.reset(seid);
                if (matcher.matches()) {
                    symset.addAll(val);
                }
            }
        }
        return new ArrayList<SeqSymmetry>(symset);
    }

    /** Finds all symmetries with the given case-insensitive ID.
     *  @return a non-null List, possibly an empty one.
     */
    final public List<SeqSymmetry> findSyms(String id) {
        if (id == null) {
            return Collections.<SeqSymmetry>emptyList();
        }
        ArrayList<SeqSymmetry> sym_list = id2sym_hash.get(id.toLowerCase());
        if (sym_list == null) {
            return Collections.<SeqSymmetry>emptyList();
        }
        return Collections.<SeqSymmetry>unmodifiableList(sym_list);
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
    //TODO: does this routine do what is expected?  What if id does not exist, but id + ".1" does?
    // What if id + ".1" does not exist, but id + ".2" does?
    // Does this list need to be in order?
    //TODO: This method is used only by ChpParser.  Move there.
    final public boolean findSyms(String id, List<SeqSymmetry> results, boolean try_appended_id) {
        if (id == null) {
            return false;
        }

        final String lid = id.toLowerCase();
        final List<SeqSymmetry> seqsym_list = id2sym_hash.get(lid);
        if (seqsym_list != null) {
            results.addAll(seqsym_list);
            return true;
        }

        if (!try_appended_id) {
            return false;
        }
        
        return lookForAppendedIDs(lid, results);
    }

    // try id appended with ".n" where n is 0, 1, etc. till there is no match
    private boolean lookForAppendedIDs(String lid, List<SeqSymmetry> results) {
        boolean success = false;
        int postfix = 0;
        List<SeqSymmetry> seq_sym_list;
        while ((seq_sym_list = id2sym_hash.get(lid + "." + postfix)) != null) {
            results.addAll(seq_sym_list);
            success = true;
            postfix++;
        }
        return success;
    }

    /**
     *  Associates a symmetry with a case-insensitive ID.  You can later retrieve the
     *  list of all matching symmetries for a given ID by calling findSyms(String).
     *  Neither argument should be null.
     */
    final public void addToIndex(String id, SeqSymmetry sym) {
        if (id == null || sym == null) {
            throw new NullPointerException();
        }
        this.putSeqInList(id.toLowerCase(), sym);
    }

    /** Returns a set of the String IDs that have been added to the ID index using
     *  addToIndex(String, SeqSymmetry).  The IDs will be returned in lower-case.
     *  Each of the keys can be used as a parameter for the findSyms(String) method.
     */
    final public Set<String> getSymmetryIDs() {
        return id2sym_hash.keySet();
    }

    /** Returns a set of the String IDs alphabetically between start and end (including start, excluding end)
     *  that have been added to the ID index using
     *  addToIndex(String, SeqSymmetry).  The IDs will be returned in lower-case.
     *  Each of the keys can be used as a parameter for the findSyms(String) method.
     *  @param start  A String indicating the lowest index value; null or empty start
     *   string will get all index strings up to (but excluding) the given end value.
     *  @param end    A String indicating the highest index value; null or empty end
     *   string will get all index strings above (and including) the given start value.
     *  PRECONDITION: start !=null.  end != null.
     */
    final public Set<String> getSymmetryIDs(String start, String end) {
        if (start.length() == 0 && end.length() == 0) {
            return getSymmetryIDs();
        }

        start = start.toLowerCase();
        end = end.toLowerCase();

        if (start.length() == 0) {
            return id2sym_hash.headMap(end).keySet();   // exclusive of end
        } else if (end.length() == 0) {
            return id2sym_hash.tailMap(start).keySet(); // inclusive of start
        } else {
            return id2sym_hash.subMap(start, end).keySet(); // inclusive of start, exclusive of end
        }
    }

   


    /**
     *  Returns input id if no GraphSyms on any seq in the given seq group
     *  are already using that id.
     *  Otherwise uses id to build a new unique id.
     *  The id returned is unique for GraphSyms on all seqs in the given group.
     */
    final public static String getUniqueGraphID(String id, AnnotatedSeqGroup seq_group) {
        String result = id;
        for (SmartAnnotBioSeq seq : seq_group.getSeqList()) {
            result = getUniqueGraphID(result, seq);
        }
        return result;
    }

    /**
     *  Returns input id if no GraphSyms on seq with given id.
     *  Otherwise uses id to build a new id that is not used by a GraphSym (or top-level container sym )
     *     currently on the seq.
     *  The id returned is only unique for GraphSyms on that seq, may be used for graphs on other seqs.
     */
    final public static String getUniqueGraphID(String id, BioSeq seq) {
        if (id == null) {
            return null;
        }
        if (!(seq instanceof SmartAnnotBioSeq) && !(seq instanceof AnnotatedBioSeq)) {
            // if not a SmartAnnotBioSeq or AnnotatedBioSeq, just return original ID for now.
            return id;
        }
     
        if (seq instanceof SmartAnnotBioSeq) {
            final SmartAnnotBioSeq sab = (SmartAnnotBioSeq) seq;
            int prevcount = 0;
            String newid = id;
            while (sab.getAnnotation(newid) != null) {
                prevcount++;
                newid = id + "." + prevcount;
            }
            return newid;
        }

        // AnnotatedBioSeq -- can't do getAnnotation on String
        final AnnotatedBioSeq aseq = (AnnotatedBioSeq) seq;
        int prevcount = 0;

        String newid = id;
        while (findGraphSym(aseq, newid)) {
            prevcount++;
            newid = id + "." + prevcount;
        }
        return newid;
    }

    // check every annotation on seq, but assume graphs are directly attached to seq, so
    //   don't have to do recursive descent into children?
    // potentially O(N^2) performance, but this is just a fallback -- most
    //      seqs that GraphSyms are being attached to will be SmartAnnotBioSeqs and dealt with
    //      in the other branch of the conditional
    final private static boolean findGraphSym(AnnotatedBioSeq aseq, String id) {
        final int count = aseq.getAnnotationCount();	// otherwise the loop will recalculate this every time.
        for (int i = 0; i < count; i++) {
            SeqSymmetry sym = aseq.getAnnotation(i);
            if ((sym instanceof GraphSym) && (id.equals(sym.getID()))) {
                return true;
            }
        }
        return false;
    }


    /**
     * Function to add a SeqSymmetry to the id2sym_hash.
     * @param key ID string.
     * @param value SeqSymmetry to add to the hash.
     */
    final private void putSeqInList(String key, SeqSymmetry value) {
        ArrayList<SeqSymmetry> seq_list = id2sym_hash.get(key);
        if (seq_list == null) {
            seq_list = new ArrayList<SeqSymmetry>();
        }
        if (!seq_list.contains(value)) {
            seq_list.add(value);
            id2sym_hash.put(key,seq_list);
        }
    }
}
