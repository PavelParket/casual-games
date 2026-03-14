import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { PlayerBet, Room, RoomStatus, RoomType } from "../../models/Room";
import { RoomAPI, TicTacToeRoomApi } from "../../api/WsHubApi";
import type { RootState } from "../store";
import { extractErrorResponse, extractErrorResponseMessage, type ErrorResponse } from "../../helpers/ApiErrorHelper";

export const TTT_OPERATION_REYS = {
    GET_ROOM: "getRoom",
    GET_ROOM_STATUS: "getRoomStatus",
    GET_PLAYERS: "getPlayers",
    GET_READY_PLAYERS: "getReadyPlayers",
    GET_PLAYERS_BETS: "getPlayersBets",
} as const;

export interface TicTacToeRoomState {
    room?: Room;
    roomStatus?: RoomStatus;
    players?: Record<string, string>;
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

export const getUsernamesInRoom = createAsyncThunk<Record<string, string>, { roomId: string, roomType: RoomType }, { rejectValue: string }>(
    "ticTacToeRoom/getUsernamesInRoom",
    async ({ roomId, roomType }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getUsernamesInRoom(roomId, roomType);
            return response.data;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch usernames"));
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
        await dispatch(getUsernamesInRoom({ roomId, roomType })).unwrap();
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
    },
    extraReducers: (builder) => {
        builder

            /* === Get Room === */
            .addCase(getRoomById.pending, (state) => {
                state.errors[TTT_OPERATION_REYS.GET_ROOM] = null;
            })
            .addCase(getRoomById.fulfilled, (state, action) => {
                state.room = action.payload;
            })
            .addCase(getRoomById.rejected, (state, action) => {
                state.errors[TTT_OPERATION_REYS.GET_ROOM] = action.payload?.message ?? "Failed to fetch room";
            })

            .addCase(getRoomStatus.pending, (state) => {
                state.errors[TTT_OPERATION_REYS.GET_ROOM_STATUS] = null;
            })
            .addCase(getRoomStatus.fulfilled, (state, action) => {
                state.roomStatus = action.payload;
            })
            .addCase(getRoomStatus.rejected, (state, action) => {
                state.errors[TTT_OPERATION_REYS.GET_ROOM_STATUS] = action.payload ?? "Failed to fetch room status";
            })

            /* === Get Players === */
            .addCase(getUsernamesInRoom.pending, (state) => {
                state.errors[TTT_OPERATION_REYS.GET_PLAYERS] = null;
            })
            .addCase(getUsernamesInRoom.fulfilled, (state, action) => {
                state.players = action.payload;
                state.totalPlayersCount = Object.keys(action.payload).length;
            })
            .addCase(getUsernamesInRoom.rejected, (state, action) => {
                state.errors[TTT_OPERATION_REYS.GET_PLAYERS] = action.payload ?? "Failed to fetch usernames";
            })

            /* === Get Ready Players === */
            .addCase(getReadyPlayers.pending, (state) => {
                state.errors[TTT_OPERATION_REYS.GET_READY_PLAYERS] = null;
            })
            .addCase(getReadyPlayers.fulfilled, (state, action) => {
                state.readyPlayersCount = action.payload;
            })
            .addCase(getReadyPlayers.rejected, (state, action) => {
                state.errors[TTT_OPERATION_REYS.GET_READY_PLAYERS] = action.payload ?? "Failed to fetch ready players";
            })

            /* === Get Players Bets === */
            .addCase(getPlayersBets.pending, (state) => {
                state.errors[TTT_OPERATION_REYS.GET_PLAYERS_BETS] = null;
            })
            .addCase(getPlayersBets.fulfilled, (state, action) => {
                state.playerBets = action.payload;
                const bets: Record<string, number> = {};

                action.payload.forEach(({ guid, bet }) => {
                    const username = state.players?.[guid];
                    if (username != null) {
                        bets[username] = bet;
                    }
                });

                state.playerBetMap = bets;
            })
            .addCase(getPlayersBets.rejected, (state, action) => {
                state.errors[TTT_OPERATION_REYS.GET_PLAYERS_BETS] = action.payload ?? "Failed to fetch player bets";
            });
    },
});

export const { clearError, clearAllErrors } = ticTacToeRoomSlice.actions;

export default ticTacToeRoomSlice.reducer;
