package com.affymetrix.genometry.symmetry;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.MutableSeqSpan;
import com.affymetrix.genometry.SeqSpan;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author hiralv
 */
//
public abstract class BasicSeqSymmetry implements SeqSpan, TypedSym, SymWithProps {

    protected BioSeq seq; // "chrom"
    protected int txMin;
    protected int txMax;
    protected String name; // "name"
    protected boolean forward; // "strand"
    protected int[] blockMins; // "blockStarts" + "txMin"
    protected int[] blockMaxs; // "blockStarts" + "txMin" + "blockSizes"
    protected String type;
    protected Map<String, Object> props;

    public BasicSeqSymmetry(String type, BioSeq seq, int txMin, int txMax, String name,
            boolean forward, int[] blockMins, int[] blockMaxs) {
        this.type = type;
        this.seq = seq;  // replace chrom name-string with reference to chrom BioSeq
        this.txMin = txMin;
        this.txMax = txMax;
        this.name = name;
        this.forward = forward;
        this.blockMins = blockMins;
        this.blockMaxs = blockMaxs;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getID() {
        return name;
    }

    public SeqSpan getSpan(BioSeq bs) {
        if (bs.equals(this.seq)) {
            return this;
        } else {
            return null;
        }
    }

    public SeqSpan getSpan(int index) {
        if (index == 0) {
            return this;
        } else {
            return null;
        }
    }

    public boolean getSpan(BioSeq bs, MutableSeqSpan span) {
        if (bs.equals(this.seq)) {
            if (forward) {
                span.set(txMin, txMax, seq);
            } else {
                span.set(txMax, txMin, seq);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean getSpan(int index, MutableSeqSpan span) {
        if (index == 0) {
            if (forward) {
                span.set(txMin, txMax, seq);
            } else {
                span.set(txMax, txMin, seq);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Always returns 1.
     */
    public int getSpanCount() {
        return 1;
    }

    /**
     * Returns null if index is not 1.
     */
    public BioSeq getSpanSeq(int index) {
        if (index == 0) {
            return seq;
        } else {
            return null;
        }
    }

    public int getChildCount() {
        if (blockMins == null) {
            return 0;
        } else {
            return blockMins.length;
        }
    }

    // SeqSpan implementation
    public int getStart() {
        return (forward ? txMin : txMax);
    }

    public int getEnd() {
        return (forward ? txMax : txMin);
    }

    public int getMin() {
        return txMin;
    }

    public int getMax() {
        return txMax;
    }

    public int getLength() {
        return (txMax - txMin);
    }

    public boolean isForward() {
        return forward;
    }

    public BioSeq getBioSeq() {
        return seq;
    }

    public double getStartDouble() {
        return getStart();
    }

    public double getEndDouble() {
        return getEnd();
    }

    public double getMaxDouble() {
        return getMax();
    }

    public double getMinDouble() {
        return getMin();
    }

    public double getLengthDouble() {
        return getLength();
    }

    public boolean isIntegral() {
        return true;
    }

    public Map<String, Object> getProperties() {
        return cloneProperties();
    }

    public Map<String, Object> cloneProperties() {
        HashMap<String, Object> tprops = new HashMap<>();
        tprops.put("id", name);
        tprops.put("type", type);
        tprops.put("name", name);
        tprops.put("seq id", seq.getId());
        tprops.put("forward", forward);
        if (props != null) {
            tprops.putAll(props);
        }
        return tprops;
    }

    public Object getProperty(String key) {
        // test for standard gene sym  props
        if (key.equals("id")) {
            return name;
        } else if (key.equals("type")) {
            return getType();
        } else if (key.equals("method")) {
            return getType();
        } else if (key.equals("name")) {
            return name;
        } else if (key.equals("seq id")) {
            return seq.getId();
        } else if (key.equals("forward")) {
            return forward;
        } else if (props != null) {
            return props.get(key);
        } else {
            return null;
        }
    }

    public boolean setProperty(String name, Object val) {
        if (props == null) {
            props = new Hashtable<>();
        }
        props.put(name, val);
        return true;
    }

    public int[] getBlockMins() {
        return blockMins;
    }

    public int[] getBlockMaxs() {
        return blockMaxs;
    }

    private void outputBasicFormat(DataOutputStream out) throws IOException {
        out.write(seq.getId().getBytes());
        out.write('\t');
        out.write(Integer.toString(txMin).getBytes());
        out.write('\t');
        out.write(Integer.toString(txMax).getBytes());
		// only first three fields are required

        // only keep going if has name
        if (name != null) {
            out.write('\t');
            out.write(getName().getBytes());
            out.write('\t');
            if (isForward()) {
                out.write('+');
            } else {
                out.write('-');
            }
            out.write('\t');
            int child_count = this.getChildCount();
            if (child_count > 0) {
                out.write('\t');
                // writing out extra "reserved" field, which currently should always be 0
                out.write('0');
                out.write('\t');
                out.write(Integer.toString(child_count).getBytes());
                out.write('\t');
                // writing blocksizes
                for (int i = 0; i < child_count; i++) {
                    out.write(Integer.toString(blockMaxs[i] - blockMins[i]).getBytes());
                    out.write(',');
                }
                out.write('\t');
                // writing blockstarts
                for (int i = 0; i < child_count; i++) {
                    out.write(Integer.toString(blockMins[i] - txMin).getBytes());
                    out.write(',');
                }
            }
        }
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            outputBasicFormat(new DataOutputStream(baos));
        } catch (IOException x) {
            return x.getMessage();
        }
        return baos.toString();
    }
}
