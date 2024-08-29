package io.github.tonyguerra122.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public final class JSONUtils {
    public static List<JSONObject> jsonArrayToListJsonObject(JSONArray jsonArray) {
        final List<JSONObject> jsonObjects = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObjects.add(jsonArray.getJSONObject(i));
        }

        return jsonObjects;
    }
}
