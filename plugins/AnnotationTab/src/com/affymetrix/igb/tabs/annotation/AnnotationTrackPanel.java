package com.affymetrix.igb.tabs.annotation;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.TrackViewPanel;
import static com.affymetrix.igb.shared.Selections.*;

/**
 *
 * @author hiralv
 */
public class AnnotationTrackPanel extends TrackViewPanel{
	private static final long serialVersionUID = 1L;
	public static final java.util.ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("annotation");
	private static final int TAB_POSITION = 4;
	
	public AnnotationTrackPanel(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("annotationTab"), BUNDLE.getString("annotationTab"), false, TAB_POSITION);
	}
	
	@Override
	protected void selectAllButtonReset() {
		
	}

	@Override
	protected void clearButtonReset() {
		javax.swing.JButton clearButton = getClearButton();
		clearButton.setEnabled(annotStyles.size() > 0);
	}

	@Override
	protected void saveButtonReset() {
		javax.swing.JButton saveButton = getSaveButton();
		saveButton.setEnabled(annotStyles.size() > 0);
	}

	@Override
	protected void deleteButtonReset() {
		javax.swing.JButton deleteButton = getDeleteButton();
		deleteButton.setEnabled(annotStyles.size() > 0);
	}

	@Override
	protected void restoreButtonReset() {
		javax.swing.JButton restoreButton = getRestoreButton();
		restoreButton.setEnabled(annotStyles.size() > 0);
	}
	
	@Override
	public boolean isEmbedded() {
		return true;
	}
}
