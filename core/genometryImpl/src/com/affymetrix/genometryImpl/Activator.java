package com.affymetrix.genometryImpl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.common.ExtensionPointListener;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.parsers.FileTypeHandler;
import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import com.affymetrix.genometryImpl.thread.CThreadHolder;
import com.affymetrix.genometryImpl.thread.WaitHelperI;
import com.affymetrix.genometryImpl.util.ServerTypeI;

/**
 * OSGi Activator for genometry bundle
 */
public class Activator implements BundleActivator {
	protected BundleContext bundleContext;

	@Override
	public void start(BundleContext _bundleContext) throws Exception {
		bundleContext = _bundleContext;
    	if (CommonUtils.getInstance().isExit(bundleContext)) {
    		return;
    	}
		bundleContext.registerService(WaitHelperI.class, CThreadHolder.getInstance(), null);
		initFileTypeHandlers();
		initGenericActions();
		initServerTypes();
		initOperators();
	}

	@Override
	public void stop(BundleContext _bundleContext) throws Exception {}

	private void initFileTypeHandlers() {
		// add all FileTypeHandler implementations to FileTypeHolder
		ExtensionPointHandler<FileTypeHandler> extensionPoint = ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, FileTypeHandler.class);
		extensionPoint.addListener(new ExtensionPointListener<FileTypeHandler>() {
			@Override
			public void removeService(FileTypeHandler fileTypeHandler) {
				FileTypeHolder.getInstance().removeFileTypeHandler(fileTypeHandler);
			}

			@Override
			public void addService(FileTypeHandler fileTypeHandler) {
				FileTypeHolder.getInstance().addFileTypeHandler(fileTypeHandler);
			}
		});
	}

	private void initGenericActions() {
		// add all GenericAction implementations to GenericActionHolder
		ExtensionPointHandler<GenericAction> extensionPoint = ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, GenericAction.class);
		extensionPoint.addListener(new ExtensionPointListener<GenericAction>() {
			@Override
			public void addService(GenericAction genericAction) {
				GenericActionHolder.getInstance().addGenericAction(genericAction);
			}

			@Override
			public void removeService(GenericAction genericAction) {
				GenericActionHolder.getInstance().removeGenericAction(genericAction);
			}
		});
	}

	private void initServerTypes() {
		ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, ServerTypeI.class);
		bundleContext.registerService(ServerTypeI.class, ServerTypeI.LocalFiles, null);
		bundleContext.registerService(ServerTypeI.class, ServerTypeI.QuickLoad, null);
		bundleContext.registerService(ServerTypeI.class, ServerTypeI.DAS, null);
		bundleContext.registerService(ServerTypeI.class, ServerTypeI.DAS2, null);
	}

	private void initOperators() {
		ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, Operator.class);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.ComplementSequenceOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.CopyAlignmentOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.CopyAnnotationOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.CopyGraphOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.CopyMismatchOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.CopySequenceOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.DepthOperator(FileTypeCategory.Alignment), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.DepthOperator(FileTypeCategory.Annotation), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.DiffOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.ExclusiveAOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.ExclusiveBOperator(), null);
		//bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.FilterOperator(FileTypeCategory.Annotation, new SymmetryFilterProps()), null);
		//bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.IdentityTransform(), null); //Same as copy graph operator
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.IntersectionOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.InverseTransformer(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.InverseLogTransform(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.InverseLogTransform(Math.E), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.InverseLogTransform(2.0), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.InverseLogTransform(10.0), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.LogTransform(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.LogTransform(Math.E), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.LogTransform(2.0), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.LogTransform(10.0), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.PowerTransformer(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.PowerTransformer(0.5), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.MaxOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.MeanOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.MedianOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.MinOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.NotOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.ProductOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.RatioOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.SumOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.UnionOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.XorOperator(), null);
		bundleContext.registerService(Operator.class, new com.affymetrix.genometryImpl.operator.GraphMultiplexer(), null);
	}
}
