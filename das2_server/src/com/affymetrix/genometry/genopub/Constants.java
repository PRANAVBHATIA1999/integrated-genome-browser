package com.affymetrix.genometry.genopub;

public class Constants {
	public static final String GENOMETRY_MODE         = "genometry_mode";

	public static final String GENOMETRY_MODE_GENOPUB = "genopub";
	public static final String GENOMETRY_MODE_CLASSIC = "classic";

	public static final String GENOMETRY_SERVER_DIR_CLASSIC  = "genometry_server_dir";
	public static final String GENOMETRY_SERVER_DIR_GENOPUB  = "genometry_server_dir_genopub";
	public static final String FDT_DIR                       = "fdt_dir";
	public static final String FDT_DIR_FOR_GENOPUB           = "fdt_dir_genopub";
	public static final String FDT_CLIENT_CODEBASE           = "fdt_client_codebase";
	public static final String FDT_TASK_DIR                  = "fdt_task_dir";
	public static final String FDT_SERVER_NAME               = "fdt_server_name";

	public static final String SEQUENCE_DIR_PREFIX    = "SEQ";

	public static final int MAXIMUM_NUMBER_TEXT_FILE_LINES = 10000;
	
	public static final String UCSC_URL = "http://genome.ucsc.edu";
	public static final int DAYS_TO_KEEP_URL_LINKS= 7;
	public static final String UCSC_URL_LINK_DIR_NAME = "UCSCLinks";
	public static final String UCSC_EXECUTABLE_DIR_NAME = "UCSCExecutables";
	public static final String UCSC_WIG_TO_BIG_WIG_NAME = "wigToBigWig";
	public static final String UCSC_BED_TO_BIG_BED_NAME = "bedToBigBed";

	static final String[] ANNOTATION_FILE_EXTENSIONS = new String[] 
	                                                              {
		".bar",
		".bam",
		".bai",
		".bed",
		".bgn",
		".bgr",
		".bps",
		".bp1",
		".bp2",
		".brs",
		".cyt",
		".ead",
		".gff", 
		".gtf",
		".psl",
		".useq",
		".bulkUpload",
		".bb", 
		".bw"
	                                                              };

	static final String[] FILE_EXTENSIONS_TO_CHECK_SIZE_BEFORE_UPLOADING = new String[] {
		".bed", 
		".bgn", 
		".gff", 
		".gtf", 
		".psl", 
	};

	/**xxx.ext for bigBed, bigWig, bam that can be accessed via http.*/
	static final String[] FILE_EXTENSIONS_FOR_UCSC_LINKS = new String[] {
		".bb",
		".bw",
		".bam",
		".bai"
	};

	static final String[] SEQUENCE_FILE_EXTENSIONS = new String[] 
	                                                            {
		".bnib", 
		".fasta",
	                                                            };



}
