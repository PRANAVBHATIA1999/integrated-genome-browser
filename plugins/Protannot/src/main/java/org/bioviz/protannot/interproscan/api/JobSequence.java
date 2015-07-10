/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioviz.protannot.interproscan.api;

/**
 *
 * @author jeckstei
 */
public final class JobSequence {
    private String sequenceName;
    private String proteinSequence;
    
    public JobSequence(String sequenceName, String proteinSequence) {
        setSequenceName(sequenceName);
        setProteinSequence(proteinSequence);
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public String getProteinSequence() {
        return proteinSequence;
    }

    public void setProteinSequence(String proteinSequence) {
        if(proteinSequence.endsWith("*")) {
            proteinSequence = proteinSequence.substring(0, proteinSequence.length()-1);
        }
        this.proteinSequence = proteinSequence;
    }
    
    
}
