package com.affymetrix.igb.search;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.color.RGB;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.SeqMapRefreshed;
import com.affymetrix.genometry.event.SeqSelectionEvent;
import com.affymetrix.genometry.event.SeqSelectionListener;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.parsers.TrackLineParser;
import com.affymetrix.genometry.quickload.QuickLoadSymLoader;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.style.DefaultStateProvider;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.symmetry.impl.SingletonSymWithProps;
import com.affymetrix.genometry.thread.CThreadHolder;
import com.affymetrix.genometry.thread.CThreadWorker;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils;
import com.affymetrix.genometry.util.ModalUtils;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.util.DNAUtils;
import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.services.search.ISearchMode;
import org.lorainelab.igb.services.search.ISearchModeExtended;
import org.lorainelab.igb.services.search.IStatus;
import org.lorainelab.igb.services.search.SearchResults;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;

@Component(name = SearchModeResidue.COMPONENT_NAME, immediate = true, service = ISearchMode.class)
public class SearchModeResidue implements ISearchModeExtended, SeqMapRefreshed, SeqSelectionListener {

    public static final String COMPONENT_NAME = "SearchModeResidue";
    private static final int SEARCH_ALL_ORDINAL = 3;
    private static final String CONFIRM_BEFORE_SEQ_CHANGE = "Confirm before sequence change";
    private static final boolean default_confirm_before_seq_change = true;
    private static final String SEPARATOR = "\\|";
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("searchmoderesidue");
    private static final int MAX_RESIDUE_LEN_SEARCH = 1000000;
    private static final GenometryModel gmodel = GenometryModel.getInstance();
    private static final String ESCAPE_BEGIN = "\\Q";
    private static final String ESCAPE_END = "\\E";
    private static final Map<Character,String> SPECIAL_NUCLEOTIDES;

    static{
        Map<Character,String> tempNucleotides= new HashMap<>();
        tempNucleotides.put('R',"[AG]");
        tempNucleotides.put('Y',"[CT]");
        tempNucleotides.put('S',"[GC]");
        tempNucleotides.put('W',"[AT]");
        tempNucleotides.put('K',"[GT]");
        tempNucleotides.put('M',"[AC]");
        tempNucleotides.put('B',"[CGT]");
        tempNucleotides.put('D',"[AGT]");
        tempNucleotides.put('H',"[ACT]");
        tempNucleotides.put('V',"[ACG]");
        tempNucleotides.put('N',"[ACGT]");
        SPECIAL_NUCLEOTIDES= Collections.unmodifiableMap(tempNucleotides);
    }
    private static final Color hitcolors[] = {
        Color.magenta,
        new Color(0x00cd00),
        Color.orange,
        new Color(0x00d7d7),
        new Color(0xb50000),
        Color.blue,
        Color.gray,
        Color.pink};//Distinct Colors for View/Print Ease

    private final List<GlyphI> glyphs = new ArrayList<>();
    private IgbService igbService;
    private int color = 0;
    private boolean optionSelected;

