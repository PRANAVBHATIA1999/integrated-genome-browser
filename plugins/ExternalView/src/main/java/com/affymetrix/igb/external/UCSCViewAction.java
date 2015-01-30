/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.external;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.genometry.general.GenericServer;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.SeqSelectionEvent;
import com.affymetrix.genometry.event.SeqSelectionListener;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.SynonymLookup;
import com.affymetrix.genometry.das.DasServerInfo;
import com.affymetrix.genoviz.swing.AMenuItem;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.service.api.IgbService;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.affymetrix.igb.external.ExternalViewer.BUNDLE;
import com.affymetrix.igb.swing.JRPMenuItem;
import org.osgi.framework.BundleContext;

/**
 *
 * @author sgblanch
 * @version $Id: UCSCViewAction.java 7258 2010-12-17 21:40:02Z lfrohman $
 */
@Component(name = UCSCViewAction.COMPONENT_NAME, provide = UCSCViewAction.class, immediate = true)
public class UCSCViewAction extends GenericAction implements SeqSelectionListener {

    public static final String COMPONENT_NAME = "UCSCViewAction";
    private static final int VIEW_MENU_POS = 3;
    private static final long serialVersionUID = 1l;
    private static final String UCSC_DAS_URL = "http://genome.cse.ucsc.edu/cgi-bin/das/dsn";
    private static final String UCSC_URL = "http://genome.ucsc.edu/cgi-bin/hgTracks?";
    private static final SynonymLookup LOOKUP = SynonymLookup.getDefaultLookup();
    private static final Set<String> UCSCSources = Collections.synchronizedSet(new HashSet<>());
    private IgbService igbService;

    public UCSCViewAction() {
        super(BUNDLE.getString("viewRegionInUCSCBrowser"), "16x16/actions/system-search.png", "22x22/actions/system-search.png");
    }

    @Activate
    public void activate(BundleContext context) {
        GenometryModel model = GenometryModel.getInstance();
        model.addSeqSelectionListener(this);
        this.seqSelectionChanged(new SeqSelectionEvent(this, Collections.singletonList(model.getSelectedSeq())));
        JRPMenuItem menuItem = new JRPMenuItem("ExternalViewer_ucscView", this);
        context.registerService(AMenuItem.class, new AMenuItem(menuItem, "view", VIEW_MENU_POS), null);
    }

    @Reference(optional = false)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        super.actionPerformed(ae);
        String query = getUCSCQuery();

        if (!query.isEmpty()) {
            GeneralUtils.browse(UCSC_URL + query);
        } else {
            ErrorHandler.errorPanel("Unable to map genome '" + igbService.getSeqMapView().getAnnotatedSeq().getSeqGroup().getID() + "' to a UCSC genome.");
        }
    }

    public void seqSelectionChanged(SeqSelectionEvent evt) {
        boolean enableThis = evt.getSelectedSeq() != null;
        // don't do the enabling tests, because it will contact the UCSC server when it's not truly necessary.
        this.setEnabled(enableThis);
    }

    /**
     * Returns the genome UcscVersion in UCSC two-letter plus number format,
     * like "hg17".
     */
    private String getUcscGenomeVersion(String version) {
        initUCSCSources();
        String ucsc_version = LOOKUP.findMatchingSynonym(UCSCSources, version);
        return UCSCSources.contains(ucsc_version) ? ucsc_version : "";
    }

    private void initUCSCSources() {
        synchronized (UCSCSources) {
            if (UCSCSources.isEmpty()) {
                // Get the sources from the UCSC server.  If the server has already been initialized, get from there.
                // This is done to avoid additional slow DAS queries.
                DasServerInfo ucsc = null;
                GenericServer server = null;
                if ((server = igbService.getServer(UCSC_DAS_URL)) != null) {
                    // UCSC server already exists!
                    ucsc = (DasServerInfo) server.serverObj;
                } else {
                    ucsc = new DasServerInfo(UCSC_DAS_URL);
                }
                UCSCSources.addAll(ucsc.getDataSources().keySet());
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

        String UcscVersion = getUcscGenomeVersion(aseq.getSeqGroup().getID());
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
        return span.getBioSeq() + ":" + span.getMin() + "-" + span.getMax();
    }
}
