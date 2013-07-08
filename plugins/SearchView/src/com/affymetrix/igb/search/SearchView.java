package com.affymetrix.igb.search;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import java.awt.Dimension;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableColumn;


import com.affymetrix.common.ExtensionPointHandler;

import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.thread.CThreadEvent;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.event.GenericServerInitEvent;
import com.affymetrix.genometryImpl.event.GenericServerInitListener;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.SearchListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.util.Constants;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genometryImpl.thread.CThreadHolder;
import com.affymetrix.genometryImpl.thread.CThreadListener;
import com.affymetrix.genometryImpl.thread.CThreadWorker;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.swing.CCPUtils;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.JRPComboBoxWithSingleListener;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPCheckBox;
import com.affymetrix.genoviz.swing.recordplayback.JRPTable;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.shared.ISearchHints;
import com.affymetrix.igb.shared.ISearchMode;
import com.affymetrix.igb.shared.ISearchModeExtended;
import com.affymetrix.igb.shared.ISearchModeSym;
import com.affymetrix.igb.shared.IStatus;
import com.jidesoft.hints.ListDataIntelliHints;

public final class SearchView extends IGBTabPanel implements
		GroupSelectionListener, SeqSelectionListener, GenericServerInitListener, SearchListener, IStatus {

	private static final long serialVersionUID = 0;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("search");
	private static final String DEFAULT_SEARCH_MODE_CLASS = "SearchModeID";
	private static final int TAB_POSITION = 2;
	private static String[] regexChars = new String[]{"|"};
	
	public class SearchModeAction extends GenericAction {
		private static final long serialVersionUID = 1L;

		private SearchModeAction() {
			super(null, null, null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			String searchMode = (String) SearchView.this.searchCB.getSelectedItem();
			selectedSearchMode = searchModeMap.get(searchMode);
			if (selectedSearchMode == null) {
				return;
			}
			clearTable();
			igbService.getSeqMap().updateWidget();

			SearchView.this.initSequenceCB();
//			SearchView.this.searchTF.setEnabled(true);

			initOptionCheckBox();

			if (selectedSearchMode instanceof SearchModeResidue) {
				setModel(new GlyphSearchResultsTableModel(null, null));
			}
			else {
				setModel(new SymSearchResultsTableModel(null));
			}

			SearchView.this.searchTF.setToolTipText(selectedSearchMode.getTooltip());

			return;
		}
	}
	private SearchModeAction searchModeAction = new SearchModeAction();

	public class SearchAction extends GenericAction {
		private static final long serialVersionUID = 1L;

		private SearchAction() {
			super(null, null, null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			String searchMode = (String) SearchView.this.searchCB.getSelectedItem();
			final boolean search_hint_selection = e == null;
			selectedSearchMode = searchModeMap.get(searchMode);
			String chrStr = (String) SearchView.this.sequenceCB.getSelectedItem();
			final BioSeq chrfilter = Constants.GENOME_SEQ_ID.equals(chrStr) ? null : group.getSeq(chrStr);
			String errorMessage = selectedSearchMode.checkInput(SearchView.this.searchTF.getText().trim(), chrfilter, SearchView.this.sequenceCB.getSelectedItem().toString());
			if (errorMessage == null) {
				enableComp(false);
				clearTable();
				CThreadWorker<SearchResultsTableModel, Void> worker = new CThreadWorker<SearchResultsTableModel, Void>("Searching...") {

					@Override
					protected SearchResultsTableModel runInBackground() {
						if (selectedSearchMode instanceof SearchModeResidue) {
							List<GlyphI> glyphs = ((SearchModeResidue)selectedSearchMode).search(SearchView.this.searchTF.getText().trim(), chrfilter, SearchView.this, optionCheckBox.isSelected());
							if(isCancelled()){
								setStatus("Search cancelled");
								return null;
							}
							return new GlyphSearchResultsTableModel(glyphs, SearchView.this.sequenceCB.getSelectedItem().toString());
						}
						else {
							String search_term = SearchView.this.searchTF.getText().trim();
							if(search_hint_selection){
								for(String c : regexChars){
									search_term = search_term.replace(c, "\\"+c);
								}
							}
							List<SeqSymmetry> syms = ((ISearchModeSym)selectedSearchMode).search(search_term, chrfilter, SearchView.this, optionCheckBox.isSelected());
							if(isCancelled()){
								setStatus("Search cancelled");
								return null;
							}
							return new SymSearchResultsTableModel(syms);
						}
					}

					@Override
					protected void finished() {
						enableComp(true);
						initOptionCheckBox();
						try {
							if(!isCancelled()){
								if (selectedSearchMode instanceof SearchModeResidue) {
									((SearchModeResidue)selectedSearchMode).finished(chrfilter);
								}
								SearchResultsTableModel model = get();
								if (model != null) {
									setModel(model);
								}
							}else{
								clearResults();
								setStatus("Search cancelled");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					}
				};
//				CThreadHolder.getInstance().addListener(cancel);
				CThreadHolder.getInstance().execute(this, worker);
			}
			else {
				ErrorHandler.errorPanel(errorMessage);
			}
		}
	}
	private SearchAction searchAction = new SearchAction();

	public class ClearAction extends GenericAction {
		private static final long serialVersionUID = 1L;

		private ClearAction() {
			super(null, null, null);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			clearResults();
			searchTF.setText("");
		}
	}
	
	ItemListener itemListener = new ItemListener(){

		public void itemStateChanged(ItemEvent e) {
			if(selectedSearchMode != null && selectedSearchMode instanceof ISearchModeExtended){
				JCheckBox checkbox = (JCheckBox)e.getSource();
				((ISearchModeExtended)selectedSearchMode).setOptionState(checkbox.isSelected());
			}
		}
		
	};
	
	private ClearAction clearAction = new ClearAction();
	// A maximum number of hits that can be found in a search.
	// This helps protect against out-of-memory errors.
	private static GenometryModel gmodel = GenometryModel.getGenometryModel();
	private static final String CHOOSESEARCH = BUNDLE.getString("searchChooseSearch");
	private static final String FINDANNOTS = BUNDLE.getString("findAnnots");
	private static final String FINDANNOTSNULL = BUNDLE.getString("pleaseSelectGenome");
	private static final String SEQUENCETOSEARCH = BUNDLE.getString("searchSequenceToSearch");
	private final JRPTextField searchTF = new JRPTextField("SearchView_searchTF", 10);
	private final JPanel pan1 = new JPanel();
	private final JRPComboBoxWithSingleListener sequenceCB = new JRPComboBoxWithSingleListener("SearchView_sequenceCB");
	private final JRPComboBoxWithSingleListener searchCB = new JRPComboBoxWithSingleListener("SearchView_searchCB");
	private final JRPCheckBox optionCheckBox = new JRPCheckBox("SearchView_optionCheckBox", "");
	private final JRPButton searchButton = new JRPButton("SearchView_searchButton", MenuUtil.getIcon("16x16/actions/search.png"));
	private final JRPButton clearButton = new JRPButton("SearchView_clearButton", MenuUtil.getIcon("16x16/actions/delete.png"));
	//private final CancelButton cancel = new CancelButton("SearchView_CancelButton",MenuUtil.getIcon("16x16/actions/stop.png"));
	private JRPTable table = new JRPTable("SearchView_table");
	private JLabel status_bar = new JLabel(BUNDLE.getString("noResults"));
	private TableRowSorter<SearchResultsTableModel> sorter;
	private ListSelectionModel lsm;
	private AnnotatedSeqGroup group;
	private int seqCount = 0;
	private CThreadWorker<Object, Void> worker;
	private Map<String, ISearchMode> searchModeMap;
	private ISearchMode selectedSearchMode;

	ListDataIntelliHints<String> searchHints = new ListDataIntelliHints<String>(searchTF, new String[]{}){

        @Override
        public void acceptHint(Object context) {
            String text = (String) context;
            super.acceptHint(context);
            searchTF.setText(text);
			searchAction.actionPerformed(null);
        }

        @Override
		public boolean updateHints(Object context) {
			String search_term = (String) context;
			if (GenometryModel.getGenometryModel().getSelectedSeqGroup() == null || search_term.length() <= 1) {
				return false;
			} else {
				if(!(selectedSearchMode instanceof ISearchHints)){
					return false;
				}
				Set<String> results = ((ISearchHints)selectedSearchMode).search(search_term);
				        
                if (results != null && results.size() >= 1) {
                    this.setListData(results.toArray());
                    return true;
                }
			}
			return false;
		}
	};
	
	public SearchView(IGBService igbService) {
		super(igbService, BUNDLE.getString("searchTab"), BUNDLE.getString("searchTab"), false, TAB_POSITION);
		
		group = gmodel.getSelectedSeqGroup();

		this.setLayout(new BorderLayout());

		initSearchCB();

		initComponents();
		String annotsStr = (group == null) ? FINDANNOTSNULL : MessageFormat.format(FINDANNOTS, group.getID());
		pan1.setBorder(BorderFactory.createTitledBorder(annotsStr));
		pan1.setLayout(new BoxLayout(pan1, BoxLayout.X_AXIS));

		pan1.add(new JLabel(BUNDLE.getString("searchLabelText")));
		pan1.add(searchCB);

		pan1.add(Box.createRigidArea(new Dimension(4, 0)));
		pan1.add(new JLabel(BUNDLE.getString("inLabelText")));
		sequenceCB.setMinimumSize(new Dimension(4, 0));
		sequenceCB.setToolTipText(SEQUENCETOSEARCH);
		pan1.add(sequenceCB);

		pan1.add(Box.createRigidArea(new Dimension(4, 0)));
		pan1.add(new JLabel(BUNDLE.getString("forLabelText")));
		pan1.add(searchTF);

		pan1.add(Box.createRigidArea(new Dimension(4, 0)));

		pan1.add(searchButton);
		pan1.add(clearButton);

		pan1.add(Box.createRigidArea(new Dimension(2, 0)));

		pan1.add(optionCheckBox);

		if (group == null) {
			searchCB.setEnabled(false);
			searchTF.setEnabled(false);
			searchButton.setEnabled(false);
		}

		this.initSequenceCB();

		this.initTable();

		this.add("North", pan1);


		JScrollPane scroll_pane = new JScrollPane(table);
		this.add(scroll_pane, BorderLayout.CENTER);

		Box bottom_row = Box.createHorizontalBox();
		this.add(bottom_row, BorderLayout.SOUTH);

//		bottom_row.add(cancel);
		bottom_row.add(status_bar);
		validate();

		searchTF.setComponentPopupMenu(CCPUtils.getCCPPopup());
		gmodel.addGroupSelectionListener(this);
		gmodel.addSeqSelectionListener(this);
		searchCB.addActionListener(searchModeAction);
		searchTF.addActionListener(searchAction);
		searchButton.addActionListener(searchAction);
		clearButton.addActionListener(clearAction);
		optionCheckBox.addItemListener(itemListener);
		igbService.addServerInitListener(this);
		igbService.addSearchListener(this);
	}

	private void initOptionCheckBox() {
		String searchMode = (String) searchCB.getSelectedItem();
		selectedSearchMode = searchModeMap.get(searchMode);
		
		if(selectedSearchMode == null)
			return;
		
		if(selectedSearchMode instanceof ISearchModeExtended){
			ISearchModeExtended extenedSearch = (ISearchModeExtended)selectedSearchMode;
			optionCheckBox.setText(extenedSearch.getOptionName());
			optionCheckBox.setToolTipText(extenedSearch.getOptionTooltip());
			boolean enabled = extenedSearch.getOptionEnable();
			optionCheckBox.setEnabled(enabled);
			if(!enabled){
				optionCheckBox.setSelected(false);
			}else{
				optionCheckBox.setSelected(extenedSearch.getOptionState());
			}
		}else{
			optionCheckBox.setEnabled(false);
			optionCheckBox.setSelected(false);
		}
		
	}

	private void initSequenceCB() {
		ThreadUtils.runOnEventQueue(new Runnable() {

			public void run() {
				// set up the sequence combo_box
				sequenceCB.removeAllItems();
				if (group != null) {
					if (selectedSearchMode != null && selectedSearchMode.useGenomeInSeqList()) {
						sequenceCB.addItem(Constants.GENOME_SEQ_ID); // put this at top of list
					}
					for (BioSeq seq : group.getSeqList()) {
						if (seq.getID().equals(Constants.GENOME_SEQ_ID)) {
							continue;
						}
						sequenceCB.addItem(seq.getID());
					}
					sequenceCB.setToolTipText(SEQUENCETOSEARCH);
					sequenceCB.setEnabled(true);
				} else {
					sequenceCB.setToolTipText("Genome has not been selected");
					sequenceCB.setEnabled(false);
				}

				sequenceCB.setPreferredSize(new Dimension(searchCB.getPreferredSize().width, searchCB.getPreferredSize().height));
				setSequenceCBValue();
			}
		});
	}

	public void initSearchCB() {
		Object saveSearchMode = searchCB.getSelectedItem();
		searchCB.removeAllItems();
		searchModeMap = new HashMap<String, ISearchMode>();
		boolean saveFound = false;
		List<ISearchMode> searchModes = new ArrayList<ISearchMode>();
		searchModes.addAll(ExtensionPointHandler.getExtensionPoint(ISearchModeSym.class).getExtensionPointImpls());
		searchModes.add(new SearchModeResidue(igbService));
		// consistent order for search modes
		Collections.sort(searchModes,
			new Comparator<ISearchMode>() {
				@Override
				public int compare(ISearchMode o1, ISearchMode o2) {
					return o1.searchAllUse() - o2.searchAllUse();
				}
			}
		);

		ISearchMode defaultSearchMode = null;
		for (ISearchMode searchMode : searchModes) {
			searchCB.addItem(searchMode.getName());
			searchModeMap.put(searchMode.getName(), searchMode);
			if (searchMode == saveSearchMode) {
				saveFound = true;
			}
			if (DEFAULT_SEARCH_MODE_CLASS.equals(searchMode.getClass().getSimpleName())) {
				defaultSearchMode = searchMode;
			}
		}
		searchCB.setToolTipText(CHOOSESEARCH);
		if (saveSearchMode == null || !saveFound) {
			if (defaultSearchMode != null) {
				searchCB.setSelectedItem(defaultSearchMode);
				saveSearchMode = defaultSearchMode;
			}
			else if (searchCB.getItemCount() > 0) {
				searchCB.setSelectedIndex(0);
				saveSearchMode = searchCB.getSelectedItem();
			}
		} else {
			searchCB.setSelectedItem(saveSearchMode);
		}
		initSequenceCB();
	}

	private void initComponents() {
		searchTF.setEnabled(true);
		searchTF.setMinimumSize(new Dimension(125, 50));

		searchButton.setToolTipText("Search");
		searchButton.setEnabled(true);

		clearButton.setToolTipText("Clear");
//		cancel.setEnabled(false);
	}

	private void initTable() {

		lsm = table.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

//		setModel(SearchModeHolder.getInstance().getSearchModes().get(0).getEmptyTableModel());
		table.setRowSelectionAllowed(true);
		table.setEnabled(true);

		table.addMouseListener(list_selection_listener);
	}

	private void setModel(SearchResultsTableModel model) {
		sorter = new TableRowSorter<SearchResultsTableModel>(model);
		table.setModel(model);
		table.setRowSorter(sorter);
		
		for (int i = 0; i < model.getColumnWidth().length; i++) {
			int colPer = model.getColumnWidth()[i];
			int colWidth = table.getWidth() * colPer / 100;
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(colWidth);

			int colAlign = model.getColumnAlign()[i];
			DefaultTableCellRenderer dtcr = model.getColumnRenderer(i);
			dtcr.setHorizontalAlignment(colAlign);
			column.setCellRenderer(dtcr);
		}
	}

	public void zoomToSym(SeqSymmetry sym, List<SeqSymmetry> altSymList) {
		GenometryModel gmodel = GenometryModel.getGenometryModel();
		AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();

		if (sym != null) {
			if (altSymList != null && altSymList.contains(sym)) {
				if (group == null) {
					return;
				}
				zoomToCoord(sym);
				return;
			}

			if (igbService.getSeqMapView().getItemFromTier(sym) == null) {
				if (group == null) {
					return;
				}
				// Couldn't find sym in map view! Go ahead and zoom to it.
				zoomToCoord(sym);
				return;
			}

			// Set selected symmetry normally
			List<SeqSymmetry> syms = new ArrayList<SeqSymmetry>(1);
			syms.add(sym);
			igbService.getSeqMapView().select(syms, true);
		}
	}

	private void zoomToCoord(SeqSymmetry sym) throws NumberFormatException {
		GenometryModel gmodel = GenometryModel.getGenometryModel();
		AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();
		String seqID = sym.getSpanSeq(0).getID();
		BioSeq seq = group.getSeq(seqID);
		if (seq != null) {
			SeqSpan span = sym.getSpan(0);
			if (span != null) {
				// zoom to its coordinates
				igbService.zoomToCoord(seqID, span.getStart(), span.getEnd());
			}
		}
	}

	/** This is called when the user double click a row of the table. */
	private final MouseListener list_selection_listener = new MouseListener() {

		public void mouseClicked(MouseEvent e) {
			if (e.getComponent().isEnabled()
					&& e.getButton() == MouseEvent.BUTTON1
					&& e.getClickCount() == 2) {
				int srow = table.getSelectedRow();
				srow = table.convertRowIndexToModel(srow);
				if (srow < 0) {
					return;
				}
				if (selectedSearchMode instanceof SearchModeResidue) {
					GlyphI glyph = ((GlyphSearchResultsTableModel)table.getModel()).get(srow);
					((SearchModeResidue)selectedSearchMode).valueChanged(glyph, ((GlyphSearchResultsTableModel)table.getModel()).seq);
				}
				else {
					SeqSymmetry sym = ((SymSearchResultsTableModel)table.getModel()).get(srow);
					zoomToSym(sym, ((ISearchModeSym)selectedSearchMode).getAltSymList());
				}
			}
		}

		public void mousePressed(MouseEvent me) {}

		public void mouseReleased(MouseEvent me) {}

		public void mouseEntered(MouseEvent me) {}

		public void mouseExited(MouseEvent me) {}
	};

	// remove the previous search results from the map.
	private void clearResults() {
		String searchMode = (String) SearchView.this.searchCB.getSelectedItem();
		selectedSearchMode = searchModeMap.get(searchMode);
		if (selectedSearchMode != null && selectedSearchMode instanceof SearchModeResidue) {
			((SearchModeResidue)selectedSearchMode).clear();
		}
		clearTable();
	}

	private void clearTable() {
		if (table.getModel() instanceof SearchResultsTableModel) {
			((SearchResultsTableModel) table.getModel()).clear();
		}
		((AbstractTableModel) table.getModel()).fireTableDataChanged();
	}

	public void enableComp(boolean enabled) {
		searchTF.setEnabled(enabled);
		sequenceCB.setEnabled(enabled);
		searchCB.setEnabled(enabled);
		searchButton.setEnabled(enabled);
		clearButton.setEnabled(enabled);
	}

	public void genericServerInit(GenericServerInitEvent evt) {
		initOptionCheckBox();
	}
	
	public void groupSelectionChanged(GroupSelectionEvent evt) {
		groupOrSeqChange();
		clearResults();
	}

	public void seqSelectionChanged(SeqSelectionEvent evt) {
		groupOrSeqChange();
	}

	private void groupOrSeqChange() {
		AnnotatedSeqGroup newGroup = gmodel.getSelectedSeqGroup();
		int newSeqCount = (group == null) ? 0 : group.getSeqCount();
		String annotsStr = (newGroup == null) ? FINDANNOTSNULL : MessageFormat.format(FINDANNOTS, newGroup.getID());
		pan1.setBorder(BorderFactory.createTitledBorder(annotsStr));
		this.searchCB.setEnabled(newGroup != null);
		this.searchButton.setEnabled(newGroup != null);
		this.searchTF.setEnabled(newGroup != null);
		setStatus("");

		// only re-initialize the combobox if the group or seqs have changed
		if (newGroup != group || seqCount != newSeqCount) {
			group = newGroup;
			seqCount = newSeqCount;
			this.initSequenceCB();
			initOptionCheckBox();
		}
		
		setSequenceCBValue();
	}

	/** Set the text in the status bar in a thread-safe way. */
	public void setStatus(final String text) {
		ThreadUtils.runOnEventQueue(new Runnable() {

			public void run() {
				status_bar.setText(text);
			}
		});
	}


	@Override
	public boolean isEmbedded() {
		return true;
	}

	@SuppressWarnings("serial")
	private class CancelButton extends JRPButton implements CThreadListener, ActionListener {

		public CancelButton(String id, ImageIcon icon) {
			super(id, icon);
			setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
			addActionListener(this);
		}

		public void heardThreadEvent(CThreadEvent cte) {
			if (cte.getState() == CThreadEvent.STARTED) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}

		public void actionPerformed(ActionEvent ae) {
			if (worker != null && !worker.isCancelled() && !worker.isDone()) {
				worker.cancel(true);
			}
		}
	}

	@Override
	public void searchResults(String searchText, List<SeqSymmetry> symList) {
		searchTF.setText(searchText);
		
		clearResults();
		// Hard code the search mode to 'Keyword'
		// As currently this only happens when searching from the IGB main panel search box which is a keyword search
		searchCB.setSelectedItem("Keyword");
		searchModeAction.actionPerformed(null);
		
		setModel(new SymSearchResultsTableModel(symList));
		select();
	}
	
	// Set sequence checkbox value as current selected chromosome for residue search mode
	private void setSequenceCBValue() {
		if (selectedSearchMode instanceof SearchModeResidue && gmodel.getSelectedSeq() != null) {
			sequenceCB.setSelectedItem(gmodel.getSelectedSeq().getID());
		} else {
			sequenceCB.setSelectedItem(Constants.GENOME_SEQ_ID);
		}
	}
}
