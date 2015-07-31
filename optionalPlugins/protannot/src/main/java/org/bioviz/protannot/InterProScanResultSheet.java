/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioviz.protannot;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.bioviz.protannot.PropertySheetHelper;
import org.bioviz.protannot.model.InterProScanTableModel;

/**
 *
 * @author Tarun
 */
public class InterProScanResultSheet extends JPanel {

    public static final String COMPONENT_NAME = "InterProScanResultSheet";
    private final JLabel title;
    private final JScrollPane scrollPane;
    private final JViewport jvp;
    private static final String DEFAULT_TITLE = "InterProScan";
    private InterProScanTableModel ipsTableModel;
    private final JTable table;
    private final PropertySheetHelper helper;
    private final JButton cancelAllJobs;

    public InterProScanResultSheet() {
        super();
        this.title = new JLabel(DEFAULT_TITLE);
        this.table = new JTable();
        this.helper = new PropertySheetHelper(table);
        this.jvp = new JViewport();
        this.scrollPane = new JScrollPane(table);
        cancelAllJobs = new JButton("Cancel All Jobs");
        setUpPanel();
    }

    private void setUpPanel() {
        jvp.setView(title);
        scrollPane.setColumnHeader(jvp);
        LC lc = new LC();
        setLayout(new MigLayout("fill"));
        add(cancelAllJobs, "wrap");
        add(scrollPane, "grow, push, span");

        table.addMouseListener(helper);
        table.addMouseMotionListener(helper);
        table.setRowSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setAutoCreateRowSorter(true);
        table.setEnabled(true);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public String getTitle() {
        return title.getText();
    }

    public void showTableData(InterProScanTableModel ipsTableModel) {
        this.ipsTableModel = ipsTableModel;
        table.setModel(ipsTableModel);
        table.setDefaultRenderer(Object.class, helper);
    }

    public JButton getCancelAllJobs() {
        return cancelAllJobs;
    }

}
