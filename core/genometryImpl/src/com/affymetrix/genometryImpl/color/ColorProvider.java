package com.affymetrix.genometryImpl.color;

import com.affymetrix.genometryImpl.style.ITrackStyle;
import java.awt.Color;
import java.util.Map;

/**
 * A helper class to be used when color is to extracted for each object.
 * @author hiralv
 */
public abstract class ColorProvider {
	
	/**
	 * Get color for the given object
	 * @param obj
	 * @return 
	 */
	public abstract Color getColor(Object obj);
	
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
