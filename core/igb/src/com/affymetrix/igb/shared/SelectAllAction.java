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
	private static SelectAllAction ACTION = new SelectAllAction(null);
	private static Map<FileTypeCategory, SelectAllAction> CATEGORY_ACTION =
			new HashMap<FileTypeCategory, SelectAllAction>();
	private FileTypeCategory category;

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

	protected SelectAllAction(FileTypeCategory category) {
		super(category == null ? IGBConstants.BUNDLE.getString("selectAll") : category.toString(), "16x16/actions/Select_all.png", "22x22/actions/Select_all.png");
		this.category = category;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		execute(category);
	}

	public void execute(FileTypeCategory... categories) {
		getSeqMapView().selectAll(categories);
	}
}
