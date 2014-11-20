package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author dcnorris
 */
public class PreprocessorTypeReference {

    private final Table<String, FileTypeCategory, GlyphPreprocessorI> preprocessorTypeReferenceTable;

    private PreprocessorTypeReference() {
        preprocessorTypeReferenceTable = HashBasedTable.create();
    }

    public static PreprocessorTypeReference getInstance() {
        return PreprocessorTypeReferenceHolder.INSTANCE;
    }

    private static class PreprocessorTypeReferenceHolder {

        private static final PreprocessorTypeReference INSTANCE = new PreprocessorTypeReference();
    }

    public void addPreprocessor(FileTypeCategory category, GlyphPreprocessorI factory) {
        checkNotNull(category);
        checkNotNull(factory);
        if (!preprocessorTypeReferenceTable.contains(factory.getName(), category)) {
            preprocessorTypeReferenceTable.put(factory.getName(), category, factory);
        }
    }

    public void removePreprocessor(GlyphPreprocessorI factory, FileTypeCategory category) {
        checkNotNull(factory);
        if (preprocessorTypeReferenceTable.containsValue(factory)) {
            preprocessorTypeReferenceTable.remove(factory.getName(), category);
        }
    }

    public Collection<GlyphPreprocessorI> getPreprocessorsForType(FileTypeCategory category) {
        checkNotNull(category);
        if (preprocessorTypeReferenceTable.columnMap().containsKey(category)) {
            return preprocessorTypeReferenceTable.columnMap().get(category).values();
        }
        return Collections.<GlyphPreprocessorI>emptyList();
    }

}
