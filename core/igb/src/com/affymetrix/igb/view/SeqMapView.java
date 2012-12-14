package com.affymetrix.igb.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.Border;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.event.*;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.*;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.util.SeqUtils;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.RubberBand;
import com.affymetrix.genoviz.bioviews.SceneI;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.event.NeoMouseEvent;
import com.affymetrix.genoviz.glyph.AxisGlyph;
import com.affymetrix.genoviz.glyph.InsertionSeqGlyph;
import com.affymetrix.genoviz.glyph.PixelFloaterGlyph;
import com.affymetrix.genoviz.glyph.RootGlyph;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.*;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.genoviz.util.NeoConstants;
import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import com.affymetrix.genoviz.widget.NeoMap;
import com.affymetrix.genoviz.widget.AutoScroll;

import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.action.*;
import com.affymetrix.igb.glyph.*;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor.TrackStylePropertyListener;
import com.affymetrix.igb.shared.*;
import com.affymetrix.igb.tiers.*;
import com.affymetrix.igb.view.factories.DefaultTierGlyph;
import com.affymetrix.igb.view.factories.TransformTierGlyph;

import static com.affymetrix.igb.IGBConstants.BUNDLE;
/**
 * A panel hosting a labeled tier map.
 * Despite it's name this is actually a panel and not a {@link ViewI}.
 */
