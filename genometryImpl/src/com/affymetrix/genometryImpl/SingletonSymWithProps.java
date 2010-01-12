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

package com.affymetrix.genometryImpl;

import java.util.*;

import com.affymetrix.genometryImpl.symmetry.MutableSingletonSeqSymmetry;


public class SingletonSymWithProps extends MutableSingletonSeqSymmetry
	implements SymWithProps {

	Map<String,Object> props;

	public SingletonSymWithProps(int start, int end, BioSeq seq) {
		super(start, end, seq);
	}

	public SingletonSymWithProps(CharSequence id, int start, int end, BioSeq seq) {
		super(id, start, end, seq);
	}

	/** Returns the properties map, or null. */
	public Map<String,Object> getProperties() {
		return props;
	}

	public String getID() {
		if (id != null) { return id.toString(); };
		if (props != null) { return (String)props.get("id"); }
		return null;
	}

	/**
	 *  Creates a clone of the properties Map.
	 *  Uses the same type of Map class (HashMap, TreeMap, etc.)
	 *  as the original.
	 */
	public Map<String,Object> cloneProperties() {
		if (props == null) { return null; }
		// quick check for efficient Hashtable cloning
		else if (props instanceof Hashtable) {
			return (Map<String,Object>)((Hashtable)props).clone();
		}
		// quick check for efficient HashMap cloning
		else if (props instanceof HashMap) {
			return (Map<String,Object>)((HashMap)props).clone();
		}
		// quick check for efficient TreeMap cloning
		else if (props instanceof TreeMap) {
			return (Map<String,Object>)((TreeMap)props).clone();
		}
		else {
			try {
				Map<String,Object> newprops = (Map<String,Object>) props.getClass().newInstance();
				newprops.putAll(props);
				return newprops;
			}
			catch (Exception ex) {
				System.out.println("problem trying to clone SymWithProps properties, " +
						"returning null instead");
				return null;
			}
		}
	}

	public boolean setProperty(String name, Object val) {
		if (props == null) {
			props = new Hashtable<String,Object>();
		}
		props.put(name, val);
		return true;
	}

	public Object getProperty(String name) {
		if (props == null) { return null; }
		return props.get(name);
	}
}
