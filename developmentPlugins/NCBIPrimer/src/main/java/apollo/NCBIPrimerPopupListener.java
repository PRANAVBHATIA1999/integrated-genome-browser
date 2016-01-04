package apollo;

import apollo.action.PrimerSearchAction;
import apollo.analysis.NCBIPrimerBlastOpts;
import com.affymetrix.genometry.event.ContextualPopupListener;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import org.lorainelab.igb.igb.genoviz.extensions.SeqMapViewI;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author hiralv
 */
public class NCBIPrimerPopupListener implements ContextualPopupListener {

    private final SeqMapViewI smv;
    private final NCBIPrimerBlastOpts ncbiPrimerBlastOpts;

    public NCBIPrimerPopupListener(SeqMapViewI smv, NCBIPrimerBlastOpts ncbiPrimerBlastOpts) {
        this.smv = smv;
        this.ncbiPrimerBlastOpts = ncbiPrimerBlastOpts;
    }

    @Override
    public void popupNotify(JPopupMenu popup, List<SeqSymmetry> selected_items, SeqSymmetry primary_sym) {
        if (!selected_items.isEmpty() && !(selected_items.get(0) instanceof GraphSym)) {
            JMenuItem remote_ncbi_primer_action = new JMenuItem(new PrimerSearchAction(smv, ncbiPrimerBlastOpts));
            popup.add(remote_ncbi_primer_action, 18);

        }
    }
}
