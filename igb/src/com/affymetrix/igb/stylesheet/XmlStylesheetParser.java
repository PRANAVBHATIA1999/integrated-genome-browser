/**
*   Copyright (c) 2007 Affymetrix, Inc.
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

package com.affymetrix.igb.stylesheet;

import com.affymetrix.genometry.Propertied;
import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.das.DasLoader;
import com.affymetrix.igb.view.SeqMapView;
import java.awt.Color;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;

/**
 *  Loads an XML document using the igb_stylesheet_1.dtd.
 */
public class XmlStylesheetParser {

  Stylesheet stylesheet = new Stylesheet();
  static Stylesheet system_stylesheet = null;
  static Stylesheet user_stylesheet = null;
  
  // This resource should in the top-level igb source directory, or top level of jar file
  static final String system_stylesheet_resource_name = "/igb_system_stylesheet.xml";

  public static synchronized Stylesheet getSystemStylesheet() {
    if (system_stylesheet == null) {
      try {
        XmlStylesheetParser parser = new XmlStylesheetParser();
        // If using class.getResource... use name beginning with "/"
        InputStream istr = XmlStylesheetParser.class.getResourceAsStream(system_stylesheet_resource_name);
        // If using getContextClassLoader... use name NOT beginning with "/"
        //InputStream istr = Thread.currentThread().getContextClassLoader().getResourceAsStream(system_stylesheet_resource_name);
        system_stylesheet = parser.parse(istr);
      } catch (Exception e) {
        System.out.println("ERROR: Couldn't initialize system stylesheet.");
        e.printStackTrace();
        system_stylesheet = null;
      }
    }
    if (system_stylesheet == null) {
      system_stylesheet = new Stylesheet();
    }
    return system_stylesheet;
  }
  
  public static Stylesheet getUserStylesheet() {
    return new Stylesheet();
  }
  
  public Stylesheet parse(File fl) throws IOException {
    FileInputStream fistr = null;
    BufferedInputStream bistr = null;
    try {
      fistr = new FileInputStream(fl);
      bistr = new BufferedInputStream(fistr);
      stylesheet = parse(bistr);
    }
    finally {
      if (bistr != null) try {bistr.close();} catch (Exception e) {}
      if (fistr != null) try {fistr.close();} catch (Exception e) {}
    }
    return stylesheet;
  }

  public Stylesheet parse(InputStream istr) throws IOException {
    InputSource insrc = new InputSource(istr);
    parse(insrc);
    return stylesheet;
  }

  public Stylesheet parse(InputSource insource) throws IOException {
    try {
      //DOMParser parser = new DOMParser();
      DOMParser parser = DasLoader.nonValidatingParser();

      parser.parse(insource);
      Document prefsdoc = parser.getDocument();
      processDocument(prefsdoc);
    }
    catch (IOException ioe) {
      throw ioe;
    }
    catch (Exception ex) {
      IOException ioe = new IOException("Error processing stylesheet file");
      ioe.initCause(ex);
      throw ioe;
    }
    return stylesheet;
  }

