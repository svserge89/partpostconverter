package com.github.svserge89.partpostconverter.exception;

public class PropertyResolverException extends RuntimeException {
    public PropertyResolverException(String message) {
        super(message);
    }

    public PropertyResolverException(String message, Throwable cause) {
        super(message, cause);
    }
}
