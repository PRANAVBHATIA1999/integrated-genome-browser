package com.affymetrix.igb.tutorial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;

import furbelow.AbstractComponentDecorator;


public class Marquee extends AbstractComponentDecorator {
    final int LINE_WIDTH = 4;
    static Timer timer = new Timer();

    private float phase = 0f;
    public Marquee(JComponent target) {
        super(target);
        // Make the ants march
        timer.schedule(new TimerTask() {
            public void run() {
                phase += 1.0f;
                repaint();
            }
        }, 0, 50);
    }
    public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        //g.setColor(UIManager.getColor("Table.selectionBackground"));
        g.setColor(Color.red);
        Rectangle r = getDecorationBounds();
        g.setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_BUTT, 
                                    BasicStroke.JOIN_ROUND, 10.0f, 
                                    new float[]{4.0f}, phase));
        g.drawRect(r.x, r.y, r.width, r.height);
    }
}
