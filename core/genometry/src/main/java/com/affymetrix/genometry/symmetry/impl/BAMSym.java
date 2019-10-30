package com.affymetrix.genometry.symmetry.impl;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.symmetry.BasicSeqSymmetry;
import com.affymetrix.genometry.symmetry.SymWithBaseQuality;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.AVERAGE_QUALITY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.FEATURE_TYPE;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.FORWARD;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.ID;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.MAPQ;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.RESIDUES;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.SCORES;
import com.affymetrix.genometry.util.SearchableCharIterator;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;

/**
 *
 * @author hiralv
 */
public class BAMSym extends BasicSeqSymmetry implements SymWithBaseQuality, SearchableCharIterator {

    public static final int NO_MAPQ = 255;

    private static final char DELETION_CHAR = '_';
    private static final char N_CHAR = '-';
    private static final char PADDING_CHAR = '*';
    private static final char ERROR_CHAR = '.';

    private final int[] iblockMins, iblockMaxs;
    private final int[] sblockMins, sblockMaxs;
    private final Cigar cigar;
    private final int min;
    private final String residues;
    private final String baseQuality;
    private final int mapq;
    private BitSet residueMask;
    private SeqSymmetry children[];
    private SeqSymmetry insChildren[];
    private SeqSymmetry softChildren[];
    private Integer averageQualityScore;
    //Should be made final
    private boolean readPairedFlag, firstOfPairFlag, secondOfPairFlag, duplicateReadFlag;
    private int flags;
    //1-based inclusive leftmost position of the clippped mate sequence, or 0 if there is no position.
    private int mateStart;
    private boolean mateUnMapped;
    //strand of the mate (false for forward; true for reverse strand).
    private boolean mateNegativeStrandFlag;

    //Residues residues;
    private String insResidues; 
    private String softResidues;
    
    public BAMSym(String type, BioSeq seq, int txMin, int txMax, String name,
            boolean forward, int[] blockMins, int[] blockMaxs, int iblockMins[],
            int[] iblockMaxs, Cigar cigar, String residues) {
        this(type, seq, txMin, txMax, name, NO_MAPQ, forward, blockMins,
                blockMaxs, iblockMins, iblockMaxs, cigar, residues, null);
    }
    
    public BAMSym(String type, BioSeq seq, int txMin, int txMax, String name,
            boolean forward, int[] blockMins, int[] blockMaxs, int iblockMins[],
            int[] iblockMaxs, Cigar cigar, String residues, int sblockMins[], int[] sblockMaxs) {
        this(type, seq, txMin, txMax, name, NO_MAPQ, forward, blockMins,
                blockMaxs, iblockMins, iblockMaxs, cigar, residues, null, sblockMins, sblockMaxs);
    }
    
    public BAMSym(String type, BioSeq seq, int txMin, int txMax, String name,
            int mapq, boolean forward, int[] blockMins, int[] blockMaxs,
            int iblockMins[], int[] iblockMaxs, Cigar cigar, String residues, String baseQuality) {
        super(type, seq, txMin, txMax, name, forward, blockMins, blockMaxs);
        this.iblockMins = iblockMins;
        this.iblockMaxs = iblockMaxs;
        this.cigar = cigar;
        this.residues = residues;
        this.baseQuality = baseQuality;
        this.min = Math.min(txMin, txMax);
        this.mapq = mapq;
        this.sblockMins = null;
        this.sblockMaxs = null;
    }
    
    public BAMSym(String type, BioSeq seq, int txMin, int txMax, String name,
            int mapq, boolean forward, int[] blockMins, int[] blockMaxs,
            int iblockMins[], int[] iblockMaxs, Cigar cigar, String residues, String baseQuality,
            int sblockMins[], int[] sblockMaxs) {
        super(type, seq, txMin, txMax, name, forward, blockMins, blockMaxs);
        this.iblockMins = iblockMins;
        this.iblockMaxs = iblockMaxs;
        this.cigar = cigar;
        this.residues = residues;
        this.baseQuality = baseQuality;
        this.min = Math.min(txMin, txMax);
        this.mapq = mapq;
        this.sblockMins = sblockMins;
        this.sblockMaxs = sblockMaxs;
    }

    public int getMapq() {
        return mapq;
    }

    public int getInsChildCount() {
        if (iblockMins == null) {
            return 0;
        } else {
            return iblockMins.length;
        }
    }
    
    public int getSoftChildCount() {
        if (sblockMins == null) {
            return 0;
        } else {
            return sblockMins.length;
        }
    }
    
