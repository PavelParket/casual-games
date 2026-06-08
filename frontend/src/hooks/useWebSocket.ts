import { useCallback, useEffect, useRef, useState } from "react";
import type { WSMessage } from "../models/WsMessage";
import { ROOM_TYPE_HANDLERS, type WsTicket, type WsTicketRequest } from "../models/Room";
import { WEBSOCKET_HUB_SERVICE_URL_WS } from "../api/ApiDictionary";
import { AuthAPI } from '../api/AuthApi';

export type ConnectionState =
    | "connecting"
    | "connected"
    | "reconnecting"
    | "closed_fatal"
    | "closed_normal";

export interface UseWebSocketOptions<T extends WSMessage> {
    roomId?: string;
    roomType?: string;
    onMessage?: (data: T) => void;
    onReconnected?: () => void;
    onDisplaced?: () => void;
    onConnectionLost?: () => void;
}

export interface UseWebSocketReturn<T extends WSMessage> {
    isConnected: boolean;
    connectionState: ConnectionState;
    message?: T;
    send: (message: T) => boolean;
}

const CLOSE_NORMAL = 1000;
const CLOSE_GOING_AWAY = 1001;
const CLOSE_PROTOCOL_VIOLATION = 1008;
const CLOSE_DISPLACED = 4001;

const RECONNECT_DELAY_MS = 1000;
const PING_INTERVAL_MS = 25_000;
const PONG_TIMEOUT_MS = 10_000;

type CloseAction = "fatal" | "retry_once" | "normal";

function classifyClose(code: number, wasEverOpen: boolean): CloseAction {
    if (code === 1006 && !wasEverOpen) {
        return "fatal";
    }

    switch (code) {
        case CLOSE_NORMAL:
        case CLOSE_GOING_AWAY:
            return "normal";
        case CLOSE_DISPLACED:
        case CLOSE_PROTOCOL_VIOLATION:
            return "fatal";
        default:
            return "retry_once";
    }
}

