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

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.operator.OperatorComparator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenuItem;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.action.*;
import com.affymetrix.igb.shared.*;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.AffyTieredMap.ActionToggler;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.factories.DefaultTierGlyph;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.swing.*;

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

	private JMenu addOperationMenu(List<SeqSymmetry> syms) {
		JMenu operationsMenu = null;
		TreeSet<Operator> operators = new TreeSet<Operator>(new OperatorComparator());
		operators.addAll(ExtensionPointHandler.getExtensionPoint(Operator.class).getExtensionPointImpls());
		for (Operator operator : operators) {
			if (TrackUtils.getInstance().checkCompatible(syms, operator, false)) { // cannot handle Operators with parameters
				String title = operator.getDisplay();
				JMenuItem operatorMI = new JMenuItem(title);
				operatorMI.addActionListener(new TrackOperationAction(operator));
				if (operationsMenu == null) {
					operationsMenu = new JMenu(BUNDLE.getString("operationsMenu"));
				}
				operationsMenu.add(operatorMI);
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
		showMenu.setEnabled(false);
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
						repack(false, true);
					}
				});
				showMenu.add(show_tier);
				showMenu.setEnabled(true);
			}
		}
		showMenu.setEnabled(containHiddenTiers);
		return showMenu;
	}

	@Override
	public void popupNotify(javax.swing.JPopupMenu popup, final TierLabelManager handler) {
		final List<TierLabelGlyph> labels = handler.getSelectedTierLabels();
		int num_selections = labels.size();
		boolean any_are_collapsed = false;
		boolean any_are_expanded = false;
		boolean any_are_color_on = false; // whether any allow setColorByScore()
		boolean any_are_color_off = false; // whether any allow setColorByScore()
		boolean any_are_separate_tiers = false;
		boolean any_are_single_tier = false;
		boolean any_view_mode = false;
		boolean coordinates_track_selected = false;
		boolean containHiddenTiers = false;
		boolean any_lockable = false;
		boolean any_locked = false;
		boolean all_but_one_locked = false;
		
		for (TierLabelGlyph label : labels) {
			TierGlyph glyph = label.getReferenceTier();
			ITrackStyleExtended astyle = glyph.getAnnotStyle();
			any_are_color_on = any_are_color_on || astyle.getColorByScore();
			any_are_color_off = any_are_color_off || (!astyle.getColorByScore());
			if (!astyle.isGraphTier()) {
				any_are_separate_tiers = any_are_separate_tiers || astyle.getSeparate();
				any_are_single_tier = 
						any_are_single_tier || (!astyle.getSeparate() && 
						MapTierTypeHolder.getInstance().supportsTwoTrack(glyph.getFileTypeCategory()));
			}
			any_view_mode = any_view_mode || (!astyle.isGraphTier());

			if (astyle.getExpandable()) {
				any_are_collapsed = any_are_collapsed || astyle.getCollapsed();
				any_are_expanded = any_are_expanded || !astyle.getCollapsed();
			}
			String name = label.getReferenceTier().getAnnotStyle().getTrackName();
			if (name.equals(TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
				coordinates_track_selected = true;
			}
			
			any_lockable = any_lockable || glyph.getTierType() == TierGlyph.TierType.ANNOTATION;
			any_locked = any_locked || (glyph instanceof DefaultTierGlyph && ((DefaultTierGlyph)glyph).isHeightFixed());
		}

		int no_of_locked = 0;
		for (TierLabelGlyph label : handler.getAllTierLabels()) {
			TierGlyph tier = (TierGlyph) label.getInfo();
			ITrackStyleExtended style = tier.getAnnotStyle();
			if (!style.getShow()) {
				containHiddenTiers = true;
			}
			if(style.getShow() && tier instanceof DefaultTierGlyph && ((DefaultTierGlyph)tier).isHeightFixed()){
				no_of_locked++;
			}
		}
		all_but_one_locked = no_of_locked == handler.getVisibleTierGlyphs().size() - 2;

		TierGlyph tierGlyph = (num_selections == 1 ? (TierGlyph) labels.get(0).getInfo() : null);
		JMenuItem customize = new JRPMenuItemTLP(CustomizeAction.getAction());
		popup.add(customize);
		popup.add(addChangeMenu(num_selections, any_are_expanded, any_are_separate_tiers, any_are_single_tier, any_are_color_off, coordinates_track_selected));
		strandsMenu.removeAll();
		strandsMenu.add(at1);
		strandsMenu.add(at2);
		JMenuItem show_two_tiers = new JRPMenuItemTLP(ShowTwoTiersAction.getAction());
		GenericActionHolder.getInstance().addGenericAction(ShowTwoTiersAction.getAction());
		show_two_tiers.setEnabled(any_are_single_tier && num_selections > 0 && !coordinates_track_selected);
		strandsMenu.add(show_two_tiers);
		JMenuItem show_one_tier = new JRPMenuItemTLP(ShowOneTierAction.getAction());
		GenericActionHolder.getInstance().addGenericAction(ShowOneTierAction.getAction());
		show_one_tier.setEnabled(any_are_separate_tiers);
		strandsMenu.add(show_one_tier);
		strandsMenu.setEnabled(!coordinates_track_selected);
		popup.add(strandsMenu);
		JMenuItem collapse = new JRPMenuItemTLP(CollapseAction.getAction());
		collapse.setEnabled(any_are_expanded);
		popup.add(collapse);
		JMenuItem expand = new JRPMenuItemTLP(ExpandAction.getAction());
		expand.setEnabled(any_are_collapsed);
		popup.add(expand);
		JCheckBoxMenuItem lock = new JCheckBoxMenuItem();
		if(any_locked){
			lock.setAction(UnlockTierHeightAction.getAction());
			lock.setSelected(any_locked);
			lock.setText(LockTierHeightAction.getAction().getDisplay());
		}else{
			lock.setAction(LockTierHeightAction.getAction());
		}
		lock.setEnabled(any_lockable);
		if(!any_locked){
			lock.setEnabled(!all_but_one_locked && any_lockable);
		}
		popup.add(lock);
//		JMenuItem repack_selected_tiers = new JRPMenuItemTLP(RepackSelectedTiersAction.getAction());
//		repack_selected_tiers.setEnabled(num_selections > 0 && !coordinates_track_selected);
//		popup.add(repack_selected_tiers);
		popup.add(new JSeparator());
		popup.add(new JRPMenuItemTLP(CenterAtHairlineAction.getAction()));
		if (num_selections == 1 && ((TierGlyph) labels.get(0).getInfo()).getDirection() != TierGlyph.Direction.AXIS) {
			JMenuItem maximize_track = new JRPMenuItemTLP(MaximizeTrackAction.getAction());
			popup.add(maximize_track);
		}
		popup.add(new JSeparator());
		JMenuItem hide = new JRPMenuItemTLP(HideAction.getAction());
		hide.setEnabled(num_selections > 0);
		popup.add(hide);
		popup.add(addShowMenu(containHiddenTiers));
		JMenuItem show_all = new JRPMenuItemTLP(ShowAllAction.getAction());
		show_all.setEnabled(containHiddenTiers);
		popup.add(show_all);
		JMenuItem remove_data_from_tracks = new JRPMenuItemTLP(RemoveDataFromTracksAction.getAction());
		remove_data_from_tracks.setEnabled(num_selections > 0 && !coordinates_track_selected);
		popup.add(remove_data_from_tracks); // Remove data from selected tracks.
		JMenuItem save_track = new JRPMenuItemTLP(ExportFileAction.getAction());
		save_track.setEnabled(num_selections == 1 && !coordinates_track_selected);
		popup.add(save_track);
		JMenuItem save_selected_annotations = new JRPMenuItemTLP(ExportSelectedAnnotationFileAction.getAction());
		save_selected_annotations.setEnabled(tierGlyph != null && !tierGlyph.getSelected().isEmpty());
		popup.add(save_selected_annotations);
		if (tierGlyph != null) {
			// Check whether this selection is a graph or an annotation
			ITrackStyleExtended style = tierGlyph.getAnnotStyle();
			GenericFeature feature = style.getFeature();
			if (feature != null) {
				if (tierGlyph.getFileTypeCategory() == FileTypeCategory.Sequence) {
					popup.add(new JSeparator());
					JMenuItem use_as_reference_seq = new JRPMenuItemTLP(UseAsReferenceSeqAction.getAction());
					popup.add(use_as_reference_seq);
				}

				if (feature.friendlyURL != null) {
					popup.add(new JRPMenuItemTLP(new FeatureInfoAction(feature.friendlyURL.toString())));
				}
			}
		}
		popup.add(new JSeparator());
		JMenu operationsMenu = addOperationMenu(TrackUtils.getInstance().getSymsFromLabelGlyphs(labels));
		if (operationsMenu != null) {
			popup.add(operationsMenu);
		}
		JCheckBoxMenuItem color_by_score = new JCheckBoxMenuItem(ColorByScoreAction.getAction());
		color_by_score.setSelected(!any_are_color_off && num_selections > 0 && !coordinates_track_selected);
		color_by_score.setEnabled(num_selections == 1);
		popup.add(color_by_score);
		JMenuItem set_color_by_score = new JRPMenuItemTLP(SetColorByScoreAction.getAction());
		popup.add(set_color_by_score);
		popup.add(new JSeparator());
		popup.add(new JRPMenuItemTLP(AutoLoadThresholdAction.getAction()));
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
