/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lorainelab.igb.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.lorainelab.igb.plugins.model.PluginListItemMetadata;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import javafx.scene.paint.Color;
import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import org.junit.Test;

/**
 *
 * @author dcnorris
 */
public class PluginManagerFxPanelTest {

    @Test
    public void testPanelUI() throws InterruptedException, IOException {
        JFrame testFrame = new JFrame("");
        MigLayout migLayout = new MigLayout("fill");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setLayout(migLayout);
        testFrame.setSize(new Dimension(885, 541));
        PluginManagerFxPanel fxPanel = new PluginManagerFxPanel();
        fxPanel.setMaterialDesignColors(getColors());
        fxPanel.updateListContent(getListItems());
        testFrame.add(fxPanel, "grow");
        testFrame.setVisible(true);
        Thread.sleep(150000);
    }

    private List<Color> getColors() {
        return ImmutableList.of(
                Color.rgb(156, 39, 176),
                Color.rgb(233, 30, 99),
                Color.rgb(244, 67, 54),
                Color.rgb(33, 150, 243),
                Color.rgb(63, 81, 181)
        );
    }

    private List<PluginListItemMetadata> getListItems() throws IOException {
        String readmeMarkdown = CharStreams.toString(new InputStreamReader(PluginManagerFxPanelTest.class.getClassLoader().getResourceAsStream("README.md")));
        return ImmutableList.of(
                new PluginListItemMetadata("ProtAnnot", "bioviz", "1.0.1", readmeMarkdown, Boolean.FALSE, Boolean.TRUE),
                new PluginListItemMetadata("Crisper Cas", "bioviz", "1.0.1", "## test", Boolean.FALSE, Boolean.FALSE),
                new PluginListItemMetadata("Command Socket", "bioviz", "1.0.1", "### test", Boolean.TRUE, Boolean.TRUE));
    }
}
