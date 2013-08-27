package com.affymetrix.genometryImpl.color;

import com.affymetrix.genometryImpl.general.IParameters;
import com.affymetrix.genometryImpl.general.Parameters;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import java.util.Map;

/**
 * A helper class to be used when color is to extracted for each object.
 * @author hiralv
 */
public abstract class ColorProvider implements ColorProviderI, IParameters {
	protected Parameters parameters;
	
	protected ColorProvider(){
		parameters = new Parameters();
	}
	
	@Override
	public Map<String, Class<?>> getParametersType(){
		return parameters.getParametersType();
	}

	@Override
	public final boolean setParametersValue(Map<String, Object> params){
		return parameters.setParametersValue(params);
	}
	
	@Override
	public boolean setParameterValue(String key, Object value) {
		return parameters.setParameterValue(key, value);
	}

	@Override
	public Object getParameterValue(String key) {
		return parameters.getParameterValue(key);
	}
	
	@Override
	public String getName(){
		return getClass().getName();
	}
	
	@Override
	public String getDisplay(){
		return getClass().getSimpleName();
	}
	
	@Override
	public boolean isFileTypeCategorySupported(FileTypeCategory fileTypeCategory) {
		return fileTypeCategory == FileTypeCategory.Annotation 
				|| fileTypeCategory == FileTypeCategory.Alignment
				|| fileTypeCategory == FileTypeCategory.ProbeSet;
	}
	
	@Override
	public ColorProvider newInstance(){
		try {
			return getClass().getConstructor().newInstance();
		} catch (Exception ex) {
		}
		return null;
	}
}
