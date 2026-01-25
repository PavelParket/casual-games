import axios from "axios";
import { WEBSOCKET_HUB_SERVICE_URL } from "./ApiDictionary";
import type { Room, RoomRequest, RoomType } from "../models/Room";

export const RoomAPI = {
   getRooms: () => axios.get<Room[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/all`),

   getTypes: () => axios.get<RoomType[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/types`),

   getPlayersInRoom: (roomId: string, roomType: RoomType) =>
      axios.get(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/${roomId}/players`, {
         params: { roomType }
      }),

   getReadyPlayers: (roomId: string, roomType: RoomType) =>
      axios.get(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/${roomId}/ready-count`, {
         params: { roomType }
      }),

   createRoom: (room: RoomRequest) => axios.post<Room>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms`, room),
};
