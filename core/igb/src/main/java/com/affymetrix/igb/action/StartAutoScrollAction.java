package com.affymetrix.igb.action;

import com.affymetrix.genometry.event.GenericActionHolder;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author hiralv
 */
public class StartAutoScrollAction extends SeqMapViewActionA {

    private static final long serialVersionUID = 1L;
    private static StartAutoScrollAction ACTION = new StartAutoScrollAction();
    private final int TOOLBAR_INDEX = 12;

    private StartAutoScrollAction() {
        super(BUNDLE.getString("startAutoScroll"), BUNDLE.getString("autoscrollTooltip"), "16x16/actions/autoscroll.png",
                "22x22/actions/autoscroll.png", 0);
        setEnabled(true);
    }

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
    }

    public static StartAutoScrollAction getAction() {
        return ACTION;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        start();
    }

    public void start() {
        // Calculate start, end and bases per pixels
        Rectangle2D.Double cbox = getTierMap().getViewBounds();
        int start_pos = (int) cbox.x;
        // Temporary fix to avoid npe.
        if (getSeqMapView().getViewSeq() == null) {
            return;
        }
        int end_pos = getSeqMapView().getViewSeq().getLength();

        getSeqMapView().getAutoScroll().configure(this.getTierMap(), start_pos, end_pos);
        getSeqMapView().getAutoScroll().start(this.getTierMap());
        setEnabled(false);
        StopAutoScrollAction.getAction().setEnabled(true);
    }

//	@Override
//	public boolean isEnabled(){
//		return getSeqMapView().getViewSeq() != null;
//	}
    @Override
    public boolean isToolbarAction() {
        return false;
    }

    @Override
    public int getToolbarIndex() {
        return TOOLBAR_INDEX;
    }
}
