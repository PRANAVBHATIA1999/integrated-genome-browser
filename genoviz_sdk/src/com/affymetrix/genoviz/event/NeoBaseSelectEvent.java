/**
 *   Copyright (c) 1998-2005 Affymetrix, Inc.
 *    
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.  
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */

package com.affymetrix.genoviz.event;

import java.util.EventObject;

/**
 * Event generated by a NeoTracer when a single base is selected
 */
public class NeoBaseSelectEvent extends EventObject  {
	private static final long serialVersionUID = 1L;
	int selected_index;

	public NeoBaseSelectEvent( Object source, int base_index ) {
		super( source );
		selected_index = base_index;
	}

	public int getSelectedIndex() {
		return selected_index;
	}

}
