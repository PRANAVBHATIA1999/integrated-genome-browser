package com.lorainelab.igb.preferences.weblink;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.genometry.symmetry.impl.CdsSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import com.lorainelab.context.menu.AnnotationContextMenuProvider;
import com.lorainelab.context.menu.model.AnnotationContextEvent;
import com.lorainelab.context.menu.model.ContextMenuItem;
import com.lorainelab.context.menu.model.MenuIcon;
import com.lorainelab.igb.preferences.weblink.model.WebLink;
import com.lorainelab.igb.services.IgbService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

@Component(immediate = true)
public class LinkControl implements AnnotationContextMenuProvider {

    private static final String searchWebIconPath = "16x16/actions/searchweb.png";
    private IgbService igbService;

    public LinkControl() {
    }

    @Activate
    private void activate() {
    }

    @Reference
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Override
    public Optional<ContextMenuItem> buildMenuItem(AnnotationContextEvent event) {
        if (event.getSelectedItems().isEmpty()) {
            return Optional.empty();
        }
        SeqSymmetry primarySym = event.getSelectedItems().get(0);
        if (primarySym instanceof CdsSeqSymmetry) {
            primarySym = ((CdsSeqSymmetry) primarySym).getPropertySymmetry();
        }
        return buildContextMenuItem(primarySym);
    }

    @Override
    public MenuSection getMenuSection() {
        return MenuSection.INFORMATION;
    }

    public void popupNotify(JPopupMenu popup, SeqSymmetry primarySym) {
        if (primarySym == null) {
            return;
        }

    }

    private Optional<ContextMenuItem> buildContextMenuItem(SeqSymmetry primarySym) {
        List<WebLink> results = new ArrayList<>();
        results.addAll(WebLinkUtils.getServerList().getWebLinks(primarySym));
        results.addAll(WebLinkUtils.getLocalList().getWebLinks(primarySym));
        if (results.isEmpty()) {
            return Optional.empty();
        }

        String name;
        ContextMenuItem contextMenuItem;
        if (results.size() == 1) {
            for (WebLink webLink : results) {
                name = webLink.getName();
                final String url = webLink.getURLForSym(primarySym);
                if (name == null || name.equals(url)) {
                    name = "Search Web";
                }
                contextMenuItem = new ContextMenuItem(name, (Void t) -> {
                    GeneralUtils.browse(url);
                    return t;
                });
                MenuIcon menuIcon = new MenuIcon(webLink.getImageIconPath());
                contextMenuItem.setMenuIcon(menuIcon);
                return Optional.of(contextMenuItem);

            }
        } else {
//            name = "Search Web";
//            JMenu linkMenu = new JMenu(name);
//            linkMenu.setIcon(CommonUtils.getInstance().getIcon(searchWebIconPath));
//            popup.add(linkMenu, 2);
//
//            for (WebLink webLink : results) {
//                url = webLink.getURLForSym(primarySym);
//                name = webLink.getName();
//                if (name == null || name.equals(url)) {
//                    name = "Unnamed link to web";
//                }
//                mi = makeMenuItem(name, url);
//                if (!Strings.isNullOrEmpty(webLink.getImageIconPath())) {
//                    mi.setIcon(CommonUtils.getInstance().getIcon(webLink.getImageIconPath()));
//                }
//                linkMenu.add(mi);
//            }
        }
        return Optional.empty();
    }

    private static JMenuItem makeMenuItem(String name, final String url) {
        JMenuItem linkMI = new JMenuItem(name);
        if (url != null) {
            linkMI.addActionListener(evt -> GeneralUtils.browse(url));
        }
        return linkMI;
    }

}
