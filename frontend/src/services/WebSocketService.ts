import type { WSMessage } from "../types/ws";
import { getAccessToken } from "../utils/TokenManager";

type MessageHandler<T extends WSMessage = WSMessage> = (message: T) => void;

class WebSocketService {
   private ws: WebSocket | null = null;
   private handlers: MessageHandler[] = [];

   constructor() { }

   connect(roomName?: string): void {
      if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
         console.log('[WS] Already connected or connecting');
         return;
      }

      const token = getAccessToken();

      const url = `ws://localhost:8081/ws/game?roomId=${roomName}&token=${token}`;

      this.ws = new WebSocket(url);

      this.ws.onopen = () => {
         console.log("Open connection");
      }

      this.ws.onmessage = (event) => {
         try {
            const data = JSON.parse(event.data);
            this.handlers.forEach((h) => h(data));
         } catch (err) {
            console.error('[WS] Invalid message', err);
         }
      };

      this.ws.onclose = (e) => {
         console.log('[WS] Disconnected', e);
         this.ws = null;
      };
   }

   disconnect(): void {
      if (this.ws) {
         console.log('[WS] Closing connection');
         this.ws.close();
         this.ws = null;
      }
   }

   send<T extends WSMessage>(msg: T): void {
      if (this.ws?.readyState === WebSocket.OPEN) {
         this.ws.send(JSON.stringify(msg));
      } else {
         console.warn('[WS] Cannot send, socket not open');
      }
   }

   subscribe<T extends WSMessage>(handler: MessageHandler<T>): () => void {
      this.handlers.push(handler as MessageHandler);
      return () => {
         this.handlers = this.handlers.filter((h) => h !== handler);
      };
   }

   isConnected(): boolean {
      return this.ws?.readyState === WebSocket.OPEN;
   }
}

export const wsService = new WebSocketService();