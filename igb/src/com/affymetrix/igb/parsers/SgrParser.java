package com.affymetrix.igb.parsers;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.affymetrix.genoviz.bioviews.Point2D;
import com.affymetrix.genometry.*;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.util.IntList;
import com.affymetrix.igb.util.FloatList;
import com.affymetrix.igb.util.Point2DComparator;


public class SgrParser {
  static boolean DEBUG = false;
  static Comparator pointcomp = new Point2DComparator(true, true);
  static Pattern line_regex = Pattern.compile("\\s+");  // replaced single tab with one or more whitespace

  public List parse(InputStream istr, Map seqhash, boolean annotate_seq, String stream_name) {
    System.out.println("trying to parse with SgrParser: " + stream_name);
    ArrayList results = new ArrayList();
    InputStreamReader isr = new InputStreamReader(istr);
    BufferedReader br = new BufferedReader(isr);

    String line;
    Map xhash = new HashMap();
    Map yhash = new HashMap();

    try {

    while ((line = br.readLine()) != null) {
      if (line.startsWith("#")) { continue; }
      if (line.startsWith("%")) { continue; }
      String[] fields = line_regex.split(line);
      String seqid = fields[0];
      IntList xlist = (IntList)xhash.get(seqid);
      if (xlist == null) {
	xlist = new IntList();
	xhash.put(seqid, xlist);
      }
      FloatList ylist = (FloatList)yhash.get(seqid);
      if (ylist == null) {
	ylist = new FloatList();
	yhash.put(seqid, ylist);
      }
      int x = Integer.parseInt(fields[1]);
      float y = Float.parseFloat(fields[2]);

      if (DEBUG)  { System.out.println("seq = " + seqid + ", x = " + x + ", y = " + y); }

      xlist.add(x);
      ylist.add(y);
    }

    }
    catch (Exception ex) { ex.printStackTrace(); }

    // after populating all xlists, now make sure sorted
    sortAll(xhash, yhash);

    Iterator iter = xhash.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry keyval = (Map.Entry)iter.next();
      String seqid = (String)keyval.getKey();
      BioSeq aseq = (BioSeq)seqhash.get(seqid);
      IntList xlist = (IntList)keyval.getValue();
      FloatList ylist = (FloatList)yhash.get(seqid);

      if (aseq == null) {
	aseq = new SmartAnnotBioSeq(seqid, "unknown_version", xlist.get(xlist.size()-1));
	seqhash.put(seqid, aseq);
      }

      int[] xcoords = xlist.copyToArray();
      xlist = null;
      float[] ycoords = ylist.copyToArray();
      ylist = null;
      GraphSym graf = new GraphSym(xcoords, ycoords, stream_name, aseq);
      results.add(graf);
    }

    return results;
  }


  public static void sortAll(Map xhash, Map yhash) {
    // after populating all xlists, now make sure sorted
    Iterator iter = xhash.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry keyval = (Map.Entry)iter.next();
      String seqid = (String)keyval.getKey();
      IntList xlist = (IntList)keyval.getValue();
      if (DEBUG)  { System.out.println("key = " + seqid); }
      int xcount = xlist.size();
      boolean sorted = true;
      int prevx = Integer.MIN_VALUE;
      for (int i=0; i<xcount; i++) {
	int x = xlist.get(i);
	if (x < prevx) { 
	  sorted = false;
	  break;
	}
	prevx = x;
      }
      if (! sorted) {
	pointSort(seqid, xhash, yhash);
      }
    }
  }


  protected static void pointSort(String seqid, Map xhash, Map yhash) {
    System.out.println("points aren't sorted for seq = " + seqid + ", sorting now");
    IntList xlist = (IntList)xhash.get(seqid);
    FloatList ylist = (FloatList)yhash.get(seqid);
    int graph_length = xlist.size();
    List points = new ArrayList(graph_length);
    for (int i=0; i<graph_length; i++) {
      int x = xlist.get(i);
      float y = ylist.get(i);
      Point2D pnt = new Point2D((double)x, (double)y);
      points.add(pnt);
    }
    Collections.sort(points, pointcomp);
    IntList new_xlist = new IntList(graph_length);
    FloatList new_ylist = new FloatList(graph_length);
    for (int i=0; i<graph_length; i++) {
      Point2D pnt = (Point2D)points.get(i);
      new_xlist.add((int)pnt.x);
      new_ylist.add((float)pnt.y);
    }
    xhash.put(seqid, new_xlist);
    yhash.put(seqid, new_ylist);
  }
    

  public static void main(String[] args) {
    String test_file = System.getProperty("user.dir") + "/testdata/graph/test1.sgr";
    Map testhash = new HashMap();
    SgrParser test = new SgrParser();

    try {
      FileInputStream fis = new FileInputStream(new File(test_file));
      test.parse(fis, testhash, true, test_file);
    }
    catch (Exception ex) { ex.printStackTrace(); }
  }

}
