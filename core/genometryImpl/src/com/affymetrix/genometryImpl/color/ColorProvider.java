package com.affymetrix.genometryImpl.color;

import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A helper class to be used when color is to extracted for each object.
 * @author hiralv
 */
public abstract class ColorProvider {
	public final static Map<String, Class<? extends ColorProvider>> OPTIONS;
	static {
		OPTIONS = new LinkedHashMap<String, Class<? extends ColorProvider>>();
		OPTIONS.put("None", null);
		OPTIONS.put("RGB", RGB.class);
		OPTIONS.put("Score", Score.class);
		OPTIONS.put("Strand", Strand.class);
	}
	
	public static ColorProvider getCPInstance(Class<? extends ColorProvider> clazz) {
		try {
			if(clazz != null){
				return clazz.getConstructor().newInstance();
			}
		} catch (Exception ex) {
			
		}
		return null;
	}
	
	public static String getCPName(Class<? extends ColorProvider> clazz) {
		for (Entry<String, Class<? extends ColorProvider>> entry : OPTIONS.entrySet()) {
			if (entry.getValue() == clazz) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Get color for the given object
	 * @param sym
	 * @return 
	 */
	public abstract Color getColor(SeqSymmetry sym);
	
	public void update() {
		//Do Nothing
	}
	
	public Map<String, Class<?>> getParameters(){
		return null;
	}

	public void setParameters( Map<String, Object> params){
		//Do Nothing
	}
	
	public boolean setParameter(String key, Object value) {
		return false;
	}

	public Object getParameterValue(String key) {
		return null;
	}
	
}
