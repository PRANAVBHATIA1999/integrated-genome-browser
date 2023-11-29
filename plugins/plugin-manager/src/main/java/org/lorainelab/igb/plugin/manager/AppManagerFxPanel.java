/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.plugin.manager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import static org.lorainelab.igb.plugin.manager.Constants.MATERIAL_DESIGN_COLORS;
import org.lorainelab.igb.plugin.manager.model.PluginListItemMetadata;
import org.lorainelab.igb.plugin.manager.repos.events.PluginRepositoryEventPublisher;
import org.lorainelab.igb.plugin.manager.repos.events.ShowBundleRepositoryPanelEvent;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.SwingUtilities;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = AppManagerFxPanel.class)
public class AppManagerFxPanel extends JFXPanel {

    private static final Logger logger = LoggerFactory.getLogger(AppManagerFxPanel.class);
    private final String PLUGIN_INFO_TEMPLATE = "pluginInfoTemplate.html";

    private AppManagerPanel appManagerPanel;
    private WebView description;
    private TextField search;
    private ComboBox filterOptions;
    private ListView<PluginListItemMetadata> listView;
    private Button updateAllBtn;
    private Button manageReposBtn;
    private SplitPane splitPane;
    private VBox pane;
    private WebEngine webEngine;

    private EventBus eventBus;
    private Map<String, Color> repoToColor;
    private int colorIndex = 0;
    private BundleContext bundleContext;

    private final ObservableList<PluginListItemMetadata> listData;
    private final FilteredList<PluginListItemMetadata> filteredList;

    private Predicate<PluginListItemMetadata> currentStaticPredicate;
    private Predicate<PluginListItemMetadata> currentSearchPredicate;

    private BundleInfoManager bundleInfoManager;
    private BundleActionManager bundleActionManager;
    private RepositoryInfoManager repositoryInfoManager;

    private JSBridge jsBridge;
    private JSLogger jsLogger;

    private void refreshUpdateAllBtn() {
        Platform.runLater(() -> {
            updateAllBtn.setDisable(!filteredList.stream().anyMatch(plugin -> plugin.getIsUpdatable().getValue()));
        });
    }

    private void updateAllBundles() {
        listData.stream().filter(plugin -> plugin.getIsUpdatable().getValue()).forEach(plugin -> {
            updatePlugin(plugin);
        });
    }

    @Subscribe
    public void udpateDataEventNotification(UpdateDataEvent event) {
        Platform.runLater(() -> {
            listData.clear();
            updateWebContent();
            List<Bundle> toAdd = Lists.newArrayList();
            for (Bundle bundle : bundleInfoManager.getRepositoryManagedBundles()) {
                Optional<Bundle> match = toAdd.stream()
                        .filter(b -> b.getSymbolicName().equals(bundle.getSymbolicName()))
                        .findFirst();
                if (match.isPresent()) {
                    if (bundle.getVersion().compareTo(match.get().getVersion()) >= 1) {
                        toAdd.remove(match.get());
                        toAdd.add(bundle);
                    }
                } else {
                    toAdd.add(bundle);
                }
            }

            toAdd.stream().forEach(bundle -> {
                final boolean isInstalled = bundleInfoManager.isVersionOfBundleInstalled(bundle);
                final boolean isUpdateable = bundleInfoManager.isUpdateable(bundle);
                listData.add(new PluginListItemMetadata(bundle, bundleInfoManager.getBundleVersion(bundle), repositoryInfoManager.getBundlesRepositoryName(bundle), isInstalled, isUpdateable));
            });
            refreshListViewContent();
        });
    }

    public enum FilterOption {

        ALL_APPS("All Apps"), INSTALLED("Installed"), UNINSTALLED("Uninstalled");

        private final String label;

        private FilterOption(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    public AppManagerFxPanel() {
        listData = FXCollections.observableArrayList((PluginListItemMetadata p) -> new Observable[]{p});
        currentStaticPredicate = (PluginListItemMetadata s) -> true;
        currentSearchPredicate = (PluginListItemMetadata s) -> true;
        filteredList = new FilteredList<>(listData, s -> true);
        Platform.runLater(() -> {
            init();
        });
    }

    @Activate
    private void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        udpateDataEventNotification(new UpdateDataEvent());
    }

    @Reference
    public void setBundleInfoManager(BundleInfoManager bundleInfoManager) {
        this.bundleInfoManager = bundleInfoManager;
    }

    @Reference
    public void setBundleActionManager(BundleActionManager bundleActionManager) {
        this.bundleActionManager = bundleActionManager;
    }

