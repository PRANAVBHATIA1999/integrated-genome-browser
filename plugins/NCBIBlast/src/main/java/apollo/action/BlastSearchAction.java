package apollo.action;

import apollo.analysis.RemoteBlastNCBI;
import apollo.datamodel.Sequence;
import apollo.datamodel.StrandedFeatureSet;
import apollo.datamodel.StrandedFeatureSetI;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.GenericActionDoneCallback;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.thread.CThreadHolder;
import com.affymetrix.genometry.util.ErrorHandler;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.igb.shared.SequenceLoader;
import org.lorainelab.igb.genoviz.extensions.SeqMapViewI;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author hiralv
 */
public abstract class BlastSearchAction extends GenericAction {

    private static final long serialVersionUID = 1L;

    protected final SeqMapViewI smv;
    protected final RemoteBlastNCBI.BlastType blastType;

    public BlastSearchAction(SeqMapViewI smv, RemoteBlastNCBI.BlastType blastType) {
        super(blastType.toString().toUpperCase() + " nr protein database", "16x16/actions/ncbi.png", null);
        this.smv = smv;
        this.blastType = blastType;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        try {
            final SeqSymmetry residues_sym = getResidueSym();
            final BioSeq aseq = smv.getAnnotatedSeq();
            final SeqSpan span = residues_sym.getSpan(aseq);

            final GenericActionDoneCallback doneback = action -> {
                try {
                    StrandedFeatureSetI sf = new StrandedFeatureSet();
                    String id = MessageFormat.format("{0} {1}:{2}-{3} {4} {5} [{6}]",
                            residues_sym.getID(), aseq, span.getStart(), span.getEnd(), span.isForward() ? "+" : "-", aseq.getGenomeVersion().getName(), aseq.getGenomeVersion().getSpeciesName());
                    Sequence seq = new Sequence(id, getSequence(residues_sym));

                    RemoteBlastNCBI blast = new RemoteBlastNCBI(blastType, new RemoteBlastNCBI.BlastOptions());
                    String url = blast.runAnalysis(sf, seq, 1);

                    GeneralUtils.browse(url);
                } catch (Exception ex) {
                    ArrayList<Throwable> exs = new ArrayList<>(1);
                    exs.add(ex);
                    ErrorHandler.errorPanel("Error", ex.getMessage(), exs, Level.SEVERE);
                }
            };

            SequenceLoader worker = new SequenceLoader("RemoteBlastNCBI", residues_sym.getSpan(aseq), doneback);
            CThreadHolder.getInstance().execute(this, worker);
        } catch (Exception ex) {
            ErrorHandler.errorPanel("Problem occured in copying sequences to sequence viewer", ex, Level.WARNING);
        }
    }

    protected abstract SeqSymmetry getResidueSym();

    public abstract String getSequence(SeqSymmetry residues_sym);
}
