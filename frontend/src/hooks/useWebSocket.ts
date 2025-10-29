import { useCallback, useEffect, useRef, useState } from "react";
import { wsService, type WSMessage } from "../services/WebSocketService";

interface UseWebSocketOptions {
   roomName: string;
   onConnect?: () => void;
   onDisconnect?: () => void;
   onError?: (error: Event) => void;
   autoConnect?: boolean;
}

export function useWebSocket(options: UseWebSocketOptions) {
   const { roomName, onConnect, onDisconnect, onError, autoConnect = true } = options;
   const [isConnected, setIsConnected] = useState(false);
   const [connectionState, setConnectionState] = useState<number>(WebSocket.CLOSED);
   const unsubscribers = useRef<Array<() => void>>([]);

   // Connect to WebSocket
   const connect = useCallback(async () => {
      try {
         await wsService.connect(roomName);
         setIsConnected(true);
         setConnectionState(WebSocket.OPEN);
         onConnect?.();
      } catch (error) {
         console.error('Failed to connect to WebSocket:', error);
         setIsConnected(false);
         onError?.(error as Event);
      }
   }, [roomName, onConnect, onError]);

   // Disconnect from WebSocket
   const disconnect = useCallback(() => {
      wsService.disconnect();
      setIsConnected(false);
      setConnectionState(WebSocket.CLOSED);
      onDisconnect?.();
   }, [onDisconnect]);

   // Send message
   const sendMessage = useCallback((message: Omit<WSMessage, 'fromUserId'>, username: string) => {
      wsService.send(message, username);
   }, []);

   // Subscribe to message type
   const subscribe = useCallback((type: string, handler: (message: WSMessage) => void) => {
      const unsubscribe = wsService.subscribe(type, handler);
      unsubscribers.current.push(unsubscribe);
      return unsubscribe;
   }, []);

   // Auto-connect on mount
   useEffect(() => {
      if (autoConnect && roomName) {
         connect();
      }

      return () => {
         // Cleanup: unsubscribe all and disconnect
         unsubscribers.current.forEach((unsubscribe) => unsubscribe());
         unsubscribers.current = [];
         disconnect();
      };
   }, [roomName, autoConnect, connect, disconnect]); // Intentionally limited deps

   // Monitor connection state
   useEffect(() => {
      const interval = setInterval(() => {
         const state = wsService.getConnectionState();
         setConnectionState(state);
         setIsConnected(state === WebSocket.OPEN);
      }, 1000);

      return () => clearInterval(interval);
   }, []);

   return {
      isConnected,
      connectionState,
      connect,
      disconnect,
      sendMessage,
      subscribe,
   };
}