    @Reference
    public void setRepositoryInfoManager(RepositoryInfoManager repositoryInfoManager) {
        this.repositoryInfoManager = repositoryInfoManager;
    }

    private void setupView() {
        Font.loadFont(bundleContext.getBundle().getEntry("Roboto-Regular.ttf").toExternalForm(), 35);
        filterOptions.getItems().addAll(FilterOption.ALL_APPS, FilterOption.INSTALLED, FilterOption.UNINSTALLED);
        filterOptions.valueProperty().addListener(new ChangeListener<FilterOption>() {
            @Override
            public void changed(ObservableValue ov, FilterOption t, FilterOption newValue) {
                changeStaticFilter(newValue);
            }
        });
        listView.setItems(filteredList.sorted());
        refreshUpdateAllBtn();
        listView.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends PluginListItemMetadata> observable,
                        PluginListItemMetadata previousSelection,
                        PluginListItemMetadata selectedPlugin) -> {
                    if (selectedPlugin != null) {
                        updateWebContent();
                    }
                });
        listView.setCellFactory((ListView<PluginListItemMetadata> l) -> new BuildCell());
        description.setContextMenuEnabled(false);
        webEngine = description.getEngine();
        /*
         * ~kiran: IGBF-1244- Creating instance variables to avoid garbage collection of the weak references
         * causing subsequent accesses to the JavaScript objects to have no effect.
         */
        jsBridge = new JSBridge();
        jsLogger = new JSLogger();
        description.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    JSObject jsobj = (JSObject) description.getEngine().executeScript("window");
                    jsobj.setMember("Bridge", jsBridge);
                    jsobj.setMember("logger", jsLogger);
                }
            }
        });

        String htmlUrl;
        if (bundleContext != null) {
            htmlUrl = bundleContext.getBundle().getEntry(PLUGIN_INFO_TEMPLATE).toExternalForm();
        } else {
            htmlUrl = AppManagerFxPanel.class.getClassLoader().getResource(PLUGIN_INFO_TEMPLATE).toExternalForm();
        }
        webEngine.load(htmlUrl);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            changeSearchFilter(newValue);
        });
    }

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
            JSObject jsobj = (JSObject) webEngine.executeScript("window");
            jsobj.setMember("pluginInfo", new JSPluginWrapper(listView, bundleInfoManager));
            try {
                webEngine.executeScript("updatePluginInfo()");
            } catch (JSException ex) {
                logger.debug(ex.getMessage());
            }
        });
    }

    private void init() {
        repoToColor = new HashMap<>();
        appManagerPanel = new AppManagerPanel();
        Scene scene = new Scene(appManagerPanel);
        setScene(scene);
        setupView();
    }

    @FXML
    protected void updateAllBtnAction(ActionEvent event) {
        logger.info("updateAllBtnClicked");
        updateAllBundles();
        refreshListViewContent();

    }

    private class BuildCell extends ListCell<PluginListItemMetadata> {

        @Override
        public void updateItem(PluginListItemMetadata plugin, boolean empty) {
            updateItemAction(plugin, empty);
        }

        public void updateItemAction(PluginListItemMetadata plugin, boolean empty) {
            Platform.runLater(() -> {
                super.updateItem(plugin, empty);
                if (!empty) {
                    Image updateImage;
                    ImageView updateImageView = new ImageView();
                    updateImageView.setFitWidth(16);
                    updateImageView.setPreserveRatio(true);
                    updateImageView.setSmooth(true);
                    updateImageView.setCache(true);

                    if (plugin.getIsUpdatable().getValue()) {
                        if (bundleContext != null) {
                            updateImage = new Image(bundleContext.getBundle().getEntry("fa-arrow-circle-up.png").toExternalForm());
                        } else {
                            updateImage = new Image("fa-arrow-circle-up.png");
                        }
                        updateImageView.setImage(updateImage);
                        Tooltip updateTooltip = new Tooltip("Update available");
                        Tooltip.install(updateImageView, updateTooltip);
                    } else if (plugin.getIsInstalled().getValue()) {
                        if (bundleContext != null) {
                            updateImage = new Image(bundleContext.getBundle().getEntry("installed.png").toExternalForm());
                        } else {
                            updateImage = new Image("installed.png");
                        }
                        updateImageView.setImage(updateImage);
                        Tooltip updateTooltip = new Tooltip("Installed");
                        Tooltip.install(updateImageView, updateTooltip);
                    } else {
                        if (bundleContext != null) {
                            updateImage = new Image(bundleContext.getBundle().getEntry("uninstalled.png").toExternalForm());
                        } else {
                            updateImage = new Image("uninstalled.png");
                        }
                        updateImageView.setImage(updateImage);
                        Tooltip updateTooltip = new Tooltip("Uninstalled");
                        Tooltip.install(updateImageView, updateTooltip);
                    }

                    HBox textPane = new HBox();
                    textPane.setAlignment(Pos.CENTER);
                    HBox.setHgrow(textPane, Priority.ALWAYS);
                    textPane.setPrefHeight(35);
                    textPane.setPrefWidth(35);
                    Color paneColor;
                    if (repoToColor.containsKey(plugin.getRepository().getValue())) {
                        paneColor = repoToColor.get(plugin.getRepository().getValue());
                    } else {
                        if ((colorIndex + 1) > MATERIAL_DESIGN_COLORS.size()) {
                            colorIndex = 0;
                        }
                        paneColor = MATERIAL_DESIGN_COLORS.get(colorIndex);
                        colorIndex++;
                        repoToColor.put(plugin.getRepository().getValue(), paneColor);
                    }

                    Tooltip avatarTooltip = new Tooltip("Located in the " + plugin.getRepository() + " repository");
                    Tooltip.install(textPane, avatarTooltip);
                    textPane.setBackground(new Background(new BackgroundFill(paneColor, CornerRadii.EMPTY, Insets.EMPTY)));
                    Text avatar = new Text();
                    avatar.setTextAlignment(TextAlignment.CENTER);
                    avatar.setText(plugin.getRepository().getValue().substring(0, 1).toUpperCase());
                    avatar.setFill(Color.rgb(255, 255, 255));
                    textPane.getChildren().add(avatar);
                    avatar.getStyleClass().add("avatar");
                    avatar.setTextOrigin(VPos.CENTER);
                    textPane.setMinWidth(35);
                    textPane.setMaxWidth(35);

                    HBox row = new HBox(5);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPrefWidth(250);

                    String label = plugin.getPluginName().getValue();

                    Label ltext = new Label();
                    ltext.setText(label);
                    ltext.setWrapText(false);
                    ltext.setMinWidth(0);

                    HBox spacer = new HBox();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    if (updateImageView.getImage() != null) {
                        row.getChildren().addAll(textPane, ltext, spacer, updateImageView);
                    } else {
                        row.getChildren().addAll(textPane, ltext, spacer);
                    }

                    setGraphic(row);
                } else {
                    setText(null);
                    setGraphic(null);
                }
            });
        }
    }

    public class JSBridge {

        public void installPlugin() {
            final PluginListItemMetadata plugin = listView.getSelectionModel().getSelectedItem();
            if (plugin == null) {
                Platform.runLater(() -> {
                    updateWebContent();
                });
                return;
            }
            final Function<Boolean, ? extends Class<Void>> functionCallback = (Boolean t) -> {
                if (t) {
                    Platform.runLater(() -> {
                        plugin.setIsInstalled(Boolean.TRUE);
                        plugin.setIsBusy(Boolean.FALSE);
                        updateWebContent();
                        listView.setItems(listView.getItems());
                    });
                } else {
                    /*
                     * ~kiran:IGBF-1108:Added to update UI if no network connection
                     */
                    Platform.runLater(() -> {
                        plugin.setIsBusy(Boolean.FALSE);
                        updateWebContent();
                    });
                }
                return Void.TYPE;
            };
            plugin.setIsBusy(Boolean.TRUE);
            updateWebContent();
            bundleActionManager.installBundle(plugin, functionCallback);
        }

        public void handleUnInstallClick() {
            final PluginListItemMetadata plugin = listView.getSelectionModel().getSelectedItem();
            if (plugin == null) {
                Platform.runLater(() -> {
                    updateWebContent();
                });
                return;
            }
            final Function<Boolean, ? extends Class<Void>> functionCallback = (Boolean t) -> {
                if (t) {
                    Platform.runLater(() -> {
                        plugin.setIsBusy(Boolean.FALSE);
                        plugin.setIsInstalled(Boolean.FALSE);
                        plugin.setIsUpdatable(Boolean.FALSE);
                        updateWebContent();
                    });
                }
                return Void.TYPE;
            };
            Platform.runLater(() -> {
                plugin.setIsBusy(Boolean.TRUE);
                updateWebContent();
            });
            bundleActionManager.uninstallBundle(plugin, functionCallback);
        }

        public void handleUpdateClick() {
            final PluginListItemMetadata plugin = listView.getSelectionModel().getSelectedItem();
            if (plugin == null) {
                Platform.runLater(() -> {
                    updateWebContent();
                });
                return;
            }
            updatePlugin(plugin);
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

    private void updatePlugin(final PluginListItemMetadata plugin) {
        final Function<Boolean, ? extends Class<Void>> handleUpdateCallback = (Boolean b) -> {
            if (b) {
                Platform.runLater(() -> {
                    plugin.setIsBusy(Boolean.FALSE);
                    updateWebContent();
                    refreshUpdateAllBtn();
                });
            }
            return Void.TYPE;
        };
        Platform.runLater(() -> {
            plugin.setIsBusy(Boolean.TRUE);
            updateWebContent();
        });
        bundleActionManager.updateBundle(plugin, handleUpdateCallback);
    }

    private void refreshListViewContent() {
        listView.setItems(filteredList.sorted());
        refreshUpdateAllBtn();
    }

    public void changeSearchFilter(String filter) {
        Platform.runLater(() -> {
            currentSearchPredicate = (s -> {
                String escapedFilter = Pattern.quote(filter);
                Pattern nameStartWith = Pattern.compile("^" + escapedFilter, Pattern.CASE_INSENSITIVE);
                Pattern nameContains = Pattern.compile(escapedFilter, Pattern.CASE_INSENSITIVE);
                Pattern descContains = Pattern.compile(escapedFilter, Pattern.CASE_INSENSITIVE);
                if (nameStartWith.matcher(s.getPluginName().getValue()).find()) {
                    s.setWeight(10);
                    return true;
                } else if (nameContains.matcher(s.getPluginName().getValue()).find()) {
                    s.setWeight(5);
                    return true;
                } else if (descContains.matcher(s.getDescription().getValue()).find()) {
                    s.setWeight(1);
                    return true;
                }
                s.setWeight(0);
                return false;
            });
            filteredList.setPredicate(currentSearchPredicate.and(currentStaticPredicate));
            updateWebContent();
        });
    }

    void changeStaticFilter(FilterOption filter) {
        Platform.runLater(() -> {
            switch (filter) {
                case ALL_APPS:
                    currentStaticPredicate = (PluginListItemMetadata s) -> true;
                    filteredList.setPredicate(currentStaticPredicate.and(currentSearchPredicate));
                    break;
                case INSTALLED:
                    currentStaticPredicate = (PluginListItemMetadata s) -> s.getIsInstalled().getValue();
                    filteredList.setPredicate(currentStaticPredicate.and(currentSearchPredicate));
                    break;
                case UNINSTALLED:
                    currentStaticPredicate = (PluginListItemMetadata s) -> !s.getIsInstalled().getValue();
                    filteredList.setPredicate(currentStaticPredicate.and(currentSearchPredicate));
                    break;
                default:
                    currentStaticPredicate = (PluginListItemMetadata s) -> true;
                    filteredList.setPredicate(currentStaticPredicate.and(currentSearchPredicate));
                    break;
            };
            updateWebContent();
        });
    }

    //IGBF-1608 : Update the installation status in the Local App Store
    /**
     * This method will return the list of Apps available in IGB to the Web App Manager
     *
     * @return ListView<PluginListItemMetaData>
     */
    public ListView<PluginListItemMetadata> getListView() {
        return listView;
    }

    //IGBF-1608 : end
    class AppManagerPanel extends VBox {

        public AppManagerPanel() {
            listView = new ListView<>();
            description = new WebView();
            search = new TextField();
            search.setPromptText("Search");
            search.getStyleClass().add("searchTextField");
            filterOptions = new ComboBox<>();
            filterOptions.setPromptText("All Apps");
            updateAllBtn = new Button("Update All");
            manageReposBtn = new Button("Manage Repositories...");
            splitPane = new SplitPane();
            setPrefSize(885, 541);
            HBox topPane = new HBox();
            topPane.setMinHeight(46);
            topPane.setPrefWidth(640);

            Insets margin = new Insets(10);
            HBox.setMargin(search, margin);
            HBox.setMargin(filterOptions, margin);
            HBox.setMargin(updateAllBtn, margin);
            HBox.setMargin(manageReposBtn, margin);
            VBox.setMargin(topPane, margin);

            topPane.getChildren().addAll(search, filterOptions, new Pane(),
                    updateAllBtn, manageReposBtn);
            VBox.setMargin(topPane, new Insets(10));
            splitPane.setDividerPositions(0.3488);
            splitPane.setPrefHeight(305);
            splitPane.setPrefWidth(640);
            splitPane.getItems().addAll(listView, description);
            getChildren().addAll(topPane, splitPane);
        }
    }
}
