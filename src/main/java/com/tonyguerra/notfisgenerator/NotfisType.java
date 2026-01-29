package com.tonyguerra.notfisgenerator;

public enum NotfisType {
    VERSION31("notfis31.json"), VERSION50("notfis50.json");

    private final String configFIlename;

    NotfisType(String configFilename) {
        this.configFIlename = configFilename;
    }

    public String getConfigFilename() {
        return this.configFIlename;
    }

}
