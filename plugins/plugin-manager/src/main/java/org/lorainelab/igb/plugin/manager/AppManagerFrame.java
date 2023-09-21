/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.plugin.manager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import java.awt.Dimension;
import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import org.lorainelab.igb.services.IgbService;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, service = AppManagerFrame.class)
public class AppManagerFrame extends JFrame {

    private AppManagerFxPanel fxPanel;
    private IgbService igbService;

    public AppManagerFrame() {
        setTitle("IGB App Manager");
        init();
    }

    private void init() {
        MigLayout migLayout = new MigLayout("fill");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(migLayout);
        setSize(new Dimension(1000, 618));
    }

    @Activate
    private void activate() {
        setLocationRelativeTo(igbService.getApplicationFrame());
        add(fxPanel, "grow");
    }

    @Reference
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Reference
    public void setFxPanel(AppManagerFxPanel fxPanel) {
        this.fxPanel = fxPanel;
    }

}
