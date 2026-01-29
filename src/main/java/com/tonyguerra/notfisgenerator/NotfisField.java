package com.tonyguerra.notfisgenerator;

import java.util.Objects;

public final class NotfisField {
    private final String name;
    private final NotfisFieldType format;
    private final short position;
    private final short size;
    private final boolean mandatory;
    private final Object value;

    // Construtor
    public NotfisField(String name, NotfisFieldType format, short position, short size, boolean mandatory,
            Object value) {
        this.name = name;
        this.format = format;
        this.position = position;
        this.size = size;
        this.mandatory = mandatory;
        this.value = value;
    }

    // Getters
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

    public Object getValue() {
        return value;
    }

    // toString
    @Override
    public String toString() {
        return "NotfisField{" +
                "name='" + name + '\'' +
                ", format=" + format +
                ", position=" + position +
                ", size=" + size +
                ", mandatory=" + mandatory +
                ", value=" + value +
                '}';
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NotfisField that = (NotfisField) o;
        return position == that.position &&
                size == that.size &&
                mandatory == that.mandatory &&
                Objects.equals(name, that.name) &&
                format == that.format &&
                Objects.equals(value, that.value);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(name, format, position, size, mandatory, value);
    }
}
