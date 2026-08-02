package com.savemyseat.hold;

public class InsufficientCapacityException extends RuntimeException {
    public InsufficientCapacityException(String message) {
        super(message);
    }
}
