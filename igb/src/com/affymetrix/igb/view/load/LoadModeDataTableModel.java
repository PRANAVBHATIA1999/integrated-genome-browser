package com.affymetrix.igb.view.load;

import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.parsers.CytobandParser;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.TrackConstants;
import com.affymetrix.genometryImpl.style.ITrackStyle;
import com.affymetrix.igb.prefs.PreferencesPanel;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.view.SeqMapView;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 * Model for table of features.
 */
public final class LoadModeDataTableModel extends AbstractTableModel implements ChangeListener {

	private static final long serialVersionUID = 1L;
	private static final String[] columnNames = {"", "", "", "FG", "BG", "Choose Load Mode", "Data Set/File Name", "Track Name (Click To Edit)", ""};
	private final Map<String, LoadStrategy> reverseLoadStrategyMap;  // from friendly string to enum
	static final int INFO_FEATURE_COLUMN = 0;
	static final int HIDE_FEATURE_COLUMN = 1;
	static final int REFRESH_FEATURE_COLUMN = 2;
	static final int FOREGROUND_COLUMN = 3;
	static final int BACKGROUND_COLUMN = 4;
	static final int LOAD_STRATEGY_COLUMN = 5;
	static final int FEATURE_NAME_COLUMN = 6;
	static final int TRACK_NAME_COLUMN = 7;
	static final int DELETE_FEATURE_COLUMN = 8;
	private final GeneralLoadView glv;
	private final static featureTableComparator visibleFeatureComp = new featureTableComparator();
	private SeqMapView smv;
	private List<TrackStyle> currentStyles;
	public List<VirtualFeature> virtualFeatures;
	public List<GenericFeature> features;

	LoadModeDataTableModel(GeneralLoadView glv) {
		this.glv = glv;
		this.features = null;
		this.virtualFeatures = new ArrayList<VirtualFeature>();
		Application igb = Application.getSingleton();
		if (igb != null) {
			smv = igb.getMapView();
		}

		// Here we map the friendly string back to the LoadStrategy.
		this.reverseLoadStrategyMap = new HashMap<String, LoadStrategy>(3);
		for (LoadStrategy strategy : EnumSet.allOf(LoadStrategy.class)) {
			this.reverseLoadStrategyMap.put(strategy.toString(), strategy);
		}
	}

	void clearFeatures() {
		if (this.virtualFeatures != null) {
			this.virtualFeatures.clear();
		}
		this.fireTableDataChanged();
	}

	void createVirtualFeatures(List<GenericFeature> features) {
		this.features = features;
		if (virtualFeatures != null) {
			virtualFeatures.clear();
		}
		for (GenericFeature gFeature : features) {
			createPrimaryVirtualFeatures(gFeature);
		}
		if (LoadModeTable.jTable != null) {
			this.fireTableDataChanged();
		}
	}

	void createPrimaryVirtualFeatures(GenericFeature gFeature) {
		currentStyles = this.getCurrentStyles();
		VirtualFeature vFeature = new VirtualFeature(gFeature, currentStyles);
		vFeature.isPrimary = true;
		virtualFeatures.add(vFeature);
		if (gFeature.getMethods().size() > 1 && vFeature.getStyle() != null) {
			createSecondaryVirtualFeatures(vFeature);
		}
	}

	void createSecondaryVirtualFeatures(VirtualFeature vFeature) {
		boolean isPrimary = vFeature.isPrimary;
		VirtualFeature subVfeature;
		for (TrackStyle style : currentStyles) {
			if (style.getFeature().equals(vFeature.getFeature())) {
				subVfeature = new VirtualFeature(vFeature.getFeature(), style);
				if (isPrimary) {
					virtualFeatures.remove(vFeature);
					subVfeature.isPrimary = true;
					isPrimary = false;
				} else {
					subVfeature.isPrimary = false;
				}
				virtualFeatures.add(subVfeature);
			}
		}
	}

