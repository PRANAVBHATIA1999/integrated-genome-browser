package com.affymetrix.genometry.operator.service;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import com.affymetrix.genometry.operator.*;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 *
 * @author dcnorris
 */
@Component(name = OperatorServiceFactory.COMPONENT_NAME, immediate = true)
public class OperatorServiceFactory {

    public static final String COMPONENT_NAME = "OperatorServiceFactory";
    private BundleContext bundleContext;
    private final List<ServiceReference<Operator>> serviceReferences;

    public OperatorServiceFactory() {
        serviceReferences = new ArrayList<>();
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        bundleContext.registerService(Operator.class, new ComplementSequenceOperator(), null);
        bundleContext.registerService(Operator.class, new CopyGraphOperator(), null);
        //note: CopyMismatchOperator appears to have never worked, and is disabled now until it is repaired
//        bundleContext.registerService(Operator.class, new CopyMismatchOperator(), null);
        bundleContext.registerService(Operator.class, new CopySequenceOperator(), null);
        bundleContext.registerService(Operator.class, new CopyXOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new CopyXOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new CopyXOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new DepthOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new DepthOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new DepthOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new StartDepthOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new SoftClipDepthOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new StartDepthOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new StartDepthOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new SummaryOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new SummaryOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new SummaryOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new NotOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new NotOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new NotOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new DiffOperator(), null);
        bundleContext.registerService(Operator.class, new ExclusiveAOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new ExclusiveAOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new ExclusiveAOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new ExclusiveBOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new ExclusiveBOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new ExclusiveBOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new IntersectionOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new IntersectionOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new IntersectionOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new InverseTransformer(), null);
        bundleContext.registerService(Operator.class, new InverseLogTransform(), null);
        bundleContext.registerService(Operator.class, new InverseLogTransform(Math.E, true), null);
        bundleContext.registerService(Operator.class, new InverseLogTransform(2.0, true), null);
        bundleContext.registerService(Operator.class, new InverseLogTransform(10.0, true), null);
        bundleContext.registerService(Operator.class, new LogTransform(), null);
        bundleContext.registerService(Operator.class, new LogTransform(Math.E, true), null);
        bundleContext.registerService(Operator.class, new LogTransform(2.0, true), null);
        bundleContext.registerService(Operator.class, new LogTransform(10.0, true), null);
        bundleContext.registerService(Operator.class, new PowerTransformer(), null);
        bundleContext.registerService(Operator.class, new PowerTransformer(0.5), null);
        bundleContext.registerService(Operator.class, new MaxOperator(), null);
        bundleContext.registerService(Operator.class, new MeanOperator(), null);
        bundleContext.registerService(Operator.class, new MedianOperator(), null);
        bundleContext.registerService(Operator.class, new MinOperator(), null);
        bundleContext.registerService(Operator.class, new ProductOperator(), null);
        bundleContext.registerService(Operator.class, new RatioOperator(), null);
        bundleContext.registerService(Operator.class, new SumOperator(), null);
        bundleContext.registerService(Operator.class, new UnionOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new UnionOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new UnionOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new XorOperator(FileTypeCategory.Alignment), null);
        bundleContext.registerService(Operator.class, new XorOperator(FileTypeCategory.Annotation), null);
        bundleContext.registerService(Operator.class, new XorOperator(FileTypeCategory.ProbeSet), null);
        bundleContext.registerService(Operator.class, new GraphMultiplexer(), null);
        bundleContext.registerService(Operator.class, new AddMathTransform(), null);
        bundleContext.registerService(Operator.class, new DivideMathTransform(), null);
        bundleContext.registerService(Operator.class, new MultiplyMathTransform(), null);
        bundleContext.registerService(Operator.class, new SubtractMathTransform(), null);
    }

    @Deactivate
    public void deactivate() {
        serviceReferences.forEach(sr -> bundleContext.ungetService(sr));
    }
}
