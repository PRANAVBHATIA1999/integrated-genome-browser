package cytoscape.visual.ui.editors.continuous;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author kono
 */
public class MinMaxDialog extends javax.swing.JDialog {

//	private static final Font TEXTBOX_FONT = new java.awt.Font("SansSerif", 1, 10);
    private static final long serialVersionUID = 7350824820761046009L;

    private static MinMaxDialog dialog;

    /**
     * Creates new form MinMaxDialog
     */
    private MinMaxDialog(Frame parent, boolean modal, Float min, Float max, final String attrName) {
        super(parent, modal);
        this.min = min;
        this.max = max;
        this.defaultMin = min;
        this.defaultMax = max;
        this.attrName = attrName;
        initComponents();

        this.minTextField.setText(min.toString());
        this.maxTextField.setText(max.toString());
    }

    private Float min, defaultMin;
    private Float max, defaultMax;
    private final NumericFilter numericFilter = new NumericFilter() {
        @Override
        protected void valueUpdated() {
            float min, max;
            try {
                min = Float.valueOf(minTextField.getText());
                max = Float.valueOf(maxTextField.getText());
                okButton.setEnabled(max > min);
            } catch (NumberFormatException e) {
                okButton.setEnabled(false);
            }
        }
    };

    private String attrName;

    /**
     * DOCUMENT ME!
     *
     * @param min DOCUMENT ME!
     * @param max DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Float[] getMinMax(Frame frame, float min, float max, final String attrName) {
        final Float[] minMax = new Float[2];

        dialog = new MinMaxDialog(frame, true, min, max, attrName);
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        if ((dialog.min == null) || (dialog.max == null)) {
            return null;
        }

        minMax[0] = dialog.min;
        minMax[1] = dialog.max;

        return minMax;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        minLabel = new javax.swing.JLabel();
        maxLabel = new javax.swing.JLabel();
        minTextField = new javax.swing.JTextField();
//		minTextField.setFont(TEXTBOX_FONT);
        ((AbstractDocument) minTextField.getDocument()).setDocumentFilter(numericFilter);

        maxTextField = new javax.swing.JTextField();
//		maxTextField.setFont(TEXTBOX_FONT);
        ((AbstractDocument) maxTextField.getDocument()).setDocumentFilter(numericFilter);

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
//		titlePanel = new javax.swing.JPanel();
//		titleLabel = new javax.swing.JLabel();

        restoreButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set Range");
        setAlwaysOnTop(true);
        setResizable(false);

//		minLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
        minLabel.setText("Min");

//		maxLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
        maxLabel.setText("Max");

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.addActionListener(this::okButtonActionPerformed);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        restoreButton.setText("Restore");
        restoreButton.setToolTipText("Set range by current attribute's min and max.");
        restoreButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        restoreButton.addActionListener(this::restoreButtonActionPerformed);

//		titlePanel.setBackground(new java.awt.Color(255, 255, 255));
//		titleLabel.setFont(new java.awt.Font("SansSerif", 1, 14));
//		titleLabel.setText("Set Value Range");
//
//		org.jdesktop.layout.GroupLayout titlePanelLayout = new org.jdesktop.layout.GroupLayout(titlePanel);
//		titlePanel.setLayout(titlePanelLayout);
//		titlePanelLayout.setHorizontalGroup(titlePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//		                                                    .add(titlePanelLayout.createSequentialGroup()
//		                                                                         .addContainerGap()
//		                                                                         .add(titleLabel)
//		                                                                         .addContainerGap(125,
//		                                                                                          Short.MAX_VALUE)));
//		titlePanelLayout.setVerticalGroup(titlePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//		                                                  .add(titlePanelLayout.createSequentialGroup()
//		                                                                       .addContainerGap()
//		                                                                       .add(titleLabel)
//		                                                                       .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
//		                                                                                        Short.MAX_VALUE)));
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                //		                                .add(titlePanel,
                //		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                //		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                //		                                     Short.MAX_VALUE)
                .add(layout.createSequentialGroup().addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(minLabel,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                        35,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(maxLabel,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                        35, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(minTextField,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                        182, Short.MAX_VALUE)
                                .add(maxTextField,
                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                        182, Short.MAX_VALUE))
                        .addContainerGap())
                .add(org.jdesktop.layout.GroupLayout.TRAILING,
                        layout.createSequentialGroup()
                        .addContainerGap(163, Short.MAX_VALUE)
                        .add(cancelButton)
                        .add(restoreButton)
                        .add(okButton).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                        //		                                         .add(titlePanel,
                        //		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                        //		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                        //		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(minLabel)
                                .add(minTextField,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                        30,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(maxLabel)
                                .add(maxTextField,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
                                        30,
                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton).add(restoreButton).add(cancelButton))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)));

        pack();
    } // </editor-fold>

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            min = Float.valueOf(minTextField.getText());
            max = Float.valueOf(maxTextField.getText());
        } catch (NumberFormatException e) {
            min = null;
            max = null;
        }

        dispose();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        min = null;
        max = null;
        dispose();
    }

    private void restoreButtonActionPerformed(ActionEvent evt) {
        minTextField.setText(defaultMin.toString());
        maxTextField.setText(defaultMax.toString());
    }

    // Variables declaration - do not modify
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel maxLabel;
    private javax.swing.JTextField maxTextField;
    private javax.swing.JLabel minLabel;
    private javax.swing.JTextField minTextField;
    private javax.swing.JButton okButton;
//	private javax.swing.JLabel titleLabel;
//	private javax.swing.JPanel titlePanel;

    private JButton restoreButton;
    // End of variables declaration

    private static abstract class NumericFilter extends DocumentFilter {

        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset,
                String text, AttributeSet attr) throws BadLocationException {
            fb.insertString(offset, text.replaceAll(getRegex(), ""), attr);
            valueUpdated();
        }

        // no need to override remove(): inherited version allows all removals
        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
                String text, AttributeSet attr) throws BadLocationException {
            fb.replace(offset, length, text.replaceAll(getRegex(), ""), attr);
            valueUpdated();
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws
                BadLocationException {
            super.remove(fb, offset, length);
            valueUpdated();
        }

        protected String getRegex() {
            return "[^0-9\\.\\-]";
        }

        protected abstract void valueUpdated();
    }
}
