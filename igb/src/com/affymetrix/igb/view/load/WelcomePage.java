/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainWorkspaceManager2.java
 *
 * Created on Nov 9, 2011, 2:54:08 PM
 */
package com.affymetrix.igb.view.load;

import be.pwnt.jflow.JFlowPanel;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import com.affymetrix.common.CommonUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * 
 * This JPanel composes the welcome page.  The bottom panel is filled with a 
 * CoverFlow class.
 * 
 * @author jfvillal
 */
public class WelcomePage extends javax.swing.JPanel {
	private static final long serialVersionUID = 1L;
	/** Creates new form MainWorkspaceManager2 */
	public WelcomePage( JPanel cover_flow) {
		initComponents();
		final JFlowPanel flow_panel = (JFlowPanel) cover_flow;
		CoverFlowPane.setLayout( new BorderLayout());
		CoverFlowPane.add( cover_flow );
		URL url = CommonUtils.class.getClassLoader().getResource("welcome.html");
		WelcomePane.setContentType("text/html");
		try {
			WelcomePane.setText( getContent(this, url.openStream()));
		} catch (IOException ex) {
			Logger.getLogger(WelcomePage.class.getName()).log(Level.SEVERE, null, ex);
		}
		UIManager.put ("Button.select", Color.TRANSLUCENT ) ; 
		try {
			ImageIcon icon = createImageIcon(
					CommonUtils.class.getClassLoader().getResource("images/wright.png")
					);
			RightSlide.setIcon( icon );
			ImageIcon icon2 = createImageIcon(
					CommonUtils.class.getClassLoader().getResource("images/right_selected.png")
					);
			RightSlide.setPressedIcon( icon2);
			RightSlide.setSelectedIcon(icon2);
			RightSlide.setRolloverEnabled(true); // turn on before rollovers work
			RightSlide.setRolloverIcon(icon2);
			RightSlide.setBorderPainted(false);
			RightSlide.setFocusPainted(false);
			RightSlide.setContentAreaFilled(false);
			RightSlide.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					flow_panel.shiftBy( SHIFT_BY  );
				}
			});;
			ImageIcon icon3 = createImageIcon(
					CommonUtils.class.getClassLoader().getResource("images/right_more_selected.png")
					);
			RightSlide.setPressedIcon(icon3 );
		} catch (IOException ex) {
			Logger.getLogger(WelcomePage.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		
		try {
			ImageIcon icon = createImageIcon(
					CommonUtils.class.getClassLoader().getResource("images/wleft.png")
					);
			LeftSlide.setIcon( icon);
			
			ImageIcon icon2 = createImageIcon(
					CommonUtils.class.getClassLoader().getResource("images/left_selected.png")
					);
			LeftSlide.setPressedIcon( icon2);
			LeftSlide.setSelectedIcon(icon2);
			LeftSlide.setRolloverEnabled(true);
			LeftSlide.setRolloverIcon(icon2);
			LeftSlide.setBorderPainted(false);
			LeftSlide.setFocusPainted(false);
			LeftSlide.setContentAreaFilled(false);
			ImageIcon icon3 = createImageIcon(
					CommonUtils.class.getClassLoader().getResource("images/left_more_selected.png")
					);
			LeftSlide.setPressedIcon(icon3 );
			LeftSlide.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					flow_panel.shiftBy( -SHIFT_BY  );
				}
			});
		} catch (IOException ex) {
			Logger.getLogger(WelcomePage.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		
		
		
		
	}
	
	static final double SHIFT_BY = 0.3333333333333333;
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(URL imgURL )throws IOException {
		if (imgURL != null) {
			return new ImageIcon(imgURL, "");
		} else {
			throw new IOException( "file not found");
		}
	}

	
	 public static String getContent( JPanel context, InputStream resource){
        
        BufferedReader stream = new BufferedReader( 
                                        new InputStreamReader( 
                                            new DataInputStream(  
                                                    resource
                                            ) 
                                        )
        );
        
        String content = "";
        String line = "";
        try {
            while( (line = stream.readLine() ) != null){
                content += line + "\n";
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return content;
    }
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CoverFlowPane = new javax.swing.JPanel();
        WelcomePane = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        LeftSlide = new javax.swing.JButton();
        RightSlide = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();

        setBackground(new java.awt.Color(0, 0, 0));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        javax.swing.GroupLayout CoverFlowPaneLayout = new javax.swing.GroupLayout(CoverFlowPane);
        CoverFlowPane.setLayout(CoverFlowPaneLayout);
        CoverFlowPaneLayout.setHorizontalGroup(
            CoverFlowPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 391, Short.MAX_VALUE)
        );
        CoverFlowPaneLayout.setVerticalGroup(
            CoverFlowPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 244, Short.MAX_VALUE)
        );

        WelcomePane.setBorder(null);
        WelcomePane.setEditable(false);

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 191, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 75, Short.MAX_VALUE)
        );

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 162, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 75, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 162, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        LeftSlide.setBackground(new java.awt.Color(0, 0, 0));
        LeftSlide.setBorder(null);
        LeftSlide.setMaximumSize(new java.awt.Dimension(20, 40));
        LeftSlide.setPreferredSize(new java.awt.Dimension(20, 40));

        RightSlide.setBackground(new java.awt.Color(0, 0, 0));
        RightSlide.setBorder(null);
        RightSlide.setMaximumSize(new Dimension(20,40));
        RightSlide.setPreferredSize(new java.awt.Dimension(20, 40));

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 191, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 77, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WelcomePane)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(LeftSlide, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CoverFlowPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(RightSlide, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(WelcomePane, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CoverFlowPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(RightSlide, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LeftSlide, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CoverFlowPane;
    private javax.swing.JButton LeftSlide;
    private javax.swing.JButton RightSlide;
    private javax.swing.JEditorPane WelcomePane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables


}
