package com.affymetrix.sequenceviewer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.SymSelectionEvent;
import com.affymetrix.genometry.event.SymSelectionListener;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.util.ErrorHandler;
import com.affymetrix.igb.swing.JRPMenuItem;
import org.lorainelab.igb.igb.services.IgbService;
import org.lorainelab.igb.igb.services.window.menus.IgbMenuItemProvider;
import org.lorainelab.igb.igb.services.window.menus.IgbToolBarParentMenu;
import org.lorainelab.igb.image.exporter.service.ImageExportService;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

@Component(name = ViewGenomicSequenceInSeqViewerAction.COMPONENT_NAME, immediate = true, provide = {GenericAction.class, IgbMenuItemProvider.class})
public class ViewGenomicSequenceInSeqViewerAction extends GenericAction implements SymSelectionListener, IgbMenuItemProvider {

    public static final String COMPONENT_NAME = "ViewGenomicSequenceInSeqViewerAction";
    private static final long serialVersionUID = 1L;
    private IgbService igbService;
    private ImageExportService imageExportService;

    public ViewGenomicSequenceInSeqViewerAction() {
        super(AbstractSequenceViewer.BUNDLE.getString("ViewGenomicSequenceInSeqViewer"), null, "16x16/actions/Sequence_Viewer.png", "22x22/actions/Sequence_Viewer.png", KeyEvent.VK_UNDEFINED, null, false);
        setEnabled(false);
    }

    @Activate
    public void activate() {
        GenometryModel.getInstance().addSymSelectionListener(this);
    }

    @Reference
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            DefaultSequenceViewer sv = new DefaultSequenceViewer(igbService, imageExportService);
            sv.startSequenceViewer();
        } catch (Exception ex) {
            ErrorHandler.errorPanel("Problem occured in copying sequences to sequence viewer", ex, Level.WARNING);
        }
    }

    @Override
    public void symSelectionChanged(SymSelectionEvent evt) {
        if ((evt.getSelectedGraphSyms().isEmpty() && igbService.getSeqMapView().getSeqSymmetry() == null)
                || (!evt.getSelectedGraphSyms().isEmpty() && evt.getSelectedGraphSyms().get(0) instanceof GraphSym)) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }

    @Override
    public IgbToolBarParentMenu getParentMenu() {
        return IgbToolBarParentMenu.VIEW;
    }

    @Override
    public JRPMenuItem getMenuItem() {
        JRPMenuItem genomicSequenceMenuItem = new JRPMenuItem("SequenceViewer_viewGenomicSequenceInSeqViewer", this);
        return genomicSequenceMenuItem;
    }

    @Override
    public int getMenuItemWeight() {
        return 5;
    }

    @Reference
    public void setImageExportService(ImageExportService imageExportService) {
        this.imageExportService = imageExportService;
    }

}
