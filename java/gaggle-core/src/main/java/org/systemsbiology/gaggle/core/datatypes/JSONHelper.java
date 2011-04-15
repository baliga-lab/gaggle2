package org.systemsbiology.gaggle.core.datatypes;

import net.sf.json.*;
import java.util.*;
import java.io.Serializable;

public class JSONHelper {
    private static final String KEY_GAGGLE_DATA = "gaggle-data";
    private static final String KEY_METADATA    = "metadata";
    private static final String KEY_NAME        = "name";
    private static final String KEY_SPECIES     = "species";

    private static final String KEY_NAMELIST    = "namelist";
    private static final String KEY_TUPLE       = "tuple";

    public GaggleData createFromJsonString(String json) {
        JSONObject obj = JSONObject.fromObject(json);
        return json2GaggleData(obj);
    }

    private GaggleData json2GaggleData(JSONObject jsonObj) {
        if (!isGaggleData(jsonObj)) {
            throw new IllegalArgumentException("JSON object does specify a Gaggle data structure");
        } else { 
            JSONObject jsonGaggleData = jsonObj.getJSONObject(KEY_GAGGLE_DATA);
            System.out.println("GAGGLEDATA: " + jsonGaggleData);
            return createGaggleData(jsonGaggleData);
        }
    }

    private GaggleData createGaggleData(JSONObject jsonGaggleData) {
        if (isGaggleTuple(jsonGaggleData)) {
            return extractGaggleTuple(jsonGaggleData);
        } else if (isNameList(jsonGaggleData)) {
            return extractNamelist(jsonGaggleData);
        }
        throw new IllegalArgumentException("unknown data type specified in JSON");
    }

    private boolean isGaggleData(JSONObject jsonObj) {
        return jsonObj.containsKey(KEY_GAGGLE_DATA);
    }

    private boolean isGaggleTuple(JSONObject jsonGaggleData) {
        return jsonGaggleData.containsKey(KEY_TUPLE);
    }
    private boolean isNameList(JSONObject jsonGaggleData) {
        return jsonGaggleData.containsKey(KEY_NAMELIST);
    }

    private GaggleTuple extractGaggleTuple(JSONObject jsonGaggleData) {
        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(extractName(jsonGaggleData));
        gaggleTuple.setSpecies(extractSpecies(jsonGaggleData));
        gaggleTuple.setMetadata(extractMetadata(jsonGaggleData));
        gaggleTuple.setData(createTuple(jsonGaggleData.getJSONObject(KEY_TUPLE)));
        return gaggleTuple;
    }

    private Tuple createTuple(JSONObject jsonTuple) {
        Tuple tuple = new Tuple(); // TODO: What about named tuples ?
        for (Object entry : jsonTuple.entrySet()) {
            Map.Entry<String, Object> mapEntry =
                (Map.Entry<String, Object>) entry;
            tuple.addSingle(createSingle(mapEntry.getKey(), mapEntry.getValue()));
        }
        return tuple;
    }

    private Single createSingle(String name, Object jsonValue) {
        Single single = new Single();
        single.setName(name);
        if (jsonValue instanceof JSONObject) {
            single.setValue(json2GaggleData((JSONObject) jsonValue));
        } else {
            single.setValue((Serializable) jsonValue);
        }
        return single;
    }

    private Namelist extractNamelist(JSONObject jsonGaggleData) {
        Namelist namelist = new Namelist();
        List<String> names = new ArrayList<String>();
        namelist.setName(extractName(jsonGaggleData));
        namelist.setSpecies(extractSpecies(jsonGaggleData));
        JSONArray nameArray = jsonGaggleData.getJSONArray(KEY_NAMELIST);
        for (int i = 0; i < nameArray.size(); i++) {
            names.add(nameArray.getString(i));
        }
        namelist.setNames(names.toArray(new String[0]));
        return namelist;
    }

    private String extractName(JSONObject jsonGaggleData) {
        return jsonGaggleData.getString(KEY_NAME);
    }

    private Tuple extractMetadata(JSONObject jsonGaggleData) {
        return null;
    }

    private String extractSpecies(JSONObject jsonGaggleData) {
        if (hasSpecies(jsonGaggleData)) {   
            return jsonGaggleData.getJSONObject(KEY_METADATA).getString(KEY_SPECIES);
        }
        return null;
    }
    private boolean hasMetadata(JSONObject jsonGaggleData) {
        return jsonGaggleData.containsKey(KEY_METADATA);
    }
    private JSONObject getJSONGaggleMetadata(JSONObject jsonGaggleData) {
        return jsonGaggleData.getJSONObject(KEY_METADATA);
    }

    private boolean hasSpecies(JSONObject jsonGaggleData) {
        return hasMetadata(jsonGaggleData) &&
            getJSONGaggleMetadata(jsonGaggleData).containsKey(KEY_SPECIES);
    }
}