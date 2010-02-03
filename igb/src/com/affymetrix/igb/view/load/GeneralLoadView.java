package com.affymetrix.igb.view.load;

import com.affymetrix.genometryImpl.event.GenericServerInitEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.genometryImpl.SeqSpan;

import com.affymetrix.genometryImpl.util.LoadUtils.LoadStatus;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.comparator.StringVersionDateComparator;
import com.affymetrix.genometryImpl.event.GenericServerInitListener;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;

import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.util.ThreadUtils;
import com.affymetrix.igb.view.SeqMapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;

public final class GeneralLoadView extends JComponent
				implements ItemListener, ActionListener, GroupSelectionListener, SeqSelectionListener, GenericServerInitListener {

	static GeneralLoadUtils glu = new GeneralLoadUtils();
	private static final boolean DEBUG_EVENTS = false;
	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	private static final String SELECT_SPECIES = "Species";
	private static final String SELECT_GENOME = "Genome Version";
	private static final String GENOME_SEQ_ID = "genome";
	private static final String ENCODE_REGIONS_ID = "encode_regions";
	private static final String CHOOSE = "Choose";
	private AnnotatedSeqGroup curGroup = null;
	private final JComboBox versionCB;
	private final JComboBox speciesCB;
	private final JButton all_residuesB;
	private final JButton partial_residuesB;
	private final JButton refresh_dataB;
	private static final SeqMapView gviewer = Application.getSingleton().getMapView();
	private JTableX feature_table;
	private FeaturesTableModel feature_model;
	JScrollPane featuresTableScrollPane;
	private final FeatureTreeView feature_tree_view;
	private TrackInfoView track_info_view;

	public GeneralLoadView() {
		this.setLayout(new BorderLayout());

		JPanel choicePanel = new JPanel();
		choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.X_AXIS));
		choicePanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));

		speciesCB = new JComboBox();
		speciesCB.addItem(SELECT_SPECIES);
		speciesCB.setMaximumSize(new Dimension(speciesCB.getPreferredSize().width*4,speciesCB.getPreferredSize().height));
		speciesCB.setEnabled(false);
		speciesCB.setEditable(false);
		speciesCB.setToolTipText(CHOOSE + " " + SELECT_SPECIES);

		choicePanel.add(new JLabel(CHOOSE + ":"));
		choicePanel.add(Box.createHorizontalStrut(5));
		choicePanel.add(speciesCB);
		choicePanel.add(Box.createHorizontalStrut(50));

		versionCB = new JComboBox() {
			/**
			 * Default implementation of addListener permits the same class
			 * to be added as a listener multiple times, causing it to be
			 * notified of an event multiple times.
			 *
			 * This is a quick kludge to prevent a listener from being added
			 * multiple times.  Hopefully this can be removed once we
			 * sort out adding and removing ItemListeners.
			 */
			@Override public void addItemListener(ItemListener aListener) {
				for (ItemListener listener :  this.getItemListeners()) {
					if (listener == aListener) {
						Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Attempt to add duplicate ItemListener, ignoring");
						return;
					}
				}
				super.addItemListener(aListener);
			}
		};
		versionCB.addItem(SELECT_GENOME);
		versionCB.setMaximumSize(new Dimension(versionCB.getPreferredSize().width*4, versionCB.getPreferredSize().height));
		versionCB.setEnabled(false);
		versionCB.setEditable(false);
		versionCB.setToolTipText(CHOOSE + " " + SELECT_GENOME);
		choicePanel.add(versionCB);


		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3));

		all_residuesB = new JButton("Load All Sequence");
		all_residuesB.setToolTipText("Load nucleotide sequence");
		all_residuesB.setMaximumSize(all_residuesB.getPreferredSize());
		all_residuesB.setEnabled(false);
		all_residuesB.addActionListener(this);
		buttonPanel.add(all_residuesB);
		partial_residuesB = new JButton("Load Sequence in View");
		partial_residuesB.setToolTipText("Load partial nucleotide sequence");
		partial_residuesB.setMaximumSize(partial_residuesB.getPreferredSize());
		partial_residuesB.setEnabled(false);
		
		partial_residuesB.addActionListener(this);
		buttonPanel.add(partial_residuesB);
		refresh_dataB = new JButton("Refresh Data");
		refresh_dataB.setToolTipText("Load data sets currently in view");
		refresh_dataB.setMaximumSize(refresh_dataB.getPreferredSize());
		refresh_dataB.setEnabled(false);
		refresh_dataB.addActionListener(this);
		buttonPanel.add(refresh_dataB);
		this.add("South", buttonPanel);

		this.feature_model = new FeaturesTableModel(this, null, null);
		this.feature_table = new JTableX(this.feature_model);
		this.feature_table.setModel(this.feature_model);

		featuresTableScrollPane = new JScrollPane(this.feature_table);

		JPanel featuresPanel = new JPanel();
		featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
		featuresPanel.add(new JLabel("Choose Load Mode for Data Sets:"));
		featuresPanel.add(featuresTableScrollPane);

		this.add("North", choicePanel);

		/* COMMENTED OUT.  The Track Info table makes the data load view
		 *                 too busy, so for now, the code is commented out
		 */
