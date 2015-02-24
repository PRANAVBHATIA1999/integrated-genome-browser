package com.affymetrix.igb.swing;

import com.affymetrix.igb.swing.script.ScriptManager;
import com.affymetrix.igb.swing.util.WeightUtil;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class JRPMenu extends JMenu implements WeightedJRPWidget {

    private static final long serialVersionUID = 1L;
    private final String id;
    private int weight;
    
    private List<WeightedJRPWidget> menuItemComponents;

    public JRPMenu(String id) {
        super();
        this.id = id;
        init();
    }

    public JRPMenu(String id, Action a) {
        super(a);
        this.id = id;
        init();
    }

    public JRPMenu(String id, String s) {
        super(s);
        this.id = id;
        init();
    }

    public JRPMenu(String id, String s, int index) {
        this(id, s);
        this.weight = index;
    }

    public JRPMenu(String id, String s, boolean b) {
        super(s, b);
        this.id = id;
        init();
    }

    private void init() {
        menuItemComponents = new ArrayList<>();
        ScriptManager.getInstance().addWidget(this);
        addActionListener(e -> ScriptManager.getInstance().recordOperation(new Operation(JRPMenu.this, "doClick()")));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean consecutiveOK() {
        return true;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public JMenuItem add(JMenuItem newMenuItem) {
        if(newMenuItem instanceof WeightedJRPWidget) {
            int loc = WeightUtil.locationToAdd(menuItemComponents, (WeightedJRPWidget)newMenuItem);
            menuItemComponents.add(loc, (WeightedJRPWidget)newMenuItem);
            return (JMenuItem) super.add(newMenuItem, loc);
            
        } else {
            return (JMenuItem) super.add(newMenuItem, -1);
        }
    }

    @Override
    public void addSeparator() {
        JRPSeparator separator = new JRPSeparator(menuItemComponents.get(menuItemComponents.size() - 1).getWeight() + 1);
        menuItemComponents.add(separator);
        super.add(separator, -1);
    }
}
