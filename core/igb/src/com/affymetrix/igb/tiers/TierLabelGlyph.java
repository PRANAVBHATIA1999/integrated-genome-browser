package com.affymetrix.igb.tiers;

import com.affymetrix.genometryImpl.util.StringUtils;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.glyph.SolidGlyph;
import com.affymetrix.genoviz.util.NeoConstants;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.igb.view.factories.DefaultTierGlyph;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.net.URL;

/**
 * A glyph used to display a label for a TierGlyph.
 */
public final class TierLabelGlyph extends SolidGlyph implements NeoConstants {
	private static final double FUDGE_FACTOR = 0.05;
	private static final String LOADING_IMG_NAME = "images/hourglass.png";
	private static final Image LOADING_IMG;
	static {
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		if(loader == null) {
			LOADING_IMG = null;
		}
		else {
			URL url = loader.getResource(LOADING_IMG_NAME);
			if(url == null) {
				url = loader.getResource("/"+LOADING_IMG_NAME);
			}
			if(url == null) {
				LOADING_IMG = null;
			}
			else {
				Toolkit tk = Toolkit.getDefaultToolkit();
				LOADING_IMG = tk.getImage(url);
			}
		}
	}
	private static int pbBuffer_x = 5;
	private static Color IGBTrackMakerColor = Color.YELLOW;
	private int position;
	private static final int placement = CENTER;
	private boolean isIGBTrack;
	private final TierGlyph reference_tier;
	private boolean isLoading;

	@Override
	public String toString() {
		return ("TierLabelGlyph: label: \"" + getLabelString() + "\"  +coordbox: " + getCoordBox());
	}

	/**
	 * @param tier in the main part of the AffyLabelledTierMap.
	 *        It must not be null.
	 */
	public TierLabelGlyph(TierGlyph tier, int position) {
		reference_tier = tier;
		setInfo(reference_tier);
		setPosition(position);
		isIGBTrack = false;/*reference_tier.getFileTypeCategory() == null;*/
	}

