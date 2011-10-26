/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
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
package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.SimpleSymWithProps;
import com.affymetrix.genometryImpl.SymWithProps;
import com.affymetrix.genometryImpl.TypeContainerAnnot;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.style.GraphType;
import com.affymetrix.genometryImpl.style.HeatMap;
import com.affymetrix.genometryImpl.style.ITrackStyle;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.style.SimpleTrackStyle;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.igb.bookmarks.Bookmark.GRAPH;
import com.affymetrix.igb.bookmarks.Bookmark.SYM;
import com.affymetrix.igb.osgi.service.IGBService;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.awt.Color;

/**
 *  Allows creation of bookmarks based on a SeqSymmetry, and viewing of
 *  a bookmark.
 *
 * @version $Id: BookmarkController.java 7007 2010-10-11 14:26:55Z hiralv $
 */
public abstract class BookmarkController {

	/** Causes a bookmark to be executed.  If this is a Unibrow bookmark,
	 *  it will be opened in the viewer using
	 *  {@link BookmarkUnibrowControlServlet.getInstance()#goToBookmark}.  If it is an external
	 *  bookmark, it will be opened in an external browser.
	 */
	public static void viewBookmark(IGBService igbService, Bookmark bm) {
		if (bm.isUnibrowControl()) {
			try {
				Map<String, String[]> props = Bookmark.parseParameters(bm.getURL());
				BookmarkUnibrowControlServlet.getInstance().goToBookmark(igbService, props);
			} catch (Exception e) {
				String message = e.getClass().getName() + ": " + e.getMessage();
				ErrorHandler.errorPanel("Error opening bookmark.\n" + message);
			}
		} else {
			GeneralUtils.browse(bm.getURL().toExternalForm());
		}
	}

