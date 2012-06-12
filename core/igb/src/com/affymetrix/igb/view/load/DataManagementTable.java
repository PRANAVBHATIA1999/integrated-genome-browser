package com.affymetrix.igb.view.load;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;
import com.jidesoft.combobox.ColorExComboBox;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.symloader.Delegate;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;

import com.affymetrix.genoviz.swing.*;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextFieldTableCellRenderer;

import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.JRPStyledTable;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor.TrackStylePropertyListener;
import com.affymetrix.igb.util.JComboBoxToolTipRenderer;
import com.affymetrix.igb.view.SeqMapView;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 * A table with two customizations: 1. An always-visible combo box. For a user,
 * this differentiates the field from a text box, and thus indicates they have a
 * choice. 2. Different combo box elements per row. This allows different
 * behavior per server type.
 */
public final class DataManagementTable {

	private static final JComboBoxToolTipRenderer comboRenderer = new JComboBoxToolTipRenderer();
	static final Icon refresh_icon = CommonUtils.getInstance().getIcon("images/refresh16.png");
	static final Icon delete_icon = CommonUtils.getInstance().getIcon("images/delete.gif");
	static final Icon invisible_icon = CommonUtils.getInstance().getIcon("images/invisible.gif");
	static final Icon visible_icon = CommonUtils.getInstance().getIcon("images/visible.gif");
	static final Icon error_icon = CommonUtils.getInstance().getIcon("images/stop.png");
	static final Icon igb_icon = CommonUtils.getInstance().getIcon("images/warning.png");
	
	//public static boolean iconTest;

	/**
	 * Set the columns to use the ComboBox DAScb and renderer (which also
	 * depends on the row/server type)
	 *
	 * @param table
	 * @param column
	 * @param enabled
	 */
	static void setComboBoxEditors(JTableX table, boolean enabled) {
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
		table.setRowEditorModel(DataManagementTableModel.REFRESH_FEATURE_COLUMN, action);
		table.setRowEditorModel(DataManagementTableModel.HIDE_FEATURE_COLUMN, action);
		table.setRowEditorModel(DataManagementTableModel.BACKGROUND_COLUMN, color);
		table.setRowEditorModel(DataManagementTableModel.FOREGROUND_COLUMN, color);
		table.setRowEditorModel(DataManagementTableModel.SEPARATE_COLUMN, bool);
		table.setRowEditorModel(DataManagementTableModel.LOAD_STRATEGY_COLUMN, choices);
		table.setRowEditorModel(DataManagementTableModel.FEATURE_NAME_COLUMN, text);
		table.setRowEditorModel(DataManagementTableModel.TRACK_NAME_COLUMN, text);
		table.setRowEditorModel(DataManagementTableModel.DELETE_FEATURE_COLUMN, action);

		table.setDefaultRenderer(Color.class, new ColorTableCellRenderer());
		table.setDefaultEditor(Color.class, new DefaultCellEditor(new ColorExComboBox()));
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
			JRPTextFieldTableCellRenderer trackNameFieldEditor;
			if (vFeature.getStyle() != null) {
				trackNameFieldEditor = new JRPTextFieldTableCellRenderer("LoadModeTable_trackNameFieldEditor" + row,
						vFeature.getStyle().getTrackName());
			} else {
				Logger.getLogger(DataManagementTable.class.getName()).log(Level.WARNING, "Found a feature with null style", vFeature.getFeature().featureName);
				trackNameFieldEditor = new JRPTextFieldTableCellRenderer("LoadModeTable_trackNameFieldEditor" + row,
						vFeature.getFeature().featureName);
			}
			text.addEditorForRow(row, trackNameFieldEditor); 
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
		private final JRPTextField gtextField;	// If an entire genome is loaded in, change the combo box to a text field.
		private final JRPTextField dtextField;	// If only do not load is available, change the combo box to a text field.

		public ColumnRenderer() {

			gtextField = new JRPTextField("LoadModeTable_textField", LoadStrategy.GENOME.toString());
			gtextField.setToolTipText(IGBConstants.BUNDLE.getString("genomeCBToolTip"));	// only for whole genome
			gtextField.setBorder(null);
			gtextField.setHorizontalAlignment(JRPTextField.CENTER);

			dtextField = new JRPTextField("LoadModeTable_textField", LoadStrategy.NO_LOAD.toString());
			//dtextField.setToolTipText(IGBConstants.BUNDLE.getString("genomeCBToolTip"));	
			dtextField.setBorder(null);
			dtextField.setHorizontalAlignment(JRPTextField.CENTER);
		}

		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			DataManagementTableModel ftm = (DataManagementTableModel) table.getModel();
			VirtualFeature vFeature = ftm.getFeature(row);
			if ((String) value != null) { // Fixes null pointer exception caused by clicking cell after load mode has been set to whole genome
				if (((String) value).equals(gtextField.getText())) {
					return gtextField;
				} else if (vFeature.getLoadChoices().size() == 1 && ((String) value).equals(dtextField.getText())) {
					return dtextField;
				} else {
					ComboBoxRenderer renderer = new ComboBoxRenderer(vFeature.getLoadChoices().toArray());
					Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					renderer.combobox.setSelectedItem(vFeature.getLoadStrategy());
					return c;
				}
			} else {
				ComboBoxRenderer renderer = new ComboBoxRenderer(vFeature.getLoadChoices().toArray());
				Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				renderer.combobox.setSelectedItem(vFeature.getLoadStrategy());
				return c;
			}
		}
	}
}

