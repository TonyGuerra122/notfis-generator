package io.github.tonyguerra122.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JSONUtils {
    public static List<JSONObject> jsonArrayToListJsonObject(JSONArray array) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                list.add((JSONObject) value);  // Adiciona apenas JSONObject
            } else if (value instanceof JSONArray) {
                // Itera sobre o JSONArray e processa os objetos internos
                JSONArray innerArray = (JSONArray) value;
                for (int j = 0; j < innerArray.length(); j++) {
                    Object innerValue = innerArray.get(j);
                    if (innerValue instanceof JSONObject) {
                        list.add((JSONObject) innerValue);  // Adiciona JSONObject do array aninhado
                    } else {
                        throw new JSONException("Esperado JSONObject dentro de JSONArray, mas encontrou: " + innerValue.getClass().getSimpleName());
                    }
                }
            } else {
                throw new JSONException("Esperado JSONObject ou JSONArray, mas encontrou: " + value.getClass().getSimpleName());
            }
        }
        return list;
    }
    

    public static List<Object> jsonArrayToList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                list.add(jsonArrayToList((JSONArray) value)); // Trata arrays aninhados
            } else if (value instanceof JSONObject) {
                list.add(value); // Adiciona o JSONObject diretamente
            }
        }
        return list;
    }

}
