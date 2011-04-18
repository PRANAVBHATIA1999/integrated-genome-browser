package com.affymetrix.igb.view;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.comparator.SeqSymIdComparator;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.util.DisplayUtils;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;

import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.event.MouseEvent;

public final class SeqGroupView extends IGBTabPanel implements ListSelectionListener, GroupSelectionListener, SeqSelectionListener {
	private static final long serialVersionUID = 1L;
	private static final int TAB_POSITION = 7;
	private static final NumberFormat nformat = NumberFormat.getIntegerInstance(Locale.ENGLISH);
	private static final boolean DEBUG_EVENTS = false;
	protected String[] columnToolTips = {null,BUNDLE.getString("sequenceHeaderLengthToolTip")};
	
	private final GenometryModel gmodel;
	private final JTable seqtable;
	private final ListSelectionModel lsm;
	private BioSeq selected_seq = null;
	private AnnotatedSeqGroup previousGroup = null;
	private int previousSeqCount = 0;
	private TableRowSorter<SeqGroupTableModel> sorter;
	private String most_recent_seq_id = null;

	private static SeqGroupView singleton;

	public static void init(IGBService _igbService) {
		singleton = new SeqGroupView(_igbService);
	}

	public static SeqGroupView getInstance() {
		return singleton;
	}

