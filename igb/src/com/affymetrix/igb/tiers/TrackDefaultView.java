/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.tiers;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.stylesheet.AssociationElement;
import com.affymetrix.igb.stylesheet.XmlStylesheetParser;
import java.awt.Color;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import com.affymetrix.genoviz.swing.BooleanTableCellRenderer;
import com.affymetrix.genoviz.swing.ColorTableCellRenderer;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPCheckBox;
import com.affymetrix.igb.stylesheet.PropertyConstants;
import com.affymetrix.igb.stylesheet.PropertyMap;
import com.affymetrix.igb.tiers.TrackConstants.DIRECTION_TYPE;
import com.jidesoft.combobox.ColorComboBox;
import com.jidesoft.grid.ColorCellEditor;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dcnorris
 */
public final class TrackDefaultView implements ListSelectionListener {

	private static final String FILE_TYPE = "File Type";
	private static final String FOREGROUND = "Foreground";
	private static final String BACKGROUND = "Background";
	private static final String TRACK_NAME_SIZE = "Track Name Size";
	private final static String[] col_headings = {
		FILE_TYPE,
		BACKGROUND, FOREGROUND,
		TRACK_NAME_SIZE,};
	private static final int COL_TRACK_DEFAULT = 0;
	private static final int COL_BACKGROUND = 1;
	private static final int COL_FOREGROUND = 2;
	private static final int COL_TRACK_NAME_SIZE = 3;
	private static final int COL_COLLAPSED = 4;
	private static final int COL_MAX_DEPTH = 5;
	private static final int Col_Show_2_Tracks = 6;
	private static final int COL_LABEL_FIELD = 7;
	private static final int COL_CONNECTED = 8;
	private static final int COL_DIRECTION_TYPE = 9;
	private static final int COL_POS_STRAND_COLOR = 10;
	private static final int COL_NEG_STRAND_COLOR = 11;
	private static TrackStyle default_annot_style = TrackStyle.getDefaultInstance();
	private final TrackDefaultPrefTableModel model;
	private ListSelectionModel lsm;
	private boolean initializationDetector; //Test to detect action events triggered by clicking a row in the table.
	private boolean settingValueFromTable;  //Test to prevent action events triggered by the setValueAt method from calling the method again.  This improves efficiency.
	private float trackNameSize;
	private int[] selectedRows;
	private Object[] temp;
	private String[] trackDefaults;
	private String[] graphFormats = {"bar", "bgr", "egr", "egr.txt", "sin", "gr", "sgr", "useq", "wig"};
	private ArrayList<String> list = new ArrayList<String>();
	private String allowedTrackDefaults;
	private String AddButtonTitle = "Add Track Default";
	private String selectedTrackDefaultType;
	private TrackStyle selectedStyle;
	private JRPButton addTrackDefaultButton;
	private JRPCheckBox arrowCheckBox;
	private com.jidesoft.combobox.ColorComboBox bgColorComboBox;
	private JRPCheckBox  collapsedCheckBox;
	private JRPCheckBox  colorCheckBox;
	private JRPCheckBox  connectedCheckBox;
	private com.jidesoft.combobox.ColorComboBox fgColorComboBox;
	private javax.swing.JComboBox labelFieldComboBox;
	private javax.swing.JTextField maxDepthTextField;
	private com.jidesoft.combobox.ColorComboBox negativeColorComboBox;
	private com.jidesoft.combobox.ColorComboBox possitiveColorComboBox;
	private JRPButton removeTrackDefaultButton;
	private JRPCheckBox  show2TracksCheckBox;
	private javax.swing.JTable table;
	private javax.swing.JTextField trackDefaultTextField;
	private javax.swing.JComboBox trackNameSizeComboBox;
	private javax.swing.JLabel labelFieldTip;
	private static TrackDefaultView singleton;
	
	public static void init() {
		singleton = new TrackDefaultView();
	}

	public static synchronized TrackDefaultView getTrackDefaultView() {
		return singleton;
	}

	/** Creates new form FileTypeViewNew */
	public TrackDefaultView() {
		model = new TrackDefaultPrefTableModel();
		model.setElements(XmlStylesheetParser.getUserFileTypeAssociation());
		initializeFileTypes();
		initComponents();
		table.setRowSelectionInterval(0, 0);
	}

