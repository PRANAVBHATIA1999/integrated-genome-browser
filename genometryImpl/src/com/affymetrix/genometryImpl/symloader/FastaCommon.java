package com.affymetrix.genometryImpl.symloader;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * common parent class for all Fasta SymLoaders
 * @author jnicol
 */
public abstract class FastaCommon extends SymLoader {
	private static final List<String> pref_list = new ArrayList<String>();
	static {
		pref_list.add("fa");
	}

	protected final List<BioSeq> chrSet = new ArrayList<BioSeq>();

	private static final List<LoadStrategy> strategyList = new ArrayList<LoadStrategy>();
	static {
		strategyList.add(LoadStrategy.NO_LOAD);
		strategyList.add(LoadStrategy.VISIBLE);
		strategyList.add(LoadStrategy.CHROMOSOME);
	}

	public FastaCommon(URI uri, String featureName, AnnotatedSeqGroup group) {
		super(uri, "", group);
		this.isResidueLoader = true;
	}

	@Override
	public void init() throws Exception {
		if (this.isInitialized) {
			return;
		}
		if(initChromosomes())
			super.init();
	}

	@Override
	public List<LoadStrategy> getLoadChoices() {
		return strategyList;
	}

	@Override
	public List<BioSeq> getChromosomeList() throws Exception {
		init();
		return chrSet;
	}

	/**
	 * Get seqids and lengths for all chromosomes.
	 */
	protected abstract boolean initChromosomes() throws Exception;

	@Override
	public List<String> getFormatPrefList() {
		return pref_list;
	}
}
