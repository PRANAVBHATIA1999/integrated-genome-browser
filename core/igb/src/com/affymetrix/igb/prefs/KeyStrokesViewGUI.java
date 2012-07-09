/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * KeyStrokesViewGUI.java
 *
 * Created on Jan 19, 2012, 10:11:49 AM
 */
package com.affymetrix.igb.prefs;

/**
 *
 * @author lorainelab
 */
public class KeyStrokesViewGUI extends IPrefEditorComponent {
	private static final long serialVersionUID = 1L;
	private static KeyStrokesViewGUI singleton;
	private static KeyStrokesView ksv;

	public static synchronized KeyStrokesViewGUI getSingleton() {
		if (singleton == null) {
			return singleton = new KeyStrokesViewGUI();
		}
		return singleton;
	}

	/** Creates new form KeyStrokesViewGUI */
	private KeyStrokesViewGUI() {
		ksv = KeyStrokesView.getSingleton();
		validate();
		this.setName("Shortcuts");
		this.setToolTipText("Edit Locations");
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = ksv.table;
        jLabel1 = ksv.edit_panel.note_label;

        jTable1.setModel(ksv.table.getModel());
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setForeground(new java.awt.Color(255, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(189, Short.MAX_VALUE)
                .add(jLabel1)
                .add(82, 82, 82))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

	public void refresh() {
		ksv.invokeRefreshTable();
	}

	private void setDefaultCloseOperation(int i) {
		// do nothing
	}

	private void pack() {
		//do nothing
	}

	private IPrefEditorComponent getContentPane() {
		return this;
	}
}