//		track_info_view = new TrackInfoView();		
//		JSplitPane featurePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, featuresPanel, track_info_view);		
//		featurePane.setResizeWeight(0.5);		
//		JSplitPane jPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.feature_tree_view, featurePane);

		this.feature_tree_view = new FeatureTreeView(this);
		JSplitPane jPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.feature_tree_view, featuresPanel);
		jPane.setResizeWeight(0.5);		
		this.add("Center", jPane);

		this.setBorder(BorderFactory.createEtchedBorder());

		ServerList.addServerInitListener(this);

		populateSpeciesData();

		addListeners();
	}

	/**
	 * Discover servers, species, etc., asynchronously.
	 */
	private void populateSpeciesData() {
		Executor vexec = Executors.newSingleThreadExecutor();

		for (final GenericServer gServer : ServerList.getEnabledServers()) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				protected Void doInBackground() throws Exception {
					Application.getSingleton().addNotLockedUpMsg("Loading server " + gServer.serverName);
					GeneralLoadUtils.discoverServer(gServer);
					return null;
				}

				@Override
				public void done() {
					/*
					Application.getSingleton().setNotLockedUpStatus("Loading previous genome...");
					RestorePersistentGenome();
					Application.getSingleton().setStatus("", false);
					 */
				}
			};

			vexec.execute(worker);
		}
	}


	/**
	 * Add and verify another server.  Called from DataLoadPrefsView.
	 * @param serverName
	 * @param serverURL
	 * @param serverType
	 * @return true or false
	 */
	public GenericServer addServer(ServerType serverType, String serverName, String serverURL) {
		GenericServer server = GeneralLoadUtils.addServer(serverType, serverName, serverURL);

		if (server != null) {
			// server has been added.  Refresh necessary boxes, tables, etc.
			String version = (String)this.versionCB.getSelectedItem();
			if (!SELECT_GENOME.equals(version)) {initVersion(version); }
			gmodel.setSelectedSeqGroup(gmodel.getSelectedSeqGroup());
			gmodel.setSelectedSeq(gmodel.getSelectedSeq());
		}

		return server;
	}

	public void GenericServerInit(GenericServerInitEvent evt) {
		GenericServer gServer = (GenericServer)evt.getSource();

		Application.getSingleton().removeNotLockedUpMsg("Loading server " + gServer.serverName);

		// Need to refresh species names
		boolean speciesListener = this.speciesCB.getItemListeners().length > 0;
		refreshSpeciesCB();

		String speciesName = (String)this.speciesCB.getSelectedItem();
		if (speciesName != null && !speciesName.equals(SELECT_SPECIES)) {
			//refresh version names if a species is selected
			refreshVersionCB(speciesName);

			String versionName = (String)this.versionCB.getSelectedItem();
			if (versionName != null && !versionName.equals(SELECT_GENOME)) {
				// refresh this version
				initVersion(versionName);

				// TODO: refresh feature tree view if a version is selected
				refreshTreeView();
			}
		}

		if (speciesListener) {
			this.speciesCB.addItemListener(this);
		}
	}


	private void addListeners() {
		gmodel.addGroupSelectionListener(this);
		gmodel.addSeqSelectionListener(this);

		speciesCB.setEnabled(true);
		versionCB.setEnabled(true);
		speciesCB.addItemListener(this);
		versionCB.addItemListener(this);

	}

	/**
	 * Initialize Species combo box.  It is assumed that we have the species data at this point.
	 * If a species was already selected, leave it as the selected species.
	 */
	private void refreshSpeciesCB() {
		int speciesListLength = GeneralLoadUtils.species2genericVersionList.keySet().size();
		if (speciesListLength == speciesCB.getItemCount() -1) {
			// No new species.  Don't bother refreshing.
			if (speciesListLength == 0 && speciesCB.isEnabled()) {
				speciesCB.setEnabled(false);
				// disable if there are no species yet.
			}
			return;
		}
		String oldSpecies = (String)speciesCB.getSelectedItem();
		speciesCB.removeItemListener(this);
		speciesCB.removeAllItems();
		speciesCB.addItem(SELECT_SPECIES);

		// Add names to combo boxes.
		List<String> speciesList = new ArrayList<String>();
		speciesList.addAll(GeneralLoadUtils.species2genericVersionList.keySet());
		Collections.sort(speciesList);
		// Sort the species
		for (String speciesName : speciesList) {
			speciesCB.addItem(speciesName);
		}


		if (oldSpecies != null && speciesList.contains(oldSpecies)) {
			speciesCB.setSelectedItem(oldSpecies);
		}
	}


	/**
	 * Refresh the genome versions, now that the species has changed.
	 * If there's precisely one versionName, just select it.
	 * @param speciesName
	 */
	private void refreshVersionCB(String speciesName) {
		List<GenericVersion> versionList = GeneralLoadUtils.species2genericVersionList.get(speciesName);
		int versionListLength = versionList == null ? 0 : versionList.size();
		if (versionListLength == versionCB.getItemCount() -1) {
			// No new versions.  Don't bother refreshing.
			if (versionListLength == 0 && versionCB.isEnabled()) {
				versionCB.setEnabled(false);
				// disable if there are no versions yet.
			}
			return;
		}

		String oldVersion = (String)versionCB.getSelectedItem();
		versionCB.removeItemListener(this);
		versionCB.removeAllItems();
		versionCB.addItem(SELECT_GENOME);
		versionCB.setSelectedIndex(0);

		if (speciesName.equals(SELECT_SPECIES) && versionCB.isEnabled()) {
			// Disable the versionName.
			versionCB.setEnabled(false);
			return;
		}

		// Add names to combo boxes.
		List<String> versionNames = new ArrayList<String>();
		for(GenericVersion gVersion : GeneralLoadUtils.species2genericVersionList.get(speciesName)) {
			// the same versionName name may occur on multiple servers
			if (!versionNames.contains(gVersion.versionName)) {
				versionNames.add(gVersion.versionName);
			}
		}
		Collections.sort(versionNames, new StringVersionDateComparator());
		// Sort the versions (by date)

		for (String versionName : versionNames) {
			versionCB.addItem(versionName);
		}
		versionCB.setEnabled(true);
		if (oldVersion != null && !oldVersion.equals(SELECT_GENOME) && GeneralLoadUtils.versionName2species.containsKey(oldVersion)) {
			versionCB.setSelectedItem(oldVersion);
		}
		if (versionCB.getItemCount() > 1) {
			versionCB.addItemListener(this);
		}
	}


	/**
	 * bootstrap bookmark from Preferences for last species/versionName/genome / sequence / region
	 */
	/**
	private void RestorePersistentGenome() {
		// Get group and seq info from persistent preferences.
		// (Recovering as much data as possible before activating listeners.)
		AnnotatedSeqGroup group = Persistence.restoreGroupSelection();
		if (group == null) {
			return;
		}

		List<GenericVersion> gVersions = group.getVersions();
		if (gVersions.isEmpty()) {
			return;
		}
		String versionName = gVersions.get(0).versionName;
		if (versionName == null || GeneralLoadUtils.versionName2species.get(versionName) == null) {
			return;
		}

		gmodel.addGroupSelectionListener(this);
		if (group != gmodel.getSelectedSeqGroup()) {
			gmodel.setSelectedSeqGroup(group);
		}

		initVersion(versionName);

		// Select the persistent chromosome, and restore the span.
		BioSeq seq = Persistence.restoreSeqSelection(group);
		if (seq == null) {
			seq = group.getSeq(0);
		}
		gmodel.addSeqSelectionListener(this);
		if (gmodel.getSelectedSeq() != seq) {
			gmodel.setSelectedSeq(seq);
		}

		// Try/catch may not be needed.
		try {
			Persistence.restoreSeqVisibleSpan(gviewer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
**/
	private static void initVersion(String versionName) {
		Application.getSingleton().addNotLockedUpMsg("Loading chromosomes...");
		GeneralLoadUtils.initVersionAndSeq(versionName); // Make sure this genome versionName's feature names are initialized.
		Application.getSingleton().removeNotLockedUpMsg("Loading chromosomes...");
	}
	
	/**
	 * Handles clicking of partial residue, all residue, and refresh data buttons.
	 * @param evt
	 */
	public void actionPerformed(ActionEvent evt) {
		final Object src = evt.getSource();
		if (src == refresh_dataB) {
			loadVisibleData();
			return;
		}
		if (src != partial_residuesB && src != all_residuesB) {
			return;
		}

		Application.getSingleton().addNotLockedUpMsg("Loading residues");

		final String genomeVersionName = (String) versionCB.getSelectedItem();


		final BioSeq curSeq = gmodel.getSelectedSeq();
		// Use a SwingWorker to avoid locking up the GUI.
		Executor vexec = ThreadUtils.getPrimaryExecutor(src);

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			public Void doInBackground() {
				try {
				if (src == partial_residuesB) {
					SeqSpan viewspan = gviewer.getVisibleSpan();
					if (!GeneralLoadUtils.loadResidues(genomeVersionName, curSeq, viewspan.getMin(), viewspan.getMax(), viewspan)) {
						// Load the full sequence if the partial one couldn't be loaded.
						if (!GeneralLoadUtils.loadResidues(genomeVersionName, curSeq, 0, curSeq.getLength(), null)) {
							ErrorHandler.errorPanel("Couldn't load sequence",
											"Was not able to locate the sequence.");
						}
					}
				} else {
					if (!GeneralLoadUtils.loadResidues(genomeVersionName, curSeq, 0, curSeq.getLength(), null)) {
						ErrorHandler.errorPanel("Couldn't load sequence",
										"Was not able to locate the sequence.");
					}
				}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			}
			@Override
			public void done() {
				Application.getSingleton().removeNotLockedUpMsg("Loading residues");
			}
		};

		vexec.execute(worker);
	}

	/**
	 * Load any data that's marked for visible range.
	 */
	public void loadVisibleData() {
		SeqSpan request_span = gviewer.getVisibleSpan();

		if (DEBUG_EVENTS) {
			System.out.println("Visible load request span: " + request_span.getStart() + " " + request_span.getEnd());
		}

		BioSeq curSeq = gmodel.getSelectedSeq();

		// Load any features that have a visible strategy and haven't already been loaded.
		String genomeVersionName = (String) versionCB.getSelectedItem();
		for (GenericFeature gFeature : GeneralLoadUtils.getFeatures(genomeVersionName)) {
			if (gFeature.loadStrategy != LoadStrategy.VISIBLE && gFeature.loadStrategy != LoadStrategy.CHROMOSOME) {
				continue;
			}
			// Even if it's already loaded, we may want to reload... for example, if the viewsize changes.

			if (!gFeature.LoadStatusMap.containsKey(curSeq)) {
				// Should never get here.
				System.out.println("ERROR!  " + curSeq.getID() + " does not contain feature status");
			}

			if (DEBUG_EVENTS) {
				System.out.println("Selected : " + gFeature.featureName);
			}
			GeneralLoadUtils.loadAndDisplayAnnotations(gFeature, curSeq, feature_model);
		}

	}

	/**
	 * One of the combo boxes changed state.
	 * @param evt
	 */
	public void itemStateChanged(ItemEvent evt) {
		Object src = evt.getSource();
		if (DEBUG_EVENTS) {
			System.out.println("####### GeneralLoadView received itemStateChanged event: " + evt);
		}

		try {
			if ((src == speciesCB) && (evt.getStateChange() == ItemEvent.SELECTED)) {
				speciesCBChanged(); // make sure display gets updated
			} else if ((src == versionCB) && (evt.getStateChange() == ItemEvent.SELECTED)) {
				versionCBChanged();
			}
		} catch (Throwable t) {
			// some out-of-memory errors could happen during this code, so
			// this catch block will report that to the user.
			ErrorHandler.errorPanel("Error ", t);
		}
	}

	/**
	 * The species combo box changed.
	 * If the species changes to SELECT, the SelectedSeqGroup is set to null.
	 * If the species changes to a specific organism and there's only one choice for the genome versionName, the SelectedSeqGroup is set to that versionName.
	 * Otherwise, the SelectedSetGroup is set to null.
	 */
	private void speciesCBChanged() {
		String speciesName = (String) speciesCB.getSelectedItem();

		// Populate the versionName CB
		refreshVersionCB(speciesName);

		if (gmodel.getSelectedSeqGroup() != null) {
			gmodel.setSelectedSeqGroup(null);
		}
	}

	/**
	 * The versionName combo box changed.
	 * This changes the selected group (either to null, or to a valid group).
	 * It is assumed that at this point, the species is valid.
	 */
	private void versionCBChanged() {
		String versionName = (String) versionCB.getSelectedItem();
		if (DEBUG_EVENTS) {
			System.out.println("Selected version: " + versionName);
		}

		if (versionName.equals(SELECT_GENOME)) {
			// Select the null group (and the null seq), if it's not already selected.
			gmodel.setSelectedSeqGroup(null);
			gmodel.setSelectedSeq(null);
			return;
		}

		AnnotatedSeqGroup group = gmodel.getSeqGroup(versionName);
		if (group == null) {
			System.out.println("Group was null -- trying species instead");
			group = gmodel.getSeqGroup(GeneralLoadUtils.versionName2species.get(versionName));
		}

		speciesCB.setEnabled(false);
		versionCB.setEnabled(false);
		(new InitVersionWorker(versionName, group)).execute();	
	}

	/**
	 * Run initialization of version on thread, so we don't lock up the GUI.
	 * Merge with initVersion();
	 */
	private class InitVersionWorker extends SwingWorker<Void, Void> {

		private final String versionName;
		private final AnnotatedSeqGroup group;

		InitVersionWorker(String versionName, AnnotatedSeqGroup group) {
			this.versionName = versionName;
			this.group = group;
		}

		@Override
		public Void doInBackground() {
			Application.getSingleton().addNotLockedUpMsg("Loading chromosomes...");
			GeneralLoadUtils.initVersionAndSeq(versionName); // Make sure this genome versionName's feature names are initialized.
			return null;
		}

		@Override
		protected void done() {
			Application.getSingleton().removeNotLockedUpMsg("Loading chromosomes...");
			speciesCB.setEnabled(true);
			versionCB.setEnabled(true);
			gmodel.setSelectedSeqGroup(group);
			// TODO: Need to be certain that the group is selected at this point!
			gmodel.setSelectedSeq(group.getSeq(0));
		}
	}



	/**
	 * This gets called when the genome versionName is changed.
	 * This occurs via the combo boxes, or by an external event like bookmarks, or LoadFileAction
	 * @param evt
	 */
	public void groupSelectionChanged(GroupSelectionEvent evt) {
		AnnotatedSeqGroup group = evt.getSelectedGroup();

		if (DEBUG_EVENTS) {
			System.out.println("GeneralLoadView.groupSelectionChanged() called, group: " + (group == null ? null : group.getID()));
		}
		if (group == null) {
			if (versionCB.getSelectedItem() != SELECT_GENOME) {
				versionCB.removeItemListener(this);
				versionCB.setSelectedItem(SELECT_GENOME);
				versionCB.setEnabled(false);
				versionCB.addItemListener(this);
			}
			curGroup = null;
			return;
		}
		if (curGroup == group) {
			if (DEBUG_EVENTS) {
				System.out.println("GeneralLoadView.groupSelectionChanged(): group was same as previous.");
			}
			return;
		}
		curGroup = group;

		List<GenericVersion> gVersions = group.getVersions();
		if (gVersions.isEmpty()) {
			createUnknownVersion(group);
			return;
		}
		String versionName = gVersions.get(0).versionName;
		if (versionName == null) {
			System.out.println("ERROR -- couldn't find version");
			return;
		}
		String speciesName = GeneralLoadUtils.versionName2species.get(versionName);
		if (speciesName == null) {
			// Couldn't find species matching this versionName -- we have problems.
			System.out.println("ERROR - Couldn't find species for version " + versionName);
			return;
		}

		if (!speciesName.equals(speciesCB.getSelectedItem())) {
			// Set the selected species (the combo box is already populated)
			speciesCB.removeItemListener(this);
			speciesCB.setSelectedItem(speciesName);
			speciesCB.addItemListener(this);
		}
		if (!versionName.equals(versionCB.getSelectedItem())) {
			refreshVersionCB(speciesName);			// Populate the versionName CB
			versionCB.removeItemListener(this);
			versionCB.setSelectedItem(versionName);
			versionCB.addItemListener(this);
		}

		clearFeaturesTable();

		disableAllButtons();
	}

	/**
	 * Changed the selected chromosome.
	 * @param evt
	 */
	public void seqSelectionChanged(SeqSelectionEvent evt) {
		BioSeq aseq = evt.getSelectedSeq();

		if (DEBUG_EVENTS) {
			System.out.println("GeneralLoadView.seqSelectionChanged() called, aseq: " + (aseq == null ? null : aseq.getID()));
		}

		if (aseq == null) {
			clearFeaturesTable();
			disableAllButtons();
			return;
		}

		// validate that this sequence is in our group.
		AnnotatedSeqGroup group = aseq.getSeqGroup();
		if (group == null) {
			if (DEBUG_EVENTS) {
				System.out.println("sequence was null");
			}
			return;
		}
		List<GenericVersion> gVersions = group.getVersions();
		if (gVersions.isEmpty()) {
			createUnknownVersion(group);
			return;
		}

		String speciesName = (String) this.speciesCB.getSelectedItem();
		String versionName = (String) this.versionCB.getSelectedItem();
		if (speciesName.equals(SELECT_SPECIES) || versionName.equals(SELECT_GENOME)) {
			return;
		}

		if (!(gVersions.get(0).versionName.equals(versionName))) {
			System.out.println("ERROR - version doesn't match");
			return;
		}

		Application.getSingleton().addNotLockedUpMsg("Loading features");

		refreshTreeView();
		createFeaturesTable();
		loadWholeRangeFeatures(versionName);
		Application.getSingleton().removeNotLockedUpMsg("Loading features");
	}


	/**
	 * group has been created independently of the discovery process (probably by loading a file).
	 * create new "unknown" species/versionName.
	 */
	private void createUnknownVersion(AnnotatedSeqGroup group) {
		gmodel.removeGroupSelectionListener(this);
		gmodel.removeSeqSelectionListener(this);

		speciesCB.removeItemListener(this);
		versionCB.removeItemListener(this);
		GenericVersion gVersion = glu.getUnknownVersion(group);
		String species = GeneralLoadUtils.versionName2species.get(gVersion.versionName);
		refreshSpeciesCB();
		if (DEBUG_EVENTS) {
			System.out.println("Species is " + species + ", version is " + gVersion.versionName);
		}

		if (!species.equals(speciesCB.getSelectedItem())) {
			gmodel.removeGroupSelectionListener(this);
			gmodel.removeSeqSelectionListener(this);

			speciesCB.removeItemListener(this);
			versionCB.removeItemListener(this);

			// Set the selected species (the combo box is already populated)
			speciesCB.setSelectedItem(species);
			// populate the versionName combo box.
			refreshVersionCB(species);
		}

		initVersion(gVersion.versionName);

		versionCB.setSelectedItem(gVersion.versionName);
		versionCB.setEnabled(true);
		all_residuesB.setEnabled(false);
		partial_residuesB.setEnabled(false);
		refresh_dataB.setEnabled(false);
		addListeners();
	}


	private void clearFeaturesTable() {
		this.feature_model = new FeaturesTableModel(this, null, null);
		this.feature_table.setModel(this.feature_model);
		featuresTableScrollPane.setViewportView(this.feature_table);
		feature_tree_view.clearTreeView();
	}

	private void refreshTreeView() {
		String versionName = (String) versionCB.getSelectedItem();
		List<GenericFeature> features = GeneralLoadUtils.getFeatures(versionName);
		if (features == null || features.isEmpty()) {
			clearFeaturesTable();
			feature_tree_view.clearTreeView();
			return;
		}
		feature_tree_view.initOrRefreshTree(features);
	}

	/**
	 * Create the table with the list of features and their status.
	 */
	void createFeaturesTable() {
		String versionName = (String) this.versionCB.getSelectedItem();
		BioSeq curSeq = gmodel.getSelectedSeq();
		if (DEBUG_EVENTS) {
			System.out.println("Creating new table with chrom " + (curSeq == null ? null : curSeq.getID()));
		}

		List<GenericFeature> features = GeneralLoadUtils.getFeatures(versionName);
		if (DEBUG_EVENTS) {
			System.out.println("features for " + versionName + ": " + features.toString());
		}
		
		if (DEBUG_EVENTS) {
			System.out.println("Creating table with features: " + features.toString());
		}

		this.feature_model = new FeaturesTableModel(this, features, curSeq);
		this.feature_model.fireTableDataChanged();
		this.feature_table = new JTableX(this.feature_model);
		this.feature_table.setRowHeight(20);    // TODO: better than the default value of 16, but still not perfect.

		// Handle sizing of the columns
		this.feature_table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);   // Allow columns to be resized
		int maxFeatureNameLength = 1;
		for (GenericFeature feature : features) {
			maxFeatureNameLength = Math.max(maxFeatureNameLength, feature.featureName.length());
		}
		// the second column contains the feature names.  Resize it so that feature names are fully displayed.
		TableColumn col = this.feature_table.getColumnModel().getColumn(FeaturesTableModel.FEATURE_NAME_COLUMN);
		col.setPreferredWidth(maxFeatureNameLength);

		// Don't enable combo box for full genome sequence
		TableWithVisibleComboBox.setComboBoxEditors(
				this.feature_table, FeaturesTableModel.LOAD_STRATEGY_COLUMN, !GeneralLoadView.IsGenomeSequence());

		
		/* COMMENTED OUT.  The Track Info table makes the data load view
		 *                 too busy, so for now, the code is commented out
		 */		
		//Listen for selection of feature to fill in track info
