import { useCallback, useEffect, useRef, useState } from "react";
import { wsService, type GameMessage, type WSMessage } from "../services/WebSocketService";

interface UseWebSocketOptions {
   roomName: string;
   isGameRoom?: boolean;
   onConnect?: () => void;
   onDisconnect?: () => void;
   onError?: (error: Event) => void;
   autoConnect?: boolean;
}

export function useWebSocket(options: UseWebSocketOptions) {
   const { roomName, isGameRoom = false, onConnect, onDisconnect, onError, autoConnect = true } = options;
   const [isConnected, setIsConnected] = useState(false);
   const unsubscribers = useRef<Array<() => void>>([]);

   const connect = useCallback(async () => {
      try {
         await wsService.connect(roomName, isGameRoom);
         setIsConnected(true);
         onConnect?.();
      } catch (error) {
         console.error('Failed to connect to WebSocket:', error);
         setIsConnected(false);
         onError?.(error as Event);
      }
   }, [roomName, isGameRoom, onConnect, onError]);

   const disconnect = useCallback(() => {
      wsService.disconnect();
      setIsConnected(false);
      onDisconnect?.();
   }, [onDisconnect]);

   const sendMessage = useCallback(<T extends WSMessage | GameMessage>(message: T) => {
      wsService.send(message);
   }, []);

   const subscribe = useCallback((
      type: string,
      handler: (message: WSMessage | GameMessage) => void
   ) => {
      const unsubscribe = wsService.subscribe(type, handler);
      unsubscribers.current.push(unsubscribe);
      return unsubscribe;
   }, []);

   useEffect(() => {
      if (autoConnect && roomName) {
         connect();
      }

      return () => {
         unsubscribers.current.forEach((unsubscribe) => unsubscribe());
         unsubscribers.current = [];
         disconnect();
      };
   }, [roomName, autoConnect, connect, disconnect]);

   return { isConnected, connect, disconnect, sendMessage, subscribe };
}