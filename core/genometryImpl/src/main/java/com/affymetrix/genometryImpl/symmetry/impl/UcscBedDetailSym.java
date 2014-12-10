package com.affymetrix.genometryImpl.symmetry.impl;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.symmetry.SupportsGeneName;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class UcscBedDetailSym extends UcscBedSym implements SupportsGeneName {

    private final String geneName;
    private String description;

    public UcscBedDetailSym(String type, BioSeq seq, int txMin, int txMax,
            String name, float score, boolean forward, int cdsMin, int cdsMax,
            int[] blockMins, int[] blockMaxs, String geneName, String description) {
        super(type, seq, txMin, txMax, name, score, forward, cdsMin, cdsMax, blockMins,
                blockMaxs);
        this.geneName = geneName;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getGeneName() {
        return geneName;
    }

    public Map<String, Object> cloneProperties() {
        Map<String, Object> tprops = super.cloneProperties();
        //tprops.put("gene name", geneName);
        tprops.put("title", geneName);
        tprops.put("description", description);
        return tprops;
    }

    public Object getProperty(String key) {
        if (key.equals("gene name")) {
            return geneName;
        } else if (key.equals("description")) {
            return description;
        } else {
            return super.getProperty(key);
        }
    }

    public void outputBedDetailFormat(DataOutputStream out) throws IOException {
        outputBedFormat(out);
        out.write('\t');
        out.write(geneName.getBytes());
        out.write('\t');
        out.write(description.getBytes());
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            outputBedDetailFormat(new DataOutputStream(baos));
        } catch (IOException x) {
            return x.getMessage();
        }
        return baos.toString();
    }
}
