package com.affymetrix.igb.external;

import java.awt.CardLayout;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;

import com.affymetrix.genometryImpl.util.MenuUtil;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.view.SeqMapView;

/**
 * Container panel for the external views
 * Shows up as tab in IGB
 * Allows selection of subviews with combobox
 *
 * @author Ido M. Tamir
 */
public class ExternalViewer extends IGBTabPanel {
	private static final long serialVersionUID = 1L;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("external");
	private static final int TAB_POSITION = 5;
	private static final int VIEW_MENU_POS = 2;

	private static final String[] names = {UCSCView.viewName};
	final JComboBox ucscBox;
	private final UCSCViewAction ucscViewAction;
	private final JMenuItem menuItem;

	public ExternalViewer(IGBService igbService_) {
		super(igbService_, BUNDLE.getString("externalViewTab"), BUNDLE.getString("externalViewTab"), false, TAB_POSITION);
		this.setLayout(new CardLayout());
		ucscBox = createBox();
		ucscViewAction = new UCSCViewAction((SeqMapView)igbService.getMapView());
		menuItem = new JMenuItem(ucscViewAction);
		MenuUtil.insertIntoMenu(igbService.getViewMenu(), menuItem, VIEW_MENU_POS);

		final UCSCView ucsc = new UCSCView(ucscBox, igbService, ucscViewAction);

		add(ucsc, ucsc.getViewName());
	}

	private JComboBox createBox() {
		JComboBox box = new JComboBox(names);
		box.setPrototypeDisplayValue("ENSEMBL");
		box.setMaximumSize(box.getPreferredSize());
		box.setEditable(false);
		return box;
	}

	public void removeViewer() {
		MenuUtil.removeFromMenu(igbService.getViewMenu(), menuItem);
	}

	@Override
	public boolean isEmbedded() {
		return true;
	}
}
