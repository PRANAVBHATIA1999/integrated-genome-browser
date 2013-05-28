
package com.affymetrix.genometryImpl.operator;

import com.affymetrix.genometryImpl.GenometryConstants;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;

/**
 *
 * @author hiralv
 */
public abstract class AbstractAnnotationTransformer implements Operator {
	protected final FileTypeCategory fileTypeCategory;

	public AbstractAnnotationTransformer(FileTypeCategory fileTypeCategory) {
		this.fileTypeCategory = fileTypeCategory;
	}
	
	@Override
	public String getDisplay() {
		return GenometryConstants.BUNDLE.getString("operator_" + getName());
	}
	
	@Override
	public int getOperandCountMin(FileTypeCategory category) {
		return category == this.fileTypeCategory ? 1 : 0;
	}

	@Override
	public int getOperandCountMax(FileTypeCategory category) {
		return category == this.fileTypeCategory ? 1 : 0;
	}

	@Override
	public boolean supportsTwoTrack() {
		return false;
	}

	@Override
	public FileTypeCategory getOutputCategory() {
		return FileTypeCategory.Annotation;
	}
	
	@Override
	public Operator clone(){
		try {
			return getClass().getConstructor(FileTypeCategory.class).newInstance(fileTypeCategory);
		} catch (Exception ex) {
		}
		return null;
	}
}