	/**
	 * Only want to display features with visible attribute set to true.
	 * @param features
	 * @return list of visible features
	 */
	static List<GenericFeature> getVisibleFeatures(List<GenericFeature> features) {
		if (features == null) {
			return null;
		}
		List<GenericFeature> visibleFeatures = new ArrayList<GenericFeature>();
		for (GenericFeature gFeature : features) {
			if (gFeature.isVisible()) {
				visibleFeatures.add(gFeature);
			}
		}

		Collections.sort(visibleFeatures, visibleFeatureComp);

		// Also sort these features so the features to be loaded are at the top.

		return visibleFeatures;
	}

	private final static class featureTableComparator implements Comparator<GenericFeature> {

		public int compare(GenericFeature feature1, GenericFeature feature2) {
			if (feature1.getLoadStrategy() != feature2.getLoadStrategy()) {
				return (feature1.getLoadStrategy().compareTo(feature2.getLoadStrategy()));
			}
			if (feature1.featureName.compareTo(feature2.featureName) != 0) {
				return feature1.featureName.compareTo(feature2.featureName);
			}
			return feature1.gVersion.gServer.serverType.compareTo(
					feature2.gVersion.gServer.serverType);
		}
	}

	VirtualFeature getFeature(int row) {
		return (getRowCount() <= row) ? null : virtualFeatures.get(row);
	}

	private int getRow(VirtualFeature feature) {
		return (virtualFeatures == null) ? -1 : virtualFeatures.indexOf(feature);

	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return (virtualFeatures == null) ? 0 : virtualFeatures.size();
	}

