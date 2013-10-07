/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.bamindexer;

import com.affymetrix.igb.action.ShowConsoleAction;
import static com.affymetrix.igb.bamindexer.BAMIndexer.CAT;
import static com.affymetrix.igb.bamindexer.BAMIndexer.DEBUG;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;

/**
 *
 * @author ktsuttle
 */
public class BAMIndexerProgress extends javax.swing.JFrame {

    private final BAMIndexer backend;
    private double stepSize;
    private double min = 0;
    private double max = 100;
    private Double[] jobs = null;
    private int iCompleteJobs = 0;

    public void setNewTasks(double tasks) throws Exception {
        if (iCompleteJobs >= jobs.length) {
            throw new Exception("This progress bar can not have any more jobs!");
        }
        this.stepSize = (max * jobs[iCompleteJobs]) / tasks;
    }

    public void finishedTask() {
        double newValue = jProgressBar1.getValue() + stepSize;


        if (jProgressBar1.isIndeterminate()) {
            jProgressBar1.setIndeterminate(false);
        }
        jProgressBar1.setValue((int) newValue);
        isDone(false);

    }

    public void updateTitle(Object... args) {
        jLabel1.setText(DEBUG(CAT(BAMIndexer.TAB.HEADER, args)).trim());
    }
    
    private Double sum(Number[] arr){
        return sum(arr,0,arr.length-1);
    }
    private Double sum(Number[] arr, int start, Integer end){
        Double answer = new Double(0);
        for(Double value: (Double[]) Arrays.copyOfRange(arr,start,end+1)){
            answer += value;
        }
        return answer;
    }

    public boolean isDone(boolean force) {
        if (jProgressBar1.getValue() >= sum(jobs, 0, iCompleteJobs)) {
            iCompleteJobs++;
        }
        if (jProgressBar1.getValue() >= this.max) {
			this.jButton1.setText("Close");  
            this.updateProgressBarMessage("No Errors");
			this.updateTitle("Completed");
			this.updateStatus("OK to close");
			          
            this.repaint();
            return true;
        }
		if(force){
            jButton1.setText("Log");
            this.updateProgressBarMessage("Errors exist!");
			this.updateTitle("inComplete with errors");
            this.updateStatus("Check the log");
            this.repaint();
            return true;
		}
        return false;
    }

    public void updateStatus(Object... args) {
        String message = DEBUG(args).trim();
        this.jLabel2.setText(message);
    }

    public Double[] getRange() {
        return new Double[]{min, max};
    }

    public BAMIndexerProgress() throws Exception {
        this(null, null);
    }
	
	public void cancel(){
		this.updateTitle("Canceled. You may now close the window.");
		this.updateStatus("canceled");
		this.jButton1.setText("Close");
		this.updateProgressBarMessage("");
	}

    /**
     * Creates new form BAMIndexerFrame
     */
    public BAMIndexerProgress(BAMIndexer backend, Double[] jobs) throws Exception {
        initComponents();
        this.backend = backend;
        
        if(sum(jobs) != 1){
            throw new Exception("Job partitions should equal 100%");
        }
        this.jobs = jobs;
        

        jProgressBar1.setMinimum((int) min);
        jProgressBar1.setMaximum((int) max);
        jProgressBar1.setIndeterminate(true);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jProgressBar1.setString("");
        jProgressBar1.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        getContentPane().add(jProgressBar1, gridBagConstraints);

        jLabel1.setText("Initializing");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        getContentPane().add(jLabel1, gridBagConstraints);

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        getContentPane().add(jButton1, gridBagConstraints);

        jLabel2.setText(">");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(jLabel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String message = ((JButton) evt.getSource()).getText();
		if (message.equalsIgnoreCase("close")) {
            this.dispose();
        } else if (message.equalsIgnoreCase("log")){
			ShowConsoleAction.getAction().actionPerformed(evt);
		}else{
            backend.cancel();
            this.dispose();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


//                final BAMProgress it = new BAMProgress(null, 1);
//
//        it.setVisible(true);
//
//        // run a loop to demonstrate raising
//        for (int i = it.min; i <= it.max; i++) {
//            final int percent = i;
//            try {
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        it.updateBar(percent);
//                    }
//                });
//                java.lang.Thread.sleep(100);
//            } catch (InterruptedException e) {
//                ;
//            }
//        }
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BAMIndexerProgress.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BAMIndexerProgress.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BAMIndexerProgress.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BAMIndexerProgress.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new BAMIndexerProgress().setVisible(true);
                } catch (Exception ex) {
                    Logger.getLogger(BAMIndexerProgress.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables

	void updateProgressBarMessage(String message) {
		this.jProgressBar1.setStringPainted(true);
		this.jProgressBar1.setString(message);
	}

}
