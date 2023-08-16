package org.lorainelab.igb.preferences.weblink.view;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometry.util.DisplayUtils;
import org.lorainelab.igb.services.IgbService;
import java.awt.Point;
import javax.swing.JFrame;
import org.lorainelab.igb.preferences.weblink.view.WebLinkDisplayProvider;

/**
 *
 * @author dcnorris
 */
@Component(name = WebLinksViewFrame.COMPONENT_NAME, immediate = true, provide = WebLinkDisplayProvider.class)
public class WebLinksViewFrame extends JFrame implements WebLinkDisplayProvider {

    public static final String COMPONENT_NAME = "WebLinksViewFrame";
    private static final long serialVersionUID = 1L;
    public static WebLinksView wlv;
    private IgbService igbService;

    /**
     * Creates new form WebLinksViewFrame
     */
    public WebLinksViewFrame() {
        wlv = new WebLinksView();
        initComponents();
    }

    @Reference(optional = false)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Override
    public void displayPanel() {
        JFrame topFrame = igbService.getApplicationFrame();
        Point location = topFrame.getLocation();
        setLocation(location.x + topFrame.getWidth() / 2 - getWidth() / 2,
                location.y + getHeight() / 2 - getHeight() / 2);
        DisplayUtils.bringFrameToFront(this);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
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
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        startWithTextField = wlv.startWithTextField;
        jLabel5 = new javax.swing.JLabel();
        endWithTextField = wlv.endWithTextField;
        jLabel6 = new javax.swing.JLabel();
        containsTextField = wlv.containsTextField;
        ignoreCaseCheckBox = wlv.ignoreCaseCheckBox;
        jPanel3 = new javax.swing.JPanel();
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

        jPanel2.setPreferredSize(new java.awt.Dimension(575, 50));

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(startWithTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(endWithTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(24, 24, 24)
                        .addComponent(containsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ignoreCaseCheckBox)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(endWithTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(startWithTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(containsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ignoreCaseCheckBox))
                .addGap(42, 42, 42))
        );

        jTabbedPane1.addTab("Build Web Link", null, jPanel2, "Build web link regular expression");

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(regexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(regularExpressionTip, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(regexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(regularExpressionTip))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Advanced", null, jPanel3, "Expert user to enter regular expression");

        javax.swing.GroupLayout builderPanelLayout = new javax.swing.GroupLayout(builderPanel);
        builderPanel.setLayout(builderPanelLayout);
        builderPanelLayout.setHorizontalGroup(
            builderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(builderPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(builderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(urlLabel)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(builderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(builderPanelLayout.createSequentialGroup()
                        .addComponent(urlTextField)
                        .addGap(80, 80, 80))
                    .addGroup(builderPanelLayout.createSequentialGroup()
                        .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(builderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nameRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(idRadioButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        builderPanelLayout.setVerticalGroup(
            builderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(builderPanelLayout.createSequentialGroup()
                .addGroup(builderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addGap(5, 5, 5)
                .addGroup(builderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(builderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameRadioButton)
                    .addComponent(idRadioButton)))
        );

        javax.swing.GroupLayout localPanelLayout = new javax.swing.GroupLayout(localPanel);
        localPanel.setLayout(localPanelLayout);
        localPanelLayout.setHorizontalGroup(
            localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, localPanelLayout.createSequentialGroup()
                .addComponent(createButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(deleteButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(importButton)
                .addGap(0, 0, 0)
                .addComponent(exportButton))
            .addComponent(builderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        localPanelLayout.setVerticalGroup(
            localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(localPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(localPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(importButton)
                    .addComponent(createButton)
                    .addComponent(deleteButton))
                .addGap(10, 10, 10)
                .addComponent(builderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        defaultPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Server Provided Web Link"));

        defaultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                defaultTableMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(defaultTable);

        javax.swing.GroupLayout defaultPanelLayout = new javax.swing.GroupLayout(defaultPanel);
        defaultPanel.setLayout(defaultPanelLayout);
        defaultPanelLayout.setHorizontalGroup(
            defaultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
        );
        defaultPanelLayout.setVerticalGroup(
            defaultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(defaultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(localPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(defaultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(localPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 632, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 622, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void localTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_localTableMousePressed
        if (defaultTable.getSelectedRow() != -1
                && localTable.getSelectedRow() != -1) {
            defaultTable.removeRowSelectionInterval(0, defaultTable.getRowCount() - 1);
        }
    }//GEN-LAST:event_localTableMousePressed

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        wlv.add();
    }//GEN-LAST:event_createButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        wlv.delete(wlv.localTable, this);
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        wlv.importWebLinks();
    }//GEN-LAST:event_importButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        wlv.exportWebLinks();
    }//GEN-LAST:event_exportButtonActionPerformed

    private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
        wlv.nameTextFieldKeyReleased();
    }//GEN-LAST:event_nameTextFieldKeyReleased

    private void nameRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameRadioButtonActionPerformed
        wlv.nameRadioButton();
    }//GEN-LAST:event_nameRadioButtonActionPerformed

    private void idRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idRadioButtonActionPerformed
        wlv.idRadioButton();
    }//GEN-LAST:event_idRadioButtonActionPerformed

    private void urlTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urlTextFieldKeyReleased
        wlv.urlTextField();
    }//GEN-LAST:event_urlTextFieldKeyReleased

    private void startWithTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_startWithTextFieldKeyReleased
        wlv.composeRegex();
    }//GEN-LAST:event_startWithTextFieldKeyReleased

    private void endWithTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_endWithTextFieldKeyReleased
        wlv.composeRegex();
    }//GEN-LAST:event_endWithTextFieldKeyReleased

    private void containsTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_containsTextFieldKeyReleased
        wlv.composeRegex();
    }//GEN-LAST:event_containsTextFieldKeyReleased

    private void ignoreCaseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreCaseCheckBoxActionPerformed
        wlv.ignoreCaseCheckBoxStateChanged();
    }//GEN-LAST:event_ignoreCaseCheckBoxActionPerformed

    private void regexTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_regexTextFieldKeyReleased
        wlv.regexTextFieldKeyReleased();
    }//GEN-LAST:event_regexTextFieldKeyReleased

    private void regularExpressionTipMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regularExpressionTipMouseReleased
        wlv.regexTipMouseReleased();
    }//GEN-LAST:event_regularExpressionTipMouseReleased

    private void regularExpressionTipMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regularExpressionTipMouseExited
        setCursor(WebLinksView.defaultCursor);
    }//GEN-LAST:event_regularExpressionTipMouseExited

    private void regularExpressionTipMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regularExpressionTipMouseEntered
        setCursor(WebLinksView.handCursor);
    }//GEN-LAST:event_regularExpressionTipMouseEntered

    private void defaultTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_defaultTableMousePressed
        if (defaultTable.getSelectedRow() != -1
                && localTable.getSelectedRow() != -1) {
            localTable.removeRowSelectionInterval(0, localTable.getRowCount() - 1);
        }
    }//GEN-LAST:event_defaultTableMousePressed

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
    private javax.swing.JPanel jPanel3;
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
