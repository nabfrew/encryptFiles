package com.task.exceptions;

public class InvalidArgumentException extends Exception {
    private final String message;

    public InvalidArgumentException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
