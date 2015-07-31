package org.bioviz.protannot;

import com.affymetrix.genometry.util.DNAUtils;
import java.math.BigInteger;
import org.bioviz.protannot.model.Dnaseq;
import org.bioviz.protannot.model.Dnaseq.MRNA;
import org.bioviz.protannot.model.Dnaseq.MRNA.Cds;
import org.bioviz.protannot.model.Dnaseq.MRNA.Exon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Internal to the application, convert to a "positive strand" format.
 *
 * @author jnicol
 */
public class NormalizeXmlStrand {
    
    private static final Logger LOG = LoggerFactory.getLogger(NormalizeXmlStrand.class);
    
    private static boolean isNegativeStrand = false;
    private static boolean isStrandSet = false;

    /**
     * Transforms sequence coordinates. Normalizes all coordinates respective to the sequence's start coordinates. If
     * strand is negative, flips all coordinates and reverse-complements the sequence.
     *
     * @param seqdoc Document object name
     * @return Returns BioSeq of given document object.
     * @see com.affymetrix.genometryImpl.BioSeq
     */
    public static void normalizeDnaseq(Dnaseq dnaseq) {
        // get residues and normalize their attributes
        final int residuesStart;
        final String residues;
        Dnaseq.Residues residues1 = dnaseq.getResidues();
        residues = residues1.getValue();
        residuesStart = residues1.getStart().intValue();
        //residuesNode.setAttribute(ProtannotParser.STARTSTR, Integer.toString(0)); // normalize start of residues to 0
        residues1.setStart(BigInteger.ZERO);
        int residuesEnd = residues1.getEnd().intValue();
        residues1.setEnd(new BigInteger((residuesEnd - residuesStart) + ""));
        // normalize end of residues, if end exists

        dnaseq.getMRNAAndAaseq().stream().filter(obj -> obj instanceof MRNA).forEach(obj -> normalizemRNA((MRNA) obj, residuesStart, residues));
    }

    private static void normalizemRNA(MRNA mrna, int residuesStart, String residues) {
        // Get strand of mRNA.  Normalize attributes
        int start = mrna.getStart().intValue();
        int end = mrna.getEnd().intValue();
        start -= residuesStart;
        end -= residuesStart;
        
        try {
            String strand = mrna.getStrand();
            isNegativeStrand = "-".equals(strand);
            if (isNegativeStrand) {
                int newEnd = residues.length() - start;
                start = residues.length() - end;
                end = newEnd;
                if (!isStrandSet) {
                    residues = DNAUtils.reverseComplement(residues);
                    isStrandSet = true;
                }
                mrna.setStrand("+"); // Normalizing to positive strand
            }
        } catch (Exception e) {
              isStrandSet = true;
              mrna.setStrand("+");
//            LOG.error(e.getMessage(), e);
        }
        mrna.setStart(new BigInteger(start + ""));
        mrna.setEnd(new BigInteger(end + ""));
        
        for (Exon exon : mrna.getExon()) {
            normalizeExonNodes(exon, residuesStart, residues);
        }
        normalizeCdsNodes(mrna.getCds(), residuesStart, residues);
    }
    
    private static void normalizeExonNodes(Exon exon, int residuesStart, String residues) {
        int start = exon.getStart().intValue();
        int end = exon.getEnd().intValue();
        start -= residuesStart;
        end -= residuesStart;
        if (isNegativeStrand) {
            int newEnd = residues.length() - start;
            start = residues.length() - end;
            end = newEnd;
        }
        exon.setStart(new BigInteger(start + ""));
        exon.setEnd(new BigInteger(end + ""));
    }
    
    private static void normalizeCdsNodes(Cds cds, int residuesStart, String residues) {
        int start = cds.getStart().intValue();
        int end = cds.getEnd().intValue();
        start -= residuesStart;
        end -= residuesStart;
        if (isNegativeStrand) {
            int newEnd = residues.length() - start;
            start = residues.length() - end;
            end = newEnd;
        }
        cds.setStart(new BigInteger(start + ""));
        cds.setEnd(new BigInteger(end + ""));
    }
    
    private static class SimpleErrorHandler implements ErrorHandler {
        
        @Override
        public void warning(SAXParseException e) throws SAXException {
            LOG.warn("Line " + e.getLineNumber() + ": " + e.getMessage());
        }
        
        @Override
        public void error(SAXParseException e) throws SAXException {
            LOG.error("Line " + e.getLineNumber() + ": " + e.getMessage());
        }
        
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            LOG.error("Line " + e.getLineNumber() + ": " + e.getMessage());
        }
    }
    
}
