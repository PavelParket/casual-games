import { useCallback, useEffect, useState } from "react";
import type { WSMessage } from "../types/ws";
import { wsService } from "../services/WebSocketService";

export function useWebSocket<T extends WSMessage = WSMessage>(roomName?: string) {
   const [connected, setConnected] = useState(false);

   useEffect(() => {
      wsService.connect(roomName);

      const interval = setInterval(() => {
         if (wsService.isConnected()) setConnected(true);
         else setConnected(false);
      }, 500);

      return () => {
         clearInterval(interval);
         wsService.disconnect();
      };
   }, [roomName]);

   const send = useCallback((msg: T) => {
      wsService.send(msg);
   }, []);

   const subscribe = useCallback((handler: (msg: T) => void) => {
      return wsService.subscribe(handler);
   }, []);

   return { connected, send, subscribe };
}