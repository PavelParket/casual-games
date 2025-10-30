import axios from "axios";

const WS_HUB_URL = 'http://localhost:8081';

export interface GameRoom {
   name: string;
   id: string;
   playerCount: number;
   players: string[];
   playerDetails?: { userId: string; username: string }[];
   maxPlayers: number;
   createdAt: string;
}

export interface ChatRoom {
   name: string;
   id: string;
   userCount: number;
   users: string[];
   userDetails?: { userId: string; username: string }[];
   createdAt: string;
}

export const RoomAPI = {
   getAllRooms: () =>
      axios.get<{ chatRooms: string[]; gameRooms: string[]; totalRooms: number }>(
         `${WS_HUB_URL}/api/rooms`
      ),

   getGameRooms: () =>
      axios.get<GameRoom[]>(`${WS_HUB_URL}/api/rooms/game`),

   getGameRoomInfo: (roomName: string) =>
      axios.get<GameRoom>(`${WS_HUB_URL}/api/rooms/game/${roomName}`),

   getChatRooms: () =>
      axios.get<ChatRoom[]>(`${WS_HUB_URL}/api/rooms/chat`),

   getChatRoomInfo: (roomName: string) =>
      axios.get<ChatRoom>(`${WS_HUB_URL}/api/rooms/chat/${roomName}`),
};