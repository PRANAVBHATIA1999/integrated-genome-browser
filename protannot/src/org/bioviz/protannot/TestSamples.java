/*
 * A testing class to check whether ProtAnnot XML files can be read by
 * ProtAnnot
 */

package org.bioviz.protannot;

import com.affymetrix.genometryImpl.BioSeq;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class opens a directory and then attempts to read every file in the
 * directory that terminates with file extension suffix .paxml. Use this
 * class to test whether each .paxml file in the directory is readable by
 * the ProtAnnot Xml2GenometryParser class.
 * @author hvora1
 * @author loraine
 */
public class TestSamples {

    static public void main(String args[])
    {
		String dirpath = ".";
		if (args.length==1) {
			dirpath = args[0];
		}
        File dir = new File(dirpath);
        String[] files = dir.list();
        System.err.println("Total files " + files.length);
        for(String s : files)
        {
			if (!s.endsWith(".paxml")) {
				System.err.println(s + " doesn't end with .paxml.Skipping it.");
				continue;
			}
            if(testFile(dirpath+s))
                System.err.println(s + "read sucessfully.");
            else
                System.err.println("Error reading " + s);
        }
        
    }

	/**
	 * Test whether the given file can be read into ProtAnnot data models.
	 * @param filename	the name of the file to test
	 * @return	boolean	true if ProtAnnot can read the file, false if not
	 */
    static private boolean testFile(String filename)
    {
        BufferedInputStream bistr = null;
        try {
            bistr = new BufferedInputStream(new FileInputStream(filename));
			NormalizeXmlStrand nxs = new NormalizeXmlStrand(bistr);
			//NormalizeXmlStrand.outputXMLToScreen(nxs.doc);
            Xml2GenometryParser parser = new Xml2GenometryParser();
			try {
				BioSeq seq = parser.parse(nxs.doc);
				if (seq != null) {
					GenomeView gview = new GenomeView(GenomeView.COLORS.defaultColorList());
					gview.setBioSeq(seq, true);
					return true;
				}
			} catch (Exception ex) {
				Logger.getLogger(TestSamples.class.getName()).log(Level.SEVERE, null, ex);
			}
        } catch (FileNotFoundException ex) {
            System.err.println(filename + "File not found");
        }
		moveToFailedDir(filename);
        return false;
    }

	/**
	 * Moves give file to dir named failed.
	 * @param filename	File to be moved
	 * @return boolean true if file was move sucessfully, false if not.
	 */
	static private boolean moveToFailedDir(String filename){
		File file = new File(filename);
		File dir = new File(file.getParentFile().getPath() + "/failed");
		if(!dir.exists()){
			dir.mkdir();
		}
		File newFile = new File(dir.getPath() + "/" + file.getName());
		if(newFile.exists()){
			newFile.delete();
		}
		return file.renameTo(newFile);
	}
}
