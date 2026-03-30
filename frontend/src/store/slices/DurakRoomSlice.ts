import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { PlayerBet, Room, RoomStatus, RoomType } from "../../models/Room";
import { extractErrorResponse, extractErrorResponseMessage, type ErrorResponse } from "../../helpers/ApiErrorHelper";
import { DurakRoomApi, RoomAPI } from "../../api/WsHubApi";
import type { RootState } from "../store";

export const DURAK_OPERATION_KEYS = {
    GET_ROOM: "getRoom",
    GET_ROOM_STATUS: "getRoomStatus",
    GET_PLAYERS: "getPlayers",
    GET_READY_PLAYERS: "getReadyPlayers",
    GET_PLAYERS_BETS: "getPlayersBets",
} as const;

export interface DurakRoomState {
    room?: Room;
    roomStatus?: RoomStatus;
    players?: Record<string, string>;
    readyPlayersCount?: number;
    totalPlayersCount?: number;
    playerBets?: PlayerBet[];
    playerBetMap?: Record<string, number>;
    errors: Record<string, string | null>;
}

// ── Thunks ──

export const getRoomById = createAsyncThunk<Room, { roomId: string }, { rejectValue: ErrorResponse }>(
    "durakRoom/getRoom",
    async ({ roomId }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getRoomById(roomId);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponse(err, "Failed to fetch room"));
        }
    }
);

export const getRoomStatus = createAsyncThunk<RoomStatus, { roomId: string; roomType: RoomType }, { rejectValue: string }>(
    "durakRoom/getRoomStatus",
    async ({ roomId, roomType }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getRoomStatus(roomId, roomType);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch room status"));
        }
    }
);

export const getUsernamesInRoom = createAsyncThunk<
    Record<string, string>,
    { roomId: string; roomType: RoomType },
    { rejectValue: string }
>(
    "durakRoom/getUsernamesInRoom",
    async ({ roomId, roomType }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getUsernamesInRoom(roomId, roomType);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch usernames"));
        }
    }
);

export const getReadyPlayers = createAsyncThunk<
    number,
    { roomId: string; roomType: RoomType },
    { rejectValue: string }
>(
    "durakRoom/getReadyPlayers",
    async ({ roomId, roomType }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getReadyPlayers(roomId, roomType);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch ready players"));
        }
    }
);

export const getPlayersBets = createAsyncThunk<PlayerBet[], { roomId: string }, { rejectValue: string }>(
    "durakRoom/getPlayersBets",
    async ({ roomId }, { rejectWithValue }) => {
        try {
            const response = await DurakRoomApi.getPlayersBets(roomId);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch player bets"));
        }
    }
);

export const syncRoomState = createAsyncThunk<void, { roomId: string; roomType: RoomType }, { state: RootState }>(
    "durakRoom/syncRoomState",
    async ({ roomId, roomType }, { dispatch }) => {
        await dispatch(getUsernamesInRoom({ roomId, roomType })).unwrap();
        await dispatch(getReadyPlayers({ roomId, roomType })).unwrap();
        await dispatch(getPlayersBets({ roomId })).unwrap();
    }
);

export const syncReadiness = createAsyncThunk<void, { roomId: string; roomType: RoomType }>(
    "durakRoom/syncReadiness",
    async ({ roomId, roomType }, { dispatch }) => {
        await dispatch(getReadyPlayers({ roomId, roomType })).unwrap();
        await dispatch(getPlayersBets({ roomId })).unwrap();
    }
);

// ── Slice ──

const initialState: DurakRoomState = {
    room: undefined,
    roomStatus: undefined,
    players: undefined,
    readyPlayersCount: undefined,
    totalPlayersCount: undefined,
    playerBets: undefined,
    playerBetMap: {},
    errors: {},
};

const durakRoomSlice = createSlice({
    name: "durakRoom",
    initialState,
    reducers: {
        clearError: (state, action: PayloadAction<string>) => {
            state.errors[action.payload] = null;
        },
        clearAllErrors: (state) => {
            state.errors = {};
        },
    },
    extraReducers: (builder) => {
        builder
            /* === Get Room === */
            .addCase(getRoomById.pending, (state) => {
                state.errors[DURAK_OPERATION_KEYS.GET_ROOM] = null;
            })
            .addCase(getRoomById.fulfilled, (state, action) => {
                state.room = action.payload;
            })
            .addCase(getRoomById.rejected, (state, action) => {
                state.errors[DURAK_OPERATION_KEYS.GET_ROOM] = action.payload?.message ?? "Failed to fetch room";
            })

            /* === Room Status === */
            .addCase(getRoomStatus.pending, (state) => {
                state.errors[DURAK_OPERATION_KEYS.GET_ROOM_STATUS] = null;
            })
            .addCase(getRoomStatus.fulfilled, (state, action) => {
                state.roomStatus = action.payload;
            })
            .addCase(getRoomStatus.rejected, (state, action) => {
                state.errors[DURAK_OPERATION_KEYS.GET_ROOM_STATUS] = action.payload ?? "Failed to fetch room status";
            })

            /* === Players === */
            .addCase(getUsernamesInRoom.pending, (state) => {
                state.errors[DURAK_OPERATION_KEYS.GET_PLAYERS] = null;
            })
            .addCase(getUsernamesInRoom.fulfilled, (state, action) => {
                state.players = action.payload;
                state.totalPlayersCount = Object.keys(action.payload).length;
            })
            .addCase(getUsernamesInRoom.rejected, (state, action) => {
                state.errors[DURAK_OPERATION_KEYS.GET_PLAYERS] = action.payload ?? "Failed to fetch usernames";
            })

            /* === Ready Players === */
            .addCase(getReadyPlayers.pending, (state) => {
                state.errors[DURAK_OPERATION_KEYS.GET_READY_PLAYERS] = null;
            })
            .addCase(getReadyPlayers.fulfilled, (state, action) => {
                state.readyPlayersCount = action.payload;
            })
            .addCase(getReadyPlayers.rejected, (state, action) => {
                state.errors[DURAK_OPERATION_KEYS.GET_READY_PLAYERS] = action.payload ?? "Failed to fetch ready players";
            })

            /* === Player Bets === */
            .addCase(getPlayersBets.pending, (state) => {
                state.errors[DURAK_OPERATION_KEYS.GET_PLAYERS_BETS] = null;
            })
            .addCase(getPlayersBets.fulfilled, (state, action) => {
                state.playerBets = action.payload;
                const bets: Record<string, number> = {};
                action.payload.forEach(({ guid, bet }) => {
                    bets[guid] = bet;
                });
                state.playerBetMap = bets;
            })
            .addCase(getPlayersBets.rejected, (state, action) => {
                state.errors[DURAK_OPERATION_KEYS.GET_PLAYERS_BETS] = action.payload ?? "Failed to fetch player bets";
            });
    },
});

export const { clearError, clearAllErrors } = durakRoomSlice.actions;

export default durakRoomSlice.reducer;
