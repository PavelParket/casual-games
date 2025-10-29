import { getAccessToken } from "../utils/tokenManager";

export enum MessageType {
   SYSTEM = "system",
   READY = "ready",
   START = "start",
   MOVE = "move",
   WINNER_X = "winner X",
   WINNER_O = "winner O",
   DRAW = "draw",
   JOIN = "joined",
   LEFT = "left",
}

export interface WSMessage {
   type: MessageType;
   fromUserId?: string;
   toUserId?: string;
   roomName?: string;
}

type MessageHandler = (message: WSMessage) => void;

class WebSocketService {
   private ws: WebSocket | null = null;
   private reconnectAttempts = 0;
   private maxReconnectAttempts = 5;
   private reconnectDelay = 3000;
   private reconnectTimeout: ReturnType<typeof setTimeout> | null = null;
   private messageHandlers: Map<string, Set<MessageHandler>> = new Map();
   private pendingMessages: WSMessage[] = [];
   private isIntentionallyClosed = false;
   private currentRoomId: string | null = null;
   private heartbeatInterval: ReturnType<typeof setInterval> | null = null;

   connect(roomName: string): Promise<void> {
      return new Promise((resolve, reject) => {
         if (this.ws?.readyState === WebSocket.OPEN) {
            console.log('WebSocket already connected');
            resolve();

            return;
         }

         this.currentRoomId = roomName;
         this.isIntentionallyClosed = false;

         const token = getAccessToken();
         const wsUrl = `ws://localhost:8081/ws?token=${token}&roomId=${roomName}`;

         try {
            this.ws = new WebSocket(wsUrl);

            this.ws.onopen = () => {
               console.log('WebSocket connected to room:', roomName);
               this.reconnectAttempts = 0;
               this.startHeartbeat();
               this.flushPendingMessages();
               resolve();
            };

            this.ws.onmessage = (event) => {
               try {
                  const message: WSMessage = JSON.parse(event.data);
                  this.handleMessage(message);
               } catch (error) {
                  console.error('Failed to parse WebSocket message:', error);
               }
            };

            this.ws.onclose = (event) => {
               console.log('WebSocket closed:', event.code, event.reason);
               this.stopHeartbeat();

               if (!this.isIntentionallyClosed && this.currentRoomId) {
                  this.attemptReconnect();
               }
            };
         } catch (error) {
            console.error('Failed to create WebSocket connection:', error);
            reject(error);
         }
      });
   }

   disconnect(): void {
      this.isIntentionallyClosed = true;
      this.currentRoomId = null;
      this.stopHeartbeat();

      if (this.reconnectTimeout) {
         clearTimeout(this.reconnectTimeout);
         this.reconnectTimeout = null;
      }

      if (this.ws) {
         this.ws.close(1000, "Client disconnect");
         this.ws = null;
      }

      this.reconnectAttempts = 0;
   }

   private attemptReconnect(): void {
      if (this.reconnectAttempts >= this.maxReconnectAttempts) {
         console.error('Max reconnect attempts reached');
         this.notifyConnectionLost();

         return;
      }

      this.reconnectAttempts++;
      const delay = this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts - 1);

      console.log(`Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

      this.reconnectTimeout = setTimeout(() => {
         if (this.currentRoomId && !this.isIntentionallyClosed) {
            this.connect(this.currentRoomId).catch((error) => {
               console.error('Reconnection failed:', error);
            });
         }
      }, delay);
   }

   private startHeartbeat(): void {
      this.stopHeartbeat();
      this.heartbeatInterval = setInterval(() => {
         if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({ type: "ping" }));
         }
      }, 30000);
   }

   private stopHeartbeat(): void {
      if (this.heartbeatInterval) {
         clearInterval(this.heartbeatInterval);
         this.heartbeatInterval = null;
      }
   }

   send(message: Omit<WSMessage, "fromUserId">, username: string): void {
      const fullMessage: WSMessage = {
         ...message,
         fromUserId: username,
      };

      if (this.ws?.readyState === WebSocket.OPEN) {
         this.ws.send(JSON.stringify(fullMessage));
      } else {
         console.warn('WebSocket not connected, queuing message');
         this.pendingMessages.push(fullMessage as WSMessage);
      }
   }

   private flushPendingMessages(): void {
      while (this.pendingMessages.length > 0 && this.ws?.readyState === WebSocket.OPEN) {
         const message = this.pendingMessages.shift();
         if (message) {
            this.ws.send(JSON.stringify(message));
         }
      }
   }

   subscribe(type: string, handler: MessageHandler): () => void {
      if (!this.messageHandlers.has(type)) {
         this.messageHandlers.set(type, new Set());
      }
      this.messageHandlers.get(type)!.add(handler as MessageHandler);

      // Return unsubscribe function
      return () => {
         const handlers = this.messageHandlers.get(type);
         if (handlers) {
            handlers.delete(handler as MessageHandler);
            if (handlers.size === 0) {
               this.messageHandlers.delete(type);
            }
         }
      };
   }

   private handleMessage(message: WSMessage): void {
      const handlers = this.messageHandlers.get(message.type);
      if (handlers) {
         handlers.forEach((handler) => handler(message));
      }

      // Global handlers
      const globalHandlers = this.messageHandlers.get('*');
      if (globalHandlers) {
         globalHandlers.forEach((handler) => handler(message));
      }
   }

   private notifyConnectionLost(): void {
      const handlers = this.messageHandlers.get('connection_lost');
      if (handlers) {
         handlers.forEach((handler) => handler({
            type: MessageType.SYSTEM,
            fromUserId: 'system',
            roomName: this.currentRoomId || '',
         }));
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