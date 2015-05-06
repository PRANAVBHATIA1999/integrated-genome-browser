package com.affymetrix.igb.shared;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.genometry.general.ID;
import com.affymetrix.genometry.general.IParameters;
import com.affymetrix.genometry.general.NewInstance;
import com.affymetrix.genometry.style.HeatMap;
import com.affymetrix.genometry.style.HeatMapExtended;
import com.affymetrix.genometry.util.IDComparator;
import com.affymetrix.genoviz.swing.NumericFilter;
import com.jidesoft.combobox.ColorComboBox;
import cytoscape.visual.ui.editors.continuous.ColorInterpolator;
import cytoscape.visual.ui.editors.continuous.GradientColorInterpolator;
import cytoscape.visual.ui.editors.continuous.GradientEditorPanel;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author hiralv
 */
@SuppressWarnings("unchecked")
public class ConfigureOptionsPanel<T extends ID & NewInstance> extends JPanel {

    private static final long serialVersionUID = 1L;

    private T returnValue, selectedCP;
    private Map<String, Object> paramMap;
    private JComboBox comboBox;
    private JPanel paramsPanel;
    private List<SelectionChangeListener> tChangeListeners;

    /**
     * Creates the reusable dialog.
     */
    public ConfigureOptionsPanel(Class clazz, Object label) {
        this(clazz, label, null);
    }

    public ConfigureOptionsPanel(Class clazz, Object label, Filter<T> filter) {
        this(clazz, label, filter, true);
    }

    public ConfigureOptionsPanel(Class clazz, Object label, Filter<T> filter, boolean includeNone) {
        init(clazz, label, filter, includeNone);
    }

    private void init(Class clazz, Object label, Filter<T> filter, boolean includeNone) throws SecurityException {
        setLayout(new MigLayout());

        comboBox = new JComboBox();
        comboBox.setRenderer(new IDListCellRenderer());

        if (includeNone) {
            comboBox.addItem(null);
        }
        TreeSet<T> tProviders = new TreeSet<>(new IDComparator());
        tProviders.addAll(ExtensionPointHandler.getExtensionPoint(clazz).getExtensionPointImpls());
        for (T cp : tProviders) {
            if (filter != null) {
                if (!filter.shouldInclude(cp)) {
                    continue;
                }
            }
            comboBox.addItem(cp);
        }
        if (includeNone) {
            comboBox.setSelectedItem(null);
        }

        JPanel optionsBox = new JPanel();
        optionsBox.setLayout(new BoxLayout(optionsBox, BoxLayout.X_AXIS));
        if (label instanceof JComponent) {
            optionsBox.add((JComponent) label);
        } else if (label instanceof String) {
            optionsBox.add(new JLabel(label + " :  "));
        }
        optionsBox.add(comboBox);

        paramsPanel = new JPanel(new MigLayout("fill"));

        add(optionsBox, "wrap");
        add(paramsPanel);

        addListeners();

        //Initialized with first value
        if (selectedCP == null && comboBox.getItemCount() > 0) {
            T cp = (T) comboBox.getItemAt(0);
            returnValue = cp;
            setSelected(cp);
        }
    }

