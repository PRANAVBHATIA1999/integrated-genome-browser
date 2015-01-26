package com.affymetrix.sequenceviewer;

import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.event.GenericActionDoneCallback;
import com.affymetrix.genometry.thread.CThreadWorker;
import com.affymetrix.igb.shared.LoadResidueAction;

public class SequenceViewWorker extends CThreadWorker<Object, Void> {

    private SeqSpan span;
    private GenericActionDoneCallback doneback;

    public SequenceViewWorker(String msg, SeqSpan span, GenericActionDoneCallback doneback) {
        super(msg);
        this.span = span;
        this.doneback = doneback;
    }

    @Override
    protected Object runInBackground() {
        LoadResidueAction loadResidue = new LoadResidueAction(span, true);
        loadResidue.addDoneCallback(doneback);
        loadResidue.actionPerformed(null);
        loadResidue.removeDoneCallback(doneback);
        return null;
    }

    @Override
    protected void finished() {
    }
}
