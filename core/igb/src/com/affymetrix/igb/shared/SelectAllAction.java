package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.action.SeqMapViewActionA;

public class SelectAllAction extends SeqMapViewActionA {

	private static final long serialVersionUID = 1L;
	private static SelectAllAction ACTION = new SelectAllAction(
			new FileTypeCategory[]{FileTypeCategory.Alignment, FileTypeCategory.Annotation,
			FileTypeCategory.Graph, FileTypeCategory.Mismatch, FileTypeCategory.ProbeSet,
			FileTypeCategory.Sequence});
	private static Map<FileTypeCategory, SelectAllAction> CATEGORY_ACTION =
			new HashMap<FileTypeCategory, SelectAllAction>();
	private FileTypeCategory[] categories;

	static {
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}

	public static SelectAllAction getAction() {
		return ACTION;
	}

	public static SelectAllAction getAction(final FileTypeCategory category) {
		SelectAllAction selectAllAction = CATEGORY_ACTION.get(category);
		if (selectAllAction == null) {
			selectAllAction = new SelectAllAction(category);
			CATEGORY_ACTION.put(category, selectAllAction);
		}
		return selectAllAction;
	}

	protected SelectAllAction(FileTypeCategory... categories) {
		super(categories.length > 1 ? IGBConstants.BUNDLE.getString("selectAllTracks") : categories[0].toString(), null, null);
		this.categories = categories;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		execute(categories);
	}

	public void execute(FileTypeCategory... categories) {
		getSeqMapView().selectAll(categories);
	}
}
