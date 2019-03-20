package com.github.svserge89.partpostconverter.exception;

public class ArgumentResolverException extends RuntimeException {
    public ArgumentResolverException(String message) {
        super(message);
    }

    public ArgumentResolverException(String message, Throwable cause) {
        super(message, cause);
    }
}
