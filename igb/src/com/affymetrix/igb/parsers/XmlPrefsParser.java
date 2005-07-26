/**
*   Copyright (c) 2001-2004 Affymetrix, Inc.
*    
*   Licensed under the Common Public License, Version 1.0 (the "License").
*   A copy of the license must be included with any distribution of
*   this source code.
*   Distributions from Affymetrix, Inc., place this in the
*   IGB_LICENSE.html file.  
*
*   The license is also available at
*   http://www.opensource.org/licenses/cpl.php
*/

package com.affymetrix.igb.parsers;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.Color;
import javax.swing.KeyStroke;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;

import com.affymetrix.igb.glyph.*;
import com.affymetrix.igb.util.ObjectUtils;
import com.affymetrix.igb.view.PluginInfo;

/**
 *Class for parsing preferences for IGB.
 *<pre>
Description of <annotation_style> element format

---------------------------------------------------------------------

One form of type attribute is required, possible attribute names are:
       annot_type="val"
       annot_type_starts_with="val"
       annot_type_ends_with="val"
       annot_type_regex="val"
There can also be any combination of these type attributes.

So the simplest <annotation_style> example possible would be:
  <annotation_style annot_type="xyz" />

Although note that the above example is not very useful without using some
of the options discussed below.  Because if an annotation is loaded that has no
<annotation_style> entry in the prefs file(s), then a default annotation style
is automatically assigned, which has the same effect as the above
<annotation_style> entry...

All other attributes and sub-elements are optional, and are either
unnecessary or automatically assigned a default if not present.

Optionally specifying the factory class:
The "factory" attribute specifies a class to be instantiated
as the glyph factory to "glyphify" annotations of the given annotation type.
Other attributes of an <annotation_style> element are passed to the factory
in an initialization step as a Map with key/value pairs of form
{ attribute_name ==> attribute_value }, and it is up to the the specific
factory implementation to decide what to do with this information.  This
means that there are no restrictions on the attribute names in the
<annotation_style> element, as different factories may recognize different
attributes.
Example:
    <annotation_style annot_type="abc" factory="com.affymetrix.igb.glyph.GenericSymGlyphFactory" />

Optionally specifying color:
Method 1: specify single color for this annot type by adding red, green, blue attributes:
   <annotation_style annot_type="test" red="200" green="0" blue="200" />
Method 2: specify multiple colors to be used for this annot type by adding <color> sub-elements
   to the <annotation_style> element:
   <annotation_style annot_type="test">
      <color name="parent_color" red="255" green="255" blue="255"/>
      <color name="child_color" red="200" green="255" blue="100"/>
   </annotation_style>

The name attribute value for a color element should _not_ match any attribute id
in the annotation_style. Colors specified this way will be passed to the glyph factory
used for this annotation style as part of the initialization hash, as entries of form
{ "name_value" ==> new Color(redval, greenval, blueval) }  It is up to the glyph
factory to decide how to use these colors, based on their names.  If red/green/blue
attributes are included in <annotation_style> element (Method 1), the resulting Color
 is also added to the hash with key = "color".

The usual default factory is the GenerAnnotGlyphFactory.
Attributes that GenericAnnotGlyphFactory recognizes currently include:

"child glyph": This attribute specifies what glyph to use to render
     the (visible) leaf spans of the annotation
"parent_glyph": This attribute specifies what glyph to use to connect
     the child glyphs
Example:
<annotation_style annot_type="test2"
    parent_glyph="com.affymetrix.igb.glyph.EfficientOutlineContGlyph"
    child_glyph="com.affymetrix.igb.glyph.EfficientFillRectGlyph"  />

"glyph_depth": This attribute specifies which symmetries in a symmetry hierarchy
   should be rendered into glyphs.  For annotations like transcripts or alignments,
   it is usually "2", indicating that the leaf symmetry nodes (for example, exons
   or alignment blocks) should be rendered with objects of class "child_glyph",
   and that the nodes just above the leaves (for example, the genomic span of the
   transcript or alignment) should be rendered with objects of class "parent_glyph".
   If it is "1", then only the leaf nodes are rendered as glyphs, as objects of
   class "child_glyph" (good for repeats and SNPs, for example).  There _might_ be
   reasons to use glyph_depth > 2 (which would imply that leaf nodes might not be
   rendered), but I'd suggest sticking with 1 or 2 for now.  The default is 2.

Color names that GenericAnnotGlyphFactory currently recognizes;
   "color":  if no "parent_color" or "child_color", uses this for both
   "parent_color":  the color used for parent glyphs
   "child_color":  the color used for child glyphs

Other attributes that GenericAnnotGlyphFactory should recognize in the near future:
     glyph_height="15"
     tier_name="Aligned RefSeqs"
     tier_start_state="EXPAND"
     tier_expand_max="10"
*</pre>
*/
public class XmlPrefsParser {


