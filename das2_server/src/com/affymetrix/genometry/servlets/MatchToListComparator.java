package com.affymetrix.genometry.servlets;

import java.io.*;
import java.util.*;

/**
 *  Given a list of Strings in a file (one per line),
 *     compares for sorting two input Strings based on where they are in the list
 *  If one of the two input Strings is not in list, should sort to bottom
 *  Any whitspace at end of Strings in file is trimmed off
 */
public class MatchToListComparator implements Comparator {
  List match_list = null;

  public MatchToListComparator(String filename) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
      match_list = new ArrayList();
      String line;
      while ((line = br.readLine()) != null) {
	if (line.equals("") || line.startsWith("#") || (line.length() == 0))  { continue; }
	String match_term = line.trim();
	match_list.add(match_term);
      }
      br.close();
    }
    catch (Exception ex) {
      System.out.println("Error initializing MatchToListComparator: ");
      match_list = null;
      ex.printStackTrace();
    }
    System.out.println("done initializing MatchToListComparator");
  }

  public int compare(Object o1, Object o2) {
    if (match_list == null) { return 0; }
    String name1 = (String)o1;
    String name2 = (String)o2;
    int index1 = match_list.indexOf(name1);
    int index2 = match_list.indexOf(name2);
    if (index1 == -1 && index2 == -1) { return 0; } // neither found in list
    else if (index1 == -1) { return 1; } // name1 not in list 
    else if (index2 == -1) { return -1; } // name2 not in list
    else if (index1 < index2) { return -1; }
    else if (index2 < index1) { return 1; }
    else { return 0; }
  }
}