    private void addOptions(final IParameters cp, final JPanel paramsPanel) {
        paramMap = new HashMap<>();
        JPanel panel = new JPanel(new MigLayout("fill"));

        paramsPanel.removeAll();
        if (cp != null && cp.getParametersType() != null) {
            for (Map.Entry<String, Class<?>> entry : cp.getParametersType().entrySet()) {
                final String label = entry.getKey();
                final Class<?> clazz = entry.getValue();
                final List<Object> possibleValues = cp.getParametersPossibleValues(label);
                JComponent component = null;

                if (possibleValues != null) {
                    final JComboBox cb = new JComboBox();
                    cb.setRenderer(new IDListCellRenderer());
                    possibleValues.forEach(cb::addItem);
                    cb.setSelectedItem(cp.getParameterValue(label));

                    cb.addItemListener(e -> ConfigureOptionsPanel.this.setParameter(cp, label, cb.getSelectedItem()));

                    //cb.setMaximumSize(new java.awt.Dimension(70, 60));
                    cb.setPreferredSize(new java.awt.Dimension(70, 60));
                    cb.setMinimumSize(new java.awt.Dimension(70, 60));
                    component = cb;
                } else if (Number.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz)) {
                    final JTextField tf;
                    if (Number.class.isAssignableFrom(clazz)) {
                        tf = new JTextField();
                        if (Integer.class.isAssignableFrom(clazz)) {
                            ((AbstractDocument) tf.getDocument()).setDocumentFilter(new NumericFilter.IntegerNumericFilter(Integer.MIN_VALUE, Integer.MAX_VALUE));
                        } else {
                            ((AbstractDocument) tf.getDocument()).setDocumentFilter(new NumericFilter.FloatNumericFilter());
                        }

                    } else {
                        tf = new JTextField();
                    }
                    tf.setText(String.valueOf(cp.getParameterValue(label)));
                    tf.getDocument().addDocumentListener(new DocumentListener() {
                        public void insertUpdate(DocumentEvent e) {
                            setParameter();
                        }

                        public void removeUpdate(DocumentEvent e) {
                            setParameter();
                        }

                        public void changedUpdate(DocumentEvent e) {
                            setParameter();
                        }

                        private void setParameter() {
                            if (Number.class.isAssignableFrom(clazz)) {
                                if (tf.getText() != null && tf.getText().length() > 0) {
                                    try {
                                        if (Integer.class.isAssignableFrom(clazz)) {
                                            int value = Integer.valueOf(tf.getText());
                                            ConfigureOptionsPanel.this.setParameter(cp, label, value);
                                        } else {
                                            float value = Float.valueOf(tf.getText());
                                            ConfigureOptionsPanel.this.setParameter(cp, label, value);
                                        }
                                    } catch (NumberFormatException ex) {
                                    }
                                }
                            } else {
                                ConfigureOptionsPanel.this.setParameter(cp, label, tf.getText());
                            }
                        }
                    });

//                    tf.setMaximumSize(new java.awt.Dimension(40, 20));
//                    tf.setPreferredSize(new java.awt.Dimension(40, 20));
                    tf.setMinimumSize(new java.awt.Dimension(40, 20));
                    component = tf;
                } else if (Color.class.isAssignableFrom(clazz)) {
                    final ColorComboBox colorComboBox = new ColorComboBox();
                    colorComboBox.setSelectedColor((Color) cp.getParameterValue(label));
                    colorComboBox.addItemListener(e -> setParameter(cp, label, e.getItem()));
                    colorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
                    colorComboBox.setButtonVisible(false);
                    colorComboBox.setColorValueVisible(false);
                    colorComboBox.setMaximumSize(new java.awt.Dimension(20, 20));
                    colorComboBox.setPreferredSize(new java.awt.Dimension(20, 20));
                    colorComboBox.setMinimumSize(new java.awt.Dimension(20, 20));
                    //colorComboBox.setStretchToFit(true);
                    component = colorComboBox;
                } else if (HeatMapExtended.class.isAssignableFrom(clazz)) {
                    final GradientEditorPanel editor = new GradientEditorPanel(null);
                    Object hm = cp.getParameterValue(label);
                    float[] positions;
                    Color[] colorRanges;
                    if (hm instanceof HeatMapExtended) {
                        positions = ((HeatMapExtended) hm).getValues();
                        colorRanges = ((HeatMapExtended) hm).getRangeColors();
                    } else {
                        HeatMapExtended hme = (HeatMapExtended) cp.getParameterValue(label);
                        positions = hme.getValues();
                        colorRanges = hme.getColors();
                    }
                    editor.setVirtualRange(positions, colorRanges);

                    final JButton editButton = new JButton("Edit");
                    editButton.addActionListener(evt -> {
                        editor.setTitle("Configure Heatmap");
                        editor.setModal(true);
                        editor.setAlwaysOnTop(false);
                        editor.setLocationRelativeTo(ConfigureOptionsPanel.this);
                        editor.setDefaultCloseOperation(GradientEditorPanel.DISPOSE_ON_CLOSE);
                        Object value = editor.showDialog();
                        if (value.equals(JOptionPane.OK_OPTION)) {
                            ColorInterpolator colorInterpolator = new GradientColorInterpolator(editor.getVirtualRange());
                            setParameter(cp, label, new HeatMapExtended("HeatMapExtended",
                                    colorInterpolator.getColorRange(HeatMap.BINS),
                                    editor.getVirtualRange().getVirtualValues(),
                                    editor.getVirtualRange().getColors()));
                        }
                    });
                    component = editButton;
                }

                if (component != null) {
                    panel.add(new JLabel(label), "gap rel");
                    panel.add(component, "grow");
//                    panel.add(Box.createHorizontalStrut(30));
                }

                if (panel.getComponentCount() > 4) {
                    paramsPanel.add(panel);
                    panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                    panel.add(new JLabel("                "));
                }
            }
        }

