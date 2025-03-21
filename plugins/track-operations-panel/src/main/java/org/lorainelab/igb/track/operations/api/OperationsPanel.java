package org.lorainelab.igb.track.operations.api;

import com.affymetrix.genometry.general.IParameters;
import com.affymetrix.genometry.operator.Operator;
import com.affymetrix.genometry.operator.service.OperatorServiceRegistry;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.util.Constants;
import com.affymetrix.genometry.util.IDComparator;
import com.affymetrix.genometry.util.ThreadUtils;
import com.affymetrix.genoviz.swing.NumericFilter;
import com.affymetrix.igb.shared.Selections;
import com.affymetrix.igb.shared.Selections.RefreshSelectionListener;
import com.affymetrix.igb.shared.TrackOperationAction;
import com.affymetrix.igb.shared.TrackTransformAction;
import com.affymetrix.igb.shared.TrackUtils;
import com.affymetrix.igb.swing.JRPComboBoxWithSingleListener;
import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.track.operations.OperationsPanelGui;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

public class OperationsPanel extends OperationsPanelGui implements RefreshSelectionListener {

    private static final long serialVersionUID = 1L;

    private final Map<String, Operator> name2transformation;
    private final Map<String, Operator> name2operation;

    private String preserved_transformationCB_selection = null;
    private String preserved_operationCB_selection = null;

    private IgbService igbService;
    protected FileTypeCategory[] categories;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundle");

    public OperationsPanel() {
        name2transformation = new HashMap<>();
        name2operation = new HashMap<>();
        getTransformationCB().addItemListener(e -> setTransformationDisplay(true));
        getOperationCB().addMouseListener(new HoverEffect());
        getOperationCB().addItemListener(e -> setOperationDisplay(true));
        getTransformationParamLabel().setVisible(false);
        getTransformationParam().setVisible(false);
        getOperationParamLabel().setVisible(false);
        getOperationParam().setVisible(false);
    }

    public void init(IgbService igbService) {
        this.igbService = igbService;
        invalidate();
        resetAll(false);
        Selections.addRefreshSelectionListener(this);
    }

    public void setCategories(FileTypeCategory[] categories) {
        this.categories = categories;
    }

    public void addOperator(Operator operator) {
        resetAll(true);
    }

    public void removeOperator(Operator operator) {
        resetAll(true);
    }

    @Override
    public void selectionRefreshed() {
        resetAll(igbService.getSeqMapView().getAnnotatedSeq() != null
                && !Constants.GENOME_SEQ_ID.equals(igbService.getSeqMapView().getAnnotatedSeq().getId()));
    }

    private void resetAll(boolean enable) {
        isListening = false; // turn off propagation of events from the GUI while we modify the settings
        loadOperators(enable);
        setPanelEnabled(enable);
        isListening = true; // turn back on GUI events
    }

    public void updateViewer() {
        // set selections to empty so that options get turned off
        resetAll(false);
        SwingUtilities.invokeLater(() -> {
            igbService.getSeqMapView().updatePanel();
            resetAll(true);
        });
    }

    @Override
    protected void transformationGoBActionPerformedA(ActionEvent evt) {
        String selection = (String) getTransformationCB().getSelectedItem();
        preserved_transformationCB_selection = selection; // store selection
        Operator operator = setParameters(name2transformation.get(selection), getTransformationParam());
        (new TrackTransformAction(operator)).actionPerformed(evt);
    }

    @Override
    protected void operationGoBActionPerformedA(ActionEvent evt) {
        String selection = (String) getOperationCB().getSelectedItem();
        preserved_operationCB_selection = selection; // store selection
        Operator operator = setParameters(name2operation.get(selection), getOperationParam());
        (new TrackOperationAction(operator)).actionPerformed(evt);
    }

    private static Operator setParameters(Operator operator, JTextField paramField) {
        Operator operatorClone = operator.newInstance();

        if (operator instanceof IParameters
                && paramField.isEnabled() && paramField.getText() != null
                && paramField.getText().length() > 0) {
            Map<String, Class<?>> params = ((IParameters) operatorClone).getParametersType();
            Map<String, Object> setparams = new HashMap<>();
            setparams.put(params.keySet().iterator().next(), paramField.getText());
            ((IParameters) operatorClone).setParametersValue(setparams);
        }

        return operatorClone;
    }

