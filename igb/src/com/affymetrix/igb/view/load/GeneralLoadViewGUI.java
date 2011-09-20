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

import static com.affymetrix.igb.IGBConstants.BUNDLE;
import javax.swing.JTable;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
/**
 *
 * @author dcnorris
 */
public final class GeneralLoadViewGUI extends IGBTabPanel {

	private static final long serialVersionUID = 1L;
	private static final int TAB_POSITION = Integer.MIN_VALUE;
	private static DataManagementTableModel dataManagementTableModel;
	private static GeneralLoadViewGUI singleton;

	public static void init(IGBService _igbService) {
		GeneralLoadView.init(_igbService);
		singleton = new GeneralLoadViewGUI(_igbService);
	}

	public static synchronized GeneralLoadViewGUI getLoadView() {
		return singleton;
	}

	/** Creates new form GeneralLoadView */
	public GeneralLoadViewGUI(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("dataAccessTab"), BUNDLE.getString("dataAccessTab"), true, TAB_POSITION);
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tree = GeneralLoadView.getLoadView().getTree();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataManagementTableModel =GeneralLoadView.getLoadView().getLoadModeTableModel();
        dataManagementTable = GeneralLoadView.getLoadView().getDataManagementTable();
        partial_residuesB = GeneralLoadView.getLoadView().getPartial_residuesButton();
        refresh_dataB = GeneralLoadView.getLoadView().getRefreshDataButton();
        all_residuesB = GeneralLoadView.getLoadView().getAll_ResiduesButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Sources and Data Sets"));

        tree.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jScrollPane1.setViewportView(tree);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Data Management Table"));

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
                .add(all_residuesB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(partial_residuesB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(refresh_dataB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .add(9, 9, 9)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(all_residuesB)
                    .add(partial_residuesB)
                    .add(refresh_dataB)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton all_residuesB;
    private javax.swing.JTable dataManagementTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton partial_residuesB;
    private javax.swing.JButton refresh_dataB;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables
	@Override
	public boolean isEmbedded() {
		return true;
	}
}
