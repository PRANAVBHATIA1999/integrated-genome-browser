/**
 * Copyright (c) 2001-2004 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.restrictions;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.util.DNAUtils;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.ThreadUtils;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.swing.JRPButton;
import org.lorainelab.igb.services.IgbService;
import static org.lorainelab.igb.services.ServiceComponentNameReference.RESTRICTIONS_TAB;
import org.lorainelab.igb.services.window.tabs.IgbTabPanel;
import org.lorainelab.igb.services.window.tabs.IgbTabPanelI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.*;

@Component(name = RESTRICTIONS_TAB, service = IgbTabPanelI.class, immediate = true)
public final class RestrictionControlView extends IgbTabPanel
        implements ListSelectionListener, ActionListener {

    private static final long serialVersionUID = 0;

    private static final Logger LOG = LoggerFactory.getLogger(RestrictionControlView.class);
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("restrictions");
    private static final int TAB_POSITION = 10;
    private final Map<String, String> site_hash = new HashMap<>();
    private JList siteList;
    private JPanel labelP;
    private final List<String> sites = new ArrayList<>();
    private static Color colors[] = {
        Color.magenta,
        new Color(0x00cd00),
        Color.orange,
        new Color(0x00d7d7),
        new Color(0xb50000),
        Color.blue,
        Color.gray,
        Color.pink};//Distinct Colors for View/Print Ease
    private ArrayList<JLabel> labelList = new ArrayList<>();
    private JRPButton actionB;
    private JRPButton clearB;
    private IgbService igbService;
    /**
     * keep track of added glyphs
     */
    private final List<GlyphI> glyphs = new ArrayList<>();

    private static final Map<Character,String> SPECIAL_NUCLEOTIDES;

    static{
        Map<Character,String> tempNucleotides= new HashMap<>();
        tempNucleotides.put('R',"[AG]");
        tempNucleotides.put('Y',"[CT]");
        tempNucleotides.put('S',"[GC]");
        tempNucleotides.put('W',"[AT]");
        tempNucleotides.put('K',"[GT]");
        tempNucleotides.put('M',"[AC]");
        tempNucleotides.put('B',"[CGT]");
        tempNucleotides.put('D',"[AGT]");
        tempNucleotides.put('H',"[ACT]");
        tempNucleotides.put('V',"[ACG]");
        tempNucleotides.put('N',"[ACGT]");
        SPECIAL_NUCLEOTIDES= Collections.unmodifiableMap(tempNucleotides);
    }

    public RestrictionControlView() {
        super(BUNDLE.getString("restrictionSitesTab"), BUNDLE.getString("restrictionSitesTab"), BUNDLE.getString("restrictionSitesTooltip"), false, TAB_POSITION);

        boolean load_success = true;

        String rest_file = "/rest_enzymes";
        InputStream file_input_str
                = RestrictionControlView.class.getResourceAsStream(rest_file);

        if (file_input_str == null) {
            ErrorHandler.errorPanel(BUNDLE.getString("notFoundTitle"),
                    MessageFormat.format(BUNDLE.getString("notFound"), rest_file));
        }

        BufferedReader d = null;

        if (file_input_str == null) {
            load_success = false;
        } else {
            try {
                //Loading the name of all the restriction sites to GUI
                d = new BufferedReader(new InputStreamReader(file_input_str));
                StringTokenizer string_toks;
                String site_name, site_dna;
                String reply_string;
                //    String reply_string = distr.readLine();
                //int rcount = 0;
                while ((reply_string = d.readLine()) != null) {
                    //	System.out.println(reply_string);
                    string_toks = new StringTokenizer(reply_string);
                    site_name = string_toks.nextToken();
                    site_dna = string_toks.nextToken();
                    site_hash.put(site_name, site_dna);
                    sites.add(site_name);
                    //rcount++;
                }
            } catch (Exception ex) {
                load_success = false;
                ErrorHandler.errorPanel(MessageFormat.format("loadError", ex.toString()));
            } finally {
                GeneralUtils.safeClose(d);
                GeneralUtils.safeClose(file_input_str);
            }
        }

        if (load_success) {
            siteList = new JList(sites.toArray());

            JScrollPane scrollPane = new JScrollPane(siteList);
            labelP = new JPanel();
            labelP.setBackground(Color.white);
            labelP.setLayout(new GridLayout(sites.size(), 1));

            this.setLayout(new BorderLayout());
            scrollPane.setPreferredSize(new Dimension(100, 100));

            this.add("West", scrollPane);
            actionB = new JRPButton("RestrictionControlView_actionB", BUNDLE.getString("action"));
            clearB = new JRPButton("RestrictionControlView_clearB", BUNDLE.getString("clear"));
            this.add("Center", new JScrollPane(labelP));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(2, 1));
            buttonPanel.add(actionB);
            buttonPanel.add(clearB);
            this.add("South", buttonPanel);

            siteList.addListSelectionListener(this);
            actionB.addActionListener(this);
            clearB.addActionListener(this);
        } else {
            this.setLayout(new BorderLayout());
            JLabel lab = new JLabel(BUNDLE.getString("notAvailable"));
            this.add("North", lab);
        }
    }

    @Override
    public TabState getDefaultTabState() {
        return TabState.COMPONENT_STATE_RIGHT_TAB;
    }

    @SuppressWarnings("deprecation")
    public void valueChanged(ListSelectionEvent evt) {
        Object src = evt.getSource();
        if (src == siteList) {
            Object[] selected_names = siteList.getSelectedValues();

            removeUnselectedItem(selected_names);
            addSelectedItem(selected_names);
            labelP.updateUI();
            labelP.repaint();
        }
    }

    /*
     * Iterate to labelList, delete unselected item
     * @param selected_names selected items in JList
     */
    private void removeUnselectedItem(Object[] selected_names) {
        boolean isContained = false;

        Iterator<JLabel> it = labelList.iterator();
        while (it.hasNext()) {
            JLabel label = it.next();
            for (Object selected_name : selected_names) {
                if (label.getText().equals(selected_name.toString())) {
                    isContained = true;
                    break;
                } else {
                    isContained = false;
                }
            }

            if (!isContained) {
                labelP.remove(label);
                it.remove();
            }
        }
    }

    /*
     * Iterate to selected item list, add new item to labelList
     * @param selected_names selected items in JList
     */
    private void addSelectedItem(Object[] selected_names) {
        boolean isContained = false;

        for (Object selected_name : selected_names) {
            for (JLabel label : labelList) {
                if (label.getText().equals(selected_name.toString())) {
                    isContained = true;
                    break;
                } else {
                    isContained = false;
                }
            }

            if (!isContained) {
                JLabel label = new JLabel();
                int index = 0;
                if (!labelList.isEmpty()) {
                    index = labelList.size() % colors.length;
                }
                label.setForeground(colors[index]);//We're repeating the colors..deal with it, users.
                label.setText(selected_name.toString());
                labelP.add(label);
                labelList.add(label);
            }
        }
    }

    private void clearAll() {
        clearGlyphs();
        siteList.clearSelection();
        labelList.clear();
    }

    private void clearGlyphs() {
        igbService.getSeqMap().removeItem(glyphs);
        glyphs.clear();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == clearB) {
            clearAll();
            igbService.getSeqMap().updateWidget();
            return;
        }

        clearGlyphs();

        final BioSeq vseq = igbService.getSeqMapView().getViewSeq();
        if (vseq == null) {
            ErrorHandler.errorPanel(BUNDLE.getString("noSeq"));
            return;
        }

        final SeqSpan span = igbService.getSeqMapView().getVisibleSpan();

        ThreadUtils.getPrimaryExecutor(this).execute(new Thread(new GlyphifyMatchesThread(vseq, span)));
    }

    private class GlyphifyMatchesThread implements Runnable {

        final SeqSpan span;
        final BioSeq vseq;

        GlyphifyMatchesThread(BioSeq vseq, SeqSpan span) {
            this.vseq = vseq;
            this.span = span;
        }

        public void run() {

            GenericAction loadResidue = igbService.loadResidueAction(span, true);
            loadResidue.actionPerformed(null);

            try {
                igbService.addNotLockedUpMsg(BUNDLE.getString("findingSites"));
                if (vseq == null || !vseq.isAvailable(span)) {
                    ErrorHandler.errorPanel(BUNDLE.getString("notAvail"));
                    return;
                }
                int residue_offset = vseq.getMin();
                String residues = vseq.getResidues();
                // Search for reverse complement of query string
                String rev_searchstring = DNAUtils.reverseComplement(residues);
                int i = 0;
                for (JLabel label : labelList) {
                    String site_name = label.getText();
                    // done when hit first non-labelled JLabel
                    if (site_name == null || site_name.length() == 0) {
                        break;
                    }
                    String site_residues = site_hash.get(site_name);
                    if (site_residues == null) {
                        continue;
                    }
                    site_residues = replaceSpecialNucleotidesWithItsResidues(site_residues);
                    Pattern regex = null;
                    try {
                        regex = Pattern.compile(site_residues, Pattern.CASE_INSENSITIVE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        continue;
                    }

                    System.out.println(MessageFormat.format(BUNDLE.getString("searching"), site_residues));

                    residue_offset = vseq.getMin();
                    List<GlyphI> results = igbService.getSeqMapView().searchForRegexInResidues(
                            true, regex, residues, residue_offset, colors[i % colors.length]);
                    int hit_count1 = results.size();
                    glyphs.addAll(results);

                    // Search for reverse complement of query string
                    //   flip searchstring around, and redo nibseq search...
                    residue_offset = vseq.getMax();
                    results = igbService.getSeqMapView().searchForRegexInResidues(
                            false, regex, rev_searchstring, residue_offset, colors[i % colors.length]);
                    int hit_count2 = results.size();
                    glyphs.addAll(results);

                    System.out.println(MessageFormat.format(BUNDLE.getString("found"), site_residues, "" + hit_count1, "" + hit_count2));
                    igbService.getSeqMap().updateWidget();
                    i++;
                }
            } finally {
                igbService.removeNotLockedUpMsg(BUNDLE.getString("findingSites"));
            }

        }

        public static String replaceSpecialNucleotidesWithItsResidues(String sequence){
            StringBuilder newEnzyme = new StringBuilder();
            for(int i=0;i<sequence.length();i++){
                char curr = Character.toUpperCase(sequence.charAt(i));
                if(SPECIAL_NUCLEOTIDES.containsKey(curr)){
                    newEnzyme.append(SPECIAL_NUCLEOTIDES.get(curr));
                }else{
                    newEnzyme.append(curr);
                }
            }
            return newEnzyme.toString();
        }
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    @Override
    public boolean isCheckMinimumWindowSize() {
        return true;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setIgbService(IgbService igbService) {
        this.igbService = igbService;
    }
}
