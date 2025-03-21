package com.affymetrix.igb.view.load;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import com.affymetrix.genoviz.swing.BooleanTableCellRenderer;
import com.affymetrix.genoviz.swing.ButtonTableCellEditor;
import com.affymetrix.genoviz.swing.ColorTableCellRenderer;
import com.affymetrix.genoviz.swing.ComboBoxRenderer;
import com.affymetrix.genoviz.swing.LabelTableCellRenderer;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.swing.JRPTextField;
import com.affymetrix.igb.swing.JRPTextFieldTableCellRenderer;
import com.affymetrix.igb.swing.jide.JRPStyledTable;
import com.affymetrix.igb.tiers.TrackStylePropertyListener;
import com.affymetrix.igb.util.JComboBoxToolTipRenderer;
import com.affymetrix.igb.view.SeqMapView;
import com.jidesoft.combobox.ColorExComboBox;
import org.lorainelab.igb.genoviz.extensions.glyph.TierGlyph;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * A table with two customizations: 1. An always-visible combo box. For a user,
 * this differentiates the field from a text box, and thus indicates they have a
 * choice. 2. Different combo box elements per row. This allows different
 * behavior per server type.
 */
public final class DataManagementTable {

    private static final JComboBoxToolTipRenderer comboRenderer = new JComboBoxToolTipRenderer();
    static final Icon refresh_icon = CommonUtils.getInstance().getIcon("16x16/actions/refresh.png");
    static final Icon delete_icon = CommonUtils.getInstance().getIcon("16x16/actions/delete.gif");
    static final Icon invisible_icon = CommonUtils.getInstance().getIcon("16x16/actions/hide.png");
    static final Icon visible_icon = CommonUtils.getInstance().getIcon("16x16/actions/show.png");
    static final Icon error_icon = CommonUtils.getInstance().getIcon("16x16/actions/stop.png");
    static final Icon igb_icon = CommonUtils.getInstance().getIcon("16x16/actions/warning.png");

    //public static boolean iconTest;
    /**
     * Set the columns to use the ComboBox DAScb and renderer. (which also
     * depends on the row/server type)
     */
    static void setComboBoxEditors(JTableX table, boolean enabled) {
        comboRenderer.setToolTipEntry(LoadStrategy.NO_LOAD.toString(), IGBConstants.BUNDLE.getString("noLoadCBToolTip"));
        comboRenderer.setToolTipEntry(LoadStrategy.AUTOLOAD.toString(), IGBConstants.BUNDLE.getString("autoLoadCBToolTip"));
        comboRenderer.setToolTipEntry(LoadStrategy.VISIBLE.toString(), IGBConstants.BUNDLE.getString("visibleCBToolTip"));
//		comboRenderer.setToolTipEntry(LoadStrategy.CHROMOSOME.toString(), IGBConstants.BUNDLE.getString("chromosomeCBToolTip"));
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
        table.setRowEditorModel(DataManagementTableModel.TRACK_NAME_COLUMN, text);
        table.setRowEditorModel(DataManagementTableModel.DELETE_FEATURE_COLUMN, action);

        table.setDefaultRenderer(Color.class, new ColorTableCellRenderer());
        table.setDefaultEditor(Color.class, new DefaultCellEditor(new ColorExComboBox()));
        table.setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());

