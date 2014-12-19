/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometryImpl.util;

import com.affymetrix.genometryImpl.symmetry.SymWithProps;
import static com.affymetrix.genometryImpl.tooltip.ToolTipConstants.BAM_PROP_LIST;
import static com.affymetrix.genometryImpl.tooltip.ToolTipConstants.BED14_PROP_LIST;
import static com.affymetrix.genometryImpl.tooltip.ToolTipConstants.DEFAULT_PROP_LIST;
import static com.affymetrix.genometryImpl.tooltip.ToolTipConstants.GFF_PROP_LIST;
import static com.affymetrix.genometryImpl.tooltip.ToolTipConstants.PSL_PROP_LIST;
import static com.affymetrix.genometryImpl.util.SeqUtils.isBamSym;
import static com.affymetrix.genometryImpl.util.SeqUtils.isBedSym;
import static com.affymetrix.genometryImpl.util.SeqUtils.isGFFSym;
import static com.affymetrix.genometryImpl.util.SeqUtils.isLinkPSL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            propertyKeys = BAM_PROP_LIST;
        } else if (isBedSym(sym)) {
            propertyKeys = BED14_PROP_LIST;
        } else if (isLinkPSL(sym)) {
            propertyKeys = PSL_PROP_LIST;
        } else if (isGFFSym(sym)) {
            propertyKeys = GFF_PROP_LIST;
        } else {
            logger.warn("Sym class not handled: " + sym.getClass().getSimpleName());
            propertyKeys = DEFAULT_PROP_LIST;
        }
        return orderProperties(propertyKeys, properties);
    }

    private static Map<String, Object> orderProperties(List<String> propertyKeys, Map<String, Object> properties) {
        Map<String, Object> orderedProps = new LinkedHashMap<String, Object>();
        for (String property : propertyKeys) {
            if (properties.containsKey(property)) {
                orderedProps.put(property, properties.get(property).toString());
            }
        }

        for (String key : properties.keySet()) {
            if (!propertyKeys.contains(key)) {
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
            }
        }
        return orderedProps;
    }
}
