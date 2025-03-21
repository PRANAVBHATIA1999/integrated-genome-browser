package com.affymetrix.igb.swing;

import com.affymetrix.genoviz.awt.AdjustableJSlider;
import com.affymetrix.igb.swing.script.ScriptManager;

public class RPAdjustableJSlider extends AdjustableJSlider implements JRPWidget {

    private static final long serialVersionUID = 1L;
    private String id;

    public RPAdjustableJSlider(String id) {
        super();
        this.id = id;
        init();
    }

    public RPAdjustableJSlider(String id, int orientation) {
        super(orientation);
        this.id = id;
        init();
    }

    private void init() {
        if (id != null) {
            ScriptManager.getInstance().addWidget(this);
        }
        addAdjustmentListener(e -> ScriptManager.getInstance().recordOperation(new Operation(RPAdjustableJSlider.this, "setValue(" + getValue() + ")")));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean consecutiveOK() {
        return true;
    }
}
