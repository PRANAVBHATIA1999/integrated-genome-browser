/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.plugin.manager.repos.view;

import com.affymetrix.genometry.util.FileTracker; 
import com.affymetrix.igb.swing.JRPButton;
import com.affymetrix.igb.swing.JRPTextField;
import com.google.common.base.Strings;
import java.awt.Point;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.StringUtils;
import org.lorainelab.igb.javafx.DirectoryChooserUtil;
import org.lorainelab.igb.plugin.manager.repos.PluginRepositoryList;
import org.lorainelab.igb.preferences.model.PluginRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author noorzahara
 * @author shamikakulkarni
 * 
 * IGBF-1902 - Add Edit Button to edit the URL/ Name.
 * IGBF-2012 - Improve Edit App repository function.
 * The file is now created using GUI Builder and keeping AddBundleRepositoryFrame.java as a reference
 * 
 */
public class EditBundleRepositoryFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final String REPOSITORY_XML_FILE_PATH = "repository.xml";

    private static final Logger logger = LoggerFactory.getLogger(EditBundleRepositoryFrame.class);
    private JPanel parent;
    private PluginRepository currentRepo;
    private PluginRepositoryList pluginRepositoryList;
    
    public EditBundleRepositoryFrame(JPanel parent, PluginRepositoryList pluginRepositoryList) {
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
    
    public void init(PluginRepository pluginRepository) {
        currentRepo = pluginRepository;
        setTitle("Edit Plugin Repository");
        nameText.setText(pluginRepository.getName());
        urlText.setText(pluginRepository.getUrl());
        addServerButton.setText("Save Changes");
        errorMsg.setText("");
        errorMsg.setVisible(false);
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
        cancelButton = new javax.swing.JButton();
        addServerButton = new javax.swing.JButton();
        openDir = new JRPButton("DataLoadPrefsView_openDir", "\u2026");
        errorMsg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabelField.setText("Name:");

        urlLabelField.setText("URL:");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        addServerButton.setText("Save Changes");
        addServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerButtonActionPerformed(evt);
            }
        });

        openDir.setText("Choose local folder");
        openDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDirActionPerformed(evt);
            }
        });

        errorMsg.setIconTextGap(1);
        errorMsg.setInheritsPopupMenu(false);
        errorMsg.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(openDir))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabelField)
                            .addComponent(urlLabelField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(urlText)
                            .addComponent(nameText)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap(32, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(errorMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addServerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(8, 8, 8))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabelField)
                    .addComponent(nameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabelField)
                    .addComponent(urlText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(openDir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(errorMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addServerButton)
                    .addComponent(cancelButton))
                .addGap(13, 13, 13))
        );

        openDir.setToolTipText("Open Local Directory");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void addServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerButtonActionPerformed
        // TODO add your handling code here:
        if (!isValidRepositoryUrl(urlText.getText())) {
            return;
        }
        currentRepo.setName(nameText.getText());
        currentRepo.setUrl(urlText.getText());
        pluginRepositoryList.updatePluginRepoPrefs(currentRepo);
        this.setVisible(false);
    }//GEN-LAST:event_addServerButtonActionPerformed

    private void openDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDirActionPerformed
        // TODO add your handling code here:
        File f = dirChooser();
        if (f != null && f.isDirectory()) {
            try {
                urlText.setText(f.toURI().toURL().toString());
            } catch (MalformedURLException ex) {
                //
            }
        }
    }//GEN-LAST:event_openDirActionPerformed

    
     protected static File dirChooser() {
        // IGBF-1157: Changing file/directory chooser style to Native OS file chooser. 
        FileTracker fileTracker = FileTracker.DATA_DIR_TRACKER;
        File dir = null;
        Optional<File> selectedDir = DirectoryChooserUtil.build()
                .setContext(fileTracker.getFile())
                .setTitle("Choose local folder")
                .retrieveDirFromFxChooser();
        
        if (selectedDir.isPresent()) {
            dir = selectedDir.get();
            fileTracker.setFile(dir.getParentFile());
        }
        
        return dir;
    }
     
    /**
     * The function checks if the given string is a valid url.
     * 
     * @param url
     * @return 
     */
     
    private boolean isValidRepositoryUrl(String url) {
        try {
                String repositoryXmlUrl =  (url.lastIndexOf("/") == (url.length()-1)) ? 
                        url + REPOSITORY_XML_FILE_PATH : url + "/" + REPOSITORY_XML_FILE_PATH;
                
            if (isLocalFile(new URI(repositoryXmlUrl))){
                File file = new File(new URL(repositoryXmlUrl).getFile());
                if (!file.exists()) {
                    throw new Exception("File not Found!");
                }
                return true;
            }
            
            int responseCode = 0;
            if ( repositoryXmlUrl.contains("https")) {
               HttpsURLConnection con = (HttpsURLConnection) new URL(repositoryXmlUrl).openConnection();
               responseCode = con.getResponseCode();
            } else {
               HttpURLConnection con = (HttpURLConnection) new URL(repositoryXmlUrl).openConnection();
               responseCode = con.getResponseCode();
            }
            
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                 throw new Exception("Malformed URL.");
            }
            
            return true;
        } catch ( Exception e) {
            this.setVisible(true);
            logger.debug("Exception has occured", e);
            showTextMessage("<html>The changes made seem to have a problem...<BR>Please try Again!</html>");
            return false;
        }
    }
     
     private void showTextMessage(String message) {
            errorMsg.setVisible(true);
            errorMsg.setText(message);
	}
     
     /**
     * The function checks if the given uri contains a local or a remote path.
     * @param uri
     * @return 
     */
    private static boolean isLocalFile(URI uri) {
        String scheme = uri.getScheme();
        return StringUtils.equalsIgnoreCase(scheme, "file");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addServerButton;
    private static javax.swing.JButton cancelButton;
    private javax.swing.JLabel errorMsg;
    private static javax.swing.JLabel nameLabelField;
    private static javax.swing.JTextField nameText;
    private static javax.swing.JButton openDir;
    private static javax.swing.JLabel urlLabelField;
    private static javax.swing.JTextField urlText;
    // End of variables declaration//GEN-END:variables
}
