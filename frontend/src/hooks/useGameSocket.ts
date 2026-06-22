import { useCallback, useEffect, useRef } from "react";
import type { ErrorWSMessage, WSMessage } from "../models/WsMessage";
import { errorCodeMessages, systemErrorCodes } from "../models/constants/ErrorCodeMessages";
import { useWebSocket, type ConnectionState } from "./useWebSocket";
import type { ToastVariant } from "../ui";
import { useSystemToastContext } from "./useSystemToastContext";

export interface UseGameSocketOptions<T extends WSMessage> {
    roomId?: string;
    roomType?: string;
    showGameToast: (message: string, variant: ToastVariant) => void;
    onGameMessage?: (data: T) => void;
    onError?: (errorCode: string) => void;
    onReconnected?: () => void;
    onDisplaced?: () => void;
    onConnectionLost?: () => void;
}

export interface UseGameSocketReturn<T extends WSMessage> {
    isConnected: boolean;
    connectionState: ConnectionState;
    send: (message: T) => boolean;
}

export function useGameSocket<T extends WSMessage = WSMessage>(
    options: UseGameSocketOptions<T>,
): UseGameSocketReturn<T> {
    const {
        roomId,
        roomType,
        showGameToast,
        onGameMessage,
        onError,
        onReconnected,
        onDisplaced,
        onConnectionLost,
    } = options;

    const { showSystemToast } = useSystemToastContext();

    const showGameToastRef = useRef(showGameToast);
    const onGameMessageRef = useRef(onGameMessage);
    const onErrorRef = useRef(onError);

    useEffect(() => {
        showGameToastRef.current = showGameToast;
    }, [showGameToast]);

    useEffect(() => {
        onGameMessageRef.current = onGameMessage;
    }, [onGameMessage]);

    useEffect(() => {
        onErrorRef.current = onError;
    }, [onError]);

    const handleMessage = useCallback((data: T) => {
        if (data.event !== "ERROR") {
            onGameMessageRef.current?.(data);
            return;
        }

        const errorMsg = data as unknown as ErrorWSMessage;
        const code = errorMsg.errorCode ?? "";
        const text = errorMsg.message ?? errorCodeMessages[code] ?? errorCodeMessages.DEFAULT;

        console.warn(`[GameSocket] ERROR: code=${code || "(none)"}, message=${errorMsg.message}`);

        if (systemErrorCodes.has(code)) {
            showSystemToast(text, "system-error");
        } else {
            showGameToastRef.current(text, "game-error");
        }

        onErrorRef.current?.(code);
    }, [showSystemToast]);

    const { isConnected, connectionState, send } = useWebSocket<T>({
        roomId,
        roomType,
        onMessage: handleMessage,
        onReconnected,
        onDisplaced,
        onConnectionLost,
    });

    return { isConnected, connectionState, send };
}
