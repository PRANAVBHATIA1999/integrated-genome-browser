/*
 * AddSource.java
 *
 * Created on Dec 30, 2011, 12:26:34 PM
 */
package com.affymetrix.igb.prefs;

import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.thread.CThreadHolder;
import com.affymetrix.genometryImpl.thread.CThreadWorker;
import com.affymetrix.genometryImpl.util.ServerTypeI;
import com.affymetrix.genometryImpl.util.ServerUtils;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.shared.FileTracker;
import com.affymetrix.igb.swing.JRPButton;
import com.affymetrix.igb.swing.JRPTextField;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Point;
import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author dcnorris Modified by nick
 */
public class AddSource extends JFrame {

    private static final long serialVersionUID = 1L;

    // server type combobox will be hide in repository panel - enableCombo
    private boolean enableCombo, isEditPanel;
    private String serverURL, mirrorURL;
    private static AddSource singleton;

    public static AddSource getSingleton() {
        if (singleton == null) {
            singleton = new AddSource();
        }

        return singleton;
    }

    public AddSource() {
        initComponents();
        DocumentListener dl = new MyDocumentListener();
        nameText.getDocument().addDocumentListener(dl);
        urlText.getDocument().addDocumentListener(dl);

    }

    private void checkFieldsChange() {
        if (nameText.getText().trim().isEmpty() || urlText.getText().trim().isEmpty()) {
            addServerButton.setEnabled(false);
            return;
        }
        addServerButton.setEnabled(true);

    }

