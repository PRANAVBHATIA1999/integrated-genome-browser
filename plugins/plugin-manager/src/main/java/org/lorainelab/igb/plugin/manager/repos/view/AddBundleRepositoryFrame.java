/*
 * AddSource.java
 *
 * Created on Dec 30, 2011, 12:26:34 PM
 */
package org.lorainelab.igb.plugin.manager.repos.view;

import com.affymetrix.common.PreferenceUtils;
import com.affymetrix.genometry.util.FileTracker;
import com.affymetrix.igb.swing.JRPButton;
import com.affymetrix.igb.swing.JRPTextField;
import com.google.common.base.Strings;
import org.lorainelab.igb.plugin.manager.repos.PluginRepositoryList;
import org.lorainelab.igb.preferences.model.PluginRepository;
import org.lorainelab.igb.javafx.DirectoryChooserUtil;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Point;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AddBundleRepositoryFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private boolean isEditPanel;
    private JPanel parent;
    private PluginRepository currentRepo;
    private PluginRepositoryList pluginRepositoryList;

    public AddBundleRepositoryFrame(JPanel parent, PluginRepositoryList pluginRepositoryList) {
        this.parent = parent;
        this.pluginRepositoryList = pluginRepositoryList;
        initComponents();
        DocumentListener dl = new MyDocumentListener();
        nameText.getDocument().addDocumentListener(dl);
        urlText.getDocument().addDocumentListener(dl);

    }

    private void checkFieldsChange() {
        if (Strings.isNullOrEmpty(nameText.getText()) || Strings.isNullOrEmpty(urlText.getText())) {
            addServerButton.setEnabled(false);
            return;
        }
        addServerButton.setEnabled(true);

    }

    private class MyDocumentListener implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkFieldsChange();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkFieldsChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkFieldsChange();
        }
    }

    public void init(boolean isEditP, PluginRepository pluginRepository) {
        currentRepo = pluginRepository;
        isEditPanel = isEditP;
        if (isEditPanel) {
            setTitle("Edit Plugin Repository");
            nameText.setText(pluginRepository.getName());
            urlText.setText(pluginRepository.getUrl());
            addServerButton.setText("Save Changes");
        } else {
            setTitle("Add Plugin Repository");
            nameText.setText("Your repository name");
            addServerButton.setText("Submit");
            urlText.setText("http://");
        }
        display();
    }

    private void display() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(parent);
        Point location = topFrame.getLocation();
        setLocation(location.x + topFrame.getWidth() / 2 - getWidth() / 2,
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
        urlLabelField = new javax.swing.JLabel();
        urlText = new JRPTextField("ServerPrefsView_url", "http://");
        openDir = new JRPButton("DataLoadPrefsView_openDir", "\u2026");
        cancelButton = new javax.swing.JButton();
        addServerButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabelField.setText("Name:");

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
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabelField)
                            .add(urlLabelField))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(urlText)
                            .add(nameText)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, openDir)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(addServerButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
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
                .add(28, 28, 28)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabelField)
                    .add(urlText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(openDir)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 18, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addServerButton)
                    .add(cancelButton))
                .addContainerGap())
        );

        openDir.setToolTipText("Open Local Directory");

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void openDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDirActionPerformed
        // IGBF-1157: Changing directory chooser window style
        File f = dirChooser();
        if (f != null && f.isDirectory()) {
            try {
                urlText.setText(f.toURI().toURL().toString());
            } catch (MalformedURLException ex) {
                //
            }
        }
	}//GEN-LAST:event_openDirActionPerformed

	private void addServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerButtonActionPerformed
        if (isEditPanel) {
            currentRepo.setName(nameText.getText());
            currentRepo.setUrl(urlText.getText());
        } else {
            PluginRepository pluginRepository = new PluginRepository();
            pluginRepository.setName(nameText.getText());
            pluginRepository.setUrl(urlText.getText());
            pluginRepository.setEnabled(true);
            pluginRepository.setDefault(Boolean.toString(Boolean.FALSE));
            pluginRepositoryList.addPluginRepository(pluginRepository);
        }

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
    private static javax.swing.JLabel urlLabelField;
    private static javax.swing.JTextField urlText;
    // End of variables declaration//GEN-END:variables

    protected static File fileChooser(int mode, Component parent) throws HeadlessException {
        JFileChooser chooser = new JFileChooser();
        File file;
        // chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
        chooser.setFileSelectionMode(mode);
        chooser.setDialogTitle("Choose " + (mode == DIRECTORIES_ONLY ? "Directory" : "File"));
        chooser.setAcceptAllFileFilterUsed(mode != DIRECTORIES_ONLY);
        chooser.rescanCurrentDirectory();

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        file = chooser.getSelectedFile();
        // FileTracker.DATA_DIR_TRACKER.setFile(file.getParentFile());
        return file;
    }
       
    protected static File dirChooser() {
        // IGBF-1157: Changing file/directory chooser style to Native OS file chooser. 
        FileTracker fileTracker = FileTracker.DATA_DIR_TRACKER;
        File dir = null;
        Optional<File> selectedDir = DirectoryChooserUtil.build()
                .setContext(fileTracker.getFile())
                .setTitle("Choose Local Folder")
                .retrieveDirFromFxChooser();
        
        if (selectedDir.isPresent()) {
            dir = selectedDir.get();
            fileTracker.setFile(dir.getParentFile());
        }
        
        return dir;
    }

    private void infoPanel(final String message, final String check, final boolean def_val) {

        final JCheckBox checkbox = new JCheckBox("Do not show this message again.");
        final Object[] params = new Object[]{message, checkbox};
        final Preferences node = PreferenceUtils.getTopNode();

        //If all parameters are provided then look up for boolean value from preference.
        final boolean b = node.getBoolean(check, def_val);

        //If user has already set preference then return true.
        if (b != def_val) {
            return;
        }

        JOptionPane.showMessageDialog(this, params, "IGB", JOptionPane.INFORMATION_MESSAGE);

        if (checkbox.isSelected()) {
            node.putBoolean(check, checkbox.isSelected() != b);
        }
    }
}
