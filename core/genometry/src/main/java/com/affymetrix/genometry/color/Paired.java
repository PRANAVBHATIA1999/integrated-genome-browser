package com.affymetrix.genometry.color;

import com.affymetrix.genometry.general.Parameter;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.impl.BAMSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import java.awt.Color;

/**
 *
 * @author hiralv
 */
public class Paired extends ColorProvider {

    private final static String PAIRED_COLOR = "read_with_mates";
    private final static String NOTPAIRED_COLOR = "read_without_mates";
    private final static Color DEFAULT_PAIRED_COLOR = new Color(204, 255, 255);
    private final static Color DEFAULT_NOTPAIRED_COLOR = new Color(51, 255, 255);

    private Parameter<Color> pairColor = new Parameter<>(DEFAULT_PAIRED_COLOR);
    private Parameter<Color> notPairColor = new Parameter<>(DEFAULT_NOTPAIRED_COLOR);

    public Paired() {
        super();
        parameters.addParameter(PAIRED_COLOR, Color.class, pairColor);
        parameters.addParameter(NOTPAIRED_COLOR, Color.class, notPairColor);
    }

    @Override
    public boolean isFileTypeCategorySupported(FileTypeCategory fileTypeCategory) {
        return fileTypeCategory == FileTypeCategory.Alignment;
    }

    @Override
    public String getName() {
        return "paired";
    }

    @Override
    public Color getColor(SeqSymmetry sym) {
        if (sym instanceof BAMSym && ((BAMSym) sym).getReadPairedFlag()) {
            return pairColor.get();
        }
        return notPairColor.get();
    }
}
