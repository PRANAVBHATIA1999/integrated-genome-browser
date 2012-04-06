/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GeneralLoadView.java
 *
 * Created on Aug 24, 2011, 11:49:48 AM
 */
package com.affymetrix.igb.view.load;

import com.affymetrix.genoviz.swing.CustomTitleBorder;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import javax.swing.JTable;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.prefs.PreferencesPanel;
import java.awt.Cursor;
import java.awt.Rectangle;

/**
 *
 * @author dcnorris
 */
public final class GeneralLoadViewGUI extends IGBTabPanel {

	private static final long serialVersionUID = 1L;
	private static final int TAB_POSITION = Integer.MIN_VALUE;
	private static DataManagementTableModel dataManagementTableModel;
	private static GeneralLoadViewGUI singleton;
	static final Cursor defaultCursor, openHandCursor, closedHandCursor;

	static {
		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		openHandCursor = new Cursor(Cursor.HAND_CURSOR);
		closedHandCursor = new Cursor(Cursor.HAND_CURSOR);
	}

	public static void init(IGBService _igbService) {
		GeneralLoadView.init(_igbService);
		singleton = new GeneralLoadViewGUI(_igbService);
	}

	public static synchronized GeneralLoadViewGUI getLoadView() {
		return singleton;
	}

	/**
	 * Creates new form GeneralLoadView
	 */
	public GeneralLoadViewGUI(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("dataAccessTab"), BUNDLE.getString("dataAccessTab"), true, TAB_POSITION);
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataManagementTableModel =GeneralLoadView.getLoadView().getTableModel();
        dataManagementTable = GeneralLoadView.getLoadView().getTable();
        partial_residuesB = GeneralLoadView.getLoadView().getPartial_residuesButton();
        refresh_dataB = GeneralLoadView.getLoadView().getRefreshDataButton();
        all_residuesB = GeneralLoadView.getLoadView().getAll_ResiduesButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tree = GeneralLoadView.getLoadView().getTree();

        jSplitPane1.setDividerLocation(270);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Management Table", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("SansSerif", 0, 13))); // NOI18N

        dataManagementTable.setModel(dataManagementTableModel);
        dataManagementTable.setRowHeight(20);    // TODO: better than the default value of 16, but still not perfect.
        // Handle sizing of the columns
        dataManagementTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);   // Allow columns to be resized
        jScrollPane2.setViewportView(dataManagementTable);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(all_residuesB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(partial_residuesB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(refresh_dataB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(all_residuesB)
                    .add(partial_residuesB)
                    .add(refresh_dataB)))
        );

        jSplitPane1.setRightComponent(jPanel2);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom Border at runtime"));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel1MouseMoved(evt);
            }
        });

        tree.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Empty");
        tree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(tree);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel1);
        jPanel1.setBorder(new CustomTitleBorder("Available Data -", "Configure  "));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

	private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
		Rectangle bounds = new Rectangle(100, 5, 70, 12);
		if (bounds.contains(evt.getX(), evt.getY())) {
			PreferencesPanel pp = PreferencesPanel.getSingleton();
			pp.setTab(PreferencesPanel.TAB_DATALOAD_PREFS);
			javax.swing.JFrame f = pp.getFrame();
			f.setVisible(true);
		}
	}//GEN-LAST:event_jPanel1MouseClicked

	private void jPanel1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseMoved
		Rectangle bounds = new Rectangle(100, 5, 70, 12);
		if (bounds.contains(evt.getX(), evt.getY())) {
			this.setCursor(openHandCursor);
		} else {
			this.setCursor(defaultCursor);
		}
	}//GEN-LAST:event_jPanel1MouseMoved
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton all_residuesB;
    private javax.swing.JTable dataManagementTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JButton partial_residuesB;
    private javax.swing.JButton refresh_dataB;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables

	@Override
	public boolean isEmbedded() {
		return true;
	}
}