  static Class default_factory_class =
    com.affymetrix.igb.glyph.GenericAnnotGlyphFactory.class;

  private static final String FILENAME_LIST = "FILENAME_LIST";

  /** The name of a Map used to link regular expressions for method names to urls.
   *  Use with {@link #getNamedMap(Map, String)}.
   */
  public static final String REGEX_URLS = "regex_urls";

  /** The name of a Map used to link exact names to glyph factories.
   *  Use with {@link #getNamedMap(Map, String)}.
   */
  public static final String MATCH_FACTORIES = "match_factories";

  /** The name of a Map used to link regular expressions to glyph factories.
   *  Use with {@link #getNamedMap(Map, String)}.
   */
  public static final String REGEX_FACTORIES = "regex_factories";

  /** The name of a Map used to link plugin names to plugins classes
   *  Use with {@link #getNamedMap(Map, String)}.
   */
  public static final String PLUGINS = "plugins";

  /** Allows you to keep track of which files preferences were read from.
   *  Add as many names as you want to this list, and retrieve them later
   *  with {@link #getFilenames}.
   */
  public static void addFilename(String filename, Map prefs_hash) {
    if (filename != null && filename.length() > 0) {
      List filenames = getFilenames(prefs_hash);
      filenames.add(filename);
    }
  }

  /** Returns a List of Strings added with {@link #addFilename}.
   *  The list can be empty but is never null.
   */
  public static List getFilenames(Map prefs_hash) {
    List filenames = (List) prefs_hash.get(FILENAME_LIST);
    if (filenames == null) {
      filenames = new ArrayList(4);
      prefs_hash.put(FILENAME_LIST, filenames);
    }
    return filenames;
  }

  public Map parse(File fl) {
    Map prefs_hash = new HashMap();
    FileInputStream fistr = null;
    BufferedInputStream bistr = null;
    try {
      fistr = new FileInputStream(fl);
      bistr = new BufferedInputStream(fistr);
      prefs_hash = parse(bistr, fl.getCanonicalPath(), prefs_hash);
    }
    catch(Exception ex) {
      System.out.println("ERROR: Exception processing preferences file "+ex.toString());
    }
    finally {
      if (bistr != null) try {bistr.close();} catch (Exception e) {}
      if (fistr != null) try {fistr.close();} catch (Exception e) {}
    }
    return prefs_hash;
  }

  public Map parse(InputStream istr, String file_name)  {
    HashMap prefs_hash = new HashMap();
    return parse(istr, file_name, prefs_hash);
  }

  public Map parse(InputStream istr, String file_name, Map prefs_hash) {
    try {
      InputSource insrc = new InputSource(istr);
      prefs_hash = parse(insrc, file_name, prefs_hash);
    }
    catch (Exception ex) {
      System.err.println("ERROR while reading preferences.");
      System.err.println("  "+ex.toString());
      ex.printStackTrace();
    }
    return prefs_hash;
  }

  public Map parse(InputSource insource, String file_name, Map prefs_hash) {
    try {
      DOMParser parser = new DOMParser();
      //      System.out.println("parsing from source: " + insource);
      parser.parse(insource);
      Document prefsdoc = parser.getDocument();
      prefs_hash = processDocument(prefsdoc, file_name, prefs_hash);
    }
    catch (Exception ex) {
      System.err.println("ERROR while reading preferences.");
      System.err.println("  "+ex.toString());
      ex.printStackTrace();
    }
    return prefs_hash;
  }

  /**
   * Returns a Map stored in "prefs_hash" under "name", creating a new one if necessary.
   * These will be LinkedHashMap's, to guarantee the ordering of keys.
   */
  public static Map getNamedMap(Map prefs_hash, String name) {
    Map m = (Map) prefs_hash.get(name);
    if (m == null) {
      m = new LinkedHashMap();
      prefs_hash.put(name, m);
    }
    return m;
  }



