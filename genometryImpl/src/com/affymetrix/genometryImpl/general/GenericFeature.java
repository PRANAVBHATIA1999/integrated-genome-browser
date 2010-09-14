package com.affymetrix.genometryImpl.general;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.das2.Das2Type;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import com.affymetrix.genometryImpl.util.SeqUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
 * @version $Id$
 */
public final class GenericFeature {

	public final String featureName;      // friendly name of the feature.
	public final Map<String, String> featureProps;
	public final GenericVersion gVersion;        // Points to the version that uses this feature.
	private boolean visible;							// indicates whether this feature should be visible or not (used in FeatureTreeView/GeneralLoadView interaction).
	public LoadStrategy loadStrategy;  // range chosen by the user, defaults to NO_LOAD.
	public URL friendlyURL = null;			// friendly URL that users may look at.
	public final Object typeObj;    // Das2Type, ...?
	public final SymLoader symL;
	private static final List<LoadStrategy> standardLoadChoices = new ArrayList<LoadStrategy>();

	static {
		standardLoadChoices.add(LoadStrategy.NO_LOAD);
		standardLoadChoices.add(LoadStrategy.VISIBLE);
		standardLoadChoices.add(LoadStrategy.CHROMOSOME);
	}
	
	// Requests that have been made for this feature (to avoid overlaps)
	private final MutableSeqSymmetry requestSym = new SimpleMutableSeqSymmetry();
	
	/**
	 * @param featureName
	 * @param featureProps
	 * @param gVersion
	 * @param typeObj
	 */
	public GenericFeature(
			String featureName, Map<String, String> featureProps, GenericVersion gVersion, SymLoader gsr, Object typeObj, boolean autoload){
		this.featureName = featureName;
		this.featureProps = featureProps;
		this.gVersion = gVersion;
		this.symL = gsr;
		this.typeObj = typeObj;
		if (typeObj instanceof Das2Type) {
			((Das2Type)typeObj).setFeature(this);
		}
		this.setFriendlyURL();
		this.setAutoload(autoload);
	}

	public void setAutoload(boolean auto){
		if (shouldAutoLoad(featureProps) && auto) {
			this.loadStrategy = LoadStrategy.GENOME;
			this.setVisible();
		} else {
			this.loadStrategy = LoadStrategy.NO_LOAD;
			this.visible = false;
		}
	}
	
	public void setVisible() {
		this.visible = true;
		if (this.loadStrategy != LoadStrategy.NO_LOAD) {
			return;
		}
		if (gVersion != null && gVersion.gServer != null) {
			if (gVersion.gServer.serverType == ServerType.DAS || gVersion.gServer.serverType == ServerType.DAS2) {
				this.loadStrategy = LoadStrategy.VISIBLE;
			} else {
				// Local File or QuickLoad
				/*if (this.symL != null) {
					if (this.symL.getLoadChoices().contains(LoadStrategy.VISIBLE)) {
						this.loadStrategy = LoadStrategy.VISIBLE;
					} else if (this.symL.getLoadChoices().contains(LoadStrategy.CHROMOSOME)) {
						this.loadStrategy = LoadStrategy.CHROMOSOME;
					} else {
						this.loadStrategy = LoadStrategy.GENOME;
					}
				}*/
			}
		}
	}

	public void setInvisible(){
		this.visible = false;
		this.loadStrategy = LoadStrategy.NO_LOAD;
	}
	
	public boolean isVisible() {
		return this.visible;
	}


	/**
	 * @param featureProps feature properties
	 * @return true if feature should be loaded automatically
	 */
	private static boolean shouldAutoLoad(Map<String,String> featureProps) {
		return (featureProps != null &&
				featureProps.containsKey("load_hint") &&
				featureProps.get("load_hint").equals("Whole Sequence"));
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

	/**
	 * Split the requested span into spans that still need to be loaded.
	 * Note we can't filter inside spans (in general) until after the data is returned.
	 * @param span
	 * @return
	 */
	public SeqSymmetry optimizeRequest(SeqSpan span) {
		MutableSeqSymmetry query_sym = new SimpleMutableSeqSymmetry();
		query_sym.addSpan(span);

		SeqSymmetry optimized_sym = SeqUtils.exclusive(query_sym, requestSym, span.getBioSeq());
		if (SeqUtils.hasSpan(optimized_sym)) {
			addLoadedSymRequest(optimized_sym);	// later this will be done *after* the sym is loaded
			return optimized_sym;
		}
		return null;
	}

	public void addLoadedSymRequest(SeqSymmetry optimized_sym) {
		requestSym.addChild(optimized_sym);
	}

	public List<SeqSymmetry> filterOutExistingSymmetries(List<SeqSymmetry> syms, BioSeq seq) {
		List<SeqSymmetry> newSyms = new ArrayList<SeqSymmetry>(syms.size());	// roughly this size
		MutableSeqSymmetry dummySym = new SimpleMutableSeqSymmetry();
		for (SeqSymmetry sym : syms) {
			if (SeqUtils.intersection(sym, requestSym, dummySym, seq)) {
				// There is an intersection with previous requests.  Ignore this symmetry
				continue;
			}
			newSyms.add(sym);
		}
		return newSyms;
	}

	public MutableSeqSymmetry getRequestSym(){
		return requestSym;
	}

	public List<LoadStrategy> getLoadChoices(){
		if(symL != null)
			return symL.getLoadChoices();

		return standardLoadChoices;
	}
	
	@Override
	public String toString() {
		// remove all but the last "/", since these will be represented in a friendly tree view.
		if (!this.featureName.contains("/")) {
			return this.featureName;
		}

		int lastSlash = this.featureName.lastIndexOf("/");
		return this.featureName.substring(lastSlash + 1,featureName.length());
	}

	public static void setPreferredLoadStrategy(GenericFeature feature, LoadStrategy s){
		if (feature.getLoadChoices().contains(s)){
			feature.loadStrategy = s;
		}else{
			feature.loadStrategy = feature.getLoadChoices().get(1);
			Logger.getLogger(GenericFeature.class.getName()).log(Level.WARNING,
					"Given {0} strategy is not permitted instead using {1} "
					+ "strategy.", new Object[]{s, feature.loadStrategy});
		}
	}

	public URI getURI(){
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


}
