package com.affymetrix.igb.trackOperations;

import java.awt.event.ActionEvent;
import javax.swing.DefaultComboBoxModel;
import static com.affymetrix.igb.shared.Selections.*;

public abstract class GraphPanel extends javax.swing.JPanel {
	protected boolean is_listening = true; // used to turn on and off listening to GUI events
	private javax.swing.JRadioButton hiddenRadioButton;
	/**
	 * Creates new form GraphType
	 */
	public GraphPanel() {
		hiddenRadioButton = new javax.swing.JRadioButton();
		initComponents();
		getButtonGroup1().add(hiddenRadioButton);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        graphPanel = new javax.swing.JPanel();
        graphStyleLineRadioButton = new javax.swing.JRadioButton();
        graphStyleBarRadioButton = new javax.swing.JRadioButton();
        graphStyleStairStepRadioButton = new javax.swing.JRadioButton();
        floatCheckBox = new javax.swing.JCheckBox();
        YAxisCheckBox = new javax.swing.JCheckBox();
        graphStyleHeatMapRadioButton = new javax.swing.JRadioButton();
        graphStyleDotRadioButton = new javax.swing.JRadioButton();
        graphStyleMinMaxAvgRadioButton = new javax.swing.JRadioButton();
        graphStyleHeatMapComboBox = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        labelCheckBox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(214, 190));

        graphPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));
        graphPanel.setPreferredSize(new java.awt.Dimension(155, 180));

        buttonGroup1.add(graphStyleLineRadioButton);
        graphStyleLineRadioButton.setText("Line");
        graphStyleLineRadioButton.setMinimumSize(new java.awt.Dimension(0, 0));
        graphStyleLineRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphStyleLineRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(graphStyleBarRadioButton);
        graphStyleBarRadioButton.setText("Bar");
        graphStyleBarRadioButton.setMinimumSize(new java.awt.Dimension(0, 0));
        graphStyleBarRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphStyleBarRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(graphStyleStairStepRadioButton);
        graphStyleStairStepRadioButton.setText("StairStep");
        graphStyleStairStepRadioButton.setMinimumSize(new java.awt.Dimension(0, 0));
        graphStyleStairStepRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphStyleStairStepRadioButtonActionPerformed(evt);
            }
        });

        floatCheckBox.setText("Float");
        floatCheckBox.setIconTextGap(2);
        floatCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        floatCheckBox.setMinimumSize(new java.awt.Dimension(0, 0));
        floatCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                floatCheckBoxActionPerformed(evt);
            }
        });

        YAxisCheckBox.setText("Y-axis");
        YAxisCheckBox.setIconTextGap(2);
        YAxisCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        YAxisCheckBox.setMinimumSize(new java.awt.Dimension(0, 0));
        YAxisCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YAxisCheckBoxActionPerformed(evt);
            }
        });

        buttonGroup1.add(graphStyleHeatMapRadioButton);
        graphStyleHeatMapRadioButton.setMinimumSize(new java.awt.Dimension(0, 0));
        graphStyleHeatMapRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphStyleHeatMapRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(graphStyleDotRadioButton);
        graphStyleDotRadioButton.setText("Dot");
        graphStyleDotRadioButton.setMinimumSize(new java.awt.Dimension(0, 0));
        graphStyleDotRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphStyleDotRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(graphStyleMinMaxAvgRadioButton);
        graphStyleMinMaxAvgRadioButton.setText("Min/Max/Mean");
        graphStyleMinMaxAvgRadioButton.setMinimumSize(new java.awt.Dimension(0, 0));
        graphStyleMinMaxAvgRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphStyleMinMaxAvgRadioButtonActionPerformed(evt);
            }
        });

        graphStyleHeatMapComboBox.setModel(new DefaultComboBoxModel(com.affymetrix.genometryImpl.style.HeatMap.getStandardNames()));
        graphStyleHeatMapComboBox.setMinimumSize(new java.awt.Dimension(0, 0));
        graphStyleHeatMapComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphStyleHeatMapComboBoxActionPerformed(evt);
            }
        });

        labelCheckBox.setText("Label");
        labelCheckBox.setIconTextGap(2);
        labelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout graphPanelLayout = new org.jdesktop.layout.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSeparator1)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(graphPanelLayout.createSequentialGroup()
                    .add(graphStyleHeatMapRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, 0)
                    .add(graphStyleHeatMapComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(graphStyleMinMaxAvgRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(graphPanelLayout.createSequentialGroup()
                    .add(labelCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(10, 10, 10)
                    .add(YAxisCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(10, 10, 10)
                    .add(floatCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(graphPanelLayout.createSequentialGroup()
                    .add(graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(graphStyleStairStepRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(graphStyleLineRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(50, 50, 50)
                    .add(graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(graphStyleBarRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(graphStyleDotRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, graphPanelLayout.createSequentialGroup()
                .add(graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(floatCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(YAxisCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(graphStyleLineRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(graphStyleBarRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(graphStyleStairStepRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(graphStyleDotRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(graphStyleMinMaxAvgRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(graphPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(graphStyleHeatMapRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(graphStyleHeatMapComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(graphPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 224, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(graphPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void labelCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelCheckBoxActionPerformed
        if (is_listening) {
            labelCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_labelCheckBoxActionPerformed

    private void graphStyleHeatMapComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphStyleHeatMapComboBoxActionPerformed
        if (is_listening) {
            graphStyleHeatMapComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_graphStyleHeatMapComboBoxActionPerformed

    private void graphStyleMinMaxAvgRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphStyleMinMaxAvgRadioButtonActionPerformed
        if (is_listening) {
            buttonGroup1ActionPerformedA(evt);
        }
    }//GEN-LAST:event_graphStyleMinMaxAvgRadioButtonActionPerformed

    private void graphStyleDotRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphStyleDotRadioButtonActionPerformed
        if (is_listening) {
            buttonGroup1ActionPerformedA(evt);
        }
    }//GEN-LAST:event_graphStyleDotRadioButtonActionPerformed

    private void graphStyleHeatMapRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphStyleHeatMapRadioButtonActionPerformed
        if (is_listening) {
            buttonGroup1ActionPerformedA(evt);
        }
    }//GEN-LAST:event_graphStyleHeatMapRadioButtonActionPerformed

    private void YAxisCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YAxisCheckBoxActionPerformed
        if (is_listening) {
            YAxisCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_YAxisCheckBoxActionPerformed

    private void floatCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_floatCheckBoxActionPerformed
        if (is_listening) {
            floatCheckBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_floatCheckBoxActionPerformed

    private void graphStyleStairStepRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphStyleStairStepRadioButtonActionPerformed
        if (is_listening) {
            buttonGroup1ActionPerformedA(evt);
        }
    }//GEN-LAST:event_graphStyleStairStepRadioButtonActionPerformed

    private void graphStyleBarRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphStyleBarRadioButtonActionPerformed
        if (is_listening) {
            buttonGroup1ActionPerformedA(evt);
        }
    }//GEN-LAST:event_graphStyleBarRadioButtonActionPerformed

    private void graphStyleLineRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphStyleLineRadioButtonActionPerformed
        if (is_listening) {
            buttonGroup1ActionPerformedA(evt);
        }
    }//GEN-LAST:event_graphStyleLineRadioButtonActionPerformed


	public javax.swing.JCheckBox getFloatCheckBox() {
		return floatCheckBox;
	}

	public javax.swing.JCheckBox getYAxisCheckBox() {
		return YAxisCheckBox;
	}
		
	public javax.swing.JCheckBox getLabelCheckBox() {
		return labelCheckBox;
	}
	
	public javax.swing.JPanel getGraphPanel() {
		return graphPanel;
	}

	public javax.swing.JRadioButton getGraphStyleBarRadioButton() {
		return graphStyleBarRadioButton;
	}

	public javax.swing.JRadioButton getGraphStyleDotRadioButton() {
		return graphStyleDotRadioButton;
	}

	public javax.swing.JComboBox getGraphStyleHeatMapComboBox() {
		return graphStyleHeatMapComboBox;
	}

	public javax.swing.JRadioButton getGraphStyleHeatMapRadioButton() {
		return graphStyleHeatMapRadioButton;
	}

	public javax.swing.JRadioButton getGraphStyleLineRadioButton() {
		return graphStyleLineRadioButton;
	}

	public javax.swing.JRadioButton getGraphStyleMinMaxAvgRadioButton() {
		return graphStyleMinMaxAvgRadioButton;
	}

	public javax.swing.JRadioButton getGraphStyleStairStepRadioButton() {
		return graphStyleStairStepRadioButton;
	}

	public final javax.swing.ButtonGroup getButtonGroup1() {
		return buttonGroup1;
	}
	
	protected abstract void labelCheckBoxActionPerformedA(ActionEvent evt);
	protected abstract void floatCheckBoxActionPerformedA(ActionEvent evt);
	protected abstract void YAxisCheckBoxActionPerformedA(ActionEvent evt);
	protected abstract void buttonGroup1ActionPerformedA(ActionEvent evt);
	protected abstract void graphStyleHeatMapComboBoxActionPerformedA(ActionEvent evt);

	protected abstract void labelCheckBoxReset();
	protected abstract void floatCheckBoxReset();
	protected abstract void YAxisCheckBoxReset();
	protected abstract void buttonGroup1Reset();
	protected abstract void graphStyleHeatMapComboBoxReset();
	protected final void resetAll() {
		is_listening = false;
		getGraphPanel().setEnabled(graphStates.size() > 0);
		labelCheckBoxReset();
		floatCheckBoxReset();
		YAxisCheckBoxReset();
		buttonGroup1Reset();
		graphStyleHeatMapComboBoxReset();
		is_listening = true;
	}
	
	protected void unselectGraphStyle() {
		hiddenRadioButton.setSelected(true); // deselect all visible radio buttons
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox YAxisCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox floatCheckBox;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JRadioButton graphStyleBarRadioButton;
    private javax.swing.JRadioButton graphStyleDotRadioButton;
    private javax.swing.JComboBox graphStyleHeatMapComboBox;
    private javax.swing.JRadioButton graphStyleHeatMapRadioButton;
    private javax.swing.JRadioButton graphStyleLineRadioButton;
    private javax.swing.JRadioButton graphStyleMinMaxAvgRadioButton;
    private javax.swing.JRadioButton graphStyleStairStepRadioButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox labelCheckBox;
    // End of variables declaration//GEN-END:variables
}
