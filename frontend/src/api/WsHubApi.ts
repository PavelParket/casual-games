import { WEBSOCKET_HUB_SERVICE_URL } from "./ApiDictionary";
import type { PlayerResponse, PlayerBet, Room, RoomFilterRequest, RoomRequest, RoomResponseMap, RoomStatus, RoomType } from "../models/Room";
import type { HorseRaceGamePreset } from "../models/HorseRace";
import { client } from "./AxiosConfig";

export const RoomAPI = {
    getRooms: () => client.get<Room[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/all`),

    getTypes: () => client.get<RoomType[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/types`),

    searchRooms: (request: RoomFilterRequest) =>
        client.post<RoomResponseMap>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/search`, request),

    getPlayers: (roomId: string, roomType: RoomType) =>
        client.get<Record<string, PlayerResponse>>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/players/${roomId}/${roomType}`),

    getReadyPlayers: (roomId: string, roomType: RoomType) =>
        client.get(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/ready-count/${roomId}/${roomType}`),

    createRoom: (room: RoomRequest) => client.post<Room>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms`, room),

    getRoomById: (roomId: string) => client.get<Room>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/${roomId}`),

    getRoomStatus: (roomId: string, roomType: RoomType) =>
        client.get<RoomStatus>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/status/${roomId}/${roomType}`),
};

export const TicTacToeRoomApi = {
    getPlayersBets: (roomId: string) => client.get<PlayerBet[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/t-t-t/player-bets/${roomId}`),
};

export const HorseRaceRoomApi = {
    getPreset: (roomId: string) => client.get<HorseRaceGamePreset>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/horse-race/preset/${roomId}`),
};

export const DurakRoomApi = {
    getPlayersBets: (roomId: string) => client.get<PlayerBet[]>(`${WEBSOCKET_HUB_SERVICE_URL}/ws/rooms/durak/player-bets/${roomId}`),
};
