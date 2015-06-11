/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.swing;

/**
 *
 * @author tarun
 */
public interface WeightedJRPWidget extends JRPWidget, Comparable<WeightedJRPWidget> {

    public int getWeight();

    @Override
    public default int compareTo(WeightedJRPWidget o) {
        return this.getWeight() - o.getWeight();
    }
    
}
