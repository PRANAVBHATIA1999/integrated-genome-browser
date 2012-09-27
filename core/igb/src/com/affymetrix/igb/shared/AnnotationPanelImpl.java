package com.affymetrix.igb.shared;

import com.jidesoft.combobox.ColorComboBox;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.DerivedSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithProps;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.action.ChangeExpandMaxOptimizeAction;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.tiers.TrackConstants;
import static com.affymetrix.igb.shared.Selections.*;

/**
 *
 * @author hiralv
 */
public class AnnotationPanelImpl extends AnnotationPanel implements Selections.RefreshSelectionListener{
	private static final long serialVersionUID = 1L;
	protected IGBService igbService;
	
	public AnnotationPanelImpl(IGBService _igbService){
		super();
		igbService = _igbService;
		resetAll();
		Selections.addRefreshSelectionListener(this);
	}
	
	private void updateDisplay() {
		updateDisplay(true, true);
	}

	private void updateDisplay(final boolean preserveX, final boolean preserveY){
		ThreadUtils.runOnEventQueue(new Runnable() {
	
			public void run() {
//				igbService.getSeqMap().updateWidget();
//				igbService.getSeqMapView().setTierStyles();
//				igbService.getSeqMapView().repackTheTiers(true, true);
				igbService.getSeqMapView().updatePanel(preserveX, preserveY);
			}
		});
	}
	
	private void refreshView() {
		ThreadUtils.runOnEventQueue(new Runnable() {	
			public void run() {
				igbService.getSeqMap().updateWidget();
			}
		});
	}
	
	private void setStackDepth() {
		final JTextField stackDepthTextField = getStackDepthTextField();
		String mdepth_string = stackDepthTextField.getText();
		if (mdepth_string == null) {
			return;
		}
		
		ParameteredAction action = (ParameteredAction) GenericActionHolder.getInstance()
				.getGenericAction("com.affymetrix.igb.action.ChangeExpandMaxAction");
		try{
			action.performAction(Integer.parseInt(mdepth_string));
			updateDisplay(true, false);
		}catch(Exception ex){
			ErrorHandler.errorPanel("Invalid value "+mdepth_string);
		}
	}
	
	@Override
	protected void stackDepthTextFieldActionPerformedA(ActionEvent evt) {
		setStackDepth();
	}

	@Override
	protected void labelFieldComboBoxActionPerformedA(ActionEvent evt) {
		final JComboBox labelFieldComboBox = getLabelFieldComboBox();
		String labelField = (String)labelFieldComboBox.getSelectedItem();
		if (labelField == null) {
			return;
		}
		ParameteredAction action = (ParameteredAction) GenericActionHolder.getInstance()
				.getGenericAction("com.affymetrix.igb.action.LabelGlyphAction");
		action.performAction(labelField);
		updateDisplay();
	}

	@Override
	protected void strands2TracksCheckBoxActionPerformedA(ActionEvent evt) {
		final JCheckBox strands2TracksCheckBox = getStrands2TracksCheckBox();
	    String actionId = strands2TracksCheckBox.isSelected() ?
			"com.affymetrix.igb.action.ShowTwoTiersAction" :
			"com.affymetrix.igb.action.ShowOneTierAction";
		GenericAction action = GenericActionHolder.getInstance().getGenericAction(actionId);
		if (action != null) {
			action.actionPerformed(evt);
		}
		updateDisplay();
	}

	@Override
	protected void strandsArrowCheckBoxActionPerformedA(ActionEvent evt) {
		final JCheckBox strandsArrowCheckBox = getStrandsArrowCheckBox();
		String actionId = strandsArrowCheckBox.isSelected() ?
			"com.affymetrix.igb.action.SetDirectionStyleArrowAction" :
			"com.affymetrix.igb.action.UnsetDirectionStyleArrowAction";
		GenericAction action = GenericActionHolder.getInstance().getGenericAction(actionId);
		if (action != null) {
			action.actionPerformed(evt);
		}
		updateDisplay();
	}

	@Override
	protected void strandsColorCheckBoxActionPerformedA(ActionEvent evt) {
		 final JCheckBox strandsColorCheckBox = getStrandsColorCheckBox();
		String actionId = strandsColorCheckBox.isSelected() ?
			"com.affymetrix.igb.action.SetDirectionStyleColorAction" :
			"com.affymetrix.igb.action.UnsetDirectionStyleColorAction";
		GenericAction action = GenericActionHolder.getInstance().getGenericAction(actionId);
		if (action != null) {
			action.actionPerformed(evt);
		}
		is_listening = false;
		strandsForwardColorComboBoxReset();
		strandsReverseColorComboBoxReset();
		is_listening = true;
		updateDisplay();
	}

