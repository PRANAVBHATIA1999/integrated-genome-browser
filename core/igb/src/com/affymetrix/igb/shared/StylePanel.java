package com.affymetrix.igb.shared;

import com.affymetrix.genoviz.color.ColorScheme;
import com.affymetrix.genoviz.color.ColorSchemeComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author hiralv
 */
public abstract class StylePanel extends javax.swing.JPanel {
	protected boolean is_listening = true; // used to turn on and off listening to GUI events
	private static final Object[] SUPPORTED_SIZE = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
	
	/**
	 * Creates new form StylePanel
	 */
	public StylePanel() {
		initComponents();
	}

	private ColorSchemeComboBox createColorSchemeComboBox() {
		final ItemListener itemListener = new ItemListener() {

			public void itemStateChanged(ItemEvent ie) {
				switch (ie.getStateChange()) {
					case ItemEvent.DESELECTED:
						break;
					case ItemEvent.SELECTED:
						Object o = ie.getSource();
						if (o instanceof ColorSchemeComboBox) {
							ColorSchemeComboBox csb = (ColorSchemeComboBox) o;
							ColorScheme s = (ColorScheme) csb.getSelectedItem();
							ColorSchemeAction.getAction().tempAction(s);
						}
						break;
					default:
						System.err.println(
								"SchemeChoser.$ItemListener.itemStateChanged: Unexpected state change: "
								+ ie.getStateChange());
				}
			}
		};
		ColorSchemeComboBox colorSchemeBox = new ColorSchemeComboBox() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setChoices(int i) {
				this.removeItemListener(itemListener);
				super.setChoices(i);
				this.addItemListener(itemListener);
			}
		};
		colorSchemeBox.addItemListener(itemListener);
		colorSchemeBox.setChoices(0);
		return colorSchemeBox;
    }
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stylePanel = new javax.swing.JPanel();
        labelSizeComboBox = new javax.swing.JComboBox();
        colorSchemeLabel = new javax.swing.JLabel();
        colorSchemeComboBox = createColorSchemeComboBox();
        foregroundColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        backgroundColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        labelColorComboBox = new com.jidesoft.combobox.ColorComboBox();
        foregroundColorLabel = new javax.swing.JLabel();
        labelColorLabel = new javax.swing.JLabel();
        backgroundColorLabel = new javax.swing.JLabel();
        labelSizeLabel = new javax.swing.JLabel();

        stylePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Style"));

        labelSizeComboBox.setModel(new DefaultComboBoxModel(SUPPORTED_SIZE));
        labelSizeComboBox.setMinimumSize(new java.awt.Dimension(0, 0));
        labelSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelSizeComboBoxActionPerformed(evt);
            }
        });

        colorSchemeLabel.setText("Scheme");

        colorSchemeComboBox.setMinimumSize(new java.awt.Dimension(0, 0));
        colorSchemeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorSchemeComboBoxActionPerformed(evt);
            }
        });

        foregroundColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        foregroundColorComboBox.setButtonVisible(false);
        foregroundColorComboBox.setColorValueVisible(false);
        foregroundColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        foregroundColorComboBox.setStretchToFit(true);
        foregroundColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foregroundColorComboBoxActionPerformed(evt);
            }
        });

        backgroundColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        backgroundColorComboBox.setButtonVisible(false);
        backgroundColorComboBox.setColorValueVisible(false);
        backgroundColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        backgroundColorComboBox.setStretchToFit(true);
        backgroundColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundColorComboBoxActionPerformed(evt);
            }
        });

        labelColorComboBox.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 1, true));
        labelColorComboBox.setButtonVisible(false);
        labelColorComboBox.setColorValueVisible(false);
        labelColorComboBox.setMaximumSize(new java.awt.Dimension(150, 20));
        labelColorComboBox.setStretchToFit(true);
        labelColorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelColorComboBoxActionPerformed(evt);
            }
        });

        foregroundColorLabel.setText("Foreground Color");

        labelColorLabel.setText("Label Color");

        backgroundColorLabel.setText("Background Color");

        labelSizeLabel.setText("Font Size");

        org.jdesktop.layout.GroupLayout stylePanelLayout = new org.jdesktop.layout.GroupLayout(stylePanel);
        stylePanel.setLayout(stylePanelLayout);
        stylePanelLayout.setHorizontalGroup(
            stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(stylePanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(stylePanelLayout.createSequentialGroup()
                        .add(labelColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(labelColorLabel))
                    .add(stylePanelLayout.createSequentialGroup()
                        .add(backgroundColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(backgroundColorLabel))
                    .add(stylePanelLayout.createSequentialGroup()
                        .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(labelSizeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(colorSchemeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(0, 0, 0)
                        .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(colorSchemeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(stylePanelLayout.createSequentialGroup()
                        .add(foregroundColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(foregroundColorLabel)))
                .add(0, 0, 0))
        );
        stylePanelLayout.setVerticalGroup(
            stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(stylePanelLayout.createSequentialGroup()
                .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(foregroundColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(foregroundColorLabel))
                .add(12, 12, 12)
                .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(backgroundColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(backgroundColorLabel))
                .add(12, 12, 12)
                .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelColorLabel)
                    .add(labelColorComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0)
                .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelSizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(labelSizeLabel))
                .add(0, 0, 0)
                .add(stylePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(colorSchemeLabel)
                    .add(colorSchemeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(stylePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(stylePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void labelSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelSizeComboBoxActionPerformed
        if (is_listening) {
            labelSizeComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_labelSizeComboBoxActionPerformed

    private void colorSchemeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorSchemeComboBoxActionPerformed
        if (is_listening) {
            colorSchemeComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_colorSchemeComboBoxActionPerformed

    private void foregroundColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foregroundColorComboBoxActionPerformed
        if (is_listening) {
            foregroundColorComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_foregroundColorComboBoxActionPerformed

    private void backgroundColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundColorComboBoxActionPerformed
        if (is_listening) {
            backgroundColorComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_backgroundColorComboBoxActionPerformed

    private void labelColorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelColorComboBoxActionPerformed
        if (is_listening) {
            labelColorComboBoxActionPerformedA(evt);
        }
    }//GEN-LAST:event_labelColorComboBoxActionPerformed

// getters generated by Eclipse, NetBeans balked: "Read-only block of text cannot be refactored."
	public com.jidesoft.combobox.ColorComboBox getBackgroundColorComboBox() {
		return backgroundColorComboBox;
	}

	public javax.swing.JLabel getBackgroundColorLabel() {
		return backgroundColorLabel;
	}

	public com.affymetrix.genoviz.color.ColorSchemeComboBox getColorSchemeComboBox() {
		return colorSchemeComboBox;
	}

	public javax.swing.JLabel getColorSchemeLabel() {
		return colorSchemeLabel;
	}
	
	public com.jidesoft.combobox.ColorComboBox getForegroundColorComboBox() {
		return foregroundColorComboBox;
	}

	public javax.swing.JLabel getForegroundColorLabel() {
		return foregroundColorLabel;
	}

	public com.jidesoft.combobox.ColorComboBox getLabelColorComboBox() {
		return labelColorComboBox;
	}

	public javax.swing.JLabel getLabelColorLabel() {
		return labelColorLabel;
	}

	public javax.swing.JComboBox getLabelSizeComboBox() {
		return labelSizeComboBox;
	}

	public javax.swing.JPanel getStylePanel() {
		return stylePanel;
	}

	public javax.swing.JLabel getLabelSizeLabel() {
		return labelSizeLabel;
	}
	
	// you can "generate" these by copying all the event handlers
	// into your text processor and globally changing (must handle regex)
	// "ActionPerformed" to "ActionPerformedA"
	// "private" to "protected abstract"
	// "// TODO add your handling code here:" to ""
	// "                                     
	// " 
	protected abstract void labelSizeComboBoxActionPerformedA(ActionEvent evt);
	protected abstract void colorSchemeComboBoxActionPerformedA(ActionEvent evt);
	protected abstract void foregroundColorComboBoxActionPerformedA(ActionEvent evt);
	protected abstract void backgroundColorComboBoxActionPerformedA(ActionEvent evt);
	protected abstract void labelColorComboBoxActionPerformedA(ActionEvent evt);
	
	
//	protected abstract void trackNameTextFieldActionPerformedA(ActionEvent evt);
//	protected abstract void selectAllButtonActionPerformedA(ActionEvent evt);
//	protected abstract void hideButtonActionPerformedA(ActionEvent evt);
//	protected abstract void clearButtonActionPerformedA(ActionEvent evt);
//	protected abstract void restoreToDefaultButtonActionPerformedA(ActionEvent evt);
	
	// you can "generate" these by copying all the event handlers
	// into your text processor and globally changing (must handle regex)
	// "ActionPerformed" to "Reset"
	// "private" to "protected abstract"
	// "java.awt.event.ActionEvent evt" to ""
	// "// TODO add your handling code here:" to ""
	// "                                     
	// "
	protected abstract void labelSizeComboBoxReset();
	protected abstract void colorSchemeComboBoxReset();
	protected abstract void foregroundColorComboBoxReset();
	protected abstract void backgroundColorComboBoxReset();
	protected abstract void labelColorComboBoxReset();
	
//	protected abstract void strandsLabelReset();
//	protected abstract void trackNameTextFieldReset();
//	protected abstract void selectAllButtonReset();
//	protected abstract void hideButtonReset();
//	protected abstract void clearButtonReset();
//	protected abstract void restoreToDefaultButtonReset();
	
	protected final void resetAll() {
		is_listening = false;
		//getStylePanel().setEnabled(allStyles.size() > 0);
		//getAnnotationsPanel().setEnabled(annotStyles.size() > 0);
		//getGraphPanel().setEnabled(graphStates.size() > 0);		
		labelSizeComboBoxReset();
		colorSchemeComboBoxReset();
		backgroundColorComboBoxReset();
		foregroundColorComboBoxReset();
		labelColorComboBoxReset();
		
//		strandsLabelReset();
//		trackNameTextFieldReset();
//		selectAllButtonReset();
//		hideButtonReset();
//		clearButtonReset();
//		restoreToDefaultButtonReset();
		
		is_listening = true;
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jidesoft.combobox.ColorComboBox backgroundColorComboBox;
    private javax.swing.JLabel backgroundColorLabel;
    private com.affymetrix.genoviz.color.ColorSchemeComboBox colorSchemeComboBox;
    private javax.swing.JLabel colorSchemeLabel;
    private com.jidesoft.combobox.ColorComboBox foregroundColorComboBox;
    private javax.swing.JLabel foregroundColorLabel;
    private com.jidesoft.combobox.ColorComboBox labelColorComboBox;
    private javax.swing.JLabel labelColorLabel;
    private javax.swing.JComboBox labelSizeComboBox;
    private javax.swing.JLabel labelSizeLabel;
    private javax.swing.JPanel stylePanel;
    // End of variables declaration//GEN-END:variables
}
