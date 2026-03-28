import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { Room, RoomType } from "../../models/Room";
import { RoomAPI } from "../../api/WsHubApi";
import type { AxiosError } from "axios";

export interface DeCoderRoomState {
    room?: Room;
    players?: Record<string, string>;
    error?: string;
}

// ------------------ Thunks ------------------

export const getRoomById = createAsyncThunk<Room, { roomId: string }, { rejectValue: string }>(
    "deCoderRoom/getRoom",
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
    "deCoderRoom/getUsernamesInRoom",
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

// ------------------ Slice ------------------

const initialState: DeCoderRoomState = {
    room: undefined,
    players: undefined,
    error: undefined,
};

const deCoderRoomSlice = createSlice({
    name: "deCoderRoom",
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
            })
            .addCase(getUsernamesInRoom.rejected, (state, action) => {
                state.error = action.payload ?? "Failed to fetch usernames";
            })
    },
});

export const { clearError } = deCoderRoomSlice.actions;
export default deCoderRoomSlice.reducer;