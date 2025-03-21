package apollo.action;

import apollo.analysis.RemoteBlastNCBI;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.symmetry.MutableSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.MutableSingletonSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SimpleMutableSeqSymmetry;
import com.affymetrix.genometry.util.SeqUtils;
import org.lorainelab.igb.genoviz.extensions.SeqMapViewI;
import java.util.List;

/**
 *
 * @author hiralv
 */
public class BlastXSearchAction extends BlastSearchAction {

    private static final long serialVersionUID = 1L;

    public BlastXSearchAction(SeqMapViewI smv) {
        super(smv, RemoteBlastNCBI.BlastType.blastx);
    }

    @Override
    protected SeqSymmetry getResidueSym() {
        SeqSymmetry residues_sym = null;
        List<SeqSymmetry> syms = smv.getSelectedSyms();
        if (syms.size() >= 1) {
            if (syms.size() == 1) {
                residues_sym = syms.get(0);
            } else {
                BioSeq aseq = smv.getAnnotatedSeq();
                if (syms.size() > 1 || smv.getSeqSymmetry() != null) {
                    MutableSeqSymmetry temp = new SimpleMutableSeqSymmetry();
                    SeqUtils.union(syms, temp, aseq);

                    // Make all reverse children forward
                    residues_sym = new SimpleMutableSeqSymmetry();
                    SeqSymmetry child;
                    SeqSpan childSpan;
                    for (int i = 0; i < temp.getChildCount(); i++) {
                        child = temp.getChild(i);
                        childSpan = child.getSpan(aseq);
                        ((MutableSeqSymmetry) residues_sym).addChild(new MutableSingletonSeqSymmetry(childSpan.getMin(), childSpan.getMax(), childSpan.getBioSeq()));
                    }
                    ((MutableSeqSymmetry) residues_sym).addSpan(temp.getSpan(aseq));
                }
            }
        } else {
            residues_sym = smv.getSeqSymmetry();
        }
        return residues_sym;
    }

    @Override
    public String getSequence(SeqSymmetry residues_sym) {
        return SeqUtils.getResidues(residues_sym, smv.getAnnotatedSeq());
    }

    @Override
    public boolean isEnabled() {
        List<SeqSymmetry> syms = smv.getSelectedSyms();
        if (syms.size() >= 1 || smv.getSeqSymmetry() != null) {
            return true;
        }
        return false;
    }
}
