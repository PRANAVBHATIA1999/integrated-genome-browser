package com.affymetrix.igb.window.service.def;

import com.lorainelab.igb.service.api.IgbTabPanel;
import com.lorainelab.igb.service.api.TabHolder;
import java.util.HashSet;
import java.util.Set;

/**
 * TabHolder implementation for all tabs that are hidden
 *
 */
public class HiddenTabs implements TabHolder {

    private Set<IgbTabPanel> addedPlugins;

    public HiddenTabs() {
        super();
        addedPlugins = new HashSet<>();
    }

    @Override
    public Set<IgbTabPanel> getIGBTabPanels() {
        return addedPlugins;
    }

    @Override
    public void addTab(IgbTabPanel plugin) {
        addedPlugins.add(plugin);
    }

    @Override
    public void removeTab(IgbTabPanel plugin) {
        addedPlugins.remove(plugin);
    }

    @Override
    public void restoreState() {
    }

    @Override
    public void resize() {
    }

    @Override
    public void close() {
    }

    @Override
    public void selectTab(IgbTabPanel panel) {
    }

    @Override
    public String getName() {
        return "Hidden";
    }
}
