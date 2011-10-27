package com.affymetrix.igb.search;

import org.osgi.framework.BundleActivator;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.shared.ISearchMode;
import com.affymetrix.igb.window.service.WindowActivator;

public class Activator extends WindowActivator implements BundleActivator {
	@Override
	protected IGBTabPanel getPage(IGBService igbService) {
		final SearchView searchView = new SearchView(igbService);
		ExtensionPointHandler.addExtensionPoint(bundleContext,
			new ExtensionPointHandler<ISearchMode>() {
				@Override
				public void addService(ISearchMode searchMode) {
					searchView.addSearchMode(searchMode);
					searchView.initSearchCB();
				}
				@Override
				public void removeService(ISearchMode searchMode) {
					searchView.removeSearchMode(searchMode);
					searchView.initSearchCB();
				}
			}
		);
		return searchView;
	}
}
