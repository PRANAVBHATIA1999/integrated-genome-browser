package com.affymetrix.igb.action;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.thread.CThreadEvent;
import com.affymetrix.genometry.thread.CThreadHolder;
import com.affymetrix.genometry.thread.CThreadListener;
import com.affymetrix.genometry.thread.CThreadWorker;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.swing.JRPButton;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;

/**
 * Display a pop up menu of all the running threads with a progress bar and
 * cancel button for each thread.
 *
 * @author hiralv
 */
public class ThreadHandlerAction extends GenericAction implements CThreadListener {

    private static final long serialVersionUID = 1L;
    private static final ThreadHandlerAction ACTION = new ThreadHandlerAction();
    private static final Logger ourLogger
            = Logger.getLogger(ThreadHandlerAction.class.getPackage().getName());

    static {
        //GenericActionHolder.getInstance().addGenericAction(ACTION);
        CThreadHolder.getInstance().addListener(ACTION);
    }

    public static ThreadHandlerAction getAction() {
        return ACTION;
    }

    private static final ImageIcon closeIcon = CommonUtils.getInstance().getIcon("16x16/actions/stop.png");

    private final JPopupMenu runningTasks;
    private JPanel outerBox;
    private final Map<CThreadWorker<?, ?>, Box> cThreadWorker2Box = new HashMap<>();

    private ThreadHandlerAction() {
        super("Handle Threads", null, null);
        runningTasks = new JPopupMenu();
        runningTasks.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        int size = setCancelPopup();
        if (size == 0) {
            return;
        }

        JFrame frame = IGB.getInstance().getFrame();
        final int x = (int) frame.getAlignmentX();
        final int y = (int) frame.getAlignmentY() + frame.getHeight() - ((size + 1) * 29);
        runningTasks.show(frame, x, y);
    }

    private int setCancelPopup() {
        Set<CThreadWorker<?, ?>> workers = CThreadHolder.getInstance().getAllCThreadWorkers();
        int size = workers.size();
        if (size == 0) {
            return 0;
        }

        outerBox = new JPanel();
        outerBox.setLayout(new GridLayout(size, 1));
        outerBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        cThreadWorker2Box.clear();
        for (final CThreadWorker<?, ?> worker : workers) {
            final Box box = Box.createHorizontalBox();
            //String string = worker.getMessage().substring(0, Math.min(40, worker.getMessage().length()));
            final JLabel taskName = new JLabel(worker.getMessage());

            final JRPButton cancelTask = new JRPButton("ThreadHandler_cancelTask", closeIcon);
            cancelTask.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
            cancelTask.addActionListener(ae -> {
                box.setVisible(false);
                if (worker != null && !worker.isCancelled() && !worker.isDone()) {
                    worker.cancelThread(true);
                    ourLogger.log(Level.INFO, "Cancelled thread {0}", worker.getMessage());
                }
            });
            final JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            progressBar.setMaximumSize(new Dimension(100, 25));

            worker.addPropertyChangeListener(
                    evt -> {
                        if ("progress".equals(evt.getPropertyName())) {
                            if (runningTasks != null && runningTasks.isShowing()) {
                                progressBar.setValue((Integer) evt.getNewValue());
                                progressBar.repaint();
                            }
                        }
                    }
            );

            box.setBorder(BorderFactory.createEtchedBorder());
            box.add(cancelTask);
            box.add(Box.createRigidArea(new Dimension(5, 25)));
            box.add(taskName);
            box.add(Box.createRigidArea(new Dimension(5, 25)));
            box.add(progressBar);
            box.add(Box.createRigidArea(new Dimension(5, 25)));
            cThreadWorker2Box.put(worker, box);
            outerBox.add(box);

        }
        runningTasks.removeAll();
        runningTasks.add(outerBox);
        return size;
    }

    @Override
    public void heardThreadEvent(CThreadEvent cte) {
        CThreadWorker<?, ?> w = (CThreadWorker<?, ?>) cte.getSource();
        if (cte.getState() == CThreadEvent.STARTED) {
            IGB.getInstance().addNotLockedUpMsg(w.getMessage());
        } else {
            IGB.getInstance().removeNotLockedUpMsg(w.getMessage());
        }

        if (CThreadHolder.getInstance().getCThreadWorkerCount() == 0 || !runningTasks.isShowing()) {
            runningTasks.setVisible(false);
        } else {
            actionPerformed(null);
        }
    }
}
