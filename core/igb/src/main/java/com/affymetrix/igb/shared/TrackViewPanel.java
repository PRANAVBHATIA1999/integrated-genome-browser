package com.affymetrix.igb.shared;

import com.affymetrix.igb.shared.Selections.RefreshSelectionListener;
import org.lorainelab.igb.services.window.tabs.IgbTabPanel;

/**
 *
 * @author hiralv
 */
public abstract class TrackViewPanel extends IgbTabPanel implements RefreshSelectionListener {

    private boolean is_listening = true;
    private final javax.swing.GroupLayout.SequentialGroup horizonatalGroup;
    private final javax.swing.GroupLayout.ParallelGroup verticalGroup;

    /**
     * Creates new form TrackViewPanel
     */
    public TrackViewPanel(String displayName, String title, String tooltip, boolean focus, int position) {
        super(displayName, title, tooltip, focus, position);
        initComponents();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(componentPanel);
        componentPanel.setLayout(layout);
        horizonatalGroup = layout.createSequentialGroup();
        verticalGroup = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE);
        clearButton.setEnabled(false);
        deleteButton.setEnabled(false);
        restoreButton.setEnabled(false);
        saveButton.setEnabled(false);
        layout.setHorizontalGroup(horizonatalGroup);
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(verticalGroup));
        Selections.addRefreshSelectionListener(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonsPanel = new javax.swing.JPanel();
        selectAllButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        restoreButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        customButton = new javax.swing.JButton();
        componentPanel = new javax.swing.JPanel();

        selectAllButton.setText("Select All");
        selectAllButton.addActionListener(this::selectAllButtonActionPerformed);

        saveButton.setText("Save Track");
        saveButton.addActionListener(this::saveButtonActionPerformed);

        deleteButton.setText("Delete Track");
        deleteButton.addActionListener(this::deleteButtonActionPerformed);

        restoreButton.setText("Restore Default");
        restoreButton.addActionListener(this::restoreButtonActionPerformed);

        clearButton.setText("Clear Track");
        clearButton.addActionListener(this::clearButtonActionPerformed);

        customButton.setText("customButton");
        customButton.addActionListener(this::customButtonActionPerformed);

        org.jdesktop.layout.GroupLayout buttonsPanelLayout = new org.jdesktop.layout.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonsPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(selectAllButton)
                .add(0, 0, 0)
                .add(clearButton)
                .add(0, 0, 0)
                .add(saveButton)
                .add(0, 0, 0)
                .add(deleteButton)
                .add(0, 0, 0)
                .add(restoreButton)
                .add(0, 0, 0)
                .add(customButton)
                .add(0, 0, 0))
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, buttonsPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(buttonsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectAllButton)
                    .add(saveButton)
                    .add(deleteButton)
                    .add(restoreButton)
                    .add(clearButton)
                    .add(customButton)))
        );

        org.jdesktop.layout.GroupLayout componentPanelLayout = new org.jdesktop.layout.GroupLayout(componentPanel);
        componentPanel.setLayout(componentPanelLayout);
        componentPanelLayout.setHorizontalGroup(
            componentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        componentPanelLayout.setVerticalGroup(
            componentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 176, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(componentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(buttonsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(buttonsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(componentPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        if (is_listening) {
            selectAllButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        if (is_listening) {
            clearButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_clearButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        if (is_listening) {
            saveButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        if (is_listening) {
            deleteButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void restoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreButtonActionPerformed
        if (is_listening) {
            restoreButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_restoreButtonActionPerformed

    private void customButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customButtonActionPerformed
        if (is_listening) {
            customButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_customButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JPanel componentPanel;
    private javax.swing.JButton customButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton restoreButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton selectAllButton;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JPanel getButtonsPanel() {
        return buttonsPanel;
    }

    public javax.swing.JButton getClearButton() {
        return clearButton;
    }

    public javax.swing.JPanel getComponentPanel() {
        return componentPanel;
    }

    public javax.swing.JButton getDeleteButton() {
        return deleteButton;
    }

    public javax.swing.JButton getRestoreButton() {
        return restoreButton;
    }

    public javax.swing.JButton getSaveButton() {
        return saveButton;
    }

    public javax.swing.JButton getSelectAllButton() {
        return selectAllButton;
    }

    public javax.swing.JButton getCustomButton() {
        return customButton;
    }

    protected void selectAllButtonActionPerformedA(java.awt.event.ActionEvent evt) {
        SelectAllAction.getAction().actionPerformed(evt);
    }

    protected void clearButtonActionPerformedA(java.awt.event.ActionEvent evt) {
        com.affymetrix.igb.action.RemoveDataFromTracksAction.getAction().actionPerformed(evt);
    }

    protected void restoreButtonActionPerformedA(java.awt.event.ActionEvent evt) {
        com.affymetrix.igb.action.RestoreToDefaultAction.getAction().actionPerformed(evt);
    }

    protected void deleteButtonActionPerformedA(java.awt.event.ActionEvent evt) {
        com.affymetrix.igb.action.CloseTracksAction.getAction().actionPerformed(evt);
    }

    protected void saveButtonActionPerformedA(java.awt.event.ActionEvent evt) {
        com.affymetrix.igb.action.ExportFileAction.getAction().actionPerformed(evt);
    }

    protected abstract void customButtonActionPerformedA(java.awt.event.ActionEvent evt);

    protected abstract void selectAllButtonReset();

    protected abstract void clearButtonReset();

    protected abstract void saveButtonReset();

    protected abstract void deleteButtonReset();

    protected abstract void restoreButtonReset();

    protected abstract void customButtonReset();

    public void resetAll() {
        is_listening = false;

        selectAllButtonReset();
        clearButtonReset();
        saveButtonReset();
        deleteButtonReset();
        restoreButtonReset();
        customButtonReset();

        is_listening = true;
    }

    @Override
    public void selectionRefreshed() {
        resetAll();
    }

    public void addPanel(javax.swing.JPanel panel) {
        componentPanel.add(panel);
        horizonatalGroup.addComponent(panel);
        verticalGroup.addComponent(panel);
    }

}
