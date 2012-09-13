package com.affymetrix.igb.view.factories;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.util.LoadUtils;
import com.affymetrix.genometryImpl.util.SeqUtils;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.bioviews.AbstractCoordPacker;
import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.event.NeoRangeEvent;
import com.affymetrix.genoviz.glyph.FillRectGlyph;
import com.affymetrix.genoviz.glyph.SolidGlyph;
import com.affymetrix.genoviz.widget.tieredmap.PaddedPackerI;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.action.SetSummaryThresholdAction;
import com.affymetrix.igb.shared.CollapsePacker;
import com.affymetrix.igb.shared.FasterExpandPacker;
import com.affymetrix.igb.shared.MapTierGlyphFactoryI;
import com.affymetrix.igb.shared.MapTierTypeHolder;
import com.affymetrix.igb.shared.SeqMapViewExtendedI;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.TrackConstants;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import com.affymetrix.igb.view.load.GeneralLoadView;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;


/**
 *  This is the glyph that displays the contents of a Tier/Track.
 */
public abstract class AbstractTierGlyph extends SolidGlyph implements TierGlyph{
	private static final Map<String,Class<?>> NONE_PREFERENCES = new HashMap<String,Class<?>>();
	SwingWorker previousWorker, worker;
	protected ITrackStyleExtended style;
	protected TierGlyph.Direction direction = TierGlyph.Direction.NONE;
	private static final int handle_width = 10;  // width of handle in pixels
	private final Rectangle pixel_hitbox = new Rectangle();  // caching rect for hit detection
	/** glyphs to be drawn in the "middleground" --
	 *    in front of the solid background, but behind the child glyphs
	 *    For example, to indicate how much of the xcoord range has been covered by feature retrieval attempts
	 */
	private final List<GlyphI> middle_glyphs = new ArrayList<GlyphI>();
	/*
	 * other_fill_color is derived from fill_color whenever setFillColor() is called.
	 * if there are any "middle" glyphs, then background is drawn with other_fill_color and
	 *    middle glyphs are drawn with fill_color
	 * if no "middle" glyphs, then background is drawn with fill_color
	 */
	protected Color other_fill_color = null;
	
	protected String label = null;
	private double spacer = 2;
	protected FasterExpandPacker expand_packer = new FasterExpandPacker();
	protected CollapsePacker collapse_packer = new CollapsePacker();
	protected TierType tierType;
		
	public AbstractTierGlyph(ITrackStyleExtended style){
		setHitable(false);
		setSpacer(spacer);
		setStyle(style);
		setTierType(TierType.NONE);
	}
	
	private void setSpacer(double spacer) {
		this.spacer = spacer;
		((PaddedPackerI) collapse_packer).setParentSpacer(spacer);
		((PaddedPackerI) expand_packer).setParentSpacer(spacer);
	}
		
	protected void updateParent(TierGlyph vmg){
		//Do Nothing
	}

	
	protected RootSeqSymmetry loadRegion(SeqSpan span){
		loadData(span);
		return (RootSeqSymmetry) this.getInfo();
	}
	
	protected List<SeqSymmetry> loadData(SeqSpan span) {
		try {
			GenericFeature feature = style.getFeature();
			if(feature == null){
				return Collections.<SeqSymmetry>emptyList();
			}
			
			SeqSymmetry optimized_sym = feature.optimizeRequest(span);
			if (optimized_sym == null) {
				return Collections.<SeqSymmetry>emptyList();
			}
			
			Application.getSingleton().addNotLockedUpMsg("Loading "+getAnnotStyle().getTrackName());
			
			return GeneralLoadUtils.loadFeaturesForSym(feature, optimized_sym);
		} catch (Exception ex) {
			Logger.getLogger(AbstractTierGlyph.class.getName()).log(Level.SEVERE, null, ex);
		}

		return Collections.<SeqSymmetry>emptyList();
	}
	
