package com.affymetrix.igb.shared;

import com.affymetrix.genoviz.swing.PartialLineBorder;
import com.jidesoft.grid.JideTable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A customized table for IGB.
 * 
 * @author david, modified by nick
 */
public class StyledJTable extends JideTable {

	private static final long serialVersionUID = 1L;
	private Color selectionBackground, selectionForeground;
	
	// The list will save all the unchangeable column num
	public ArrayList<Integer> list = new ArrayList<Integer>();

	public StyledJTable(TableModel tm) {
		super(tm);
		init();
	}

	public StyledJTable() {
		super();
		init();
	}

	public StyledJTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		init();
	}

	public StyledJTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		init();
	}

	public StyledJTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		init();
	}

	public StyledJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		init();
	}

	@SuppressWarnings("rawtypes")
	public StyledJTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
		init();
	}

	@SuppressWarnings("deprecation")
	private void init() {
		// Jidesoft table configuration methods
		setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN_FILL); 
		setFillsGrids(true);

		// Java Default Table Configuration methods
		setCellSelectionEnabled(true);
		UIDefaults defaults = javax.swing.UIManager.getDefaults();
		selectionForeground = defaults.getColor("Table.selectionForeground");
		selectionBackground = defaults.getColor("Table.selectionBackground");
		if(selectionForeground == null){
			selectionForeground = Color.WHITE;
		}
		if(selectionBackground == null){
			selectionBackground = Color.BLUE;
		}
		setSelectionForeground(selectionBackground);
		setIntercellSpacing(new Dimension(1, 1));
		setShowGrid(true);
		setGridColor(new Color(11184810));
		setRowHeight(20);

		JTableHeader header = getTableHeader();
		header.setReorderingAllowed(false);
		header.setBorder(new PartialLineBorder(Color.black, 1, "B"));
		TableCellRenderer renderer = header.getDefaultRenderer();
		JLabel label = (JLabel) renderer;
		label.setHorizontalAlignment(JLabel.CENTER);
		header.setDefaultRenderer(renderer);

		Font f = new Font("SansSerif", Font.BOLD, 12);
		header.setFont(f);

		TableCellEditor editor = getDefaultEditor(String.class);
		((DefaultCellEditor) editor).setClickCountToStart(1);
		setDefaultEditor(String.class, editor);
	}

	@Override
	public TableCellRenderer getCellRenderer(int r, int c) {
		TableCellRenderer renderer = super.getCellRenderer(r, c);
		if (renderer instanceof DefaultTableCellRenderer) {
			((DefaultTableCellRenderer) renderer).setHorizontalAlignment(SwingConstants.CENTER);
		}
		return renderer;
	}

	@Override
	public Component prepareRenderer(TableCellRenderer tcr, int r, int c) {
		Component component = super.prepareRenderer(tcr, r, c);
		if(component!=null)
			return setComponentBackground(component, r, c);
		else 
			return null;
	}

	@Override
	public Component prepareEditor(TableCellEditor tce, int r, int c) {
		Component component = super.prepareEditor(tce, r, c);
		return setComponentBackground(component, r, c);
	}

	public Component setComponentBackground(Component component, int r, int c) {
		if (!list.contains(c)) {
			if (isCellEditable(r, c)) {
				component.setBackground(Color.WHITE);
			} else {
				component.setBackground(new Color(235, 235, 235));
			}
			if(isCellSelected(r, c)){
				component.setBackground(selectionBackground);
				component.setForeground(selectionForeground);
			}else{
				component.setForeground(Color.BLACK);
			}
		}
	
		return component;
	}

	public void stopCellEditing() {
		TableCellEditor tce = getCellEditor();
		if (tce != null) {
			tce.cancelCellEditing();
		}
	}
}