public class SeqMapView extends JPanel
		implements SeqMapViewExtendedI, SymSelectionListener, SeqSelectionListener, GroupSelectionListener, TrackStylePropertyListener, PropertyHolder, JRPWidget {
	private static final long serialVersionUID = 1L;
	public static enum MapMode {

		MapSelectMode(true, false, defaultCursor, defaultCursor),
		MapScrollMode(false, true, openHandCursor, closedHandCursor),
		MapZoomMode(false, false, defaultCursor, defaultCursor);
		public final boolean rubber_band, drag_scroll;
		public final Cursor defCursor, pressedCursor;

		private MapMode(boolean rubber_band, boolean drag_scroll, Cursor defaultCursor, Cursor pressedCursor) {
			this.rubber_band = rubber_band;
			this.drag_scroll = drag_scroll;
			this.defCursor = defaultCursor;
			this.pressedCursor = pressedCursor;
		}
	}
		
	public static final String PREF_AUTO_CHANGE_VIEW = "Auto change view of BAM/SAM";
	private final static String SEQ_MODE = "SEQ_MODE";
	public static final boolean default_auto_change_view = false;
	private static final boolean DEBUG_TIERS = false;
	
	static final Cursor defaultCursor, openHandCursor, closedHandCursor;

	static {
		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		openHandCursor = new Cursor(Cursor.HAND_CURSOR);
		closedHandCursor = new Cursor(Cursor.HAND_CURSOR);
	}

	protected boolean subselectSequence = true;  // try to visually select range along seq glyph based on rubberbanding
	boolean show_edge_matches = true;
	protected boolean coord_shift = false;
	private boolean show_prop_tooltip = true;
	private MapMode mapMode;
	private JRPToggleButton select_mode_button;
	private JRPToggleButton scroll_mode_button;
//	private JToggleButton zoom_mode_button;
	private final Set<ContextualPopupListener> popup_listeners = new CopyOnWriteArraySet<ContextualPopupListener>();
	/**
	 * maximum number of query glyphs for edge matcher. any more than this and
	 * won't attempt to edge match (edge matching is currently very inefficient
	 * with large numbers of glyphs -- something like O(N * M), where N is
	 * number of query glyphs and M is total number of glyphs to try and match
	 * against query glyphs [or possibly O(N^2 * M) ???] )
	 */
	private static final int max_for_matching = 500;
	private String id;
	/**
	 * boolean for setting map range to min and max bounds of AnnotatedBioSeq's
	 * annotations
	 */
	private boolean shrinkWrapMapBounds = false;
	protected AffyTieredMap seqmap;
	private UnibrowHairline hairline = null;
	protected BioSeq aseq;
	/**
	 * a virtual sequence that maps the BioSeq aseq to the map coordinates. if
	 * the mapping is identity, then: vseq == aseq OR
	 * vseq.getComposition().getSpan(aseq) = SeqSpan(0, aseq.getLength(), aseq)
	 * if the mapping is reverse complement, then:
	 * vseq.getComposition().getSpan(aseq) = SeqSpan(aseq.getLength(), 0, aseq);
	 *
	 */
	protected BioSeq viewseq;
	// mapping of annotated seq to virtual "view" seq
	protected MutableSeqSymmetry seq2viewSym;
	protected SeqSymmetry[] transform_path;
	public static final String PREF_COORDINATE_LABEL_FORMAT = "Coordinate label format";
	/**
	 * One of the acceptable values of {@link #PREF_COORDINATE_LABEL_FORMAT}
	 * {@link #PREF_COORDINATE_LABEL_FORMAT}.
	 */
	public static final String VALUE_COORDINATE_LABEL_FORMAT_COMMA = "COMMA";
	/**
	 * One of the acceptable values of {@link #PREF_COORDINATE_LABEL_FORMAT},
	 * {@link #PREF_COORDINATE_LABEL_FORMAT}.
	 */
	public static final String VALUE_COORDINATE_LABEL_FORMAT_FULL = "FULL";
	/**
	 * One of the acceptable values of {@link #PREF_COORDINATE_LABEL_FORMAT},
	 * {@link #PREF_COORDINATE_LABEL_FORMAT}.
	 */
	public static final String VALUE_COORDINATE_LABEL_FORMAT_ABBREV = "ABBREV";
	/**
	 * One of the acceptable values of {@link #PREF_COORDINATE_LABEL_FORMAT},
	 * {@link #PREF_COORDINATE_LABEL_FORMAT}.
	 */
	public static final String VALUE_COORDINATE_LABEL_FORMAT_NO_LABELS = "NO_LABELS";
	public static final String PREF_EDGE_MATCH_COLOR = "Edge match color";
	public static final String PREF_EDGE_MATCH_FUZZY_COLOR = "Edge match fuzzy color";
	/**
	 * Name of a boolean preference for whether the horizontal zoom slider is
	 * above the map.
	 */
	private static final String PREF_X_ZOOMER_ABOVE = "Horizontal Zoomer Above Map";
	/**
	 * Name of a boolean preference for whether the vertical zoom slider is left
	 * of the map.
	 */
	private static final String PREF_Y_ZOOMER_LEFT = "Vertical Zoomer Left of Map";
	/**
	 * Name of a boolean preference for whether to show properties in tooltip.
	 */
	public static final String PREF_SHOW_TOOLTIP = "Show properties in tooltip";
	public static final Color default_edge_match_color = Color.WHITE;
	public static final Color default_edge_match_fuzzy_color = new Color(200, 200, 200); // light gray
	private static final boolean default_x_zoomer_above = true;
	private static final boolean default_y_zoomer_left = true;
	private static final Font max_zoom_font = NeoConstants.default_bold_font.deriveFont(30.0f);
	private final PixelFloaterGlyph pixel_floater_glyph = new PixelFloaterGlyph();
	private final AutoScroll autoScroll = new AutoScroll();
	private final GlyphEdgeMatcher edge_matcher;
	private JPopupMenu sym_popup = null;
	private JLabel sym_info;
	private String currentToolTip;
	// A fake menu item, prevents null pointer exceptions in loadResidues()
	// for menu items whose real definitions are commented-out in the code
	private static final JMenuItem empty_menu_item = new JMenuItem("");
	//JMenuItem zoomtoMI = empty_menu_item;
//	JMenuItem selectParentMI = empty_menu_item;
	JMenuItem slicendiceMI = empty_menu_item;
//	JMenu seqViewerOptions = new JMenu("Show genomic sequence for ..");
	JMenuItem seqViewerOptions = empty_menu_item;
//	JMenuItem viewFeatureinSequenceViewer = empty_menu_item;
//	JMenuItem viewParentinSequenceViewer = empty_menu_item;
	// for right-click on background
	private final SeqMapViewMouseListener mouse_listener;
	private CharSeqGlyph seq_glyph = null;
	private SeqSymmetry seq_selected_sym = null;  // symmetry representing selected region of sequence
	private SeqSpan clampedRegion = null; //Span representing clamped region
	private final List<GlyphI> match_glyphs = new ArrayList<GlyphI>();
	protected TierLabelManager tier_manager;
	protected JComponent xzoombox;
	protected JComponent yzoombox;
	protected JRPButton zoomInXB;
	protected JRPButton zoomInYB;
	protected JRPButton zoomOutXB;
	protected JRPButton zoomOutYB;
	protected MapRangeBox map_range_box;
	protected JRPButton partial_residuesB;
	public static final Font axisFont = NeoConstants.default_bold_font;
	boolean report_hairline_position_in_status_bar = false;
	boolean report_status_in_status_bar = true;
	private SeqSymmetry sym_used_for_title = null;
	private TierGlyph tier_used_in_selection_info = null;
	private PropertyHandler propertyHandler;
	private final GenericAction refreshDataAction;
	private SeqMapViewPopup popup;
	private final static int xoffset_pop = 10;
	private final static int yoffset_pop = 0;
	private final static int[] default_range = new int[]{0, 100};
	private final static int[] default_offset = new int[]{0, 100};
	private final Set<SeqMapRefreshed> seqmap_refresh_list = new CopyOnWriteArraySet<SeqMapRefreshed>();
	private TierGlyph axis_tier;
	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	// This preference change listener can reset some things, like whether
	// the axis uses comma format or not, in response to changes in the stored
	// preferences.  Changes to axis, and other tier, colors are not so simple,
	// in part because of the need to coordinate with the label glyphs.
	private final PreferenceChangeListener pref_change_listener = new PreferenceChangeListener() {

		public void preferenceChange(PreferenceChangeEvent pce) {
			if (axis_tier == null) {
				return;
			}

			if (!pce.getNode().equals(PreferenceUtils.getTopNode())) {
				return;
			}

			if (pce.getKey().equals(PREF_COORDINATE_LABEL_FORMAT)) {
				AxisGlyph ag = getAxisGlyph();
				if (ag != null) {
					setAxisFormatFromPrefs(ag);
				}
				seqmap.updateWidget();
			} else if (pce.getKey().equals(PREF_EDGE_MATCH_COLOR) || pce.getKey().equals(PREF_EDGE_MATCH_FUZZY_COLOR)) {
				if (show_edge_matches) {
					doEdgeMatching(seqmap.getSelected(), true);
				}
			} else if (pce.getKey().equals(PREF_X_ZOOMER_ABOVE)) {
				boolean b = PreferenceUtils.getBooleanParam(PREF_X_ZOOMER_ABOVE, default_x_zoomer_above);
				SeqMapView.this.remove(xzoombox);
				if (b) {
					SeqMapView.this.add(BorderLayout.NORTH, xzoombox);
				} else {
					SeqMapView.this.add(BorderLayout.SOUTH, xzoombox);
				}
				SeqMapView.this.invalidate();
			} else if (pce.getKey().equals(PREF_Y_ZOOMER_LEFT)) {
				boolean b = PreferenceUtils.getBooleanParam(PREF_Y_ZOOMER_LEFT, default_y_zoomer_left);
				SeqMapView.this.remove(yzoombox);
				if (b) {
					SeqMapView.this.add(BorderLayout.WEST, yzoombox);
				} else {
					SeqMapView.this.add(BorderLayout.EAST, yzoombox);
				}
				SeqMapView.this.invalidate();
			}
		}
	};

	private MouseListener continuousActionListener = new MouseAdapter() {
		private javax.swing.Timer timer;

		@Override
		public void mousePressed(MouseEvent e) {
			if (!(e.getSource() instanceof JButton)
					|| ((JButton) e.getSource()).getAction() == null) {
				return;
			}

			timer = new javax.swing.Timer(200, ((JButton) e.getSource()).getAction());
			timer.start();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			timer.stop();
		}
	};
	
	public SeqMapView(boolean add_popups, String theId) {
		super();
		this.id = theId;
		ScriptManager.getInstance().addWidget(this);
		seqmap = createAffyTieredMap();

		seqmap.setReshapeBehavior(NeoAbstractWidget.X, NeoConstants.NONE);
		seqmap.setReshapeBehavior(NeoAbstractWidget.Y, NeoConstants.NONE);

		seqmap.addComponentListener(new SeqMapViewComponentListener());

		// the MapColor MUST be a very dark color or else the hairline (which is
		// drawn with XOR) will not be visible!
		seqmap.setMapColor(Color.BLACK);

		edge_matcher = GlyphEdgeMatcher.getSingleton();

		mouse_listener = new SeqMapViewMouseListener(this);

		seqmap.getNeoCanvas().setDoubleBuffered(false);

		seqmap.setScrollIncrementBehavior(AffyTieredMap.X, AffyTieredMap.AUTO_SCROLL_HALF_PAGE);

		Adjustable xzoomer = getXZoomer(this.id);

		((JSlider) xzoomer).setToolTipText(BUNDLE.getString("horizontalZoomToolTip"));
		Adjustable yzoomer = new RPAdjustableJSlider(this.id + "_yzoomer", Adjustable.VERTICAL);
		((JSlider) yzoomer).setToolTipText(BUNDLE.getString("verticalZoomToolTip"));

		seqmap.setZoomer(NeoMap.X, xzoomer);
		seqmap.setZoomer(NeoMap.Y, yzoomer);

		tier_manager = new TierLabelManager((AffyLabelledTierMap) seqmap);
		popup = new SeqMapViewPopup(tier_manager, this);
		MouseShortCut msc = new MouseShortCut(popup);

		tier_manager.setDoGraphSelections(true);

		GraphSelectionManager gsm = new GraphSelectionManager(this);
		seqmap.addMouseListener(gsm);

		if (add_popups) {
			//NOTE: popup listeners are called in reverse of the order that they are added
			// Must use separate instances of GraphSelectioManager if we want to use
			// one as a ContextualPopupListener AND one as a TierLabelHandler.PopupListener
			//tier_manager.addPopupListener(new GraphSelectionManager(this));
			//tier_manager.addPopupListener(new TierArithmetic(tier_manager, this));
			tier_manager.addPopupListener(gsm);
			//TODO: tier_manager.addPopupListener(new CurationPopup(tier_manager, this));
			tier_manager.addPopupListener(popup);
		}

		// Listener for track selection events.  We will use this to populate 'Selection Info'
		// grid with properties of the Type.
		TierLabelManager.TrackSelectionListener track_selection_listener = new TierLabelManager.TrackSelectionListener() {

			@Override
			public void trackSelectionNotify(GlyphI topLevelGlyph, TierLabelManager handler) {
				// TODO:  Find properties of selected track and show in 'Selection Info' tab.
			}
		};
		tier_manager.addTrackSelectionListener(track_selection_listener);


		seqmap.setSelectionAppearance(SceneI.SELECT_OUTLINE);
		seqmap.addMouseListener(mouse_listener);
		seqmap.addMouseListener(msc);
		seqmap.addMouseMotionListener(mouse_listener);
		((AffyLabelledTierMap) seqmap).getLabelMap().addMouseMotionListener(mouse_listener);
		//((AffyLabelledTierMap)seqmap).getLabelMap().addMouseListener(msc); //Enable mouse short cut here.

		tier_manager.setDoGraphSelections(true);

		// A "Smart" rubber band is necessary becaus we don't want our attempts
		// to drag the graph handles to also cause rubber-banding
		RubberBand srb = new RubberBand(seqmap);
		seqmap.setRubberBand(srb);
		seqmap.addRubberBandListener(mouse_listener);
		srb.setColor(new Color(100, 100, 255));

		SmartDragScrollMonitor sdsm = new SmartDragScrollMonitor(this);
		seqmap.setDragScrollMonitor(sdsm);

		this.setLayout(new BorderLayout());

		xzoombox = Box.createHorizontalBox();
		map_range_box = new MapRangeBox(this);
		addSearchButton(this.id);

		xzoombox.add(map_range_box.range_box);

		select_mode_button = new JRPToggleButton(this.id + "_select_mode_button",
				new MapModeSelectAction(this.id));
		select_mode_button.setText("");
		select_mode_button.setToolTipText(BUNDLE.getString("selectModeToolTip"));
		select_mode_button.setMargin(new Insets(2,4,2,4));
		xzoombox.add(select_mode_button);

		scroll_mode_button = new JRPToggleButton(this.id + "_scroll_mode_button",
				new MapModeScrollAction(this.id));
		scroll_mode_button.setText("");
		scroll_mode_button.setToolTipText(BUNDLE.getString("scrollModeToolTip"));
		scroll_mode_button.setMargin(new Insets(2,4,2,4));
		xzoombox.add(scroll_mode_button);

//		zoom_mode_button = new JToggleButton(new MapModeAction(this, MapMode.MapZoomMode));
//		zoom_mode_button.setText("");
//		xzoombox.add(zoom_mode_button);

		ButtonGroup group = new ButtonGroup();
		group.add(select_mode_button);
		group.add(scroll_mode_button);
//		group.add(zoom_mode_button);
		select_mode_button.doClick(); // default
		
		xzoombox.add(Box.createRigidArea(new Dimension(6, 0)));

		addZoomOutXButton(this.id);
		xzoombox.add((Component)xzoomer);
		addZoomInXButton(this.id);
		
		xzoombox.add(Box.createRigidArea(new Dimension(6, 0)));
		
		refreshDataAction = new RefreshDataAction(this);
		addRefreshButton(this.id);
		addLoadResidueButton(this.id);
		
		boolean x_above = PreferenceUtils.getBooleanParam(PREF_X_ZOOMER_ABOVE, default_x_zoomer_above);
		JPanel pan = new JPanel(new BorderLayout(0,0));
		pan.add("Center", xzoombox);
		if (x_above) {
			this.add(BorderLayout.NORTH, pan);
		} else {
			this.add(BorderLayout.SOUTH, pan);
		}

//		JSlider specialZoomer = new JSlider(JSlider.VERTICAL, 0, 100, 50);
//		ChangeListener zoomie = new LawrencianZoomer(seqmap, specialZoomer.getModel());
//		specialZoomer.addChangeListener(zoomie);

		yzoombox = Box.createVerticalBox();
		
		yzoombox.add(Box.createRigidArea(new Dimension(6, 0)));

		addZoomOutYButton(this.id);
		yzoombox.add((Component)yzoomer, BorderLayout.CENTER);
		addZoomInYButton(this.id);
		
		yzoombox.add(Box.createRigidArea(new Dimension(6, 0)));
		
		boolean y_left = PreferenceUtils.getBooleanParam(PREF_Y_ZOOMER_LEFT, default_y_zoomer_left);
		if (y_left) {
			this.add(BorderLayout.WEST, yzoombox);
		} else {
			this.add(BorderLayout.EAST, yzoombox);
		}

		this.add(BorderLayout.CENTER, seqmap);
		
		LinkControl link_control = new LinkControl();
		this.addPopupListener(link_control);

		this.addPopupListener(new ReadAlignmentView());

		PreferenceUtils.getTopNode().addPreferenceChangeListener(pref_change_listener);
		TrackstylePropertyMonitor.getPropertyTracker().addPropertyListener(this);
		
	}
	
	protected void addZoomInXButton(String id) {
		zoomInXB = new JRPButton(id + "_zoomInX_button", ZoomInXAction.getIconOnlyAction());
		zoomInXB.setMargin(new Insets(0,0,0,0));
		zoomInXB.addMouseListener(continuousActionListener);
		xzoombox.add(zoomInXB);
	}

	protected void addZoomOutXButton(String id) {
		zoomOutXB = new JRPButton(id + "_zoomOutX_button", ZoomOutXAction.getIconOnlyAction());
		zoomOutXB.setMargin(new Insets(0,0,0,0));
		zoomOutXB.addMouseListener(continuousActionListener);
		xzoombox.add(zoomOutXB);
	}

	protected void addZoomInYButton(String id) {
		zoomInYB = new JRPButton(id + "_zoomInY_button", ZoomInYAction.getIconOnlyAction());
		zoomInYB.setAlignmentX(CENTER_ALIGNMENT);
		zoomInYB.setMargin(new Insets(0,0,0,0));
		zoomInYB.addMouseListener(continuousActionListener);
		yzoombox.add(zoomInYB, BorderLayout.SOUTH);
	}

	protected void addZoomOutYButton(String id) {
		zoomOutYB = new JRPButton(id + "_zoomOutYX_button", ZoomOutYAction.getIconOnlyAction());
		zoomOutYB.setAlignmentX(CENTER_ALIGNMENT);
		zoomOutYB.setMargin(new Insets(0,0,0,0));
		zoomOutYB.addMouseListener(continuousActionListener);
		yzoombox.add(zoomOutYB, BorderLayout.NORTH);
	}

	protected void addRefreshButton(String id) {
		JRPButton refresh_button = new JRPButton(id + "_refresh_button", refreshDataAction);
//		refresh_button.setText("");
		refresh_button.setIcon(MenuUtil.getIcon("16x16/actions/refresh.png"));
		refresh_button.setMargin(new Insets(2,4,2,4));
		xzoombox.add(refresh_button);
	}
	
	protected void addLoadResidueButton(String id) {
		partial_residuesB = new JRPButton("DataAccess_sequenceInView", LoadPartialSequenceAction.getAction());
		partial_residuesB.setToolTipText(MessageFormat.format(IGBConstants.BUNDLE.getString("load"), IGBConstants.BUNDLE.getString("partialNucleotideSequence")));
		partial_residuesB.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/dna.gif"));
		partial_residuesB.setText("Load Sequence");
		partial_residuesB.setMargin(new Insets(2,4,2,4));
		xzoombox.add(partial_residuesB);
	}
	
	protected void addSearchButton(String id) {
		JRPButton searchButton = new JRPButton(this.id + "_search_button",
				new GenericAction(null, BUNDLE.getString("goToRegionToolTip"),
				"16x16/actions/system-search.png",
				null, //"22x22/actions/system-search.png",
				KeyEvent.VK_UNDEFINED) {

					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						super.actionPerformed(e);
						map_range_box.actionPerformed(e);
					}
			}
		);
		searchButton.setMargin(new Insets(2,4,2,4));
		xzoombox.add(searchButton);
	}

	protected Adjustable getXZoomer(String id) {
		return new ThresholdXZoomer(id, this);
	}

	public final class SeqMapViewComponentListener extends ComponentAdapter {
		// update graphs and annotations when the map is resized.

		@Override
		public void componentResized(ComponentEvent e) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					List<GraphGlyph> graphs = collectGraphs();
					for (int i = 0; i < graphs.size(); i++) {
						GraphGlyphUtils.checkPixelBounds(graphs.get(i), getSeqMap());
					}
					getSeqMap().stretchToFit(false, false);
					getSeqMap().updateWidget();

				}
			});
		}
	};

	/**
	 * Creates an instance to be used as the SeqMap. Set-up of listeners and
	 * such will be done in init()
	 */
	private static AffyTieredMap createAffyTieredMap() {
		AffyTieredMap resultSeqMap = new AffyLabelledTierMap(true, true);
		resultSeqMap.enableDragScrolling(true);
		((AffyLabelledTierMap) resultSeqMap).getLabelMap().enableMouseWheelAction(false);
		resultSeqMap.setMaxZoomToFont(max_zoom_font);
		NeoMap label_map = ((AffyLabelledTierMap) resultSeqMap).getLabelMap();
		label_map.setSelectionAppearance(SceneI.SELECT_OUTLINE);
		label_map.setReshapeBehavior(NeoAbstractWidget.Y, NeoConstants.NONE);
		label_map.setMaxZoomToFont(max_zoom_font);
		return resultSeqMap;
	}

	public final TierLabelManager getTierManager() {
		return tier_manager;
	}

	private void setupPopups() {
		sym_info = new JLabel("");
		sym_info.setEnabled(false); // makes the text look different (usually lighter)
		sym_info.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
//		zoomtoMI = setUpMenuItem(sym_popup, "Zoom to selected");
//		zoomtoMI.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/Zoom16.gif"));

//		selectParentMI = new JRPMenuItem("SeqMapView_" + getId() + "_popup_selectParent", SelectParentAction.getAction());
//		KeyStroke ks = MenuUtil.addAccelerator(this,
//				SelectParentAction.getAction(), SelectParentAction.getAction().getId());
//		if (ks != null) {
//			// Make the accelerator be visible in the menu item.
//			selectParentMI.setAccelerator(ks);
//		}
//		setThreshold = setUpMenuItem(sym_popup, "Set AutoLoad Threshold to Current View");
//		seqViewerOptions = setUpMenuItem(sym_popup, "View Genomic Sequence in Sequence Viewer");
//		viewFeatureinSequenceViewer = setUpMenuItemDuplicate(seqViewerOptions, "Just selected span using genomic coordinates");
//		viewParentinSequenceViewer = setUpMenuItemDuplicate(seqViewerOptions, "Linked spans using transcript coordinates");
//		viewFeatureinSequenceViewer = new JMenuItem("View selected feature in Sequence Viewer");
//		viewParentinSequenceViewer = new JMenuItem("View sequence for parent in sequence Viewer");
//		seqViewerOptions.add(viewFeatureinSequenceViewer);
//		seqViewerOptions.add(viewParentinSequenceViewer);// get more info
	}

	public final TierGlyph getAxisTier() {
		return axis_tier;
	}

	/**
	 * Sets the axis label format from the value in the persistent preferences.
	 */
	public static void setAxisFormatFromPrefs(AxisGlyph axis) {
		// It might be good to move this to AffyTieredMap
		String axis_format = PreferenceUtils.getTopNode().get(PREF_COORDINATE_LABEL_FORMAT, VALUE_COORDINATE_LABEL_FORMAT_COMMA);
		if (VALUE_COORDINATE_LABEL_FORMAT_COMMA.equalsIgnoreCase(axis_format)) {
			axis.setLabelFormat(AxisGlyph.COMMA);
		} else if (VALUE_COORDINATE_LABEL_FORMAT_FULL.equalsIgnoreCase(axis_format)) {
			axis.setLabelFormat(AxisGlyph.FULL);
		} else if (VALUE_COORDINATE_LABEL_FORMAT_NO_LABELS.equalsIgnoreCase(axis_format)) {
			axis.setLabelFormat(AxisGlyph.NO_LABELS);
		} else {
			axis.setLabelFormat(AxisGlyph.ABBREV);
		}
	}

	protected void clear() {
		seqmap.clearWidget();
		aseq = null;
		this.viewseq = null;
		clearSelection();
		TrackView.getInstance().clear();
		match_glyphs.clear();
		seqmap.setMapRange(default_range[0], default_range[1]);
		seqmap.setMapOffset(default_offset[0], default_offset[1]);
		seqmap.stretchToFit(true, true);
		seqmap.updateWidget();
	}

	/**
	 * Clears the graphs, and reclaims some memory.
	 */
	public final void clearGraphs() {
		if (aseq != null) {
			if (IGBConstants.GENOME_SEQ_ID.equals(aseq.getID())) {
				// clear graphs for all sequences in the genome
				for (BioSeq seq : aseq.getSeqGroup().getSeqList()) {
					removeGraphsFromSeq(seqmap, seq);
				}
			}
			removeGraphsFromSeq(seqmap, aseq);
		} else {
			System.err.println("Please select a chromosome!");
		}

		//Make sure the graph is un-selected in the genometry model, to allow GC
		gmodel.clearSelectedSymmetries(this);
		setAnnotatedSeq(aseq, false, true);
	}

	private static void removeGraphsFromSeq(AffyTieredMap map, BioSeq mseq) {
		int acount = mseq.getAnnotationCount();
		for (int i = acount - 1; i >= 0; i--) {
			SeqSymmetry annot = mseq.getAnnotation(i);
			if (annot instanceof GraphSym) {
				GlyphI glyph = map.getItem(annot);
				if (glyph != null) {
					map.removeItem(glyph);
				}
				mseq.unloadAnnotation(annot); // This also removes from the AnnotatedSeqGroup.
			}
		}
	}

	public void dataRemoved() {
		setAnnotatedSeq(aseq);
		AltSpliceView slice_view = (AltSpliceView) ((IGB) IGB.getSingleton()).getView(AltSpliceView.class.getName());
		if (slice_view != null) {
			slice_view.getSplicedView().dataRemoved();
		}
	}

	/**
	 * Sets the sequence; if null, has the same effect as calling clear().
	 */
	public final void setAnnotatedSeq(BioSeq seq) {
		setAnnotatedSeq(seq, false, (seq == this.aseq) && (seq != null));
		// if the seq is not changing, try to preserve current view
	}

	/**
	 * Sets the sequence. If null, has the same effect as calling clear().
	 *
	 * @param preserve_selection if true, then try and keep same selections
	 * @param preserve_view if true, then try and keep same scroll and zoom /
	 * scale and offset in // both x and y direction. [GAH: temporarily changed
	 * to preserve scale in only the x direction]
	 */
	@Override
	public void setAnnotatedSeq(BioSeq seq, boolean preserve_selection, boolean preserve_view) {
		setAnnotatedSeq(seq, preserve_selection, preserve_view, false);
	}

	//   want to optimize for several situations:
	//       a) merging newly loaded data with existing data (adding more annotations to
	//           existing BioSeq) -- would like to avoid recreation and repacking
	//           of already glyphified annotations
	//       b) reverse complementing existing BioSeq
	//       c) coord shifting existing BioSeq
	//   in all these cases:
	//       "new" BioSeq == old BioSeq
	//       existing glyphs could be reused (in (b) they'd have to be "flipped")
	//       should preserve selection
	//       should preserve view (x/y scale/offset) (in (b) would preserve "flipped" view)
	//   only some of the above optimization/preservation are implemented yet
	//   WARNING: currently graphs are not properly displayed when reverse complementing,
	//               need to "genometrize" them
	//            currently sequence is not properly displayed when reverse complementing
	//
	@Override
	public void setAnnotatedSeq(BioSeq seq, boolean preserve_selection, boolean preserve_view_x, boolean preserve_view_y) {
		if (seq == null) {
			//clear();
			return;
		}

		boolean same_seq = (seq == this.aseq);

		match_glyphs.clear();
		List<SeqSymmetry> old_selections = Collections.<SeqSymmetry>emptyList();
		double old_zoom_spot_x = seqmap.getZoomCoord(AffyTieredMap.X);
		double old_zoom_spot_y = seqmap.getZoomCoord(AffyTieredMap.Y);

		if (same_seq) {
			// Gather information about what is currently selected, so can restore it later
			if (preserve_selection) {
				old_selections = getSelectedSyms();
			} else {
				old_selections = Collections.<SeqSymmetry>emptyList();
			}
		}
		
		// Save selected tiers
		List<TierGlyph> old_tier_selections = getTierManager().getSelectedTiers();
		
		// stash annotation tiers for proper state restoration after resetting for same seq
		//    (but presumably added / deleted / modified annotations...)
		List<TierGlyph> cur_tiers = new ArrayList<TierGlyph>(seqmap.getTiers());
		TierGlyph axisTierGlyph = (axis_tier == null) ? null : axis_tier;
		int axis_index = Math.max(0, cur_tiers.indexOf(axisTierGlyph));	// if not found, set to 0
		List<TierGlyph> temp_tiers = copyMapTierGlyphs(cur_tiers, axis_index);

		seqmap.clearWidget();
		seqmap.clearSelected(); // may already be done by map.clearWidget()

		pixel_floater_glyph.removeAllChildren();
		pixel_floater_glyph.setParent(null);

		seqmap.addItem(pixel_floater_glyph);

		// Synchronized to keep aseq from getting set to null
		synchronized (this) {
			aseq = seq;

			// if shifting coords, then seq2viewSym and viewseq are already taken care of,
			//   but reset coord_shift to false...
			if (coord_shift) {
				// map range will probably change after this if SHRINK_WRAP_MAP_BOUNDS is set to true...
				coord_shift = false;
			} else {
				this.viewseq = seq;
				seq2viewSym = null;
				transform_path = null;
			}

			seqmap.setMapRange(viewseq.getMin(), viewseq.getMax());
			addGlyphs(temp_tiers, axis_index);
		}

		seqmap.repack();

		if (same_seq && preserve_selection) {
			// reselect glyph(s) based on selected sym(s);
			// Unfortunately, some previously selected syms will not be directly
			// associatable with new glyphs, so not all selections can be preserved
			Iterator<SeqSymmetry> iter = old_selections.iterator();
			while (iter.hasNext()) {
				SeqSymmetry old_selected_sym = iter.next();

				GlyphI gl = seqmap.<GlyphI>getItem(old_selected_sym);
				if (gl != null) {
					seqmap.select(gl);
				}
			}
			setZoomSpotX(old_zoom_spot_x);
			setZoomSpotY(old_zoom_spot_y);
		} else {
			// do selection based on what the genometry model thinks is selected
			List<SeqSymmetry> symlist = gmodel.getSelectedSymmetries(seq);
			select(symlist, false, false, false);

			setSelectionStatus(getSelectionTitle(seqmap.getSelected()));
		}

		// Restore selected tiers
		if(old_tier_selections != null){
			for(TierLabelGlyph tierLabelGlyph : getTierManager().getAllTierLabels()){
				if(tierLabelGlyph.getReferenceTier().isVisible() && 
						old_tier_selections.contains(tierLabelGlyph.getReferenceTier())){
					((AffyLabelledTierMap)getSeqMap()).getLabelMap().select(tierLabelGlyph);
				}
			}
		}
		
		if (show_edge_matches) {
			doEdgeMatching(seqmap.getSelected(), false);
		}

		if (shrinkWrapMapBounds) {
			shrinkWrap();
		}

		seqmap.toFront(axis_tier);

		// restore floating layers to front of map
		for (GlyphI layer_glyph : getFloatingLayers(seqmap.getScene().getGlyph())) {
			seqmap.toFront(layer_glyph);
		}

		// Ignore preserve_view if seq has changed
		if ((preserve_view_x || preserve_view_y) && same_seq) {
			if (clampedRegion != null) {
				seqmap.setMapRange(clampedRegion.getStart(), clampedRegion.getEnd());
			}
			seqmap.stretchToFit(!preserve_view_x, !preserve_view_y);

			/**
			 * Possible bug : When all strands are hidden. tier label and tiers
			 * do appear at same position.
			 *
			 */
			// NOTE: Below call to stretchToFit is not redundancy. It is there
			//       to solve above mentioned bug.
			// Probably not necessary after a fix in r9248 - HV
			//seqmap.stretchToFit(!preserve_view_x, !preserve_view_y);
		} else {
			seqmap.stretchToFit(true, true);

			/**
			 * Possible bug : Below both ranges are different
			 * System.out.println("SeqMapRange "+seqmap.getMapRange()[1]);
			 * System.out.println("VisibleRange "+seqmap.getVisibleRange()[1]);
			 *
			 */
			// NOTE: Below call to stretchToFit is not redundancy. It is there
			//       to solve a bug (ID: 2912651 -- tier map and tiers off-kilter)
			// Probably not necessary after a fix in r9248 - HV
			//seqmap.stretchToFit(true, true);
			zoomToSelections();
			postSelections();
			int[] range = seqmap.getVisibleRange();
			setZoomSpotX(0.5 * (range[0] + range[1]));
			if (clampedRegion != null) {
				clamp(false);
			}
		}

		seqMapRefresh();

		seqmap.updateWidget();

		//A Temporary hack to solve problem when a 'genome' is selected
		if (IGBConstants.GENOME_SEQ_ID.equals((seq.getID()))) {
			seqmap.scroll(NeoMap.X, seqmap.getScroller(NeoMap.X).getMinimum());
		}
		
		autoScroll.configure(seqmap, 0, seq.getLength());
		//GeneralLoadView.getLoadView().getTableModel().fireTableDataChanged(); //for updating cell renderers/editors
	}
	
	public void seqMapRefresh(){
		for(SeqMapRefreshed smr : seqmap_refresh_list){
			smr.mapRefresh();
		}
	}

	/**
	 * Returns a tier for the given style and direction, creating them if they
	 * don't already exist. Generally called by the Glyph Factory. Note that
	 * this can create empty tiers. But if the tiers are not filled with
	 * something, they will later be removed automatically.
	 *
	 * @param sym The SeqSymmetry (data model) for the track
	 * @param style a non-null instance of IAnnotStyle; tier label and other
	 * properties are determined by the IAnnotStyle.
	 * @param tier_direction the direction of the track
	 *       (FORWARD, REVERSE, or BOTH)
	 */
	public TierGlyph getTrack(ITrackStyleExtended style, TierGlyph.Direction tier_direction) {
		return TrackView.getInstance().getTrack(this, style, tier_direction);
	}

	public void preserveSelectionAndPerformAction(AbstractAction action) {
		// If action is null then there is no point of this method.
		if (action == null) {
			return;
		}

		List<SeqSymmetry> old_sym_selections = getSelectedSyms();
		seqmap.clearSelected();
		if (show_edge_matches) {
			doEdgeMatching(seqmap.getSelected(), false);
		}
		
		action.actionPerformed(null);

		// reselect glyph(s) based on selected sym(s);
		// Unfortunately, some previously selected syms will not be directly
		// associatable with new glyphs, so not all selections can be preserved
		Iterator<SeqSymmetry> sym_iter = old_sym_selections.iterator();
		while (sym_iter.hasNext()) {
			SeqSymmetry old_selected_sym = sym_iter.next();

			GlyphI gl = seqmap.<GlyphI>getItem(old_selected_sym);
			if (gl != null) {
				seqmap.select(gl);
			}
		}
		
//		setSelectionStatus(getSelectionTitle(seqmap.getSelected()));
		if (show_edge_matches) {
			doEdgeMatching(seqmap.getSelected(), true);
		}
	}

	// copying map tiers to separate list to avoid problems when removing tiers
	//   (and thus modifying map.getTiers() list -- could probably deal with this
	//    via iterators, but feels safer this way...)
	private List<TierGlyph> copyMapTierGlyphs(List<TierGlyph> cur_tiers, int axis_index) {
		List<TierGlyph> temp_tiers = new ArrayList<TierGlyph>();
		for (int i = 0; i < cur_tiers.size(); i++) {
			if (i == axis_index) {
				continue;
			}
			TierGlyph tg = cur_tiers.get(i);
			temp_tiers.add(tg);
			if (DEBUG_TIERS) {
				System.out.println("removing tier from map: " + tg.getAnnotStyle().getTrackName());
			}
			seqmap.removeTier(tg);
		}
		return temp_tiers;
	}

	private void addGlyphs(List<TierGlyph> temp_tiers, int axis_index) {
		// The hairline needs to be among the first glyphs added,
		// to keep it from interfering with selection of other glyphs.
		if (hairline != null) {
			hairline.destroy();
		}
		hairline = new UnibrowHairline(seqmap);
		//hairline.getShadow().setLabeled(hairline_is_labeled);
		addPreviousTierGlyphs(seqmap, temp_tiers);
		axis_tier = addAxisTier(axis_index);
		addAnnotationTracks();
		hideEmptyTierGlyphs(new ArrayList<TierGlyph>(seqmap.getTiers()));
	}

	private static void addPreviousTierGlyphs(AffyTieredMap seqmap, List<TierGlyph> temp_tiers) {
		// add back in previous annotation tiers (with all children removed)
		if (temp_tiers != null) {
			for (int i = 0; i < temp_tiers.size(); i++) {
				TierGlyph tg = temp_tiers.get(i);
				if (DEBUG_TIERS) {
					System.out.println("adding back tier: " + tg.getAnnotStyle().getTrackName() + ", scene = " + tg.getScene());
				}
				if (tg.getAnnotStyle() != null) {
					tg.setStyle(tg.getAnnotStyle());
				}
				seqmap.addTier(tg, false);
			}
			temp_tiers.clear(); // redundant hint to garbage collection
		}
	}

	/**
	 * Set up a tier with fixed pixel height and place axis in it.
	 */
	private TierGlyph addAxisTier(int tier_index) {
		TransformTierGlyph resultAxisTier = new TransformTierGlyph(CoordinateStyle.coordinate_annot_style);
		resultAxisTier.setInfo(new RootSeqSymmetry(){
			@Override
			public FileTypeCategory getCategory() {
				return FileTypeCategory.Axis;
			}
		});
		resultAxisTier.setPacker(null);
		resultAxisTier.setFixedPixHeight(54);
		resultAxisTier.setDirection(TierGlyph.Direction.AXIS);
		AxisGlyph axis = seqmap.addAxis(27);
		axis.setHitable(true);
		axis.setFont(axisFont);

		Color axis_bg = CoordinateStyle.coordinate_annot_style.getBackground();
		Color axis_fg = CoordinateStyle.coordinate_annot_style.getForeground();

		axis.setBackgroundColor(axis_bg);
		resultAxisTier.setBackgroundColor(axis_bg);
		resultAxisTier.setFillColor(axis_bg);
		axis.setForegroundColor(axis_fg);
		resultAxisTier.setForegroundColor(axis_fg);
		setAxisFormatFromPrefs(axis);

		GlyphI cytoband_glyph = CytobandGlyph.makeCytobandGlyph(getAnnotatedSeq(), resultAxisTier, tier_index, this);
		if (cytoband_glyph != null) {
			resultAxisTier.addChild(cytoband_glyph);
			resultAxisTier.setFixedPixHeight(resultAxisTier.getFixedPixHeight() + (int) cytoband_glyph.getCoordBox().height);
		}

		resultAxisTier.addChild(axis);

		// it is important to set the colors before adding the tier
		// to the map, else the label tier colors won't match
		if (seqmap.getTiers().size() >= tier_index) {
			seqmap.addTier(resultAxisTier, tier_index);
		} else {
			seqmap.addTier(resultAxisTier, false);
		}

		seq_glyph = CharSeqGlyph.initSeqGlyph(viewseq, axis_fg, axis);

		resultAxisTier.addChild(seq_glyph);
		resultAxisTier.setCoords(0, 0, seqmap.getScene().getCoordBox().getWidth(), 54);
		
		return resultAxisTier;
	}

	private void shrinkWrap() {
		/*
		 * Shrink wrapping is a little more complicated than one might expect,
		 * but it needs to take into account the mapping of the annotated
		 * sequence to the view (although currently assumes this mapping doesn't
		 * do any rearrangements, etc.) (alternative, to ensure that _arbitrary_
		 * genometry mapping can be accounted for, is to base annotation bounds
		 * on map glyphs, but then have to go into tiers to get children bounds,
		 * and filter out stuff like axis and DNA glyphs, etc...)
		 */
		SeqSpan annot_bounds = SeqUtils.getAnnotationBounds(aseq);
		if (annot_bounds != null) {
			// transform to view
			MutableSeqSymmetry sym = new SimpleMutableSeqSymmetry();
			sym.addSpan(annot_bounds);
			if (aseq != viewseq) {
				SeqUtils.transformSymmetry(sym, transform_path);
			}
			SeqSpan view_bounds = sym.getSpan(viewseq);
			seqmap.setMapRange(view_bounds.getMin(), view_bounds.getMax());
		}
	}

	/**
	 * Returns all floating layers _except_ grid layer (which is supposed to
	 * stay behind everything else).
	 */
	private static List<GlyphI> getFloatingLayers(GlyphI root_glyph) {
		List<GlyphI> layers = new ArrayList<GlyphI>();
		int gcount = root_glyph.getChildCount();
		for (int i = 0; i < gcount; i++) {
			GlyphI cgl = root_glyph.getChild(i);
			if (cgl instanceof PixelFloaterGlyph) {
				layers.add(cgl);
			}
		}
		return layers;
	}

	/**
	 * hide TierGlyphs with no children (that is how IGB indicates that a glyph
	 * is garbage)
	 *
	 * @param tiers the list of TierGlyphs
	 */
	private void hideEmptyTierGlyphs(List<TierGlyph> tiers) {
		for (TierGlyph tg : tiers) {
			if (tg.getChildCount() == 0) {
				tg.setVisibility(false);
				if(tg instanceof DefaultTierGlyph){
					((DefaultTierGlyph)tg).setHeightFixed(false);
				}
			}
		}
	}

	private void addAnnotationTracks() {
		TrackView.getInstance().addTracks(this, aseq);
		if (aseq.getComposition() != null) {
			handleCompositionSequence();
		}
		addDependentAndEmptyTrack();
	}

	protected void addDependentAndEmptyTrack(){
		TrackView.getInstance().addDependentAndEmptyTrack(this, aseq);
	}

	// muck with aseq, seq2viewsym, transform_path to trick addAnnotationTiers(),
	//   addLeafsToTier(), addToTier(), etc. into mapping from composition sequences
	private void handleCompositionSequence() {
		BioSeq cached_aseq = aseq;
		MutableSeqSymmetry cached_seq2viewSym = seq2viewSym;
		SeqSymmetry[] cached_path = transform_path;
		SeqSymmetry comp = aseq.getComposition();
		// assuming a two-level deep composition hierarchy for now...
		//   need to make more recursive at some point...
		//   (or does recursive call to addAnnotationTiers already give us full recursion?!!)
		int scount = comp.getChildCount();
		for (int i = 0; i < scount; i++) {
			SeqSymmetry csym = comp.getChild(i);
			// return seq in a symmetry span that _doesn't_ match aseq
			BioSeq cseq = SeqUtils.getOtherSeq(csym, cached_aseq);
			if (cseq != null) {
				aseq = cseq;
				if (cached_seq2viewSym == null) {
					transform_path = new SeqSymmetry[1];
					transform_path[0] = csym;
				} else {
					transform_path = new SeqSymmetry[2];
					transform_path[0] = csym;
					transform_path[1] = cached_seq2viewSym;
				}
				addAnnotationTracks();
			}
		}
		// restore aseq and seq2viewsym afterwards...
		aseq = cached_aseq;
		seq2viewSym = cached_seq2viewSym;
		transform_path = cached_path;
	}

	@Override
	public boolean isGenomeSequenceSupported() {
		return true;
	}

	@Override
	public final BioSeq getAnnotatedSeq() {
		return aseq;
	}

	/**
	 * Gets the view seq. Note: {@link #getViewSeq()} and {@link #getAnnotatedSeq()}
	 * may return different BioSeq's ! This allows for reverse complement, coord
	 * shifting, seq slicing, etc. Returns BioSeq that is the SeqMapView's
	 * _view_ onto the BioSeq returned by getAnnotatedSeq()
	 *
	 * @see #getTransformPath()
	 */
	@Override
	public final BioSeq getViewSeq() {
		return viewseq;
	}

	/**
	 * Returns the series of transformations that can be used to map a
	 * SeqSymmetry from {@link #getAnnotatedSeq()} to
	 *  {@link #getViewSeq()}.
	 */
	@Override
	public final SeqSymmetry[] getTransformPath() {
		return transform_path;
	}

	/**
	 * Returns a transformed copy of the given symmetry based on
	 *  {@link #getTransformPath()}. If no transform is necessary, simply returns
	 * the original symmetry.
	 */
	@Override
	public final SeqSymmetry transformForViewSeq(SeqSymmetry insym, BioSeq seq_to_compare) {
		if (seq_to_compare != getViewSeq()) {
			MutableSeqSymmetry tempsym = SeqUtils.copyToDerived(insym);
			SeqUtils.transformSymmetry(tempsym, getTransformPath());
			return tempsym;
		}
		return insym;
	}

	@Override
	public final AffyTieredMap getSeqMap() {
		return seqmap;
	}

	@Override
	public void setDataModelFromOriginalSym(GlyphI g, SeqSymmetry sym) {
		seqmap.setDataModelFromOriginalSym(g, sym);
	}

	@Override
	public final void selectAllGraphs() {
		List<GraphGlyph> glyphlist = collectGraphs();
		List<GraphGlyph> visibleList = new ArrayList<GraphGlyph>(glyphlist.size());

		//Remove hidden Graphs
		for (GraphGlyph g : glyphlist) {
			if (g.getGraphState().getTierStyle().getShow()) {
				visibleList.add(g);
			}
		}
		glyphlist.clear();

		// convert graph glyphs to GraphSyms via glyphsToSyms

		// Bring them all into the visual area
		for (GraphGlyph gl : visibleList) {
			GraphGlyphUtils.checkPixelBounds(gl, getSeqMap());
		}

		select(glyphsToSyms(visibleList), false, true, true);
	}

	@Override
	public final void select(List<SeqSymmetry> sym_list, boolean normal_selection) {
		select(sym_list, false, normal_selection, true);
		if (normal_selection) {
			zoomToSelections();
			List<GlyphI> glyphs = seqmap.getSelected();
			setSelectionStatus(getSelectionTitle(glyphs));
			if (show_edge_matches) {
				doEdgeMatching(glyphs, false);
			}
		}
	}
	
	public final void selectTrack(TierGlyph tier, boolean selected) {
		if (tier.getAnnotStyle().getFloatTier()) {
			tier.setSelected(selected);
		}
		else if (selected) {
			tier_manager.select(tier);
		}
		else {
			tier_manager.deselect(tier);
		}
		seqmap.updateWidget();
		postSelections();
	}

	private void select(List<SeqSymmetry> sym_list, boolean add_to_previous,
			boolean call_listeners, boolean update_widget) {
		if (!add_to_previous) {
			clearSelection();
		}

		for (SeqSymmetry sym : sym_list) {
			// currently assuming 1-to-1 mapping of sym to glyph
			GlyphI gl = seqmap.<GlyphI>getItem(sym);
			if (gl != null) {
				seqmap.select(gl);
			}
		}
		if (update_widget) {
			seqmap.updateWidget();
		}
		if (call_listeners) {
			postSelections();
		}
	}

	protected final void clearSelection() {
		sym_used_for_title = null;
		tier_used_in_selection_info = null;
		seqmap.clearSelected();
		setSelectedRegion(null, false);
		//  clear match_glyphs?
	}

	/**
	 * Figures out which symmetries are currently selected and then calls
	 *  {@link GenometryModel#setSelectedSymmetries(List, List, Object)}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void postSelections() {
		// Note that seq_selected_sym (the selected residues) is not included in selected_syms
		gmodel.setSelectedSymmetries(glyphsToRootSyms(tier_manager.getSelectedTiers()), getSelectedSyms(), this);
	}

	public void trackstylePropertyChanged(EventObject eo) {
		//postSelections();
	}

	// assumes that region_sym contains a span with span.getBioSeq() ==  current seq (aseq)
	public final void setSelectedRegion(SeqSymmetry region_sym, boolean update_widget) {
		seq_selected_sym = region_sym;
		// Note: SUBSELECT_SEQUENCE might possibly be set to false in the AltSpliceView
		if (subselectSequence && seq_glyph != null) {
			if (region_sym == null) {
				seq_glyph.setSelected(false);
			} else {
				SeqSpan seq_region = seq_selected_sym.getSpan(aseq);
				// corrected for interbase coords
				seq_glyph.select(seq_region.getMin(), seq_region.getMax() - 1);
				setSelectionStatus(SeqUtils.spanToString(seq_region));
			}
			if (update_widget) {
				seqmap.updateWidget();
			}
		}
	}

	public List<GlyphI> getAllSelectedTiers() {
		List<GlyphI> allSelectedTiers = new ArrayList<GlyphI>();
		// this adds all tracks selected on the label, including join tracks (not join children)
		for (TierGlyph tierGlyph : tier_manager.getSelectedTiers()) {
			allSelectedTiers.add(tierGlyph);
		}	
		// this adds all tracks selected on the track itself (arrow on left edge), including join tracks and join children
		for (TierGlyph tierGlyph : tier_manager.getVisibleTierGlyphs()) {
			if (!allSelectedTiers.contains(tierGlyph)) {
				if(tierGlyph.getTierType() == TierGlyph.TierType.GRAPH && tierGlyph.getChildCount() > 0){
					for (GlyphI child : tierGlyph.getChildren()) {
						if (child.isSelected()) {
							allSelectedTiers.add(child);
						}
					}
				}else if (tierGlyph.isSelected()) {
					allSelectedTiers.add(tierGlyph);
				}
			}
		}
		return allSelectedTiers;
	}

	@Override
	public List<GraphGlyph> getSelectedFloatingGraphGlyphs() {
		List<GraphGlyph> graphGlyphs = new ArrayList<GraphGlyph>();
		if (pixel_floater_glyph.getChildren() != null) {
			for (GlyphI floatGlyph : pixel_floater_glyph.getChildren()) {
				if (floatGlyph.isSelected()) {
					graphGlyphs.add((GraphGlyph)floatGlyph);
				}
			}
		}
		return graphGlyphs;
	}

	/**
	 * Determines which SeqSymmetry's are selected by looking at which Glyph's
	 * are currently selected. The list will not include the selected sequence
	 * region, if any. Use getSelectedRegion() for that.
	 *
	 * @return a List of SeqSymmetry objects, possibly empty.
	 */
	@Override
	public List<SeqSymmetry> getSelectedSyms() {
		return glyphsToSyms(seqmap.getSelected());
	}

	/**
	 * Given a list of glyphs, returns a list of syms that those glyphs
	 * represent.
	 */
	public static List<SeqSymmetry> glyphsToSyms(List<? extends GlyphI> glyphs) {
		Set<SeqSymmetry> symSet = new LinkedHashSet<SeqSymmetry>(glyphs.size());	// use LinkedHashSet to preserve order
		for (GlyphI gl : glyphs) {
			if (gl.getInfo() instanceof SeqSymmetry) {
				symSet.add((SeqSymmetry) gl.getInfo());
			}
		}
		return new ArrayList<SeqSymmetry>(symSet);
	}

	/**
	 * Given a list of glyphs, returns a list of root syms that those glyphs
	 * represent.
	 */
	public static List<RootSeqSymmetry> glyphsToRootSyms(List<? extends GlyphI> glyphs) {
		Set<RootSeqSymmetry> symSet = new LinkedHashSet<RootSeqSymmetry>(glyphs.size());	// use LinkedHashSet to preserve order
		for (GlyphI gl : glyphs) {
			if (gl.getInfo() instanceof RootSeqSymmetry) {
				symSet.add((RootSeqSymmetry) gl.getInfo());
			}
		}
		return new ArrayList<RootSeqSymmetry>(symSet);
	}

	@Override
	public final void zoomTo(SeqSpan span) {
		BioSeq zseq = span.getBioSeq();
		if (zseq != null && zseq != this.getAnnotatedSeq()) {
			gmodel.setSelectedSeq(zseq);
		}
		zoomTo(span.getMin(), span.getMax());
	}

	public final double getPixelsToCoord(double smin, double smax) {
		if (getAnnotatedSeq() == null) {
			return 0;
		}
		double coord_width = Math.min(getAnnotatedSeq().getLengthDouble(), smax) - Math.max(getAnnotatedSeq().getMin(), smin);
		double pixel_width = seqmap.getView().getPixelBox().width;
		double pixels_per_coord = pixel_width / coord_width; // can be Infinity, but the Math.min() takes care of that
		pixels_per_coord = Math.min(pixels_per_coord, seqmap.getMaxZoom(NeoAbstractWidget.X));
		return pixels_per_coord;
	}

	public final void zoomTo(double smin, double smax) {
		double pixels_per_coord = getPixelsToCoord(smin, smax);
		seqmap.zoom(NeoAbstractWidget.X, pixels_per_coord);
		seqmap.scroll(NeoAbstractWidget.X, smin);
		seqmap.setZoomBehavior(AffyTieredMap.X, AffyTieredMap.CONSTRAIN_COORD, (smin + smax) / 2);
		seqmap.updateWidget();
	}

	/**
	 * Zoom to a region including all the currently selected Glyphs.
	 */
	public final void zoomToSelections() {
		List<GlyphI> selections = seqmap.getSelected();
		if (selections.size() > 0) {
			zoomToGlyphs(selections);
		} else if (seq_selected_sym != null) {
			SeqSpan span = getViewSeqSpan(seq_selected_sym);
			zoomTo(span);
		}
//		else{
//			zoomToRectangle(seqmap.getCoordBounds()); //Enable double click to zoom out here.
//		}
	}

	/**
	 * Center at the hairline.
	 */
	@Override
	public final void centerAtHairline() {
		if (this.hairline == null) {
			return;
		}
		double pos = this.hairline.getSpot();
		Rectangle2D.Double vbox = this.getSeqMap().getViewBounds();
		double map_start = pos - vbox.width / 2;

		this.getSeqMap().scroll(NeoMap.X, map_start);
		this.setZoomSpotX(pos);
		this.getSeqMap().updateWidget();
	}

	public void zoomToGlyphs(List<GlyphI> glyphs) {
		zoomToRectangle(getRegionForGlyphs(glyphs));
	}

	/**
	 * Returns a rectangle containing all the current selections.
	 *
	 * @return null if the vector of glyphs is empty
	 */
	private static Rectangle2D.Double getRegionForGlyphs(List<GlyphI> glyphs) {
		if (glyphs.isEmpty()) {
			return null;
		}
		Rectangle2D.Double rect = new Rectangle2D.Double();
		GlyphI g0 = glyphs.get(0);
		rect.setRect(g0.getCoordBox());
		for (GlyphI g : glyphs) {
			rect.add(g.getCoordBox());
		}
		return rect;
	}

	/**
	 * Zoom to include (and slightly exceed) a given rectangular region in
	 * coordbox coords.
	 */
	private void zoomToRectangle(Rectangle2D.Double rect) {
		if (rect != null && aseq != null) {
			double desired_width = Math.min(rect.width * 1.1f, aseq.getLength() * 1.0f);
			seqmap.zoom(NeoAbstractWidget.X, Math.min(
					seqmap.getView().getPixelBox().width / desired_width,
					seqmap.getMaxZoom(NeoAbstractWidget.X)));
			seqmap.scroll(NeoAbstractWidget.X, -(seqmap.getVisibleRange()[0]));
			seqmap.scroll(NeoAbstractWidget.X, (rect.x - rect.width * 0.05));
			double map_center = rect.x + rect.width / 2 - seqmap.getViewBounds().width / 2;
			seqmap.scroll(NeoAbstractWidget.X, map_center);	// Center at hairline
			seqmap.setZoomBehavior(AffyTieredMap.X, AffyTieredMap.CONSTRAIN_COORD, (rect.x + rect.width / 2));
			seqmap.setZoomBehavior(AffyTieredMap.Y, AffyTieredMap.CONSTRAIN_COORD, (rect.y + rect.height / 2));
			seqmap.updateWidget();
		}
	}

	public final void toggleClamp() {
		clamp(clampedRegion == null);
		seqmap.stretchToFit(false, false); // to adjust scrollers and zoomers
		seqmap.updateWidget();
	}

	public void clamp(boolean clamp) {
		if (clamp) {
			Rectangle2D.Double vbox = seqmap.getViewBounds();
			seqmap.setMapRange((int) (vbox.x), (int) (vbox.x + vbox.width));
			clampedRegion = new SimpleSeqSpan((int) (vbox.x), (int) (vbox.x + vbox.width), viewseq);
		} else {
			if (viewseq != null) {
				int min = viewseq.getMin();
				int max = viewseq.getMax();
				seqmap.setMapRange(min, max);
				clampedRegion = null;
			}
		}
		ClampViewAction.getAction().putValue(Action.SELECTED_KEY, clampedRegion != null);
	}

	/**
	 * Do edge matching. If query_glyphs is empty, clear all edges.
	 *
	 * @param query_glyphs
	 * @param update_map
	 */
	public final void doEdgeMatching(List<GlyphI> query_glyphs, boolean update_map) {
		// Clear previous edges
		if (match_glyphs != null && match_glyphs.size() > 0) {
			seqmap.removeItem(match_glyphs);  // remove all match glyphs in match_glyphs vector
		}

		int qcount = query_glyphs.size();
		int match_query_count = query_glyphs.size();
		for (int i = 0; i < qcount && match_query_count <= max_for_matching; i++) {
			match_query_count += query_glyphs.get(i).getChildCount();
		}

		if (match_query_count <= max_for_matching) {
			match_glyphs.clear();
			ArrayList<GlyphI> target_glyphs = new ArrayList<GlyphI>();
			target_glyphs.add(seqmap.getScene().getGlyph());
			double fuzz = getEdgeMatcher().getFuzziness();
			if (fuzz == 0.0) {
				Color edge_match_color = PreferenceUtils.getColor(PreferenceUtils.getTopNode(), PREF_EDGE_MATCH_COLOR, default_edge_match_color);
				getEdgeMatcher().setColor(edge_match_color);
			} else {
				Color edge_match_fuzzy_color = PreferenceUtils.getColor(PreferenceUtils.getTopNode(), PREF_EDGE_MATCH_FUZZY_COLOR, default_edge_match_fuzzy_color);
				getEdgeMatcher().setColor(edge_match_fuzzy_color);
			}
			getEdgeMatcher().matchEdges(seqmap, query_glyphs, target_glyphs, match_glyphs);
			setStatus(null);
		} else {
			setStatus("Skipping edge matching; too many items selected.");
		}

		if (update_map) {
			seqmap.updateWidget();
		}
	}

	public final boolean getEdgeMatching() {
		return show_edge_matches;
	}

	public final void setEdgeMatching(boolean b) {
		show_edge_matches = b;
		if (show_edge_matches) {
			doEdgeMatching(seqmap.getSelected(), true);
		} else {
			doEdgeMatching(new ArrayList<GlyphI>(0), true);
		}
	}

	public final void adjustEdgeMatching(int bases) {
		getEdgeMatcher().setFuzziness(bases);
		if (show_edge_matches) {
			doEdgeMatching(seqmap.getSelected(), true);
		}
	}

	/**
	 * return a SeqSpan representing the visible bounds of the view seq
	 */
	@Override
	public final SeqSpan getVisibleSpan() {
		Rectangle2D.Double vbox = seqmap.getView().getCoordBox();
		SeqSpan vspan = new SimpleSeqSpan((int) vbox.x,
				(int) (vbox.x + vbox.width),
				viewseq);
		return vspan;
	}

	public final GlyphEdgeMatcher getEdgeMatcher() {
		return edge_matcher;
	}

	public final void setShrinkWrap(boolean b) {
		shrinkWrapMapBounds = b;
		setAnnotatedSeq(aseq);
		ShrinkWrapAction.getAction().putValue(Action.SELECTED_KEY, b);
	}

	public final boolean getShrinkWrap() {
		return shrinkWrapMapBounds;
	}

	/**
	 * SymSelectionListener interface
	 */
	public void symSelectionChanged(SymSelectionEvent evt) {
		Object src = evt.getSource();

		// ignore self-generated xym selection -- already handled internally
		if (src == this) {
			String title = getSelectionTitle(seqmap.getSelected());
			setSelectionStatus(title);
		} // ignore sym selection originating from AltSpliceView, don't want to change internal selection based on this
		else if ((src instanceof AltSpliceView) || (src instanceof SeqMapView)) {
			// catching SeqMapView as source of event because currently sym selection events actually originating
			//    from AltSpliceView have their source set to the AltSpliceView's internal SeqMapView...
		} else {
			List<SeqSymmetry> symlist = evt.getSelectedGraphSyms();
			// select:
			//   add_to_previous ==> false
			//   call_listeners ==> false
			//   update_widget ==>  false   (zoomToSelections() will make an updateWidget() call...)
			select(symlist, true, true, false);
			// Zoom to selections, unless the selection was caused by the TierLabelManager
			// (which sets the selection source as the AffyTieredMap, i.e. getSeqMap())
			if (src != getSeqMap() && src != getTierManager()) {
				zoomToSelections();
			}
			String title = getSelectionTitle(seqmap.getSelected());
			setSelectionStatus(title);
		}
	}

	/**
	 * Sets the hairline position and zoom center to the given spot. Does not
	 * call map.updateWidget()
	 */
	public final void setZoomSpotX(double x) {
		int intx = (int) x;
		if (hairline != null) {
			hairline.setSpot(intx);
		}
		seqmap.setZoomBehavior(AffyTieredMap.X, AffyTieredMap.CONSTRAIN_COORD, intx);
	}

	/**
	 * Sets the hairline position to the given spot. Does not call
	 * map.updateWidget()
	 */
	public final void setZoomSpotY(double y) {
		seqmap.setZoomBehavior(AffyTieredMap.Y, AffyTieredMap.CONSTRAIN_COORD, y);
	}

	@Override
	public final SeqMapViewMouseListener getMouseListener() {
		return mouse_listener;
	}

	/**
	 * Select the parents of the current selections
	 */
	public final void selectParents() {
		if (seqmap.getSelected().isEmpty()) {
			ErrorHandler.errorPanel("Nothing selected");
			return;
		}

		boolean top_level = seqmap.getSelected().size() > 1;
		// copy selections to a new list before starting, because list of selections will be modified
		List<GlyphI> all_selections = new ArrayList<GlyphI>(seqmap.getSelected());
		Iterator<GlyphI> iter = all_selections.iterator();
		while (iter.hasNext()) {
			GlyphI child = iter.next();
			GlyphI pglyph = getParent(child, top_level);
			if (pglyph != child) {
				seqmap.deselect(child);
				seqmap.select(pglyph);
			}
		}

		if (show_edge_matches) {
			doEdgeMatching(seqmap.getSelected(), false);
		}
		seqmap.updateWidget();
		postSelections();
	}

	/**
	 * Find the top-most parent glyphs of the given glyphs.
	 *
	 * @param childGlyphs a list of GlyphI objects, typically the selected
	 * glyphs
	 * @return a list where each child is replaced by its top-most parent, if it
	 * has a parent, or else the child itself is included in the list
	 */
	static List<GlyphI> getParents(List<GlyphI> childGlyphs) {
		// linked hash set keeps parents in same order as child list so that comparison
		// like childList.equals(parentList) can be used.
		Set<GlyphI> results = new LinkedHashSet<GlyphI>(childGlyphs.size());
		for (GlyphI child : childGlyphs) {
			GlyphI pglyph = getParent(child, true);
			results.add(pglyph);
		}
		return new ArrayList<GlyphI>(results);
	}

	/**
	 * Get the parent, or top-level parent, of a glyph, with certain
	 * restrictions. Will not return a TierGlyph or RootGlyph or a glyph that
	 * isn't hitable, but will return the original GlyphI instead.
	 *
	 * @param top_level if true, will recurse up to the top-level parent, with
	 * certain restrictions: recursion will stop before reaching a TierGlyph
	 */
	private static GlyphI getParent(GlyphI g, boolean top_level) {
		GlyphI pglyph = g.getParent();
		// the test for isHitable will automatically exclude seq_glyph
		if (pglyph != null && pglyph.isHitable() && !(pglyph instanceof TierGlyph) && !(pglyph instanceof RootGlyph)) {
			if (top_level) {
				GlyphI t = pglyph;
				while (t != null && t.isHitable() && !(t instanceof TierGlyph) && !(t instanceof RootGlyph)) {
					pglyph = t;
					t = t.getParent();
				}
			}
			return pglyph;
		}
		return g;
	}

	private void setSelectionStatus(String title){
		Map<String, Object> props = null;
		if(tier_used_in_selection_info != null){
			props = TierLabelManager.getTierProperties(tier_used_in_selection_info);
		}else{
			 props = determineProps(sym_used_for_title);
		}
		Application.getSingleton().setSelField(props, title);
	}
	
	private void setStatus(String title) {
		if (!report_status_in_status_bar) {
			return;
		}
		Application.getSingleton().setStatus(title, false);
	}

	// Compare the code here with SymTableView.selectionChanged()
	// The logic about finding the ID from instances of DerivedSeqSymmetry
	// should be similar in both places, or else users could get confused.
	private String getSelectionTitle(List<GlyphI> selected_glyphs) {
		String id = null;
		tier_used_in_selection_info = null;
		if (selected_glyphs.isEmpty()) {
			id = "";
			sym_used_for_title = null;
			List<TierGlyph> tierglyphs = getTierManager().getSelectedTiers();
			if(!tierglyphs.isEmpty()){
				if(tierglyphs.size() > 1){
					id = "" + tierglyphs.size() + " Selections";
					sym_used_for_title = null;
				}else{
					tier_used_in_selection_info = tierglyphs.get(0);
					Map<String,Object> props = TierLabelManager.getTierProperties(tier_used_in_selection_info);
					if(props != null && !props.isEmpty() && props.containsKey("File Name")){
						id = props.get("File Name").toString();
						sym_used_for_title = (SeqSymmetry)tierglyphs.get(0).getInfo();
					}
				}
			}
		} else {
			if (selected_glyphs.size() == 1) {
				GlyphI topgl = selected_glyphs.get(0);
				Object info = topgl.getInfo();
				SeqSymmetry sym = null;
				if (info instanceof SeqSymmetry) {
					sym = (SeqSymmetry) info;
				}
				if (sym instanceof MutableSingletonSeqSymmetry) {
					id = ((LeafSingletonSymmetry) sym).getID();
					sym_used_for_title = sym;
				}
				if (id == null && sym instanceof GraphSym) {
					id = ((GraphSym) sym).getGraphName();
					sym_used_for_title = sym;
				}
				if (id == null && sym instanceof SymWithProps) {
					id = (String) ((SymWithProps) sym).getProperty("gene name");
					sym_used_for_title = sym;
				}
				if (id == null && sym instanceof SymWithProps) {
					id = (String) ((SymWithProps) sym).getProperty("id");
					sym_used_for_title = sym;
				}
				if (id == null && sym instanceof DerivedSeqSymmetry) {
					SeqSymmetry original = ((DerivedSeqSymmetry) sym).getOriginalSymmetry();
					if (original instanceof MutableSingletonSeqSymmetry) {
						id = ((LeafSingletonSymmetry) original).getID();
						sym_used_for_title = original;
					} else if (original instanceof SymWithProps) {
						id = (String) ((SymWithProps) original).getProperty("id");
						sym_used_for_title = original;
					}
				}
				if (id == null && topgl instanceof CharSeqGlyph && seq_selected_sym != null) {
					SeqSpan seq_region = seq_selected_sym.getSpan(aseq);
					id = SeqUtils.spanToString(seq_region);
					sym_used_for_title = seq_selected_sym;
				}
				if (id == null && topgl instanceof GraphGlyph) {
					GraphGlyph gg = (GraphGlyph) topgl;
					if (gg.getLabel() != null) {
						id = "Graph: " + gg.getLabel();
					} else {
						id = "Graph Selected";
					}
					sym_used_for_title = null;
				}
				if (id == null) {
					// If ID of item is null, check recursively for parent ID, or parent of that...
					GlyphI pglyph = topgl.getParent();
					if (pglyph != null && !(pglyph instanceof TierGlyph) && !(pglyph instanceof RootGlyph)) {
						// Add one ">" symbol for each level of getParent()
						sym_used_for_title = null; // may be re-set in the recursive call
						id = "> " + getSelectionTitle(Arrays.asList(pglyph));
					} else {
						id = "Unknown Selection";
						sym_used_for_title = null;
					}
				}
			} else {
				sym_used_for_title = null;
				id = "" + selected_glyphs.size() + " Selections";
			}
		}
		return id;
	}

	final void showPopup(NeoMouseEvent nevt) {
		if (sym_popup == null) {
			sym_popup = new JPopupMenu();
			setupPopups();
		}
		sym_popup.setVisible(false); // in case already showing
		sym_popup.removeAll();

		if (seqmap.getSelected().isEmpty()) { // if no glyphs selected, use regular popup
//			sym_popup.setVisible(true);
//			getTierManager().doPopup(nevt);
			return;
		}

		preparePopup(sym_popup, nevt);

		if (sym_popup.getComponentCount() > 0) {

			if (nevt == null) {
				// this might happen from pressing the Windows context menu key
				sym_popup.show(seqmap, 15, 15);
				return;
			}

			// if resultSeqMap is a MultiWindowTierMap, then using resultSeqMap as Component target arg to popup.show()
			//  won't work, since its component is never actually rendered -- so checking here
			/// to use appropriate target Component and pixel position
			EventObject oevt = nevt.getOriginalEvent();
			if ((oevt != null) && (oevt.getSource() instanceof Component)) {
				Component target = (Component) oevt.getSource();
				if (oevt instanceof MouseEvent) {
					MouseEvent mevt = (MouseEvent) oevt;
					sym_popup.show(target, mevt.getX() + xoffset_pop, mevt.getY() + yoffset_pop);
				} else {
					sym_popup.show(target, nevt.getX() + xoffset_pop, nevt.getY() + yoffset_pop);
				}
			} else {
				sym_popup.show(seqmap, nevt.getX() + xoffset_pop, nevt.getY() + yoffset_pop);
			}
		}
		// For garbage collection, it would be nice to add a listener that
		// could call sym_popup.removeAll() when the popup is removed from view.

		/*
		 * Force a repaint of the JPopupMenu. This is a work-around for an Apple
		 * JVM Bug (verified on 10.5.8, Java Update 5). Affected systems will
		 * display a stale copy of the JPopupMenu if the current number of menu
		 * items is equal to the previous number of menu items.
		 *
		 * The repaint must occur after the menu has been drawn: it appears to
		 * skip the repaint if isVisible is false. (another optimisation?)
		 */
		sym_popup.repaint();
	}

	/**
	 * Prepares the given popup menu to be shown. The popup menu should have
	 * items added to it by this method. Display of the popup menu will be
	 * handled by showPopup(), which calls this method.
	 */
	protected void preparePopup(JPopupMenu popup, NeoMouseEvent nevt) {
		List<GlyphI> selected_glyphs = seqmap.getSelected();
		
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		if(selected_glyphs.size() == 1){
			Border colorBorder = BorderFactory.createLineBorder(selected_glyphs.get(0).getColor());
			popup.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		} else {
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			popup.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		}
		
		JMenuItem select_parent_action = new JMenuItem(SelectParentAction.getAction());
		select_parent_action.setIcon(null);
		JMenuItem zoom_on_selected = new JMenuItem(ZoomOnSelectedSymsAction.getAction());
		zoom_on_selected.setIcon(null);
		JMenuItem view_genomic_sequence_action = new JMenuItem(ViewGenomicSequenceInSeqViewerAction.getAction());
		view_genomic_sequence_action.setIcon(null);
		JMenuItem view_read_sequence_action = new JMenuItem(ViewReadSequenceInSeqViewerAction.getAction());
		view_read_sequence_action.setIcon(null);
		JMenuItem load_partial_sequence = new JMenuItem(LoadPartialSequenceAction.getAction());
		load_partial_sequence.setIcon(null);
		JMenuItem copy_residues_action = new JMenuItem(CopyResiduesAction.getAction());
		copy_residues_action.setIcon(null);

		setPopupMenuTitle(sym_info, selected_glyphs);

		popup.add(sym_info);
//		if (!selected_glyphs.isEmpty()) {
//			popup.add(zoomtoMI);
//		}
		List<SeqSymmetry> selected_syms = getSelectedSyms();
		if (!selected_syms.isEmpty() && !(selected_syms.get(0) instanceof GraphSym)) {

			//popup.add(SelectParentAction.getAction());
			popup.add(select_parent_action);
			//popup.add(ZoomOnSelectedSymsAction.getAction());
			popup.add(zoom_on_selected);
			// Disable view seq in seq viewer option for insertion
			//ViewGenomicSequenceInSeqViewerAction viewGenomicSequenceInSeqViewerAction = ViewGenomicSequenceInSeqViewerAction.getAction();
			view_genomic_sequence_action.setEnabled(selected_glyphs.size() > 1 || (!selected_glyphs.isEmpty() && !(selected_glyphs.get(0) instanceof InsertionSeqGlyph)));
			popup.add(view_genomic_sequence_action);
			//popup.add(new JMenuItem(viewGenomicSequenceInSeqViewerAction));
			popup.add(view_read_sequence_action);
			//popup.add(new JMenuItem(ViewReadSequenceInSeqViewerAction.getAction()));
		}

		for (ContextualPopupListener listener : popup_listeners) {
			listener.popupNotify(popup, selected_syms, sym_used_for_title);
		}

		TierGlyph tglyph = tier_manager.getTierGlyph(nevt);

		if (tglyph != null) {
			GenericFeature feature = tglyph.getAnnotStyle().getFeature();
			if (feature == null) {
				//Check if clicked on axis.
				if (tglyph == axis_tier) {
					SeqSpan visible = getVisibleSpan();
					if (selected_syms.isEmpty() && !gmodel.getSelectedSeq().isAvailable(visible.getMin(), visible.getMax())) {
						popup.add(load_partial_sequence);
						//popup.add(new JMenuItem(LoadPartialSequenceAction.getAction()));
					}

					if (seq_selected_sym != null && aseq.isAvailable(seq_selected_sym.getSpan(aseq))) {
						//popup.add(new JMenuItem(CopyResiduesAction.getActionShort()));
						//popup.add(new JMenuItem(ViewGenomicSequenceInSeqViewerAction.getAction()));
						//popup.add(new JMenuItem(ViewReadSequenceInSeqViewerAction.getAction()));
						popup.add(copy_residues_action);
						view_genomic_sequence_action.setEnabled(true);
						popup.add(view_genomic_sequence_action);
						popup.add(view_read_sequence_action);
					}
				}

//				return;
			}

//			if (feature.getLoadStrategy() != LoadStrategy.NO_LOAD && feature.getLoadStrategy() != LoadStrategy.GENOME) {
//				JMenuItem refresh_a_feature= new JMenuItem(RefreshAFeatureAction.createRefreshAFeatureAction(feature));
//				refresh_a_feature.setIcon(null);
//				popup.add(refresh_a_feature);
//			}
		}
	}

	// sets the text on the JLabel based on the current selection
	private void setPopupMenuTitle(JLabel label, List<GlyphI> selected_glyphs) {
		String title = "";
		if (selected_glyphs.size() == 1 && selected_glyphs.get(0) instanceof GraphGlyph) {
			GraphGlyph gg = (GraphGlyph) selected_glyphs.get(0);
			title = gg.getLabel();
		} else {
			title = getSelectionTitle(selected_glyphs);
		}
		// limit the popup title to 30 characters because big popup-menus don't work well
		if (title != null && title.length() > 30) {
			title = title.substring(0, 30) + " ...";
		}
		label.setText(title);
	}

	@Override
	public void addPopupListener(ContextualPopupListener listener) {
		popup_listeners.add(listener);
	}

	@Override
	public void removePopupListener(ContextualPopupListener listener) {
		popup_listeners.remove(listener);
	}
	
	private boolean matchesCategory(RootSeqSymmetry rootSeqSymmetry, FileTypeCategory... categories) {
		if (rootSeqSymmetry == null){
			return false;
		}
		
		if(categories == null || categories.length == 0 || (categories.length == 1 && categories[0] == null))
			return true;
		
		for(FileTypeCategory category : categories){
			if(rootSeqSymmetry.getCategory() == category){
				return true;
			}
		}
		
		return false;
	}

	public void selectAll(FileTypeCategory... category) {
		clearAllSelections();
		// this selects all regular tracks on the label
		AffyTieredMap labelmap = ((AffyLabelledTierMap) seqmap).getLabelMap();
		for (TierLabelGlyph labelGlyph : tier_manager.getAllTierLabels()) {
			TierGlyph tierGlyph = (TierGlyph) labelGlyph.getInfo();
			if (labelGlyph.isVisible()
					&& tierGlyph.getInfo() != null) {
				boolean matches = matchesCategory((RootSeqSymmetry) tierGlyph.getInfo(), category);
				if (matches) {
					labelmap.select(labelGlyph);
				}
			}
		}
		// this selects all floating tracks
		if (pixel_floater_glyph.getChildren() != null) {
			for (GlyphI floatGlyph : pixel_floater_glyph.getChildren()) {
				boolean matches = matchesCategory((RootSeqSymmetry) floatGlyph.getInfo(), category);
				if (matches) {
					seqmap.select(floatGlyph);
				}
			}
		}
		// this selects all join subtracks on the track itself (arrow on left edge)
		for (TierGlyph tierGlyph : tier_manager.getVisibleTierGlyphs()) {
			if (tierGlyph.getTierType() == TierGlyph.TierType.GRAPH && tierGlyph.getChildCount() > 0) {
				for (GlyphI child : tierGlyph.getChildren()) {
					boolean matches = matchesCategory((RootSeqSymmetry) child.getInfo(), category);
					if (matches) {
						seqmap.select(child);
					}
				}
			}
		}
		gmodel.setSelectedSymmetries(glyphsToRootSyms(tier_manager.getSelectedTiers()), getSelectedSyms(), this);
		seqmap.updateWidget();
	}
	
	private void clearAllSelections() {
		AffyTieredMap labelmap = ((AffyLabelledTierMap) seqmap).getLabelMap();
		labelmap.clearSelected();
		if (pixel_floater_glyph.getChildren() != null) {
			for (GlyphI floatGlyph : pixel_floater_glyph.getChildren()) {
				floatGlyph.setSelected(false);
			}
		}
		for (TierGlyph tierGlyph : tier_manager.getVisibleTierGlyphs()) {
			if (tierGlyph.getTierType() == TierGlyph.TierType.GRAPH && tierGlyph.getChildCount() > 0) {
				for (GlyphI child : tierGlyph.getChildren()) {
					child.setSelected(false);
				}
			}
		}
	}

	public void deselectAll() {
		clearAllSelections();
		gmodel.setSelectedSymmetries(Collections.<RootSeqSymmetry>emptyList(), Collections.<SeqSymmetry>emptyList(), this);
		seqmap.updateWidget();
	}

	/**
	 * Recurse through glyphs and collect those that are instances of
	 * GraphGlyph.
	 */
	final List<GraphGlyph> collectGraphs() {
		List<GraphGlyph> graphs = new ArrayList<GraphGlyph>();
		GlyphI root = seqmap.getScene().getGlyph();
		collectGraphs(root, graphs);
		return graphs;
	}

	/**
	 * Recurse through glyph hierarchy and collect graphs.
	 */
	private static void collectGraphs(GlyphI gl, List<GraphGlyph> graphs) {
		int max = gl.getChildCount();
		for (int i = 0; i < max; i++) {
			GlyphI child = gl.getChild(i);
			if (child instanceof TierGlyph 
					&& ((TierGlyph) child).getTierType() == TierGlyph.TierType.GRAPH) {
				TierGlyph agg = (TierGlyph)child;
				if (agg.getChildCount() > 0) {
					for (GlyphI g : agg.getChildren()) {
						if (g instanceof GraphGlyph) {
							graphs.add((GraphGlyph)g);
						}
					}
				}
			}
			if (child.getChildCount() > 0) {
				collectGraphs(child, graphs);
			}
		}
	}

	@Override
	public final void addToPixelFloaterGlyph(GlyphI glyph) {
		PixelFloaterGlyph floater = pixel_floater_glyph;
		Rectangle2D.Double cbox = getSeqMap().getCoordBounds();
		floater.setCoords(cbox.x, 0, cbox.width, 0);
		floater.addChild(glyph);
	}

	@Override
	public PixelFloaterGlyph getPixelFloater() {
		return pixel_floater_glyph;
	}

	@Override
	public boolean autoChangeView() {
		return PreferenceUtils.getBooleanParam(PREF_AUTO_CHANGE_VIEW, default_auto_change_view);
	}

	public void groupSelectionChanged(GroupSelectionEvent evt) {
		AnnotatedSeqGroup current_group = null;
		AnnotatedSeqGroup new_group = evt.getSelectedGroup();
		if (aseq != null) {
			current_group = aseq.getSeqGroup();
		}

		if (IGBService.DEBUG_EVENTS) {
			System.out.println("SeqMapView received seqGroupSelected() call: " + ((new_group != null) ? new_group.getID() : "null"));
		}

		if ((new_group != current_group) && (current_group != null)) {
			clear();
		}
	}

	public void seqSelectionChanged(SeqSelectionEvent evt) {
		if (IGBService.DEBUG_EVENTS) {
			System.out.println("SeqMapView received SeqSelectionEvent, selected seq: " + evt.getSelectedSeq());
		}
		final BioSeq newseq = evt.getSelectedSeq();
		// Don't worry if newseq is null, setAnnotatedSeq can handle that
		// (It can also handle the case where newseq is same as old seq.)

		// trying out not calling setAnnotatedSeq() unless seq that is selected is actually different than previous seq being viewed
		// Maybe should change GenometryModel.setSelectedSeq() to only fire if seq changes...

		// reverted to calling setAnnotatedSeq regardless of whether newly selected seq is same as previously selected seq,
		//    because often need to trigger repacking / rendering anyway
		setAnnotatedSeq(newseq);
	}

	/**
	 * Get the span of the symmetry that is on the seq being viewed.
	 */
	@Override
	public final SeqSpan getViewSeqSpan(SeqSymmetry sym) {
		return sym.getSpan(viewseq);
	}

	/**
	 * Sets tool tip from given glyphs.
	 *
	 * @param glyphs
	 */
	public final void setToolTip(MouseEvent evt, List<GlyphI> glyphs) {
		if (!show_prop_tooltip) {
			return;
		}

		String toolTip = null;
		if (!glyphs.isEmpty()) {
			List<SeqSymmetry> sym = SeqMapView.glyphsToSyms(glyphs);
			if (!sym.isEmpty()) {
				if (propertyHandler != null) {
					String[][] properties = propertyHandler.getPropertiesRow(sym.get(0), this);
					toolTip = convertPropsToString(properties);
				}
			} else if (glyphs.get(0) instanceof TierLabelGlyph) {
				Map<String, Object> properties = TierLabelManager.getTierProperties(((TierLabelGlyph) glyphs.get(0)).getReferenceTier());
				toolTip = convertPropsToString(properties);
			} else {
				toolTip = null;
			}
		}
		
		setToolTip(evt, toolTip);
	}

	/**
	 * Sets tool tip from graph glyph.
	 *
	 * @param glyph
	 */
	public final void setToolTip(MouseEvent evt, int x, GraphGlyph glyph) {
		if (!show_prop_tooltip) {
			return;
		}

		List<GraphGlyph> glyphs = new ArrayList<GraphGlyph>();
		glyphs.add(glyph);
		List<SeqSymmetry> sym = SeqMapView.glyphsToSyms(glyphs);

		String toolTip = null;
		if (!sym.isEmpty()) {
			if (propertyHandler != null) {
				String[][] properties = propertyHandler.getGraphPropertiesRowColumn((GraphSym) sym.get(0), x, this);
				toolTip = convertPropsToString(properties);
			}
		} else {
			toolTip = null;
		}
		
		setToolTip(evt, toolTip);
	}

	private void setToolTip(MouseEvent evt, String toolTip) {
		
		// Nothing has changed return
		if(currentToolTip == null && toolTip == null) {
			return;
		}
		
		// Check if tooltip has changed from current tooltip.
		if ((currentToolTip == null && toolTip != null) 
				|| (currentToolTip != null && toolTip == null)
				|| !(currentToolTip.equals(toolTip))){
			currentToolTip = toolTip;
			seqmap.getNeoCanvas().setToolTipText(toolTip);
		}
	}
	
	public void showProperties(int x, GraphGlyph glyph) {
		List<GraphGlyph> glyphs = new ArrayList<GraphGlyph>();
		glyphs.add(glyph);
		List<SeqSymmetry> sym = SeqMapView.glyphsToSyms(glyphs);

		if (!sym.isEmpty()) {
			if (propertyHandler != null) {
				propertyHandler.showGraphProperties((GraphSym) sym.get(0), x, this);
			}
		}
	}

	private static String convertPropsToString(Map<String, Object> properties) {
		if (properties == null) {
			return null;
		}

		StringBuilder props = new StringBuilder();
		props.append("<html>");		
		for (Entry<String, Object> prop : properties.entrySet()) {
			props.append("<b>");
			props.append(prop.getKey());
			props.append(" : </b>");
			props.append(getSortString(prop.getValue()));
			props.append("<br>");
		}
		props.append("</html>");

		return props.toString();
	}

	/**
	 * Converts given properties into string.
	 */
	private static String convertPropsToString(String[][] properties) {
		StringBuilder props = new StringBuilder();
		props.append("<html>");
		if(properties.length >= 1){
			props.append("<div align='center'> <b> ").append(getSortString(properties[0][1])).append(" </b> </div> <hr>");
		}
		for (int i = 0; i < properties.length; i++) {
			props.append("<b>");
			props.append(properties[i][0]);
			props.append(" : </b>");
			props.append(getSortString(properties[i][1]));
			props.append("<br>");
		}
		props.append("</html>");

		return props.toString();
	}

	private static String getSortString(Object str){
		if(str == null){
			return "";
		}
		
		String string = GeneralUtils.URLDecode(str.toString());
		int strlen = string.length();
		if (strlen < 30) {
			return string;
		}
		
		return " ..." + string.substring(strlen - 25, strlen);
	}
	
	public boolean togglePropertiesTooltip() {
//		show_prop_tooltip = !show_prop_tooltip;
//		setToolTip(null);
		return show_prop_tooltip;
	}

	public final boolean shouldShowPropTooltip() {
		return show_prop_tooltip;
	}

	public final GlyphI getSequnceGlyph() {
		return seq_glyph;
	}

	public final AxisGlyph getAxisGlyph() {
		if (axis_tier == null) {
			return null;
		}

		AxisGlyph ag = null;
		for (GlyphI child : axis_tier.getChildren()) {
			if (child instanceof AxisGlyph) {
				ag = (AxisGlyph) child;
			}
		}
		return ag;
	}

	@Override
	public int getAverageSlots() {
		int slot = 1;
		int noOfTiers = 1;
		for (TierGlyph tier : seqmap.getTiers()) {
			if (!tier.isVisible()) {
				continue;
			}

			slot += tier.getActualSlots();
			noOfTiers += 1;
		}

		return slot / noOfTiers;
	}

	@Override
	public final void addToRefreshList(SeqMapRefreshed smr) {
		seqmap_refresh_list.add(smr);
	}

	@Override
	public SeqSymmetry getSeqSymmetry() {
		return seq_selected_sym;
	}

	public GenericAction getRefreshDataAction() {
		return refreshDataAction;
	}
	
	@Override
	public void setPropertyHandler(PropertyHandler propertyHandler) {
		this.propertyHandler = propertyHandler;
	}

	public MapMode getMapMode() {
		return mapMode;
	}

	public void setMapMode(MapMode mapMode) {
		this.mapMode = mapMode;

		seqmap.setRubberBandBehavior(mapMode.rubber_band);
		seqmap.enableCanvasDragging(mapMode.drag_scroll);
		seqmap.enableDragScrolling(!mapMode.drag_scroll);
		seqmap.setCursor(mapMode.defCursor);
	}

	public void saveSession() {
		PreferenceUtils.getSessionPrefsNode().put(SEQ_MODE, getMapMode().name());
	}

	public void loadSession() {
		String mapMode = PreferenceUtils.getSessionPrefsNode().get(SEQ_MODE, SeqMapView.MapMode.MapSelectMode.name());
		if (MapMode.MapScrollMode.name().equals(mapMode)) {
			scroll_mode_button.doClick();
		}
		if (MapMode.MapSelectMode.name().equals(mapMode)) {
			select_mode_button.doClick();
		}
//		if (MapMode.MapZoomMode.name().equals(mapMode)) {
//			zoom_mode_button.doClick();
//		}
	}

	@Override
	public void setRegion(int start, int end, BioSeq seq) {
		if (start >= 0 && end > 0 && end != Integer.MAX_VALUE) {
			final SeqSpan view_span = new SimpleSeqSpan(start, end, seq);
			zoomTo(view_span);
			final double middle = (start + end) / 2.0;
			setZoomSpotX(middle);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Map<String, Object>> getProperties() {
		List<Map<String, Object>> propList = new ArrayList<Map<String, Object>>();
		List<SeqSymmetry> selected_syms = getSelectedSyms();
		for (GlyphI glyph : getSeqMap().getSelected()) {

			if (glyph.getInfo() instanceof SeqSymmetry
					&& selected_syms.contains(glyph.getInfo())) {
				continue;
			}

			Map<String, Object> props = null;
			if (glyph.getInfo() instanceof Map) {
				props = (Map<String, Object>) glyph.getInfo();
			} else {
				props = new HashMap<String, Object>();
			}

			boolean direction = true;
			if (props.containsKey("direction")) {
				if (((String) props.get("direction")).equals("reverse")) {
					direction = false;
				}
			}

			Rectangle2D.Double boundary = glyph.getSelectedRegion();
			int start = (int) boundary.getX();
			int length = (int) boundary.getWidth();
			int end = start + length;
			if (!direction) {
				int temp = start;
				start = end;
				end = temp;
			}
			props.put("start", start);
			props.put("end", end);
			props.put("length", length);

			propList.add(props);
		}
		propList.addAll(getTierManager().getProperties());
		return propList;
	}

	@Override
	public Map<String, Object> determineProps(SeqSymmetry sym) {
		Map<String, Object> props = new HashMap<String, Object>();
		if(sym == null){
			return props;
		}
		Map<String, Object> tierprops = getTierManager().determineProps(sym);
		if(tierprops != null){
			props.putAll(tierprops);
		}
		SeqSpan span = getViewSeqSpan(sym);
		if (span != null) {
			String chromID = span.getBioSeq().getID();
			props.put("chromosome", chromID);
			props.put("start",
					NumberFormat.getIntegerInstance().format(span.getStart()));
			props.put("end",
					NumberFormat.getIntegerInstance().format(span.getEnd()));
			props.put("length",
					NumberFormat.getIntegerInstance().format(span.getLength()));
			props.put("strand",
					span.isForward() ? "+" : "-");
			props.remove("seq id"); // this is redundant if "chromosome" property is set
			if (props.containsKey("method")) {
				props.remove("method");
			}
			if (props.containsKey("type")) {
				props.remove("type");
			}
		}
		return props;
	}

	public MapRangeBox getMapRangeBox() {
		return map_range_box;
	}
	
	public JRPButton getPartial_residuesButton() {
		return partial_residuesB;
	}

	public SeqMapViewPopup getPopup() {
		return popup;
	}

	public AutoScroll getAutoScroll(){
		return autoScroll;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean consecutiveOK() {
		return true;
	}

	/**
	 * Update the widget in this panel.
	 * Putting this awkward idiom here to try to contain its spread.
	 * It is here for backward compatability.
	 */
	@Override
	public void updatePanel(boolean preserveViewX, boolean preserveViewY) {
		this.setAnnotatedSeq(this.getAnnotatedSeq(), true, preserveViewX, preserveViewY);
	}

	/**
	 * Update the widget in this panel.
	 */
	@Override
	public void updatePanel() {
		this.updatePanel(true, true);
	}
	
	@Override
	public void setTierStyles() {
		seqmap.setTierStyles();
	}

	@Override
	public void repackTheTiers(boolean full_repack, boolean stretch_vertically) {
		seqmap.repackTheTiers(full_repack, stretch_vertically);
	}
	
}