  public Map processDocument(Document prefsdoc, String file_name, Map prefs_hash) {
    addFilename(file_name, prefs_hash);
    Map type2factory = getNamedMap(prefs_hash, MATCH_FACTORIES);
    Map regex2factory = getNamedMap(prefs_hash, REGEX_FACTORIES);

    Element top_element = prefsdoc.getDocumentElement();
    String topname = top_element.getTagName();
    if (! (topname.equalsIgnoreCase("prefs"))) {
      System.err.println("not a prefs file -- can't parse in prefs!");
    }
    NodeList children = top_element.getChildNodes();

      // if red, green, blue attributes then val = color(red, green, blue)
      // else if has nested tags then val = (recursive hashtable into nesting)
      // else val = String(content)

    for (int i=0; i<children.getLength(); i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      Object val = null;
      if (child instanceof Element) {
        Element el = (Element)child;
        String the_name = name; // used for error reporting

        try {
          if (name.equalsIgnoreCase("annotation_style")) {
            processAnnotStyle(el, type2factory, regex2factory);
          }
          else if (name.equalsIgnoreCase("annotation_url")) {
            processLinkUrl(el, prefs_hash);
          }
          else if (name.equalsIgnoreCase("annotation_style_defaults")) {
            // processDefaultAnnotStyle();
          }
	  else if (name.equalsIgnoreCase("plugin")) {
	    processPlugin(el, prefs_hash);
	  }
          else if (name.equalsIgnoreCase("tagval")) {
            String tag = el.getAttribute("tag");
            the_name = tag;
            val = el.getAttribute("val");
            prefs_hash.put(tag, val);
            //            System.out.println("added tagval to prefs hash: tag = " + tag + ", val = " + val);
          }
          else if (name.equalsIgnoreCase("boolean")) {
            String tag = el.getAttribute("tag");
            the_name = tag;
            val = new Boolean(el.getAttribute("val"));
            prefs_hash.put(tag, val);
            //            System.out.println("added boolean to prefs hash: tag = " + tag + ", val = " + val);
          }
          else if (name.equalsIgnoreCase("keystroke")) {
            the_name = el.getAttribute("function").toLowerCase();
            String stroke = el.getAttribute("stroke").trim();
            if (stroke.length()>0) {
              val = KeyStroke.getKeyStroke(stroke);
              if (val != null) {
                prefs_hash.put(the_name, val);
              } else {
//                                System.out.println("ERROR keystroke preference not understood: "+
//                                  "function='"+the_name+"', stroke='"+stroke+"'" + "val=" + val);
              }
            }
          }
          else if (el.hasAttribute("red")) {
            // if red, green, blue attributes then val = color(red, green, blue)
            int red = Integer.parseInt(el.getAttribute("red"));
            int green = Integer.parseInt(el.getAttribute("green"));
            int blue = Integer.parseInt(el.getAttribute("blue"));
            val = new Color(red, green, blue);
            // prefs_hash.put(name.trim().toLowerCase(), val);
            prefs_hash.put(name.trim(), val);
            //          System.out.println(tag + ",   " + val);
          }
          else if (name.equalsIgnoreCase("das_server")) {
            //Map das_servers = getNamedMap(prefs_hash, DAS_SERVERS);
            String server_name = el.getAttribute("name");
            String server_url = el.getAttribute("url");
            //das_servers.put(server_name, server_url);
            com.affymetrix.igb.das.DasDiscovery.addDasServer(server_name, server_url);
          }
        } catch (Exception nfe) {
          System.err.println("ERROR setting preference '"+the_name+"':");
          System.err.println("  "+nfe.toString());
        }
      }
    }
    return prefs_hash;

  }

