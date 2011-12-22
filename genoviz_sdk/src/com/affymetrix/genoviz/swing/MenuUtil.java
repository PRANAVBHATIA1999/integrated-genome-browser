/**
*   Copyright (c) 2001-2007 Affymetrix, Inc.
*    
*   Licensed under the Common Public License, Version 1.0 (the "License").
*   A copy of the license must be included with any distribution of
*   this source code.
*   Distributions from Affymetrix, Inc., place this in the
*   IGB_LICENSE.html file.  
*
*   The license is also available at
*   http://www.opensource.org/licenses/cpl.php
*/

package com.affymetrix.genoviz.swing;

import javax.swing.*;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenu;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public abstract class MenuUtil {

//  private static JMenuBar main_menu_bar = new JMenuBar();
    
  private static Map<String, KeyStroke> accelerators = new HashMap<String, KeyStroke>();
  
  public static void setAccelerators(Map<String, KeyStroke> _accelerators) {
	MenuUtil.accelerators = _accelerators;
  }

  /** Sets the accelerator for the given JMenuItem based on
   *  the preference associated with the action command.
   *  The action command Strings should be unique across the whole application.
   */
  private static final void addAccelerator(JMenuItem item, String command) {
    item.setAccelerator(accelerators.get(command));
  }
  
  /** Sets up an association such that the accelerator given in
   *  the user prefs for the action_command will cause the given
   *  action_command to be sent to the given ActionListener
   *  when that accelerator key is pressed and the given component
   *  is in the window that has keyboard focus.
   *  If there was no user preference given for the action command,
   *  this routine does nothing.
   *  @return a KeyStroke, from {@link PreferenceUtils#getAccelerator(String)}
   *  is returned as a convenience
   *  @see PreferenceUtils#getAccelerator(String)
   */
  public static final KeyStroke addAccelerator(JComponent comp, ActionListener al,
    String action_command) {
    KeyStroke ks = accelerators.get(action_command);
    if (ks != null) {
      comp.registerKeyboardAction(al, action_command, ks,
       JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    return ks;
  }

//  public static final JMenuBar getMainMenuBar() {
//    return main_menu_bar;
//  }

  private static JMenu findMenu(JMenuBar main_menu_bar, String name) {
    int num_menus = main_menu_bar.getMenuCount();
    for (int i=0; i<num_menus; i++) {
    	JMenu menu_i = main_menu_bar.getMenu(i);
      if (name.equals(menu_i.getText())) {
        menu_i.getName();
        return menu_i;
      }
    }
    return null;
  }

  private static void addMenu(JMenuBar main_menu_bar, JMenu new_menu) {
	int num_menus = main_menu_bar.getMenuCount();
    // Add the new menu, but keep the "Help" menu in last place
    if (num_menus > 0 && "Help".equals(main_menu_bar.getMenu(num_menus-1).getName())) {
      main_menu_bar.add(new_menu, num_menus-1);
    } else {
      main_menu_bar.add(new_menu);
    }
  }

  public static final JMenu getMenu(JMenuBar main_menu_bar, String name) {
    JMenu new_menu = findMenu(main_menu_bar, name);
    if (new_menu != null) {
    	return new_menu;
    }
    new_menu = new JMenu(name);
    new_menu.setName(name); // JMenu.getName() and JMenu.getText() aren't automatically equal
    addMenu(main_menu_bar, new_menu);
    return new_menu;
  }
  
  public static final JRPMenu getRPMenu(JMenuBar main_menu_bar, String id, String name) {
    JRPMenu new_menu = (JRPMenu)findMenu(main_menu_bar, name);
    if (new_menu != null) {
    	return new_menu;
    }
    new_menu = new JRPMenu(id, name);
    new_menu.setName(name); // JMenu.getName() and JMenu.getText() aren't automatically equal
    addMenu(main_menu_bar, new_menu);
    return new_menu;
  }
  
  /**
   *  Calls {@link #addToMenu(JMenu, JMenuItem)}
   *  with command set to null.
   */
  public static final JMenuItem addToMenu(JMenu menu, JMenuItem item) {
    return addToMenu(menu, item, "");
  }

  public static final JMenuItem addToMenu(JMenu menu, JMenuItem item, String prefix){
	String command = item.getText();
	if (command != null) {
		if(prefix != null && prefix.length() > 0){
			command = prefix + " / " + command;
		}
		addAccelerator(item, command);
	}
    return menu.add(item);
  }

  public static final JMenuItem insertIntoMenu(JMenu menu, JMenuItem item, int position) {
    String command = item.getText();
    if (command != null) { addAccelerator(item, command); }
    return menu.insert(item, position);
  }

  public static final void removeFromMenu(JMenu menu, JMenuItem item) {
    menu.remove(item);
  }

  public static ImageIcon getIcon(String resource_name) {
    return CommonUtils.getInstance().getIcon(resource_name);    
  }
}
