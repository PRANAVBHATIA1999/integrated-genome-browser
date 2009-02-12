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

package com.affymetrix.genometryImpl.event;

import java.util.*;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;

public class GroupSelectionEvent extends EventObject {
	List<AnnotatedSeqGroup> selected_groups;
	AnnotatedSeqGroup primary_selection = null;
	static final long serialVersionUID = 1L;

	/**
	 *  Constructor.
	 * @param src The source of the event
	 * @param groups  a List of AnnotatedSeqGroup's that have been selected.
	 *   (If null, will default to {@link Collections#EMPTY_LIST}.)
	 */
	public GroupSelectionEvent(Object src, List<AnnotatedSeqGroup> groups) {
		super(src);
		this.selected_groups = groups;
		this.primary_selection = null;
		if (selected_groups == null) {
			selected_groups = Collections.<AnnotatedSeqGroup>emptyList();
		} else if (! selected_groups.isEmpty()) {
			primary_selection = groups.get(0);
		}
	}

	public GroupSelectionEvent(Object src, AnnotatedSeqGroup group) {
		super(src);
		if (group == null) {
			primary_selection = null;
			selected_groups = Collections.<AnnotatedSeqGroup>emptyList();
		} else {
			primary_selection = group;
			selected_groups = new ArrayList<AnnotatedSeqGroup>(1);
			selected_groups.add(group);
		}
	}

	/**
	 *  @return a non-null List of AnnotatedSeqGroups that have been selected.
	 *    The list might be empty, but will not be null.
	 */
	public List<AnnotatedSeqGroup> getSelectedGroups() {
		return selected_groups;
	}

	/** Gets the first entry in the list {@link #getSelectedGroups()}.
	 *  @return an AnnotatedSeqGroup or null.
	 */
	public AnnotatedSeqGroup getSelectedGroup() {
		return primary_selection;
	}

	@Override
		public String toString() {
			return "GroupSelectionEvent: group count: " + selected_groups.size() +
				" first group: '" + (primary_selection == null ? "null" : primary_selection.getID()) +
				"' source: " + this.getSource();
		}
}
