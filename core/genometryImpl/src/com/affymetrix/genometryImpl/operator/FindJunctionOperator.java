package com.affymetrix.genometryImpl.operator;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.filter.ChildThresholdFilter;
import com.affymetrix.genometryImpl.filter.NoIntronFilter;
import com.affymetrix.genometryImpl.filter.SymmetryFilterI;
import com.affymetrix.genometryImpl.filter.UniqueLocationFilter;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleSymWithProps;
import com.affymetrix.genometryImpl.symmetry.UcscBedSym;
import com.affymetrix.genometryImpl.util.SeqUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Anuj
 */
public class FindJunctionOperator extends AbstractAnnotationTransformer implements Operator{
	public static final String THRESHOLD = "threshold";
	public static final String TWOTRACKS = "twoTracks";
	public static final String UNIQUENESS = "uniqueness";
	
	/**
	 * TopHat style flanking makes the junction flanks as long as the largest length 
	 * of extrons from each side of a qualified intron.
	 * 
	 * If not specifying TopHat style, the flank length equals to threadhold (5 by default) 
	 * 
	 */
	public static final String TOPHATSTYLEFLANKING = "topHatStyleFlanking";
	
    public static final int default_threshold = 5;
	public static final boolean default_twoTracks = true;
	public static final boolean default_uniqueness = true;
	public static final boolean default_topHatStyleFlanking = false;
	
	private static final SymmetryFilterI noIntronFilter = new NoIntronFilter();
    private static final SymmetryFilterI childThresholdFilter = new ChildThresholdFilter();
    private static final SymmetryFilterI uniqueLocationFilter = new UniqueLocationFilter();
    
    private int threshold;
    private boolean twoTracks;
	private boolean uniqueness;
	private boolean topHatStyleFlanking;
	
    public FindJunctionOperator(){
		super(FileTypeCategory.Alignment);
		threshold = default_threshold;
		twoTracks = default_twoTracks;
		uniqueness = default_uniqueness;
		topHatStyleFlanking = default_topHatStyleFlanking;
    }   
    
    @Override
    public String getName() {
        return "findjunctions";
    }
    
    /* This is an Operator method which is used to operates on a given list of symmetries and find the junctions between them
	 * by applying different kinds of filters and writes the resultant symmetries onto a Symmetry Container.
	 */
	
	@Override
    public SeqSymmetry operate(BioSeq bioseq, List<SeqSymmetry> list) {
		
		SimpleSymWithProps container = new SimpleSymWithProps();
		if(list.isEmpty())
			return container;
		SeqSymmetry topSym = list.get(0);
		List<SeqSymmetry> symList = new ArrayList<SeqSymmetry>();
		for(int i=0; i<topSym.getChildCount(); i++){
			symList.add(topSym.getChild(i));
		}
        HashMap<String, SeqSymmetry> map = new HashMap<String ,SeqSymmetry>();
        subOperate(bioseq, symList, map);
		for(SeqSymmetry sym : map.values()){
			container.addChild(sym);
		}
        map.clear();
        symList.clear();
		
        return container;
    }
    
    /*
	 * This is specifically used to apply the filters on the given list of symmetries and updates the resultant hash map
	 * with the resultant symmetries.
	 */
	
	public void subOperate(BioSeq bioseq, List<SeqSymmetry> list, HashMap<String, SeqSymmetry> map){
      for(SeqSymmetry sym : list){
            if(noIntronFilter.filterSymmetry(bioseq, sym) && ((!uniqueness) || (uniqueness && uniqueLocationFilter.filterSymmetry(bioseq, sym)))){
                updateIntronHashMap(sym , bioseq, map, threshold, twoTracks, topHatStyleFlanking);
            }
        }
    }
	
    @Override
    public boolean setParameters(Map<String, Object> map) {
        if(map.size() <= 0)
            return false;                
        for(String s: map.keySet()){
            if(s.equalsIgnoreCase(THRESHOLD))
                threshold = (Integer)map.get(s);
            else if(s.equalsIgnoreCase(TWOTRACKS))
                twoTracks = (Boolean)map.get(s);
            else if(s.equalsIgnoreCase(UNIQUENESS))
                uniqueness = (Boolean)map.get(s);
			else if(s.equalsIgnoreCase(TOPHATSTYLEFLANKING))
				topHatStyleFlanking = (Boolean)map.get(s);
        }
        return true;
    }


