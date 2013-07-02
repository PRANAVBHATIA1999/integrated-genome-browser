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

package com.affymetrix.genometryImpl.symmetry;

import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.parsers.FileTypeHandler;
import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Top-level annots attached to a BioSeq.
 */
public final class TypeContainerAnnot extends RootSeqSymmetry implements TypedSym   {
	private static final String[] PROPERTIES_TO_SEARCH = new String[]{"id", "gene name", "description"};
	private static final FileTypeCategory DEFAULT_CATEGORY = FileTypeCategory.Annotation;
	final private TreeMap<String,Set<SeqSymmetry>> id2sym_hash;	// list of names -> sym
	private final String ext;
	private final boolean index;
	private final String type;
	
	public TypeContainerAnnot(String type) {
		this(type,"");
	}

	public TypeContainerAnnot(String type, String ext) {
		super();
		this.setProperty("method", type);
		this.setProperty(CONTAINER_PROP, Boolean.TRUE);
		this.type   = type;
		this.ext    = ext;
		this.index  = false;
		id2sym_hash = !index ? null : new TreeMap<String,Set<SeqSymmetry>>() ;
	}

	public String getType()  { return type; }

	@Override
	public FileTypeCategory getCategory() {
		FileTypeCategory category = null;
		FileTypeHandler handler = FileTypeHolder.getInstance().getFileTypeHandler(ext);
		if (handler != null) {
			category = handler.getFileTypeCategory();
		}
		if (category == null) {
			category = DEFAULT_CATEGORY;
		}
		return category;
	}
	
	@Override
	public void addChild(SeqSymmetry sym) {
		super.addChild(sym);
		if(index){
			addToIndex(sym.getID(), sym);
			if(sym instanceof SupportsGeneName){
				addToIndex(((SupportsGeneName)sym).getGeneName(), sym);
			}
		}
	}
	
	private void addToIndex(String key, SeqSymmetry sym){
		if (key != null && key.length() > 0) {
			Set<SeqSymmetry> seq_list = id2sym_hash.get(key);
			if (seq_list == null) {
				seq_list = new LinkedHashSet<SeqSymmetry>();
				id2sym_hash.put(key, seq_list);
			}
			seq_list.add(sym);
		}
	}
	
	@Override
	public void searchHints(List<String> results, Pattern regex, int limit) {	
		final Matcher matcher = regex.matcher("");
		int size = Math.min(limit, id2sym_hash.size());
		int count = results.size();
		
		for (String key : id2sym_hash.keySet()) {
			matcher.reset(key);
			if (matcher.matches()) {
				results.add(key);
				
				count++;
				if(count > size){
					break;
				}
			}
		}
	}
	
	@Override
	public void search(List<SeqSymmetry> results, Pattern regex, int limit) {
		int size;
		int count;
		if(limit > 0){
			size  = Math.min(limit, id2sym_hash.size());
			count = results.size();
		}else{
			size  = -1;
			count = Integer.MIN_VALUE;
		}
		final Matcher matcher = regex.matcher("");
		Thread current_thread = Thread.currentThread();
		for (Map.Entry<String, Set<SeqSymmetry>> ent : id2sym_hash.entrySet()) {
			if (current_thread.isInterrupted() || count > size) {
				break;
			}

			String seid = ent.getKey();
			Set<SeqSymmetry> val = ent.getValue();
			if (seid != null && val != null) {
				matcher.reset(seid);
				if (matcher.matches()) {
					results.addAll(val);
					count++;
				}
			}
		}
	}
	
	@Override
	public void searchProperties(List<SeqSymmetry> results, Pattern regex, int limit) {
		int size;
		int count;
		if(limit > 0){
			size  = Math.min(limit,id2sym_hash.size());
			count = results.size();
		}else{
			size  = -1;
			count = Integer.MIN_VALUE;
		}
		
		final Matcher matcher = regex.matcher("");
		SymWithProps swp;
		String match;
		Thread current_thread = Thread.currentThread();
		for (Map.Entry<String, Set<SeqSymmetry>> ent : id2sym_hash.entrySet()) {
			if(current_thread.isInterrupted()) {
				break;
			}
			
			for (SeqSymmetry seq : ent.getValue()) {
				if(current_thread.isInterrupted()) {
					break;
				}
				
				if (seq instanceof SymWithProps) {
					swp = (SymWithProps) seq;

					// Iterate through each properties.
					for (Map.Entry<String, Object> prop : swp.getProperties().entrySet()) {
						if(current_thread.isInterrupted() || count > size) {
							break;
						}
						
						if (prop.getValue() != null) {
							match = prop.getValue().toString();
							matcher.reset(match);
							if (matcher.matches()) {
								results.add(seq);
								count++;
							}
						}
					}
				}
			}
		}
	}
}
