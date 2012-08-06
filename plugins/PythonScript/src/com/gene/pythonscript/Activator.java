package com.gene.pythonscript;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.affymetrix.genoviz.swing.recordplayback.ScriptProcessorHolder;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		ScriptProcessorHolder.getInstance().addScriptProcessor(new PythonScriptProcessor());
    }

	@Override
	public void stop(BundleContext bundleContext) throws Exception {}
}
