package com.affymetrix.genometryImpl;

import com.affymetrix.genometryImpl.event.FeatureSelectionListener;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.event.SymSelectionEvent;
import com.affymetrix.genometryImpl.event.SymSelectionListener;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

public final class GenometryModel {

	private static GenometryModel smodel = new GenometryModel();
	private static Logger default_logger = Logger.getLogger(GenometryModel.class.getName());
	private static Logger logger = default_logger;


	

	/**
	 * Ann's comment: There is a lot of logic related to selection of
	 * SeqSymmetry objects. It appears that SeqSymmetry on many different
	 * BioSeq objects can be selected simultaneously. Why? What does
	 * selection mean in this context?
	 */

	static public boolean DEBUG = false;

	Map<String,AnnotatedSeqGroup> seq_groups = new LinkedHashMap<String,AnnotatedSeqGroup>();
	// LinkedHashMap preserves the order things were added in, which is nice for QuickLoad

	// maps sequences to lists of selected symmetries
	Map<BioSeq,List<SeqSymmetry>> seq2selectedSymsHash = new HashMap<BioSeq,List<SeqSymmetry>>();

	final List<SeqSelectionListener> seq_selection_listeners = new CopyOnWriteArrayList<SeqSelectionListener>();
	final List<GroupSelectionListener> group_selection_listeners = new CopyOnWriteArrayList<GroupSelectionListener>();
	final List<SymSelectionListener> sym_selection_listeners = new CopyOnWriteArrayList<SymSelectionListener>();
	final List<FeatureSelectionListener> feature_selection_listeners = new CopyOnWriteArrayList<FeatureSelectionListener>();

	AnnotatedSeqGroup selected_group = null;
	BioSeq selected_seq = null;

	private GenometryModel() {
	}

	public static GenometryModel getGenometryModel() {
		return smodel;
	}

	public static GenometryModel refreshGenometryModel() {
		smodel = new GenometryModel();
		return smodel;
	}

	/** Returns a Map of String names to AnnotatedSeqGroup objects. */
	public Map<String,AnnotatedSeqGroup> getSeqGroups() {
		return seq_groups;
	}

	public List<String> getSeqGroupNames() {
		List<String> list = new ArrayList<String>(getSeqGroups().keySet());
		Collections.sort(list);
		return Collections.unmodifiableList(list);
	}

	public AnnotatedSeqGroup getSeqGroup(String group_syn) {
		if (group_syn == null) { return null; }
		AnnotatedSeqGroup group = seq_groups.get(group_syn);
		if (group == null) {
			// try and find a synonym
			for (AnnotatedSeqGroup curgroup : seq_groups.values()) {
				if (curgroup.isSynonymous(group_syn)) {
					group = curgroup;
					break;
				}
			}
		}
		return group;
	}

	/**
	 *  Returns the seq group with the given id, creating a new one if there
	 *  isn't an existing one.
	 *  @return a non-null AnnotatedSeqGroup
	 */
	public AnnotatedSeqGroup addSeqGroup(String group_id) {
		// if AnnotatedSeqGroup with same or synonymous id already exists, then return it
		AnnotatedSeqGroup group = getSeqGroup(group_id);
		// otherwise create a new AnnotatedSeqGroup
		if (group == null) {
			group = createSeqGroup(group_id);
			seq_groups.put(group.getID(), group);
			//fireModelChangeEvent(GenometryModelChangeEvent.SEQ_GROUP_ADDED, group);
		}
		return group;
	}

	/** The routine that actually creates a new AnnotatedSeqGroup.
	 *  Override this to provide a specific subclass of AnnotatedSeqGroup.
	 */
	protected AnnotatedSeqGroup createSeqGroup(String group_id) {
		return new AnnotatedSeqGroup(group_id);
	}

	public void addSeqGroup(AnnotatedSeqGroup group) {
		seq_groups.put(group.getID(), group);
		//fireModelChangeEvent(GenometryModelChangeEvent.SEQ_GROUP_ADDED, group);
	}

