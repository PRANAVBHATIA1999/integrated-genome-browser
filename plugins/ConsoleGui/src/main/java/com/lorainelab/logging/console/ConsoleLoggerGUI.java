package com.lorainelab.logging.console;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class ConsoleLoggerGUI extends javax.swing.JFrame {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleLoggerGUI.class);
    private static final int MAX_CONSOLE_LENGTH = 1000;

    //TODO replace singleton pattern
    public static ConsoleLoggerGUI getInstance() {
        return ConsoleLoggerGUIHolder.INSTANCE;
    }

    private ConsoleLoggerGUI() {
        initComponents();

        setupLogging();
    }

    private void setupLogging() {
        try {
            consoleTextArea.getDocument().addDocumentListener(new ConsoleLogDocumentListener(MAX_CONSOLE_LENGTH));
            final JTextAreaOutputStream tout = new JTextAreaOutputStream(consoleTextArea, System.out);
            System.setOut(new PrintStream(tout, false, "UTF-8"));
            System.setErr(new PrintStream(new JTextAreaOutputStream(consoleTextArea, System.err), false, "UTF-8"));
            final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            final LoggerContext loggerContext = rootLogger.getLoggerContext();
            final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(loggerContext);
            encoder.setPattern("%-5level [%thread]: %message%n");
            encoder.start();
            OutputStreamAppender<ILoggingEvent> outputStreamAppender = new OutputStreamAppender<ILoggingEvent>();
            outputStreamAppender.setName("OutputStream Appender");
            outputStreamAppender.setContext(loggerContext);
            outputStreamAppender.setEncoder(encoder);
            outputStreamAppender.setOutputStream(tout);
            outputStreamAppender.start();
            rootLogger.addAppender(outputStreamAppender);
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error setting up gui console logger", ex);
        }
    }

    /**
     * Displays the console and brings it to the front. If necessary, it will be
     * de-iconified. This will call {@link #init} if necessary, but it is better
     * for you to call init() at the time you want the console to begin working.
     */
    public void showConsole() {

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
                //toggleVisible();
            }
        });

    }

//    private void toggleVisible() {
//        setVisible(!isVisible());
//        if (isVisible()) {
//            toFront();
//            requestFocus();
//            setAlwaysOnTop(true);
//            try {
//                //remember the last location of mouse
//                final Point oldMouseLocation = MouseInfo.getPointerInfo().getLocation();
//
//                //simulate a mouse click on title bar of window
//                Robot robot = new Robot();
//                robot.mouseMove(getX() + 100, getY() + 5);
//                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
//                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
//
//                //move mouse to old location
//                robot.mouseMove((int) oldMouseLocation.getX(), (int) oldMouseLocation.getY());
//            } catch (Exception ex) {
//                //just ignore exception, or you can handle it as you want
//            } finally {
//                setAlwaysOnTop(false);
//            }
//        }
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        consoleTextArea = new javax.swing.JTextArea();
        closeBtn = new javax.swing.JButton();
        copyToClipboardBtn = new javax.swing.JButton();

        setTitle("IGB Log Viewer");

        consoleTextArea.setColumns(20);
        consoleTextArea.setRows(5);
        jScrollPane1.setViewportView(consoleTextArea);

        closeBtn.setText("Close");
        closeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeBtnActionPerformed(evt);
            }
        });

        copyToClipboardBtn.setText("Copy To Clipboard");
        copyToClipboardBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyToClipboardBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(copyToClipboardBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeBtn)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeBtn)
                    .addComponent(copyToClipboardBtn))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtnActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeBtnActionPerformed

    private void copyToClipboardBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyToClipboardBtnActionPerformed
        StringSelection stringSelection = new StringSelection(consoleTextArea.getText());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }//GEN-LAST:event_copyToClipboardBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeBtn;
    private javax.swing.JTextArea consoleTextArea;
    private javax.swing.JButton copyToClipboardBtn;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private static class ConsoleLoggerGUIHolder {

        private static final ConsoleLoggerGUI INSTANCE = new ConsoleLoggerGUI();
    }
}
