package com.affymetrix.igb.view;

import com.affymetrix.genoviz.swing.CustomTitleBorder;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.shared.JRPStyledTable;
import java.awt.Cursor;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

public class SeqGroupViewGUI extends IGBTabPanel {

	private static final long serialVersionUID = 1L;
	private static final int TAB_POSITION = 7;
	private final JRPStyledTable seqtable;
	private static SeqGroupViewGUI singleton;
	private SeqGroupView seqGroupModel;
	static final Cursor defaultCursor, openHandCursor, closedHandCursor;

	static {
		defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		openHandCursor = new Cursor(Cursor.HAND_CURSOR);
		closedHandCursor = new Cursor(Cursor.HAND_CURSOR);
	}
	public static void init(IGBService _igbService) {
		singleton = new SeqGroupViewGUI(_igbService);
	}

	public static SeqGroupViewGUI getInstance() {
		return singleton;
	}

	private SeqGroupViewGUI(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("genomeTab"), BUNDLE.getString("genomeTab"), true, TAB_POSITION);

		SeqGroupView.init(_igbService);
	    seqGroupModel = SeqGroupView.getInstance();

		seqtable = seqGroupModel.getTable();

		JScrollPane scroller = new JScrollPane(seqtable);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(scroller);

		initComponents();
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(220, 50);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(220, 50);
	}

	@Override
	public TabState getDefaultState() {
		return TabState.COMPONENT_STATE_RIGHT_TAB;
	}

	@Override
	public boolean isEmbedded() {
		return true;
	}

	@Override
	public boolean isCheckMinimumWindowSize() {
		return true;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        currentSequenceTable = seqtable;
        speciesPanel = new javax.swing.JPanel();
        speciesCB = seqGroupModel.getSpeciesCB();
        genomeVersionPanel = new javax.swing.JPanel();
        versionCB = seqGroupModel.getVersionCB();

        currentSequenceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(currentSequenceTable);

        speciesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom Border at Runtime"));
        speciesPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speciesPanelMousePressed(evt);
            }
        });
        speciesPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                speciesPanelMouseMoved(evt);
            }
        });

        speciesCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speciesCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout speciesPanelLayout = new org.jdesktop.layout.GroupLayout(speciesPanel);
        speciesPanel.setLayout(speciesPanelLayout);
        speciesPanelLayout.setHorizontalGroup(
            speciesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(speciesCB, 0, 182, Short.MAX_VALUE)
        );
        speciesPanelLayout.setVerticalGroup(
            speciesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(speciesCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        genomeVersionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Genome Version"));

        org.jdesktop.layout.GroupLayout genomeVersionPanelLayout = new org.jdesktop.layout.GroupLayout(genomeVersionPanel);
        genomeVersionPanel.setLayout(genomeVersionPanelLayout);
        genomeVersionPanelLayout.setHorizontalGroup(
            genomeVersionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(versionCB, 0, 182, Short.MAX_VALUE)
        );
        genomeVersionPanelLayout.setVerticalGroup(
            genomeVersionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(versionCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(speciesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(genomeVersionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(speciesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(genomeVersionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))
        );

        speciesPanel.setBorder(new CustomTitleBorder("  ", "Species"));
    }// </editor-fold>//GEN-END:initComponents

	private void speciesPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speciesPanelMousePressed
		Rectangle bounds = new Rectangle(10, 5, 50, 12);
		if (bounds.contains(evt.getX(), evt.getY())) {
			seqGroupModel.getSpeciesCB().setSelectedItem(SeqGroupView.SELECT_SPECIES);
		}// TODO add your handling code here:
	}//GEN-LAST:event_speciesPanelMousePressed

	private void speciesPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speciesPanelMouseMoved
		Rectangle bounds = new Rectangle(10, 5, 50, 12);
		if (bounds.contains(evt.getX(), evt.getY())) {
			this.setCursor(openHandCursor);
		} else {
			this.setCursor(defaultCursor);
		}
	}//GEN-LAST:event_speciesPanelMouseMoved

	private void speciesCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speciesCBActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_speciesCBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable currentSequenceTable;
    private javax.swing.JPanel genomeVersionPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox speciesCB;
    private javax.swing.JPanel speciesPanel;
    private javax.swing.JComboBox versionCB;
    // End of variables declaration//GEN-END:variables
}
