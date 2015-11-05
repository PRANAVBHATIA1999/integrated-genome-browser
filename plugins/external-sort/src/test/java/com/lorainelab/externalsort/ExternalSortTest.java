/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lorainelab.externalsort;

import com.google.code.externalsorting.ExternalSort;
import com.lorainelab.externalsort.api.ComparatorMetadata;
import com.lorainelab.externalsort.api.ExternalSortConfiguration;
import com.lorainelab.externalsort.api.ExternalSortService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jeckstei
 */
public class ExternalSortTest {

    private static final String TEST_FILE1_TXT = "TAIR10.bed";

    private File file1;
    private List<File> fileList;

    private ExternalSortService exsort = new ExternalSort();

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        this.fileList = new ArrayList<File>(1);
        this.file1 = new File(this.getClass().getClassLoader()
                .getResource(TEST_FILE1_TXT).toURI());

        File tmpFile1 = new File(this.file1.getPath().toString() + ".tmp");

        copyFile(this.file1, tmpFile1);

        this.fileList.add(tmpFile1);
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        this.file1 = null;
        for (File f : this.fileList) {
            f.delete();
        }
        this.fileList.clear();
        this.fileList = null;
    }

    @Test
    public void simpleTest() throws IOException {

        ExternalSortConfiguration conf = new ExternalSortConfiguration();
        conf.setNumHeaderRows(2);
        conf.setMaxMemoryInBytes(1000);
        conf.setMaxTmpFiles(100);

        ComparatorMetadata comparatorMetadata = new ComparatorMetadata();

        //Define multisort
        //First sort
        comparatorMetadata.getPreparers().add(s -> {
            String[] sSplit = s.split("\\s+");
            return sSplit[0];
        });
        //Second sort
        comparatorMetadata.getPreparers().add(s -> {
            String[] sSplit = s.split("\\s+");
            return Long.parseLong(sSplit[1]);
        });
        //Third sort
        comparatorMetadata.getPreparers().add(s -> {
            String[] sSplit = s.split("\\s+");
            return Long.parseLong(sSplit[2]);
        });

        List<File> listOfFiles = exsort.sortInBatch(this.file1, comparatorMetadata, conf);
        Assert.assertEquals(1, listOfFiles.size());

        ArrayList<String> result = readLines(listOfFiles.get(0));
        result.forEach(r -> {
            System.out.println(r);
        });
    }

    public static ArrayList<String> readLines(File f) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(f));
        ArrayList<String> answer = new ArrayList<String>();
        String line;
        while ((line = r.readLine()) != null) {
            answer.add(line);
        }
        r.close();
        return answer;
    }

}
