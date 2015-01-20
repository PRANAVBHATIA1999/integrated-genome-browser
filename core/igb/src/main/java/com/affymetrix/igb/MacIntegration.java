package com.affymetrix.igb;

import com.affymetrix.igb.action.AboutIGBAction;
import com.affymetrix.igb.action.ExitAction;
import com.affymetrix.igb.prefs.PreferencesPanel;
import java.awt.Image;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * Class to handle Integration of IGB so it will behave more mac-like on OS X.
 * This is achieved using reflection so that the apple-specific classes will not
 * interfere with IGB on other platforms.
 *
 * @author sgblanch
 * @version $Id: MacIntegration.java 11085 2012-04-13 16:09:40Z lfrohman $
 */
public final class MacIntegration {

    /**
     * private instance of MacIntegration for singleton pattern
     */
    private static MacIntegration instance = null;
    private Class<?> applicationClass;
    private Object application;
    private static final Logger ourLogger
            = Logger.getLogger(MacIntegration.class.getPackage().getName());

    /**
     * Private constructor to enforce singleton pattern
     */
    private MacIntegration() {
        applicationClass = null;
        application = null;

        try {
            applicationClass = Class.forName("com.apple.eawt.Application");
            Method getApplication = applicationClass.getDeclaredMethod("getApplication");
            application = getApplication.invoke(null);

            Method setEnabledPreferencesMenu = applicationClass.getDeclaredMethod("setEnabledPreferencesMenu", Boolean.TYPE);
            setEnabledPreferencesMenu.invoke(application, true);

            Method addApplicationListener = applicationClass.getDeclaredMethod(
                    "addApplicationListener",
                    Class.forName("com.apple.eawt.ApplicationListener"));

            Class<?> applicationAdapterClass = Class.forName("com.apple.eawt.ApplicationAdapter");
            Object proxy = ApplicationListenerProxy.newInstance(applicationAdapterClass.newInstance());
            addApplicationListener.invoke(application, proxy);

        } catch (Exception ex) {
            ourLogger.log(Level.SEVERE, "?", ex);
        }
    }

    /**
     * Initialize the singleton copy of MacIntegration. This should only be
     * called once by the application, but it protects itself against multiple
     * invocations. Do not call this function on anything platform other than
     * Macintosh: Undefined things will happen.
     *
     * @return a singleton instance of MacIntegration
     */
    public static synchronized MacIntegration getInstance() {
        if (instance == null) {
            instance = new MacIntegration();
        }
        return instance;
    }

    /**
     * Wrapper around Apple's com.apple.eawt.setDockIconImage.
     *
     * @param image the Image to use as the Dock icon.
     */
    public void setDockIconImage(Image image) {
        try {
            Method setDockIconImage = applicationClass.getDeclaredMethod("setDockIconImage", Image.class);
            setDockIconImage.invoke(application, image);
        } catch (Exception ex) {
            ourLogger.log(Level.SEVERE, "?", ex);
        }
    }
}

final class ApplicationListenerProxy implements InvocationHandler {

    private final Object o;
    private static final Logger ourLogger
            = Logger.getLogger(ApplicationListenerProxy.class.getPackage().getName());

    public static Object newInstance(Object o) {
        return Proxy.newProxyInstance(
                o.getClass().getClassLoader(),
                o.getClass().getInterfaces(),
                new ApplicationListenerProxy(o));
    }

    private ApplicationListenerProxy(Object o) {
        this.o = o;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        try {
            switch (method.getName()) {
                case "handleAbout":
                    {
                        AboutIGBAction a = AboutIGBAction.getAction();
                        a.actionPerformed(null);
                        Method setHandled = Class.forName("com.apple.eawt.ApplicationEvent").getDeclaredMethod("setHandled", Boolean.TYPE);
                        setHandled.invoke(args[0], true);
                        break;
                    }
                case "handleQuit":
                    {
                        ExitAction a = ExitAction.getAction();
                        a.actionPerformed(null);
                        break;
                    }
                case "handlePreferences":
                    PreferencesPanel pv = PreferencesPanel.getSingleton();
                    JFrame f = pv.getFrame();
                    f.setVisible(true);
                    break;
                default:
                    result = method.invoke(o, args);
                    break;
            }
        } catch (Exception ex) {
            ourLogger.log(Level.SEVERE, "?", ex);
        }
        return result;
    }
}
