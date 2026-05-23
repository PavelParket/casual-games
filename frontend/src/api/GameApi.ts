import { GAME_SERVICE_URL } from "./ApiDictionary";
import { client } from "./AxiosConfig";
import type { GameMatchRequestFilter, GameMatchResponse } from "../models/GameMatch";
import type { PageResponse } from "../models/Bank";

export const GameAPI = {
    getMatches: (userGuid: string, filter: GameMatchRequestFilter, page: number = 0, size: number = 4) =>
        client.post<PageResponse<GameMatchResponse>>(`${GAME_SERVICE_URL}/game/history/${userGuid}`, filter, {
            params: { page, size }
        }),
};
