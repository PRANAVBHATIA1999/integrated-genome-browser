/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.tiers;

import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.action.ChangeExpandMaxActionA;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author blossome
 */
public class MaxSlotsChooser extends javax.swing.JFrame {

	private Integer optimum = 5;
	private final Integer unlimitted = 0;
	private Integer initial = 0;
	private String theMessage ="";
	private ChangeExpandMaxActionA ac;
	/**
	 * Creates new form MaxSlotsChooser
	 */
	public MaxSlotsChooser(String theMessage, int theInitialValue, int theOptimalValue, ChangeExpandMaxActionA ac) {
		this.theMessage = theMessage;
		this.initial = theInitialValue;
		this.optimum = theOptimalValue;
		this.ac = ac;
		setLocationRelativeTo(IGB.getSingleton().getFrame());
		init();
	}
	private void init(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				setLocationRelativeTo(IGB.getSingleton().getFrame());
				initComponents();
				pack();
			}
		});
	}
	@Override
	public String toString() {
		return this.maxSlots.getText();
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        Unlimitter = new javax.swing.JButton();
        Optimizer = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        message = new javax.swing.JLabel();
        maxSlots = new javax.swing.JFormattedTextField(new java.text.DecimalFormat("###0"));

        Unlimitter.setText("Unlimited");
        Unlimitter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                UnlimitterMouseClicked(evt);
            }
        });
        Unlimitter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                unlimitterKeyPressed(evt);
            }
        });

        Optimizer.setText("Optimal : "+ optimum);
        Optimizer.setToolTipText("Deep enough to fit the deepest stack visible now.");
        Optimizer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                OptimizerMouseClicked(evt);
            }
        });
        Optimizer.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                optimizerKeyPressed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                okButtonMouseClicked(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancelButtonMouseClicked(evt);
            }
        });

        message.setText(theMessage);

        maxSlots.setColumns(5);
        maxSlots.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        maxSlots.setText(""+initial);
        maxSlots.setToolTipText("An integer.");
        maxSlots.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                maxSlotsKeyPressed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(okButton)
                        .add(18, 18, 18)
                        .add(cancelButton)
                        .add(38, 38, 38))
                    .add(message, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(maxSlots, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(37, 37, 37)
                        .add(Optimizer)
                        .add(18, 18, 18)
                        .add(Unlimitter)
                        .addContainerGap(33, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(message)
                .add(11, 11, 11)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(Unlimitter)
                    .add(Optimizer)
                    .add(maxSlots, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	public MaxSlotsChooser(JTextField maxSlots) {
		this.maxSlots = maxSlots;
	}

	private void UnlimitterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_UnlimitterMouseClicked
		maxSlots.setText(this.unlimitted.toString());
	}//GEN-LAST:event_UnlimitterMouseClicked

	private void OptimizerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OptimizerMouseClicked
		maxSlots.setText(this.optimum.toString());
	}//GEN-LAST:event_OptimizerMouseClicked

	private void okButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseClicked
		dispose();
		change();
		
	}//GEN-LAST:event_okButtonMouseClicked

	private void cancelButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseClicked
		maxSlots.setText(this.initial.toString());
		dispose();
	}//GEN-LAST:event_cancelButtonMouseClicked

	private void maxSlotsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxSlotsKeyPressed
		if(evt.getKeyCode() == 10){
			dispose();
			change();
		}
	}//GEN-LAST:event_maxSlotsKeyPressed

	private void optimizerKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_optimizerKeyPressed
		if(evt.getKeyCode() == 10){
			dispose();
			change();
		}
	}//GEN-LAST:event_optimizerKeyPressed

	private void unlimitterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_unlimitterKeyPressed
		if(evt.getKeyCode() == 10){
			dispose();
			change();
		}
	}//GEN-LAST:event_unlimitterKeyPressed
	public void change(){
		try{
			ac.changeExpandMax(Integer.parseInt(maxSlots.getText()));
		}
		catch(NumberFormatException e){
			ErrorHandler.errorPanel(e.getLocalizedMessage()
							+ " Maximum must be an integer: "
							+ this.toString());
		}
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Optimizer;
    private javax.swing.JButton Unlimitter;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField maxSlots;
    private javax.swing.JLabel message;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