	public AnnotatedSeqGroup getSelectedSeqGroup() {
		return selected_group;
	}

	// TODO: modify so that fireGroupSelectionEvent() is only called if
	//     group arg is different than previous selected_group
	public void setSelectedSeqGroup(AnnotatedSeqGroup group) {
		if (DEBUG)  {
			System.out.println("GenometryModel.setSelectedSeqGroup() called, ");
			System.out.println("    group = " + (group == null ? null : group.getID()));
		}

		selected_group = group;
		selected_seq = null;
		List<AnnotatedSeqGroup> glist = new ArrayList<AnnotatedSeqGroup>();
		glist.add(selected_group);
		fireGroupSelectionEvent(this, glist);
	}

	public void addGroupSelectionListener(GroupSelectionListener listener) {
		if (!group_selection_listeners.contains(listener)) {
			group_selection_listeners.add(listener);
		}
	}

	public void removeGroupSelectionListener(GroupSelectionListener listener) {
		group_selection_listeners.remove(listener);
	}

	public List<GroupSelectionListener> getGroupSelectionListeners() {
		return group_selection_listeners;
	}

	private void fireGroupSelectionEvent(Object src, List<AnnotatedSeqGroup> glist) {
		GroupSelectionEvent evt = new GroupSelectionEvent(src, glist);
		for (GroupSelectionListener listener : group_selection_listeners) {
			listener.groupSelectionChanged(evt);
		}
	}

	//Feature Select Event
	public void setSelectedFeature(TreePath path) {
		if (DEBUG)  {
			System.out.println("GenometryModel.setSelectedFeature() called, ");
		}
		fireFeatureSelectionEvent(this, path);
	}

	public void addFeatureSelectionListener(FeatureSelectionListener listener) {
		if (!feature_selection_listeners.contains(listener)) {
			feature_selection_listeners.add(listener);
		}
	}

	public void removeFeatureSelectionListener(FeatureSelectionListener listener) {
		feature_selection_listeners.remove(listener);
	}

	public List<FeatureSelectionListener> getFeatureSelectionListeners() {
		return feature_selection_listeners;
	}

	private void fireFeatureSelectionEvent(Object src, TreePath path) {
		TreeSelectionEvent evt = new TreeSelectionEvent(src, path, false, null, null);
		for (FeatureSelectionListener listener : feature_selection_listeners) {
			listener.featureSelectionChanged(evt);
		}
	}


	public BioSeq getSelectedSeq() {
		return selected_seq;
	}

	public void setSelectedSeq(BioSeq seq) {
		setSelectedSeq(seq, this);
	}

	/**
	 *  currently seq selection events _need_ to be fired even if seq is same as previously selected seq,
	 *     because in some SeqSelectionListeners (such as SeqMapView) the side effects of receiving a
	 *     SeqSelectionEvent are important even if selected seq is same.
	 */
	public void setSelectedSeq(BioSeq seq, Object src) {
		if (DEBUG)  {
			System.out.println("GenometryModel.setSelectedSeq() called, ");
			System.out.println("    seq = " + (seq == null ? null : seq.getID()));
		}

		selected_seq = seq;
		ArrayList<BioSeq> slist = new ArrayList<BioSeq>();
		slist.add(selected_seq);
		fireSeqSelectionEvent(src, slist);
	}

	public void addSeqSelectionListener(SeqSelectionListener listener) {
		if (!seq_selection_listeners.contains(listener)) {
			seq_selection_listeners.add(listener);
		}
	}

	public void removeSeqSelectionListener(SeqSelectionListener listener) {
		seq_selection_listeners.remove(listener);
	}

	public List<SeqSelectionListener> getSeqSelectionListeners() {
		return seq_selection_listeners;
	}

