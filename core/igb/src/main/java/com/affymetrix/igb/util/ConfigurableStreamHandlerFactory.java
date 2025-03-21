package com.affymetrix.igb.util;

/*
 * http://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
 */
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

public class ConfigurableStreamHandlerFactory implements URLStreamHandlerFactory {

    private final Map<String, URLStreamHandler> protocolHandlers;

    public ConfigurableStreamHandlerFactory(String protocol, URLStreamHandler urlHandler) {
        protocolHandlers = new HashMap<>();
        addHandler(protocol, urlHandler);
    }

    public void addHandler(String protocol, URLStreamHandler urlHandler) {
        protocolHandlers.put(protocol, urlHandler);
    }

    public URLStreamHandler createURLStreamHandler(String protocol) {
        return protocolHandlers.get(protocol);
    }
}
