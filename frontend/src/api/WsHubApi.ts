import axios from "axios";
import type { RoomTypeInfo } from "../models/rooms";

const WS_HUB_URL = 'http://localhost:8081/ws';

export const RoomAPI = {
   getAllRooms: () =>
      axios.get(`${WS_HUB_URL}/rooms/all`),

   getTypes: () =>
      axios.get<RoomTypeInfo[]>(`${WS_HUB_URL}/rooms/types`),

   getPlayersInRoom: (roomName: string, roomType: string) =>
      axios.get(`${WS_HUB_URL}/rooms/${roomName}/players`, {
         params: { roomType }
      }),

   getReadyPlayers: (roomName: string, roomType: string) =>
      axios.get(`${WS_HUB_URL}/rooms/${roomName}/ready-count`, {
         params: { roomType }
      }),
};
