package com.affymetrix.genometry.span;

import com.affymetrix.genometry.SeqSpan;
import java.util.Comparator;

public class SeqSpanComparator implements Comparator<SeqSpan> {

    @Override
    public int compare(SeqSpan ss1, SeqSpan ss2) {
        int result = ss1.getBioSeq().getId().compareTo(ss2.getBioSeq().getId());
        if (result == 0) {
            result = (ss1.isForward() ? 0 : 1) - (ss2.isForward() ? 0 : 1);
        }
        if (result == 0) {
            result = ss1.isForward() ? (ss1.getStart() - ss2.getStart()) : (ss2.getStart() - ss1.getStart());
        }
        if (result == 0) {
            result = ss1.isForward() ? (ss1.getEnd() - ss2.getEnd()) : (ss2.getEnd() - ss1.getEnd());
        }
        return result;
    }
}
