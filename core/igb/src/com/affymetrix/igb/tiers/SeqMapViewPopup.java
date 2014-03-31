/**
 * Copyright (c) 2005-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.tiers;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.border.Border;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.general.IParameters;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.util.IDComparator;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenuItem;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.action.*;
import com.affymetrix.igb.shared.*;
import com.affymetrix.igb.tiers.AffyTieredMap.ActionToggler;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.factories.DefaultTierGlyph;
import com.affymetrix.igb.view.load.GeneralLoadView;

public final class SeqMapViewPopup implements TierLabelManager.PopupListener {
    private class JRPMenuItemTLP extends JRPMenuItem implements TrackListProvider {
		private static final long serialVersionUID = 1L;
		private JRPMenuItemTLP(GenericAction genericAction) {
    		super("Toolbar_" + genericAction.getId(), genericAction);
    	}
		@Override
		public List<TierGlyph> getTrackList() {
			return gviewer.getTierManager().getSelectedTiers();
		}
    }
	
	private static final boolean DEBUG = false;
	private ResourceBundle BUNDLE = IGBConstants.BUNDLE;
	private final SeqMapView gviewer;
	private final TierLabelManager handler;
	private final JMenu strandsMenu = new JMenu(BUNDLE.getString("strandsMenu"));
	private final ActionToggler at1;
	private final ActionToggler at2;
	private final RepackTiersAction repackStub;

	public SeqMapViewPopup(TierLabelManager handler, SeqMapView smv) {
		this.handler = handler;
		this.gviewer = smv;
		this.repackStub = new RepackTiersAction(null, null, null) {
			private static final long serialVersionUID = 1L;
		};
		at1 = new ActionToggler(smv.getClass().getSimpleName() + "_SeqMapViewPopup.showPlus", ShowPlusStrandAction.getAction());
		at2 = new ActionToggler(smv.getClass().getSimpleName() + "_SeqMapViewPopup.showMinus", ShowMinusStrandAction.getAction());
//		addActions();
	}

	/**
	 * Load all the actions in use by this pop up.
	 * Beware, this is a maintenance nightmare.
	 * This was created by grepping for '\.getAction\('
	 * and then making an {@link IGB#addAction} call out of each one.
	 * So if anyone ever adds or removes one of the actions in the pop up
	 * this list should be modified to match.
	 * Watch out for duplicates (like ShowOneTierAction).
	 * Note that this should be called early,
	 * preferably by the constructor.
	 * The shortcuts should be available
	 * even before this pop up is ever popped up.
	 * Sigh. - elb
	 */
	// Replacing this with GenericActionListener in Activator. HV 11/14/12
