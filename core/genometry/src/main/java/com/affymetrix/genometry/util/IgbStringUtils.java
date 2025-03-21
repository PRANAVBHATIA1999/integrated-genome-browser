/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License").
 * A copy of the license must be included with any distribution of
 * this source code.
 * Distributions from Affymetrix, Inc., place this in the
 * IGB_LICENSE.html file.
 *
 * The license is also available at
 * http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometry.util;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

public final class IgbStringUtils {

    private static final String ELLIPSIS = "\u2026";
    private static final String SPACE = " ";
    private static final String SEPRATORS = "\\s/\\\\._";
    private static final String SEPRATOR_REGEX = "(?<=[" + SEPRATORS + "])";

    /**
     * Wrap the given string into the given number of pixels. This is a
     * convenience method that calls the 4-argument wrap function with the
     * maxLines argument as zero.
     *
     * @param toWrap String to wrap
     * @param metrics FontMetrics used to translate words into pixel widths
     * @param pixels number of pixels to wrap width to
     * @return an array of Strings, one for each line of wrapped text
     */
    public static String[] wrap(String toWrap, FontMetrics metrics, int pixels) {
        return wrap(toWrap, metrics, pixels, 0);
    }

    /**
     * Wrap the given string into a given number of pixels. The returned array
     * of strings will be limited to maxLines, or unlimited if maxLines is zero.
     *
     * @param toWrap String to wrap
     * @param metrics FontMetrics used to translate words into pixel widths
     * @param pixels the number of pixels to wrap width to
     * @param maxLines the maximum number of lines permitted or 0 if unlimited
     * @return an array of Strings, one for each line of wrapped text
     */
    public static String[] wrap(String toWrap, FontMetrics metrics, int pixels, int maxLines) {
        List<String> lines = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        final int spaceWidth = metrics.stringWidth(SPACE);
        final int ellipsisWidth = metrics.stringWidth(ELLIPSIS);
        int remainingWidth = pixels;
        int wordWidth;

        if (pixels < 0 || maxLines < 0) {
            throw new IllegalArgumentException("Neither pixels nor maxLines can be less than 0");
        }

        /* Special case to ensure ellipsis will fit when maxLines == 1 */
        if (maxLines == 1) {
            remainingWidth -= ellipsisWidth;
        }

        for (String word : toWrap.split(SEPRATOR_REGEX)) {
            wordWidth = metrics.stringWidth(word);

            if (wordWidth + spaceWidth > remainingWidth) {
                /* Finished last line, add ellipsis and break*/
                if (maxLines != 0 && maxLines - 1 == lines.size()) {
                    buffer.append(ELLIPSIS);
                    break;
                }

                /* Only add current line if it is non-empty */
                if (buffer.length() > 0) {
                    buffer.append(SPACE);
                    lines.add(buffer.toString());
                }

                /* Start a new line and add current word to it */
                buffer.setLength(0);
                buffer.append(word);
                remainingWidth = pixels - wordWidth;

                /* Starting last line, save room for ellipsis */
                if (maxLines != 0 && maxLines - 1 == lines.size()) {
                    remainingWidth -= ellipsisWidth;
                }
            } else {
                /* append current word to current line */
                buffer.append(word);
                remainingWidth -= wordWidth;
            }
        }

        /* Add last line if it is non-empty */
        if (buffer.length() > 0) {
            lines.add(buffer.toString());
        }

        return lines.toArray(new String[lines.size()]);
    }

}
