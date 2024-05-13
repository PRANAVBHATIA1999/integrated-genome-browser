package com.affymetrix.igb.util;

import com.affymetrix.genometry.filter.ChainFilter;
import com.affymetrix.genometry.filter.SymmetryFilterI;
import com.affymetrix.genometry.general.IParameters;
import com.affymetrix.igb.shared.ConfigureOptionsPanel.Filter;
import com.affymetrix.igb.tiers.TierLabelManager;

/**
 *
 * @author hiralv
 */
public class ConfigureFilters extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private Filter<SymmetryFilterI> optionFilter;
    private TierLabelManager tierLabelManager;

    /**
     * Creates new form Filter
     */
    public ConfigureFilters() {
        initComponents();
    }

    public void setFilter(SymmetryFilterI filter) {
        if (filter instanceof ChainFilter) {
            ChainFilter chainFilter = (ChainFilter) filter;
            chainFilter.getFilters().forEach(((javax.swing.DefaultListModel) filterList.getModel())::addElement);
        } else if (filter != null) {
            ((javax.swing.DefaultListModel) filterList.getModel()).addElement(filter);
        }
        if (filterList.getModel().getSize() > 0) {
            filterList.setSelectedIndex(0);
            removeButton.setEnabled(true);
        } else {
            removeButton.setEnabled(false);
        }
    }

    public SymmetryFilterI getFilter() {
        int size = filterList.getModel().getSize();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return (SymmetryFilterI) filterList.getModel().getElementAt(0);
        } else {
            ChainFilter filter = new ChainFilter();
            java.util.List<SymmetryFilterI> filters = new java.util.ArrayList<>(size);
            for (int i = 0; i < filterList.getModel().getSize(); i++) {
                filters.add((SymmetryFilterI) filterList.getModel().getElementAt(i));
            }
            filter.setFilter(filters);
            return filter;
        }
    }

    public void setOptionsFilter(Filter<SymmetryFilterI> optionFilter) {
        this.optionFilter = optionFilter;
    }

    public void setTierLabelManager(TierLabelManager tierLabelManager) {
        this.tierLabelManager = tierLabelManager;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterListScrollPane = new javax.swing.JScrollPane();
        filterList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();

        filterList.setModel(new javax.swing.DefaultListModel());
        filterList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        filterList.setCellRenderer(new FilterListCellRenderer());
        filterList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                filterListMouseClicked(evt);
            }
        });
        filterList.addListSelectionListener(this::filterListValueChanged);
        filterListScrollPane.setViewportView(filterList);

        addButton.setText("Add");
        addButton.addActionListener(this::addButtonActionPerformed);

        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        removeButton.addActionListener(this::removeButtonActionPerformed);

        editButton.setText("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(this::editButtonActionPerformed);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(filterListScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 207, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(editButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editButton)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(filterListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        addNew();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeSelected();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        beginEditing();
    }//GEN-LAST:event_editButtonActionPerformed

    private void filterListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_filterListValueChanged
        if (isFilterEditable((SymmetryFilterI) filterList.getSelectedValue())) {
            editButton.setEnabled(true);
        } else {
            editButton.setEnabled(false);
        }
    }//GEN-LAST:event_filterListValueChanged

    private void filterListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterListMouseClicked
        if (evt.getClickCount() == 2 && isFilterEditable((SymmetryFilterI) filterList.getSelectedValue())) {
            beginEditing();
        }
    }//GEN-LAST:event_filterListMouseClicked

    private boolean isFilterEditable(SymmetryFilterI filter) {
        if (filter instanceof IParameters
                && ((IParameters) filter).getParametersType() != null
                && !((IParameters) filter).getParametersType().isEmpty()) {
            return true;
        }
        return false;
    }

    private void addNew() {
        ConfigureOptionsDialog<SymmetryFilterI> optionDialog = new ConfigureOptionsDialog<>(SymmetryFilterI.class, "Show Only", optionFilter, false, tierLabelManager);
        optionDialog.setTitle("Add filter");
        optionDialog.setLocationRelativeTo(this);
        SymmetryFilterI selectedFilter = optionDialog.showDialog();
        Object value = optionDialog.getValue();
        if (value instanceof Integer && (Integer) value == javax.swing.JOptionPane.OK_OPTION && selectedFilter != null) {
            ((javax.swing.DefaultListModel) filterList.getModel()).addElement(selectedFilter);
            filterList.setSelectedIndex(filterList.getModel().getSize() - 1);
            removeButton.setEnabled(true);
        }
    }

    private void removeSelected() {
        int selected = filterList.getSelectedIndex();
        ((javax.swing.DefaultListModel) filterList.getModel()).removeElementAt(selected);
        if (selected - 1 >= 0) {
            filterList.setSelectedIndex(selected - 1);
        } else if (filterList.getModel().getSize() > 0) { //If first element is deleted then selected one below it
            filterList.setSelectedIndex(0);
        } else {
            removeButton.setEnabled(false);
        }
    }

    private void beginEditing() {
        SymmetryFilterI selectedFilter = (SymmetryFilterI) filterList.getSelectedValue();
        SymmetryFilterI selectedClone = selectedFilter.newInstance();

        ConfigureOptionsDialog<SymmetryFilterI> optionDialog = new ConfigureOptionsDialog<>(SymmetryFilterI.class, "Show Only");
        optionDialog.setTitle("Edit filter");
        optionDialog.setLocationRelativeTo(this);
        optionDialog.setInitialValue(selectedClone);
        optionDialog.setEnabled(false);
        optionDialog.setVisible(true);
        Object value = optionDialog.getValue();

        if (value != null && (Integer) value == javax.swing.JOptionPane.OK_OPTION) {
            for (String key : ((IParameters) selectedClone).getParametersType().keySet()) {
                ((IParameters) selectedFilter).setParameterValue(key, ((IParameters) selectedClone).getParameterValue(key));
            }
        }
    }

    private static class FilterListCellRenderer implements javax.swing.ListCellRenderer {

        private static final javax.swing.DefaultListCellRenderer filterRenderer, parameterRenderer;
        private static final java.util.Map<java.awt.font.TextAttribute, Object> filterAttrMap, parameterAttrMap;

        static {
            filterRenderer = new javax.swing.DefaultListCellRenderer();
            parameterRenderer = new javax.swing.DefaultListCellRenderer();

            filterAttrMap = new java.util.HashMap<>();
            filterAttrMap.put(java.awt.font.TextAttribute.WEIGHT, java.awt.font.TextAttribute.WEIGHT_BOLD);

            parameterAttrMap = new java.util.HashMap<>();
            parameterAttrMap.put(java.awt.font.TextAttribute.POSTURE, java.awt.font.TextAttribute.POSTURE_OBLIQUE);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            SymmetryFilterI filter = (SymmetryFilterI) value;
            javax.swing.Box panel = new javax.swing.Box(javax.swing.BoxLayout.LINE_AXIS);

            javax.swing.JLabel comp = (javax.swing.JLabel) filterRenderer.getListCellRendererComponent(list, filter.getDisplay(), index, isSelected, cellHasFocus);
            comp.setFont(comp.getFont().deriveFont(filterAttrMap));
            panel.add(comp);

            if (filter instanceof IParameters && ((IParameters) filter).getParametersType() != null
                    && !((IParameters) filter).getParametersType().isEmpty()) {

                javax.swing.JLabel paramComp = (javax.swing.JLabel) parameterRenderer.getListCellRendererComponent(list, "  (" + ((IParameters) filter).getPrintableString() + ")", index, isSelected, cellHasFocus);
                paramComp.setFont(paramComp.getFont().deriveFont(parameterAttrMap));
                panel.add(paramComp);
            }

            return panel;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JList filterList;
    private javax.swing.JScrollPane filterListScrollPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
