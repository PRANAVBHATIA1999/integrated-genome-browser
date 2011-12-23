package com.affymetrix.igb.view.load;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genoviz.swing.BooleanTableCellRenderer;
import com.affymetrix.genoviz.swing.ButtonTableCellEditor;
import com.affymetrix.genoviz.swing.ColorTableCellRenderer;
import com.affymetrix.genoviz.swing.LabelTableCellRenderer;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.util.JComboBoxToolTipRenderer;
import com.affymetrix.igb.view.SeqMapView;
import com.jidesoft.combobox.ColorComboBox;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.Icon;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import com.jidesoft.grid.ColorCellEditor;
import java.awt.Color;

/**
 * A table with two customizations:
 * 1.  An always-visible combo box. For a user, this differentiates the field from a text box, and thus indicates they have a choice.
 * 2.  Different combo box elements per row.  This allows different behavior per server type.
 */
public final class DataManagementTable {

	private static JTableX jTable;
	private static final JComboBoxToolTipRenderer comboRenderer = new JComboBoxToolTipRenderer();
	static final Icon refresh_icon = CommonUtils.getInstance().getIcon("images/refresh16.png");
	static final Icon delete_icon = CommonUtils.getInstance().getIcon("images/delete.gif");
	static final Icon invisible_icon = CommonUtils.getInstance().getIcon("images/invisible.gif");
	static final Icon visible_icon = CommonUtils.getInstance().getIcon("images/visible.gif");
	static final Icon info_icon = CommonUtils.getInstance().getIcon("images/warning.png");
	static final int INFO_FEATURE_COLUMN = 0;
	static final int HIDE_FEATURE_COLUMN = 1;
	static final int REFRESH_FEATURE_COLUMN = 2;
	static final int FOREGROUND_COLUMN = 3;
	static final int BACKGROUND_COLUMN = 4;
	static final int SEPARATE_COLUMN = 5;
	static final int LOAD_STRATEGY_COLUMN = 6;
	static final int FEATURE_NAME_COLUMN = 7;
	static final int TRACK_NAME_COLUMN = 8;
	static final int DELETE_FEATURE_COLUMN = 9;
	//public static boolean iconTest;