	public static void applyProperties(IGBService igbService, final BioSeq seq, final Map<String, ?> map, final GenericFeature gFeature) {
		double default_ypos = 30;
		double default_yheight = 60;
		Color default_col = Color.lightGray;
		boolean default_float = true;
		boolean default_show_label = true;
		boolean default_show_axis = false;
		double default_minvis = Double.NEGATIVE_INFINITY;
		double default_maxvis = Double.POSITIVE_INFINITY;
		double default_score_thresh = 0;
		int default_minrun_thresh = 30;
		int default_maxgap_thresh = 100;
		boolean default_show_thresh = false;
		int default_thresh_direction = GraphState.THRESHOLD_DIRECTION_GREATER;
		Map<String, ITrackStyleExtended> combos = new HashMap<String, ITrackStyleExtended>();

		try {
			for (int i = 0; map.get(SYM.FEATURE_URL.toString() + i) != null; i++) {
				String feature_path = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.FEATURE_URL.toString() + i);

				if (gFeature == null || !feature_path.equals(gFeature.getURI().toString())) {
					continue;
				}

				String method = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.METHOD.toString() + i);

				SeqSymmetry sym = seq.getAnnotation(method);

				if (sym == null) {
					continue;
				}

				if (!(sym instanceof GraphSym) && !(sym instanceof TypeContainerAnnot)) {
					continue;
				}

				// for some parameters, testing more than one parameter name because how some params used to have
				//    slightly different names, and we need to support legacy bookmarks
				String sym_name = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.NAME.toString() + i);
				String sym_ypos = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.YPOS.toString() + i);
				String sym_height = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.YHEIGHT.toString() + i);

				// sym_col is String rep of RGB integer
				String sym_col = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.COL.toString() + i);
				String sym_bg_col = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.BG.toString() + i);
				String view_mode = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, SYM.VIEW_MODE.toString() + i);
				// sym_bg_col will often be null

				//        int graph_min = (graph_visible_min == null) ?
				String graph_style = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.STYLE.toString() + i);
				String heatmap_name = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.HEATMAP.toString() + i);

				String combo_name = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.COMBO.toString() + i);

				double ypos = (sym_ypos == null) ? default_ypos : Double.parseDouble(sym_ypos);
				double yheight = (sym_height == null) ? default_yheight : Double.parseDouble(sym_height);
				Color col = default_col;
				Color bg_col = Color.BLACK;
				if (sym_col != null) {
					try {
						// Color.decode() can handle colors in plain integer format
						// as well as hex format: "-20561" == "#FFAFAF" == "0xFFAFAF" == "16756655"
						// We now write in the hex format, but can still read the older int format.
						col = Color.decode(sym_col);
					} catch (NumberFormatException nfe) {
						ErrorHandler.errorPanel("Couldn't parse graph color from '" + sym_col + "'\n"
								+ "Please use a hexidecimal RGB format,\n e.g. red = '0xFF0000', blue = '0x0000FF'.");
					}
				}
				if (sym_bg_col != null) {
					try {
						bg_col = Color.decode(sym_bg_col);
					} catch (NumberFormatException nfe) {
						ErrorHandler.errorPanel("Couldn't parse graph background color from '" + sym_bg_col + "'\n"
								+ "Please use a hexidecimal RGB format,\n e.g. red = '0xFF0000', blue = '0x0000FF'.");
					}
				}

				if (sym_name == null || sym_name.trim().length() == 0) {
					sym_name = feature_path;
				}

				ITrackStyleExtended style = null;

				if (sym instanceof GraphSym) {
					String graph_float = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.FLOAT.toString() + i);
					String show_labelstr = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.SHOW_LABEL.toString() + i);
					String show_axisstr = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.SHOW_AXIS.toString() + i);
					String minvis_str = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.MINVIS.toString() + i);
					String maxvis_str = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.MAXVIS.toString() + i);
					String score_threshstr = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.SCORE_THRESH.toString() + i);
					String maxgap_threshstr = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.MAXGAP_THRESH.toString() + i);
					String minrun_threshstr = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.MINRUN_THRESH.toString() + i);
					String show_threshstr = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.SHOW_THRESH.toString() + i);
					String thresh_directionstr = BookmarkUnibrowControlServlet.getInstance().getStringParameter(map, GRAPH.THRESH_DIRECTION.toString() + i);

					boolean use_floating_graphs =
							(graph_float == null) ? default_float : (graph_float.equals("true"));
					boolean show_label =
							(show_labelstr == null) ? default_show_label : (show_labelstr.equals("true"));
					boolean show_axis =
							(show_axisstr == null) ? default_show_axis : (show_axisstr.equals("true"));
					double minvis = (minvis_str == null) ? default_minvis : Double.parseDouble(minvis_str);
					double maxvis = (maxvis_str == null) ? default_maxvis : Double.parseDouble(maxvis_str);
					double score_thresh =
							(score_threshstr == null) ? default_score_thresh : Double.parseDouble(score_threshstr);
					int maxgap_thresh =
							(maxgap_threshstr == null) ? default_maxgap_thresh : Integer.parseInt(maxgap_threshstr);

					int minrun_thresh =
							(minrun_threshstr == null) ? default_minrun_thresh : Integer.parseInt(minrun_threshstr);
					boolean show_thresh =
							(show_threshstr == null) ? default_show_thresh : (show_threshstr.equals("true"));
					int thresh_direction =
							(thresh_directionstr == null) ? default_thresh_direction : Integer.parseInt(thresh_directionstr);


					GraphState gstate = ((GraphSym) sym).getGraphState();
					style = gstate.getTierStyle();
					GenericFeature feature = style.getFeature();

					if (!gFeature.equals(feature)) {
						continue;
					}

					GraphType graph_style_num = null;
					if (graph_style != null) {
						graph_style_num = GraphState.getStyleNumber(graph_style);
					}

					((GraphSym) sym).setGraphName(sym_name);

					applyGraphProperties(igbService, gstate, graph_style_num, heatmap_name, use_floating_graphs,
							show_label, show_axis, minvis, maxvis, score_thresh, minrun_thresh,
							maxgap_thresh, show_thresh, thresh_direction, combo_name, combos);

				} else {
					TypeContainerAnnot tca = (TypeContainerAnnot) sym;
					style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(tca.getType());
					GenericFeature feature = style.getFeature();

					if (!gFeature.equals(feature)) {
						continue;
					}
					style.setTrackName(sym_name);
				}

				applyStyleProperties(style, col, bg_col, ypos, yheight, view_mode);

			}

		} catch (Exception ex) {
			ErrorHandler.errorPanel("ERROR", "Error while applying symmetry properties", ex);
		} catch (Error er) {
			ErrorHandler.errorPanel("ERROR", "Error while applying symmetry properties", er);
		}
	}

	private static void applyStyleProperties(ITrackStyle tier_style, Color col, Color bg_col, double ypos, double yheight, String view_mode) {
		tier_style.setForeground(col);
		tier_style.setBackground(bg_col);
		tier_style.setY(ypos);
		tier_style.setHeight(yheight);
		if (tier_style instanceof ITrackStyleExtended) {
			((ITrackStyleExtended) tier_style).setViewMode(view_mode);
		}
	}

	private static void applyGraphProperties(IGBService igbService, GraphState gstate, GraphType graph_style_num, String heatmap_name,
			boolean use_floating_graphs, boolean show_label, boolean show_axis, double minvis, double maxvis,
			double score_thresh, int minrun_thresh, int maxgap_thresh, boolean show_thresh, int thresh_direction,
			String combo_name, Map<String, ITrackStyleExtended> combos) {

		if (graph_style_num != null) {
			gstate.setGraphStyle(graph_style_num);
		}
		if (heatmap_name != null) {
			HeatMap heat_map = HeatMap.getStandardHeatMap(heatmap_name);
			if (heat_map != null) {
				gstate.setHeatMap(heat_map);
			}
		}

		gstate.setFloatGraph(use_floating_graphs);
		gstate.setShowLabel(show_label);
		gstate.setShowAxis(show_axis);
		gstate.setVisibleMinY((float) minvis);
		gstate.setVisibleMaxY((float) maxvis);
		gstate.setMinScoreThreshold((float) score_thresh);
		gstate.setMinRunThreshold(minrun_thresh);
		gstate.setMaxGapThreshold(maxgap_thresh);
		gstate.setShowThreshold(show_thresh);
		gstate.setThresholdDirection(thresh_direction);
		if (combo_name != null) {
			ITrackStyleExtended combo_style = combos.get(combo_name);
			if (combo_style == null) {
				combo_style = new SimpleTrackStyle("Joined Graphs", true);
				combo_style.setTrackName("Joined Graphs");
				combo_style.setExpandable(true);
				combo_style.setCollapsed(true);
				combo_style.setBackground(igbService.getDefaultBackgroundColor());
				combo_style.setForeground(igbService.getDefaultForegroundColor());
				combos.put(combo_name, combo_style);
			}
			gstate.setComboStyle(combo_style, 0);
		}

	}

	public static void addSymmetries(Bookmarks bookmark) {
		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();
		if (seq != null) {
			AnnotatedSeqGroup group = seq.getSeqGroup();

			for (GenericVersion version : group.getEnabledVersions()) {
				for (GenericFeature feature : version.getFeatures()) {
					if (feature.getLoadStrategy() != LoadStrategy.NO_LOAD) {
						bookmark.add(feature, false);
					}
				}
			}
		}
	}

	public static void addProperties(SymWithProps mark_sym) {
		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();

		Map<ITrackStyle, Integer> combo_styles = new HashMap<ITrackStyle, Integer>();

		// Holds a list of labels of graphs for which no url could be found.
		Set<String> unfound_labels = new LinkedHashSet<String>();

		// "j" loops throug all graphs, while "i" counts only the ones
		// that are actually book-markable (thus i <= j)
		int i = -1;

		for (int j = 0; j < seq.getAnnotationCount(); j++) {

			SeqSymmetry sym = seq.getAnnotation(j);

			if (!(sym instanceof GraphSym) && !(sym instanceof TypeContainerAnnot)) {
				continue;
			}

			i++;

			if (sym instanceof TypeContainerAnnot) {
				TypeContainerAnnot tca = (TypeContainerAnnot) sym;
				ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(tca.getType());
				GenericFeature feature = style.getFeature();

				if (feature == null) {
					unfound_labels.add(tca.getType());
					continue;
				}

				addStyleProps(style, mark_sym, feature.getURI().toString(), style.getTrackName(), tca.getType(), i);
				continue;
			}

			GraphSym graph = (GraphSym) sym;
			GraphState gstate = graph.getGraphState();
			ITrackStyleExtended style = gstate.getTierStyle();
			GenericFeature feature = style.getFeature();

			if (feature == null) {
				unfound_labels.add(sym.getID());
				continue;
			}

			addStyleProps(style, mark_sym, feature.getURI().toString(), graph.getGraphName(), graph.getID(), i);

			mark_sym.setProperty(GRAPH.FLOAT.toString() + i, Boolean.toString(gstate.getFloatGraph()));
			mark_sym.setProperty(GRAPH.SHOW_LABEL.toString() + i, (Boolean.toString(gstate.getShowLabel())));
			mark_sym.setProperty(GRAPH.SHOW_AXIS.toString() + i, (Boolean.toString(gstate.getShowAxis())));
			mark_sym.setProperty(GRAPH.MINVIS.toString() + i, Double.toString(gstate.getVisibleMinY()));
			mark_sym.setProperty(GRAPH.MAXVIS.toString() + i, Double.toString(gstate.getVisibleMaxY()));
			mark_sym.setProperty(GRAPH.SCORE_THRESH.toString() + i, Double.toString(gstate.getMinScoreThreshold()));
			mark_sym.setProperty(GRAPH.MAXGAP_THRESH.toString() + i, Integer.toString(gstate.getMaxGapThreshold()));
			mark_sym.setProperty(GRAPH.MINRUN_THRESH.toString() + i, Integer.toString(gstate.getMinRunThreshold()));
			mark_sym.setProperty(GRAPH.SHOW_THRESH.toString() + i, (Boolean.toString(gstate.getShowThreshold())));
			mark_sym.setProperty(GRAPH.STYLE.toString() + i, gstate.getGraphStyle().toString());
			mark_sym.setProperty(GRAPH.THRESH_DIRECTION.toString() + i, Integer.toString(gstate.getThresholdDirection()));
			if (gstate.getGraphStyle() == GraphType.HEAT_MAP && gstate.getHeatMap() != null) {
				mark_sym.setProperty(GRAPH.HEATMAP.toString() + i, gstate.getHeatMap().getName());
			}

			ITrackStyle combo_style = gstate.getComboStyle();
			if (combo_style != null) {
				Integer combo_style_num = combo_styles.get(combo_style);
				if (combo_style_num == null) {
					combo_style_num = Integer.valueOf(combo_styles.size() + 1);
					combo_styles.put(combo_style, combo_style_num);
				}
				mark_sym.setProperty(GRAPH.COMBO.toString() + i, combo_style_num.toString());
			}

		}

		// TODO: Now save the colors and such of the combo graphs!

		if (!unfound_labels.isEmpty()) {
			ErrorHandler.errorPanel("WARNING: Cannot bookmark some graphs",
					"Warning: could not bookmark some graphs.\n"
					+ "No source URL was available for: " + unfound_labels.toString());
		}

	}

	private static void addStyleProps(ITrackStyleExtended style, SymWithProps mark_sym,
			String featureURI, String name, String method, int i) {

		mark_sym.setProperty(SYM.FEATURE_URL.toString() + i, featureURI);
		mark_sym.setProperty(SYM.METHOD.toString() + i, method);
		mark_sym.setProperty(SYM.YPOS.toString() + i, Integer.toString((int) style.getY()));
		mark_sym.setProperty(SYM.YHEIGHT.toString() + i, Integer.toString((int) style.getHeight()));
		mark_sym.setProperty(SYM.COL.toString() + i, sixDigitHex(style.getForeground()));
		mark_sym.setProperty(SYM.BG.toString() + i, sixDigitHex(style.getBackground()));
		mark_sym.setProperty(SYM.NAME.toString() + i, name);
		mark_sym.setProperty(SYM.VIEW_MODE.toString() + i, style.getViewMode());
	}

	/**
	 *  Creates a Map containing bookmark properties.
	 *  All keys and values are Strings.
	 *  Assumes correct span is the first span in the sym.
	 *
	 * @param sym The symmetry to generate the bookmark properties from
	 * @return a map of bookmark properties
	 */
	private static Map<String, String[]> constructBookmarkProperties(SeqSymmetry sym) {
		SeqSpan span = sym.getSpan(0);
		BioSeq seq = span.getBioSeq();
		Map<String, String[]> props = new LinkedHashMap<String, String[]>();
		props.put(Bookmark.SEQID, new String[]{seq.getID()});
		props.put(Bookmark.VERSION, new String[]{seq.getVersion()});
		props.put(Bookmark.START, new String[]{Integer.toString(span.getMin())});
		props.put(Bookmark.END, new String[]{Integer.toString(span.getMax())});
		return props;
	}

	/**
	 *  Constructs a bookmark from a SeqSymmetry.
	 *  Assumes correct span is the first span in the sym.
	 *  Passes through to makeBookmark(Map props, String name).
	 *
	 * @param sym The symmetry to bookmark
	 * @param name name of the bookmark
	 * @return the bookmark for the symmetry
	 * @throws MalformedURLException if the URL specifies an unknown protocol
	 */
	public static Bookmark makeBookmark(SeqSymmetry sym, String name) throws MalformedURLException {
		Map<String, String[]> props = constructBookmarkProperties(sym);
		String url = Bookmark.constructURL(props);
		return new Bookmark(name, "", url);
	}

	/** Returns a hexadecimal representation of the color with
	 *  "0x" plus exactly 6 digits.  Example  Color.BLUE -> "0x0000FF".
	 */
	private static String sixDigitHex(Color c) {
		int i = c.getRGB() & 0xFFFFFF;
		String s = Integer.toHexString(i).toUpperCase();
		while (s.length() < 6) {
			s = "0" + s;
		}
		s = "0x" + s;
		return s;
	}

	/** A simple extension of SimpleSymWithProps that uses a LinkedHashMap to
	 *  store the properties.  This ensures the bookmark properties will be
	 *  listed in a predictable order.
	 */
	private static class BookmarkSymmetry extends SimpleSymWithProps {

		public BookmarkSymmetry() {
			super();
			props = new LinkedHashMap<String, Object>();
		}
	}

	public static Bookmark getCurrentBookmark(boolean include_sym_and_props, SeqSpan span)
			throws MalformedURLException {
		GenometryModel gmodel = GenometryModel.getGenometryModel();
		BioSeq aseq = gmodel.getSelectedSeq();
		if (aseq == null) {
			return null;
		}

		SeqSpan mark_span = new SimpleSeqSpan(span.getStart(), span.getEnd(), aseq);

		SimpleSymWithProps mark_sym = new BookmarkSymmetry();
		mark_sym.addSpan(mark_span);

		String version = "unknown";
		version = aseq.getVersion();

		String default_name =
				version + ", " + aseq.getID() + ":" + mark_span.getMin()
				+ ", " + mark_span.getMax();
		mark_sym.setProperty(Bookmark.VERSION, version);
		mark_sym.setProperty(Bookmark.SEQID, aseq.getID());
		mark_sym.setProperty(Bookmark.START, Integer.valueOf(mark_span.getMin()));
		mark_sym.setProperty(Bookmark.END, Integer.valueOf(mark_span.getMax()));
		mark_sym.setProperty(Bookmark.LOADRESIDUES, new String[]{Boolean.toString(aseq.isComplete())});

		Bookmarks bookmarks = new Bookmarks();

		if (include_sym_and_props) {
			BookmarkController.addSymmetries(bookmarks);
			BookmarkController.addProperties(mark_sym);
		}

		bookmarks.set(mark_sym);
		Map<?, ?> props = mark_sym.getProperties();
		@SuppressWarnings("unchecked")
		String url = Bookmark.constructURL((Map<String, String[]>) props);
		return new Bookmark(default_name, "", url);
	}

	public static boolean hasSymmetriesOrGraphs() {
		Bookmarks bookmarks = new Bookmarks();
		BookmarkController.addSymmetries(bookmarks);
		return !bookmarks.getSyms().isEmpty();
	}
}
