package com.affymetrix.genometryImpl.util;

import com.affymetrix.genometry.MutableAnnotatedBioSeq;
import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.UcscPslSym;
import com.affymetrix.genometryImpl.parsers.IndexWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jnicol
 */
public class IndexingUtils {
	private static final boolean DEBUG = false;
	private static int symsPerIndex = 10;	// symmetries per index.  Otherwise even the indexing is pretty large.
	public static class IndexedSyms {
		public File file;
		public int[] min;
		public int[] max;
		public long[] filePos;
		public String typeName;
		public IndexWriter iWriter;

		public IndexedSyms(int resultSize, File file, String typeName, IndexWriter iWriter) {
			min = new int[resultSize];
			max = new int[resultSize];
			filePos = new long[resultSize + 1];
			this.file = file;
			this.typeName = typeName;
			this.iWriter = iWriter;
		}
	}


	/**
	 * Create a file of annotations, and index its entries.
	 * @param syms -- a sorted list of annotations (on one chromosome)
	 * @param seq -- the chromosome
	 * @param iSyms
	 * @param fos
	 * @return - success or failure
	 */
	public static boolean writeIndexedAnnotations(
			List<SeqSymmetry> syms,
			MutableAnnotatedBioSeq seq,
			IndexedSyms iSyms,
			FileOutputStream fos) {
		if (DEBUG){
			System.out.println("in IndexingUtils.writeIndexedAnnotations()");
		}
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(fos);
			FileChannel fChannel = fos.getChannel();
			int index = 0;
			iSyms.filePos[index] = 0;
			IndexWriter iWriter = iSyms.iWriter;
			for (SeqSymmetry sym : syms) {
				iSyms.min[index] = iWriter.getMin(sym, seq);
				iSyms.max[index] = iWriter.getMax(sym, seq);
				iWriter.writeSymmetry(sym, seq, dos);
				index++;
				iSyms.filePos[index] = fChannel.position();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}

	/**
	 * Create a file of annotations, and index its entries.
	 * @param syms -- a sorted list of annotations (on one chromosome)
	 * @param seq -- the chromosome
	 * @param iSyms
	 * @param fos
	 * @return - success or failure
	 */
	public static boolean writeIndexedAnnotations(
			List<SeqSymmetry> syms,
			MutableAnnotatedBioSeq seq,
			IndexedSyms iSyms
			) {
		ObjectOutputStream oos = null;
		try {
			if (DEBUG) {
				System.out.println("in IndexingUtils.writeIndexedAnnotations()");
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			int index = 0;
			long currentFilePos = 0;
			IndexWriter iWriter = iSyms.iWriter;
			for (SeqSymmetry sym : syms) {
				iSyms.min[index] = iWriter.getMin(sym, seq);
				iSyms.max[index] = iWriter.getMax(sym, seq);
				oos.writeObject(sym);
				byte [] buf = baos.toByteArray();
				index++;
				currentFilePos += buf.length;
				baos.reset();
				iSyms.filePos[index] = currentFilePos;
			}
			return true;
		} catch (Exception ex) {
			Logger.getLogger(IndexingUtils.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				oos.close();
			} catch (IOException ex) {
				Logger.getLogger(IndexingUtils.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return false;
	}

	/**
	 * Writes out the indexes (for later server reboots).
	 * @param iSyms
	 * @param fos
	 * @return - success or failure
	 */
	public static boolean writeIndexes(
			IndexedSyms iSyms,
			FileOutputStream fos) {
		if (DEBUG){
			System.out.println("in IndexingUtils.writeIndexes()");
		}
		BufferedOutputStream bos = null;
		DataOutputStream dos = null;
		try {
			bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
			int indexSymsSize = iSyms.min.length;
			dos.writeInt(indexSymsSize);	// used to determine iSyms size.
			for (int i=0;i<indexSymsSize;i++) {
				if (i < iSyms.min.length) {
					dos.writeInt(iSyms.min[i]);
					dos.writeInt(iSyms.max[i]);
				}
				dos.writeLong(iSyms.filePos[i]);
			}
			dos.writeLong(iSyms.filePos[indexSymsSize]);
			dos.flush();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Reads the indexes from a file.
	 * @param indexesFile - file to read
	 * @param typeName - passed in to IndexedSyms
	 * @param iWriter - passed in to IndexedSyms
	 * @return indexedSyms data structure.
	 */
	public static IndexedSyms readIndexes(
			File indexesFile,
			File indexedAnnotationFile,
			String typeName,
			IndexWriter iWriter) {
		if (DEBUG){
			System.out.println("in IndexingUtils.readIndexes()");
		}
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		

		IndexedSyms iSyms = null;
		try {
			fis = new FileInputStream(indexesFile);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			
			int indexSymsSize = dis.readInt();	// determine number of rows.
			iSyms = new IndexedSyms(indexSymsSize, indexedAnnotationFile, typeName, iWriter);
			for (int i=0;i<indexSymsSize;i++) {
				iSyms.min[i] = dis.readInt();
				iSyms.max[i] = dis.readInt();
				iSyms.filePos[i] = dis.readLong();
			}
			iSyms.filePos[indexSymsSize] = dis.readLong();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return iSyms;
		} finally {
			GeneralUtils.safeClose(dis);
			GeneralUtils.safeClose(bis);
			GeneralUtils.safeClose(fis);
		}
		return iSyms;
	}


	/**
	 * Returns annotations for specific chromosome, sorted by comparator.
	 * Class cannot be generic, since symmetries could be UcscPslSyms or SeqSymmetries.
	 * @param syms - original list of annotations
	 * @param seq - specific chromosome
	 * @param comp - comparator
	 * @return - sorted list of annotations
	 */
	@SuppressWarnings("unchecked")
	public static List<SeqSymmetry> getSortedAnnotationsForChrom(List syms, BioSeq seq, Comparator comp) {
		List<SeqSymmetry> results = new ArrayList<SeqSymmetry>(10000);
		int symSize = syms.size();
		for (int i = 0; i < symSize; i++) {
			SeqSymmetry sym = (SeqSymmetry) syms.get(i);
			if (sym instanceof UcscPslSym) {
				// add the lines specifically with Target seq == seq.
				if (((UcscPslSym)sym).getTargetSeq() == seq) {
					results.add(sym);
				}
				continue;
			}
			// sym is instance of SeqSymmetry.
			if (sym.getSpan(seq) != null) {
				// add the lines specifically with seq.
				results.add(sym);
			}
		}

		Collections.sort(results, comp);

		return results;
	}

	/**
	 * Get "length" annotations from filePosStart
	 * @param fis
	 * @param filePosStart
	 * @param length
	 * @return
	 */
	public static byte[] getIndexedAnnotations(FileInputStream fis, long filePosStart, int length) {
		byte[] contentsOnly = null;
		try {
			FileChannel fc = fis.getChannel();
			MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, filePosStart, length);
			contentsOnly = new byte[length];
			mbb.get(contentsOnly);
		} catch (IOException ex) {
			Logger.getLogger(IndexingUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
		return contentsOnly;
	}

	/**
	 * Find the maximum overlap given a range.
	 * @param insideRange -- an array of length 2, with a start and end coordinate.
	 * @param outputRange -- an outputted array of length 2, with a start position (from min[] array) and an end position (from max[] array).
	 * @param min -- array of SORTED min points.
	 * @param max -- array of max points.
	 */
	public static void findMaxOverlap(int [] overlapRange, int [] outputRange, int [] min, int [] max) {
		int tempPos = findMinimaGreaterOrEqual(min, overlapRange[0]);
		outputRange[0] = tempPos;

		tempPos = findMaximaLessOrEqual(min, overlapRange[1]);
		outputRange[1] = tempPos;
	}


	/**
	 * Find minimum index of min[] array that is >= start range.
	 * @param min
	 * @param elt
	 * @return
	 */
	private static int findMinimaGreaterOrEqual(int[] min, int elt) {
		int tempPos = Arrays.binarySearch(min, elt);
		if (tempPos >= 0) {
			tempPos = backTrack(min, tempPos);
		} else {
			// This means the start element was not found in the array.  Translate back to "insertion point", which is:
			//the index of the first element greater than the key, or min.length, if all elements in the list are less than the specified key.
			tempPos = (-(tempPos + 1));
			// Don't go past array limit.
			tempPos = Math.min(min.length - 1, tempPos);
		}
		return tempPos;
	}

	/**
	 * Find maximum index of min[] array that is <= end range.
	 * @param min
	 * @param elt
	 * @return
	 */
	private static int findMaximaLessOrEqual(int[] min, int elt) {
		int tempPos = Arrays.binarySearch(min, elt);
		if (tempPos >= 0) {
			tempPos = forwardtrack(min, tempPos);
		} else {
			// This means the end element was not found in the array.  Translate back to "insertion point", which is:
			//the index of the first element greater than the key, or min.length, if all elements in the list are less than the specified key.
			tempPos = (-(tempPos + 1));
			// But here, we want to backtrack to the element less than the key.
			if (tempPos > 0) {
				tempPos--;
				tempPos = backTrack(min, tempPos);
			}
			// Don't go past array limit
			tempPos = Math.min(min.length - 1, tempPos);
		}
		return tempPos;
	}

	/**
	 * backtrack if necessary
	 * (since binarySearch is not guaranteed to return lowest index of equal elements)
	 * @param arr
	 * @param pos
	 * @return lowest index of equal elements
	 */
	public static int backTrack(int[] arr, int pos) {
		while (pos > 0) {
			if (arr[pos - 1] == arr[pos]) {
				pos--;
			} else {
				break;
			}
		}
		return pos;
	}

	/**
	 * forward-track if necessary
	 * (since binarySearch is not guaranteed to return highest index of equal elements)
	 * @param arr
	 * @param pos
	 * @return highest index of equal elements
	 */
	public static int forwardtrack(int[] arr, int pos) {
		while (pos < arr.length - 1) {
			if (arr[pos + 1] == arr[pos]) {
				pos++;
			} else {
				break;
			}
		}
		return pos;
	}

}
