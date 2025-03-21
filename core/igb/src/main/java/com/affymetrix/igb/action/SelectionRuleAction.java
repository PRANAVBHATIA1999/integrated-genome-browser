/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.action;

import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.symmetry.SymWithProps;
import static com.affymetrix.genometry.util.SelectionInfoUtils.orderProperties;
import com.affymetrix.igb.IGB;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkanapar
 */
public class SelectionRuleAction extends GenericAction {

    private static final long serialVersionUID = 1L;

    private static final String no_selection_text = "Click the map below to select annotations";
    private static final String selection_info = "Selection Info";
    private static final SelectionRuleAction ACTION = new SelectionRuleAction();
    private SymWithProps sym;
    private static final Logger logger = LoggerFactory.getLogger(SelectionRuleAction.class);
    private Map<String, Object> properties;
    private String selectionText;

    private SelectionRuleAction() {
        super("Get Info", BUNDLE.getString("selectionInforTooltip"), "16x16/actions/info.png", "16x16/actions/info.png", 0);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setSelectionText(String selectionText) {
        this.selectionText = selectionText;
    }

    public static SelectionRuleAction getAction() {
        return ACTION;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame messageFrame = new JFrame();
        JTextArea rules_text = new JTextArea();
        rules_text.setBorder(new EmptyBorder(10, 10, 10, 10));
        rules_text.setEditable(false);
        rules_text.setLineWrap(true);
        rules_text.setColumns(40);
        JScrollPane scroll_pane = new JScrollPane(rules_text);
        messageFrame.add(scroll_pane);
        if (no_selection_text.equals(selectionText)) {
            messageFrame.setTitle("How to Select and De-select Data in IGB");
            rules_text.append(getRules());
        } else {
            Map<String, Object> properties = orderProperties(this.properties, sym);
            messageFrame.setTitle(selection_info);
            if (properties != null && !properties.isEmpty()) {
                int maxLength = 0;
                for (String key : properties.keySet()) {
                    rules_text.append(key + ": " + properties.get(key) + "\n");
                    if (properties.get(key).toString().length() > maxLength) {
                        maxLength = properties.get(key).toString().length();
                    }
                }
                if (maxLength > 200) {
                    rules_text.setColumns(60);
                }
            } else {
                rules_text.append(selectionText);
            }
        }
        messageFrame.setMinimumSize(new Dimension(250, 100));
        messageFrame.pack();
        messageFrame.setLocationRelativeTo(IGB.getInstance().getFrame());
        messageFrame.setVisible(true);
    }

    private String getRules() {
        return "1. Click on an annotation to select it.\n"
                + "2. Double-click something to zoom in on it.\n"
                + "3. Click-drag a region to select and count many items.\n"
                + "4. Click-SHIFT to add to the currently selected items.\n"
                + "5. Control-SHIFT click to remove an item from the currently selected items.\n"
                + "6. Click-drag the axis to zoom in on a region.\n";
    }

    public SymWithProps getSym() {
        return sym;
    }

    public void setSym(SymWithProps sym) {
        this.sym = sym;
    }

}
