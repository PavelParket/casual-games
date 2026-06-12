import { GAME_SERVICE_URL } from "./ApiDictionary";
import { client } from "./AxiosConfig";
import type { GameMatchRequestFilter, GameMatchResponse, GamePageResponse } from "../models/GameMatch";

export const GameAPI = {
    getMatches: (userGuid: string, filter: GameMatchRequestFilter, page: number = 0, size: number = 4) =>
        client.post<GamePageResponse<GameMatchResponse>>(`${GAME_SERVICE_URL}/game/history/${userGuid}`, filter, {
            params: { page, size, sort: "created_at,desc" }
        }),
};
