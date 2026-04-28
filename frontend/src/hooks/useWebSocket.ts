import { useCallback, useEffect, useRef, useState } from "react";
import type { WSMessage } from "../models/WsMessage";
import { type RootState } from "../store/store";
import { useSelector } from "react-redux";
import { ROOM_TYPE_HANDLERS } from "../models/Room";
import { WEBSOCKET_HUB_SERVICE_URL_WS } from "../api/ApiDictionary";

export type ConnectionState = "connecting" | "connected" | "disconnected" | "error";

export interface UseWebSocketReturn<T extends WSMessage> {
   isConnected: boolean;
   connectionState: ConnectionState;
   reconnectAttempt: number;
   message?: T;
   error?: string;
   send: (message: T) => void;
}

export const MAX_RECONNECT_ATTEMPTS = 3;
const CLOSE_CODE_DISPLACED = 1008;

export function useWebSocket<T extends WSMessage = WSMessage>(
   roomId?: string,
   roomType?: string,
   onDisconnect?: () => void,
   onDisplaced?: () => void,
): UseWebSocketReturn<T> {
   const [isConnected, setIsConnected] = useState<boolean>(false);
   const [connectionState, setConnectionState] = useState<ConnectionState>('disconnected');
   const [reconnectAttempt, setReconnectAttempt] = useState<number>(0);
   const [message, setMessage] = useState<T>();
   const [error, setError] = useState<string>();

   const accessToken = useSelector((state: RootState) => state.auth.user?.accessToken);

   const client = useRef<WebSocket | null>(null);
   const isUnmounting = useRef<boolean>(false);
   const reconnectAttemptRef = useRef<number>(0);
   const reconnectTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
   const wsUrlRef = useRef<string | null>(null);

   const onDisconnectRef = useRef(onDisconnect);
   const onDisplacedRef = useRef(onDisplaced);

   useEffect(() => {
      onDisconnectRef.current = onDisconnect;
   }, [onDisconnect]);

   useEffect(() => {
      onDisplacedRef.current = onDisplaced;
   }, [onDisplaced]);

   const connect = useCallback((url: string) => {
      if (isUnmounting.current) {
         return;
      }

      setConnectionState("connecting");

      const socket = new WebSocket(url);

      socket.onopen = () => {
         reconnectAttemptRef.current = 0;
         setReconnectAttempt(0);
         setIsConnected(true);
         setConnectionState("connected");
         setError("");
      };

      socket.onclose = (event) => {
         setIsConnected(false);
         setConnectionState("disconnected");

         if (isUnmounting.current) {
            return;
         }

         const isNormalClose = event.code === 1000 || event.code === 1001;

         if (isNormalClose) {
            return;
         }

         if (event.code === CLOSE_CODE_DISPLACED) {
            onDisplacedRef.current?.();
            return;
         }

         if (reconnectAttemptRef.current < MAX_RECONNECT_ATTEMPTS) {
            const attempt = reconnectAttemptRef.current + 1;
            reconnectAttemptRef.current = attempt;
            setReconnectAttempt(attempt);

            const jitter = Math.random() * 1000;
            const delay = Math.pow(2, attempt - 1) * 1000 + jitter;

            reconnectTimerRef.current = setTimeout(() => {
               connect(url);
            }, delay);
         } else {
            reconnectAttemptRef.current = 0;
            setReconnectAttempt(0);
            onDisconnectRef.current?.();
         }
      };

      socket.onerror = (event) => {
         console.error("WebSocket error:", event);
         setConnectionState("error");
         setError("WebSocket connection error");
      };

      socket.onmessage = (event) => {
         try {
            const data: T = JSON.parse(event.data);
            setMessage(data);
         } catch (e) {
            console.error("Failed to parse message:", e);
            setError("Invalid message format");
         }
      };

      client.current = socket;
   }, []);

   useEffect(() => {
      if (!roomId || !roomType || !accessToken) {
         setConnectionState("error");
         setError("Missing required params!");
         return;
      }

      const handlerUrl = ROOM_TYPE_HANDLERS[roomType];
      if (!handlerUrl) {
         setConnectionState("error");
         setError(`Unknown room type: ${roomType}`);
         return;
      }

      const url = `${WEBSOCKET_HUB_SERVICE_URL_WS}/ws/${handlerUrl}?roomId=${roomId}&token=${accessToken}`;
      wsUrlRef.current = url;
      isUnmounting.current = false;
      reconnectAttemptRef.current = 0;
      setReconnectAttempt(0);
      setError("");

      connect(url);

      const handleVisibilityChange = () => {
         if (document.visibilityState === "visible") {
            const isSocketDead =
               !client.current ||
               client.current.readyState === WebSocket.CLOSED ||
               client.current.readyState === WebSocket.CLOSING;

            if (isSocketDead && !isUnmounting.current && wsUrlRef.current) {
               reconnectAttemptRef.current = 0;
               setReconnectAttempt(0);
               connect(wsUrlRef.current);
            }
         }
      };

      document.addEventListener("visibilitychange", handleVisibilityChange);

      return () => {
         isUnmounting.current = true;
         document.removeEventListener("visibilitychange", handleVisibilityChange);

         if (reconnectTimerRef.current) {
            clearTimeout(reconnectTimerRef.current);
            reconnectTimerRef.current = null;
         }

         const socket = client.current;
         if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
            socket.close();
         }
      };
   }, [accessToken, roomId, roomType, connect]);

   const send = useCallback((message: T) => {
      if (client.current?.readyState === WebSocket.OPEN) {
         client.current.send(JSON.stringify(message));
      } else {
         console.warn('Cannot send message: WebSocket is not open', {
            readyState: client.current?.readyState,
            message
         });
      }
   }, []);

   return { isConnected, connectionState, reconnectAttempt, message, error, send };
}
