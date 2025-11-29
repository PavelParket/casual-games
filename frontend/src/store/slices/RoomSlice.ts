import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { RoomAPI } from "../../api/WsHubApi";
import type { AxiosError } from "axios";
import type { Room, RoomType } from "../../types/room";

export interface RoomState {
   rooms: Room[];
   types: RoomType[];
   isLoading: boolean;
   error: string | null;
}

export const fetchRooms = createAsyncThunk<Room[], void, { rejectValue: string }>(
   "rooms/fetchAll",
   async (_, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.getAllRooms();
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Failed to fetch rooms");
      }
   }
);

export const fetchTypes = createAsyncThunk<RoomType[], void, { rejectValue: string }>(
   "rooms/fetchTypes",
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

export const findTypeByRoomType = (types: RoomType[], roomName: string): RoomType | undefined => {
   return types.find(type => type.name === roomName);
};

const initialState: RoomState = {
   rooms: [],
   types: [],
   isLoading: false,
   error: null,
};

const roomSlice = createSlice({
   name: "rooms",
   initialState,
   reducers: {
      clearRooms: (state) => {
         state.rooms = [];
         state.error = null;
      },
   },
   extraReducers: (builder) => {
      builder

         /* === Rooms === */
         .addCase(fetchRooms.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(fetchRooms.fulfilled, (state, action) => {
            state.isLoading = false;
            state.rooms = action.payload;
         })
         .addCase(fetchRooms.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Failed to fetch rooms";
         })

         /* === Types === */
         .addCase(fetchTypes.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(fetchTypes.fulfilled, (state, action) => {
            state.isLoading = false;
            state.types = action.payload;
         })
         .addCase(fetchTypes.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Failed to fetch room types";
         });
   },
});

export const { clearRooms } = roomSlice.actions;
export default roomSlice.reducer;
