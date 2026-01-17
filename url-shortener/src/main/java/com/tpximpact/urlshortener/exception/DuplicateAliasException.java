package com.tpximpact.urlshortener.exception;

public class DuplicateAliasException extends RuntimeException {

    public DuplicateAliasException(String alias) {
        super("Alias already exists: " + alias);
    }

    public DuplicateAliasException(String message, Throwable cause) {
        super(message, cause);
    }
}