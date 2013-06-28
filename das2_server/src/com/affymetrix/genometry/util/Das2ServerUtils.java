package com.affymetrix.genometry.util;

import com.affymetrix.genometryImpl.AnnotSecurity;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.das2.SimpleDas2Type;
import com.affymetrix.genometryImpl.parsers.AnnotsXmlParser;
import com.affymetrix.genometryImpl.parsers.AnnotsXmlParser.AnnotMapElt;
import com.affymetrix.genometryImpl.parsers.IndexWriter;
import com.affymetrix.genometryImpl.parsers.ProbeSetDisplayPlugin;
import com.affymetrix.genometryImpl.parsers.useq.USeqUtilities;
import com.affymetrix.genometryImpl.symloader.BAM;
import com.affymetrix.genometryImpl.symloader.SymLoader;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithProps;
import com.affymetrix.genometryImpl.util.DasServerUtils;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.HiddenFileFilter;
import com.affymetrix.genometryImpl.util.IndexingUtils;
import com.affymetrix.genometryImpl.util.IndexingUtils.IndexedSyms;
import com.affymetrix.genometryImpl.util.ParserController;
import com.affymetrix.genometryImpl.util.ServerUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author hiralv
 */
public class Das2ServerUtils {
	private static final String annots_filename = "annots.xml"; // potential originalFile for annots parsing
	private static final String graph_dir_suffix = ".graphs.seqs";
	
