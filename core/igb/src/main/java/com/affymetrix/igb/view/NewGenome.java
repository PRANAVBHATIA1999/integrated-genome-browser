package com.affymetrix.igb.view;

import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.SpeciesLookup;
import com.affymetrix.genometryImpl.util.UniFileFilter;
import com.affymetrix.igb.shared.FileTracker;
import com.affymetrix.igb.shared.OpenURIAction;
import com.affymetrix.igb.swing.JRPFileChooser;
import com.jidesoft.hints.ListDataIntelliHints;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;

/**
 *
 * @author hiralv
 */
public class NewGenome extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private static final Pattern space_regex = Pattern.compile("\\s+");

    /**
     * Creates new form NewGenome
     */
    public NewGenome() {
        initComponents();
        PromptSupport.setPrompt("Enter Species Name", speciesTextField);
        PromptSupport.setPrompt("Enter Version Name", versionTextField);
        PromptSupport.setFocusBehavior(FocusBehavior.HIGHLIGHT_PROMPT, speciesTextField);
        PromptSupport.setFocusBehavior(FocusBehavior.HIGHLIGHT_PROMPT, versionTextField);

    }

    public String getSpeciesName() {
        return speciesTextField.getText();
    }

    public String getVersionName() {
        return versionTextField.getText();
    }

    public String getRefSeqFile() {
        return refSeqTextField.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        speciesLabel = new javax.swing.JLabel();
        speciesTextField = new javax.swing.JTextField();
        versionLabel = new javax.swing.JLabel();
        versionTextField = new javax.swing.JTextField();
        refSeqLabel = new javax.swing.JLabel();
        refSeqTextField = new javax.swing.JTextField();
        refSeqBrowseButton = new javax.swing.JButton();

        speciesLabel.setText("Species");

        ListDataIntelliHints<String> hints = new ListDataIntelliHints<>(speciesTextField, SpeciesLookup.getAllSpeciesName().toArray(new String[1]));

        versionLabel.setText("Genome Version");

        refSeqLabel.setText("Reference Sequence");

        refSeqBrowseButton.setText("...");
        refSeqBrowseButton.addActionListener(this::refSeqBrowseButtonActionPerformed);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(speciesLabel)
                    .add(versionLabel)
                    .add(refSeqLabel))
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(speciesTextField)
                            .add(versionTextField))
                        .add(5, 5, 5))
                    .add(layout.createSequentialGroup()
                        .add(refSeqTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 257, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE)
                        .add(refSeqBrowseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(speciesLabel)
                    .add(speciesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(versionLabel)
                    .add(versionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(refSeqLabel)
                    .add(refSeqTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refSeqBrowseButton))
                .add(43, 43, 43))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refSeqBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refSeqBrowseButtonActionPerformed
        FileTracker fileTracker = FileTracker.DATA_DIR_TRACKER;
        Set<String> all_known_endings = new HashSet<>();
        JRPFileChooser chooser = new JRPFileChooser("newGenome", fileTracker.getFile());

        List<UniFileFilter> filters = OpenURIAction.getSupportedFiles(FileTypeCategory.Sequence);
        for (UniFileFilter filter : filters) {
            chooser.addChoosableFileFilter(filter);
            all_known_endings.addAll(filter.getExtensions());
        }
        UniFileFilter filter = new UniFileFilter("bam", "BAM Files");
        filter.addCompressionEndings(GeneralUtils.compression_endings);
        chooser.addChoosableFileFilter(filter);
        all_known_endings.addAll(filter.getExtensions());
        filter = new UniFileFilter("txt", "Chromosome lengths information");
        chooser.addChoosableFileFilter(filter);
        all_known_endings.addAll(filter.getExtensions());

        UniFileFilter all_known_types = new UniFileFilter(
                all_known_endings.toArray(new String[all_known_endings.size()]),
                "Known Types");
        all_known_types.setExtensionListInDescription(false);
        all_known_types.addCompressionEndings(GeneralUtils.compression_endings);
        chooser.addChoosableFileFilter(all_known_types);
        chooser.setFileFilter(all_known_types);

        chooser.setMultiSelectionEnabled(false);
        int selection = chooser.showOpenDialog(this);
        if (selection != JFileChooser.APPROVE_OPTION) {
            return;
        }
        fileTracker.setFile(chooser.getCurrentDirectory());

        File selectedRefSeqFile = chooser.getSelectedFile();
        if (selectedRefSeqFile != null) {
            try {
                this.refSeqTextField.setText(selectedRefSeqFile.getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger(NewGenome.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_refSeqBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton refSeqBrowseButton;
    private javax.swing.JLabel refSeqLabel;
    private javax.swing.JTextField refSeqTextField;
    private javax.swing.JLabel speciesLabel;
    private javax.swing.JTextField speciesTextField;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JTextField versionTextField;
    // End of variables declaration//GEN-END:variables
}
