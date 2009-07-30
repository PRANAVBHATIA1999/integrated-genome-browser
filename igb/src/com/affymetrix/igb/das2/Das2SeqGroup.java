package com.affymetrix.igb.das2;

import java.util.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;

/**
 *  
 *  Das2SeqGroup extends AnnotatedSeqGroup to represent a group of sequences (usually a genome) that was 
 *     initially accessed from a DAS/2 server as a Das2VersionedSource
 *  ensures that group's AnnotatedBioSeqs are instantiated when they are needed
 */
public final class Das2SeqGroup extends AnnotatedSeqGroup {

  private final Das2VersionedSource original_version;
  //private Set versions = new LinkedHashSet();

  public Das2SeqGroup(Das2VersionedSource version, String gid) {
    super(gid);
    original_version = version;
  }

  public Das2VersionedSource getOriginalVersionedSource()  { return original_version; }

  /** NOT YET IMPLEMENTED */
  public Set getVersionedSources() { return null; }

  protected void ensureSeqsLoaded()  {
    original_version.getSegments();
  }

  

  /**
   *  Returns a List of MutableAnnotatedBioSeq objects.
   *  Will not return null.  The list is in the same order as in
   *  {@link #getSeq(int)}.
   */
    @Override
  public List<BioSeq> getSeqList() {
    ensureSeqsLoaded();
    return super.getSeqList();
  }

  /**
   *  Returns the sequence at the given position in the sequence list.
   */
  public BioSeq getSeq(int index) {
    ensureSeqsLoaded();
    return super.getSeq(index);
  }

  /** Returns the number of sequences in the group. */
  public int getSeqCount() {
    ensureSeqsLoaded();
    return super.getSeqCount();
  }


  /** Gets a sequence based on its name, possibly taking synonyms into account.
   *  See {@link #setUseSynonyms(boolean)}.
   */
  public BioSeq getSeq(String synonym) {
    ensureSeqsLoaded();
    return super.getSeq(synonym);

  }

  /**
   *  For the given symmetry, tries to find in the group a sequence
   *    that is pointed to by that symmetry.
   *  @return the first sequence it finds (by iterating through sym's spans),
   *    or null if none is found.
   */
  public BioSeq getSeq(SeqSymmetry sym) {
    ensureSeqsLoaded();
    return super.getSeq(sym);
  }

  /**
   *  Returns the MutableAnnotatedBioSeq with the given id (or synonym), creating it if necessary,
   *  and increasing its length to the given value if necessary.
   */
    @Override
  public BioSeq addSeq(String seqid, int length) {
    if (seqid == null) {
      throw new NullPointerException();
    }
    BioSeq aseq;
    // calling super.getSeq() to avoid ensureSeqsLoaded() calls??
    aseq = super.getSeq(seqid);
    if (aseq != null) {
      if (aseq.getLength() < length) {
        aseq.setLength(length);
      }
    }
    else {
      aseq = new BioSeq(seqid, this.getID(), length);
      this.addSeq(aseq);
    }
    return aseq;
  }


}
