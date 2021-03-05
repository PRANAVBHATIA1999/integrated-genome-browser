package com.affymetrix.igb.tiers;

import java.awt.Color;
import java.util.regex.Pattern;

/**
 *
 * @author hiralv
 */
public interface TrackConstants {

    // A pattern that matches two or more slash "/" characters.
    // A preference node name can't contain two slashes, nor end with a slash.
    static final Pattern multiple_slashes = Pattern.compile("/{2,}");
    static final String NAME_OF_DEFAULT_INSTANCE = "* Default *";
    static final String NAME_OF_COORDINATE_INSTANCE = "Coordinates";
    static final String NO_LABEL = "* none *";
    // The String constants named PREF_* are for use in the persistent preferences
    // They are not displayed to users, and should never change
    static final String PREF_CONNECTED = "Connected";
    static final String PREF_SHOW_AS_PAIRED = "Show As Paired";
    static final String PREF_GLYPH_DEPTH = "Glyph Depth";
    static final String PREF_COLLAPSED = "Collapsed";
    static final String PREF_MAX_DEPTH = "Max Depth";
    static final String PREF_FOREGROUND = "Foreground";
    static final String PREF_BACKGROUND = "Background";
    static final String PREF_LABEL_FOREGROUND = "Label Foreground";
    static final String PREF_LABEL_BACKGROUND = "Label Background";
    static final String PREF_START_COLOR = "Start Color";
    static final String PREF_END_COLOR = "End Color";
    static final String PREF_TRACK_NAME = "Track Name";
    static final String PREF_LABEL_FIELD = "Label Field";
    static final String PREF_SHOW2TRACKS = "Show 2 Tracks";
    static final String PREF_HEIGHT = "Height"; // height per glyph? // linear transform value?
    static final String PREF_TRACK_SIZE = "Track Name Size";
    static final String PREF_DIRECTION_TYPE = "Direction Type";
    static final String PREF_VIEW_MODE = "View Mode";
    static final String PREF_DRAW_COLLAPSE_ICON = "Draw Collapse Icon";
    static final String PREF_SHOW_IGB_TRACK_MARK = "Show IGB Track Mark";
    static final String PREF_SHOW_FILTER_MARK = "Show Filter Mark";//TK
    static final String PREF_SHOW_LOCKED_TRACK_ICON = "Show Locked Track Icon";
    static final String PREF_SHOW_FULL_FILE_PATH_IN_TRACK = "Show Full File Path";//TK
    static final String PREF_SHOW_RESIDUE_MASK = "Show Residue Mask";
    static final String PREF_SHOW_SOFT_CLIPPED = "Hide soft-clipping";
    static final String PREF_SOFT_CLIP_COLOR = "Show as custom color";
    static final String PREF_SHOW_SOFT_CLIPPED_RESIDUES = "Show as mismatches";
    static final String PREF_SHOW_SOFT_CLIPPED_DEFAULT_COLOR = "Show as default color";
    static final String PREF_SHOW_SOFT_CLIPPED_CUSTOM_COLOR = "Show as custom color...";
    static final String PREF_SHADE_BASED_ON_QUALITY_SCORE = "Shade Based On Quality Score";

    static final boolean default_show = true;
    static final boolean default_connected = true;
    static final boolean default_collapsed = false;
    static final boolean default_show2tracks = true;
    static final boolean default_expandable = true;
    static final boolean default_show_summary = false;
    static final boolean default_draw_collapse_icon = true;
    static final boolean default_show_igb_track_mark = true;
    static final boolean default_show_filter_mark = true;
    static final boolean default_show_locked_track_icon = true;
    static final boolean default_show_full_file_path_in_track = false;
    static final boolean default_color_by_score = false;
    static final boolean default_color_by_rgb = false;
    static final boolean default_showResidueMask = true;
    static final boolean default_showSoftClipped = false;
    static final Color default_softClipColor = Color.GRAY;
    static final boolean default_showSoftClippedResidues = false;
    static final boolean default_showSoftClipDefaultColor = true;
    static final boolean default_showSoftClipCustomColor = false;
    static final boolean default_shadeBasedOnQualityScore = false;
    static final int default_max_depth = 10;
    static final Color default_foreground = Color.decode("0x0247FE"); //Blue Color;
    static final Color default_background = Color.WHITE;
    static final Color default_start = new Color(128, 0, 128);
    static final Color default_end = new Color(51, 102, 255);
    static final String default_label_field = "";
    static final int default_glyphDepth = 2;
    static final double default_height = 50.0;//25.0;
    static final double default_y = 0.0;
    static final float default_track_name_size = 12;
    static final float default_min_score_color = 1.0f;
    static final float default_max_score_color = 1000.f;
    static final DirectionType DEFAULT_DIRECTION_TYPE = DirectionType.NONE;
    public static final Object[] SUPPORTED_SIZE = {6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 13.0f, 14.0f, 15.0f, 16.0f, 17.0f, 18.0f, 19.0f, 20.0f, 22.0f, 24.0f, 26.0f, 28.0f, 36.0f, 48.0f, 72.0f};
    public static final Object[] LABELFIELD = {NO_LABEL, "id", "name", "score"};

    public static enum DirectionType {

        NONE,
        ARROW,
        COLOR,
        BOTH;

        public static DirectionType valueFor(String string) {
            for (DirectionType type : DirectionType.values()) {
                if (type.name().equalsIgnoreCase(string)) {
                    return type;
                }
            }
            return DEFAULT_DIRECTION_TYPE;
        }

        public static DirectionType valueFor(int i) {
            if (i < DirectionType.values().length) {
                return DirectionType.values()[i];
            }
            return DEFAULT_DIRECTION_TYPE;
        }
    }
}