        if (panel.getComponentCount() > 0) {
            paramsPanel.add(panel, "right, grow");
        }
    }

    private void setParameter(IParameters cp, String key, Object value) {
//		boolean isValid = cp.setParameterValue(key, value);
//		okOption.setEnabled(isValid);
        paramMap.put(key, value);
    }

    private void initParamPanel(IParameters cp) {
        addOptions(cp, paramsPanel);
    }

    private void addListeners() {
        comboBox.addItemListener(e -> {
            T cp = (T) comboBox.getSelectedItem();
            // If a user selects same color provider as initial then reuses the same object
            if (returnValue != null && cp != null && cp.getName().equals(returnValue.getName())) {
                cp = returnValue;
            } else if (cp != null) {
                cp = (T) cp.newInstance();
            }
            setSelected(cp);
            if (tChangeListeners != null && !tChangeListeners.isEmpty() && cp != returnValue) {
                for (SelectionChangeListener tChangeListener : tChangeListeners) {
                    tChangeListener.selectionChanged(cp);
                }
            }
        });

    }

    private void setSelected(T cp) {
        selectedCP = cp;
        if (cp instanceof IParameters) {
            initParamPanel((IParameters) cp);
        } else {
            paramsPanel.removeAll();
        }
        paramsPanel.revalidate();
        revalidate();
    }

    public void addTChangeListner(SelectionChangeListener tcl) {
        if (tChangeListeners == null) {
            tChangeListeners = new ArrayList<>();
        }
        tChangeListeners.add(tcl);
    }

    public void removeTChangeListner(SelectionChangeListener tcl) {
        tChangeListeners.remove(tcl);
        if (tChangeListeners.isEmpty()) {
            tChangeListeners = null;
        }
    }

    @Override
    public void setEnabled(boolean b) {
        comboBox.setEnabled(b);
    }

    public void setInitialValue(T cp) {
        returnValue = cp;
        if (cp == null) {
            comboBox.setSelectedItem(cp);
        } else {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                T item = (T) comboBox.getItemAt(i);
                if (item != null && item.getName().equals(cp.getName())) {
                    comboBox.setSelectedItem(item);
                    break;
                }
            }
        }
    }

    public T getReturnValue(boolean applyChanges) {
        if (applyChanges) {
            returnValue = selectedCP;
            if (returnValue instanceof IParameters) {
                ((IParameters) returnValue).setParametersValue(paramMap);
            }
        }
        return returnValue;
    }

    public JComboBox getComboBox() {
        return comboBox;
    }

    /**
     * Interface to listen to combobox event change
     *
     * @param <T>
     */
    public static interface SelectionChangeListener<T> {

        public void selectionChanged(T t);
    }

    /**
     * Interface to filter out available
     *
     * @param <T>
     */
    public static interface Filter<T> {

        public boolean shouldInclude(T t);
    }

    private static class IDListCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return super.getListCellRendererComponent(list, "None", index, isSelected, cellHasFocus);
            }
            if (value instanceof ID) {
                return super.getListCellRendererComponent(list, ((ID) value).getDisplay(),
                        index, isSelected, cellHasFocus);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
