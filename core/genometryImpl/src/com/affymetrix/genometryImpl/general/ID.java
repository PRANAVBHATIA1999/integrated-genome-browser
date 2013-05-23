package com.affymetrix.genometryImpl.general;

/**
 *
 * @author hiralv
 */
public interface ID {
	
	/**
	 * Unique identifier, MUST be unique,
	 * and should not be displayed to the users, use getDisplay().
	 * This should help you keep track of different operators.
	 * Note that this should be different for each instance.
	 * @return a name suitable for identifying this operator.
	 */
	public String getName();

	/**
	 * user display
	 * @return a string suitable for showing a user.
	 */
	public String getDisplay();
}
