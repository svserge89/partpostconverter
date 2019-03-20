package com.github.svserge89.partpostconverter.exception;

public class FileCorrectorException extends RuntimeException {
    public FileCorrectorException(String message) {
        super(message);
    }

    public FileCorrectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
