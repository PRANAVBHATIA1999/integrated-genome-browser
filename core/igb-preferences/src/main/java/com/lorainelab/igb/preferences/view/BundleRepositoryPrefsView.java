package com.lorainelab.igb.preferences.view;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.igb.service.api.PreferencesPanelProvider;
import com.lorainelab.igb.preferences.IgbPreferencesService;
import javax.swing.JPanel;

/**
 *
 * @author dcnorris
 */
@Component(name = BundleRepositoryPrefsView.COMPONENT_NAME, immediate = true, provide = PreferencesPanelProvider.class)
public class BundleRepositoryPrefsView extends JPanel implements PreferencesPanelProvider {

    public static final String COMPONENT_NAME = "BundleRepositoryPrefsView";
    public static final String TAB_NAME = "Plugin Repositories";
    private BundleRepositoryTableModel tableModel;
    private IgbPreferencesService igbPreferencesService;

    public BundleRepositoryPrefsView() {

    }

    @Activate
    public void activate() {
        tableModel = new BundleRepositoryTableModel(igbPreferencesService.fromDefaultPreferences().get().getRepository());
        initComponents();
    }

    @Reference
    public void setIgbPreferencesService(IgbPreferencesService igbPreferencesService) {
        this.igbPreferencesService = igbPreferencesService;
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

        jScrollPane1 = new javax.swing.JScrollPane();
        pluginRepositoryTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Plugin Repositories"));

        pluginRepositoryTable.setModel(tableModel);
        jScrollPane1.setViewportView(pluginRepositoryTable);

        jButton1.setText("Remove");

        jButton2.setText("Add...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable pluginRepositoryTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getName() {
        return TAB_NAME;
    }

    @Override
    public int getTabWeight() {
        return 6;
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public void refresh() {
        //do nothing
    }

}
