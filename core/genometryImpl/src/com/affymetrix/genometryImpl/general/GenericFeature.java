package com.affymetrix.genometryImpl.general;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryConstants;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.das2.Das2Type;
import com.affymetrix.genometryImpl.das2.FormatPriorities;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symloader.SymLoader;
import com.affymetrix.genometryImpl.symmetry.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LoadUtils.RefreshStatus;
import com.affymetrix.genometryImpl.util.SeqUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that's useful for visualizing a generic feature.
 * A feature is unique to a genome version/species/server.
 * Thus, there is a many-to-one map to GenericVersion.
 * (Even if the feature names and version names match, but the servers don't,
 * we can't guarantee that they would contain the same information.)
 *
 * @version $Id: GenericFeature.java 10112 2012-02-02 16:24:42Z imnick $
 */
public final class GenericFeature {
	public static final String howtoloadmsg = GenometryConstants.BUNDLE.getString("howtoloadmessage");
	public static final String show_how_to_load = GenometryConstants.BUNDLE.getString("show_how_to_load");
	public static final boolean default_show_how_to_load = true;
	
	public final String featureName;      // friendly name of the feature.
	public final Map<String, String> featureProps;
	public final GenericVersion gVersion;        // Points to the version that uses this feature.
	private boolean visible;							// indicates whether this feature should be visible or not (used in FeatureTreeView/GeneralLoadView interaction).
	private LoadStrategy loadStrategy;  // range chosen by the user, defaults to NO_LOAD.
	public URL friendlyURL = null;			// friendly URL that users may look at.
	private RefreshStatus lastRefresh;
	public final Object typeObj;    // Das2Type, ...?
	public final SymLoader symL;
	private final Set<String> methods = new HashSet<String>();
	private static final List<LoadStrategy> standardLoadChoices = new ArrayList<LoadStrategy>();

	static {
		standardLoadChoices.add(LoadStrategy.NO_LOAD);
		standardLoadChoices.add(LoadStrategy.VISIBLE);
		//standardLoadChoices.add(LoadStrategy.CHROMOSOME);
	}
	// Requests that have been made for this feature (to avoid overlaps)
	private final MutableSeqSymmetry requestSym = new SimpleMutableSeqSymmetry();
	// Request that are currently going on. (To avoid parsing more than once)
	private final MutableSeqSymmetry currentRequestSym = new SimpleMutableSeqSymmetry();

	/**
	 * @param featureName
	 * @param featureProps
	 * @param gVersion
	 * @param typeObj
	 */
	public GenericFeature(
			String featureName, Map<String, String> featureProps, GenericVersion gVersion, SymLoader gsr, Object typeObj, boolean autoload) {
		this.featureName = featureName;
		this.featureProps = featureProps;
		this.gVersion = gVersion;
		this.symL = gsr;
		this.typeObj = typeObj;
		if (typeObj instanceof Das2Type) {
			((Das2Type) typeObj).setFeature(this);
		}
		this.setFriendlyURL();
		boolean autoloadSet = this.setAutoload(autoload);
		if (!autoloadSet && featureProps != null && featureProps.get("load_strategy") != null) {
			try {
				setLoadStrategy(LoadStrategy.valueOf(featureProps.get("load_strategy")));
			}
			catch (IllegalArgumentException x) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, featureProps.get("load_strategy") + " for " + featureName + " is not a valid load strategy");
			}
		}
		this.lastRefresh = RefreshStatus.NOT_REFRESHED;
		//methods.add(featureName);
	}

	public boolean setAutoload(boolean auto) {
		if (shouldAutoLoad(featureProps) && auto) {
			setLoadStrategy(LoadStrategy.GENOME);
			this.setVisible();
			return true;
		} else if (!visible) {
			setLoadStrategy(LoadStrategy.NO_LOAD);
		}
		return false;
	}

	public void setVisible() {
		this.visible = true;
		if (this.loadStrategy != LoadStrategy.NO_LOAD) {
			return;
		}
		if (gVersion != null && gVersion.gServer != null) {
			if (gVersion.gServer.serverType.loadStrategyVisibleOnly()) {
				setLoadStrategy(LoadStrategy.VISIBLE);
			} else {
				// Local File or QuickLoad
				if (this.symL != null) {
					if (this.symL.getLoadChoices().contains(LoadStrategy.VISIBLE)) {
						setLoadStrategy(LoadStrategy.VISIBLE);
//					} else if (this.symL.getLoadChoices().contains(LoadStrategy.CHROMOSOME)) {
//						setLoadStrategy(LoadStrategy.CHROMOSOME);
					} else {
						setLoadStrategy(LoadStrategy.GENOME);
					}
				}
			}
		}
	}

	private void setInvisible() {
		this.visible = false;
		setLoadStrategy(LoadStrategy.NO_LOAD);
	}

	public boolean isVisible() {
		return this.visible;
	}

	public LoadStrategy getLoadStrategy() {
		return loadStrategy;
	}

	public void setLoadStrategy(LoadStrategy loadStrategy) {
		this.loadStrategy = loadStrategy;
	}

	public boolean setPreferredLoadStrategy(LoadStrategy loadStrategy) {
		if (getLoadChoices().contains(loadStrategy)) {
			setLoadStrategy(loadStrategy);
			return true;
		} else {
			setLoadStrategy(getLoadChoices().get(1));
			Logger.getLogger(GenericFeature.class.getName()).log(Level.WARNING,
					"Given {0} strategy is not permitted instead using {1} "
					+ "strategy.", new Object[]{loadStrategy, getLoadStrategy()});
		}
		return false;
	}

	/**
	 * @param featureProps feature properties
	 * @return true if feature should be loaded automatically
	 */
	private static boolean shouldAutoLoad(Map<String, String> featureProps) {
		return (featureProps != null
				&& featureProps.containsKey("load_hint")
				&& featureProps.get("load_hint").equals("Whole Sequence"));
	}

	private void setFriendlyURL() {
		if (this.featureProps == null || !this.featureProps.containsKey("url") || this.featureProps.get("url").length() == 0) {
			return;
		}
		try {
			this.friendlyURL = new URL(this.featureProps.get("url"));
		} catch (MalformedURLException ex) {
			Logger.getLogger(GenericFeature.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public String description() {
		if (this.featureProps != null) {
			String summary = featureProps.get("summary");
			String descrip = featureProps.get("description");

			if (summary != null && summary.length() > 0) {
				return summary;
			}
			if (descrip != null && descrip.length() > 0) {
				if (descrip.length() > 100) {
					return descrip.substring(0, 100) + "...";
				}
				return descrip;
			}
		}
		return featureName;
	}

	public Set<String> getMethods() {
		return Collections.<String>unmodifiableSet(methods);
	}

	/**
	 * Add all method generated by feature.
	 * @param method
	 */
	public void addMethod(String method) {
		methods.add(method);
		ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(method, method, getExtension(), featureProps);
		style.setFeature(this);
	}

	public void setLastRefreshStatus(RefreshStatus status) {
		lastRefresh = status;
	}

	public RefreshStatus getLastRefreshStatus() {
		return lastRefresh;
	}

	public void clear(BioSeq seq) {
		List<SeqSymmetry> removeList = new ArrayList<SeqSymmetry>();

		for (int i = 0; i < requestSym.getChildCount(); i++) {
			SeqSymmetry sym = requestSym.getChild(i);
			if (sym.getSpan(seq) != null) {
				removeList.add(sym);
			}
		}

		for (SeqSymmetry sym : removeList) {
			requestSym.removeChild(sym);
		}

		removeList.clear();
	}

	/**
	 * Remove all methods and set feature invisible.
	 */
	public void clear() {
		// Remove all childred from request
		requestSym.removeChildren();
		if (currentRequestSym.getChildCount() > 0) {
			Logger.getLogger(GenericFeature.class.getName()).log(Level.WARNING, "Genericfeature contains current request sym for server {0}", gVersion.gServer.serverType);
			currentRequestSym.removeChildren();
		}
		methods.clear();
		if(symL != null){
			symL.clear();
		}
		setInvisible();
		setLastRefreshStatus(RefreshStatus.NOT_REFRESHED);
	}

	public boolean isLoaded(SeqSpan span){
		MutableSeqSymmetry query_sym = new SimpleMutableSeqSymmetry();
		query_sym.addSpan(span);

		SeqSymmetry optimized_sym = SeqUtils.exclusive(query_sym, requestSym, span.getBioSeq());
		if (SeqUtils.hasSpan(optimized_sym)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Split the requested span into spans that still need to be loaded.
	 * Note we can't filter inside spans (in general) until after the data is returned.
	 */
	public SeqSymmetry optimizeRequest(SeqSpan span) {
		MutableSeqSymmetry query_sym = new SimpleMutableSeqSymmetry();
		query_sym.addSpan(span);

		SeqSymmetry optimized_sym = SeqUtils.exclusive(query_sym, requestSym, span.getBioSeq());
		optimized_sym = SeqUtils.exclusive(optimized_sym, currentRequestSym, span.getBioSeq());
		if (SeqUtils.hasSpan(optimized_sym)) {
			return optimized_sym;
		}
		return null;
	}

	/**
	 * This span is now considered loaded.
	 * @param span
	 */
	public void addLoadedSpanRequest(SeqSpan span) {
		MutableSeqSymmetry query_sym = new SimpleMutableSeqSymmetry();
		query_sym.addSpan(span);
		requestSym.addChild(query_sym);
		removeCurrentRequest(span);
	}

	public final void removeCurrentRequest(SeqSpan span) {
		for (int i = 0; i < currentRequestSym.getChildCount(); i++) {
			SeqSymmetry sym = currentRequestSym.getChild(i);
			if (span == sym.getSpan(span.getBioSeq())) {
				currentRequestSym.removeChild(sym);
			}
		}
	}

	public void addLoadingSpanRequest(SeqSpan span) {
		MutableSeqSymmetry query_sym = new SimpleMutableSeqSymmetry();
		query_sym.addSpan(span);
		currentRequestSym.addChild(query_sym);
	}

	public MutableSeqSymmetry getRequestSym() {
		return requestSym;
	}

	public List<LoadStrategy> getLoadChoices() {
		if (symL != null) {
			return symL.getLoadChoices();
		}

		return standardLoadChoices;
	}

	@Override
	public String toString() {
		// remove all but the last "/", since these will be represented in a friendly tree view.
		if (!this.featureName.contains("/")) {
			return this.featureName;
		}

		int lastSlash = this.featureName.lastIndexOf("/");
		return this.featureName.substring(lastSlash + 1, featureName.length());
	}

	public URI getURI() {
		if (typeObj instanceof Das2Type) {
			return ((Das2Type) typeObj).getURI();
		}
		if (typeObj instanceof String) {
			return URI.create(typeObj.toString());
		}

		if (symL != null) {
			return symL.uri;
		}
		return null;
	}

	public String getExtension() {
		if (typeObj instanceof Das2Type) {
			String ext = FormatPriorities.getFormat((Das2Type) typeObj);
			if (ext == null) {
				ext = "";
			}
			return ext;
		}

		if (symL != null) {
			return symL.extension;
		}

		return "";
	}
}
