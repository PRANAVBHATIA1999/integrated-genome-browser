/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.menuitem;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URI;
import java.text.MessageFormat;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.parsers.FishClonesParser;
import com.affymetrix.genometryImpl.parsers.SegmenterRptParser;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerStatus;
import com.affymetrix.genometryImpl.util.UniFileFilter;
import com.affymetrix.genoviz.util.FileDropHandler;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.symloader.QuickLoad;
import com.affymetrix.igb.util.MergeOptionChooser;
import com.affymetrix.igb.view.DataLoadView;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @version $Id$
 */
public final class LoadFileAction extends AbstractAction {

	private final JFrame gviewerFrame;
	private final FileTracker load_dir_tracker;
	public static int unknown_group_count = 1;
	public static final String UNKNOWN_GROUP_PREFIX = "Unknown Group";
	private static final String MERGE_MESSAGE = 
			"Must select a genome before loading a graph.  "
			+ "Graph data must be merged with already loaded genomic data.";
	private final TransferHandler fdh = new FileDropHandler(){

		@Override
		public void openFileAction(File f) {
			LoadFileAction.openFileAction(gviewerFrame,f);
		}

		@Override
		public void openURLAction(String url) {
			LoadFileAction.openURLAction(gviewerFrame,url);
		}
	};
	private static MergeOptionChooser chooser = null;
	/**
	 *  Constructor.
	 *  @param ft  a FileTracker used to keep track of directory to load from
	 */
	public LoadFileAction(JFrame gviewerFrame, FileTracker ft) {
		super(MessageFormat.format(
					BUNDLE.getString("menuItemHasDialog"),
					BUNDLE.getString("openFile")),
				MenuUtil.getIcon("toolbarButtonGraphics/general/Open16.gif"));
		this.putValue(MNEMONIC_KEY, KeyEvent.VK_O);

		this.gviewerFrame = gviewerFrame;
		load_dir_tracker = ft;
		this.gviewerFrame.setTransferHandler(fdh);
	}

	public void actionPerformed(ActionEvent e) {
		loadFile(load_dir_tracker, gviewerFrame);
	}
	

