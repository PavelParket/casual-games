import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { HorseRaceGamePreset } from "../../models/HorseRace";
import type { Room, RoomType } from "../../models/Room";
import { HorseRaceRoomApi, RoomAPI } from "../../api/WsHubApi";
import type { AxiosError } from "axios";
import type { RootState } from "../store";

export interface HorseRaceRoomState {
    room?: Room;
    players?: Record<string, string>;
    readyPlayersCount?: number;
    totalPlayersCount?: number;
    preset?: HorseRaceGamePreset;
    error?: string;
}

// ------------------ Thunks ------------------

export const getRoomById = createAsyncThunk<Room, { roomId: string }, { rejectValue: string }>(
    "horseRaceRoom/getRoom",
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

export const getUsernamesInRoom = createAsyncThunk<Record<string, string>, { roomId: string; roomType: RoomType }, { rejectValue: string }>(
    "horseRaceRoom/getUsernamesInRoom",
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

export const getReadyPlayers = createAsyncThunk<number, { roomId: string; roomType: RoomType }, { rejectValue: string }>(
    "horseRaceRoom/getReadyPlayers",
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

export const getPreset = createAsyncThunk<HorseRaceGamePreset, { roomId: string }, { rejectValue: string }>(
    "horseRaceRoom/getPreset",
    async ({ roomId }, { rejectWithValue }) => {
        try {
            const response = await HorseRaceRoomApi.getPreset(roomId);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch race preset");
        }
    }
);

export const syncRoomState = createAsyncThunk<void, { roomId: string; roomType: RoomType }, { state: RootState }>(
    "horseRaceRoom/syncRoomState",
    async ({ roomId, roomType }, { dispatch }) => {
        await dispatch(getUsernamesInRoom({ roomId, roomType })).unwrap();
        await dispatch(getReadyPlayers({ roomId, roomType })).unwrap();
    }
);

export const syncReadiness = createAsyncThunk<void, { roomId: string; roomType: RoomType }>(
    "horseRaceRoom/syncReadiness",
    async ({ roomId, roomType }, { dispatch }) => {
        await dispatch(getReadyPlayers({ roomId, roomType })).unwrap();
    }
);

// ------------------ Slice ------------------

const initialState: HorseRaceRoomState = {
    room: undefined,
    players: undefined,
    readyPlayersCount: undefined,
    totalPlayersCount: undefined,
    preset: undefined,
    error: undefined,
};

const horseRaceRoomSlice = createSlice({
    name: "horseRaceRoom",
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

            /* === Get Preset === */
            .addCase(getPreset.pending, (state) => {
                state.error = undefined;
            })
            .addCase(getPreset.fulfilled, (state, action) => {
                state.preset = action.payload;
            })
            .addCase(getPreset.rejected, (state, action) => {
                state.error = action.payload ?? "Failed to fetch race preset";
            });
    },
});

export const { clearError } = horseRaceRoomSlice.actions;

export default horseRaceRoomSlice.reducer;
