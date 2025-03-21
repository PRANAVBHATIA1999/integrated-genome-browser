/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.util;

import com.affymetrix.genometry.symmetry.SymWithProps;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BAM_INS_PROP_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BAM_PROP_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.BED14_PROP_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.DEFAULT_PROP_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.GFF_PROP_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.NARROW_PEAK_PROP_LIST;
import static com.affymetrix.genometry.tooltip.ToolTipConstants.PSL_PROP_LIST;
import static com.affymetrix.genometry.util.SeqUtils.isBamInsSym;
import static com.affymetrix.genometry.util.SeqUtils.isBamSoftSym;
import static com.affymetrix.genometry.util.SeqUtils.isBamSym;
import static com.affymetrix.genometry.util.SeqUtils.isBedSym;
import static com.affymetrix.genometry.util.SeqUtils.isGFFSym;
import static com.affymetrix.genometry.util.SeqUtils.isLinkPSL;
import static com.affymetrix.genometry.util.SeqUtils.isNarrowPeakSym;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tarun
 */
public class SelectionInfoUtils {

    private static final Logger logger = LoggerFactory.getLogger(SelectionInfoUtils.class);

    public static Map<String, Object> orderProperties(Map<String, Object> properties, SymWithProps sym) {
        List<String> propertyKeys;
        if (isBamSym(sym)) {
            if (isBamInsSym(sym) || isBamSoftSym(sym)) {
                propertyKeys = BAM_INS_PROP_LIST;
                return orderProperties(propertyKeys, properties, true);
            } else {
                propertyKeys = BAM_PROP_LIST;
            }
        } else if (isBedSym(sym)) {
            propertyKeys = BED14_PROP_LIST;
        } else if (isNarrowPeakSym(sym)) {
            propertyKeys = NARROW_PEAK_PROP_LIST;
        } else if (isLinkPSL(sym)) {
            propertyKeys = PSL_PROP_LIST;
        } else if (isGFFSym(sym)) {
            propertyKeys = GFF_PROP_LIST;
        } else {
            if (logger.isDebugEnabled()) {
                if (sym != null) {
                    logger.debug("Sym class not handled: " + sym.getClass().getSimpleName());
                } else {
                    logger.debug("Sym class is null, fallback to default properties");
                }
            }
            propertyKeys = DEFAULT_PROP_LIST;
        }
        return orderProperties(propertyKeys, properties, false);
    }

    private static Map<String, Object> orderProperties(List<String> propertyKeys, Map<String, Object> properties, boolean ignoreNotInKeys) {
        Map<String, Object> orderedProps = new LinkedHashMap<>();
        final Predicate<String> keyMapsToNull = key -> properties.get(key) != null;
        propertyKeys.stream().filter(property -> properties.containsKey(property)).filter(keyMapsToNull).forEach(property -> {
            orderedProps.put(property, properties.get(property).toString());
        });

        if (!ignoreNotInKeys) {
            properties.keySet().stream().filter(key -> !propertyKeys.contains(key)).filter(keyMapsToNull).forEach(key -> {
                Object property = properties.get(key);
                if (property instanceof String[]) {
                    StringBuilder value = new StringBuilder();
                    for (String str : (String[]) property) {
                        value.append(str);
                    }
                    orderedProps.put(key, value.toString());
                } else {
                    orderedProps.put(key, properties.get(key).toString());
                }
            });
        }
        return orderedProps;
    }

}
