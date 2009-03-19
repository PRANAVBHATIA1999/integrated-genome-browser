package com.affymetrix.igb.general;

import com.affymetrix.genometry.AnnotatedBioSeq;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import com.affymetrix.igb.view.load.GeneralLoadUtils.LoadStatus;
import com.affymetrix.igb.view.load.GeneralLoadUtils.LoadStrategy;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that's useful for visualizing a generic feature.
 * A feature is unique to a genome version/species/server.
 * (Even if the feature names and version names match, but the servers don't,
 * we can't guarantee that they would contain the same information.)
 */
public final class GenericFeature {

	public final String featureName;      // friendly name of the feature.
	public final GenericVersion gVersion;        // Points to the version that uses this feature.
	public boolean visible;							// indicates whether this feature should be visible or not (used in FeatureTreeView/GeneralLoadView interaction).
	public LoadStrategy loadStrategy;  // range chosen by the user, defaults to NO_LOAD.
	public Map<AnnotatedBioSeq, LoadStatus> LoadStatusMap; // each chromosome maps to a feature loading status.

	/**
	 * @param featureName
	 * @param gVersion
	 */
	public GenericFeature(String featureName, GenericVersion gVersion) {
		this.featureName = featureName;
		this.gVersion = gVersion;
		this.visible = false;
		this.loadStrategy = LoadStrategy.NO_LOAD;
		this.LoadStatusMap = new HashMap<AnnotatedBioSeq, LoadStatus>();
	}

	@Override
	public String toString() {
		if (this.gVersion.gServer.serverType == GenericServer.ServerType.QuickLoad) {
			return stripFilenameExtensions(this.featureName);
		}
		return this.featureName;
	}

	/**
	 * Used to give a friendly name for QuickLoad features.
	 * @param name
	 * @return
	 */
	private static String stripFilenameExtensions(final String name) {
		// Remove ending .gz or .zip extension.
		if (name.endsWith(".gz")) {
			return stripFilenameExtensions(name.substring(0, name.length() -3));
		}
		if (name.endsWith(".zip")) {
			return stripFilenameExtensions(name.substring(0, name.length() -4));
		}

		if (name.indexOf('.') > 0) {
			return name.substring(0, name.lastIndexOf('.'));
		}
		return name;
	}
}
