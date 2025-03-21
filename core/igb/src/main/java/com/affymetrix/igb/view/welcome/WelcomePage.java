/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainWorkspaceManager2.java
 *
 * Created on Nov 9, 2011, 2:54:08 PM
 */
package com.affymetrix.igb.view.welcome;

import be.pwnt.jflow.JFlowPanel;
import com.affymetrix.common.CommonUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 *
 * This JPanel composes the welcome page. The bottom panel is filled with a
 * CoverFlow class.
 *
 * @author jfvillal
 */
public class WelcomePage extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    static final double SHIFT_BY = -0.3333333333333333;
    final JFlowPanel flow_panel;
    private boolean scrollLeft;
    private Timer timer = new Timer(100, new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            if (scrollLeft) {
                flow_panel.shiftBy(-SHIFT_BY);
            } else {
                flow_panel.shiftBy(SHIFT_BY);
            }
        }
    });

    /**
     * Creates new form MainWorkspaceManager2
     */
    public WelcomePage(JPanel cover_flow) {
        initComponents();
        flow_panel = (JFlowPanel) cover_flow;
        CoverFlowPane.setLayout(new BorderLayout());
        CoverFlowPane.add(cover_flow);
        URL url = CommonUtils.class.getClassLoader().getResource("welcome.html");

        TitlePane.setLayout(new BorderLayout());
        TitlePane.add(new WelcomeTitle());
        TitlePane.setPreferredSize(new Dimension(400, 150));
        TitlePane.setMaximumSize(new Dimension(3000, 150));

        UIManager.put("Button.select", Color.TRANSLUCENT);
        try {
            ImageIcon icon = createImageIcon(
                    CommonUtils.class.getClassLoader().getResource("images/wright.png"));
            RightSlide.setIcon(icon);
            ImageIcon icon2 = createImageIcon(
                    CommonUtils.class.getClassLoader().getResource("images/right_selected.png"));
            RightSlide.setPressedIcon(icon2);
            RightSlide.setSelectedIcon(icon2);
            RightSlide.setRolloverEnabled(true); // turn on before rollovers work
            RightSlide.setRolloverIcon(icon2);
            RightSlide.setBorderPainted(false);
            RightSlide.setFocusPainted(false);
            RightSlide.setContentAreaFilled(false);

            ImageIcon icon3 = createImageIcon(
                    CommonUtils.class.getClassLoader().getResource("images/right_more_selected.png"));
            RightSlide.setPressedIcon(icon3);
        } catch (IOException ex) {
            Logger.getLogger(WelcomePage.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            ImageIcon icon = createImageIcon(
                    CommonUtils.class.getClassLoader().getResource("images/wleft.png"));
            LeftSlide.setIcon(icon);

            ImageIcon icon2 = createImageIcon(
                    CommonUtils.class.getClassLoader().getResource("images/left_selected.png"));
            LeftSlide.setPressedIcon(icon2);
            LeftSlide.setSelectedIcon(icon2);
            LeftSlide.setRolloverEnabled(true);
            LeftSlide.setRolloverIcon(icon2);
            LeftSlide.setBorderPainted(false);
            LeftSlide.setFocusPainted(false);
            LeftSlide.setContentAreaFilled(false);
            ImageIcon icon3 = createImageIcon(
                    CommonUtils.class.getClassLoader().getResource("images/left_more_selected.png"));
            LeftSlide.setPressedIcon(icon3);

        } catch (IOException ex) {
            Logger.getLogger(WelcomePage.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected ImageIcon createImageIcon(URL imgURL) throws IOException {
        if (imgURL != null) {
            return new ImageIcon(imgURL, "");
        } else {
            throw new IOException("file not found");
        }
    }

    public static String getContent(JPanel context, InputStream resource) {

        BufferedReader stream = new BufferedReader(
                new InputStreamReader(
                        new DataInputStream(
                                resource)));

        String content = "";
        String line = "";
        try {
            while ((line = stream.readLine()) != null) {
                content += line + "\n";
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return content;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CoverFlowPane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        RightSlide = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        TitlePane = new javax.swing.JPanel();
        LeftSlide = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setBackground(new java.awt.Color(0, 0, 0));

        CoverFlowPane.setMinimumSize(new java.awt.Dimension(0, 130));

        javax.swing.GroupLayout CoverFlowPaneLayout = new javax.swing.GroupLayout(CoverFlowPane);
        CoverFlowPane.setLayout(CoverFlowPaneLayout);
        CoverFlowPaneLayout.setHorizontalGroup(
            CoverFlowPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );
        CoverFlowPaneLayout.setVerticalGroup(
            CoverFlowPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 195, Short.MAX_VALUE)
        );

        jPanel1.setBackground(new java.awt.Color(255, 102, 0));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setPreferredSize(new java.awt.Dimension(150, 75));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 49, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(255, 102, 0));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel4.setPreferredSize(new java.awt.Dimension(150, 80));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 32, Short.MAX_VALUE)
        );

        RightSlide.setBackground(new java.awt.Color(255, 102, 0));
        RightSlide.setToolTipText("Scroll genome version right");
        RightSlide.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 102, 0), 2));
        RightSlide.setMaximumSize(new Dimension(20,40));
        RightSlide.setPreferredSize(new java.awt.Dimension(20, 40));
        RightSlide.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                RightSlideMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                RightSlideMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RightSlideMouseClicked(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(255, 102, 0));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setPreferredSize(new java.awt.Dimension(150, 77));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 32, Short.MAX_VALUE)
        );

        TitlePane.setMaximumSize(new java.awt.Dimension(1000, 300));
        TitlePane.setMinimumSize(new java.awt.Dimension(100, 130));
        TitlePane.setPreferredSize(new java.awt.Dimension(881, 150));

        javax.swing.GroupLayout TitlePaneLayout = new javax.swing.GroupLayout(TitlePane);
        TitlePane.setLayout(TitlePaneLayout);
        TitlePaneLayout.setHorizontalGroup(
            TitlePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );
        TitlePaneLayout.setVerticalGroup(
            TitlePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 169, Short.MAX_VALUE)
        );

        LeftSlide.setBackground(new java.awt.Color(255, 102, 0));
        LeftSlide.setToolTipText("Scroll genome version left");
        LeftSlide.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 102, 0), 2));
        LeftSlide.setMaximumSize(new java.awt.Dimension(20, 40));
        LeftSlide.setPreferredSize(new java.awt.Dimension(20, 40));
        LeftSlide.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                LeftSlideMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                LeftSlideMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LeftSlideMouseClicked(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 102, 0));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 49, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(17, 17, 17)
                            .addComponent(LeftSlide, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, 0)
                .addComponent(CoverFlowPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(RightSlide, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(17, 17, 17)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                        .addGap(0, 0, 0))))
            .addComponent(TitlePane, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, LeftSlide, RightSlide);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, jPanel1, jPanel2);

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, jPanel3, jPanel4);

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(TitlePane, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(CoverFlowPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(LeftSlide, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                        .addGap(1, 1, 1)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(RightSlide, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                        .addGap(1, 1, 1)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, jPanel1, jPanel2);

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel4.setBackground(new java.awt.Color(0, 0, 0));
        RightSlide.setBackground(new java.awt.Color(0, 0, 0));
        RightSlide.setBorder(null);
        jPanel3.setBackground(new java.awt.Color(0, 0, 0));
        LeftSlide.setBackground(new java.awt.Color(0, 0, 0));
        LeftSlide.setBorder(null);
        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
    }// </editor-fold>//GEN-END:initComponents

	private void LeftSlideMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LeftSlideMousePressed
        scrollLeft = true;
        timer.start();
	}//GEN-LAST:event_LeftSlideMousePressed

	private void LeftSlideMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LeftSlideMouseClicked
        flow_panel.shiftBy(-SHIFT_BY);
	}//GEN-LAST:event_LeftSlideMouseClicked

	private void RightSlideMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RightSlideMouseClicked
        flow_panel.shiftBy(SHIFT_BY);
	}//GEN-LAST:event_RightSlideMouseClicked

	private void RightSlideMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RightSlideMousePressed
        scrollLeft = false;
        timer.start();
	}//GEN-LAST:event_RightSlideMousePressed

	private void RightSlideMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RightSlideMouseReleased
        timer.stop();
	}//GEN-LAST:event_RightSlideMouseReleased

	private void LeftSlideMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LeftSlideMouseReleased
        timer.stop();
	}//GEN-LAST:event_LeftSlideMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CoverFlowPane;
    private javax.swing.JButton LeftSlide;
    private javax.swing.JButton RightSlide;
    private javax.swing.JPanel TitlePane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
