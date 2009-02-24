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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 *  A way of mapping synonyms to each other.
 *
 * @version $Id$
 */
public final class SynonymLookup {
	private static final boolean DEBUG = false;
	private static final Pattern line_regex = Pattern.compile("\t");
	private static SynonymLookup default_lookup = new SynonymLookup();
	private final LinkedHashMap<String, ArrayList<String>> lookup_hash = new LinkedHashMap<String, ArrayList<String>>();
	private boolean caseSensitive = true;
	/**
	 * Set this flag to true to automatically take care of synonyms of "_random"
	 * sequence names.  If true, then "XXX_random" is a synonym of "YYY_random"
	 * if "XXX" is a synonym of "YYY" (or if that particular set of synonyms
	 * was explicitly specified.)
	 */
	public boolean stripRandom = false;

	/*public static void setDefaultLookup(SynonymLookup lookup) {
	  default_lookup = lookup;
	  }*/
	public static SynonymLookup getDefaultLookup() {
		return default_lookup;
	}

	/** Set whether tests should be case sensitive or not.
	 *  You can turn this on or off safely before or after loading the synonyms.
	 *  Default is true: case does matter by default.
	 */
	public void setCaseSensitive(boolean case_sensitive) {
		this.caseSensitive = case_sensitive;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void loadSynonyms(InputStream istream) throws IOException {
		final InputStreamReader ireader = new InputStreamReader(istream);
		final BufferedReader br = new BufferedReader(ireader);
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String[] fields = line_regex.split(line);
				if (fields.length > 0) {
					addSynonyms(fields);
				}
			}
		} finally {
			GeneralUtils.safeClose(ireader);
			GeneralUtils.safeClose(br);
		}
	}

	public boolean isSynonym(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return false;
		}
		if (str1.equals(str2)) {
			return true;
		}
		final ArrayList<String> al = getSynonyms(str1);
		if (al != null) {
			for (String curstr : al) {
				if (isCaseSensitive()) {
					if (str2.equals(curstr)) {
						return true;
					}
				} else {
					if (str2.equalsIgnoreCase(curstr)) {
						return true;
					}
				}
			}
		}
		if (stripRandom) {
			if (str1.toLowerCase().endsWith("_random") && str2.toLowerCase().endsWith("_random")) {
				return isSynonym(stripRandom(str1), stripRandom(str2));
			}
		}
		return false;
	}

	// Strip the word "_random" from the item name
	// Will not change the case of the input, but if isCaseSensitive is false, will
	// also strip "_RanDom", etc.
	private String stripRandom(String s) {
		if (s == null) {
			return null;
		}
		String s2 = s;
		if (isCaseSensitive()) {
			if (s.endsWith("_random")) {
				s2 = s.substring(0, s.length() - 7);
			}
		} else {
			if (s.toLowerCase().endsWith("_random")) {
				s2 = s.substring(0, s.length() - 7);
			}
		}
		return s2;
	}

	public void addSynonyms(String[] syns) {
		if (DEBUG) {
			System.out.print("adding synonyms :");
		}
		for (int i = 0; i < syns.length; i++) {
			String syn1 = syns[i];
			if (DEBUG) {
				System.out.print(syns[i] + ":");
			}
			for (int k = i + 1; k < syns.length; k++) {
				String syn2 = syns[k];
				addSynonym(syn1, syn2);
			}
		}
	}

	private void addSynonym(String str1, String str2) {
		if (str1 == null || str2 == null || "".equals(str1.trim()) || "".equals(str2.trim())) {
			return;
		}
		final ArrayList<String> list = getSharedList(str1, str2);
		if (!list.contains(str1)) {
			list.add(str1);
		}
		if (!list.contains(str2)) {
			list.add(str2);
		}
	}

	private ArrayList<String> getSharedList(String str1, String str2) {
		ArrayList<String> result = null;

		// We want both synonyms to map to the *identical* List object
		final ArrayList<String> list1 = lookup_hash.get(str1);
		final ArrayList<String> list2 = lookup_hash.get(str2);

		if (list1 != null && list2 != null) {
			if (list1 == list2) {
				// great, they are already the same object
				result = list1;
			} else {
				// If the two strings map to different lists, then merge them into one
				Set<String> the_set = new TreeSet<String>(list1);
				the_set.addAll(list2);

				result = new ArrayList<String>(the_set);
			}
		} else if (list1 != null && list2 == null) {
			result = list1;
		} else if (list1 == null && list2 != null) {
			result = list2;
		} else if (list1 == null && list2 == null) {
			result = new ArrayList<String>();
		}

		lookup_hash.put(str1, result);
		lookup_hash.put(str2, result);
		return result;
	}


	/** Returns all known synonyms for a given string.
	 *  Even if isCaseSensitive() is false, the items in the returned list will still
	 *  have the same cases as were given in the input, but the lookup to find the
	 *  list of synonyms will be done in a case-insensitive way.
	 */
	public ArrayList<String> getSynonyms(String str) {
		if (isCaseSensitive()) {
			return lookup_hash.get(str);
		} else {
			// If not case-sensitive

			final ArrayList<String> o = lookup_hash.get(str);
			if (o != null) {
				return o;
			}

			for (String key : lookup_hash.keySet()) {
				if (key.equalsIgnoreCase(str)) {
					return lookup_hash.get(key);
				}
			}
		}
		return null;
	}

	/**
	 *  Finds the first synonym in a list that matches the given string.
	 *  @param choices a list of possible synonyms that might match the given test
	 *  @param test  the id you want to find a synonym for
	 *  @return either null or a String s, where isSynonym(test, s) is true.
	 */
	public String findMatchingSynonym(Collection<String> choices, String test) {
		String result = null;
		for (String id : choices) {
			if (this.isSynonym(test, id)) {
				result = id;
				break;
			}
		}
		return result;
	}
}
