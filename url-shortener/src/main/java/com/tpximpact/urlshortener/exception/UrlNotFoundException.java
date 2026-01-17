package com.tpximpact.urlshortener.exception;


public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String alias) {
        super("URL not found for alias: " + alias);
    }

    public UrlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}