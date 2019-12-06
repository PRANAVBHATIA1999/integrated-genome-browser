/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.plugin.manager;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import org.lorainelab.igb.plugin.manager.model.PluginListItemMetadata;
import org.lorainelab.igb.services.IgbService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.application.Platform;
import org.apache.felix.bundlerepository.InterruptedResolutionException;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = BundleActionManager.class)
public class BundleActionManager {

    private static final Logger logger = LoggerFactory.getLogger(BundleActionManager.class);
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("plugins");
    private BundleInfoManager bundleInfoManager;
    private IgbService igbService;
    private RepositoryAdmin repoAdmin;
    private BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Reference
    public void setBundleInfoManager(BundleInfoManager bundleInfoManager) {
        this.bundleInfoManager = bundleInfoManager;
    }

    @Reference
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Reference(optional = false)
    public void setRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
        repoAdmin = repositoryAdmin;
    }

    public void updateBundle(PluginListItemMetadata plugin, final Function<Boolean, ? extends Class<Void>> callback) {
        CompletableFuture.supplyAsync(() -> {
            Bundle bundle = plugin.getBundle();
            Optional<Bundle> installedBundled = Arrays.asList(bundleContext.getBundles()).stream()
                    .filter(installedBundle -> installedBundle.getSymbolicName().equals(bundle.getSymbolicName())).findFirst();
            if (installedBundled.isPresent()) {
                try {
                    installedBundled.get().uninstall();
                } catch (BundleException ex) {
                    logger.error(ex.getMessage(), ex);
                }
                installBundle(plugin, installSucceeded -> {
                    if (installSucceeded) {
                        Platform.runLater(() -> {
                            plugin.setBundle(bundle);
                            plugin.setVersion(bundle.getVersion().toString());
                            plugin.setIsUpdatable(Boolean.FALSE);
                        });
                        callback.apply(installSucceeded);
                    }
                    return Void.TYPE;
                });
            }
            return true;
        });
    }
    /*~Kiran:IGBF-1108:Added this method as we cannot believe in InetAddress.isReachable method.*/
    private static boolean isInternetReachable(URL url)
    {   
        try {
            //Do this test only if it is http or https url skip for local url's
            if(url.toString().toLowerCase().startsWith("http")){
                //open a connection to that source
                /*
                IGBF-2164
                URL Input to this function might have the link to jar which will 
                increase download count for that app (Refer to Issue for more 
                information)
                */
                // Get the Domain Name from the URL
                String urlBuilder = url.getAuthority();
                // Build URL to cehck the connection
                URL connectURL = new URL("http://" + urlBuilder);
                HttpURLConnection urlConnect = (HttpURLConnection)connectURL.openConnection();
                //try connecting to the source, If there is no connection, this line will fail and throw exception
                Object objData = urlConnect.getContent();
            }
        } catch (UnknownHostException ex) {
            logger.error(ex.getMessage());
            return false;
        }
        catch (IOException ex) {
            logger.error(ex.getMessage());
            return false;
        }catch (Exception ex){
            logger.error(ex.getMessage());
            return false;
        }
        return true;
    }

    public void installBundle(final PluginListItemMetadata plugin, final Function<Boolean, ? extends Class<Void>> callback) {
        Bundle bundle = plugin.getBundle();
        Resource resource = ((ResourceWrapper) bundle).getResource();
        CompletableFuture.supplyAsync(new Supplier<Boolean>() {
            boolean tryToRecover = true;
            @Override
            public Boolean get() {
                try {
                    /*~Kiran:IGBF-1108:Added to make sure an active internet connection exists*/
                    if (isInternetReachable(new URL(resource.getURI()))){
                        installBundle(resource, bundle);
                    }else{
                        return false;
                    }
                } catch (IllegalStateException ex) {
                    if (tryToRecover && ex.getMessage().equals(KNOWN_FELIX_EXCEPTION)) {
                        tryToRecover = false; //only try this once
                        installBundle(plugin, callback);
                    }
                } catch (Throwable ex) {
                    logger.error(ex.getMessage(), ex);
                    return false;
                }
                return true;
            }
        }).thenApply(callback);

    }
    private final String KNOWN_FELIX_EXCEPTION = "Framework state has changed, must resolve again."; //See

    private synchronized void installBundle(Resource resource, Bundle bundle) throws InterruptedResolutionException {
        Resolver resolver = repoAdmin.resolver();
        resolver.add(resource);
        if (resolver.resolve()) {
            resolver.deploy(Resolver.START);
            logger.info("Installed app: " + bundle.getSymbolicName() + "," + bundle.getVersion());
            igbService.setStatus(MessageFormat.format(BUNDLE.getString("bundleInstalled"), bundle.getSymbolicName(), bundle.getVersion()));
        } else {
            String msg = MessageFormat.format(BUNDLE.getString("bundleInstallError"), bundle.getSymbolicName(), bundle.getVersion());
            StringBuilder sb = new StringBuilder(msg);
            sb.append(" -> ");
            boolean started = false;
            for (Reason reason : resolver.getUnsatisfiedRequirements()) {
                if (started) {
                    sb.append(", ");
                }
                started = true;
                sb.append(reason.getRequirement().getComment());
            }
            logger.error(sb.toString());
        }
    }

    public void uninstallBundle(final PluginListItemMetadata plugin, final Function<Boolean, ? extends Class<Void>> callback) {
        CompletableFuture.supplyAsync(() -> {
            Bundle bundle = plugin.getBundle();
            try {
                for (Bundle b : Arrays.asList(bundleContext.getBundles())) {
                    if (b.getSymbolicName().equals(bundle.getSymbolicName())) {
                        if (b.getState() == Bundle.ACTIVE) {
                            b.uninstall();
                            logger.info("Uninstalled app: " + b.getSymbolicName() + "," + b.getVersion());
                        }
                    }
                }

            } catch (BundleException bex) {
                String msg = BUNDLE.getString("bundleUninstallError");
                logger.error(msg);
            }
            return true;
        }).thenApply(callback);
    }
}