    private void loadOperators(boolean enable) {
        name2transformation.clear();
        name2operation.clear();

        getTransformationCB().removeAllItems();
        getOperationCB().removeAllItems();

        if (Selections.rootSyms.isEmpty() || !enable) {
            return;
        }
        FileTypeCategory transformCategory = Selections.rootSyms.get(0).getCategory();
        for (RootSeqSymmetry rootSym : Selections.rootSyms) {
            if (transformCategory != rootSym.getCategory()) {
                transformCategory = null;
                break;
            }
        }
        boolean transformOK = transformCategory != null;
        TreeSet<Operator> operators = new TreeSet<>(new IDComparator());
        operators.addAll(OperatorServiceRegistry.getOperators());
        List<RootSeqSymmetry> transformSyms = new ArrayList<>(); // fake List to test compatibility of Transform operations
        transformSyms.add(Selections.rootSyms.get(0));
        for (Operator operator : operators) {
            if (!addThisOperator(operator)) {
                continue;
            }

            if (transformOK && TrackUtils.getInstance().checkCompatible(transformSyms, operator)) {
                name2transformation.put(operator.getDisplay(), operator);
                getTransformationCB().addItem(operator.getDisplay());
            } else if (TrackUtils.getInstance().checkCompatible(Selections.rootSyms, operator)) {
                name2operation.put(operator.getDisplay(), operator);
                getOperationCB().addItem(operator.getDisplay());
            }
        }
    }

    protected boolean addThisOperator(Operator operator) {
        for (FileTypeCategory category : categories) {
            if (operator.getOperandCountMin(category) > 0) {
                return true;
            }
        }
        return false;
    }

    public void setPanelEnabled(boolean enable) {
        isListening = false; // turn off propagation of events from the GUI while we modify the settings
        int transformCount = name2transformation.size();
        int operatorCount = name2operation.size();
        boolean enableTransformation = enable && Selections.isAllRootSeqSymmetrySame() && transformCount > 0;

        getTransformationParamLabel().setEnabled(enableTransformation);
        getTransformationCB().setEnabled(enableTransformation);

        // restore selection
        if (enableTransformation && preserved_transformationCB_selection != null) {
            getTransformationCB().setSelectedItem(preserved_transformationCB_selection);
        }

        if (!enableTransformation) {
            getTransformationCB().removeAllItems();
        }
        getTransformationGoB().setEnabled(enableTransformation);
        setTransformationDisplay(enableTransformation);
        boolean enableOperation = enable && Selections.rootSyms.size() > 1 && operatorCount > 0;

        getOperationParamLabel().setEnabled(enableOperation);
        getOperationCB().setEnabled(enableOperation);

        // restore selection
        if (enableOperation && preserved_operationCB_selection != null) {
            getOperationCB().setSelectedItem(preserved_operationCB_selection);
        }

        if (!enableOperation) {
            getOperationCB().removeAllItems();
        }
        getOperationGoB().setEnabled(enableOperation);
        setOperationDisplay(enableOperation);

        isListening = true; // turn back on GUI events
    }

    private void setTransformationDisplay(boolean enable) {
        setAtionDisplay(getTransformationCB(), getTransformationParam(), name2transformation, getTransformationGoB(), enable, true);
        invalidate();
    }

    private void setOperationDisplay(boolean enable) {
        setAtionDisplay(getOperationCB(), getOperationParam(), name2operation, getOperationGoB(), enable, false);
        invalidate();
    }

    private void setAtionDisplay(
            JComboBox ationCB,
            JTextField ationParam,
            Map<String, Operator> name2ation,
            JButton ationGoB,
            boolean enable,
            boolean singleOK
    ) {
        String selection = (String) ationCB.getSelectedItem();
        if (!enable || selection == null) {
            ationParam.setEditable(false);
            ationParam.setEnabled(false);
            ationParam.setVisible(false);
        } else {
            Operator operator = name2ation.get(selection);
//			ationGoB.setToolTipText(getTooltipMessage(operator));
            Map<String, Class<?>> params = operator instanceof IParameters ? ((IParameters) operator).getParametersType() : null;
            if (params == null || params.isEmpty() || (!singleOK && Selections.rootSyms.size() < 2)) {
                ationParam.setEditable(false);
                ationParam.setEnabled(false);
                ationParam.setVisible(false);
            } else {
                Entry<String, Class<?>> param = params.entrySet().iterator().next();
                if (Integer.class.isAssignableFrom(param.getValue())) {
                    ((AbstractDocument) ationParam.getDocument()).setDocumentFilter(new NumericFilter.IntegerNumericFilter());
                } else if (Number.class.isAssignableFrom(param.getValue())) {
                    ((AbstractDocument) ationParam.getDocument()).setDocumentFilter(new NumericFilter.FloatNumericFilter());
                }
                ationParam.setEditable(true);
                ationParam.setText("");
                ationParam.setEnabled(true);
                ationParam.setVisible(true);
                getStPanel().revalidate();
            }
        }
    }

