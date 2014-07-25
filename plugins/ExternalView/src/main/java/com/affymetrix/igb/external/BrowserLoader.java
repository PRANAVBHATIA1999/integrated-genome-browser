package com.affymetrix.igb.external;

import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import com.google.common.io.Resources;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for browser loaders returns image upon query with: query(genome +
 * region) pixwidth (width of image) cookies
 *
 * @author Ido M. Tamir
 */
public abstract class BrowserLoader {

    static final Logger logger = Logger.getLogger(BrowserLoader.class.getName());

    public static BufferedImage createErrorImage(String error, int pixWidth) {
        final BufferedImage image = new BufferedImage(pixWidth, 70, BufferedImage.TYPE_3BYTE_BGR);
        image.createGraphics();
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, pixWidth, 70);
        final Font font = new Font("Serif", Font.PLAIN, 12);
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(error, 30, 20);
        return image;
    }

    abstract public BufferedImage getImage(Loc loc, int pixWidth, Map<String, String> cookies) throws ImageUnavailableException;

    /**
     *
     * @param url the UCSC genome/region url
     * @param userId the UCSC userId (hguid cookie value)
     * @return url of the image of the region
     */
    public String getImageUrl(String url, String cookie, URLFinder urlfinder) throws ImageUnavailableException {
        Closer closer = Closer.create();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(Resources.asByteSource(new URL(url)).openBufferedStream()));
            closer.register(in);
            return urlfinder.findUrl(in, null);
        } catch (Throwable t) {
            //wrap exception if needed
            if (!(t instanceof ImageUnavailableException)) {
                logger.log(Level.WARNING, null, t.getStackTrace());
                throw new ImageUnavailableException();
            }
            throw (ImageUnavailableException) t;
        } finally {
            try {
                closer.close();
            } catch (IOException ex) {
                logger.log(Level.WARNING, "IOException thrown while closing Closeable.", ex);
                throw new ImageUnavailableException();
            }
        }
    }
}
