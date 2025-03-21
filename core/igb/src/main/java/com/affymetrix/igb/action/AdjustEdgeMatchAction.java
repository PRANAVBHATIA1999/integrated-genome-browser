package com.affymetrix.igb.action;

import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.GenericActionHolder;
import com.affymetrix.igb.IGB;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.glyph.EdgeMatchAdjuster;
import com.affymetrix.igb.view.SeqMapView;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;

/**
 *
 * @author sgblanch
 * @version $Id: AdjustEdgeMatchAction.java 11333 2012-05-01 17:54:56Z anuj4159
 * $
 */
public class AdjustEdgeMatchAction extends GenericAction {

    private static final long serialVersionUID = 1L;
    private static final AdjustEdgeMatchAction ACTION = new AdjustEdgeMatchAction();

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
    }

    public static AdjustEdgeMatchAction getAction() {
        return ACTION;
    }

    private AdjustEdgeMatchAction() {
        super(MessageFormat.format(BUNDLE.getString("menuItemHasDialog"), BUNDLE.getString("adjustEdgeMatchFuzziness")), null, null, null, KeyEvent.VK_F);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        SeqMapView map_view = IGB.getInstance().getMapView();
        EdgeMatchAdjuster.showFramedThresholder(map_view.getEdgeMatcher(), map_view);
    }
}