//	private void addActions() {
//		IGB igb = (IGB) Application.getSingleton();
//		igb.addAction(ShowPlusStrandAction.getAction());
//		igb.addAction(ShowMinusStrandAction.getAction());
//		igb.addAction(ChangeForegroundColorAction.getAction());
//		igb.addAction(ChangeBackgroundColorAction.getAction());
//		igb.addAction(ChangeForwardColorAction.getAction());
//		igb.addAction(ChangeReverseColorAction.getAction());
//		igb.addAction(RenameTierAction.getAction());
//		igb.addAction(ChangeFontSizeAction.getAction());
//		igb.addAction(ChangeExpandMaxAction.getAction());
////		igb.addAction(ChangeExpandMaxAllAction.getAction());
//		igb.addAction(ShowTwoTiersAction.getAction());
//		igb.addAction(ShowOneTierAction.getAction());
//		igb.addAction(SetColorByScoreAction.getAction());
//		igb.addAction(ColorByScoreAction.getAction());
//		igb.addAction(ExportFileAction.getAction());
//		igb.addAction(ExportSelectedAnnotationFileAction.getAction());
//		igb.addAction(UseAsReferenceSeqAction.getAction());
//		igb.addAction(CustomizeAction.getAction());
//		igb.addAction(HideAction.getAction());
//		igb.addAction(ShowAllAction.getAction());
//		igb.addAction(CenterAtHairlineAction.getAction());
//		igb.addAction(MaximizeTrackAction.getAction());
//		igb.addAction(CollapseAction.getAction());
//		igb.addAction(ExpandAction.getAction());
//		igb.addAction(RemoveDataFromTracksAction.getAction());
//		igb.addAction(RepackSelectedTiersAction.getAction());
//		igb.addAction(AutoLoadThresholdAction.getAction());
//	}

	public void refreshMap(boolean stretch_vertically, boolean stretch_horizonatally) {
		if (gviewer != null) {
			// if an AnnotatedSeqViewer is being used, ask it to update itself.
			// later this can be made more specific to just update the tiers that changed
			boolean preserve_view_x = !stretch_vertically;
			boolean preserve_view_y = !stretch_horizonatally;
			gviewer.updatePanel(preserve_view_x, preserve_view_y);
		} else {
			// if no AnnotatedSeqViewer (as in simple test programs), update the tiermap itself.
			handler.repackTheTiers(false, stretch_vertically);
		}
	}

	public void repack(final boolean full_repack, boolean tier_changed) {
		repackStub.repack(full_repack, tier_changed);
	}

	private JMenu addOperationMenu(List<? extends SeqSymmetry> syms) {
		JMenu operationsMenu = new JMenu(BUNDLE.getString("operationsMenu"));
		if (IGBConstants.GENOME_SEQ_ID.equals(gviewer.getAnnotatedSeq().getID())) {
			return operationsMenu; 
		}
		TreeSet<Operator> operators = new TreeSet<Operator>(new IDComparator());
		operators.addAll(ExtensionPointHandler.getExtensionPoint(Operator.class).getExtensionPointImpls());
		for (Operator operator : operators) {
			if (TrackUtils.getInstance().checkCompatible(syms, operator, true)) {
				String title = operator.getDisplay();
				Operator newOperator = operator.newInstance();
				if(newOperator == null){
					Logger.getLogger(SeqMapViewPopup.class.getName()).log(Level.SEVERE, "Could not create instance for operator {0}", title);
					continue;
				}
				
				Map<String, Class<?>> params = operator instanceof IParameters? ((IParameters)operator).getParametersType() : null;
				if (null != params && 0 < params.size()) {
					JMenu operatorSMI = new JMenu(title);
					
					JMenuItem operatorMI = new JMenuItem("Use Default");
					operatorMI.addActionListener(new TrackOperationAction(newOperator));
					operatorSMI.add(operatorMI);
					
					operatorMI = new JMenuItem("Configure...");
					operatorMI.addActionListener(new TrackOperationWithParametersAction(newOperator));
					operatorSMI.add(operatorMI);
					
					operationsMenu.add(operatorSMI);
				} else {
					JMenuItem operatorMI = new JMenuItem(title);
					operatorMI.addActionListener(new TrackOperationAction(newOperator));
					operationsMenu.add(operatorMI);
				}
			}
		}
		return operationsMenu;
	}

	private JMenu addChangeMenu(int num_selections, boolean any_are_expanded, boolean any_are_separate_tiers, boolean any_are_single_tier, boolean any_are_color_off, boolean coordinates_track_selected) {
		JMenu changeMenu = new JMenu(BUNDLE.getString("changeMenu"));
		JMenuItem change_foreground_color = new JRPMenuItemTLP(ChangeForegroundColorAction.getAction());
		change_foreground_color.setEnabled(num_selections > 0);
		changeMenu.add(change_foreground_color);
		JMenuItem change_background_color = new JRPMenuItemTLP(ChangeBackgroundColorAction.getAction());
		change_background_color.setEnabled(num_selections > 0);
		changeMenu.add(change_background_color);
		JMenuItem change_label_color = new JRPMenuItemTLP(ChangeLabelColorAction.getAction());
		change_label_color.setEnabled(num_selections > 0);
		changeMenu.add(change_label_color);
		JMenuItem rename = new JRPMenuItemTLP(RenameTierAction.getAction());
		rename.setEnabled(num_selections == 1);
		changeMenu.add(rename);
		JMenuItem change_font_size = new JRPMenuItemTLP(ChangeFontSizeAction.getAction());
		change_font_size.setEnabled(num_selections > 0);
		changeMenu.add(change_font_size);
		JMenuItem change_Tier_Height = new JRPMenuItemTLP(ChangeTierHeightAction.getAction());
		if(num_selections > 0 && !(handler.getSelectedTierLabels().get(0).getReferenceTier().getAnnotStyle().getTrackName().equals(TrackConstants.NAME_OF_COORDINATE_INSTANCE)) 
				&& (((DefaultTierGlyph)(handler.getSelectedTierLabels().get(0).getReferenceTier())).isHeightFixed())){
			change_Tier_Height.setEnabled(true);
		}
		else{
			change_Tier_Height.setEnabled(false);
		}
		changeMenu.add(change_Tier_Height);
		JMenuItem change_expand_max = new JRPMenuItemTLP(ChangeExpandMaxAction.getAction());
		change_expand_max.setEnabled(any_are_expanded);
		changeMenu.add(change_expand_max);
//		JMenuItem change_expand_max_all = new JRPMenuItemTLP(ChangeExpandMaxAllAction.getAction());
//		change_expand_max_all.setEnabled(num_selections > 0);
//		changeMenu.add(change_expand_max_all);
		return changeMenu;
	}

	private JMenu addShowMenu(boolean containHiddenTiers) {
		final JMenu showMenu = new JMenu(BUNDLE.getString("showMenu"));
		showMenu.removeAll();
		JMenuItem show_all_action = new JMenuItem(ShowAllAction.getAction());
		show_all_action.setIcon(null);
		show_all_action.setText("All");
		showMenu.add(show_all_action);
		showMenu.add(new JSeparator());
		//showMenu.setEnabled(false);
		List<TierLabelGlyph> tiervec = handler.getAllTierLabels();

		for (TierLabelGlyph label : tiervec) {
			TierGlyph tier = (TierGlyph) label.getInfo();
			final ITrackStyleExtended style = tier.getAnnotStyle();
			if (style != null && !style.getShow() && tier.getDirection() != TierGlyph.Direction.REVERSE) {
				final JMenuItem show_tier = new JMenuItem() {

					private static final long serialVersionUID = 1L;
					// override getText() because the HumanName of the style might change

					@Override
					public String getText() {
						String name = style.getTrackName();
						if (name == null) {
							name = "<" + BUNDLE.getString("unnamed") + ">";
						}
						if (name.length() > 30) {
							name = name.substring(0, 30) + "...";
						}
						return name;
					}
				};
				show_tier.setName(style.getMethodName());
				show_tier.setAction(new AbstractAction() {

					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						style.setShow(true);
						showMenu.remove(show_tier);
						handler.sortTiers();
						GeneralLoadView.getLoadView().refreshDataManagementView();						
						repack(false, true);
					}
				});
				showMenu.add(show_tier);
				containHiddenTiers = true;
			}
		}
		showMenu.setEnabled(containHiddenTiers);
		return showMenu;
	}

	@Override
	public void popupNotify(javax.swing.JPopupMenu popup, final TierLabelManager handler) {
		int num_selections = Selections.allGlyphs.size();
		boolean any_are_collapsed = false;
		boolean any_are_expanded = false;
		boolean coordinates_track_selected = false;
		boolean containHiddenTiers = false;
		boolean any_alignment = false;
		boolean any_show_residue_mask = false;
		boolean any_shade_based_on_quality = false;
		boolean all_same_category = num_selections > 0;
//		boolean any_are_separate_tiers = false;
//		boolean any_are_single_tier = false;
//		boolean any_lockable = false;
//		boolean any_locked = false;
		int no_of_locked = 0;
		FileTypeCategory category = num_selections > 0 && Selections.allGlyphs.get(0).getInfo() != null ? 
				((RootSeqSymmetry)Selections.allGlyphs.get(0).getInfo()).getCategory()
				: null;
		
		for (StyledGlyph glyph : Selections.allGlyphs) {
			ITrackStyleExtended astyle = glyph.getAnnotStyle();
			
			if (astyle.getExpandable()) {
				any_are_collapsed = any_are_collapsed || astyle.getCollapsed();
				any_are_expanded = any_are_expanded || !astyle.getCollapsed();
			}
			
			if(astyle.getShow() && glyph instanceof DefaultTierGlyph && ((DefaultTierGlyph)glyph).isHeightFixed()){
				no_of_locked++;
			}
			
			if(glyph.getInfo() != null && ((RootSeqSymmetry)glyph.getInfo()).getCategory() != category){
				all_same_category = false;
			}
			
			if(glyph.getInfo() != null && ((RootSeqSymmetry)glyph.getInfo()).getCategory() == FileTypeCategory.Alignment){
				any_alignment = true;
				any_show_residue_mask = any_show_residue_mask || astyle.getShowResidueMask();
				any_shade_based_on_quality = any_shade_based_on_quality || astyle.getShadeBasedOnQualityScore();
			}
			
			if (astyle == CoordinateStyle.coordinate_annot_style) {
				coordinates_track_selected = true;
			}
			
			if (!astyle.getShow()) {
				containHiddenTiers = true;
			}
			
//			if (!astyle.isGraphTier()) {
//				any_are_separate_tiers = any_are_separate_tiers || astyle.getSeparate();
//				any_are_single_tier = 
//						any_are_single_tier || (!astyle.getSeparate() && 
//						MapTierTypeHolder.getInstance().supportsTwoTrack(glyph.getFileTypeCategory()));
//			} 
		
//			any_lockable = any_lockable || glyph.getTierType() == TierGlyph.TierType.ANNOTATION;
//			any_locked = any_locked || (glyph instanceof DefaultTierGlyph && ((DefaultTierGlyph)glyph).isHeightFixed());
		}

		
		StyledGlyph styledGlyph = (num_selections == 1 ? Selections.allGlyphs.get(0) : null);
		
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder, finalBorder;
		if(num_selections == 1 && styledGlyph != null){
			colorBorder = BorderFactory.createLineBorder(styledGlyph.getAnnotStyle().getForeground());
			finalBorder = BorderFactory.createCompoundBorder(colorBorder, emptyBorder);
			
		} else {
			colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			finalBorder = BorderFactory.createCompoundBorder(colorBorder, emptyBorder);
		}
		popup.setBorder(finalBorder);
		
		JMenuItem optimize_stack_height = new JRPMenuItemTLP(ChangeExpandMaxOptimizeAction.getAction());
		optimize_stack_height.setIcon(null);
		optimize_stack_height.setEnabled(Selections.annotSyms.size() > 0);
		optimize_stack_height.setText("Optimize Stack Height");
		popup.add(optimize_stack_height);
		JMenuItem change_expand_max = new JRPMenuItemTLP(ChangeExpandMaxAction.getAction());
		change_expand_max.setText("Set Stack Height...");
		change_expand_max.setIcon(null);
		popup.add(change_expand_max);
//		popup.add(addChangeMenu(num_selections, any_are_expanded, any_are_separate_tiers, any_are_single_tier, any_are_color_off, coordinates_track_selected));
//		strandsMenu.removeAll();
//		strandsMenu.add(at1);
//		strandsMenu.add(at2);
//		JMenuItem show_two_tiers = new JRPMenuItemTLP(ShowTwoTiersAction.getAction());
//		GenericActionHolder.getInstance().addGenericAction(ShowTwoTiersAction.getAction());
//		show_two_tiers.setEnabled(any_are_single_tier && num_selections > 0 && !coordinates_track_selected);
//		strandsMenu.add(show_two_tiers);
//		JMenuItem show_one_tier = new JRPMenuItemTLP(ShowOneTierAction.getAction());
//		GenericActionHolder.getInstance().addGenericAction(ShowOneTierAction.getAction());
//		show_one_tier.setEnabled(any_are_separate_tiers);
//		strandsMenu.add(show_one_tier);
//		strandsMenu.setEnabled(!coordinates_track_selected);
//		popup.add(strandsMenu);
		JMenuItem hide = new JMenuItem();
		hide.setAction(HideAction.getAction());
		hide.setIcon(null);
		hide.setEnabled(num_selections > 0);
		popup.add(hide);
		JMenu showMenu = addShowMenu(containHiddenTiers);	
		popup.add(showMenu);
		showMenu.getPopupMenu().setBorder(finalBorder);
		JMenuItem collapse = new JCheckBoxMenuItem();
		if((!any_are_expanded && !any_are_collapsed) || (any_are_expanded && any_are_collapsed) || coordinates_track_selected){
			collapse.setEnabled(false);
		} else if(any_are_expanded){
			collapse.setAction(CollapseAction.getAction());
			collapse.setSelected(false);
		} else if(any_are_collapsed){
			collapse.setAction(ExpandAction.getAction());
			collapse.setSelected(true);
		} 
		collapse.setText("Collapse");
		collapse.setIcon(null);
		popup.add(collapse);
		JMenuItem customize = new JRPMenuItemTLP(CustomizeAction.getAction());
		customize.setIcon(null);
		customize.setText("Customize...");
		popup.add(customize);
		
		popup.add(new JSeparator());
		JCheckBoxMenuItem showResidueMask = new JCheckBoxMenuItem(ShowMismatchAction.getAction());
		showResidueMask.setEnabled(any_alignment);
		showResidueMask.setSelected(any_alignment && any_show_residue_mask);
		popup.add(showResidueMask);
		JCheckBoxMenuItem useBaseQuality = new JCheckBoxMenuItem(ShadeUsingBaseQualityAction.getAction());
		useBaseQuality.setEnabled(any_alignment);
		useBaseQuality.setSelected(any_alignment && any_shade_based_on_quality);
		popup.add(useBaseQuality);
//		JMenuItem expand = new JRPMenuItemTLP(ExpandAction.getAction());
//		expand.setEnabled(any_are_collapsed);
//		popup.add(expand);
//		JCheckBoxMenuItem lock = new JCheckBoxMenuItem();
//		if(any_locked){
//			lock.setAction(UnlockTierHeightAction.getAction());
//			lock.setSelected(any_locked);
//			lock.setText(LockTierHeightAction.getAction().getDisplay());
//		}else{
//			lock.setAction(LockTierHeightAction.getAction());
//		}
//		lock.setEnabled(any_lockable);
//		if(!any_locked){
//			lock.setEnabled(!all_but_one_locked && any_lockable);
//		}
//		popup.add(lock);
//		JMenuItem repack_selected_tiers = new JRPMenuItemTLP(RepackSelectedTiersAction.getAction());
//		repack_selected_tiers.setEnabled(num_selections > 0 && !coordinates_track_selected);
//		popup.add(repack_selected_tiers);
		popup.add(new JSeparator());
//		popup.add(new JRPMenuItemTLP(CenterAtHairlineAction.getAction()));
//		if (num_selections == 1 && ((TierGlyph) labels.get(0).getInfo()).getDirection() != TierGlyph.Direction.AXIS) {
//			JMenuItem maximize_track = new JRPMenuItemTLP(MaximizeTrackAction.getAction());
//			popup.add(maximize_track);
//		}
		//popup.add(new JSeparator());
		//JMenuItem show_all = new JRPMenuItemTLP(ShowAllAction.getAction());
		//show_all.setEnabled(containHiddenTiers);
		//popup.add(show_all);
		
		JMenu operationsMenu = addOperationMenu(Selections.rootSyms);
		popup.add(operationsMenu);
		operationsMenu.getPopupMenu().setBorder(finalBorder);
		operationsMenu.setEnabled(operationsMenu.getItemCount() > 0);
//		JCheckBoxMenuItem color_by_score = new JCheckBoxMenuItem(ColorByScoreAction.getAction());
//		color_by_score.setSelected(any_are_color_by_score && num_selections > 0 && !coordinates_track_selected);
//		color_by_score.setEnabled(num_selections == 1 && !coordinates_track_selected && !any_graph);
//		color_by_score.setIcon(null);
//		popup.add(color_by_score);
//		JMenuItem set_color_by_score = new JRPMenuItemTLP(SetColorByScoreAction.getAction());
//		set_color_by_score.setIcon(null);
//		set_color_by_score.setEnabled(!coordinates_track_selected && !any_graph);
//		popup.add(set_color_by_score);
		
		popup.add(new JSeparator());
			
		JMenuItem set_color_by = new JRPMenuItemTLP(ColorByAction.getAction());
		set_color_by.setIcon(null);
		set_color_by.setEnabled(all_same_category && (category == FileTypeCategory.Annotation 
				|| category == FileTypeCategory.Alignment || category == FileTypeCategory.ProbeSet));
		popup.add(set_color_by);
			
		JMenuItem filter_action = new JRPMenuItemTLP(FilterAction.getAction());
		filter_action.setIcon(null);
		filter_action.setEnabled(all_same_category && (category == FileTypeCategory.Annotation 
				|| category == FileTypeCategory.Alignment || category == FileTypeCategory.ProbeSet));
		popup.add(filter_action);
		
		popup.add(new JSeparator());
				
		JMenuItem save_selected_annotations = new JRPMenuItemTLP(ExportSelectedAnnotationFileAction.getAction());
		save_selected_annotations.setEnabled(styledGlyph != null && styledGlyph instanceof TierGlyph && 
				!((TierGlyph)styledGlyph).getSelected().isEmpty() && ExportSelectedAnnotationFileAction.getAction().isExportable(styledGlyph.getFileTypeCategory()));
		save_selected_annotations.setIcon(null);
		popup.add(save_selected_annotations);
		JMenuItem save_track = new JRPMenuItemTLP(ExportFileAction.getAction());
		save_track.setEnabled(num_selections == 1 && !coordinates_track_selected && styledGlyph.getInfo() != null && ExportSelectedAnnotationFileAction.getAction().isExportable(styledGlyph.getFileTypeCategory()));
		save_track.setIcon(null);
		popup.add(save_track);
		
		popup.add(new JSeparator());
		
		JMenuItem remove_data_from_tracks = new JRPMenuItemTLP(RemoveDataFromTracksAction.getAction());
		remove_data_from_tracks.setText("Clear Data");
		remove_data_from_tracks.setEnabled(Selections.rootSyms.size() > 0);
		remove_data_from_tracks.setIcon(null);
		popup.add(remove_data_from_tracks); // Remove data from selected tracks.
		
		JMenuItem delete_track = new JRPMenuItemTLP(CloseTracksAction.getAction());
		delete_track.setText("Delete Track");
		delete_track.setEnabled(Selections.rootSyms.size() > 0 && !coordinates_track_selected);
		delete_track.setIcon(null);
		popup.add(delete_track);
				
	//	if (tierGlyph != null) {
	//		// Check whether this selection is a graph or an annotation
	//		ITrackStyleExtended style = tierGlyph.getAnnotStyle();
	//		GenericFeature feature = style.getFeature();
	//		if (feature != null) {
	//			if (tierGlyph.getFileTypeCategory() == FileTypeCategory.Sequence) {
	//				popup.add(new JSeparator());
	//				JMenuItem use_as_reference_seq = new JRPMenuItemTLP(UseAsReferenceSeqAction.getAction());
	//				popup.add(use_as_reference_seq);
	//			}
//
//				if (feature.friendlyURL != null) {
//					popup.add(new JRPMenuItemTLP(new FeatureInfoAction(feature.friendlyURL.toString())));
//				}
//			}
//		}
		//popup.add(new JSeparator());
		//popup.add(new JSeparator());
//		popup.add(new JRPMenuItemTLP(AutoLoadThresholdAction.getAction()));
		if (DEBUG) {
			popup.add(new AbstractAction("DEBUG") {

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					doDebugAction();
				}
			});
		}
	}

	// purely for debugging
	private void doDebugAction() {
		for (TierGlyph tg : handler.getSelectedTiers()) {
			ITrackStyleExtended style = tg.getAnnotStyle();
			System.out.println("Track: " + tg);
			System.out.println("Style: " + style);
		}
	}

	SeqMapView getSeqMapView() {
		return gviewer;
	}
}