export function useWebSocket<T extends WSMessage = WSMessage>(
    options: UseWebSocketOptions<T>,
): UseWebSocketReturn<T> {

    const { roomId, roomType, onMessage, onReconnected, onDisplaced, onConnectionLost } = options;

    const [connectionState, setConnectionState] = useState<ConnectionState>("connecting");
    const [message, setMessage] = useState<T>();

    const client = useRef<WebSocket | null>(null);
    const isUnmounting = useRef<boolean>(false);

    const generationRef = useRef<number>(0);
    const reconnectUsedRef = useRef<boolean>(false);
    const reconnectTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const wasEverOpenRef = useRef<boolean>(false);

    const pingTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
    const pongTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const onMessageRef = useRef(onMessage);
    const onReconnectedRef = useRef(onReconnected);
    const onDisplacedRef = useRef(onDisplaced);
    const onConnectionLostRef = useRef(onConnectionLost);

    useEffect(() => { onMessageRef.current = onMessage; }, [onMessage]);
    useEffect(() => { onReconnectedRef.current = onReconnected; }, [onReconnected]);
    useEffect(() => { onDisplacedRef.current = onDisplaced; }, [onDisplaced]);
    useEffect(() => { onConnectionLostRef.current = onConnectionLost; }, [onConnectionLost]);

    const clearReconnectTimer = useCallback(() => {
        if (reconnectTimerRef.current) {
            clearTimeout(reconnectTimerRef.current);
            reconnectTimerRef.current = null;
        }
    }, []);

    const clearPongTimeout = useCallback(() => {
        if (pongTimeoutRef.current) {
            clearTimeout(pongTimeoutRef.current);
            pongTimeoutRef.current = null;
        }
    }, []);

    const clearPingTimer = useCallback(() => {
        if (pingTimerRef.current) {
            clearInterval(pingTimerRef.current);
            pingTimerRef.current = null;
        }
        clearPongTimeout();
    }, [clearPongTimeout]);

    const connect = useCallback(async () => {
        if (isUnmounting.current) {
            return;
        }

        const generation = ++generationRef.current;
        const isReconnect = reconnectUsedRef.current;

        setConnectionState(isReconnect ? "reconnecting" : "connecting");

        let ticket: WsTicket;

        const ticketRequest: WsTicketRequest = {
            roomId: roomId!,
        };

        try {
            const response = await AuthAPI.getWsTicket(ticketRequest);
            ticket = response.data;
        } catch (e) {
            if (generation !== generationRef.current) {
                return;
            }
            console.error("[WS] failed to fetch ticket", e);
            setConnectionState("closed_fatal");
            onConnectionLostRef.current?.();
            return;
        }

        if (generation !== generationRef.current) {
            return;
        }

        const handlerUrl = ROOM_TYPE_HANDLERS[roomType!];

        const wsBase = WEBSOCKET_HUB_SERVICE_URL_WS || `${location.protocol === "https:" ? "wss:" : "ws:"}//${location.host}`;
        const url = `${wsBase}/ws/${handlerUrl}?roomId=${roomId}&ticket=${ticket.ticketId}`;

        const socket = new WebSocket(url);
        client.current = socket;

        socket.onopen = () => {
            if (generation !== generationRef.current) {
                return;
            }

            const wasReconnect = reconnectUsedRef.current;

            wasEverOpenRef.current = true;
            reconnectUsedRef.current = false;
            setConnectionState("connected");

            startHeartbeat();

            if (wasReconnect) {
                onReconnectedRef.current?.();
            }
        };

        socket.onmessage = (event) => {
            if (generation !== generationRef.current) {
                return;
            }

            try {
                const data: T = JSON.parse(event.data);

                if (data.event === "PONG") {
                    clearPongTimeout();
                    return;
                }

                clearPongTimeout();

                onMessageRef.current?.(data);
                setMessage(data);
            } catch (e) {
                console.error("[WS] failed to parse message:", e);
            }
        };

        socket.onerror = () => {
            if (generation !== generationRef.current) {
                return;
            }
            console.error("[WS] socket error");
        };

        socket.onclose = (event) => {
            if (generation !== generationRef.current) {
                return;
            }
            if (isUnmounting.current) {
                return;
            }

            clearPingTimer();

            const action = classifyClose(event.code, wasEverOpenRef.current);

            if (action === "normal") {
                setConnectionState("closed_normal");
                return;
            }

            if (action === "fatal") {
                setConnectionState("closed_fatal");

                if (event.code === CLOSE_DISPLACED) {
                    console.warn("[WS] session displaced (4001)");
                    onDisplacedRef.current?.();
                } else {
                    console.warn(`[WS] fatal close: code=${event.code}`);
                    onConnectionLostRef.current?.();
                }
                return;
            }

            if (!reconnectUsedRef.current) {
                reconnectUsedRef.current = true;
                setConnectionState("reconnecting");

                reconnectTimerRef.current = setTimeout(() => {
                    connect();
                }, RECONNECT_DELAY_MS);
            } else {
                console.warn("[WS] reconnect exhausted — connection lost");
                setConnectionState("closed_normal");
                onConnectionLostRef.current?.();
            }
        };

        function startHeartbeat() {
            clearPingTimer();

            pingTimerRef.current = setInterval(() => {
                sendPing();
            }, PING_INTERVAL_MS);
        }

        function sendPing() {
            const socket = client.current;
            if (!socket || socket.readyState !== WebSocket.OPEN) {
                return;
            }

            const ping: WSMessage = {
                type: "SYSTEM",
                event: "PING",
                roomId: roomId ?? "",
            };

            try {
                socket.send(JSON.stringify(ping));
            } catch (e) {
                console.warn("[WS] PING send failed", e);
                return;
            }

            clearPongTimeout();
            pongTimeoutRef.current = setTimeout(() => {
                console.warn(`[WS] PONG timeout (${PONG_TIMEOUT_MS}ms) — forcing close`);
                try {
                    socket.close();
                } catch (e) {
                    console.warn("[WS] force close failed", e);
                }
            }, PONG_TIMEOUT_MS);
        }
    }, [clearPingTimer, clearPongTimeout, roomId, roomType]);

    useEffect(() => {
        if (!roomId || !roomType) {
            console.warn(`[WS] missing params: roomId=${!!roomId} roomType=${!!roomType}`);
            setConnectionState("closed_fatal");
            return;
        }

        const handlerUrl = ROOM_TYPE_HANDLERS[roomType];
        if (!handlerUrl) {
            console.warn(`[WS] unknown room type: ${roomType}`);
            setConnectionState("closed_fatal");
            return;
        }

        isUnmounting.current = false;
        reconnectUsedRef.current = false;
        wasEverOpenRef.current = false;

        connect();

        const handleVisibilityChange = () => {
            if (document.visibilityState !== "visible") {
                return;
            }

            const socket = client.current;
            const isSocketDead =
                !socket ||
                socket.readyState === WebSocket.CLOSED ||
                socket.readyState === WebSocket.CLOSING;

            if (isSocketDead && !isUnmounting.current) {
                reconnectUsedRef.current = false;
                clearReconnectTimer();
                connect();
            }
        };

        document.addEventListener("visibilitychange", handleVisibilityChange);

        return () => {
            console.debug("[WS] cleanup");
            isUnmounting.current = true;
            document.removeEventListener("visibilitychange", handleVisibilityChange);
            clearReconnectTimer();
            clearPingTimer();

            generationRef.current += 1;

            const socket = client.current;
            if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
                socket.close();
            }
            client.current = null;
        };
    }, [roomId, roomType, connect, clearReconnectTimer, clearPingTimer]);

    const send = useCallback((message: T): boolean => {
        const socket = client.current;

        if (socket?.readyState !== WebSocket.OPEN) {
            console.warn(`[WS] send failed — not open (readyState=${socket?.readyState})`);
            return false;
        }

        try {
            socket.send(JSON.stringify(message));
            return true;
        } catch (e) {
            console.warn("[WS] send threw", e);
            return false;
        }
    }, []);

    return {
        isConnected: connectionState === "connected",
        connectionState,
        message,
        send,
    };
}