    public void processPlugin(Element el, Map prefs_hash) {
      String loadstr = el.getAttribute("load");
      // ignore if load attribute set to false
      //     if (loadstr == null || (! loadstr.equalsIgnoreCase("false")) ) {
	Map plugins = getNamedMap(prefs_hash, PLUGINS);
	String plugin_name = el.getAttribute("name");
	String class_name = el.getAttribute("class");
	String description = el.getAttribute("description");
	String info_url = el.getAttribute("info_url");
	boolean load = (loadstr == null ? true : (! loadstr.equalsIgnoreCase("false")));
	if (plugin_name != null && class_name != null) {
	  System.out.println("plugin, name = " + plugin_name + ", class = " + class_name);
	  //PluginInfo pinfo = new PluginInfo(class_name, plugin_name, description, info_url, load);
	  PluginInfo pinfo = new PluginInfo(class_name, plugin_name, load);
	  plugins.put(plugin_name, pinfo);
	}
      //      }
    }

  /**
   *  Sets-up a regular-expression matching between a method name and a url,
   *  which can be used, for example, in SeqMapView to "get more info" about
   *  an item.
   *  For example:
   *  <p>
   *  <code>&gt;annotation_url annot_type_regex="google" match_case="false" url="http://www.google.com/search?q=$$" /&lt;</code>
   *  <p>
   *  Note that the url can contain "$$" which will later be substituted with the
   *  "id" of the annotation to form a link.
   *  By default, match is case-insensitive;  use match_case="true" if you want
   *  to require an exact match.
   */
  public void processLinkUrl(Element el, Map prefs_hash) {
    Map attmap = this.getAttributeMap(el);
    String annot_type_regex_string = (String) attmap.get("annot_type_regex");
    if (annot_type_regex_string != null && annot_type_regex_string.trim().length()==0) {
      annot_type_regex_string = null;
    }
    String url = (String) attmap.get("url");
    if (url != null && url.trim().length()==0) {
      url = null;
    }
    if (annot_type_regex_string != null && url != null) {
     try {
      Pattern regex = null;
      if ("true".equalsIgnoreCase((String) attmap.get("match_case"))) {
        regex = Pattern.compile(annot_type_regex_string);
      } else {
        regex = Pattern.compile(annot_type_regex_string, Pattern.CASE_INSENSITIVE);
      }
      if (regex != null) {
        Map regex2url = getNamedMap(prefs_hash, REGEX_URLS);
        regex2url.put(regex, url);
      }
     } catch (PatternSyntaxException pse) {
        System.out.println("ERROR: Regular expression syntax error in preferences\n"+pse.getMessage());
     }
    }
    else {
      System.out.println("ERROR: Empty data in preferences file for an 'annotation_url':  "
        +"annot_type_regex='"+annot_type_regex_string
        +"'  url='"+url+"'");
    }
  }

  /**
   *  Returns a URL based on regular-expression matching of the given method name.
   *  For example, if the preferences file contains:
   *  <p>
   *  <code>&gt;annotation_url annot_type_regex="google" ignore_case="true" url="http://www.google.com/search?q=$$" /&lt;</code>
   *  <p>
   *  then if method="GooGle", this method will return "http://www.google.com/search?q=$$"
   *  @return a String or null
   */
  public static String getLinkURL(Map prefs_hash, String method) {
    // This is not terribly fast, but it is not called in places where speed matters
    // Loop through the regular expressions in reverse order, so that ones added
    // at the end of the user's prefs file will be tested AFTER those at
    // the beginning of the default prefs file.
    
    String url = null;
    if (method != null) {
      Map regex2url = getNamedMap(prefs_hash, REGEX_URLS);
      Vector keyset = new Vector(regex2url.keySet());
      for (int j=keyset.size()-1 ; j >= 0 ; j--) {
        Pattern regex = (Pattern) keyset.get(j);
        if (regex.matcher(method).matches()) {
          url = (String) regex2url.get(regex);
          break;
        }
      }
    }

    return url;
  }

