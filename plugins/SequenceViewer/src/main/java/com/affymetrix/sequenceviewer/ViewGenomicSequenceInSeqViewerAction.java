package com.affymetrix.sequenceviewer;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.SymSelectionEvent;
import com.affymetrix.genometry.event.SymSelectionListener;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.util.ErrorHandler;

import com.lorainelab.igb.services.IgbService;

public class ViewGenomicSequenceInSeqViewerAction extends GenericAction implements SymSelectionListener {

    private static final long serialVersionUID = 1l;
    private IgbService igbService;

    public ViewGenomicSequenceInSeqViewerAction(IgbService igbService) {
        super(AbstractSequenceViewer.BUNDLE.getString("ViewGenomicSequenceInSeqViewer"), null, "16x16/actions/Sequence_Viewer.png", "22x22/actions/Sequence_Viewer.png", KeyEvent.VK_UNDEFINED, null, false);
        GenometryModel.getInstance().addSymSelectionListener(this);
        setEnabled(false);
        this.igbService = igbService;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        try {
            DefaultSequenceViewer sv = new DefaultSequenceViewer(igbService);
            sv.startSequenceViewer();
        } catch (Exception ex) {
            ErrorHandler.errorPanel("Problem occured in copying sequences to sequence viewer", ex, Level.WARNING);
        }
    }

    public void symSelectionChanged(SymSelectionEvent evt) {
        if ((evt.getSelectedGraphSyms().isEmpty() && igbService.getSeqMapView().getSeqSymmetry() == null)
                || (!evt.getSelectedGraphSyms().isEmpty() && evt.getSelectedGraphSyms().get(0) instanceof GraphSym)) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }
}
