package com.affymetrix.igb.action;

import com.affymetrix.genometry.event.ContinuousAction;
import com.affymetrix.genometry.event.GenericActionHolder;
import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.tiers.AffyTieredMap;
import java.awt.event.ActionEvent;

public class ScrollRightAction extends SeqMapViewActionA implements ContinuousAction {

    private static final long serialVersionUID = 1L;
    private static final ScrollRightAction ACTION = new ScrollRightAction();
    private final int TOOLBAR_INDEX = 11;

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
    }

    public static ScrollRightAction getAction() {
        return ACTION;
    }

    public ScrollRightAction() {
        super("Scroll Right", BUNDLE.getString("rightGreenArrowTooltip"), "16x16/actions/go-next.png", "22x22/actions/go-next.png", 0);
        this.ordinal = -4007020;
        setKeyStrokeBinding("ctrl RIGHT");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        AffyTieredMap seqmap = getSeqMapView().getSeqMap();
        int[] visible = seqmap.getVisibleRange();
        seqmap.scroll(NeoAbstractWidget.X, visible[0] + (visible[1] - visible[0]) / 10);
        seqmap.updateWidget();
        getSeqMapView().getAutoLoadAction().loadData();
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