	public void genericFeatureRefreshed(GenericFeature feature) {
		int row = -1;
		for (VirtualFeature vFeature : virtualFeatures) {
			if (vFeature.getFeature().equals(feature)) {
				row = getRow(vFeature);
			}
		}
		if (row >= 0) {
			fireTableCellUpdated(row, INFO_FEATURE_COLUMN);
		}
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		if (virtualFeatures == null || virtualFeatures.isEmpty()) {
			// Indicate to user that there's no data.
			if (row == 0 && col == 2) {
				return "No feature data found";
			}
			return "";
		}

		VirtualFeature vFeature;
		ITrackStyle style;
		if (getFeature(row) == null) {
			return "";
		} else {
			vFeature = getFeature(row);
			style = vFeature.getStyle();
		}

		switch (col) {
			case INFO_FEATURE_COLUMN:
				return "";
			case REFRESH_FEATURE_COLUMN:
				return "";
			case LOAD_STRATEGY_COLUMN:
				// return the load strategy
				if (!vFeature.isPrimary) {
					return "";
				}
				return vFeature.getLoadStrategy().toString();
			case FEATURE_NAME_COLUMN:
				// the friendly feature name removes slashes.  Clip it here.
				if (vFeature.getServer() == ServerType.QuickLoad) {
					return vFeature.getFeature().featureName;
				} else if (!vFeature.isPrimary) {
					return "";
				}
				return vFeature.getFeature().featureName;
			case TRACK_NAME_COLUMN:
				if (vFeature.getFeature().featureName.equals(CytobandParser.CYTOBAND_TIER_NAME)) {
					return "Uneditable Track";
				} else if (style == null) {
					return "No Data Loaded";
				}
				return style.getTrackName();
			case BACKGROUND_COLUMN:
				if (style == null) {
					return Color.WHITE;
				}
				return style.getBackground();
			case FOREGROUND_COLUMN:
				if (style == null) {
					return Color.WHITE;
				}
				return style.getForeground();
			case DELETE_FEATURE_COLUMN:
				return "";
			case HIDE_FEATURE_COLUMN:
				return "";
			default:
				System.out.println("Shouldn't reach here: " + row + " " + col);
				return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int c) {
		if ((getValueAt(0, c)) == null) {
			System.out.println("Null Reference ERROR: column " + c);
		}
		return getValueAt(0, c).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		VirtualFeature vFeature = virtualFeatures.get(row);
		if ((vFeature.getStyle() == null)
				&& (col == TRACK_NAME_COLUMN
				|| col == BACKGROUND_COLUMN || col == FOREGROUND_COLUMN
				|| col == HIDE_FEATURE_COLUMN)) {
			return false;
		} else if (col == FEATURE_NAME_COLUMN) {
			return false;
		} else if ((col == DELETE_FEATURE_COLUMN || col == REFRESH_FEATURE_COLUMN)
				&& !vFeature.isPrimary) {
			return false;
		} else if (col == DELETE_FEATURE_COLUMN || col == REFRESH_FEATURE_COLUMN
				|| col == HIDE_FEATURE_COLUMN || col == TRACK_NAME_COLUMN
				|| col == BACKGROUND_COLUMN || col == FOREGROUND_COLUMN) {
			return true;
		} else if (col == INFO_FEATURE_COLUMN) {
			switch (vFeature.getFeature().getLastRefreshStatus()) {
				case NO_DATA_LOADED:
					return true;
				default:
					return false;
			}
		} else if (getFeature(row) == null) {
			return false;
		}
		// This cell is only editable if the feature isn't already fully loaded.
		return (getFeature(row).getLoadStrategy() != LoadStrategy.GENOME);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		VirtualFeature vFeature = getFeature(row);

		if (value == null || vFeature == null) {
			return;
		}

		switch (col) {
			case INFO_FEATURE_COLUMN:
				ErrorHandler.errorPanel(vFeature.getFeature().featureName, vFeature.getLastRefreshStatus().toString());
				break;
			case DELETE_FEATURE_COLUMN:
				String message = "Really remove entire " + vFeature.getFeature().featureName + " data set ?";
				if (Application.confirmPanel(message, PreferenceUtils.getTopNode(),
						PreferenceUtils.CONFIRM_BEFORE_DELETE, PreferenceUtils.default_confirm_before_delete)) {
					for (GenericFeature gFeature : features) {
						if (gFeature.equals(vFeature.getFeature())) {
							GeneralLoadView.getLoadView().removeFeature(gFeature, true);
						}
					}
					this.fireTableDataChanged(); //clear row selection
				}
				break;
			case REFRESH_FEATURE_COLUMN:
				if (vFeature.getLoadStrategy() != LoadStrategy.NO_LOAD
						&& vFeature.getLoadStrategy() != LoadStrategy.GENOME) {
					for (GenericFeature gFeature : features) {
						if (gFeature.equals(vFeature.getFeature())) {
							GeneralLoadUtils.loadAndDisplayAnnotations(gFeature);
						}
					}
				}
				break;
			case LOAD_STRATEGY_COLUMN:
				if (vFeature.getFeature().getLoadStrategy() == LoadStrategy.GENOME) {
					return;	// We can't change strategies once we've loaded the entire genome.
				}
				String valueString = value.toString();
				if (!vFeature.getFeature().getLoadStrategy().toString().equals(valueString)) {
					// strategy changed.  Update the feature object.
					vFeature.getFeature().setLoadStrategy(reverseLoadStrategyMap.get(valueString));
					updatedStrategy(row, col, vFeature.getFeature());
				}
				break;
			case HIDE_FEATURE_COLUMN:
				if (vFeature.getStyle() != null) {
					setVisibleTracks(vFeature.getStyle());
				}
				break;
			case BACKGROUND_COLUMN:
				if (vFeature.getStyle() != null) {
					vFeature.getStyle().setBackground((Color) value);
				}
				break;
			case FOREGROUND_COLUMN:
				if (vFeature.getStyle() != null) {
					vFeature.getStyle().setForeground((Color) value);
				}
				break;
			case TRACK_NAME_COLUMN:
				if (vFeature.getStyle() != null) {
					vFeature.getStyle().setTrackName((String) value);
				}
				break;
			default:
				System.out.println("Unknown column selected: " + col);
		}

		fireTableCellUpdated(row, col);
		if (col != LOAD_STRATEGY_COLUMN && col != DELETE_FEATURE_COLUMN
				&& col != INFO_FEATURE_COLUMN
				&& col != FEATURE_NAME_COLUMN
				&& col != REFRESH_FEATURE_COLUMN) {
			refreshSeqMapView();
		}
		PreferencesPanel.getSingleton().tpv.externalChange();
	}

	private void setVisibleTracks(ITrackStyle style) {
		String trackName = style.getTrackName();
		if (style.getShow()) {
			smv.getPopup().hideOneTier(style);
			System.out.println("Here");
		} else {
			for (int i = 0; i < smv.getPopup().getShowMenu().getItemCount(); i++) {
				String text = smv.getPopup().getShowMenu().getItem(i).getText();
				if (text.length() > 29) {
					text = text.substring(0, 30);
				}
				if (trackName.length() > 29) {
					trackName = trackName.substring(0, 30);
				}
				if (text.equalsIgnoreCase(trackName)) {
					style.setShow(true);
					smv.getPopup().getShowMenu().remove(smv.getPopup().getShowMenu().getItem(i));
					smv.getPopup().getHandler().sortTiers();
					smv.getPopup().getHandler().repackTheTiers(false, true);
				}
			}
		}
	}

	private void refreshSeqMapView() {
		if (smv != null) {
			smv.setAnnotatedSeq(smv.getAnnotatedSeq(), true, true, true);
		}
	}

	private List<TrackStyle> getCurrentStyles() {
		ArrayList<TrackStyle> currentStyleList = new ArrayList<TrackStyle>();
		if (smv != null) {
			List<TierGlyph> temp;
			temp = smv.getSeqMap().getTiers();
			LinkedHashMap<TrackStyle, TrackStyle> stylemap = new LinkedHashMap<TrackStyle, TrackStyle>();
			Iterator<TierGlyph> titer = temp.iterator();
			int i = 0;
			while (titer.hasNext()) {
				TierGlyph tier = titer.next();
				ITrackStyle style = tier.getAnnotStyle();
				if ((style instanceof TrackStyle)
						&& (!tier.getAnnotStyle().getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_COORDINATE_INSTANCE))
						) {
					stylemap.put((TrackStyle) style, (TrackStyle) style);
				}
			}
			currentStyleList.addAll(stylemap.values());
		}
		ArrayList<TrackStyle> customizables = new ArrayList<TrackStyle>(currentStyleList.size());
		for (int i = 0; i < currentStyleList.size(); i++) {
			TrackStyle the_style = currentStyleList.get(i);
			if (the_style.getCustomizable()) {
				customizables.add(the_style);
			}
		}

		return customizables;
	}

	/**
	 * The strategy was changed.  Update the table, and if necessary, load the annotations and change the button statuses.
	 * @param row
	 * @param col
	 * @param gFeature
	 */
	private void updatedStrategy(int row, int col, GenericFeature gFeature) {
		fireTableCellUpdated(row, col);

		if (gFeature.getLoadStrategy() == LoadStrategy.GENOME || gFeature.getLoadStrategy() == LoadStrategy.AUTOLOAD) {
			GeneralLoadUtils.loadAndDisplayAnnotations(gFeature);
		}

		//  Whatever feature strategy changed, it may have affected
		// the enable status of the "load visible" button

		this.glv.changeVisibleDataButtonIfNecessary(features);
	}

	public void stateChanged(ChangeEvent evt) {//????
		Object src = evt.getSource();
		if (src instanceof GenericFeature) {
			int row = getRow((VirtualFeature) src);
			if (row >= 0) {  // if typestate is present in table, then send notification of row change
				fireTableRowsUpdated(row, row);
			}
		}
	}
}
