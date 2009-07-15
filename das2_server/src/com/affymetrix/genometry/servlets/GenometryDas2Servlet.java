package com.affymetrix.genometry.servlets;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.SimpleDateFormat;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import com.affymetrix.genometry.MutableAnnotatedBioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genometry.util.SeqUtils;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.parsers.*;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.Optimize;
import com.affymetrix.genometryImpl.util.ServerUtils;
import com.affymetrix.genometryImpl.util.Timer;


/**
 *  genometry-based DAS2 server
 *
 *  Servlet-based, object-oriented, in-memory "database" of genomic
 *     annotations for efficient retrieval of annotations that overlap a query range
 *     on a sequence.
 *  Search strategy: "interval array"
 *                   binary search followed by constrained location-tuned bidirectional scan
 *

// Server looks for data in directory specified by system property "das2_genometry_server_dir"
// Within data directory should be subdirectories, each corresponding to a different organism
//    organism_name = directory name
//    subdirectory under organsism name for each genome_version
//    for each genome version:
//       genome_version_name = directory name
//       parse liftAll or chromInfo file to create new AnnotatedSeqGroup for genome
//           in SingletonGenometryModel
//       for each file in genome_version directory
//           if directory:
//                if ends with "..seqs" then indicates a subdirectory with same annotation type,
//                      but split up into one annotation file per seq (for example .bar graph files)
//                recurse in
//           else if non-loading filetype (for example .bar):
//                add as annot type without loading
//           else try to parse and annotate seqs based on file suffix (.xyz)
//
*/
public final class GenometryDas2Servlet extends HttpServlet {

	static final boolean DEBUG = false;
	private static final String RELEASE_VERSION = "2.6";
	private static final boolean TIME_RESPONSES = true;
	//private static final boolean ADD_VERSION_TO_CONTENT_TYPE = false;
	private static final boolean USE_CREATED_ATT = true;
	private static boolean WINDOWS_OS_TEST = false;

	private static final String SERVER_SYNTAX_EXPLANATION =
		"See http://netaffxdas.affymetrix.com/das2 for proper query syntax.";
	private static final String LIMITED_FEATURE_QUERIES_EXPLANATION =
		"See http://netaffxdas.affymetrix.com/das2 for supported feature queries.";

	/* The long versions of these error messages appears to cause problems for Apache.
	   Specifically, they cause Apache to display an uninformative 502 Bad Gateway error
	   page because it deems the http header 'syntactically invalid'.
	   I suspect that the presence of multiple lines and/or blank lines is specically to blame.
	   To be safe, we're now using very succinct, single-line messages containing a pointer to
	   a web page which contains additional information to help guide the user about
	   acceptable queries.  - SteveC 15 Apr 2008
	   */
	/*private static String SERVER_SYNTAX_EXPLANATION_LONG =
	  "The Genometry DAS/2 server always uses a standard URI syntax for DAS/2 query URIs,\n\n" +
	  " and enforces this by specifying URIs in the SOURCES doc according to this standard:\n" +
	  "    das_server_root/genome_name/capability_name[?query_parameters]";*/
	/*private static String LIMITED_FEATURE_QUERIES_EXPLANATION_LONG =
	  "The Genometry DAS/2 server currently does not support the full range of \n" +
	  "DAS/2 feature queries and feature filters required by the DAS/2 specification. \n" +
	  "To allow the Genometry DAS/2 server to still comply with the specification, \n" +
	  "the server considers responses to any feature query it does not support as \n" +
	  "being 'too large'.  Therefore it responds with an error message with HTTP \n" +
	  "status code 413 'Request Entity Too Large', which is allowed by the DAS/2 spec \n" +
	  "when the server considers the response too large.\n\n" +
	  "Currently for the Genometry server to send a useful response containing features, \n" +
	  "  the feature query string must contain: \n" +
	  "     1 type filter \n" +
	  "     1 segment filter \n" +
	  "     1 overlaps filter \n" +
	  "     0 or 1 inside filter \n" +
	  "     0 or 1 format parameter \n" +
	  "     0 other filters/parameters \n";*/

	private static Map<String,Das2Coords> genomeid2coord;


	static {
		// GAH 11-2006
		// for now hardwiring URIs for agreed upon genome assembly coordinates, based on
		//    http://www.open-bio.org/wiki/DAS:GlobalSeqIDs
		// Plan to replace this with a smarter system once coordinates and reference URIs are specified in XML
		//     rather than an HTML page (hopefully will be served up as DAS/2 sources & segments XML)
		// covering the two naming schemes currently in use with this server, for example
		//     "H_sapiens_date" and "Human_date"
		genomeid2coord = new HashMap<String,Das2Coords>();
		genomeid2coord.put("H_sapiens_Mar_2006",
				new Das2Coords("http://www.ncbi.nlm.nih.gov/genome/H_sapiens/B36.1/",
					"NCBI", "9606", "36", "Chromosome", null));
		genomeid2coord.put("Human_Mar_2006",
				new Das2Coords("http://www.ncbi.nlm.nih.gov/genome/H_sapiens/B36.1/",
					"NCBI", "9606", "36", "Chromosome", null));
		genomeid2coord.put("H_sapiens_May_2004",
				new Das2Coords("http://www.ncbi.nlm.nih.gov/genome/H_sapiens/B35.1/",
					"NCBI", "9606", "35", "Chromosome", null));
		genomeid2coord.put("Human_May_2004",
				new Das2Coords("http://www.ncbi.nlm.nih.gov/genome/H_sapiens/B35.1/",
					"NCBI", "9606", "35", "Chromosome", null));
		genomeid2coord.put("D_melanogaster_Apr_2004",
				new Das2Coords("http://www.flybase.org/genome/D_melanogaster/R3.1/",
					"BDGP", "7227", "4", "Chromosome", null));
		genomeid2coord.put("Drosophila_Apr_2004",
				new Das2Coords("http://www.flybase.org/genome/D_melanogaster/R3.1/",
					"BDGP", "7227", "4", "Chromosome", null));
		//Zebrafish
		genomeid2coord.put("D_rerio_Jul_2007",
				new Das2Coords(
					//uri,rather odd, none of these actually point to anything
					"http://zfin.org/genome/D_rerio/Zv7/",
					//authority
					"ZFISH_7",
					//taxid
					"7955",
					//version
					"Zv7",
					//source
					"Chromosome",
					null));

		//C_elegans
		genomeid2coord.put("C_elegans_Jan_2007",
				new Das2Coords(
					//uri
					"http://www.wormbase.org/genome/C_elegans/WS180/",
					//authority
					"WS",
					//taxid
					"6239",
					//version
					"180",
					//source
					"Chromosome",
					null));

		//S_pombe
		genomeid2coord.put("S_pombe_Apr_2007",
				new Das2Coords(
					//uri
					"http://www.sanger.ac.uk/Projects/S_pombe/Apr_2007",
					//authority
					"Sanger",
					//taxid
					"4896",
					//version
					"Apr_2007",
					//source
					"Chromosome",
					null));
		//S_glossinidius
		genomeid2coord.put("S_glossinidius_Jan_2006",
				new Das2Coords(
					//uri
					"ftp://ftp.ncbi.nih.gov/genomes/Bacteria/Sodalis_glossinidius_morsitans/Jan_2006",
					//authority
					"NCBI",
					//taxid
					"343509",
					//version
					"Jan_2006",
					//source
					"Chromosome",
					null));

		WINDOWS_OS_TEST = System.getProperty("os.name").startsWith("Windows");


	}
	//private static final String DAS2_VERSION = "2.0";
	private static final String DAS2_NAMESPACE = Das2FeatureSaxParser.DAS2_NAMESPACE;
	private static final String SOURCES_CONTENT_TYPE = "application/x-das-sources+xml";
	private static final String SEGMENTS_CONTENT_TYPE = "application/x-das-segments+xml";
	private static final String TYPES_CONTENT_TYPE = "application/x-das-types+xml";
	private static final String LOGIN_CONTENT_TYPE = "application/x-das-login+xml";
	//    FEATURES_CONTENT_TYPE is set in the Das2FeatureSaxParser
	//  static String FEATURES_CONTENT_TYPE = "application/x-das-features+xml";

