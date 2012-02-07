/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AddSource.java
 *
 * Created on Dec 30, 2011, 12:26:34 PM
 */
package com.affymetrix.igb.view;

import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import java.util.logging.Logger;
import java.net.MalformedURLException;
import java.util.logging.Level;
import com.affymetrix.igb.shared.FileTracker;
import javax.swing.JFileChooser;
import java.awt.HeadlessException;
import java.awt.Component;
import com.affymetrix.genometryImpl.util.LoadUtils;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import java.io.File;
import javax.swing.JComboBox;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

/**
 *
 * @author dcnorris
 */
public class AddSource extends javax.swing.JFrame {

	private boolean enableCombo;

	/** Creates new form AddSource */
	public AddSource(boolean comboActive) {
		enableCombo = comboActive;
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

        nameLabelField = new javax.swing.JLabel();
        name = new JRPTextField("ServerPrefsView_name", "Your server name");
        typeLabelField = new javax.swing.JLabel();
        type = new javax.swing.JComboBox();
        urlLabelField = new javax.swing.JLabel();
        url = new JRPTextField("ServerPrefsView_url", "http://");
        openDir = new JRPButton("DataLoadPrefsView_openDir", "\u2026");
        cancelButton = new javax.swing.JButton();
        addServerButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabelField.setText("Name");

        typeLabelField.setText("Type");

        type = new JComboBox(LoadUtils.ServerType.values());
        type.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeActionPerformed(evt);
            }
        });

        urlLabelField.setText("URL");

        openDir.setText("...");
        openDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDirActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        addServerButton.setText("Add Server");
        addServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(typeLabelField)
                            .add(nameLabelField))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(type, 0, 320, Short.MAX_VALUE)
                            .add(name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                        .add(20, 20, 20))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addServerButton)
                        .addContainerGap())))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(32, 32, 32)
                .add(urlLabelField)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(url, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(314, Short.MAX_VALUE)
                .add(openDir)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabelField)
                    .add(name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabelField)
                    .add(url, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(typeLabelField)
                    .add(type, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(openDir)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(addServerButton)))
        );

        if (!enableCombo){
            typeLabelField.setVisible(enableCombo);
        }
        if (type != null) {
            type.removeItem(LoadUtils.ServerType.LocalFiles);
            type.setSelectedItem(LoadUtils.ServerType.QuickLoad);	// common default
        }

        if (!enableCombo){
            type.setEnabled(enableCombo);
            type.setVisible(enableCombo);
        }
        openDir.setToolTipText("Open Local Directory");
        openDir.setEnabled(type != null && type.getSelectedItem() == LoadUtils.ServerType.QuickLoad);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void openDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDirActionPerformed
		// TODO add your handling code here:
		File f = fileChooser(DIRECTORIES_ONLY, this);
		if (f != null && f.isDirectory()) {
			try {
				url.setText(f.toURI().toURL().toString());
			} catch (MalformedURLException ex) {
				Logger.getLogger(ServerPrefsView.class.getName()).log(Level.WARNING, "Unable to convert File '" + f.getName() + "' to URL", ex);
			}
		}
	}//GEN-LAST:event_openDirActionPerformed

	private void typeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeActionPerformed
		// TODO add your handling code here:
		openDir.setEnabled(type.getSelectedItem() == LoadUtils.ServerType.QuickLoad);
	}//GEN-LAST:event_typeActionPerformed

	private void addServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerButtonActionPerformed
		// TODO add your handling code here:
		if (enableCombo) {
			DataLoadPrefsView.getSingleton().addDataSource((ServerType) type.getSelectedItem(), name.getText(), url.getText());
		} else {
			BundleRepositoryPrefsView.getSingleton().addDataSource(null, name.getText(), url.getText());
		}
		this.setVisible(false);
	}//GEN-LAST:event_addServerButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
		// TODO add your handling code here:
		this.setVisible(false);
	}//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
    private static javax.swing.JButton cancelButton;
    private static javax.swing.JTextField name;
    private static javax.swing.JLabel nameLabelField;
    private static javax.swing.JButton openDir;
    private static javax.swing.JComboBox type;
    private static javax.swing.JLabel typeLabelField;
    private static javax.swing.JTextField url;
    private static javax.swing.JLabel urlLabelField;
    // End of variables declaration//GEN-END:variables

	protected static File fileChooser(int mode, Component parent) throws HeadlessException {
		JFileChooser chooser = new JFileChooser();

		chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
		chooser.setFileSelectionMode(mode);
		chooser.setDialogTitle("Choose " + (mode == DIRECTORIES_ONLY ? "Directory" : "File"));
		chooser.setAcceptAllFileFilterUsed(mode != DIRECTORIES_ONLY);
		chooser.rescanCurrentDirectory();

		if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		return chooser.getSelectedFile();
	}
}
