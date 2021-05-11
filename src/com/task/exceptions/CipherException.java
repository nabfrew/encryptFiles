package com.task.exceptions;

/**
 * Simple wrapper to avoid clutter of various cipher-related exceptions.
 */
public class CipherException extends Exception {

    final String message;

    public CipherException(Exception ex) {
        message = ex.getClass() + "\n" + ex.getMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }
}