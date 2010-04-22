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

package com.affymetrix.igb.view;

import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genoviz.event.NeoViewBoxChangeEvent;
import com.affymetrix.genoviz.event.NeoViewBoxListener;
import com.affymetrix.genoviz.widget.NeoMap;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.bookmarks.Bookmark;
import com.affymetrix.igb.bookmarks.UnibrowControlServlet;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextField;


/**
 * A Text Box for displaying and setting the range of a SeqMapView.
 * 
 * @version $Id$
 */
final class MapRangeBox implements NeoViewBoxListener, GroupSelectionListener {
	private static final long serialVersionUID = 1l;
  
  private final NeoMap map;
  private final SeqMapView gview;
  
  public final JTextField range_box;

  // Use the ENGLISH locale here because we want the user to be able to
  // cut and paste this text into the UCSC browser.
  // (Also, the Pattern's below were written to work for the English locale.)
  private static final NumberFormat nformat = NumberFormat.getIntegerInstance(Locale.ENGLISH);
  
  // accepts a pattern like: "chr2 : 3,040,000 : 4,502,000"  or "chr2:10000-20000"
  // (The chromosome name cannot contain any spaces.)
  private static final Pattern chrom_start_end_pattern = Pattern.compile("^\\s*(\\S+)\\s*[:]\\s*([0-9,]+)\\s*[:-]\\s*([0-9,]+)\\s*$");
  
  // accepts a pattern like: "chr2 : 3,040,000 + 20000"
  // (The chromosome name cannot contain any spaces.)
  private static final Pattern chrom_start_width_pattern = Pattern.compile("^\\s*(\\S+)\\s*[:]\\s*([0-9,]+)\\s*\\+\\s*([0-9,]+)\\s*$");

  // accepts a pattern like: "3,040,000 : 4,502,000"  or "10000-20000"
  private static final Pattern start_end_pattern = Pattern.compile("^\\s*([0-9,]+)\\s*[:-]\\s*([0-9,]+)\\s*$");
  private static final Pattern start_width_pattern = Pattern.compile("^\\s*([0-9,]+)\\s*[+]\\s*([0-9,]+)\\s*$");
  private static final Pattern center_pattern = Pattern.compile("^\\s*([0-9,]+)\\s*\\s*$");
  
  public MapRangeBox(SeqMapView gview) {
    this.gview = gview;
    this.map = gview.getSeqMap();

	range_box = new JTextField("");
    Dimension d = new Dimension(250, range_box.getPreferredSize().height);
    range_box.setPreferredSize(d);
    range_box.setMaximumSize(d);
    
    range_box.setToolTipText("<html>Enter a coordinate range here.<br>" +
      "Use the format 'start : end' or 'start + width' or 'center',<br>" +
      "or use the UCSC browser format 'chrom:start-end'.<html>");

    range_box.setEditable(true);
    range_box.addActionListener(action_listener);
    map.addViewBoxListener(this);
	GenometryModel.getGenometryModel().addGroupSelectionListener(this);
  }

  public void viewBoxChanged(NeoViewBoxChangeEvent e) {
    Rectangle2D.Double vbox = e.getCoordBox();
    setRangeText(vbox.x, vbox.width + vbox.x);
  }

  public void groupSelectionChanged(GroupSelectionEvent evt) {
    range_box.setText("");
  }

  void setRangeText(double start, double end) {
      range_box.setText(nformat.format(start) + " : " + nformat.format(end));
  }

  ActionListener action_listener = new ActionListener() {
    public void actionPerformed(ActionEvent evt) {
      int[] current = map.getVisibleRange();
      double start = current[0];
      double end = current[1];
      gview.zoomTo(start, end);

      double width = end - start;

      try {
        Matcher chrom_start_end_matcher = chrom_start_end_pattern.matcher(range_box.getText());
        Matcher chrom_start_width_matcher = chrom_start_width_pattern.matcher(range_box.getText());
        Matcher start_end_matcher = start_end_pattern.matcher(range_box.getText());
        Matcher start_width_matcher = start_width_pattern.matcher(range_box.getText());
        Matcher center_matcher = center_pattern.matcher(range_box.getText());
        
        if (chrom_start_end_matcher.matches() || chrom_start_width_matcher.matches()) {
          Matcher matcher;
          boolean uses_width;
          if (chrom_start_width_matcher.matches()) {
            matcher = chrom_start_width_matcher;
            uses_width = true;
          } else {
            matcher = chrom_start_end_matcher;
            uses_width = false;
          }
          
          String chrom_text = matcher.group(1);
          String start_text = matcher.group(2);
          String end_or_width_text = matcher.group(3);
          start = nformat.parse(start_text).doubleValue();
          double end_or_width = nformat.parse(end_or_width_text).doubleValue();
          if (uses_width) {
            end = start + end_or_width;
          } else {
            end = end_or_width;
          }

		  zoomToSeqAndSpan( chrom_text, (int)start, (int)end);

        }
        else
         if (start_end_matcher.matches()) {
          String start_text = start_end_matcher.group(1);
          String end_text = start_end_matcher.group(2);
          start = nformat.parse(start_text).doubleValue();
          end = nformat.parse(end_text).doubleValue();
          gview.zoomTo(start, end);
        }
        else if (start_width_matcher.matches()) {
          String start_text = start_width_matcher.group(1);
          String width_text = start_width_matcher.group(2);
          start = nformat.parse(start_text).doubleValue();
          end = start + nformat.parse(width_text).doubleValue();
          gview.zoomTo(start, end);
        }
        else if (center_matcher.matches()) {
          String center_text = center_matcher.group(1);
          double center = nformat.parse(center_text).doubleValue();
          start = center - width/2;
          end = center + width/2;
          gview.zoomTo(start, end);
          gview.setZoomSpotX(center);
        }

        // generally this is redundant, because zooming the view will make
        // a call back to change this text.
        // But if the user tries to zoom to something illogical, this can be helpful
        SeqSpan span = gview.getVisibleSpan();
        if (span == null) {
          range_box.setText("");
        } else {
          setRangeText(span.getStart(), span.getEnd());
        }

      } catch (Exception ex) {
        System.out.println("Exception in MapRangeBox: " + ex);
        setRangeText(start, end);
      }
    }
	};

	static void zoomToSeqAndSpan(String chrom_text, int start, int end) throws NumberFormatException {
		GenometryModel gmodel = GenometryModel.getGenometryModel();
		if (gmodel.getSelectedSeqGroup() != null) {
			Map<String, String[]> m = new HashMap<String, String[]>();
			m.put(Bookmark.SEQID, new String[] {chrom_text});
			m.put(Bookmark.START, new String[] {Integer.toString(start)});
			m.put(Bookmark.END, new String[] {Integer.toString(end)});
			m.put(Bookmark.VERSION, new String[] {gmodel.getSelectedSeqGroup().getID()});
			UnibrowControlServlet.goToBookmark(Application.getSingleton(), m);
		}
	}
}
