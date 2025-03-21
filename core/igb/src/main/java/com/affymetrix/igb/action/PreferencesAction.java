package com.affymetrix.igb.action;

import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.GenericActionHolder;
import com.affymetrix.igb.IGB;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.prefs.PreferencesPanel;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
    
/**
 *
 * @author sgblanch
 * @version $Id: PreferencesAction.java 11361 2012-05-02 14:46:42Z anuj4159 $
 * Modified by nick
 */
public class PreferencesAction extends GenericAction {

    private static final long serialVersionUID = 1L;
    private static final PreferencesAction ACTION = new PreferencesAction();
    private final int TOOLBAR_INDEX = 6;

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
    }

    public static PreferencesAction getAction() {
        return ACTION;
    }

    private PreferencesAction() {
        super(BUNDLE.getString("Preferences"), BUNDLE.getString("preferencesTooltip"),
                "16x16/actions/preferences_updated.png",
                "22x22/actions/preferences_updated.png",
                KeyEvent.VK_E, null, true);
        this.ordinal = -9006100;
        setKeyStrokeBinding("alt P");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        IGB igb = IGB.getInstance();
        List<TierLabelGlyph> tier_label_glyphs = null;
        if (igb != null) {
            tier_label_glyphs = igb.getMapView().getTierManager().getSelectedTierLabels();
        }

        PreferencesPanel p = PreferencesPanel.getSingleton();
        p.getFrame().setVisible(true);
        p.getFrame().setState(Frame.NORMAL);
    }
    @Override
    public boolean isToolbarDefault() {
        return true; 
    }

    @Override
    public int getToolbarIndex() {
        return TOOLBAR_INDEX; 
    }
}
