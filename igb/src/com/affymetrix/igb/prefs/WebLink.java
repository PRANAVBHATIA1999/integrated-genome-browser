/**
 *   Copyright (c) 2006-2007 Affymetrix, Inc.
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

package com.affymetrix.igb.prefs;

import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.igb.Application;
import com.affymetrix.genometryImpl.SymWithProps;
import com.affymetrix.igb.parsers.XmlPrefsParser;
import com.affymetrix.igb.tiers.AnnotStyle;
import com.affymetrix.igb.util.UnibrowPrefsUtil;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

public final class WebLink {
  String url = null;
  String name = "";
  String id_field_name = null; // null implies use getId(); "xxx" means use getProperty("xxx");
  String original_regex = null;

  Pattern pattern = null;
  static DefaultListModel weblink_list = new DefaultListModel();
  
  public WebLink() {
  }
  
  public WebLink(String name, String regex, String url) throws PatternSyntaxException {
    this();
    setName(name);
    setRegex(regex);
    setUrl(url);
  }

  /** Used to compute the hashCode and in the equals() method. */
  String toComparisonString() {
    // Do NOT consider the "name" in tests of equality.
    // We do not want to allow two links that are identical except for name.
    // This is important in allowing users to over-ride the default links.
    return original_regex + ", " + url.toString() + ", " + id_field_name;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof WebLink) {
      WebLink w = (WebLink) o;
      return toComparisonString().equals(w.toComparisonString());
    } else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return toComparisonString().hashCode();
  }
  
  /**
   *  A a WebLink to the static list.  Multiple WebLink's with the same 
   *  regular expressions are allowed, as long as they have different URLs.
   *  WebLinks that differ only in name are not allowed; the one added last
   *  will be the one that is kept, unless the one added first had a name and
   *  the one added later does not.
   */
  public static void addWebLink(WebLink wl) {
    int index = weblink_list.indexOf(wl);
    if ( index >= 0 ) {
      if (wl.getName() == null || wl.getName().trim().length() == 0) {
        //Application.getSingleton().logInfo("Not adding duplicate web link for regex: '" + wl.getRegex() + "'");
      } else {
        //Application.getSingleton().logInfo("---------- Renaming Web Link To: " + wl.getName());
        weblink_list.removeElementAt(index);
        weblink_list.addElement(wl);
      }
    } else {
      weblink_list.addElement(wl);
    }
    sortList();
  }

  public static void sortList() {
    ArrayList<WebLink> link_list = new ArrayList<WebLink>(weblink_list.size() + 1);
    for (Object w : weblink_list.toArray()) {
      link_list.add((WebLink) w);
    }
    Collections.sort(link_list, webLinkComp);
    weblink_list.removeAllElements();
    for (WebLink w : link_list) {
      weblink_list.addElement(w);
    }
  }

  static Comparator<WebLink> webLinkComp = new Comparator<WebLink>() {
    String sortString(WebLink wl) {
      return wl.name + ", " + wl.original_regex + ", " + wl.url.toString() + ", " + wl.id_field_name;
    }
    
    public int compare(WebLink o1, WebLink o2) {
      return (sortString(o1).compareTo(sortString(o2)));
    }    
  };
  
  /**
   *  Remove a WebLink from the static list.
   */
  public static void removeWebLink(WebLink wl) {
    weblink_list.removeElement(wl);
  }
  
  /** Get all web-link patterns for the given method name.
   *  These can come from regular-expression matching from the semi-obsolete
   *  XML-based preferences file, or from UCSC-style track lines in the
   *  input files.  It is entirely possible that some of the WebLinks in the
   *  array will have the same regular expression or point to the same URL.
   *  You may want to filter-out such duplicate results.
   */
  public static List<WebLink> getWebLinks(String method) {
    if (method == null) { // rarely happens, but can
      return Collections.<WebLink>emptyList();
    }

    ArrayList<WebLink> results = new ArrayList<WebLink>();
    
    // If the method name has already been used, then the annotStyle must have already been created
    AnnotStyle style = AnnotStyle.getInstance(method, false);
    String style_url = style.getUrl();
    if (style_url != null && ! style_url.equals("")) {
      WebLink link = new WebLink("Track Line URL", null, style_url);
      results.add(link);
    }

    if (method != null) {
      // This is not terribly fast, but it is not called in places where speed matters
      Enumeration en = weblink_list.elements();
      while (en.hasMoreElements()) {
        WebLink link = (WebLink) en.nextElement();
        if (link.matches(method) && link.url != null) {
          results.add(link);
        }
      }
    }
    
    return results;
  }
  
  /** Returns a ListModel backed by the list of WebLink items. */
  public static ListModel getWebLinkListModel() {
    return weblink_list;
  }
    
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    if ("null".equals(name) || name == null) {
      this.name = "";
    } else {
      this.name = name;
    }
  }
  
  public String getRegex() {
    return original_regex;
  }
  
  /** Sets the regular expression that must be matched.
   *  The special value <b>null</b> is also allowed, and matches every String.
   *  If the Regex does not begin with "(?i)", then this will be pre-pended
   *  automatically to generate a case-insensitive pattern.  If you want a
   *  case-sensitive pattern, start your regex with "(?-i)" and this will 
   *  cancel-out the effect of the "(?i)" flag.
   */
  public void setRegex(String regex) throws PatternSyntaxException {
    if (regex == null || ".*".equals(regex)|| "(?i).*".equals(regex)) {
      pattern = null;
      original_regex = null;
    } else {
      // delete any double, triple, etc., "(?i)" strings caused by a bug in a previous version
      while (regex.startsWith("(?i)(?i)")) {
        regex = regex.substring(4);
      }
      if (! regex.startsWith("(?i)")) {
        regex = "(?i)" + regex; // force all web link matches to be case-insensitive
      }
      original_regex = regex;
      pattern = Pattern.compile(regex);
    }
  }
  
  /** Return the compiled form of the regular expression. */
  public Pattern getPattern() {
    return pattern;
  }
  
  /** Returns the URL (or URL pattern) associated with this WebLink.
   *  If the URL pattern contains any "$$" characters, those should be
   *  replaced with URL-Encoded annotation IDs to get the final URL.
   *  Better to use {@link #getURLForSym(SeqSymmetry)}.
   */
  public String getUrl() {
    return this.url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public boolean matches(String s) {
      return (pattern == null ||
              pattern.matcher(s).matches());
  }
  
  /** A pattern that matches the string "$$". */
  static final Pattern DOUBLE_DOLLAR_PATTERN = Pattern.compile("[$][$]");
  
  /** A pattern that matches the string "$:genome:$". */
  static final Pattern DOLLAR_GENOME_PATTERN = Pattern.compile("[$][:]genome[:][$]");
  
  public static String replacePlaceholderWithId(String url, String id) {
    // Now replace all "$$" in the url pattern with the given id, URLEncoded
    if (url != null && id != null) {
      String encoded_id = URLEncoder.encode(id);
      url = DOUBLE_DOLLAR_PATTERN.matcher(url).replaceAll(encoded_id);
    }
    return url;
  }
  
  public static String replaceGenomeId(String url) {
    // Now replace all "$:genome:$" in the url pattern with the current seqGroup id, URLEncoded
    if (url != null) {
      SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
      AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();
      if (group != null) {
        String encoded_id = URLEncoder.encode(group.getID());
        url = DOLLAR_GENOME_PATTERN.matcher(url).replaceAll(encoded_id);
      }
    }
    return url;
  }

  public String getURLForSym(SeqSymmetry sym) {
    String url2 = getURLForSym_(sym);
    return replaceGenomeId(url2);
  }

  String getURLForSym_(SeqSymmetry sym) {
    // Currently this just replaces any "$$" with the ID, but it could
    // do something more sophisticated later, like replace "$$" with
    // some other sym property.
    if (id_field_name == null) {
      return replacePlaceholderWithId(getUrl(), sym.getID());
    }
        
    Object field_value = null;
    if (id_field_name != null && sym instanceof SymWithProps) {
      field_value = ((SymWithProps) sym).getProperty(id_field_name);
    }
    if (field_value == null) {
      Application.logWarning("Selected item has no value for property '"+id_field_name+
          "' which is needed to construct the web link.");
      return replacePlaceholderWithId(getUrl(), "");
    }
    else {
      return replacePlaceholderWithId(getUrl(), field_value.toString());
    }
  }
  
  @Override
  public String toString() {
    return "WebLink: name=" + name + 
        ", regex=" + getRegex() + 
        ", url=" + url +
        ", id_field_name=" + id_field_name;
  }

  /** Name of the xml file used to store the web links data. */
  public static final String FILE_NAME = "weblinks.xml";
  
  /**
   *  Returns the file that is used to store the user-edited web links.
   */
  public static File getLinksFile() {
    String app_dir = UnibrowPrefsUtil.getAppDataDirectory();
    File f = new File(app_dir, FILE_NAME);
    return f;
  }

  public static void importWebLinks(File f) throws FileNotFoundException, IOException {
    importWebLinks(new FileInputStream(f));
  }
  
  public static void importWebLinks(InputStream st) throws IOException {
    // The existing XmlPrefsParser is capable of importing the web links
    XmlPrefsParser parser = new XmlPrefsParser();
    // Create a Map named "foo", which will be discarded after parsing
    Map map = parser.parse(st, "foo", new HashMap());
  }
  
  static String separator = System.getProperty("line.separator");

  public static void exportWebLinks(File f, boolean include_warning) throws IOException {
    FileWriter fw = null;
    BufferedWriter bw = null;
    try {
      
      fw = new FileWriter(f);
      bw = new BufferedWriter(fw);
      bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      bw.write(separator);
      bw.write("");
      bw.write(separator);
      bw.write("<!--");
      bw.write(separator);
      bw.write("  This file was generated by "+Application.getSingleton().getApplicationName()+" "+Application.getSingleton().getVersion()+"\n");
      bw.write(separator);
      if (include_warning) {
        bw.write("  WARNING: This file is automatically created by the application.");
        bw.write(separator);
        bw.write("  Edit the Web-Links from inside the application.");
        bw.write(separator);
      }
      bw.write("-->");
      bw.write(separator);
      bw.write("");
      bw.write(separator);
      bw.write("<prefs>");
      bw.write(separator);
      
      Enumeration en = weblink_list.elements();
      while (en.hasMoreElements()) {
        WebLink link = (WebLink) en.nextElement();
        String xml = link.toXML();
        bw.write(xml);
        bw.write(separator);
      }
      
      bw.write("</prefs>");
      bw.write(separator);
      bw.write(separator);
      bw.close();
    } finally {
      if (bw != null) {bw.close();}
      if (fw != null) {fw.close();}
    }
  }

  String toXML() {
    StringBuffer sb = new StringBuffer();
    sb.append("<annotation_url ").append(separator);
    sb.append(" annot_type_regex=\"")
    .append(escapeXML(getRegex() == null ? ".*" : getRegex()))
    .append("\"").append(separator);
    sb.append(" name=\"")
    .append(escapeXML(name))
    .append("\"").append(separator)
    .append(" url=\"")
    .append(escapeXML(url))
    .append("\"").append(separator)
    .append("/>");
    return sb.toString();
  }
  
  String escapeXML(String s) {
    if (s==null) {
      return null;
    } else {
      return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt")
      .replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
    }
  }
  
  /**
   *  Loads links from the file specified by {@link #getLinksFile()}.
   */
  public static void autoLoad() {
    File f = getLinksFile();
    String filename = f.getAbsolutePath();
    if (f.exists()) {
      try {
        Application.logInfo("Loading web links from file \"" + filename + "\"");

        WebLink.importWebLinks(f);
      } catch (Exception ioe) {

        Application.logError("Could not load web links from file \"" + filename + "\"");
      }
    }
  }

  /** Will save the current web links into the file that was specified
   *  by {@link #getLinksFile()}.
   *  @return true for sucessfully saving the file
   */
  public static boolean autoSave() {
    boolean saved = false;

    File f = getLinksFile();
    String filename = f.getAbsolutePath();
    try {
      Application.logInfo("Saving web links to file \""+filename+"\"");
      File parent_dir = f.getParentFile();
      if (parent_dir != null) {
        parent_dir.mkdirs();
      }
      WebLink.exportWebLinks(f, true);
      saved = true;
    } catch (IOException ioe) {
      Application.logError("Error while saving web links to \"" +filename + "\"");
    }
    return saved;
  }
}
