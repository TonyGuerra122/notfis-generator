package com.tonyguerra.notfisgenerator;

import java.util.List;
import java.util.Map;

/**
 * identifier -> linhas -> params
 * Ex:
 * "000" -> [ [ {name,value}, {name,value} ], [ ... ] ]
 */
public final class NotfisPayload {
    private final Map<String, List<List<NotfisParam>>> records;

    public NotfisPayload(Map<String, List<List<NotfisParam>>> records) {
        this.records = records;
    }

    public Map<String, List<List<NotfisParam>>> getRecords() {
        return records;
    }
}