/**
 * A JTable with a RowEditorModel.
 */
class JTableX extends JRPStyledTable implements TrackStylePropertyListener {

	private static final long serialVersionUID = 1L;
	protected String[] columnToolTips = {
		"Load data for tracks",
		"Show or hide tracks",
		"Set foreground color for tracks",
		"Set background color for tracks",
		"Show 2 Tracks (+) and (-) or one track (+/-)",
		"Set track name",
		"Load Strategy",
		"Name of active data set or file",
		"Remove data set or file."
	};
	private final Map<Integer, RowEditorModel> rmMap;
	private List<TierGlyph> currentTiers;
	private SeqMapView smv;

	public JTableX(String id, TableModel tm) {
		super(id, tm);

		super.list.add(DataManagementTableModel.BACKGROUND_COLUMN);
		super.list.add(DataManagementTableModel.FOREGROUND_COLUMN);

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
		//Special Case
		if (col == DataManagementTableModel.FEATURE_NAME_COLUMN) {
			if (isCellEditable(row, col)) {
				DataManagementTableModel ftm = (DataManagementTableModel) getModel();
				VirtualFeature vFeature = ftm.getFeature(row);
				return new ErrorNotificationCellRenderer(vFeature.getFeature().featureName,
						vFeature.getFeature().getLastRefreshStatus().toString(), DataManagementTable.error_icon);
			}
		}

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
		} else if (column == DataManagementTableModel.FEATURE_NAME_COLUMN) {
			switch (vFeature.getLastRefreshStatus()) {
				case NO_DATA_LOADED: {
					return new ErrorNotificationCellRenderer(vFeature.getFeature().featureName, 
						vFeature.getLastRefreshStatus().toString(), DataManagementTable.error_icon);
				}
			}
			TableCellRenderer renderer = super.getCellRenderer(row, column);
			((DefaultTableCellRenderer) renderer).setHorizontalAlignment(SwingConstants.RIGHT);
			return renderer;
		} else if (column == DataManagementTableModel.TRACK_NAME_COLUMN) {
			if (vFeature.getStyle() != null) {
				if(Delegate.EXT.equalsIgnoreCase(vFeature.getStyle().getFileType())){
					return new ErrorNotificationCellRenderer(vFeature.getFeature().featureName, 
						BUNDLE.getString("igb_track"), DataManagementTable.igb_icon);
				}
				return new JRPTextFieldTableCellRenderer(vFeature.getFeature().featureName, vFeature.getStyle().getTrackName());
			} else {
				return new JRPTextFieldTableCellRenderer(vFeature.getFeature().featureName, vFeature.getFeature().featureName);
			}

		} else if (column == DataManagementTableModel.DELETE_FEATURE_COLUMN) {
			if (!vFeature.isPrimary()) {
				return new LabelTableCellRenderer(null, false);
			}
			return new LabelTableCellRenderer(DataManagementTable.delete_icon, true);
		} else if (column == DataManagementTableModel.HIDE_FEATURE_COLUMN) {
			currentTiers = smv.getSeqMap().getTiers();
			for (TierGlyph tier : currentTiers) {
				if (vFeature.getStyle() != null && tier.getAnnotStyle().getMethodName() != null
						&& tier.getAnnotStyle().getMethodName().equalsIgnoreCase(
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
				tip = "Click to edit track name";
				break;
			case DataManagementTableModel.BACKGROUND_COLUMN:
				tip = "Background";
				break;
			case DataManagementTableModel.FOREGROUND_COLUMN:
				tip = "Foreground";
				break;
			case DataManagementTableModel.SEPARATE_COLUMN:
				tip = "Show 2 Tracks (+) and (-) or one track (+/-)";
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

	public void trackstylePropertyChanged(EventObject eo) {
		if (eo.getSource() == this.getModel()) {
			return;
		}

		repaint();
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