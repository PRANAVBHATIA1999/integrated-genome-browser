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

package com.affymetrix.igb.util;

import java.io.IOException;
import java.util.Map;
import com.affymetrix.igb.IGB;
import javax.swing.SwingUtilities;

/**
 * A simple, static class to display a URL in the system browser.
 *
 *  Based on:
 *    "Java Tip 66: Control browsers from your Java application", 
 *    Steven Spencer
 *    http://www.javaworld.com/javaworld/javatips/jw-javatip66.html
 *
 * ----------------------------------------------------------------
 *
 * Under Unix, the default system browser is hard-coded to be 'netscape'.
 * Netscape must be in your PATH for this to work.  This has been
 * tested with the following platforms: AIX, HP-UX and Solaris.
 * You can change this with a preference, see {@link #getUnixPath()}.
 *
 * Under Windows, this will bring up the default browser
 * as determined by the OS.  
 * This has been tested under Windows 95/98/NT.
 *
 * Examples:
 * WebBrowserControl.displayURL("http://www.javaworld.com")
 * WebBrowserControl.displayURL("file://c:\\docs\\index.html")
 * WebBrowserContorl.displayURL("file:///user/joe/index.html");
 * 
 * Note - you must include the url type, such as "http://" or "file://".
 */
public class WebBrowserControl {
  private static String unix_path = null;

  /** Calls {@link #displayURL(String)} on an independent Thread. */
  public static void displayURLEventually(final String url) {
    try {
      java.net.URL the_url = new java.net.URL(url);
    } catch (java.net.MalformedURLException mfue) {
      ErrorHandler.errorPanel("URL malformed:\n"+url);
      return;
    }
    Thread t = new Thread() {
      public void run() {
        try {
          System.out.println("Opening URL in browser: " + url);
          displayURL(url);
        } catch (final Exception e) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              ErrorHandler.errorPanel("Error while opening URL in external browser.", e);
            }
          });
        }
      }
    };
    t.start();
  }

  /**
   * Display a file in the system browser.  If you want to display a
   * file, you must include the absolute path name.
   *
   * @param url the file's url (the url must start with either "http://" or
   * "file://").
   */
  public static void displayURL(String url) {
    boolean windows = isWindowsPlatform();
    String cmd = null;
    try {
      if (windows) {
        cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
        Process p = Runtime.getRuntime().exec(cmd);
      }
      else {
        try {
          int exitCode = -1;
          if (isNetscapeOrMozilla(getUnixPath())) {
            // Under Unix, Netscape or Mozilla or Firefox has to be running for the
            // "-remote" flag to work.  So, we try sending the command and
            // check for an exit value.  If the exit command is 0,
            // it worked, otherwise we need to start the browser.
            cmd = getUnixPath() + " -remote openURL(" + url + ")";
            //System.out.println("cmd: "+cmd);
            Process p = Runtime.getRuntime().exec(cmd);

            // wait for exit code -- if it's 0, command worked,
            // otherwise we need to start the browser.
            exitCode = p.waitFor();
          }
          if (exitCode != 0) {
            // Command failed, start up the browser
            cmd = getUnixPath() + " "  + url;
            //System.out.println("cmd: "+cmd);
            Process p = Runtime.getRuntime().exec(cmd);
          }
        }
        catch(InterruptedException x) {
          ErrorHandler.errorPanel("Error invoking browser, command:\n" 
            + cmd + "\n\n" + x.toString());
        }
      }
    }
    catch(IOException x) {
      // couldn't exec browser
      IGB.errorPanel("Could not invoke browser, command:\n" 
        + cmd + "\n\n" + x.toString());
    }
  }

  /**
   * Try to determine whether this application is running under Windows
   * by examing the "os.name" property.
   *
   * @return true if this application is running under a Windows OS
   */
  public static boolean isWindowsPlatform()
  {
    String os = System.getProperty("os.name");
    return ( os != null && os.startsWith(WIN_ID));
  }

  private static boolean isNetscapeOrMozilla(String path) {
    return (path != null && 
      (path.endsWith("mozilla") ||
       path.endsWith("netscape")));
  }

  public static final String PREF_BROWSER_CMD = "browser command";
  public static final String DEFAULT_BROWSER_CMD = "netscape";
  
  /** Returns the command name used to start a browser on a unix system.
   *  (Actually, used on any non-windows system; on Windows not needed, thus ignored.)
   *  The default value is {@link #DEFAULT_BROWSER_CMD}, but this can be changed
   *  in the preferences, using the preference named
   *  {@link #PREF_BROWSER_CMD}.  The command should be one that
   *  takes the URL as a parameter after the command name,
   *  for instance "/home/bin/startFirefox.sh http://url.com".
   *<p>
   *  Special treatment has been hard-coded for the case where
   *  getUnixPath().endsWith() "mozilla" or "netscape".  In these
   *  cases it will first try "netscape -remote openURL(url)" and if that
   *  fails will then try just "netscape url".
   *<p>
   *  In general, it is better for the user to set-up a preference for
   *  a command that points to a shell script.  Then the user
   *  has more control over opening in a new tab or new window and any
   *  other special flags for their browser.
   */
  public static String getUnixPath() {
    return UnibrowPrefsUtil.getTopNode().get(PREF_BROWSER_CMD, DEFAULT_BROWSER_CMD);
  }

  /**
   * Simple example.
   */
  public static void main(String[] args) {
    WebBrowserControl.displayURL("http://www.affymetrix.com");
  }

  // Used to identify the windows platform.
  private static final String WIN_ID = "Windows";
  // The default system browser under windows.
  private static final String WIN_PATH = "rundll32";
  // The flag to display a url.
  private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
}
