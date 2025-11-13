import axios from "axios";

const WS_HUB_URL = 'http://localhost:8081/ws';

export const RoomAPI = {
   getAllRooms: () =>
      axios.get<string[]>(`${WS_HUB_URL}/rooms/all`),

   getPlayersInRoom: (roomName: string) =>
      axios.get<string[]>(`${WS_HUB_URL}/rooms/${roomName}/players`),

   getReadyPlayers: (roomName: string) =>
      axios.get<number>(`${WS_HUB_URL}/rooms/${roomName}/count`),
};