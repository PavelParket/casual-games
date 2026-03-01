package com.websocket_hub.domain.enums.events;

/**
 * <h2>
 * RoomEvent
 *
 * <h4>
 * Events only for testing room broadcast functionality and websocket connections.
 * Don't use it in production!
 *
 * <p>
 * Fields {@link #JOIN} and {@link #LEAVE} marks user joined or left a room
 */

@Deprecated
public enum RoomEvent implements EventType {

    JOIN,
    LEAVE;

    @Override
    public String join() {
        return "";
    }

    @Override
    public String leave() {
        return "";
    }
}
