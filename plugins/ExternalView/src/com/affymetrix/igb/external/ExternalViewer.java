package com.affymetrix.igb.external;

import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;

import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.JRPComboBox;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;

/**
 * Container panel for the external views
 * Shows up as tab in IGB
 * Allows selection of subviews with combobox
 *
 * The mappings for ensembl are defined in ensemblURLs tab delimited text file
 *
 * @author Ido M. Tamir
 */
public class ExternalViewer extends IGBTabPanel implements ItemListener {
	private static final long serialVersionUID = 1L;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("external");
	private static final int TAB_POSITION = 5;
	private static final int VIEW_MENU_POS = 2;

	private static final String[] names = {UCSCView.viewName, EnsemblView.viewName};
	final JRPComboBox ucscBox;
	final JRPComboBox ensemblBox;
	
	private final UCSCViewAction ucscViewAction;
	private final JMenuItem menuItem;

	public ExternalViewer(IGBService igbService_) {
		super(igbService_, BUNDLE.getString("externalViewTab"), BUNDLE.getString("externalViewTab"), false, TAB_POSITION);
		this.setLayout(new CardLayout());
		ucscBox = createBox("ExternalViewer.ucsc");
		ensemblBox = createBox("ExternalViewer.ensemble");
		
		ucscViewAction = new UCSCViewAction(igbService);
		menuItem = new JMenuItem(ucscViewAction);
		MenuUtil.insertIntoMenu(igbService.getViewMenu(), menuItem, VIEW_MENU_POS);

		final UCSCView ucsc = new UCSCView(ucscBox, igbService, ucscViewAction);
		add(ucsc, ucsc.getViewName());
		final EnsemblView ensembl = new EnsemblView(ensemblBox, igbService, ucscViewAction);
		add(ensembl, ensembl.getViewName());
	}

	private JRPComboBox createBox(String id) {
		JRPComboBox box = new JRPComboBox(id, names);
		box.setPrototypeDisplayValue("ENSEMBL");
		box.setMaximumSize(box.getPreferredSize());
		box.setEditable(false);
		box.addItemListener(this);
		return box;
	}

	public void removeViewer() {
		MenuUtil.removeFromMenu(igbService.getViewMenu(), menuItem);
	}

	@Override
	public boolean isEmbedded() {
		return true;
	}

	@Override
	public boolean isCheckMinimumWindowSize() {
		return true;
	}
	
	public void itemStateChanged(ItemEvent e){
		if(e.getID() == ItemEvent.ITEM_STATE_CHANGED){
			CardLayout cl = (CardLayout) getLayout();
			if(e.getSource() == ucscBox){
				ensemblBox.setSelectedItem(EnsemblView.viewName);
			}
			if(e.getSource() == ensemblBox){
				ucscBox.setSelectedItem(UCSCView.viewName);
			}
			cl.show(ExternalViewer.this, (String) e.getItem());
		}
	}

}
