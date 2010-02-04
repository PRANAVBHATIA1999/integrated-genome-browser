package com.affymetrix.igb.view.ucsc;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 * Simple Image Wrapper for the UCSC View
 * 
 * 
 * 
 * @author Ido M. Tamir
 */
public class UCSCImage extends JPanel {
    private Image image;

    public void setImage(Image image){
        this.image = image;
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image != null){
            g.drawImage(image,0,0,this);
        }
    }
    
    
    @Override
    public Dimension getPreferredSize(){
        if(image != null){
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            return new Dimension(width, height);
        }
        else{
            return super.getPreferredSize();
        }
    }



	


}
