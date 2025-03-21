/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometry.symmetry.impl;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.symmetry.MutableSeqSymmetry;
import java.util.ArrayList;

public class MutableSingletonSeqSymmetry
        extends SingletonSeqSymmetry
        implements MutableSeqSymmetry {

    protected CharSequence id;

    public MutableSingletonSeqSymmetry(int start, int end, BioSeq seq) {
        super(start, end, seq);
    }

    public MutableSingletonSeqSymmetry(CharSequence id, int start, int end, BioSeq seq) {
        this(start, end, seq);
        this.id = id;
    }

    public void addChild(SeqSymmetry sym) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(sym);
    }

    public void removeChild(SeqSymmetry sym) {
        children.remove(sym);
    }

    public void removeChildren() {
        children = null;
    }

    /**
     * Operation not allowed, it will throw an exception.
     */
    public void removeSpans() {
        throw new RuntimeException("can't removeSpans(), MutableSingletonSeqSymmetry is not mutable itself, only its children");
    }

    /**
     * Operation not allowed, it will throw an exception.
     */
    public void clear() {
        throw new RuntimeException("can't clear(), MutableSingletonSeqSymmetry is not mutable itself, only its children");
    }

    /**
     * Operation not allowed, it will throw an exception.
     */
    public void addSpan(SeqSpan span) {
        throw new RuntimeException("Operation Not Allowed. Can't add a span to a SingletonSeqSymmetry.");
    }

    /**
     * Operation not allowed, it will throw an exception.
     */
    public void removeSpan(SeqSpan span) {
        throw new RuntimeException("Operation Not Allowed. Can't remove a span froma a SingletonSeqSymmetry.");
    }

    public String getID() {
        return id != null ? id.toString() : null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
