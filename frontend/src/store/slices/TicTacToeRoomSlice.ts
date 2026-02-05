import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { PlayerBet, Room, RoomType } from "../../models/Room";
import { RoomAPI, TicTacToeRoomApi } from "../../api/WsHubApi";
import type { AxiosError } from "axios";
import type { RootState } from "../store";

export interface TicTacToeRoomState {
    room?: Room;
    players?: Record<string, string>;
    readyPlayersCount?: number;
    totalPlayersCount?: number;
    playerBets?: PlayerBet[];
    playerBetMap?: Record<string, number>;
    error?: string;
}

// ------------------ Thunks ------------------

export const getRoomById = createAsyncThunk<Room, { roomId: string }, { rejectValue: string }>(
    "ticTacToeRoom/getRoom",
    async ({ roomId }, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.getRoomById(roomId);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch room");
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
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch usernames");
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
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch ready players");
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
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch ready players");
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
    players: undefined,
    readyPlayersCount: undefined,
    totalPlayersCount: undefined,
    playerBets: undefined,
    playerBetMap: {},
    error: undefined,
};

const ticTacToeRoomSlice = createSlice({
    name: "ticTacToeRoom",
    initialState,
    reducers: {
        clearError: (state) => {
            state.error = undefined;
        },
    },
    extraReducers: (builder) => {
        builder

            /* === Get Room === */
            .addCase(getRoomById.pending, (state) => {
                state.error = undefined;
            })
            .addCase(getRoomById.fulfilled, (state, action) => {
                state.room = action.payload;
            })
            .addCase(getRoomById.rejected, (state, action) => {
                state.error = action.payload ?? "Failed to fetch room";
            })

            /* === Get Players === */
            .addCase(getUsernamesInRoom.pending, (state) => {
                state.error = undefined;
            })
            .addCase(getUsernamesInRoom.fulfilled, (state, action) => {
                state.players = action.payload;
                state.totalPlayersCount = Object.keys(action.payload).length;
            })
            .addCase(getUsernamesInRoom.rejected, (state, action) => {
                state.error = action.payload ?? "Failed to fetch usernames";
            })

            /* === Get Ready Players === */
            .addCase(getReadyPlayers.pending, (state) => {
                state.error = undefined;
            })
            .addCase(getReadyPlayers.fulfilled, (state, action) => {
                state.readyPlayersCount = action.payload;
            })
            .addCase(getReadyPlayers.rejected, (state, action) => {
                state.error = action.payload ?? "Failed to fetch ready players";
            })

            /* === Get Players Bets === */
            .addCase(getPlayersBets.pending, (state) => {
                state.error = undefined;
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
                state.error = action.payload ?? "Failed to fetch player bets";
            });
    },
});

export const { clearError } = ticTacToeRoomSlice.actions;

export default ticTacToeRoomSlice.reducer;
