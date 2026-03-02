import axios from "axios";
import { WEBSOCKET_HUB_SERVICE_URL } from "./ApiDictionary";
import type { PlayerBet, Room, RoomRequest, RoomType } from "../models/Room";
import type { HorseRaceGamePreset } from "../models/HorseRace";

export const RoomAPI = {
   getRooms: () => axios.get<Room[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/all`),

   getTypes: () => axios.get<RoomType[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/types`),

   getUsernamesInRoom: (roomId: string, roomType: RoomType) =>
      axios.get(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/players/${roomId}/${roomType}`),

   getReadyPlayers: (roomId: string, roomType: RoomType) =>
      axios.get(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/ready-count/${roomId}/${roomType}`),

   createRoom: (room: RoomRequest) => axios.post<Room>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms`, room),

   getRoomById: (roomId: string) => axios.get<Room>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/${roomId}`),
};

export const TicTacToeRoomApi = {
   getPlayersBets: (roomId: string) => axios.get<PlayerBet[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/t-t-t/player-bets/${roomId}`),
};

export const HorseRaceRoomApi = {
   getPreset: (roomId: string) => axios.get<HorseRaceGamePreset>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/horse-race/preset/${roomId}`),
};
