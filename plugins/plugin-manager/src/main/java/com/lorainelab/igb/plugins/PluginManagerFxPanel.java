/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lorainelab.igb.plugins;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.eventbus.EventBus;
import com.google.common.io.CharStreams;
import com.lorainelab.igb.plugins.model.PluginListItemMetadata;
import com.lorainelab.igb.plugins.repos.events.PluginRepositoryEventPublisher;
import com.lorainelab.igb.plugins.repos.events.ShowBundleRepositoryPanelEvent;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.SwingUtilities;
import netscape.javascript.JSObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class PluginManagerFxPanel extends JFXPanel {

    private static final Logger logger = LoggerFactory.getLogger(PluginManagerFxPanel.class);

    @FXML
    private WebView description;
    @FXML
    private TextField search;
    @FXML
    private ComboBox filterOptions;
    @FXML
    private ListView<PluginListItemMetadata> listView;
    @FXML
    private Button updateAllBtn;
    @FXML
    private Button manageReposBtn;

    private VBox pane;
    private WebEngine webEngine;
    private String htmlTemplate;
    private List<PluginListItemMetadata> listData;
    private EventBus eventBus;

    @FXML
    private void initialize() {
        listView.setCellFactory((ListView<PluginListItemMetadata> l) -> new BuildCell());
        description.setContextMenuEnabled(false);
        webEngine = description.getEngine();
        JSObject jsobj = (JSObject) webEngine.executeScript("window");
        jsobj.setMember("Bridge", new Bridge());
        jsobj.setMember("logger", new JSLogger());
        webEngine.load(PluginManagerFxPanel.class.getClassLoader().getResource("pluginInfoTemplate.html").toExternalForm());
    }

    public class JSLogger {

        public void log(String message) {
            Platform.runLater(() -> {
                logger.info(message);
            });
        }
    }

    public class Bridge {

        public void installPlugin() {
            logger.info("installPlugin clicked");
        }

        public void handleUnInstallClick() {
            logger.info("handleUnInstall clicked");
        }

        public void handleUpdateClick() {
            logger.info("handleUpdate clicked");
        }

        public void openWebpage(String uriString) {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                if (StringUtils.isNotBlank(uriString)) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            URI uri = new URI(uriString);
                            desktop.browse(uri);
                        } catch (IOException | URISyntaxException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    });
                }
            }
        }

    }

    public class JSPluginWrapper {

        final PluginListItemMetadata plugin = listView.getSelectionModel().getSelectedItem();

        public String getPluginName() {
            return plugin.getPluginName();
        }

        public String getRepository() {
            return plugin.getRepository();
        }

        public String getVersion() {
            return plugin.getVersion();
        }

        public String getDescription() {
            return plugin.getDescription();
        }

        public Boolean isUpdatable() {
            return plugin.isUpdatable();
        }

        public Boolean isInstalled() {
            return plugin.isInstalled();
        }
    }

    public void updateListContent(List<PluginListItemMetadata> list) {
        Platform.runLater(() -> {
            listData = list;
            ObservableList<PluginListItemMetadata> data = FXCollections.observableArrayList(list);
            listView.setItems(data);
            listView.setOnMouseClicked((MouseEvent event) -> {
                final PluginListItemMetadata plugin = listView.getSelectionModel().getSelectedItem();
                selectedIndex = listView.getSelectionModel().getSelectedIndex();
                JSObject jsobj = (JSObject) webEngine.executeScript("window");
                jsobj.setMember("pluginInfo", new JSPluginWrapper());
                webEngine.executeScript("updatePluginInfo()");

            });
        });
    }

    private int selectedIndex;

    @Reference
    public void setEventBus(PluginRepositoryEventPublisher eventManager) {
        this.eventBus = eventManager.getPluginRepositoryEventBus();
        eventBus.register(this);
    }

    @FXML
    private void manageReposBtnAction() {
        SwingUtilities.invokeLater(() -> {
            eventBus.post(new ShowBundleRepositoryPanelEvent());
        });
    }

    private void updateWebContent() {
        Platform.runLater(() -> {

        });
    }

    public PluginManagerFxPanel() {
        Platform.runLater(() -> {
            init();
        });
    }

    private void init() {
        final URL resource = PluginManagerFxPanel.class.getClassLoader().getResource("PluginConfigurationPanel.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        pane = loader.getRoot();
        Platform.runLater(this::createScene);
    }

    private void createScene() {
        Scene scene = new Scene(pane, 885, 541);
        setScene(scene);
    }

    @FXML
    protected void updateAllBtnAction(ActionEvent event) {
        logger.info("updateAllBtnClicked");
    }

    private static String getClassPathResourceAsString(String resourcePath) {
        try {
            String htmlString = CharStreams.toString(new InputStreamReader(PluginManagerFxPanel.class.getClassLoader().getResourceAsStream(resourcePath)));
            return htmlString;
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private class BuildCell extends ListCell<PluginListItemMetadata> {

        @Override
        public void updateItem(PluginListItemMetadata plugin, boolean empty) {
            super.updateItem(plugin, empty);
            if (!empty) {
                Image image = new Image("plugin.png");
                if (plugin.isUpdatable()) {
                    image = new Image("fa-arrow-circle-up.png");
                }
                ImageView pluginImage = new ImageView();
                pluginImage.setFitWidth(16);
                pluginImage.setPreserveRatio(true);
                pluginImage.setSmooth(true);
                pluginImage.setCache(true);
                pluginImage.setImage(image);

                HBox row = new HBox(5);

                Text text = new Text(plugin.getPluginName());
                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                CheckBox cb = new CheckBox();
                cb.setSelected(plugin.isInstalled());
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    public void changed(ObservableValue ov,
                            Boolean old_val, Boolean new_val) {
                        //TODO
                    }
                });

                row.getChildren().addAll(pluginImage, text, spacer, cb);
                setGraphic(row);
            }
        }
    }

}
