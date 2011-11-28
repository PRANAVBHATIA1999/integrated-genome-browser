/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.view.load;

import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.view.SeqGroupView;
import com.affymetrix.igb.view.SeqMapView;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import be.pwnt.jflow.JFlowPanel;
import be.pwnt.jflow.event.ShapeEvent;
import be.pwnt.jflow.event.ShapeListener;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.igb.Application;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

/**
 * This class manages the cover flow visualization.  It has a card layout where
 * the welcome panel is held as well as the SeqMapView object.
 * 
 * Upon the user's choice (from the cover flow, or from the SeqGroupView combo box)
 * the welcome screen falls out to the background, and the SeqMapView is brought to 
 * the foreground.  
 * 
 * 
 * @author jfvillal
 */
public class MainWorkspaceManager extends JPanel implements ItemListener{
	private static final long serialVersionUID = 1L;
	public static final String WELCOME_PANE = "WelcomePane";
	public static final String SEQ_MAP_PANE = "SeqMapPane";
	private static final MainWorkspaceManager singleton = new MainWorkspaceManager();
	
	private static final String SELECT_SPECIES = IGBConstants.BUNDLE.getString("speciesCap");
	
	private final GenometryModel gmodel;
	
	public static MainWorkspaceManager getWorkspaceManager(){
		return singleton;
	}
	public MainWorkspaceManager(){
		this.setLayout( new CardLayout());
		add( new WelcomePage( getWelcomePane() ), WELCOME_PANE);
		CardLayout layout = (CardLayout) getLayout();
        layout.show( this, WELCOME_PANE );    
		gmodel = GenometryModel.getGenometryModel();
	}
	public void setSeqMapViewObj( SeqMapView obj){
		add(obj, SEQ_MAP_PANE);
	}
	/**
	 * Returns welcome JPanel
	 * @return 
	 */
	public JPanel getWelcomePane(){
		//return new JPanel();
		final JFlowPanel panel = new JFlowPanel(new GeneConfiguration());
		panel.setPreferredSize(new Dimension(500, 200));
		panel.addListener(new ShapeListener() {
			@Override
			public void shapeClicked(ShapeEvent e) {
				MouseEvent me = e.getMouseEvent();
				if (!me.isConsumed() && me.getButton() == MouseEvent.BUTTON1
						&& me.getClickCount() == 1) {
					//JOptionPane.showMessageDialog(panel,
					//		"You clicked on " + e.getShape() + ".",
					//		"Event Test", JOptionPane.INFORMATION_MESSAGE);
					CargoPicture pic = (CargoPicture) e.getShape();
					Object obj = pic.getCargo();

					if(obj == null)
						return;

					String groupStr = (String)obj;
					AnnotatedSeqGroup group = gmodel.getSeqGroup(groupStr);

					if(group == null){
						Application.getSingleton().setStatus(groupStr+" Not Available", true);
						return;
					}

					SeqGroupView.getInstance().setSelectedGroup(groupStr);
				}
			}

			@Override
			public void shapeActivated(ShapeEvent e) {
			}

			@Override
			public void shapeDeactivated(ShapeEvent e) {
			}
		});
		return panel;
	
		 
	}
	
	/**
	 * Receives state update from the genus/species combo boxes. 
	 * @param e 
	 */
	public void itemStateChanged(ItemEvent e) {
		CardLayout layout = (CardLayout) getLayout();
		System.out.println("MainWorkspaceManager:itemStateChanged hit");
		JComboBox jb = (JComboBox) e.getSource();
		if(jb.getSelectedItem() != null &&
				SELECT_SPECIES.equals(jb.getSelectedItem().toString())){
			layout.show( this, WELCOME_PANE );
		}else{
			layout.show( this, SEQ_MAP_PANE );
		}
	}
	
}
