package com.affymetrix.igb.action;

import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.GenericActionHolder;
import com.affymetrix.common.PreferenceUtils;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.view.UnibrowHairline;
import java.awt.event.KeyEvent;

/**
 *
 * @author sgblanch
 * @version $Id: ToggleHairlineLabelAction.java 11366 2012-05-02 14:57:55Z
 * anuj4159 $
 */
public class ToggleHairlineLabelAction extends GenericAction {

    private static final long serialVersionUID = 1;
    private static final ToggleHairlineLabelAction ACTION = new ToggleHairlineLabelAction();

    private ToggleHairlineLabelAction() {
        super(BUNDLE.getString("toggleHairlineLabel"), null, "16x16/apps/office-calendar.png", "22x22/apps/office-calendar.png", KeyEvent.VK_Z);
        /* TODO: This is only correct for English Locale" */
        this.putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 5);
        this.ordinal = -4003100;
    }

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
        PreferenceUtils.saveToPreferences(UnibrowHairline.PREF_HAIRLINE_LABELED, UnibrowHairline.default_show_hairline_label, ACTION);
    }

    public static ToggleHairlineLabelAction getAction() {
        return ACTION;
    }

    @Override
    public boolean isToggle() {
        return true;
    }

    @Override
    public boolean isToolbarAction() {
        return false;
    }
}
