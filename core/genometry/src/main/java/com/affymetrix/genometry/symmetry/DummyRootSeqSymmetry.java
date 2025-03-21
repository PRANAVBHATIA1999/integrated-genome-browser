package com.affymetrix.genometry.symmetry;

import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import java.util.Set;
import java.util.regex.Pattern;

public class DummyRootSeqSymmetry extends RootSeqSymmetry {

    private final FileTypeCategory category;

    public DummyRootSeqSymmetry(FileTypeCategory category) {
        this.category = category;
    }

    @Override
    public FileTypeCategory getCategory() {
        return category;
    }

    @Override
    public void search(Set<SeqSymmetry> results, String id) {
    }

    @Override
    public void searchHints(Set<String> results, Pattern regex, int limit) {
    }

    @Override
    public void search(Set<SeqSymmetry> result, Pattern regex, int limit) {
    }

    @Override
    public void searchProperties(Set<SeqSymmetry> results, Pattern regex, int limit) {
    }
}
