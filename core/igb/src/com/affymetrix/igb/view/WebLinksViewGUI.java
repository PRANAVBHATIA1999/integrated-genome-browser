package com.affymetrix.igb.view;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.util.DisplayUtils;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.IGB;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A Web links View UI class for IGB. It is generated by Netbeans GUI Builder.
 * All the function codes are implemented in WebLinksView class.
 *
 * @author nick
 */
public class WebLinksViewGUI extends JPanel {
	private static final long serialVersionUID = 1L;
	public static WebLinksView wlv;
	public static JFrame static_frame = null;
	private static WebLinksViewGUI singleton;

	public static synchronized WebLinksViewGUI getSingleton() {
		if (singleton == null) {
			singleton = new WebLinksViewGUI();
		}
		return singleton;
	}

	public synchronized JFrame displayPanel() {
		if (static_frame == null) {
			static_frame = PreferenceUtils.createFrame("Web Links Tool",
					getSingleton());
			static_frame.setLocationRelativeTo(IGB.getSingleton().getFrame());
		}
		DisplayUtils.bringFrameToFront(static_frame);
		return static_frame;
	}

	/**
	 * Creates new form WebLinksViewGUI
	 */
	public WebLinksViewGUI() {
		wlv = WebLinksView.getSingleton();

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

        localPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        localTable = wlv.localTable;
        createButton = new javax.swing.JButton();
        deleteButton = wlv.deleteButton;
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        builderPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = wlv.nameTextField;
        urlLabel = new javax.swing.JLabel();
        nameRadioButton = wlv.nameRadioButton;
        idRadioButton = wlv.idRadioButton;
        urlTextField = wlv.urlTextField;
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        startWithTextField = wlv.startWithTextField;
        jLabel5 = new javax.swing.JLabel();
        endWithTextField = wlv.endWithTextField;
        jLabel6 = new javax.swing.JLabel();
        containsTextField = wlv.containsTextField;
        ignoreCaseCheckBox = wlv.ignoreCaseCheckBox;
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        regexTextField = wlv.regexTextField;
        regularExpressionTip = new javax.swing.JLabel();
        defaultPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        defaultTable = wlv.serverTable;

        localPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom Web Links - Click To Edit"));

        localTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                localTableMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(localTable);

        createButton.setText("Create New");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        builderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Web Link Builder"));

        nameLabel.setText("Name:");

        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
            }
        });

        urlLabel.setText("URL Pattern:");

        nameRadioButton.setText("Track Name");
        nameRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameRadioButtonActionPerformed(evt);
            }
        });

        idRadioButton.setText("Annotation ID");
        idRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idRadioButtonActionPerformed(evt);
            }
        });

        urlTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                urlTextFieldKeyReleased(evt);
            }
        });

        jLabel1.setText("Regular Expression Matches:");

        jTabbedPane1.setToolTipText("");

        jPanel1.setPreferredSize(new java.awt.Dimension(575, 50));

        jLabel4.setText("Start With");

        startWithTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                startWithTextFieldKeyReleased(evt);
            }
        });

        jLabel5.setText("End With");

        endWithTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                endWithTextFieldKeyReleased(evt);
            }
        });

        jLabel6.setText("Contains");

        containsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                containsTextFieldKeyReleased(evt);
            }
        });

        ignoreCaseCheckBox.setSelected(true);
        ignoreCaseCheckBox.setText("Ignore Case");
        ignoreCaseCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreCaseCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel4)
                        .add(18, 18, 18)
                        .add(startWithTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 168, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel5)
                        .add(18, 18, 18)
                        .add(endWithTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 168, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel6)
                        .add(24, 24, 24)
                        .add(containsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 168, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(ignoreCaseCheckBox)))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel5)
                        .add(endWithTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel4)
                        .add(startWithTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(containsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ignoreCaseCheckBox))
                .add(42, 42, 42))
        );

        jTabbedPane1.addTab("Build Web Link", null, jPanel1, "Build web link regular expression");

        jLabel2.setText("Regular Expression");

        regexTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                regexTextFieldKeyReleased(evt);
            }
        });

        regularExpressionTip.setToolTipText("Click to the instruction page.");
        regularExpressionTip.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/info.png"));
        regularExpressionTip.setText(" ");
        regularExpressionTip.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                regularExpressionTipMouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                regularExpressionTipMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                regularExpressionTipMouseEntered(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(regexTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 498, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(regularExpressionTip, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel2))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(regexTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(regularExpressionTip))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Advanced", null, jPanel2, "Expert user to enter regular expression");

        org.jdesktop.layout.GroupLayout builderPanelLayout = new org.jdesktop.layout.GroupLayout(builderPanel);
        builderPanel.setLayout(builderPanelLayout);
        builderPanelLayout.setHorizontalGroup(
            builderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(builderPanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(builderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(urlLabel)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(builderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(builderPanelLayout.createSequentialGroup()
                        .add(urlTextField)
                        .add(80, 80, 80))
                    .add(builderPanelLayout.createSequentialGroup()
                        .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 177, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .add(builderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(nameRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(idRadioButton)
                .add(0, 0, Short.MAX_VALUE))
            .add(jTabbedPane1)
        );
        builderPanelLayout.setVerticalGroup(
            builderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(builderPanelLayout.createSequentialGroup()
                .add(builderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .add(5, 5, 5)
                .add(builderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(builderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(nameRadioButton)
                    .add(idRadioButton)))
        );

        org.jdesktop.layout.GroupLayout localPanelLayout = new org.jdesktop.layout.GroupLayout(localPanel);
        localPanel.setLayout(localPanelLayout);
        localPanelLayout.setHorizontalGroup(
            localPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, localPanelLayout.createSequentialGroup()
                .add(createButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(deleteButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(importButton)
                .add(0, 0, 0)
                .add(exportButton))
            .add(builderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        localPanelLayout.setVerticalGroup(
            localPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(localPanelLayout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(localPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(exportButton)
                    .add(importButton)
                    .add(createButton)
                    .add(deleteButton))
                .add(10, 10, 10)
                .add(builderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        defaultPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Server Provided Web Link"));

        defaultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                defaultTableMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(defaultTable);

        org.jdesktop.layout.GroupLayout defaultPanelLayout = new org.jdesktop.layout.GroupLayout(defaultPanel);
        defaultPanel.setLayout(defaultPanelLayout);
        defaultPanelLayout.setHorizontalGroup(
            defaultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2)
        );
        defaultPanelLayout.setVerticalGroup(
            defaultPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(defaultPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, localPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(defaultPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(localPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void nameRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameRadioButtonActionPerformed
		wlv.nameRadioButton();
	}//GEN-LAST:event_nameRadioButtonActionPerformed

	private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
		wlv.add();
	}//GEN-LAST:event_createButtonActionPerformed

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
		wlv.delete(wlv.localTable);
	}//GEN-LAST:event_deleteButtonActionPerformed

	private void idRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idRadioButtonActionPerformed
		wlv.idRadioButton();
	}//GEN-LAST:event_idRadioButtonActionPerformed

	private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
		wlv.importWebLinks();
	}//GEN-LAST:event_importButtonActionPerformed

	private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
		wlv.exportWebLinks();
	}//GEN-LAST:event_exportButtonActionPerformed

	private void localTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_localTableMousePressed
		if (defaultTable.getSelectedRow() != -1
				&& localTable.getSelectedRow() != -1) {
			defaultTable.removeRowSelectionInterval(0, defaultTable.getRowCount() - 1);
		}
	}//GEN-LAST:event_localTableMousePressed

	private void defaultTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultTableMousePressed
		if (defaultTable.getSelectedRow() != -1
				&& localTable.getSelectedRow() != -1) {
			localTable.removeRowSelectionInterval(0, localTable.getRowCount() - 1);
		}
	}//GEN-LAST:event_defaultTableMousePressed

	private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
		wlv.nameTextFieldKeyReleased();
	}//GEN-LAST:event_nameTextFieldKeyReleased

	private void urlTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urlTextFieldKeyReleased
		wlv.urlTextField();
	}//GEN-LAST:event_urlTextFieldKeyReleased

	private void regularExpressionTipMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regularExpressionTipMouseReleased
		wlv.regexTipMouseReleased();
	}//GEN-LAST:event_regularExpressionTipMouseReleased

	private void regularExpressionTipMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regularExpressionTipMouseEntered
		setCursor(WebLinksView.handCursor);
	}//GEN-LAST:event_regularExpressionTipMouseEntered

	private void regularExpressionTipMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regularExpressionTipMouseExited
		setCursor(WebLinksView.defaultCursor);
	}//GEN-LAST:event_regularExpressionTipMouseExited

	private void ignoreCaseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreCaseCheckBoxActionPerformed
		wlv.ignoreCaseCheckBoxStateChanged();
	}//GEN-LAST:event_ignoreCaseCheckBoxActionPerformed

    private void regexTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_regexTextFieldKeyReleased
		wlv.regexTextFieldKeyReleased();
    }//GEN-LAST:event_regexTextFieldKeyReleased

	private void startWithTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_startWithTextFieldKeyReleased
		wlv.composeRegex();
	}//GEN-LAST:event_startWithTextFieldKeyReleased

	private void endWithTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_endWithTextFieldKeyReleased
		wlv.composeRegex();
	}//GEN-LAST:event_endWithTextFieldKeyReleased

	private void containsTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_containsTextFieldKeyReleased
		wlv.composeRegex();
	}//GEN-LAST:event_containsTextFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel builderPanel;
    private javax.swing.JTextField containsTextField;
    private javax.swing.JButton createButton;
    private javax.swing.JPanel defaultPanel;
    private javax.swing.JTable defaultTable;
    private javax.swing.JButton deleteButton;
    private javax.swing.JTextField endWithTextField;
    private javax.swing.JButton exportButton;
    private javax.swing.JRadioButton idRadioButton;
    private javax.swing.JCheckBox ignoreCaseCheckBox;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel localPanel;
    private javax.swing.JTable localTable;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JRadioButton nameRadioButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTextField regexTextField;
    private javax.swing.JLabel regularExpressionTip;
    private javax.swing.JTextField startWithTextField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