  public void processAnnotStyle(Element el, Map type2factory, Map regex2factory) {
    /*  Builds two hash tables:
     *  type2factory ==> hash of "annot_type" attribute mapped to MapViewGlyphFactoryI
     *  regex2factory ==> hash of RE objects derived from "annot_starts_with",
     *      "annot_ends_with", and "annot_regex" fields mapped to MapViewGlyphFactoryI
     */
    Class factory_class = default_factory_class;
    Map attmap = this.getAttributeMap(el);
    // add colors
    this.addColors(el, attmap);

    // annotation_style element _must_ have and annot_type attribute
    // planning to relax this at some point to allow for element to have one (and only one) of:
    //     annot_type, annot_type_starts_with, annot_type_ends_with, annot_type_regex...
    String annot_type = (String)attmap.get("annot_type");
    if (attmap.get("factory") != null) {
      String factory_name = null;
      try {
        factory_name = (String) attmap.get("factory");
        
        factory_class = ObjectUtils.classForName(factory_name);
        //System.out.println("mapping annot_type to factory: "+annot_type+" --> "+factory_class.getName());
      }
      catch (ClassNotFoundException ex) {
        System.out.println("ERROR: Class '"+factory_name+"' specified in the preferences file can not be found");
        factory_class = default_factory_class;
      }
      catch (Exception e) {
        System.out.println("ERROR: Exception while processing preferences mapping annot_type "
          +annot_type+" to factory "+factory_name + ":\n"+e.toString());
      }
    }
    try {
      MapViewGlyphFactoryI factory = (MapViewGlyphFactoryI)factory_class.newInstance();
      factory.init(attmap);
      if (annot_type != null) {
        type2factory.put(annot_type, factory);
      }
      else {
        String regex_string = null;
        try {
          String annot_starts_with = (String)attmap.get("annot_type_starts_with");
          String annot_ends_with = (String)attmap.get("annot_type_ends_with");
          String annot_regex = (String)attmap.get("annot_type_regex");
          if (annot_starts_with != null) {
            regex_string = "^"+annot_starts_with;
	    //            System.out.println("regex string: " + regex_string);
          }
          else if (annot_ends_with != null) {
            regex_string = annot_ends_with+"$";
          }
          else if (annot_regex != null)  {
            regex_string = annot_regex;
          }
          if (regex_string != null) {
            Pattern regex = Pattern.compile(regex_string);
            if (regex != null) {
              //System.out.println("mapping regex to factory: "+regex_string+" --> "+factory.getClass().getName());
              regex2factory.put(regex, factory);
            }
          }
        }
        catch (PatternSyntaxException pse) {
        System.out.println("ERROR: Regular expression syntax error in preferences\n"+pse.getMessage());
        }
      }
    }
    catch (InstantiationException ex) {
      System.out.println("ERROR: Could not instantiate a glyph factory while processing preferences file: "+factory_class);
    }
    catch (IllegalAccessException ex) {
      System.out.println("ERROR: Could not instantiate a glyph factory while processing preferences file: "+factory_class);
    }
    catch (ClassCastException ex) {
      System.out.println("ERROR: Could not instantiate a glyph factory while processing preferences file: "+factory_class+" is not an instance of MapViewGlyphFactoryI");
    }
    catch (Exception ex) {
      System.out.println("ERROR: Exception while parsing preferences: "+ex.toString());
    }
  }

  public static void addColors(Element el, Map hash) {
    if (el.hasAttribute("red")) {
      int red = Integer.parseInt(el.getAttribute("red"));
      int green = Integer.parseInt(el.getAttribute("green"));
      int blue = Integer.parseInt(el.getAttribute("blue"));
      Color col = new Color(red, green, blue);
      hash.put("color", col);
    }
    /*
    else if (el.getAttribute("color")) {
      // handle color="r,g,b" attribute...
    }
    */
    // check for color elements in child nodes of annotation_style element
    NodeList children = el.getChildNodes();
    int child_count = children.getLength();
    for (int i=0; i<child_count; i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      if (child instanceof Element) {
        Element cel = (Element)child;
        if (name.equalsIgnoreCase("color")) {
          int red = Integer.parseInt(cel.getAttribute("red"));
          int green = Integer.parseInt(cel.getAttribute("green"));
          int blue = Integer.parseInt(cel.getAttribute("blue"));
          String color_name = cel.getAttribute("name");
          Color col = new Color(red, green, blue);
          hash.put(color_name, col);
          //          System.out.println("adding color: " + color_name + ",  " + col);
        }
      }
    }
  }

  public static Map getAttributeMap(Element el) {
    HashMap amap = new HashMap();
    NamedNodeMap atts = el.getAttributes();
    int attcount = atts.getLength();
    for (int i=0; i<attcount; i++) {
      Attr att = (Attr)atts.item(i);
      String tag = att.getName();
      String val = att.getValue();
      amap.put(tag, val);
    }
    return amap;
  }
}

