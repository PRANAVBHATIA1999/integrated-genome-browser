package com.affymetrix.genometryImpl.symloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;

import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.style.DefaultTrackStyle;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.style.ITrackStyle;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symloader.BAM;
import com.affymetrix.genometryImpl.symloader.LineProcessor;
import com.affymetrix.genometryImpl.symloader.LineTrackerI;
import com.affymetrix.genometryImpl.symmetry.BAMSym;
import com.affymetrix.genometryImpl.symmetry.GraphIntervalSym;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleSymWithProps;

import org.broad.tribble.readers.LineReader;

public class VCF extends UnindexedSymLoader implements LineProcessor {
	private static final String[] EXTENSIONS = new String[]{"vcf"};
	private static final String NO_DATA = ".";
	private static final Pattern line_regex = Pattern.compile("\\s+");
	private static final Pattern info_regex = Pattern.compile(";");

	private enum Type {
//		Numeric,
		Integer,
		String,
		Float,
		Flag
	}
	private class INFO {
		private final String ID;
//		private final int number;
		private final Type type;
		private final String description;
//		private final boolean onePerAllele;
//		private final boolean onePerGenotype;
		public INFO(String ID, int number, Type type, String description, boolean onePerAllele, boolean onePerGenotype) {
			this.ID = ID;
//			this.number = number;
			this.type = type;
			this.description = description;
//			this.onePerAllele = onePerAllele;
//			this.onePerGenotype = onePerGenotype;
		}
		public String getID() {
			return ID;
		}
//		public int getNumber() {
//			return number;
//		}
		public Type getType() {
			return type;
		}
		public String getDescription() {
			return description;
		}
//		public boolean isOnePerAllele() {
//			return onePerAllele;
//		}
//		public boolean isOnePerGenotype() {
//			return onePerGenotype;
//		}
	}
	private class FILTER {
		private final String ID;
		private final String description;
		public FILTER(String ID, String description) {
			this.ID = ID;
			this.description = description;
		}
		public String getID() {
			return ID;
		}
		public String getDescription() {
			return description;
		}
	}
	private class FORMAT {
		private final String ID;
//		private final int number;
		private final Type type;
//		private final String description;
		public FORMAT(String ID, int number, Type type, String description) {
			this.ID = ID;
//			this.number = number;
			this.type = type;
//			this.description = description;
		}
		public String getID() {
			return ID;
		}
//		public int getNumber() {
//			return number;
//		}
		public Type getType() {
			return type;
		}
//		public String getDescription() {
//			return description;
//		}
	}
//	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	private double version = -1.0;
//	private Date date;
	private String[] samples = new String[]{};
	private Map<String, String> metaMap = new HashMap<String, String>();
	private Map<String, INFO> infoMap = new HashMap<String, INFO>();
	private Map<String, FILTER> filterMap = new HashMap<String, FILTER>();
	private Map<String, FORMAT> formatMap = new HashMap<String, FORMAT>();
	private boolean combineGenotype;
	private List<String> selectedFields = new ArrayList<String>();

	static {
		Set<String> types = new HashSet<String>();

		types.add("protein");
	}

	private static final Pattern idPattern = Pattern.compile(",ID=\\w+,");
	private static final Pattern numberPattern = Pattern.compile(",Number=\\w+,");
	private static final Pattern typePattern = Pattern.compile(",Type=\\w+,");
	private static final Pattern descriptionPattern = Pattern.compile(",Description=\\\"[^\\\"]+\\\",");

	public VCF(URI uri, String featureName, AnnotatedSeqGroup group) {
		super(uri, featureName, group);
	}