	private SeqGroupView(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("sequenceTab"), BUNDLE.getString("sequenceTab"), true, TAB_POSITION);
		gmodel = GenometryModel.getGenometryModel();
		seqtable = new JTable(){
			//Implement table header tool tips.
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return columnToolTips[realIndex];
                    }
                };
			}
		};
		seqtable.setToolTipText(BUNDLE.getString("chooseSeq"));
		seqtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		seqtable.setFillsViewportHeight(true);
		
		SeqGroupTableModel mod = new SeqGroupTableModel(null);
		seqtable.setModel(mod);	// Force immediate visibility of column headers (although there's no data).

		JScrollPane scroller = new JScrollPane(seqtable);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(scroller);

		gmodel.addGroupSelectionListener(this);
		gmodel.addSeqSelectionListener(this);
		lsm = seqtable.getSelectionModel();
		lsm.addListSelectionListener(this);
	}

	@Override
	public TabState getDefaultState() {
		return TabState.COMPONENT_STATE_RIGHT_TAB;
	}

	/**
   * Refresh seqtable if more chromosomes are added, for example.
   */
  public void refreshTable() {
	  seqtable.validate();
	  seqtable.updateUI();
	  seqtable.repaint();
	  updateTableHeader();
  }

  public void updateTableHeader(){
	JTableHeader headers = seqtable.getTableHeader();
	TableColumnModel model = headers.getColumnModel();

	TableColumn col1 = model.getColumn(0);
	int n = seqtable.getRowCount();
	//Exclude 'genome'
	if(n >= 2){
		n -= 1;
	}
	col1.setHeaderValue("("+ nformat.format(n) +") Sequence(s)");
  }

  public void groupSelectionChanged(GroupSelectionEvent evt) {
		AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();
		if (SeqGroupView.DEBUG_EVENTS) {
			System.out.println("SeqGroupView received groupSelectionChanged() event");
			if (group == null) {
				System.out.println("  group is null");
			} else {
				System.out.println("  group: " + group.getID());
				System.out.println("  seq count: " + group.getSeqCount());
			}
		}
		if (previousGroup == group) {
			if (group == null) {
				return;
			}
			warnAboutNewlyAddedChromosomes(previousSeqCount, group);
		}

		previousGroup = group;
		previousSeqCount = group == null ? 0 : group.getSeqCount();


		SeqGroupTableModel mod = new SeqGroupTableModel(group);

		sorter = new TableRowSorter<SeqGroupTableModel>(mod){
			@Override
			public Comparator<?> getComparator(int column){
				if(column == 0){
					return String.CASE_INSENSITIVE_ORDER;
				}
				return new SeqLengthComparator();
			}
		};

		selected_seq = null;
		seqtable.setModel(mod);
		seqtable.setRowSorter(sorter);
		
		TableColumn c = seqtable.getColumnModel().getColumn(1);
		c.setCellRenderer(new ColumnRenderer());

		refreshTable();

		if (group != null && most_recent_seq_id != null) {
			// When changing genomes, try to keep the same chromosome selected when possible
			BioSeq aseq = group.getSeq(most_recent_seq_id);
			if (aseq != null) {
				gmodel.setSelectedSeq(aseq);
			}
		}
	}

	private void warnAboutNewlyAddedChromosomes(int previousSeqCount, AnnotatedSeqGroup group) {
		if (previousSeqCount > group.getSeqCount()) {
			System.out.println("WARNING: chromosomes have been added");
			if (previousSeqCount < group.getSeqCount()) {
				System.out.print("New chromosomes:");
				for (int i = previousSeqCount; i < group.getSeqCount(); i++) {
					System.out.print(" " + group.getSeq(i).getID());
				}
				System.out.println();
			}
		}
	}

  public void seqSelectionChanged(SeqSelectionEvent evt) {
		if (SeqGroupView.DEBUG_EVENTS) {
			System.out.println("SeqGroupView received seqSelectionChanged() event: seq is " + evt.getSelectedSeq());
		}
		synchronized (seqtable) {  // or should synchronize on lsm?
			lsm.removeListSelectionListener(this);
			selected_seq = evt.getSelectedSeq();
			if (selected_seq == null) {
				seqtable.clearSelection();
			} else {
				most_recent_seq_id = selected_seq.getID();

				int rowCount = seqtable.getRowCount();
				for (int i = 0; i < rowCount; i++) {
					// should be able to use == here instead of equals(), because table's model really returns seq.getID()
					if (most_recent_seq_id == seqtable.getValueAt(i, 0)) {
						if (seqtable.getSelectedRow() != i) {
							seqtable.setRowSelectionInterval(i, i);
							scrollTableLater(seqtable, i);
						}
						break;
					}
				}
			}
			lsm.addListSelectionListener(this);
		}
	}

  // Scroll the table such that the selected row is visible
  void scrollTableLater(final JTable table, final int i) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // Check the row count first since this is multi-threaded
        if (table.getRowCount() >= i) {
          DisplayUtils.scrollToVisible(table, i, 0);
        }
      }
    });
  }

  public void valueChanged(ListSelectionEvent evt) {
    Object src = evt.getSource();
    if ((src == lsm) && (! evt.getValueIsAdjusting())) { // ignore extra messages
      if (SeqGroupView.DEBUG_EVENTS)  { System.out.println("SeqGroupView received valueChanged() ListSelectionEvent"); }
      int srow = seqtable.getSelectedRow();
      if (srow >= 0)  {
        String seq_name = (String) seqtable.getValueAt(srow, 0);
        selected_seq = gmodel.getSelectedSeqGroup().getSeq(seq_name);
        if (selected_seq != gmodel.getSelectedSeq()) {
          gmodel.setSelectedSeq(selected_seq);
        }
      }
    }
  }

  @Override
  public Dimension getMinimumSize() { return new Dimension(220, 50); }
    @Override
  public Dimension getPreferredSize() { return new Dimension(220, 50); }

	private final class SeqLengthComparator implements Comparator<String>{

		public int compare(String o1, String o2) {
			if (o1 == null || o2 == null) {
				return SeqSymIdComparator.compareNullIDs(o2, o1);	// null is last
			}
			if (o1.length() == 0 || o2.length() == 0) {
				return o2.compareTo(o1);	// empty string is last
			}

			// use valueOf to get a Long object versus a long primitive.
			return Long.valueOf(o1).compareTo(Long.parseLong(o2));
		}
	}

	static final class ColumnRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public ColumnRenderer(){
			setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		}
		
		@Override
		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {

			if(value.toString().length() == 0)
				return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);

			return super.getTableCellRendererComponent(table, nformat.format(Double.valueOf(value.toString())),
					isSelected, hasFocus, row, column);
		}
	}

	@Override
	public boolean isEmbedded() {
		return true;
	}

	@Override
	public boolean isCheckMinimumWindowSize() {
		return true;
	}
}
