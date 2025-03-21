/**
 * Copyright (c) 2007 Affymetrix, Inc.
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
package com.affymetrix.genometry.style;

import java.util.Map;

public interface StateProvider {

    public ITrackStyleExtended getAnnotStyle(String name);

    public ITrackStyleExtended getAnnotStyle(String name, String human_name, String file_type, Map<String, String> props);

    public void removeAnnotStyle(String name);

    public GraphState getGraphState(String name);

    public GraphState getGraphState(String name, String human_name, String file_type, Map<String, String> props);

    public void removeGraphState(String name);
}