	/**
	 * Set the columns to use the ComboBox DAScb and renderer (which also depends on the row/server type)
	 * @param table
	 * @param column
	 * @param enabled
	 */
	static void setComboBoxEditors(JTableX table, boolean enabled) {
		jTable = table;
		comboRenderer.setToolTipEntry(LoadStrategy.NO_LOAD.toString(), IGBConstants.BUNDLE.getString("noLoadCBToolTip"));
		comboRenderer.setToolTipEntry(LoadStrategy.AUTOLOAD.toString(), IGBConstants.BUNDLE.getString("autoLoadCBToolTip"));
		comboRenderer.setToolTipEntry(LoadStrategy.VISIBLE.toString(), IGBConstants.BUNDLE.getString("visibleCBToolTip"));
		comboRenderer.setToolTipEntry(LoadStrategy.CHROMOSOME.toString(), IGBConstants.BUNDLE.getString("chromosomeCBToolTip"));
		comboRenderer.setToolTipEntry(LoadStrategy.GENOME.toString(), IGBConstants.BUNDLE.getString("genomeCBToolTip"));
		DataManagementTableModel ftm = (DataManagementTableModel) table.getModel();

		int featureSize = ftm.getRowCount();
		RowEditorModel choices = new RowEditorModel(featureSize);
		RowEditorModel action = new RowEditorModel(featureSize);
		RowEditorModel text = new RowEditorModel(featureSize);
		RowEditorModel color = new RowEditorModel(featureSize);
		RowEditorModel bool = new RowEditorModel(featureSize);

		// tell the JTableX which RowEditorModel we are using
		table.setRowEditorModel(DataManagementTableModel.INFO_FEATURE_COLUMN, action);
		table.setRowEditorModel(DataManagementTableModel.REFRESH_FEATURE_COLUMN, action);
		table.setRowEditorModel(DataManagementTableModel.HIDE_FEATURE_COLUMN, action);
		table.setRowEditorModel(DataManagementTableModel.BACKGROUND_COLUMN, color);
		table.setRowEditorModel(DataManagementTableModel.FOREGROUND_COLUMN, color);
		table.setRowEditorModel(DataManagementTableModel.SEPARATE_COLUMN, bool);
		table.setRowEditorModel(DataManagementTableModel.LOAD_STRATEGY_COLUMN, choices);
		table.setRowEditorModel(DataManagementTableModel.FEATURE_NAME_COLUMN, text);
		table.setRowEditorModel(DataManagementTableModel.TRACK_NAME_COLUMN, text);
		table.setRowEditorModel(DataManagementTableModel.DELETE_FEATURE_COLUMN, action);

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

		for (int row = 0; row < featureSize; row++) {
			VirtualFeature vFeature = ftm.getFeature(row);
			JComboBox featureCB = new JComboBox(vFeature.getLoadChoices().toArray());
			featureCB.setRenderer(comboRenderer);
			featureCB.setEnabled(true);
			DefaultCellEditor featureEditor = new DefaultCellEditor(featureCB);
			choices.addEditorForRow(row, featureEditor);
			ButtonTableCellEditor buttonEditor = new ButtonTableCellEditor(vFeature);
			action.addEditorForRow(row, buttonEditor);
			JRPTextField trackNameFieldEditor = new JRPTextField("LoadModeTable_trackNameFieldEditor");
			DefaultCellEditor textEditor = new DefaultCellEditor(trackNameFieldEditor);
			text.addEditorForRow(row, textEditor);
			color.addEditorForRow(row, cellEditor);
		}


		TableColumn c = table.getColumnModel().getColumn(DataManagementTableModel.LOAD_STRATEGY_COLUMN);
		c.setCellRenderer(new ColumnRenderer());
		((JComponent) c.getCellRenderer()).setEnabled(enabled);

		c = table.getColumnModel().getColumn(DataManagementTableModel.DELETE_FEATURE_COLUMN);
		c.setCellRenderer(new LabelTableCellRenderer(delete_icon, true));

		c = table.getColumnModel().getColumn(DataManagementTableModel.REFRESH_FEATURE_COLUMN);
		c.setCellRenderer(new LabelTableCellRenderer(refresh_icon, true));

		c = table.getColumnModel().getColumn(DataManagementTableModel.HIDE_FEATURE_COLUMN);
		c.setCellRenderer(new LabelTableCellRenderer(visible_icon, true));
	}

	static final class ColumnRenderer extends JComponent implements TableCellRenderer {

		private static final long serialVersionUID = 1L;
		private final JComboBox comboBox;
		private final JRPTextField textField;	// If an entire genome is loaded in, change the combo box to a text field.

		public ColumnRenderer() {
			comboBox = new JComboBox();
			comboBox.setRenderer(comboRenderer);
			comboBox.setBorder(null);

			textField = new JRPTextField("LoadModeTable_textField", LoadStrategy.GENOME.toString());
			textField.setToolTipText(IGBConstants.BUNDLE.getString("genomeCBToolTip"));	// only for whole genome
			textField.setBorder(null);
		}

		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			if ((String) value != null) { // Fixes null pointer exception caused by clicking cell after load mode has been set to whole genome
				if (((String) value).equals(textField.getText())) {
					return textField;
				} else {
					comboBox.removeAllItems();
					comboBox.addItem(value);
					return comboBox;
				}
			} else {
				comboBox.removeAllItems();
				comboBox.addItem(value);
				return comboBox;
			}
		}
	}

	public static void updateVirtualFeatureList() {
		if (jTable != null) {
			GeneralLoadView.getLoadView().createFeaturesTable();
		}
	}

	public static JTableX getTable() {
		if (jTable != null) {
			return jTable;
		}
		return null;
	}

	public static DataManagementTableModel getModel() {
		if (jTable != null) {
			return (DataManagementTableModel) jTable.getModel();
		}
		return null;
	}
}

/**
 * A JTable with a RowEditorModel.
 */
class JTableX extends JTable implements MouseListener {

	private static final long serialVersionUID = 1L;
	protected String[] columnToolTips = {
		"Status update for track loading",
		"Show or hide tracks.",
		"Load data for track.",
		"Set annotation color when Color by Strand Preference is not checked.",
		"Set track background color.",
		"Show 2 Tracks (+/-)",
		"Load Strategy",
		"Name of active file or data set",
		"Set label text (display name) for Track Label.",
		"Remove data set or file."
	};
	private final Map<Integer, RowEditorModel> rmMap;
	private List<TierGlyph> currentTiers;
	private SeqMapView smv;

