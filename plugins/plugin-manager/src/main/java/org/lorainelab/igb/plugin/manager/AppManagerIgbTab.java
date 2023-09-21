package org.lorainelab.igb.plugin.manager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import static org.lorainelab.igb.services.ServiceComponentNameReference.APP_MANAGER_TAB;
import org.lorainelab.igb.services.window.tabs.IgbTabPanel;
import org.lorainelab.igb.services.window.tabs.IgbTabPanelI;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javax.swing.JButton;
import javax.swing.JFrame;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tab Panel for managing plugins / bundles.
 */
@Component(name = APP_MANAGER_TAB, service = {IgbTabPanelI.class}, immediate = true)
public class AppManagerIgbTab extends IgbTabPanel {

    private static final Logger logger = LoggerFactory.getLogger(AppManagerIgbTab.class);
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("plugins");
    private static final int TAB_POSITION = 8;
    private AppManagerFrame frame;

    public AppManagerIgbTab() {
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
                frame.setState(JFrame.NORMAL);
                frame.setVisible(true);
            });
        });
    }

    @Reference
    public void setFxPanel(AppManagerFrame frame) {
        this.frame = frame;
    }

}