	/**
	 *  SeqSelectionListeners are notified in the order they were added as listeners
	 *    (order is important when one listener relies on another's state --
	 *       for example in IGB some listeners assume main SeqMapView has been
	 *       notified first)
	 */
	void fireSeqSelectionEvent(Object src, List<BioSeq> slist) {
		SeqSelectionEvent evt = new SeqSelectionEvent(src, slist);
		for (SeqSelectionListener listener : seq_selection_listeners) {
				listener.seqSelectionChanged(evt);
		}
	}

	public void addSymSelectionListener(SymSelectionListener listener) {
		if (!sym_selection_listeners.contains(listener)) {
			sym_selection_listeners.add(listener);
		}
	}

	public void removeSymSelectionListener(SymSelectionListener listener) {
		sym_selection_listeners.remove(listener);
	}

	public List<SymSelectionListener> getSymSelectionListeners() {
		return sym_selection_listeners;
	}

	private void fireSymSelectionEvent(Object src, List<SeqSymmetry> syms) {
		if (DEBUG) {
			System.out.println("Firing event: " + syms.size());
		}
		SymSelectionEvent sevt = new SymSelectionEvent(src, syms);
		for (SymSelectionListener listener : sym_selection_listeners) {
			listener.symSelectionChanged(sevt);
		}
	}

	/**
	 *  Sets the selected symmetries.
	 *  The symmetries can be on multiple sequences, and selecting
	 *  them will not automatically change the selected sequence.
	 *  All the SymSelectionListener's will be notified.
	 *  @param syms A List of SeqSymmetry objects to select.
	 *  @param src The object responsible for selecting the sequences.
	 */
	public void setSelectedSymmetries(List<SeqSymmetry> syms, Object src)  {
		setSelectedSymmetries(syms);
		fireSymSelectionEvent(src, syms); // Note this is the complete list of selections
	}

	/**
	 *  Sets the selected symmetries AND selects one of the sequences that they lie on.
	 *  The symmetries can be on multiple sequences.
	 *  If the current bio seq contains one of the symmetries, the seq will not
	 *  be changed.  Otherwise the seq will be changed to one of the ones which
	 *  has a selected symmetry.
	 *  The SeqSelectionListener's will be notified first (only if the seq changes),
	 *  and then the SymSelectionListener's will be notified.
	 *  @param syms A List of SeqSymmetry objects to select.
	 *  @param src The object responsible for selecting the sequences.
	 */
	public void setSelectedSymmetriesAndSeq(List<SeqSymmetry> syms, Object src) {
		List seqs_with_selections = setSelectedSymmetries(syms);
		if (! seqs_with_selections.contains(getSelectedSeq())) {
			if (getSelectedSymmetries(getSelectedSeq()).isEmpty()) {
				BioSeq seq = null;
				if (! seqs_with_selections.isEmpty()) {
					seq = (BioSeq) seqs_with_selections.get(0);
				}
				setSelectedSeq(seq, src);
			}
		}
		fireSymSelectionEvent(src, syms); // Note this is the complete list of selections
	}

