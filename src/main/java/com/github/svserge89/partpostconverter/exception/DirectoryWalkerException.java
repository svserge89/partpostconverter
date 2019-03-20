package com.github.svserge89.partpostconverter.exception;

public class DirectoryWalkerException extends RuntimeException {
    public DirectoryWalkerException(String message) {
        super(message);
    }

    public DirectoryWalkerException(String message, Throwable cause) {
        super(message, cause);
    }
}