//		feature_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		feature_table.getSelectionModel().addListSelectionListener(
//				new ListSelectionListener() {
//
//					public void valueChanged(ListSelectionEvent event) {
//						int row = feature_table.getSelectedRow();
//						if (row >= 0) {
//							GenericFeature feature = feature_model.getFeature(row);
//							if (feature != null) {
//								track_info_view.initializeFeature(feature);
//							}
//						}
//					}
//				});
		
		
		this.feature_model.fireTableDataChanged();
		featuresTableScrollPane.setViewportView(this.feature_table);


		disableButtonsIfNecessary();
		changeVisibleDataButtonIfNecessary(features);	// might have been disabled when switching to another chromosome or genome.
	}

	/**
	 * Load any features that have a whole strategy and haven't already been loaded.
	 * @param versionName
	 */
	private void loadWholeRangeFeatures(String versionName) {
		BioSeq curSeq = gmodel.getSelectedSeq();
		for (GenericFeature gFeature : GeneralLoadUtils.getFeatures(versionName)) {
			if (gFeature.loadStrategy != LoadStrategy.GENOME) {
				continue;
			}

			if (!gFeature.LoadStatusMap.containsKey(curSeq)) {
				System.out.println("ERROR!  " + curSeq.getID() + " does not contain feature status");
			}
			LoadStatus ls = gFeature.LoadStatusMap.get(curSeq);
			if (ls != LoadStatus.UNLOADED) {
				continue;
			}
			if (gFeature.gVersion.gServer.serverType == ServerType.QuickLoad) {
				// These have already been loaded(QuickLoad is loaded for the entire genome at once)
				if (ls == LoadStatus.UNLOADED) {
					gFeature.LoadStatusMap.put(curSeq, LoadStatus.LOADED);
				}
				continue;
			}

			if (DEBUG_EVENTS) {
				System.out.println("Selected : " + gFeature.featureName);
			}
			GeneralLoadUtils.loadAndDisplayAnnotations(gFeature, curSeq, feature_model);
		}
	}

	/**
	 * Don't allow buttons to be used if they're not valid.
	 */
	private void disableButtonsIfNecessary() {
		// Don't allow buttons for a full genome sequence
		boolean enabled = !IsGenomeSequence();
		if (enabled) {
			BioSeq curSeq = gmodel.getSelectedSeq();
			enabled = curSeq.getSeqGroup() != null;	// Don't allow a null sequence group either.
			if (enabled) {		// Don't allow buttons for an "unknown" versionName
				List<GenericVersion> gVersions = curSeq.getSeqGroup().getVersions();
				enabled = (!gVersions.isEmpty() && gVersions.get(0).gServer.serverType != ServerType.Unknown);
			}
		}

		all_residuesB.setEnabled(enabled);
		partial_residuesB.setEnabled(enabled);
		refresh_dataB.setEnabled(enabled);
	}

	private void disableAllButtons() {
		all_residuesB.setEnabled(false);
		partial_residuesB.setEnabled(false);
		refresh_dataB.setEnabled(false);
	}

	/**
	 * Accessor method.
	 * See if we need to enable/disable the refresh_dataB button
	 * by looking at the features' load strategies.
	 */
	void changeVisibleDataButtonIfNecessary(List<GenericFeature> features) {
		if (IsGenomeSequence()) {
			return;
		// Currently not enabling this button for the full sequence.
		}
		boolean enabled = false;
		for (GenericFeature gFeature : features) {
			if (gFeature.loadStrategy == LoadStrategy.VISIBLE || gFeature.loadStrategy == LoadStrategy.CHROMOSOME) {
				enabled = true;
				break;
			}
		}

		if (refresh_dataB.isEnabled() != enabled) {
			refresh_dataB.setEnabled(enabled);
		}
	}

	private static boolean IsGenomeSequence() {
		// hardwiring names for genome and encode virtual seqs, need to generalize this
		BioSeq curSeq = gmodel.getSelectedSeq();
		final String seqID = curSeq == null ? null : curSeq.getID();
		return (seqID == null || ENCODE_REGIONS_ID.equals(seqID) || GENOME_SEQ_ID.equals(seqID));
	}

}

