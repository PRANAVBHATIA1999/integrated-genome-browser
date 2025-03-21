package com.affymetrix.igb.glyph;

import com.affymetrix.genometry.util.ImprovedStringCharIter;
import com.affymetrix.genometry.util.SearchableCharIterator;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.igb.util.ResidueColorHelper;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.BitSet;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author jnicol
 *
 * A glyph that shows a sequence of aligned residues. At low resolution (small scale) as a solid background rectangle
 * and at high resolution overlays the residue letters.
 *
 * Residues can be masked out if they agree with a reference sequence.
 *
 */
public class AlignedResidueGlyph extends AbstractAlignedTextGlyph {

    private static final ResidueColorHelper helper = ResidueColorHelper.getColorHelper();
    private static final Map<Float, AlphaComposite> alphaCompositeCache = new WeakHashMap<>();

    private static int minQ = 0;
    private static int maxQ = 40;
    private boolean showMask = true;
    private boolean useBaseQuality = false;
    private SearchableCharIterator qualCharIter;

    public void setShowMask(boolean show) {
        showMask = show;
    }

    @Override
    protected boolean getShowMask() {
        return showMask;
    }

    @Override
    protected boolean shouldSkip() {
        return super.shouldSkip() && !useBaseQuality;
    }

    @Override
    protected void drawResidueRectangles(ViewI view, double pixelsPerBase,
            char[] charArray, int seqBegIndex, int seqEndIndex, BitSet residueMask) {

        Graphics2D g = view.getGraphics();
        g.setPaintMode();
        Composite dac = g.getComposite();
        byte[] quals = useBaseQuality ? qualCharIter.substring(seqBegIndex, seqEndIndex).getBytes() : null;

        int intPixelsPerBase = (int) Math.ceil(pixelsPerBase);
        float alpha;
        for (int j = 0; j < charArray.length; j++) {

            if (showMask && residueMask != null && !residueMask.get(j)) {
                if (useBaseQuality) {
                    alpha = 1.0f - getAlpha(quals[j]);

                    if (alpha < 1.0f) {
                        AlphaComposite ac = alphaCompositeCache.get(alpha);
                        if (ac == null) {
                            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                            alphaCompositeCache.put(alpha, ac);
                        }
                        g.setComposite(ac);
                    }
                    g.setColor(this.getBackgroundColor());
                    int offset = (int) (j * pixelsPerBase);
                    //ceiling is done to the width because we want the width to be as wide as possible to avoid losing pixels.
                    g.fillRect(getPixelBox().x + offset, getPixelBox().y, intPixelsPerBase, getPixelBox().height);
                    g.setComposite(dac);
                }
                continue;	// skip drawing of this residue
            }

            if (useBaseQuality) {
                g.setColor(this.getBackgroundColor());
                int offset = (int) (j * pixelsPerBase);
                //ceiling is done to the width because we want the width to be as wide as possible to avoid losing pixels.
                g.fillRect(getPixelBox().x + offset, getPixelBox().y, intPixelsPerBase, getPixelBox().height);

                alpha = getAlpha(quals[j]);

                if (alpha < 1.0f) {
                    AlphaComposite ac = alphaCompositeCache.get(alpha);
                    if (ac == null) {
                        ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                        alphaCompositeCache.put(alpha, ac);
                    }
                    g.setComposite(ac);
                }
            }

            g.setColor(helper.determineResidueColor(charArray[j]));

            //Create a colored rectangle.
            //We calculate the floor of the offset as we want the offset to stay to the extreme left as possible.
            int offset = (int) (j * pixelsPerBase);
            //ceiling is done to the width because we want the width to be as wide as possible to avoid losing pixels.
            g.fillRect(getPixelBox().x + offset, getPixelBox().y, intPixelsPerBase, getPixelBox().height);

            if (useBaseQuality) {
                g.setComposite(dac);
            }
        }

        if (useBaseQuality) {
            g.setColor(this.getForegroundColor());
            g.drawRect(getPixelBox().x, getPixelBox().y, getPixelBox().width, getPixelBox().height);
        }
    }

    public void setBaseQuality(String baseQuality) {
        qualCharIter = new ImprovedStringCharIter(baseQuality);
    }

    public void setUseBaseQuality(boolean useBaseQuality) {
        this.useBaseQuality = useBaseQuality;
    }

    private float getAlpha(byte qual) {
        qual = (byte) (qual - 33);
        float alpha;

        if (qual < minQ) {
            alpha = 0.1f;
        } else {
            alpha = Math.max(0.1f, Math.min(1.0f, 0.1f + 0.9f * (qual - minQ) / (maxQ - minQ)));
        }

        // Round alpha to nearest 0.1
        alpha = ((int) (alpha * 10 + 0.5f)) / 10.0f;

        return alpha;
    }
}
