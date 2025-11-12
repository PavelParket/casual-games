import { useCallback, useEffect, useRef, useState } from "react";
import type { WSMessage } from "../types/ws";
import { getAccessToken } from "../utils/TokenManager";

export function useWebSocket<T extends WSMessage = WSMessage>(url: string, roomName: string) {
   const [isConnected, setIsConnected] = useState<boolean>(false);
   const [message, setMessage] = useState<T>();

   const client = useRef<WebSocket | null>(null);

   useEffect(() => {
      const token = getAccessToken();
      const socket = new WebSocket(`${url}?roomName=${roomName}&token=${token}`);

      socket.onopen = () => {
         setIsConnected(true);
      }
      socket.onclose = () => {
         setIsConnected(false);
      }

      socket.onmessage = (event) => {
         try {
            const data: T = JSON.parse(event.data);
            setMessage(data);
         } catch (e) {
            console.log("Invalid message", e);
         }
      };

      client.current = socket;

      return () => {
         socket.close();
      }
   }, [url, roomName]);

   const send = useCallback((message: T) => {
      if (client.current?.readyState === WebSocket.OPEN) {
         client.current.send(JSON.stringify(message));
      } else {
         console.warn("Cannot send, socket not open");
      }
   }, []);

   return { isConnected, message, send };
}