	/**
	 * if ".seqs" suffix, then handle as graphs
	 * otherwise recursively call on each child files;
	 * @param type_name
	 * @param genome
	 * @param current_file
	 * @param new_type_prefix
	 * @param graph_name2dir
	 * @param graph_name2file
	 * @param dataRoot
	 */
	static void loadAnnotsFromDir(String type_name, AnnotatedSeqGroup genome, File current_file, String new_type_prefix, Map<AnnotatedSeqGroup, List<AnnotMapElt>> annots_map, Map<String, String> graph_name2dir, Map<String, String> graph_name2file, String dataRoot) throws IOException {
		File annot = new File(current_file, annots_filename);
		if (annot.exists()) {
			FileInputStream istr = null;
			FileInputStream validationIstr = null;
			try {
				istr = new FileInputStream(annot);
				validationIstr = new FileInputStream(annot);
				List<AnnotMapElt> annotList = annots_map.get(genome);
				if (annotList == null) {
					annotList = new ArrayList<AnnotMapElt>();
					annots_map.put(genome, annotList);
				}
				AnnotsXmlParser.parseAnnotsXml(istr, validationIstr, annotList);
			} catch (FileNotFoundException ex) {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.SEVERE, null, ex);
			} catch (JDOMException ex) {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.SEVERE, null, ex);
			} catch (SAXException ex) {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				GeneralUtils.safeClose(istr);
			}
		}
		if (type_name.endsWith(graph_dir_suffix)) {
			String graph_name = type_name.substring(0, type_name.length() - graph_dir_suffix.length());
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.FINE, "@@@ adding graph directory to types: {0}, path: {1}", new Object[]{graph_name, current_file.getPath()});
			graph_name2dir.put(graph_name, current_file.getPath());
			genome.addType(graph_name, null);
		} else {
			File[] child_files = current_file.listFiles(new HiddenFileFilter());
			Arrays.sort(child_files);
			for (File child_file : child_files) {
				loadAnnotsFromFile(child_file, genome, new_type_prefix, annots_map, graph_name2dir, graph_name2file, dataRoot);
			}
		}
	}


	/**
	 * Load annotations from root of genome directory.
	 * @param genomeDir
	 * @param genome
	 * @param graph_name2dir
	 * @param graph_name2file
	 * @param dataRoot
	 */
	public static void loadAnnots(File genomeDir, AnnotatedSeqGroup genome, Map<AnnotatedSeqGroup, List<AnnotMapElt>> annots_map, Map<String, String> graph_name2dir, Map<String, String> graph_name2file, String dataRoot) throws IOException {
		if (genomeDir.isDirectory()) {
			loadAnnotsFromDir(genomeDir.getName(), genome, genomeDir, "", annots_map, graph_name2dir, graph_name2file, dataRoot);
		} else {
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING, "" + "{0} is not a directory.  Skipping.", genomeDir.getAbsolutePath());
		}
	}



	/**
	 * see if can parse as annotation originalFile.
	 * @param current_file
	 * @param genome
	 * @param type_prefix
	 * @param graph_name2dir
	 * @param graph_name2file
	 * @param dataRoot
	 */
	static void loadAnnotsFromFile(File current_file, AnnotatedSeqGroup genome, String type_prefix, Map<AnnotatedSeqGroup, List<AnnotMapElt>> annots_map, Map<String, String> graph_name2dir, Map<String, String> graph_name2file, String dataRoot) throws IOException {
		String file_name = current_file.getName();
		String extension = GeneralUtils.getExtension(GeneralUtils.getUnzippedName(current_file.getName()));
		if (extension != null && extension.length() > 0) {
			file_name = file_name.substring(0, file_name.lastIndexOf(extension));
			extension = extension.substring(extension.indexOf('.') + 1);
		}
		String type_name = type_prefix + file_name;
		if (current_file.isDirectory()) {
			String new_type_prefix = type_name + "/";
			loadAnnotsFromDir(type_name, genome, current_file, new_type_prefix, annots_map, graph_name2dir, graph_name2file, dataRoot);
			return;
		}
		if (isSequenceFile(current_file) || isGraph(current_file, type_name, graph_name2file, genome) || isAnnotsFile(current_file)) {
			return;
		}
		if (isSymLoader(extension)) {
			List<AnnotMapElt> annotList = annots_map.get(genome);
			String annotTypeName = ParserController.getAnnotType(annotList, current_file.getName(), extension, type_name);
			genome.addType(annotTypeName, null);
			SymLoader symloader = ServerUtils.determineLoader(extension, current_file.toURI(), type_name, genome);
			genome.addSymLoader(annotTypeName, symloader);
			return;
		}
		if (!annots_map.isEmpty() && annots_map.containsKey(genome)) {
			if (AnnotMapElt.findFileNameElt(file_name, annots_map.get(genome)) == null) {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.INFO, "Ignoring file {0} which was not found in annots.xml", file_name);
				return;
			}
		}
		indexOrLoadFile(dataRoot, current_file, type_name, extension, annots_map, genome, null);
	}

	private static boolean isGraph(File current_file, String type_name, Map<String, String> graph_name2file, AnnotatedSeqGroup genome) {
		String file_name = current_file.getName();
		if (file_name.endsWith(".bar") || USeqUtilities.USEQ_ARCHIVE.matcher(file_name).matches()) {
			String file_path = current_file.getPath();
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.FINE, "@@@ adding graph file to types: {0}, path: {1}", new Object[]{type_name, file_path});
			graph_name2file.put(type_name, file_path);
			genome.addType(type_name, null);
			return true;
		}
		return false;
	}
	
	private static boolean isSymLoader(String extension) {
		return extension.endsWith("bam") || isResidueFile(extension);
	}
		
	private static boolean isResidueFile(String format) {
		return format.equalsIgnoreCase("bnib") || format.equalsIgnoreCase("fa") || format.equalsIgnoreCase("2bit");
	}
		
	private static boolean isSequenceFile(File current_file) {
		return current_file.getName().equals("mod_chromInfo.txt") || current_file.getName().equals("liftAll.lft");
	}
	
	private static boolean isAnnotsFile(File current_file) {
		return current_file.getName().equals("annots.xml");
	}

	/**
	 *   If current_file is directory:
	 *       if ".seqs" suffix, then handle as graphs
	 *       otherwise recursively call on each child files;
	 *   if not directory, see if can parse as annotation file.
	 *   if type prefix is null, then at top level of genome directory, so make type_prefix = "" when recursing down
	 */
	public static void loadGenoPubAnnotsFromFile(String dataroot, File current_file, AnnotatedSeqGroup genome, Map<AnnotatedSeqGroup, List<AnnotMapElt>> annots_map, String type_prefix, Integer annot_id, Map<String, String> graph_name2file) throws FileNotFoundException, IOException {
		if (isGenoPubSequenceFile(current_file) || isGenoPubGraph(current_file, type_prefix, graph_name2file, genome, annot_id)) {
			return;
		}
		String currentFileName = current_file.getName();
		if (currentFileName.endsWith("bam")) {
			String type_name = type_prefix;
			List<AnnotMapElt> annotList = annots_map.get(genome);
			String annotTypeName = ParserController.getAnnotType(annotList, currentFileName, "bam", type_name);
			genome.addType(annotTypeName, annot_id);
			SymLoader symloader = ServerUtils.determineLoader("bam", current_file.toURI(), type_name, genome);
			genome.addSymLoader(annotTypeName, symloader);
			return;
		}
		String extension = GeneralUtils.getExtension(GeneralUtils.getUnzippedName(current_file.getName()));
		indexOrLoadFile(dataroot, current_file, type_prefix, extension, annots_map, genome, annot_id);
	}

	/**
	 * Index the file, if possible or load the file.
	 * @param dataRoot -- root of data directory
	 * @param file -- file to load or index
	 * @param annot_name
	 * @param annots_map
	 * @param genome
	 * @param annot_id
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	private static void indexOrLoadFile(String dataRoot, File file, String annot_name, String extension, Map<AnnotatedSeqGroup, List<AnnotMapElt>> annots_map, AnnotatedSeqGroup genome, Integer annot_id) throws FileNotFoundException, IOException {
		String stream_name = GeneralUtils.getUnzippedName(file.getName());
		IndexWriter iWriter = ParserController.getIndexWriter(stream_name);
		List<AnnotMapElt> annotList = annots_map.get(genome);
		String annotTypeName = ParserController.getAnnotType(annotList, file.getName(), extension, annot_name);
		AnnotatedSeqGroup tempGenome = AnnotatedSeqGroup.tempGenome(genome);
		if (iWriter == null) {
			loadAnnotFile(file, annotTypeName, annotList, genome, false);
			getAddedChroms(genome, tempGenome, false);
			getAlteredChroms(genome, tempGenome, false);
			// Not yet indexable
			return;
		}
		List<? extends SeqSymmetry> loadedSyms = loadAnnotFile(file, annotTypeName, annotList, tempGenome, true);
		getAddedChroms(tempGenome, genome, true);
		getAlteredChroms(tempGenome, genome, true);
		String returnTypeName = annotTypeName;
		if (stream_name.endsWith(".link.psl")) {
			returnTypeName = annotTypeName + " " + ProbeSetDisplayPlugin.CONSENSUS_TYPE;
		}
		genome.addType(returnTypeName, annot_id);
		
		ServerUtils.createDirIfNecessary(IndexingUtils.indexedGenomeDirName(dataRoot, genome));
		
		IndexingUtils.determineIndexes(genome, tempGenome, dataRoot, file, loadedSyms, iWriter, annotTypeName, returnTypeName, extension);
	}
	
	private static boolean isGenoPubSequenceFile(File current_file) {
		return (current_file.getName().equals("mod_chromInfo.txt") || current_file.getName().equals("liftAll.lft"));
	}

	private static boolean isGenoPubGraph(File current_file, String type_prefix, Map<String, String> graph_name2file, AnnotatedSeqGroup genome, Integer annot_id) {
		String file_name = current_file.getName();
		if (file_name.endsWith(".bar") || USeqUtilities.USEQ_ARCHIVE.matcher(file_name).matches()) {
			String file_path = current_file.getPath();
			// special casing so bar files are seen in types request, but not parsed in on startup
			//    (because using graph slicing so don't have to pull all bar file graphs into memory)
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.FINE,
					"@@@ adding graph file to types: {0}, path: {1}", new Object[]{type_prefix, file_path});
			graph_name2file.put(type_prefix, file_path);
			genome.addType(type_prefix, annot_id);
			return true;
		}
		return false;
	}

	/**
	 * Load an annotations file (indexed or non-indexed).
	 * @return the symmetries found.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<? extends SeqSymmetry> loadAnnotFile(File current_file, String type_name, List<AnnotMapElt> annotList, AnnotatedSeqGroup genome, boolean isIndexed) throws FileNotFoundException, IOException {
		String stream_name = GeneralUtils.getUnzippedName(current_file.getName());
		InputStream istr = null;
		try {
			istr = GeneralUtils.getInputStream(current_file, new StringBuffer());
			if (!isIndexed) {
				return ParserController.parse(istr, annotList, stream_name, genome, type_name);
			}
			return ParserController.parseIndexed(istr, annotList, stream_name, genome, type_name);
		} finally {
			GeneralUtils.safeClose(istr);
		}
	}

	private static void getAddedChroms(AnnotatedSeqGroup newGenome, AnnotatedSeqGroup oldGenome, boolean isIgnored) {
		if (oldGenome.getSeqCount() == newGenome.getSeqCount()) {
			return;
		}

		Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
				"found {0} chromosomes instead of {1}",
				new Object[]{newGenome.getSeqCount(), oldGenome.getSeqCount()});
		if (isIgnored) {
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
					"Due to indexing, this was ignored.");
		} else {
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
					"The genome has been altered.");
		}

		// output the altered seq
		Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
				"Extra chromosomes : ");
		for (BioSeq seq : newGenome.getSeqList()) {
			BioSeq genomeSeq = oldGenome.getSeq(seq.getID());
			if (genomeSeq == null) {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,seq.getID());
			}
		}
	}


	private static void getAlteredChroms(AnnotatedSeqGroup newGenome, AnnotatedSeqGroup oldGenome, boolean isIgnored) {
		List<String> alteredChromStrings = new ArrayList<String>();
		for (BioSeq seq : newGenome.getSeqList()) {
			BioSeq genomeSeq = oldGenome.getSeq(seq.getID());
			if (genomeSeq != null && genomeSeq.getLength() != seq.getLength()) {
				alteredChromStrings.add(
						seq.getID() + ":" + seq.getLength() + "(was " + genomeSeq.getLength() + ") ");
			}
		}

		if (alteredChromStrings.size() > 0) {
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
					"altered chromosomes found for genome {0}. ", oldGenome.getID());
			if (isIgnored) {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
						"Indexing; this may cause problems.");
			} else {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
						"The genome has been altered.");
			}
			// output the altered seq
			Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,"Altered chromosomes : ");
			for (String alteredChromString : alteredChromStrings) {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING,
						alteredChromString);
			}
		}
	}

	/**
	 *  Gets the list of types of annotations for a given genome version.
	 *  Assuming top-level annotations hold type info in property "method" or "meth".
	 *  @return a Map where keys are feature type Strings and values are 
	 *    instances of SimpleDas2Type, which contains a list graph formats and
	 *    a map of properties.
	 *
	 *  may want to cache this info (per versioned source) at some point...
	 */
	public static Map<String, SimpleDas2Type> getAnnotationTypes(
					String data_root,
					AnnotatedSeqGroup genome,
					AnnotSecurity annotSecurity) {
		List<BioSeq> seqList = genome.getSeqList();
		Map<String,SimpleDas2Type> genome_types = new LinkedHashMap<String,SimpleDas2Type>();		
		for (BioSeq aseq : seqList) {
			for (String type : aseq.getTypeList()) {
				if (genome_types.get(type) != null) {
					continue;
				}
				List<String> flist = Collections.<String>emptyList();
				SymWithProps tannot = aseq.getAnnotation(type);
				SymWithProps first_child = (SymWithProps) tannot.getChild(0);
				if (first_child != null) {
					List formats = (List)first_child.getProperty("preferred_formats");
					if (formats != null) {
						flist = new ArrayList<String>(formats.size());
						for (Object o : formats) {
							flist.add((String) o);
						}
					}
				}
				
		        if (annotSecurity == null || isAuthorized(genome, annotSecurity, type)) {
					genome_types.put(type, new SimpleDas2Type(type, flist, getProperties(genome, annotSecurity, type)));
		        }
			}
			for (String type : aseq.getIndexedTypeList()) {
				if (genome_types.get(type) != null) {
					continue;
				}
				IndexedSyms iSyms = aseq.getIndexedSym(type);
				List<String> flist = new ArrayList<String>();
				flist.addAll(iSyms.iWriter.getFormatPrefList());
				
		        if (annotSecurity == null || isAuthorized(genome, annotSecurity, type)) {
					genome_types.put(type, new SimpleDas2Type(type, flist, getProperties(genome, annotSecurity, type)));
		        }

			}
		}
		return genome_types;
	}

	/**
	 * Add symloader types to map.
	 */
	public static void getSymloaderTypes(AnnotatedSeqGroup genome, AnnotSecurity annotSecurity, Map<String, SimpleDas2Type> genome_types) {
		for (String type : genome.getSymloaderList()) {
			SymLoader sym = genome.getSymLoader(type);
			if (genome_types.containsKey(type)) {
				return;
			}
			if (annotSecurity == null || isAuthorized(genome, annotSecurity, type)) {
				genome_types.put(type, new SimpleDas2Type(type, sym.getFormatPrefList(), getProperties(genome, annotSecurity, type)));
			}
		}
	}

	/**
	 * Add graph types to the map.
	 * @param data_root
	 * @param genome
	 * @param annotSecurity
	 * @param genome_types
	 */
	public static void getGraphTypes(String data_root, AnnotatedSeqGroup genome, AnnotSecurity annotSecurity, Map<String, SimpleDas2Type> genome_types) {
		for (String type : genome.getTypeList()) {
			if (genome_types.containsKey(type) || !isAuthorized(genome, annotSecurity, type)) {
				continue;
			}
			if (annotSecurity == null) {
				if (USeqUtilities.USEQ_ARCHIVE.matcher(type).matches()) {
					genome_types.put(type, new SimpleDas2Type(genome.getID(), USeqUtilities.USEQ_FORMATS, getProperties(genome, annotSecurity, type)));
				} else {
					genome_types.put(type, new SimpleDas2Type(genome.getID(), DasServerUtils.BAR_FORMATS, getProperties(genome, annotSecurity, type)));
				}
				continue;
			}
			if (annotSecurity.isBarGraphData(data_root, genome.getID(), type, genome.getAnnotationId(type))) {
				genome_types.put(type, new SimpleDas2Type(genome.getID(), DasServerUtils.BAR_FORMATS, getProperties(genome, annotSecurity, type)));
			} else if (annotSecurity.isUseqGraphData(data_root, genome.getID(), type, genome.getAnnotationId(type))) {
				genome_types.put(type, new SimpleDas2Type(genome.getID(), USeqUtilities.USEQ_FORMATS, getProperties(genome, annotSecurity, type)));
			} else if (annotSecurity.isBamData(data_root, genome.getID(), type, genome.getAnnotationId(type))) {
				genome_types.put(type, new SimpleDas2Type(genome.getID(), BAM.pref_list, getProperties(genome, annotSecurity, type)));
			} else {
				Logger.getLogger(Das2ServerUtils.class.getName()).log(Level.WARNING, "Non-graph annotation {0} encountered, but does not match known entry.  This annotation will not show in the types request.", type);
			}
		}
	}
	
	private static boolean isAuthorized(AnnotatedSeqGroup group, AnnotSecurity annotSecurity, String type) {
		boolean isAuthorized = annotSecurity == null || annotSecurity.isAuthorized(group.getID(), type, group.getAnnotationId(type));
		Logger.getLogger(AnnotatedSeqGroup.class.getName()).log(Level.FINE,
				"{0} Annotation {1} ID={2}", new Object[]{isAuthorized ? "Showing  " : "Blocking ", type, group.getAnnotationId(type)});
		return isAuthorized;
	}

	private static Map<String, Object> getProperties(AnnotatedSeqGroup group, AnnotSecurity annotSecurity, String type) {
		return annotSecurity == null ? null : annotSecurity.getProperties(group.getID(), type, group.getAnnotationId(type));
	}
}
