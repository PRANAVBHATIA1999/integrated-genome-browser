package com.affymetrix.igb.action;

import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.GenericActionHolder;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.swing.script.ScriptManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ClearScriptAction extends GenericAction {

    private static final long serialVersionUID = 1L;
    private static final ClearScriptAction ACTION = new ClearScriptAction();

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
    }

    public static ClearScriptAction getAction() {
        return ACTION;
    }

    private ClearScriptAction() {
        super(BUNDLE.getString("clearScript"), null, "16x16/status/clear script.png", "22x22/status/clear script.png", KeyEvent.VK_C, null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        ScriptManager.getInstance().clearScript();
    }
}
