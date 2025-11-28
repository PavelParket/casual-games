import { useCallback, useEffect, useRef, useState } from "react";
import type { WSMessage } from "../types/ws";
import { getAccessToken } from "../utils/TokenManager";
import type { LastRoom } from "../types/room";

export function useWebSocket<T extends WSMessage = WSMessage>(baseUrl: string, handlerUrl: string, roomName: string, roomType: string) {
   const [isConnected, setIsConnected] = useState<boolean>(false);
   const [message, setMessage] = useState<T>();

   const lastRoom: LastRoom = JSON.parse(localStorage.getItem("lastRoom")!);
   const actionRef = useRef<string>(localStorage.getItem("action") ?? "join");
   const roomNameRef = roomName ? roomName : lastRoom.name;
   const roomTypeRef = roomType ? roomType : lastRoom.type?.name;
   const handlerUrlRef = handlerUrl ? handlerUrl : lastRoom.type?.handlerUrl;

   const client = useRef<WebSocket | null>(null);

   useEffect(() => {
      const token = getAccessToken();

      const socket = new WebSocket(
         `${baseUrl}/${handlerUrlRef}?roomName=${roomNameRef}&roomType=${roomTypeRef}&action=${actionRef.current}&token=${token}`
      );

      socket.onopen = () => {
         setIsConnected(true);
         localStorage.removeItem("action");
      }
      socket.onclose = () => {
         setIsConnected(false);
         localStorage.removeItem("action");
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
   }, [baseUrl, handlerUrlRef, roomNameRef, roomTypeRef]);

   const send = useCallback((message: T) => {
      if (client.current?.readyState === WebSocket.OPEN) {
         client.current.send(JSON.stringify(message));
      } else {
         console.warn("Cannot send, socket not open");
      }
   }, []);

   return { isConnected, message, send };
}
