package com.affymetrix.igb.bamindexer;

import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.affymetrix.genoviz.swing.AMenuItem;
import com.affymetrix.igb.service.api.IgbService;
import com.affymetrix.igb.service.api.XServiceRegistrar;
import com.affymetrix.igb.swing.JRPMenu;
import com.affymetrix.igb.swing.JRPMenuItem;

public class Activator extends XServiceRegistrar<IgbService> implements BundleActivator {

    private static final Logger logger = Logger.getLogger(Activator.class.getPackage().getName());

    public Activator() {
        super(IgbService.class);
    }

    @Override
    protected ServiceRegistration<?>[] getServices(BundleContext bundleContext, IgbService igbService) throws Exception {

        // assuming last file menu item is Exit, leave it there
        JRPMenu file_menu = igbService.getMenu("tools");
        final int index = file_menu.getItemCount();
        //file_menu.insertSeparator(index);

        return new ServiceRegistration[]{
            bundleContext.registerService(AMenuItem.class, new AMenuItem(new JRPMenuItem("Make Index for BAM File(s)", BAMIndexer.getAction()), "tools", index), null),};
    }
}
