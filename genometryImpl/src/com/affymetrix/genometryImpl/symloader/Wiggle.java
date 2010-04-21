/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.affymetrix.genometryImpl.symloader;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GraphIntervalSym;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.general.SymLoader;
import com.affymetrix.genometryImpl.parsers.TrackLineParser;
import com.affymetrix.genometryImpl.parsers.graph.BarParser;
import com.affymetrix.genometryImpl.parsers.graph.WiggleData;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author hiralv
 */
public final class Wiggle extends SymLoader{
	
	private static enum WigFormat {

		BED4, VARSTEP, FIXEDSTEP
	};

	private static final Pattern field_regex = Pattern.compile("\\s+");  // one or more whitespace
	private static final boolean ensure_unique_id = true;
	private final TrackLineParser track_line_parser;

	private final File f;
	private final AnnotatedSeqGroup group;
	private final String featureName;

	public Wiggle(URI uri, String featureName, AnnotatedSeqGroup seq_group) {
		super(uri);
		this.f = LocalUrlCacher.convertURIToFile(uri);
		this.group = seq_group;
		this.featureName = featureName;
		track_line_parser = new TrackLineParser();
	}

	@Override
	public List<BioSeq> getChromosomeList() {
		List<BioSeq> seqs = new ArrayList<BioSeq>();
		List<GraphSym> allSyms = getGenome();
		for(GraphSym sym : allSyms){
			if(!seqs.contains(sym.getGraphSeq()))
				seqs.add(sym.getGraphSeq());
		}
		allSyms = null;
		return seqs;
	}

	@Override
	public List<GraphSym> getGenome() {
		return parse(null,-1,-1);
	}

	@Override
	public List<GraphSym> getChromosome(BioSeq seq) {
		return parse(seq,seq.getMin(),seq.getMax());
	}


