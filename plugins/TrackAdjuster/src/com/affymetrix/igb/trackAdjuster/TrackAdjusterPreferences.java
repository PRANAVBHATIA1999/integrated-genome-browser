/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.trackAdjuster;

import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.osgi.service.IGBService;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.JFrame;

/**
 *
 * @author lorainelab
 */
public class TrackAdjusterPreferences extends javax.swing.JFrame {
	
	public static int TAB_STYLE = -1;
	public static int TAB_ANNOTATION = -1;
	public static int TAB_VISIBLE_RANGE = -1;
	public static int TAB_GRAPH = -1;
	public static int TAB_DISPLAY_PLUGIN = -1;
	private TrackAdjusterTab sgt;
	private static final long serialVersionUID = 1L;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("trackAdjuster");
	private static final String WINDOW_NAME = BUNDLE.getString("preferenceWindow");
	public static TrackAdjusterPreferences singleton = null;
	private JFrame frame = null;
	private IGBService igbService;

	public TrackAdjusterPreferences(IGBService igbS) {
		igbService = igbS;
		sgt = TrackAdjusterTab.getSingleton();
		initComponents();
		
		this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent evt) {
				PreferenceUtils.saveWindowLocation(singleton, WINDOW_NAME);				
				singleton.dispose();
			}
		});
	}
	
	public static TrackAdjusterPreferences getSingleton(IGBService igbS) {
		if (singleton != null) {
			return singleton;
		}
		singleton = new TrackAdjusterPreferences(igbS);		
		return singleton;
	}

	/**
	 * Set the tab pane to the given index.
	 */
	public void setTab(int i) {
		if (i < 0 || i >= mainPane.getComponentCount()) {
			return;
		}
		mainPane.setSelectedIndex(i);		
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPane = new javax.swing.JTabbedPane();
        displayTab = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        annotationTab = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        visibleRangeTab = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        graphTab = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = sgt.heat_mapCB;
        pluginTab = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel1.setText("Not implemented yet");

        org.jdesktop.layout.GroupLayout displayTabLayout = new org.jdesktop.layout.GroupLayout(displayTab);
        displayTab.setLayout(displayTabLayout);
        displayTabLayout.setHorizontalGroup(
            displayTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(displayTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addContainerGap(240, Short.MAX_VALUE))
        );
        displayTabLayout.setVerticalGroup(
            displayTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(displayTabLayout.createSequentialGroup()
                .add(jLabel1)
                .add(0, 378, Short.MAX_VALUE))
        );

        mainPane.addTab("Display", displayTab);
        TAB_STYLE = mainPane.indexOfComponent(displayTab);

        jLabel5.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel5.setText("Not implemented yet");

        org.jdesktop.layout.GroupLayout annotationTabLayout = new org.jdesktop.layout.GroupLayout(annotationTab);
        annotationTab.setLayout(annotationTabLayout);
        annotationTabLayout.setHorizontalGroup(
            annotationTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, annotationTabLayout.createSequentialGroup()
                .addContainerGap(240, Short.MAX_VALUE)
                .add(jLabel5)
                .addContainerGap())
        );
        annotationTabLayout.setVerticalGroup(
            annotationTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(annotationTabLayout.createSequentialGroup()
                .add(jLabel5)
                .add(0, 378, Short.MAX_VALUE))
        );

        mainPane.addTab("Annotation", annotationTab);
        TAB_ANNOTATION = mainPane.indexOfComponent(annotationTab);

        jLabel6.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel6.setText("Not implemented yet");

        org.jdesktop.layout.GroupLayout visibleRangeTabLayout = new org.jdesktop.layout.GroupLayout(visibleRangeTab);
        visibleRangeTab.setLayout(visibleRangeTabLayout);
        visibleRangeTabLayout.setHorizontalGroup(
            visibleRangeTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(visibleRangeTabLayout.createSequentialGroup()
                .add(116, 116, 116)
                .add(jLabel6)
                .addContainerGap(130, Short.MAX_VALUE))
        );
        visibleRangeTabLayout.setVerticalGroup(
            visibleRangeTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(visibleRangeTabLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6)
                .addContainerGap(372, Short.MAX_VALUE))
        );

        mainPane.addTab("Visible Range", visibleRangeTab);
        TAB_VISIBLE_RANGE = mainPane.indexOfComponent(visibleRangeTab);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Heat Map"));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 10, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout graphTabLayout = new org.jdesktop.layout.GroupLayout(graphTab);
        graphTab.setLayout(graphTabLayout);
        graphTabLayout.setHorizontalGroup(
            graphTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(graphTabLayout.createSequentialGroup()
                .add(150, 150, 150)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(153, Short.MAX_VALUE))
        );
        graphTabLayout.setVerticalGroup(
            graphTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(graphTabLayout.createSequentialGroup()
                .add(58, 58, 58)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(277, Short.MAX_VALUE))
        );

        mainPane.addTab("Graph", graphTab);
        TAB_GRAPH = mainPane.indexOfComponent(graphTab);

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel7.setText("Not implemented yet");

        org.jdesktop.layout.GroupLayout pluginTabLayout = new org.jdesktop.layout.GroupLayout(pluginTab);
        pluginTab.setLayout(pluginTabLayout);
        pluginTabLayout.setHorizontalGroup(
            pluginTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 440, Short.MAX_VALUE)
            .add(pluginTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pluginTabLayout.createSequentialGroup()
                    .add(123, 123, 123)
                    .add(jLabel7)
                    .addContainerGap(123, Short.MAX_VALUE)))
        );
        pluginTabLayout.setVerticalGroup(
            pluginTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
            .add(pluginTabLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pluginTabLayout.createSequentialGroup()
                    .add(189, 189, 189)
                    .add(jLabel7)
                    .addContainerGap(189, Short.MAX_VALUE)))
        );

        mainPane.addTab("Plugin", pluginTab);
        TAB_DISPLAY_PLUGIN = mainPane.indexOfComponent(pluginTab);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel annotationTab;
    private javax.swing.JPanel displayTab;
    private javax.swing.JPanel graphTab;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane mainPane;
    private javax.swing.JPanel pluginTab;
    private javax.swing.JPanel visibleRangeTab;
    // End of variables declaration//GEN-END:variables

	/**
	 * Gets a JFrame containing the PreferencesView
	 */
	public JFrame getFrame() {
		if (frame == null) {
			frame = singleton;
			final Container cont = frame.getContentPane();
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				
				@Override
				public void windowClosing(WindowEvent evt) {
					// save the current size into the preferences, so the window
					// will re-open with this size next time
					PreferenceUtils.saveWindowLocation(frame, WINDOW_NAME);
					// if the TierPrefsView is being displayed, the apply any changes from it.
					// if it is not being displayed, then its changes have already been applied in componentHidden()
					
					frame.dispose();
				}
			});			
			Rectangle pos = PreferenceUtils.retrieveWindowLocation(WINDOW_NAME, new Rectangle(558, 582));
			if (pos != null) {
				PreferenceUtils.setWindowSize(frame, pos);
			}
			/*
			 * sets the Preferences window at the centre of the IGB window
			 */			
			frame.setLocationRelativeTo(igbService.getFrame());
		}
		return frame;
	}
}
