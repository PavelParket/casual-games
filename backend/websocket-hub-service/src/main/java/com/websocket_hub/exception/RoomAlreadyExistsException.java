package com.websocket_hub.exception;

public class RoomAlreadyExistsException extends RuntimeException {

    public RoomAlreadyExistsException(String roomName) {
        super("Room with name '" + roomName + "' already exists");
    }
}
