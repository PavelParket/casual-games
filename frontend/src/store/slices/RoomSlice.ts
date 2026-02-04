import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { RoomAPI } from "../../api/WsHubApi";
import type { AxiosError } from "axios";
import type { Room, RoomRequest, RoomType } from "../../models/Room";

export interface RoomState {
   rooms?: Room[];
   roomTypes?: RoomType[];
   error?: string;
}

// ------------------ Thunks ------------------

export const getRooms = createAsyncThunk<Room[], void, { rejectValue: string }>(
   "rooms/getRooms",
   async (_, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.getRooms();
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Failed to fetch rooms");
      }
   }
);

export const getTypes = createAsyncThunk<RoomType[], void, { rejectValue: string }>(
   "rooms/getTypes",
   async (_, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.getTypes();
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Failed to fetch room types");
      }
   }
);

export const createRoom = createAsyncThunk<Room, RoomRequest, { rejectValue: string }>(
   "rooms/createRoom",
   async (roomRequest, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.createRoom(roomRequest);
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Failed to create room");
      }
   }
);

// ------------------ Slice ------------------

const initialState: RoomState = {
   rooms: [],
   roomTypes: [],
   error: undefined,
};

const roomSlice = createSlice({
   name: "rooms",
   initialState,
   reducers: {
      clearRooms: (state) => {
         state.rooms = [];
      },
      clearRoomTypes: (state) => {
         state.roomTypes = [];
      },
      clearError: (state) => {
         state.error = undefined;
      }
   },
   extraReducers: (builder) => {
      builder

         /* === Get Rooms === */
         .addCase(getRooms.pending, (state) => {
            state.error = undefined;
         })
         .addCase(getRooms.fulfilled, (state, action) => {
            state.rooms = action.payload;
         })
         .addCase(getRooms.rejected, (state, action) => {
            state.error = action.payload ?? "Failed to fetch rooms";
         })

         /* === Get Room Types === */
         .addCase(getTypes.pending, (state) => {
            state.error = undefined;
         })
         .addCase(getTypes.fulfilled, (state, action) => {
            state.roomTypes = action.payload;
         })
         .addCase(getTypes.rejected, (state, action) => {
            state.error = action.payload ?? "Failed to fetch room types";
         })

         /* === Create Room === */
         .addCase(createRoom.pending, (state) => {
            state.error = undefined;
         })
         .addCase(createRoom.fulfilled, (state, action) => {
            if (state.rooms) {
               state.rooms.push(action.payload);
            }
         })
         .addCase(createRoom.rejected, (state, action) => {
            state.error = action.payload ?? "Failed to create room";
         });
   },
});

export const { clearRooms, clearRoomTypes, clearError } = roomSlice.actions;

export default roomSlice.reducer;
