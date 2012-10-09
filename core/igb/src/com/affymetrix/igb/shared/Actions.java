package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.igb.action.*;
import java.awt.Color;

/**
 *
 * @author hiralv
 */
public class Actions {
	
	public static void setForegroundColor(Color color){
		ParameteredAction action = ChangeForegroundColorAction.getAction();
		action.performAction(color);
	}
	
	public static void setBackgroundColor(Color color){
		ParameteredAction action = ChangeBackgroundColorAction.getAction();
		action.performAction(color);
	}
	
	public static void setLabelColor(Color color){
		ParameteredAction action = ChangeLabelColorAction.getAction();
		action.performAction(color);
	}
	
	public static void setTierFontSize(int fontSize){
		ParameteredAction action = TierFontSizeAction.getAction();
		action.performAction(fontSize);
	}
		
	public static void setStackDepth(int depth) {
		ParameteredAction action = ChangeExpandMaxAction.getAction();
		action.performAction(depth);
	}
	
	public static void setLabelField(String labelField) {
		ParameteredAction action = LabelGlyphAction.getAction();
		action.performAction(labelField);
	}
	
	public static void setStrandsReverseColor(Color color) {
		if(color == null)
			return;
		
		ParameteredAction action = ChangeReverseColorAction.getAction();
		action.performAction(color);
	}
	
	public static void setStrandsForwardColor(Color color){
		if(color == null)
			return;
		
		ParameteredAction action = ChangeForwardColorAction.getAction();
		action.performAction(color);
	}
	
	/**
	 * @param showOneTier Show one tier
	 */
	public static void showOneTwoTier(boolean showOneTier) {
		GenericAction action = showOneTier ? 
				ShowOneTierAction.getAction() : ShowTwoTiersAction.getAction();
		
		action.actionPerformed(null);
	}
	
	/**
	 * @param showArrow Show arrow
	 */
	public static void showArrow(boolean showArrow) {
		GenericAction action = showArrow ? 
				SetDirectionStyleArrowAction.getAction() : UnsetDirectionStyleArrowAction.getAction();
		action.actionPerformed(null);
	}
	
	/**
	 * @param showColor Show color
	 */
	public static void showStrandsColor(boolean showColor) {
		GenericAction action = showColor?
				SetDirectionStyleColorAction.getAction() : UnsetDirectionStyleColorAction.getAction();
		action.actionPerformed(null);
	}
		
	public static void setLockedTierHeight(int height) {
		ParameteredAction action = ChangeTierHeightAction.getAction();
		action.performAction(height);
	}
	
	/**
	 * @param floatTier Float tier
	 */
	public static void setFloatTier(boolean floatTier){
		GenericAction action = floatTier ? 
				FloatTiersAction.getAction() : UnFloatTiersAction.getAction();
		action.actionPerformed(null);
	}
	
	public static void setRenameTier(ITrackStyleExtended style, String name){
		ParameteredAction action = RenameTierAction.getAction();
		action.performAction(style, name);
	}

}
