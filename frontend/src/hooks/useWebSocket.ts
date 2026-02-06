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
   message?: T;
   error?: string;
   send: (message: T) => void;
}

export function useWebSocket<T extends WSMessage = WSMessage>(
   roomId?: string,
   roomType?: string
): UseWebSocketReturn<T> {
   const [isConnected, setIsConnected] = useState<boolean>(false);
   const [connectionState, setConnectionState] = useState<ConnectionState>('disconnected');
   const [message, setMessage] = useState<T>();
   const [error, setError] = useState<string>();

   const accessToken = useSelector((state: RootState) => state.auth.user?.accessToken);

   const client = useRef<WebSocket | null>(null);

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

      const wsUrl = `${WEBSOCKET_HUB_SERVICE_URL_WS}/ws/${handlerUrl}?roomId=${roomId}&token=${accessToken}`;

      setConnectionState('connecting');
      setError("");

      const socket = new WebSocket(wsUrl);

      socket.onopen = () => {
         setIsConnected(true);
         setConnectionState("connected");
         setError("");
      };

      socket.onclose = () => {
         setIsConnected(false);
         setConnectionState("disconnected");
      };

      socket.onerror = (event) => {
         console.error("WebSocket error:", event);
         setConnectionState("error");
         setError("WebSocket connection error");
      }

      socket.onmessage = (event) => {
         try {
            const data: T = JSON.parse(event.data);
            setMessage(data);
         } catch (e) {
            console.error('Failed to parse message:', e);
            setError('Invalid message format');
         }
      };

      client.current = socket;

      return () => {
         if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
            socket.close();
         }
      };
   }, [accessToken, roomId, roomType]);

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

   return { isConnected, connectionState, message, error, send };
}
