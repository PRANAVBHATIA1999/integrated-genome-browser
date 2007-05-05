/**
 *   Copyright (c) 2006 Affymetrix, Inc.
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

package com.affymetrix.igb.das2;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;


/**
 *  A class to keep track of the result of and log messages from a single
 *  Das2Request.
 */
public class Das2RequestLog {

  boolean ECHO_LOG_TO_SYS_OUT = true;
  java.util.List status_strings = new ArrayList();

  boolean success = false;
  Exception exception = null;
  int httpResponseCode;
  String httpResponseMsg;
    
  public Das2RequestLog() {
  }

  /** Gets any Exception that was stored by {@link #setException(Exception)}. */
  public Exception getException() {
    return this.exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  /** Returns the value that was stored by {@link #setSuccess()}. */
  public boolean getSuccess() {
    return this.success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }
  
  /** Returns the value that was stored by {@link #setHttpResponse(int,String)}. */
  public int getHttpResponseCode() {
    return httpResponseCode;
  }
  
  /** Returns the message that was stored by {@link #setHttpResponse(int,String)}. */
  public String getHttpResponseMsg() {
    return httpResponseMsg;
  }
  
  public void setHttpResponse(int code, String msg) {
    httpResponseCode = code;
    httpResponseMsg = msg;
  }
  
  /** Adds a message string to a log.  May also echo that String to System.out,
   *  depending on the flat {@link #ECHO_LOG_TO_SYS_OUT}.
   */
  public void addLogMessage(String s) {
    status_strings.add(s);
    if (ECHO_LOG_TO_SYS_OUT) {
      System.out.println(s);
    }
  }
  
  public void printLogMessages(PrintStream stream) {
    Iterator iter = status_strings.iterator();
    while (iter.hasNext()) {
      String s = (String) iter.next();
      stream.println(s);
    }
  }
}
