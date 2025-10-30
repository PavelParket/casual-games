import { getAccessToken } from "../utils/TokenManager";

export interface WSMessage {
   type: string;
   fromUserId?: string;
   toUserId?: string;
   roomName?: string;
}

export interface GameMessage extends WSMessage {
   board?: (string | null)[][];
   cell?: number;
   player?: string;
   nextPlayer?: string;
   playersSymbols?: Record<string, string>;
   players?: string[];
   winner?: string;
   message?: string;
}

type MessageHandler = (message: WSMessage | GameMessage) => void;

class WebSocketService {
   private ws: WebSocket | null = null;
   private messageHandlers: Map<string, Set<MessageHandler>> = new Map();
   private currentRoomId: string | null = null;

   connect(roomName: string, isGameRoom: boolean = false): Promise<void> {
      return new Promise((resolve, reject) => {
         if (this.ws?.readyState === WebSocket.OPEN) {
            console.log('WebSocket already connected');
            resolve();
            return;
         }

         this.currentRoomId = roomName;
         const token = getAccessToken();
         const endpoint = isGameRoom ? "/ws/game" : "/ws/room";
         const url = `ws://localhost:8081${endpoint}?token=${token}&roomId=${roomName}`;

         try {
            this.ws = new WebSocket(url);

            this.ws.onopen = () => {
               console.log('WebSocket connected to room:', roomName);
               resolve();
            };

            this.ws.onmessage = (event) => {
               try {
                  const message = JSON.parse(event.data);
                  this.handleMessage(message);
               } catch (error) {
                  console.error('Failed to parse WebSocket message:', error);
               }
            };

            this.ws.onerror = (error) => {
               console.error('WebSocket error:', error);
               reject(error);
            };

            this.ws.onclose = () => {
               console.log('WebSocket closed');
            };
         } catch (error) {
            console.error('Failed to create WebSocket connection:', error);
            reject(error);
         }
      });
   }

   disconnect(): void {
      if (this.ws) {
         this.ws.close(1000, "Client disconnect");
         this.ws = null;
      }
      this.currentRoomId = null;
   }

   send<T extends WSMessage | GameMessage>(message: T): void {
      if (this.ws?.readyState === WebSocket.OPEN) {
         this.ws.send(JSON.stringify(message));
      } else {
         console.warn('WebSocket not connected');
      }
   }

   subscribe(type: string, handler: MessageHandler): () => void {
      if (!this.messageHandlers.has(type)) {
         this.messageHandlers.set(type, new Set());
      }
      this.messageHandlers.get(type)!.add(handler);

      return () => {
         const handlers = this.messageHandlers.get(type);
         if (handlers) {
            handlers.delete(handler);
            if (handlers.size === 0) {
               this.messageHandlers.delete(type);
            }
         }
      };
   }

   private handleMessage(message: WSMessage | GameMessage): void {
      const handlers = this.messageHandlers.get(message.type);
      if (handlers) {
         handlers.forEach((handler) => handler(message));
      }
   }

   isConnected(): boolean {
      return this.ws?.readyState === WebSocket.OPEN;
   }

   getConnectionState(): number {
      return this.ws?.readyState ?? WebSocket.CLOSED;
   }
}

export const wsService = new WebSocketService();