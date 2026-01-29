package com.tonyguerra.notfisgenerator.errors;

public final class NotfisException extends Exception {
    public NotfisException(String message) {
        super(message);
    }

    public NotfisException(String message, Throwable cause) {
        super(message, cause);
    }
}