	private static final String URID = "uri";
	private static final String NAME = "title";

	/*
	 *  DAS commands recognized by GenometryDas2Servlet
	 *  (additional commands may be recognized by command plugins)
	 */
	// sources query is same as root URL (xml_base) minus trailing slash
	private static String sources_query_with_slash = "";  // set in setXmlBase()
	private static String sources_query_no_slash = ""; // set in setXmlBase();
	private static final String segments_query = "segments";
	private static final String types_query = "types";
	private static final String features_query = "features";
	private static final String query_att = "query_uri";
	private static final String login_query = "login";
	private static final String default_feature_format = "das2feature";

	//the following are now set by the loadAndSetFields() method
	private static String genometry_server_dir;
	private static String maintainer_email;
	private static String xml_base;
	/** The root directory of the data to be served-up.
	 *  Defaults to system property "user.dir" + "/query_server_smaller/".
	 *  The user can change this by setting a property for "genometry_server_dir"
	 *  on the command line.  For example "java -Dgenometry_server_dir=/home/me/mydir/ ...".
	 */
	private  static String data_root;

	private static String types_xslt_file;


	private static final Pattern query_splitter = Pattern.compile("[;\\&]");
	private static final Pattern tagval_splitter = Pattern.compile("=");



	/**
	 *  Top level data structure that holds all the genome models
	 */
	private static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
	/**
	 *  Top level data structure that holds all the genome models in source/version hierarchy
	 *  maps organism names to list of genome versions for that organism
	 */
	private static Map<String,List<AnnotatedSeqGroup>> organisms = new LinkedHashMap<String,List<AnnotatedSeqGroup>>();

	private Map<String,Class> output_registry = new HashMap<String,Class>();
	private final SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private long date_initialized = 0;
	private String date_init_string = null;
	private Map<AnnotatedSeqGroup,Map<String,String>> genome2graphfiles = new LinkedHashMap<AnnotatedSeqGroup,Map<String,String>>();
	private Map<AnnotatedSeqGroup,Map<String,String>> genome2graphdirs = new LinkedHashMap<AnnotatedSeqGroup,Map<String,String>>();
	private ArrayList<String> graph_formats = new ArrayList<String>();
	private Transformer types_transformer;
	private boolean DEFAULT_USE_TYPES_XSLT = true;
	private boolean use_types_xslt;
	private Das2Authorization dasAuthorization;

	private static String synonym_file;
	private static String org_order_filename;

	@Override
		public void init() throws ServletException {
			System.out.println("Called GenometryDas2Servlet.init()");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				Logger.getLogger(GenometryDas2Servlet.class.getName()).log(Level.SEVERE, null, ex);
			}
			//attempt to load fields from System.properties or file
			if (loadAndSetFields() == false) {
				System.out.println("FAILED to init() GenometryDas2Servlet, aborting!");
				return;
			}

			try {
				super.init();
				use_types_xslt = DEFAULT_USE_TYPES_XSLT && (new File(types_xslt_file)).exists();
				if (use_types_xslt) {
					Source type_xslt = new javax.xml.transform.stream.StreamSource(types_xslt_file);
					javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
					types_transformer = transFact.newTransformer(type_xslt);
				}

				System.out.println("GenometryDas2Servlet version: " + RELEASE_VERSION);
				if (!(new File(data_root)).isDirectory()) {
					throw new ServletException("Aborting: Specified directory does not exist: '" + data_root + "'");
				}
				System.out.println("Starting GenometryDas2Servlet in directory: '" + data_root + "'");

				initFormats(output_registry, graph_formats);

				ServerUtils.loadSynonyms(synonym_file);

				loadGenomes();

				ServerUtils.printGenomes(organisms);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//instantiate DasAuthorization
			dasAuthorization = new Das2Authorization(new File(data_root));

			System.out.println("finished with GenometryDas2Servlet.init()");
			date_initialized = System.currentTimeMillis();
			date_init_string = date_formatter.format(new Date(date_initialized));
		}

	/**
	 * Attempts to load the genometry_server_dir, maintainer_email, and the
	 * xml_base from the servlet context, System.properties or from a
	 * genometryDas2ServletParameters.txt.  Lastly it will set several fields
	 * for the servlet.
	 *
	 * @return true if fields loaded or false if not.
	 */
	private final boolean loadAndSetFields() {
		// attempt to get properties from servlet context
		ServletContext context = getServletContext();
		genometry_server_dir = context.getInitParameter("genometry_server_dir");
		maintainer_email = context.getInitParameter("maintainer_email");
		xml_base = context.getInitParameter("xml_base");


		//attempt to get from System.properties
		if (genometry_server_dir == null || maintainer_email == null || xml_base == null) {
			genometry_server_dir = System.getProperty("das2_genometry_server_dir");
			maintainer_email = System.getProperty("das2_maintainer_email");
			xml_base = System.getProperty("das2_xml_base");
		}

		//attempt to load from file?
		if (genometry_server_dir == null || maintainer_email == null || xml_base == null) {
			//look for file
			File p = new File("genometryDas2ServletParameters.txt");
			if (p.exists() == false) {
				System.out.println("\tLooking for but couldn't find " + p);
				File dir = new File(System.getProperty("user.dir"));
				p = new File(dir, "genometryDas2ServletParameters.txt");
				//look for it in the users home
				if (p.exists() == false) {
					System.out.println("\tLooking for but couldn't find " + p);
					dir = new File(System.getProperty("user.home"));
					p = new File(dir, "genometryDas2ServletParameters.txt");
					if (p.exists() == false) {
						System.out.println("\tLooking for but couldn't find " + p);
						System.out.println("\tERROR: Failed to load fields from " +
								"System.properties or from the 'genometryDas2ServletParameters.txt' file.");
						return false;
					}
				}
			}
			System.out.println("\tFound and loading " + p);

			//load file
			HashMap<String, String> prop = ServerUtils.loadFileIntoHashMap(p);
			if (prop == null) {
				System.out.println("\tERROR: loading " + p + " file, aborting.");
				return false;
			}
			//load fields
			if (genometry_server_dir == null && prop.containsKey("genometry_server_dir")) {
				genometry_server_dir = prop.get("genometry_server_dir");
			}
			if (maintainer_email == null && prop.containsKey("maintainer_email")) {
				maintainer_email = prop.get("maintainer_email");
			}
			if (xml_base == null && prop.containsKey("xml_base")) {
				xml_base = prop.get("xml_base");
			}
			//check for data dir and xml base, email is apparently optional
			if (genometry_server_dir == null || xml_base == null) {
				System.out.println("\tERROR: could not set the following:\n\t\tgenometry_server_dir\t" + genometry_server_dir + "\n\t\txml_base\t" + xml_base);
				return false;
			}
		}

		//print values
		System.out.println("\t\tgenometry_server_dir\t" + genometry_server_dir);
		System.out.println("\t\txml_base\t" + xml_base);
		System.out.println("\t\tmaintainer_email\t" + maintainer_email);

		//set data root
		// Note adding an extra "/" at the end of genometry_server_dir just to be certain
		// there is one there.  If it ends up with two "/" characters, that hurts nothing
		data_root = genometry_server_dir + "/";

		//set various files as Strings
		synonym_file = data_root + "synonyms.txt";
		types_xslt_file = data_root + "types.xslt";
		org_order_filename = data_root + "organism_order.txt";

		//set xml base
		System.out.println("setting xml_base: " + xml_base);
		setXmlBase(xml_base);

		return true;
	}

	

