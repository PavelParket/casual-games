import { useCallback, useEffect, useRef, useState } from "react";
import type { WSMessage } from "../types/ws";
import { WebSocketService } from "../services/WebSocketService";

export function useWebSocket<T extends WSMessage = WSMessage>(roomName?: string) {
   const [connected, setConnected] = useState(false);
   const wsClient = useRef<WebSocketService | null>(null);

   useEffect(() => {
      const client = new WebSocketService();
      wsClient.current = client;
      console.log("Executed");

      client.connect(roomName)
         .then(() => {
            setConnected(true);
            console.log("What about there: " + roomName);
         })
         .catch((e: unknown) => {
            console.log("WS connection failed: ", e);
            setConnected(false);
         });

      return () => {
         client.disconnect();
         wsClient.current = null;
         setConnected(false);
      };
   }, [roomName]);

   const send = useCallback((message: T) => {
      wsClient.current?.send(message);
   }, []);

   const subscribe = useCallback((handler: (msg: T) => void) => {
      return wsClient.current?.subscribe(handler);
   }, []);

   return { connected, send, subscribe };
}