    @Override
    public boolean supportsTwoTrack() {
        return true;
    }
   
    /* This method splits the given Sym into introns and filters out the qualified Introns
	 * and adds the qualified introns into map using addtoMap method
	 */
    private static void updateIntronHashMap(SeqSymmetry sym , BioSeq bioseq, HashMap<String, SeqSymmetry> map, int threshold, boolean twoTracks, boolean topHatStyleFlanking){
        List<Integer> childIntronIndices = new ArrayList<Integer>();
        int childCount = sym.getChildCount();
		int flanksLength[] = new int[2];
        childThresholdFilter.setParam(threshold);
        for(int i=0;i<childCount - 1;i++){
            if(childThresholdFilter.filterSymmetry(bioseq, sym.getChild(i)) && childThresholdFilter.filterSymmetry(bioseq, sym.getChild(i+1))){
                childIntronIndices.add(i);
            }
        }
        if(childIntronIndices.size() > 0){
			SeqSymmetry intronChild, intronSym;
            intronSym = SeqUtils.getIntronSym(sym, bioseq);
            for(Integer i : childIntronIndices){
                intronChild = intronSym.getChild(i);
                if(intronChild != null){
					int leftExtronLength = sym.getChild(i).getSpan(bioseq).getLength();
					int rightExtronLength = sym.getChild(i+1).getSpan(bioseq).getLength();
					flanksLength[0] = leftExtronLength;
					flanksLength[1] = rightExtronLength;
					SeqSpan span = intronChild.getSpan(bioseq);
                    addToMap(span, map, bioseq, threshold, twoTracks, topHatStyleFlanking, flanksLength);
				}
            }
        }
    }
    
    /*
	 * This builds the JunctionUcscBedSym based on different properties of sym and adds the sym into map.
	 */
	private static void addToMap(SeqSpan span , HashMap<String, SeqSymmetry> map, BioSeq bioseq, int threshold, boolean twoTracks, boolean topHatStyleFlanking, int[] flanksLength){
       
        boolean currentForward = false;
		String name = "J:" + bioseq.getID() + ":" + span.getMin() + "-" + span.getMax() + ":";
		if(map.containsKey(name)){
			JunctionUcscBedSym sym = (JunctionUcscBedSym)map.get(name);
			if(!twoTracks){
				currentForward = sym.isCanonical() ? sym.isForward() : (sym.isRare() ? span.isForward() : sym.isForward());					
			}
			else{
				currentForward = span.isForward();
			}
			
			if(topHatStyleFlanking) {
				
				/**
				 * txMin/blockMins(txMax/blockMaxs) from UcscBedSym should be updated to get a longer flank
				 * A new symmentry will be created and old properties kept
				 */

				// Remember current symmetry's properties
				int[] oldBlockMins = sym.getBlockMins();
				int[] oldBlockMaxs = sym.getBlockMaxs();
				boolean canonical = sym.canonical;
				boolean rare = sym.rare;
				int localScore = sym.localScore;
				int positiveScore = sym.positiveScore;
				int negativeScore = sym.negativeScore;

				if (span.getMin() - sym.getBlockMins()[0] < flanksLength[0]) {
					
					/**
					 * Create a symmetry if a longer left flank length found
					 * Use new blockMins property and retain all other properties
					 * txMin will be updated upon blockMins
					 */
					
					sym = new JunctionUcscBedSym(bioseq, name, 
							currentForward, 
							new int[]{span.getMin() - flanksLength[0], span.getMax()}, // new length of left flank 
							oldBlockMaxs, canonical, rare,
							localScore, positiveScore, negativeScore
							);
				}
				
				if(sym.getBlockMaxs()[1] - span.getMax() < flanksLength[1]) {
					
					/**
					 * Create a symmetry if a longer right flank length found
					 * Use new blockMaxs property and retain all other properties
					 * txMax will be updated upon blockMaxs
					 */
					
					sym = new JunctionUcscBedSym(bioseq, name, 
							currentForward, 
							oldBlockMins, 
							new int[]{span.getMin(), span.getMax() + flanksLength[1]}, // new length of right flank
							canonical, rare,
							localScore, positiveScore, negativeScore
							);
				}
			}
			
			sym.updateScore(currentForward);
			map.put(name, sym);
		}
		else{
			boolean canonical = true;
			boolean rare = false;
			if(!twoTracks){
				String leftResidues = bioseq.getResidues(span.getMin(), span.getMin() + 2);
				String rightResidues = bioseq.getResidues(span.getMax() - 2, span.getMax());
	            if(leftResidues.equalsIgnoreCase("GT") && rightResidues.equalsIgnoreCase("AG")){
		            canonical = true;
			        currentForward = true;
				}
				else if(leftResidues.equalsIgnoreCase("CT") && rightResidues.equalsIgnoreCase("AC")){
					canonical = true;
					currentForward = false;
				}
				else if((leftResidues.equalsIgnoreCase("AT") && rightResidues.equalsIgnoreCase("AC")) || 
					    (leftResidues.equalsIgnoreCase("GC") && rightResidues.equalsIgnoreCase("AG"))){
					canonical = false;
					currentForward = true;
				}
				else if((leftResidues.equalsIgnoreCase("GT") && rightResidues.equalsIgnoreCase("AT")) || 
					    (leftResidues.equalsIgnoreCase("CT") && rightResidues.equalsIgnoreCase("GC"))){
					canonical = false;
					currentForward = false;
				}
				else{
					canonical = false;
					currentForward = span.isForward();
					rare = true;
				}
			}
			else{
				currentForward = span.isForward();
			}
			

			// Create TopHat style flanking if requested by parameter
			
			int[] blockMins = new int[2]; 
			int[] blockMaxs = new int[2];
			
			if(topHatStyleFlanking) {
				blockMins = new int[]{span.getMin() - flanksLength[0], span.getMax()};
				blockMaxs = new int[]{span.getMin(), span.getMax() + flanksLength[1]};
			} else {
				blockMins = new int[]{span.getMin() - threshold, span.getMax()};
				blockMaxs = new int[]{span.getMin(), span.getMax() + threshold};
			}
			
            JunctionUcscBedSym tempSym = new JunctionUcscBedSym(bioseq, name, 
					currentForward, blockMins, blockMaxs, canonical, rare, 0, 0, 0);			
            map.put(name, (SeqSymmetry)tempSym);
        }
    }
    