	private static final void initFormats(Map<String,Class> output_registry, ArrayList<String> graph_formats) {
		// Alternatives: (for now trying option B)
		//   A. hashing to AnnotationWriter object:
		//        output_registry.put("bps", new BpsParser());
		//        output_registry.put("psl", new PSLParser())
		//   B. hashing to AnnotationWriter Class object rather than instance of a writer object:
		output_registry.put("link.psl", ProbeSetDisplayPlugin.class);
		output_registry.put("bps", BpsParser.class);
		//      id2mime.put("bps", "binary/
		output_registry.put("psl", PSLParser.class);
		output_registry.put("dasgff", Das1FeatureSaxParser.class);
		output_registry.put("dasxml", Das1FeatureSaxParser.class);
		output_registry.put("bed", BedParser.class);
		output_registry.put("simplebed", SimpleBedParser.class);
		output_registry.put("bgn", BgnParser.class);
		output_registry.put("brs", BrsParser.class);
		// GFFParser.
		output_registry.put("gff", GFFParser.class);
		//      output_registry.put("link.psl", ProbeSetDisplayPlugin.class);
		output_registry.put("das2feature", Das2FeatureSaxParser.class);
		output_registry.put("das2xml", Das2FeatureSaxParser.class);
		output_registry.put("bar", BarParser.class);
		output_registry.put(Das2FeatureSaxParser.FEATURES_CONTENT_TYPE, Das2FeatureSaxParser.class);
		output_registry.put(Das2FeatureSaxParser.FEATURES_CONTENT_SUBTYPE, Das2FeatureSaxParser.class);
		output_registry.put("bp2", Bprobe1Parser.class);
		output_registry.put("ead", ExonArrayDesignParser.class);
		output_registry.put("cyt", CytobandParser.class);
		graph_formats.add("bar");
	}


	private final void loadGenomes() throws IOException {
		// get list of all directories in data root
		// each directory corresponds to a different organism
		//    organism_name = directory name
		//    subdirectory for each genome_version
		//    for each genome version:
		//       genome_version_name = directory name
		//       parse liftAll or chromInfo file to create new AnnotatedSeqGroup for genome
		//           in SingletonGenometryModel
		//       for each file in genome_version directory
		//           if directory, recurse in
		//           else try to parse and annotate seqs based on file suffix (.xyz)

		File top_level = new File(data_root);
		if (!top_level.exists()) {
			throw new IOException("File does not exist: '" + top_level + "'");
		}
		File[] orgs = top_level.listFiles();
		if (orgs == null || orgs.length == 0) {
			throw new IOException("Directory has no contents: '" + top_level + "'");
		}
		for (int i = 0; i < orgs.length; i++) {
			File org = orgs[i];
			if (org.isDirectory()) {  // assuming all directories at this level represent organisms
				File[] versions = org.listFiles();
				for (int k = 0; k < versions.length; k++) {
					File version = versions[k];
					if (version.isDirectory()) {
						loadGenome(version, org.getName());
					}
				}
			}
		}
		
		ServerUtils.sortGenomes(organisms, org_order_filename);
	}

	private final void loadGenome(File genome_directory, String organism) throws IOException {
		// first, create MutableAnnotatedSeqs for each chromosome via ChromInfoParser
		String genome_version = genome_directory.getName();
		System.out.println("loading data for genome: " + genome_version);

		ServerUtils.parseChromosomeData(genome_directory, genome_version);

		AnnotatedSeqGroup genome = gmodel.getSeqGroup(genome_version);
		if (genome == null) {
			return;
		}  // bail out if genome didn't get added to AnnotatedSeqGroups
		genome2graphdirs.put(genome, new LinkedHashMap<String, String>());
		genome2graphfiles.put(genome, new LinkedHashMap<String, String>());
		genome.setOrganism(organism);
		List<AnnotatedSeqGroup> versions = organisms.get(organism);
		if (versions == null) {
			versions = new ArrayList<AnnotatedSeqGroup>();
			organisms.put(organism, versions);
		}
		versions.add(genome);

		// second: search genome directory for annotation files to load
		// (and recursively descend through subdirectories doing same)
		Map<String,String> graph_name2dir = genome2graphdirs.get(genome);
		Map<String,String> graph_name2file = genome2graphfiles.get(genome);
		ServerUtils.loadAnnotsFromFile(genome_directory, genome, null, graph_name2dir, graph_name2file);

		//Third: optimize genome by replacing second-level syms with IntervalSearchSyms
		Optimize.Genome(genome);
	}



