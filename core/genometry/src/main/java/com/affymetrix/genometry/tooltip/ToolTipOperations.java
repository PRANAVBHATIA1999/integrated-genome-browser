/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.tooltip;

import static com.affymetrix.genometry.tooltip.ToolTipConstants.BAM_DETAILS_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BAM_IGNORE_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BAM_INFO_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BAM_LOCATION_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BED14_CIGAR_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BED14_IGNORE_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BED14_INFO_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BED14_LOCATION_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.DEFAULT_CIGAR_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.DEFAULT_IGNORE_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.DEFAULT_INFO_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.DEFAULT_LOCATION_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.GFF_CIGAR_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.GFF_IGNORE_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.GFF_INFO_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.GFF_LOCATION_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.MISC_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.NARROW_PEAK_CIGAR_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.NARROW_PEAK_IGNORE_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.NARROW_PEAK_INFO_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.NARROW_PEAK_LOCATION_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.PSL_IGNORE_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.PSL_INFO_CATEGORY;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.PSL_LOCATION_CATEGORY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 * @author tarun
 */
public class ToolTipOperations {

    public static List<ToolTipCategory> formatBamSymTooltip(Map<String, Object> props) {
        List<ToolTipCategory> categories = new ArrayList<>();

        populateCategory(props, BAM_INFO_CATEGORY, categories);
        populateCategory(props, BAM_LOCATION_CATEGORY, categories);
        populateCategory(props, BAM_DETAILS_CATEGORY, categories);
        populateMisc(props, categories, BAM_IGNORE_LIST);
        return categories;
    }
    
    public static List<ToolTipCategory> formatBamInsSymTooltip(Map<String, Object> props) {
        List<ToolTipCategory> categories = new ArrayList<>();

        populateCategory(props, ToolTipConstants.BAM_INS_INFO_CATEGORY, categories);
        populateMisc(props, categories, ToolTipConstants.BAM_INS_IGNORE_LIST);
        return categories;
    }

    public static List<ToolTipCategory> formatBED14SymTooltip(Map<String, Object> props) {
        List<ToolTipCategory> categories = new ArrayList<>();

        populateCategory(props, BED14_INFO_CATEGORY, categories);
        populateCategory(props, BED14_LOCATION_CATEGORY, categories);
        populateCategory(props, BED14_CIGAR_CATEGORY, categories);
        populateMisc(props, categories, BED14_IGNORE_LIST);
        return categories;
    }

    public static List<ToolTipCategory> formatNarrowPeakSymTooltip(Map<String, Object> props) {
        List<ToolTipCategory> categories = new ArrayList<>();

        populateCategory(props, NARROW_PEAK_INFO_CATEGORY, categories);
        populateCategory(props, NARROW_PEAK_LOCATION_CATEGORY, categories);
        populateCategory(props, NARROW_PEAK_CIGAR_CATEGORY, categories);
        populateMisc(props, categories, NARROW_PEAK_IGNORE_LIST);
        return categories;
    }

    public static List<ToolTipCategory> formatLinkPSLSymTooltip(Map<String, Object> props) {
        List<ToolTipCategory> categories = new ArrayList<>();

        populateCategory(props, PSL_INFO_CATEGORY, categories);
        populateCategory(props, PSL_LOCATION_CATEGORY, categories);
        populateMisc(props, categories, PSL_IGNORE_LIST);
        return categories;
    }

    public static List<ToolTipCategory> formatDefaultSymTooltip(Map<String, Object> props) {
        List<ToolTipCategory> categories = new ArrayList<>();

        populateCategory(props, DEFAULT_INFO_CATEGORY, categories);
        populateCategory(props, DEFAULT_LOCATION_CATEGORY, categories);
        populateCategory(props, DEFAULT_CIGAR_CATEGORY, categories);
        populateMisc(props, categories, DEFAULT_IGNORE_LIST);
        return categories;
    }

    public static List<ToolTipCategory> formatGFFSymTooltip(Map<String, Object> props) {
        List<ToolTipCategory> categories = new ArrayList<>();

        populateCategory(props, GFF_INFO_CATEGORY, categories);
        populateCategory(props, GFF_LOCATION_CATEGORY, categories);
        populateCategory(props, GFF_CIGAR_CATEGORY, categories);
        populateMisc(props, categories, GFF_IGNORE_LIST);

        return categories;
    }

    private static void populateCategory(Map<String, Object> props, Map<String, List<String>> categoryData, List<ToolTipCategory> categories) {
        final Predicate<String> keyMapsToNull = key -> props.get(key) != null;
        categoryData.entrySet().stream().forEach((entry) -> {
            String categoryKey = entry.getKey();
            List<String> keys = entry.getValue();
            Map<String, String> destProps = new LinkedHashMap<>();
            ToolTipCategory category;
            keys.stream().filter((key) -> (props.containsKey(key))).filter(keyMapsToNull).forEach((key) -> {
                String value = props.get(key).toString();
                props.remove(key);
                destProps.put(key, value);
            });
            if (destProps.size() > 0) {
                category = new ToolTipCategory(categoryKey, 1, destProps);
                categories.add(category);
            }
        });
    }

    private static void populateMisc(Map<String, Object> props, List<ToolTipCategory> categories, List<String> ignoreList) {
        final Predicate<String> keyMapsToNull = key -> props.get(key) != null;
        Map<String, String> miscInfoProps = new HashMap<>();
        ToolTipCategory miscInfoCategory = new ToolTipCategory(MISC_CATEGORY, 3, miscInfoProps);
        props.keySet().stream().filter(key -> !ignoreList.contains(key)).filter(keyMapsToNull).forEach(key -> {
            miscInfoProps.put(key, props.get(key).toString());
        });

        if (miscInfoProps.size() > 1) {
            categories.add(miscInfoCategory);
        }
    }

}
