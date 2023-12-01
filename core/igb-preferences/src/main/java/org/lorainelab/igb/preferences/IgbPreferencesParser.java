package org.lorainelab.igb.preferences;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.lorainelab.igb.preferences.model.IgbPreferences;
import org.lorainelab.igb.preferences.model.JsonWrapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
/**
 *
 * @author dcnorris
 */
@Component(name = IgbPreferencesParser.COMPONENT_NAME, immediate = true)
public class IgbPreferencesParser implements IgbPreferencesService {

    private static final Logger logger = LoggerFactory.getLogger(IgbPreferencesParser.class);
    public static final String COMPONENT_NAME = "IgbPreferencesParser";
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private JAXBContext jaxbContext;
    private Unmarshaller unmarshaller;
    private Optional<IgbPreferences> defaultPreferences;

    public IgbPreferencesParser() {
        try {
            jaxbContext = JAXBContext.newInstance(IgbPreferences.class.getPackage().getName(), IgbPreferences.class.getClassLoader());
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException ex) {
            logger.error("Could not initialize JAXBContext for igb preferences", ex);
        }
    }

    @Override
    public Optional<IgbPreferences> fromJson(URL url) {
        try {
            return fromJson(Resources.asCharSource(url, Charsets.UTF_8).read());
        } catch (IOException ex) {
            logger.error("Error Loading preferences file from url {}", url, ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<IgbPreferences> fromJson(String input) {
        return Optional.ofNullable(gson.fromJson(input, JsonWrapper.class).getPrefs());
    }

    @Override
    public Optional<IgbPreferences> fromJson(Reader reader) {
        return Optional.ofNullable(gson.fromJson(reader, JsonWrapper.class).getPrefs());
    }

    @Override
    public String toJson(JsonWrapper config) {
        return gson.toJson(config);
    }

    @Override
    public Optional<IgbPreferences> fromXml(Reader reader) {

        try {
            return Optional.ofNullable((IgbPreferences) unmarshaller.unmarshal(reader));
        } catch (JAXBException ex) {
            logger.error("Error Loading xml preferences file", ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<IgbPreferences> fromDefaultPreferences() {
        if (defaultPreferences == null) {
            Reader reader = new InputStreamReader(IgbPreferencesParser.class.getClassLoader().getResourceAsStream("igbDefaultPrefs.json"));
            defaultPreferences = fromJson(reader);
        }
        return defaultPreferences;
    }

}