	/*
	 * Specific BED Sym used for Junction representation which has some extra parameters than a normal UcscBedSym
	 */
	private static class JunctionUcscBedSym extends UcscBedSym {

		int positiveScore, negativeScore;
		int localScore;
		boolean canonical, rare;

		private JunctionUcscBedSym(BioSeq seq, String name, boolean forward, 
				int[] blockMins, int[] blockMaxs, boolean canonical, boolean rare, int localScore, int positiveScore, int negativeScore) {
			super(name, seq, blockMins[0], blockMaxs[1], name, 1, forward, 
					0, 0, blockMins, blockMaxs);
			
			if(localScore > 1) {
				this.localScore = localScore;
			} else {
				this.localScore = 1;
			}
			
			if(positiveScore > 0) {
				this.positiveScore = positiveScore;
			} else {
				this.positiveScore = forward? 1 : 0;
			}
			
			if(positiveScore > 0) {
				this.negativeScore = negativeScore;
			} else {
				this.negativeScore = forward? 0 : 1;
			}
			
			this.canonical = canonical;
			this.rare = rare;
		}

		private void updateScore(boolean isForward) {
			localScore++;
			if (!canonical) {
				if (isForward) {
					this.positiveScore++;
				} else {
					this.negativeScore++;
				}
			}
		}
		
		@Override
		public float getScore() {
			return localScore;
		}

		@Override
		protected String getScoreString(){
			return Integer.toString(localScore);
		}
		
		@Override
		public Map<String, Object> cloneProperties() {
			Map<String, Object> tprops = super.cloneProperties();
			tprops.put("score", localScore);
			if(!canonical){
				tprops.put("canonical", canonical);
				tprops.put("positive_score", positiveScore);
				tprops.put("negative_score", negativeScore);
			}
			return tprops;
		}

		@Override
		public Object getProperty(String key) {
			if (key.equals("score")) {
				return localScore;
			}
			return super.getProperty(key);
		}
		
		@Override
		public String getName() {
			return getID();
		}

		@Override
		public String getID() {
			return super.getID() + (isForward() ? "+" : "-");
		}

		@Override
		public boolean isForward() {
			return canonical ? super.isForward() : positiveScore > negativeScore ? true : false;
		}
		
		public boolean isCanonical(){
			return canonical;
		}
		
		public boolean isRare(){
			return rare;
		}
	}
}