	@Override
		public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String path_info = request.getPathInfo();
		String query = request.getQueryString();
		System.out.println("GenometryDas2Servlet received POST request: ");
		System.out.println("   path: " + path_info);
		System.out.println("   query: " + query);
		}

	@Override
		public void doPut(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		System.out.println("GenometryDas2Servlet received PUT request: ");
		}

	@Override
		public long getLastModified(HttpServletRequest request) {
			System.out.println("getLastModified() called");
			String path_info = request.getPathInfo();
			String query = request.getQueryString();
			System.out.println("   path: " + path_info);
			System.out.println("   query: " + query);
			return date_initialized;
		}

	@Override
		public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		Timer timecheck = null;
			if (TIME_RESPONSES) {
				timecheck = new Timer();
				timecheck.start();
			}
			System.out.println("*************** Genometry Das2 Servlet ***************");
			System.out.println(date_formatter.format(new Date(System.currentTimeMillis())));
			//    Memer mem = new Memer();
			//    mem.printMemory();
			String path_info = request.getPathInfo();
			String query = request.getQueryString();
			String request_url = request.getRequestURL().toString();
			System.out.println("HttpServletResponse buffer size: " + response.getBufferSize());
			System.out.println("path_info: " + path_info);
			System.out.println("url: " + request_url);
			System.out.println("query: " + query);
			System.out.println("path translated = " + request.getPathTranslated());
			System.out.println("context path = " + request.getContextPath());
			System.out.println("request uri = " + request.getRequestURI());
			System.out.println("servlet path = " + request.getServletPath());

			HandleDas2Request(path_info, response, request, request_url);
			
			if (TIME_RESPONSES) {
					long tim = timecheck.read();
					System.out.println("---------- response time: " + tim / 1000f + "----------");
				}
		}

	private final void HandleDas2Request(String path_info, HttpServletResponse response, HttpServletRequest request, String request_url) throws IOException {
		if (path_info == null || path_info.trim().length() == 0 || path_info.endsWith(sources_query_no_slash) || path_info.endsWith(sources_query_with_slash)) {
			handleSourcesRequest(request, response, date_init_string);
		} else if (path_info.endsWith(login_query)) {
			handleLoginRequest(this.dasAuthorization, request, response);
		} else {
			AnnotatedSeqGroup genome = getGenome(request);
			// System.out.println("Genome version: '"+ genome.getID() + "'");
			if (genome == null) {
				System.out.println("Unknown genome version");
				//        response.setStatus(response.SC_BAD_REQUEST);
				response.sendError(response.SC_BAD_REQUEST, "Query was not recognized, possibly the genome name is incorrect or missing from path? " + SERVER_SYNTAX_EXPLANATION);
			} else {
				String das_command = path_info.substring(path_info.lastIndexOf("/") + 1);
				System.out.println("das2 command: " + das_command);
				//        DasCommandPlugin plugin = (DasCommandPlugin)command2plugin.get(das_command);
				if (das_command.equals(segments_query)) {
					handleSegmentsRequest(request, response);
				} else if (das_command.equals(types_query)) {
					handleTypesRequest(request, response);
				} else if (das_command.equals(features_query)) {
					handleFeaturesRequest(request, response);
				} else if (genome.getSeq(das_command) != null) {
					System.out.println("handling seq request: " + request_url);
					handleSequenceRequest(request, response);
				} else {
					System.out.println("DAS2 request not recognized, setting HTTP status header to 400, BAD_REQUEST");
					response.sendError(response.SC_BAD_REQUEST, "Query was not recognized. " + SERVER_SYNTAX_EXPLANATION);
				}
			}
		}
	}

	/**
	 * Extracts name of (versioned?) genome from servlet request,
	 *    and uses to retrieve AnnotatedSeqGroup (genome) from SingletonGenometryModel
	 */
	private static final AnnotatedSeqGroup getGenome(HttpServletRequest request) {
		String path_info = request.getPathInfo();
		if (path_info == null) {
			return null;
		}
		int last_slash = path_info.lastIndexOf('/');
		int prev_slash = path_info.lastIndexOf('/', last_slash - 1);
		//    System.out.println("last_slash: " + last_slash + ",  prev_slash: " + prev_slash);
		String genome_name = path_info.substring(prev_slash + 1, last_slash);
		AnnotatedSeqGroup genome = gmodel.getSeqGroup(genome_name);
		if (genome == null) {
			System.out.println("unknown genome version: '" + genome_name + "'");
		}
		return genome;
	}

	/**
	 * Handle a sequence request.
	 * @param request
	 * @param response
	 * @throws java.io.IOException
	 */
	private static final void handleSequenceRequest(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
		String path_info = request.getPathInfo();
		String query = request.getQueryString();
		query = URLDecoder.decode(query);
		ArrayList<String> formats = new ArrayList<String>();
		ArrayList<String> ranges = new ArrayList<String>();
		String[] query_array = query_splitter.split(query);
		String format = "";
		//boolean all_params_known = true;

		for (int i = 0; i < query_array.length; i++) {
			String tagval = query_array[i];
			String[] tagval_array = tagval_splitter.split(tagval);
			String tag = tagval_array[0];
			String val = tagval_array[1];
			System.out.println("tag = " + tag + ", val = " + val);
			if (tag.equals("format")) {
				formats.add(val);
			} else if (tag.equals("range")) {
				ranges.add(val);
			}
			//else {
				//all_params_known = false;
			//}
		}

		AnnotatedSeqGroup genome = getGenome(request);
		System.out.println("Organism: " + genome.getOrganism());
		System.out.println("ID: " + genome.getID());
		//PrintWriter pw = response.getWriter();
		String org_name = genome.getOrganism();
		String version_name = genome.getID();
		String seqname = path_info.substring(path_info.lastIndexOf("/") + 1);

		SeqSpan span = null;
		if (ranges.size() > 1) {
			System.out.println("too many range params, aborting");
			return;
		} else if (ranges.size() == 1) {
			span = ServerUtils.getLocationSpan(seqname, ranges.get(0), genome);
		}
		if (formats.size() > 1) {
			System.out.println("too many format params, aborting");
			return;
		}
		if (formats.size() == 1) {
			format = formats.get(0);
		}

		//pw.println("Stub for handling sequence residues request, currently not implemented");
		// PhaseI: retrieval of whole chromosome in bnib format
		if (format.equals("bnib")) {
			retrieveBNIB(ranges, org_name, version_name, seqname, format, response, request);
			return;
		}


		if (format.equals("fasta")) {
			retrieveFASTA(ranges, span, org_name, version_name, seqname, format, response, request);
			return;
		}

		PrintWriter pw = response.getWriter();
		pw.println("This DAS/2 server cannot currently handle request:    ");
		pw.println(request.getRequestURL().toString());
	}


	private static final void retrieveBNIB(ArrayList ranges, String org_name, String version_name, String seqname, String format, HttpServletResponse response, HttpServletRequest request) throws IOException {
		/*if (ranges.size() != 0) {
			// A ranged request for a bnib.  Not supported.
			PrintWriter pw = response.getWriter();
			pw.println("This DAS/2 server does not support ranged " + format + " requests:    ");
			pw.println(request.getRequestURL().toString());
			return;
		}*/

		// range requests are ignored.  The entire sequence is returned.

		String file_name = data_root + org_name + "/" + version_name + "/dna/" + seqname + ".bnib";
		System.out.println("seq request mapping to file: " + file_name);
		File seqfile = new File(file_name);
		if (seqfile.exists()) {
			byte[] buf = NibbleResiduesParser.ReadBNIB(seqfile);
			response.setContentType(NibbleResiduesParser.getMimeType()); // set bnib format mime type
			DataOutputStream dos = new DataOutputStream(response.getOutputStream());
			try {
				dos.write(buf, 0, buf.length);
			} finally {
				// should output stream get closed here?
				GeneralUtils.safeClose(dos);
			}
		} else {
			PrintWriter pw = response.getWriter();
			pw.println("File not found: " + file_name);
			pw.println("This DAS/2 server cannot currently handle request:    ");
			pw.println(request.getRequestURL().toString());
		}
	}

	/**
	 * Retrieve sequence from FASTA file.  Please note restrictions in FASTA parser for DAS/2 serving.
	 * @param ranges
	 * @param span
	 * @param org_name
	 * @param version_name
	 * @param seqname
	 * @param format
	 * @param response
	 * @param request
	 * @throws java.io.IOException
	 */
	@Deprecated
	private static final void retrieveFASTA(ArrayList ranges, SeqSpan span, String org_name, String version_name, String seqname, String format, HttpServletResponse response, HttpServletRequest request)
		throws IOException {
		String file_name = data_root + org_name + "/" + version_name + "/dna/" + seqname + ".fa";
		File seqfile = new File(file_name);
		if (!seqfile.exists()) {
			System.out.println("seq request mapping to nonexistent file: " + file_name);
			PrintWriter pw = response.getWriter();
			pw.println("File not found: " + file_name);
			pw.println("This DAS/2 server cannot currently handle request:    ");
			pw.println(request.getRequestURL().toString());
			return;
		}

		// Determine spanStart and spanEnd.  If it's an unranged query, then just make SpanEnd no larger than the filesize.
		int spanStart = 0, spanEnd = 0;
		if (ranges.size() == 0) {
			if (seqfile.length() > (long) Integer.MAX_VALUE) {
				spanEnd = Integer.MAX_VALUE;
			} else {
				spanEnd = (int) seqfile.length();
			}
		} else {
			spanStart = span.getStart();
			spanEnd = span.getEnd();
		}

		System.out.println("seq request mapping to file: " + file_name + " spanning " + spanStart + " to " + spanEnd);

		response.setContentType(FastaParser.getMimeType());
		byte[] buf = FastaParser.ReadFASTA(seqfile, spanStart, spanEnd);
		byte[] header = FastaParser.GenerateNewHeader(seqname, org_name, spanStart, spanEnd);
		OutputFormattedFasta(buf, header, response.getOutputStream());
	}

	// Write a formatted fasta file out to the ServletOutputStream.
	private static final void OutputFormattedFasta(byte[] buf, byte[] header, ServletOutputStream sos)
		throws IOException, IOException, IllegalArgumentException {
		if (buf == null) {
			return;
		}

		DataOutputStream dos = new DataOutputStream(sos);
		try {
			dos.write(header, 0, header.length);

			byte[] newlineBuf = new byte[1];
			newlineBuf[0] = '\n';

			// Write out Fasta sequence, adding a newline after every LINELENGTH characters.
			int lines = buf.length / FastaParser.LINELENGTH;
			for (int i = 0; i < lines; i++) {
				//System.out.println("Writing out line " + i + " with " + i * FastaParser.LINELENGTH);
				dos.write(buf, i * FastaParser.LINELENGTH, FastaParser.LINELENGTH);
				dos.write(newlineBuf, 0, 1);
			}
			if (buf.length % FastaParser.LINELENGTH > 0) {
				// Write remainder of last line out to buffer
				//System.out.println("Writing out remainder of " + buf.length % FastaParser.LINELENGTH + " at:" + lines * FastaParser.LINELENGTH);
				dos.write(buf, lines * FastaParser.LINELENGTH, buf.length % FastaParser.LINELENGTH);
				dos.write(newlineBuf, 0, 1);
			}
		} finally {
			GeneralUtils.safeClose(dos);
		}
	}

	private static final void handleSourcesRequest(HttpServletRequest request, HttpServletResponse response, String date_init_string)
		throws IOException {
		System.out.println("received data source query");
		response.setContentType(SOURCES_CONTENT_TYPE);
		PrintWriter pw = response.getWriter();

		String xbase = getXmlBase(request);
		printXmlDeclaration(pw);
		//    pw.println("<!DOCTYPE DAS2XML SYSTEM \"http://www.biodas.org/dtd/das2xml.dtd\">");
		pw.println("<SOURCES");
		pw.println("    xmlns=\"" + DAS2_NAMESPACE + "\"");
		pw.println("    xml:base=\"" + xbase + "\" >");
		if (maintainer_email != null && maintainer_email.length() > 0) {
			pw.println("  <MAINTAINER email=\"" + maintainer_email + "\" />");
		}
		// other elements to add:

		for (Map.Entry<String,List<AnnotatedSeqGroup>> oentry : organisms.entrySet()) {
			String org = oentry.getKey();
			List<AnnotatedSeqGroup> versions = oentry.getValue();
			//Iterator giter = genomes.entrySet().iterator();
			//      pw.println("  <SOURCE id=\"" + org + "\" >" );
			pw.println("  <SOURCE uri=\"" + org + "\" title=\"" + org + "\" >");

			for (AnnotatedSeqGroup genome : versions) {
				Das2Coords coords = genomeid2coord.get(genome.getID());
				//	System.out.println("Genome: " + genome.getID() + ", organism: " + genome.getOrganism() +
				//		     ", version: " + genome.getVersion() + ", seq count: " + genome.getSeqCount());
				//      pw.println("      <VERSION id=\"" + genome.getID() + "\" />" );
				if (USE_CREATED_ATT) {
					pw.println("      <VERSION uri=\"" + genome.getID() + "\" title=\"" + genome.getID() +
							"\" created=\"" + date_init_string + "\" >");
				} else {
					pw.println("      <VERSION uri=\"" + genome.getID() + "\" title=\"" + genome.getID() + "\" >");
				}
				if (coords != null) {
					pw.println("           <COORDINATES uri=\"" + coords.getURI() +
							"\" authority=\"" + coords.getAuthority() +
							"\" taxid=\"" + coords.getTaxid() +
							"\" version=\"" + coords.getVersion() +
							"\" source=\"" + coords.getSource() + "\" />");
				}
				pw.println("           <CAPABILITY type=\"" + segments_query + "\" " + query_att + "=\"" +
						genome.getID() + "/" + segments_query + "\" />");
				pw.println("           <CAPABILITY type=\"" + types_query + "\" " + query_att + "=\"" +
						genome.getID() + "/" + types_query + "\" />");
				pw.println("           <CAPABILITY type=\"" + features_query + "\" " + query_att + "=\"" +
						genome.getID() + "/" + features_query + "\" />");
				// other attributes to add:
				//   title, created, modified, writeable
				//   also doc_href -- need to change sources.rnc file for this!
				// other elements to add:
				//   <ASSEMBLY>
				//   <COORDINATES>
				//   <PROP>
				pw.println("      </VERSION>");
			}
			pw.println("  </SOURCE>");
		}
		pw.println("</SOURCES>");
	}

	private static final void handleSegmentsRequest(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
		System.out.println("Received region query");
		AnnotatedSeqGroup genome = getGenome(request);
		// genome null check already handled, so if it get this far the genome is non-null
		Das2Coords coords = genomeid2coord.get(genome.getID());

		response.setContentType(SEGMENTS_CONTENT_TYPE);

		PrintWriter pw = response.getWriter();
		printXmlDeclaration(pw);

		String xbase = getXmlBase(request) + genome.getID() + "/";
		String segments_uri = xbase + segments_query;
		//    pw.println("<!DOCTYPE DAS2XML SYSTEM \"http://www.biodas.org/dtd/das2xml.dtd\">");
		pw.println("<SEGMENTS ");
		pw.println("    xmlns=\"" + DAS2_NAMESPACE + "\"");
		//    pw.println("    xml:base=\"" + xbase + "\" >");
		pw.println("    xml:base=\"" + xbase + "\" ");
		// uri attribute is added purely to satisfy DAS 2.0 RelaxNG schema, it points back to this same document
		pw.println("    " + URID + "=\"" + segments_uri + "\" >");


		for (BioSeq aseq : genome.getSeqList()) {
			String refatt = "";
			if (coords != null) {
				// GAH 11-2006
				// for now guessing at the reference URI, based on assembly URI and typical syntax used
				//    at http://www.open-bio.org/wiki/DAS:GlobalSeqIDs for these URIs
				// Plan to replace this with a smarter system once reference URIs are specified in XML
				//     rather than an HTML page (hopefully will be served up as DAS/2 sources & segments XML)
				String ref = coords.getURI() + "dna/" + aseq.getID();
				refatt = "reference=\"" + ref + "\"";
			}
			pw.println("   <SEGMENT " + URID + "=\"" + aseq.getID() + "\" " + NAME + "=\"" + aseq.getID() + "\"" +
					" length=\"" + aseq.getLength() + "\" " + refatt + " />");
			//      pw.println("<REGION id=\"" + aseq.getID() +
			//		 "\" start=\"0\" end=\"" + aseq.getLength() + "\" />");
		}
		pw.println("</SEGMENTS>");
	}

	/**
	 *  Handles a request for "entry_types", building a "<DASTYPES>" response
	 *  into the HttpServletRequest. If this server instance is authorizing, some restricted types will be
	 *  filtered based on the users Session object. 
	 *  
	 */
	private final void handleTypesRequest(HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		System.out.println("Received types request");
		AnnotatedSeqGroup genome = getGenome(request);
		if (genome == null) {
			System.out.println("Unknown genome version");
			response.setStatus(response.SC_BAD_REQUEST);
			return;
		}
		HashMap<String,HashSet<String>> userAuthorizedResources = getUserAuthorizedResources(dasAuthorization, request);

		response.setContentType(TYPES_CONTENT_TYPE);

		Map<String,List<String>> types_hash = 
						ServerUtils.getTypes(
						genome,
						genome2graphfiles.get(genome),
						genome2graphdirs.get(genome),
						graph_formats);

		ByteArrayOutputStream buf = null;
		PrintWriter pw = null;
		if (use_types_xslt) {
			buf = new ByteArrayOutputStream(types_hash.size() * 1000);
			pw = new PrintWriter(buf);
		} else {
			pw = response.getWriter();
		}

		String xbase = getXmlBase(request) + genome.getID() + "/";
		writeTypesXML(pw, xbase, genome.getID(), types_hash, userAuthorizedResources, dasAuthorization);

		if (use_types_xslt) {
			pw.flush();
			byte[] buf_array = buf.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(buf_array);
			try {
				Source types_doc = new StreamSource(bais);
				Result result = new StreamResult(response.getWriter());
				try {
					types_transformer.transform(types_doc, result);
				} catch (TransformerException ex) {
					ex.printStackTrace();
				}
			} finally {
				GeneralUtils.safeClose(bais);
			}
		}
	}

	private static final HashMap<String,HashSet<String>> getUserAuthorizedResources(
					Das2Authorization dasAuthorization,
					HttpServletRequest request) {
		//fetch Session object and userAccessibleDirectories?
		HashMap<String,HashSet<String>> userAuthorizedResources = null;
		if (dasAuthorization.isAuthorizing()) {
			System.out.println("\tDas authorization in effect ");
			HttpSession userSession = request.getSession(false);
			if (userSession != null) {
				Object obj = userSession.getAttribute("authorizedResources");
				if (obj != null) {
					userAuthorizedResources = (HashMap<String,HashSet<String>>) obj;
					System.out.println("\t\tFound 'authorizedResources'");
				} else {
					System.out.println("\t\tCould not fetch 'authorizedResources' from user session");
				}
			} else {
				System.out.println("\t\tUser session is null");
			}
		}
		return userAuthorizedResources;
	}


	

	private static final void writeTypesXML(
			PrintWriter pw,
			String xbase,
			String genome_id,
			Map<String,List<String>> types_hash,
			HashMap<String,HashSet<String>> userAuthorizedResources,
			Das2Authorization dasAuthorization) {
		printXmlDeclaration(pw);
		//    pw.println("<!DOCTYPE DAS2TYPES SYSTEM \"http://www.biodas.org/dtd/das2types.dtd\" >");
		pw.println("<TYPES ");
		pw.println("    xmlns=\"" + DAS2_NAMESPACE + "\"");
		pw.println("    xml:base=\"" + xbase + "\" >");

		List<String> sorted_types_list = new ArrayList<String>(types_hash.keySet());
		Collections.sort(sorted_types_list);
		Iterator types_iter = sorted_types_list.iterator();
		while (types_iter.hasNext()) {
			String feat_type = (String) types_iter.next();
			//check if authorizing particular types
			if (dasAuthorization.isAuthorizing()) {
				boolean showType = dasAuthorization.showResource(userAuthorizedResources, genome_id, feat_type);
				if (showType) {
					System.out.println("\t\tShowing " + genome_id + " " + feat_type);
				} else {
					System.out.println("\t\tBlocking " + genome_id + " " + feat_type);
					continue;
				}
			}
			List<String> formats = types_hash.get(feat_type);
			String feat_type_encoded = URLEncoder.encode(feat_type);
			// URLEncoding replaces slashes, want to keep those...
			feat_type_encoded = feat_type_encoded.replaceAll("%2F", "/");
			/*if (DEBUG) {
			  System.out.println("feat_type: " + feat_type + ", formats: " + formats);
			  }*/
			/*pw.println("   <TYPE " + URID + "=\"" + feat_type_encoded + "\" " + NAME + "=\"" + feat_type +
			  "\" " + SO_ACCESSION + "=\"" + default_onto_term + "\" " + ONTOLOGY + "=\"" + default_onto_uri + "\" >");
			  */
			// Not currently using accession or ontology

			// Title may be stored in annots.xml file.
			String title = feat_type;

			pw.println("   <TYPE " + URID + "=\"" + feat_type_encoded + "\" " + NAME + "=\"" + title + "\" >");
			if ((formats != null) && (!formats.isEmpty())) {
				for (int k = 0; k < formats.size(); k++) {
					String format = formats.get(k);
					pw.println("       <FORMAT name=\"" + format + "\" />");
				}
			}
			pw.println("   </TYPE>");
		}
		pw.println("</TYPES>");
			}


	/**Checks to see if a this server instance is authorizing. If so, will check to see if any user and password parameters were supplied.
	 * If no parameters are supplied the an xml doc is returned with a AUTHORIZED tag set to true, otherwise false.
	 * This is basically a call to see if login capabilities are implemented.
	 * If parameters are supplied, the method attempts to authenticate the user, if OK an HTTPSession object is created 
	 * for the user and a JSESSIONID is attached to the xml response as a cookie.*/
	private static final void handleLoginRequest(
					Das2Authorization dasAuthorization,
					HttpServletRequest request,
					HttpServletResponse response) throws IOException {
		System.out.println("Received login request");
		String comment;
		boolean authorized;
		if (dasAuthorization.isAuthorizing()) {
			//fetch parameters
			String userName = request.getParameter("user");
			String password = request.getParameter("password");
			String encrypted = null;
			if (password != null) {
				encrypted = Das2Authorization.encrypt(password);
			}
			System.out.println("\tName: " + userName);
			System.out.println("\tEncryptedPassword: " + encrypted);

			//look up to see if match
			HashMap authorizedResources = dasAuthorization.validate(userName, password);
			if (authorizedResources != null) {
				//get session or create a new one
				HttpSession session = request.getSession(true);
				session.setAttribute("authorizedResources", authorizedResources);
				session.setMaxInactiveInterval(259200); //72hrs
				System.out.println("\tSet HashMap in user session " + authorizedResources);
				comment = "Logged in.";
				authorized = true;
			} else {
				comment = "Failed to log in, either the user doesn't exist or the password is incorrect.";
				authorized = false;
			}
		} else {
			comment = "This DAS2 server is not restricting access to any resources.  No need for authentication.";
			authorized = true;
		}
		//send response
		response.setContentType(LOGIN_CONTENT_TYPE);
		PrintWriter pw = response.getWriter();
		String xbase = getXmlBase(request);
		printXmlDeclaration(pw);
		pw.println("<LOGIN");
		pw.println("    xmlns=\"" + DAS2_NAMESPACE + "\"");
		pw.println("    xml:base=\"" + xbase + "\" >");
		if (maintainer_email != null && maintainer_email.length() > 0) {
			pw.println("  <MAINTAINER email=\"" + maintainer_email + "\" />");
		}
		pw.println("\t<AUTHORIZED>" + authorized + "</AUTHORIZED>");
		pw.println("\t<COMMENT>" + comment + "</COMMENT>");
		pw.println("</LOGIN>");
	}


	/**
	 *  precedence for feature out format:
	 *     specified in query string > specified in content-negotiation header > default
	 *
	 *  Would like to change this code to use ServletRequest getParameterValues(tag) instead,
	 *   but getParameterValues() is stricter about the query parameters, so that a non-url-encoded ":"
	 *   in a query parameter can lead to dropping any following parameters.  Which would be okay
	 *   if parameters were always url-encoded like they are supposed to be, but I want the genometry
	 *   server to handle cases where parameters were accidentally not url-encoded.  So this code
	 *   takes the raw query string and handles processing itself.
	// split query tagval list into format and filters
	// any tagval where tag = "format" determines format -- should only be one
	// all other tagvals should be filters
	//
	// GAH 4-18-2005 for now only trying to handle region filters and types filters
	//   currently assumes the following:
	//                    one "overlap" region filter, no ORing of multiple overlap filters
	//                    zero or one "inside" region filter, no ORing of multiple inside filters
	//                    one "type" filter (by typeid), no ORing of multiple type filters
	 *
	 *  New logic for DAS/2 feature request filters:
	 *  If there are multiple occurrences of the same filter name in the request, take the union
	 *      of the results of each of these filters individually
	 *  Then take intersection of results of each different filter name
	 *  (OR similar filters, AND different filters [ except excludes ] )
	 *
	 *  General query strategy:
	 *  [NOT SURE WHAT TO DO YET ABOUT COORDINATES FILTERS]
	 *    if any link, note, or prop filter, then return empty results
	 *    if format parameter:
	 *       if > 1 format parameter, then return "bad request" error
	 *       if > 1 type (or no type), then need to make sure that format can support multiple types
	 *         (for now, return "REQUEST TOO LARGE" error)
	 *       if 1 type, then make sure server supports returning that type in that format
	 *    if just name filter:
	 *        special case to search for names...
	 *    else:
	 *      if no type given, then return "REQUEST TOO LARGE" error
	 *      else for each type:
	 *       for each segment:
	 *          for each overlap:
	 *               collect (top-level) RESULTS syms with given type, segment, and overlap range
	 *    for each sym in RESULTS
	 *        filter by inside(s)
	 *        filter by exclude(s)
	 *        filter by name(s)
	 *
	 */
	private final void handleFeaturesRequest(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("received features request");

		AnnotatedSeqGroup genome = getGenome(request);
		if (genome == null) {

			return;
		}
		//    addDasHeaders(response);
		//String path_info = request.getPathInfo();
		String query = request.getQueryString();
		String xbase = getXmlBase(request);
		String output_format = default_feature_format;
		String query_type = null;
		SeqSpan overlap_span = null;
		SeqSpan inside_span = null;
		//SeqSpan contain_span = null;
		//SeqSpan identical_span = null;

		List<SeqSymmetry> result = null;
		MutableAnnotatedBioSeq outseq = null;

		if (query == null || query.length() == 0) {
			// no query string, so requesting _all_ features for a versioned source
			//    genometry server does not support this
			//    so leave result = null and null test below will trigger sending
			//    HTTP error message with status 413 "Request Entity Too Large"
		} else {  // request contains query string

			query = URLDecoder.decode(query);
			ArrayList<String> formats = new ArrayList<String>();
			ArrayList<String> types = new ArrayList<String>();
			ArrayList<String> segments = new ArrayList<String>();
			ArrayList<String> overlaps = new ArrayList<String>();
			ArrayList<String> insides = new ArrayList<String>();
			ArrayList<String> excludes = new ArrayList<String>();
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<String> coordinates = new ArrayList<String>();
			ArrayList<String> links = new ArrayList<String>();
			ArrayList<String> notes = new ArrayList<String>();
			Map<String,ArrayList<String>> props = new HashMap<String,ArrayList<String>>();

			// genometry server does not currently serve up features with PROPERTY, LINK, or NOTE element,
			//   so if any of these are encountered and the response is not an error for some other reason,
			//   the response should be a FEATURES doc with zero features.

			String[] query_array = query_splitter.split(query);
			//boolean has_segment = false;
			boolean known_query = true;
			for (int i = 0; i < query_array.length; i++) {
				String tagval = query_array[i];
				String[] tagval_array = tagval_splitter.split(tagval);
				String tag = tagval_array[0];
				String val = tagval_array[1];
				System.out.println("tag = " + tag + ", val = " + val);
				if (tag.equals("format")) {
					formats.add(val);
				} else if (tag.equals("type")) {
					types.add(val);
				} else if (tag.equals("segment")) {
					segments.add(val);
				} else if (tag.equals("overlaps")) {
					overlaps.add(val);
				} else if (tag.equals("inside")) {
					insides.add(val);
				} else if (tag.equals("excludes")) {
					excludes.add(val);
				} else if (tag.equals("name")) {
					names.add(val);
				} else if (tag.equals("coordinates")) {
					coordinates.add(val);
				} else if (tag.equals("link")) {
					links.add(val);
				} else if (tag.equals("note")) {
					notes.add(val);
				} else if (tag.startsWith("prop-")) {
					// extract prop's key from tag ('prop-key')
					// if already seen this key, get list from hash and add to it
					// if not already seen, create new list and add to hash with key prop-key
					String pkey = tag.substring(5);  // strip off "prop-" to get key
					ArrayList<String> vlist = props.get(pkey);
					if (vlist == null) {
						vlist = new ArrayList<String>();
						props.put(pkey, vlist);
					}
					vlist.add(val);
				} else {
					known_query = false;  // tag not recognized, so reject whole query
				}
			}
			if (formats.size() == 1) {
				output_format = formats.get(0);
			}

			if (!known_query) {
				// at least one query parameter was not recognized, throw bad request error
				result = null;
			} else if (formats.size() > 1) {
				// can only be zero or one format, otherwise it's a bad request
				result = null;
			} // the Genometry DAS/2 server does not return features with LINK, NOTE, or PROP elements,
			//    so if any of these are queried for the server can return a response with zero features
			//    in the appropriate format
			else if (links.size() > 0 ||
					notes.size() > 0 ||
					props.size() > 0) {
				result = new ArrayList<SeqSymmetry>();
					} /* support for single name, single format, no other filters */
			else if (names != null && names.size() == 1) {
				String name = names.get(0);
				result = ServerUtils.FindNameInGenome(name, genome);
			} // handling one type, one segment, one overlaps, optionally one inside
			else if (types.size() == 1 && // one and only one type
					segments.size() == 1 && // one and only one segment
					overlaps.size() <= 1 && // one and only one overlaps
					insides.size() <= 1 && // zere or one inside
					excludes.size() == 0 && // zero excludes
					names.size() == 0) {

				String seqid = segments.get(0);
				// using end of URI for internal seqid if segment is given as full URI (as it should according to DAS/2 spec)
				int sindex = seqid.lastIndexOf("/");
				if (sindex >= 0) {
					seqid = seqid.substring(sindex + 1);
				}
				String type_full_uri = types.get(0);
				query_type = getInternalType(type_full_uri, genome);

				System.out.println("   query type: " + query_type);

				String overlap = null;
				if (overlaps.size() == 1) {
					overlap = overlaps.get(0);
				}
				System.out.println("overlaps val = " + overlap);
				// if overlap string is null (no overlap parameter), then no overlap filter --
				///   which is the equivalent of any annot on seq passing overlap filter --
				//    which is same as an overlap filter with range = [0, seq.length]
				//    (therefore any annotation on the seq passes overlap filter)
				//     then want all getLocationSpan will return bounds of seq as overlap

				overlap_span = ServerUtils.getLocationSpan(seqid, overlap, genome);
				if (overlap_span != null) {
					System.out.println("   overlap_span: " + SeqUtils.spanToString(overlap_span));
					if (insides.size() == 1) {
						String inside = insides.get(0);
						inside_span = ServerUtils.getLocationSpan(seqid, inside, genome);
						if (inside_span != null) {
							System.out.println("   inside_span: " + SeqUtils.spanToString(inside_span));
						}
					}

					//	if (query_type.endsWith(".bar")) {
					Map<String,String> graph_name2dir = genome2graphdirs.get(genome);
					Map<String,String> graph_name2file = genome2graphfiles.get(genome);
					if ((graph_name2dir.get(query_type) != null) ||
							(graph_name2file.get(query_type) != null) ||
							// (query_type.startsWith("file:") && query_type.endsWith(".bar"))  ||
							(query_type.endsWith(".bar"))) {
						handleGraphRequest(output_registry, xbase, response, query_type, overlap_span);
						return;
							}

					MutableAnnotatedBioSeq oseq = overlap_span.getBioSeq();
					outseq = oseq;

					Timer timecheck = new Timer();
					timecheck.start();

					/** this is the main call to retrieve symmetries meeting query constraints */
					result = ServerUtils.getIntersectedSymmetries(overlap_span, query_type);


					if (result == null) {
						result = Collections.<SeqSymmetry>emptyList();
					}
					System.out.println("  overlapping annotations of type " + query_type + ": " + result.size());
					System.out.println("  time for range query: " + (timecheck.read()) / 1000f);

					if (inside_span != null) {
						result = ServerUtils.SpecifiedInsideSpan(inside_span, oseq, result, query_type);
					}
					}
				} else {
					// any query combination not recognized above may  be correct based on DAS/2 spec
					//    but is not currently supported, so leave result = null and and null test below will trigger sending
					//    HTTP error message with status 413 "Request Entity Too Large"
					result = null;
					System.out.println("  ***** query combination not supported, throwing an error");
				}
					}
		Class writerclass = output_registry.get(output_format);
			OutputTheAnnotations(writerclass, output_format, response, result, outseq, query_type, xbase);

		}

		/**
		 *  1) looks for graph files in graph seq grouping directories (".graphs.seqs")
		 *  if not 1), then
		 *     2) looks for graph files as bar files sans seq grouping directories, but within data directory hierarchy
		 *     if not 2), then
		 *        3) tries to directly access file
		 */
		private final void handleGraphRequest(Map<String,Class> output_registry, String xbase, HttpServletResponse response,
				String type, SeqSpan span) {
			System.out.println("#### handling graph request");
			BioSeq seq = (BioSeq) span.getBioSeq();
			String seqid = seq.getID();
			AnnotatedSeqGroup genome = seq.getSeqGroup();
			System.out.println("#### type: " + type + ", genome: " + genome.getID() + ", span: " + SeqUtils.spanToString(span));
			// use bar parser to extract just the overlap slice from the graph
			String graph_name = type;   // for now using graph_name as graph type

			Map<String,String> graph_name2dir = genome2graphdirs.get(genome);
			Map<String,String> graph_name2file = genome2graphfiles.get(genome);

			String file_path = DetermineFilePath(graph_name2dir, graph_name2file, graph_name, seqid);
			System.out.println("####    file:  " + file_path);
			Class writerclass = output_registry.get("bar");
			OutputGraphSlice(writerclass, file_path, span, type, xbase, response);
		}

		private static final String DetermineFilePath(Map<String,String> graph_name2dir, Map<String,String> graph_name2file, String graph_name, String seqid) {
			// for now using graph_name as graph type
			boolean use_graph_dir = false;
			String file_path = graph_name2dir.get(graph_name);
			if (file_path != null) {
				use_graph_dir = true;
			}
			if (file_path == null) {
				file_path = graph_name2file.get(graph_name);
			}
			if (file_path == null) {
				file_path = graph_name;
			}
			if (use_graph_dir) {
				file_path += "/" + seqid + ".bar";
			}
			if (file_path.startsWith("file:")) {
				// if file_path is URI string, strip off "file:" prefix
				if (WINDOWS_OS_TEST) {
					file_path = "C:/data/transcriptome/database_test_Human_May_2004" + file_path.substring(5);
				} else {
					file_path = file_path.substring(5);
				}
			}
			return file_path;
		}

		private static final void OutputGraphSlice(
						Class writerclass, String file_path, SeqSpan span, String type, String xbase, HttpServletResponse response) {
			GraphSym graf = null;
			try {
				graf = BarParser.getSlice(file_path, gmodel, span);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (graf != null) {
				ArrayList<SeqSymmetry> gsyms = new ArrayList<SeqSymmetry>();
				gsyms.add(graf);
				System.out.println("#### returning graph slice in bar format");
				outputAnnotations(gsyms, span.getBioSeq(), type, xbase, response, writerclass, "bar");
			} else {
				// couldn't generate a GraphSym, so return an error?
				System.out.println("####### problem with retrieving graph slice ########");
				response.setStatus(response.SC_NOT_FOUND);
				try {
					PrintWriter pw = response.getWriter();
					pw.println("DAS/2 server could not find graph to return for type: " + type);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				System.out.println("set status to 404 not found");
			}
		}

	

		private static final void OutputTheAnnotations(
						Class writerclass,
						String output_format,
						HttpServletResponse response,
						List<SeqSymmetry> result,
						MutableAnnotatedBioSeq outseq,
						String query_type,
						String xbase) {
			try {
				Timer timecheck = new Timer();
				timecheck.start();
				System.out.println("return format: " + output_format);

				if (DEBUG) {
					response.setContentType("text/html");
					PrintWriter pw = response.getWriter();
					pw.println("overlapping annotations found: " + result.size());
				} else {
					if (result == null) {
						response.sendError(
								response.SC_REQUEST_ENTITY_TOO_LARGE,
								"Query could not be handled. " + LIMITED_FEATURE_QUERIES_EXPLANATION);
					} else {
						outputAnnotations(result, outseq, query_type, xbase, response, writerclass, output_format);
					}
					long tim = timecheck.read();
					System.out.println("  time for buffered output of results: " + tim / 1000f);
					timecheck.start();
					tim = timecheck.read();
					System.out.println("  time for closing output: " + tim / 1000f);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		
		private static final String getInternalType(String full_type_uri, AnnotatedSeqGroup genome) {
			String query_type = URLDecoder.decode(full_type_uri);
			// using end of URI for internal typeid if type is given as full URI
			//    (as it should according to DAS/2 spec)
			//    special-case exception is when need to know full URL for locating graph data,
			if (!(query_type.endsWith(".bar"))) {
				String gid = genome.getID();
				int gindex = query_type.indexOf(gid);
				if (gindex >= 0) {
					query_type = query_type.substring(gindex + gid.length() + 1);
				}
			}
			return query_type;
		}

	

		private static final boolean outputAnnotations(List<SeqSymmetry> syms, MutableAnnotatedBioSeq seq,
				String annot_type,
				String xbase, HttpServletResponse response,
				Class writerclass,
				String format) {
			boolean success = true;
			try {
				if (writerclass == null) {
					System.out.println("no AnnotationWriter found for format: " + format);
					response.setStatus(response.SC_BAD_REQUEST);
					success = false;
				} else {
					AnnotationWriter writer = (AnnotationWriter) writerclass.newInstance();
					String mime_type = writer.getMimeType();

					if (writer instanceof Das2FeatureSaxParser) {
						((Das2FeatureSaxParser) writer).setBaseURI(new URI(xbase));
					}
					response.setContentType(mime_type);

					System.out.println("return mime type: " + mime_type);
					OutputStream outstream = response.getOutputStream();
					try {
						success = writer.writeAnnotations(syms, seq, annot_type, outstream);
					} finally {
						GeneralUtils.safeClose(outstream);
					}
				}
			} catch (Exception ex) {
				System.err.println("problem in GenometryDas2Servlet.outputAnnotations():");
				ex.printStackTrace();
				success = false;
			}
			return success;
		}

		private static final void printXmlDeclaration(PrintWriter pw) {
			// medium declaration (version, encoding)
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			// long declaration (version, encoding, standalone)
			//  pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
			//    pw.println("<!DOCTYPE DAS2XML SYSTEM \"http://www.biodas.org/dtd/das2xml.dtd\">");
		}

		static final void setXmlBase(String xbase) {
			xml_base = xbase;
			String trimmed_xml_base;
			if (xml_base.endsWith("/")) {
				trimmed_xml_base = xml_base.substring(0, xml_base.length() - 1);
			} else {
				trimmed_xml_base = xml_base;
				xml_base += "/";
			}
			sources_query_no_slash = trimmed_xml_base.substring(trimmed_xml_base.lastIndexOf("/"));
			sources_query_with_slash = sources_query_no_slash + "/";
			System.out.println("*** xml_base: " + xml_base);
		}

		/** getXmlBase() should no longer depend on request, should always be set via setXmlBase()
		  when servlet starts up -- need to remove request arg soon
		  */
		private static final String getXmlBase(HttpServletRequest request) {
			if (xml_base != null) {
				return xml_base;
			} else {
				return request.getRequestURL().toString();
			}
		}	
	}