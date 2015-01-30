package com.affymetrix.igb.external;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;

import com.affymetrix.igb.swing.JRPComboBox;
import com.affymetrix.igb.service.api.IgbService;
import com.affymetrix.igb.service.api.IgbTabPanel;
import com.affymetrix.igb.service.api.IgbTabPanelI;

/**
 * Container panel for the external views Shows up as tab in IGB Allows
 * selection of subviews with combobox
 *
 * The mappings for ensembl are defined in ensemblURLs tab delimited text file
 *
 * @author Ido M. Tamir
 */
@Component(name = ExternalViewer.COMPONENT_NAME, provide = IgbTabPanelI.class, immediate = true)
public class ExternalViewer extends IgbTabPanel implements ItemListener {

    public static final String COMPONENT_NAME = "ExternalViewer";
    private static final long serialVersionUID = 1L;
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("external");
    private static final int TAB_POSITION = 6;

    private static final String[] names = {UCSCView.viewName, EnsemblView.viewName};
    private JRPComboBox ucscBox;
    private JRPComboBox ensemblBox;
    private IgbService igbService;
    private UCSCViewAction ucscViewAction;

    public ExternalViewer() {
        super(BUNDLE.getString("externalViewTab"), BUNDLE.getString("externalViewTab"), BUNDLE.getString("externalViewTooltip"), false, TAB_POSITION);
    }

    @Activate
    public void activate() {
        this.setLayout(new CardLayout());
        ucscBox = createBox("ExternalViewer_ucsc");
        ensemblBox = createBox("ExternalViewer_ensemble");
        final UCSCView ucsc = new UCSCView(ucscBox, igbService, ucscViewAction);
        add(ucsc, ucsc.getViewName());
        final EnsemblView ensembl = new EnsemblView(ensemblBox, igbService, ucscViewAction);
        add(ensembl, ensembl.getViewName());
    }

    @Reference(optional = false)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Reference(optional = false)
    public void setUcscViewAction(UCSCViewAction ucscViewAction) {
        this.ucscViewAction = ucscViewAction;
    }

    private JRPComboBox createBox(String id) {
        JRPComboBox box = new JRPComboBox(id, names);
        box.setPrototypeDisplayValue("ENSEMBL");
        box.setMaximumSize(box.getPreferredSize());
        box.setEditable(false);
        box.addItemListener(this);
        return box;
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    @Override
    public boolean isCheckMinimumWindowSize() {
        return true;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getID() == ItemEvent.ITEM_STATE_CHANGED) {
            CardLayout cl = (CardLayout) getLayout();
            if (e.getSource() == ucscBox) {
                ensemblBox.setSelectedItem(EnsemblView.viewName);
            }
            if (e.getSource() == ensemblBox) {
                ucscBox.setSelectedItem(UCSCView.viewName);
            }
            cl.show(ExternalViewer.this, (String) e.getItem());
        }
    }

}
