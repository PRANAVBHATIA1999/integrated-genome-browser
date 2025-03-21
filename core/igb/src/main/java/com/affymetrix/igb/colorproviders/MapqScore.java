package com.affymetrix.igb.colorproviders;

import com.affymetrix.genometry.color.ColorProvider;
import com.affymetrix.genometry.general.Parameter;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.style.HeatMap;
import com.affymetrix.genometry.style.HeatMapExtended;
import com.affymetrix.genometry.symmetry.impl.BAMSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.igb.IGBConstants;
import cytoscape.visual.ui.editors.continuous.BasicVirtualRange;
import cytoscape.visual.ui.editors.continuous.ColorInterpolator;
import cytoscape.visual.ui.editors.continuous.GradientColorInterpolator;
import cytoscape.visual.ui.editors.continuous.VirtualRange;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hiralv
 */
public class MapqScore extends ColorProvider {

    private final static String HEATMAP = "heatmap";
    public final static float DEFAULT_MIN_SCORE = 1.0f;
    public final static float DEFAULT_MAX_SCORE = 254.0f;
    public static final float[] DEFAULT_VALUES = new float[]{0, 50, 200, 254};
    public static final Color[] DEFAULT_COLORS = new Color[]{Color.BLACK, Color.BLACK, Color.RED, Color.RED};
    public static final VirtualRange VIRTUAL_RANGE = new BasicVirtualRange(DEFAULT_VALUES, DEFAULT_COLORS);
    public static final ColorInterpolator COLOR_INTERPOLATOR = new GradientColorInterpolator(VIRTUAL_RANGE);
    public final static HeatMap DEFAULT_HEATMAP = new HeatMapExtended("Default_Mapq_HeatMapExtended",
            COLOR_INTERPOLATOR.getColorRange(HeatMap.BINS),
            VIRTUAL_RANGE.getVirtualValues(),
            VIRTUAL_RANGE.getColors());

    private float min_score_color = DEFAULT_MIN_SCORE;
    private float max_score_color = DEFAULT_MAX_SCORE;
    private float range = max_score_color - min_score_color;
    private Color botton_color = DEFAULT_COLORS[0];
    private Color top_color = DEFAULT_COLORS[DEFAULT_COLORS.length - 1];
    private Map<Float, Color> rangeToColor = new HashMap<>();

    private Parameter<HeatMap> custom_heatmap = new Parameter<HeatMap>(DEFAULT_HEATMAP) {
        @Override
        public boolean set(Object e) {
            super.set(e);
            HeatMapExtended heatmap = (HeatMapExtended) e;
            min_score_color = heatmap.getValues()[0];
            max_score_color = heatmap.getValues()[heatmap.getValues().length - 1];
            range = max_score_color - min_score_color;
            botton_color = heatmap.getRangeColors()[0];
            top_color = heatmap.getRangeColors()[heatmap.getValues().length - 1];
            fillRangeToColor(heatmap);
            return true;
        }
    };

    public MapqScore() {
        super();
        parameters.addParameter(HEATMAP, HeatMapExtended.class, custom_heatmap);
        fillRangeToColor((HeatMapExtended) custom_heatmap.get());
    }

    private void fillRangeToColor(HeatMapExtended heatmap) {
        float[] values = heatmap.getValues();
        Color[] valCols = heatmap.getRangeColors();
        rangeToColor.clear();
        //Don't include top and bottom colors
        for (int i = 1; i < values.length - 1; i++) {
            rangeToColor.put(values[i], valCols[i]);
        }
    }

    @Override
    public String getName() {
        return "mapqscore";
    }

    @Override
    public String getDisplay() {
        return IGBConstants.BUNDLE.getString("color_by_" + getName());
    }

    @Override
    public Color getColor(SeqSymmetry sym) {
        if (sym instanceof BAMSym) {
            int score = ((BAMSym) sym).getMapq();
            if (score != BAMSym.NO_MAPQ) {
                return getScoreColor(score);
            }
        }
        return null;
    }

    @Override
    public boolean isFileTypeCategorySupported(FileTypeCategory fileTypeCategory) {
        return fileTypeCategory == FileTypeCategory.Alignment;
    }

    protected Color getScoreColor(float score) {
        if (rangeToColor.containsKey(score)) {
            return rangeToColor.get(score);
        } else if (score <= min_score_color) {
            return botton_color;
        } else if (score >= max_score_color) {
            return top_color;
        } else {
            int index = Math.round(((score - min_score_color) / range) * (HeatMap.BINS - 1));
            return custom_heatmap.get().getColors()[index];
        }
    }
}
