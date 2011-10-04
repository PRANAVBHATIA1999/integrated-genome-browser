/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * QuickloadServerRootExisting.java
 *
 * Created on Sep 27, 2011, 1:02:50 PM
 */
package edu.uncc.bioinformatics.quickloadbuilder;

import java.awt.Color;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author jfvillal
 */
public class QuickloadServerRootExisting extends javax.swing.JPanel {
    QuickLoadArchiveBuilderView Parent;
    String DefaultPath ;
    /** Creates new form QuickloadServerRootExisting */
    public QuickloadServerRootExisting(  QuickLoadArchiveBuilderView parent ) {
        Initialized = false;
        Parent = parent;
        initComponents();
        ResourceMap resourceMap = parent.getResourceMap();
        Logo.setIcon( resourceMap.getIcon( "WelcomeIcon" ) );
        
        DefaultPath = System.getProperty("user.home") + System.getProperty("file.separator" )  + QuickloadServerRoot.QUICKLOAD;
        
        QuickloadPath.setText( DefaultPath );
        
        UseDefault.setSelected(validatePath());
        
        onDefaultChanged();
        Initialized = true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        QuickloadPath = new javax.swing.JTextField();
        Browse = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        UseDefault = new javax.swing.JCheckBox();
        ErrorPathLabel = new javax.swing.JLabel();
        ArchiveName = new javax.swing.JLabel();
        Logo = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edu.uncc.bioinformatics.quickloadbuilder.QuickLoadArchiveBuilderApp.class).getContext().getResourceMap(QuickloadServerRootExisting.class);
        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        QuickloadPath.setText(resourceMap.getString("QuickloadPath.text")); // NOI18N
        QuickloadPath.setName("QuickloadPath"); // NOI18N
        QuickloadPath.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                QuickloadPathCaretUpdate(evt);
            }
        });

        Browse.setText(resourceMap.getString("Browse.text")); // NOI18N
        Browse.setName("Browse"); // NOI18N
        Browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseActionPerformed(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        UseDefault.setSelected(true);
        UseDefault.setText(resourceMap.getString("UseDefault.text")); // NOI18N
        UseDefault.setName("UseDefault"); // NOI18N
        UseDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UseDefaultActionPerformed(evt);
            }
        });

        ErrorPathLabel.setText(resourceMap.getString("ErrorPathLabel.text")); // NOI18N
        ErrorPathLabel.setName("ErrorPathLabel"); // NOI18N

        ArchiveName.setText(resourceMap.getString("ArchiveName.text")); // NOI18N
        ArchiveName.setName("ArchiveName"); // NOI18N

        Logo.setName("Logo"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Logo, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(QuickloadPath, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(Browse))
                    .addComponent(UseDefault)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(ErrorPathLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 299, Short.MAX_VALUE)
                        .addComponent(ArchiveName)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel2)
                        .addGap(62, 62, 62)
                        .addComponent(UseDefault)
                        .addGap(78, 78, 78)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(QuickloadPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Browse))
                        .addGap(18, 18, 18)
                        .addComponent(ErrorPathLabel)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(ArchiveName)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(Logo, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    
     final JFileChooser fc = new JFileChooser();
    private void BrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseActionPerformed
        fc.setMultiSelectionEnabled( false );
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog( this );
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            QuickloadPath.setText( file.getPath() );
            
        } else {
            //do nothing
        }
    }
        // TODO add your handling code here:}//GEN-LAST:event_BrowseActionPerformed

    private void UseDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UseDefaultActionPerformed
        onDefaultChanged();
    }//GEN-LAST:event_UseDefaultActionPerformed
    boolean Initialized;
    boolean ValidPath;
    
    public boolean validatePath(){
         //check the path is a quickload path
            String file_name = QuickloadPath.getText();
            File file = new File(file_name);
            if( !file.exists() ){
                    ErrorPathLabel.setText( C.getRed("Path does not exixts."));
                    QuickloadPath.setBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.red));
                    ValidPath = false;
            }else{
                if( !file.isDirectory() ){
                    ErrorPathLabel.setText( C.getRed("Quickload archive must be a directory."));
                    QuickloadPath.setBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.red));
                    ValidPath = false;
                }else{
                    //show message 
                    File[] lst = file.listFiles();
                    if( lst.length == 0){
                        ErrorPathLabel.setText( C.getRed("Quickload archive is empty.  Select a valid path or choose create Quickload archive in the previous step to create a new archive."));    
                        QuickloadPath.setBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.red));
                        ValidPath = false;
                    }else{
                        boolean valid = false;
                        for( File i : lst){
                            if( i.getName().equals(QuickloadSourceCreator.CONTENTS_TXT_FILE_NAME)  ){
                                valid = true;
                                break;
                            }
                        }
                        if( !valid ){
                            ValidPath = false;
                            ErrorPathLabel.setText( C.getRed("A " + QuickloadSourceCreator.CONTENTS_TXT_FILE_NAME + " file was not found.  Please choose a valid Quickload path or create a new archive."));        
                            QuickloadPath.setBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.red));
                        }else{
                            ValidPath = true;
                            QuickloadPath.setBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.green));
                            ErrorPathLabel.setText( "" );        
                        }
                    }
                }
            }
            return ValidPath;
    }
    private void QuickloadPathCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_QuickloadPathCaretUpdate
        if( Initialized ){
            validatePath();
            updateControls();
        }
    }//GEN-LAST:event_QuickloadPathCaretUpdate

    public String getArchivePath(){
        return QuickloadPath.getText();
    }
    public void updateControls(){
         if( ValidPath ){
            Parent.enableNext();
        }else{
            Parent.disableNext();
        }
    }
    
    public void onDefaultChanged(){
        if( UseDefault.isSelected() ){
            QuickloadPath.setEnabled(false);
            Browse.setEnabled(false);
            QuickloadPath.setText( DefaultPath );
            
        }else{
            QuickloadPath.setEnabled(true);
            Browse.setEnabled(true);
        }
       
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ArchiveName;
    private javax.swing.JButton Browse;
    private javax.swing.JLabel ErrorPathLabel;
    private javax.swing.JLabel Logo;
    private javax.swing.JTextField QuickloadPath;
    private javax.swing.JCheckBox UseDefault;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}
