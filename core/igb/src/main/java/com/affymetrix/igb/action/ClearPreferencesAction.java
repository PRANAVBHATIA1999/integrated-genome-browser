package com.affymetrix.igb.action;

import aQute.bnd.annotation.component.Component;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.GenericActionHolder;
import com.affymetrix.genometry.util.PreferenceUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.prefs.PreferencesPanel;
import com.affymetrix.igb.shared.NoToolbarActions;
import com.affymetrix.igb.stylesheet.XmlStylesheetParser;
import com.lorainelab.igb.services.window.menus.IgbMenuItemProvider;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

@Component(name = ClearPreferencesAction.COMPONENT_NAME, immediate = true, provide = {NoToolbarActions.class})
public class ClearPreferencesAction extends GenericAction implements NoToolbarActions {

    public static final String COMPONENT_NAME = "ClearPreferencesAction";
    private static final long serialVersionUID = 1l;
    private static final ClearPreferencesAction ACTION = new ClearPreferencesAction();

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
    }

    public static ClearPreferencesAction getAction() {
        return ACTION;
    }

    private ClearPreferencesAction() {
        super(BUNDLE.getString("ClearPreferences"), null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        // The option pane used differs from the confirmDialog only in
        // that "No" is the default choice.
        String[] options = {"Yes", "No"};
        if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
                PreferencesPanel.getSingleton(), "Really reset all preferences to defaults?\n(this will also exit the application)", "Clear preferences?",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[1])) {

            try {
                XmlStylesheetParser.removeUserStylesheetFile();
                ((IGB) Application.getSingleton()).defaultCloseOperations();
                PreferenceUtils.clearPreferences();
                System.exit(0);
            } catch (Exception ex) {
                ErrorHandler.errorPanel("ERROR", "Error clearing preferences", ex);
            }
        }
    }
}