	/**
	 *  Parses VCF format
	 */
	public List<? extends SeqSymmetry> processLines(BioSeq seq, final LineReader lineReader, LineTrackerI lineTracker) {
		SimpleSymWithProps mainSym = new SimpleSymWithProps();
		mainSym.setProperty("seq", seq);
		mainSym.setProperty("type", featureName);
		mainSym.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
		int line_count = 0;
		Map<String, SimpleSymWithProps> dataMap = new HashMap<String, SimpleSymWithProps>();
		Map<String, GraphData> graphDataMap = new HashMap<String, GraphData>();
		Map<String, SimpleSymWithProps> genotypeDataMap = new HashMap<String, SimpleSymWithProps>();

		String line = null;

		try {
			while ((line = lineReader.readLine()) != null && (!Thread.currentThread().isInterrupted())) {
				if (line.startsWith("#")) {
					line_count++;
					continue;
				}
				else if (line.length() > 0) {
					processDataLine(mainSym, seq, 0, Integer.MAX_VALUE, featureName, dataMap, graphDataMap, genotypeDataMap, line, line_count, combineGenotype);
					line_count++;
				}
			}
		}
		catch (Exception x) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.SEVERE, "failed to parse vcf file ", x);
		}
		SeqSpan span = new SimpleSeqSpan(seq.getMin(), seq.getMax(), seq);
		List<SeqSymmetry> symlist = new ArrayList<SeqSymmetry>();
		if (mainSym.getChildCount() > 0) {
			mainSym.addSpan(span);
		}
		symlist.add(mainSym);
		for (String key : dataMap.keySet()) {
			SimpleSymWithProps container = dataMap.get(key);
			container.addSpan(span);
			symlist.add(container);
		}
		for (String key : genotypeDataMap.keySet()) {
			SimpleSymWithProps container = genotypeDataMap.get(key);
			container.addSpan(span);
			symlist.add(container);
		}
		Map<String, ITrackStyle> styleMap = new HashMap<String, ITrackStyle>();
		for (String key : graphDataMap.keySet()) {
			GraphData graphData = graphDataMap.get(key);
			int dataSize = graphData.xData.size();
			int[] xList = Arrays.copyOf(graphData.xData.elements(), dataSize);
			float[] yList = Arrays.copyOf(graphData.yData.elements(), dataSize);
			int[] wList =  Arrays.copyOf(graphData.wData.elements(), dataSize);
			GraphIntervalSym graphIntervalSym = new GraphIntervalSym(xList, wList, yList, key, seq);
			String comboKey = key.substring(0, key.lastIndexOf('/'));
			GraphState gstate = graphIntervalSym.getGraphState();
			if (combineGenotype && key.indexOf('/') != key.lastIndexOf('/')) {
				ITrackStyle combo_style = styleMap.get(comboKey);
				if (combo_style == null) {
					combo_style = new DefaultTrackStyle(comboKey, true);
					combo_style.setTrackName(comboKey);
					combo_style.setExpandable(true);
					combo_style.setCollapsed(true);
					styleMap.put(comboKey, combo_style);
				}
				if (combo_style instanceof ITrackStyleExtended) {
					gstate.setComboStyle((ITrackStyleExtended)combo_style, 0);
				}
				else {
					gstate.setComboStyle(null, 0);
				}
//				gstate.getTierStyle().setHeight(combo_style.getHeight());
				gstate.getTierStyle().setFloatTier(false); // ignored since combo_style is set
			}
			else {
				gstate.setComboStyle(null, 0);
//				gstate.getTierStyle().setHeight(combo_style.getHeight());
				gstate.getTierStyle().setFloatTier(false); // ignored since combo_style is set
			}
			symlist.add(graphIntervalSym);
		}
		return symlist;
	}

	@Override
	public void init(URI uri) {
		BufferedReader br = null;
		try {
			InputStream is;
			is = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			Thread thread = Thread.currentThread();
			boolean started = false;
			while ((!thread.isInterrupted()) && ((line = br.readLine()) != null)) {
				if (!started && !(line.startsWith("##fileformat=VCFv") || line.startsWith("##format=VCFv"))) {
					Logger.getLogger(this.getClass().getName()).log(
						Level.SEVERE, "VCF - first line must be ##fileformat or ##format");
				}
				started = true;
				if (line.startsWith("##")) {
					processMetaInformationLine(line.substring(2));
				}
				else if (line.startsWith("#")) {
					processHeaderLine(line.substring(1));
				}
				else {
					break;
				}
			}
			if (version < 0) {
				throw new UnsupportedOperationException("file version not supported");
			}
		}
		catch (IOException x) {
			Logger.getLogger(this.getClass().getName()).log(
				Level.SEVERE, "Error loading headers from vcf file", x);
		} finally {
			GeneralUtils.safeClose(br);
		}
	}

	public void setCombineGenotype(boolean combineGenotype) {
		this.combineGenotype = combineGenotype;
	}

	public void select(String name, boolean separateTracks, Map<String, List<String>> selections) {
		setCombineGenotype(!separateTracks);
		for (String dataField : new ArrayList<String>(selectedFields)) {
			if (dataField.indexOf('/') > -1) {
				selectedFields.remove(dataField);
			}
		}
		for (String type : selections.keySet()) {
			for (String sample : selections.get(type)) {
				selectedFields.add(name + "/" + type + "/" + sample);
			}
		}
	}

	public List<String> getSelectedFields() {
		return selectedFields;
	}

	@Override
	public List<String> getFormatPrefList() {
		return Arrays.asList(EXTENSIONS);
	}

	public List<String> getAllFields() {
		return new ArrayList<String>(infoMap.keySet());
	}

	public List<String> getSamples() {
		return Arrays.asList(samples);
	}

	public List<String> getGenotypes() {
		return new ArrayList<String>(formatMap.keySet());
	}

	private String getID(String line) {
		Matcher matcher = idPattern.matcher(line);
		if (matcher.find()) {
			String group = matcher.group();
			return group.substring(",ID=".length(), group.length() - 1);
		}
		else {
			return null;
		}
	}

	private int getNumber(String line) {
		int number = -1;
		String numberString = getNumberString(line);
		if (numberString != null) {
			try {
				number = Integer.parseInt(numberString);
			}
			catch (NumberFormatException x) {}
		}
		return number;
	}

	private String getNumberString(String line) {
		Matcher matcher = numberPattern.matcher(line);
		if (matcher.find()) {
			String group = matcher.group();
			return group.substring(",Number=".length(), group.length() - 1);
		}
		else {
			return null;
		}
	}

	private Type getType(String line) {
		Matcher matcher = typePattern.matcher(line);
		if (matcher.find()) {
			String group = matcher.group();
			return Type.valueOf(group.substring(",Type=".length(), group.length() - 1));
		}
		else {
			return null;
		}
	}

	private String getDescription(String line) {
		Matcher matcher = descriptionPattern.matcher(line);
		if (matcher.find()) {
			String group = matcher.group();
			return group.substring(",Description=\"".length(), group.length() - 2);
		}
		else {
			return null;
		}
	}

	private INFO getInfo(String line) {
		String dataline = "," + line.substring(1, line.length() - 1) + ",";
		int number = -1;
		boolean onePerAllele = false;
		boolean onePerGenotype = false;
		String numberString = getNumberString(line);
		if (numberString == null) {
		}
		else if ("A".equals(numberString)) {
			onePerAllele = true;
		}
		else if ("G".equals(numberString)) {
			onePerGenotype = true;
		}
		else if (".".equals(numberString)) {
		}
		else {
			number = Integer.parseInt(numberString);
		}
		return new INFO(getID(dataline), number, getType(dataline), getDescription(dataline), onePerAllele, onePerGenotype);
	}

	private FILTER getFilter(String line) {
		String dataline = "," + line.substring(1, line.length() - 1) + ",";
		return new FILTER(getID(dataline), getDescription(dataline));
	}

	private FORMAT getFormat(String line) {
		String dataline = "," + line.substring(1, line.length() - 1) + ",";
		return new FORMAT(getID(dataline), getNumber(dataline), getType(dataline), getDescription(dataline));
	}

	private void processMetaInformationLine(String line) throws IOException {
		if (line.startsWith("fileformat=")) {
			String format = line.substring("fileformat=".length());
			if (format.equals("VCFv4.0")) {
				version = 4.0;
			}
			else if (format.equals("VCFv4.1")) {
				version = 4.1;
			}
			else {
				ErrorHandler.errorPanel("file version not supported " + format);
				throw new UnsupportedOperationException("file version not supported " + format);
			}
		}
		else if (line.startsWith("format=")) {
			String format = line.substring("format=".length());
			ErrorHandler.errorPanel("file version not supported " + format);
			throw new UnsupportedOperationException("file version not supported " + format);
		}
//		else if (line.startsWith("fileDate=")) {
//			try {
//				date = DATE_FORMAT.parse(line.substring("fileDate=".length()));
//			}
//			catch (ParseException x) {
//				Logger.getLogger(this.getClass().getName()).log(
//					Level.WARNING, "Unable to process date " + line.substring("fileDate=".length()));
//			}
//		}
		else if (line.startsWith("INFO=")) {
			INFO info = getInfo(line.substring("INFO=".length()));
			infoMap.put(info.getID(), info);
		}
		else if (line.startsWith("FILTER=")) {
			FILTER filter = getFilter(line.substring("FILTER=".length()));
			filterMap.put(filter.getID(), filter);
		}
		else if (line.startsWith("FORMAT=")) {
			FORMAT format = getFormat(line.substring("FORMAT=".length()));
			formatMap.put(format.getID(), format);
		}
		else {
			int pos = line.indexOf('=');
			metaMap.put(line.substring(0, pos), line.substring(pos + 1));
		}
	}

	private void processHeaderLine(String line) {
		String[] fields = line_regex.split(line);
		if (fields.length > 8) {
			samples = Arrays.copyOfRange(fields, 9, fields.length);
		}
		else {
			samples = new String[]{};
		}
	}

	private SimpleSymWithProps getContainerSymFromMap(Map<String, SimpleSymWithProps> symMap, String key, BioSeq seq) {
		SimpleSymWithProps container = symMap.get(key);
		if (container == null) {
			container = new SimpleSymWithProps();
			container.setProperty("seq", seq);
			container.setProperty("type", key);
			container.setProperty("id", key);
			container.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
			symMap.put(key, container);
		}
		return container;
	}

	private String getMultiple(char c, int count) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	private BAMSym getBAMSym(String nameType, BioSeq seq, String id, int start, int end, int width, String qualString, String filter, String ref, String alt) {
		Cigar cigar = null;
// Cigar cigar = TextCigarCodec.getSingleton().decode(cigarString); 
		boolean equal = false;
		boolean equalLength = false;
		boolean insertion = false;
		boolean deletion = false;
		if (ref.equals(alt)) {
			equal = true;
		}
		else if (ref.length() == alt.length()) {
			cigar = new Cigar();
			CigarElement cigarElement = new CigarElement(width, CigarOperator.M);
			cigar.add(cigarElement);
			equalLength = true;
		}
		else if (ref.length() == 1) {
			cigar = new Cigar();
			CigarElement cigarElement = new CigarElement(1, CigarOperator.M);
			cigar.add(cigarElement);
			cigarElement = new CigarElement(alt.length() - 1, CigarOperator.I);
			cigar.add(cigarElement);
			insertion = true;
		}
		else if (alt.length() == 1) {
			cigar = new Cigar();
			CigarElement cigarElement = new CigarElement(1, CigarOperator.M);
			cigar.add(cigarElement);
			cigarElement = new CigarElement(ref.length() - 1, CigarOperator.D);
			cigar.add(cigarElement);
			deletion = true;
		}
		int[] iblockMins = insertion ? new int[]{start + 1} : new int[]{};
		int[] iblockMaxs = insertion ? new int[]{start + alt.length()} : new int[]{};
		String residuesStr = "";
		if (equal || equalLength) {
			residuesStr = alt;
		}
		else if (insertion) {
			residuesStr = ref;
		}
		else if (deletion) {
			String repeated = getMultiple('_', ref.length() - 1);
			residuesStr = alt + repeated;
		}
		BAMSym residueSym = new BAMSym(nameType, seq, start, end, id, (float)0.0, true, 0, 0, new int[]{start}, new int[]{end}, iblockMins, iblockMaxs, cigar, residuesStr);
		if (cigar != null ) {
			residueSym.setProperty(BAM.CIGARPROP, cigar);
		}
		residueSym.setInsResidues(insertion ? alt.substring(1) : "");
		residueSym.setProperty(BAM.SHOWMASK, true);
		residueSym.setProperty("type", nameType);
		residueSym.setProperty("seq", seq.getID());
		residueSym.setProperty("pos", start);
		residueSym.setProperty("id", id);
		residueSym.setProperty("ref", ref);
		residueSym.setProperty("alt", alt);
		if (!NO_DATA.equals(qualString)) {
			residueSym.setProperty("qual", Float.parseFloat(qualString));
		}
		if (!"PASS".equals(filter) && filterMap.get(filter) != null) {
			filter += " - " + filterMap.get(filter).getDescription();
		}
		residueSym.setProperty("filter", filter);
		return residueSym;
	}

	private void processInfo(String key, String valuesString, BioSeq seq, String nameType, int start, int end, int width,
			Map<String, SimpleSymWithProps> dataMap, Map<String, GraphData> graphDataMap) {
		String[] values = valuesString.split(",");
		if (infoMap.get(key).getType() == Type.Integer || infoMap.get(key).getType() == Type.Float) {
			for (String value : values) {
				addGraphData(graphDataMap, nameType + "/" + key, seq, start, width, Float.parseFloat(value));
			}
		}
		else {
			SimpleSymWithProps container = getContainerSymFromMap(dataMap, nameType + "/" + key, seq);
			for (String value : values) {
				SimpleSymWithProps sym = new SimpleSymWithProps();
				sym.addSpan(new SimpleSeqSpan(start, end, (BioSeq)container.getProperty("seq")));
				sym.setProperty(key, value);
				container.addChild(sym);
			}
		}
	}

	private void processSamples(BioSeq seq, String nameType, int start, int end, int width,
		String[] fields, Map<String, SimpleSymWithProps> genotypeDataMap,
		Map<String, GraphData> graphDataMap, boolean combineGenotype,
		BAMSym refSym, BAMSym[] altSyms, int line_count) {
		if (samples.length > 0) {
			if (fields.length < samples.length + 9) {
				Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING, "vcf line " + line_count + " has " + (fields.length - 9) + " genotype records, but header has " + samples.length);
			}
			else if (fields.length > samples.length + 9) {
				throw new IllegalStateException("vcf format error, line " + line_count + " has " + (fields.length - 9) + " genotype records, but header has " + samples.length);
			}
		}
		if (fields.length > 8) {
			String[] format = fields[8].split(":");
			// format[0] must be "GT"
			if (!"GT".equals(format[0])) {
				throw new IllegalStateException("vcf format error, line " + line_count + " first genotype field must be \"GT\"");
			}
			for (int j = 9; j < fields.length; j++) {
				String sample;
				if (j - 9 >= samples.length || samples[j - 9].trim().length() == 0) {
					sample = "sample #" + (j - 8); // start with sample1, not sample0
				}
				else {
					sample = samples[j - 9];
				}
				String[] data = fields[j].split(":");
				if (format.length < data.length) {
					throw new IllegalStateException("vcf format error, line " + line_count + " has " + data.length + "genotype fields, but definition has " + format.length);
				}
				for (int k = 0; k < format.length; k++) {
					String type = format[k];
					String fullKey = nameType + "/" + type + "/" + sample;
					String dataKey = nameType + "/" + type + (combineGenotype ? "" : ("/" + sample));
					if (k < data.length && selectedFields.contains(fullKey)) {
						if (!NO_DATA.equals(data[k])) {
							if (formatMap.get(format[k]) != null && (formatMap.get(format[k]).getType() == Type.Integer || formatMap.get(format[k]).getType() == Type.Float)) {
								for (String datum : data[k].split(",")) {
									if (!NO_DATA.equals(datum)) {
										addGraphData(graphDataMap, fullKey, seq, start, width, Float.parseFloat(datum));
									}
								}
							}
							else {
								if ("GT".equals(format[k])) {
									String[] genotypes = data[k].split("[|/]");
									for (int l = 0; l < genotypes.length; l++) {
										if (!NO_DATA.equals(genotypes[l])) {
											SimpleSymWithProps container = getContainerSymFromMap(genotypeDataMap, dataKey, seq);
											int index = Integer.parseInt(genotypes[l]);
											if (index == 0) {
												container.addChild(refSym);
											}
											else {
												container.addChild(altSyms[index - 1]);
											}
										}
									}
								}
								else {
									SimpleSymWithProps container = getContainerSymFromMap(genotypeDataMap, dataKey, seq);
									for (String datum : data[k].split(",")) {
										if (!NO_DATA.equals(datum)) {
											SimpleSymWithProps sym = new SimpleSymWithProps();
											sym.addSpan(new SimpleSeqSpan(start, end, (BioSeq)container.getProperty("seq")));
											container.addChild(sym);
											sym.setProperty(format[k], datum);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void processDataLine(SimpleSymWithProps mainSym, BioSeq seq, int min, int max, String nameType,
			Map<String, SimpleSymWithProps> dataMap, Map<String, GraphData> graphDataMap,
			Map<String, SimpleSymWithProps> genotypeDataMap,
			String line, int line_count, boolean combineGenotype) {
		String[] fields = line_regex.split(line);
		int start = Integer.parseInt(fields[1]) - 1; // vcf is one based, but IGB is zero based
		if (max <= start) {
			return;
		}
		String ref = fields[3];
		int width = ref.length();
		int end = start + width;
		if (min >= end) {
			return;
		}
		String id = fields[2];
		String[] alts = fields[4].split(",");
		String qualString = fields[5];
		String filter = fields[6];
		if (!NO_DATA.equals(qualString) && selectedFields.contains("qual")) {
			addGraphData(graphDataMap, nameType + "/qual", seq, start, width, Float.parseFloat(qualString));
		}
		BAMSym[] altSyms = new BAMSym[alts.length];
		for (int i = 0; i < alts.length; i++) {
			String alt = alts[i];
			altSyms[i] = getBAMSym(nameType, seq, id, start, end, width, qualString, filter, ref, alt);
			mainSym.addChild(altSyms[i]);
			String[] info_fields = info_regex.split(fields[7]);
			for (String info_field : info_fields) {
				String[] prop_fields = info_field.split("=");
				String key = prop_fields[0];
				String valuesString = (prop_fields.length == 1) ? "true" : prop_fields[1];
				String fullKey = key;
				if (infoMap.get(key) != null && infoMap.get(key).getDescription() != null) {
					fullKey += " - " + infoMap.get(key).getDescription();
				}
				altSyms[i].setProperty(fullKey, valuesString);
				if (selectedFields.contains(key)) {
					processInfo(key, valuesString, seq, nameType, start, end, width, dataMap, graphDataMap);
				}
			}
		}
		BAMSym refSym = getBAMSym(nameType, seq, id, start, end, width, qualString, filter, ref, ref);
		processSamples(seq, nameType, start, end, width, fields, genotypeDataMap, graphDataMap,
			combineGenotype, refSym, altSyms, line_count);
	}

	private class GraphData {
		IntArrayList xData = new IntArrayList();
		FloatArrayList yData = new FloatArrayList();
		IntArrayList wData = new IntArrayList();
	}

	private void addGraphData(Map<String, GraphData> graphDataMap, String key, BioSeq seq, int pos, int width, float value) {
		GraphData graphData = graphDataMap.get(key);
		if (graphData == null) {
			graphData = new GraphData();
			graphDataMap.put(key, graphData);
		}
		graphData.xData.add(pos);
		graphData.yData.add(value);
		graphData.wData.add(width);
	}

	@Override
	public SeqSpan getSpan(String line) {
		String[] fields = line_regex.split(line);
		String seq_name = fields[0];
		int start = Integer.parseInt(fields[1]) - 1; // vcf is one based, but IGB is zero based
		String ref = fields[3];
		int width = ref.length();
		int end = start + width;
		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeqGroup().getSeq(seq_name);
		if (seq == null) {
			seq = new BioSeq(seq_name, "", 0);
		}
		return new SimpleSeqSpan(start, end, seq);
	}

	@Override
	public boolean processInfoLine(String line, List<String> infoLines) {
		if (line.startsWith("#")) {
			return true; // handled in init()
		}
		return false;
	}

	@Override
	protected LineProcessor createLineProcessor(String featureName) {
		return this;
	}
}
