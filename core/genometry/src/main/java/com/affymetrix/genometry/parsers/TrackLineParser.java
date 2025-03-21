/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.style.DefaultStateProvider;
import com.affymetrix.genometry.style.GraphState;
import com.affymetrix.genometry.style.GraphType;
import com.affymetrix.genometry.style.ITrackStyle;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.style.PropertyConstants;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TrackLineParser {

    private static final Pattern COMMA_REGEX = Pattern.compile(",");

    public static final String NAME = "name";
    public static final String COLOR = "color";
    public static final String DESCRIPTION = "description";
    public static final String VISIBILITY = "visibility";
    private static final String URL = "url";

    /**
     * If this property has the value "on" (case-insensitive) and the SeqSymmetry has a property {@link #ITEM_RGB}, then
     * that color will be used for drawing that symmetry. Value is stored in the IAnnotStyle extended properties.
     */
    public static final String ITEM_RGB = "itemrgb";

    /**
     * A pattern that matches things like <b>aaa=bbb</b> or <b>aaa="bbb"</b>
     * or even <b>"aaa"="bbb=ccc"</b>.
     */
    private static final Pattern TRACK_LINE_REGEX = Pattern.compile(
            "([\\S&&[^=]]+)" // Group 1: One or more non-whitespace characters (except =)
            + "=" //  an equals sign
            + "(" // Group 2: Either ....
            + "(?:\"[^\"]*\")" // Any characters (except quote) inside quotes
            + "|" //    ... or ...
            + "(?:\\S+)" // Any non-whitespace characters
            + ")");               //    ... end of group 2

    private final Map<String, String> trackLineContent;

    public TrackLineParser() {
        trackLineContent = new TreeMap<>();
    }

    public TrackLineParser(String trackLine) {
        trackLineContent = new TreeMap<>();
        parseTrackLine(trackLine);
    }

    public Map<String, String> getTrackLineContent() {
        return trackLineContent;
    }

    /**
     * Convert a color in string representation "RRR,GGG,BBB" into a Color. Note that this can throw an exception if the
     * String is poorly formatted.
     *
     * @param color_string
     * @return
     */
    public static Color reformatColor(String color_string) {
        String[] rgb = COMMA_REGEX.split(color_string);
        if (rgb.length == 3) {
            int red = Integer.parseInt(rgb[0]);
            int green = Integer.parseInt(rgb[1]);
            int blue = Integer.parseInt(rgb[2]);
            return new Color(red, green, blue);
        }
        return null;
    }

    /**
     * If the string starts and ends with '\"' characters, this removes them.
     */
    private static String unquote(String str) {
        int length = str.length();
        if (length > 1 && str.charAt(0) == '\"' && str.charAt(length - 1) == '\"') {
            return str.substring(1, length - 1);
        }
        return str;
    }

    public Map<String, String> parseTrackLine(String track_line) {
        return parseTrackLine(track_line, null);
    }

    /**
     * Parses a track line putting the keys and values into the current value of getCurrentTrackHash(), but does not use
     * these properties to change any settings of TrackStyle, etc. The Map is returned and is also available as
     * {@link #getTrackLineContent()}. Any old values are cleared from the existing track line hash first. If
     * track_name_prefix arg is non-null, it is added as prefix to parsed in track name
     */
    public Map<String, String> parseTrackLine(String track_line, String track_name_prefix) {
        trackLineContent.clear();
        Matcher matcher = TRACK_LINE_REGEX.matcher(track_line);
        // If performance becomes important, it is possible to save and re-use a Matcher,
        // but it isn't thread-safe
        while (matcher.find() && (!Thread.currentThread().isInterrupted())) {
            if (matcher.groupCount() == 2) {
                String tag = unquote(matcher.group(1).toLowerCase().trim());
                String val = unquote(matcher.group(2));
                trackLineContent.put(unquote(tag), unquote(val));
            } else {
                // We will only get here if the definition of track_line_parser has been messed with
                System.out.println("Couldn't parse this part of the track line: " + matcher.group(0));
            }
        }
        String track_name = trackLineContent.get(NAME);
        if (track_name != null && track_name_prefix != null) {
            String new_track_name = track_name_prefix + track_name;
            trackLineContent.put(NAME, new_track_name);
        }
        return trackLineContent;
    }

    /**
     * Creates an instance of TrackStyle based on the given track hash. A default track name must be provided in case
     * none is specified by the track line itself.
     *
     * @param trackLineContent
     * @param trackUri
     * @param fileType
     * @return
     */
    public static ITrackStyleExtended createTrackStyle(Map<String, String> trackLineContent, String trackUri, String fileType) {
        String name = trackUri;
        if (trackLineContent.containsKey(NAME)) {
            name = trackLineContent.get(NAME);
        } else if (name.contains(File.separator)) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
        } else {
            name = name.substring(name.lastIndexOf("/") + 1);
        }

        ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(trackUri, name, fileType, getTrackProperties(trackLineContent));
        applyTrackProperties(trackLineContent, style);
        return style;
    }

    private static String getHumanName(Map<String, String> trackLineContent, String id, String default_name) {
        String description = trackLineContent.get(DESCRIPTION);
        if (description != null && !description.equals(id)) {
            return description;
        }

        String name = trackLineContent.get(NAME);
        if (name != null && !name.equals(id)) {
            return name;
        }

        return default_name;
    }

    /**
     * Copies the properties, such as color, into a given ITrackStyle. (For a graph, the ITrackStyle will be an instance
     * of DefaultTrackStyle, for a non-graph, it will be an instance of TrackStyle.)
     */
    private static Map<String, String> getTrackProperties(Map<String, String> trackLineContent) {
        Map<String, String> props = new HashMap<>();
        String visibility = trackLineContent.get(VISIBILITY);
        String color_string = trackLineContent.get(COLOR);
        if (color_string != null) {
            props.put(PropertyConstants.PROP_FOREGROUND, color_string);
        }

        List<String> collapsed_modes = Arrays.asList("1", "dense");
        List<String> expanded_modes = Arrays.asList("2", "full", "3", "pack", "4", "squish");

        if (visibility != null) {
            // 0 - hide, 1 - dense, 2 - full, 3 - pack, and 4 - squish.
            // The numerical values or the words can be used, i.e. full mode may be
            // specified by "2" or "full". The default is "1".
            if (collapsed_modes.contains(visibility)) {
                props.put(PropertyConstants.PROP_COLLAPSED, PropertyConstants.TRUE);
            } else if (expanded_modes.contains(visibility)) {
                props.put(PropertyConstants.PROP_COLLAPSED, PropertyConstants.FALSE);
            }
        }
        return props;
    }

    private static void applyTrackProperties(Map<String, String> trackLineContent, ITrackStyle style) {
        if (style instanceof ITrackStyleExtended) { // for non-graph tiers
            ITrackStyleExtended annot_style = (ITrackStyleExtended) style;
            String url = trackLineContent.get(URL);
            if (url != null) {
                annot_style.setUrl(url);
            }
//			if ("1".equals(track_hash.get(USE_SCORE))) {
//				Score score = new Score();
////				score.setTrackStyle(style);
//				annot_style.setColorProvider(score);
//			}
        }

        // Probably shouldn't copy ALL keys to the extended values
        // since some are already included in the standard values above
        for (String key : trackLineContent.keySet()) {
            Object value = trackLineContent.get(key);
            style.getTransientPropertyMap().put(key, value);
        }
    }

    /**
     * Applies the UCSC track properties that it understands to the GraphState object. Understands: "viewlimits",
     * "graphtype" = "bar" or "points".
     *
     * @param trackLineContent
     */
    public static void createGraphStyle(Map<String, String> trackLineContent, String graphId, String graphName, String extension) {
        GraphState gstate = DefaultStateProvider.getGlobalStateProvider().getGraphState(graphId, getHumanName(trackLineContent, graphId, graphName), extension, getTrackProperties(trackLineContent));
        applyTrackProperties(trackLineContent, gstate.getTierStyle());

        String view_limits = trackLineContent.get("viewlimits");
        if (view_limits != null) {
            String[] limits = view_limits.split(":");
            if (limits.length == 2) {
                float min = Float.parseFloat(limits[0]);
                float max = Float.parseFloat(limits[1]);
                gstate.setVisibleMinY(min);
                gstate.setVisibleMaxY(max);
            }
        }

        String graph_type = trackLineContent.get("graphtype");
        // UCSC browser supports only the types "points" and "bar"
        if ("points".equalsIgnoreCase(graph_type)) {
            gstate.setGraphStyle(GraphType.DOT_GRAPH);
        } else if ("bar".equalsIgnoreCase(graph_type)) {
            gstate.setGraphStyle(GraphType.EMPTY_BAR_GRAPH);
        }
    }
}