	public void initUnloaded() {
		Glyph glyph;

		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();//smv.getAnnotatedSeq();
		SeqSymmetry sym = new SimpleMutableSeqSymmetry();
	
		if(style.getFeature() != null){
			sym = style.getFeature().getRequestSym();
		}
		
		// Add middle glyphs.
		SeqSymmetry inverse = SeqUtils.inverse(sym, seq);
		int child_count = inverse.getChildCount();
		//If any request was made.
		if (child_count > 0) {
			for (int i = 0; i < child_count; i++) {
				SeqSymmetry child = inverse.getChild(i);
				for (int j = 0; j < child.getSpanCount(); j++) {
					SeqSpan ospan = child.getSpan(j);
					if (ospan.getLength() > 1) {
						glyph = new FillRectGlyph();
						glyph.setCoords(ospan.getMin(), 0, ospan.getLength() - 1, 0);
						addMiddleGlyph(glyph);
					}
				}
			}
		} else {
			glyph = new FillRectGlyph();
			glyph.setCoords(seq.getMin(), 0, seq.getLength() - 1, 0);
			addMiddleGlyph(glyph);
		}
		
		glyph = new FillRectGlyph();
		glyph.setCoords(0, 0, 0, getChildHeight());
		addChild(glyph);
	}
	
	@Override
	public final void setTierType(TierType method){
		this.tierType = method;
		if(tierType == TierType.SEQUENCE){
			style.setSeparable(false);
			style.setSeparate(false);
		}
	}
	
	public final TierType getTierType(){
		return tierType;
	}
	
	@Override
	public FileTypeCategory getFileTypeCategory(){
		if(getInfo() != null && getInfo() instanceof RootSeqSymmetry){
			return ((RootSeqSymmetry)getInfo()).getCategory();
		}
		return null;
	}
	
	/**
	 * Sets direction.
	 */
	public void setDirection(TierGlyph.Direction d) {
		direction = d;
	}
	
	public final void setStyle(ITrackStyleExtended style) {
		this.style = style;

		// most tier glyphs ignore their foreground color, but AffyTieredLabelMap copies
		// the fg color to the TierLabel glyph, which does pay attention to that color.
		setForegroundColor(style.getForeground());
		setFillColor(style.getBackground());

		//If any visibilty bug occurs, fix here. -HV 22/03/2012
		setVisibility(style.getShow());
		if (style.getCollapsed()) {
			setPacker(collapse_packer);
		} else {
			setPacker(expand_packer);
		}
		setMaxExpandDepth(style.getMaxDepth());
	}
		
	public void drawMiddle(ViewI view) {
		view.transformToPixels(getCoordBox(), getPixelBox());

		getPixelBox().width = Math.max(getPixelBox().width, getMinPixelsWidth());
		getPixelBox().height = Math.max(getPixelBox().height, getMinPixelsHeight());

		Graphics g = view.getGraphics();
		Rectangle vbox = view.getPixelBox();
		setPixelBox(getPixelBox().intersection(vbox));

		if (middle_glyphs.isEmpty()) { // no middle glyphs, so use fill color to fill entire tier
			if (style.getBackground() != null) {
				g.setColor(style.getBackground());
				//Hack : Add one to height to resolve black line bug.
				g.fillRect(getPixelBox().x, getPixelBox().y, getPixelBox().width, getPixelBox().height+1);
			}
		} else {
			if (style.getBackground() != null) {
				g.setColor(style.getBackground());
				//Hack : Add one to height to resolve black line bug.
				g.fillRect(getPixelBox().x, getPixelBox().y, 2 * getPixelBox().width, getPixelBox().height+1);
			}

			// cycle through "middleground" glyphs,
			//   make sure their coord box y and height are set to same as TierGlyph,
			//   then call mglyph.draw(view)
			// TODO: This will draw middle glyphs on the Whole Genome, which appears to cause problems due to coordinates vs. pixels
			// See bug 3032785
			if(other_fill_color != null){
				for (GlyphI mglyph : middle_glyphs) {
					Rectangle2D.Double mbox = mglyph.getCoordBox();
					mbox.setRect(mbox.x, getCoordBox().y, mbox.width, getCoordBox().height);
					mglyph.setColor(other_fill_color);
					mglyph.drawTraversal(view);
				}
			}
		}
	}
	
	@Override
	public int getActualSlots() {
		return 1;
	}

	@Override
	public int getSlotsNeeded(ViewI ourView) {
		return 1;
	}

	@Override
	public void setPreferredHeight(double maxHeight, ViewI view) {
		//Do Nothing
	}
	
	protected boolean shouldDrawToolBar(){
		return style.drawCollapseControl();
	}
	
