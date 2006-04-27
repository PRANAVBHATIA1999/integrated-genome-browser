/**
 *   Copyright (c) 2006 Affymetrix, Inc.
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

package com.affymetrix.swing;

import java.text.NumberFormat;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


/**
 *  A TableCellRenderer for displaying formatted integers with commas.
 *  Use this only with non-editable table cells unless an appropriate
 *  TableCellEditor is also used.
 */
public class IntegerTableCellRenderer extends DefaultTableCellRenderer {
  
  NumberFormat nf;
  
  public IntegerTableCellRenderer() {
    super();
    nf = NumberFormat.getIntegerInstance();
    setHorizontalAlignment(SwingConstants.RIGHT);
  }

  protected void setValue(Object value) {
    if (value instanceof Integer) {
      super.setValue(nf.format(value));
    } else {
      super.setValue(value);
    }
  }
    
}
