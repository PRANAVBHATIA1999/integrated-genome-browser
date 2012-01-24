/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.view.welcome;

import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.view.SeqGroupView;
import com.affymetrix.igb.view.SeqMapView;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;
import be.pwnt.jflow.JFlowPanel;
import be.pwnt.jflow.event.ShapeEvent;
import be.pwnt.jflow.event.ShapeListener;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.util.ErrorHandler;
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
	private static MainWorkspaceManager singleton;
	
	private static final String SELECT_SPECIES = IGBConstants.BUNDLE.getString("speciesCap");
	
	private final GenometryModel gmodel;
	
	public static MainWorkspaceManager getWorkspaceManager(){
		if(singleton == null){
			singleton = new MainWorkspaceManager();
		}
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

					if(group == null || group.getEnabledVersions().isEmpty()){
						Application.getSingleton().setStatus(groupStr+" Not Available", true);
						ErrorHandler.errorPanel("NOTICE", groupStr + " not available at this time. "
								+ "Please check that the appropriate data source is available.");
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
		if(e.getStateChange() != ItemEvent.SELECTED || e.getItem() == null)
			return;
		
		CardLayout layout = (CardLayout) getLayout();
		System.out.println("MainWorkspaceManager:itemStateChanged hit");
		String species = e.getItem().toString();
		if(gmodel.getSelectedSeqGroup() == null && SELECT_SPECIES.equals(species)){
			layout.show( this, WELCOME_PANE );
		}else{
			layout.show( this, SEQ_MAP_PANE );
		}
	}
	
}