	private void initializeFileTypes() {
		list.clear();
		temp = FileTypeHolder.getInstance().getNameToExtensionMap().values().toArray();
		for (int i = 0; i < temp.length; i++) {
			StringTokenizer tokens = new StringTokenizer(temp[i].toString(), ",");
			while (tokens.hasMoreElements()) {
				allowedTrackDefaults = tokens.nextToken();
				allowedTrackDefaults = allowedTrackDefaults.replace("[", "");
				allowedTrackDefaults = allowedTrackDefaults.replace("]", "");
				allowedTrackDefaults = allowedTrackDefaults.trim();
				list.add(allowedTrackDefaults);
			}
		}
		Collections.sort(list);
		trackDefaults = list.toArray(new String[list.size()]);
	}

	private void initComponents() {
		trackDefaultTextField = new javax.swing.JTextField();
		possitiveColorComboBox = new com.jidesoft.combobox.ColorComboBox();
		negativeColorComboBox = new com.jidesoft.combobox.ColorComboBox();
		colorCheckBox = new JRPCheckBox("TrackDefaultView_colorCheckBox");
		arrowCheckBox = new JRPCheckBox("TrackDefaultView_arrowCheckBox");
		table = new javax.swing.JTable();
		addTrackDefaultButton = new JRPButton("TrackDefaultView_addTrackDefaultButton");
		removeTrackDefaultButton = new JRPButton("TrackDefaultView_removeTrackDefaultButton");
		bgColorComboBox = new com.jidesoft.combobox.ColorComboBox();
		trackNameSizeComboBox = new javax.swing.JComboBox();
		fgColorComboBox = new com.jidesoft.combobox.ColorComboBox();
		labelFieldComboBox = new javax.swing.JComboBox();
		maxDepthTextField = new javax.swing.JTextField();
		show2TracksCheckBox = new JRPCheckBox("TrackDefaultView_show2TracksCheckBox");
		connectedCheckBox = new JRPCheckBox("TrackDefaultView_connectedCheckBox");
		collapsedCheckBox = new JRPCheckBox("TrackDefaultView_collapsedCheckBox");
		labelFieldTip = new javax.swing.JLabel();

		possitiveColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
		possitiveColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
		possitiveColorComboBox.setButtonVisible(false);
		possitiveColorComboBox.setColorValueVisible(false);
		possitiveColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
		possitiveColorComboBox.setStretchToFit(true);

		negativeColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
		negativeColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
		negativeColorComboBox.setButtonVisible(false);
		negativeColorComboBox.setColorValueVisible(false);
		negativeColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
		negativeColorComboBox.setStretchToFit(true);

		colorCheckBox.setText("Color");

		arrowCheckBox.setText("Arrow");

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
		table.getColumnModel().getColumn(COL_FOREGROUND).setPreferredWidth(72);
		table.getColumnModel().getColumn(COL_FOREGROUND).setMinWidth(72);
		table.getColumnModel().getColumn(COL_FOREGROUND).setMaxWidth(72);
		table.getColumnModel().getColumn(COL_BACKGROUND).setPreferredWidth(72);
		table.getColumnModel().getColumn(COL_BACKGROUND).setMinWidth(72);
		table.getColumnModel().getColumn(COL_BACKGROUND).setMaxWidth(72);
		table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setPreferredWidth(95);
		table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setMinWidth(95);
		table.getColumnModel().getColumn(COL_TRACK_NAME_SIZE).setMaxWidth(95);

		Font f = new Font("SansSerif", Font.BOLD, 12);
		table.getTableHeader().setFont(f);

		addTrackDefaultButton.setText("Add");


		removeTrackDefaultButton.setText("Remove");


		bgColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
		bgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
		bgColorComboBox.setButtonVisible(false);
		bgColorComboBox.setColorValueVisible(false);
		bgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
		bgColorComboBox.setStretchToFit(true);


		trackNameSizeComboBox.setEditable(true);
		trackNameSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.SUPPORTED_SIZE));


		fgColorComboBox.setBackground(new java.awt.Color(255, 255, 255));
		fgColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
		fgColorComboBox.setButtonVisible(false);
		fgColorComboBox.setColorValueVisible(false);
		fgColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
		fgColorComboBox.setStretchToFit(true);


		labelFieldComboBox.setEditable(true);
		labelFieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(TrackConstants.LABELFIELD));



		show2TracksCheckBox.setText("Show (+/-) tracks");

		connectedCheckBox.setText("Connected");

		collapsedCheckBox.setText("Collapsed");

		labelFieldTip.setToolTipText("Type or choose label field.");
		labelFieldTip.setIcon(CommonUtils.getInstance().getIcon("images/info_icon.gif"));
		labelFieldTip.setText(" ");
	}

	public void possitiveColorComboBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(possitiveColorComboBox.getSelectedColor(), selectedRows[i], COL_POS_STRAND_COLOR);
			}
		}
	}

	public void negativeColorComboBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(negativeColorComboBox.getSelectedColor(), selectedRows[i], COL_NEG_STRAND_COLOR);
			}
		}
	}

	public void trackDefaultTextFieldActionPerformed() {
		if (!settingValueFromTable) {
			model.setValueAt(trackDefaultTextField.getText(), selectedRows[0], COL_TRACK_DEFAULT);
		}
	}

	public void bgColorComboBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(bgColorComboBox.getSelectedColor(), selectedRows[i], COL_BACKGROUND);
			}
		}
	}

	public void trackNameSizeComboBoxActionPerformed() {
		if (!settingValueFromTable && !initializationDetector) {   // !initializationDetector condition is for the initialization when multiple rows are selected to prevent null exception
			trackNameSize = Float.parseFloat(trackNameSizeComboBox.getSelectedItem().toString());
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(trackNameSize, selectedRows[i], COL_TRACK_NAME_SIZE);
			}
		}
	}

	public void fgColorComboBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(fgColorComboBox.getSelectedColor(), selectedRows[i], COL_FOREGROUND);
			}
		}
	}

	public void labelFieldComboBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(labelFieldComboBox.getSelectedItem(), selectedRows[i], COL_LABEL_FIELD);
			}
		}
	}

	public void maxDepthTextFieldActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(maxDepthTextField.getText(), selectedRows[i], COL_MAX_DEPTH);
			}
		}
	}

	public void show2TracksCheckBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(show2TracksCheckBox.isSelected(), selectedRows[i], Col_Show_2_Tracks);
			}
		}
	}

	public void connectedCheckBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(connectedCheckBox.isSelected(), selectedRows[i], COL_CONNECTED);
			}
		}
	}

	public void collapsedCheckBoxActionPerformed() {
		if (!settingValueFromTable) {
			for (int i = 0; i < selectedRows.length; i++) {
				model.setValueAt(collapsedCheckBox.isSelected(), selectedRows[i], COL_COLLAPSED);
			}
		}
	}

	public void addTrackDefaultButtonActionPerformed() {
		initializeFileTypes();
		selectedTrackDefaultType = (String) JOptionPane.showInputDialog(null, "Enter value:", AddButtonTitle,
				JOptionPane.PLAIN_MESSAGE, null, trackDefaults, trackDefaults[0]);

		if (selectedTrackDefaultType == null) {
			return;
		}

		if (XmlStylesheetParser.getUserFileTypeAssociation().get(selectedTrackDefaultType) != null) {
			ErrorHandler.errorPanel("Duplicate Entry", "File type " + selectedTrackDefaultType + " exists");
			return;
		}

		AssociationElement element = AssociationElement.getFileTypeAssocation(selectedTrackDefaultType);
		AssociationElement sysElement = XmlStylesheetParser.getSystemFileTypeAssociation().get(selectedTrackDefaultType);
		if (sysElement != null) {
			PropertyMap props = sysElement.getPropertyMap();
			element.getPropertyMap().putAll(props);
		}

		XmlStylesheetParser.getUserFileTypeAssociation().put(selectedTrackDefaultType, element);
		model.addElement(selectedTrackDefaultType, element);
		model.fireTableDataChanged();
	}

	public void arrowCheckBoxActionPerformed() {
		if (!settingValueFromTable) {
			if (colorCheckBox.isSelected()) {
				if (arrowCheckBox.isSelected()) {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.BOTH, selectedRows[i], COL_DIRECTION_TYPE);
					}
				} else {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.COLOR, selectedRows[i], COL_DIRECTION_TYPE);
					}
				}
			} else {
				if (arrowCheckBox.isSelected()) {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.ARROW, selectedRows[i], COL_DIRECTION_TYPE);
					}
				} else {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.NONE, selectedRows[i], COL_DIRECTION_TYPE);
					}
				}
			}
		}
	}

	public void colorCheckBoxActionPerformed() {
		if (!settingValueFromTable) {
			if (colorCheckBox.isSelected()) {
				if (arrowCheckBox.isSelected()) {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.BOTH, selectedRows[i], COL_DIRECTION_TYPE);
					}
				} else {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.COLOR, selectedRows[i], COL_DIRECTION_TYPE);
					}
				}
			} else {
				if (arrowCheckBox.isSelected()) {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.ARROW, selectedRows[i], COL_DIRECTION_TYPE);
					}
				} else {
					for (int i = 0; i < selectedRows.length; i++) {
						model.setValueAt(TrackConstants.DIRECTION_TYPE.NONE, selectedRows[i], COL_DIRECTION_TYPE);
					}
				}
			}
		}
	}

	public void removeTrackDefaultButtonActionPerformed() {
		if (table.getSelectedRow() != -1) {
			selectedStyle = model.tier_styles.get(table.getSelectedRow());
			XmlStylesheetParser.getUserFileTypeAssociation().remove(selectedStyle.getTrackName());
			model.removeElement(selectedStyle.getTrackName());
			model.fireTableDataChanged();
		}
	}

	/** Called when the user selects a row of the table.
	 * @param evt
	 */
	public void valueChanged(ListSelectionEvent evt) {
		labelFieldComboBox.setEnabled(true);
		maxDepthTextField.setEnabled(true);
		show2TracksCheckBox.setEnabled(true);
		connectedCheckBox.setEnabled(true);
		collapsedCheckBox.setEnabled(true);
		selectedRows = table.getSelectedRows();
		removeTrackDefaultButton.setEnabled(true);
		initializationDetector = true;
		trackDefaultTextField.setEnabled(false);
		possitiveColorComboBox.setEnabled(true);
		negativeColorComboBox.setEnabled(true);
		arrowCheckBox.setEnabled(true);
		colorCheckBox.setEnabled(true);

		if (selectedRows.length > 1) {
			bgColorComboBox.setSelectedColor(null);
			fgColorComboBox.setSelectedColor(null);
			trackNameSizeComboBox.setSelectedItem("");
			labelFieldComboBox.setEnabled(false);
			maxDepthTextField.setEnabled(false);
			show2TracksCheckBox.setEnabled(false);
			connectedCheckBox.setEnabled(false);
			collapsedCheckBox.setEnabled(false);
			colorCheckBox.setEnabled(false);
			arrowCheckBox.setEnabled(false);
			possitiveColorComboBox.setEnabled(false);
			negativeColorComboBox.setEnabled(false);
		} else if (selectedRows.length == 1) {
			selectedStyle = model.getStyles().get(selectedRows[0]);

			if (selectedStyle.getTrackName().equalsIgnoreCase(TrackConstants.NAME_OF_DEFAULT_INSTANCE)) {
				removeTrackDefaultButton.setEnabled(false);
			}

			trackDefaultTextField.setText(selectedStyle.getTrackName());
			bgColorComboBox.setSelectedColor(selectedStyle.getBackground());
			fgColorComboBox.setSelectedColor(selectedStyle.getForeground());

			if (Arrays.asList(graphFormats).contains(selectedStyle.getTrackName())) {
				labelFieldComboBox.setEnabled(false);
				maxDepthTextField.setEnabled(false);
				show2TracksCheckBox.setEnabled(false);
				connectedCheckBox.setEnabled(false);
				collapsedCheckBox.setEnabled(false);
				colorCheckBox.setEnabled(false);
				arrowCheckBox.setEnabled(false);
				possitiveColorComboBox.setEnabled(false);
				negativeColorComboBox.setEnabled(false);
			} else {
				int connected = selectedStyle.getGlyphDepth();
				boolean isConnected = true;
				if (connected == 1) {
					isConnected = false;
				} else if (connected == 2) {
					isConnected = true;
				}
				possitiveColorComboBox.setSelectedColor(selectedStyle.getForwardColor());
				negativeColorComboBox.setSelectedColor(selectedStyle.getReverseColor());
				trackNameSizeComboBox.setSelectedItem(selectedStyle.getTrackNameSize());
				labelFieldComboBox.setSelectedItem(selectedStyle.getLabelField());
				maxDepthTextField.setText(String.valueOf(selectedStyle.getMaxDepth()));
				show2TracksCheckBox.setSelected(selectedStyle.getSeparate());
				connectedCheckBox.setSelected(isConnected);
				collapsedCheckBox.setSelected(selectedStyle.getCollapsed());

				switch (DIRECTION_TYPE.valueFor(selectedStyle.getDirectionType())) {
					case NONE:
						colorCheckBox.setSelected(false);
						arrowCheckBox.setSelected(false);
						break;
					case ARROW:
						colorCheckBox.setSelected(false);
						arrowCheckBox.setSelected(true);
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
						System.out.println("Unknown enum selected");
						break;
				}
			}
		}

		initializationDetector = false;

		if (evt.getSource() == lsm && !evt.getValueIsAdjusting()) {
		}
	}

	public com.jidesoft.combobox.ColorComboBox getPossitiveColorCombo() {
		return possitiveColorComboBox;
	}

	public com.jidesoft.combobox.ColorComboBox getNegativeColorComboBox() {
		return negativeColorComboBox;
	}

	public JRPCheckBox getColorCheckBox() {
		return colorCheckBox;
	}

	public JRPCheckBox getArrowCheckBox() {
		return arrowCheckBox;
	}

	public javax.swing.JTable getTable() {
		return table;
	}

	public JRPButton getAddTrackDefaultButton() {
		return addTrackDefaultButton;
	}

	public JRPButton getRemoveTrackDefaultButton() {
		return removeTrackDefaultButton;
	}

	public javax.swing.JTextField getTrackDefaultTextField() {
		return trackDefaultTextField;
	}

	public com.jidesoft.combobox.ColorComboBox getBgColorComboBox() {
		return bgColorComboBox;
	}

	public javax.swing.JComboBox getTrackNameSizeComboBox() {
		return trackNameSizeComboBox;
	}

	public com.jidesoft.combobox.ColorComboBox getFgColorComboBox() {
		return fgColorComboBox;
	}

	public javax.swing.JComboBox getLabelFieldComboBox() {
		return labelFieldComboBox;
	}

	public javax.swing.JTextField getMaxDepthTextField() {
		return maxDepthTextField;
	}

	public javax.swing.JCheckBox getShow2TracksCheckBox() {
		return show2TracksCheckBox;
	}

	public javax.swing.JCheckBox getConnectedCheckBox() {
		return connectedCheckBox;
	}

	public javax.swing.JCheckBox getCollapsedCheckBox() {
		return collapsedCheckBox;
	}

	public javax.swing.JLabel getLabelFieldTip() {
		return labelFieldTip;
	}


	class TrackDefaultPrefTableModel extends AbstractTableModel implements PropertyConstants {

		private List<TrackStyle> tier_styles;
		private AssociationElement element;
		private Entry[] file2types;
		private Entry entry;

		public TrackDefaultPrefTableModel() {
			this.tier_styles = new ArrayList<TrackStyle>();
		}

		public void setStyles(List<TrackStyle> tier_styles) {
			this.tier_styles = tier_styles;
		}

		public List<TrackStyle> getStyles() {
			return this.tier_styles;
		}

		private void setElements(java.util.Map<String, AssociationElement> elements) {
			file2types = elements.entrySet().toArray(new Entry[elements.size()]);
			tier_styles.add(default_annot_style);
			for (Entry entries : file2types) {
				element = (AssociationElement) entries.getValue();

				addElement(entries.getKey().toString(), element);
			}
			fireTableDataChanged();
		}

		private void addElement(String filetype, AssociationElement element) {
			if (element.getPropertyMap() != null) {
				PropertyMap props = default_annot_style.getProperties();
				props.putAll(element.getPropertyMap());
				element.getPropertyMap().putAll(props);
				TrackStyle style = new TrackStyle(props);
				style.setTrackName(filetype);
				tier_styles.add(style);
			}
		}

		private void removeElement(String filetype) {
			Iterator<TrackStyle> iterator = tier_styles.iterator();
			TrackStyle style;

			while (iterator.hasNext()) {
				style = iterator.next();
				if (style.getTrackName().equals(filetype)) {
					iterator.remove();
				}
			}
		}

		// Allow editing most fields in normal rows, but don't allow editing some
		// fields in the "default" style row.
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == COL_TRACK_DEFAULT) {
				return false;
			}

			return true;
		}

		public int getRowCount() {
			return tier_styles.size();
		}

		@Override
		public String getColumnName(int columnIndex) {
			return col_headings[columnIndex];
		}

		public int getColumnCount() {
			return col_headings.length;
		}

		public Object getValueAt(int row, int column) {
			TrackStyle style = tier_styles.get(row);
			switch (column) {
				case COL_TRACK_DEFAULT:
					return style.getTrackName();
				case COL_FOREGROUND:
					return style.getForeground();
				case COL_BACKGROUND:
					return style.getBackground();
				case COL_TRACK_NAME_SIZE:
					return style.getTrackNameSize();
				default:
					return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int c) {
			Object val = getValueAt(0, c);
			if (val == null) {
				return Object.class;
			} else {
				return val.getClass();
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			settingValueFromTable = true;
			if (value != null && !initializationDetector) {
				try {
					TrackStyle style = tier_styles.get(row);
					if (!style.equals(default_annot_style)) {
						entry = file2types[row - 1];
						element = (AssociationElement) entry.getValue();
					}
					switch (col) {
						case COL_FOREGROUND:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_FOREGROUND, value);
							}
							style.setForeground((Color) value);
							fgColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_BACKGROUND:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_BACKGROUND, value);
							}
							style.setBackground((Color) value);
							bgColorComboBox.setSelectedColor((Color) value);
							break;
						case COL_TRACK_NAME_SIZE:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_FONT_SIZE, value.toString());
								style.setTrackNameSize((Float) value);
							}
							style.setTrackNameSize((Float) value);
							trackNameSizeComboBox.setSelectedItem((Float) value);
							break;
						case COL_LABEL_FIELD:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_LABEL_FIELD, value.toString());
							}
							style.setLabelField((String) value);
							break;
						case COL_MAX_DEPTH: {
							int i = parseInteger(((String) value), 0, style.getMaxDepth());
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_MAX_DEPTH, value.toString());
							}
							style.setMaxDepth(i);
						}
						break;
						case COL_DIRECTION_TYPE:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_DIRECTION_TYPE, value.toString());
							}
							style.setDirectionType((TrackConstants.DIRECTION_TYPE) value);
							break;
						case Col_Show_2_Tracks:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_SEPARATE, value.toString());
							}
							style.setSeparate(((Boolean) value).booleanValue());
							break;
						case COL_CONNECTED:
							if (!style.equals(default_annot_style)) {
								if (Boolean.TRUE.equals(value)) {
									element.getPropertyMap().put(PROP_GLYPH_DEPTH, String.valueOf(2));
								} else {
									element.getPropertyMap().put(PROP_GLYPH_DEPTH, String.valueOf(1));
								}
							}
							if (Boolean.TRUE.equals(value)) {
								style.setGlyphDepth(2);
							} else {
								style.setGlyphDepth(1);
							}
							break;
						case COL_COLLAPSED:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_COLLAPSED, value.toString());
							}
							style.setCollapsed(((Boolean) value).booleanValue());
							break;
						case COL_POS_STRAND_COLOR:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_START_COLOR, value);
							}
							style.setForwardColor((Color) value);
							break;
						case COL_NEG_STRAND_COLOR:
							if (!style.equals(default_annot_style)) {
								element.getPropertyMap().put(PROP_END_COLOR, value);
							}
							style.setReverseColor((Color) value);
							break;
						default:
							System.out.println("Unknown column selected: " + col);
					}
					fireTableCellUpdated(row, col);
					table.setRowSelectionInterval(row, row);
				} catch (Exception e) {
					// exceptions should not happen, but must be caught if they do
					System.out.println("Exception in TierPrefsView.setValueAt(): " + e);
				}
			}
			settingValueFromTable = false;
		}

		int parseInteger(String s, int empty_string, int fallback) {
			//System.out.println("Parsing string: '" + s + "'");
			int i = fallback;
			try {
				if ("".equals(s.trim())) {
					i = empty_string;
				} else {
					i = Integer.parseInt(s);
				}
			} catch (Exception e) {
				//System.out.println("Exception: " + e);
				// don't report the error, use the fallback value
			}
			return i;
		}
	}
}
