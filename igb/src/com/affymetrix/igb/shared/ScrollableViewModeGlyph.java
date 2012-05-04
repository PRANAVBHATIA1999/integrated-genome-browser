
package com.affymetrix.igb.shared;

/**
 *
 * @author hiralv
 */
public interface ScrollableViewModeGlyph {
	public void setOffset(int offset);
	public int getOffset();
	public void setScale(float scale);
	public float getScale();
	public boolean isScrollingAllowed();
}
