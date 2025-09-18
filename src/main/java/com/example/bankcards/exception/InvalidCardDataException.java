package com.example.bankcards.exception;

public class InvalidCardDataException extends RuntimeException {
    public InvalidCardDataException(String message) {
        super(message);
    }

    public InvalidCardDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
