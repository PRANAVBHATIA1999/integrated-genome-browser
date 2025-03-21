/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.view.welcome;

import be.pwnt.jflow.Configuration;
import be.pwnt.jflow.Shape;
import com.affymetrix.common.CommonUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.lorainelab.igb.synonymlookup.services.SpeciesSynonymsLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

/**
 * Loads pictures into the Cover Flow Welcome screen.
 *
 * This configuration class can control multiple setting on the cover flow component Refer to the parent class for more
 * information.
 *
 * The class reads a resource file at $IGB_HOME/common/display_species.txt to configure the data sets displayed on the
 * welcome screen.<br>
 * The file should be of the form<br>
 * [image file][tab][Data set to load upon click][tab][Name to display on tag][tab][color] An example is next:<br>
 * a_lyrata.png	A_lyrata_Apr_2011	A. lyrata #000000
 *
 * @author jfvillal
 */
public class GeneConfiguration extends Configuration {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GeneConfiguration.class);

//	private static final SynonymLookup LOOKUP = SynonymLookup.getDefaultLookup();
//	private static final GenometryModel gmodel = GenometryModel.getInstance();
//	private static final Color COLOR_1 = Color.WHITE;
    private static final Color COLOR_2 = new Color(0xfffb86);//Color.YELLOW;
//	private static final float FONT_SIZE_1 = 36.0f;
//	private static final float FONT_SIZE_2 = 22.0f;
//	private static final float FONT_SIZE_3 = 12.0f;
    public final static int THUMB_WIDTH = 250;
    public final static int THUMB_HEIGHT = 250;

    public GeneConfiguration() {
//		String os = System.getProperty("os.name");
        this.zoomFactor = 0.1;
        this.zoomScale = 1.0;
        this.shapeRotation = 0.0;
        this.shapeSpacing = 1.2 / 3.0;
        this.shadingFactor = 0;
        this.reflectionOpacity = 0.0;

        /*if(os.equals("Mac OS X") || os.equals("Mac OS") ){
         this.SlowSystem = true;
         this.reflectionOpacity = 0.0;
         this.highQuality = false;
         }else{*/
        this.SlowSystem = false;
        this.highQuality = true;
        //}

        URL config_file_url = CommonUtils.class.getClassLoader().getResource("display_species.txt");
        try (BufferedReader stream = new BufferedReader(new InputStreamReader(new DataInputStream(config_file_url.openStream())))) {
            //load the messge class from a configuration file.
            List<String> list = new ArrayList<>();
            //this is at $IGB_SRC/common/rerources and image paths are relative to $IGB_SRC/common/images/

            String line = "";
            try {
                while ((line = stream.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }

                    //tripple check no null message is being inserted into the list.
                    if (line != null) {
                        list.add(line);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                System.err.println("The display_species.txt may not comply with specification.  make sure it has \n"
                        + "[image file][tab][Data set to load upon click][tab][Name to display on tag][tab][color (example #122322]");
                e.printStackTrace();
            }

            this.activeShapeBorderColor = Color.white;
            this.framesPerSecond = 30;
            shapes = new Shape[list.size()];
            for (int i = 0; i < shapes.length; i++) {

                try {
                    String version = list.get(i);
                    URL url = CommonUtils.class.getClassLoader().getResource("images/" + version);

                    if (url == null) {
                        url = CommonUtils.class.getClassLoader().getResource("images/default.png");
                    }

                    BufferedImage img = ImageIO.read(url);
                    Graphics2D g = img.createGraphics();

                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                    Font f = new Font("Helvetica", Font.BOLD, 24);
                    //ImageIO.write( img, "png", new File("saved.png") );
                    FontMetrics metrics = g.getFontMetrics(f);
                    g.setColor(new Color(0xd4d4d4));
                    g.setFont(f);
                    version = version.split(".png")[0];

                    Bundle bundle = FrameworkUtil.getBundle(GeneConfiguration.class);
                    SpeciesSynonymsLookup speciesSynLookup = null;
                    if (bundle != null) {
                        BundleContext bundleContext = bundle.getBundleContext();
                        ServiceReference<SpeciesSynonymsLookup> serviceReference = bundleContext.getServiceReference(SpeciesSynonymsLookup.class);
                        speciesSynLookup = bundleContext.getService(serviceReference);
                    }
                    // e.g., Homo sapiens 
                    // Note: this is ultimately coming from the first column of the file species.txt
                    // If you want to add a new image to the start screen, then you have to make
                    // sure that there is an entry for same in species.txt
                    // Also note that species.txt can also be obtained from a Quickload site. When
                    // IGB launches, it tries to get a species.txt from every Quickload site
                    String species = speciesSynLookup.getSpeciesName(version);
                    String speciesName = species;
                    //If name is very long shorten the name by abbreviating the first name.
                    if (species.length() > 8) {
                        String delims = "[ ]+";
                        String[] tokens = species.split(delims);
                        // first letter of genus followed by period
                        species = tokens[0].substring(0, 1).toUpperCase() + "."; 
                        // add species name, variety name (if present), and so on
                        for (int j = 1; j < tokens.length; j++) {
                            species += " " + tokens[j];
                        }
                    }
                    // IGBF-2294
                    // species="IGB Helper";
                    int num = metrics.stringWidth(species);
                    try {
                        g.setColor(Color.BLACK);
                        g.fill(new Rectangle2D.Double(0, img.getHeight() - 20, img.getWidth(), metrics.getHeight() + 4));
                        //draw the label
                        g.setColor(COLOR_2);
                        g.drawString(species, img.getWidth() / 2 - num / 2, img.getHeight() - 4);
                    } catch (Exception x) {
                    } // ignore NPE
                    CargoPicture n = null;
                    if (!this.SlowSystem) {
                        n = new CargoPicture(img);
                    } else {
                        n = new CargoPicture(scaleImage(img, 10));
                    }
                    // This next line determines which species' genome will
                    // be opened when user clicks a start screen image
                    // For example, hard-coding this as in the next (commented)
                    // line causes the latest human reference genome to be opened
                    // no matter what. 
                    // n.setCargo("Homo sapiens");
                    n.setCargo(speciesName);
                    shapes[i] = n;

                } catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    /**
     * from http://stackoverflow.com/questions/1324106/jai-change-jpeg-resolution
     */
    BufferedImage scaleImage(BufferedImage sourceImage, int scaledWidth) {
        float scale = scaledWidth / (float) sourceImage.getWidth();
        int scaledHeight = (int) (sourceImage.getHeight() * scale);
        Image scaledImage = sourceImage.getScaledInstance(
                scaledWidth,
                scaledHeight,
                Image.SCALE_AREA_AVERAGING);

        BufferedImage bufferedImage = new BufferedImage(
                scaledImage.getWidth(null),
                scaledImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }

    public double getGrayScale(Color col) {
        int g = col.getGreen();
        int b = col.getBlue();
        int r = col.getRed();
        double gray_scale = (g + r + b) / 3.0;
        return gray_scale;
    }
}
