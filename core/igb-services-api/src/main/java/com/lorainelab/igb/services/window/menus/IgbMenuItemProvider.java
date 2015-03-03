package com.lorainelab.igb.services.window.menus;

import com.affymetrix.igb.swing.JRPMenuItem;
import javax.swing.JMenuItem;

/**
 *
 * @author dcnorris
 */
public interface IgbMenuItemProvider {

    public String getParentMenuName();

    public JRPMenuItem getMenuItem();

    public int getMenuItemWeight();
}
