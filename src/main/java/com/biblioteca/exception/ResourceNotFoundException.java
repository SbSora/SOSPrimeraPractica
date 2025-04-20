package com.biblioteca.exception;

// Exception: ResourceNotFoundException
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
