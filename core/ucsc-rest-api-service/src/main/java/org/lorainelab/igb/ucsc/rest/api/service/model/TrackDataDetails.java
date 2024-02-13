package org.lorainelab.igb.ucsc.rest.api.service.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDataDetails<T> {
    private String downloadTime;
    private long downloadTimeStamp;
    private String genome;
    private String dataTime;
    private long dataTimeStamp;
    private String trackType;
    private String track;
    private String chrom;
    private int chromSize;
    private String bigDataUrl;
    private int start;
    private int end;
    private ArrayList<T> trackData;
    private int itemsReturned;
    public static final String GENE_PRED = "genePred";
    public static final String PSL = "psl";
    public static final List<String> BED_FORMATS = new ArrayList<>(Arrays.asList("bed", "bigbed", "beddetail"));

    public void setTrackData(String jsonString, String track, String trackType) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
        if (jsonObject.has(track) && jsonObject.get(track).isJsonArray()) {
            JsonArray trackDataJson = jsonObject.getAsJsonArray(track);
            Type type = getType(trackType);
            if (type != null) {
                if(BED_FORMATS.contains(trackType.toLowerCase())){
                    trackData = new ArrayList<>();
                    trackDataJson.forEach(trackDataJsonElement -> {
                        BedTrackTypeData trackDataElement = gson.fromJson(trackDataJsonElement.toString(), BedTrackTypeData.class);
                        trackDataElement.setProps(trackDataJsonElement.toString());
                        trackData.add((T) trackDataElement);
                    });
                }
                else
                    trackData = gson.fromJson(trackDataJson.toString(), type);
            }
        }
    }

    private static Type getType(String trackType) {
        Type type = null;
        if(trackType.equalsIgnoreCase(GENE_PRED)) {
            type = new TypeToken<ArrayList<GenePred>>() {
            }.getType();
        }
        else if(trackType.equalsIgnoreCase(PSL)){
            type = new TypeToken<ArrayList<Psl>>() {
            }.getType();
        }
        else if(BED_FORMATS.contains(trackType.toLowerCase())){
            type = new TypeToken<ArrayList<BedTrackTypeData>>() {
            }.getType();
        }
        return type;
    }
}