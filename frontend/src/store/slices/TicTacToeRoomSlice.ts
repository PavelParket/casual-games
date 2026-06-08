import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { PlayerResponse, PlayerBet, Room, RoomStatus, RoomType } from "../../models/Room";
import { RoomAPI, TicTacToeRoomApi } from "../../api/WsHubApi";
import type { RootState } from "../store";
import { extractErrorResponse, extractErrorResponseMessage, type ErrorResponse } from "../../helpers/ApiErrorHelper";

export const TTT_OPERATION_KEYS = {
    GET_ROOM: "getRoom",
    GET_ROOM_STATUS: "getRoomStatus",
    GET_PLAYERS: "getPlayers",
    GET_READY_PLAYERS: "getReadyPlayers",
    GET_PLAYERS_BETS: "getPlayersBets",
} as const;

export interface TicTacToeRoomState {
    room?: Room;
    roomStatus?: RoomStatus;
    players?: Record<string, PlayerResponse>;
    readyPlayersCount?: number;
    totalPlayersCount?: number;
    playerBets?: PlayerBet[];
    playerBetMap?: Record<string, number>;
    errors: Record<string, string | null>;
}

// ------------------ Thunks ------------------

export const getRoomById = createAsyncThunk<Room, { roomId: string }, { rejectValue: ErrorResponse }>(
    "ticTacToeRoom/getRoom",
    async ({ roomId }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getRoomById(roomId);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponse(err, "Failed to fetch room"));
        }
    }
);

export const getRoomStatus = createAsyncThunk<RoomStatus, { roomId: string, roomType: RoomType }, { rejectValue: string }>(
    "ticTacToeRoom/getRoomStatus",
    async ({ roomId, roomType }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getRoomStatus(roomId, roomType);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch room status"));
        }
    }
);

export const getPlayers = createAsyncThunk<Record<string, PlayerResponse>, { roomId: string, roomType: RoomType }, { rejectValue: string }>(
    "ticTacToeRoom/getPlayers",
    async ({ roomId, roomType }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getPlayers(roomId, roomType);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch players"));
        }
    }
);

export const getReadyPlayers = createAsyncThunk<number, { roomId: string, roomType: RoomType }, { rejectValue: string }>(
    "ticTacToeRoom/getReadyPlayers",
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
    "ticTacToeRoom/getPlayersBets",
    async ({ roomId }, { rejectWithValue }) => {
        try {
            const response = await TicTacToeRoomApi.getPlayersBets(roomId);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch player bets"));
        }
    }
);

export const syncRoomState = createAsyncThunk<void, { roomId: string, roomType: RoomType }, { state: RootState }>(
    "ticTacToeRoom/syncRoomState",
    async ({ roomId, roomType }, { dispatch }) => {
        await dispatch(getPlayers({ roomId, roomType })).unwrap();
        await dispatch(getReadyPlayers({ roomId, roomType })).unwrap();
        await dispatch(getPlayersBets({ roomId })).unwrap();
    }
);

export const syncReadiness = createAsyncThunk<void, { roomId: string; roomType: RoomType }>(
    "ticTacToeRoom/syncReadiness",
    async ({ roomId, roomType }, { dispatch }) => {
        await dispatch(getReadyPlayers({ roomId, roomType })).unwrap();
        await dispatch(getPlayersBets({ roomId })).unwrap();
    }
);

// ------------------ Slice ------------------

const initialState: TicTacToeRoomState = {
    room: undefined,
    roomStatus: undefined,
    players: undefined,
    readyPlayersCount: undefined,
    totalPlayersCount: undefined,
    playerBets: undefined,
    playerBetMap: {},
    errors: {},
};

const ticTacToeRoomSlice = createSlice({
    name: "ticTacToeRoom",
    initialState,
    reducers: {
        clearError: (state, action: PayloadAction<string>) => {
            state.errors[action.payload] = null;
        },
        clearAllErrors: (state) => {
            state.errors = {};
        },
        clearTicTacToeRoomState: () => initialState,
    },
    extraReducers: (builder) => {
        builder

            /* === Get Room === */
            .addCase(getRoomById.pending, (state) => {
                state.errors[TTT_OPERATION_KEYS.GET_ROOM] = null;
            })
            .addCase(getRoomById.fulfilled, (state, action) => {
                state.room = action.payload;
            })
            .addCase(getRoomById.rejected, (state, action) => {
                state.errors[TTT_OPERATION_KEYS.GET_ROOM] = action.payload?.message ?? "Failed to fetch room";
            })

            .addCase(getRoomStatus.pending, (state) => {
                state.errors[TTT_OPERATION_KEYS.GET_ROOM_STATUS] = null;
            })
            .addCase(getRoomStatus.fulfilled, (state, action) => {
                state.roomStatus = action.payload;
            })
            .addCase(getRoomStatus.rejected, (state, action) => {
                state.errors[TTT_OPERATION_KEYS.GET_ROOM_STATUS] = action.payload ?? "Failed to fetch room status";
            })

            /* === Get Players === */
            .addCase(getPlayers.pending, (state) => {
                state.errors[TTT_OPERATION_KEYS.GET_PLAYERS] = null;
            })
            .addCase(getPlayers.fulfilled, (state, action) => {
                state.players = action.payload;
                state.totalPlayersCount = Object.keys(action.payload).length;
            })
            .addCase(getPlayers.rejected, (state, action) => {
                state.errors[TTT_OPERATION_KEYS.GET_PLAYERS] = action.payload ?? "Failed to fetch players";
            })

            /* === Get Ready Players === */
            .addCase(getReadyPlayers.pending, (state) => {
                state.errors[TTT_OPERATION_KEYS.GET_READY_PLAYERS] = null;
            })
            .addCase(getReadyPlayers.fulfilled, (state, action) => {
                state.readyPlayersCount = action.payload;
            })
            .addCase(getReadyPlayers.rejected, (state, action) => {
                state.errors[TTT_OPERATION_KEYS.GET_READY_PLAYERS] = action.payload ?? "Failed to fetch ready players";
            })

            /* === Get Players Bets === */
            .addCase(getPlayersBets.pending, (state) => {
                state.errors[TTT_OPERATION_KEYS.GET_PLAYERS_BETS] = null;
            })
            .addCase(getPlayersBets.fulfilled, (state, action) => {
                state.playerBets = action.payload;
                const bets: Record<string, number> = {};

                action.payload.forEach(({ guid, bet }) => {
                    const player = state.players?.[guid];
                    if (player != null) {
                        bets[player.username] = bet;
                    }
                });

                state.playerBetMap = bets;
            })
            .addCase(getPlayersBets.rejected, (state, action) => {
                state.errors[TTT_OPERATION_KEYS.GET_PLAYERS_BETS] = action.payload ?? "Failed to fetch player bets";
            });
    },
});

export const { clearError, clearAllErrors, clearTicTacToeRoomState } = ticTacToeRoomSlice.actions;

export default ticTacToeRoomSlice.reducer;
