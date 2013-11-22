package com.affymetrix.igb.search;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.color.RGB;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.SeqMapRefreshed;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.parsers.TrackLineParser;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.SingletonSymWithProps;
import com.affymetrix.genometryImpl.thread.CThreadHolder;
import com.affymetrix.genometryImpl.thread.CThreadWorker;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.util.DNAUtils;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.ISearchModeExtended;
import com.affymetrix.igb.shared.IStatus;
import com.affymetrix.igb.shared.SearchResults;
import java.util.Calendar;

public class SearchModeResidue implements ISearchModeExtended, 
		SeqMapRefreshed, SeqSelectionListener {
	
	private static final int SEARCH_ALL_ORDINAL = -1;
	private static final String CONFIRM_BEFORE_SEQ_CHANGE = "Confirm before sequence change";
	private static final String OVERLAY_RESULTS = "Overlay Results";
	private static final boolean default_confirm_before_seq_change = true;
	private static final boolean default_optionSelected = true;
	private static final String SEPARATOR = "\\|";
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("searchmoderesidue");
	private static final int MAX_RESIDUE_LEN_SEARCH = 1000000;
	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	private static final Color hitcolors[] = {
		Color.magenta,
		new Color(0x00cd00),
		Color.orange,
		new Color(0x00d7d7),
		new Color(0xb50000),
		Color.blue,
		Color.gray,
		Color.pink};//Distinct Colors for View/Print Ease
	
	private final List<GlyphI> glyphs = new ArrayList<GlyphI>();
	private IGBService igbService;
	private int color = 0;
	private boolean optionSelected;
	
	private final Action createTrackAction = new AbstractAction("Create Track"){

		@Override
		public void actionPerformed(ActionEvent e) {
			CThreadWorker worker = new CThreadWorker("Creating track"){

				@Override
				protected Object runInBackground() {
					String trackId = "Search Track:"+Calendar.getInstance().getTime().toString();
					ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(trackId, "Search Track", null, null);
					style.setColorProvider(new RGB());
					style.setLabelField("match");
					style.setSeparate(false);
					for(GlyphI glyph : glyphs) {
						SingletonSymWithProps info = (SingletonSymWithProps) glyph.getInfo();
						if(info != null) {
							BioSeq seq = info.getSpanSeq(0);
							info.setProperty("method", trackId);
							info.setProperty(TrackLineParser.ITEM_RGB, glyph.getColor());
							seq.addAnnotation(info);
						}
					}
					return null;
				}

				@Override
				protected void finished() {
					igbService.getSeqMapView().updatePanel(true, false);
				}
				
			};
			CThreadHolder.getInstance().execute(SearchModeResidue.this, worker);
		}
	};
	
	public SearchModeResidue(IGBService igbService) {
		super();
		this.igbService = igbService;
		igbService.getSeqMapView().addToRefreshList(this);
		optionSelected = PreferenceUtils.getBooleanParam(OVERLAY_RESULTS, default_optionSelected);
		
		gmodel.addSeqSelectionListener(this);
	}
	
	public String checkInput(String search_text, final BioSeq vseq, final String seq) {
		if (vseq == null ) {
			return MessageFormat.format(BUNDLE.getString("searchErrorNotLoaded"), seq);
		}
		if (search_text.length() < 3) {
			return BUNDLE.getString("searchErrorShort");
		}
		try {
			for(String st : search_text.split(SEPARATOR)){
				Pattern.compile(st, Pattern.CASE_INSENSITIVE);
			}
		} catch (PatternSyntaxException pse) {
			return MessageFormat.format(BUNDLE.getString("searchErrorSyntax"), pse.getMessage());
		} catch (Exception ex) {
			return MessageFormat.format(BUNDLE.getString("searchError"), ex.getMessage());
		}
		
		if (vseq != igbService.getSeqMapView().getAnnotatedSeq()){
			boolean confirm = igbService.confirmPanel(MessageFormat.format(BUNDLE.getString("searchSelectSeq"), 
					vseq.getID(), vseq.getID()), CONFIRM_BEFORE_SEQ_CHANGE, default_confirm_before_seq_change);
			if(!confirm) {
				return BUNDLE.getString("searchCancelled");
			}
			SeqSpan newspan = new SimpleSeqSpan(vseq.getMin(), vseq.getMax(), vseq);
			gmodel.setSelectedSeq(vseq);
			igbService.getSeqMapView().zoomTo(newspan);
		}

//		boolean isComplete = vseq.isComplete();
//		boolean confirm = isComplete ? true : igbService.confirmPanel(MessageFormat.format(BUNDLE.getString("searchConfirmLoad"), seq));
//		if (!confirm) {
//			return false;
//		}
		return null;
	}

	public void finished(BioSeq vseq) {
		boolean isComplete = vseq.isComplete();
		if (!isComplete) {
			igbService.getSeqMapView().setAnnotatedSeq(vseq, true, true, true);
		}
	}

	public void clear(){
		clearResults();
	}
	
	public void mapRefresh() {
		igbService.mapRefresh(glyphs);
	}
	
	public void seqSelectionChanged(SeqSelectionEvent evt) {
		clearResults();
	}
	
	private void clearResults() {
		if (!glyphs.isEmpty()) {
			glyphs.clear();
			igbService.getSeqMapView().updatePanel();
		}
		color = 0;
	}
		
	@Override
	public String getName() {
		return BUNDLE.getString("searchRegexResidue");
	}

	@Override
	public int searchAllUse() {
		return SEARCH_ALL_ORDINAL;
	}

	@Override
	public String getTooltip() {
		return BUNDLE.getString("searchRegexResidueTF");
	}
	
	@Override
	public String getOptionName() {
		return BUNDLE.getString("optionCheckBox");
	}

	@Override
	public String getOptionTooltip() {
		return BUNDLE.getString("optionCheckBoxTT");
	}
	
	@Override
	public boolean getOptionEnable() {
		return hitcolors.length - 1 > color;
	}
	
	@Override
	public void setOptionState(boolean selected){
		optionSelected = selected;
		PreferenceUtils.getTopNode().putBoolean(OVERLAY_RESULTS, selected);
	}
	
	@Override
	public boolean getOptionState(){
		return optionSelected;
	}
	
	public void valueChanged(GlyphI glyph, String seq) {
		for(GlyphI g : glyphs){
			igbService.getSeqMap().deselect(g);
		}
		if(glyph != null){
			int start = (int)glyph.getCoordBox().x;
			int end = (int)(glyph.getCoordBox().x + glyph.getCoordBox().width);
			igbService.getSeqMap().select(glyph);
			igbService.zoomToCoord(seq, start, end);
			igbService.getSeqMapView().centerAtHairline();
		}
	}

	@Override
	public boolean useGenomeInSeqList() {
		return false;
	}

	public SearchResults<GlyphI> search(String search_text, final BioSeq chrFilter, IStatus statusHolder, boolean option) {
		String[] search_terms = search_text.split(SEPARATOR);
		if(!option && search_terms.length <= 1){
			clearResults();
		}
		StringBuilder searchSummary = new StringBuilder();
		for(String st : search_terms) {
			st = st.trim();
			if(st.length() > 3) {
				SearchResults<GlyphI> results = search(st, chrFilter, statusHolder);
				searchSummary.append(st).append(" : ").append(results.getSearchSummary()).append(" ");
			} else {
				searchSummary.append(st).append(" : ").append("Search skipped because character lenght is less than 3.");
			}
		}
		return new SearchResults<GlyphI>(getName(), search_text, chrFilter.getID(), searchSummary.toString(), glyphs);
	}

	private SearchResults<GlyphI> search(String search_text, final BioSeq chrFilter, IStatus statusHolder) {
		SeqSpan visibleSpan = igbService.getSeqMapView().getVisibleSpan();
		GenericAction loadResidue = igbService.loadResidueAction(visibleSpan, true);
		loadResidue.actionPerformed(null);
		
//		boolean isComplete = chrFilter.isComplete();
//		if (!isComplete) {
//			igbService.loadResidues(igbService.getSeqMapView().getVisibleSpan(), true);
//		}
		
		String friendlySearchStr = MessageFormat.format(BUNDLE.getString("friendlyPattern"), search_text, chrFilter.getID());
		Pattern regex = null;
		try {
			regex = Pattern.compile(search_text, Pattern.CASE_INSENSITIVE);
		} catch (Exception ex) { // should not happen already checked above
			return new SearchResults<GlyphI>(getName(), search_text, chrFilter.getID(), ex.getLocalizedMessage(), null);
		}

		statusHolder.setStatus(friendlySearchStr);

		int residuesLength = chrFilter.getLength();
		int hit_count1 = 0;
		int hit_count2 = 0;
		int residue_offset1 = chrFilter.getMin();
		int residue_offset2 = chrFilter.getMax();
		Thread current_thread = Thread.currentThread();
		
		for(int i=visibleSpan.getMin(); i<visibleSpan.getMax(); i+=MAX_RESIDUE_LEN_SEARCH){
			if(current_thread.isInterrupted()) {
				break;
			}
			
			int start = Math.max(i-search_text.length(), 0);
			int end = Math.min(i+MAX_RESIDUE_LEN_SEARCH, residuesLength);
			
			String residues = chrFilter.getResidues(start, end);
			List<GlyphI> results = igbService.getSeqMapView().searchForRegexInResidues(true, regex, residues, Math.max(residue_offset1,start), hitcolors[color]);
			hit_count1 += results.size();
			glyphs.addAll(results);
			
			// Search for reverse complement of query string
			// flip searchstring around, and redo nibseq search...
			String rev_searchstring = DNAUtils.reverseComplement(residues);
			results = igbService.getSeqMapView().searchForRegexInResidues(false, regex, rev_searchstring, Math.min(residue_offset2,end), hitcolors[color]);
			hit_count2 += results.size();
			glyphs.addAll(results);
		}
		String statusStr = MessageFormat.format(BUNDLE.getString("searchSummary"), hit_count1, hit_count2);
		statusHolder.setStatus(statusStr);
		igbService.getSeqMap().updateWidget();

		Collections.sort(glyphs, new Comparator<GlyphI>() {
			public int compare(GlyphI g1, GlyphI g2) {
				return Integer.valueOf((int)g1.getCoordBox().x).compareTo((int)g2.getCoordBox().x);
			}
		});
		color++;
		return new SearchResults<GlyphI>(getName(), search_text, chrFilter.getID(), statusStr, glyphs);
	}

	@Override
	public Action getCustomAction() {
		return createTrackAction;
	}
}
