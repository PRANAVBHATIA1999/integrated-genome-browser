package com.lorainelab.igb.plugins;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.lorainelab.igb.services.window.tabs.IgbTabPanel;
import com.lorainelab.igb.services.window.tabs.IgbTabPanelI;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javax.swing.JButton;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tab Panel for managing plugins / bundles.
 */
@Component(name = PluginsView.COMPONENT_NAME, provide = {IgbTabPanelI.class}, immediate = true)
public class PluginsView extends IgbTabPanel {

    public static final String COMPONENT_NAME = "PluginsView";
    private static final Logger logger = LoggerFactory.getLogger(PluginsView.class);
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("plugins");
    private static final int TAB_POSITION = 7;
    private AppManagerFrame frame;

    public PluginsView() {
        super(BUNDLE.getString("viewTab"), BUNDLE.getString("viewTab"), BUNDLE.getString("pluginsTooltip"), false, TAB_POSITION);
        setLayout(new MigLayout("fill"));
        message = new JButton("Launch App Manager");
        add(message, new CC().alignX("center").spanX());
    }
    private JButton message;

    @Activate
    private void activate() {
        message.addActionListener((ActionEvent e) -> {
            Platform.runLater(() -> {
                frame.setVisible(true);
            });
        });
    }

    @Reference
    public void setFxPanel(AppManagerFrame frame) {
        this.frame = frame;
    }

}
