package com.affymetrix.igb.view.load;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
import javax.swing.table.JTableHeader;

import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.genometry.SeqSpan;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.genometryImpl.SmartAnnotBioSeq;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;

import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.general.GenericFeature;
import com.affymetrix.igb.general.GenericVersion;
import com.affymetrix.igb.general.Persistence;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.load.GeneralLoadUtils.LoadStatus;
import com.affymetrix.igb.view.load.GeneralLoadUtils.LoadStrategy;

import org.jdesktop.swingworker.SwingWorker;


final public class GeneralLoadView extends JComponent
				implements ItemListener, ActionListener, GroupSelectionListener, SeqSelectionListener {

	GeneralLoadUtils glu;
	private static final boolean DEBUG_EVENTS = false;
	private static final SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
	private static final String SELECT = "Select";
	private static final String GENOME_SEQ_ID = "genome";
	private static final String ENCODE_REGIONS_ID = "encode_regions";
	private JComboBox kingdomCB;
	private final JComboBox versionCB;
	private final JComboBox speciesCB;
	private JPanel feature_panel;
	private JButton all_residuesB;
	private JButton partial_residuesB;
	private final JButton refresh_dataB;
	private AnnotatedSeqGroup current_group;
	private SmartAnnotBioSeq current_seq;
	private SeqMapView gviewer;
	private JTableX feature_table;
	private FeaturesTableModel feature_model;


	public GeneralLoadView() {
		if (Application.getSingleton() != null) {
			gviewer = Application.getSingleton().getMapView();
		}

		this.glu = new GeneralLoadUtils();

		this.setLayout(new BorderLayout());
		feature_panel = new JPanel();
		feature_panel.setLayout(new BoxLayout(feature_panel, BoxLayout.Y_AXIS));

		JPanel choice_panel = new JPanel();
		choice_panel.setLayout(new BoxLayout(choice_panel, BoxLayout.X_AXIS));
		choice_panel.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));

		/*kingdomCB = new JComboBox();
		kingdomCB.setEnabled(false);
		kingdomCB.setEditable(false);
		choice_panel.add(new JLabel("Kingdom:"));
		choice_panel.add(Box.createHorizontalStrut(5));
		choice_panel.add(kingdomCB);
		choice_panel.add(Box.createHorizontalGlue());*/

		speciesCB = new JComboBox();
		speciesCB.setEnabled(false);
		speciesCB.setEditable(false);
		choice_panel.add(new JLabel("Species:"));
		choice_panel.add(Box.createHorizontalStrut(5));
		choice_panel.add(speciesCB);
		choice_panel.add(Box.createHorizontalGlue());

		versionCB = new JComboBox();
		versionCB.setEnabled(false);
		versionCB.setEditable(false);
		choice_panel.add(new JLabel("Genome Version:"));
		choice_panel.add(Box.createHorizontalStrut(5));
		choice_panel.add(versionCB);
		choice_panel.add(Box.createHorizontalStrut(20));


		JPanel buttonP = new JPanel();
		buttonP.setLayout(new GridLayout(1, 3));

		refresh_dataB = new JButton("Refresh Data");
		refresh_dataB.setEnabled(false);
		refresh_dataB.addActionListener(this);
		buttonP.add(refresh_dataB);

		if (IGB.isSequenceAccessible()) {
			all_residuesB = new JButton("Load All Sequence");
			all_residuesB.setEnabled(false);
			all_residuesB.addActionListener(this);
			buttonP.add(all_residuesB);
			partial_residuesB = new JButton("Load Sequence in View");
			partial_residuesB.setEnabled(false);
			if (IGB.ALLOW_PARTIAL_SEQ_LOADING) {
				partial_residuesB.addActionListener(this);
				buttonP.add(partial_residuesB);
			}
		} else {
			buttonP.add(Box.createRigidArea(new Dimension(5, 0)));
			buttonP.add(new JLabel("No sequence available", JLabel.CENTER));
		}

		this.add("North", choice_panel);
		this.add("Center", new JScrollPane(feature_panel));
		this.add("South", buttonP);

		this.setBorder(BorderFactory.createEtchedBorder());

		populateSpeciesData();

	}

	/**
	 * Discover servers, species, etc., asynchronously.
	 */
	private void populateSpeciesData() {
		Executor vexec = Executors.newSingleThreadExecutor();

		SwingWorker worker = new SwingWorker() {
			@Override
			public void done() {
				initializeKingdomCB();
				initializeSpeciesCB();
				RestorePersistentGenome();
				addListeners();
			}

			protected Object doInBackground() throws Exception {
				discoverServersAndSpeciesAndVersions();
				return null;
			}
		};

		vexec.execute(worker);

	}

	private void discoverServersAndSpeciesAndVersions() {
		this.glu.discoverServersAndSpeciesAndVersions();
	}

	private void addListeners() {

		gmodel.addGroupSelectionListener(this);
		gmodel.addSeqSelectionListener(this);

		speciesCB.addItemListener(this);
		versionCB.addItemListener(this);
	}

	private void initializeKingdomCB() {
		// TODO
	}

	/**
	 * Initialize Species combo box.  It is assumed that we have the species data at this point.
	 */
	private void initializeSpeciesCB() {
		speciesCB.removeAllItems();
		speciesCB.addItem(SELECT);

		int speciesListLength = this.glu.species2genericVersionList.keySet().size();
		if (speciesListLength == 0) {
			// Disable the genome_name selectedSpecies.
			speciesCB.setEnabled(false);
			return;
		}

		// Sort the species before presenting them
		SortedSet<String> speciesList = new TreeSet<String>();
		speciesList.addAll(this.glu.species2genericVersionList.keySet());
		for (String genome_name : speciesList) {
			speciesCB.addItem(genome_name);
		}

		speciesCB.setEnabled(true);
		speciesCB.setSelectedIndex(0);
	}

		/**
	 * bootstrap bookmark from Preferences for last species/version/genome / sequence / region
	 * @param gviewer
	 * @return
	 */
	private void RestorePersistentGenome() {
		// Get group and seq info from persistent preferences.
		// (Recovering as much data as possible before activating listeners.)
		AnnotatedSeqGroup group = Persistence.restoreGroupSelection();
		if (group == null) {
			return;
		}

		GenericVersion gVersion = this.glu.group2version.get(group);
		if (gVersion == null || gVersion.versionName == null) {
			return;
		}

		String speciesName = this.glu.versionName2species.get(gVersion.versionName);
		if (speciesName == null) {
			return;
		}

		// Set the selected species (the combo box is already populated)
		speciesCB.setSelectedItem(speciesName);

		// populate the version combo box.
		refreshVersionCB(speciesName);
		
		// select the version, using events to populate the feature and chrom table.
		addListeners();
		versionCB.setSelectedItem(gVersion.versionName);
		
		// Select the persistent chromosome, and restore the span.
		SmartAnnotBioSeq seq = Persistence.restoreSeqSelection(group);
		if (seq == null) {
			return;
		}
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

	/**
	 * Handles clicking of partial residue, all residue, and refresh data buttons.
	 * @param evt
	 */
	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		String genomeVersionName = (String)versionCB.getSelectedItem();
		if (src == partial_residuesB) {
			SeqSpan viewspan = gviewer.getVisibleSpan();
			this.glu.loadResidues(genomeVersionName, current_seq, viewspan.getMin(), viewspan.getMax(), viewspan);
		} else if (src == all_residuesB) {
			this.glu.loadResidues(genomeVersionName, current_seq, 0, current_seq.getLength(), null);
		} else if (src == refresh_dataB) {
			loadVisibleData();
		}
	}

	/**
	 * Load any data that's marked for visible range.
	 */
	private void loadVisibleData() {
		SeqSpan request_span = gviewer.getVisibleSpan();

		if (DEBUG_EVENTS) {
			System.out.println("Visible load request span: " + request_span.getStart() + " " + request_span.getEnd());
		}
		// Load any features that have a visible strategy and haven't already been loaded.
		String genomeVersionName = (String) versionCB.getSelectedItem();
		for (GenericFeature gFeature : this.glu.getFeatures(genomeVersionName)) {
			if (gFeature.loadStrategy != LoadStrategy.VISIBLE) {
				continue;
			}
			// Even if it's already loaded, we may want to reload... for example, if the viewsize changes.

			if (!gFeature.LoadStatusMap.containsKey(current_seq)) {
				// Should never get here.
				System.out.println("ERROR!  " + current_seq.getID() + " does not contain feature status");
			}

			if (DEBUG_EVENTS) {
				System.out.println("Selected : " + gFeature.featureName);
			}
			this.glu.loadAndDisplayAnnotations(gFeature, current_seq, feature_model);
		}
		Application.getSingleton().setStatus("", false);

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
	 * ANY time the species changes, everything will be disabled and cleared aside from the species and version combo boxes.
	 * (That is all handled in the group selection code.)
	 * Additionally, if the species is changed to SELECT, then even the version combo box will be disabled.
	 */
	private void speciesCBChanged() {
		String speciesName = (String) speciesCB.getSelectedItem();
		if (speciesName.equals(SELECT)) {
			// Turn off version combo box
			versionCB.removeItemListener(this);
			versionCB.setSelectedIndex(0);
			versionCB.setEnabled(false);
		} else {
			refreshVersionCB(speciesName);
			versionCB.setEnabled(true);
			versionCB.addItemListener(this);
		}
		
		// Select the null group (and the null seq), if it's not already selected.
		this.ChangeSelectedGroups(null);
	}

	/**
	 * The version combo box changed.
	 * This changes the selected group (either to null, or to a valid group).
	 */
	private void versionCBChanged() {
		String version_name = (String) versionCB.getSelectedItem();
		if (DEBUG_EVENTS) {
			System.out.println("Selected version: " + version_name);
		}
		if (version_name.equals(SELECT)) {
			// Select the null group (and the null seq), if it's not already selected.
			this.ChangeSelectedGroups(null);
		} else {
			AnnotatedSeqGroup group = gmodel.getSeqGroup(version_name);
			if (group == null) {
				System.out.println("Group was null");
				group = gmodel.getSeqGroup(this.glu.versionName2species.get(version_name));
			}
			// Select the group (and the first seq), if it's not already selected.
			this.ChangeSelectedGroups(group);
		}
	}


	/**
	 * Refresh the genome versions, now that the species has changed.
	 * @param speciesName
	 */
	private void refreshVersionCB(String speciesName) {
		versionCB.removeItemListener(this);
		versionCB.removeAllItems();
		versionCB.addItem(SELECT);
		versionCB.setSelectedIndex(0);

		if (speciesName.equals(SELECT)) {
			// Disable the version.
			versionCB.setEnabled(false);
			return;
		}

		// Add version names to combo boxes.
		// Since the same version name may occur on multiple servers, we use sets
		// to eliminate the redundant elements.
		SortedSet<String> versionNames = new TreeSet<String>();
		for (GenericVersion gVersion : this.glu.species2genericVersionList.get(speciesName)) {
			versionNames.add(gVersion.versionName);
		}

		for (String versionName : versionNames) {
			versionCB.addItem(versionName);
		}
	}

	
	/**
	 * This gets called when the genome version is changed.
	 * This occurs via the combo boxes, or by an external event like bookmarks.
	 * @param evt
	 */
	public void groupSelectionChanged(GroupSelectionEvent evt) {
		AnnotatedSeqGroup group = evt.getSelectedGroup();
		if (DEBUG_EVENTS) {
			System.out.println("GeneralLoadView.groupSelectionChanged() called, group: " + (group == null ? null : group.getID()));
		}
		if (current_group != group) {
			current_group = group;

			String genomeVersionName = null;
			if (group == null || this.glu == null || !this.glu.group2version.containsKey(group)) {
				genomeVersionName = null;
			} else {
				genomeVersionName = this.glu.group2version.get(group).versionName;
			}

			groupSelectionChangedInternal(genomeVersionName);
		}
	}

	/**
	 * The genome version actually changed, rather than just someone choosing the same version.
	 *
	 * @param group
	 */
	private void groupSelectionChangedInternal(String genomeVersionName) {
		if (DEBUG_EVENTS) {
			System.out.println("groupSelectionChangedInternal to " + genomeVersionName);
		}

		String selectedSpecies = (String) speciesCB.getSelectedItem();
		String selectedVersion = (String) versionCB.getSelectedItem();
		
		if (genomeVersionName == null) {
			// group and/or genome is null.  Disable most everything.

			// This could happen if, say, someone loaded a file directly.
			return;
		}
		
		// A valid group was selected.

		// Don't allow combo boxes to be selected while refreshing.
		speciesCB.removeItemListener(this);
		versionCB.removeItemListener(this);

		if (selectedSpecies.equals(SELECT) || selectedVersion.equals(SELECT)) {
			// We need to be sure that the combo boxes are set properly.
			// They could have been set improperly if called from bookmark code.
			speciesCB.setSelectedItem(this.glu.versionName2species.get(genomeVersionName));
			if (SELECT.equals(selectedVersion)) {
				versionCB.setSelectedItem(genomeVersionName);
				versionCB.setEnabled(true);
			}
		}
		
		// Initialize this genome version.
		this.glu.initVersion(genomeVersionName);
		this.glu.initSeq(genomeVersionName);

		speciesCB.addItemListener(this);
		versionCB.addItemListener(this);

		speciesCB.setEnabled(true);
		versionCB.setEnabled(true);
	}



	/**
	 * Changed the selected chromosome.
	 * @param evt
	 */
	public void seqSelectionChanged(SeqSelectionEvent evt) {
		current_seq = (SmartAnnotBioSeq)evt.getSelectedSeq();

		if (DEBUG_EVENTS) {
			System.out.println("GeneralLoadView.seqSelectionChanged() called, current_seq: " + (current_seq == null ? null : current_seq.getID()));
		}

		disableButtonsIfNullOrGenomeSequence();	// disable buttons if we need to.

		this.feature_panel.removeAll();

		String speciesName = (String) speciesCB.getSelectedItem();
		String versionName = (String) this.versionCB.getSelectedItem();
		if (current_seq == null || speciesName.equals(SELECT) || versionName.equals(SELECT)) {
			return;
		}

		createFeaturesTable(versionName);

		loadWholeRangeFeatures(versionName);
	}

		/**
	 * Create the table with the list of features and their status.
	 */
	private void createFeaturesTable(String genomeVersionName) {
		if (DEBUG_EVENTS) {
			System.out.println("Creating new table with chrom " + (current_seq == null ? null : current_seq.getID()));
		}

		List<GenericFeature> features = this.glu.getFeatures(genomeVersionName);
		if (DEBUG_EVENTS) {
			System.out.println("features: " + features.toString());
		}
		this.feature_model = new FeaturesTableModel(this, features, current_seq);

		this.feature_table = new JTableX(this.feature_model);
		this.feature_table.setRowHeight(20);    // TODO: better than the default value of 16, but still not perfect.
		this.feature_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);   // Allow columns to be resized

		// Must explicitly show the header, since we're not in a scroll panel
		JTableHeader header = this.feature_table.getTableHeader();
    this.feature_panel.setLayout(new BorderLayout());
    this.feature_panel.add(header, BorderLayout.NORTH);
    this.feature_panel.add(this.feature_table, BorderLayout.CENTER);


		// Don't enable combo box for full genome sequence
		TableWithVisibleComboBox.setComboBoxEditors(this.feature_table, 0, !this.IsGenomeSequence());

		this.feature_panel.add(this.feature_table);
		this.feature_model.fireTableDataChanged();
		this.feature_panel.invalidate();

		changeVisibleDataButtonIfNecessary(features);	// might have been disabled when switching to another chromosome or genome.
	}

	/**
	 * Load any features that have a whole strategy and haven't already been loaded.
	 * @param versionName
	 */
	private void loadWholeRangeFeatures(String versionName) {
		for (GenericFeature gFeature : this.glu.getFeatures(versionName)) {
			if (gFeature.loadStrategy != LoadStrategy.WHOLE) {
				continue;
			}
			if (!gFeature.LoadStatusMap.containsKey(current_seq)) {
				System.out.println("ERROR!  " + current_seq.getID() + " does not contain feature status");
			}
			if (gFeature.LoadStatusMap.get(current_seq) != LoadStatus.UNLOADED) {
				continue;
			}
			if (DEBUG_EVENTS) {
				System.out.println("Selected : " + gFeature.featureName);
			}
			this.glu.loadAndDisplayAnnotations(gFeature, current_seq, feature_model);
		}
	}



	/**
	 * Don't allow buttons to be used if we're viewing the entire sequence.
	 * (We do this because it's not clear if the user REALLY would want to load all of the specified data.)
	 * (In fact, for the full sequence this would currently be too much memory for the app.)
	 * @param seqID
	 */
	private void disableButtonsIfNullOrGenomeSequence() {
		boolean isGenomeSequence = IsGenomeSequence();
		all_residuesB.setEnabled(!isGenomeSequence);
		partial_residuesB.setEnabled(!isGenomeSequence);
		refresh_dataB.setEnabled(!isGenomeSequence);
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
			if (gFeature.loadStrategy == LoadStrategy.VISIBLE) {
				enabled = true;
				break;
			}
		}

		if (refresh_dataB.isEnabled() != enabled) {
			refresh_dataB.setEnabled(enabled);
		}
	}

	boolean IsGenomeSequence() {
		// hardwiring names for genome and encode virtual seqs, need to generalize this
		final String seqID = current_seq == null ? null : current_seq.getID();
		return (seqID == null || ENCODE_REGIONS_ID.equals(seqID) || GENOME_SEQ_ID.equals(seqID));
	}


	/**
	 * Calling gmodel.setSelectedSeq() will also bounce event back to this.seqSelectionChanged()
	 * calling gmodel.setSelectedSeqGroup() will also bounce event back to this.groupSelectionChanged()
	 * @param sabq
	 */
	private void ChangeSelectedGroups(AnnotatedSeqGroup group) {
		gmodel.setSelectedSeqGroup(group);
		
		// Note that this SmartAnnotBioSeq may be set by the gmodel, above.
		SmartAnnotBioSeq sabq = group == null ? null : group.getSeq(0);
		gmodel.setSelectedSeq(sabq);
	}
}

