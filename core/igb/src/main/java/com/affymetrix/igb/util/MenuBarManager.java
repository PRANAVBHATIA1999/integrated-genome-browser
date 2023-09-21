package com.affymetrix.igb.util;

import com.affymetrix.genoviz.swing.AMenuItem;
import com.affymetrix.igb.IGBConstants;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.action.AboutIGBAction;
import com.affymetrix.igb.action.AutoLoadThresholdAction;
import com.affymetrix.igb.action.CancelScriptAction;
import com.affymetrix.igb.action.ClampViewAction;
import com.affymetrix.igb.action.ClearVisualTools;
import com.affymetrix.igb.action.ConfigureScrollAction;
import com.affymetrix.igb.action.CopyResiduesAction;
import com.affymetrix.igb.action.DocumentationAction;
import com.affymetrix.igb.action.DrawCollapseControlAction;
import com.affymetrix.igb.action.ExitAction;
import com.affymetrix.igb.action.ExportFileAction;
import com.affymetrix.igb.action.LoadFileAction;
import com.affymetrix.igb.action.PreferencesAction;
import com.affymetrix.igb.action.RemoveFeatureAction;
import com.affymetrix.igb.action.RunScriptAction;
import com.affymetrix.igb.action.ShowAllVisualToolsAction;
import com.affymetrix.igb.action.ShowConsoleAction;
import com.affymetrix.igb.action.ShowFilterMarkAction;
import com.affymetrix.igb.action.ShowFullFilePathInTrack;
import com.affymetrix.igb.action.ShowIGBTrackMarkAction;
import com.affymetrix.igb.action.ShowLockedTrackIconAction;
import com.affymetrix.igb.action.StartAutoScrollAction;
import com.affymetrix.igb.action.StopAutoScrollAction;
import com.affymetrix.igb.action.ToggleEdgeMatchingAction;
import com.affymetrix.igb.action.ToggleHairlineAction;
import com.affymetrix.igb.action.ToggleHairlineLabelAction;
import com.affymetrix.igb.action.ToggleToolTipAction;
import com.affymetrix.igb.shared.DeselectAllAction;
import com.affymetrix.igb.shared.LoadURLAction;
import com.affymetrix.igb.shared.SelectAllAction;
import com.affymetrix.igb.swing.MenuUtil;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.MenuItemEventService;
import org.lorainelab.igb.menu.api.model.MenuBarMenuItemEvent;
import org.lorainelab.igb.menu.api.model.MenuBarParentMenu;
import org.lorainelab.igb.menu.api.model.MenuItem;
import static org.lorainelab.igb.menu.api.util.MenuUtils.convertContextMenuItemToJMenuItem;
import org.lorainelab.igb.services.IgbService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.LoggerFactory;
import javax.swing.SwingConstants;
import javax.swing.Action;