	private static MergeOptionChooser getFileChooser() {
		if (chooser != null) {
			return chooser;
		}

		chooser = new MergeOptionChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"2bit"},
						".2bit Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"bam"}, "BAM Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"bed"}, "BED Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"bps", "bgn", "brs", "bsnp", "brpt", "bnib", "bp1", "bp2", "ead","useq"},
						"Binary Files"));
		chooser.addChoosableFileFilter(new UniFileFilter("cyt", "Cytobands"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"gff", "gtf", "gff3"},
						"GFF Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"fa", "fasta", "fas"},
						"FASTA Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"psl", "psl3"},
						"PSL Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"das", "dasxml", "das2xml"},
						"DAS Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"gr", "bgr", "sgr", "bar", "chp", "wig"},
						"Graph Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"sin", "egr", "egr.txt"},
						"Scored Interval Files"));
		chooser.addChoosableFileFilter(new UniFileFilter(
						"cnt", "Copy Number Files")); // ".cnt" files from CNAT
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"cnchp", "lohchp"}, "Copy Number CHP Files"));

		chooser.addChoosableFileFilter(new UniFileFilter(
						"var", "Genomic Variation Files")); // ".var" files (Toronto DB of genomic variations)
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{SegmenterRptParser.CN_REGION_FILE_EXT, SegmenterRptParser.LOH_REGION_FILE_EXT},
						"Regions Files")); // Genotype Console Segmenter
		chooser.addChoosableFileFilter(new UniFileFilter(
						FishClonesParser.FILE_EXT, "FishClones")); // ".fsh" files (fishClones.txt from UCSC)
		chooser.addChoosableFileFilter(new UniFileFilter(
						new String[]{"map"}, "Scored Map Files"));

		Set<String> all_known_endings = new HashSet<String>();
		for (javax.swing.filechooser.FileFilter filter : chooser.getChoosableFileFilters()) {
			if (filter instanceof UniFileFilter) {
				UniFileFilter uff = (UniFileFilter) filter;
				uff.addCompressionEndings(GeneralUtils.compression_endings);
				all_known_endings.addAll(uff.getExtensions());
			}
		}
		UniFileFilter all_known_types = new UniFileFilter(
						all_known_endings.toArray(new String[all_known_endings.size()]),
						"Known Types");
		all_known_types.setExtensionListInDescription(false);
		all_known_types.addCompressionEndings(GeneralUtils.compression_endings);
		chooser.addChoosableFileFilter(all_known_types);
		chooser.setFileFilter(all_known_types);
		return chooser;
	}

	/** Load a file into the global singleton genometry model. */
	private static void loadFile(final FileTracker load_dir_tracker, final JFrame gviewerFrame) {

		GenometryModel gmodel = GenometryModel.getGenometryModel();
		MergeOptionChooser fileChooser = getFileChooser();
		File currDir = load_dir_tracker.getFile();
		if (currDir == null) {
			currDir = new File(System.getProperty("user.home"));
		}
		fileChooser.setCurrentDirectory(currDir);
		fileChooser.rescanCurrentDirectory();
		if (gmodel.getSelectedSeqGroup() == null) {
			fileChooser.no_merge_button.setEnabled(true);
			fileChooser.no_merge_button.setSelected(true);
			fileChooser.merge_button.setEnabled(false);
		} else {
			// default to "merge" if already have a selected seq group to merge with,
			//    because non-merging is an uncommon choice
			fileChooser.merge_button.setSelected(true);
			fileChooser.merge_button.setEnabled(true);
		}
		fileChooser.genome_name_TF.setEnabled(fileChooser.no_merge_button.isSelected());
		fileChooser.genome_name_TF.setText(UNKNOWN_GROUP_PREFIX + " " + unknown_group_count);

		int option = fileChooser.showOpenDialog(gviewerFrame);

		if (option != JFileChooser.APPROVE_OPTION) {
			return;
		}

		load_dir_tracker.setFile(fileChooser.getCurrentDirectory());

		final File[] fils = fileChooser.getSelectedFiles();
		final boolean mergeSelected = fileChooser.merge_button.isSelected();
		if (!mergeSelected) {
			// Not merging, so create a new Seq Group
			unknown_group_count++;
		}
		
		final AnnotatedSeqGroup loadGroup = mergeSelected ? gmodel.getSelectedSeqGroup() : gmodel.addSeqGroup(fileChooser.genome_name_TF.getText());

		if (!mergeSelected) {
			// Select the "unknown" group.
			gmodel.setSelectedSeqGroup(loadGroup);
		}
		
		URI uri = fils[0].toURI();
		
		openURI(uri, fils[0].getName(), mergeSelected, loadGroup);
	}

	public static void openURI(URI uri, final String fileName, final boolean mergeSelected, final AnnotatedSeqGroup loadGroup) {
		// Make sure this URI is not already used within the group.  Otherwise there could be collisions in BioSeq.addAnnotations(type)
		boolean uniqueURI = true;
		for (GenericVersion version : loadGroup.getAllVersions()) {
			// See if symloader feature was created with the same uri.
			for (GenericFeature feature : version.getFeatures()) {
				if (feature.symL != null) {
					if (feature.symL.uri.equals(uri)) {
						uniqueURI = false;
						break;
					}
				}
			}
		}
		if (!uniqueURI) {
			ErrorHandler.errorPanel("Cannot add same feature",
					"The feature " + uri + " has already been added.");
			return;
		}

		GenericVersion version = GeneralLoadUtils.getLocalFilesVersion(loadGroup);

		// handle URL case.
		String uriString = uri.toString();
		int httpIndex = uriString.toLowerCase().indexOf("http:");
		if (httpIndex > -1) {
			// Strip off initial characters up to and including http:
			// Sometimes this is necessary, as URLs can start with invalid "http:/"
			uriString = GeneralUtils.convertStreamNameToValidURLName(uriString);
			uri = URI.create(uriString);
		}
		GenericFeature gFeature = new GenericFeature(fileName, null, version, new QuickLoad(version, uri), File.class);
		if (!mergeSelected && gFeature.symL != null) {
			addChromosomesForUnknownGroup(fileName, gFeature, loadGroup);
			if (loadGroup.getSeqCount() > 0) {
				GenometryModel.getGenometryModel().setSelectedSeq(loadGroup.getSeq(0));
				// select a chromosomes
			}
		}
		version.addFeature(gFeature);
		gFeature.setVisible(); // this should be automatically checked in the feature tree
		DataLoadView view = ((IGB) Application.getSingleton()).data_load_view;
		view.tableChanged();
		// force a refresh of this server
		ServerList.fireServerInitEvent(ServerList.getLocalFilesServer(), ServerStatus.Initialized, true);
	}

	private static void addChromosomesForUnknownGroup(final String fileName, final GenericFeature gFeature, final AnnotatedSeqGroup loadGroup) {
		ExecutorService vexec = Executors.newSingleThreadExecutor();
		final String notLockedUpMsg = "Retrieving chromosomes for " + fileName;
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			public Void doInBackground() {
				Application.getSingleton().addNotLockedUpMsg(notLockedUpMsg);
				List<BioSeq> seqs = gFeature.symL.getChromosomeList();
				for (BioSeq seq : seqs) {
					if (seq.getSeqGroup() == null) {
						seq.setSeqGroup(loadGroup);
					}
				}
				return null;
			}

			@Override
			public void done() {
				Application.getSingleton().removeNotLockedUpMsg(notLockedUpMsg);
			}
		};
		vexec.execute(worker);
		vexec.shutdown();
	}


	private static void openURLAction(JFrame gviewerFrame,String url){
		try {
			URI uri = new URI(url.trim());
		
			if(!openURI(uri)){
				ErrorHandler.errorPanel(gviewerFrame, "FORMAT NOT RECOGNIZED", "Format not recognized for file: " + url, null);
			}
			
		} catch (URISyntaxException ex) {
			ex.printStackTrace();
			ErrorHandler.errorPanel(gviewerFrame, "INVALID URL", url + "\n Url provided is not valid: ", null);
		}
	}

	private static void openFileAction(JFrame gviewerFrame, File f){
		URI uri = f.toURI();
		if(!openURI(uri)){
			ErrorHandler.errorPanel(gviewerFrame, "FORMAT NOT RECOGNIZED", "Format not recognized for file: " + f.getName(), null);			
		}
	}

	private static boolean openURI(URI uri) {
		String unzippedName = GeneralUtils.getUnzippedName(uri.toString());
		String friendlyName = unzippedName.substring(unzippedName.lastIndexOf("/") + 1);

		if(!getFileChooser().accept(new File(friendlyName))){
			return false;
		}

		GenometryModel gmodel = GenometryModel.getGenometryModel();
		AnnotatedSeqGroup loadGroup = gmodel.getSelectedSeqGroup();
		boolean mergeSelected = loadGroup == null ? false :true;
		if (loadGroup == null) {
			loadGroup = gmodel.addSeqGroup("unknowGroup");
		}

		openURI(uri, friendlyName, mergeSelected, loadGroup);
		
		return true;
	}
}