	@Override
	public void draw(ViewI view){
		if (shouldDrawToolBar()) {
			drawExpandCollapse(view);
		}
	}
	
	@Override
	public void drawTraversal(ViewI view)  {
		if (isVisible() && (withinView(view) || RectangleIntersectHack(view))) {
			drawMiddle(view);
			if (getChildCount() > 0) {
				drawChildren(view);
			}
			draw(view);
		}
	}
	
	private void drawExpandCollapse(ViewI view) {
		Rectangle hpix = getToolbarPixel(view);
		if (hpix != null) {
			Graphics g = view.getGraphics();
			g.setColor(Color.WHITE);
			g.fill3DRect(hpix.x, hpix.y, hpix.width, hpix.height, true);
//			g.drawOval(hpix.x, hpix.y, hpix.width, hpix.height);
			g.setColor(Color.BLACK);
			g.drawRect(hpix.x, hpix.y, hpix.width, hpix.height);
			g.drawLine(hpix.x + hpix.width/5, hpix.y + hpix.height/2, hpix.x + hpix.width - hpix.width/5, hpix.y + hpix.height/2);
			if(style.getCollapsed()){
				g.drawLine(hpix.x + hpix.width/2, hpix.y + hpix.height/5, hpix.x + hpix.width/2, hpix.y + hpix.height - hpix.height/5);
			}
		}
	}
	
	@Override
	public void setInfo(Object info) {
		super.setInfo(info);
		if(info != null && !(info instanceof RootSeqSymmetry)){
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "!!!!! {0} is not instance of RootSeqSymmetry !!!!!", info);
		}
	}
	
	@Override
	public void addChild(GlyphI glyph, int position) {
		throw new RuntimeException("AbstractViewModeGlyph.addChild(glyph, position) not allowed, "
				+ "use AbstractViewModeGlyph.addChild(glyph) instead");
	}
	
	/**
	 * Remove all children of the glyph,
	 * including those added with addMiddleGlyph(GlyphI).
	 */
	@Override
	public void removeAllChildren() {
		super.removeAllChildren();
		// also remove all middleground glyphs
		// this is currently the only place where middleground glyphs are treated as if they were children
		//   maybe should rename this method clear() or something like that...
		// only reference to middle glyphs should be in this.middle_glyphs, so should be able to GC them by
		//     clearing middle_glyphs.  These glyphs never have setScene() called on them,
		//     so it is not necessary to call setScene(null) on them.
		middle_glyphs.clear();
	}
	
	protected boolean isAutoLoadMode() {
		if (this.getAnnotStyle() == null) {
			return false;
		}

		if (this.getAnnotStyle().getFeature() == null) {
			return false;
		}

		if (this.getAnnotStyle().getFeature().getLoadStrategy() != LoadUtils.LoadStrategy.AUTOLOAD) {
			return false;
		}
		return true;
	}
		
	public boolean isDetail(ViewI view) {
		return SetSummaryThresholdAction.getAction().isDetail(getAnnotStyle());
	}
	