    public SeqSymmetry getInsChild(int index) {
        if (iblockMins == null || (iblockMins.length <= index)) {
            return null;
        }
        if (insChildren == null) {
            insChildren = new SeqSymmetry[iblockMins.length];
        }

        if (insChildren[index] == null) {
            if (forward) {
                insChildren[index] = new BamInsChildSingletonSeqSym(iblockMins[index], iblockMaxs[index], index, seq);
            } else {
                insChildren[index] = new BamInsChildSingletonSeqSym(iblockMaxs[index], iblockMins[index], index, seq);
            }
        }
        return insChildren[index];
    }
    
    public SeqSymmetry getSoftChild(int index) {
        if (sblockMins == null || (sblockMins.length <= index)) {
            return null;
        }
        if (softChildren == null) {
            softChildren = new SeqSymmetry[sblockMins.length];
        }

        if (softChildren[index] == null) {
            if (forward) {
                softChildren[index] = new BamSoftChildSingletonSeqSym(sblockMins[index], sblockMaxs[index], index, seq);
            } else {
                softChildren[index] = new BamSoftChildSingletonSeqSym(sblockMaxs[index], sblockMins[index], index, seq);
            }
        }
        return softChildren[index];
    }
    
    @Override
    public SeqSymmetry getChild(int index) {
        if (blockMins == null || index >= (blockMins.length)) {
            return null;
        }
        if (children == null) {
            children = new SeqSymmetry[blockMins.length];
        }

        if (children[index] == null) {
            if (forward) {
                children[index] = new BamChildSingletonSeqSym(blockMins[index], blockMaxs[index], seq);
            } else {
                children[index] = new BamChildSingletonSeqSym(blockMaxs[index], blockMins[index], seq);
            }
        }
        return children[index];
    }

    @Override
    public String substring(int start, int end) {
        return getResidues(start, end);
    }