	@Override
	protected void strandsReverseColorComboBoxActionPerformedA(ActionEvent evt) {
		final ColorComboBox strandsReverseColorComboBox = getStrandsReverseColorComboBox();
		if (igbService.getSeqMap() == null) {
			return;
		}
		Color color = strandsReverseColorComboBox.getSelectedColor();
		ParameteredAction action = (ParameteredAction) GenericActionHolder.getInstance()
				.getGenericAction("com.affymetrix.igb.action.ChangeReverseColorAction");
		if (action != null && color != null) {
			action.performAction(color);
		}
		updateDisplay();
	}

	@Override
	protected void strandsForwardColorComboBoxActionPerformedA(ActionEvent evt) {
		 final ColorComboBox strandsForwardColorComboBox = getStrandsForwardColorComboBox();
		if (igbService.getSeqMap() == null) {
			return;
		}
		Color color = strandsForwardColorComboBox.getSelectedColor();
		ParameteredAction action = (ParameteredAction) GenericActionHolder.getInstance()
				.getGenericAction("com.affymetrix.igb.action.ChangeForwardColorAction");
		if (action != null && color != null) {
			action.performAction(color);
		}
		updateDisplay();
	}

	@Override
	protected void stackDepthGoButtonActionPerformedA(ActionEvent evt) {
		setStackDepth();
	}

	@Override
	protected void stackDepthAllButtonActionPerformedA(ActionEvent evt) {
		getStackDepthTextField().setText("" + ChangeExpandMaxOptimizeAction.getAction().getOptimum());
	}

	@Override
	protected void stackDepthTextFieldReset() {
		JTextField stackDepthTextField = getStackDepthTextField();
		boolean enabled = allGlyphs.size() > 0 && isAllAnnot();
		stackDepthTextField.setEnabled(enabled);
		getStackDepthLabel().setEnabled(enabled);
		stackDepthTextField.setText("");
		if (enabled) {
			Integer stackDepth = -1;
			boolean stackDepthSet = false;
			for (StyledGlyph glyph : allGlyphs) {
				if (stackDepth == -1 && !stackDepthSet) {
					if (glyph instanceof TierGlyph) {
						switch (((TierGlyph) glyph).getDirection()) {
							case FORWARD:
								stackDepth = glyph.getAnnotStyle().getForwardMaxDepth();
								break;
							case REVERSE:
								stackDepth = glyph.getAnnotStyle().getReverseMaxDepth();
								break;
							default:
								stackDepth = glyph.getAnnotStyle().getMaxDepth();
						}
					}
					stackDepthSet = true;
				} else if (stackDepth != glyph.getAnnotStyle().getMaxDepth()) {
					stackDepth = -1;
					break;
				}
			}
			if (stackDepth != -1) {
				stackDepthTextField.setText("" + stackDepth);
			}
		}
	}

	@Override
	protected void labelFieldComboBoxReset() {
		JComboBox labelFieldComboBox = getLabelFieldComboBox();
		labelFieldComboBox.setEnabled(isAllAnnot());
		getLabelFieldLabel().setEnabled(isAllAnnot());
		String labelField = null;
		boolean labelFieldSet = false;
		Set<String> allFields = null;
		for (ITrackStyleExtended style : annotStyles) {
			if (style.getLabelField() != null) {
				String field = style.getLabelField();
				if (!labelFieldSet) {
					labelField = field;
					labelFieldSet = true;
				}
				else if (labelField != null && !field.equals(labelField)) {
					labelField = null;
				}
			}
			Set<String> fields = getFields(style);
			SeqSymmetry sym = GenometryModel.getGenometryModel().getSelectedSeq().getAnnotation(style.getMethodName());
			if (sym instanceof SeqSymmetry) {
				if (allFields == null) {
					allFields = new TreeSet<String>(fields);
				}
				else {
					allFields.retainAll(fields);
				}
			}
		}
		if (allFields == null) {
			allFields = new TreeSet<String>();
		}
		labelFieldComboBox.setModel(new DefaultComboBoxModel(allFields.toArray()));
		if (labelField != null) {
			labelFieldComboBox.setSelectedItem(labelField);
		}
	}

	@Override
	protected void strands2TracksCheckBoxReset() {
		JCheckBox strands2TracksCheckBox = getStrands2TracksCheckBox();
		strands2TracksCheckBox.setEnabled(isAllAnnot() && isAllSupportTwoTrack());
		boolean all2Tracks = isAllAnnot();
		for (ITrackStyleExtended style : annotStyles) {
			if (!style.getSeparate()) {
				all2Tracks = false;
				break;
			}
		}
		strands2TracksCheckBox.setSelected(all2Tracks);
	}

