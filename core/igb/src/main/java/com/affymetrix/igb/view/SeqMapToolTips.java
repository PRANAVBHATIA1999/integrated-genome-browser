/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.view;

import com.affymetrix.genometryImpl.tooltip.ToolTipCategory;
import com.affymetrix.genometryImpl.tooltip.ToolTipOperations;
import com.affymetrix.genometryImpl.tooltip.ToolTipValue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author tkanapar
 */
public class SeqMapToolTips extends JWindow {

    private static final long serialVersionUID = 1L;
    private static final SimpleAttributeSet NAME = new SimpleAttributeSet();

    static {
        StyleConstants.setBold(NAME, true);
    }
    private static final Color DEFAULT_BACKGROUNDCOLOR = new Color(253, 254, 196);
    private static final int MIN_HEIGHT = 200;
    private static final int MAX_WIDTH = 300;
    private final JTextPane tooltip;
    private final Color backgroundColor;
    private String[][] properties;

    public SeqMapToolTips(Window owner) {
        super(owner);
        tooltip = new JTextPane();
        tooltip.setEditable(false);
        this.backgroundColor = DEFAULT_BACKGROUNDCOLOR;
        init();
    }

    public void setToolTip(Point point, String[][] properties) {
        if (isVisible() && properties == null) {
            setVisible(false);
        }
        timer.stop();
        if (!getOwner().isActive()) {
            return;
        }

        if (properties != null && properties.length > 1) {
            timer.stop();

            this.properties = properties;
            formatTooltip();
            tooltip.setCaretPosition(0);
            setLocation(determineBestLocation(point));
            pack();
            setSize(MAX_WIDTH, getSize().height);
            timer.setInitialDelay(500);
            timer.start();

        } else if (isVisible()) {
        } else {
            this.properties = properties;
            tooltip.setText(null);
        }
    }

    public void setToolTip(Point point, Map<String, Object> properties) {
        ArrayList<ToolTipCategory> propList = null;
        if (isVisible() && properties == null) {
            setVisible(false);
        }
        timer.stop();
        if (!getOwner().isActive()) {
            return;
        }
        tooltip.setText(null);
        if (properties != null && properties.size() > 0) {
            propList = ToolTipOperations.formatBamSymTooltip(properties);
            formatCategoryToolTip(propList);
            tooltip.setCaretPosition(0);
            setLocation(determineBestLocation(point));
            pack();
            setSize(MAX_WIDTH, getSize().height);
            timer.setInitialDelay(500);
            timer.start();
        } else if (isVisible()) {

        } else {
            tooltip.setText(null);
        }
    }

    private void formatCategoryToolTip(ArrayList<ToolTipCategory> properties) {
        HashMap<String, ToolTipValue> toolTipProps = null;
        ToolTipValue propValue = null;
        try {
            for (ToolTipCategory category : properties) {
                tooltip.getDocument().insertString(tooltip.getDocument().getLength(), category.getCategory() + ":\n", NAME);
                toolTipProps = category.getProperties();
                for (String propKey : toolTipProps.keySet()) {
                    propValue = toolTipProps.get(propKey);
                    if (propValue.getWeight() != -1) {
                        tooltip.getDocument().insertString(tooltip.getDocument().getLength(), propKey + " ", NAME);
                        tooltip.getDocument().insertString(tooltip.getDocument().getLength(), propValue.getValue() + "\n", null);
                    }
                }
                tooltip.getDocument().insertString(tooltip.getDocument().getLength(), "----------\n", null);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void formatTooltip() {
        tooltip.setText(null);
        for (String[] propertie : properties) {
            try {
                tooltip.getDocument().insertString(tooltip.getDocument().getLength(), propertie[0], NAME);
                tooltip.getDocument().insertString(
                        tooltip.getDocument().getLength(), " ", null);
                tooltip.getDocument().insertString(tooltip.getDocument().getLength(), propertie[1], null);
                tooltip.getDocument().insertString(
                        tooltip.getDocument().getLength(), "\n", null);

            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

    }

    private Point determineBestLocation(Point currentPoint) {
        Point bestLocation = new Point(currentPoint.x + 10, currentPoint.y + 10);
        return bestLocation;
    }

    private void init() {
        setFocusableWindowState(false);
        setBackground(backgroundColor);
        setForeground(backgroundColor);
        tooltip.setBackground(backgroundColor);
        tooltip.setDisabledTextColor(tooltip.getForeground());

        JScrollPane scrollPane = new JScrollPane(tooltip);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(backgroundColor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        setLayout(new BorderLayout(0, 0));
        add(scrollPane);

        pack();
        setSize(MAX_WIDTH, MIN_HEIGHT);
    }

    Timer timer = new Timer(100, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(true);

        }
    });

}
