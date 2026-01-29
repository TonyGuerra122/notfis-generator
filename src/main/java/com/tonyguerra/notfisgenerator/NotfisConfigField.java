package com.tonyguerra.notfisgenerator;

public final class NotfisConfigField {
    private final String name;
    private final NotfisFieldType format;
    private final short position;
    private final short size;
    private final boolean mandatory;

    public NotfisConfigField(String name, NotfisFieldType format, short position, short size, boolean mandatory) {
        this.name = name;
        this.format = format;
        this.position = position;
        this.size = size;
        this.mandatory = mandatory;
    }

    public String getName() {
        return name;
    }

    public NotfisFieldType getFormat() {
        return format;
    }

    public short getPosition() {
        return position;
    }

    public short getSize() {
        return size;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
