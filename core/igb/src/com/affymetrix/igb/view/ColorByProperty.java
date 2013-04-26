package com.affymetrix.igb.view;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.affymetrix.genometryImpl.color.ColorProvider;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithProps;
import com.affymetrix.genoviz.color.ColorPalette;
import com.affymetrix.genoviz.color.ColorScheme;

/**
 *
 * @author hiralv
 */
public class ColorByProperty extends ColorProvider{
	private final static String PROPERTY = "property";
	private final static Map<String, Class<?>> PARAMETERS = new HashMap<String, Class<?>>();
	static {
		PARAMETERS.put(PROPERTY, String.class);
	}
	
	private String property = null;
	ColorPalette cp = new ColorPalette(ColorScheme.ACCENT8);
	
	@Override
	public Color getColor(SeqSymmetry sym){
		if(sym instanceof SymWithProps){
			String value = ((SymWithProps)sym).getProperty(property).toString();
			return cp.getColor(value);
		}
		return null;
	}
	
	@Override
	public Map<String, Class<?>> getParameters(){
		return PARAMETERS;
	}

	@Override
	public boolean setParameter(String key, Object value){
		if(PROPERTY.equals(key) && value instanceof String){
			property = (String)value;
			return true;
		} 
		return false;
	}
	
	@Override
	public Object getParameterValue(String key) {
		if(PROPERTY.equals(key)){
			return property;
		}
		return null;
	}
}