@Component(immediate = true, service = MenuBarManager.class)
public class MenuBarManager {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MenuBarManager.class);
    private JMenuBar menuBar;
    private IgbService igbService;
    private boolean componentActivated;

    private final List<MenuBarEntryProvider> menuBarExtensionsQueue;
    private final List<AMenuItem> aMenuItemQueue;
    private EnumMap<MenuBarParentMenu, JMenu> parentMenuReference;
    private EnumMap<MenuBarParentMenu, TreeMultimap<Integer, JComponent>> menuBarMenuContainer;
    private TreeMultimap<Integer, JComponent> fileMenuEntries;
    private TreeMultimap<Integer, JComponent> editMenuEntries;
    private TreeMultimap<Integer, JComponent> viewMenuEntries;
    private TreeMultimap<Integer, JComponent> toolsMenuEntries;
    private TreeMultimap<Integer, JComponent> helpMenuEntries;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu toolsMenu;
    private JMenu helpMenu;
    private MenuItemEventService menuItemEventService;
    private TreeMultimap<Integer, JComponent> parentMenuEntries;

    public MenuBarManager() {
        componentActivated = false;
        menuBarExtensionsQueue = new ArrayList<>();
        aMenuItemQueue = new ArrayList<>();
        fileMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        editMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        toolsMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        viewMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        helpMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        parentMenuEntries = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
        menuBarMenuContainer = new EnumMap<>(MenuBarParentMenu.class);
        menuBarMenuContainer.put(MenuBarParentMenu.FILE, fileMenuEntries);
        menuBarMenuContainer.put(MenuBarParentMenu.EDIT, editMenuEntries);
        menuBarMenuContainer.put(MenuBarParentMenu.TOOLS, toolsMenuEntries);
        menuBarMenuContainer.put(MenuBarParentMenu.VIEW, viewMenuEntries);
        menuBarMenuContainer.put(MenuBarParentMenu.HELP, helpMenuEntries);
        parentMenuReference = new EnumMap<>(MenuBarParentMenu.class);
        initializeMenus();
        initializeParentMenuReference();
        initializeParentMenuEntries();
    }

    @Activate
    public void activate() {
        componentActivated = true;
        menuBar = (JMenuBar) igbService.getApplicationFrame().getJMenuBar();
        loadQueuedMenuItems();
        rebuildMenus();
        menuItemEventService.getEventBus().register(this);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Reference
    public void setMenuItemEventService(MenuItemEventService menuItemEventService) {
        this.menuItemEventService = menuItemEventService;
    }

    private void loadQueuedMenuItems() {
        menuBarExtensionsQueue.stream().forEach(menuBarExtension -> addMenuBarExtension(menuBarExtension));
        aMenuItemQueue.stream().forEach(aMenuItem -> addAMenuItem(aMenuItem));
    }
    
    private JCheckBoxMenuItem setCheckBoxTextAlignment(Action action){
        JCheckBoxMenuItem j = new JCheckBoxMenuItem(action);
        j.setHorizontalTextPosition(SwingConstants.LEFT);
        return j;
    }


    private void initializeMenus() {
        initFileMenu();
        initEditMenu();
        initViewMenu();
        initToolsMenu();
        initHelpMenu();
    }

    private void initFileMenu() {
        fileMenu = new JMenu(BUNDLE.getString("fileMenu"));
        fileMenu.setMnemonic(BUNDLE.getString("fileMenuMnemonic").charAt(0));
        fileMenuEntries.put(1, new JMenuItem(LoadFileAction.getAction()));
        fileMenuEntries.put(5, new JMenuItem(LoadURLAction.getAction()));
        fileMenuEntries.put(10, new JSeparator());
        fileMenuEntries.put(15, new JMenuItem(ExportFileAction.getAction()));
        fileMenuEntries.put(20, new JMenuItem(RemoveFeatureAction.getAction()));
        fileMenuEntries.put(25, new JSeparator());
        fileMenuEntries.put(35, new JMenuItem(PreferencesAction.getAction()));
        fileMenuEntries.put(40, new JSeparator());
        fileMenuEntries.put(60, new JSeparator());
        fileMenuEntries.put(70, new JMenuItem(ExitAction.getAction()));
    }

    private void initEditMenu() {
        editMenu = new JMenu(BUNDLE.getString("editMenu"));
        editMenu.setMnemonic(BUNDLE.getString("editMenuMnemonic").charAt(0));
        editMenuEntries.put(1, new JMenuItem(CopyResiduesAction.getAction()));
        editMenuEntries.put(5, getSelectionsOptionsMenu());
    }

    private JMenu getSelectionsOptionsMenu() {
        JMenu selectMenu = new JMenu(IGBConstants.BUNDLE.getString("selectTracks"));
        selectMenu.setIcon(MenuUtil.getIcon("16x16/actions/blank_placeholder.png"));
        selectMenu.add(new JMenuItem(SelectAllAction.getAction()));
        selectMenu.add(new JMenuItem(DeselectAllAction.getAction()));
        return selectMenu;
    }

    private void initViewMenu() {
        viewMenu = new JMenu(BUNDLE.getString("viewMenu"));
        viewMenu.setMnemonic(BUNDLE.getString("viewMenuMnemonic").charAt(0));
        viewMenuEntries.put(1, new JMenuItem(ClearVisualTools.getAction()));
        viewMenuEntries.put(5, new JMenuItem(ShowAllVisualToolsAction.getAction()));
        viewMenuEntries.put(10, new JSeparator());
        viewMenuEntries.put(15, setCheckBoxTextAlignment(ToggleHairlineAction.getAction()));
        viewMenuEntries.put(20, setCheckBoxTextAlignment(ToggleHairlineLabelAction.getAction()));
        viewMenuEntries.put(25, setCheckBoxTextAlignment(DrawCollapseControlAction.getAction()));
        viewMenuEntries.put(30, setCheckBoxTextAlignment(ShowIGBTrackMarkAction.getAction()));
        viewMenuEntries.put(35, setCheckBoxTextAlignment(ShowFilterMarkAction.getAction()));
        viewMenuEntries.put(40, setCheckBoxTextAlignment(ToggleEdgeMatchingAction.getAction()));
        viewMenuEntries.put(45, setCheckBoxTextAlignment(ShowLockedTrackIconAction.getAction()));
        viewMenuEntries.put(50, new JSeparator());
        viewMenuEntries.put(55, setCheckBoxTextAlignment(ToggleToolTipAction.getAction()));
        viewMenuEntries.put(60, setCheckBoxTextAlignment(ShowFullFilePathInTrack.getAction()));
        viewMenuEntries.put(65, new JMenuItem(AutoLoadThresholdAction.getAction()));
        viewMenuEntries.put(70, new JMenuItem(ClampViewAction.getAction()));
        
    }

    private void initToolsMenu() {
        toolsMenu = new JMenu(BUNDLE.getString("toolsMenu"));
        toolsMenu.setMnemonic(BUNDLE.getString("toolsMenuMnemonic").charAt(0));
        toolsMenuEntries.put(1, new JMenuItem(StartAutoScrollAction.getAction()));
        toolsMenuEntries.put(5, new JMenuItem(StopAutoScrollAction.getAction()));
        toolsMenuEntries.put(10, new JMenuItem(ConfigureScrollAction.getAction()));
        toolsMenuEntries.put(15, new JSeparator());
        toolsMenuEntries.put(20, getScriptsActionMenu());
        toolsMenuEntries.put(25, new JSeparator());
    }

    private JMenu getScriptsActionMenu() {
        JMenu scriptsMenu = new JMenu(BUNDLE.getString("scripts"));
        scriptsMenu.setIcon(MenuUtil.getIcon("16x16/actions/blank_placeholder.png"));
        MenuUtil.addToMenu(scriptsMenu, new JMenuItem(RunScriptAction.getAction()));
        MenuUtil.addToMenu(scriptsMenu, new JMenuItem(CancelScriptAction.getAction()));
        return scriptsMenu;
    }

    private void initHelpMenu() {
        helpMenu = new JMenu(BUNDLE.getString("helpMenu"));
        helpMenu.setMnemonic(BUNDLE.getString("helpMenuMnemonic").charAt(0));
        helpMenuEntries.put(1, new JMenuItem(AboutIGBAction.getAction()));
        helpMenuEntries.put(5, new JMenuItem(DocumentationAction.getAction()));
        helpMenuEntries.put(10, new JMenuItem(ShowConsoleAction.getAction()));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, unbind = "removeAMenuItem", policy = ReferencePolicy.DYNAMIC)
    public void addAMenuItem(AMenuItem aMenuItem) {
        if (componentActivated) {
            JMenu parent = parentMenuReference.get(MenuBarParentMenu.valueOf(aMenuItem.getParentMenu()));
            MenuUtil.addToMenu(parent, aMenuItem.getMenuItem());
        }
    }

    public void removeAMenuItem(AMenuItem aMenuItem) {
        JMenu parent = parentMenuReference.get(MenuBarParentMenu.valueOf(aMenuItem.getParentMenu()));
        parent.remove(aMenuItem.getMenuItem());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, unbind = "removeMenuBarExtension", policy = ReferencePolicy.DYNAMIC)
    public void addMenuBarExtension(MenuBarEntryProvider menuBarExtension) {
        if (componentActivated) {
            Optional<List<MenuItem>> menuItems = menuBarExtension.getMenuItems();
            if (menuItems.isPresent()) {
                menuItems.get().stream().forEach(menuItem -> {
                    switch (menuBarExtension.getMenuExtensionParent()) {
                        case FILE:
                            fileMenuEntries.put(menuItem.getWeight(), convertContextMenuItemToJMenuItem(menuItem));
                            break;
                        case EDIT:
                            editMenuEntries.put(menuItem.getWeight(), convertContextMenuItemToJMenuItem(menuItem));
                            break;
                        case TOOLS:
                            toolsMenuEntries.put(menuItem.getWeight(), convertContextMenuItemToJMenuItem(menuItem));
                            break;
                        case VIEW:
                            viewMenuEntries.put(menuItem.getWeight(), convertContextMenuItemToJMenuItem(menuItem));
                            break;
                        case HELP:
                            helpMenuEntries.put(menuItem.getWeight(), convertContextMenuItemToJMenuItem(menuItem));
                            break;
                    }
                });

            }
        } else {
            menuBarExtensionsQueue.add(menuBarExtension);
        }
        if (componentActivated) {
            rebuildMenus();
        }
    }

    public void removeMenuBarExtension(MenuBarEntryProvider menuBarExtension) {
        Optional<List<MenuItem>> menuItems = menuBarExtension.getMenuItems();
        if (menuItems.isPresent()) {
            menuItems.get().stream().forEach(menuItem -> {
                final JMenuItem menuItemComponent = convertContextMenuItemToJMenuItem(menuItem);
                switch (menuBarExtension.getMenuExtensionParent()) {
                    case FILE:
                        removeMenuEntry(fileMenuEntries, menuItem);
                        break;
                    case EDIT:
                        removeMenuEntry(editMenuEntries, menuItem);
                        break;
                    case TOOLS:
                        removeMenuEntry(toolsMenuEntries, menuItem);
                        break;
                    case VIEW:
                        removeMenuEntry(viewMenuEntries, menuItem);
                        break;
                    case HELP:
                        removeMenuEntry(helpMenuEntries, menuItem);
                        break;
                }
            });
        }
        if (componentActivated) {
            rebuildMenus();
        }
    }

    private void removeMenuEntry(TreeMultimap<Integer, JComponent> menuEntryHolder, MenuItem menuItem) {
        Optional<JMenuItem> toRemove = findMenuToRemove(menuEntryHolder, menuItem);
        if (toRemove.isPresent()) {
            menuEntryHolder.remove(menuItem.getWeight(), toRemove.get());
        }
    }

    private Optional<JMenuItem> findMenuToRemove(TreeMultimap<Integer, JComponent> menuEntryHolder, MenuItem menuItem) {
        Optional<JMenuItem> toRemove = menuEntryHolder.keySet().stream()
                .filter(key -> key.equals(menuItem.getWeight()))
                .map(key -> menuEntryHolder.get(key))
                .flatMap(comps -> comps.stream())
                .filter(comp -> comp instanceof JMenuItem)
                .map(comp -> JMenuItem.class.cast(comp))
                .filter(jmenuItem -> jmenuItem.getText().equalsIgnoreCase(menuItem.getMenuLabel()))
                .findFirst();
        return toRemove;
    }

    public void addParentMenuEntry(JMenu jMenu, int weight) {
        parentMenuEntries.put(weight, jMenu);
        rebuildMenus();
    }

    private void rebuildMenus() {
        rebuildFileMenu();
        rebuildEditMenu();
        rebuildViewMenu();
        rebuildToolsMenu();
        rebuildHelpMenu();
        rebuildParentMenus();
    }

    private void rebuildParentMenus() {
        menuBar.removeAll();
        parentMenuEntries.keySet().stream().forEach(key -> {
            parentMenuEntries.get(key).forEach(menuBar::add);
        });
    }

    private void rebuildHelpMenu() {
        helpMenu.removeAll();
        helpMenuEntries.keySet().stream().forEach(key -> {
            helpMenuEntries.get(key).forEach(helpMenu::add);
        });
    }

    private void rebuildToolsMenu() {
        toolsMenu.removeAll();
        toolsMenuEntries.keySet().stream().forEach(key -> {
            toolsMenuEntries.get(key).forEach(toolsMenu::add);
        });
    }

    private void rebuildViewMenu() {
        viewMenu.removeAll();
        viewMenuEntries.keySet().stream().forEach(key -> {
            viewMenuEntries.get(key).forEach(viewMenu::add);
        });
    }

    private void rebuildEditMenu() {
        editMenu.removeAll();
        editMenuEntries.keySet().stream().forEach(key -> {
            editMenuEntries.get(key).forEach(editMenu::add);
        });
    }

    private void rebuildFileMenu() {
        fileMenu.removeAll();
        fileMenuEntries.keySet().stream().forEach(key -> {
            fileMenuEntries.get(key).forEach(fileMenu::add);
        });
    }

    private void initializeParentMenuReference() {
        parentMenuReference.put(MenuBarParentMenu.FILE, fileMenu);
        parentMenuReference.put(MenuBarParentMenu.EDIT, editMenu);
        parentMenuReference.put(MenuBarParentMenu.VIEW, viewMenu);
        parentMenuReference.put(MenuBarParentMenu.TOOLS, toolsMenu);
        parentMenuReference.put(MenuBarParentMenu.HELP, helpMenu);
    }

    @Subscribe
    public void menuItemEvenListener(MenuBarMenuItemEvent menuItemEvent) {
        switch (menuItemEvent.getMenuBarParentMenu()) {
            case FILE:
                Optional<JMenuItem> fileMatch = findJMatchingJMenuItem(menuItemEvent, fileMenuEntries);
                if (fileMatch.isPresent()) {
                    fileMatch.get().setEnabled(menuItemEvent.getMenuItem().isEnabled());
                }
                break;
            case EDIT:
                Optional<JMenuItem> editMatch = findJMatchingJMenuItem(menuItemEvent, editMenuEntries);
                if (editMatch.isPresent()) {
                    editMatch.get().setEnabled(menuItemEvent.getMenuItem().isEnabled());
                }
                break;
            case TOOLS:
                Optional<JMenuItem> toolMenuMatch = findJMatchingJMenuItem(menuItemEvent, toolsMenuEntries);
                if (toolMenuMatch.isPresent()) {
                    toolMenuMatch.get().setEnabled(menuItemEvent.getMenuItem().isEnabled());
                }
                break;
            case VIEW:
                Optional<JMenuItem> viewMenuMatch = findJMatchingJMenuItem(menuItemEvent, viewMenuEntries);
                if (viewMenuMatch.isPresent()) {
                    viewMenuMatch.get().setEnabled(menuItemEvent.getMenuItem().isEnabled());
                }
                break;
            case HELP:
                Optional<JMenuItem> helpMenuMatch = findJMatchingJMenuItem(menuItemEvent, helpMenuEntries);
                if (helpMenuMatch.isPresent()) {
                    helpMenuMatch.get().setEnabled(menuItemEvent.getMenuItem().isEnabled());
                }
                break;
        }
    }

    private static Optional<JMenuItem> findJMatchingJMenuItem(MenuBarMenuItemEvent menuItemEvent, TreeMultimap<Integer, JComponent> fileMenuEntries) {
        return fileMenuEntries.get(menuItemEvent.getMenuItem().getWeight()).stream()
                .filter(jMenuItem -> jMenuItem instanceof JMenuItem)
                .map(jMenuItem -> JMenuItem.class.cast(jMenuItem))
                .filter(jMenuItem -> jMenuItem.getText().equals(menuItemEvent.getMenuItem().getMenuLabel()))
                .findFirst();

    }

    private void initializeParentMenuEntries() {
        parentMenuEntries.put(0, fileMenu);
        parentMenuEntries.put(1, editMenu);
        parentMenuEntries.put(5, toolsMenu);
        parentMenuEntries.put(20, viewMenu);
        parentMenuEntries.put(100, helpMenu);
    }
}
