/**
 *   Copyright (c) 2001-2006 Affymetrix, Inc.
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

package com.affymetrix.genometryImpl.parsers;

import com.affymetrix.genometryImpl.BioSeq;
import java.io.*;


import com.affymetrix.genometryImpl.util.Timer;
import com.affymetrix.genometryImpl.util.NibbleIterator;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.util.GeneralUtils;

public final class NibbleResiduesParser {

	/**
	 *  Parses an input stream.
	 *  The sequence will get added to the existing NibbleBioSeq found by
	 *  seq_group.getSeq(name), where the name comes from data in the file.
	 *  If such a NibbleBioSeq doesn't exist, it will be created, but if a
	 *  BioSeq does exist that is not of the type NibbleBioSeq, an exception
	 *  will be thrown.
	 */
	public static BioSeq parse(InputStream istr, AnnotatedSeqGroup seq_group) throws IOException {
			return parse(istr,seq_group,0,-1);
	}

	/*public static GeneralBioSeq readBinaryFile(String file_name) throws IOException {
		FileInputStream fis = null;
		GeneralBioSeq seq = null;
		try {
			File fil = new File(file_name);
			fis = new FileInputStream(fil);
			AnnotatedSeqGroup seq_group = new AnnotatedSeqGroup("Test Group");
			seq = parse(fis, seq_group);
		}
		finally {
			GeneralUtils.safeClose(fis);
		}
		return seq;
	}*/

	// Read BNIB sequence from specified file.  Note that range can't be specified.
	public static byte[] ReadBNIB(File seqfile) throws FileNotFoundException, IOException {
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(seqfile));
			int filesize = (int) seqfile.length();
			byte[] buf = new byte[filesize];
			dis.readFully(buf);
			return buf;
		}
		finally {
			GeneralUtils.safeClose(dis);
		}
	}

	public static BioSeq parse(InputStream istr, AnnotatedSeqGroup seq_group, int start, int end) throws IOException
	{
		BioSeq result_seq = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		try {
			Timer tim = new Timer();
			tim.start();
			if (istr instanceof BufferedInputStream) {
				bis = (BufferedInputStream)istr;
			}
			else {
				bis = new BufferedInputStream(istr);
			}
			dis = new DataInputStream(bis);
			String name = dis.readUTF();
			//      System.out.println("name: " + name);
			String version = dis.readUTF();
			//      System.out.println("version: " + version);
			int total_residues = dis.readInt();

			start = start < 0 ? 0 : start;
			end = end > total_residues ? total_residues: end;

			int num_residues = end - start;
			num_residues = num_residues < 0 ? total_residues : num_residues;

			BioSeq existing_seq = seq_group.getSeq(name);
			if (existing_seq != null) {
				result_seq = existing_seq;
			} else {
				result_seq = seq_group.addSeq(name, num_residues);
			}

			System.out.println("NibbleBioSeq: " + result_seq);

			SetResiduesIterator(start, num_residues,dis, result_seq);

			float read_time = tim.read()/1000f;
			System.out.println("time to read in bnib residues file: " + read_time);
		}
		finally {
			GeneralUtils.safeClose(dis);
			GeneralUtils.safeClose(bis);
		}
		return result_seq;
	}

	private static void SetResiduesIterator(int start, int num_residues, DataInputStream dis, BioSeq result_seq) throws IOException {

		byte[] nibble_array = new byte[num_residues / 2 + num_residues % 2];
		dis.skipBytes(start/2);
		dis.readFully(nibble_array);
		NibbleIterator residues_provider = new NibbleIterator(nibble_array, num_residues);
		result_seq.setResiduesProvider(residues_provider);
	}

	/**
	 * Read BNIB sequence from specified file.  (begin,end] are in interbase coords.
	 * @param seqfile
	 * @param begin
	 * @param end
	 * @return
	 */
	
	public static void writeBinaryFile(String file_name, String seqname, String seqversion,
			String residues) throws IOException {

		// Note: We need to support case because many groups, including
		// UCSC use case to indicate when a base is in a repeat region
		//   AL - 10/1/08
		//
		// binary DNA residues format
		// header:
		//   UTF8-encoded sequence name
		//   UTF8-encoded sequence version
		//   length of sequence as 4-byte int
		//   residues encoded as "nibbles" (4 bits per residue ==> 2 bases / byte)
		//      4-bit residue encoding maps to 16 possible IUPAC codes:
		//     [ A, C, G, T, N, M, R, W, S, Y, K, V, H, D, B, X ]

		FileOutputStream fos = null;
		DataOutputStream dos = null;
		BufferedOutputStream bos = null;

		try {
			File fil = new File(file_name);
			fos = new FileOutputStream(fil);
			bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			dos.writeUTF(seqname);
			dos.writeUTF(seqversion);
			dos.writeInt(residues.length());
			System.out.println("creating nibble array");
			//      byte[] nibble_array = NibbleBioSeq.stringToNibbles(residues, 0, residues.length());
			byte[] nibble_array = NibbleIterator.stringToNibbles(residues, 0, residues.length());

			System.out.println("done creating nibble array, now writing nibble array out");
			// hit problem with trying to output full nibble_array at once for large
			// (>100 Mb) sequences.  I would have thought the BufferedOutputStream would
			// handle this, but maybe it just passes through calls that write large amounts...
			//
			// anyway, trying to work around this by looping over the array and writing
			// smaller chunks
			int chunk_length = 65536;
			if (nibble_array.length > chunk_length) {
				int bytepos = 0;
				while (bytepos < (nibble_array.length - chunk_length)) {
					dos.write(nibble_array, bytepos, chunk_length);
					bytepos += chunk_length;
				}
				dos.write(nibble_array, bytepos, nibble_array.length - bytepos);
			}
			else {
				dos.write(nibble_array);
			}
			dos.flush();
			dos.close();
			System.out.println("done writing out nibble file");
		}
		finally {
			GeneralUtils.safeClose(dos);
			GeneralUtils.safeClose(bos);
			GeneralUtils.safeClose(fos);
		}
	}

	/** Reads a FASTA file and outputs a binary sequence file. 
	 *  @param args  sequence name, input file, output file, sequence version
	 */
	public static void main(String[] args) {
		try {
			String infile_name = null;
			String outfile_name = null;
			String seq_version = null;
			String seq_name = null;
			if (args.length >= 4) {
				seq_name = args[0];
				infile_name = args[1];
				outfile_name = args[2];
				seq_version = args[3];
			}
			else {
				System.err.println("Usage: java -cp <exe_filename> com.affymetrix.genometryImpl.parsers.NibbleResiduesParser [seq_name] [in_file] [out_file] [seq_version]");
				System.exit(1);
			}
			File fil = new File(infile_name);
			StringBuffer sb = new StringBuffer();
			InputStream isr = GeneralUtils.getInputStream(fil, sb);

			// if file is gzipped or zipped, then fil.length() will not really be the max_seq_length,
			//   but that's okay because the parse() method only uses it as a suggestion
			BioSeq seq = FastaParser.parse(isr, null, (int)(fil.length()));
			int seqlength = seq.getResidues().length();
			System.out.println("length: " + seqlength);

			writeBinaryFile(outfile_name, seq_name, seq_version, seq.getResidues());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static String getMimeType()  { return "binary/bnib"; }

}

