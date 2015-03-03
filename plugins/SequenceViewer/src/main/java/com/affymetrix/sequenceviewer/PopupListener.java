package com.affymetrix.sequenceviewer;

import com.affymetrix.genometry.event.AxisPopupListener;
import com.affymetrix.genometry.event.ContextualPopupListener;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author hiralv
 */
public class PopupListener implements ContextualPopupListener, AxisPopupListener {

    JMenuItem genomicSequenceViewer, readSequenceViewer;

    PopupListener(GenericAction genomicSequenceAction, GenericAction readSequencAction) {
        this.genomicSequenceViewer = new JMenuItem(genomicSequenceAction);
        this.readSequenceViewer = new JMenuItem(readSequencAction);
    }

    @Override
    public void popupNotify(JPopupMenu popup, List<SeqSymmetry> selected_syms, SeqSymmetry primary_sym) {
        if (!selected_syms.isEmpty() && !(selected_syms.get(0) instanceof GraphSym)) {

            popup.add(genomicSequenceViewer, 8);
            popup.add(readSequenceViewer, 10);

        }
    }

    @Override
    public void addPopup(JPopupMenu popup) {
        popup.add(genomicSequenceViewer);
    }

}
