package com.tonyguerra.notfisgenerator;

public final class NotfisParam {
    private final String name;
    private final Object value;

    public NotfisParam(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
}
