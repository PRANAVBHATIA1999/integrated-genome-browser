package com.affymetrix.igb.trackOperations;

import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPComboBoxWithSingleListener;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import java.util.ResourceBundle;

public class TrackOperationsTabGUI extends IGBTabPanel {

	private static final long serialVersionUID = 1L;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("trackOperations");
	private static final int TAB_POSITION = 4;

	public TrackOperationsTabGUI(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("trackOperationsTab"), BUNDLE.getString("trackOperationsTab"), false, TAB_POSITION);
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stPanel = new javax.swing.JPanel();
        transformationGoB = new JRPButton("TrackOperationsTab_transformationGoB");
        transformationParam = new JRPTextField("TrackOperationsTab_transformParam");
        transformationCB = new JRPComboBoxWithSingleListener("TrackOperationsTab_transformation");
        transformationParamLabel = new javax.swing.JLabel(BUNDLE.getString("transformationLabel"));
        mtPanel = new javax.swing.JPanel();
        operationCB = new JRPComboBoxWithSingleListener("TrackOperationsTab_operation");
        operationParam = new JRPTextField("TrackOperationsTab_operationParam");
        operationGoB = new JRPButton("TrackOperationsTab_operationGoB");
        operationParamLabel = new javax.swing.JLabel(BUNDLE.getString("operationLabel"));
        btPanel = new javax.swing.JPanel();
        threshB = new JRPButton("TrackOperationsTab_threshB");
        splitB = new JRPButton("TrackOperationsTab_splitB");
        combineB = new JRPButton("TrackOperationsTab_combineB");

        stPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Single Track Operation"));

        transformationGoB.setText("Go");

        transformationParam.setEditable(false);

        transformationParamLabel.setText(null);
        transformationParamLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        transformationParamLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        transformationParamLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        org.jdesktop.layout.GroupLayout stPanelLayout = new org.jdesktop.layout.GroupLayout(stPanel);
        stPanel.setLayout(stPanelLayout);
        stPanelLayout.setHorizontalGroup(
            stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, stPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(transformationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(transformationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(transformationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(transformationGoB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
        stPanelLayout.setVerticalGroup(
            stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(stPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(transformationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(transformationGoB)
                .add(transformationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(transformationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        mtPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Multi-Track Operation"));

        operationParam.setEditable(false);

        operationGoB.setText("Go");

        operationParamLabel.setText(null);
        operationParamLabel.setMaximumSize(new java.awt.Dimension(50, 16));
        operationParamLabel.setMinimumSize(new java.awt.Dimension(50, 16));
        operationParamLabel.setPreferredSize(new java.awt.Dimension(50, 16));

        org.jdesktop.layout.GroupLayout mtPanelLayout = new org.jdesktop.layout.GroupLayout(mtPanel);
        mtPanel.setLayout(mtPanelLayout);
        mtPanelLayout.setHorizontalGroup(
            mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mtPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(operationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(operationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(operationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(operationGoB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        mtPanelLayout.setVerticalGroup(
            mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mtPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(mtPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(operationCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(operationGoB)
                    .add(operationParam, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(operationParamLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
        );

        threshB.setText("Threshhold");

        splitB.setText("Split");

        combineB.setText("Join");

        org.jdesktop.layout.GroupLayout btPanelLayout = new org.jdesktop.layout.GroupLayout(btPanel);
        btPanel.setLayout(btPanelLayout);
        btPanelLayout.setHorizontalGroup(
            btPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, btPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(combineB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(splitB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(threshB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        btPanelLayout.setVerticalGroup(
            btPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(btPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(btPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(threshB)
                    .add(splitB)
                    .add(combineB))
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(btPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, mtPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(stPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(stPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(mtPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(btPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btPanel;
    private javax.swing.JButton combineB;
    private javax.swing.JPanel mtPanel;
    private javax.swing.JComboBox operationCB;
    private javax.swing.JButton operationGoB;
    private javax.swing.JTextField operationParam;
    private javax.swing.JLabel operationParamLabel;
    private javax.swing.JButton splitB;
    private javax.swing.JPanel stPanel;
    private javax.swing.JButton threshB;
    private javax.swing.JComboBox transformationCB;
    private javax.swing.JButton transformationGoB;
    private javax.swing.JTextField transformationParam;
    private javax.swing.JLabel transformationParamLabel;
    // End of variables declaration//GEN-END:variables

	
	public javax.swing.JPanel getBtPanel() {
		return btPanel;
	}

	public javax.swing.JButton getCombineB() {
		return combineB;
	}

	public javax.swing.JPanel getMtPanel() {
		return mtPanel;
	}

	public javax.swing.JComboBox getOperationCB() {
		return operationCB;
	}

	public javax.swing.JButton getOperationGoB() {
		return operationGoB;
	}

	public javax.swing.JTextField getOperationParam() {
		return operationParam;
	}

	public javax.swing.JLabel getOperationParamLabel() {
		return operationParamLabel;
	}

	public javax.swing.JButton getSplitB() {
		return splitB;
	}

	public javax.swing.JPanel getStPanel() {
		return stPanel;
	}

	public javax.swing.JButton getThreshB() {
		return threshB;
	}

	public javax.swing.JComboBox getTransformationCB() {
		return transformationCB;
	}

	public javax.swing.JButton getTransformationGoB() {
		return transformationGoB;
	}

	public javax.swing.JTextField getTransformationParam() {
		return transformationParam;
	}

	public javax.swing.JLabel getTransformationParamLabel() {
		return transformationParamLabel;
	}
	
	@Override
	public boolean isEmbedded() {
		return true;
	}
}