    @Override
    public int indexOf(String searchstring, int offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static boolean isBamChildType(SeqSymmetry sym) {
        return sym instanceof BamChildSingletonSeqSym || sym instanceof BamInsChildSingletonSeqSym || sym instanceof BamSoftChildSingletonSeqSym;
    }
    
    public static boolean isBamInsChildType(SeqSymmetry sym) {
        return sym instanceof BamInsChildSingletonSeqSym;
    }
    
    public static boolean isBamSoftChildType(SeqSymmetry sym) {
        return sym instanceof BamSoftChildSingletonSeqSym;
    }
    
    class BamChildSingletonSeqSym extends SingletonSeqSymmetry implements SymWithBaseQuality {

        private BitSet residueMask;
        private Integer averageQualityScore;

        public BamChildSingletonSeqSym(int start, int end, BioSeq seq) {
            super(start, end, seq);
        }

        @Override
        public String getResidues() {
            return BAMSym.this.getResidues(this.getMin(), this.getMax(), false, false);
        }

        @Override
        public String getResidues(int start, int end) {
            return BAMSym.this.getResidues(start, end, false, false);
        }

        @Override
        public String getBaseQuality() {
            return BAMSym.this.getBaseQuality(this.getMin(), this.getMax(), false, false);
        }

        @Override
        public String getBaseQuality(int start, int end) {
            return BAMSym.this.getBaseQuality(start, end, false, false);
        }

        @Override
        public int getAverageQuality() {
            if (averageQualityScore == null) {
                averageQualityScore = BAMSym.this.getAverageQuality(getBaseQuality());
            }
            return averageQualityScore;
        }

        @Override
        public BitSet getResidueMask() {
            return residueMask;
        }

        @Override
        public void setResidueMask(BitSet bitset) {
            this.residueMask = bitset;
        }

        // For the web links to be constructed properly, this class must implement getID(),
        // or must NOT implement SymWithProps.
        @Override
        public String getID() {
            return BAMSym.this.getID();
        }

        @Override
        public Object getProperty(String key) {
            return BAMSym.this.getProperty(key);
        }

        @Override
        public boolean setProperty(String key, Object val) {
            return false;
        }

        @Override
        public Map<String, Object> getProperties() {
            return cloneProperties();
        }

        @Override
        public Map<String, Object> cloneProperties() {
            HashMap<String, Object> tprops = new HashMap<>();
            tprops.putAll(BAMSym.this.cloneProperties());
            tprops.put(ID, name);
            tprops.put(RESIDUES, getResidues());
            tprops.put(FORWARD, this.isForward());
            tprops.put(SCORES, getBaseQuality());
            tprops.put(AVERAGE_QUALITY, getAverageQuality());
            return tprops;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    class BamInsChildSingletonSeqSym extends SingletonSeqSymmetry implements SymWithBaseQuality {

        private BitSet residueMask;
        private Integer averageQualityScore;
        final int index;

        public BamInsChildSingletonSeqSym(int start, int end, int index, BioSeq seq) {
            super(start, end, seq);
            this.index = index;
        }

        @Override
        public String getResidues(int start, int end) {
            return BAMSym.this.getResidues(start, end, true, false);
        }

        @Override
        public String getResidues() {
            return BAMSym.this.getResidues(this.getMin(), this.getMax(), true, false);
        }

        @Override
        public BitSet getResidueMask() {
            return residueMask;
        }

        @Override
        public void setResidueMask(BitSet bitset) {
            this.residueMask = bitset;
        }

        @Override
        public String getBaseQuality() {
            return BAMSym.this.getBaseQuality(this.getMin(), this.getMax(), true, false);
        }

        @Override
        public String getBaseQuality(int start, int end) {
            return BAMSym.this.getBaseQuality(start, end, true, false);
        }

        @Override
        public int getAverageQuality() {
            if (averageQualityScore == null) {
                averageQualityScore = BAMSym.this.getAverageQuality(getBaseQuality());
            }
            return averageQualityScore;
        }

        // For the web links to be constructed properly, this class must implement getID(),
        // or must NOT implement SymWithProps.
        @Override
        public String getID() {
            return BAMSym.this.getID();
        }

        @Override
        public Object getProperty(String key) {
            return BAMSym.this.getProperty(key);
        }

        @Override
        public boolean setProperty(String key, Object val) {
            return false;
        }

        @Override
        public Map<String, Object> getProperties() {
            return cloneProperties();
        }

        @Override
        public Map<String, Object> cloneProperties() {
            HashMap<String, Object> tprops = new HashMap<>();
            tprops.putAll(BAMSym.this.cloneProperties());
            tprops.put(ID, name);
            tprops.put(RESIDUES, getResidues());
            tprops.put(FORWARD, this.isForward());
            tprops.put(FEATURE_TYPE, "insertion");
            tprops.put(AVERAGE_QUALITY, getAverageQuality());
            return tprops;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    class BamSoftChildSingletonSeqSym extends SingletonSeqSymmetry implements SymWithBaseQuality {

        private BitSet residueMask;
        private Integer averageQualityScore;
        final int index;

        public BamSoftChildSingletonSeqSym(int start, int end, int index, BioSeq seq) {
            super(start, end, seq);
            this.index = index;
        }

        @Override
        public String getResidues(int start, int end) {
            return BAMSym.this.getResidues(start, end, false, true);
        }

        @Override
        public String getResidues() {
            return BAMSym.this.getResidues(this.getMin(), this.getMax(), false, true);
        }

        @Override
        public BitSet getResidueMask() {
            return residueMask;
        }

        @Override
        public void setResidueMask(BitSet bitset) {
            this.residueMask = bitset;
        }

        @Override
        public String getBaseQuality() {
            return BAMSym.this.getBaseQuality(this.getMin(), this.getMax(), false, true);
        }

        @Override
        public String getBaseQuality(int start, int end) {
            return BAMSym.this.getBaseQuality(start, end, false, true);
        }

        @Override
        public int getAverageQuality() {
            if (averageQualityScore == null) {
                averageQualityScore = BAMSym.this.getAverageQuality(getBaseQuality());
            }
            return averageQualityScore;
        }

        // For the web links to be constructed properly, this class must implement getID(),
        // or must NOT implement SymWithProps.
        @Override
        public String getID() {
            return BAMSym.this.getID();
        }

        @Override
        public Object getProperty(String key) {
            return BAMSym.this.getProperty(key);
        }

        @Override
        public boolean setProperty(String key, Object val) {
            return false;
        }

        @Override
        public Map<String, Object> getProperties() {
            return cloneProperties();
        }

        @Override
        public Map<String, Object> cloneProperties() {
            HashMap<String, Object> tprops = new HashMap<>();
            tprops.putAll(BAMSym.this.cloneProperties());
            tprops.put(ID, name);
            tprops.put(RESIDUES, getResidues());
            tprops.put(FORWARD, this.isForward());
            tprops.put(FEATURE_TYPE, "soft-clip");
            tprops.put(AVERAGE_QUALITY, getAverageQuality());
            return tprops;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    @Override
    public String getResidues() {
        if (residues != null) {
            return residues;
        }
        return getEmptyString('-', txMax - txMin);
    }

    @Override
    public String getResidues(int start, int end) {
        if (residues != null) {
            return getResidues(start, end, false, false);
        }
        return getEmptyString('-', end - start);
    }

    @Override
    public String getBaseQuality() {
        if (baseQuality != null) {
            return baseQuality;
        }
        return getEmptyString('*', txMax - txMin);
    }

    @Override
    public String getBaseQuality(int start, int end) {
        if (baseQuality != null) {
            return getBaseQuality(start, end, false, false);
        }
        return getEmptyString('*', end - start);
    }

    @Override
    public int getAverageQuality() {
        if (averageQualityScore == null) {
            if (blockMins != null && blockMins.length > 0) {
                int quality = 0;
                averageQualityScore = quality;
                for (int i = 0; i < blockMins.length; i++) {
                    quality = getAverageQuality(((SymWithBaseQuality) this.getChild(i)).getBaseQuality());
                    // If any child has score of -1 then set overall score to -1
                    if (quality == -1) {
                        averageQualityScore = -1;
                        break;
                    }
                    averageQualityScore += quality;
                }
                averageQualityScore /= blockMins.length;
            } else {
                averageQualityScore = getAverageQuality(getBaseQuality());
            }
        }
        return averageQualityScore;
    }

    @Override
    public BitSet getResidueMask() {
        return residueMask;
    }

    @Override
    public void setResidueMask(BitSet bitset) {
        this.residueMask = bitset;
    }

    private static String getEmptyString(char ch, int length) {
        char[] tempArr = new char[length];
        Arrays.fill(tempArr, ch);

        return new String(tempArr);
    }

    public void setInsResidues(String residues) {
        this.insResidues = residues;
    }
    
    public void setSoftResidues(String residues) {
        this.softResidues = residues;
    }
    
    public String getInsResidue(int childNo) {
        if (childNo > iblockMins.length) {
            return "";
        }

        int start = 0;
        for (int i = 0; i < childNo; i++) {
            start += (iblockMaxs[i] - iblockMins[i]);
        }
        int end = start + (iblockMaxs[childNo] - iblockMins[childNo]);

        return insResidues.substring(start, end);
    }
    
    public String getSoftResidue(int childNo) {
        if (childNo > sblockMins.length) {
            return "";
        }

        int start = 0;
        for (int i = 0; i < childNo; i++) {
            start += (sblockMaxs[i] - sblockMins[i]);
        }
        int end = start + (sblockMaxs[childNo] - sblockMins[childNo]);

        return softResidues.substring(start, end);
    }
    
    @Override
    public Map<String, Object> cloneProperties() {
        if (props == null) {
            props = new HashMap<>();
        }
        props.put(RESIDUES, getResidues().replaceAll("-", ""));
        props.put(MAPQ, mapq);
        props.put(SCORES, getBaseQuality());
        props.put(AVERAGE_QUALITY, getAverageQuality());
        return super.cloneProperties();
    }

    @Override
    public Object getProperty(String key) {
        if (RESIDUES.equalsIgnoreCase(key)) {
            return getResidues();
        }
        return super.getProperty(key);
    }

    public Cigar getCigar() {
        return cigar;
    }

    private String getResidues(int start, int end, boolean isIns, boolean isSoft) {
        return interpretCigar(residues, start, end, isIns, DELETION_CHAR, N_CHAR, PADDING_CHAR, ERROR_CHAR, isSoft);
    }

    private String getBaseQuality(int start, int end, boolean isIns, boolean isSoft) {
        return interpretCigar(baseQuality, start, end, isIns, ERROR_CHAR, ERROR_CHAR, ERROR_CHAR, ERROR_CHAR, isSoft);
    }

    private String interpretCigar(String str, int start, int end, boolean isIns,
            char D, char N, char P, char E, boolean isSoft) {
        
        // IGBF-1173: User reported issue that when BAM files are loaded, IGB freezes.
        // After analysis, it is found that according to the sam/bam spec (https://samtools.github.io/hts-specs/) 
        // '*' is the standard code for 'unavailable' in SAM plain text format. 
        // This means that Phred quality score string was unavailable in BAM file and it contained '*' instead.
        // This scenario arises when user creates BAM file from fasta file format. 
        // IGB did not handle this earlier and hence exceptions were thrown for substring operation. 
        // To fix it, we are now checking if input string is '*'. 
        if (cigar == null || cigar.numCigarElements() == 0 || str == null || str.equals("*")) {
            return "";
        }
            start = Math.max(start, txMin);
            end = Math.min(txMax, end);

            start -= min;
            end -= min;

        if (start > end) {
            return "";
        }

        char[] sb = new char[end - start];
        int stringPtr = 0;
        int currentPos = 0;
        int offset = 0;
        int celLength;
        char[] tempArr;
        for (CigarElement cel : cigar.getCigarElements()) {
            try {
                if (offset >= sb.length) {
                    return String.valueOf(sb);
                }

                celLength = cel.getLength();
                tempArr = new char[celLength];

                if (cel.getOperator() == CigarOperator.INSERTION) {
                    if (isIns && currentPos == start) {
                        return str.substring(stringPtr, stringPtr + celLength);
                    } else {
                        stringPtr += celLength;
                        continue;
                    }
                } else if (cel.getOperator() == CigarOperator.SOFT_CLIP) {
                    if (isSoft && currentPos == start) {
                        return str.substring(stringPtr, stringPtr + celLength);
                    } else {
                        tempArr = str.substring(stringPtr, stringPtr + celLength).toCharArray();
                        stringPtr += celLength;	// print matches
                        currentPos += celLength;
                    }
                } else if (cel.getOperator() == CigarOperator.HARD_CLIP) {
                    continue;				// hard clip can be ignored
                } else if (cel.getOperator() == CigarOperator.DELETION) {
                    Arrays.fill(tempArr, D);		// print deletion as '_'
                    currentPos += celLength;
                } else if (cel.getOperator() == CigarOperator.M || cel.getOperator() == CigarOperator.X || cel.getOperator() == CigarOperator.EQ) {
                        tempArr = str.substring(stringPtr, stringPtr + celLength).toCharArray();
                        stringPtr += celLength;	// print matches
                        currentPos += celLength;
                } else if (cel.getOperator() == CigarOperator.N) {
                    Arrays.fill(tempArr, N);
                    currentPos += celLength;
                } else if (cel.getOperator() == CigarOperator.PADDING) {
                    Arrays.fill(tempArr, P);		// print padding as '*'
                    stringPtr += celLength;
                    currentPos += celLength;
                }

                if (currentPos > start) {
                    int tempOffset = Math.max(tempArr.length - (currentPos - start), 0);
                    int len = Math.min(tempArr.length - tempOffset, sb.length - offset);
                    System.arraycopy(tempArr, tempOffset, sb, offset, len);
                    offset += len;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                if ((end - start) - stringPtr > 0) {
                    tempArr = new char[(end - start) - stringPtr];
                    Arrays.fill(tempArr, E);
                    System.arraycopy(tempArr, 0, sb, 0, tempArr.length);
                }
            }
        }

        return String.valueOf(sb);
    }

    private int getAverageQuality(String qualityStr) {
        if (!Strings.isNullOrEmpty(qualityStr)) {
            int quality = 0;
            byte[] quals = qualityStr.getBytes();
            for (byte qual : quals) {
                quality += (qual - 33);
            }
            return quality / quals.length;
        }
        return -1;
    }

    public boolean getReadPairedFlag() {
        return readPairedFlag;
    }

    public void setReadPairedFlag(boolean b) {
        this.readPairedFlag = b;
    }

    public boolean getFirstOfPairFlag() {
        return firstOfPairFlag;
    }

    public void setFirstOfPairFlag(boolean b) {
        this.firstOfPairFlag = b;
    }

    public boolean getSecondOfPairFlag() {
        return secondOfPairFlag;
    }

    public void setSecondOfPairFlag(boolean b) {
        this.secondOfPairFlag = b;
    }

    public boolean getDuplicateReadFlag() {
        return duplicateReadFlag;
    }

    public void setDuplicateReadFlag(boolean b) {
        this.duplicateReadFlag = b;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int i) {
        this.flags = i;
    }

    public int getMateStart() {
        return mateStart;
    }

    public void setMateStart(int mateStart) {
        this.mateStart = mateStart;
    }

    public boolean isMateUnMapped() {
        return mateUnMapped;
    }

    public void setMateUnMapped(boolean mateUnMapped) {
        this.mateUnMapped = mateUnMapped;
    }

    public boolean isMateNegativeStrandFlag() {
        return mateNegativeStrandFlag;
    }

    public void setMateNegativeStrandFlag(boolean mateNegativeStrandFlag) {
        this.mateNegativeStrandFlag = mateNegativeStrandFlag;
    }

}
