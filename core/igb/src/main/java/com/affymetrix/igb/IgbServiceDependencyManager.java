package com.affymetrix.igb;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.genometry.data.DataProviderFactory;
import com.affymetrix.igb.window.service.IWindowService;
import org.lorainelab.igb.context.menu.service.AnnotationContextMenuRegistryI;
import org.lorainelab.igb.frame.api.FrameManagerService;
import org.lorainelab.igb.services.window.menus.IgbMenuItemProvider;
import org.lorainelab.igb.synonymlookup.services.ChromosomeSynonymLookup;
import org.lorainelab.igb.synonymlookup.services.GenomeVersionSynonymLookup;
import org.lorainelab.igb.synonymlookup.services.SpeciesSynonymsLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(name = IgbServiceDependencyManager.COMPONENT_NAME, immediate = true, provide = IgbServiceDependencyManager.class)
public class IgbServiceDependencyManager {

    private static final Logger logger = LoggerFactory.getLogger(IgbServiceDependencyManager.class);
    public static final String COMPONENT_NAME = "IgbServiceDependencyManager";
    private IWindowService windowService;

    public IgbServiceDependencyManager() {
    }

    @Activate
    public void activate() {
        logger.info("Igb Module Service Dependencies are now available.");
    }

    @Reference(target = "(&(component.name=ShowConsoleAction))")
    public void trackConsoleService(IgbMenuItemProvider consoleService) {
        logger.info("Console Service now available.");
    }

    //This isn't strictly necessary, but waiting for this will prevent brief opportunity in the ui for users to click on genome icons before providers are initialized
    @Reference(target = "(&(component.name=QuickloadFactory))")
    public void trackQuickloadDataProviderFactory(DataProviderFactory quickloadFactory) {
        logger.info("QuickloadFactory now available.");
    }

    //This isn't strictly necessary, but waiting for this will prevent brief opportunity in the ui for users to click on genome icons before providers are initialized
//    @Reference(target = "(&(component.name=DasDataProviderFactory))")
//    public void trackDasDataProviderFactory(DataProviderFactory quickloadFactory) {
//        logger.info("Das Factory now available.");
//    }
    //This isn't strictly necessary, but waiting for this will prevent brief opportunity in the ui for users to click on genome icons before providers are initialized
//    @Reference(target = "(&(component.name=Das2DataProviderFactory))")
//    public void trackDas2DataProviderFactory(DataProviderFactory quickloadFactory) {
//        logger.info("Das2 Factory now available.");
//    }
    @Reference
    public void trackWindowService(IWindowService windowService) {
        logger.info("Window Service now available.");
        this.windowService = windowService;
    }

    @Reference
    public void trackSpeciesSynonymLookupService(SpeciesSynonymsLookup speciesSynLookup) {
        logger.info("SpeciesSynonymLookupService now available.");
    }

    @Reference
    public void trackChromosomeSynonymLookupService(ChromosomeSynonymLookup chromosomeSynLookup) {
        logger.info("ChromosomeSynonymLookupService now available.");
    }

    @Reference
    public void trackGenomeVersionSynonymLookupService(GenomeVersionSynonymLookup genomeVersionSynLookup) {
        logger.info("GenomeVersionSynonymLookupService now available.");
    }

    @Reference
    public void trackFrameManagerService(FrameManagerService frameManagerService) {
        logger.info("FrameManagerService now available.");
    }

    @Reference
    public void trackAnnotationContextMenuRegistry(AnnotationContextMenuRegistryI annotationContextMenuRegistry) {
        logger.info("AnnotationContextMenuRegistryI now available.");
    }

    public IWindowService getWindowService() {
        return windowService;
    }

}
