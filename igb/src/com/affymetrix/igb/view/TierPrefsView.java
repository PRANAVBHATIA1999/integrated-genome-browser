package com.affymetrix.igb.view;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.igb.Application;
import java.awt.Color;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.style.ITrackStyle;
import com.affymetrix.genometryImpl.symmetry.DerivedSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithProps;
import com.affymetrix.genoviz.swing.BooleanTableCellRenderer;
import com.affymetrix.genoviz.swing.ColorTableCellRenderer;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPCheckBox;
import com.affymetrix.genoviz.swing.recordplayback.JRPComboBox;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.igb.glyph.MapViewModeHolder;
import com.affymetrix.igb.tiers.TrackConstants;
import com.affymetrix.igb.tiers.TrackConstants.DIRECTION_TYPE;
import com.affymetrix.igb.view.load.DataManagementTable;
import com.jidesoft.combobox.ColorComboBox;
import com.jidesoft.grid.ColorCellEditor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class TierPrefsView implements ListSelectionListener {

	public static final long serialVersionUID = 1l;
	public static TierPrefsView singleton;
	public static final String TRACK_NAME = "Display Name";
	public static final String FOREGROUND = "Foreground";
	public static final String BACKGROUND = "Background";
	public static final String TRACK_NAME_SIZE = "Track Name Size";
	public final static String[] col_headings = {
		TRACK_NAME,
		BACKGROUND, FOREGROUND,
		TRACK_NAME_SIZE //    GRAPH_TIER,
	};
	//subclass variables
	public static final int COL_TRACK_NAME = 0;
	public static final int COL_BACKGROUND = 1;
	public static final int COL_FOREGROUND = 2;
	public static final int COL_TRACK_NAME_SIZE = 3;
	public static final int COL_COLLAPSED = 4;
	public static final int COL_MAX_DEPTH = 5;
	public static final int COL_SHOW2TRACKS = 6;
	public static final int COL_LABEL_FIELD = 7;
	public static final int COL_CONNECTED = 8;
	public static final int COL_DIRECTION_TYPE = 9;
	public static final int COL_POS_STRAND_COLOR = 10;
	public static final int COL_NEG_STRAND_COLOR = 11;
	public static final int COL_VIEW_MODE = 12;
	public TierPrefsTableModel model;
	public ListSelectionModel lsm;
	public static final String PREF_AUTO_REFRESH = "Auto-Apply Track Customizer Changes";
	public static final boolean default_auto_refresh = true;
	public static final String AUTO_REFRESH = "Auto Refresh";
	public SeqMapView smv;
	public boolean initializationDetector; //Test to detect action events triggered by clicking a row in the table.
	public boolean settingValueFromTable;  //Test to prevent action events triggered by the setValueAt method from calling the method again.  This improves efficiency.
	public float trackNameSize;
	public int[] selectedRows;
	public List<TierLabelGlyph> selectedTiers;
	public int selectedRow;
	public TrackStyle selectedStyle;
	public List<TierGlyph> currentTiers;
	public List<TrackStyle> currentStyles;
	public JRPButton applyToAllButton;
	public JRPCheckBox arrowCheckBox;
	public JCheckBox autoRefreshCheckBox;
	public ColorComboBox bgColorComboBox;
	public JRPCheckBox collapsedCheckBox;
	public JRPCheckBox colorCheckBox;
	public JRPCheckBox connectedCheckBox;
	public JRPTextField displayNameTextField;
	public ColorComboBox fgColorComboBox;
	public JRPComboBox labelFieldComboBox;
	public JRPTextField maxDepthTextField;
	public ColorComboBox negativeColorComboBox;
	public ColorComboBox possitiveColorComboBox;
	public JRPButton refreshButton;
	public JRPCheckBox show2TracksCheckBox;
	public ButtonGroup showStrandButtonGroup;
	public JTable table;
	public JRPComboBox trackNameSizeComboBox;
	public JRPComboBox viewModeCB;
	public JLabel applyToAllTip;

	public static synchronized TierPrefsView getSingleton() {
		if (singleton == null) {
			singleton = new TierPrefsView();
		}
		return singleton;
	}

	public TierPrefsView() {
		Application igb = Application.getSingleton();
		if (igb != null) {
			smv = igb.getMapView();
		}

		initComponents();
	}

	private void initComponents() {
		model = new TierPrefsTableModel();

		initTable();

		displayNameTextField = new JRPTextField("TierPrefsView_displayNameTextField");
		fgColorComboBox = new ColorComboBox();
		bgColorComboBox = new ColorComboBox();
		trackNameSizeComboBox = new JRPComboBox("TierPrefsView_trackNameSizeComboBox");
		maxDepthTextField = new JRPTextField("TierPrefsView_maxDepthTextField");
		labelFieldComboBox = new JRPComboBox("TierPrefsView_labelFieldComboBox");
		show2TracksCheckBox = new JRPCheckBox("TierPrefsView_show2TracksCheckBox");
		connectedCheckBox = new JRPCheckBox("TierPrefsView_connectedCheckBox");
		collapsedCheckBox = new JRPCheckBox("TierPrefsView_collapsedCheckBox");
		showStrandButtonGroup = new javax.swing.ButtonGroup();
		possitiveColorComboBox = new ColorComboBox();
		negativeColorComboBox = new ColorComboBox();
		colorCheckBox = new JRPCheckBox("TierPrefsView_colorCheckBox");
		arrowCheckBox = new JRPCheckBox("TierPrefsView_arrowCheckBox");
		viewModeCB = new JRPComboBox("TierPrefsView_viewModeCB");
		applyToAllButton = new JRPButton("TierPrefsView_applyToAllButton");
		refreshButton = new JRPButton("TierPrefsView_refreshButton");

		displayNameTextField.setEnabled(false);
		bgColorComboBox.setEnabled(false);
		fgColorComboBox.setEnabled(false);
		trackNameSizeComboBox.setEnabled(false);
		labelFieldComboBox.setEnabled(false);
		maxDepthTextField.setEnabled(false);
		show2TracksCheckBox.setEnabled(false);
		connectedCheckBox.setEnabled(false);
		collapsedCheckBox.setEnabled(false);
		colorCheckBox.setEnabled(false);
		arrowCheckBox.setEnabled(false);
		possitiveColorComboBox.setEnabled(false);
		negativeColorComboBox.setEnabled(false);
		viewModeCB.setEnabled(false);
		applyToAllButton.setEnabled(false);

		trackNameSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.SUPPORTED_SIZE));

		labelFieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.LABELFIELD));

		viewModeCB.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.VIEWMODE));

		ImageIcon infoIcon = CommonUtils.getInstance().getIcon("images/info.png");

		applyToAllTip = new javax.swing.JLabel();
		applyToAllTip.setToolTipText("Apply Background, Foreground, and Name Size to all tracks.");
		applyToAllTip.setIcon(infoIcon);

		// Add a "refresh map" button, if there is an instance of IGB
		if (smv != null) {
			refreshButton.addActionListener(new ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent evt) {
					refreshSeqMapView();
				}
			});

			autoRefreshCheckBox = PreferenceUtils.createCheckBox(AUTO_REFRESH,
					PreferenceUtils.getTopNode(), PREF_AUTO_REFRESH,
					default_auto_refresh);
			autoRefreshCheckBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					if (refreshButton != null) {
						refreshButton.setEnabled(!autoRefreshCheckBox.isSelected());
						if (autoRefreshCheckBox.isSelected()) {
							refreshSeqMapView();
						}
					}
				}
			});
			refreshButton.setEnabled(!autoRefreshCheckBox.isSelected());
		}
	}

	private void initTable() {
		table = new JTable();
		model.addTableModelListener(new javax.swing.event.TableModelListener() {

			public void tableChanged(javax.swing.event.TableModelEvent e) {
				// do nothing.
			}
		});

		lsm = table.getSelectionModel();
		lsm.addListSelectionListener(this);
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		table.setRowSelectionAllowed(true);

		ColorCellEditor cellEditor = new ColorCellEditor() {

			private static final long serialVersionUID = 1L;

			@Override
			protected ColorComboBox createColorComboBox() {
				final ColorComboBox combobox = new ColorComboBox();
				combobox.setColorValueVisible(false);
				combobox.setCrossBackGroundStyle(false);
				combobox.setButtonVisible(false);
				combobox.setStretchToFit(true);
				return combobox;
			}
		};
		table.setDefaultRenderer(Color.class, new ColorTableCellRenderer());
		table.setDefaultEditor(Color.class, cellEditor);
		table.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());
		table.setDefaultEditor(Float.class, new DefaultCellEditor(new JComboBox(TrackConstants.SUPPORTED_SIZE)));
		table.setDefaultEditor(TrackConstants.DIRECTION_TYPE.class, new DefaultCellEditor(new JComboBox(TrackConstants.DIRECTION_TYPE.values())));
		table.setModel(model);

		table.getColumnModel().getColumn(COL_FOREGROUND).setPreferredWidth(80);
		table.getColumnModel().getColumn(COL_FOREGROUND).setMinWidth(80);
		table.getColumnModel().getColumn(COL_FOREGROUND).setMaxWidth(80);
		table.getColumnModel().getColumn(COL_BACKGROUND).setPreferredWidth(80);
		table.getColumnModel().getColumn(COL_BACKGROUND).setMinWidth(80);
		table.getColumnModel().getColumn(COL_BACKGROUND).setMaxWidth(80);
		table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setPreferredWidth(110);
		table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setMinWidth(110);
		table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setMaxWidth(110);

		Font f = new Font("SansSerif", Font.BOLD, 12);
		table.getTableHeader().setFont(f);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
	}

	public void setTier_label_glyphs(List<TierLabelGlyph> tier_label_glyphs) {
		selectedTiers = tier_label_glyphs;

		//set Selected Rows
		ITrackStyle style;
		TierGlyph tier;
		table.removeRowSelectionInterval(0, table.getRowCount() - 1);
		for (TierLabelGlyph tlg : selectedTiers) {
			tier = (TierGlyph) tlg.getInfo();
			style = tier.getAnnotStyle();

			for (int i = 0; i < table.getRowCount(); i++) {
				if (model.getValueAt(i, 0).equals(style.getTrackName())) {
					table.addRowSelectionInterval(i, i);
				}
			}
		}
	}

	/** Whether or not changes to the trackOptionsTable should automatically be
	 *  applied to the view.
	 */
	public boolean autoApplyChanges() {
		return PreferenceUtils.getBooleanParam(PREF_AUTO_REFRESH,
				default_auto_refresh);
	}

	public void refreshSeqMapView() {
		if (smv != null) {
			smv.setAnnotatedSeq(smv.getAnnotatedSeq(), true, true, true);
		}
	}

	public void refreshList() {
		if (currentStyles == null) {
			currentStyles = new ArrayList<TrackStyle>();
		}

		boolean isContained = true;

		if (smv != null) {
			currentTiers = smv.getSeqMap().getTiers();
			LinkedHashMap<TrackStyle, TrackStyle> stylemap = new LinkedHashMap<TrackStyle, TrackStyle>();
			Iterator<TierGlyph> titer = currentTiers.iterator();
			while (titer.hasNext()) {
				TierGlyph tier = titer.next();
				ITrackStyle style = tier.getAnnotStyle();

				if (!currentStyles.contains(style)) {
					isContained = false;
				}

				if (style instanceof TrackStyle
						&& style.getShow()
						&& tier.getChildCount() > 0) {
					stylemap.put((TrackStyle) style, (TrackStyle) style);
				}
			}

			currentStyles.clear();
			currentStyles.addAll(stylemap.values());
		}

		ArrayList<TrackStyle> customizables = new ArrayList<TrackStyle>(currentStyles.size());
		for (int i = 0; i < currentStyles.size(); i++) {
			TrackStyle the_style = currentStyles.get(i);
			if (the_style.getCustomizable()) {
				// if graph tier style then only include if include_graph_styles toggle is set (app is _not_ IGB)
				//if ((!the_style.isGraphTier())) {
				customizables.add(the_style);
				//}
			}
		}

		if (!isContained) {
			model.setStyles(customizables);
			model.fireTableDataChanged();
			if (table.getRowCount() != 0) {
				table.setRowSelectionInterval(0, 0);
				this.initialLabelField();
			}
		}
	}

	public void applyChanges() {
		refreshSeqMapView();
	}

	public void externalChange() {
		model.fireTableDataChanged();
		if (table.getRowCount() != 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}

	/** Called when the user selects a row of the table.
	 * @param evt
	 */
	public void valueChanged(ListSelectionEvent evt) {
		displayNameTextField.setEnabled(true);
		viewModeCB.setEnabled(true);
		labelFieldComboBox.setEnabled(true);
		maxDepthTextField.setEnabled(true);
		connectedCheckBox.setEnabled(true);
		collapsedCheckBox.setEnabled(true);
		colorCheckBox.setEnabled(true);
		arrowCheckBox.setEnabled(true);
		possitiveColorComboBox.setEnabled(true);
		negativeColorComboBox.setEnabled(true);
		show2TracksCheckBox.setEnabled(true);
		displayNameTextField.setEnabled(true);
		bgColorComboBox.setEnabled(true);
		fgColorComboBox.setEnabled(true);
		trackNameSizeComboBox.setEnabled(true);
		applyToAllButton.setEnabled(true);
		labelFieldComboBox.setEnabled(true);
		maxDepthTextField.setEnabled(true);
		selectedRows = table.getSelectedRows();

		initializationDetector = true;

		if (table.getRowCount() == 0) {
			displayNameTextField.setEnabled(false);
			viewModeCB.setEnabled(false);
			labelFieldComboBox.setEnabled(false);
			maxDepthTextField.setEnabled(false);
			connectedCheckBox.setEnabled(false);
			collapsedCheckBox.setEnabled(false);
			colorCheckBox.setEnabled(false);
			arrowCheckBox.setEnabled(false);
			possitiveColorComboBox.setEnabled(false);
			negativeColorComboBox.setEnabled(false);
			show2TracksCheckBox.setEnabled(false);
			displayNameTextField.setEnabled(false);
			bgColorComboBox.setEnabled(false);
			fgColorComboBox.setEnabled(false);
			trackNameSizeComboBox.setEnabled(false);
			applyToAllButton.setEnabled(false);
			labelFieldComboBox.setEnabled(false);
			maxDepthTextField.setEnabled(false);
		}

		if (selectedRows.length > 1) {
			displayNameTextField.setEnabled(false);
			viewModeCB.setEnabled(false);
			bgColorComboBox.setSelectedColor(null);
			fgColorComboBox.setSelectedColor(null);
			trackNameSizeComboBox.setSelectedItem("");
			labelFieldComboBox.setSelectedIndex(-1);
			maxDepthTextField.setText(null);
			show2TracksCheckBox.setSelected(false);
			connectedCheckBox.setSelected(false);
			collapsedCheckBox.setSelected(false);
			colorCheckBox.setSelected(false);
			arrowCheckBox.setSelected(false);
			possitiveColorComboBox.setSelectedColor(null);
			negativeColorComboBox.setSelectedColor(null);
		}

		if (selectedRows.length == 1) {
			selectedStyle = model.getStyles().get(selectedRows[0]);

			if (selectedStyle.getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_COORDINATE_INSTANCE)
					|| selectedStyle.isGraphTier()) {
				if (!selectedStyle.isGraphTier()) {
					displayNameTextField.setEnabled(false);
				}
				viewModeCB.setEnabled(false);
				labelFieldComboBox.setEnabled(false);
				maxDepthTextField.setEnabled(false);
				connectedCheckBox.setEnabled(false);
				collapsedCheckBox.setEnabled(false);
				colorCheckBox.setEnabled(false);
				arrowCheckBox.setEnabled(false);
				possitiveColorComboBox.setEnabled(false);
				negativeColorComboBox.setEnabled(false);
				show2TracksCheckBox.setEnabled(false);
			} else {
				initialLabelField();
			}

			possitiveColorComboBox.setSelectedColor(selectedStyle.getForwardColor());
			negativeColorComboBox.setSelectedColor(selectedStyle.getReverseColor());
			String file_type = selectedStyle.getFileType();
			viewModeCB.removeAllItems();

			if (!selectedStyle.getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_COORDINATE_INSTANCE) && !selectedStyle.isGraphTier()) {
				viewModeCB.setModel(new javax.swing.DefaultComboBoxModel(MapViewModeHolder.getInstance().getAllViewModesFor(file_type)));
				String view_mode = selectedStyle.getViewMode();
				if (view_mode == null) {
					viewModeCB.setSelectedIndex(0);
				} else {
					viewModeCB.setSelectedItem(view_mode);
				}
			}

			displayNameTextField.setText(selectedStyle.getTrackName());
			bgColorComboBox.setSelectedColor(selectedStyle.getBackground());
			fgColorComboBox.setSelectedColor(selectedStyle.getForeground());
			trackNameSizeComboBox.setSelectedItem(selectedStyle.getTrackNameSize());
			labelFieldComboBox.setSelectedItem(selectedStyle.getLabelField());
			maxDepthTextField.setText(String.valueOf(selectedStyle.getMaxDepth()));
			show2TracksCheckBox.setSelected(selectedStyle.getSeparate());
			connectedCheckBox.setSelected(selectedStyle.getConnected());
			collapsedCheckBox.setSelected(selectedStyle.getCollapsed());

			DIRECTION_TYPE direction = selectedStyle.getDirectionName();
			switch (direction) {
				case NONE:
					colorCheckBox.setSelected(false);
					arrowCheckBox.setSelected(false);
					break;
				case ARROW:
					colorCheckBox.setSelected(true);
					arrowCheckBox.setSelected(false);
					break;
				case COLOR:
					colorCheckBox.setSelected(true);
					arrowCheckBox.setSelected(false);
					break;
				case BOTH:
					colorCheckBox.setSelected(true);
					arrowCheckBox.setSelected(true);
					break;
				default:
					System.out.println("Wrong Direction Type");
			}
		}

		initializationDetector = false;
	}

	private void initialLabelField() {
		SeqSymmetry sym = smv.getAnnotatedSeq().getAnnotation(selectedStyle.getMethodName());
		if (sym != null && sym.getChildCount() > 0) {
			SeqSymmetry child = sym.getChild(0);
			SeqSymmetry original = getMostOriginalSymmetry(child);
			if (original instanceof SymWithProps) {
				Map<String, Object> props = ((SymWithProps) original).getProperties();

				ArrayList<String> properties = new ArrayList<String>();
				properties.add((String) TrackConstants.LABELFIELD[0]);
				properties.addAll(props.keySet());
				labelFieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(properties.toArray()));
			}
		}
	}

	private static SeqSymmetry getMostOriginalSymmetry(SeqSymmetry sym) {
		if (sym instanceof DerivedSeqSymmetry) {
			return getMostOriginalSymmetry(((DerivedSeqSymmetry) sym).getOriginalSymmetry());
		}
		return sym;
	}

	/**
	 *  Call this whenver this component is removed from the view, due to the
	 *  tab pane closing or the window closing.  It will decide whether it is
	 *  necessary to update the SeqMapView in response to changes in settings
	 *  in this panel.
	 */
	public void removedFromView() {
		// if autoApplyChanges(), then the changes were already applied,
		// otherwise apply changes as needed.
		if (!autoApplyChanges()) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					applyChanges();
				}
			});
		}
	}

	public void show2TracksCheckBox() {
		if (!settingValueFromTable) {
			model.setValueAt(show2TracksCheckBox.isSelected(), selectedRows[0], COL_SHOW2TRACKS);
		}
	}

	public void connectedCheckBox() {
		if (!settingValueFromTable) {
			model.setValueAt(connectedCheckBox.isSelected(), selectedRows[0], COL_CONNECTED);
		}
	}

	public void collapsedCheckBox() {
		if (!settingValueFromTable) {
			model.setValueAt(collapsedCheckBox.isSelected(), selectedRows[0], COL_COLLAPSED);
		}
	}

	public void displayNameTextField() {
		if (!settingValueFromTable) {
			model.setValueAt(displayNameTextField.getText(), selectedRows[0], COL_TRACK_NAME);
		}
	}

	public void maxDepthTextField() {
		if (!settingValueFromTable) {
			model.setValueAt(maxDepthTextField.getText(), selectedRows[0], COL_MAX_DEPTH);
		}
	}

	public void trackNameSizeComboBox() {
		if (!settingValueFromTable
				&& !initializationDetector) {   // !initializationDetector condition is for the initialization when multiple rows are selected to prevent null exception
			trackNameSize = Float.parseFloat(trackNameSizeComboBox.getSelectedItem().toString());
			model.setValueAt(trackNameSize, selectedRows[0], COL_TRACK_NAME_SIZE);
		}
	}

	public void fgColorComboBox() {
		if (!settingValueFromTable) {
			model.setValueAt(fgColorComboBox.getSelectedColor(), selectedRows[0], COL_FOREGROUND);
		}
	}

	public void bgColorComboBox() {
		if (!settingValueFromTable) {
			model.setValueAt(bgColorComboBox.getSelectedColor(), selectedRows[0], COL_BACKGROUND);
		}
	}

	public void applyToAllButton() {
		selectedRow = table.getSelectedRow();
		ITrackStyle style;
		for (int i = 0; i < table.getRowCount(); i++) {
			style = model.getStyles().get(i);
			if (!style.getTrackName().equalsIgnoreCase(
					TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
				model.setValueAt(model.getStyles().get(selectedRow).getBackground(),
						i, COL_BACKGROUND, true);
				model.setValueAt(model.getStyles().get(selectedRow).getTrackNameSize(),
						i, COL_TRACK_NAME_SIZE, true);
				model.setValueAt(model.getStyles().get(selectedRow).getForeground(),
						i, COL_FOREGROUND, false);
			}
			if (i == table.getRowCount() - 1) {
				applyChanges();
			}
		}
	}

	public void labelFieldComboBox() {
		if (!settingValueFromTable) {
			model.setValueAt(labelFieldComboBox.getSelectedItem(), selectedRows[0], COL_LABEL_FIELD);
		}
	}

	public void possitiveColorComboBox() {
		if (!settingValueFromTable) {
			model.setValueAt(possitiveColorComboBox.getSelectedColor(), selectedRows[0], COL_POS_STRAND_COLOR);
		}
	}

	public void negativeColorComboBox() {
		if (!settingValueFromTable) {
			model.setValueAt(negativeColorComboBox.getSelectedColor(), selectedRows[0], COL_NEG_STRAND_COLOR);
		}
	}

	public void colorCheckBox() {
		if (!settingValueFromTable) {
			if (colorCheckBox.isSelected()) {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.BOTH,
							selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.COLOR,
							selectedRows[0], COL_DIRECTION_TYPE);
				}
			} else {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.ARROW,
							selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.NONE,
							selectedRows[0], COL_DIRECTION_TYPE);
				}
			}
		}
	}

	public void arrowCheckBox() {
		if (!settingValueFromTable) {
			if (colorCheckBox.isSelected()) {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.BOTH,
							selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.COLOR,
							selectedRows[0], COL_DIRECTION_TYPE);
				}
			} else {
				if (arrowCheckBox.isSelected()) {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.ARROW,
							selectedRows[0], COL_DIRECTION_TYPE);
				} else {
					model.setValueAt(TrackConstants.DIRECTION_TYPE.NONE,
							selectedRows[0], COL_DIRECTION_TYPE);
				}
			}
		}
	}

	public void viewModeCB() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(viewModeCB.getSelectedItem(),
						selectedRows[i], COL_VIEW_MODE);
			}
		}
	}

	public void restoreToDefault() {
		int row;
		TrackStyle style;
		int[] previousSelectedRows = selectedRows;
		for (int i = 0; i < selectedRows.length; i++) {
			row = selectedRows[i];
			style = model.getStyles().get(row);
			style.restoreToDefault();
		}

		model.fireTableDataChanged();

		applyChanges();

		table.setRowSelectionInterval(previousSelectedRows[0], previousSelectedRows[0]);
	}

	class TierPrefsTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		List<TrackStyle> tier_styles;
		private Object tempObject;
		private int tempInt;

		TierPrefsTableModel() {
			this.tier_styles = Collections.<TrackStyle>emptyList();
		}

		public void setStyles(List<TrackStyle> tier_styles) {
			this.tier_styles = tier_styles;
		}

		public List<TrackStyle> getStyles() {
			return this.tier_styles;
		}

		// Allow editing most fields in normal rows, but don't allow editing some
		// fields in the "default" style row.
		@Override
		public boolean isCellEditable(int row, int column) {
//			if (column == COL_TRACK_NAME) {
//				return false;
//			}
			return true;
		}

		@Override
		public Class<?> getColumnClass(int c) {
			tempObject = getValueAt(0, c);
			if (tempObject == null) {
				return Object.class;
			} else {
				return tempObject.getClass();
			}
		}

		public int getColumnCount() {
			return col_headings.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return col_headings[columnIndex];
		}

		public int getRowCount() {
			return tier_styles.size();
		}

		public Object getValueAt(int row, int column) {
			TrackStyle style;
			style = tier_styles.get(row);
			switch (column) {
				case COL_FOREGROUND:
					return style.getForeground();
				case COL_BACKGROUND:
					return style.getBackground();
				case COL_TRACK_NAME_SIZE:
					return style.getTrackNameSize();
				case COL_TRACK_NAME:
					return style.getTrackName();
				default:
					return null;
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {

			for (int i = 0; i < selectedRows.length; i++) {
				if (i == selectedRows.length - 1) {
					setValueAt(value, selectedRows[i], col, true);
				} else {
					setValueAt(value, selectedRows[i], col, false);
				}
			}
		}

		public void setValueAt(Object value, int row, int col, boolean apply) {
			settingValueFromTable = true;
			if (value != null && !initializationDetector) {
				try {
					TrackStyle style = tier_styles.get(row);
					switch (col) {
						case COL_TRACK_NAME:
							//Test prevents a bug allowing Coordinate track to be renamed when multi-selecting
							if (!style.getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
								style.setTrackName((String) value);
								displayNameTextField.setText((String) value);
							}
							break;
						case COL_FOREGROUND:
							style.setForeground((Color) value);
							fgColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_BACKGROUND:
							style.setBackground((Color) value);
							bgColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_TRACK_NAME_SIZE:
							style.setTrackNameSize((Float) value);
							trackNameSizeComboBox.setSelectedItem((Float) value);
							break;
						case COL_LABEL_FIELD:
							style.setLabelField((String) value);
							labelFieldComboBox.setSelectedItem(value);
							break;
						case COL_MAX_DEPTH: {
							tempInt = parseInteger(((String) value), 0, style.getMaxDepth());
							style.setMaxDepth(tempInt);
						}
						break;
						case COL_DIRECTION_TYPE:
							style.setDirectionType((TrackConstants.DIRECTION_TYPE) value);
							break;
						case COL_SHOW2TRACKS:
							style.setSeparate(((Boolean) value).booleanValue());
							break;
						case COL_CONNECTED:
							if (Boolean.TRUE.equals(value)) {
								style.setGlyphDepth(2);
							} else {
								style.setGlyphDepth(1);
							}
							break;
						case COL_COLLAPSED:
							style.setCollapsed(((Boolean) value).booleanValue());
							break;
						case COL_POS_STRAND_COLOR:
							style.setForwardColor((Color) value);
							break;
						case COL_NEG_STRAND_COLOR:
							style.setReverseColor((Color) value);
							break;
						case COL_VIEW_MODE:
							style.setViewMode((String) value);
							break;
						default:
							System.out.println("Unknown column selected: " + col);
					}
					fireTableCellUpdated(row, col);
				} catch (Exception e) {
					// exceptions should not happen, but must be caught if they do
					System.out.println("Exception in TierPrefsView.setValueAt(): " + e);
				}

				if (autoApplyChanges() && apply) {
					if (col == COL_BACKGROUND || col == COL_TRACK_NAME_SIZE
							|| col == COL_TRACK_NAME || col == COL_COLLAPSED) {
						if (col == COL_TRACK_NAME || col == COL_COLLAPSED) {
							smv.getSeqMap().setTierStyles();
							smv.getSeqMap().repackTheTiers(true, true, false);
						}
						if (col == COL_TRACK_NAME || col == COL_BACKGROUND) {
							if (DataManagementTable.getModel() != null) {
								DataManagementTable.getModel().fireTableDataChanged();
							}
						}

						smv.getSeqMap().updateWidget();
					} else {
						applyChanges();
					}
				}

			}
			settingValueFromTable = false;
		}

		/** Parse an integer, using the given fallback if any exception occurrs.
		 *  @param s  The String to parse.
		 *  @param empty_string  the value to return if the input is an empty string.
		 *  @param fallback  the value to return if the input String is unparseable.
		 */
		int parseInteger(String s, int empty_string, int fallback) {
			try {
				if ("".equals(s.trim())) {
					return empty_string;
				} else {
					return Integer.parseInt(s);
				}
			} catch (Exception e) {
				//System.out.println("Exception: " + e);
				// don't report the error, use the fallback value
			}
			return fallback;
		}
	};
}