    /**
     * get the error message text for an attempted graph/annotation operation
     *
     * @param graphCount the number of graph glyphs
     * @param minCount the minimum graphs for the operator
     * @param maxCount the maximum graphs for the operator
     * @return the error message text
     */
    private static final String selectExactGraphsMessage = BUNDLE.getString("operatorExactTooltip");
    private static final String selectMinGraphsMessage = BUNDLE.getString("operatorMinTooltip");
    private static final String selectMaxGraphsMessage = BUNDLE.getString("operatorMaxTooltip");
    private static final String selectRangeGraphsMessage = BUNDLE.getString("operatorRangeTooltip");

    public static String getTooltipMessage(Operator operator) {
        if (operator == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (FileTypeCategory category : FileTypeCategory.values()) {
            int minCount = operator.getOperandCountMin(category);
            int maxCount = operator.getOperandCountMax(category);
            String categoryName = category.toString(); // not translated for now
            if (maxCount > 0) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                if (minCount == maxCount) {
                    sb.append(MessageFormat.format(selectExactGraphsMessage, minCount, categoryName));
                } else if (minCount == 0) {
                    sb.append(MessageFormat.format(selectMaxGraphsMessage, maxCount, categoryName));
                } else if (maxCount == Integer.MAX_VALUE) {
                    sb.append(MessageFormat.format(selectMinGraphsMessage, minCount, categoryName));
                } else {
                    sb.append(MessageFormat.format(selectRangeGraphsMessage, minCount, maxCount, categoryName));
                }
            }
        }
        return sb.toString();
    }

    private class HoverEffect implements MouseListener {

        private String A = null;
        private String B = null;

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
            JRPComboBoxWithSingleListener comp = (JRPComboBoxWithSingleListener) e.getComponent();
            String selection = (String) comp.getSelectedItem();
            Operator operator = name2operation.get(selection);
            if (operator == null) {
                return;
            }

            if (Selections.rootSyms.size() >= operator.getOperandCountMin(FileTypeCategory.Graph)
                    && Selections.rootSyms.size() <= operator.getOperandCountMax(FileTypeCategory.Graph)) {
                setGraphName(comp, operator);
            } else {
                comp.setToolTipText(getTooltipMessage(operator));
            }
        }

        public void mouseExited(MouseEvent e) {
            JRPComboBoxWithSingleListener comp = (JRPComboBoxWithSingleListener) e.getComponent();
            String selection = (String) comp.getSelectedItem();
            unsetGraphName(name2operation.get(selection));
        }

        public void setGraphName(JRPComboBoxWithSingleListener comp, Operator operator) {
            if (operator != null && operator.getOperandCountMin(FileTypeCategory.Graph) == 2 && operator.getOperandCountMax(FileTypeCategory.Graph) == 2) {
                A = ((GraphSym) Selections.rootSyms.get(0)).getGraphName();
                B = ((GraphSym) Selections.rootSyms.get(1)).getGraphName();

                ((GraphSym) Selections.rootSyms.get(0)).setGraphName("A");
                ((GraphSym) Selections.rootSyms.get(1)).setGraphName("B");

                comp.setToolTipText(null);
                ThreadUtils.runOnEventQueue(() -> igbService.getSeqMap().updateWidget());
            }
        }

        public void unsetGraphName(Operator operator) {
            if (operator != null && operator.getOperandCountMin(FileTypeCategory.Graph) == 2 && operator.getOperandCountMax(FileTypeCategory.Graph) == 2) {
                if (A != null && B != null && Selections.rootSyms.size() > 1) {
                    ((GraphSym) Selections.rootSyms.get(0)).setGraphName(A);
                    ((GraphSym) Selections.rootSyms.get(1)).setGraphName(B);

                    ThreadUtils.runOnEventQueue(() -> igbService.getSeqMap().updateWidget());
                    A = null;
                    B = null;

                }
            }
        }
    }
}
