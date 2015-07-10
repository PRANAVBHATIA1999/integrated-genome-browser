/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioviz.protannot;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.genometry.thread.CThreadHolder;
import com.affymetrix.genometry.thread.CThreadWorker;
import com.google.common.collect.Sets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.bioviz.protannot.interproscan.InterProscanTranslator;
import org.bioviz.protannot.interproscan.api.InterProscanService;
import org.bioviz.protannot.interproscan.api.InterProscanService.Status;
import org.bioviz.protannot.interproscan.api.Job;
import org.bioviz.protannot.interproscan.api.JobRequest;
import org.bioviz.protannot.interproscan.api.JobSequence;
import org.bioviz.protannot.interproscan.appl.model.ParameterType;
import org.bioviz.protannot.model.Dnaseq;
import org.bioviz.protannot.model.ProtannotParser;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author jeckstei
 */
@Component(provide = SequenceService.class)
public class SequenceService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SequenceService.class);

    private InterProscanService interProscanService;

    private InterProscanTranslator interProscanTranslator;

    private JLabel infoLabel;
    private JProgressBar progressBar;
    private JLabel showDetailLabel;
    private JTextArea detailText;
    private JScrollPane areaScrollPane;
    private Timer resultFetchTimer;
    private JDialog dialog;
    private JPanel parentPanel;
    private final Set<String> inputAppl;
    private JPanel configParentPanel;
    private ProtannotParser parser;
    private final JAXBContext jaxbContext;
    private final Unmarshaller jaxbUnmarshaller;
    private final Marshaller jaxbMarshaller;

    public SequenceService() throws JAXBException {
        inputAppl = Sets.newConcurrentHashSet();
        jaxbContext = JAXBContext.newInstance(Dnaseq.class);
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbMarshaller = jaxbContext.createMarshaller();

    }

    private void initInfoLabel(String text) {
        infoLabel = new JLabel(text);
    }

    private void initProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
    }

    private void initShowDetailLabel() {
        showDetailLabel = new JLabel("+ show detail");
        showDetailLabel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (areaScrollPane.isVisible()) {
//                    showDetailLabel.setText("+ show detail");
//                    parentPanel.remove(areaScrollPane);
//                    areaScrollPane.setVisible(false);
                } else {
//                    showDetailLabel.setText("- hide detail");
//                    parentPanel.add(areaScrollPane, "grow, height 200");
//                    areaScrollPane.setVisible(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //
            }
        });
    }

    private void initDetailText() {
        detailText = new JTextArea();
        detailText.setEditable(false);
        detailText.setLineWrap(true);

    }

    private void initAreaScrollPane() {
        initDetailText();
        areaScrollPane = new JScrollPane(detailText);
        areaScrollPane.setVisible(false);
        areaScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private boolean showApplicationOptionsLoadingModal() {
        CThreadWorker< Void, Void> worker = new CThreadWorker<Void, Void>("Loading InterProscan Options") {
            @Override
            protected Void runInBackground() {
                ParameterType applications = interProscanService.getApplications();
                applications.getValues().getValue().forEach(vt -> {
                    JCheckBox applCheckBox = new JCheckBox(vt.getLabel());
                    applCheckBox.setName(vt.getValue());
                    applCheckBox.setSelected(true);
                    configParentPanel.add(applCheckBox);
                });
                dialog.dispose();
                return null;
            }

            @Override
            protected void finished() {
            }
        };
        CThreadHolder.getInstance().execute(this, worker);

        parentPanel = new JPanel(new MigLayout());

        initInfoLabel("Loading InterProscan Options. Please wait...");
        parentPanel.add(infoLabel, "wrap");

        initProgressBar();
        parentPanel.add(progressBar, "align center, wrap");

        final JComponent[] inputs = new JComponent[]{
            parentPanel
        };
        Object[] options = {"Cancel"};

        JOptionPane pane = new JOptionPane(inputs, JOptionPane.CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null);

        pane.setInitialValue(null);

        dialog = pane.createDialog("Loading InterProscan Options");

        dialog.show();
        dialog.dispose();
        Object selectedValue = pane.getValue();
        if (selectedValue != null && selectedValue.equals(options[0])) {
            LOG.debug("cancelling request");
            return false;
        }
        return true;
    }

    private void showResultLoadingModal() {
        parentPanel = new JPanel(new MigLayout());

        initInfoLabel("Loading InterProscan data, Please wait...");
        parentPanel.add(infoLabel, "wrap");

        initProgressBar();
        parentPanel.add(progressBar, "align center, wrap");

        final JComponent[] inputs = new JComponent[]{
            parentPanel
        };
        Object[] options = {"Cancel"};

        JOptionPane pane = new JOptionPane(inputs, JOptionPane.CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null);

        pane.setInitialValue(null);

        dialog = pane.createDialog("Loading InterProscan Data");

        dialog.show();
        dialog.dispose();
        Object selectedValue = pane.getValue();
        if (selectedValue != null && selectedValue.equals(options[0])) {
            LOG.debug("cancelling request");
            resultFetchTimer.cancel();
        }
    }

    private boolean showSetupModal() {
        inputAppl.clear();
        configParentPanel = new JPanel(new MigLayout(new LC().wrapAfter(3)));
        configParentPanel.add(new JLabel("Select the applications to run."), "wrap");
        if (!showApplicationOptionsLoadingModal()) {
            return false;
        }

        final JComponent[] inputs = new JComponent[]{
            configParentPanel
        };
        Object[] options = {"Run", "Cancel"};
        int optionChosen = JOptionPane.showOptionDialog(null, inputs, "InterProscan Job Configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (optionChosen == 0) {
            for (java.awt.Component c : configParentPanel.getComponents()) {
                if (c instanceof JCheckBox) {
                    if (((JCheckBox) c).isSelected()) {
                        String value = ((JCheckBox) c).getName();
                        inputAppl.add(value);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void asyncLoadSequence(Callback callback) {
        if (showSetupModal()) {
            CThreadWorker< Void, Void> worker = new CThreadWorker<Void, Void>("Loading InterProscan") {
                @Override
                protected Void runInBackground() {
                    loadSequence(callback);
                    return null;
                }

                @Override
                protected void finished() {
                }
            };
            CThreadHolder.getInstance().execute(this, worker);
            showResultLoadingModal();
        }
    }

    public void loadSequence(Callback callback) {

        //Testing
//        Dnaseq original = parser.getDnaseq();
//        
//        Document doc = null;
//        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File("/home/jeckstei/Projects/igb/code/integrated-genome-browser/plugins/Protannot/src/test/resources/sample2.xml")))) {
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            doc = dBuilder.parse(bis);
//        } catch (IOException | SAXException | ParserConfigurationException ex) {
//            LOG.error(ex.getMessage());
//        } 
//        Dnaseq dnaseqIPS = interProscanTranslator.translateFromResultDocumentToModel(doc);
//        original.getMRNAAndAaseq().addAll(dnaseqIPS.getMRNAAndAaseq());
//        callback.execute(original);
        //end Testing
        //For testing
        JobRequest request = new JobRequest();
        request.setEmail("tmall@uncc.edu");

        request.setSignatureMethods(Optional.of(inputAppl));
        request.setTitle(Optional.empty());
        request.setGoterms(Optional.empty());
        request.setPathways(Optional.empty());
        for (Object obj : parser.getDnaseq().getMRNAAndAaseq()) {
            if (obj instanceof Dnaseq.MRNA) {
                String proteinSequence = null;
                String sequenceName = null;
                for (Dnaseq.Descriptor d : ((Dnaseq.MRNA) obj).getDescriptor()) {
                    if (d.getType().equals("protein sequence")) {
                        proteinSequence = d.getValue();
                    }
                    if(d.getType().equals("protein_product_id")) {
                        sequenceName = d.getValue();
                    }
                }
                
                request.getJobSequences().add(new JobSequence(sequenceName, proteinSequence));
            }
        }
        final List<Job> jobs = interProscanService.run(request);
        final List<Job> successfulJobs = new ArrayList<>();

        for (Job job : jobs) {
            LOG.info(job.getId());
        }

        resultFetchTimer = new Timer();
        resultFetchTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                Iterator<Job> it = jobs.iterator();
                while (it.hasNext()) {
                    Job job = it.next();
                    Status status = interProscanService.status(job.getId());
                    LOG.info(status.toString());
                    if (status.equals(Status.FINISHED)) {
                        successfulJobs.add(job);
                        it.remove();
                    }
                    if (status.equals(Status.ERROR)) {
                        //TODO: Notify user
                        it.remove();
                    }
                    if (status.equals(Status.FAILURE)) {
                        //TODO: Notify user
                        it.remove();
                    }
                    if (status.equals(Status.NOT_FOUND)) {
                        //TODO: Notify user
                        it.remove();
                    }
                }
                if (jobs.isEmpty()) {
                    Dnaseq original = parser.getDnaseq();
                    for (Job job : successfulJobs) {
                        Optional<Document> doc = interProscanService.result(job.getId());
                        if (doc.isPresent()) {

                            Dnaseq.Aaseq aaseq = interProscanTranslator.translateFromResultDocumentToModel(job.getSequenceName(), doc.get());
                            original.getMRNAAndAaseq().add(aaseq);

                        }
                    }
                    callback.execute(original);
                    try {
                        jaxbMarshaller.marshal(original, new File("sample_dnaseq_final.xml"));
                    } catch (JAXBException ex) {
                        java.util.logging.Logger.getLogger(ProtannotParser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    dialog.dispose();
                    resultFetchTimer.cancel();
                }
            }
        }, new Date(), 1000);

    }

    @Reference
    public void setInterProscanService(InterProscanService interProscanService) {
        this.interProscanService = interProscanService;
    }

    @Reference
    public void setParser(ProtannotParser parser) {
        this.parser = parser;
    }

    @Reference
    public void setInterProscanTranslator(InterProscanTranslator interProscanTranslator) {
        this.interProscanTranslator = interProscanTranslator;
    }

    public interface Callback {

        public void execute(Dnaseq dnaseq);
    }

}
