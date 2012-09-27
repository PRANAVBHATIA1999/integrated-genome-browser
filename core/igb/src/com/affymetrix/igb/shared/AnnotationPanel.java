package com.affymetrix.igb.shared;

import java.awt.event.ActionEvent;

/**
 *
 * @author hiralv
 */
public abstract class AnnotationPanel extends javax.swing.JPanel {
	protected boolean is_listening = true; // used to turn on and off listening to GUI events
	
	/**
	 * Creates new form AnnotationPanel
	 */
	public AnnotationPanel() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        annotationsPanel = new javax.swing.JPanel();
        stackDepthLabel = new javax.swing.JLabel();
        stackDepthTextField = new com.affymetrix.genoviz.swing.recordplayback.JRPNumTextField("trackPreference_maxDepth");
        labelFieldLabel = new javax.swing.JLabel();
        labelFieldComboBox = new javax.swing.JComboBox();
        strands2TracksCheckBox = new javax.swing.JCheckBox();
        strandsArrowCheckBox = new javax.swing.JCheckBox();
        strandsColorCheckBox = new javax.swing.JCheckBox();
        strandsLabel = new javax.swing.JLabel();
        strandsForwardColorLabel = new javax.swing.JLabel();
        strandsReverseColorLabel = new javax.swing.JLabel();
        strandsReverseColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        strandsForwardColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        stackDepthGoButton = new javax.swing.JButton();
        stackDepthAllButton = new javax.swing.JButton(com.affymetrix.igb.action.ChangeExpandMaxOptimizeAction.getAction());

        annotationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Annotations"));

        stackDepthLabel.setText("Depth");

        stackDepthTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stackDepthTextFieldActionPerformed(evt);
            }
        });

        labelFieldLabel.setText("Label Field");

        labelFieldComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelFieldComboBoxActionPerformed(evt);
            }
        });

        strands2TracksCheckBox.setText("2 track");
        strands2TracksCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strands2TracksCheckBoxActionPerformed(evt);
            }
        });

        strandsArrowCheckBox.setText("Arrow");
        strandsArrowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strandsArrowCheckBoxActionPerformed(evt);
            }
        });

        strandsColorCheckBox.setText("Color:");
        strandsColorCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strandsColorCheckBoxActionPerformed(evt);
            }
        });

        strandsLabel.setText("+/- Strand Options:");

        strandsForwardColorLabel.setText("+");

        strandsReverseColorLabel.setText("-");

        strandsReverseColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        strandsReverseColorComboBox.setButtonVisible(false);
        strandsReverseColorComboBox.setColorValueVisible(false);
        strandsReverseColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        strandsReverseColorComboBox.setStretchToFit(true);
        strandsReverseColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strandsReverseColorComboBoxActionPerformed(evt);
            }
        });

        strandsForwardColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        strandsForwardColorComboBox.setButtonVisible(false);
        strandsForwardColorComboBox.setColorValueVisible(false);
        strandsForwardColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        strandsForwardColorComboBox.setStretchToFit(true);
        strandsForwardColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strandsForwardColorComboBoxActionPerformed(evt);
            }
        });

        stackDepthGoButton.setText("Go");
        stackDepthGoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        stackDepthGoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stackDepthGoButtonActionPerformed(evt);
            }
        });

        stackDepthAllButton.setText("All");
        stackDepthAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stackDepthAllButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout annotationsPanelLayout = new org.jdesktop.layout.GroupLayout(annotationsPanel);
        annotationsPanel.setLayout(annotationsPanelLayout);
        annotationsPanelLayout.setHorizontalGroup(
            annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(annotationsPanelLayout.createSequentialGroup()
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(strandsLabel)
                    .add(annotationsPanelLayout.createSequentialGroup()
                        .add(strandsColorCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(strandsForwardColorLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(strandsForwardColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(10, 10, 10)
                        .add(strandsReverseColorLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(strandsReverseColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(annotationsPanelLayout.createSequentialGroup()
                        .add(strands2TracksCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(strandsArrowCheckBox)))
                .addContainerGap())
            .add(annotationsPanelLayout.createSequentialGroup()
                .add(labelFieldLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(labelFieldComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(annotationsPanelLayout.createSequentialGroup()
                .add(stackDepthLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stackDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stackDepthGoButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(stackDepthAllButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
        );
        annotationsPanelLayout.setVerticalGroup(
            annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(annotationsPanelLayout.createSequentialGroup()
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stackDepthLabel)
                    .add(stackDepthTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(stackDepthGoButton)
                    .add(stackDepthAllButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelFieldLabel)
                    .add(labelFieldComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(strandsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(strands2TracksCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(strandsArrowCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(annotationsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(strandsColorCheckBox)
                    .add(strandsForwardColorLabel)
                    .add(strandsForwardColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(strandsReverseColorLabel)
                    .add(strandsReverseColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(annotationsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(annotationsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 162, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void stackDepthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackDepthTextFieldActionPerformed
        if (is_listening) {
            stackDepthTextFieldActionPerformedA(evt);
        }
    }//GEN-LAST:event_stackDepthTextFieldActionPerformed

    private void labelFieldComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelFieldComboBoxActionPerformed
        if (is_listening) {
            labelFieldComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_labelFieldComboBoxActionPerformed

    private void strands2TracksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strands2TracksCheckBoxActionPerformed
        if (is_listening) {
            strands2TracksCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strands2TracksCheckBoxActionPerformed

    private void strandsArrowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsArrowCheckBoxActionPerformed
        if (is_listening) {
            strandsArrowCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsArrowCheckBoxActionPerformed

    private void strandsColorCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsColorCheckBoxActionPerformed
        if (is_listening) {
            strandsColorCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsColorCheckBoxActionPerformed

    private void strandsReverseColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsReverseColorComboBoxActionPerformed
        if (is_listening) {
            strandsReverseColorComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsReverseColorComboBoxActionPerformed

    private void strandsForwardColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strandsForwardColorComboBoxActionPerformed
        if (is_listening) {
            strandsForwardColorComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_strandsForwardColorComboBoxActionPerformed

    private void stackDepthGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackDepthGoButtonActionPerformed
        if (is_listening) {
            stackDepthGoButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_stackDepthGoButtonActionPerformed

    private void stackDepthAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackDepthAllButtonActionPerformed
        if (is_listening) {
            stackDepthAllButtonActionPerformedA(evt);
        }
    }//GEN-LAST:event_stackDepthAllButtonActionPerformed

// getters generated by Eclipse, NetBeans balked: "Read-only block of text cannot be refactored."
	public javax.swing.JPanel getAnnotationsPanel() {
		return annotationsPanel;
	}

	public javax.swing.JComboBox getLabelFieldComboBox() {
		return labelFieldComboBox;
	}

	public javax.swing.JLabel getLabelFieldLabel() {
		return labelFieldLabel;
	}

	public javax.swing.JLabel getStackDepthLabel() {
		return stackDepthLabel;
	}

	public javax.swing.JTextField getStackDepthTextField() {
		return stackDepthTextField;
	}

	public javax.swing.JCheckBox getStrands2TracksCheckBox() {
		return strands2TracksCheckBox;
	}

	public javax.swing.JCheckBox getStrandsArrowCheckBox() {
		return strandsArrowCheckBox;
	}

	public javax.swing.JCheckBox getStrandsColorCheckBox() {
		return strandsColorCheckBox;
	}

	public com.jidesoft.combobox.ColorComboBox getStrandsForwardColorComboBox() {
		return strandsForwardColorComboBox;
	}

	public javax.swing.JLabel getStrandsForwardColorLabel() {
		return strandsForwardColorLabel;
	}

	public javax.swing.JLabel getStrandsLabel() {
		return strandsLabel;
	}

	public com.jidesoft.combobox.ColorComboBox getStrandsReverseColorComboBox() {
		return strandsReverseColorComboBox;
	}

	public javax.swing.JLabel getStrandsReverseColorLabel() {
		return strandsReverseColorLabel;
	}

	public javax.swing.JButton getStackDepthAllButton() {
		return stackDepthAllButton;
	}

	public javax.swing.JButton getStackDepthGoButton() {
		return stackDepthGoButton;
	}

	// you can "generate" these by copying all the event handlers
	// into your text processor and globally changing (must handle regex)
	// "ActionPerformed" to "ActionPerformedA"
	// "private" to "protected abstract"
	// "// TODO add your handling code here:" to ""
	// "                                     
	// "       
	protected abstract void stackDepthTextFieldActionPerformedA(ActionEvent evt);
	protected abstract void labelFieldComboBoxActionPerformedA(ActionEvent evt);
	protected abstract void strands2TracksCheckBoxActionPerformedA(ActionEvent evt);
	protected abstract void strandsArrowCheckBoxActionPerformedA(ActionEvent evt);
	protected abstract void strandsColorCheckBoxActionPerformedA(ActionEvent evt);
	protected abstract void strandsReverseColorComboBoxActionPerformedA(ActionEvent evt);
	protected abstract void strandsForwardColorComboBoxActionPerformedA(ActionEvent evt);
	protected abstract void stackDepthGoButtonActionPerformedA(ActionEvent evt);
	protected abstract void stackDepthAllButtonActionPerformedA(ActionEvent evt);
	
	
	// you can "generate" these by copying all the event handlers
	// into your text processor and globally changing (must handle regex)
	// "ActionPerformed" to "Reset"
	// "private" to "protected abstract"
	// "java.awt.event.ActionEvent evt" to ""
	// "// TODO add your handling code here:" to ""
	// "                                     
	// "
	protected abstract void stackDepthTextFieldReset();
	protected abstract void labelFieldComboBoxReset();
	protected abstract void strands2TracksCheckBoxReset();
	protected abstract void strandsArrowCheckBoxReset();
	protected abstract void strandsColorCheckBoxReset();
	protected abstract void strandsReverseColorComboBoxReset();
	protected abstract void strandsForwardColorComboBoxReset();
	protected abstract void stackDepthGoButtonReset();
	protected abstract void stackDepthAllButtonReset();
	
	

	
	protected final void resetAll() {
		is_listening = false;
		//getStylePanel().setEnabled(allStyles.size() > 0);
		//getAnnotationsPanel().setEnabled(annotStyles.size() > 0);
		//getGraphPanel().setEnabled(graphStates.size() > 0);
		stackDepthTextFieldReset();
		labelFieldComboBoxReset();
		strands2TracksCheckBoxReset();
		strandsArrowCheckBoxReset();
		strandsColorCheckBoxReset();
		strandsReverseColorComboBoxReset();
		strandsForwardColorComboBoxReset();
		stackDepthGoButtonReset();
		stackDepthAllButtonReset();
			
		is_listening = true;
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel annotationsPanel;
    private javax.swing.JComboBox labelFieldComboBox;
    private javax.swing.JLabel labelFieldLabel;
    private javax.swing.JButton stackDepthAllButton;
    private javax.swing.JButton stackDepthGoButton;
    private javax.swing.JLabel stackDepthLabel;
    private javax.swing.JTextField stackDepthTextField;
    private javax.swing.JCheckBox strands2TracksCheckBox;
    private javax.swing.JCheckBox strandsArrowCheckBox;
    private javax.swing.JCheckBox strandsColorCheckBox;
    private com.jidesoft.combobox.ColorComboBox strandsForwardColorComboBox;
    private javax.swing.JLabel strandsForwardColorLabel;
    private javax.swing.JLabel strandsLabel;
    private com.jidesoft.combobox.ColorComboBox strandsReverseColorComboBox;
    private javax.swing.JLabel strandsReverseColorLabel;
    // End of variables declaration//GEN-END:variables
}