    private final Action createTrackAction = new AbstractAction("Create Track") {

        @Override
        public void actionPerformed(ActionEvent e) {
            final String trackId = GeneralUtils.URLEncode("SearchTrack://" + Calendar.getInstance().getTime().toString());
            CThreadWorker worker = new CThreadWorker("Creating track") {

                @Override
                protected Object runInBackground() {
                    for (GlyphI glyph : glyphs) {
                        SingletonSymWithProps info = (SingletonSymWithProps) glyph.getInfo();
                        if (info != null) {
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
                    try {
                        ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(trackId, "Search Track", null, null);
                        style.setColorProvider(new RGB());
                        style.setLabelField("match");
                        style.setSeparate(false);

                        DataSet feature = igbService.createFeature(trackId, new DummySymLoader(trackId, "Search Track", GenometryModel.getInstance().getSelectedGenomeVersion()));
                        igbService.loadAndDisplayAnnotations(feature);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(SearchModeResidue.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            };
            CThreadHolder.getInstance().execute(SearchModeResidue.this, worker);
        }
    };

    @Activate
    public void activate() {
        igbService.getSeqMapView().addToRefreshList(this);
        gmodel.addSeqSelectionListener(this);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Override
    public String checkInput(String search_text, final BioSeq vseq, final String seq) {
        if (vseq == null) {
            return MessageFormat.format(BUNDLE.getString("searchErrorNotLoaded"), seq);
        }
        if (search_text.isEmpty()) {
            return BUNDLE.getString("searchErrorShort");
        }
        try {
            for (String st : search_text.split(SEPARATOR)) {
                Pattern.compile(st, Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException pse) {
            return MessageFormat.format(BUNDLE.getString("searchErrorSyntax"), pse.getMessage());
        } catch (Exception ex) {
            return MessageFormat.format(BUNDLE.getString("searchError"), ex.getMessage());
        }

        if (vseq != igbService.getSeqMapView().getAnnotatedSeq()) {
            boolean confirm = ModalUtils.confirmPanel(MessageFormat.format(BUNDLE.getString("searchSelectSeq"),
                    vseq.getId(), vseq.getId()), CONFIRM_BEFORE_SEQ_CHANGE, default_confirm_before_seq_change);
            if (!confirm) {
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

    public void clear() {
        clearResults();
    }

    @Override
    public void mapRefresh() {
        igbService.mapRefresh(glyphs);
    }

    @Override
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
        return "";
    }

    @Override
    public String getOptionTooltip() {
        return "";
    }

    @Override
    public boolean getOptionEnable() {
        return hitcolors.length - 1 > color;
    }

    @Override
    public void setOptionState(boolean selected) {
        optionSelected = selected;
    }

    @Override
    public boolean getOptionState() {
        return optionSelected;
    }

    public void valueChanged(GlyphI glyph, String seq) {
        for (GlyphI g : glyphs) {
            igbService.getSeqMap().deselect(g);
        }
        if (glyph != null) {
            int start = (int) glyph.getCoordBox().x;
            int end = (int) (glyph.getCoordBox().x + glyph.getCoordBox().width);
            igbService.getSeqMap().clearSelected();
            igbService.getSeqMap().select(glyph);
            igbService.zoomToCoord(seq, start, end);
            igbService.getSeqMapView().centerAtHairline();
            igbService.getSeqMapView().select(glyph);

        }
    }

    @Override
    public boolean useGenomeInSeqList() {
        return false;
    }

    public SearchResults<GlyphI> searchResidue(String search_text, final BioSeq chrFilter, IStatus statusHolder) {
        String[] search_terms = search_text.split(SEPARATOR);
        StringBuilder searchSummary = new StringBuilder();
        for (String st : search_terms) {
            st = st.trim();
            if (st.length() > 0) {
                SearchResults<GlyphI> results = search(st, chrFilter, statusHolder);
                searchSummary.append(st).append(" : ").append(results.getSearchSummary()).append(" ");
            } else {
                searchSummary.append(st).append(" : ").append("Search skipped because character length is 0.");
            }
        }
        return new SearchResults<>(getName(), search_text, chrFilter.getId(), searchSummary.toString(), glyphs);
    }

    private SearchResults<GlyphI> search(String search_text, final BioSeq chrFilter, IStatus statusHolder) {
        SeqSpan visibleSpan = igbService.getSeqMapView().getVisibleSpan();
        GenericAction loadResidue = igbService.loadResidueAction(visibleSpan, true);
        loadResidue.actionPerformed(null);

//		boolean isComplete = chrFilter.isComplete();
//		if (!isComplete) {
//			igbService.loadResidues(igbService.getSeqMapView().getVisibleSpan(), true);
//		}
        String friendlySearchStr = MessageFormat.format(BUNDLE.getString("friendlyPattern"), search_text, chrFilter.getId());
        Pattern regex = null;
        try {
            regex = Pattern.compile(processSpecialNucleotides(search_text), Pattern.CASE_INSENSITIVE);
        } catch (Exception ex) { // should not happen already checked above
            return new SearchResults<>(getName(), search_text, chrFilter.getId(), ex.getLocalizedMessage(), null);
        }

        statusHolder.setStatus(friendlySearchStr);

        int residuesLength = chrFilter.getLength();
        int hit_count1 = 0;
        int hit_count2 = 0;
        int residue_offset1 = chrFilter.getMin();
        int residue_offset2 = chrFilter.getMax();
        Thread current_thread = Thread.currentThread();

        for (int i = visibleSpan.getMin(); i < visibleSpan.getMax(); i += MAX_RESIDUE_LEN_SEARCH) {
            if (current_thread.isInterrupted()) {
                break;
            }

            int start = Math.max(i - search_text.length(), 0);
            int end = Math.min(i + MAX_RESIDUE_LEN_SEARCH, residuesLength);

            String residues = chrFilter.getResidues(start, end);
            List<GlyphI> results = igbService.getSeqMapView().searchForRegexInResidues(true, regex, residues, Math.max(residue_offset1, start), hitcolors[color % hitcolors.length]);
            hit_count1 += results.size();
            glyphs.addAll(results);

            // Search for reverse complement of query string
            // flip searchstring around, and redo nibseq search...
            String rev_searchstring = DNAUtils.reverseComplement(residues);
            results = igbService.getSeqMapView().searchForRegexInResidues(false, regex, rev_searchstring, Math.min(residue_offset2, end), hitcolors[color % hitcolors.length]);
            hit_count2 += results.size();
            glyphs.addAll(results);
        }
        String statusStr = MessageFormat.format(BUNDLE.getString("searchSummary"), hit_count1, hit_count2);
        statusHolder.setStatus(statusStr);
        igbService.getSeqMap().updateWidget();

        Collections.sort(glyphs, new Comparator<GlyphI>() {
            @Override
            public int compare(GlyphI g1, GlyphI g2) {
                return Integer.valueOf((int) g1.getCoordBox().x).compareTo((int) g2.getCoordBox().x);
            }
        });
        color++;
        return new SearchResults<>(getName(), search_text, chrFilter.getId(), statusStr, glyphs);
    }

    private String processSpecialNucleotides(String searchText) {
        Set<Integer> beginEscapeIndices = getIndices(searchText,ESCAPE_BEGIN);
        StringBuilder processedSearchText = new StringBuilder();
        int i;
        char curr;
        for(i=0;i<searchText.length();i++){
            curr = searchText.charAt(i);
           if(beginEscapeIndices.contains(i)){
               int escapeEndIndex = getEscapeEndIndex(searchText,i);
               processedSearchText.append(searchText.substring(i,escapeEndIndex+1));
               i=escapeEndIndex;
           }else{
              processedSearchText.append(SPECIAL_NUCLEOTIDES.getOrDefault(Character.toUpperCase(curr),String.valueOf(curr)));
           }
        }
        return processedSearchText.toString();
    }
    private int getEscapeEndIndex(String searchText,int fromIndex){
        int index=searchText.indexOf(ESCAPE_END,fromIndex);
        return index==-1?searchText.length()-1:index+1;
    }
    private Set<Integer> getIndices(String searchText, String escapeLiteral){
        Set<Integer> indices = new HashSet<>();
        int fromIdx=0;
        while(fromIdx<searchText.length()){
            int idx = searchText.indexOf(escapeLiteral,fromIdx);
            if(idx==-1) return indices;
            indices.add(idx);
            fromIdx=idx+1;
        }
        return indices;
    }


    @Override
    public Action getCustomAction() {
        return createTrackAction;
    }

    static class DummySymLoader extends QuickLoadSymLoader {

        private static final List<LoadUtils.LoadStrategy> strategyList = new ArrayList<>();

        static {
            strategyList.add(LoadUtils.LoadStrategy.NO_LOAD);
            strategyList.add(LoadUtils.LoadStrategy.VISIBLE);

        }

        public DummySymLoader(String trackId, String featureName, GenomeVersion genomeVersion) throws URISyntaxException {
            super(new URI(trackId), Optional.empty(), featureName, genomeVersion);
        }

        @Override
        public List<LoadUtils.LoadStrategy> getLoadChoices() {
            return strategyList;
        }
    }
}
