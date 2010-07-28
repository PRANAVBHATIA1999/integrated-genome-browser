package org.bioviz.protannot;

import com.affymetrix.genometryImpl.util.GeneralUtils;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

/**
 * Displays Properties (name, value pairs) associated with
 * whatever Glyph objects the user has selected.
 */
final class ModPropertySheet extends JPanel {

    private final JLabel title;
    private JScrollPane scroll_pane;
    private final JViewport jvp;
    private static final String DEFAULT_TITLE = " ";
    private Properties[] props;


    /**
     * Create a new PropertySheet containing no data.
     */
    ModPropertySheet() {
        super();
        scroll_pane = new JScrollPane();
        title = new JLabel(DEFAULT_TITLE);
        jvp = new JViewport();
        jvp.setView(title);
        scroll_pane.setColumnHeaderView(jvp);
    }

    /**
     * Set the title, a JLabel attached to a JViewPort.
     * @param   ttl     Name of the title
     */
    void setTitle(String ttl) {
        this.title.setText(ttl);
        jvp.setView(title);
    }

    /**
     * Gets column heading from properties.
     * @param   props   Properties from which header names are to be retrieved.
     * @return          Returns array of string containing header names.
     */
    private static String[] getColumnHeadings(
            Properties[] props) {
        // will contain number of Properties + 1
        String[] col_headings = null;
        // the number of items being described
        int num_items = props.length;

        col_headings = new String[num_items + 1];
        col_headings[0] = "";
        for (int i = 1; i < col_headings.length; i++) {
            Properties properties = props[i - 1];
            Object value = properties.getProperty("Match id");
            if (value == null) {
                value = properties.getProperty("mRNA accession");
                if (value == null) {
                    value = "";
                }
            }
            // now we just number the columns - TODO: use
            // a label that lets the user connect the heading
            // with what they see on the display
            col_headings[i] = (String) value;
        }
        return col_headings;
    }

    /**
     * Build and return rows for the table to be shown in
     * this PropertySheet.
     * If there are no Properties to be shown, then returns
     * default rows.
     * @param   name_values - a List containing name-values for a
     * one or more Properties
     * @param   props       - the list of Properties
     * @return  String[]
     */
    private static String[][] buildRows(List<String[]> name_values, Properties[] props) {
        int num_props = props.length;
        List<String[]> nv = new ArrayList<String[]>();
        for (String[] vals : name_values) {
            String content = vals[0];
            if (!Xml2GenometryParser.IDSTR.equals(content) && !Xml2GenometryParser.NAMESTR.equals(content)) {
                nv.add(vals);
            }
        }
        String[][] rows = null;
        rows = new String[nv.size()][num_props + 1];
        for (int i = 0; i < nv.size(); i++) {
            String[] vals = nv.get(i);
            rows[i][0] = vals[0];
			System.arraycopy(vals, 1, rows[i], 1, vals.length - 1);
        }
        return rows;
    }

    /**
     * Show data associated with the given properties.
     * Uses buildRows() to retrieve ordered name-value pairs.
     * @param   props - the given Properties
     * @see     java.util.Properties
     * @see     #buildRows(List, Properties[])
     */
    void showProperties(Properties[] props) {
        this.props = props;
        List<String[]> name_values = ModPropertyKeys.getNameValues(props);
        String[][] rows = buildRows(name_values, props);
        String[] col_headings = getColumnHeadings(props);
        JTable table = new JTable(); // the table showing name-value pairs
        
		TableModel model = new DefaultTableModel(rows, col_headings){
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
        table.setModel(model);
		PropertySheetHelper helper = new PropertySheetHelper(table);
		table.addMouseListener(helper);
		table.addMouseMotionListener(helper);
        table.setRowSelectionAllowed(true);
		table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
        table.setEnabled(true);
		setColmnWidth(rows, table);
		Dimension size = new Dimension(1000, 1000);
        size.height = table.getSize().height;
        table.setSize(size);
        this.removeAll();
        this.setLayout(new BorderLayout());
        scroll_pane = new JScrollPane(table);
        this.add(title, BorderLayout.NORTH);
        this.add(scroll_pane, BorderLayout.CENTER);
        validate();
    }

	// measure column headings so we can make size decisions
	private static void setColmnWidth(String[][] rows, JTable table){
		int extra = 50;
        int champion = 0;
        int candidate = 0;
        FontMetrics metrix = table.getFontMetrics(table.getFont());
        for (int i = 0; i < rows.length; i++) {
            candidate = metrix.stringWidth(rows[i][0]);
            champion = (candidate > champion ? candidate : champion);
        }

		for(int i=0; i<table.getColumnCount(); i++){
			if(i==0){
				table.getColumnModel().getColumn(0).setPreferredWidth(champion+extra);
				table.getColumnModel().getColumn(0).setMaxWidth(champion+extra);
			}
		}
		
	}
	
    /**
     * Returns properties of selected glyph.
     * @return  Return properties of selected glyph.
     */
    Properties[] getProperties() {
        return this.props;
    }

	private class PropertySheetHelper extends DefaultTableCellRenderer implements
			MouseListener, MouseMotionListener {
		
		private final JTable jtable;
		private final DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
		private final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
		private final Cursor defaultCursor = null;

		public PropertySheetHelper(JTable jtable){
			this.jtable = jtable;
			for(int i=0; i<jtable.getColumnCount(); i++){
				jtable.getColumnModel().getColumn(i).setCellRenderer(this);
			}
		}

		@Override
		public Component getTableCellRendererComponent (JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row, int column){

			if(isURLField(row,column)){

				String url = "<html> <a href='" + (String)obj + "'>" +
						(String)obj + "</a> </html>)";

				cell.setText(url);
			}else{
				cell.setText((String) obj);
			}
			
			return cell;
		}

		@Override
		public void mouseClicked(MouseEvent e){

			Point p = e.getPoint();
			int row = jtable.rowAtPoint(p);
			int column = jtable.columnAtPoint(p);
			if (isURLField(row,column)) {
				GeneralUtils.browse((String) jtable.getValueAt(row, column));
			}

		}
		public void mouseMoved(MouseEvent e) {
			Point p = e.getPoint();
			int row = jtable.rowAtPoint(p);
			int column = jtable.columnAtPoint(p);

			if(isURLField(row,column)){
				jtable.setCursor(handCursor);
			}else if(jtable.getCursor() != defaultCursor) {
				jtable.setCursor(defaultCursor);
			}
		}

		public void mouseDragged(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}

		private boolean isURLField(int row, int column){
			return (column != 0 && jtable.getValueAt(row, 0).equals("URL"));
		}
	}
}