    private class MyDocumentListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            checkFieldsChange();
        }

        public void insertUpdate(DocumentEvent e) {
            checkFieldsChange();
        }

        public void removeUpdate(DocumentEvent e) {
            checkFieldsChange();
        }
    }

    public void init(boolean isEditP, boolean comboActive, String title,
            GenericServer server, String url, String mirrorurl) {
        enableCombo = comboActive;
        isEditPanel = isEditP;
        serverURL = url;
        mirrorURL = mirrorurl;

        if (isEditPanel) {
            nameText.setText(server.serverName);
            typeCombo.setSelectedItem(server.serverType);
            urlText.setText(server.URL);
            addServerButton.setText("Save Changes");
        } else {
            if (enableCombo) {
                nameText.setText("Your server name");
                typeCombo.setSelectedItem(ServerTypeI.QuickLoad);
                addServerButton.setText("Submit");
            } else {
                nameText.setText("Your repository name");
                addServerButton.setText("Submit");
            }

            urlText.setText("http://");

        }

        setTitle(title);
        resetTypeLabelAndCombo();
        display();
    }

    private void resetTypeLabelAndCombo() {
        typeLabel.setVisible(enableCombo);
        typeCombo.setVisible(enableCombo);
    }

    private void display() {
        JFrame frame = PreferencesPanel.getSingleton().getFrame();
        Point location = frame.getLocation();
        setLocation(location.x + frame.getWidth() / 2 - getWidth() / 2,
                location.y + getHeight() / 2 - getHeight() / 2);
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabelField = new javax.swing.JLabel();
        nameText = new JRPTextField("ServerPrefsView_name", "Your server nameText");
        typeLabel = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox();
        urlLabelField = new javax.swing.JLabel();
        urlText = new JRPTextField("ServerPrefsView_url", "http://");
        openDir = new JRPButton("DataLoadPrefsView_openDir", "\u2026");
        cancelButton = new javax.swing.JButton();
        addServerButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabelField.setText("Name:");

        typeLabel.setText("Type:");

        typeCombo = new JComboBox(ServerUtils.getServerTypes().toArray());
        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboActionPerformed(evt);
            }
        });

        urlLabelField.setText("URL:");

        openDir.setText("Choose local folder");
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

        addServerButton.setText("Submit");
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(openDir)
                        .add(0, 0, 0))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabelField)
                            .add(typeLabel)
                            .add(urlLabelField))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(urlText)
                            .add(typeCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(nameText)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(164, Short.MAX_VALUE)
                        .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addServerButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(8, 8, 8))
        );

        layout.linkSize(new java.awt.Component[] {addServerButton, openDir}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabelField)
                    .add(nameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(typeLabel)
                    .add(typeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabelField)
                    .add(urlText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(openDir)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addServerButton)
                    .add(cancelButton))
                .addContainerGap())
        );

        typeCombo.removeItem(ServerTypeI.LocalFiles);
        openDir.setToolTipText("Open Local Directory");

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void openDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDirActionPerformed
            File f = fileChooser(DIRECTORIES_ONLY, this);
            if (f != null && f.isDirectory()) {
                try {
                    urlText.setText(f.toURI().toURL().toString());
                } catch (MalformedURLException ex) {
                    Logger.getLogger(ServerPrefsView.class.getName()).log(Level.WARNING, "Unable to convert File '" + f.getName() + "' to URL", ex);
                }
            }
	}//GEN-LAST:event_openDirActionPerformed

	private void typeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboActionPerformed
            openDir.setEnabled(typeCombo.getSelectedItem() == ServerTypeI.QuickLoad);
	}//GEN-LAST:event_typeComboActionPerformed

	private void addServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerButtonActionPerformed

            CThreadWorker<Boolean, Void> worker;
            worker = new CThreadWorker<Boolean, Void>("Adding " + nameText.getText()) {
                @Override
                protected Boolean runInBackground() {
                    if (isEditPanel) {
                        DataLoadPrefsView.getSingleton().updateSource(serverURL,
                                (ServerTypeI) typeCombo.getSelectedItem(), nameText.getText(), urlText.getText(), mirrorURL);
                    } else {
                        if (enableCombo) {
                            return DataLoadPrefsView.getSingleton().addDataSource(
                                    (ServerTypeI) typeCombo.getSelectedItem(),
                                    nameText.getText(), urlText.getText(), -1, false, mirrorURL);
                        } else {
                            return BundleRepositoryPrefsView.getSingleton().addDataSource(null,
                                    nameText.getText(), urlText.getText(), -1, false, mirrorURL);
                        }
                    }
                    return true;
                }

                @Override
                protected void finished() {
                    boolean serverAdded = true;
                    try {
                        serverAdded = get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AddSource.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(AddSource.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (serverAdded) {
                        Application.infoPanel("<html>Your data source <b>" + nameText.getText() + "</b> is now available in <b>Data Access Tab</b> under <b>Available Data</b>.</html>", "", false);
                        //Application.confirmPanel("<html>Your data source <b>" + nameText.getText() + "</b> is now available in <b>Data Access Tab</b> under <b>Available Data</b>.</html>");//TK
                    }/*else{

                     ErrorHandler.errorPanel(
                     "Unable to Load Data Source",
                     "Unable to load " + (ServerTypeI) typeCombo.getSelectedItem() + " data source" + urlText.getText() + ".");
                     }*/

                }
            };
            CThreadHolder.getInstance().execute(evt, worker);

            this.setVisible(false);
	}//GEN-LAST:event_addServerButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
            this.setVisible(false);
	}//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
    private static javax.swing.JButton cancelButton;
    private static javax.swing.JLabel nameLabelField;
    private static javax.swing.JTextField nameText;
    private static javax.swing.JButton openDir;
    private static javax.swing.JComboBox typeCombo;
    private static javax.swing.JLabel typeLabel;
    private static javax.swing.JLabel urlLabelField;
    private static javax.swing.JTextField urlText;
    // End of variables declaration//GEN-END:variables

    protected static File fileChooser(int mode, Component parent) throws HeadlessException {
        JFileChooser chooser = new JFileChooser();
        File file;
        chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
        chooser.setFileSelectionMode(mode);
        chooser.setDialogTitle("Choose " + (mode == DIRECTORIES_ONLY ? "Directory" : "File"));
        chooser.setAcceptAllFileFilterUsed(mode != DIRECTORIES_ONLY);
        chooser.rescanCurrentDirectory();

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        file = chooser.getSelectedFile();
        FileTracker.DATA_DIR_TRACKER.setFile(file.getParentFile());
        return file;
    }
}