	@Override
	protected void strandsArrowCheckBoxReset() {
		JCheckBox strandsArrowCheckBox = getStrandsArrowCheckBox();
		strandsArrowCheckBox.setEnabled(isAllAnnot() && isAllSupportTwoTrack());
		boolean allArrow = isAllAnnot();
		for (ITrackStyleExtended style : annotStyles) {
			if (!(style.getDirectionType() == TrackConstants.DIRECTION_TYPE.ARROW.ordinal() 
					|| style.getDirectionType() == TrackConstants.DIRECTION_TYPE.BOTH.ordinal())) {
				allArrow = false;
				break;
			}
		}
		strandsArrowCheckBox.setSelected(allArrow);
	}

	@Override
	protected void strandsColorCheckBoxReset() {
		JCheckBox strandsColorCheckBox = getStrandsColorCheckBox();
		strandsColorCheckBox.setEnabled(isAllAnnot() && isAllSupportTwoTrack());
		strandsColorCheckBox.setSelected(isAllAnnot() && isAllStrandsColor());
	}

	@Override
	protected void strandsReverseColorComboBoxReset() {
		ColorComboBox strandsReverseColorComboBox = getStrandsReverseColorComboBox();
		strandsReverseColorComboBox.setEnabled(isAllAnnot() && isAllStrandsColor() && isAllSupportTwoTrack());
		getStrandsReverseColorLabel().setEnabled(isAllAnnot() && isAllStrandsColor());
		Color strandsReverseColor = null;
		if (isAllAnnot() && isAllStrandsColor()) {
			boolean strandsReverseColorSet = false;
			for (ITrackStyleExtended style : annotStyles) {
				if (strandsReverseColor == null && !strandsReverseColorSet) {
					strandsReverseColor = style.getReverseColor();
					strandsReverseColorSet = true;
				}
				else if (strandsReverseColor != style.getReverseColor()) {
					strandsReverseColor = null;
					break;
				}
			}
		}
		strandsReverseColorComboBox.setSelectedColor(strandsReverseColor);
	}

	@Override
	protected void strandsForwardColorComboBoxReset() {
		ColorComboBox strandsForwardColorComboBox = getStrandsForwardColorComboBox();
		strandsForwardColorComboBox.setEnabled(isAllAnnot() && isAllStrandsColor() && isAllSupportTwoTrack());
		getStrandsForwardColorLabel().setEnabled(isAllAnnot() && isAllStrandsColor());
		Color strandsForwardColor = null;
		if (isAllAnnot() && isAllStrandsColor()) {
			boolean strandsForwardColorSet = false;
			for (ITrackStyleExtended style : annotStyles) {
				if (strandsForwardColor == null && !strandsForwardColorSet) {
					strandsForwardColor = style.getForwardColor();
					strandsForwardColorSet = true;
				}
				else if (strandsForwardColor != style.getForwardColor()) {
					strandsForwardColor = null;
					break;
				}
			}
		}
		strandsForwardColorComboBox.setSelectedColor(strandsForwardColor);
	}

	@Override
	protected void stackDepthGoButtonReset() {
		JButton stackDepthGoButton = getStackDepthGoButton();
		stackDepthGoButton.setEnabled(annotStyles.size() > 0 && isAllAnnot());
	}

	@Override
	protected void stackDepthAllButtonReset() {
		JButton stackDepthAllButton = getStackDepthAllButton();
		stackDepthAllButton.setEnabled(annotStyles.size() > 0 && isAllAnnot());
	}

	private boolean isAllStrandsColor() {
		boolean allColor = true;
		for (ITrackStyleExtended style : annotStyles) {
			if (!(style.getDirectionType() == TrackConstants.DIRECTION_TYPE.COLOR.ordinal() 
					|| style.getDirectionType() == TrackConstants.DIRECTION_TYPE.BOTH.ordinal())) {
				allColor = false;
				break;
			}
		}
		return allColor;
	}
	
	private Set<String> getFields(ITrackStyleExtended style) {
		Set<String> fields = new TreeSet<String>();
		SeqSymmetry sym = GenometryModel.getGenometryModel().getSelectedSeq().getAnnotation(style.getMethodName());
		if (sym != null) {
			if (sym.getChildCount() > 0) {
				SeqSymmetry child = sym.getChild(0);
				SeqSymmetry original = getMostOriginalSymmetry(child);
				if (original instanceof SymWithProps) {
					Map<String, Object> props = ((SymWithProps) original).getProperties();
					fields.add(TrackConstants.NO_LABEL);
					if(props != null){
						fields.addAll(props.keySet());
					}
				}
			}
		}
		return fields;
	}
	
	private static SeqSymmetry getMostOriginalSymmetry(SeqSymmetry sym) {
		if (sym instanceof DerivedSeqSymmetry) {
			return getMostOriginalSymmetry(((DerivedSeqSymmetry) sym).getOriginalSymmetry());
		}
		return sym;
	}
	
	@Override
	public void selectionRefreshed() {
		resetAll();
	}
	
}
