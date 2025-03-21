/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.external;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.SeqSelectionEvent;
import com.affymetrix.genometry.event.SeqSelectionListener;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import static com.affymetrix.igb.external.ExternalViewer.BUNDLE;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.lorainelab.igb.menu.api.MenuItemEventService;
import org.lorainelab.igb.menu.api.model.MenuBarMenuItemEvent;
import org.lorainelab.igb.menu.api.model.MenuBarParentMenu;
import org.lorainelab.igb.menu.api.model.MenuIcon;
import org.lorainelab.igb.menu.api.model.MenuItem;
import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.synonymlookup.services.GenomeVersionSynonymLookup;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;

/**
 *
 * @author sgblanch
 * @version $Id: UCSCViewAction.java 7258 2010-12-17 21:40:02Z lfrohman $
 */
@Component(name = UCSCViewAction.COMPONENT_NAME, service = {UCSCViewAction.class, MenuBarEntryProvider.class}, immediate = true)
public class UCSCViewAction extends GenericAction implements SeqSelectionListener, MenuBarEntryProvider {

    private static final Logger LOG = LoggerFactory.getLogger(UCSCViewAction.class);
    public static final String COMPONENT_NAME = "UCSCViewAction";
    private static final int MENU_WEIGHT = 75;
    private static final long serialVersionUID = 1L;
    protected static final String UCSC_JSON_ENDPOINT = "https://api.genome.ucsc.edu/list/ucscGenomes"; 
    private static final String UCSC_URL = "https://genome.ucsc.edu/cgi-bin/hgTracks?";
    private static final Set<String> UCSCSources = Collections.synchronizedSet(new HashSet<>());
    private IgbService igbService;
    private GenomeVersionSynonymLookup genomeVersionSynonymLookup;
    private MenuItemEventService menuItemEventService;

    public UCSCViewAction() {
        super(BUNDLE.getString("viewRegionInUCSCBrowser"), "16x16/actions/system-search.png", "22x22/actions/system-search.png");
        setKeyStrokeBinding("ctrl U");
        menuItem = new MenuItem(BUNDLE.getString("viewRegionInUCSCBrowser"), (Void t) -> {
            actionPerformed(null);
            return t;
        });
    }

    @Activate
    public void activate() {
        GenometryModel model = GenometryModel.getInstance();
        model.addSeqSelectionListener(this);
        final Optional<BioSeq> selectedSeq = model.getSelectedSeq();
        this.seqSelectionChanged(new SeqSelectionEvent(this, Collections.singletonList(selectedSeq.orElse(null))));
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Reference
    public void setMenuItemEventService(MenuItemEventService menuItemEventService) {
        this.menuItemEventService = menuItemEventService;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        super.actionPerformed(ae);
        String query = getUCSCQuery();

        if (!query.isEmpty()) {
            GeneralUtils.browse(UCSC_URL + query);
        } else {
            ErrorHandler.errorPanel("Unable to map genome '" + igbService.getSeqMapView().getAnnotatedSeq().getGenomeVersion().getName() + "' to a UCSC genome.");
        }
    }

    @Override
    public void seqSelectionChanged(SeqSelectionEvent evt) {
        boolean isSeqSelected = evt.getSelectedSeq() != null;
        // don't do the enabling tests, because it will contact the UCSC server when it's not truly necessary.
        this.setEnabled(isSeqSelected);
        menuItem.setEnabled(isSeqSelected);
        MenuBarMenuItemEvent menuItemEvent = new MenuBarMenuItemEvent(menuItem, MenuBarParentMenu.VIEW);
        menuItemEventService.getEventBus().post(menuItemEvent);
    }

    /**
     * Returns the genome UcscVersion in UCSC two-letter plus number format,
     * like "hg17".
     */
    protected String getUcscGenomeVersion(String version) {
        try {
            initUCSCSources();
        }
        catch (IOException ex) {
            LOG.error("Unable to look up UCSC genome name");
            return "";
        }
        String ucsc_version = genomeVersionSynonymLookup.findMatchingSynonym(UCSCSources, version);
        return UCSCSources.contains(ucsc_version) ? ucsc_version : "";
    }
    

    private void initUCSCSources() throws IOException {
        synchronized (UCSCSources) {
            if (UCSCSources.isEmpty()) {
                UCSCSources.addAll(getUcscGenomeNamesFromJsonEndpoint());
                
                /**
                Optional<DataProvider> dasDataProvider = igbService.getAllServersList().stream().filter(dataProvider -> dataProvider.getUrl().equals(UCSC_DAS_URL)).findFirst();
                if (dasDataProvider.isPresent()) {
                    Set<String> supportedGenomeVersionNames = dasDataProvider.get().getSupportedGenomeVersionNames();
                    UCSCSources.addAll(supportedGenomeVersionNames);
                }
                */

            }
        }
    }

    /**
     * generates part of UCSC query url for current genome coordinates.
     *
     * @return query URL for current view. "" on error.
     */
    public String getUCSCQuery() {
        BioSeq aseq = igbService.getSeqMapView().getAnnotatedSeq();

        if (aseq == null) {
            return "";
        }

        String UcscVersion = getUcscGenomeVersion(aseq.getGenomeVersion().getName());
        if (!UcscVersion.isEmpty()) {
            return "db=" + UcscVersion + "&position=" + getRegionString();
        }

        return "";
    }

    /**
     * Returns the current position in the format used by the UCSC browser. This
     * format is also understood by GBrowse and the MapRangeBox of IGB.
     *
     * @return a String such as "chr22:15916196-31832390", or null.
     */
    private String getRegionString() {
        SeqSpan span = igbService.getSeqMapView().getVisibleSpan();
        String chromosomeName = span.getBioSeq().toString();
        if (!chromosomeName.startsWith("chr")) {
            chromosomeName = "chr" + chromosomeName;
        }
        return chromosomeName + ":" + span.getMin() + "-" + span.getMax();
    }

    @Override
    public Optional<List<MenuItem>> getMenuItems() {
        try (InputStream resourceAsStream = UCSCViewAction.class.getClassLoader().getResourceAsStream(UCSC_VIEW_ACTION_ICON)) {
            menuItem.setMenuIcon(new MenuIcon(resourceAsStream));
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        menuItem.setWeight(MENU_WEIGHT);
        return Optional.of(Arrays.asList(menuItem));
    }
    private MenuItem menuItem;
    private static final String UCSC_VIEW_ACTION_ICON = "system-search.png";

    @Override
    public MenuBarParentMenu getMenuExtensionParent() {
        return MenuBarParentMenu.VIEW;
    }

    @Reference
    public void setGenomeVersionSynonymLookup(GenomeVersionSynonymLookup genomeVersionSynonymLookup) {
        this.genomeVersionSynonymLookup = genomeVersionSynonymLookup;
    }

    protected static Collection<? extends String> getUcscGenomeNamesFromJsonEndpoint() throws MalformedURLException, IOException {
        URL url = new URL(UCSC_JSON_ENDPOINT);
        String data = Resources.toString(url, Charsets.UTF_8);
        Map <String, Object> map = new Gson().fromJson(
                data, new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );
        Map submap = (Map) map.get("ucscGenomes");
        Set genome_names = submap.keySet();
        return genome_names;
     }
}
