package cytoscape.visual.ui.editors.continuous;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.TrackRenderer;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class CyGradientTrackRenderer extends JComponent implements TrackRenderer {

    private static final long serialVersionUID = 1L;

    /*
     * Static variables used by the implemeted classes from VizMapperTrackRenderer
     */
    static final Font ICON_FONT = new Font("SansSerif", Font.BOLD, 8);
    static final Font LARGE_FONT = new Font("SansSerif", Font.BOLD, 18);

    static final Color BORDER_COLOR = Color.DARK_GRAY;

    static final BasicStroke STROKE1 = new BasicStroke(1.0f);
    static final BasicStroke STROKE2 = new BasicStroke(2.0f);

    private int trackHeight = 40;
    private final Font SMALL_FONT = new Font("SansSerif", Font.BOLD, 16);
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 12);

    private JXMultiThumbSlider<Color> slider;
    private MultiColorThumbModel model;

    /**
     * DOCUMENT ME!
     *
     * @param g
     * DOCUMENT ME!
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintComponent(g);
    }

    @Override
    protected void paintComponent(Graphics gfx) {
        Graphics2D g = (Graphics2D) gfx;

        // Turn AA on
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float minValue = model.getVirtualMinimum();
        float maxValue = model.getVirtualMaximum();

        // calculate the track area
        int thumb_width = 12;
        int track_width = slider.getWidth() - thumb_width;
        g.translate(thumb_width / 2, 12);

        // get the list of colors
        List<Thumb<Color>> stops = slider.getModel().getSortedThumbs();
        int len = stops.size();

        if (len != 0) {
            // set up the data for the gradient
            float[] fractions = new float[len + 2];
            Color[] colors = new Color[len + 2];
            int i = 1;

            colors[0] = model.getBelowColor();
            fractions[0] = model.getFraction(stops.get(0).getPosition());

            for (Thumb<Color> thumb : stops) {
                colors[i] = thumb.getObject();

                fractions[i] = model.getFraction(thumb.getPosition());

                g.setColor(colors[i]);
                g.setFont(SMALL_FONT);

                String valueString;
                float value = model.getVirtualValue(thumb.getPosition());

                if ((Math.abs(minValue) < 3) || (Math.abs(maxValue) < 3)) {
                    valueString = String.format("%.4f", value);
                } else {
                    valueString = String.format("%.2f", value);
                }

                final int stringWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(),
                        valueString);
                final int curPosition = (int) (track_width * fractions[i]);

                FontRenderContext frc = g.getFontRenderContext();
                TextLayout tl = new TextLayout(valueString, g.getFont(), frc);

                g.setStroke(new BasicStroke(0.6f));
                g.setColor(Color.BLACK);

                final float[] hsb = Color.RGBtoHSB(colors[i].getRed(), colors[i].getGreen(),
                        colors[i].getBlue(), null);

                int x;
                int y;

                if (curPosition < (stringWidth / 2)) {
                    x = curPosition;
                    y = trackHeight + 15;
                } else if ((track_width - curPosition) < (stringWidth / 2)) {
                    x = curPosition - stringWidth;
                    y = trackHeight + 15;
                } else {
                    x = curPosition - (stringWidth / 2);
                    y = trackHeight + 15;
                }

                if (hsb[1] < 0.5) {
                    g.setColor(colors[i]);
                    g.drawString(valueString, x, y);

                    g.translate(x, y);
                    g.setColor(Color.BLACK);
                    g.draw(tl.getOutline(null));
                    g.translate(-x, -y);
                } else {
                    g.setColor(colors[i]);
                    g.drawString(valueString, x, y);
                }

                i++;
            }

            colors[colors.length - 1] = model.getAboveColor();
            fractions[fractions.length - 1] = model.getFraction(stops.get(stops.size() - 1).getPosition());

            g.setStroke(new BasicStroke(1.0f));

            // fill in the gradient
            Point2D start = new Point2D.Float(0, 0);
            Point2D end = new Point2D.Float(track_width, trackHeight);

            drawGradient(g, start, end, fractions, colors);
        }

        // Define rectangle
        Rectangle2D rect = new Rectangle(0, 0, track_width, trackHeight);
        g.setColor(Color.gray);
        g.drawLine((int) rect.getBounds2D().getMinX(), (int) rect.getBounds2D().getMaxY(), 8,
                (int) rect.getBounds2D().getMaxY() + 25);
        g.setFont(SMALL_FONT);
        g.drawString("Min=" + minValue, (int) rect.getBounds2D().getMinX(),
                (int) rect.getBounds2D().getMaxY() + 38);

        g.drawLine((int) rect.getBounds2D().getMaxX(), (int) rect.getBounds2D().getMaxY(),
                (int) rect.getBounds2D().getMaxX() - 8, (int) rect.getBounds2D().getMaxY() + 25);
        g.setFont(SMALL_FONT);

        final String maxString = "Max=" + maxValue;
        g.drawString(maxString,
                (int) rect.getBounds2D().getMaxX()
                - SwingUtilities.computeStringWidth(g.getFontMetrics(), maxString),
                (int) rect.getBounds2D().getMaxY() + 38);

        g.setFont(TITLE_FONT);

        String legendName = "Range";
        final int titleWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), legendName);
        g.setColor(Color.black);
        g.drawString(legendName, ((int) rect.getBounds2D().getWidth() / 2) - (titleWidth / 2),
                (int) rect.getBounds2D().getMaxY() + 33);

        // draw a border
        g.draw(rect);
        g.translate(-thumb_width / 2, -12);
    }

    private static void drawGradient(Graphics2D g, Point2D start, Point2D end, float[] fractions,
            Color[] colors) {
        if (fractions.length < 1) {
            return;
        }

        final int width = (int) (end.getX() - start.getX());
        final int height = (int) (end.getY() - start.getY());

        if (colors.length == 3) {
            final int pivot = (int) (fractions[1] * width);
            g.setColor(colors[0]);
            g.fillRect((int) start.getX(), (int) start.getY(), pivot, height);
            g.setColor(colors[2]);
            g.fillRect(pivot, (int) start.getY(), width - pivot, height);

            g.setColor(colors[1]);
            g.drawLine(pivot, (int) start.getY(), pivot, height);
        } else if (colors.length > 3) {
            int pivot = (int) (fractions[1] * width);
            g.setColor(colors[0]);
            g.fillRect((int) start.getX(), (int) start.getY(), pivot, height);

            int nextPivot;

            for (int i = 1; i < (colors.length - 2); i++) {
                nextPivot = (int) (width * fractions[i + 1]);

                GradientPaint gp = new GradientPaint(pivot, height / 2, colors[i], nextPivot,
                        height / 2, colors[i + 1]);
                g.setPaint(gp);
                g.fillRect(pivot, 0, nextPivot - pivot, height);
                pivot = nextPivot;
            }

            final int lastPivot = (int) (fractions[fractions.length - 1] * width);
            g.setColor(colors[colors.length - 1]);
            g.fillRect(lastPivot, (int) start.getY(), width - lastPivot, height);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param slider
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    public JComponent getRendererComponent(JXMultiThumbSlider slider) {
        this.slider = slider;
        this.model = (MultiColorThumbModel) slider.getModel();
        trackHeight = slider.getHeight() - 50;

        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @param x
     * DOCUMENT ME!
     * @param y
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getObjectInRange(int x, int y) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param x
     * DOCUMENT ME!
     * @param y
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getToolTipForCurrentLocation(int x, int y) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param iconWidth DOCUMENT ME!
     * @param iconHeight DOCUMENT ME!
     * @param mapping DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ImageIcon getTrackGraphicIcon(int iconWidth, int iconHeight) {
        return drawIcon(iconWidth, iconHeight, false);
    }

    private ImageIcon drawIcon(int iconWidth, int iconHeight, boolean detail) {
        if (slider == null) {
            return null;
        }

        final BufferedImage bi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2 = bi.createGraphics();

        // Turn AA on.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /*
         * Fill background
         */
        g2.setColor(Color.white);
        g2.fillRect(0, 0, iconWidth, iconHeight);

        double minValue = slider.getMinimumValue();
        double maxValue = slider.getMaximumValue();
        double range = maxValue - minValue;

        List<Thumb<Color>> stops = slider.getModel().getSortedThumbs();
        int len = stops.size();
        int strWidth;
        float[] fractions = null;
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = null;

        if (detail) {
            end = new Point2D.Float(iconWidth - 3, iconHeight - 30);
        } else {
            end = new Point2D.Float(iconWidth - 3, iconHeight - 9);
        }

        if (len != 0) {
            // set up the data for the gradient
            fractions = new float[len + 2];

            Color[] colors = new Color[len + 2];
            int i = 1;

            colors[0] = model.getBelowColor();
            fractions[0] = stops.get(0).getPosition() / 100;

            for (Thumb<Color> thumb : stops) {
                colors[i] = thumb.getObject();
                fractions[i] = thumb.getPosition() / 100;
                i++;
            }

            colors[colors.length - 1] = model.getAboveColor();
            fractions[fractions.length - 1] = stops.get(stops.size() - 1).getPosition() / 100;

            // fill in the gradient
            drawGradient(g2, start, end, fractions, colors);
        }

        // Draw border line
        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(0, 0, ((Number) end.getX()).intValue(), ((Number) end.getY()).intValue());

        /*
         * draw numbers
         */
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));

        final String minStr = String.format("%.2f", minValue);
        final String maxStr = String.format("%.2f", maxValue);

        g2.setColor(Color.black);

        if (detail && (fractions != null)) {
            String fNum = null;

            for (float fraction : fractions) {
                fNum = String.format("%.2f", (fraction * range) + minValue);

                strWidth = SwingUtilities.computeStringWidth(g2.getFontMetrics(), fNum);

                g2.drawString(fNum, (fraction * iconWidth) - (strWidth / 2), iconHeight - 20);
            }

            g2.drawString(minStr, 0, iconHeight);
            strWidth = SwingUtilities.computeStringWidth(g2.getFontMetrics(), maxStr);
            g2.drawString(maxStr, iconWidth - strWidth - 2, iconHeight);

            g2.setFont(TITLE_FONT);

//			final int titleWidth = SwingUtilities.computeStringWidth(g2.getFontMetrics(), "Legend");
//			g2.setColor(Color.black);
//			g2.drawString("Legend", (iconWidth / 2) - (titleWidth / 2), iconHeight - 5);
            Polygon p = new Polygon();
            p.addPoint(iconWidth, iconHeight - 9);
            p.addPoint(iconWidth - 15, iconHeight - 15);
            p.addPoint(iconWidth - 15, iconHeight - 9);
            g2.fillPolygon(p);
//			g2.drawLine(0, iconHeight - 9, (iconWidth / 2) - (titleWidth / 2) - 3, iconHeight - 9);
//			g2.drawLine((iconWidth / 2) + (titleWidth / 2) + 3, iconHeight - 9, iconWidth,
//			            iconHeight - 9);
        } else {
            g2.drawString(minStr, 0, iconHeight);
            strWidth = SwingUtilities.computeStringWidth(g2.getFontMetrics(), maxStr);
            g2.drawString(maxStr, iconWidth - strWidth - 2, iconHeight);
        }

        return new ImageIcon(bi);
    }

}