	public void setShowIGBTrack(boolean b) {
		isIGBTrack = b && reference_tier.getFileTypeCategory() == null;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public void drawTraversal(ViewI view) {
		super.drawTraversal(view);
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
	}

	/**
	 * Overridden such that the info must be of type TierGlyph. It is used to
	 * store the reference tier that will be returned by getReferenceTier().
	 */
	@Override
	public void setInfo(Object o) {
		if (o == null) {
			throw new IllegalArgumentException("Null input parameter to setInfo() method in TierLabelGlyph found.");
		}
		if (!(o instanceof TierGlyph)) {
			String msg = "Invalid type " + o.getClass().getName() + " found in input parameter ";
			msg += "for setInfo() method in TierLabelGlyph.  Type TierGlyph required.";
			throw new IllegalArgumentException(msg);
		}
		super.setInfo(o);
	}

	/**
	 * Returns the reference tier from the main map in AffyLabelledTierMap.
	 * Equivalent to value returned by getInfo(). Will not be null.
	 */
	public TierGlyph getReferenceTier() {
		return (TierGlyph) getInfo();
	}

	private static String getDirectionString(TierGlyph tg) {
		return tg.getDirection().getDisplay();
	}

	/**
	 * Returns the label of the reference tier, or some default string if there
	 * isn't one.
	 *
	 * @return string
	 */
	private String getLabelString() {
		TierGlyph reference_tier = getReferenceTier();
		if (reference_tier.getAnnotStyle().getTrackName() == null) {
			return ".......";
		}
		String direction_str = getDirectionString(reference_tier);
		return reference_tier.getAnnotStyle().getTrackName() + direction_str;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public void setLoading(boolean isLoading) {
		if (isLoading == this.isLoading) {
			return;
		}
		this.isLoading = isLoading;
		for (ViewI view : getScene().getViews()) {
			draw(view);
		}
	}

	@Override
	public void draw(ViewI view) {
		TierGlyph reftier = this.getReferenceTier();
		ITrackStyleExtended trackStyle = reftier.getAnnotStyle();
		Color fgcolor = trackStyle.getLabelForeground();
		Color bgcolor = trackStyle.getBackground();

		final Graphics g = view.getGraphics();
		g.setPaintMode();

		//bgcolor = trackStyle.getLabelBackground();
		Font newfnt = g.getFont().deriveFont(trackStyle.getTrackNameSize());
		g.setFont(newfnt);

		final Rectangle pixelbox = new Rectangle();
		view.transformToPixels(getCoordBox(), pixelbox);

		if (bgcolor != null) {
			g.setColor(bgcolor);
			g.fillRect(pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height);
		}
		if (192 < bgcolor.getRed() + bgcolor.getGreen() + bgcolor.getBlue()) {
			g.setColor(Color.BLACK);
		}
		else {
			g.setColor(Color.GRAY);
		}
		if (this.isSelected()) {
			g.setColor(view.getScene().getSelectionColor());
		}
		g.drawRect(pixelbox.x, pixelbox.y, pixelbox.width - 1, pixelbox.height);
		g.drawRect(pixelbox.x + 1, pixelbox.y + 1, pixelbox.width - 3, pixelbox.height - 3);
		g.setColor(fgcolor);

		if (isIGBTrack) {
			g.setColor(IGBTrackMakerColor);
			g.fillRect(pixelbox.x + 1, pixelbox.y + 1, pbBuffer_x, pixelbox.height - 3);
			g.setColor(fgcolor);
		}

		drawLabel(g, view.getPixelBox(), pixelbox);
		if (isLoading) {
			drawLoading(g, pixelbox);
		}
		this.textCoordHeight = view.transformToCoords(new Rectangle(0, this.textPixelHeight), new Rectangle2D.Double()).height;
		
		if(reftier instanceof DefaultTierGlyph && ((DefaultTierGlyph)reftier).isHeightFixed() && TrackStyle.getShowLockIcon()){
			g.setColor(fgcolor);
			g.fillRect(pixelbox.x + 5, pixelbox.y + 10, 10, 10);
			g.fillArc(pixelbox.x + 6, pixelbox.y + 2, 8, 15, 0, 180);
		
			g.setColor(bgcolor);
			g.fillArc(pixelbox.x + 8, pixelbox.y + 4, 4, 12, 0, 180);
			g.fillRect(pixelbox.x + 9, pixelbox.y + 15, 2, 4);
		}
		super.draw(view);
	}
	private int textPixelHeight;
	private double textCoordHeight;

	private void drawLoading(final Graphics g, Rectangle pixelbox) {
		if (LOADING_IMG == null || pixelbox.width == 0 || pixelbox.height == 0) {
			return;
		}
		int clockWidth = LOADING_IMG.getWidth(null);
		int clockHeight = LOADING_IMG.getHeight(null);
		if (clockWidth == -1 || clockHeight == -1) {
			return;
		}
		double ratio = ((double)clockWidth) / ((double)clockHeight);
		int width = Math.min(clockWidth, pixelbox.width / 2);
		int height = Math.min(clockHeight, pixelbox.height / 2);
		double currentRatio = ((double)width) / ((double)height);
		if (currentRatio < (ratio - FUDGE_FACTOR)) {
			height = (int)Math.round(width / ratio);
		}
		else if (currentRatio > (ratio + FUDGE_FACTOR)) {
			width = (int)Math.round(ratio * height);
		}
		int x = (pixelbox.width - width) / 2; // pixelbox.x + ... does not work
		int y = pixelbox.y + (pixelbox.height - height) / 2;
        ImageObserver observer = null;
		g.drawImage(LOADING_IMG, x, y, width, height, observer);
	}

	private void drawLabel(Graphics g, Rectangle boundingPixelBox, Rectangle pixelbox) {
		// assumes that pixelbox coordinates are already computed

		String label = getLabelString();
		// this was for test:
		// label = "hey_this_is_going_to_be-a-long-text-to-test.the.behaviour";
		//label = "abc DEfgHIj  klMn		OPqRstUv  w xyz.  Antidisestablishmentarianism.  The quick brown fox jumps over a lazy dog.";

		FontMetrics fm = g.getFontMetrics();
		//int text_height = fm.getAscent() + fm.getDescent();
		int text_height = fm.getHeight();
		this.textPixelHeight = text_height;

		// Lower bound of visible glyph
		int lowerY = Math.max(pixelbox.y, boundingPixelBox.y);

		// Upper bound of visible glyph
		int upperY = Math.min(
				pixelbox.y + pixelbox.height,
				boundingPixelBox.y + boundingPixelBox.height);

		int text_width = fm.stringWidth(label);
		TierGlyph.Direction direction = getReferenceTier() != null ? getReferenceTier().getDirection() : TierGlyph.Direction.NONE;
		if (text_width + (pbBuffer_x * 2) > pixelbox.width) {
			drawWrappedLabel(label, fm, g, lowerY, upperY, text_height, pixelbox, direction);
		} else {
			// if glyph's pixelbox wider than text, then center text
			pixelbox.x += pixelbox.width / 2 - text_width / 2;
			g.drawString(label, pixelbox.x, (lowerY + upperY + text_height) / 2);
		}
	}

	@SuppressWarnings("unused")
	private static void drawWrappedLabel(String label, FontMetrics fm, Graphics g, int lowerY, int upperY, int text_height, Rectangle pixelbox, TierGlyph.Direction direction) {
		int maxLines = (upperY - lowerY) / text_height;
		if (maxLines == 0) {
			return;
		}
		String[] lines = StringUtils.wrap(label, fm, pixelbox.width - (pbBuffer_x * 2), maxLines);
		//pixelbox.x += pbBuffer_x;
		int height = (upperY + lowerY - text_height * (lines.length - 2)) / 2;

		int text_width;
		int x;
		for (String line : lines) {
			text_width = fm.stringWidth(line);
			//Remark: the "height-3" parameter in the drawString function is a fine-tune to center vertically.
			if (placement == LEFT) {
				x = pixelbox.x - text_width;
			} else if (placement == RIGHT) {
				x = pixelbox.x + pixelbox.width - text_width;
			} else {
				x = pixelbox.x + pixelbox.width / 2 - text_width / 2;
			}
			g.drawString(line, x, height - 3);
			height += text_height;
		}
	}

	/**
	 * Draws the outline in a way that looks good for tiers. With other glyphs,
	 * the outline is usually drawn a pixel or two larger than the glyph. With
	 * TierGlyphs, it is better to draw the outline inside of or contiguous with
	 * the glyph's borders.
	 *
	 */
	@Override
	protected void drawSelectedOutline(ViewI view) {
		draw(view);

		Graphics g = view.getGraphics();
		g.setColor(view.getScene().getSelectionColor());
		Rectangle pixelbox = new Rectangle();
		view.transformToPixels(getPositiveCoordBox(), pixelbox);
		g.drawRect(pixelbox.x, pixelbox.y,
				pixelbox.width - 1, pixelbox.height - 1);

		g.drawRect(pixelbox.x + 1, pixelbox.y + 1,
				pixelbox.width - 3, pixelbox.height - 3);
	}

	public boolean isManuallyResizable() {
		Object o = getInfo();
		if (o instanceof TierGlyph) {
			TierGlyph t = (TierGlyph) o;
			return t.isManuallyResizable();
		}
		return false;
	}

	public void resizeHeight(double top, double height) {
		Rectangle2D.Double cbox = getCoordBox();
		setCoords(cbox.x, top, cbox.width, height);
	}

	/**
	 * How small can this tier label be made? We still want to be able to see
	 * the beginning of the text. - elb
	 *
	 * @return size in pixels like a JComponent would.
	 */
	public Dimension getMinimumSize() {
		Dimension answer = new Dimension(0, this.textPixelHeight);
		return answer;
	}

	/**
	 * How short can this tier label be made? We still want to be able to see
	 * the beginning of the text. - elb
	 *
	 * @return height in coordinate space.
	 */
	public Double getMinimumHeight() {
		Double answer = this.textCoordHeight;
		return answer;
	}
}