//	@Override
	public void rangeChanged(NeoRangeEvent evt){
		if(evt.getSource() instanceof SeqMapViewExtendedI){
			rangeChanged(((SeqMapViewExtendedI)evt.getSource()));
		}
	}
	
	protected void rangeChanged(SeqMapViewExtendedI smv){
		if(isAutoLoadMode() && isDetail(smv.getSeqMap().getView())){
			try {
				MapTierGlyphFactoryI factory = MapTierTypeHolder.getInstance().getDefaultFactoryFor(getFileTypeCategory());
				if(factory != null){
					loadAndDisplayRegion(smv, factory);
				}
			} catch (Exception ex) {
				Logger.getLogger(AbstractTierGlyph.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	};
	
	/** Returns the color used to draw the tier background, or null
	if there is no background. */
	public final Color getFillColor() {
		return style.getBackground();
	}
	
	/** Sets the color used to fill the tier background, or null if no color
	 *  @param col  A color, or null if no background color is desired.
	 */
	public final void setFillColor(Color col) {
		if (style.getBackground() != col) {
			style.setBackground(col);
		}

		// Now set the "middleground" color based on the fill color
		if (col == null) {
			other_fill_color = Color.DARK_GRAY;
		} else {
			int intensity = col.getRed() + col.getGreen() + col.getBlue();
			if (intensity == 0) {
				other_fill_color = Color.darkGray;
			} else if (intensity > (255 + 127)) {
				other_fill_color = col.darker();
			} else {
				other_fill_color = col.brighter();
			}
		}
	}
	
	public final ITrackStyleExtended getAnnotStyle() {
		return style;
	}
		
	public final TierGlyph.Direction getDirection() {
		return direction;
	}
	
	public final void copyChildren(TierGlyph temp) {
		if(temp == null)
			return;
		
		List<GlyphI> childrens = new ArrayList<GlyphI>();
		childrens.addAll(temp.getChildren());

		for (int i = 0; i < childrens.size(); i++) {
			addChild(childrens.get(i));
		}
		
		childrens.clear();
//		childrens.addAll(temp.getMiddleGlyphs());
		
		for (int i = 0; i < childrens.size(); i++) {
			addMiddleGlyph(childrens.get(i));
		}
		
		//TODO: Set list of all getInfo
//		if(!(getInfo() instanceof List)){
//			List<Object> info = new ArrayList<Object>();
//			info.add(getInfo());
//			setInfo(info);
//		}else{
//			((List)(getInfo())).add(temp.getInfo());
//		}
		
	}
		
	/**
	 *  Adds "middleground" glyphs, which are drawn in front of the background but
	 *    behind all "real" child glyphs.
	 *  These are generally not considered children of
	 *    the glyph.  The TierGlyph will render these glyphs, but they can't be selected since they
	 *    are not considered children in pickTraversal() method.
	 *  The only way to remove these is via removeAllChildren() method,
	 *    there is currently no external access to them.
	 */
	public final void addMiddleGlyph(GlyphI gl) {
		middle_glyphs.add(gl);
	}
	
	public final List<SeqSymmetry> getSelected(){
	
		int childCount = getChildCount();
		List<SeqSymmetry> selectedSyms = new ArrayList<SeqSymmetry>(childCount);
		for(int i=0;i<childCount;i++){
			if(getChild(i).isSelected()){
				if(getChild(i).getInfo() instanceof SeqSymmetry){
					selectedSyms.add((SeqSymmetry)(getChild(i).getInfo()));
				}
			}
		}
		return selectedSyms;
	}
	
	
	public final boolean toolBarHit(Rectangle2D.Double coord_hitbox, ViewI view){
		if (shouldDrawToolBar() && isVisible() && coord_hitbox.intersects(getCoordBox())) {
			// overlapping handle ?  (need to do this one in pixel space?)
			Rectangle hpix = new Rectangle();
			view.transformToPixels(coord_hitbox, hpix);
			if (getToolbarPixel(view).intersects(hpix)) {
				return true;
			}
		}
		return false;
	}
	
	protected Rectangle getToolbarPixel(ViewI view){
		pixel_hitbox.setBounds(getPixelBox().x + 4, getPixelBox().y + 4, handle_width, handle_width);
		return pixel_hitbox;
	}
		
	public final double getChildHeight(){
		double child_height = MapTierGlyphFactoryI.DEFAULT_CHILD_HEIGHT;
		child_height = useLabel(getAnnotStyle()) ? child_height * 2 : child_height;
		child_height = child_height + getSpacing() * 2;
		return child_height;
	}
	
	protected final double getSpacing() {
		if(getPacker() instanceof AbstractCoordPacker){
			return ((AbstractCoordPacker)getPacker()).getSpacing();
		}
		return 2;
	}
	
	public final int getStyleDepth(){
		switch(getDirection()){
			case REVERSE:
				return getAnnotStyle().getReverseMaxDepth();
			
			case FORWARD:
				return getAnnotStyle().getForwardMaxDepth();
						
			default:
				return getAnnotStyle().getMaxDepth();
		}
	}
	
	protected final TierGlyph createGlyphs(RootSeqSymmetry rootSym, MapTierGlyphFactoryI factory, SeqMapViewExtendedI smv){
		return null;
		//return factory.getViewModeGlyph(rootSym, style, direction, smv);
	};
	
	protected final void loadAndDisplayRegion(final SeqMapViewExtendedI smv, final MapTierGlyphFactoryI factory) throws Exception{
		if (previousWorker != null && !previousWorker.isCancelled() && !previousWorker.isDone()) {
			previousWorker.cancel(true);
			previousWorker = null;
		}

		worker = new SwingWorker() {

			@Override
			protected Void doInBackground() throws Exception {
				RootSeqSymmetry rootSym = loadRegion(smv.getVisibleSpan());
				if (rootSym.getChildCount() > 0) {
					final TierGlyph tg = createGlyphs(rootSym, factory, smv);
					ThreadUtils.runOnEventQueue(new Runnable() {

						public void run() {
							updateParent(tg);
							GeneralLoadUtils.setLastRefreshStatus(style.getFeature(), tg.getChildCount() > 0);
							GeneralLoadView.getLoadView().refreshDataManagementView();
							//TODO: Find a way to avoid this
							//if (lastUsedGlyph == saveDetailGlyph) {
							smv.repackTheTiers(true, true);
							smv.getSeqMap().updateWidget();
							Application.getSingleton().removeNotLockedUpMsg("Loading " + getAnnotStyle().getTrackName());
							//}
						}
					});
				}else{
					Application.getSingleton().removeNotLockedUpMsg("Loading " + getAnnotStyle().getTrackName());
				}
				return null;
			}

		};
		worker.execute();
		previousWorker = worker;
		worker = null;
	};
	
	public boolean isManuallyResizable() {
		if (this.getPacker() instanceof CollapsePacker) {
			return false;
		}
		return true;
	}
	
	public final void setMinimumPixelBounds(Graphics g){
		java.awt.FontMetrics fm = g.getFontMetrics();
		int h = fm.getHeight();
		h += 2 * 2; // border height
		h += 4; // padding top
		int w = fm.stringWidth("A Moderate Label");
		w += 2; // border left
		w += 4; // padding left
		java.awt.Dimension minTierSizeInPixels = new java.awt.Dimension(w, h);
		setMinimumPixelBounds(minTierSizeInPixels);
	}
	
	
	@Override
	public void pack(ViewI view) {
		super.pack(view);
		if (this.getCoordBox().height < MapTierGlyphFactoryI.DEFAULT_CHILD_HEIGHT) {
			// Only do this for resizable tiers for now.
			// It would screw up the axis tier, for one.
			if (isManuallyResizable()) {
				Rectangle2D.Double oldBox = getCoordBox();
				setCoords(oldBox.x, oldBox.y, oldBox.width, MapTierGlyphFactoryI.DEFAULT_CHILD_HEIGHT);
			}

		}
	}
	
	public void resizeHeight(double diffy, double height) {
		Rectangle2D.Double cbox = getCoordBox();
		setCoords(cbox.x, cbox.y, cbox.width, height);
		this.moveRelative(0, diffy);
	}

	public static boolean useLabel(ITrackStyleExtended style){
		return style.getLabelField() != null && 
		!style.getLabelField().equals(TrackConstants.NO_LABEL) && 
		(style.getLabelField().trim().length() > 0);
	}
	
	protected static void scaleChildHeights(double theScale, List<GlyphI> theSiblings, ViewI theView) {
		if(theSiblings == null || theSiblings.isEmpty()){
			return;
		}
		
		int numberOfSiblings = theSiblings.size();
		GlyphI child;
		Rectangle2D.Double coordbox;
		for (int i = 0; i < numberOfSiblings; i++) {
			child =  theSiblings.get(i);
			coordbox = child.getCoordBox();
			child.setCoords(coordbox.x, 0, coordbox.width, coordbox.height * theScale);
			if (0 < child.getChildCount()) {
				// The above test is needed as of 2011-03-01
				// because child.getChildren() returns null instead of an empty list.
				scaleChildHeights(theScale, child.getChildren(), theView);
			}
			child.pack(theView);
		}
	}

	protected double getMaxChildHeight() {
		double max = 0;
		int children = this.getChildCount();
		for (int i = 0; i < children; i++) {
			max = Math.max(max, this.getChild(i).getCoordBox().height);
		}
		return max;
	}

	public Map<String,Class<?>> getPreferences(){
		return NONE_PREFERENCES;
	};

	public void setPreferences(Map<String,Object> preferences){

	};

	/**
	 * Changes the maximum depth of the expanded packer.
	 * This does not call pack() afterwards.
	 */
	protected void setMaxExpandDepth(int max) {
		expand_packer.setMaxSlots(max);
	}
}