  public void processDocument(Document prefsdoc) throws IOException {

    Element top_element = prefsdoc.getDocumentElement();
    String topname = top_element.getTagName();
    if (! (topname.equalsIgnoreCase("igb_stylesheet"))) {
      throw new IOException("Can't parse file: Initial Element is not <IGB_STYLESHEET>.");
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

          if (name.equalsIgnoreCase("import")) {
            processImport(el);
          }
          else if (name.equalsIgnoreCase("styles")) {
            processStyles(el);
          }
          else if (name.equalsIgnoreCase("associations")) {
            processAssociations(el);
          }
          else {
            cantParse(el);
          }
      }
    }
  }
  
  void cantParse(Element n) {
    System.out.println("WARNING: Stylesheet: Cannot parse element: " + n.getNodeName());
  }

  void notImplemented(String s) {
    System.out.println("WARNING: Stylesheet: Not yet implemented: " + s);
  }

  boolean isBlank(String s) {
    return (s == null || s.trim().length() == 0);
  }
  
  void processImport(Element el) throws IOException {
    notImplemented("<IMPORT>");
  }
  
  void processAssociations(Element associations) throws IOException {

    NodeList children = associations.getChildNodes();

    for (int i=0; i<children.getLength(); i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      if (child instanceof Element) {
        Element el = (Element) child;

        if (name.equalsIgnoreCase("TYPE_ASSOCIATION")) {
          String type = el.getAttribute("type");
          String style = el.getAttribute("style");
          if (isBlank(type) || isBlank(style)) {
            throw new IOException("ERROR in stylesheet: missing method or style in METHOD_ASSOCIATION");
          }
          stylesheet.type2stylename.put(type, style);
        }
        else if (name.equalsIgnoreCase("METHOD_ASSOCIATION")) {
          String method = el.getAttribute("method");
          String style = el.getAttribute("style");
          if (isBlank(method) || isBlank(style)) {
            throw new IOException("ERROR in stylesheet: missing method or style in METHOD_ASSOCIATION");
          }
          stylesheet.meth2stylename.put(method, style);
        }
        else if (name.equalsIgnoreCase("METHOD_REGEX_ASSOCIATION")) {
          String regex = el.getAttribute("regex");
          String style = el.getAttribute("style");
          if (isBlank(regex) || isBlank(style)) {
            throw new IOException("ERROR in stylesheet: missing method or style in METHOD_ASSOCIATION");
          }
          try {
            Pattern pattern = Pattern.compile(regex);
            stylesheet.regex2stylename.put(pattern, style);
          } catch (PatternSyntaxException pse) {
            IOException ioe = new IOException("ERROR in stylesheet: Regular Expression not valid: '" +
                regex + "'");
            ioe.initCause(pse);
            throw ioe;
          }
        }
        else {
          cantParse(el);
        }
      }
    }
  }

  void processStyles(Element stylesNode) throws IOException {
    NodeList children = stylesNode.getChildNodes();

    //applyProperties(stylesNode, ...);
    
    // There could be a top-level property map that applies to the
    // whole stylesheet, but that isn't implemented now
    PropertyMap top_level_property_map = null;

    for (int i=0; i<children.getLength(); i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      if (child instanceof Element) {
        Element el = (Element) child;
        
        if (name.equalsIgnoreCase(StyleElement.NAME) || name.equalsIgnoreCase(Stylesheet.WrappedStyleElement.NAME)) {
          processStyle(el, true);
        }
      }
    }
  }

  Color string2Color(String s) {
    if (s==null || s.trim().length() == 0) {
      return null;
    }
    if (s.startsWith("Ox")) {
      return Color.decode(s);
    } else {
      return Color.decode("0x"+s);
    }
  }
  
  StyleElement processStyle(Element styleel, boolean top_level) throws IOException {

    // node name should be STYLE, COPY_STYLE or USE_STYLE
    String node_name = styleel.getNodeName();

    
    StyleElement se = null;
    if (StyleElement.NAME.equalsIgnoreCase(node_name)) {
      String styleName = styleel.getAttribute(StyleElement.ATT_NAME);
      se = stylesheet.createStyle(styleName, top_level);
      se.childContainer = styleel.getAttribute(StyleElement.ATT_CONTAINER);

//    } else if ("COPY_STYLE".equalsIgnoreCase(node_name)) {
//      String newName = styleel.getAttribute("new_name");
//      String extendsName = styleel.getAttribute("extends");
//      se = stylesheet.getStyleByName(extendsName);
//      
//      if (se == null) {
//        se = stylesheet.createStyle(newName, top_level);
//      } else {
//        se = StyleElement.clone(se, newName);
//      }
//      
    } else if (Stylesheet.WrappedStyleElement.NAME.equalsIgnoreCase(node_name)) {
      String styleName = styleel.getAttribute(StyleElement.ATT_NAME);
      if (styleName==null || styleName.trim().length()==0) {
        throw new IOException("Can't have a USE_STYLE element with no name");
      }
      
      se = stylesheet.getWrappedStyle(styleName);
      // Not certain this will work
      se.childContainer = styleel.getAttribute(StyleElement.ATT_CONTAINER);
      
      return se; // do not do any other processing on a USE_STYLE element
    } else {
      cantParse(styleel);
    }

    if (se == null) {
      cantParse(styleel);
    }

    if (top_level) {
      if (isBlank(se.getName())) {
        System.out.println("WARNING: Stylesheet: All top-level styles must have a name!");
      } else {
        stylesheet.addToIndex(se);
      }
    }
    
    NodeList children = styleel.getChildNodes();
    

    // there can be multiple <PROPERTY> children
    // There should only be one child <GLYPH> OR one or more <MATCH> and <ELSE> elements
    // <COPY_STYLE> is not supposed to have <PROPERTIES>, but it is allowed to here
    
    for (int i=0; i<children.getLength(); i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      if (child instanceof Element) {
        Element el = (Element) child;
        
        if (name.equalsIgnoreCase(GlyphElement.NAME)) {
          GlyphElement ge2 = processGlyph(el);
          se.setGlyphElement(ge2);
        } else if (name.equalsIgnoreCase(PropertyMap.PROP_ELEMENT_NAME)) {
          processProperty(el, se.propertyMap);
        } else if (name.equalsIgnoreCase(MatchElement.NAME) || name.equalsIgnoreCase(ElseElement.NAME)) {
          MatchElement me = processMatchElement(el);
          se.addMatchElement(me);
        } else {
          cantParse(el);
        }
      }
    }
        
    return se;
  }
  
  GlyphElement processGlyph(Element glyphel) throws IOException {
    GlyphElement ge = new GlyphElement();

    String type = glyphel.getAttribute(GlyphElement.ATT_TYPE);
    if (GlyphElement.knownGlyphType(type)) {
      ge.setType(type);
    } else {
      System.out.println("STYLESHEET WARNING: <GLYPH type='" + type + "'> not understood");
      ge.setType(GlyphElement.TYPE_BOX);
    }
    
    String position = glyphel.getAttribute(GlyphElement.ATT_POSITION);
    ge.setPosition(position);

    NodeList children = glyphel.getChildNodes();
    for (int i=0; i<children.getLength(); i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      if (child instanceof Element) {
        Element el = (Element) child;
        
        if (name.equalsIgnoreCase(GlyphElement.NAME)) {
          GlyphElement ge2 = processGlyph(el);
          ge.addGlyphElement(ge2);
        } else if (name.equalsIgnoreCase(ChildrenElement.NAME)) {
          ChildrenElement ce = processChildrenElement(el);
          ge.setChildrenElement(ce);
        } else if (name.equalsIgnoreCase(PropertyMap.PROP_ELEMENT_NAME)) {
          processProperty(el, ge.propertyMap);
        } else {
          cantParse(el);
        }
      }
    }

    return ge;
  }
  
  ChildrenElement processChildrenElement(Element childel) throws IOException {
    ChildrenElement ce = new ChildrenElement();
    
    String position = childel.getAttribute(ChildrenElement.ATT_POSITIONS);
    ce.setPosition(position);
    String container = childel.getAttribute(ChildrenElement.ATT_CONTAINER);
    ce.setChildContainer(container);

    NodeList children = childel.getChildNodes();
    for (int i=0; i<children.getLength(); i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      if (child instanceof Element) {
        Element el = (Element) child;
        
        if (name.equalsIgnoreCase(StyleElement.NAME) || name.equalsIgnoreCase(Stylesheet.WrappedStyleElement.NAME)) {
          StyleElement se = processStyle(el, false);
          ce.setStyleElement(se);
        } else if (name.equalsIgnoreCase(MatchElement.NAME) || name.equalsIgnoreCase(ElseElement.NAME)) {
          MatchElement me = processMatchElement(el);
          ce.addMatchElement(me);
        } else if (name.equalsIgnoreCase(PropertyMap.PROP_ELEMENT_NAME)) {
          processProperty(el, ce.propertyMap);
        } else {
          cantParse(el);
        }
      }
    }
    return ce;

  }
  
  MatchElement processMatchElement(Element matchel) throws IOException {    
    MatchElement me;
    
    if (MatchElement.NAME.equalsIgnoreCase(matchel.getNodeName())) {
      me = new MatchElement();
      String type = matchel.getAttribute(MatchElement.ATT_TEST);
      String param = matchel.getAttribute(MatchElement.ATT_PARAM);
      if (! isBlank(type)) {
        me.match_test = type;
        if (! isBlank(param)) {
          me.match_param = param;
          
          if (MatchElement.MATCH_BY_METHOD_REGEX.equals(type)) {
            try {
              me.match_regex = Pattern.compile(param);
            } catch (PatternSyntaxException pse) {
              IOException ioe = new IOException("ERROR in stylesheet: Regular Expression not valid: '" +
                  param + "'");
              ioe.initCause(pse);
              throw ioe;
            }
          }
        }
      }

    } else if (ElseElement.NAME.equalsIgnoreCase(matchel.getNodeName())) { 
      // an "ELSE" element is just like MATCH,
      //  except that it always matches as true
      me = new ElseElement();      
    } else {
      cantParse(matchel);
      me = new ElseElement(); // treat it like an ELSE element
    }
    
    NodeList children = matchel.getChildNodes();

    for (int i=0; i<children.getLength(); i++) {
      Node child = children.item(i);
      String name = child.getNodeName();
      if (child instanceof Element) {
        Element el = (Element) child;
        
        if (name.equalsIgnoreCase(StyleElement.NAME) || name.equalsIgnoreCase(Stylesheet.WrappedStyleElement.NAME)) {
          StyleElement se = processStyle(el, false);
          me.setStyle(se);
        } else if (name.equalsIgnoreCase(MatchElement.NAME) || name.equalsIgnoreCase(ElseElement.NAME)) {
          MatchElement me2 = processMatchElement(el);
          me.subMatchList.add(me2);
        } else if (name.equalsIgnoreCase(PropertyMap.PROP_ELEMENT_NAME)) {
          processProperty(el, me.propertyMap);
        } else {
          cantParse(el);
        }
      }
    }
    return me;
  }
    
  void processProperty(Element properElement, PropertyMap propertied) 
  throws IOException {
    String key = properElement.getAttribute(PropertyMap.PROP_ATT_KEY);
    String value = properElement.getAttribute(PropertyMap.PROP_ATT_VALUE);
    if (key == null) {
       throw new IOException("ERROR: key or value of <PROPERTY> is null");
    }
    propertied.setProperty(key, value);
  }  
  
  static String escapeXML(String s) {
    if (s==null) {
      return "";
    } else {
      return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt")
      .replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
    }
  }

  
  public static void appendAttribute(StringBuffer sb, String name, String value) {
    if (value != null && value.trim().length() > 0) {
      sb.append(" ").append(name).append("='").append(escapeXML(value)).append("'");
    }
  }
}

