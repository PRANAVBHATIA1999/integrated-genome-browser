package com.affymetrix.igb.tabs.annotation;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.igb.shared.SelectAllAction;
import com.affymetrix.igb.shared.Selections;
import static com.affymetrix.igb.shared.Selections.annotSyms;
import com.affymetrix.igb.shared.StylePanelImpl;
import com.affymetrix.igb.shared.TrackViewPanel;
import org.lorainelab.igb.services.IgbService;
import static org.lorainelab.igb.services.ServiceComponentNameReference.ANNOTATION_TRACK_PANEL_TAB;
import org.lorainelab.igb.services.window.tabs.IgbTabPanelI;
import org.lorainelab.igb.track.operations.api.OperationsPanel;
import org.lorainelab.igb.track.operations.api.OperationsPanelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hiralv
 */
@Component(name = ANNOTATION_TRACK_PANEL_TAB, service = IgbTabPanelI.class, immediate = true)
public class AnnotationTrackPanel extends TrackViewPanel {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationTrackPanel.class);
    private static final long serialVersionUID = 1L;
    public static final java.util.ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("annotation");
    private static FileTypeCategory[] categories = new FileTypeCategory[]{FileTypeCategory.Annotation, FileTypeCategory.Alignment, FileTypeCategory.ProbeSet};
    private static final int TAB_POSITION = 1;
    private IgbService igbService;
    private OperationsPanel operationsPanel;

    public AnnotationTrackPanel() {
        super(BUNDLE.getString("annotationTab"), BUNDLE.getString("annotationTab"), BUNDLE.getString("annotationTooltip"), false, TAB_POSITION);
    }

    @Activate
    public void activate() {
        getCustomButton().setText("Other Options...");
        StylePanelImpl stylePanel = new StylePanelImpl(igbService) {

            @Override
            protected void setStyles() {
                styles.addAll(Selections.annotStyles);
                styles.addAll(Selections.axisStyles);
            }

            @Override
            protected boolean isAnyFloat() {
                return false;
            }
        };
        AnnotationPanelImpl annotationPanel = new AnnotationPanelImpl(igbService);

        this.addPanel(stylePanel);
        this.addPanel(annotationPanel);
        this.addPanel(operationsPanel);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setOperationsPanel(OperationsPanelService operationsPanelService) {
        operationsPanel = operationsPanelService.getAnnotationOperationsPanel();
    }

    @Override
    protected void selectAllButtonReset() {

    }

    @Override
    protected void selectAllButtonActionPerformedA(java.awt.event.ActionEvent evt) {
        SelectAllAction.getAction().execute(categories);
    }

    @Override
    protected void customButtonActionPerformedA(java.awt.event.ActionEvent evt) {
        igbService.openPreferencesOtherPanel();
    }

    @Override
    protected void clearButtonReset() {
        javax.swing.JButton clearButton = getClearButton();
        clearButton.setEnabled(annotSyms.size() > 0);
    }

    @Override
    protected void saveButtonReset() {
        javax.swing.JButton saveButton = getSaveButton();
        saveButton.setEnabled(annotSyms.size() > 0);
    }

    @Override
    protected void deleteButtonReset() {
        javax.swing.JButton deleteButton = getDeleteButton();
        deleteButton.setEnabled(annotSyms.size() > 0);
    }

    @Override
    protected void restoreButtonReset() {
        javax.swing.JButton restoreButton = getRestoreButton();
        restoreButton.setEnabled(annotSyms.size() > 0);
    }

    @Override
    protected void customButtonReset() {

    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

}