	@Override
	public List<GraphSym> getRegion(SeqSpan span) {
		return parse(span.getBioSeq(),span.getMin(),span.getMax());
	}

		
	/**
	 *  Reads a Wiggle-formatted file using any combination of the three formats
	 *  BED4,VARSTEP,FIXEDSTEP.
	 *  The format must be specified on the first line following a track line,
	 *  otherwise BED4 is assumed.
	 */
	public List<GraphSym> parse(BioSeq reqSeq, int min, int max){
		FileInputStream fis = null;
		InputStream istr = null;
		
		WigFormat current_format = WigFormat.BED4;

		List<GraphSym> grafs = new ArrayList<GraphSym>();
		WiggleData current_data = null;
		Map<String, WiggleData> current_datamap = null; // Map: seq_id -> WiggleData
		boolean previous_track_line = false;

		String line;

		// these may be used by fixedStep or variableStep
		String current_seq_id = null;
		int current_start=0;
		int current_step=0;
		int current_span=0;

		BufferedReader br = null;
		try {
			fis = new FileInputStream(this.f);
			istr = GeneralUtils.unzipStream(fis, featureName, new StringBuffer());
			br = new BufferedReader(new InputStreamReader(istr));


			while ((line = br.readLine()) != null && !Thread.currentThread().isInterrupted()) {
				if (line.length() == 0) {
					continue;
				}
				if (line.charAt(0) == '#' || line.charAt(0) == '%' || line.startsWith("browser")) {
					continue;
				}

				if (line.startsWith("track")) {
					if (previous_track_line) {
						// finish previous graph(s) using previous track properties
						grafs.addAll(createGraphSyms(track_line_parser.getCurrentTrackHash(), group, current_datamap, featureName));
					}

					track_line_parser.parseTrackLine(line);
					previous_track_line = true;

					current_format = WigFormat.BED4; // assume BED4 until changed.
					current_data = null;
					current_datamap = new HashMap<String, WiggleData>();
					continue;
				}

				if (line.startsWith("variableStep")) {
					if (!previous_track_line) {
						throw new IllegalArgumentException("Wiggle format error: 'variableStep' line does not have a previous 'track' line");
					}

					current_format = WigFormat.VARSTEP;
					current_seq_id = Wiggle.parseFormatLine(line, "chrom", "unknown");
					current_span = Integer.parseInt(Wiggle.parseFormatLine(line, "span", "1"));
					continue;
				}

				if (line.startsWith("fixedStep")) {
					if (!previous_track_line) {
						throw new IllegalArgumentException("Wiggle format error: 'fixedStep' line does not have a previous 'track' line");
					}

					current_format = WigFormat.FIXEDSTEP;
					current_seq_id = Wiggle.parseFormatLine(line, "chrom", "unknown");
					current_start = Integer.parseInt(Wiggle.parseFormatLine(line, "start", "1"));
					if (current_start < 1) {
						throw new IllegalArgumentException("'fixedStep' format with start of " + current_start + ".");
					}
					current_step = Integer.parseInt(Wiggle.parseFormatLine(line, "step", "1"));
					current_span = Integer.parseInt(Wiggle.parseFormatLine(line, "span", "1"));
					continue;
				}

				current_start = parseData(
						previous_track_line, line, current_format, current_data, current_datamap, current_seq_id, current_span, current_start, current_step, reqSeq, min, max);
			}
		} catch (Exception ex) {
			Logger.getLogger(Wiggle.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			GeneralUtils.safeClose(istr);
			GeneralUtils.safeClose(br);
			GeneralUtils.safeClose(fis);
		}

		grafs.addAll(createGraphSyms(
				track_line_parser.getCurrentTrackHash(), group, current_datamap, featureName));
		
		return grafs;
	}

	private static int parseData(boolean previous_track_line, String line, WigFormat current_format,
			WiggleData current_data, Map<String, WiggleData> current_datamap, String current_seq_id,
			int current_span, int current_start, int current_step, BioSeq reqSeq, int min, int max)
			throws IllegalArgumentException {
		// There should have been one track line at least...
		if (!previous_track_line) {
			throw new IllegalArgumentException("Wiggle format error: File does not have a previous 'track' line");
		}
		String[] fields = field_regex.split(line.trim()); // trim() because lines are allowed to start with whitespace


		if(reqSeq != null){
			String seq_id = null;
			if(current_format.equals(WigFormat.BED4))
				seq_id = fields[0];
			else
				seq_id = current_seq_id;

			if(!seq_id.equals(reqSeq.getID())){
				return current_start;
			}
		}

		switch(current_format) {
			case BED4:
				if (fields.length < 4) {
					throw new IllegalArgumentException("Wiggle format error: Improper " + current_format + " line: " + line);
				}
				parseDataLine(fields, current_data, current_datamap, min, max);
				break;
			case VARSTEP:
				if (fields.length < 2) {
					throw new IllegalArgumentException("Wiggle format error: Improper " + current_format + " line: " + line);
				}
				parseDataLine(fields, current_data, current_datamap, current_seq_id, current_span, min, max);
				break;
			case FIXEDSTEP:
				if (fields.length < 1) {
					throw new IllegalArgumentException("Wiggle format error: Improper " + current_format + " line: " + line);
				}
				parseDataLine(fields, current_data, current_datamap, current_seq_id, current_span, current_start, min, max);
				current_start += current_step; // We advance the start based upon the step.
				break;
		}
		return current_start;
	}


	/**
	 * Parse a single line of data (BED4 format).
	 * @param fields
	 * @param current_data
	 * @param current_datamap
	 */
	private static void parseDataLine(
					String[] fields,
					WiggleData current_data,
					Map<String, WiggleData> current_datamap, int min, int max) {

		// chrom  start end value
		String seq_id = fields[0];	// chrom

		current_data = current_datamap.get(seq_id);
		if (current_data == null) {
			current_data = new WiggleData(seq_id);
			current_datamap.put(seq_id, current_data);
		}

		int x1 = Integer.parseInt(fields[1]);	// start, or perhaps end
		int x2 = Integer.parseInt(fields[2]);	// start, or perhaps end
		int start = Math.min(x1, x2);
		int width = Math.max(x1, x2) - start;

		if(!checkRange(x1,width,min,max))
			return;

		current_data.add(x1, Float.parseFloat(fields[3]), width);
	}


	/**
	 * Parse a single line of data (variableStep format).
	 * @param fields
	 * @param current_data
	 * @param current_datamap
	 * @param current_seq_id
	 * @param current_span
	 */
	private static void parseDataLine(
					String[] fields,
					WiggleData current_data,
					Map<String, WiggleData> current_datamap,
					String current_seq_id,
					int current_span, int min, int max) {

		current_data = current_datamap.get(current_seq_id);
		if (current_data == null) {
			current_data = new WiggleData(current_seq_id);
			current_datamap.put(current_seq_id, current_data);
		}

		int current_start = Integer.parseInt(fields[0]);
		if (current_start < 1) {
			throw new IllegalArgumentException("'variableStep' format with start of " + current_start +".");
		}
		current_start -=1;	// This is because fixedStep and variableStep sequences are 1-indexed.  See http://genome.ucsc.edu/goldenPath/help/wiggle.html

		if(!checkRange(current_start,current_span,min,max))
			return;
		
		current_data.add(current_start, Float.parseFloat(fields[1]), current_span);

	}

	/**
	 * Parse a single line of data (fixedStep format).
	 * @param fields
	 * @param current_data
	 * @param current_datamap
	 * @param current_seq_id
	 * @param current_span
	 * @param current_start
	 */
	private static void parseDataLine(
					String[] fields,
					WiggleData current_data,
					Map<String, WiggleData> current_datamap,
					String current_seq_id,
					int current_span,
					int current_start, int min, int max) {

		current_data = current_datamap.get(current_seq_id);
		if (current_data == null) {
			current_data = new WiggleData(current_seq_id);
			current_datamap.put(current_seq_id, current_data);
		}

		current_start -=1;	// This is because fixedStep and variableStep formats are 1-indexed.  See http://genome.ucsc.edu/goldenPath/help/wiggle.html

		if(!checkRange(current_start,current_span,min,max))
			return;
		
		current_data.add(current_start, Float.parseFloat(fields[0]), current_span);
	}

	private static boolean checkRange(int start, int width, int min, int max){
		//getGenome && getChromosomeList
		if(min == max && min == -1){
			return true;
		}

		//getChromosome && getRegion
		if(start+width < min || start > max){
			return false;
		}

		return true;
	}

	/**
	 * Parse the line, looking for the field name.  If it can't be found, return the default value.
	 * @param name
	 * @param line
	 * @param default_val
	 * @return string
	 */
	private static String parseFormatLine(String line, String name, String default_val) {
		String[] fields = field_regex.split(line);
		String fieldName = name +"=";
		for (String field : fields) {
			if (field.startsWith(fieldName)) {
				return field.substring(name.length() + 1);
			}
		}
		return default_val;
	}

	/**
	 * Finishes the current data section and creates a list of GraphSym objects.
	 */
	private static List<GraphSym> createGraphSyms(Map<String,String> track_hash, AnnotatedSeqGroup seq_group, Map<String, WiggleData> current_datamap, String stream_name) {
		if (current_datamap == null) {
			return Collections.<GraphSym>emptyList();
		}

		List<GraphSym> grafs = new ArrayList<GraphSym>(current_datamap.size());

		String graph_id = track_hash.get(TrackLineParser.NAME);
		if (graph_id == null) {
			graph_id = stream_name;
		}
		if (ensure_unique_id) {
			graph_id = AnnotatedSeqGroup.getUniqueGraphID(graph_id, seq_group);
		}
		track_hash.put(TrackLineParser.NAME, graph_id);

		GraphState gstate = AnnotatedSeqGroup.getStateProvider().getGraphState(graph_id);
		TrackLineParser.applyTrackProperties(track_hash, gstate);

		// Need iterator because we're removing data on the fly
		Iterator<WiggleData> wiggleDataIterator = current_datamap.values().iterator();
		while (wiggleDataIterator.hasNext()) {
			GraphSym gsym = wiggleDataIterator.next().createGraph(seq_group, graph_id);

			if (gsym != null) {
				grafs.add(gsym);
			}
			wiggleDataIterator.remove();	// free up memory now that we've created the graph.
		}

		return grafs;
	}

	private static void writeGraphPoints(GraphIntervalSym graf, BufferedWriter bw, String seq_id) throws IOException {
		int total_points = graf.getPointCount();
		for (int i = 0; i < total_points; i++) {
			int x2 = graf.getGraphXCoord(i) + graf.getGraphWidthCoord(i);
			bw.write(seq_id + ' ' + graf.getGraphXCoord(i) + ' ' + x2
					+ ' ' + graf.getGraphYCoord(i) + '\n');
		}
	}
	
	/** Writes the given GraphIntervalSym in wiggle-BED format.
	 *  Also writes a track line as a header.
	 */
	public static void writeBedFormat(GraphIntervalSym graf, String genome_version, OutputStream outstream) throws IOException {
		BioSeq seq = graf.getGraphSeq();
		String seq_id = (seq == null ? "." : seq.getID());
		String human_name = graf.getGraphState().getTierStyle().getHumanName();
		String gname = graf.getGraphName();
		GraphState state = graf.getGraphState();
		Color color = state.getTierStyle().getColor();

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(outstream));
			if (genome_version != null && genome_version.length() > 0) {
				bw.write("# genome_version = " + genome_version + '\n');
			}
			bw.write("track type=wiggle_0 name=\"" + gname + "\"");
			bw.write(" description=\"" + human_name + "\"");
			bw.write(" visibility=full");
			bw.write(" color=" + color.getRed() + "," + color.getGreen() + "," + color.getBlue());
			bw.write(" viewLimits=" + Float.toString(state.getVisibleMinY()) + ":" + Float.toString(state.getVisibleMaxY()));
			bw.write("");
			bw.write('\n');
			writeGraphPoints(graf, bw, seq_id);
			bw.flush();
		} finally {
			GeneralUtils.safeClose(bw);
		}
	}

	/**
	 * Writes the give GraphSym in bar format.
	 * @param syms		GraphSym to be written.
	 * @param seq		BioSeq to be written.
	 * @param type		Type required for bar format.
	 * @param ostr		Outputstream to write bar file.
	 * @return			Returns true if bar file is written sucessfully.
	 */
	public static boolean writeBarFormat(Collection<? extends SeqSymmetry> syms, String type, OutputStream ostr){
			
			BarParser instance = new BarParser();
			return instance.writeAnnotations(syms, null, type, ostr);
	}

	public String getMimeType() {
		return "text/wig";
	}

}