        for (int row = 0; row < featureSize; row++) {
            DataSet feature = ftm.getRowFeature(row);
            // Get style by row
            ITrackStyleExtended style = ftm.getStyleFromRow(row);
            if (feature != null) {
                //some actions require a DataSet
                JComboBox featureCB = new JComboBox(feature.getLoadChoices().toArray());
                featureCB.setRenderer(comboRenderer);
                featureCB.setEnabled(true);
                featureCB.setSelectedItem(feature.getLoadStrategy());
                DefaultCellEditor featureEditor = new DefaultCellEditor(featureCB);
                choices.addEditorForRow(row, featureEditor);
                ButtonTableCellEditor buttonEditor = new ButtonTableCellEditor(feature);
                action.addEditorForRow(row, buttonEditor);
            } else {
                // Even without a DataSet, buttons should act a like buttons.
                ButtonTableCellEditor buttonEditor = new ButtonTableCellEditor(style);
                action.addEditorForRow(row, buttonEditor);
            }
            JRPTextFieldTableCellRenderer trackNameFieldEditor;
            if (style != null) {
                trackNameFieldEditor = new JRPTextFieldTableCellRenderer("LoadModeTable_trackNameFieldEditor" + row,
                        style.getTrackName(), style.getForeground(), style.getBackground());
            } else {
                trackNameFieldEditor = new JRPTextFieldTableCellRenderer("LoadModeTable_trackNameFieldEditor" + row, feature.getDataSetName(), Color.WHITE, Color.BLACK);
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
            DataSet feature = ftm.getRowFeature(row);
            if (feature == null) { //allow for the possibility that feature is null <Ivory Blakley> IGBF-201
                return null;
            }
            if (value != null) { // Fixes null pointer exception caused by clicking cell after load mode has been set to whole genome
                if (value.equals(gtextField.getText())) {
                    return gtextField;
                } else if (feature.getLoadChoices().size() == 1 && value.equals(dtextField.getText())) {
                    return dtextField;
                } else {
                    ComboBoxRenderer renderer = new ComboBoxRenderer(feature.getLoadChoices().toArray());
                    Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    renderer.combobox.setSelectedItem(feature.getLoadStrategy());
                    return c;
                }
            } else {
                ComboBoxRenderer renderer = new ComboBoxRenderer(feature.getLoadChoices().toArray());
                Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                renderer.combobox.setSelectedItem(feature.getLoadStrategy());
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
        "Load Strategy",
        "Set track name",
        "Remove data set or file."
    };
    private final Map<Integer, RowEditorModel> rmMap;
    private List<TierGlyph> currentTiers;
    private SeqMapView smv;

    public JTableX(String id, TableModel tm) {
        super(id, tm);

        super.list.add(DataManagementTableModel.BACKGROUND_COLUMN);
        super.list.add(DataManagementTableModel.FOREGROUND_COLUMN);
        super.list.add(DataManagementTableModel.TRACK_NAME_COLUMN);

        rmMap = new HashMap<>();

        IGB igb = IGB.getInstance();
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
        // Allow for the possibility that feature is null <Ivory Blakley> IGBF-201
        // Some buttons should be shown as inactive if there is no feature.
        DataManagementTableModel ftm = (DataManagementTableModel) getModel();
        DataSet feature = ftm.getRowFeature(row);
        ITrackStyleExtended style = ftm.getStyleFromRow(row);

        if (column == DataManagementTableModel.REFRESH_FEATURE_COLUMN) {
//			if (!feature.isPrimary()) {
//				return new LabelTableCellRenderer(null, false);
//			}
            if (feature == null) {
                //for joined graphs, the refresh button is inactive
                return new LabelTableCellRenderer(DataManagementTable.refresh_icon, false);
            }
            boolean enabled = (feature.getLoadStrategy() != LoadStrategy.NO_LOAD
                    && feature.getLoadStrategy() != LoadStrategy.GENOME
                    && smv.getAnnotatedSeq() != null
                    && !IGBConstants.GENOME_SEQ_ID.equals(smv.getAnnotatedSeq().getId()));
            return new LabelTableCellRenderer(DataManagementTable.refresh_icon, enabled);
        } else if (column == DataManagementTableModel.LOAD_STRATEGY_COLUMN) {
//			if (!feature.isPrimary()) {
//				return new LabelTableCellRenderer(null, false);
//			}
            return new DataManagementTable.ColumnRenderer();//This function give acceptable results for the null feature cases.
        } else if (column == DataManagementTableModel.TRACK_NAME_COLUMN) {
            if (style != null) { //get info from style only
                return new JRPTextFieldTableCellRenderer(style.getUrl(), style.getTrackName(), style.getForeground(), style.getBackground());
            } else { //get info from feature only
                return new JRPTextFieldTableCellRenderer(feature.getDataSetName(), feature.getDataSetName(), Color.BLACK, Color.WHITE);
            }

        } else if (column == DataManagementTableModel.DELETE_FEATURE_COLUMN) {
//			if (!feature.isPrimary()) {
//				return new LabelTableCellRenderer(null, false);
//			}
            if (feature == null) {
                return new LabelTableCellRenderer(DataManagementTable.delete_icon, false);
            }
            return new LabelTableCellRenderer(DataManagementTable.delete_icon, true);
        } else if (column == DataManagementTableModel.HIDE_FEATURE_COLUMN) {
            // rather than loop through all of the tiers to find the one that matches the current style, just to extract the style,
            // just use the current style
            if (style.getShow()) {
                return new LabelTableCellRenderer(DataManagementTable.visible_icon, true);
            } else {
                return new LabelTableCellRenderer(DataManagementTable.invisible_icon, true);
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
        DataSet feature = ftm.getRowFeature(rowIndex);
        String featureName = feature.getDataSetName();
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
        row2Editor = new HashMap<>(size);
    }

    void addEditorForRow(int row, TableCellEditor e) {
        row2Editor.put(row, e);
    }

    TableCellEditor getEditor(int row) {
        return row2Editor.get(row);
    }
}