	/**
	 *  Sets the selected symmetries.
	 *  The symmetries can be on multiple sequences, and selecting
	 *  them will not automatically change the selected sequence.
	 *  This non-public version does NOT send a SymSelectionEvent.
	 *  @param syms A List of SeqSymmetry objects to select.
	 *  @return The List of sequences with selections on them after this operation.
	 */
	private List<BioSeq> setSelectedSymmetries(List<SeqSymmetry> syms) {

		if (DEBUG) {
			System.out.println("SetSelectedSymmetries called, number of syms: " + syms.size());
		}

		HashMap<BioSeq,List<SeqSymmetry>> seq2SymsHash = new HashMap<BioSeq,List<SeqSymmetry>>();

		// for each ID found in the ID2sym hash, add it to the owning sequences
		// list of selected symmetries
		HashSet<BioSeq> all_seqs = new HashSet<BioSeq>(); // remember all seqs found
		for (SeqSymmetry sym : syms) {
			if (sym == null) {
				continue;
			}
			BioSeq seq = getSelectedSeqGroup().getSeq(sym);
			if (seq == null) {
				continue;
			}
			// prepare the list to add the sym to based on the seq ID
			List<SeqSymmetry> symlist = seq2SymsHash.get(seq);
			if (symlist == null) {
				symlist = new ArrayList<SeqSymmetry>();
				seq2SymsHash.put(seq, symlist);
			}
			// add the sym to the list for the correct seq ID
			symlist.add(sym);
			all_seqs.add(seq);
		}

		// clear all the existing selections first
		clearSelectedSymmetries(); // do not send an event yet

		// now perform the selections for each sequence that was matched
		for(  BioSeq seq : seq2SymsHash.keySet()) {
			List<SeqSymmetry> symslist = seq2SymsHash.get(seq);
			if (DEBUG) {
				System.out.println("Syms " + symslist.size() + " on seq " + seq.getID());
			}
			setSelectedSymmetries(symslist, seq); // do not send an event yet
		}

		return new ArrayList<BioSeq>(all_seqs);
	}

	// Selects a List of SeqSymmetry objects for a particular BioSeq.
	// Does not send a selection event.
	private void setSelectedSymmetries(List<SeqSymmetry> syms, BioSeq seq) {
		if (seq == null) { return; }
		// Should it complain if any of the syms are not on the specified seq?
		// (This is not an issue since this is not called from outside of this class.)

		if (DEBUG)  {
			System.out.println("GenometryModel.setSelectedSymmetries() called, ");
			System.out.println("    syms = " + syms);
		}
		// set the selected syms for the sequence
		if (syms != null && ! syms.isEmpty()) {
			seq2selectedSymsHash.put(seq, syms);
		} else {
			seq2selectedSymsHash.remove(seq); // to avoid memory leaks when a seq is deleted
		}
	}


	/**
	 *  Get the list of selected symmetries on the currently selected sequence.
	 *  @return A List of the selected SeqSymmetry objects, can be empty, but not null
	 */
	public final List<SeqSymmetry> getSelectedSymmetriesOnCurrentSeq() {
		return getSelectedSymmetries(selected_seq);
	}

	/**
	 *  Get the list of selected symmetries on the specified sequence.
	 *  @return A List of the selected SeqSymmetry objects, can be empty, but not null
	 */
	public final List<SeqSymmetry> getSelectedSymmetries(BioSeq seq) {
		List<SeqSymmetry> selections = seq2selectedSymsHash.get(seq);
		if (selections == null) {
			selections = new ArrayList<SeqSymmetry>();
			// NO:  This is a memory leak: seq2selectedSymsHash.put(seq, selections);
		}
		return selections;
	}

	/**
	 *  Clears the symmetry selection for every sequence. Notifies all the selection listeners.
	 *  @param src The object to use as the event source for the SymSelectionEvent.
	 */
	public void clearSelectedSymmetries(Object src) {
		clearSelectedSymmetries();
		fireSymSelectionEvent(src, Collections.<SeqSymmetry>emptyList());
	}

	/**
	 *  Clears the symmetry selection for every sequence.
	 *  Does not notifies the selection listeners.
	 */
	private void clearSelectedSymmetries() {
		for (List<SeqSymmetry> list : seq2selectedSymsHash.values()) {
			list.clear();
		}
		seq2selectedSymsHash.clear();
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger log) {
		logger = log;
		if (logger == null) {
			logger = default_logger;
		}
	}

	public static void logError(String msg) {
		getLogger().log(Level.SEVERE, msg);
	}

	public static void logWarning(String msg) {
		getLogger().log(Level.WARNING, msg);
	}

	public static void logInfo(String msg) {
		getLogger().log(Level.INFO, msg);
	}

	public static void logDebug(String msg) {
		getLogger().log(Level.FINE, msg);
	}

}
