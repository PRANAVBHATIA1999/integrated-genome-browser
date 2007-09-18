/**
*   Copyright (c) 2001-2007 Affymetrix, Inc.
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

package com.affymetrix.genometryImpl.util;

import java.util.*;

/**
 *  A hash with a name.
 */
public class NamedHash<K,V> extends TreeMap<K,V> {
  static final long serialVersionUID = 1L;
  String name;

  public NamedHash(String name, Comparator<? super K> comp) {
    super(comp);
    setName(name);
  }

  public NamedHash(String name) {
    super();
    setName(name);
  }

  // protected, because really want name to be declared in constructor
  protected void setName(String name) {
    this.name = name;
  }

  public String getName() { return name; }

  @Override
  public String toString() { return getName(); }

}