	public JTableX(TableModel tm) {
		super(tm);
		getTableHeader().addMouseListener(this);
		rmMap = new HashMap<Integer, RowEditorModel>();

		Application igb = Application.getSingleton();
		if (igb != null) {
			smv = igb.getMapView();
		}


	}

	void setRowEditorModel(int column, RowEditorModel rm) {
		this.rmMap.put(column, rm);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int col) {
		if (rmMap != null) { 

			TableCellEditor tmpEditor = rmMap.get(col).getEditor(row);
			if (tmpEditor != null) {
				return tmpEditor;
			}
		}
		return super.getCellEditor(row, col);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		DataManagementTableModel ftm = (DataManagementTableModel) getModel();
		VirtualFeature vFeature = ftm.getFeature(row);

		if (column == DataManagementTableModel.REFRESH_FEATURE_COLUMN) {
			if (!vFeature.isPrimary()) {
				return new LabelTableCellRenderer(null, false);
			}
			boolean enabled = (vFeature.getLoadStrategy() != LoadStrategy.NO_LOAD && vFeature.getLoadStrategy() != LoadStrategy.GENOME);
			return new LabelTableCellRenderer(DataManagementTable.refresh_icon, enabled);
		} else if (column == DataManagementTableModel.LOAD_STRATEGY_COLUMN) {
			if (!vFeature.isPrimary()) {
				return new LabelTableCellRenderer(null, false);
			}
			return new DataManagementTable.ColumnRenderer();
		} else if (column == DataManagementTableModel.INFO_FEATURE_COLUMN) {
			switch (vFeature.getLastRefreshStatus()) {
				case NO_DATA_LOADED: {
					return new LabelTableCellRenderer(DataManagementTable.info_icon, true);
				}
			}
		} else if (column == DataManagementTableModel.DELETE_FEATURE_COLUMN) {
			if (!vFeature.isPrimary()) {
				return new LabelTableCellRenderer(null, false);
			}
			return new LabelTableCellRenderer(DataManagementTable.delete_icon, true);
		} else if (column == DataManagementTableModel.HIDE_FEATURE_COLUMN) {
			currentTiers = smv.getSeqMap().getTiers(); //improve later
			for (TierGlyph tier : currentTiers) {
				if (vFeature.getStyle() != null
						&& tier.getAnnotStyle().getMethodName() != null) {
					if (tier.getAnnotStyle().getMethodName().equalsIgnoreCase(
							vFeature.getStyle().getMethodName()))//need changed
					{
						if (tier.getAnnotStyle().getShow()) {
							return new LabelTableCellRenderer(DataManagementTable.visible_icon, true);

						} else {
							return new LabelTableCellRenderer(DataManagementTable.invisible_icon, true);
						}
					}
				}
			}

		}
		return super.getCellRenderer(row, column);
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		String tip = null;
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		int realColumnIndex = convertColumnIndexToModel(colIndex);
		DataManagementTableModel ftm = (DataManagementTableModel) getModel();
		VirtualFeature feature = ftm.getFeature(rowIndex);
		String featureName = feature.getFeature().featureName;
		switch (realColumnIndex) {
			case DataManagementTableModel.REFRESH_FEATURE_COLUMN:
				if (feature.getLoadStrategy() != LoadStrategy.NO_LOAD) {
					tip = "Refresh " + featureName;
				} else {
					tip = "Change load strategy to refresh " + featureName;
				}
				break;
			case DataManagementTableModel.LOAD_STRATEGY_COLUMN:
				if (feature.getLoadStrategy() != LoadStrategy.GENOME) {
					tip = "Change load strategy for " + featureName;
				} else {
					tip = "Cannot change load strategy for " + featureName;
				}
				break;
			case DataManagementTableModel.FEATURE_NAME_COLUMN:
				tip = "File Name  (" + feature.getServer() + ")" + "\n " + feature.getFeature().featureName;
				break;
			case DataManagementTableModel.DELETE_FEATURE_COLUMN:
				tip = "Delete " + featureName;
				break;
			case DataManagementTableModel.HIDE_FEATURE_COLUMN:
				tip = "Switches track visibility On or OFF";
				break;
			case DataManagementTableModel.TRACK_NAME_COLUMN:
				tip = "Double click to edit track name";
				break;
			case DataManagementTableModel.BACKGROUND_COLUMN:
				tip = "Background";
				break;
			case DataManagementTableModel.FOREGROUND_COLUMN:
				tip = "Foreground";
				break;
			case DataManagementTableModel.SEPARATE_COLUMN:
				tip = "Show 2 Tracks (+/-)";
				break;
			default:
				tip = "";
		}
		return tip;
	}

	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				int realIndex = columnModel.getColumn(index).getModelIndex();
				return columnToolTips[realIndex];
			}
		};
	}

	public void mouseReleased(MouseEvent e) {
		//do nothing - functionality removed but not deleted for now.
//		java.awt.Point p = e.getPoint();
//		int index = columnModel.getColumnIndexAtX(p.x);
//
//		int realIndex = columnModel.getColumn(index).getModelIndex();
//		LoadModeDataTableModel ftm = (LoadModeDataTableModel) getModel();
//
//		switch (realIndex) {
//			case LoadModeDataTableModel.REFRESH_FEATURE_COLUMN:
//				if (GeneralLoadView.getIsDisableNecessary()) {
//					GeneralLoadView.getLoadView().getRefreshDataAction().actionPerformed(null);
//				}
//				break;
//			case LoadModeDataTableModel.DELETE_FEATURE_COLUMN:
//				int featureSize = ftm.getRowCount();
//				String message = "Really remove all data sets ?";
//				if (featureSize > 0 && Application.confirmPanel(message, PreferenceUtils.getTopNode(),
//						PreferenceUtils.CONFIRM_BEFORE_DELETE, PreferenceUtils.default_confirm_before_delete)) {
//					List<TierGlyph> tierList = smv.getSeqMap().getTiers();
//					for (GenericFeature gFeature : ftm.features) {
//						GeneralLoadView.getLoadView().removeFeature(gFeature, true);
//					}
//					ftm.virtualFeatures.clear();
//				}
//				break;
//			case LoadModeDataTableModel.HIDE_FEATURE_COLUMN:
//				currentTiers = smv.getSeqMap().getTiers();
//				TableColumn c = this.getColumnModel().getColumn(LoadModeDataTableModel.HIDE_FEATURE_COLUMN);//may be wrong
//				if (LoadModeTable.iconTest) {
//					c.setCellRenderer(new LabelTableCellRenderer(LoadModeTable.invisible_icon, true));
//					c.setHeaderRenderer(new LabelTableCellRenderer(LoadModeTable.invisible_icon, true));
//					LoadModeTable.iconTest = false;
//
//					for (TierGlyph tier : currentTiers) {
//						if (tier.getAnnotStyle().getTrackName() != null) {// need change
//							if (!tier.getAnnotStyle().getTrackName().equalsIgnoreCase(
//									TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
//								if (tier.isVisible()) {
//									smv.getPopup().hideOneTier(tier);
//								}
//							}
//						}
//					}
//
//					refreshSeqMapView();
//				} else {
//					c.setCellRenderer(new LabelTableCellRenderer(LoadModeTable.visible_icon, true));
//					c.setHeaderRenderer(new LabelTableCellRenderer(LoadModeTable.visible_icon, true));
//					LoadModeTable.iconTest = true;
//					smv.getPopup().showAllTiers();
//				}
//
//				this.getTableHeader().repaint();
//				ftm.fireTableDataChanged();
//				break;
//			default:
//			//System.out.println("Unknown header selected: " + realIndex);
//		}
	}

//	private void refreshSeqMapView() {
//		if (smv != null) {
//			smv.setAnnotatedSeq(smv.getAnnotatedSeq(), true, true, false);
//		}
//	}
	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}

/**
 * This maps a row to a specific editor.
 */
class RowEditorModel {

	private final Map<Integer, TableCellEditor> row2Editor;

	RowEditorModel(int size) {
		row2Editor = new HashMap<Integer, TableCellEditor>(size);
	}

	void addEditorForRow(int row, TableCellEditor e) {
		row2Editor.put(Integer.valueOf(row), e);
	}

	TableCellEditor getEditor(int row) {
		return row2Editor.get(Integer.valueOf(row));
	}
}
